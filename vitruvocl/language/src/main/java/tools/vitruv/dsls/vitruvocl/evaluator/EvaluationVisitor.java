/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.evaluator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvocl.VitruvOCLBaseVisitor;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.*;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Phase 3 visitor that evaluates OCL expressions and produces runtime values.
 *
 * <p>This visitor implements the <b>evaluation phase</b> of the OCL compiler pipeline, operating
 * after symbol table construction (Phase 1) and type checking (Phase 2). It traverses the parse
 * tree and computes concrete runtime values ({@link Value} objects) for OCL expressions.
 *
 * <h2>Architecture</h2>
 *
 * The evaluator uses pre-computed type information from the type checking phase stored in {@code
 * nodeTypes} (a {@code ParseTreeProperty<Type>}) to perform type-dependent operations correctly.
 * It maintains a {@code receiverStack}
 * to handle method chaining (e.g., {@code collection.select(...).size()}) and uses the symbol table
 * for variable resolution.
 *
 * <h2>Error Handling</h2>
 *
 * Runtime errors (e.g., division by zero, type mismatches) are reported through the {@link
 * ErrorCollector} with source location information. The evaluator returns {@code
 * Value.empty(Type.ERROR)} for failed operations.
 *
 * @see Value The runtime value type representing OCL collections
 * @see AbstractPhaseVisitor Base class providing common visitor infrastructure
 */
public class EvaluationVisitor extends AbstractPhaseVisitor<Value> {

  private static final String PHASE_TAG = "evaluator";

  private static OCLElement unwrapNestedCollection(OCLElement e) {
    if (e instanceof OCLElement.NestedCollection nc && !nc.value().getElements().isEmpty()) {
      return nc.value().getElements().get(0);
    }
    return e;
  }

  // ==================== Instance Fields ====================

  /**
   * Pre-computed type information from Phase 2 (type checking).
   *
   * <p>Maps parse tree nodes to their statically determined types. Used for type-dependent
   * operations like determining collection element types for iterators.
   */
  private final ParseTreeProperty<Type> nodeTypes;

  /**
   * EObject instances that violated a constraint, in evaluation order.
   *
   * <p>Populated during {@link #visitClassifierContextCS}: one entry per instance where the
   * invariant evaluated to false. Used by callers to produce precise violation messages.
   */
  private final List<EObject> violatingInstances = new ArrayList<>();

  /**
   * Violation records produced during evaluation, one per violated invariant instance.
   *
   * @param severity  The @severity value (never null; defaults to "WARNING").
   * @param customMessage The interpolated @message template, or {@code null} when no @message
   *                      annotation was present on the invariant.
   * @param instance  The EObject that violated the constraint.
   */
  public record ViolationRecord(String severity, String customMessage, EObject instance) {}

  private final List<ViolationRecord> violationRecords = new ArrayList<>();

  /* Cache for allInstances() results to avoid redundant metamodel queries during evaluation. */
  private final java.util.Map<EClass, Value> allInstancesCache = new java.util.HashMap<>();

  /**
   * Stack of receiver values for navigation chains.
   *
   * <p>When evaluating {@code receiver.operation()}, the receiver value is pushed onto this stack
   * before visiting the operation node. This allows operation implementations to access their
   * receiver via {@code receiverStack.peek()}.
   *
   * <p><b>Example:</b> For {@code [1,2,3].select(x | x > 1).size()}, the stack evolves as:
   *
   * <ol>
   *   <li>Push [1,2,3] → visit select → pop → result [2,3]
   *   <li>Push [2,3] → visit size → pop → result 2
   * </ol>
   */
  private Deque<Value> receiverStack = new ArrayDeque<>();

  // ==================== Constructor ====================

  /**
   * Constructs an EvaluationVisitor for Phase 3 of the compilation pipeline.
   *
   * @param st The symbol table containing variable and type definitions
   * @param specification The metamodel wrapper providing access to model instances
   * @param errors The error collector for reporting runtime errors
   * @param nodeTypes Pre-computed type information from the type checking phase
   */
  public EvaluationVisitor(
      SymbolTable st,
      MetamodelWrapperInterface specification,
      ErrorCollector errors,
      ParseTreeProperty<Type> nodeTypes) {
    super(st, specification, errors);
    this.nodeTypes = nodeTypes;
  }

  // ==================== Error Reporting ====================

