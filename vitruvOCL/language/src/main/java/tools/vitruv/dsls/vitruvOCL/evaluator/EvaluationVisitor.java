package tools.vitruv.dsls.vitruvOCL.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.NameContext;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
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
      MetamodelWrapperInterface specification,
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
      String qualifiedName = ctx.metamodel.getText() + "::" + ctx.className.getText();

      // ✅ FIX: Lookup the metaclass type directly, don't use nodeTypes!
      Type metaclassType = symbolTable.lookupType(qualifiedName);

      if (metaclassType == null || metaclassType == Type.ERROR) {
        throw new RuntimeException("Invalid metamodel type: " + qualifiedName);
      }

      System.out.println("CREATED currentValue:");
      System.out.println("  metaclassType: " + metaclassType);

      Value currentValue = Value.of(List.of(), metaclassType);

      System.out.println("  currentValue.getRuntimeType(): " + currentValue.getRuntimeType());

      for (VitruvOCLParser.PrimaryExpCSContext primary : ctx.primaryExpCS()) {
        currentValue = visitPrimaryExpCSWithReceiver(primary, currentValue);
        System.out.println("AFTER visitPrimaryExpCS:");
        System.out.println("  currentValue.getRuntimeType(): " + currentValue.getRuntimeType());
      }

      return currentValue;
    }

    // ========================================
    // Handle unary operators: -5, not true
    // ========================================
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

    // ========================================
    // Start with first primary
    // ========================================
    Value currentValue = visit(primaries.get(0));

    // ========================================
    // Apply unary operators
    // ========================================
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

    // ========================================
    // Handle navigation chain: .operation1().operation2()
    // ✅ HIER: Saubere Delegation statt Switch-Case!
    // ========================================
    for (int i = 1; i < primaries.size(); i++) {
      currentValue = visitPrimaryExpCSWithReceiver(primaries.get(i), currentValue);
    }

    return currentValue;
  }

  @Override
  public Value visitClassifierContextCS(VitruvOCLParser.ClassifierContextCSContext ctx) {
    Type contextType = nodeTypes.get(ctx);

    if (contextType == null || contextType == Type.ERROR) {
      return error("Invalid context type", ctx);
    }

    // Get all instances of this context type
    EClass eClass = contextType.getEClass();
    if (eClass == null) {
      return error("Context type must be a metaclass type", ctx);
    }

    List<EObject> instances = specification.getAllInstances(eClass);

    // Evaluate all invariants on each instance
    List<OCLElement> allResults = new ArrayList<>();

    for (EObject instance : instances) {
      // Create new scope with 'self' bound to current instance
      Scope instanceScope = new LocalScope(symbolTable.getCurrentScope());
      symbolTable.enterScope(instanceScope);

      // Bind 'self' to current instance
      Value selfValue =
          Value.of(List.of(new OCLElement.MetaclassValue(instance)), Type.singleton(contextType));
      // Bind 'self' via existing VariableSymbol
      Symbol selfSymbol = symbolTable.resolve("self");
      if (selfSymbol instanceof VariableSymbol varSym) {
        varSym.setValue(selfValue);
      } else {
        // Create new if not exists
        VariableSymbol newSelf =
            new VariableSymbol("self", contextType, symbolTable.getCurrentScope(), false);
        newSelf.setValue(selfValue);
        symbolTable.define(newSelf);
      }

      // Evaluate all invariants for this instance
      for (VitruvOCLParser.InvCSContext inv : ctx.invCS()) {
        Value invResult = visit(inv);

        // Store result (true/false for each constraint on each instance)
        if (invResult != null && !invResult.isEmpty()) {
          allResults.add(invResult.getElements().get(0));
        }
      }

      // Exit instance scope
      symbolTable.exitScope();
    }

    // Return collection of all results
    return Value.of(allResults, Type.bag(Type.BOOLEAN));
  }

  @Override
  public Value visitInvCS(VitruvOCLParser.InvCSContext ctx) {
    // Evaluate invariant expression
    List<VitruvOCLParser.SpecificationCSContext> specs = ctx.specificationCS();
    Value result = Value.boolValue(true);

    for (VitruvOCLParser.SpecificationCSContext spec : specs) {
      result = visit(spec);
    }

    return result;
  }

  /**
   * Visit a primaryExpCS with receiver value context. Used for navigation chains where the receiver
   * value must be passed down.
   *
   * @param ctx Primary expression context
   * @param receiver Value of the receiver (left side of navigation)
   * @return Result value of the operation
   */
  private Value visitPrimaryExpCSWithReceiver(
      VitruvOCLParser.PrimaryExpCSContext ctx, Value receiver) {
    // navigatingExpCS needs receiver context
    if (ctx.navigatingExpCS() != null) {
      return visitNavigatingExpCSWithReceiver(ctx.navigatingExpCS(), receiver);
    }

    // All other primaryExpCS don't need receiver context
    return visit(ctx);
  }

  /**
   * Visit a nameExpCS as an operation call with receiver context. Dispatches based on the type of
   * name (variable, collection op, string op).
   *
   * @param nameCtx Name expression context
   * @param navCtx Navigation context (contains arguments)
   * @param receiver Value of the receiver
   * @return Result value of the operation
   */
  private Value visitNameExpCSAsOperation(
      VitruvOCLParser.NameExpCSContext nameCtx,
      VitruvOCLParser.NavigatingExpCSContext navCtx,
      Value receiver) {
    // nameExpCS can be: name | ontologicalName | linguisticalName

    if (nameCtx instanceof VitruvOCLParser.NameContext nameContext) {
      return visitNameContextAsOperation(nameContext, navCtx, receiver);
    }

    return error("Invalid operation name", navCtx);
  }

  /**
   * Visit a name context as an operation call. Dispatches to collection operations, string
   * operations, or property/no-arg operations.
   *
   * @param nameCtx Name context
   * @param navCtx Navigation context
   * @param receiver Value of the receiver
   * @return Result value of the operation
   */
  private Value visitNameContextAsOperation(
      VitruvOCLParser.NameContext nameCtx,
      VitruvOCLParser.NavigatingExpCSContext navCtx,
      Value receiver) {
    // Collection Operations
    if (nameCtx.collectionOperationName() != null) {
      String opName = nameCtx.collectionOperationName().getText();
      return evaluateCollectionOperation(navCtx, opName, receiver);
    }

    // String Operations
    if (nameCtx.stringOperationName() != null) {
      String opName = nameCtx.stringOperationName().getText();
      return evaluateStringOperation(navCtx, opName, receiver);
    }

    // Variable/Property/No-Arg Operation
    if (nameCtx.variableName != null) {
      String name = nameCtx.variableName.getText();

      // Check if it's a no-arg operation
      if (navCtx.navigatingArgCS().isEmpty() && navCtx.navigatingCommaArgCS().isEmpty()) {
        Value result = tryEvaluateNoArgOperation(name, receiver, navCtx);
        if (result != null) {
          return result;
        }
      }

      // ✅ FIX: Fallback zu normaler Navigation
      // Wir haben bereits den receiver, aber die Operation ist nicht bekannt
      // → Behandle als unbekannte Operation
      return error("Unknown operation or property access: " + name, navCtx);
    }

    return error("Invalid operation or property name", navCtx);
  }

  /**
   * Attempts to evaluate a no-argument operation (size, isEmpty, first, last, etc.). Returns null
   * if the name is not a recognized no-arg operation.
   *
   * @param opName Operation name
   * @param receiver Value of the receiver
   * @param ctx Context for error reporting
   * @return Result value if successful, null otherwise
   */
  private Value tryEvaluateNoArgOperation(
      String opName, Value receiver, org.antlr.v4.runtime.ParserRuleContext ctx) {
    return switch (opName) {
      case "size" -> Value.intValue(receiver.size());
      case "isEmpty" -> Value.boolValue(receiver.isEmpty());
      case "notEmpty" -> Value.boolValue(receiver.notEmpty());
      case "first" -> receiver.first();
      case "last" -> receiver.last();
      case "reverse" -> receiver.reverse();
      case "flatten" -> receiver.flatten();
      default -> null; // Not a no-arg operation
    };
  }

  /**
   * Visit a navigatingExpCS with receiver value context. Extracts the operation name and delegates
   * to name-based dispatch.
   *
   * @param ctx Navigating expression context
   * @param receiver Value of the receiver
   * @return Result value of the operation
   */
  private Value visitNavigatingExpCSWithReceiver(
      VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {

    String name = ctx.indexExpCS().nameExpCS().getText();

    // Handle property/reference access on metaclass instances
    if (!receiver.isEmpty() && receiver.getElements().get(0) instanceof OCLElement.MetaclassValue) {
      if (!isCollectionOperation(name)) {
        List<OCLElement> results = new ArrayList<>();
        for (OCLElement elem : receiver.getElements()) {
          if (elem instanceof OCLElement.MetaclassValue metaclass) {
            EObject instance = metaclass.instance();
            EStructuralFeature feature = instance.eClass().getEStructuralFeature(name);

            if (feature != null) {
              Object value = instance.eGet(feature);

              // Multi-valued reference/attribute (OCL#: everything is collection)
              if (value instanceof List<?> list) {
                for (Object item : list) {
                  if (item instanceof EObject) {
                    results.add(new OCLElement.MetaclassValue((EObject) item));
                  } else {
                    results.add(wrapValue(item));
                  }
                }
              } else {
                // Single-valued becomes singleton collection
                results.add(wrapValue(value));
              }
            }
          }
        }
        Type resultType = nodeTypes.get(ctx);
        return Value.of(results, resultType);
      }
    }

    // Handle operations
    // Handle operations
    if (name.equals("allInstances")) {
      // The receiver should be empty with a metaclass type
      // We need to get all instances of that metaclass
      System.out.println("EVALUATING allInstances:");
      System.out.println("  receiver: " + receiver);
      System.out.println("  receiver.isEmpty(): " + receiver.isEmpty());
      System.out.println("  receiver.getRuntimeType(): " + receiver.getRuntimeType());
      System.out.println(
          "  receiver.getRuntimeType().isMetaclassType(): "
              + receiver.getRuntimeType().isMetaclassType());

      // Get the receiver's type (should be a metaclass type)
      Type receiverType = receiver.getRuntimeType();

      if (receiverType == null || !receiverType.isMetaclassType()) {
        return error("allInstances() requires metaclass receiver", ctx);
      }

      // Extract the EClass from the receiver type
      EClass eClass = receiverType.getEClass();

      if (eClass == null) {
        return error("Cannot extract EClass from receiver type", ctx);
      }

      // Get all instances from the specification/wrapper
      List<EObject> instances = specification.getAllInstances(eClass);

      // Wrap as MetaclassValue elements
      List<OCLElement> elements = new ArrayList<>();
      for (EObject instance : instances) {
        elements.add(new OCLElement.MetaclassValue(instance));
      }

      // Return with result type from type checker
      Type resultType = nodeTypes.get(ctx);
      return Value.of(elements, resultType);
    }

    return visitNameExpCSAsOperation(ctx.indexExpCS().nameExpCS(), ctx, receiver);
  }

  private OCLElement wrapValue(Object value) {
    if (value instanceof String s) return new OCLElement.StringValue(s);
    if (value instanceof Integer i) return new OCLElement.IntValue(i);
    if (value instanceof Boolean b) return new OCLElement.BoolValue(b);
    if (value instanceof EObject e) return new OCLElement.MetaclassValue(e);
    throw new RuntimeException("Cannot wrap: " + value);
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
    return switch (opName) {
      case "including" -> evaluateIncluding(ctx, receiver);
      case "excluding" -> evaluateExcluding(ctx, receiver);
      case "includes" -> evaluateIncludes(ctx, receiver);
      case "excludes" -> evaluateExcludes(ctx, receiver);
      case "flatten" -> evaluateFlatten(receiver);
      case "union" -> evaluateUnion(ctx, receiver);
      case "append" -> evaluateAppend(ctx, receiver);
      case "sum" -> evaluateSum(ctx, receiver);
      case "max" -> evaluateMax(ctx, receiver);
      case "min" -> evaluateMin(ctx, receiver);
      case "avg" -> evaluateAvg(ctx, receiver);
      case "abs" -> evaluateAbs(ctx, receiver);
      case "floor", "ceil", "round" -> evaluateFloorCeilRound(ctx, opName, receiver);
      case "lift" -> evaluateLift(ctx, receiver);
      case "oclIsKindOf" -> evaluateOclIsKindOf(ctx, receiver);
      case "select" -> evaluateSelect(ctx, receiver);
      case "reject" -> evaluateReject(ctx, receiver);
      case "collect" -> evaluateCollect(ctx, receiver);
      case "forAll" -> evaluateForAll(ctx, receiver);
      case "exists" -> evaluateExists(ctx, receiver);
      default -> error("Unknown collection operation: " + opName, ctx);
    };
  }

  private Value evaluateIncluding(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateExcluding(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateIncludes(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateExcludes(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateFlatten(Value receiver) {
    // flatten(): flattens nested collections
    return receiver.flatten();
  }

  private Value evaluateUnion(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
    // union(collection): set union
    List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
    if (args.isEmpty()) {
      return error("Operation 'union' requires 1 argument", ctx);
    }

    Value arg = visit(args.get(0).navigatingArgExpCS());
    return receiver.union(arg);
  }

  private Value evaluateAppend(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
    // append(collection): sequence append (alias for union in our implementation)
    List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
    if (args.isEmpty()) {
      return error("Operation 'append' requires 1 argument", ctx);
    }

    Value arg = visit(args.get(0).navigatingArgExpCS());
    return receiver.union(arg); // In OCL#, append is essentially union for sequences
  }

  private Value evaluateSum(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
    int sum = 0;
    for (OCLElement elem : receiver.getElements()) {
      if (!(elem instanceof OCLElement.IntValue)) {
        return error("sum() requires integer elements", ctx);
      }
      sum += ((OCLElement.IntValue) elem).value();
    }
    return Value.intValue(sum); // 0 if empty
  }

  private Value evaluateMax(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateMin(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateAvg(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateAbs(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateFloorCeilRound(
      VitruvOCLParser.NavigatingExpCSContext ctx, String opName, Value receiver) {
    // For integers: no-op, validate and return
    for (OCLElement elem : receiver.getElements()) {
      if (!(elem instanceof OCLElement.IntValue)) {
        return error(opName + "() requires integer elements", ctx);
      }
    }
    return receiver;
  }

  private Value evaluateLift(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateOclIsKindOf(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateSelect(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateReject(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateCollect(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateForAll(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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

  private Value evaluateExists(VitruvOCLParser.NavigatingExpCSContext ctx, Value receiver) {
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
    Symbol symbol = symbolTable.resolve("self");
    if (symbol instanceof VariableSymbol varSym) {
      Value selfValue = varSym.getValue();
      if (selfValue != null) {
        return selfValue;
      }
    }
    return error("'self' not defined in current context", ctx);
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

    // ===== Query Operations (die noch nicht woanders behandelt werden) =====

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

  private boolean isCollectionOperation(String name) {
    return Set.of(
            "including",
            "excluding",
            "includes",
            "excludes",
            "flatten",
            "union",
            "append",
            "sum",
            "max",
            "min",
            "avg",
            "abs",
            "floor",
            "ceil",
            "round",
            "oclIsKindOf",
            "lift",
            "select",
            "reject",
            "collect",
            "forAll",
            "exists",
            "size",
            "allInstances")
        .contains(name);
  }
}