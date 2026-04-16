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
package tools.vitruv.dsls.vitruvOCL.evaluator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLBaseVisitor;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.*;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Phase 3 visitor that evaluates OCL expressions and produces runtime values.
 *
 * <p>This visitor implements the <b>evaluation phase</b> of the VitruvOCL compiler pipeline,
 * operating after symbol table construction (Phase 1) and type checking (Phase 2). It traverses the
 * parse tree and computes concrete runtime values ({@link Value} objects) for OCL expressions.
 *
 * <h2>Architecture</h2>
 *
 * The evaluator uses pre-computed type information from {@link TypeCheckVisitor} stored in {@code
 * nodeTypes} to perform type-dependent operations correctly. It maintains a {@code receiverStack}
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
 * @see TypeCheckVisitor Phase 2 visitor that produces type information
 * @see AbstractPhaseVisitor Base class providing common visitor infrastructure
 */
public class EvaluationVisitor extends AbstractPhaseVisitor<Value> {

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

  /** Token stream for potential future use (e.g., accessing comments or whitespace). */
  private org.antlr.v4.runtime.TokenStream tokens;

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
   * @param symbolTable The symbol table containing variable and type definitions
   * @param specification The metamodel wrapper providing access to model instances
   * @param errors The error collector for reporting runtime errors
   * @param nodeTypes Pre-computed type information from the type checking phase
   */
  public EvaluationVisitor(
      SymbolTable symbolTable,
      MetamodelWrapperInterface specification,
      ErrorCollector errors,
      ParseTreeProperty<Type> nodeTypes) {
    super(symbolTable, specification, errors);
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
        "evaluator");
  }

  /**
   * Reports a runtime error and returns an error value.
   *
   * <p>Convenience method for reporting errors with source location and returning {@code
   * Value.empty(Type.ERROR)}.
   *
   * @param message The error message
   * @param ctx The parse tree context where the error occurred
   * @return An empty error value
   */
  private Value error(String message, org.antlr.v4.runtime.ParserRuleContext ctx) {
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        message,
        ErrorSeverity.ERROR,
        "evaluator");
    return Value.empty(Type.ERROR);
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
  public Value visitClassifierContextCS(VitruvOCLParser.ClassifierContextCSContext ctx) {
    Type contextType = nodeTypes.get(ctx);

    if (contextType == null || contextType == Type.ERROR) {
      return error("Invalid context type", ctx);
    }

    EClass eClass = contextType.getEClass();
    if (eClass == null) {
      return error("Context type must be a metaclass type", ctx);
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

    if (condVal == null || condVal.size() != 1) {
      return error("If condition must be singleton", ctx);
    }

    OCLElement condElem = condVal.getElements().get(0);
    Boolean condition = condElem.tryGetBool();

    if (condition == null) {
      return error("If condition must be Boolean", ctx);
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
    Value result = Value.boolValue(receiver.isEmpty());
    return result;
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
    if (arg.size() != 1) {
      return error("includes() requires singleton", ctx);
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
    if (arg.size() != 1) {
      return error("including() requires singleton", ctx);
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
    if (arg.size() != 1) {
      return error("excluding() requires singleton", ctx);
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
    if (arg.size() != 1) {
      return error("excludes() requires singleton", ctx);
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
        return error("sum() requires numeric elements", ctx);
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
    boolean hasReal = receiver.getElements().stream().anyMatch(e -> e.tryGetDouble() != null);
    if (hasReal) {
      double max = Double.NEGATIVE_INFINITY;
      for (OCLElement elem : receiver.getElements()) {
        Double val = elem.tryGetDouble();
        if (val == null) {
          return error("max() requires numeric elements", ctx);
        }
        if (val > max) max = val;
      }
      return Value.doubleValue(max);
    }
    int max = Integer.MIN_VALUE;
    for (OCLElement elem : receiver.getElements()) {
      Integer val = elem.tryGetInt();
      if (val == null) {
        return error("max() requires numeric elements", ctx);
      }
      if (val > max) max = val;
    }
    return Value.intValue(max);
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
    boolean hasReal = receiver.getElements().stream().anyMatch(e -> e.tryGetDouble() != null);
    if (hasReal) {
      double min = Double.POSITIVE_INFINITY;
      for (OCLElement elem : receiver.getElements()) {
        Double val = elem.tryGetDouble();
        if (val == null) {
          return error("min() requires numeric elements", ctx);
        }
        if (val < min) {
          min = val;
        }
      }
      return Value.doubleValue(min);
    }
    int min = Integer.MAX_VALUE;
    for (OCLElement elem : receiver.getElements()) {
      Integer val = elem.tryGetInt();
      if (val == null) {
        return error("min() requires numeric elements", ctx);
      }
      if (val < min) {
        min = val;
      }
    }
    return Value.intValue(min);
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
        return error("avg() requires numeric elements", ctx);
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
      } else {
        return error("abs() requires a numeric value", ctx);
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
      } else {
        return error("floor() requires a numeric value", ctx);
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
      } else {
        return error("ceil() requires a numeric value", ctx);
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
        results.add(new OCLElement.DoubleValue((double) Math.round(elem.tryGetFloat())));
      } else if (elem.tryGetDouble() != null) {
        results.add(new OCLElement.DoubleValue((double) Math.round(elem.tryGetDouble())));
      } else {
        return error("round() requires a numeric value", ctx);
      }
    }
    return Value.of(results, receiver.getRuntimeType());
  }

  // ==================== String Operations ====================

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
    if (receiver.size() != 1) {
      return error("concat() requires singleton String receiver", ctx);
    }

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) {
      return error("concat() requires String receiver", ctx);
    }

    Value arg = visit(ctx.arg);
    if (arg.size() != 1) {
      return error("concat() requires singleton String argument", ctx);
    }

    String argStr = arg.getElements().get(0).tryGetString();
    if (argStr == null) {
      return error("concat() requires String argument", ctx);
    }

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
    if (receiver.size() != 1) {
      return error("toUpper() requires singleton String receiver", ctx);
    }

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) {
      return error("toUpper() requires String receiver", ctx);
    }

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
    if (receiver.size() != 1) {
      return error("toLower() requires singleton String receiver", ctx);
    }

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) {
      return error("toLower() requires String receiver", ctx);
    }

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
    if (receiver.size() != 1) {
      return error("indexOf() requires singleton String receiver", ctx);
    }

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) {
      return error("indexOf() requires String receiver", ctx);
    }

    Value arg = visit(ctx.arg);
    if (arg.size() != 1) {
      return error("indexOf() requires singleton String argument", ctx);
    }

    String searchStr = arg.getElements().get(0).tryGetString();
    if (searchStr == null) {
      return error("indexOf() requires String argument", ctx);
    }

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
    if (receiver.size() != 1) {
      return error("equalsIgnoreCase() requires singleton String receiver", ctx);
    }

    String str = receiver.getElements().get(0).tryGetString();
    if (str == null) {
      return error("equalsIgnoreCase() requires String receiver", ctx);
    }

    Value arg = visit(ctx.arg);
    if (arg.size() != 1) {
      return error("equalsIgnoreCase() requires singleton String argument", ctx);
    }

    String compareStr = arg.getElements().get(0).tryGetString();
    if (compareStr == null) {
      return error("equalsIgnoreCase() requires String argument", ctx);
    }

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

    if (iterVars.isEmpty()) {
      return error("select requires at least one iterator variable", ctx);
    }

    if (iterVars.size() == 1) {
      return evaluateSelectSingleVar(ctx, receiver, iterVars.get(0));
    }

    if (iterVars.size() == 2) {
      return evaluateSelectTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    return error("select supports at most 2 iterator variables", ctx);
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

    if (iterVars.isEmpty()) {
      return error("reject requires at least one iterator variable", ctx);
    }

    if (iterVars.size() == 1) {
      return evaluateRejectSingleVar(ctx, receiver, iterVars.get(0));
    }

    if (iterVars.size() == 2) {
      return evaluateRejectTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    return error("reject supports at most 2 iterator variables", ctx);
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

    if (iterVars.isEmpty()) {
      return error("collect requires at least one iterator variable", ctx);
    }

    if (iterVars.size() == 1) {
      return evaluateCollectSingleVar(ctx, receiver, iterVars.get(0));
    }

    if (iterVars.size() == 2) {
      return evaluateCollectTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    return error("collect supports at most 2 iterator variables", ctx);
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

    if (iterVars.isEmpty()) {
      return error("forAll requires at least one iterator variable", ctx);
    }

    if (iterVars.size() == 1) {
      return evaluateForAllSingleVar(ctx, receiver, iterVars.get(0));
    }

    if (iterVars.size() == 2) {
      return evaluateForAllTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    return error("forAll supports at most 2 iterator variables", ctx);
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

    if (iterVars.isEmpty()) {
      return error("select requires at least one iterator variable", ctx);
    }

    if (iterVars.size() == 1) {
      return evaluateExistsSingleVar(ctx, receiver, iterVars.get(0));
    }

    if (iterVars.size() == 2) {
      return evaluateExistsTwoVars(ctx, receiver, iterVars.get(0), iterVars.get(1));
    }

    return error("select supports at most 2 iterator variables", ctx);
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
  public Value visitOclAsTypeOp(VitruvOCLParser.OclAsTypeOpContext ctx) {
    Value receiver = receiverStack.peek();
    Type targetType = nodeTypes.get(ctx);

    if (targetType == null) {
      return error("Cannot resolve target type in oclAsType", ctx);
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
        } else {
          return error(
              "oclAsType: cannot cast " + elemEClass.getName() + " to " + targetEClass.getName(),
              ctx);
        }
      } else if (elem instanceof OCLElement.CastedMetaclassValue cmv) {
        // Already casted — re-validate against new target
        EClass elemEClass = cmv.instance().eClass();
        if (targetEClass.isSuperTypeOf(elemEClass) || elemEClass.equals(targetEClass)) {
          results.add(new OCLElement.CastedMetaclassValue(cmv.instance(), targetEClass));
        } else {
          return error(
              "oclAsType: cannot cast " + elemEClass.getName() + " to " + targetEClass.getName(),
              ctx);
        }
      } else {
        return error("oclAsType: element is not a metaclass instance", ctx);
      }
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

    // null is the empty optional ?Any? = []
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
      return error("Variable '" + varName + "' has no value", ctx);
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
        if (bodyResult.size() != 1) {
          return error("forAll predicate must return singleton Boolean", ctx);
        }

        Boolean condition = bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) {
          return error("forAll predicate must return Boolean", ctx);
        }

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
          if (bodyResult.size() != 1) {
            return error("forAll predicate must return singleton Boolean", ctx);
          }

          Boolean condition = bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) {
            return error("forAll predicate must return Boolean", ctx);
          }

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
        if (bodyResult.size() != 1) {
          return error("select predicate must return singleton Boolean", ctx);
        }

        Boolean condition = bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) {
          return error("select predicate must return Boolean", ctx);
        }

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
          if (bodyResult.size() != 1) {
            return error("select predicate must return singleton Boolean", ctx);
          }

          Boolean condition = bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) {
            return error("select predicate must return Boolean", ctx);
          }

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
        if (bodyResult.size() != 1) {
          return error("reject predicate must return singleton Boolean", ctx);
        }

        Boolean condition = bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) {
          return error("reject predicate must return Boolean", ctx);
        }

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
          if (bodyResult.size() != 1) {
            return error("reject predicate must return singleton Boolean", ctx);
          }

          Boolean condition = bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) {
            return error("reject predicate must return Boolean", ctx);
          }

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
        if (bodyResult.size() != 1) {
          return error("exists predicate must return singleton Boolean", ctx);
        }

        Boolean condition = bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) {
          return error("exists predicate must return Boolean", ctx);
        }

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
          if (bodyResult.size() != 1) {
            return error("exists predicate must return singleton Boolean", ctx);
          }

          Boolean condition = bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) {
            return error("exists predicate must return Boolean", ctx);
          }

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
  public Value visitMultiplicative(VitruvOCLParser.MultiplicativeContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);
    String operator = ctx.op.getText();

    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Arithmetic requires singleton operands", ctx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    // ── Integer × Integer ──────────────────────────────────────────────────
    Integer leftInt = leftElem.tryGetInt();
    Integer rightInt = rightElem.tryGetInt();
    if (leftInt != null && rightInt != null) {
      if (operator.equals("/") && rightInt == 0) {
        return error("Integer division by zero", ctx);
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
            tools.vitruv.dsls.vitruvOCL.typechecker.Type.FLOAT);
      }
      return Value.doubleValue(result);
    }

    return error(
        "Arithmetic requires numeric operands, got "
            + leftElem.getClass().getSimpleName()
            + " and "
            + rightElem.getClass().getSimpleName(),
        ctx);
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

    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Arithmetic requires singleton operands", ctx);
    }

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
            tools.vitruv.dsls.vitruvOCL.typechecker.Type.FLOAT);
      }
      return Value.doubleValue(result);
    }

    return error(
        "Arithmetic requires numeric operands, got "
            + leftElem.getClass().getSimpleName()
            + " and "
            + rightElem.getClass().getSimpleName(),
        ctx);
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
        ctx.left, ctx.right, ctx, (left, right) -> OCLElement.semanticEquals(left, right));
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
      BiFunction<OCLElement, OCLElement, Boolean> comparisonFn) {

    Value leftValue = visit(leftCtx);
    Value rightValue = visit(rightCtx);

    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Comparison requires singleton operands", errorCtx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    boolean result = comparisonFn.apply(leftElem, rightElem);
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
    return evaluateBinaryLogical(ctx.left, ctx.right, ctx, (left, right) -> left && right);
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
    return evaluateBinaryLogical(ctx.left, ctx.right, ctx, (left, right) -> left || right);
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
    return evaluateBinaryLogical(ctx.left, ctx.right, ctx, (left, right) -> left ^ right);
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
      ParserRuleContext errorCtx,
      BiFunction<Boolean, Boolean, Boolean> logicalFn) {

    Value leftValue = visit(leftCtx);
    Value rightValue = visit(rightCtx);

    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Boolean operators require singleton operands", errorCtx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    Boolean left = leftElem.tryGetBool();
    Boolean right = rightElem.tryGetBool();

    if (left == null || right == null) {
      return error("Boolean operators require boolean operands", errorCtx);
    }

    boolean result = logicalFn.apply(left, right);
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

    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("'implies' requires singleton operands", ctx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    Boolean left = leftElem.tryGetBool();
    Boolean right = rightElem.tryGetBool();

    if (left == null || right == null) {
      return error("'implies' requires boolean operands", ctx);
    }

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
      return error("Base expression returned null", ctx);
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

    if (operandValue == null) {
      return error("Operand returned null", ctx);
    }

    if (operandValue.size() != 1) {
      return error("Unary minus requires singleton operand", ctx);
    }

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
    if (value == null) {
      return error("Unary minus requires numeric operand", ctx);
    }

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

    if (operandValue == null) {
      return error("Operand returned null", ctx);
    }

    // Handle collection of booleans - negate all elements
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : operandValue.getElements()) {
      Boolean value = elem.tryGetBool();
      if (value == null) {
        return error("Logical not requires boolean operand", ctx);
      }
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
            return error("Invalid navigation target", ctx);
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
    ParseTree nav = ctx.getParent().getParent().getParent(); // NavigationChainCS
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
      return error("allInstances() requires metaclass receiver", ctx);
    }

    // Get all instances from metamodel
    EClass eClass = receiverType.getEClass();
    List<EObject> instances = specification.getAllInstances(eClass);
    List<OCLElement> elements =
        instances.stream().map(obj -> (OCLElement) new OCLElement.MetaclassValue(obj)).toList();

    Type resultType = nodeTypes.get(ctx);
    return Value.of(elements, resultType != null ? resultType : Type.set(receiverType));
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
  private Value visitPropertyAccessWithReceiver(
      VitruvOCLParser.PropertyAccessContext ctx, Value receiver) {
    String propertyName = ctx.propertyName.getText();

    if (receiver.isEmpty()) {
      return error("Property access requires metaclass receiver", ctx);
    }

    // Validate that receiver contains metaclass instances
    EObject firstInstance = receiver.getElements().get(0).tryGetInstance();
    if (firstInstance == null) {
      return error("Property access requires metaclass receiver", ctx);
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
              results.add(wrapValue(item));
            }
          } else {
            OCLElement wrapped = wrapValue(value);
            results.add(wrapped);
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
      throw new RuntimeException("Cannot wrap null value");
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

    throw new RuntimeException("Cannot wrap value of type: " + clazz.getName());
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
      return error("Cannot resolve type in oclIsKindOf", ctx);
    }

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      boolean isKind = checkIsKindOf(elem, targetType);
      results.add(new OCLElement.BoolValue(isKind));
    }

    Type resultType = nodeTypes.get(ctx);
    Value result = Value.of(results, resultType != null ? resultType : Type.set(Type.BOOLEAN));
    return result;
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
      return error(
          "Cannot resolve type in oclIsTypeOf "
              + "targetType "
              + targetType
              + " ctx.type.getText(): "
              + ctx.type.getText(),
          ctx);
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
    String qualifiedName = ctx.metamodel.getText() + "::" + ctx.className.getText();
    Type metaclassType = symbolTable.lookupType(qualifiedName);

    if (metaclassType == null || metaclassType == Type.ERROR) {
      return error("Invalid metamodel type: " + qualifiedName, ctx);
    }

    // Start with empty collection of the metaclass type
    Value currentValue = Value.of(List.of(), metaclassType);

    // Process navigation chain (typically .allInstances())
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
      elements.addAll(partValue.getElements());
    }

    return new Value(elements, Type.ANY);
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
  public Value visitCollectionLiteralPartCS(VitruvOCLParser.CollectionLiteralPartCSContext ctx) {
    Value firstValue = visit(ctx.expCS(0));

    // Range syntax: start..end
    if (ctx.expCS().size() == 2) {
      Value secondValue = visit(ctx.expCS(1));

      if (firstValue.size() != 1 || secondValue.size() != 1) {
        return error("Range bounds must be singleton integers", ctx);
      }

      OCLElement firstElem = firstValue.getElements().get(0);
      OCLElement secondElem = secondValue.getElements().get(0);

      Integer start = firstElem.tryGetInt();
      Integer end = secondElem.tryGetInt();

      if (start == null || end == null) {
        return error("Range bounds must be integers", ctx);
      }

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
      return error("'self' not defined in current context", ctx);
    }

    Value selfValue = selfSymbol.getValue();

    if (selfValue == null) {
      return error("'self' has no value", ctx);
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
      return error("Failed to evaluate initializer for variable '" + varName + "'", ctx);
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
      return error("Empty nested expression", ctx);
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
    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Correspondence operator ~ requires singleton operands", ctx);
    }

    // Extract EObject instances
    EObject leftObject = leftValue.getElements().get(0).tryGetInstance();
    EObject rightObject = rightValue.getElements().get(0).tryGetInstance();

    if (leftObject == null || rightObject == null) {
      return error("Correspondence operator ~ requires object instances", ctx);
    }

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
    List<EObject> allRoots = specification.getAllRootObjects();

    for (EObject root : allRoots) {
      if (root.eClass().getName().equals("Correspondences")) {

        EStructuralFeature correspondencesFeature =
            root.eClass().getEStructuralFeature("correspondences");

        if (correspondencesFeature != null) {
          Object value = root.eGet(correspondencesFeature);
          if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<EObject> correspondences = (List<EObject>) value;

            for (EObject correspondence : correspondences) {
              if (correspondenceContainsBoth(correspondence, obj1, obj2)) {
                return true;
              }
            }
          }
        }
      }
    }

    return false;
  }

  /**
   * Checks if a Correspondence object contains both obj1 and obj2.
   *
   * <p>A Correspondence has leftEObjects and rightEObjects features. This method checks if (obj1 in
   * left AND obj2 in right) OR (obj2 in left AND obj1 in right).
   *
   * @param correspondence The Correspondence object to check
   * @param obj1 First object
   * @param obj2 Second object
   * @return true if correspondence relates obj1 and obj2
   */
  private boolean correspondenceContainsBoth(EObject correspondence, EObject obj1, EObject obj2) {
    EStructuralFeature leftFeature = correspondence.eClass().getEStructuralFeature("leftEObjects");
    EStructuralFeature rightFeature =
        correspondence.eClass().getEStructuralFeature("rightEObjects");

    if (leftFeature == null || rightFeature == null) {
      return false;
    }

    Object leftValue = correspondence.eGet(leftFeature);
    Object rightValue = correspondence.eGet(rightFeature);

    if (!(leftValue instanceof List) || !(rightValue instanceof List)) {
      return false;
    }

    @SuppressWarnings("unchecked")
    List<EObject> leftObjects = (List<EObject>) leftValue;
    @SuppressWarnings("unchecked")
    List<EObject> rightObjects = (List<EObject>) rightValue;

    // CRITICAL: Resolve proxies!
    List<EObject> resolvedLeft = new ArrayList<>();
    for (EObject obj : leftObjects) {
      EObject resolved = org.eclipse.emf.ecore.util.EcoreUtil.resolve(obj, correspondence);
      resolvedLeft.add(resolved);
    }

    List<EObject> resolvedRight = new ArrayList<>();
    for (EObject obj : rightObjects) {
      EObject resolved = org.eclipse.emf.ecore.util.EcoreUtil.resolve(obj, correspondence);
      resolvedRight.add(resolved);
    }

    // Check both directions with resolved objects
    boolean forwardMatch = resolvedLeft.contains(obj1) && resolvedRight.contains(obj2);
    boolean reverseMatch = resolvedLeft.contains(obj2) && resolvedRight.contains(obj1);

    return forwardMatch || reverseMatch;
  }

  /**
   * Evaluates select(~) shorthand for correspondence filtering.
   *
   * <p>Desugars to: {@code select(x | self ~ x)}
   *
   * <p>Filters the receiver collection to keep only elements that correspond to 'self' according to
   * the loaded Correspondence model instances.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * context Spacecraft inv:
   *   Satellite.allInstances().select(~).notEmpty()
   * }</pre>
   *
   * @param ctx The select(~) operation node
   * @return Filtered collection containing only elements corresponding to 'self'
   */
  @Override
  public Value visitSelectCorrespondence(VitruvOCLParser.SelectCorrespondenceContext ctx) {
    Value receiver = receiverStack.peek();

    // Get 'self' from symbol table
    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) {
      return error("'self' not defined in current context", ctx);
    }

    Value selfValue = selfSymbol.getValue();
    if (selfValue.size() != 1) {
      return error("select(~) requires singleton 'self'", ctx);
    }

    EObject selfObject = selfValue.getElements().get(0).tryGetInstance();
    if (selfObject == null) {
      return error("select(~) requires 'self' to be an object instance", ctx);
    }

    // Filter receiver collection by correspondence
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      EObject elemObject = elem.tryGetInstance();
      if (elemObject != null && checkCorrespondence(selfObject, elemObject)) {
        results.add(elem);
      }
    }

    return Value.of(results, receiver.getRuntimeType());
  }

  /**
   * Evaluates reject(~) shorthand for inverse correspondence filtering.
   *
   * <p>Desugars to: {@code reject(x | self ~ x)}
   *
   * <p>Filters the receiver collection to keep only elements that do NOT correspond to 'self'
   * according to the loaded Correspondence model instances.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * context Satellite inv:
   *   Spacecraft.allInstances().reject(~).isEmpty()
   * }</pre>
   *
   * @param ctx The reject(~) operation node
   * @return Filtered collection containing only elements NOT corresponding to 'self'
   */
  @Override
  public Value visitRejectCorrespondence(VitruvOCLParser.RejectCorrespondenceContext ctx) {
    Value receiver = receiverStack.peek();

    // Get 'self' from symbol table
    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) {
      return error("'self' not defined in current context", ctx);
    }

    Value selfValue = selfSymbol.getValue();
    if (selfValue.size() != 1) {
      return error("reject(~) requires singleton 'self'", ctx);
    }

    EObject selfObject = selfValue.getElements().get(0).tryGetInstance();
    if (selfObject == null) {
      return error("reject(~) requires 'self' to be an object instance", ctx);
    }

    // Filter OUT corresponding objects (inverse of select)
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      EObject elemObject = elem.tryGetInstance();
      if (elemObject != null && !checkCorrespondence(selfObject, elemObject)) {
        results.add(elem);
      }
    }

    return Value.of(results, receiver.getRuntimeType());
  }

  /**
   * Evaluates exists(~) shorthand for correspondence existence check.
   *
   * <p>Desugars to: {@code exists(x | self ~ x)}
   *
   * <p>Returns true if at least one element in the receiver collection corresponds to 'self'
   * according to the loaded Correspondence model instances. Short-circuits on first match.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * context Spacecraft inv:
   *   Satellite.allInstances().exists(~)
   * }</pre>
   *
   * @param ctx The exists(~) operation node
   * @return Singleton Boolean: true if any element corresponds to 'self', false otherwise
   */
  @Override
  public Value visitExistsCorrespondence(VitruvOCLParser.ExistsCorrespondenceContext ctx) {
    Value receiver = receiverStack.peek();

    // Get 'self' from symbol table
    VariableSymbol selfSymbol = symbolTable.resolveVariable("self");
    if (selfSymbol == null) {
      return error("'self' not defined in current context", ctx);
    }

    Value selfValue = selfSymbol.getValue();
    if (selfValue.size() != 1) {
      return error("exists(~) requires singleton 'self'", ctx);
    }

    EObject selfObject = selfValue.getElements().get(0).tryGetInstance();
    if (selfObject == null) {
      return error("exists(~) requires 'self' to be an object instance", ctx);
    }

    // Check if ANY element corresponds (short-circuit on first match)
    for (OCLElement elem : receiver.getElements()) {
      EObject elemObject = elem.tryGetInstance();
      if (elemObject != null && checkCorrespondence(selfObject, elemObject)) {
        return Value.boolValue(true);
      }
    }

    return Value.boolValue(false);
  }

  // ==================== Not Yet Implemented Features ====================
  /**
   * Placeholder for message operator (^).
   *
   * <p>Not yet implemented - reports error.
   */
  @Override
  public Value visitMessage(VitruvOCLParser.MessageContext ctx) {
    return error("Message operator '^' not yet implemented", ctx);
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
    return error("PropertyNav needs receiver context", ctx);
  }

  /**
   * Error: operation call visited without receiver context.
   *
   * <p>Should be visited via {@link #visitOperationCallWithReceiver}.
   */
  @Override
  public Value visitOperationNav(VitruvOCLParser.OperationNavContext ctx) {
    return error("OperationNav needs receiver context", ctx);
  }

  /**
   * Error: property access visited without receiver.
   *
   * <p>Should be visited via {@link #visitPropertyAccessWithReceiver}.
   */
  @Override
  public Value visitPropertyAccess(VitruvOCLParser.PropertyAccessContext ctx) {
    return error("PropertyAccess needs receiver context", ctx);
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

  /**
   * Sets the token stream for potential future use.
   *
   * @param tokens The ANTLR token stream
   */
  public void setTokenStream(org.antlr.v4.runtime.TokenStream tokens) {
    this.tokens = tokens;
  }

  /**
   * Normalizes a type to its singleton ctype.
   *
   * <p>Every expression has a ctype χ = τ[l,r](μ,ω). Bare primitive types (INTEGER, STRING, etc.)
   * and bare metaclass types are implicitly ¡T![1,1]. This method makes that wrapping explicit so
   * all downstream operations can rely on it.
   *
   * <p>Multi-valued collections ({T}, [T], etc.) are returned unchanged.
   *
   * @param t the type to normalize
   * @return ¡t! if t is a bare scalar, t unchanged if already wrapped
   */
  private Type normalizeToSingleton(Type t) {
    if (t == Type.ERROR || t == Type.ANY) {
      return t;
    }
    if (t.isCollection()) {
      return t;
    } // {T}, [T], <T>, {{T}} — already proper ctype
    if (t.isSingleton()) {
      return t;
    } // !T! — already wrapped
    if (t.isOptional()) {
      return t;
    } // ?T? — already wrapped
    return Type.singleton(t); // bare INTEGER, STRING, cad::Sphere → !T!
  }

  /**
   * Unwraps one level of collection/singleton to get the scalar member type.
   *
   * <p>Used when an operation needs to work on the member type τ of a ctype χ = τ[l,r].
   *
   * @param t the ctype to unwrap
   * @return the member type τ, or t if it has no wrapper
   */
  private Type unwrapOne(Type t) {
    if (t == Type.ERROR || t == Type.ANY) {
      return t;
    }
    if (t.isCollection() || t.isSingleton() || t.isOptional()) {
      return t.getElementType();
    }
    return t; // bare type — already scalar
  }
}