  /**
   * Reports an undefined variable error.
   *
   * <p>Called when attempting to resolve a variable that doesn't exist in the current scope.
   *
   * @param name The undefined variable name
   * @param ctx The parse tree context where the error occurred
   */
  @Override
  protected void handleUndefinedSymbol(String name, org.antlr.v4.runtime.ParserRuleContext ctx) {
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Variable not bound: " + name,
        ErrorSeverity.ERROR,
        PHASE_TAG);
  }

  // ==================== Context Declaration (Entry Point) ====================

  /**
   * Evaluates the top-level context declaration.
   *
   * <p>Processes all classifier contexts in the OCL document and returns the result of the last
   * evaluated constraint.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * context Person inv:
   *   self.age >= 0
   * context Company inv:
   *   self.employees->size() > 0
   * }</pre>
   *
   * @param ctx The context declaration node
   * @return The value of the last evaluated constraint, or empty if none
   */
  @Override
  public Value visitContextDeclCS(VitruvOCLParser.ContextDeclCSContext ctx) {
    Value lastResult = null;
    for (VitruvOCLParser.ClassifierContextCSContext classifierCtx : ctx.classifierContextCS()) {
      lastResult = visit(classifierCtx);
    }
    return lastResult != null ? lastResult : Value.empty(Type.ERROR);
  }

  // ==================== Classifier Context (Constraint Evaluation)
  // ====================

  /**
   * Evaluates a classifier context by iterating over all instances of the context type.
   *
   * <p>For each instance of the metaclass specified in the context (e.g., {@code context Person}),
   * this method:
   *
   * <ol>
   *   <li>Creates a new local scope
   *   <li>Binds {@code self} to the current instance
   *   <li>Evaluates all invariants in that scope
   *   <li>Collects results into a bag of boolean values
   * </ol>
   *
   * <p><b>Example:</b> If there are 3 Person instances and the constraint is {@code self.age >= 0},
   * this returns a bag of 3 boolean values, one for each instance.
   *
   * @param ctx The classifier context node
   * @return A bag containing one boolean result per instance
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Value visitClassifierContextCS(VitruvOCLParser.ClassifierContextCSContext ctx) {
    Type contextType = nodeTypes.get(ctx);

    if (contextType == null || contextType == Type.ERROR) {
      return Value.empty(Type.bag(Type.BOOLEAN));
    }

    EClass eClass = contextType.getEClass();
    if (eClass == null) {
      return Value.empty(Type.bag(Type.BOOLEAN));
    }

    // Retrieve all instances of the context type from the metamodel
    List<EObject> instances = specification.getAllInstances(eClass);
    List<OCLElement> allResults = new ArrayList<>();

    // Evaluate invariants for each instance
    for (EObject instance : instances) {
      // Create instance-specific scope
      Scope instanceScope = new LocalScope(symbolTable.getCurrentScope());
      symbolTable.enterScope(instanceScope);

      // Bind 'self' to current instance
      Value selfValue =
          Value.of(List.of(new OCLElement.MetaclassValue(instance)), Type.singleton(contextType));

      VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
      if (selfSymbol != null) {
        // Update existing 'self' binding
        selfSymbol.setValue(selfValue);
      } else {
        // Create new 'self' binding
        VariableSymbol newSelf =
            new VariableSymbol("self", contextType, symbolTable.getCurrentScope(), false);
        newSelf.setValue(selfValue);
        symbolTable.defineVariable(newSelf);
      }

      // Evaluate all invariants for this instance
      for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
        Value invResult = visit(inv);
        if (invResult != null && !invResult.isEmpty()) {
          OCLElement elem = invResult.getElements().get(0);
          allResults.add(elem);
          // Track violating instance for precise error reporting
          Boolean boolResult = elem.tryGetBool();
          if (boolResult == null || !boolResult) {
            violatingInstances.add(instance);
            String severity = extractSeverity(inv);
            String customMessage = extractCustomMessage(inv, instance);
            violationRecords.add(new ViolationRecord(severity, customMessage, instance));
          }
        }
      }

      symbolTable.exitScope();
    }

    return Value.of(allResults, Type.bag(Type.BOOLEAN));
  }

  /**
   * Returns EObject instances that violated the constraint during evaluation.
   *
   * <p>Each entry corresponds to one invariant evaluation that returned false. Call after visiting
   * the parse tree to retrieve violation context for precise error reporting.
   *
   * @return Unmodifiable list of violating EObjects in evaluation order
   */
  public List<EObject> getViolatingInstances() {
    return Collections.unmodifiableList(violatingInstances);
  }

  /** Returns violation records produced during the last evaluation, one per violated invariant instance. */
  public List<ViolationRecord> getViolationRecords() {
    return Collections.unmodifiableList(violationRecords);
  }

  private String extractSeverity(VitruvOCLParser.InvCSContext inv) {
    for (VitruvOCLParser.AnnotationCSContext ann : inv.annotationCS()) {
      if (ann instanceof VitruvOCLParser.SeverityAnnotationContext sev) {
        return sev.severityValue.getText();
      }
    }
    return "WARNING";
  }

  /**
   * Returns the interpolated {@code @message} template for the invariant, or {@code null} when no
   * {@code @message} annotation is present.
   */
  private String extractCustomMessage(VitruvOCLParser.InvCSContext inv, EObject instance) {
    for (VitruvOCLParser.AnnotationCSContext ann : inv.annotationCS()) {
      if (ann instanceof VitruvOCLParser.MessageAnnotationContext msg) {
        String raw = msg.message.getText();
        String template = raw.length() >= 2 ? raw.substring(1, raw.length() - 1) : raw;
        return interpolateTemplate(template, instance);
      }
    }
    return null;
  }

  @SuppressWarnings("java:S125")
  private String interpolateTemplate(String template, EObject instance) {
    // Replace {self.attr}
    java.util.regex.Matcher m =
        java.util.regex.Pattern.compile("\\{self\\.([a-zA-Z_]\\w*)\\}")
            .matcher(template);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String attr = m.group(1);
      org.eclipse.emf.ecore.EStructuralFeature feature =
          instance.eClass().getEStructuralFeature(attr);
      String val;
      if (feature == null) {
        val = "{" + attr + "}";
      } else {
        String raw = String.valueOf(instance.eGet(feature));
        boolean isString = feature.getEType() == org.eclipse.emf.ecore.EcorePackage.Literals.ESTRING;
        val = isString ? "\"" + raw + "\"" : raw;
      }
      m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(val));
    }
    m.appendTail(sb);
    // Replace bare {self}
    return sb.toString()
        .replace("{self}", instance.eClass().getName() + "@" + Integer.toHexString(instance.hashCode()));
  }

  // ==================== Annotations ====================

  @Override
  public Value visitSeverityAnnotation(VitruvOCLParser.SeverityAnnotationContext ctx) {
    return Value.boolValue(true);
  }

  @Override
  public Value visitMessageAnnotation(VitruvOCLParser.MessageAnnotationContext ctx) {
    return Value.boolValue(true);
  }

  // ==================== Control Flow ====================

  /**
   * Evaluates an if-then-else expression.
   *
   * <p>Evaluates the condition and returns the then-branch if true, else-branch if false.
   * Implements short-circuit evaluation (only one branch is evaluated).
   *
   * <p><b>Syntax:</b> {@code if <condition> then <thenBranch> else <elseBranch> endif}
   *
   * <p><b>Example:</b> {@code if age >= 18 then 'adult' else 'minor' endif}
   *
   * @param ctx The if-expression node
   * @return The value of the selected branch
   */
  @Override
  public Value visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    Value condVal = visit(ctx.condition);

    if (condVal == null || condVal.isEmpty()) {
      return visit(ctx.elseBranch);
    }

    OCLElement condElem = condVal.getElements().get(0);
    Boolean condition = condElem.tryGetBool();

    if (condition == null) {
      return visit(ctx.elseBranch);
    }

    // Short-circuit: only evaluate the selected branch
    return condition ? visit(ctx.thenBranch) : visit(ctx.elseBranch);
  }

  /**
   * Evaluates an invariant constraint.
   *
   * <p>Processes all specification expressions in the invariant and returns the result of the last
   * one.
   *
   * @param ctx The invariant node
   * @return The boolean result of the constraint evaluation
   */
  @Override
  public Value visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    List<VitruvOCLParser.SpecificationCSContext> specs = ctx.specificationCS();
    Value result = Value.boolValue(true);

    for (VitruvOCLParser.SpecificationCSContext spec : specs) {
      result = visit(spec);
    }

    return result;
  }

  // ==================== Collection Query Operations ====================

  /**
   * Evaluates the {@code isEmpty()} operation.
   *
   * <p>Returns true if the receiver collection contains zero elements.
   *
   * <p><b>Example:</b> {@code Bag{}.isEmpty()} → {@code true}
   *
   * @param ctx The isEmpty operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitIsEmptyOp(VitruvOCLParser.IsEmptyOpContext ctx) {
    Value receiver = receiverStack.peek();
    return Value.boolValue(receiver.isEmpty());
  }

  /**
   * Evaluates the {@code notEmpty()} operation.
   *
   * <p>Returns true if the receiver collection contains at least one element.
   *
   * <p><b>Example:</b> {@code Bag{1}.notEmpty()} → {@code true}
   *
   * @param ctx The notEmpty operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitNotEmptyOp(VitruvOCLParser.NotEmptyOpContext ctx) {
    Value receiver = receiverStack.peek();
    return Value.boolValue(!receiver.isEmpty());
  }

  /**
   * Evaluates the {@code first()} operation.
   *
   * <p>Returns the first element of an ordered collection, or empty if the collection is empty.
   *
   * <p><b>Example:</b> {@code Sequence{1,2,3}.first()} → {@code 1}
   *
   * @param ctx The first operation node
   * @return A singleton containing the first element, or empty
   */
  @Override
  public Value visitFirstOp(VitruvOCLParser.FirstOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) {
      return Value.empty(Type.optional(Type.ANY));
    }
    return new Value(List.of(receiver.getElements().get(0)), Type.optional(Type.ANY));
  }

  /**
   * Evaluates the {@code last()} operation.
   *
   * <p>Returns the last element of an ordered collection, or empty if the collection is empty.
   *
   * <p><b>Example:</b> {@code Sequence{1,2,3}.last()} → {@code 3}
   *
   * @param ctx The last operation node
   * @return A singleton containing the last element, or empty
   */
  @Override
  public Value visitLastOp(VitruvOCLParser.LastOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) {
      return Value.empty(Type.optional(Type.ANY));
    }
    int lastIndex = receiver.getElements().size() - 1;
    return new Value(List.of(receiver.getElements().get(lastIndex)), Type.optional(Type.ANY));
  }

  /**
   * Evaluates the {@code reverse()} operation.
   *
   * <p>Returns a new collection with elements in reverse order.
   *
   * <p><b>Example:</b> {@code Sequence{1,2,3}.reverse()} → {@code Sequence{3,2,1}}
   *
   * @param ctx The reverse operation node
   * @return A new collection with reversed element order
   */
  @Override
  public Value visitReverseOp(VitruvOCLParser.ReverseOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> reversed = new ArrayList<>(receiver.getElements());
    Collections.reverse(reversed);
    return Value.of(reversed, receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code size()} operation.
   *
   * <p>Returns the number of elements in the receiver collection.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.size()} → {@code 3}
   *
   * @param ctx The size operation node
   * @return A singleton integer value
   */
  @Override
  public Value visitSizeOp(VitruvOCLParser.SizeOpContext ctx) {
    Value receiver = receiverStack.peek();
    return Value.intValue(receiver.size());
  }

  // ==================== Collection Modification Operations ====================

  /**
   * Evaluates the {@code includes()} operation.
   *
   * <p>Returns true if the receiver collection contains an element semantically equal to the
   * argument.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.includes(2)} → {@code true}
   *
   * @param ctx The includes operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitIncludesOp(VitruvOCLParser.IncludesOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) {
      return Value.boolValue(false);
    }

    OCLElement searchElem = arg.getElements().get(0);
    boolean result = receiver.includes(searchElem);

    return Value.boolValue(result);
  }

  /**
   * Evaluates the {@code including()} operation.
   *
   * <p>Returns a new collection with the argument element added.
   *
   * <p><b>Example:</b> {@code Bag{1,2}.including(3)} → {@code Bag{1,2,3}}
   *
   * @param ctx The including operation node
   * @return A new collection with the element added
   */
  @Override
  public Value visitIncludingOp(VitruvOCLParser.IncludingOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) {
      return receiver;
    }
    return receiver.including(arg.getElements().get(0));
  }

  /**
   * Evaluates the {@code excluding()} operation.
   *
   * <p>Returns a new collection with all occurrences of the argument element removed.
   *
   * <p><b>Example:</b> {@code Bag{1,2,2,3}.excluding(2)} → {@code Bag{1,3}}
   *
   * @param ctx The excluding operation node
   * @return A new collection with the element removed
   */
  @Override
  public Value visitExcludingOp(VitruvOCLParser.ExcludingOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) {
      return receiver;
    }
    return receiver.excluding(arg.getElements().get(0));
  }

  /**
   * Evaluates the {@code excludes()} operation.
   *
   * <p>Returns true if the receiver collection does NOT contain the argument element.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.excludes(4)} → {@code true}
   *
   * @param ctx The excludes operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitExcludesOp(VitruvOCLParser.ExcludesOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) {
      return Value.boolValue(true);
    }
    return Value.boolValue(receiver.excludes(arg.getElements().get(0)));
  }

  /**
   * Evaluates the {@code union()} operation.
   *
   * <p>Returns a new collection containing all elements from both the receiver and argument
   * collections.
   *
   * <p><b>Example:</b> {@code Bag{1,2}.union(Bag{2,3})} → {@code Bag{1,2,2,3}}
   *
   * @param ctx The union operation node
   * @return A new collection with combined elements
   */
  @Override
  public Value visitUnionOp(VitruvOCLParser.UnionOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    return receiver.union(arg);
  }

  /**
   * Evaluates the {@code append()} operation.
   *
   * <p>Appends an element to the end of an ordered collection. Currently implemented as union.
   *
   * <p><b>Example:</b> {@code Sequence{1,2}.append(3)} → {@code Sequence{1,2,3}}
   *
   * @param ctx The append operation node
   * @return A new collection with the element appended
   */
  @Override
  public Value visitAppendOp(VitruvOCLParser.AppendOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    return receiver.union(arg);
  }

  /**
   * Evaluates the {@code flatten()} operation.
   *
   * <p>Flattens nested collections into a single-level collection.
   *
   * <p><b>Example:</b> {@code Bag{Bag{1,2}, Bag{3}}.flatten()} → {@code Bag{1,2,3}}
   *
   * @param ctx The flatten operation node
   * @return A flattened collection
   */
  @Override
  public Value visitFlattenOp(VitruvOCLParser.FlattenOpContext ctx) {
    Value receiver = receiverStack.peek();
    return receiver.flatten();
  }

  // ==================== Arithmetic Aggregation Operations ====================

  /**
   * Evaluates the {@code sum()} operation.
   *
   * <p>Returns the sum of all numeric elements. If all elements are integers, returns an Integer;
   * if any element is Float or Double, returns a Double.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.sum()} → {@code 6}, {@code Set{1.5,2.5}.sum()} → {@code
   * 4.0}
   *
   * @param ctx The sum operation node
   * @return A singleton integer or double value
   */
  @Override
  public Value visitSumOp(VitruvOCLParser.SumOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) {
      return Value.intValue(0);
    }
    boolean hasFloating = false;
    double sum = 0.0;
    for (OCLElement elem : receiver.getElements()) {
      if (!OCLElement.isNumeric(elem)) {
        continue;
      }
      if (elem.tryGetFloat() != null || elem.tryGetDouble() != null) {
        hasFloating = true;
      }
      sum += elem.toDoubleValue();
    }
    if (hasFloating) {
      return Value.doubleValue(sum);
    }
    return Value.intValue((int) sum);
  }

  /**
   * Evaluates the {@code max()} operation.
   *
   * <p>Returns the maximum integer value in the collection, or empty if the collection is empty.
   *
   * <p><b>Example:</b> {@code Bag{1,5,3}.max()} → {@code 5}
   *
   * @param ctx The max operation node
   * @return A singleton integer value, or empty
   */
  @Override
  public Value visitMaxOp(VitruvOCLParser.MaxOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) {
      return Value.empty(Type.INTEGER);
    }

    // Unwrap nested collections/singletons to get primitive values
    List<OCLElement> elements =
        receiver.getElements().stream()
            .map(e -> unwrapNestedCollection(e))
            .toList();

    double max = Double.NEGATIVE_INFINITY;
    for (OCLElement elem : elements) {
      if (!OCLElement.isNumeric(elem)) {
        continue;
      }
      double val = elem.toDoubleValue();
      if (val > max) max = val;
    }
    boolean allInt = elements.stream().allMatch(e -> e instanceof OCLElement.IntValue);
    return allInt ? Value.intValue((int) max) : Value.doubleValue(max);
  }

  /**
   * Evaluates the {@code min()} operation.
   *
   * <p>Returns the minimum integer value in the collection, or empty if the collection is empty.
   *
   * <p><b>Example:</b> {@code Bag{1,5,3}.min()} → {@code 1}
   *
   * @param ctx The min operation node
   * @return A singleton integer value, or empty
   */
  @Override
  public Value visitMinOp(VitruvOCLParser.MinOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) {
      return Value.empty(Type.INTEGER);
    }

    // Unwrap nested collections/singletons to get primitive values
    List<OCLElement> elements =
        receiver.getElements().stream()
            .map(e -> unwrapNestedCollection(e))
            .toList();

    double min = Double.POSITIVE_INFINITY;
    for (OCLElement elem : elements) {
      if (!OCLElement.isNumeric(elem)) {
        continue;
      }
      double val = elem.toDoubleValue();
      if (val < min) min = val;
    }

    boolean allInt = elements.stream().allMatch(e -> e instanceof OCLElement.IntValue);
    return allInt ? Value.intValue((int) min) : Value.doubleValue(min);
  }

  /**
   * Evaluates the {@code avg()} operation.
   *
   * <p>Returns the arithmetic mean of all elements as a Double. Supports Integer, Float, and Double
   * element types. Uses double arithmetic to avoid integer division truncation.
   *
   * <p><b>Example:</b> {@code Sequence{1,2,3,4}.avg()} → {@code 2.5}
   *
   * @param ctx The avg operation node
   * @return A singleton Double value, or empty if the collection is empty
   */
  @Override
  public Value visitAvgOp(VitruvOCLParser.AvgOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) {
      return Value.empty(Type.DOUBLE);
    }
    double sum = 0.0;
    for (OCLElement elem : receiver.getElements()) {
      if (!OCLElement.isNumeric(elem)) {
        continue;
      }
      sum += elem.toDoubleValue();
    }
    return Value.doubleValue(sum / receiver.size());
  }

  /**
   * Evaluates the {@code abs()} operation.
   *
   * <p>Returns the absolute value of each element. Supports Integer, Float, and Double receivers.
   *
   * <p><b>Example:</b> {@code (-3).abs()} → {@code 3}, {@code (-2.5).abs()} → {@code 2.5}
   *
   * @param ctx The abs operation node
   * @return A collection with absolute values
   */
  @Override
  public Value visitAbsOp(VitruvOCLParser.AbsOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (elem.tryGetInt() != null) {
        results.add(new OCLElement.IntValue(Math.abs(elem.tryGetInt())));
      } else if (elem.tryGetFloat() != null) {
        results.add(new OCLElement.FloatValue(Math.abs(elem.tryGetFloat())));
      } else if (elem.tryGetDouble() != null) {
        results.add(new OCLElement.DoubleValue(Math.abs(elem.tryGetDouble())));
      }
    }
    return Value.of(results, receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code floor()} operation.
   *
   * <p>For integers, this is a no-op. For Float and Double, applies {@link Math#floor}.
   *
   * <p><b>Example:</b> {@code 2.7.floor()} → {@code 2.0}, {@code 5.floor()} → {@code 5}
   *
   * @param ctx The floor operation node
   * @return A collection with floored values
   */
  @Override
  public Value visitFloorOp(VitruvOCLParser.FloorOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (elem.tryGetInt() != null) {
        results.add(elem); // no-op for integers
      } else if (elem.tryGetFloat() != null) {
        results.add(new OCLElement.DoubleValue(Math.floor(elem.tryGetFloat())));
      } else if (elem.tryGetDouble() != null) {
        results.add(new OCLElement.DoubleValue(Math.floor(elem.tryGetDouble())));
      }
    }
    return Value.of(results, receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code ceil()} operation.
   *
   * <p>For integers, this is a no-op. For Float and Double, applies {@link Math#ceil}.
   *
   * <p><b>Example:</b> {@code 2.3.ceil()} → {@code 3.0}, {@code 5.ceil()} → {@code 5}
   *
   * @param ctx The ceil operation node
   * @return A collection with ceiled values
   */
  @Override
  public Value visitCeilOp(VitruvOCLParser.CeilOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (elem.tryGetInt() != null) {
        results.add(elem); // no-op for integers
      } else if (elem.tryGetFloat() != null) {
        results.add(new OCLElement.DoubleValue(Math.ceil(elem.tryGetFloat())));
      } else if (elem.tryGetDouble() != null) {
        results.add(new OCLElement.DoubleValue(Math.ceil(elem.tryGetDouble())));
      }
    }
    return Value.of(results, receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code round()} operation.
   *
   * <p>For integers, this is a no-op. For Float and Double, rounds to the nearest integer using
   * "round half up" semantics ({@link Math#round}).
   *
   * <p><b>Example:</b> {@code 2.5.round()} → {@code 3.0}, {@code 2.4.round()} → {@code 2.0}
   *
   * @param ctx The round operation node
   * @return A collection with rounded values
   */
  @Override
  public Value visitRoundOp(VitruvOCLParser.RoundOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (elem.tryGetInt() != null) {
        results.add(elem); // no-op for integers
      } else if (elem.tryGetFloat() != null) {
        results.add(new OCLElement.DoubleValue(Math.round(elem.tryGetFloat())));
      } else if (elem.tryGetDouble() != null) {
        results.add(new OCLElement.DoubleValue(Math.round(elem.tryGetDouble())));
      }
    }
    return Value.of(results, receiver.getRuntimeType());
  }

  // ==================== String Operations ====================

  /**
   * Evaluates the {@code length()} operation on a String receiver.
   *
   * <p>Expects a singleton receiver containing a {@code String} value. Returns the number of
   * characters in the string as an {@code INTEGER} singleton.
   *
   * @param ctx the parse tree node for the {@code length()} operation
   * @return a singleton {@code INTEGER} value representing the string length, or an error value if
   *     the receiver is not a singleton {@code String}
   */
  @Override
  public Value visitLengthOp(VitruvOCLParser.LengthOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.size() == 1) {
      String str = receiver.getElements().get(0).tryGetString();
      if (str != null) {
        return Value.intValue(str.length());
      }
    }
    return Value.intValue(0);
  }

  /**
   * Evaluates the {@code concat()} operation.
   *
   * <p>Concatenates two strings together.
   *
   * <p><b>Example:</b> {@code 'Hello'.concat(' World')} → {@code 'Hello World'}
   *
   * @param ctx The concat operation node
   * @return A singleton string value
   */
  @Override
  public Value visitConcatOp(VitruvOCLParser.ConcatOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return receiver;

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return receiver;

    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) return receiver;

    String argStr = arg.getElements().get(0).tryGetString();
    if (argStr == null) return receiver;

    return Value.stringValue(str + argStr);
  }

  /**
   * Evaluates the substring operation on string values.
   *
   * <p>Extracts a substring from each string in the receiver collection using 1-based OCL indexing.
   * The operation takes two integer arguments: start position (inclusive) and end position
   * (exclusive in Java terms).
   *
   * <p>Example: "Hello".substring(2, 4) returns "el" (positions 2-3 in OCL's 1-based indexing)
   *
   * @param ctx The substring operation context containing start and end expressions
   * @return A collection of substring values, empty if arguments are invalid or out of bounds
   */
  @Override
  public Value visitSubstringOp(VitruvOCLParser.SubstringOpContext ctx) {
    // Get receiver from parent context
    Value receiver = receiverStack.peek();

    Value startVal = visit(ctx.start);
    Value endVal = visit(ctx.end);

    if (startVal.isEmpty() || endVal.isEmpty()) {
      return Value.of(List.of(), Type.STRING);
    }

    int start = ((OCLElement.IntValue) startVal.getElements().get(0)).value();
    int end = ((OCLElement.IntValue) endVal.getElements().get(0)).value();

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (elem instanceof OCLElement.StringValue strVal) {
        String str = strVal.value();
        int javaStart = start - 1;
        int javaEnd = end;

        if (javaStart >= 0 && javaEnd <= str.length() && javaStart < javaEnd) {
          results.add(new OCLElement.StringValue(str.substring(javaStart, javaEnd)));
        }
      }
    }

    return Value.of(results, Type.STRING);
  }

  /**
   * Evaluates the {@code toUpper()} operation.
   *
   * <p>Converts a string to uppercase.
   *
   * <p><b>Example:</b> {@code 'hello'.toUpper()} → {@code 'HELLO'}
   *
   * @param ctx The toUpper operation node
   * @return A singleton string value in uppercase
   */
  @Override
  public Value visitToUpperOp(VitruvOCLParser.ToUpperOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return receiver;

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return receiver;

    return Value.stringValue(str.toUpperCase());
  }

  /**
   * Evaluates the {@code toLower()} operation.
   *
   * <p>Converts a string to lowercase.
   *
   * <p><b>Example:</b> {@code 'HELLO'.toLower()} → {@code 'hello'}
   *
   * @param ctx The toLower operation node
   * @return A singleton string value in lowercase
   */
  @Override
  public Value visitToLowerOp(VitruvOCLParser.ToLowerOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return receiver;

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return receiver;

    return Value.stringValue(str.toLowerCase());
  }

  /**
   * Evaluates the {@code indexOf()} operation.
   *
   * <p>Returns the 1-based index of the first occurrence of a substring, or 0 if not found.
   *
   * <p><b>Example:</b> {@code 'hello'.indexOf('ll')} → {@code 3}
   *
   * @param ctx The indexOf operation node
   * @return A singleton integer value (1-based index, or 0 if not found)
   */
  @Override
  public Value visitIndexOfOp(VitruvOCLParser.IndexOfOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.intValue(0);

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return Value.intValue(0);

    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) return Value.intValue(0);

    String searchStr = arg.getElements().get(0).tryGetString();
    if (searchStr == null) return Value.intValue(0);

    // Convert Java's 0-based index to OCL's 1-based index
    int javaIndex = str.indexOf(searchStr);
    int oclIndex = (javaIndex == -1) ? 0 : javaIndex + 1;
    return Value.intValue(oclIndex);
  }

  /**
   * Evaluates the {@code equalsIgnoreCase()} operation.
   *
   * <p>Compares two strings ignoring case differences.
   *
   * <p><b>Example:</b> {@code 'Hello'.equalsIgnoreCase('HELLO')} → {@code true}
   *
   * @param ctx The equalsIgnoreCase operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitEqualsIgnoreCaseOp(VitruvOCLParser.EqualsIgnoreCaseOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.boolValue(false);

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return Value.boolValue(false);

    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) return Value.boolValue(false);

    String compareStr = arg.getElements().get(0).tryGetString();
    if (compareStr == null) return Value.boolValue(false);

    return Value.boolValue(str.equalsIgnoreCase(compareStr));
  }

  // ==================== Iterator Operations ====================

  /**
   * Evaluates the {@code select()} iterator operation.
   *
   * <p>Filters the collection, keeping only elements that satisfy the predicate.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3,4}.select(x | x > 2)} → {@code Bag{3,4}}
   *
   * @param ctx The select operation node
   * @return A filtered collection
   */
  @Override
  public Value visitSelectOp(VitruvOCLParser.SelectOpContext ctx) {
    Value receiver = receiverStack.peek();

    // Get iterator variables
    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.size() == 2) {
      return evaluateSelectTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    if (!iterVars.isEmpty()) {
      return evaluateSelectSingleVar(ctx, receiver, iterVars.get(0));
    }
    return Value.of(List.of(), receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code one()} iterator operation on a collection receiver.
   *
   * <p>Iterates over all elements of the receiver collection, binding each element to the iterator
   * variable in a fresh local scope, and evaluates the body expression. Returns {@code true} if and
   * only if exactly one element satisfies the condition.
   *
   * <p>Example: {@code employees->one(e | e.age > 60)}
   *
   * @param ctx the parse tree node for the {@code one()} operation, including the iterator variable
   *     list and body expression
   * @return a singleton {@code BOOLEAN} value — {@code true} if exactly one element satisfies the
   *     body condition, {@code false} otherwise; or an error value if no iterator variable is
   *     declared
   */
  @Override
  public Value visitOneOp(VitruvOCLParser.OneOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }
    if (iterVars.isEmpty()) return Value.boolValue(false);

    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);
    try {
      int count = 0;
      for (OCLElement elem : receiver.getElements()) {
        VariableSymbol iterSymbol =
            new VariableSymbol(iterVars.get(0), iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);
        Value bodyResult = visit(ctx.body);
        Boolean condition = bodyResult.getElements().get(0).tryGetBool();
        if (condition != null && condition) count++;
      }
      return Value.boolValue(count == 1);
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates the {@code any()} iterator operation on a collection receiver.
   *
   * <p>Iterates over the elements of the receiver collection, binding each element to the iterator
   * variable in a fresh local scope, and evaluates the body expression. Returns the first element
   * that satisfies the condition as an {@code Optional} singleton, or an empty value if no element
   * matches.
   *
   * <p>Example: {@code employees->any(e | e.age > 60)}
   *
   * @param ctx the parse tree node for the {@code any()} operation, including the iterator variable
   *     list and body expression
   * @return an {@code Optional} singleton containing the first matching element, or an empty {@code
   *     Optional} if no element satisfies the body condition; or an error value if no iterator
   *     variable is declared
   */
  @Override
  public Value visitAnyOp(VitruvOCLParser.AnyOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }
    if (iterVars.isEmpty()) return Value.empty(Type.optional(Type.ANY));

    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);
    try {
      for (OCLElement elem : receiver.getElements()) {
        VariableSymbol iterSymbol =
            new VariableSymbol(iterVars.get(0), iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);
        Value bodyResult = visit(ctx.body);
        Boolean condition = bodyResult.getElements().get(0).tryGetBool();
        if (condition != null && condition) {
          return new Value(List.of(elem), Type.optional(elemType));
        }
      }
      return Value.empty(Type.singleton(elemType));
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates the {@code isUnique()} iterator operation on a collection receiver.
   *
   * <p>Iterates over all elements of the receiver collection, binding each element to the iterator
   * variable in a fresh local scope, and evaluates the body expression. Returns {@code true} if and
   * only if all body results are mutually distinct, using {@link OCLElement#semanticEquals} for
   * value equality.
   *
   * <p>Example: {@code employees->isUnique(e | e.employeeId)}
   *
   * @param ctx the parse tree node for the {@code isUnique()} operation, including the iterator
   *     variable list and body expression
   * @return a singleton {@code BOOLEAN} value — {@code true} if all projected values are distinct,
   *     {@code false} as soon as a duplicate is detected; or an error value if no iterator variable
   *     is declared
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Value visitIsUniqueOp(VitruvOCLParser.IsUniqueOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }
    if (iterVars.isEmpty()) return Value.boolValue(true);

    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);
    try {
      List<Value> seen = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        VariableSymbol iterSymbol =
            new VariableSymbol(iterVars.get(0), iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);
        Value bodyResult = visit(ctx.body);
        for (Value s : seen) {
          if (s.getElements().size() == bodyResult.getElements().size()) {
            boolean allEqual = true;
            for (int i = 0; i < s.getElements().size(); i++) {
              if (!OCLElement.semanticEquals(
                  s.getElements().get(i), bodyResult.getElements().get(i))) {
                allEqual = false;
                break;
              }
            }
            if (allEqual) return Value.boolValue(false);
          }
        }
        seen.add(bodyResult);
      }
      return Value.boolValue(true);
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates the {@code sortedBy()} iterator operation on a collection receiver.
   *
   * <p>Projects each element of the receiver collection through the body expression to obtain a
   * sort key, then returns the elements in ascending key order. Key comparison uses {@link
   * OCLElement#compare}. The result is always an {@code OrderedSet} regardless of the receiver's
   * collection kind.
   *
   * <p>Example: {@code employees->sortedBy(e | e.lastName)}
   *
   * @param ctx the parse tree node for the {@code sortedBy()} operation, including the iterator
   *     variable list and body expression
   * @return an {@code OrderedSet} containing all receiver elements sorted in ascending order of
   *     their projected key values; or an error value if no iterator variable is declared
   */
  @Override
  public Value visitSortedByOp(VitruvOCLParser.SortedByOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }
    if (iterVars.isEmpty()) return Value.of(List.of(), Type.orderedSet(receiver.getRuntimeType().getElementType()));

    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);

    List<OCLElement> elements = new ArrayList<>(receiver.getElements());
    List<Value> keys = new ArrayList<>();

    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);
    try {
      for (OCLElement elem : elements) {
        VariableSymbol iterSymbol =
            new VariableSymbol(iterVars.get(0), iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);
        keys.add(visit(ctx.body));
      }
    } finally {
      symbolTable.exitScope();
    }

    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < elements.size(); i++) indices.add(i);
    indices.sort(
        (a, b) -> {
          OCLElement ka = keys.get(a).getElements().get(0);
          OCLElement kb = keys.get(b).getElements().get(0);
          return OCLElement.compare(ka, kb);
        });

    List<OCLElement> sorted = new ArrayList<>();
    for (int i : indices) sorted.add(elements.get(i));
    return Value.of(sorted, Type.orderedSet(elemType));
  }

  /**
   * Evaluates the {@code collectNested()} iterator operation on a collection receiver.
   *
   * <p>Projects each element of the receiver collection through the body expression and wraps each
   * result as a {@link OCLElement.NestedCollection}, preserving the collection structure of each
   * projected value rather than flattening it. This distinguishes {@code collectNested()} from
   * {@code collect()}, which flattens one level. The result is always a {@code Bag}.
   *
   * <p>Example: {@code teams->collectNested(t | t.members)}
   *
   * @param ctx the parse tree node for the {@code collectNested()} operation, including the
   *     iterator variable list and body expression
   * @return a {@code Bag} of {@link OCLElement.NestedCollection} wrappers, one per receiver
   *     element, each holding the unevaluated collection produced by the body expression; or an
   *     error value if no iterator variable is declared
   */
  @Override
  public Value visitCollectNestedOp(VitruvOCLParser.CollectNestedOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<String> iterVars = new ArrayList<>();
    for (TerminalNode id : ctx.iteratorVarList().ID()) {
      iterVars.add(id.getText());
    }
    if (iterVars.isEmpty()) return Value.of(List.of(), Type.bag(receiver.getRuntimeType().getElementType()));

    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);
    try {
      List<OCLElement> results = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        VariableSymbol iterSymbol =
            new VariableSymbol(iterVars.get(0), iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);
        Value bodyResult = visit(ctx.body);
        results.add(new OCLElement.NestedCollection(bodyResult));
      }
      return Value.of(results, Type.bag(elemType));
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates the {@code iterate()} operation on a collection receiver.
   *
   * <p>The most general collection iterator, from which all other iterator operations can be
   * derived. Initializes an accumulator variable to a given seed value, then iterates over each
   * element of the receiver collection, binding it to the iterator variable and evaluating the body
   * expression. The body result becomes the new accumulator value after each step. Returns the
   * final accumulator value.
   *
   * <p>Example: {@code numbers->iterate(n; acc : Integer = 0 | acc + n)}
   *
   * @param ctx the parse tree node for the {@code iterate()} operation, including the {@link
   *     VitruvOCLParser.IterateVarSpecContext} with iterator variable, accumulator variable,
   *     initializer expression, and body
   * @return the final value of the accumulator after all elements have been processed
   */
  @Override
  public Value visitIterateOp(VitruvOCLParser.IterateOpContext ctx) {
    Value receiver = receiverStack.peek();
    VitruvOCLParser.IterateVarSpecContext varSpec = ctx.iterateVarSpec();

    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);

    Value accValue = visit(varSpec.accInit);

    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);
    try {
      VariableSymbol accSymbol =
          new VariableSymbol(varSpec.accVar.getText(), accValue.getRuntimeType(), iterScope, false);
      accSymbol.setValue(accValue);
      symbolTable.defineVariable(accSymbol);

      for (OCLElement elem : receiver.getElements()) {
        VariableSymbol iterSymbol =
            new VariableSymbol(varSpec.iterVar.getText(), iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);

        Value bodyResult = visit(ctx.body);
        accSymbol.setValue(bodyResult);
      }
      return accSymbol.getValue();
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates the {@code reject()} iterator operation.
   *
   * <p>Filters the collection, keeping only elements that do NOT satisfy the predicate.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3,4}.reject(x | x > 2)} → {@code Bag{1,2}}
   *
   * @param ctx The reject operation node
   * @return A filtered collection
   */
  @Override
  public Value visitRejectOp(VitruvOCLParser.RejectOpContext ctx) {
    Value receiver = receiverStack.peek();

    // Get iterator variables
    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.size() == 2) {
      return evaluateRejectTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    if (!iterVars.isEmpty()) {
      return evaluateRejectSingleVar(ctx, receiver, iterVars.get(0));
    }
    return Value.of(List.of(), receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code collect()} iterator operation.
   *
   * <p>Transforms each element in the collection using the provided expression.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.collect(x | x * 2)} → {@code Bag{2,4,6}}
   *
   * @param ctx The collect operation node
   * @return A new collection with transformed elements
   */
  @Override
  public Value visitCollectOp(VitruvOCLParser.CollectOpContext ctx) {
    Value receiver = receiverStack.peek();

    // Get iterator variables
    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.size() == 2) {
      return evaluateCollectTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    if (!iterVars.isEmpty()) {
      return evaluateCollectSingleVar(ctx, receiver, iterVars.get(0));
    }
    return Value.of(List.of(), receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code forAll()} iterator operation.
   *
   * <p>Returns true if the predicate holds for ALL elements in the collection. Short-circuits on
   * first false.
   *
   * <p><b>Example:</b> {@code Bag{2,4,6}.forAll(x | x > 0)} → {@code true}
   *
   * @param ctx The forAll operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitForAllOp(VitruvOCLParser.ForAllOpContext ctx) {
    Value receiver = receiverStack.peek();

    // Get iterator variables
    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.size() == 2) {
      return evaluateForAllTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    if (!iterVars.isEmpty()) {
      return evaluateForAllSingleVar(ctx, receiver, iterVars.get(0));
    }
    return Value.boolValue(true);
  }

  /**
   * Evaluates the {@code exists()} iterator operation.
   *
   * <p>Returns true if the predicate holds for AT LEAST ONE element in the collection.
   * Short-circuits on first true.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.exists(x | x > 2)} → {@code true}
   *
   * @param ctx The exists operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitExistsOp(VitruvOCLParser.ExistsOpContext ctx) {
    Value receiver = receiverStack.peek();

    // Get iterator variables
    List<String> iterVars = new ArrayList<>();
    if (ctx.iteratorVars != null) {
      for (TerminalNode id : ctx.iteratorVarList().ID()) {
        iterVars.add(id.getText());
      }
    }

    if (iterVars.size() == 2) {
      return evaluateExistsTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    if (!iterVars.isEmpty()) {
      return evaluateExistsSingleVar(ctx, receiver, iterVars.get(0));
    }
    return Value.boolValue(false);
  }

  /**
   * Evaluates the {@code oclAsType()} operation.
   *
   * <p>Type cast operation that preserves the multiplicity semantics. The nodeTypes entry for this
   * node is now ¡targetType! (Singleton) after the TypeCheckVisitor fix — so we must unwrap through
   * isSingleton() as well as isCollection() to reach the bare MetaclassType.
   *
   * <p>Guard against infinite unwrapping: bare types return {@code this} from {@code
   * getElementType()}, so we stop as soon as the element type is the same object as the current
   * type.
   *
   * @param ctx The oclAsType operation node
   * @return The receiver elements re-wrapped with the target element type
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Value visitOclAsTypeOp(VitruvOCLParser.OclAsTypeOpContext ctx) {
    Value receiver = receiverStack.peek();
    Type targetType = nodeTypes.get(ctx);

    if (targetType == null) {
      return Value.of(receiver.getElements(), receiver.getRuntimeType());
    }

    // Unwrap to the bare member type: handles both {cad::Box} and !cad::Box!
    // nodeTypes now stores !cad::Box! for singleton receivers (TypeCheckVisitor
    // fix).
    // Guard: bare types return `this` from getElementType() — stop when no
    // progress.
    Type targetElemType = targetType;
    while ((targetElemType.isCollection() || targetElemType.isSingleton())
        && targetElemType.getElementType() != targetElemType) {
      targetElemType = targetElemType.getElementType();
    }

    // Primitive type cast: re-annotate with target element type, preserve
    // collection kind
    if (!targetElemType.isMetaclassType()) {
      Type resultType = preserveCollectionKind(receiver.getRuntimeType(), targetElemType);
      return Value.of(receiver.getElements(), resultType);
    }

    // Metaclass cast: validate each element via EMF inheritance
    EClass targetEClass = targetElemType.getEClass();
    List<OCLElement> results = new ArrayList<>();

    for (OCLElement elem : receiver.getElements()) {
      if (elem instanceof OCLElement.MetaclassValue mv) {
        EClass elemEClass = mv.instance().eClass();
        if (targetEClass.isSuperTypeOf(elemEClass) || elemEClass.equals(targetEClass)) {
          results.add(new OCLElement.CastedMetaclassValue(mv.instance(), targetEClass));
        }
        // else: element fails cast — filter it out
      } else if (elem instanceof OCLElement.CastedMetaclassValue cmv) {
        // Already casted — re-validate against new target
        EClass elemEClass = cmv.instance().eClass();
        if (targetEClass.isSuperTypeOf(elemEClass) || elemEClass.equals(targetEClass)) {
          results.add(new OCLElement.CastedMetaclassValue(cmv.instance(), targetEClass));
        }
        // else: element fails cast — filter it out
      }
      // else: non-metaclass element — filter it out
    }

    // Result type: singleton if receiver was singleton or bare metaclass,
    // collection otherwise
    Type resultType;
    if (receiver.getRuntimeType().isSingleton() || !receiver.getRuntimeType().isCollection()) {
      resultType = Type.singleton(targetElemType);
    } else {
      resultType = preserveCollectionKind(receiver.getRuntimeType(), targetElemType);
    }
    return Value.of(results, resultType);
  }

  /**
   * Evaluates variable references.
   *
   * <p>Handles the special {@code null} keyword, which evaluates to an empty optional value {@code
   * ?Any? = []} rather than looking up a variable. All other names are resolved from the symbol
   * table.
   *
   * @param ctx The variable expression node
   * @return The value bound to the variable, or empty optional for {@code null}
   */
  @Override
  public Value visitVariableExpCS(VitruvOCLParser.VariableExpCSContext ctx) {
    String varName = ctx.varName.getText();

    // null is not valid in OCL# — type checker already prevents reaching this
    if (varName.equals("null")) {
      return Value.empty(Type.optional(Type.ANY));
    }

    VariableSymbol varSymbol = symbolTable.resolveVariable(varName);
    if (varSymbol == null) {
      handleUndefinedSymbol(varName, ctx);
      return Value.empty(Type.ERROR);
    }

    Value value = varSymbol.getValue();
    if (value == null) {
      return Value.empty(Type.ANY);
    }

    return value;
  }

  /**
   * Evaluates forAll operation with a single iterator variable.
   *
   * <p>In each iterated element is a singleton ¡T!, so the iterator variable is bound with Value
   * type singleton(elemType) to match the TypeCheckVisitor's normalizeToSingleton() treatment of
   * iterator variables.
   *
   * @param ctx Parser context for forAll operation
   * @param receiver Collection to check
   * @param iterVar Name of iterator variable to bind
   * @return Singleton Boolean: true if all elements satisfy predicate, false if any fails
   */
  private Value evaluateForAllSingleVar(
      VitruvOCLParser.ForAllOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    // each iterated element is ¡T! — wrap to singleton so the variable type
    // matches what TypeCheckVisitor registered via normalizeToSingleton()
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element as singleton ¡T! value
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);

        Value bodyResult = visit(ctx.body);
        Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
            : bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) condition = Boolean.FALSE;

        // Short-circuit on first false result
        if (!condition) {
          return Value.boolValue(false);
        }
      }
      return Value.boolValue(true);
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates forAll operation with two iterator variables (Cartesian product).
   *
   * <p>Iterator variables are bound as singleton ¡T! per semantics.
   *
   * @param ctx Parser context for forAll operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Singleton Boolean: true if predicate holds for all pairs, false if any pair fails
   */
  private Value evaluateForAllTwoVars(
      VitruvOCLParser.ForAllOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables as singletons ¡T!
          VariableSymbol var1Symbol = new VariableSymbol(var1, iterVarType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), iterVarType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, iterVarType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), iterVarType));
          symbolTable.defineVariable(var2Symbol);

          Value bodyResult = visit(ctx.body);
          Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
              : bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) condition = Boolean.FALSE;

          // Short-circuit on first false result
          if (!condition) {
            return Value.boolValue(false);
          }
        }
      }
      return Value.boolValue(true);
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates select operation with a single iterator variable.
   *
   * <p>Iterator variable is bound as singleton ¡T! per semantics.
   *
   * @param ctx Parser context for select operation
   * @param receiver Collection to filter
   * @param iterVar Name of iterator variable to bind
   * @return Collection of elements satisfying the predicate
   */
  private Value evaluateSelectSingleVar(
      VitruvOCLParser.SelectOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element as singleton ¡T!
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);

        Value bodyResult = visit(ctx.body);
        Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
            : bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) condition = Boolean.FALSE;

        if (condition) {
          results.add(elem);
        }
      }
      return Value.of(results, receiver.getRuntimeType());
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates select operation with two iterator variables (Cartesian product).
   *
   * <p>Iterator variables are bound as singleton ¡T! per semantics.
   *
   * @param ctx Parser context for select operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Collection containing both elements of each matching pair
   */
  private Value evaluateSelectTwoVars(
      VitruvOCLParser.SelectOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables as singletons ¡T!
          VariableSymbol var1Symbol = new VariableSymbol(var1, iterVarType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), iterVarType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, iterVarType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), iterVarType));
          symbolTable.defineVariable(var2Symbol);

          Value bodyResult = visit(ctx.body);
          Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
              : bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) condition = Boolean.FALSE;

          if (condition) {
            results.add(elem1);
            results.add(elem2);
          }
        }
      }
      return Value.of(results, receiver.getRuntimeType());

    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates reject operation with a single iterator variable.
   *
   * <p>Iterator variable is bound as singleton ¡T! per semantics.
   *
   * @param ctx Parser context for reject operation
   * @param receiver Collection to filter
   * @param iterVar Name of iterator variable to bind
   * @return Collection of elements NOT satisfying the predicate
   */
  private Value evaluateRejectSingleVar(
      VitruvOCLParser.RejectOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element as singleton ¡T!
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);

        Value bodyResult = visit(ctx.body);
        Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
            : bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) condition = Boolean.FALSE;

        // Collect elements where predicate is false
        if (!condition) {
          results.add(elem);
        }
      }
      return Value.of(results, receiver.getRuntimeType());
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates reject operation with two iterator variables (Cartesian product).
   *
   * <p>Iterator variables are bound as singleton ¡T! per semantics.
   *
   * @param ctx Parser context for reject operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Collection containing both elements of each non-matching pair
   */
  private Value evaluateRejectTwoVars(
      VitruvOCLParser.RejectOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables as singletons ¡T!
          VariableSymbol var1Symbol = new VariableSymbol(var1, iterVarType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), iterVarType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, iterVarType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), iterVarType));
          symbolTable.defineVariable(var2Symbol);

          Value bodyResult = visit(ctx.body);
          Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
              : bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) condition = Boolean.FALSE;

          // Add pair if predicate is FALSE
          if (!condition) {
            results.add(elem1);
            results.add(elem2);
          }
        }
      }
      return Value.of(results, receiver.getRuntimeType());
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates collect operation with a single iterator variable.
   *
   * <p>Iterator variable is bound as singleton ¡T! per semantics.
   *
   * @param ctx Parser context for collect operation
   * @param receiver Collection to transform
   * @param iterVar Name of iterator variable to bind
   * @return Flattened collection of all transformation results
   */
  private Value evaluateCollectSingleVar(
      VitruvOCLParser.CollectOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element as singleton ¡T!
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);

        // Evaluate transformation and flatten all result elements into list
        Value bodyResult = visit(ctx.body);
        results.addAll(bodyResult.getElements());
      }
      Type resultType = nodeTypes.get(ctx);
      return Value.of(results, resultType != null ? resultType : Type.set(Type.ANY));
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates collect operation with two iterator variables (Cartesian product).
   *
   * <p>Iterator variables are bound as singleton ¡T! per semantics.
   *
   * @param ctx Parser context for collect operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Flattened collection of all transformation results from each pair
   */
  private Value evaluateCollectTwoVars(
      VitruvOCLParser.CollectOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables as singletons ¡T!
          VariableSymbol var1Symbol = new VariableSymbol(var1, iterVarType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), iterVarType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, iterVarType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), iterVarType));
          symbolTable.defineVariable(var2Symbol);

          // Evaluate transformation and flatten into results
          Value bodyResult = visit(ctx.body);
          results.addAll(bodyResult.getElements());
        }
      }
      Type resultType = nodeTypes.get(ctx);
      return Value.of(results, resultType != null ? resultType : Type.set(Type.ANY));
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates exists operation with a single iterator variable.
   *
   * <p>Iterator variable is bound as singleton ¡T!.
   *
   * @param ctx Parser context for exists operation
   * @param receiver Collection to iterate over
   * @param iterVar Name of iterator variable to bind
   * @return Singleton Boolean: true if any element satisfies predicate, false if none do
   */
  private Value evaluateExistsSingleVar(
      VitruvOCLParser.ExistsOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element as singleton ¡T!
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, iterVarType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), iterVarType));
        symbolTable.defineVariable(iterSymbol);

        Value bodyResult = visit(ctx.body);
        Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
            : bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) condition = Boolean.FALSE;

        // Short-circuit on first true result
        if (condition) {
          return Value.boolValue(true);
        }
      }
      return Value.boolValue(false);
    } finally {
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates exists operation with two iterator variables (Cartesian product).
   *
   * <p>Iterator variables are bound as singleton ¡T!.
   *
   * @param ctx Parser context for exists operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Singleton Boolean: true if any pair satisfies predicate, false if no pairs do
   */
  private Value evaluateExistsTwoVars(
      VitruvOCLParser.ExistsOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    Type iterVarType = Type.singleton(elemType);
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables as singletons ¡T!
          VariableSymbol var1Symbol = new VariableSymbol(var1, iterVarType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), iterVarType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, iterVarType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), iterVarType));
          symbolTable.defineVariable(var2Symbol);

          Value bodyResult = visit(ctx.body);
          Boolean condition = bodyResult.isEmpty() ? Boolean.FALSE
              : bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) condition = Boolean.FALSE;

          // Short-circuit on first true result
          if (condition) {
            return Value.boolValue(true);
          }
        }
      }
      return Value.boolValue(false);
    } finally {
      symbolTable.exitScope();
    }
  }

  // ==================== Arithmetic Operations ====================

  /**
   * Evaluates the {@code includesAll()} operation on a collection receiver.
   *
   * <p>Checks whether the receiver collection contains every element of the argument collection,
   * using {@link Value#includes} for element membership tests.
   *
   * <p>Example: {@code bag->includesAll(Set{1, 2, 3})}
   *
   * @param ctx the parse tree node for the {@code includesAll()} operation, including the argument
   *     expression
   * @return a singleton {@code BOOLEAN} value — {@code true} if every element of the argument
   *     collection is contained in the receiver, {@code false} as soon as any element is missing
   */
  @Override
  public Value visitIncludesAllOp(VitruvOCLParser.IncludesAllOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    for (OCLElement elem : arg.getElements()) {
      if (!receiver.includes(elem)) {
        return Value.boolValue(false);
      }
    }
    return Value.boolValue(true);
  }

  /**
   * Evaluates the {@code excludesAll()} operation on a collection receiver.
   *
   * <p>Checks whether the receiver collection contains none of the elements of the argument
   * collection, using {@link Value#includes} for membership tests.
   *
   * <p>Example: {@code bag->excludesAll(Set{1, 2, 3})}
   *
   * @param ctx the parse tree node for the {@code excludesAll()} operation, including the argument
   *     expression
   * @return a singleton {@code BOOLEAN} value — {@code true} if none of the argument's elements are
   *     contained in the receiver, {@code false} as soon as any argument element is found in the
   *     receiver
   */
  @Override
  public Value visitExcludesAllOp(VitruvOCLParser.ExcludesAllOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    for (OCLElement elem : arg.getElements()) {
      if (receiver.includes(elem)) {
        return Value.boolValue(false);
      }
    }
    return Value.boolValue(true);
  }

  /**
   * Evaluates the {@code count()} operation on a collection receiver.
   *
   * <p>Counts how many times the given singleton argument occurs in the receiver collection, using
   * {@link OCLElement#semanticEquals} for element equality.
   *
   * <p>Example: {@code Bag{1, 2, 2, 3}->count(2)}
   *
   * @param ctx the parse tree node for the {@code count()} operation, including the argument
   *     expression
   * @return a singleton {@code INTEGER} value representing the number of occurrences of the
   *     argument in the receiver collection; or an error value if the argument is not a singleton
   */
  @Override
  public Value visitCountOp(VitruvOCLParser.CountOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) {
      return Value.intValue(0);
    }
    OCLElement searchElem = arg.getElements().get(0);
    int count = 0;
    for (OCLElement elem : receiver.getElements()) {
      if (OCLElement.semanticEquals(elem, searchElem)) {
        count++;
      }
    }
    return Value.intValue(count);
  }

  /**
   * Evaluates the {@code prepend()} operation on a sequence receiver.
   *
   * <p>Inserts the given singleton argument at the front of the receiver collection. The result
   * type is taken from the type-checker annotation on the node if available, falling back to the
   * receiver's runtime type. If the result type enforces uniqueness (e.g. {@code OrderedSet}),
   * duplicates are removed after insertion.
   *
   * <p>Example: {@code Sequence{2, 3}->prepend(1)} yields {@code Sequence{1, 2, 3}}
   *
   * @param ctx the parse tree node for the {@code prepend()} operation, including the argument
   *     expression
   * @return a collection of the same kind as the receiver with the argument prepended as the first
   *     element, deduplicated if the type requires uniqueness; or an error value if the argument is
   *     not a singleton
   */
  @Override
  public Value visitPrependOp(VitruvOCLParser.PrependOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (arg.isEmpty()) {
      return receiver;
    }
    List<OCLElement> elements = new ArrayList<>();
    elements.add(arg.getElements().get(0));
    elements.addAll(receiver.getElements());
    Type resultType = nodeTypes.get(ctx);
    Value result = Value.of(elements, resultType != null ? resultType : receiver.getRuntimeType());
    if (result.getRuntimeType().isUnique()) {
      result = result.removeDuplicates();
    }
    return result;
  }

  /**
   * Evaluates the {@code insertAt()} operation on a sequence receiver.
   *
   * <p>Inserts the given singleton element at the specified 1-based index position into the
   * receiver collection. Valid index range is {@code [1, size+1]}. The result type is taken from
   * the type-checker annotation on the node if available, falling back to the receiver's runtime
   * type. If the result type enforces uniqueness (e.g. {@code OrderedSet}), duplicates are removed
   * after insertion.
   *
   * <p>Example: {@code Sequence{1, 3}->insertAt(2, 2)} yields {@code Sequence{1, 2, 3}}
   *
   * @param ctx the parse tree node for the {@code insertAt()} operation, including the index
   *     expression and the element argument expression
   * @return a collection of the same kind as the receiver with the argument inserted at the given
   *     position, deduplicated if the type requires uniqueness; or an error value if the index or
   *     argument is not a singleton, the index is not an {@code Integer}, or the index is out of
   *     bounds
   */
  @Override
  public Value visitInsertAtOp(VitruvOCLParser.InsertAtOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value indexVal = visit(ctx.index);
    Value arg = visit(ctx.arg);
    if (indexVal.isEmpty()) return receiver;
    Integer index = indexVal.getElements().get(0).tryGetInt();
    if (index == null) return receiver;
    if (arg.isEmpty()) return receiver;
    if (index < 1 || index > receiver.size() + 1) return receiver;
    List<OCLElement> elements = new ArrayList<>(receiver.getElements());
    elements.add(index - 1, arg.getElements().get(0));
    Type resultType = nodeTypes.get(ctx);
    Value result = Value.of(elements, resultType != null ? resultType : receiver.getRuntimeType());
    if (result.getRuntimeType().isUnique()) {
      result = result.removeDuplicates();
    }
    return result;
  }

  /**
   * Evaluates the {@code symmetricDifference()} operation on a collection receiver.
   *
   * <p>Returns a collection containing all elements that are in either the receiver or the argument
   * collection, but not in both — i.e. the set-theoretic symmetric difference. Membership is tested
   * via {@link Value#includes}. The result type is taken from the type-checker annotation on the
   * node if available, falling back to the receiver's runtime type. If the result type enforces
   * uniqueness (e.g. {@code Set}), duplicates are removed after construction.
   *
   * <p>Example: {@code Set{1, 2, 3}->symmetricDifference(Set{2, 3, 4})} yields {@code Set{1, 4}}
   *
   * @param ctx the parse tree node for the {@code symmetricDifference()} operation, including the
   *     argument expression
   * @return a collection containing all elements exclusive to either operand, deduplicated if the
   *     result type requires uniqueness
   */
  @Override
  public Value visitSymmetricDifferenceOp(VitruvOCLParser.SymmetricDifferenceOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (!arg.includes(elem)) {
        results.add(elem);
      }
    }
    for (OCLElement elem : arg.getElements()) {
      if (!receiver.includes(elem)) {
        results.add(elem);
      }
    }
    Type resultType = nodeTypes.get(ctx);
    Value result = Value.of(results, resultType != null ? resultType : receiver.getRuntimeType());
    if (result.getRuntimeType().isUnique()) {
      result = result.removeDuplicates();
    }
    return result;
  }

  /**
   * Evaluates the {@code div()} integer division operation.
   *
   * <p>Performs truncating integer division of the receiver by the argument. Both operands must be
   * singleton {@code INTEGER} values and the divisor must be non-zero.
   *
   * <p>Example: {@code 7->div(2)} yields {@code 3}
   *
   * @param ctx the parse tree node for the {@code div()} operation, including the argument
   *     expression
   * @return a singleton {@code INTEGER} value representing the truncated quotient; or an error
   *     value if either operand is not a singleton {@code Integer}, or the divisor is zero
   */
  @Override
  public Value visitDivOp(VitruvOCLParser.DivOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (receiver.isEmpty() || arg.isEmpty()) return Value.intValue(0);
    Integer left = receiver.getElements().get(0).tryGetInt();
    Integer right = arg.getElements().get(0).tryGetInt();
    if (left == null || right == null) return Value.intValue(0);
    if (right == 0) return Value.intValue(0);
    return Value.intValue(left / right);
  }

  /**
   * Evaluates the {@code mod()} integer remainder operation.
   *
   * <p>Computes the remainder of dividing the receiver by the argument using Java's {@code %}
   * operator. Both operands must be singleton {@code INTEGER} values. Division by zero yields
   * {@code 0} rather than an error, consistent with a null-free evaluation strategy.
   *
   * <p>Example: {@code 7->mod(3)} yields {@code 1}
   *
   * @param ctx the parse tree node for the {@code mod()} operation, including the argument
   *     expression
   * @return a singleton {@code INTEGER} value representing the remainder; {@code 0} if the divisor
   *     is zero; or an error value if either operand is not a singleton {@code Integer}
   */
  @Override
  public Value visitModOp(VitruvOCLParser.ModOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);
    if (receiver.isEmpty() || arg.isEmpty()) return Value.intValue(0);
    Integer left = receiver.getElements().get(0).tryGetInt();
    Integer right = arg.getElements().get(0).tryGetInt();
    if (left == null || right == null) return Value.intValue(0);
    if (right == 0) {
      return Value.intValue(0);
    }
    return Value.intValue(left % right);
  }

  /**
   * Evaluates multiplicative operations ({@code *} and {@code /}).
   *
   * <p>Numeric promotion (INTEGER ⊂ FLOAT ⊂ DOUBLE):
   *
   * <ul>
   *   <li>Both operands INTEGER with {@code *} → integer arithmetic
   *   <li>Both operands INTEGER with {@code /} → real arithmetic (DOUBLE)
   *   <li>Either operand FLOAT (and neither DOUBLE) → float arithmetic
   *   <li>Either operand DOUBLE → double arithmetic
   * </ul>
   *
   * <p>Integer division by zero is an error; floating-point division by zero produces ±Infinity
   * following IEEE 754.
   *
   * @param ctx The multiplicative operation node
   * @return A singleton numeric value
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Value visitMultiplicative(VitruvOCLParser.MultiplicativeContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);
    String operator = ctx.op.getText();

    if (leftValue.isEmpty() || rightValue.isEmpty()) return Value.empty(Type.INTEGER);

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    // ── Integer × Integer ──────────────────────────────────────────────────
    Integer leftInt = leftElem.tryGetInt();
    Integer rightInt = rightElem.tryGetInt();
    if (leftInt != null && rightInt != null) {
      if (operator.equals("/") && rightInt == 0) {
        return Value.empty(Type.DOUBLE);
      }
      if (operator.equals("/")) {
        return Value.doubleValue(((double) leftInt) / rightInt);
      }
      int result = leftInt * rightInt;
      return Value.intValue(result);
    }

    // ── Any numeric × Any numeric → promote to double ──────────────────────
    if (OCLElement.isNumeric(leftElem) && OCLElement.isNumeric(rightElem)) {
      double l = leftElem.toDoubleValue();
      double r = rightElem.toDoubleValue();
      double result = operator.equals("*") ? l * r : l / r;

      // If both operands are Float → stay Float (no Double contamination)
      if (leftElem instanceof OCLElement.FloatValue && rightElem instanceof OCLElement.FloatValue) {
        return Value.of(
            java.util.List.of(new OCLElement.FloatValue((float) result)),
            tools.vitruv.dsls.vitruvocl.typechecker.Type.FLOAT);
      }
      return Value.doubleValue(result);
    }

    return Value.empty(Type.INTEGER);
  }

  /**
   * Evaluates additive operations ({@code +} and {@code -}).
   *
   * <p>Numeric promotion (INTEGER ⊂ FLOAT ⊂ DOUBLE):
   *
   * <ul>
   *   <li>Both operands INTEGER → integer arithmetic
   *   <li>Either operand FLOAT (and neither DOUBLE) → float arithmetic
   *   <li>Either operand DOUBLE → double arithmetic
   * </ul>
   *
   * @param ctx The additive operation node
   * @return A singleton numeric value
   */
  @Override
  public Value visitAdditive(VitruvOCLParser.AdditiveContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);
    String operator = ctx.op.getText();

    if (leftValue.isEmpty() || rightValue.isEmpty()) return Value.empty(Type.INTEGER);

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    // ── Integer ± Integer ──────────────────────────────────────────────────
    Integer leftInt = leftElem.tryGetInt();
    Integer rightInt = rightElem.tryGetInt();
    if (leftInt != null && rightInt != null) {
      int result = operator.equals("+") ? leftInt + rightInt : leftInt - rightInt;
      return Value.intValue(result);
    }

    // ── Any numeric ± Any numeric → promote to double ──────────────────────
    if (OCLElement.isNumeric(leftElem) && OCLElement.isNumeric(rightElem)) {
      double l = leftElem.toDoubleValue();
      double r = rightElem.toDoubleValue();
      double result = operator.equals("+") ? l + r : l - r;

      // If both operands are Float → stay Float
      if (leftElem instanceof OCLElement.FloatValue && rightElem instanceof OCLElement.FloatValue) {
        return Value.of(
            java.util.List.of(new OCLElement.FloatValue((float) result)),
            tools.vitruv.dsls.vitruvocl.typechecker.Type.FLOAT);
      }
      return Value.doubleValue(result);
    }

    return Value.empty(Type.INTEGER);
  }

  // ==================== Comparison Operations ====================

  /**
   * Evaluates equality comparison (==). Both operands must be singletons for comparison.
   *
   * @param ctx the equality comparison context
   * @return singleton boolean Value with comparison result
   */
  @Override
  public Value visitEqualityComparison(VitruvOCLParser.EqualityComparisonContext ctx) {
    return evaluateBinaryComparison(
        ctx.left, ctx.right, ctx, OCLElement::semanticEquals);
  }

  /**
   * Evaluates inequality comparison (!=). Both operands must be singletons for comparison.
   *
   * @param ctx the inequality comparison context
   * @return singleton boolean Value with comparison result
   */
  @Override
  public Value visitInequalityComparison(VitruvOCLParser.InequalityComparisonContext ctx) {
    return evaluateBinaryComparison(
        ctx.left, ctx.right, ctx, (left, right) -> !OCLElement.semanticEquals(left, right));
  }

  /** Invalid operator sequence — type checker already reported the error; yield empty. */
  @Override
  public Value visitInvalidBinaryOp(VitruvOCLParser.InvalidBinaryOpContext ctx) {
    return Value.empty(Type.ERROR);
  }

  /** Typo of select/reject/exists with ~ syntax — type checker already reported the error. */
  @Override
  public Value visitUnknownCorrOp(VitruvOCLParser.UnknownCorrOpContext ctx) {
    return Value.empty(Type.ERROR);
  }

  /**
   * Evaluates less-than comparison. Both operands must be singletons for comparison.
   *
   * @param ctx the less-than comparison context
   * @return singleton boolean Value with comparison result
   */
  @Override
  public Value visitLessThanComparison(VitruvOCLParser.LessThanComparisonContext ctx) {
    return evaluateBinaryComparison(
        ctx.left, ctx.right, ctx, (left, right) -> OCLElement.compare(left, right) < 0);
  }

  /**
   * Evaluates less-than-or-equal comparison. Both operands must be singletons for comparison.
   *
   * @param ctx the less-than-or-equal comparison context
   * @return singleton boolean Value with comparison result
   */
  @Override
  public Value visitLessThanOrEqualComparison(
      VitruvOCLParser.LessThanOrEqualComparisonContext ctx) {
    return evaluateBinaryComparison(
        ctx.left, ctx.right, ctx, (left, right) -> OCLElement.compare(left, right) <= 0);
  }

  /**
   * Evaluates greater-than comparison. Both operands must be singletons for comparison.
   *
   * @param ctx the greater-than comparison context
   * @return singleton boolean Value with comparison result
   */
  @Override
  public Value visitGreaterThanComparison(VitruvOCLParser.GreaterThanComparisonContext ctx) {
    return evaluateBinaryComparison(
        ctx.left, ctx.right, ctx, (left, right) -> OCLElement.compare(left, right) > 0);
  }

  /**
   * Evaluates greater-than-or-equal comparison (>=). Both operands must be singletons for
   * comparison.
   *
   * @param ctx the greater-than-or-equal comparison context
   * @return singleton boolean Value with comparison result
   */
  @Override
  public Value visitGreaterThanOrEqualComparison(
      VitruvOCLParser.GreaterThanOrEqualComparisonContext ctx) {
    return evaluateBinaryComparison(
        ctx.left, ctx.right, ctx, (left, right) -> OCLElement.compare(left, right) >= 0);
  }

  /**
   * Helper method for evaluating binary comparisons. Extracts singleton operands and applies the
   * comparison function.
   *
   * @param leftCtx the left operand context
   * @param rightCtx the right operand context
   * @param errorCtx context for error reporting
   * @param comparisonFn the comparison function to apply
   * @return singleton boolean Value with comparison result
   */
  private Value evaluateBinaryComparison(
      ParserRuleContext leftCtx,
      ParserRuleContext rightCtx,
      ParserRuleContext errorCtx,
      java.util.function.BiPredicate<OCLElement, OCLElement> comparisonFn) {

    Value leftValue = visit(leftCtx);
    Value rightValue = visit(rightCtx);

    // Vacuously true if either operand is empty (no instances)
    if (leftValue.isEmpty() || rightValue.isEmpty()) {
      return Value.boolValue(true);
    }

    if (leftValue.size() != 1 || rightValue.size() != 1) {
      errors.add(
          errorCtx.getStart().getLine(),
          errorCtx.getStart().getCharPositionInLine(),
          "Comparison requires singleton operands",
          ErrorSeverity.ERROR,
          PHASE_TAG);
      return Value.boolValue(false);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    boolean result = comparisonFn.test(leftElem, rightElem);
    return Value.boolValue(result);
  }

  // ==================== Boolean Logic Operations ====================

  // ============================================================================
  // Logical operations (3 labeled alternatives)
  // ============================================================================

  /**
   * Evaluates logical AND operation. Both operands must be singleton booleans.
   *
   * <p><b>Example:</b> {@code true and false} → {@code false}
   *
   * @param ctx the logical AND context
   * @return singleton boolean Value with AND result
   */
  @Override
  public Value visitLogicalAnd(VitruvOCLParser.LogicalAndContext ctx) {
    return evaluateBinaryLogical(ctx.left, ctx.right, (left, right) -> left && right);
  }

  /**
   * Evaluates logical OR operation. Both operands must be singleton booleans.
   *
   * <p><b>Example:</b> {@code true or false} → {@code true}
   *
   * @param ctx the logical OR context
   * @return singleton boolean Value with OR result
   */
  @Override
  public Value visitLogicalOr(VitruvOCLParser.LogicalOrContext ctx) {
    return evaluateBinaryLogical(ctx.left, ctx.right, (left, right) -> left || right);
  }

  /**
   * Evaluates logical XOR operation. Both operands must be singleton booleans.
   *
   * <p><b>Example:</b> {@code true xor true} → {@code false}
   *
   * @param ctx the logical XOR context
   * @return singleton boolean Value with XOR result
   */
  @Override
  public Value visitLogicalXor(VitruvOCLParser.LogicalXorContext ctx) {
    return evaluateBinaryLogical(ctx.left, ctx.right, (left, right) -> left ^ right);
  }

  /**
   * Helper method for evaluating binary logical operations. Extracts singleton boolean operands and
   * applies the logical function.
   *
   * @param leftCtx the left operand context
   * @param rightCtx the right operand context
   * @param errorCtx context for error reporting
   * @param logicalFn the logical function to apply
   * @return singleton boolean Value with operation result
   */
  private Value evaluateBinaryLogical(
      ParserRuleContext leftCtx,
      ParserRuleContext rightCtx,
      java.util.function.BiPredicate<Boolean, Boolean> logicalFn) {

    Value leftValue = visit(leftCtx);
    Value rightValue = visit(rightCtx);

    if (leftValue.isEmpty() || rightValue.isEmpty()) return Value.boolValue(false);
    if (leftValue.size() != 1 || rightValue.size() != 1) return Value.boolValue(false);

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    Boolean left = leftElem.tryGetBool();
    Boolean right = rightElem.tryGetBool();

    if (left == null || right == null) return Value.boolValue(false);

    boolean result = logicalFn.test(left, right);
    return Value.boolValue(result);
  }

  /**
   * Evaluates the implication operation (implies).
   *
   * <p>Implements logical implication: {@code A implies B} is equivalent to {@code (not A) or B}.
   * Both operands must be singleton booleans.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code true implies true} → {@code true}
   *   <li>{@code true implies false} → {@code false}
   *   <li>{@code false implies true} → {@code true}
   *   <li>{@code false implies false} → {@code true}
   * </ul>
   *
   * @param ctx The implication operation node
   * @return A singleton boolean value
   */
  @Override
  public Value visitImplication(VitruvOCLParser.ImplicationContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);

    if (leftValue.isEmpty() || rightValue.isEmpty()) return Value.boolValue(true);
    if (leftValue.size() != 1 || rightValue.size() != 1) return Value.boolValue(true);

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    Boolean left = leftElem.tryGetBool();
    Boolean right = rightElem.tryGetBool();

    if (left == null || right == null) return Value.boolValue(true);

    // A implies B ≡ ¬A ∨ B
    boolean result = !left || right;
    return Value.boolValue(result);
  }

  // ==================== Unary Operations & Navigation ====================

  /**
   * Evaluates primary expression with navigation chain.
   *
   * <p>Processes base expression followed by zero or more navigation steps (property access, method
   * calls, iterator operations). Each navigation step uses the previous result as its receiver,
   * enabling method chaining like {@code spacecraft.payload.name}.
   *
   * @param ctx Parser context for primary expression with navigation
   * @return Final value after applying all navigation steps, or error Value if any step fails
   */
  @Override
  public Value visitPrimaryWithNav(VitruvOCLParser.PrimaryWithNavContext ctx) {
    // Evaluate base expression
    Value currentValue = visit(ctx.base);

    if (currentValue == null) {
      return Value.empty(Type.ANY);
    }

    // Process navigation chain
    List<VitruvOCLParser.NavigationChainCSContext> navChain = ctx.navigationChainCS();
    for (VitruvOCLParser.NavigationChainCSContext nav : navChain) {
      currentValue = visitNavigationWithReceiver(nav, currentValue);
    }

    return currentValue;
  }

  /**
   * Evaluates unary minus operation.
   *
   * <p>Implements OCL unary negation: requires singleton integer operand, returns negated value as
   * singleton collection following "everything is a collection" principle.
   *
   * @param ctx Parser context for unary minus expression
   * @return Singleton collection containing negated integer, or error Value if operand is
   *     non-singleton or non-integer
   */
  @Override
  public Value visitUnaryMinus(VitruvOCLParser.UnaryMinusContext ctx) {
    Value operandValue = visit(ctx.operand);

    if (operandValue == null || operandValue.isEmpty()) return Value.empty(Type.INTEGER);
    if (operandValue.size() != 1) return Value.empty(Type.INTEGER);

    OCLElement elem = operandValue.getElements().get(0);
    Double dblValue = elem.tryGetDouble();
    if (dblValue != null) {
      if (elem instanceof OCLElement.FloatValue) {
        return Value.of(
            java.util.List.of(new OCLElement.FloatValue((float) -dblValue)), Type.FLOAT);
      }
      return Value.doubleValue(-dblValue);
    }
    Integer value = elem.tryGetInt();
    if (value == null) return Value.empty(Type.INTEGER);

    return Value.intValue(-value);
  }

  /**
   * Evaluates logical not operation.
   *
   * <p>Implements OCL boolean negation: negates all boolean elements in the operand collection.
   * Supports both singleton and multi-element collections, returning a collection of the same size
   * with all values negated.
   *
   * @param ctx Parser context for logical not expression
   * @return Collection of negated boolean values, or error Value if any element is non-boolean
   */
  @Override
  public Value visitLogicalNot(VitruvOCLParser.LogicalNotContext ctx) {
    Value operandValue = visit(ctx.operand);

    if (operandValue == null) return Value.empty(Type.BOOLEAN);

    // Handle collection of booleans - negate all elements
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : operandValue.getElements()) {
      Boolean value = elem.tryGetBool();
      if (value == null) continue;
      results.add(new OCLElement.BoolValue(!value));
    }

    Type resultType = nodeTypes.get(ctx);
    return Value.of(results, resultType != null ? resultType : Type.BOOLEAN);
  }

  /**
   * Helper method to evaluate navigation with an explicit receiver value.
   *
   * <p>Delegates to either property access or operation call based on the navigation target type.
   *
   * @param ctx The navigation chain node
   * @param receiver The receiver value for the navigation
   * @return The result of the navigation
   */
  private Value visitNavigationWithReceiver(
      VitruvOCLParser.NavigationChainCSContext ctx, Value receiver) {

    VitruvOCLParser.NavigationTargetCSContext target = ctx.navigationTargetCS();
    // Use visitor pattern to dispatch based on navigation target type
    return target.accept(
        new VitruvOCLBaseVisitor<Value>() {
          @Override
          public Value visitPropertyNav(VitruvOCLParser.PropertyNavContext ctx) {
            return visitPropertyAccessWithReceiver(ctx.propertyAccess(), receiver);
          }

          @Override
          public Value visitOperationNav(VitruvOCLParser.OperationNavContext ctx) {
            VitruvOCLParser.OperationCallContext opCtx = ctx.operationCall();
            return visitOperationCallWithReceiver(opCtx, receiver);
          }

          @Override
          protected Value defaultResult() {
            return Value.empty(Type.ANY);
          }
        });
  }

  /**
   * Helper method to evaluate operation calls with an explicit receiver.
   *
   * <p>Pushes the receiver onto {@code receiverStack} before visiting the operation, then pops it
   * after. This allows operation visitor methods to access their receiver via {@code
   * receiverStack.peek()}.
   *
   * @param ctx The operation call node
   * @param receiver The receiver value for the operation
   * @return The result of the operation
   */
  private Value visitOperationCallWithReceiver(
      VitruvOCLParser.OperationCallContext ctx, Value receiver) {

    // Push receiver onto stack for operation to access
    receiverStack.push(receiver);
    try {
      return visitChildren(ctx);
    } finally {
      receiverStack.pop();
    }
  }

  // ==================== Metamodel Operations ====================

  /**
   * Evaluates the {@code allInstances()} operation.
   *
   * <p>Returns all instances of the receiver metaclass from the loaded models. This is the primary
   * operation for accessing model elements in constraints.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Person.allInstances()} → all Person instances
   *   <li>{@code spaceMission::Spacecraft.allInstances()} → all Spacecraft instances (qualified
   *       name)
   * </ul>
   *
   * @param ctx The allInstances operation node
   * @return A set of all instances of the metaclass
   */
  @Override
  public Value visitAllInstancesOp(VitruvOCLParser.AllInstancesOpContext ctx) {
    // Navigate up parse tree to find receiver type
    ParseTree nav = ctx.getParent().getParent().getParent();
    Type receiverType = nodeTypes.get(nav);

    if (receiverType == null) {
      ParseTree prefixed = nav.getParent();
      receiverType = nodeTypes.get(prefixed);
    }

    // Unwrap collection type if present
    if (receiverType != null && receiverType.isCollection()) {
      receiverType = receiverType.getElementType();
    }

    if (receiverType == null || !receiverType.isMetaclassType()) {
      return Value.empty(Type.set(Type.ANY));
    }

    EClass eClass = receiverType.getEClass();

    // Cache lookup
    if (allInstancesCache.containsKey(eClass)) {
      return allInstancesCache.get(eClass);
    }

    List<EObject> instances = specification.getAllInstances(eClass);
    List<OCLElement> elements =
        instances.stream().map(obj -> (OCLElement) new OCLElement.MetaclassValue(obj)).toList();

    Type resultType = nodeTypes.get(ctx);
    Value result = Value.of(elements, resultType != null ? resultType : Type.set(receiverType));

    // Cache result
    allInstancesCache.put(eClass, result);
    return result;
  }

  /**
   * Evaluates property access on metaclass instances.
   *
   * <p>Accesses EMF structural features (attributes and references) on model instances. For
   * multi-valued features, all values are flattened into the result collection.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code person.name} → singleton with person's name
   *   <li>{@code company.employees} → collection of all employees
   * </ul>
   *
   * @param ctx The property access node
   * @param receiver The receiver value (must contain metaclass instances)
   * @return Collection of property values from all receiver instances
   */
  @SuppressWarnings("java:S3776")
  private Value visitPropertyAccessWithReceiver(
      VitruvOCLParser.PropertyAccessContext ctx, Value receiver) {
    String propertyName = ctx.propertyName.getText();

    if (receiver.isEmpty()) {
      Type resultType = nodeTypes.get(ctx);
      return Value.empty(resultType != null ? resultType : Type.optional(Type.ANY));
    }

    // Validate that receiver contains metaclass instances
    EObject firstInstance = receiver.getElements().get(0).tryGetInstance();
    if (firstInstance == null) {
      Type resultType = nodeTypes.get(ctx);
      return Value.empty(resultType != null ? resultType : Type.optional(Type.ANY));
    }

    // Collect property values from all instances in receiver
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      EObject instance = elem.tryGetInstance();

      if (instance != null) {
        EStructuralFeature feature = instance.eClass().getEStructuralFeature(propertyName);

        if (feature != null) {
          Object value = instance.eGet(feature);

          // Handle multi-valued features (collections)
          if (feature.isMany()) {
            List<?> list = (List<?>) value;
            for (Object item : list) {
              if (item != null) results.add(wrapValue(item));
            }
          } else {
            if (value == null) continue; // unset optional attribute → ?T? = []
            results.add(wrapValue(value));
          }
        }
      }
    }

    Type resultType = nodeTypes.get(ctx);
    return Value.of(results, resultType != null ? resultType : Type.set(Type.ANY));
  }

  /**
   * Wraps a raw Java value from EMF's {@code eGet()} into an {@link OCLElement}.
   *
   * <p>Type mapping:
   *
   * <ul>
   *   <li>{@link String} → {@link OCLElement.StringValue}
   *   <li>{@link Integer} → {@link OCLElement.IntValue}
   *   <li>{@link Boolean} → {@link OCLElement.BoolValue}
   *   <li>{@link Float} → {@link OCLElement.FloatValue} (EMF {@code EFloat} attributes)
   *   <li>{@link Double} → {@link OCLElement.DoubleValue}
   *   <li>{@link EEnumLiteral} → {@link OCLElement.EnumValue}
   *   <li>{@link EObject} → {@link OCLElement.MetaclassValue}
   * </ul>
   *
   * @param value the raw Java value returned by {@code EObject.eGet(feature)}
   * @return the corresponding {@link OCLElement}
   * @throws RuntimeException if the value type is not supported
   */
  private OCLElement wrapValue(Object value) {
    if (value == null) {
      return new OCLElement.StringValue("");
    }

    Class<?> clazz = value.getClass();

    if (clazz.equals(String.class)) {
      return new OCLElement.StringValue((String) value);
    }
    if (clazz.equals(Integer.class)) {
      return new OCLElement.IntValue((Integer) value);
    }
    if (clazz.equals(Boolean.class)) {
      return new OCLElement.BoolValue((Boolean) value);
    }
    // EFloat is stored as Java Float — wrap as FloatValue, NOT DoubleValue
    if (clazz.equals(Float.class)) {
      return new OCLElement.FloatValue((Float) value);
    }
    if (clazz.equals(Double.class)) {
      return new OCLElement.DoubleValue((Double) value);
    }
    // EMF enum literals (EAttribute whose eType is an EEnum)
    if (value instanceof EEnumLiteral enumLit) {
      return new OCLElement.EnumValue(enumLit);
    }
    // Any other EObject (class instance from a metamodel)
    if (EObject.class.isAssignableFrom(clazz)) {
      return new OCLElement.MetaclassValue((EObject) value);
    }

    return new OCLElement.StringValue(value.toString());
  }

  // ==================== Type Checking Operations ====================

  /**
   * Evaluates the {@code oclIsKindOf()} operation.
   *
   * <p>Checks if each element in the receiver is an instance of the specified type or a subtype.
   * For primitive types, uses Java {@code instanceof} semantics. For metaclasses, uses EMF
   * inheritance.
   *
   * <p><b>Example:</b> {@code person.oclIsKindOf(Person)} → {@code true}
   *
   * @param ctx The oclIsKindOf operation node
   * @return A collection of boolean values, one per receiver element
   */
  @Override
  public Value visitOclIsKindOfOp(VitruvOCLParser.OclIsKindOfOpContext ctx) {
    Value receiver = receiverStack.peek();

    Type targetType = nodeTypes.get(ctx.type);

    if (targetType == null) {
      return Value.of(List.of(), Type.set(Type.BOOLEAN));
    }

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      boolean isKind = checkIsKindOf(elem, targetType);
      results.add(new OCLElement.BoolValue(isKind));
    }

    Type resultType = nodeTypes.get(ctx);
    return Value.of(results, resultType != null ? resultType : Type.set(Type.BOOLEAN));
  }

  /**
   * Evaluates the {@code oclIsTypeOf()} operation.
   *
   * <p>Currently implemented identically to {@code oclIsKindOf()}. Proper implementation would
   * check for exact type match (no subtypes).
   *
   * @param ctx The oclIsTypeOf operation node
   * @return A collection of boolean values, one per receiver element
   */
  @Override
  public Value visitOclIsTypeOfOp(VitruvOCLParser.OclIsTypeOfOpContext ctx) {
    Value receiver = receiverStack.peek();
    Type targetType = nodeTypes.get(ctx.type);

    if (targetType == null) {
      return Value.of(List.of(), Type.set(Type.BOOLEAN));
    }

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      results.add(new OCLElement.BoolValue(checkIsTypeOf(elem, targetType)));
    }

    Type resultType = nodeTypes.get(ctx);
    return Value.of(results, resultType != null ? resultType : Type.set(Type.BOOLEAN));
  }

  /** Preserves collection kind of receiver while changing element type. */
  private Type preserveCollectionKind(Type collectionType, Type newElementType) {
    if (collectionType.isUnique() && collectionType.isOrdered()) {
      return Type.orderedSet(newElementType);
    } else if (collectionType.isUnique()) {
      return Type.set(newElementType);
    } else if (collectionType.isOrdered()) {
      return Type.sequence(newElementType);
    } else {
      return Type.bag(newElementType);
    }
  }

  /**
   * Helper method to check if an element is of a given type.
   *
   * <p>Implements type checking for both primitive types (Integer, String, Boolean, Double) and
   * metaclass types (using EMF inheritance).
   *
   * @param elem The element to check
   * @param targetType The target type
   * @return true if the element is of the target type (or a subtype)
   */
  private boolean checkIsKindOf(OCLElement elem, Type targetType) {
    if (targetType == Type.INTEGER) {
      return elem.tryGetInt() != null;
    } else if (targetType == Type.STRING) {
      return elem.tryGetString() != null;
    } else if (targetType == Type.BOOLEAN) {
      return elem.tryGetBool() != null;
    } else if (targetType == Type.DOUBLE) {
      return elem.tryGetDouble() != null;
    } else if (targetType == Type.FLOAT) {
      return elem.tryGetFloat() != null || elem.tryGetDouble() != null || elem.tryGetInt() != null;
    }

    if (targetType.isMetaclassType()) {
      EClass targetEClass = targetType.getEClass();
      EClass elemEClass = elem.getEClass();

      if (elemEClass == null) {
        return false;
      }

      return targetEClass.isSuperTypeOf(elemEClass) || elemEClass.equals(targetEClass);
    }

    return false;
  }

  /**
   * Helper method to check if an element is of a given type.
   *
   * <p>Implements type checking for both primitive types (Integer, String, Boolean, Double) and
   * metaclass types (using EMF inheritance).
   *
   * @param elem The element to check
   * @param targetType The target type
   * @return true if the element is of the exact target type
   */
  private boolean checkIsTypeOf(OCLElement elem, Type targetType) {
    if (targetType == Type.INTEGER) {
      return elem.tryGetInt() != null;
    } else if (targetType == Type.STRING) {
      return elem.tryGetString() != null;
    } else if (targetType == Type.BOOLEAN) {
      return elem.tryGetBool() != null;
    } else if (targetType == Type.DOUBLE) {
      return elem.tryGetDouble() != null;
    } else if (targetType == Type.FLOAT) {
      return elem.tryGetFloat() != null;
    }

    if (targetType.isMetaclassType()) {
      EClass targetEClass = targetType.getEClass();
      EClass elemEClass = elem.getEClass();
      if (elemEClass == null) {
        return false;
      }
      return elemEClass.equals(targetEClass);
    }

    return false;
  }

  // ==================== Cross-Metamodel Support ====================

  /**
   * Evaluates fully qualified metaclass references (e.g., {@code spaceMission::Spacecraft}).
   *
   * <p>This is the entry point for cross-metamodel constraints, allowing references to metaclasses
   * from different metamodels using qualified names.
   *
   * <p><b>Example:</b> {@code satelliteSystem::Satellite.allInstances()}
   *
   * @param ctx The prefixed qualified name node
   * @return The result of evaluating navigation on the qualified type
   */
  @Override
  public Value visitPrefixedQualified(VitruvOCLParser.PrefixedQualifiedContext ctx) {
    String metamodel = ctx.metamodel.getText();
    String className = ctx.className.getText();
    String qualifiedName = metamodel + "::" + className;

    // First try: enum literal (EnumName::LiteralName)
    org.eclipse.emf.ecore.EEnum eEnum = specification.resolveEEnum(metamodel);
    if (eEnum != null) {
      org.eclipse.emf.ecore.EEnumLiteral literal = eEnum.getEEnumLiteral(className);
      if (literal != null) {
        return new Value(List.of(new OCLElement.EnumValue(literal)), Type.enumType(eEnum));
      }
    }

    // Second try: metaclass type (metamodel::ClassName)
    Type metaclassType = symbolTable.lookupType(qualifiedName);
    if (metaclassType == null || metaclassType == Type.ERROR) {
      return Value.empty(Type.set(Type.ANY));
    }

    Value currentValue = Value.of(List.of(), metaclassType);
    for (VitruvOCLParser.NavigationChainCSContext nav : ctx.navigationChainCS()) {
      currentValue = visitNavigationWithReceiver(nav, currentValue);
    }
    return currentValue;
  }

  // ==================== Collection Literals ====================

  /**
   * Evaluates collection literal expressions.
   *
   * <p>Creates runtime collections from literal syntax like {@code Bag{1,2,3}} or {@code
   * Set{'a','b'}}.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Bag{1,2,3}} → bag with 3 elements
   *   <li>{@code Set{1,2,2}} → set with 2 elements (duplicates removed)
   *   <li>{@code Sequence{1..5}} → sequence from 1 to 5
   * </ul>
   *
   * @param ctx The collection literal node
   * @return The evaluated collection value
   */
  @Override
  public Value visitCollectionLiteralExpCS(VitruvOCLParser.CollectionLiteralExpCSContext ctx) {
    Type collectionType = nodeTypes.get(ctx);

    if (ctx.arguments == null) {
      return Value.empty(collectionType);
    }

    Value argumentsValue = visit(ctx.arguments);
    List<OCLElement> elements = argumentsValue.getElements();

    Value collection = Value.of(elements, collectionType);

    // Remove duplicates for Set types
    if (collectionType.isUnique()) {
      collection = collection.removeDuplicates();
    }
    return collection;
  }

  /**
   * Evaluates collection literal arguments.
   *
   * <p>Processes the elements inside collection literal braces, handling both individual values and
   * ranges.
   *
   * @param ctx The collection arguments node
   * @return A collection containing all argument elements
   */
  @Override
  public Value visitCollectionArguments(VitruvOCLParser.CollectionArgumentsContext ctx) {
    List<OCLElement> elements = new ArrayList<>();

    for (VitruvOCLParser.CollectionLiteralPartCSContext partCtx : ctx.collectionLiteralPartCS()) {
      Value partValue = visit(partCtx);
      // Range syntax (1..5) always flattens
      if (partCtx.expCS().size() == 2) {
        elements.addAll(partValue.getElements());
      } else if (partValue.getRuntimeType().isCollection()) {
        // Nested collection literal — keep as single element
        elements.add(new OCLElement.NestedCollection(partValue));
      } else {
        elements.addAll(partValue.getElements());
      }
    }

    return new Value(elements, Type.ANY);
  }

  /**
   * Evaluates the {@code ceiling()} operation on a numeric receiver.
   *
   * <p>Applies {@link Math#ceil} element-wise to the receiver collection. {@code INTEGER} elements
   * are returned unchanged; {@code FLOAT} and {@code DOUBLE} elements are rounded up to the nearest
   * integer value and returned as {@code DOUBLE}. The result preserves the receiver's collection
   * kind and multiplicity.
   *
   * <p>Example: {@code 2.3->ceiling()} yields {@code 3.0}
   *
   * @param ctx the parse tree node for the {@code ceiling()} operation
   * @return a collection of the same kind as the receiver with each element rounded up to the
   *     nearest integer value; or an error value if any element is not numeric
   */
  @Override
  public Value visitCeilingOp(VitruvOCLParser.CeilingOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (elem.tryGetInt() != null) {
        results.add(elem);
      } else if (elem.tryGetFloat() != null) {
        results.add(new OCLElement.DoubleValue(Math.ceil(elem.tryGetFloat())));
      } else if (elem.tryGetDouble() != null) {
        results.add(new OCLElement.DoubleValue(Math.ceil(elem.tryGetDouble())));
      }
    }
    return Value.of(results, receiver.getRuntimeType());
  }

  /**
   * Evaluates a single collection literal part (element or range).
   *
   * <p>Handles:
   *
   * <ul>
   *   <li>Single values: {@code 42}
   *   <li>Ranges: {@code 1..10} (generates all integers in the range)
   * </ul>
   *
   * @param ctx The collection literal part node
   * @return The element(s) represented by this part
   */
  @Override
  @SuppressWarnings("java:S3776")
  public Value visitCollectionLiteralPartCS(VitruvOCLParser.CollectionLiteralPartCSContext ctx) {
    Value firstValue = visit(ctx.expCS(0));

    // Range syntax: start..end
    if (ctx.expCS().size() == 2) {
      Value secondValue = visit(ctx.expCS(1));

      if (firstValue.isEmpty() || secondValue.isEmpty()) return Value.empty(Type.INTEGER);
      if (firstValue.size() != 1 || secondValue.size() != 1) return Value.empty(Type.INTEGER);

      OCLElement firstElem = firstValue.getElements().get(0);
      OCLElement secondElem = secondValue.getElements().get(0);

      Integer start = firstElem.tryGetInt();
      Integer end = secondElem.tryGetInt();

      if (start == null || end == null) return Value.empty(Type.INTEGER);

      // Generate range (supports both ascending and descending)
      List<OCLElement> range = new ArrayList<>();
      if (start <= end) {
        for (int i = start; i <= end; i++) {
          range.add(new OCLElement.IntValue(i));
        }
      } else {
        for (int i = start; i >= end; i--) {
          range.add(new OCLElement.IntValue(i));
        }
      }

      return new Value(range, Type.INTEGER);
    }

    return firstValue;
  }

  // ==================== Advanced Collection Operations ====================

  /**
   * Evaluates the {@code lift()} operation.
   *
   * <p>Wraps the receiver collection in a nested collection structure. Used for creating
   * collections of collections.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.lift()} → {@code Bag{Bag{1,2,3}}}
   *
   * @param ctx The lift operation node
   * @return A singleton containing the receiver as a nested collection
   */
  @Override
  public Value visitLiftOp(VitruvOCLParser.LiftOpContext ctx) {
    Value receiver = receiverStack.peek();
    OCLElement.NestedCollection wrapped = new OCLElement.NestedCollection(receiver);
    Type outerType = nodeTypes.get(ctx);
    return Value.of(
        List.of(wrapped), outerType != null ? outerType : Type.set(receiver.getRuntimeType()));
  }

  // ==================== Literal Values ====================

  /**
   * Evaluates integer literals.
   *
   * <p><b>Example:</b> {@code 42} → singleton integer value
   *
   * @param ctx The number literal node
   * @return A singleton integer value
   */
  @Override
  public Value visitNumberLit(VitruvOCLParser.NumberLitContext ctx) {
    String text = ctx.getText();
    if (text.contains(".") || text.contains("e") || text.contains("E")) {
      return Value.doubleValue(Double.parseDouble(text));
    }
    return Value.intValue(Integer.parseInt(text));
  }

  /**
   * Evaluates string literals.
   *
   * <p><b>Example:</b> {@code 'hello'} → singleton string value
   *
   * @param ctx The string literal node
   * @return A singleton string value
   */
  @Override
  public Value visitStringLit(VitruvOCLParser.StringLitContext ctx) {
    String text = ctx.getText();
    String value = text.substring(1, text.length() - 1); // Remove quotes
    return Value.stringValue(value);
  }

  /**
   * Evaluates boolean literals.
   *
   * <p><b>Example:</b> {@code true} → singleton boolean value
   *
   * @param ctx The boolean literal node
   * @return A singleton boolean value
   */
  @Override
  public Value visitBooleanLit(VitruvOCLParser.BooleanLitContext ctx) {
    String text = ctx.getText();
    boolean value = text.equals("true");
    return Value.boolValue(value);
  }

  // ==================== Variable Handling ====================

  /**
   * Evaluates {@code self} references.
   *
   * <p>Returns the value bound to the special {@code self} variable, which represents the current
   * context instance in invariants.
   *
   * @param ctx The self expression node
   * @return The current context instance
   */
  @Override
  public Value visitSelfExpCS(VitruvOCLParser.SelfExpCSContext ctx) {
    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");

    if (selfSymbol == null) {
      return Value.empty(Type.ANY);
    }

    Value selfValue = selfSymbol.getValue();

    if (selfValue == null) {
      return Value.empty(Type.ANY);
    }

    return selfValue;
  }

  /**
   * Evaluates variable declarations in let expressions.
   *
   * <p>Binds the variable to its initializer value in the symbol table.
   *
   * @param ctx The variable declaration node
   * @return The initializer value
   */
  @Override
  public Value visitVariableDeclaration(VitruvOCLParser.VariableDeclarationContext ctx) {
    String varName = ctx.varName.getText();
    Value initValue = visit(ctx.varInit);

    if (initValue == null) {
      return Value.empty(Type.ANY);
    }

    Type varType = nodeTypes.get(ctx);
    if (varType == null) {
      varType = nodeTypes.get(ctx.varInit);
      if (varType == null) {
        varType = Type.ANY;
      }
    }

    Scope currentScope = symbolTable.getCurrentScope();
    VariableSymbol varSymbol = new VariableSymbol(varName, varType, currentScope, false);
    varSymbol.setValue(initValue);
    symbolTable.defineVariable(varSymbol);

    return initValue;
  }

  // ==================== Nested Expressions ====================

  /**
   * Evaluates nested (parenthesized) expressions.
   *
   * <p><b>Example:</b> {@code (1 + 2) * 3} - the {@code (1 + 2)} part is a nested expression.
   *
   * @param ctx The nested expression node
   * @return The value of the last expression in the parentheses
   */
  @Override
  public Value visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
    List<VitruvOCLParser.ExpCSContext> exps = ctx.expCS();

    if (exps.isEmpty()) {
      return Value.empty(Type.ANY);
    }

    Value result = null;
    for (VitruvOCLParser.ExpCSContext exp : exps) {
      result = visit(exp);
    }

    return result;
  }

  // ==================== Delegation & Default Implementations
  // ====================

  /**
   * Evaluates specification nodes (OCL constraint bodies).
   *
   * @param ctx The specification node
   * @return The value of the last expression
   */
  @Override
  public Value visitSpecificationCS(VitruvOCLParser.SpecificationCSContext ctx) {
    Value result = null;
    for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
      result = visit(exp);
    }
    return result != null ? result : Value.empty(Type.ERROR);
  }

  /** Delegates to infixedExpCS. */
  @Override
  public Value visitExpCS(VitruvOCLParser.ExpCSContext ctx) {
    return visit(ctx.infixedExpCS());
  }

  /** Delegates to prefixedExpCS. */
  @Override
  public Value visitPrefixedExpr(VitruvOCLParser.PrefixedExprContext ctx) {
    return visit(ctx.prefixedExpCS());
  }

  /** Delegates to literalExpCS. */
  @Override
  public Value visitLiteral(VitruvOCLParser.LiteralContext ctx) {
    return visit(ctx.literalExpCS());
  }

  /** Delegates to ifExpCS. */
  @Override
  public Value visitConditional(VitruvOCLParser.ConditionalContext ctx) {
    return visit(ctx.ifExpCS());
  }

  /**
   * Evaluates a {@code let} expression, binding local variables before evaluating the body.
   *
   * <p>Opens a fresh {@link LocalScope}, evaluates all variable declarations in order (each binding
   * is immediately visible to subsequent declarations and the body), then evaluates each body
   * expression in sequence. The value of the last body expression is returned as the result of the
   * {@code let} expression.
   *
   * <p>Example: {@code let x : Integer = 5, y : Integer = x * 2 in x + y}
   *
   * @param ctx the parse tree node for the {@code let} expression, containing the variable
   *     declaration list and one or more body expressions
   * @return the value of the last body expression evaluated within the let scope; or an error value
   *     if the body contains no expressions
   */
  @Override
  public Value visitLetExpCS(VitruvOCLParser.LetExpCSContext ctx) {
    LocalScope letScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(letScope);
    try {
      // Evaluate and bind each variable declaration
      visit(ctx.variableDeclarations());

      // Evaluate body, return last expression
      Value result = null;
      for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
        result = visit(exp);
      }

      if (result == null) {
        return Value.empty(Type.ANY);
      }
      return result;
    } finally {
      symbolTable.exitScope();
    }
  }

  /** Delegates to letExpCS. */
  @Override
  public Value visitLetBinding(VitruvOCLParser.LetBindingContext ctx) {
    return visit(ctx.letExpCS());
  }

  /** Delegates to collectionLiteralExpCS. */
  @Override
  public Value visitCollectionLiteral(VitruvOCLParser.CollectionLiteralContext ctx) {
    return visit(ctx.collectionLiteralExpCS());
  }

  /** Delegates to typeLiteralCS. */
  @Override
  public Value visitTypeLiteral(VitruvOCLParser.TypeLiteralContext ctx) {
    return visit(ctx.typeLiteralExpCS());
  }

  /** Delegates to nestedExpCS. */
  @Override
  public Value visitNested(VitruvOCLParser.NestedContext ctx) {
    return visit(ctx.nestedExpCS());
  }

  /** Delegates to selfExpCS. */
  @Override
  public Value visitSelf(VitruvOCLParser.SelfContext ctx) {
    return visit(ctx.selfExpCS());
  }

  /** Delegates to variableExpCS. */
  @Override
  public Value visitVariable(VitruvOCLParser.VariableContext ctx) {
    return visit(ctx.variableExpCS());
  }

  /**
   * Evaluates correspondence operator (~).
   *
   * <p>Checks if two objects are related via a Correspondence model instance. Searches through all
   * loaded Correspondence objects to find a relationship between the left and right operands.
   *
   * <p><b>Algorithm:</b>
   *
   * <ol>
   *   <li>Evaluate both operands to get EObject instances
   *   <li>Get all Correspondence objects from loaded models
   *   <li>For each Correspondence, check if leftEObjects contains obj1 and rightEObjects contains
   *       obj2
   *   <li>Also check reverse: leftEObjects contains obj2 and rightEObjects contains obj1
   *   <li>Return true if any correspondence found, false otherwise
   * </ol>
   *
   * @param ctx Correspondence expression context
   * @return Value.of(true) if correspondence exists, Value.of(false) otherwise
   */
  @Override
  public Value visitCorrespondence(VitruvOCLParser.CorrespondenceContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);

    // Both sides must be singletons
    if (leftValue.isEmpty() || rightValue.isEmpty()) return Value.boolValue(false);
    if (leftValue.size() != 1 || rightValue.size() != 1) return Value.boolValue(false);

    // Extract EObject instances
    EObject leftObject = leftValue.getElements().get(0).tryGetInstance();
    EObject rightObject = rightValue.getElements().get(0).tryGetInstance();

    if (leftObject == null || rightObject == null) return Value.boolValue(false);

    // Check if correspondence exists between these two objects
    boolean corresponds = checkCorrespondence(leftObject, rightObject);

    return Value.boolValue(corresponds);
  }

  /**
   * Checks if two EObjects are related via a Correspondence.
   *
   * <p>Searches all Correspondence objects loaded in the VSUM for a relationship between obj1 and
   * obj2. The correspondence is bidirectional: obj1 can appear in leftEObjects or rightEObjects.
   *
   * @param obj1 First object
   * @param obj2 Second object
   * @return true if a Correspondence relates obj1 and obj2, false otherwise
   */
  private boolean checkCorrespondence(EObject obj1, EObject obj2) {
    // VSUM-Pfad: über MetamodelWrapperInterface (funktioniert sowohl mit
    // VSUMWrapper als auch mit MetamodelWrapper falls getCorrespondingObjects
    // dort implementiert ist)
    Set<EObject> correspondents = specification.getCorrespondingObjects(obj1);
    if (correspondents.contains(obj2)) {
      return true;
    }
    // bidirektional
    return specification.getCorrespondingObjects(obj2).contains(obj1);
  }

  // ==================== Not Yet Implemented Features ====================
  /**
   * Placeholder for message operator (^).
   *
   * <p>Not yet implemented - reports error.
   */
  @Override
  public Value visitMessage(VitruvOCLParser.MessageContext ctx) {
    return Value.empty(Type.ANY);
  }

  // ==================== Type-Related Nodes (No Runtime Value)
  // ====================

  /** Type expressions have no runtime value. */
  @Override
  public Value visitTypeExpCS(VitruvOCLParser.TypeExpCSContext ctx) {
    if (ctx.typeNameExpCS() != null) {
      return visit(ctx.typeNameExpCS());
    }
    if (ctx.typeLiteralCS() != null) {
      return visit(ctx.typeLiteralCS());
    }
    return Value.empty(Type.ERROR);
  }

  /** Type literals have no runtime value. */
  @Override
  public Value visitTypeLiteralCS(VitruvOCLParser.TypeLiteralCSContext ctx) {
    return Value.empty(Type.ANY);
  }

  /** Collection types have no runtime value. */
  @Override
  public Value visitCollectionTypeCS(VitruvOCLParser.CollectionTypeCSContext ctx) {
    Type type = nodeTypes.get(ctx);
    return Value.empty(type != null ? type : Type.ANY);
  }

  /** Collection type identifiers have no runtime value. */
  @Override
  public Value visitCollectionTypeIdentifier(VitruvOCLParser.CollectionTypeIdentifierContext ctx) {
    return Value.empty(Type.ANY);
  }

  /** Primitive types have no runtime value. */
  @Override
  public Value visitPrimitiveTypeCS(VitruvOCLParser.PrimitiveTypeCSContext ctx) {
    return Value.empty(Type.ANY);
  }

  /** Type names have no runtime value. */
  @Override
  public Value visitTypeNameExpCS(VitruvOCLParser.TypeNameExpCSContext ctx) {
    return Value.empty(Type.ANY);
  }

  /** Type literal expressions have no runtime value. */
  @Override
  public Value visitTypeLiteralExpCS(VitruvOCLParser.TypeLiteralExpCSContext ctx) {
    return visit(ctx.typeLiteralCS());
  }

  // ==================== Error States (Require Receiver Context)
  // ====================

  /**
   * Error: navigation chain visited without receiver context.
   *
   * <p>This should never be reached - navigation chains should always be visited via {@link
   * #visitNavigationWithReceiver}.
   */
  @Override
  public Value visitNavigationChainCS(VitruvOCLParser.NavigationChainCSContext ctx) {
    return visit(ctx.navigationTargetCS());
  }

  /**
   * Error: property access visited without receiver context.
   *
   * <p>Should be visited via {@link #visitPropertyAccessWithReceiver}.
   */
  @Override
  public Value visitPropertyNav(VitruvOCLParser.PropertyNavContext ctx) {
    return Value.empty(Type.ANY);
  }

  /**
   * Error: operation call visited without receiver context.
   *
   * <p>Should be visited via {@link #visitOperationCallWithReceiver}.
   */
  @Override
  public Value visitOperationNav(VitruvOCLParser.OperationNavContext ctx) {
    return Value.empty(Type.ANY);
  }

  /**
   * Error: property access visited without receiver.
   *
   * <p>Should be visited via {@link #visitPropertyAccessWithReceiver}.
   */
  @Override
  public Value visitPropertyAccess(VitruvOCLParser.PropertyAccessContext ctx) {
    return Value.empty(Type.ANY);
  }

  // ==================== Miscellaneous ====================

  /**
   * Processes variable declarations (currently unused).
   *
   * @param ctx The variable declarations node
   * @return Empty value
   */
  @Override
  public Value visitVariableDeclarations(VitruvOCLParser.VariableDeclarationsContext ctx) {
    for (VitruvOCLParser.VariableDeclarationContext varDecl : ctx.variableDeclaration()) {
      visit(varDecl);
    }
    return Value.empty(Type.ANY);
  }

  /** Whitespace node - no value. */
  @Override
  public Value visitOnespace(VitruvOCLParser.OnespaceContext ctx) {
    return Value.empty(Type.ANY);
  }

  // ==================== Utility Methods ====================

  /** No-op — token stream is not used by the evaluator. */
  @SuppressWarnings("java:S1186")
  public void setTokenStream(org.antlr.v4.runtime.TokenStream tokens) {}

  /**
   * Evaluates the {@code asSet()} conversion operation on a collection receiver.
   *
   * <p>Converts the receiver collection to a {@code Set} by wrapping its elements in a new {@code
   * Set}-typed value and removing duplicates via {@link Value#removeDuplicates()}. The element type
   * is preserved.
   *
   * <p>Example: {@code Sequence{1, 2, 2, 3}->asSet()} yields {@code Set{1, 2, 3}}
   *
   * @param ctx the parse tree node for the {@code asSet()} operation
   * @return a {@code Set} containing the receiver's elements with duplicates removed
   */
  @Override
  public Value visitAsSetOp(VitruvOCLParser.AsSetOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> elements = new ArrayList<>(receiver.getElements());
    Type elemType = receiver.getRuntimeType().getElementType();
    Value result = Value.of(elements, Type.set(elemType));
    return result.removeDuplicates();
  }

  /**
   * Evaluates the {@code asBag()} conversion operation on a collection receiver.
   *
   * <p>Converts the receiver collection to a {@code Bag} by wrapping its elements in a new {@code
   * Bag}-typed value. All elements including duplicates are preserved; no deduplication is
   * performed. The element type is preserved.
   *
   * <p>Example: {@code Set{1, 2, 3}->asBag()} yields {@code Bag{1, 2, 3}}
   *
   * @param ctx the parse tree node for the {@code asBag()} operation
   * @return a {@code Bag} containing all elements of the receiver, including duplicates
   */
  @Override
  public Value visitAsBagOp(VitruvOCLParser.AsBagOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> elements = new ArrayList<>(receiver.getElements());
    Type elemType = receiver.getRuntimeType().getElementType();
    return Value.of(elements, Type.bag(elemType));
  }

  /**
   * Evaluates the {@code asSequence()} conversion operation on a collection receiver.
   *
   * <p>Converts the receiver collection to a {@code Sequence} by wrapping its elements in a new
   * {@code Sequence}-typed value. Element order and duplicates are preserved as-is from the
   * receiver. The element type is preserved.
   *
   * <p>Example: {@code Set{3, 1, 2}->asSequence()} yields {@code Sequence{3, 1, 2}}
   *
   * @param ctx the parse tree node for the {@code asSequence()} operation
   * @return a {@code Sequence} containing all elements of the receiver in their current order,
   *     including any duplicates
   */
  @Override
  public Value visitAsSequenceOp(VitruvOCLParser.AsSequenceOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> elements = new ArrayList<>(receiver.getElements());
    Type elemType = receiver.getRuntimeType().getElementType();
    return Value.of(elements, Type.sequence(elemType));
  }

  /**
   * Evaluates the {@code asOrderedSet()} conversion operation on a collection receiver.
   *
   * <p>Converts the receiver collection to an {@code OrderedSet} by wrapping its elements in a new
   * {@code OrderedSet}-typed value and removing duplicates via {@link Value#removeDuplicates()},
   * preserving the relative order of first occurrences. The element type is preserved.
   *
   * <p>Example: {@code Sequence{3, 1, 2, 1}->asOrderedSet()} yields {@code OrderedSet{3, 1, 2}}
   *
   * @param ctx the parse tree node for the {@code asOrderedSet()} operation
   * @return an {@code OrderedSet} containing the receiver's elements in their original order with
   *     duplicates removed
   */
  @Override
  public Value visitAsOrderedSetOp(VitruvOCLParser.AsOrderedSetOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> elements = new ArrayList<>(receiver.getElements());
    Type elemType = receiver.getRuntimeType().getElementType();
    Value result = Value.of(elements, Type.orderedSet(elemType));
    return result.removeDuplicates();
  }

  /**
   * Evaluates the {@code at()} operation on a String or ordered collection receiver.
   *
   * <p>Retrieves the element at the given 1-based index. Two receiver kinds are supported:
   *
   * <ul>
   *   <li><b>String</b> — returns the character at position {@code index} as a singleton {@code
   *       STRING}. Valid range: {@code [1, length]}.
   *   <li><b>Ordered collection</b> — returns the element at position {@code index} as a singleton
   *       of the collection's element type. Valid range: {@code [1, size]}.
   * </ul>
   *
   * <p>Example: {@code Sequence{10, 20, 30}->at(2)} yields {@code 20}<br>
   * Example: {@code 'hello'->at(1)} yields {@code 'h'}
   *
   * @param ctx the parse tree node for the {@code at()} operation, including the index argument
   *     expression
   * @return a singleton value containing the element or character at the given position; or an
   *     error value if the index is not a singleton {@code Integer}, or the index is out of bounds
   *     for the receiver
   */
  @Override
  public Value visitAtOp(VitruvOCLParser.AtOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value indexVal = visit(ctx.index);

    if (indexVal.isEmpty()) return Value.empty(Type.ANY);
    Integer index = indexVal.getElements().get(0).tryGetInt();
    if (index == null) return Value.empty(Type.ANY);

    // String case
    String str = receiver.isEmpty() ? null : receiver.getElements().get(0).tryGetString();
    if (str != null) {
      if (index < 1 || index > str.length()) return Value.empty(Type.STRING);
      return Value.stringValue(String.valueOf(str.charAt(index - 1)));
    }

    // Collection case (1-based)
    if (index < 1 || index > receiver.size()) return Value.empty(Type.ANY);
    OCLElement elem = receiver.getElements().get(index - 1);
    Type elemType = receiver.getRuntimeType().getElementType();
    return new Value(List.of(elem), Type.singleton(elemType));
  }

  /**
   * Evaluates the {@code subSequence()} operation on an ordered collection receiver.
   *
   * <p>Returns a contiguous sub-collection from the receiver using 1-based, inclusive bounds {@code
   * [start, end]}. The result preserves the receiver's collection kind and element type.
   *
   * <p>Example: {@code Sequence{10, 20, 30, 40}->subSequence(2, 3)} yields {@code Sequence{20, 30}}
   *
   * @param ctx the parse tree node for the {@code subSequence()} operation, including the start and
   *     end bound expressions
   * @return a collection of the same kind as the receiver containing the elements at positions
   *     {@code start} through {@code end} inclusive; or an error value if either bound is not a
   *     singleton {@code Integer}, or the bounds are out of range ({@code start < 1}, {@code end >
   *     size}, or {@code start > end})
   */
  @Override
  public Value visitSubSequenceOp(VitruvOCLParser.SubSequenceOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value startVal = visit(ctx.start);
    Value endVal = visit(ctx.end);

    if (startVal.isEmpty() || endVal.isEmpty()) return Value.of(List.of(), receiver.getRuntimeType());
    if (startVal.size() != 1 || endVal.size() != 1) return Value.of(List.of(), receiver.getRuntimeType());

    Integer start = startVal.getElements().get(0).tryGetInt();
    Integer end = endVal.getElements().get(0).tryGetInt();

    if (start == null || end == null) return Value.of(List.of(), receiver.getRuntimeType());
    if (start < 1 || end > receiver.size() || start > end) return Value.of(List.of(), receiver.getRuntimeType());

    // 1-based, inclusive on both ends
    List<OCLElement> sub = receiver.getElements().subList(start - 1, end);
    return Value.of(new ArrayList<>(sub), receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code characters()} operation on a String receiver.
   *
   * <p>Splits the receiver string into its individual characters and returns them as a {@code
   * Sequence<String>}, preserving order and allowing duplicates.
   *
   * <p>Example: {@code 'hello'->characters()} yields {@code Sequence{'h','e','l','l','o'}}
   *
   * @param ctx the parse tree node for the {@code characters()} operation
   * @return a {@code Sequence<String>} of single-character strings in source order; or an error
   *     value if the receiver is not a singleton {@code String}
   */
  @Override
  public Value visitCharactersOp(VitruvOCLParser.CharactersOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.of(List.of(), Type.sequence(Type.STRING));
    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return Value.of(List.of(), Type.sequence(Type.STRING));
    List<OCLElement> chars = new ArrayList<>();
    for (char c : str.toCharArray()) {
      chars.add(new OCLElement.StringValue(String.valueOf(c)));
    }
    return Value.of(chars, Type.sequence(Type.STRING));
  }

  /**
   * Evaluates the {@code tokenize()} operation on a String receiver.
   *
   * <p>Splits the receiver string on each literal occurrence of the delimiter string, returning the
   * resulting tokens as a {@code Sequence<String>}. The delimiter is treated as a plain substring
   * (not a regex) via {@link java.util.regex.Pattern#quote}. Trailing empty tokens are preserved
   * ({@code limit = -1}). If the delimiter is empty, the receiver string is returned as a
   * single-element sequence.
   *
   * <p>Example: {@code 'a,b,,c'->tokenize(',')} yields {@code Sequence{'a','b','','c'}}
   *
   * @param ctx the parse tree node for the {@code tokenize()} operation, including the delimiter
   *     argument expression
   * @return a {@code Sequence<String>} of tokens produced by splitting on the delimiter; or an
   *     error value if the receiver or delimiter is not a singleton {@code String}
   */
  @Override
  public Value visitTokenizeOp(VitruvOCLParser.TokenizeOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.of(List.of(), Type.sequence(Type.STRING));
    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return Value.of(List.of(), Type.sequence(Type.STRING));
    Value argVal = visit(ctx.arg);
    if (argVal.isEmpty()) return Value.of(List.of(new OCLElement.StringValue(str)), Type.sequence(Type.STRING));
    String delimiter = argVal.getElements().get(0).tryGetString();
    if (delimiter == null) return Value.of(List.of(new OCLElement.StringValue(str)), Type.sequence(Type.STRING));
    List<OCLElement> tokens = new ArrayList<>();
    if (delimiter.isEmpty()) {
      tokens.add(new OCLElement.StringValue(str));
    } else {
      for (String token : str.split(java.util.regex.Pattern.quote(delimiter), -1)) {
        tokens.add(new OCLElement.StringValue(token));
      }
    }
    return Value.of(tokens, Type.sequence(Type.STRING));
  }

  /**
   * Evaluates the {@code intersection()} operation on a collection receiver.
   *
   * <p>Returns a collection containing only those elements of the receiver that are also present in
   * the argument collection, tested via {@link Value#includes}. The result type is taken from the
   * type-checker annotation on the node if available, falling back to the receiver's runtime type.
   * If the result type enforces uniqueness (e.g. {@code Set}), duplicates are removed after
   * construction.
   *
   * <p>Example: {@code Set{1, 2, 3}->intersection(Set{2, 3, 4})} yields {@code Set{2, 3}}
   *
   * @param ctx the parse tree node for the {@code intersection()} operation, including the argument
   *     expression
   * @return a collection containing all elements present in both the receiver and the argument,
   *     deduplicated if the result type requires uniqueness
   */
  @Override
  public Value visitIntersectionOp(VitruvOCLParser.IntersectionOpContext ctx) {
    Value receiver = receiverStack.peek();
    Value arg = visit(ctx.arg);

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      if (arg.includes(elem)) {
        results.add(elem);
      }
    }

    Type resultType = nodeTypes.get(ctx);
    Value result = Value.of(results, resultType != null ? resultType : receiver.getRuntimeType());

    // Remove duplicates if result is unique
    if (result.getRuntimeType().isUnique()) {
      result = result.removeDuplicates();
    }
    return result;
  }

  /**
   * Evaluates the {@code toInteger()} conversion operation on a String receiver.
   *
   * <p>Parses the receiver string as a decimal integer using {@link Integer#parseInt}, trimming
   * leading and trailing whitespace before parsing. The result is returned as an {@code INTEGER}
   * singleton.
   *
   * <p>Example: {@code ' 42 '->toInteger()} yields {@code 42}
   *
   * @param ctx the parse tree node for the {@code toInteger()} operation
   * @return a singleton {@code INTEGER} value parsed from the receiver string; or an error value if
   *     the receiver is not a singleton {@code String} or the string cannot be parsed as a decimal
   *     integer
   */
  @Override
  public Value visitToIntegerOp(VitruvOCLParser.ToIntegerOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.intValue(0);
    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return Value.intValue(0);
    try {
      return Value.intValue(Integer.parseInt(str.trim()));
    } catch (NumberFormatException e) {
      return Value.intValue(0);
    }
  }

  /**
   * Evaluates the {@code toReal()} conversion operation on a String receiver.
   *
   * <p>Parses the receiver string as a floating-point number using {@link Double#parseDouble},
   * trimming leading and trailing whitespace before parsing. The result is returned as a {@code
   * DOUBLE} singleton.
   *
   * <p>Example: {@code ' 3.14 '->toReal()} yields {@code 3.14}
   *
   * @param ctx the parse tree node for the {@code toReal()} operation
   * @return a singleton {@code DOUBLE} value parsed from the receiver string; or an error value if
   *     the receiver is not a singleton {@code String} or the string cannot be parsed as a
   *     floating-point number
   */
  @Override
  public Value visitToRealOp(VitruvOCLParser.ToRealOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.doubleValue(0.0);
    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return Value.doubleValue(0.0);
    try {
      return Value.doubleValue(Double.parseDouble(str.trim()));
    } catch (NumberFormatException e) {
      return Value.doubleValue(0.0);
    }
  }

  /**
   * Evaluates the {@code substituteAll()} operation on a String receiver.
   *
   * <p>Replaces every literal occurrence of the pattern string within the receiver with the
   * replacement string using {@link String#replace}. No regex interpretation is applied — the
   * pattern is matched as a plain substring. If the pattern is not found, the receiver string is
   * returned unchanged.
   *
   * <p>Example: {@code 'abcabc'->substituteAll('b', 'X')} yields {@code 'aXcaXc'}
   *
   * @param ctx the parse tree node for the {@code substituteAll()} operation, including the pattern
   *     and replacement argument expressions
   * @return a singleton {@code STRING} value with all occurrences of the pattern replaced, or the
   *     original string if the pattern is not found; or an error value if the receiver, pattern, or
   *     replacement is not a singleton {@code String}
   */
  @Override
  public Value visitSubstituteAllOp(VitruvOCLParser.SubstituteAllOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return receiver;
    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return receiver;
    Value patternVal = visit(ctx.pattern);
    Value replacementVal = visit(ctx.replacement);
    if (patternVal.isEmpty() || replacementVal.isEmpty()) return receiver;
    String pattern = patternVal.getElements().get(0).tryGetString();
    String replacement = replacementVal.getElements().get(0).tryGetString();
    if (pattern == null || replacement == null) return receiver;
    return Value.stringValue(str.replace(pattern, replacement));
  }

  /**
   * Evaluates the {@code substituteFirst()} operation on a String receiver.
   *
   * <p>Replaces the first literal occurrence of the pattern string within the receiver with the
   * replacement string. Unlike {@code replaceAll()}, no regex interpretation is applied — the
   * pattern is matched as a plain substring via {@link String#indexOf}. If the pattern is not
   * found, the receiver string is returned unchanged.
   *
   * <p>Example: {@code 'abcabc'->substituteFirst('b', 'X')} yields {@code 'aXcabc'}
   *
   * @param ctx the parse tree node for the {@code substituteFirst()} operation, including the
   *     pattern and replacement argument expressions
   * @return a singleton {@code STRING} value with the first occurrence of the pattern replaced, or
   *     the original string if the pattern is not found; or an error value if the receiver,
   *     pattern, or replacement is not a singleton {@code String}
   */
  @Override
  public Value visitSubstituteFirstOp(VitruvOCLParser.SubstituteFirstOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return receiver;
    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return receiver;
    Value patternVal = visit(ctx.pattern);
    Value replacementVal = visit(ctx.replacement);
    if (patternVal.isEmpty() || replacementVal.isEmpty()) return receiver;
    String pattern = patternVal.getElements().get(0).tryGetString();
    String replacement = replacementVal.getElements().get(0).tryGetString();
    if (pattern == null || replacement == null) return receiver;
    int idx = str.indexOf(pattern);
    if (idx == -1) return Value.stringValue(str);
    return Value.stringValue(
        str.substring(0, idx) + replacement + str.substring(idx + pattern.length()));
  }

  /**
   * Evaluates the {@code matches()} operation on a String receiver.
   *
   * <p>Tests whether the receiver string matches the given regular expression pattern using {@link
   * String#matches}, which requires the pattern to match the entire string. Both the receiver and
   * the argument must be singleton {@code String} values.
   *
   * <p>Example: {@code 'hello123'->matches('[a-z]+\\d+')} yields {@code true}
   *
   * @param ctx the parse tree node for the {@code matches()} operation, including the pattern
   *     argument expression
   * @return a singleton {@code BOOLEAN} value — {@code true} if the receiver string matches the
   *     entire pattern, {@code false} otherwise; or an error value if the receiver or argument is
   *     not a singleton {@code String}
   */
  @Override
  public Value visitMatchesOp(VitruvOCLParser.MatchesOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.boolValue(false);
    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) return Value.boolValue(false);
    Value argVal = visit(ctx.arg);
    if (argVal.isEmpty()) return Value.boolValue(false);
    String pattern = argVal.getElements().get(0).tryGetString();
    if (pattern == null) return Value.boolValue(false);
    return Value.boolValue(str.matches(pattern));
  }

  @Override
  public Value visitSelectCorrespondence(VitruvOCLParser.SelectCorrespondenceContext ctx) {

    Value receiver = receiverStack.peek();

    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) return Value.of(List.of(), receiver.getRuntimeType());
    Value selfValue = selfSymbol.getValue();
    if (selfValue.isEmpty() || selfValue.size() != 1) return Value.of(List.of(), receiver.getRuntimeType());
    EObject selfObject = selfValue.getElements().get(0).tryGetInstance();
    if (selfObject == null) return Value.of(List.of(), receiver.getRuntimeType());

    String tagFilter = extractTagFilter(ctx.corrFilter);
    EClass typeFilter = extractTypeFilter(ctx.corrFilter);

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      EObject elemObject = elem.tryGetInstance();

      boolean passes =
          elemObject != null
              && passesCorrespondenceFilter(selfObject, elemObject, tagFilter, typeFilter);

      if (passes) {
        results.add(elem);
      }
    }

    return Value.of(results, receiver.getRuntimeType());
  }

  @Override
  public Value visitRejectCorrespondence(VitruvOCLParser.RejectCorrespondenceContext ctx) {

    Value receiver = receiverStack.peek();

    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) return Value.of(List.of(), receiver.getRuntimeType());
    Value selfValue = selfSymbol.getValue();
    if (selfValue.isEmpty() || selfValue.size() != 1) return Value.of(List.of(), receiver.getRuntimeType());
    EObject selfObject = selfValue.getElements().get(0).tryGetInstance();
    if (selfObject == null) return Value.of(List.of(), receiver.getRuntimeType());

    String tagFilter = extractTagFilter(ctx.corrFilter);
    EClass typeFilter = extractTypeFilter(ctx.corrFilter);

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      EObject elemObject = elem.tryGetInstance();

      boolean passes =
          elemObject != null
              && passesCorrespondenceFilter(selfObject, elemObject, tagFilter, typeFilter);

      if (!passes) {
        results.add(elem);
      }
    }

    return Value.of(results, receiver.getRuntimeType());
  }

  @Override
  public Value visitExistsCorrespondence(VitruvOCLParser.ExistsCorrespondenceContext ctx) {

    Value receiver = receiverStack.peek();

    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) return Value.boolValue(false);
    Value selfValue = selfSymbol.getValue();
    if (selfValue.isEmpty() || selfValue.size() != 1) return Value.boolValue(false);
    EObject selfObject = selfValue.getElements().get(0).tryGetInstance();
    if (selfObject == null) return Value.boolValue(false);

    String tagFilter = extractTagFilter(ctx.corrFilter);
    EClass typeFilter = extractTypeFilter(ctx.corrFilter);

    for (OCLElement elem : receiver.getElements()) {
      EObject elemObject = elem.tryGetInstance();

      boolean passes =
          elemObject != null
              && passesCorrespondenceFilter(selfObject, elemObject, tagFilter, typeFilter);

      if (passes) {
        return Value.boolValue(true);
      }
    }

    return Value.boolValue(false);
  }

  /** Extracts the Tag filter value from a correspondenceFilterCS, or null if none specified. */
  private String extractTagFilter(VitruvOCLParser.CorrespondenceFilterCSContext corrFilter) {
    if (corrFilter.correspondenceOptions() == null) {
      return null;
    }
    for (VitruvOCLParser.CorrespondenceOptionContext opt :
        corrFilter.correspondenceOptions().correspondenceOption()) {
      if (opt instanceof VitruvOCLParser.CorrTagFilterContext tagCtx) {
        Value tagVal = visit(tagCtx.tag);
        if (tagVal.isEmpty()) return null;
        return tagVal.getElements().get(0).tryGetString();
      }
    }
    return null;
  }

  /** Extracts the Type filter EClass from a correspondenceFilterCS, or null if none specified. */
  private EClass extractTypeFilter(VitruvOCLParser.CorrespondenceFilterCSContext corrFilter) {
    if (corrFilter.correspondenceOptions() == null) {
      return null;
    }
    for (VitruvOCLParser.CorrespondenceOptionContext opt :
        corrFilter.correspondenceOptions().correspondenceOption()) {
      if (opt instanceof VitruvOCLParser.CorrTypeFilterContext typeCtx) {
        // Use the type already resolved by the type checker phase
        Type resolvedType = nodeTypes.get(typeCtx);
        if (resolvedType != null && resolvedType.isMetaclassType()) {
          return resolvedType.getEClass();
        }
      }
    }
    return null;
  }

  /**
   * Returns true if elemObject passes both the type and tag filter relative to selfObject.
   *
   * <p>Type filter is a pure EMF instanceof check (cheap). Tag filter delegates to {@code
   * specification.correspondenceHasTag} which searches the correspondence model. When no filter is
   * set the plain {@code checkCorrespondence} result is returned.
   */
  private boolean passesCorrespondenceFilter(
      EObject selfObject, EObject elemObject, String tagFilter, EClass typeFilter) {
    // Type filter: elemObject must be an instance of typeFilter
    if (typeFilter != null && !typeFilter.isInstance(elemObject)) {
      return false;
    }
    // Tag filter: correspondence must carry the required tag
    if (tagFilter != null) {
      return specification.correspondenceHasTag(selfObject, elemObject, tagFilter);
    }
    // No filter: plain bidirectional correspondence check
    return checkCorrespondence(selfObject, elemObject);
  }
}

