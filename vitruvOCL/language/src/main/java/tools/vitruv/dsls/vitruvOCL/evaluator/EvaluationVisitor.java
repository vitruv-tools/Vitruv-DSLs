package tools.vitruv.dsls.vitruvOCL.evaluator;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import java.util.ArrayList;
import java.util.List;

import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;
import tools.vitruv.dsls.vitruvOCL.symboltable.*;
import tools.vitruv.dsls.vitruvOCL.common.VSUMWrapper;

/**
 * static helper methods for type resolution and type operations.
 * shared type resolution logic that can be used by both TypeCheckVisitor
 * and, the evaluator
 * 
 * reasoning: this logic is the same for type checking and evaluation,
 * therefore centralised in helper class
 * 
 * @see Type type system
 * @see TypeCheckVisitor TypeResolver for type checking
 */
public class EvaluationVisitor extends AbstractPhaseVisitor<Value> {
    
    /**
     * Pre-computed Types aus Pass 2 - für Type-dependent Operations.
     */
    private final ParseTreeProperty<Type> nodeTypes;
    
    public EvaluationVisitor(
            SymbolTable symbolTable, 
            VSUMWrapper vsumWrapper,
            ParseTreeProperty<Type> nodeTypes) {
        super(symbolTable, vsumWrapper);
        this.nodeTypes = nodeTypes;
    }
    
    @Override
    protected void handleUndefinedSymbol(String name, org.antlr.v4.runtime.ParserRuleContext ctx) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
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
    return Value.intValue(intValue);  // Singleton [intValue]
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
    
