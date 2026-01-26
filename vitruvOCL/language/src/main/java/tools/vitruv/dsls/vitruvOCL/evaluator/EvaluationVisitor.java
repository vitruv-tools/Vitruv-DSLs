package tools.vitruv.dsls.vitruvOCL.evaluator;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EClass;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.NameContext;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintSpecification;
import tools.vitruv.dsls.vitruvOCL.symboltable.*;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * static helper methods for type resolution and type operations. shared type resolution logic that
 * can be used by both TypeCheckVisitor and, the evaluator
 *
 * <p>reasoning: this logic is the same for type checking and evaluation, therefore centralised in
 * helper class
 *
 * @see Type type system
 * @see TypeCheckVisitor TypeResolver for type checking
 */
public class EvaluationVisitor extends AbstractPhaseVisitor<Value> {

  /** Pre-computed Types aus Pass 2 - für Type-dependent Operations. */
  private final ParseTreeProperty<Type> nodeTypes;

  public EvaluationVisitor(
      SymbolTable symbolTable,
      ConstraintSpecification specification,
      ErrorCollector errors,
      ParseTreeProperty<Type> nodeTypes) {
    super(symbolTable, specification, errors);
    this.nodeTypes = nodeTypes;
  }

  @Override
  protected void handleUndefinedSymbol(String name, org.antlr.v4.runtime.ParserRuleContext ctx) {
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        "Variable not bound: " + name,
        tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR,
        "evaluator");
  }

  // ==================== Visitor Methods ====================

  @Override
  public Value visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
    // Evaluate the expression inside the parentheses
    List<VitruvOCLParser.ExpCSContext> exps = ctx.expCS();

    if (exps.isEmpty()) {
      return error("Empty nested expression", ctx);
    }

    // Evaluate all expressions, return value of last one
    Value result = null;
    for (VitruvOCLParser.ExpCSContext exp : exps) {
      result = visit(exp);
    }

    return result;
  }

  @Override
  public Value visitNumber(VitruvOCLParser.NumberContext ctx) {
    String text = ctx.NumberLiteralExpCS().getText();
    int intValue = Integer.parseInt(text);
    return Value.intValue(intValue); // Singleton [intValue]
  }

  @Override
  public Value visitPlusMinus(VitruvOCLParser.PlusMinusContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);
    String operator = ctx.op.getText();

    // Expect singletons for arithmetic
    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Arithmetic requires singleton operands", ctx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    if (!(leftElem instanceof OCLElement.IntValue) || !(rightElem instanceof OCLElement.IntValue)) {
      return error("Arithmetic requires integer operands", ctx);
    }

    int left = ((OCLElement.IntValue) leftElem).value();
    int right = ((OCLElement.IntValue) rightElem).value();

    int result = operator.equals("+") ? left + right : left - right;
    return Value.intValue(result);
  }

  @Override
  public Value visitTimesDivide(VitruvOCLParser.TimesDivideContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);
    String operator = ctx.op.getText();

    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Arithmetic requires singleton operands", ctx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    if (!(leftElem instanceof OCLElement.IntValue) || !(rightElem instanceof OCLElement.IntValue)) {
      return error("Arithmetic requires integer operands", ctx);
    }

    int left = ((OCLElement.IntValue) leftElem).value();
    int right = ((OCLElement.IntValue) rightElem).value();

    if (operator.equals("/") && right == 0) {
      return error("Division by zero", ctx);
    }

    int result = operator.equals("*") ? left * right : left / right;
    return Value.intValue(result);
  }

  // ==================== Collection Literals ====================

  @Override
  public Value visitCollectionTypeCS(VitruvOCLParser.CollectionTypeCSContext ctx) {
    Type type = nodeTypes.get(ctx);
    return Value.empty(type);
  }

  @Override
  public Value visitCollectionLiteralExpCS(VitruvOCLParser.CollectionLiteralExpCSContext ctx) {
    // Get Ctype from Pass 2
    Type collectionType = nodeTypes.get(ctx);

    // Empty collection: Set{}
    if (ctx.argument == null) {
      return Value.empty(collectionType);
    }

    // Evaluate arguments
    Value argumentsValue = visit(ctx.argument);
    List<OCLElement> elements = argumentsValue.getElements();

    // Create collection
    Value collection = Value.of(elements, collectionType);

    // Apply uniqueness if Set/OrderedSet
    if (collectionType.isUnique()) {
      collection = collection.removeDuplicates();
    }

    return collection;
  }

  @Override
  public Value visitCollectionArguments(VitruvOCLParser.CollectionArgumentsContext ctx) {
    List<OCLElement> elements = new ArrayList<>();

    for (VitruvOCLParser.CollectionLiteralPartCSContext partCtx : ctx.collectionLiteralPartCS()) {
      Value partValue = visit(partCtx);
      elements.addAll(partValue.getElements());
    }

    return new Value(elements, Type.ANY);
  }

  @Override
  public Value visitCollectionLiteralPartCS(VitruvOCLParser.CollectionLiteralPartCSContext ctx) {
    Value firstValue = visit(ctx.expCS(0));

    // Check for range: 1..10
    if (ctx.expCS().size() == 2) {
      Value secondValue = visit(ctx.expCS(1));

      // Extract bounds
      if (firstValue.size() != 1 || secondValue.size() != 1) {
        return error("Range bounds must be singleton integers", ctx);
      }

      OCLElement firstElem = firstValue.getElements().get(0);
      OCLElement secondElem = secondValue.getElements().get(0);

      if (!(firstElem instanceof OCLElement.IntValue)
          || !(secondElem instanceof OCLElement.IntValue)) {
        return error("Range bounds must be integers", ctx);
      }

      int start = ((OCLElement.IntValue) firstElem).value();
      int end = ((OCLElement.IntValue) secondElem).value();

      // Generate range
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

    // Single element
    return firstValue;
  }

  /**
   * Evaluates navigation chains: source.operation1().operation2()
   *
   * <p>For Collection Operations like including/excluding, we need the receiver value which is only
   * available at this level of the parse tree.
   */
  @Override
  public Value visitPrefixedExpCS(VitruvOCLParser.PrefixedExpCSContext ctx) {
    // Handle Metamodel::Class.operation() pattern
    if (ctx.metamodel != null && ctx.className != null) {
      Type metaclassType = nodeTypes.get(ctx);

      if (metaclassType == null || metaclassType == Type.ERROR) {
        throw new RuntimeException("Invalid metamodel type at " + ctx.getText());
      }

      // Start with metaclass placeholder
      Value currentValue = Value.of(List.of(), Type.singleton(metaclassType));

      // Evaluate navigation operations
      for (VitruvOCLParser.PrimaryExpCSContext primary : ctx.primaryExpCS()) {
        currentValue = visitPrimaryExpCSWithReceiver(primary, currentValue);
      }

      return currentValue;
    }

    // Count how many unary operators we have at the start
    int unaryOpCount = 0;
    for (int i = 0; i < ctx.getChildCount(); i++) {
      String text = ctx.getChild(i).getText();
      if (text.equals("-") || text.equals("not")) {
        unaryOpCount++;
      } else {
        break;
      }
    }

    List<VitruvOCLParser.PrimaryExpCSContext> primaries = ctx.primaryExpCS();

    if (primaries.isEmpty()) {
      return error("Empty expression", ctx);
    }

    Value currentValue = visit(primaries.get(0));

    // Apply unary operators
    for (int i = unaryOpCount - 1; i >= 0; i--) {
      String op = ctx.getChild(i).getText();

      if (op.equals("-")) {
        if (currentValue.size() != 1) {
          return error("Unary minus requires singleton operand", ctx);
        }
        OCLElement elem = currentValue.getElements().get(0);
        if (!(elem instanceof OCLElement.IntValue)) {
          return error("Unary minus requires integer operand", ctx);
        }
        int value = ((OCLElement.IntValue) elem).value();
        currentValue = Value.intValue(-value);
      } else if (op.equals("not")) {
        if (currentValue.size() != 1) {
          return error("Unary not requires singleton operand", ctx);
        }
        OCLElement elem = currentValue.getElements().get(0);
        if (!(elem instanceof OCLElement.BoolValue)) {
          return error("Unary not requires boolean operand", ctx);
        }
        boolean value = ((OCLElement.BoolValue) elem).value();
        currentValue = Value.boolValue(!value);
      }
    }

    // Handle navigation chain
    for (int i = 1; i < primaries.size(); i++) {
      VitruvOCLParser.PrimaryExpCSContext operationCtx = primaries.get(i);

      if (operationCtx.navigatingExpCS() != null) {
        VitruvOCLParser.NavigatingExpCSContext navCtx = operationCtx.navigatingExpCS();

        boolean handledAsCollectionOp = false;

        if (navCtx.opName != null && navCtx.opName.nameExpCS() != null) {
          VitruvOCLParser.NameExpCSContext nameExpCtx = navCtx.opName.nameExpCS();

          if (nameExpCtx instanceof VitruvOCLParser.NameContext nameContext) {
            if (nameContext.collectionOperationName() != null) {
              String opName = nameContext.collectionOperationName().getText();
              currentValue = evaluateCollectionOperation(navCtx, opName, currentValue);
              handledAsCollectionOp = true;
            } else if (nameContext.stringOperationName() != null) {
              String opName = nameContext.stringOperationName().getText();
              currentValue = evaluateStringOperation(navCtx, opName, currentValue);
              handledAsCollectionOp = true;
            }
          }
        }

        if (!handledAsCollectionOp) {
          if (!navCtx.navigatingArgCS().isEmpty()) {
            for (VitruvOCLParser.NavigatingArgCSContext arg : navCtx.navigatingArgCS()) {
              currentValue = handleNavigationStep(arg, currentValue);
            }
          } else {
            String opName = navCtx.opName.nameExpCS().getText();

            switch (opName) {
              case "size":
                currentValue = Value.intValue(currentValue.size());
                break;
              case "isEmpty":
                currentValue = Value.boolValue(currentValue.isEmpty());
                break;
              case "notEmpty":
                currentValue = Value.boolValue(currentValue.notEmpty());
                break;
              case "first":
                currentValue = currentValue.first();
                break;
              case "last":
                currentValue = currentValue.last();
                break;
              case "reverse":
                currentValue = currentValue.reverse();
                break;
              case "flatten":
                currentValue = currentValue.flatten();
                break;
              default:
                return error("Unknown operation: " + opName, navCtx);
            }
          }
        }
      } else {
        currentValue = visit(operationCtx);
      }
    }

    return currentValue;
  }

  @Override
  public Value visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    // Evaluate invariant expression
    List<VitruvOCLParser.SpecificationCSContext> specs = ctx.specificationCS();
    Value result = Value.boolValue(true);

    for (VitruvOCLParser.SpecificationCSContext spec : specs) {
      result = visit(spec);
    }

    // TODO: Evaluate on all context instances from VSUM
    return result;
  }

  private Value visitPrimaryExpCSWithReceiver(
      VitruvOCLParser.PrimaryExpCSContext ctx, Value receiver) {
    if (ctx.navigatingExpCS() != null) {
      return visitNavigatingExpCSWithReceiver(ctx.navigatingExpCS(), receiver);
    }

    return visit(ctx);
  }

  private Value visitNavigatingExpCSWithReceiver(
      VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
    String opName = ctx.indexExpCS().nameExpCS().getText();

    // Handle metaclass operations
    if (opName.equals("allInstances")) {
      // TODO: Query VSUM for all instances of this metaclass
      // For now, return empty set
      Type resultType = nodeTypes.get(ctx);
      return Value.of(List.of(), resultType);
    }

    throw new RuntimeException("Unknown metaclass operation: " + opName);
  }

  /**
   * Evaluates collection operations: including, excluding, flatten, etc.
   *
   * @param ctx Navigation context containing the operation
   * @param opName Operation name (including, excluding, etc.)
   * @param receiver Receiver value (the collection)
   * @return Result value of the operation
   */
  private Value evaluateCollectionOperation(
      VitruvOCLParser.NavigatingExpCSContext ctx, String opName, Value receiver) {
    switch (opName) {
      case "including":
        {
          // including(x): adds element to collection
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'including' requires 1 argument", ctx);
          }

          Value arg = visit(args.get(0).navigatingArgExpCS());
          if (arg.size() != 1) {
            return error("including() requires exactly 1 singleton argument", ctx);
          }

          OCLElement elem = arg.getElements().get(0);
          return receiver.including(elem);
        }

      case "excluding":
        {
          // excluding(x): removes element from collection
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'excluding' requires 1 argument", ctx);
          }

          Value arg = visit(args.get(0).navigatingArgExpCS());
          if (arg.size() != 1) {
            return error("excluding() requires exactly 1 singleton argument", ctx);
          }

          OCLElement elem = arg.getElements().get(0);
          return receiver.excluding(elem);
        }

      case "includes":
        {
          // includes(x): checks if element is in collection
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'includes' requires 1 argument", ctx);
          }

          Value arg = visit(args.get(0).navigatingArgExpCS());
          if (arg.size() != 1) {
            return error("includes() requires exactly 1 singleton argument", ctx);
          }

          OCLElement elem = arg.getElements().get(0);
          return Value.boolValue(receiver.includes(elem));
        }

      case "excludes":
        {
          // excludes(x): checks if element is NOT in collection
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'excludes' requires 1 argument", ctx);
          }

          Value arg = visit(args.get(0).navigatingArgExpCS());
          if (arg.size() != 1) {
            return error("excludes() requires exactly 1 singleton argument", ctx);
          }

          OCLElement elem = arg.getElements().get(0);
          return Value.boolValue(receiver.excludes(elem));
        }

      case "flatten":
        {
          // flatten(): flattens nested collections
          return receiver.flatten();
        }

      case "union":
        {
          // union(collection): set union
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'union' requires 1 argument", ctx);
          }

          Value arg = visit(args.get(0).navigatingArgExpCS());
          return receiver.union(arg);
        }

      case "append":
        {
          // append(collection): sequence append (alias for union in our implementation)
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'append' requires 1 argument", ctx);
          }

          Value arg = visit(args.get(0).navigatingArgExpCS());
          return receiver.union(arg); // In OCL#, append is essentially union for sequences
        }
      case "sum":
        {
          int sum = 0;
          for (OCLElement elem : receiver.getElements()) {
            if (!(elem instanceof OCLElement.IntValue)) {
              return error("sum() requires integer elements", ctx);
            }
            sum += ((OCLElement.IntValue) elem).value();
          }
          return Value.intValue(sum); // 0 if empty
        }
      case "max":
        {
          // max(): Collection(Collection(Integer)) → Collection(Integer)
          if (receiver.isEmpty()) {
            return Value.empty(Type.INTEGER); // Empty collection
          }

          int max = Integer.MIN_VALUE;
          for (OCLElement elem : receiver.getElements()) {
            if (!(elem instanceof OCLElement.IntValue)) {
              return error("max() requires integer elements", ctx);
            }
            int val = ((OCLElement.IntValue) elem).value();
            if (val > max) {
              max = val;
            }
          }
          return Value.intValue(max); // Returns [max]
        }

      case "min":
        {
          // min(): Collection(Collection(Integer)) → Collection(Integer)
          if (receiver.isEmpty()) {
            return Value.empty(Type.INTEGER); // Empty collection
          }

          int min = Integer.MAX_VALUE;
          for (OCLElement elem : receiver.getElements()) {
            if (!(elem instanceof OCLElement.IntValue)) {
              return error("min() requires integer elements", ctx);
            }
            int val = ((OCLElement.IntValue) elem).value();
            if (val < min) {
              min = val;
            }
          }
          return Value.intValue(min); // Returns [min]
        }

      case "avg":
        {
          // avg(): Collection(Collection(Integer)) → Collection(Integer)
          if (receiver.isEmpty()) {
            return Value.empty(Type.INTEGER); // Empty collection
          }

          int sum = 0;
          for (OCLElement elem : receiver.getElements()) {
            if (!(elem instanceof OCLElement.IntValue)) {
              return error("avg() requires integer elements", ctx);
            }
            sum += ((OCLElement.IntValue) elem).value();
          }

          int avg = sum / receiver.size();
          return Value.intValue(avg); // Returns [avg]
        }

      case "abs":
        {
          // abs(): Collection(Integer) → Collection(Integer)
          List<OCLElement> result = new ArrayList<>();

          for (OCLElement elem : receiver.getElements()) {
            if (!(elem instanceof OCLElement.IntValue)) {
              return error("abs() requires integer elements", ctx);
            }
            int val = ((OCLElement.IntValue) elem).value();
            result.add(new OCLElement.IntValue(Math.abs(val)));
          }

          Type receiverType = nodeTypes.get(ctx);
          return Value.of(result, receiverType != null ? receiverType : Type.set(Type.INTEGER));
        }

      case "floor":
      case "ceil":
      case "round":
        {
          // For integers: no-op, validate and return
          for (OCLElement elem : receiver.getElements()) {
            if (!(elem instanceof OCLElement.IntValue)) {
              return error(opName + "() requires integer elements", ctx);
            }
          }
          return receiver;
        }

      case "lift":
        {
          // lift(): {a,b,c} → {{a,b,c}}

          // Create NestedCollection element wrapping receiver
          OCLElement.NestedCollection wrappedCollection = new OCLElement.NestedCollection(receiver);

          List<OCLElement> wrappedList = List.of(wrappedCollection);

          // Get receiver type from nodeTypes
          Type receiverType = nodeTypes.get(ctx);
          if (receiverType == null) {
            receiverType = Type.set(Type.INTEGER); // Fallback
          }

          // Create outer collection type (preserve kind)
          Type outerType;
          if (receiverType.isUnique() && receiverType.isOrdered()) {
            outerType = Type.orderedSet(receiverType);
          } else if (receiverType.isUnique()) {
            outerType = Type.set(receiverType);
          } else if (receiverType.isOrdered()) {
            outerType = Type.sequence(receiverType);
          } else {
            outerType = Type.bag(receiverType);
          }

          return Value.of(wrappedList, outerType);
        }
      case "oclIsKindOf":
        {
          // oclIsKindOf(TypeName): Collection(T) → Collection(Boolean)

          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("oclIsKindOf() requires 1 type argument", ctx);
          }

          // Get the target type from type checker
          VitruvOCLParser.NavigatingArgCSContext argCtx = args.get(0);
          Type targetType = nodeTypes.get(argCtx);

          if (targetType == null || targetType == Type.ERROR) {
            return error("Invalid type argument for oclIsKindOf", ctx);
          }

          // Apply to each element
          List<OCLElement> results = new ArrayList<>();
          for (OCLElement elem : receiver.getElements()) {
            boolean isKind = checkIsKindOf(elem, targetType);
            results.add(new OCLElement.BoolValue(isKind));
          }

          // Preserve collection kind, but with Boolean elements
          Type receiverType = nodeTypes.get(ctx);
          Type resultType;
          if (receiverType != null) {
            if (receiverType.isUnique() && receiverType.isOrdered()) {
              resultType = Type.orderedSet(Type.BOOLEAN);
            } else if (receiverType.isUnique()) {
              resultType = Type.set(Type.BOOLEAN);
            } else if (receiverType.isOrdered()) {
              resultType = Type.sequence(Type.BOOLEAN);
            } else {
              resultType = Type.bag(Type.BOOLEAN);
            }
          } else {
            resultType = Type.set(Type.BOOLEAN);
          }

          return Value.of(results, resultType);
        }
      case "select":
        {
          // select(x | predicate): filters elements that satisfy predicate
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'select' requires iterator argument (var | predicate)", ctx);
          }

          if (args.size() > 1) {
            return error(
                "Operation 'select' expects exactly 1 iterator argument, got " + args.size(), ctx);
          }

          return evaluateIteratorOperation(ctx, args.get(0), "select", receiver);
        }

      case "reject":
        {
          // reject(x | predicate): filters elements that DON'T satisfy predicate
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'reject' requires iterator argument (var | predicate)", ctx);
          }

          if (args.size() > 1) {
            return error(
                "Operation 'reject' expects exactly 1 iterator argument, got " + args.size(), ctx);
          }

          return evaluateIteratorOperation(ctx, args.get(0), "reject", receiver);
        }

      case "collect":
        {
          // collect(x | expression): transforms each element with auto-flatten
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'collect' requires iterator argument (var | expression)", ctx);
          }

          if (args.size() > 1) {
            return error(
                "Operation 'collect' expects exactly 1 iterator argument, got " + args.size(), ctx);
          }

          return evaluateIteratorOperation(ctx, args.get(0), "collect", receiver);
        }

      case "forAll":
        {
          // forAll(x | predicate): returns true if all elements satisfy predicate
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'forAll' requires iterator argument (var | predicate)", ctx);
          }

          if (args.size() > 1) {
            return error(
                "Operation 'forAll' expects exactly 1 iterator argument, got " + args.size(), ctx);
          }

          return evaluateIteratorOperation(ctx, args.get(0), "forAll", receiver);
        }

      case "exists":
        {
          // exists(x | predicate): returns true if any element satisfies predicate
          List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
          if (args.isEmpty()) {
            return error("Operation 'exists' requires iterator argument (var | predicate)", ctx);
          }

          if (args.size() > 1) {
            return error(
                "Operation 'exists' expects exactly 1 iterator argument, got " + args.size(), ctx);
          }

          return evaluateIteratorOperation(ctx, args.get(0), "exists", receiver);
        }

      default:
        return error("Unknown collection operation: " + opName, ctx);
    }
  }

  /**
   * Evaluates iterator operations with proper variable scoping.
   *
   * @param navCtx Navigation context (for error reporting)
   * @param argCtx Iterator argument context (contains var | body)
   * @param opName Operation name (select, reject, collect, forAll, exists)
   * @param receiver Collection being iterated over
   * @return Result value of the operation
   */
  private Value evaluateIteratorOperation(
      VitruvOCLParser.NavigatingExpCSContext navCtx,
      VitruvOCLParser.NavigatingArgCSContext argCtx,
      String opName,
      Value receiver) {
    // Extract iterator variable name and body
    VitruvOCLParser.NavigatingArgExpCSContext argExpCtx = argCtx.navigatingArgExpCS();

    // Must have iterator syntax: x | body
    if (argExpCtx.iteratorVar == null || argExpCtx.body == null) {
      return error(
          "Operation '" + opName + "' requires iterator syntax: variableName | expression", navCtx);
    }

    String iteratorVarName = argExpCtx.iteratorVar.getText();

    // Get element type from receiver (from Pass 2)
    Type receiverType = nodeTypes.get(navCtx);
    if (receiverType == null) {
      // Fallback: infer from receiver value
      receiverType = receiver.getRuntimeType();
    }

    Type elementType = receiverType.getElementType();

    // Create new local scope for iterator variable
    LocalScope iteratorScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(iteratorScope);

    try {
      // Prepare result collection
      List<OCLElement> results = new ArrayList<>();

      // Iterate over each element in receiver
      for (OCLElement element : receiver.getElements()) {
        // Bind iterator variable to current element
        // IMPORTANT: Iterator variable gets a SINGLETON value wrapping the element
        Value elementValue = new Value(List.of(element), elementType);

        VariableSymbol iteratorVar =
            new VariableSymbol(
                iteratorVarName, elementType, iteratorScope, true // Mark as iterator variable
                );
        iteratorVar.setValue(elementValue);

        // Define (or redefine) iterator variable for this iteration
        symbolTable.define(iteratorVar);

        // Evaluate body expression with bound iterator variable
        Value bodyResult = visit(argExpCtx.body);

        if (bodyResult == null) {
          return error("Iterator body evaluation failed", argExpCtx.body);
        }

        // Process result based on operation type
        switch (opName) {
          case "select":
            // Keep element if predicate is true
            if (bodyResult.size() != 1) {
              return error("select predicate must return singleton boolean", argExpCtx.body);
            }
            OCLElement predicateElem = bodyResult.getElements().get(0);
            if (!(predicateElem instanceof OCLElement.BoolValue)) {
              return error("select predicate must return boolean", argExpCtx.body);
            }
            boolean selectCondition = ((OCLElement.BoolValue) predicateElem).value();
            if (selectCondition) {
              results.add(element);
            }
            break;

          case "reject":
            // Keep element if predicate is FALSE
            if (bodyResult.size() != 1) {
              return error("reject predicate must return singleton boolean", argExpCtx.body);
            }
            OCLElement rejectPredicateElem = bodyResult.getElements().get(0);
            if (!(rejectPredicateElem instanceof OCLElement.BoolValue)) {
              return error("reject predicate must return boolean", argExpCtx.body);
            }
            boolean rejectCondition = ((OCLElement.BoolValue) rejectPredicateElem).value();
            if (!rejectCondition) { // Note: negated!
              results.add(element);
            }
            break;

          case "collect":
            // Transform element with auto-flatten
            // Body result elements are added directly (flatten)
            results.addAll(bodyResult.getElements());
            break;

          case "forAll":
            // Check if all elements satisfy predicate
            if (bodyResult.size() != 1) {
              return error("forAll predicate must return singleton boolean", argExpCtx.body);
            }
            OCLElement forAllElem = bodyResult.getElements().get(0);
            if (!(forAllElem instanceof OCLElement.BoolValue)) {
              return error("forAll predicate must return boolean", argExpCtx.body);
            }
            boolean forAllCondition = ((OCLElement.BoolValue) forAllElem).value();
            if (!forAllCondition) {
              // Short-circuit: return false immediately
              return Value.boolValue(false);
            }
            break;

          case "exists":
            // Check if any element satisfies predicate
            if (bodyResult.size() != 1) {
              return error("exists predicate must return singleton boolean", argExpCtx.body);
            }
            OCLElement existsElem = bodyResult.getElements().get(0);
            if (!(existsElem instanceof OCLElement.BoolValue)) {
              return error("exists predicate must return boolean", argExpCtx.body);
            }
            boolean existsCondition = ((OCLElement.BoolValue) existsElem).value();
            if (existsCondition) {
              // Short-circuit: return true immediately
              return Value.boolValue(true);
            }
            break;

          default:
            return error("Unknown iterator operation: " + opName, navCtx);
        }
      }

      // Build final result based on operation type
      return switch (opName) {
        case "select", "reject" -> {
          // Preserve collection type from receiver
          yield Value.of(results, receiverType);
        }

        case "collect" -> {
          // Get result type from Pass 2 (already computed with correct element type)
          Type resultType = nodeTypes.get(navCtx);
          if (resultType == null) {
            // Fallback: preserve collection kind, use ANY for element type
            if (receiverType.isUnique() && receiverType.isOrdered()) {
              resultType = Type.orderedSet(Type.ANY);
            } else if (receiverType.isUnique()) {
              resultType = Type.set(Type.ANY);
            } else if (receiverType.isOrdered()) {
              resultType = Type.sequence(Type.ANY);
            } else {
              resultType = Type.bag(Type.ANY);
            }
          }
          yield Value.of(results, resultType);
        }

        case "forAll" -> {
          // If we reached here, all elements satisfied predicate
          yield Value.boolValue(true);
        }

        case "exists" -> {
          // If we reached here, no element satisfied predicate
          yield Value.boolValue(false);
        }

        default -> error("Unknown iterator operation: " + opName, navCtx);
      };

    } finally {
      // CRITICAL: Exit iterator scope
      symbolTable.exitScope();
    }
  }

  /**
   * Evaluates String operations: concat, substring, toUpper, toLower, etc.
   *
   * @param ctx Navigation context containing the operation
   * @param opName Operation name (concat, substring, etc.)
   * @param receiver Receiver value (should be a String singleton)
   * @return Result value of the operation
   */
  private Value evaluateStringOperation(
      VitruvOCLParser.NavigatingExpCSContext ctx, String opName, Value receiver) {
    // Extract string from singleton
    if (receiver.size() != 1) {
      return error(
          "String operations require singleton receiver, got collection of size " + receiver.size(),
          ctx);
    }

    OCLElement elem = receiver.getElements().get(0);

    if (!(elem instanceof OCLElement.StringValue)) {
      return error("String operation requires String receiver", ctx);
    }

    String str = ((OCLElement.StringValue) elem).value();

    // Collect ALL arguments
    List<VitruvOCLParser.NavigatingArgCSContext> regularArgs = ctx.navigatingArgCS();
    List<VitruvOCLParser.NavigatingCommaArgCSContext> commaArgs = ctx.navigatingCommaArgCS();

    List<Value> argValues = new ArrayList<>();

    // First argument (if exists)
    if (!regularArgs.isEmpty()) {
      VitruvOCLParser.NavigatingArgCSContext firstArg = regularArgs.get(0);
      Value argVal = visit(firstArg.navigatingArgExpCS());
      argValues.add(argVal);
    }

    // Additional comma arguments
    for (int i = 0; i < commaArgs.size(); i++) {
      VitruvOCLParser.NavigatingCommaArgCSContext commaArg = commaArgs.get(i);
      Value argVal = visit(commaArg.navigatingArgExpCS());
      argValues.add(argVal);
    }

    return switch (opName) {
      case "concat" -> {
        if (argValues.size() != 1) {
          yield error("Operation 'concat' requires 1 argument, got " + argValues.size(), ctx);
        }

        Value argValue = argValues.get(0);
        if (argValue.size() != 1) {
          yield error("concat() requires singleton String argument", ctx);
        }

        OCLElement argElem = argValue.getElements().get(0);
        if (!(argElem instanceof OCLElement.StringValue)) {
          yield error("concat() requires String argument", ctx);
        }

        String arg = ((OCLElement.StringValue) argElem).value();
        String result = str + arg;
        yield Value.stringValue(result);
      }

      case "substring" -> {
        if (argValues.size() != 2) {
          yield error(
              "Operation 'substring' requires 2 arguments (start, end), got " + argValues.size(),
              ctx);
        }

        Value startValue = argValues.get(0);
        Value endValue = argValues.get(1);

        if (startValue.size() != 1 || endValue.size() != 1) {
          yield error("substring() requires singleton Integer arguments", ctx);
        }

        OCLElement startElem = startValue.getElements().get(0);
        OCLElement endElem = endValue.getElements().get(0);

        if (!(startElem instanceof OCLElement.IntValue)
            || !(endElem instanceof OCLElement.IntValue)) {
          yield error("substring() requires Integer arguments", ctx);
        }

        int start = ((OCLElement.IntValue) startElem).value();
        int end = ((OCLElement.IntValue) endElem).value();

        // OCL: 1-indexed → Java: 0-indexed
        // OCL: substring(1, 3) → Java: substring(0, 3)
        try {
          if (start < 1 || end < start || end > str.length()) {
            yield Value.empty(Type.STRING);
          }

          int javaStart = start - 1;
          int javaEnd = end;

          String result = str.substring(javaStart, javaEnd);
          yield Value.stringValue(result);
        } catch (IndexOutOfBoundsException e) {
          yield Value.empty(Type.STRING);
        }
      }

      case "toUpper" -> {
        if (argValues.size() != 0) {
          yield error("Operation 'toUpper' takes no arguments, got " + argValues.size(), ctx);
        }

        String result = str.toUpperCase();
        yield Value.stringValue(result);
      }

      case "toLower" -> {
        if (argValues.size() != 0) {
          yield error("Operation 'toLower' takes no arguments, got " + argValues.size(), ctx);
        }

        String result = str.toLowerCase();
        yield Value.stringValue(result);
      }

      case "indexOf" -> {
        if (argValues.size() != 1) {
          yield error("Operation 'indexOf' requires 1 argument, got " + argValues.size(), ctx);
        }

        Value searchValue = argValues.get(0);
        if (searchValue.size() != 1) {
          yield error("indexOf() requires singleton String argument", ctx);
        }

        OCLElement searchElem = searchValue.getElements().get(0);
        if (!(searchElem instanceof OCLElement.StringValue)) {
          yield error("indexOf() requires String argument", ctx);
        }

        String searchStr = ((OCLElement.StringValue) searchElem).value();
        int javaIndex = str.indexOf(searchStr);

        // Java: 0-based, -1 if not found → OCL: 1-based, 0 if not found
        int oclIndex = (javaIndex == -1) ? 0 : javaIndex + 1;
        yield Value.intValue(oclIndex);
      }

      case "equalsIgnoreCase" -> {
        if (argValues.size() != 1) {
          yield error(
              "Operation 'equalsIgnoreCase' requires 1 argument, got " + argValues.size(), ctx);
        }

        Value compareValue = argValues.get(0);
        if (compareValue.size() != 1) {
          yield error("equalsIgnoreCase() requires singleton String argument", ctx);
        }

        OCLElement compareElem = compareValue.getElements().get(0);
        if (!(compareElem instanceof OCLElement.StringValue)) {
          yield error("equalsIgnoreCase() requires String argument", ctx);
        }

        String compareStr = ((OCLElement.StringValue) compareElem).value();
        boolean result = str.equalsIgnoreCase(compareStr);
        yield Value.boolValue(result);
      }

      default -> {
        yield error("Unknown string operation: " + opName, ctx);
      }
    };
  }

  // ==================== Variables ====================
  /** Evaluates variable references. */
  @Override
  public Value visitName(NameContext ctx) {
    // Check if it's a variable reference
    if (ctx.variableName != null) {
      String varName = ctx.variableName.getText();

      // Lookup in symbol table
      Symbol symbol = symbolTable.resolve(varName);

      if (symbol == null) {
        handleUndefinedSymbol(varName, ctx);
        return Value.empty(Type.ERROR);
      }

      if (!(symbol instanceof VariableSymbol)) {
        return error("'" + varName + "' is not a variable", ctx);
      }

      VariableSymbol varSymbol = (VariableSymbol) symbol;

      // Get stored runtime value
      Value value = varSymbol.getValue();

      if (value == null) {
        return error("Variable '" + varName + "' has no value (internal error)", ctx);
      }

      return value;
    }

    // Not a variable - delegate to other handlers
    Value result = visitChildren(ctx);
    return result != null ? result : Value.empty(Type.ANY); // <-- NULL-SAFE
  }

  /**
   * Evaluates navigating argument expressions. Handles three cases: 1. Iterator syntax: x | x > 2
   * (for select/reject/collect/forAll/exists) 2. Complex iterator syntax: expr | name . body
   * (legacy, backward compat) 3. Normal argument: expression
   */
  @Override
  public Value visitNavigatingArgExpCS(VitruvOCLParser.NavigatingArgExpCSContext ctx) {
    // Case 1: Simple Iterator syntax (x | body)
    if (ctx.iteratorVar != null && ctx.body != null) {
      // This is evaluated in evaluateIteratorOperation
      // For direct visits, just evaluate the body (though this shouldn't happen)
      return visit(ctx.body);
    }

    // Case 2: Complex iterator syntax (legacy)
    if (ctx.iteratorVariable != null && ctx.iteratorBarExpCS() != null) {
      // Evaluate iterator variable expression
      Value iterVarValue = visit(ctx.iteratorVariable);

      // Evaluate body expressions
      Value bodyValue = null;
      List<VitruvOCLParser.InfixedExpCSContext> bodyExpressions = ctx.infixedExpCS();

      // Skip first if it's the iteratorVariable
      int startIndex =
          (bodyExpressions.size() > 0 && bodyExpressions.get(0) == ctx.iteratorVariable) ? 1 : 0;

      for (int i = startIndex; i < bodyExpressions.size(); i++) {
        bodyValue = visit(bodyExpressions.get(i));
      }

      return bodyValue != null ? bodyValue : Value.empty(Type.ERROR);
    }

    // Case 3: Normal argument expression
    Value resultValue = null;
    for (VitruvOCLParser.InfixedExpCSContext exp : ctx.infixedExpCS()) {
      resultValue = visit(exp);
    }

    return resultValue != null ? resultValue : Value.empty(Type.ERROR);
  }

  /**
   * Evaluates let expressions. Creates new scope and binds variables with runtime values.
   *
   * <p>Example: let x = 5, y = x * 2 in y + 3
   */
  @Override
  public Value visitLetExpCS(VitruvOCLParser.LetExpCSContext ctx) {
    LocalScope letScope = new LocalScope(symbolTable.getCurrentScope());
    symbolTable.enterScope(letScope);

    try {
      VitruvOCLParser.VariableDeclarationsContext varDecls = ctx.variableDeclarations();

      for (VitruvOCLParser.VariableDeclarationContext varDecl : varDecls.variableDeclaration()) {
        String varName = varDecl.varName.getText();

        // Evaluate initializer expression
        Value initValue = visit(varDecl.varInit);

        if (initValue == null) {
          return error("Failed to evaluate initializer for variable '" + varName + "'", varDecl);
        }

        // Get type from Pass 2
        Type varType = nodeTypes.get(varDecl);
        if (varType == null) {
          varType = nodeTypes.get(varDecl.varInit);
          if (varType == null) {
            varType = Type.ANY;
          }
        }

        // Create variable symbol with runtime value
        VariableSymbol varSymbol = new VariableSymbol(varName, varType, letScope, false);
        varSymbol.setValue(initValue);
        symbolTable.define(varSymbol);
      }

      // Evaluate body expression
      Value result = visit(ctx.body);

      if (result == null) {
        return error("Let expression body produced no value", ctx);
      }

      return result;

    } finally {
      symbolTable.exitScope();
    }
  }

  /** Evaluates 'self' references. */
  @Override
  public Value visitSelfExpCS(VitruvOCLParser.SelfExpCSContext ctx) {
    // Lookup 'self' in symbol table
    Symbol selfSymbol = symbolTable.resolve("self");

    if (selfSymbol == null) {
      return error("'self' is not defined in current context", ctx);
    }

    if (!(selfSymbol instanceof VariableSymbol)) {
      return error("'self' is not a variable (internal error)", ctx);
    }

    VariableSymbol selfVar = (VariableSymbol) selfSymbol;
    Value selfValue = selfVar.getValue();

    if (selfValue == null) {
      return error("'self' has no value (internal error)", ctx);
    }

    return selfValue;
  }

  // ==================== Navigation ====================

  @Override
  public Value visitNavigatingExpCS(VitruvOCLParser.NavigatingExpCSContext ctx) {
    // Start with source
    Value source = visit(ctx.indexExpCS());

    // Process each navigation step
    for (VitruvOCLParser.NavigatingArgCSContext arg : ctx.navigatingArgCS()) {
      source = handleNavigationStep(arg, source);
    }

    return source;
  }

  private Value handleNavigationStep(VitruvOCLParser.NavigatingArgCSContext ctx, Value source) {
    String operationName = extractOperationName(ctx.navigatingArgExpCS());

    // Evaluate arguments
    List<Value> args = new ArrayList<>();
    for (VitruvOCLParser.ExpCSContext expCtx : ctx.expCS()) {
      args.add(visit(expCtx));
    }

    // ===== Query Operations =====

    if (operationName.equals("size")) {
      return Value.intValue(source.size());
    }
    if (operationName.equals("isEmpty")) {
      return Value.boolValue(source.isEmpty());
    }
    if (operationName.equals("notEmpty")) {
      return Value.boolValue(source.notEmpty());
    }
    if (operationName.equals("includes")) {
      if (args.size() != 1 || args.get(0).size() != 1) {
        return error("includes() requires exactly 1 singleton argument", ctx);
      }
      OCLElement elem = args.get(0).getElements().get(0);
      return Value.boolValue(source.includes(elem));
    }

    if (operationName.equals("excludes")) {
      if (args.size() != 1 || args.get(0).size() != 1) {
        return error("excludes() requires exactly 1 singleton argument", ctx);
      }
      OCLElement elem = args.get(0).getElements().get(0);
      return Value.boolValue(source.excludes(elem));
    }

    // ===== Modification Operations =====

    if (operationName.equals("including")) {
      if (args.size() != 1 || args.get(0).size() != 1) {
        return error("including() requires exactly 1 singleton argument", ctx);
      }
      OCLElement elem = args.get(0).getElements().get(0);
      return source.including(elem);
    }

    if (operationName.equals("excluding")) {
      if (args.size() != 1 || args.get(0).size() != 1) {
        return error("excluding() requires exactly 1 singleton argument", ctx);
      }
      OCLElement elem = args.get(0).getElements().get(0);
      return source.excluding(elem);
    }

    // ===== Set Operations =====

    if (operationName.equals("union")) {
      if (args.size() != 1) {
        return error("union() requires exactly 1 argument", ctx);
      }
      return source.union(args.get(0));
    }

    if (operationName.equals("intersection")) {
      if (args.size() != 1) {
        return error("intersection() requires exactly 1 argument", ctx);
      }
      return source.intersection(args.get(0));
    }

    if (operationName.equals("minus")) {
      if (args.size() != 1) {
        return error("minus() requires exactly 1 argument", ctx);
      }
      return source.minus(args.get(0));
    }

    if (operationName.equals("symmetricDifference")) {
      if (args.size() != 1) {
        return error("symmetricDifference() requires exactly 1 argument", ctx);
      }
      return source.symmetricDifference(args.get(0));
    }

    if (operationName.equals("includesAll")) {
      if (args.size() != 1) {
        return error("includesAll() requires exactly 1 argument", ctx);
      }
      return Value.boolValue(source.includesAll(args.get(0)));
    }

    if (operationName.equals("excludesAll")) {
      if (args.size() != 1) {
        return error("excludesAll() requires exactly 1 argument", ctx);
      }
      return Value.boolValue(source.excludesAll(args.get(0)));
    }

    // ===== Ordered Operations =====

    if (operationName.equals("first")) {
      return source.first();
    }

    if (operationName.equals("last")) {
      return source.last();
    }

    if (operationName.equals("at")) {
      if (args.size() != 1 || args.get(0).size() != 1) {
        return error("at() requires exactly 1 singleton integer argument", ctx);
      }
      OCLElement elem = args.get(0).getElements().get(0);
      if (!(elem instanceof OCLElement.IntValue)) {
        return error("at() requires an integer argument", ctx);
      }
      int index = ((OCLElement.IntValue) elem).value();
      return source.at(index);
    }

    if (operationName.equals("indexOf")) {
      if (args.size() != 1 || args.get(0).size() != 1) {
        return error("indexOf() requires exactly 1 singleton argument", ctx);
      }
      OCLElement elem = args.get(0).getElements().get(0);
      int index = source.indexOf(elem);
      return Value.intValue(index);
    }

    if (operationName.equals("reverse")) {
      return source.reverse();
    }

    if (operationName.equals("count")) {
      if (args.size() != 1 || args.get(0).size() != 1) {
        return error("count() requires exactly 1 singleton argument", ctx);
      }
      OCLElement elem = args.get(0).getElements().get(0);
      int count = source.count(elem);
      return Value.intValue(count);
    }

    return error("Unknown operation: " + operationName, ctx);
  }

  // ==================== Comparison & Boolean Operators ====================

  @Override
  public Value visitEqualOperations(VitruvOCLParser.EqualOperationsContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);
    String operator = ctx.op.getText();

    // Expect singletons for comparison
    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Comparison requires singleton operands", ctx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    boolean result =
        switch (operator) {
          case "==" -> OCLElement.semanticEquals(leftElem, rightElem);
          case "!=" -> !OCLElement.semanticEquals(leftElem, rightElem);
          case "<" -> OCLElement.compare(leftElem, rightElem) < 0;
          case "<=" -> OCLElement.compare(leftElem, rightElem) <= 0;
          case ">" -> OCLElement.compare(leftElem, rightElem) > 0;
          case ">=" -> OCLElement.compare(leftElem, rightElem) >= 0;
          default -> {
            errors.add(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                "Unknown comparison operator: " + operator,
                tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR,
                "evaluator");
            yield false;
          }
        };

    return Value.boolValue(result);
  }

  @Override
  public Value visitAndOrXor(VitruvOCLParser.AndOrXorContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);
    String operator = ctx.op.getText();

    // Expect singleton booleans
    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("Boolean operators require singleton operands", ctx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    if (!(leftElem instanceof OCLElement.BoolValue)
        || !(rightElem instanceof OCLElement.BoolValue)) {
      return error("Boolean operators require boolean operands", ctx);
    }

    boolean left = ((OCLElement.BoolValue) leftElem).value();
    boolean right = ((OCLElement.BoolValue) rightElem).value();

    boolean result =
        switch (operator) {
          case "and" -> left && right;
          case "or" -> left || right;
          case "xor" -> left ^ right;
          default -> {
            errors.add(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                "Unknown boolean operator: " + operator,
                tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR,
                "evaluator");
            yield false;
          }
        };

    return Value.boolValue(result);
  }

  @Override
  public Value visitImplies(VitruvOCLParser.ImpliesContext ctx) {
    Value leftValue = visit(ctx.left);
    Value rightValue = visit(ctx.right);

    // Expect singleton booleans
    if (leftValue.size() != 1 || rightValue.size() != 1) {
      return error("'implies' requires singleton operands", ctx);
    }

    OCLElement leftElem = leftValue.getElements().get(0);
    OCLElement rightElem = rightValue.getElements().get(0);

    if (!(leftElem instanceof OCLElement.BoolValue)
        || !(rightElem instanceof OCLElement.BoolValue)) {
      return error("'implies' requires boolean operands", ctx);
    }

    boolean left = ((OCLElement.BoolValue) leftElem).value();
    boolean right = ((OCLElement.BoolValue) rightElem).value();

    // A implies B === !A or B
    boolean result = !left || right;

    return Value.boolValue(result);
  }

  // ==================== Literals ====================

  @Override
  public Value visitBoolean(VitruvOCLParser.BooleanContext ctx) {
    String text = ctx.BooleanLiteralExpCS().getText();
    boolean value = text.equals("true");
    return Value.boolValue(value);
  }

  @Override
  public Value visitString(VitruvOCLParser.StringContext ctx) {
    String text = ctx.STRING().getText();
    // Remove surrounding quotes
    String value = text.substring(1, text.length() - 1);
    return Value.stringValue(value);
  }

  @Override
  public Value visitPrefixedExp(VitruvOCLParser.PrefixedExpContext ctx) {
    return visit(ctx.prefixedExpCS());
  }

  // ==================== Helper Methods ====================

  private String extractOperationName(VitruvOCLParser.NavigatingArgExpCSContext ctx) {
    if (ctx.nameExpCS() != null) {
      return ctx.nameExpCS().getText();
    }
    return ctx.getText();
  }

  private Value error(String message, org.antlr.v4.runtime.ParserRuleContext ctx) {
    errors.add(
        ctx.getStart().getLine(),
        ctx.getStart().getCharPositionInLine(),
        message,
        tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR,
        "evaluator");
    return Value.empty(Type.ERROR);
  }

  // ==================== If-Then-Else ====================
  private org.antlr.v4.runtime.TokenStream tokens;

  public void setTokenStream(org.antlr.v4.runtime.TokenStream tokens) {
    this.tokens = tokens;
  }

  @Override
  public Value visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    // Grammar labels are single ExpCSContext, not lists!
    // Wrap them in lists for consistency
    List<VitruvOCLParser.ExpCSContext> condExps =
        ctx.ifexp != null ? List.of(ctx.ifexp) : List.of();
    List<VitruvOCLParser.ExpCSContext> thenExps =
        ctx.thenexp != null ? List.of(ctx.thenexp) : List.of();
    List<VitruvOCLParser.ExpCSContext> elseExps =
        ctx.elseexp != null ? List.of(ctx.elseexp) : List.of();

    if (condExps.isEmpty() || thenExps.isEmpty() || elseExps.isEmpty()) {
      return error("If expression missing condition, then, or else branch", ctx);
    }

    // Evaluate all condition expressions, use last value
    Value condVal = null;
    for (VitruvOCLParser.ExpCSContext exp : condExps) {
      condVal = visit(exp);
    }

    if (condVal == null) {
      return error("If condition evaluation failed", ctx);
    }

    if (condVal.size() != 1) {
      return error("If condition must be singleton, got collection of size " + condVal.size(), ctx);
    }

    OCLElement condElem = condVal.getElements().get(0);
    if (!(condElem instanceof OCLElement.BoolValue)) {
      return error("If condition must be Boolean", ctx);
    }

    boolean condition = ((OCLElement.BoolValue) condElem).value();

    // Evaluate appropriate branch
    Value result = null;
    if (condition) {
      for (VitruvOCLParser.ExpCSContext exp : thenExps) {
        result = visit(exp);
      }
    } else {
      for (VitruvOCLParser.ExpCSContext exp : elseExps) {
        result = visit(exp);
      }
    }

    return result != null ? result : Value.empty(Type.ERROR);
  }

  /** Helper: Find token index of keyword in context, respecting nesting levels. */
  private int findKeywordToken(VitruvOCLParser.IfExpCSContext ctx, String keyword) {
    if (tokens == null) {
      return Integer.MAX_VALUE;
    }

    int startIdx = ctx.getStart().getTokenIndex();
    int stopIdx = ctx.getStop().getTokenIndex();

    // Track nesting level
    int nestingLevel = 0;

    for (int i = startIdx; i <= stopIdx; i++) {
      org.antlr.v4.runtime.Token token = tokens.get(i);
      String text = token.getText();

      // Skip the first 'if'
      if (i == startIdx && text.equals("if")) {
        continue;
      }

      // Track nesting
      if (text.equals("if")) {
        nestingLevel++;
      } else if (text.equals("endif")) {
        nestingLevel--;
      }

      // Only match keywords at nesting level 0
      if (nestingLevel == 0 && text.equals(keyword)) {
        return i;
      }
    }

    return Integer.MAX_VALUE;
  }

  /** Checks if an element is of a given type. Supports primitive types and metamodel types. */
  private boolean checkIsKindOf(OCLElement elem, Type targetType) {
    if (targetType == Type.INTEGER) {
      return elem instanceof OCLElement.IntValue;
    } else if (targetType == Type.STRING) {
      return elem instanceof OCLElement.StringValue;
    } else if (targetType == Type.BOOLEAN) {
      return elem instanceof OCLElement.BoolValue;
    } else if (targetType == Type.DOUBLE) {
      return elem instanceof OCLElement.DoubleValue;
    }

    // Metamodel types
    if (targetType.isMetaclassType()) {
      EClass targetEClass = targetType.getEClass();
      EClass elemEClass = elem.getEClass();

      if (elemEClass == null) {
        return false;
      }

      // Check if element's EClass is same or subtype of target
      return targetEClass.isSuperTypeOf(elemEClass) || elemEClass.equals(targetEClass);
    }

    return false;
  }
}