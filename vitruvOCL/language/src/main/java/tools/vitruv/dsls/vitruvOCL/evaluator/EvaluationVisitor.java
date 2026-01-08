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
    // Handle unary operators: -x, not x
    if (!ctx.UnaryOperatorCS().isEmpty()) {
        Value expValue = visit(ctx.exp);
        
        for (var op : ctx.UnaryOperatorCS()) {
            String operator = op.getText();
            
            if (operator.equals("-")) {
                if (expValue.size() != 1) {
                    return error("Unary minus requires singleton operand", ctx);
                }
                OCLElement elem = expValue.getElements().get(0);
                if (!(elem instanceof OCLElement.IntValue)) {
                    return error("Unary minus requires integer operand", ctx);
                }
                int value = ((OCLElement.IntValue) elem).value();
                expValue = Value.intValue(-value);
            } else if (operator.equals("not")) {
                if (expValue.size() != 1) {
                    return error("Unary not requires singleton operand", ctx);
                }
                OCLElement elem = expValue.getElements().get(0);
                if (!(elem instanceof OCLElement.BoolValue)) {
                    return error("Unary not requires boolean operand", ctx);
                }
                boolean value = ((OCLElement.BoolValue) elem).value();
                expValue = Value.boolValue(!value);
            }
        }
        
        return expValue;
    }
    
    List<VitruvOCLParser.PrimaryExpCSContext> primaries = ctx.primaryExpCS();
    
    if (primaries.isEmpty()) {
        return error("Empty expression", ctx);
    }
    
    if (primaries.size() == 1) {
        return visit(primaries.get(0));
    }
    
    // Navigation chain
    Value currentValue = visit(primaries.get(0));
    
    for (int i = 1; i < primaries.size(); i++) {
        VitruvOCLParser.PrimaryExpCSContext operationCtx = primaries.get(i);
        
        if (operationCtx.navigatingExpCS() != null) {
            VitruvOCLParser.NavigatingExpCSContext navCtx = operationCtx.navigatingExpCS();
            
            boolean handledAsCollectionOp = false;
            
            // Check for collectionOperationName (including, excluding, etc.)
            if (navCtx.opName != null && navCtx.opName.nameExpCS() != null) {
                VitruvOCLParser.NameExpCSContext nameExpCtx = navCtx.opName.nameExpCS();
                
                if (nameExpCtx instanceof VitruvOCLParser.NameContext nameContext) {
                    if (nameContext.collectionOperationName() != null) {
                        String opName = nameContext.collectionOperationName().getText();
                        currentValue = evaluateCollectionOperation(navCtx, opName, currentValue);
                        handledAsCollectionOp = true;
                    }
                }
            }
            
            if (!handledAsCollectionOp) {
                // Check if there are navigatingArgs (operations with arguments)
                if (!navCtx.navigatingArgCS().isEmpty()) {
                    // Has arguments - delegate to handleNavigationStep
                    for (VitruvOCLParser.NavigatingArgCSContext arg : navCtx.navigatingArgCS()) {
                        currentValue = handleNavigationStep(arg, currentValue);
                    }
                } else {
                    // No arguments - operation is in opName (like size(), isEmpty())
                    String opName = navCtx.opName.nameExpCS().getText();
                    
                    // Handle no-arg operations directly
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




}