    if (!(leftElem instanceof OCLElement.IntValue) || 
        !(rightElem instanceof OCLElement.IntValue)) {
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
    
    if (!(leftElem instanceof OCLElement.IntValue) || 
        !(rightElem instanceof OCLElement.IntValue)) {
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
        
        if (!(firstElem instanceof OCLElement.IntValue) || 
            !(secondElem instanceof OCLElement.IntValue)) {
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
 * For Collection Operations like including/excluding, we need the receiver value
 * which is only available at this level of the parse tree.
 */
@Override
public Value visitPrefixedExpCS(VitruvOCLParser.PrefixedExpCSContext ctx) {
    // Count how many unary operators we have at the start
    int unaryOpCount = 0;
    for (int i = 0; i < ctx.getChildCount(); i++) {
        String text = ctx.getChild(i).getText();
        if (text.equals("-") || text.equals("not")) {
            unaryOpCount++;
        } else {
            break; // Stop at first non-operator
        }
    }
    
    // Get all primaries
    List<VitruvOCLParser.PrimaryExpCSContext> primaries = ctx.primaryExpCS();
    
    if (primaries.isEmpty()) {
        return error("Empty expression", ctx);
    }
    
    // Start with first primary
    Value currentValue = visit(primaries.get(0));
    
    // Apply unary operators in reverse order (right-to-left)
    // Example: "not not true" → apply second "not", then first "not"
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
    
    // Handle navigation chain (if there are more primaries)
    for (int i = 1; i < primaries.size(); i++) {
        VitruvOCLParser.PrimaryExpCSContext operationCtx = primaries.get(i);
        
        if (operationCtx.navigatingExpCS() != null) {
            VitruvOCLParser.NavigatingExpCSContext navCtx = operationCtx.navigatingExpCS();
            
            boolean handledAsCollectionOp = false;
            
            // Check if operation name is a collectionOperationName OR stringOperationName
            if (navCtx.opName != null && navCtx.opName.nameExpCS() != null) {
                VitruvOCLParser.NameExpCSContext nameExpCtx = navCtx.opName.nameExpCS();
                
                if (nameExpCtx instanceof VitruvOCLParser.NameContext nameContext) {
                    // Collection operations
                    if (nameContext.collectionOperationName() != null) {
                        String opName = nameContext.collectionOperationName().getText();
                        currentValue = evaluateCollectionOperation(navCtx, opName, currentValue);
                        handledAsCollectionOp = true;
                    }
                    // String operations (NEW!)
                    else if (nameContext.stringOperationName() != null) {
                        String opName = nameContext.stringOperationName().getText();
                        currentValue = evaluateStringOperation(navCtx, opName, currentValue);
                        handledAsCollectionOp = true; // Reuse flag
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

/**
 * Evaluates collection operations: including, excluding, flatten, etc.
 * 
 * @param ctx Navigation context containing the operation
 * @param opName Operation name (including, excluding, etc.)
 * @param receiver Receiver value (the collection)
 * @return Result value of the operation
 */
private Value evaluateCollectionOperation(
    VitruvOCLParser.NavigatingExpCSContext ctx,
    String opName,
    Value receiver
) {
    switch (opName) {
        case "including": {
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
        
        case "excluding": {
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
        
        case "includes": {
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
        
        case "excludes": {
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
        
        case "flatten": {
            // flatten(): flattens nested collections
            return receiver.flatten();
        }
        
        case "union": {
            // union(collection): set union
            List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
            if (args.isEmpty()) {
                return error("Operation 'union' requires 1 argument", ctx);
            }
            
            Value arg = visit(args.get(0).navigatingArgExpCS());
            return receiver.union(arg);
        }
        
        case "append": {
            // append(collection): sequence append (alias for union in our implementation)
            List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
            if (args.isEmpty()) {
                return error("Operation 'append' requires 1 argument", ctx);
            }
            
            Value arg = visit(args.get(0).navigatingArgExpCS());
            return receiver.union(arg); // In OCL#, append is essentially union for sequences
        }
        
        default:
            return error("Unknown collection operation: " + opName, ctx);
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
    VitruvOCLParser.NavigatingExpCSContext ctx,
    String opName,
    Value receiver
) {
    // Extract string from singleton
    if (receiver.size() != 1) {
        return error("String operations require singleton receiver, got collection of size " + 
                    receiver.size(), ctx);
    }
    
    OCLElement elem = receiver.getElements().get(0);
    if (!(elem instanceof OCLElement.StringValue)) {
        return error("String operation requires String receiver", ctx);
    }
    
    String str = ((OCLElement.StringValue) elem).value();
    
    // Collect ALL arguments (regular + comma args)
    List<Value> args = new ArrayList<>();
    
    for (VitruvOCLParser.NavigatingArgCSContext arg : ctx.navigatingArgCS()) {
        args.add(visit(arg.navigatingArgExpCS()));
    }
    
    for (VitruvOCLParser.NavigatingCommaArgCSContext commaArg : ctx.navigatingCommaArgCS()) {
        args.add(visit(commaArg.navigatingArgExpCS()));
    }
    
    return switch (opName) {
        case "concat" -> {
            if (args.size() != 1 || args.get(0).size() != 1) {
                yield error("concat() requires exactly 1 singleton String argument", ctx);
            }
            
            OCLElement argElem = args.get(0).getElements().get(0);
            if (!(argElem instanceof OCLElement.StringValue)) {
                yield error("concat() requires String argument", ctx);
            }
            
            String arg = ((OCLElement.StringValue) argElem).value();
            yield Value.stringValue(str + arg);
        }
        
        case "substring" -> {
            // OCL uses 1-based indexing: substring(1, 3) means chars at positions 1,2,3
            if (args.size() != 2) {
                yield error("substring() requires 2 arguments (start, end), got " + args.size(), ctx);
            }
            
            if (args.get(0).size() != 1 || args.get(1).size() != 1) {
                yield error("substring() requires singleton Integer arguments", ctx);
            }
            
            OCLElement startElem = args.get(0).getElements().get(0);
            OCLElement endElem = args.get(1).getElements().get(0);
            
            if (!(startElem instanceof OCLElement.IntValue) || 
                !(endElem instanceof OCLElement.IntValue)) {
                yield error("substring() requires Integer arguments", ctx);
            }
            
            int start = ((OCLElement.IntValue) startElem).value();
            int end = ((OCLElement.IntValue) endElem).value();
            
            // OCL: 1-indexed → Java: 0-indexed
            // OCL: substring(1, 3) → Java: substring(0, 3)
            try {
                if (start < 1 || end < start || end > str.length()) {
                    // OCL# null-safe: invalid indices → empty collection
                    yield Value.empty(Type.STRING);
                }
                String result = str.substring(start - 1, end);
                yield Value.stringValue(result);
            } catch (IndexOutOfBoundsException e) {
                // OCL# null-safe: errors → empty collection
                yield Value.empty(Type.STRING);
            }
        }
        
        case "toUpper" -> Value.stringValue(str.toUpperCase());
        
        case "toLower" -> Value.stringValue(str.toLowerCase());
        
        case "indexOf" -> {
            // Returns 1-based index, or 0 if not found (OCL convention)
            if (args.size() != 1 || args.get(0).size() != 1) {
                yield error("indexOf() requires exactly 1 singleton String argument", ctx);
            }
            
            OCLElement searchElem = args.get(0).getElements().get(0);
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
            if (args.size() != 1 || args.get(0).size() != 1) {
                yield error("equalsIgnoreCase() requires exactly 1 singleton String argument", ctx);
            }
            
            OCLElement compareElem = args.get(0).getElements().get(0);
            if (!(compareElem instanceof OCLElement.StringValue)) {
                yield error("equalsIgnoreCase() requires String argument", ctx);
            }
            
            String compareStr = ((OCLElement.StringValue) compareElem).value();
            yield Value.boolValue(str.equalsIgnoreCase(compareStr));
        }
        
        default -> error("Unknown string operation: " + opName, ctx);
    };
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
    
    boolean result = switch (operator) {
        case "==" -> OCLElement.semanticEquals(leftElem, rightElem);
        case "!=" -> !OCLElement.semanticEquals(leftElem, rightElem);
        case "<" -> OCLElement.compare(leftElem, rightElem) < 0;
        case "<=" -> OCLElement.compare(leftElem, rightElem) <= 0;
        case ">" -> OCLElement.compare(leftElem, rightElem) > 0;
        case ">=" -> OCLElement.compare(leftElem, rightElem) >= 0;
        default -> {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                      "Unknown comparison operator: " + operator,
                      tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR, "evaluator");
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
    
    if (!(leftElem instanceof OCLElement.BoolValue) || 
        !(rightElem instanceof OCLElement.BoolValue)) {
        return error("Boolean operators require boolean operands", ctx);
    }
    
    boolean left = ((OCLElement.BoolValue) leftElem).value();
    boolean right = ((OCLElement.BoolValue) rightElem).value();
    
    boolean result = switch (operator) {
        case "and" -> left && right;
        case "or" -> left || right;
        case "xor" -> left ^ right;
        default -> {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                      "Unknown boolean operator: " + operator,
                      tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR, "evaluator");
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
    
    if (!(leftElem instanceof OCLElement.BoolValue) || 
        !(rightElem instanceof OCLElement.BoolValue)) {
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
    errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
              message, tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity.ERROR, "evaluator");
    return Value.empty(Type.ERROR);
}

// ==================== If-Then-Else ====================
private org.antlr.v4.runtime.TokenStream tokens;

public void setTokenStream(org.antlr.v4.runtime.TokenStream tokens) {
    this.tokens = tokens;
}

@Override
public Value visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    // Partition expressions by keywords: if...then...else...endif
    List<VitruvOCLParser.ExpCSContext> allExps = ctx.expCS();
    
    if (allExps.isEmpty()) {
        return error("If expression requires expressions", ctx);
    }
    
    // Find 'then', 'else' token positions
    int thenTokenIndex = findKeywordToken(ctx, "then");
    int elseTokenIndex = findKeywordToken(ctx, "else");
    
    // Partition expressions
    List<VitruvOCLParser.ExpCSContext> condExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> thenExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> elseExps = new ArrayList<>();
    
    for (VitruvOCLParser.ExpCSContext exp : allExps) {
        int expStartIndex = exp.getStart().getTokenIndex();
        
        if (expStartIndex < thenTokenIndex) {
            condExps.add(exp);
        } else if (expStartIndex < elseTokenIndex) {
            thenExps.add(exp);
        } else {
            elseExps.add(exp);
        }
    }
    
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
    
    // Expect singleton Boolean
    if (condVal.size() != 1) {
        return error("If condition must be singleton, got collection of size " + condVal.size(), ctx);
    }
    
    OCLElement condElem = condVal.getElements().get(0);
    if (!(condElem instanceof OCLElement.BoolValue)) {
        return error("If condition must be Boolean", ctx);
    }
    
    boolean condition = ((OCLElement.BoolValue) condElem).value();
    
    // Evaluate appropriate branch (all expressions, return last value)
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

/**
 * Helper: Find token index of keyword in context, respecting nesting levels.
 */
private int findKeywordToken(VitruvOCLParser.IfExpCSContext ctx, String keyword) {
    if (tokens == null) {
        return Integer.MAX_VALUE;
    }
    
    int startIdx = ctx.getStart().getTokenIndex();
    int stopIdx = ctx.getStop().getTokenIndex();
    
    // Track nesting level: we want keywords at nesting level 0
    // (relative to this if-then-else context)
    int nestingLevel = 0;
    
    for (int i = startIdx; i <= stopIdx; i++) {
        org.antlr.v4.runtime.Token token = tokens.get(i);
        String text = token.getText();
        
        // Skip the first 'if' (that's the start of our context)
        if (i == startIdx && text.equals("if")) {
            continue;
        }
        
        // Track nesting
        if (text.equals("if")) {
            nestingLevel++;
        } else if (text.equals("endif")) {
            nestingLevel--;
        }
        
        // Only match keywords at nesting level 0 (our level)
        if (nestingLevel == 0 && text.equals(keyword)) {
            return i;
        }
    }
    
    return Integer.MAX_VALUE; // Not found
}

}