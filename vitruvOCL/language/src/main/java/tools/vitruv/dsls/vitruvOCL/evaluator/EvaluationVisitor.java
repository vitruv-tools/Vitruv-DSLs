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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLBaseVisitor;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
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
        tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR,
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
        tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR,
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

  // ==================== Classifier Context (Constraint Evaluation) ====================

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
          allResults.add(invResult.getElements().get(0));
        }
      }

      symbolTable.exitScope();
    }

    return Value.of(allResults, Type.bag(Type.BOOLEAN));
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
    if (arg.size() != 1) return error("includes() requires singleton", ctx);

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
    if (arg.size() != 1) return error("including() requires singleton", ctx);
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
    if (arg.size() != 1) return error("excluding() requires singleton", ctx);
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
    if (arg.size() != 1) return error("excludes() requires singleton", ctx);
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
   * <p>Returns the sum of all integer elements in the collection.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.sum()} → {@code 6}
   *
   * @param ctx The sum operation node
   * @return A singleton integer value
   */
  @Override
  public Value visitSumOp(VitruvOCLParser.SumOpContext ctx) {
    Value receiver = receiverStack.peek();
    int sum = 0;
    for (OCLElement elem : receiver.getElements()) {
      Integer value = elem.tryGetInt();
      if (value == null) {
        return error("sum() requires integers", ctx);
      }
      sum += value;
    }
    return Value.intValue(sum);
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
    if (receiver.isEmpty()) return Value.empty(Type.INTEGER);
    int max = Integer.MIN_VALUE;
    for (OCLElement elem : receiver.getElements()) {
      Integer val = elem.tryGetInt();
      if (val == null) {
        return error("max() requires integers", ctx);
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
    if (receiver.isEmpty()) return Value.empty(Type.INTEGER);
    int min = Integer.MAX_VALUE;
    for (OCLElement elem : receiver.getElements()) {
      Integer val = elem.tryGetInt();
      if (val == null) {
        return error("min() requires integers", ctx);
      }
      if (val < min) min = val;
    }
    return Value.intValue(min);
  }

  /**
   * Evaluates the {@code avg()} operation.
   *
   * <p>Returns the average (integer division) of all elements in the collection.
   *
   * <p><b>Example:</b> {@code Bag{1,2,3}.avg()} → {@code 2}
   *
   * @param ctx The avg operation node
   * @return A singleton integer value, or empty if collection is empty
   */
  @Override
  public Value visitAvgOp(VitruvOCLParser.AvgOpContext ctx) {
    Value receiver = receiverStack.peek();
    if (receiver.isEmpty()) return Value.empty(Type.INTEGER);
    int sum = 0;
    for (OCLElement elem : receiver.getElements()) {
      Integer value = elem.tryGetInt();
      if (value == null) {
        return error("avg() requires integers", ctx);
      }
      sum += value;
    }
    return Value.intValue(sum / receiver.size());
  }

  /**
   * Evaluates the {@code abs()} operation.
   *
   * <p>Returns a collection with absolute values of all elements.
   *
   * <p><b>Example:</b> {@code Bag{-1, 2, -3}.abs()} → {@code Bag{1, 2, 3}}
   *
   * @param ctx The abs operation node
   * @return A collection with absolute values
   */
  @Override
  public Value visitAbsOp(VitruvOCLParser.AbsOpContext ctx) {
    Value receiver = receiverStack.peek();
    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      Integer val = elem.tryGetInt();
      if (val == null) {
        return error("abs() requires integers", ctx);
      }
      results.add(new OCLElement.IntValue(Math.abs(val)));
    }
    return Value.of(results, receiver.getRuntimeType());
  }

  /**
   * Evaluates the {@code floor()} operation.
   *
   * <p>For integer collections, this is a no-op (returns the receiver unchanged).
   *
   * @param ctx The floor operation node
   * @return The unchanged receiver
   */
  @Override
  public Value visitFloorOp(VitruvOCLParser.FloorOpContext ctx) {
    return receiverStack.peek(); // No-op for integers
  }

  /**
   * Evaluates the {@code ceil()} operation.
   *
   * <p>For integer collections, this is a no-op (returns the receiver unchanged).
   *
   * @param ctx The ceil operation node
   * @return The unchanged receiver
   */
  @Override
  public Value visitCeilOp(VitruvOCLParser.CeilOpContext ctx) {
    return receiverStack.peek();
  }

  /**
   * Evaluates the {@code round()} operation.
   *
   * <p>For integer collections, this is a no-op (returns the receiver unchanged).
   *
   * @param ctx The round operation node
   * @return The unchanged receiver
   */
  @Override
  public Value visitRoundOp(VitruvOCLParser.RoundOpContext ctx) {
    return receiverStack.peek();
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
   * Evaluates select operation with a single iterator variable.
   *
   * <p>Implements OCL select semantics: filters collection by predicate, returning elements where
   * predicate evaluates to true.
   *
   * <p>Creates a fresh local scope, binds each element to the iterator variable, evaluates the
   * predicate, and collects matching elements. The predicate must return singleton Boolean for each
   * iteration.
   *
   * @param ctx Parser context for select operation
   * @param receiver Collection to filter
   * @param iterVar Name of iterator variable to bind
   * @return Collection of elements satisfying the predicate, or error Value if predicate returns
   *     non-Boolean or non-singleton
   */
  private Value evaluateSelectSingleVar(
      VitruvOCLParser.SelectOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element to iterator variable
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, elemType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), elemType));
        symbolTable.defineVariable(iterSymbol);

        // Evaluate predicate with current binding
        Value bodyResult = visit(ctx.body);
        if (bodyResult.size() != 1) {
          return error("select predicate must return singleton Boolean", ctx);
        }

        Boolean condition = bodyResult.getElements().get(0).tryGetBool();
        if (condition == null) {
          return error("select predicate must return Boolean", ctx);
        }

        // Collect elements where predicate is true
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
   * <p>Implements OCL two-variable select: iterates over all pairs (e1, e2) from the receiver
   * collection, evaluates predicate with both variables bound, and collects pairs where predicate
   * is true.
   *
   * <p>Creates nested iteration over the collection, binding both iterator variables for each pair.
   * Results are flattened into a single collection containing all elements from matching pairs.
   *
   * @param ctx Parser context for select operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Collection containing both elements of each matching pair, or error Value if predicate
   *     returns non-Boolean or non-singleton
   */
  private Value evaluateSelectTwoVars(
      VitruvOCLParser.SelectOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      List<OCLElement> elements = receiver.getElements();

      // Nested iteration: for each pair (e1, e2)
      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables
          VariableSymbol var1Symbol = new VariableSymbol(var1, elemType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), elemType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, elemType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), elemType));
          symbolTable.defineVariable(var2Symbol);

          // Evaluate predicate
          Value bodyResult = visit(ctx.body);
          if (bodyResult.size() != 1) {
            return error("select predicate must return singleton Boolean", ctx);
          }

          Boolean condition = bodyResult.getElements().get(0).tryGetBool();
          if (condition == null) {
            return error("select predicate must return Boolean", ctx);
          }

          // Add pair if predicate is true
          if (condition) {
            // Return both elements (could create tuple, or just first element)
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
   * Evaluates reject operation with a single iterator variable.
   *
   * <p>Implements OCL reject semantics: filters collection by negated predicate, returning elements
   * where predicate evaluates to false. Inverse of select.
   *
   * <p>Creates a fresh local scope, binds each element to the iterator variable, evaluates the
   * predicate, and collects non-matching elements. The predicate must return singleton Boolean for
   * each iteration.
   *
   * @param ctx Parser context for reject operation
   * @param receiver Collection to filter
   * @param iterVar Name of iterator variable to bind
   * @return Collection of elements NOT satisfying the predicate, or error Value if predicate
   *     returns non-Boolean or non-singleton
   */
  private Value evaluateRejectSingleVar(
      VitruvOCLParser.RejectOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element to iterator variable
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, elemType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), elemType));
        symbolTable.defineVariable(iterSymbol);

        // Evaluate predicate with current binding
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
   * <p>Implements OCL two-variable reject: iterates over all pairs (e1, e2) from the receiver
   * collection, evaluates predicate with both variables bound, and collects pairs where predicate
   * is false. Inverse of two-variable select.
   *
   * <p>Creates nested iteration over the collection, binding both iterator variables for each pair.
   * Results are flattened into a single collection containing all elements from non-matching pairs.
   *
   * @param ctx Parser context for reject operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Collection containing both elements of each non-matching pair, or error Value if
   *     predicate returns non-Boolean or non-singleton
   */
  private Value evaluateRejectTwoVars(
      VitruvOCLParser.RejectOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables
          VariableSymbol var1Symbol = new VariableSymbol(var1, elemType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), elemType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, elemType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), elemType));
          symbolTable.defineVariable(var2Symbol);

          // Evaluate predicate
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
   * Evaluates collect operation with a single iterator variable.
   *
   * <p>Implements OCL collect semantics: transforms each element by applying the body expression,
   * then flattens all results into a single collection. Unlike select/reject, the body can return
   * any type (not just Boolean).
   *
   * <p>Creates a fresh local scope, binds each element to the iterator variable, evaluates the
   * transformation expression, and aggregates all resulting elements into a flat collection.
   *
   * @param ctx Parser context for collect operation
   * @param receiver Collection to transform
   * @param iterVar Name of iterator variable to bind
   * @return Flattened collection of all transformation results, typed according to type checker's
   *     analysis or Set(Any) as fallback
   */
  private Value evaluateCollectSingleVar(
      VitruvOCLParser.CollectOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element to iterator variable
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, elemType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), elemType));
        symbolTable.defineVariable(iterSymbol);

        // Evaluate transformation expression and flatten into results
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
   * <p>Implements OCL two-variable collect: iterates over all pairs (e1, e2) from the receiver
   * collection, applies transformation to each pair, and flattens all results into a single
   * collection.
   *
   * <p>Creates nested iteration over the collection, binding both iterator variables for each pair,
   * evaluating the body expression, and aggregating results. Useful for pairwise transformations or
   * relationship queries.
   *
   * @param ctx Parser context for collect operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Flattened collection of all transformation results from each pair, typed according to
   *     type checker's analysis or Set(Any) as fallback
   */
  private Value evaluateCollectTwoVars(
      VitruvOCLParser.CollectOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> results = new ArrayList<>();
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables
          VariableSymbol var1Symbol = new VariableSymbol(var1, elemType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), elemType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, elemType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), elemType));
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
   * Evaluates forAll operation with a single iterator variable.
   *
   * <p>Implements OCL forAll semantics: returns true if all elements satisfy the predicate, false
   * if any element fails. Short-circuits on first failure.
   *
   * <p>Creates a fresh local scope, binds each element sequentially, and evaluates the predicate.
   * The predicate must return singleton Boolean for each iteration.
   *
   * @param ctx Parser context for forAll operation
   * @param receiver Collection to check
   * @param iterVar Name of iterator variable to bind
   * @return Singleton Boolean collection: [true] if all elements satisfy predicate, [false] if any
   *     fails, or error Value if predicate returns non-Boolean
   */
  private Value evaluateForAllSingleVar(
      VitruvOCLParser.ForAllOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element to iterator variable
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, elemType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), elemType));
        symbolTable.defineVariable(iterSymbol);

        // Evaluate predicate with current binding
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
   * <p>Implements OCL two-variable forAll: checks if predicate holds for all pairs (e1, e2) from
   * the receiver collection. Short-circuits on first failure.
   *
   * <p>Creates nested iteration over the collection, binding both iterator variables for each pair
   * and evaluating the predicate. Useful for universal pairwise constraints like transitivity or
   * symmetry checks.
   *
   * @param ctx Parser context for forAll operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Singleton Boolean collection: [true] if predicate holds for all pairs, [false] if any
   *     pair fails, or error Value if predicate returns non-Boolean
   */
  private Value evaluateForAllTwoVars(
      VitruvOCLParser.ForAllOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables
          VariableSymbol var1Symbol = new VariableSymbol(var1, elemType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), elemType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, elemType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), elemType));
          symbolTable.defineVariable(var2Symbol);

          // Evaluate predicate
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
   * Evaluates exists operation with a single iterator variable.
   *
   * <p>Implements OCL exists semantics: returns true if at least one element satisfies the
   * predicate, false if none do. Short-circuits on first match.
   *
   * <p>Creates a fresh local scope for the iterator variable, binds each collection element
   * sequentially, and evaluates the body expression. The predicate must return a singleton Boolean
   * for each iteration.
   *
   * @param ctx Parser context for exists operation
   * @param receiver Collection to iterate over
   * @param iterVar Name of iterator variable to bind
   * @return Singleton Boolean collection: [true] if any element satisfies predicate, [false] if
   *     none do, or error Value if predicate returns non-Boolean
   */
  private Value evaluateExistsSingleVar(
      VitruvOCLParser.ExistsOpContext ctx, Value receiver, String iterVar) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      for (OCLElement elem : receiver.getElements()) {
        // Bind current element to iterator variable
        VariableSymbol iterSymbol = new VariableSymbol(iterVar, elemType, iterScope, true);
        iterSymbol.setValue(new Value(List.of(elem), elemType));
        symbolTable.defineVariable(iterSymbol);

        // Evaluate predicate with current binding
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
   * <p>Implements OCL two-variable exists: returns true if at least one pair (e1, e2) satisfies the
   * predicate. Short-circuits on first match.
   *
   * <p>Creates nested iteration over the collection, binding both iterator variables for each pair
   * and evaluating the predicate. Useful for existential pairwise constraints like finding related
   * elements or checking relationships.
   *
   * @param ctx Parser context for exists operation
   * @param receiver Collection to iterate over
   * @param var1 Name of first iterator variable
   * @param var2 Name of second iterator variable
   * @return Singleton Boolean collection: [true] if any pair satisfies predicate, [false] if no
   *     pairs do, or error Value if predicate returns non-Boolean
   */
  private Value evaluateExistsTwoVars(
      VitruvOCLParser.ExistsOpContext ctx, Value receiver, String var1, String var2) {
    Type elemType = receiver.getRuntimeType().getElementType();
    LocalScope iterScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iterScope);

    try {
      List<OCLElement> elements = receiver.getElements();

      for (OCLElement elem1 : elements) {
        for (OCLElement elem2 : elements) {
          // Bind both iterator variables
          VariableSymbol var1Symbol = new VariableSymbol(var1, elemType, iterScope, true);
          var1Symbol.setValue(new Value(List.of(elem1), elemType));
          symbolTable.defineVariable(var1Symbol);

          VariableSymbol var2Symbol = new VariableSymbol(var2, elemType, iterScope, true);
          var2Symbol.setValue(new Value(List.of(elem2), elemType));
          symbolTable.defineVariable(var2Symbol);

          // Evaluate predicate
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
   * Evaluates multiplicative operations (* and /).
   *
   * <p>Both operands must be singleton integers. Division by zero is reported as an error.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code 6 * 7} → {@code 42}
   *   <li>{@code 10 / 2} → {@code 5}
   * </ul>
   *
   * @param ctx The multiplicative operation node
   * @return A singleton integer value
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

    Integer left = leftElem.tryGetInt();
    Integer right = rightElem.tryGetInt();

    if (left == null || right == null) {
      return error("Arithmetic requires integer operands", ctx);
    }

    if (operator.equals("/") && right == 0) {
      return error("Division by zero", ctx);
    }

    int result = operator.equals("*") ? left * right : left / right;
    return Value.intValue(result);
  }

  /**
   * Evaluates additive operations (+ and -).
   *
   * <p>Both operands must be singleton integers.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code 3 + 4} → {@code 7}
   *   <li>{@code 10 - 3} → {@code 7}
   * </ul>
   *
   * @param ctx The additive operation node
   * @return A singleton integer value
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

    Integer left = leftElem.tryGetInt();
    Integer right = rightElem.tryGetInt();

    if (left == null || right == null) {
      return error("Arithmetic requires integer operands", ctx);
    }

    int result = operator.equals("+") ? left + right : left - right;
    return Value.intValue(result);
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
   * Evaluates less-than comparison (<). Both operands must be singletons for comparison.
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
   * Evaluates less-than-or-equal comparison (<=). Both operands must be singletons for comparison.
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
   * Evaluates greater-than comparison (>). Both operands must be singletons for comparison.
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
    Integer value = elem.tryGetInt();
    if (value == null) {
      return error("Unary minus requires integer operand", ctx);
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
            results.add(wrapValue(value));
          }
        }
      }
    }

    Type resultType = nodeTypes.get(ctx);
    return Value.of(results, resultType != null ? resultType : Type.set(Type.ANY));
  }

  /**
   * Wraps a Java value into an {@link OCLElement}.
   *
   * <p>Converts EMF attribute values and EObject instances into their OCL runtime representations.
   *
   * @param value The Java value to wrap (must not be null)
   * @return The wrapped OCL element
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
    if (EObject.class.isAssignableFrom(clazz)) {
      return new OCLElement.MetaclassValue((EObject) value);
    }

    throw new RuntimeException("Cannot wrap: " + value.getClass());
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
      return error("Cannot resolve type in oclIsTypeOf", ctx);
    }

    List<OCLElement> results = new ArrayList<>();
    for (OCLElement elem : receiver.getElements()) {
      results.add(new OCLElement.BoolValue(checkIsKindOf(elem, targetType)));
    }

    Type resultType = nodeTypes.get(ctx);
    return Value.of(results, resultType != null ? resultType : Type.set(Type.BOOLEAN));
  }

  /**
   * Evaluates the {@code oclAsType()} operation.
   *
   * <p>Type cast operation. Currently returns the receiver with updated type information.
   *
   * @param ctx The oclAsType operation node
   * @return The receiver with the target type
   */
  @Override
  public Value visitOclAsTypeOp(VitruvOCLParser.OclAsTypeOpContext ctx) {
    Value receiver = receiverStack.peek();
    Type resultType = nodeTypes.get(ctx);
    return Value.of(
        receiver.getElements(), resultType != null ? resultType : receiver.getRuntimeType());
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
    int intValue = Integer.parseInt(text);
    return Value.intValue(intValue);
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
   * Evaluates variable references.
   *
   * <p>Looks up the variable in the symbol table and returns its bound value.
   *
   * <p><b>Example:</b> In {@code let x = 5 in x + 1}, the second {@code x} is resolved here.
   *
   * @param ctx The variable expression node
   * @return The value bound to the variable
   */
  @Override
  public Value visitVariableExpCS(VitruvOCLParser.VariableExpCSContext ctx) {
    String varName = ctx.varName.getText();

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

  // ==================== Delegation & Default Implementations ====================

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

  // ==================== Not Yet Implemented Features ====================

  /**
   * Placeholder for correspondence operator (~).
   *
   * <p>Not yet implemented - reports error.
   */
  @Override
  public Value visitCorrespondence(VitruvOCLParser.CorrespondenceContext ctx) {
    return error("Correspondence operator '~' not yet implemented", ctx);
  }

  /**
   * Placeholder for message operator (^).
   *
   * <p>Not yet implemented - reports error.
   */
  @Override
  public Value visitMessage(VitruvOCLParser.MessageContext ctx) {
    return error("Message operator '^' not yet implemented", ctx);
  }

  // ==================== Type-Related Nodes (No Runtime Value) ====================

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

  // ==================== Error States (Require Receiver Context) ====================

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
}
