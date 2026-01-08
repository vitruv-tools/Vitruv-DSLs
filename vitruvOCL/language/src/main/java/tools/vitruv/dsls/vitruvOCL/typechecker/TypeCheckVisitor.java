package tools.vitruv.dsls.vitruvOCL.typechecker;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionArgumentsContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionLiteralExpCSContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionLiteralPartCSContext;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser.CollectionTypeCSContext;
import tools.vitruv.dsls.vitruvOCL.common.AbstractPhaseVisitor;
import tools.vitruv.dsls.vitruvOCL.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvOCL.common.VSUMWrapper;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;

/**
 * Visitor for type checking
 * 
 * purpose: validate type rules and annotations from parse Tree 
 * 
 * output: @link ParseTreeProperty<Type>
 * 
 * error handling: collects errors in ErrorCollector
 * 
 * @see AbstractPhaseVisitor parent Visitor class
 * @see Type Type System
 * @see TypeResolver helper for Type Resolution
 */
public class TypeCheckVisitor extends AbstractPhaseVisitor<Type> {
    
    private final ParseTreeProperty<Type> nodeTypes = new ParseTreeProperty<>();
    
    private final TypeRegistry typeRegistry;
    
    public TypeCheckVisitor(SymbolTable symbolTable, VSUMWrapper vsumWrapper) {
        super(symbolTable, vsumWrapper);
        this.typeRegistry = new TypeRegistry(vsumWrapper);
    }
    
    public ParseTreeProperty<Type> getNodeTypes() {
        return nodeTypes;
    }
    
    @Override
    protected void handleUndefinedSymbol(String name, org.antlr.v4.runtime.ParserRuleContext ctx) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Undefined symbol: " + name, 
                  ErrorSeverity.ERROR, 
                  "type-checker");
    }
    
    // ==================== Binary Operators ====================
    
    @Override
    public Type visitNumber(VitruvOCLParser.NumberContext ctx) {
        Type type = Type.INTEGER;
        nodeTypes.put(ctx, type);
        return type;
    }
    
    @Override
    public Type visitPlusMinus(VitruvOCLParser.PlusMinusContext ctx) {
        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        String operator = ctx.op.getText();
        Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);
        
        if (resultType == Type.ERROR) {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                      "Type mismatch: cannot apply '" + operator + "' to " + leftType + " and " + rightType,
                      ErrorSeverity.ERROR,
                      "type-checker");
        }

        nodeTypes.put(ctx, resultType);
        return resultType;
    }

    @Override
    public Type visitTimesDivide(VitruvOCLParser.TimesDivideContext ctx) { 
        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        String operator = ctx.op.getText();
        Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

        if (resultType == Type.ERROR) {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                      "Type mismatch: cannot apply '" + operator + "' to " + leftType + " and " + rightType,
                      ErrorSeverity.ERROR,
                      "type-checker");
        }

        nodeTypes.put(ctx, resultType);
        return resultType;
    }

    @Override
    public Type visitPrefixedExp(VitruvOCLParser.PrefixedExpContext ctx) {
        return visit(ctx.prefixedExpCS());
    }

    @Override
    public Type visitAndOrXor(VitruvOCLParser.AndOrXorContext ctx) { 
        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);

        String operator = ctx.op.getText();
        Type resultType = TypeResolver.resolveBinaryOp(operator, leftType, rightType);

        if (resultType == Type.ERROR) {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                      "Type mismatch: cannot apply '" + operator + "' to " + leftType + " and " + rightType,
                      ErrorSeverity.ERROR,
                      "type-checker");
        }

        nodeTypes.put(ctx, resultType);
        return resultType;
    }
    
    // ==================== Collection Literals ====================
    
    /**
     * Type-checks collection type declarations.
     * Example: Set, Sequence, Bag, OrderedSet
     */
    @Override
    public Type visitCollectionTypeCS(CollectionTypeCSContext ctx) {
        // Extract collection kind
        String kind = null;
        if (ctx.collectionTypeIDentifier() != null) {
            kind = ctx.collectionTypeIDentifier().getText();
        } else if (ctx.collectionType != null) {
            kind = ctx.collectionType.getText();
        }

        if (kind == null) {
            errors.add(ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(),
                    "Unable to determine collection type",
                    ErrorSeverity.ERROR, "type-checker");
            return Type.ERROR;
        }

        // Create the collection type **with ANY as placeholder for elementType**
        Type collectionType = switch (kind) {
            case "Set" -> Type.set(Type.ANY);
            case "Sequence" -> Type.sequence(Type.ANY);
            case "Bag" -> Type.bag(Type.ANY);
            case "OrderedSet" -> Type.orderedSet(Type.ANY);
            default -> {
                errors.add(ctx.getStart().getLine(), 
                        ctx.getStart().getCharPositionInLine(),
                        "Unknown collection type: " + kind, 
                        ErrorSeverity.ERROR, 
                        "type-checker");
                yield Type.ERROR;
            }
        };

        // Store type in nodeTypes (optional: placeholder)
        nodeTypes.put(ctx, collectionType);
        return collectionType;
    }

    /**
     * Type-checks collection literals.
     * Example: Set{1, 2, 3}, Sequence{1..10}
     */
    @Override
    public Type visitCollectionLiteralExpCS(CollectionLiteralExpCSContext ctx) {
        // Visit the collection type to get the expected type
        // FIXED: Use ctx.collectionTypeCS() instead of ctx.type
        Type collectionType = visit(ctx.collectionTypeCS());
        
        if (collectionType == null || collectionType == Type.ERROR) {
            errors.add(ctx.getStart().getLine(),
                       ctx.getStart().getCharPositionInLine(),
                       "Unable to determine collection type",
                       ErrorSeverity.ERROR, "type-checker");
            // CRITICAL: Still store ERROR type in nodeTypes!
            nodeTypes.put(ctx, Type.ERROR);
            return Type.ERROR;
        }
        
        // Empty collection: Set{}
        if (ctx.argument == null) {
            // CRITICAL: Store type in nodeTypes!
            nodeTypes.put(ctx, collectionType);
            return collectionType;
        }
        
        // Type-check arguments
        Type inferredType = visit(ctx.argument);
        Type expectedElementType = collectionType.getElementType();
        
        // Check element type conformance
        if (inferredType != null && inferredType != Type.ERROR && 
            !inferredType.isConformantTo(expectedElementType)) {
            errors.add(ctx.getStart().getLine(), 
                    ctx.getStart().getCharPositionInLine(),
                    "Collection element type mismatch: expected " + 
                    expectedElementType + " but got " + inferredType, 
                    ErrorSeverity.ERROR, 
                    "type-checker");
        }
        
        // CRITICAL: Store type in nodeTypes!
        nodeTypes.put(ctx, collectionType);
        System.out.println("TypeChecker visitCollectionLiteralExpCS                                                   " + collectionType);
        return collectionType;
    }

    /**
     * Type-checks single collection literal part.
     * Can be a single element or a range (1..10).
     */
    @Override
    public Type visitCollectionLiteralPartCS(CollectionLiteralPartCSContext ctx) {
        Type firstType = visit(ctx.expCS(0));
        
        // Check if it's a range (1..10)
        if (ctx.expCS().size() == 2) {
            Type secondType = visit(ctx.expCS(1));
            
            // Both must be integers for ranges
            if (!firstType.isConformantTo(Type.INTEGER)) {
                errors.add(ctx.getStart().getLine(), 
                        ctx.getStart().getCharPositionInLine(),
                        "Range start must be Integer, got " + firstType, 
                        ErrorSeverity.ERROR, 
                        "type-checker");
            }
            if (!secondType.isConformantTo(Type.INTEGER)) {
                errors.add(ctx.getStart().getLine(), 
                        ctx.getStart().getCharPositionInLine(),
                        "Range end must be Integer, got " + secondType, 
                        ErrorSeverity.ERROR, 
                        "type-checker");
            }
            
            return Type.INTEGER;
        }
        
        // Single element
        return firstType;
    }

    /**
     * Type-checks collection arguments.
     * Validates that all elements have compatible types.
     */
    @Override
    public Type visitCollectionArguments(CollectionArgumentsContext ctx) {
        Type commonType = null;
        
        for (CollectionLiteralPartCSContext partCtx : ctx.collectionLiteralPartCS()) {
            Type partType = visit(partCtx);
            
            if (partType == Type.ERROR) {
                continue; // Skip error types
            }
            
            if (commonType == null) {
                commonType = partType;
            } else if (!partType.isConformantTo(commonType)) {
                // Try the other way around
                if (!commonType.isConformantTo(partType)) {
                    errors.add(ctx.getStart().getLine(), 
                            ctx.getStart().getCharPositionInLine(),
                            "Incompatible types in collection: " + commonType + " and " + partType, 
                            ErrorSeverity.ERROR, 
                            "type-checker");
                    return Type.ERROR;
                } else {
                    // partType is more general
                    commonType = partType;
                }
            }
        }
        
        System.out.println("TypeChecker visitCollectionArguments                                                    " + commonType);
        
        return commonType != null ? commonType : Type.ANY;
    }

    /**
     * Extracts the operation/property name from a navigatingArgExpCS.
     * This is typically a simple identifier or qualified name.
     */
    private String extractOperationName(VitruvOCLParser.NavigatingArgExpCSContext ctx) {
    if (ctx.nameExpCS() != null) {
        return ctx.nameExpCS().getText();
    }
    return ctx.getText();
}

    /**
     * Handles navigation chains: source.operation1().operation2()
     * 
     * For Collection Operations like including/excluding, we need the receiver type
     * which is only available at this level of the parse tree.
     */
    @Override
public Type visitPrefixedExpCS(VitruvOCLParser.PrefixedExpCSContext ctx) {
    // Handle unary operators: -x, not x
    if (!ctx.UnaryOperatorCS().isEmpty()) {
        Type expType = visit(ctx.exp);
        
        for (var op : ctx.UnaryOperatorCS()) {
            String operator = op.getText();
            
            if (operator.equals("-")) {
                if (!expType.isConformantTo(Type.INTEGER)) {
                    errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                              "Unary minus requires numeric type, got " + expType,
                              ErrorSeverity.ERROR, "type-checker");
                    expType = Type.ERROR;
                }
            } else if (operator.equals("not")) {
                if (!expType.isConformantTo(Type.BOOLEAN)) {
                    errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                              "Unary not requires boolean type, got " + expType,
                              ErrorSeverity.ERROR, "type-checker");
                    expType = Type.ERROR;
                }
            }
        }
        
        nodeTypes.put(ctx, expType);
        return expType;
    }
    
    // Get all primary expressions (navigation chain)
    List<VitruvOCLParser.PrimaryExpCSContext> primaries = ctx.primaryExpCS();
    
    if (primaries.isEmpty()) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Empty expression", ErrorSeverity.ERROR, "type-checker");
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
    }
    
    // Single expression without navigation: Set{1,2,3}
    if (primaries.size() == 1) {
        Type type = visit(primaries.get(0));
        nodeTypes.put(ctx, type);
        return type;
    }
    
    // Navigation chain: Set{1,2}.including(3).excluding(1)
    Type currentType = visit(primaries.get(0)); // Receiver type
    
    for (int i = 1; i < primaries.size(); i++) {
        VitruvOCLParser.PrimaryExpCSContext operationCtx = primaries.get(i);
        
        // Check if this is a navigatingExpCS (method call)
        if (operationCtx.navigatingExpCS() != null) {
            VitruvOCLParser.NavigatingExpCSContext navCtx = operationCtx.navigatingExpCS();
            
            // Check if operation name is a collectionOperationName
            if (navCtx.opName != null && navCtx.opName.nameExpCS() != null) {
                VitruvOCLParser.NameExpCSContext nameExpCtx = navCtx.opName.nameExpCS();
                
                // CRITICAL: Cast to NameContext to access collectionOperationName()
                if (nameExpCtx instanceof VitruvOCLParser.NameContext nameContext) {
                    if (nameContext.collectionOperationName() != null) {
                        String opName = nameContext.collectionOperationName().getText();
                        currentType = typeCheckCollectionOperation(navCtx, opName, currentType);
                        continue;
                    }
                }
            }
            
            // Normal navigation - delegate to existing logic
            currentType = visit(operationCtx);
        } else {
            // Other primary expression types
            currentType = visit(operationCtx);
        }
    }
    
    nodeTypes.put(ctx, currentType);
    return currentType;
}


/**
 * Type-checks collection operations: including, excluding, flatten, etc.
 * 
 * @param ctx Navigation context containing the operation
 * @param opName Operation name (including, excluding, etc.)
 * @param receiverType Type of the receiver collection
 * @return Result type of the operation
 */
private Type typeCheckCollectionOperation(
    VitruvOCLParser.NavigatingExpCSContext ctx,
    String opName,
    Type receiverType
) {
    Type resultType;
    
    switch (opName) {
        case "including":
        case "excluding":
            // including(x) / excluding(x): Collection(T) → Collection(T)
            // Argument must be compatible with element type
            
            if (!receiverType.isCollection()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation '" + opName + "' requires collection receiver, got " + receiverType,
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            List<VitruvOCLParser.NavigatingArgCSContext> args = ctx.navigatingArgCS();
            if (args.isEmpty()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation '" + opName + "' requires 1 argument",
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            // Type-check argument
            Type argType = visit(args.get(0).navigatingArgExpCS());
            Type elemType = receiverType.getElementType();
            
            if (argType != null && argType != Type.ERROR && !argType.isConformantTo(elemType)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Argument type " + argType + " not compatible with collection element type " + elemType,
                          ErrorSeverity.ERROR, "type-checker");
            }
            
            // Result has same collection type as receiver
            resultType = receiverType;
            break;
            
        case "includes":
        case "excludes":
            // includes(x) / excludes(x): Collection(T) → Boolean
            
            if (!receiverType.isCollection()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation '" + opName + "' requires collection receiver",
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            args = ctx.navigatingArgCS();
            if (args.isEmpty()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation '" + opName + "' requires 1 argument",
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            // Type-check argument
            visit(args.get(0).navigatingArgExpCS());
            
            resultType = Type.BOOLEAN;
            break;
            
        case "flatten":
            // flatten(): Collection(Collection(T)) → Collection(T)
            
            if (!receiverType.isCollection()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation 'flatten' requires collection receiver",
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            Type innerElemType = receiverType.getElementType();
            if (!innerElemType.isCollection()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation 'flatten' requires Collection(Collection(T)), got " + receiverType,
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            // Preserve collection kind: Set{Set{1}} → Set{1}
            Type flatElementType = innerElemType.getElementType();
            if (receiverType.isUnique() && receiverType.isOrdered()) {
                resultType = Type.orderedSet(flatElementType);
            } else if (receiverType.isUnique()) {
                resultType = Type.set(flatElementType);
            } else if (receiverType.isOrdered()) {
                resultType = Type.sequence(flatElementType);
            } else {
                resultType = Type.bag(flatElementType);
            }
            break;
            
        case "union":
        case "append":
            // union(collection) / append(collection): Collection(T) × Collection(T) → Collection(T)
            
            if (!receiverType.isCollection()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation '" + opName + "' requires collection receiver",
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            args = ctx.navigatingArgCS();
            if (args.isEmpty()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation '" + opName + "' requires 1 argument",
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            // Type-check argument (must be collection)
            Type argCollType = visit(args.get(0).navigatingArgExpCS());
            
            if (argCollType != null && argCollType != Type.ERROR && !argCollType.isCollection()) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Argument must be a collection, got " + argCollType,
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            // Compute common element type
            Type commonElemType = Type.commonSuperType(
                receiverType.getElementType(),
                argCollType.getElementType()
            );
            
            // Determine result collection type based on uniqueness/ordering
            // union: Set ∪ Set = Set, Sequence ∪ Sequence = Sequence
            boolean bothUnique = receiverType.isUnique() && argCollType.isUnique();
            boolean anyOrdered = receiverType.isOrdered() || argCollType.isOrdered();
            
            if (bothUnique && anyOrdered) {
                resultType = Type.orderedSet(commonElemType);
            } else if (bothUnique) {
                resultType = Type.set(commonElemType);
            } else if (anyOrdered) {
                resultType = Type.sequence(commonElemType);
            } else {
                resultType = Type.bag(commonElemType);
            }
            break;
            
        default:
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                      "Unknown collection operation: " + opName,
                      ErrorSeverity.ERROR, "type-checker");
            resultType = Type.ERROR;
    }
    
    nodeTypes.put(ctx, resultType);
    return resultType;
}

// ==================== Navigation ====================
    
    @Override
    public Type visitNavigatingExpCS(VitruvOCLParser.NavigatingExpCSContext ctx) {
        // Start with the base expression (indexExpCS is the source)
        Type sourceType = visit(ctx.indexExpCS());
        
        // Process regular arguments (property access/method calls with .)
        for (VitruvOCLParser.NavigatingArgCSContext arg : ctx.navigatingArgCS()) {
            sourceType = handleNavigatingArg(arg, sourceType);
        }
        
        // Process comma arguments (additional parameters)
        for (VitruvOCLParser.NavigatingCommaArgCSContext commaArg : ctx.navigatingCommaArgCS()) {
            sourceType = handleNavigatingCommaArg(commaArg, sourceType);
        }
        
        // Process semi arguments (iterator variables for select/reject/collect)
        for (VitruvOCLParser.NavigatingSemiAgrsCSContext semiArg : ctx.navigatingSemiAgrsCS()) {
            sourceType = handleNavigatingSemiArgs(semiArg, sourceType);
        }
        
        nodeTypes.put(ctx, sourceType);
        return sourceType;
    }

    /**
     * Handles dot navigation (.)
     * In OCL#, . is used for both object operations and collection operations
     * since everything is a collection with multiplicity.
     */
    private Type handleNavigatingArg(VitruvOCLParser.NavigatingArgCSContext ctx, Type sourceType) {
        String name = extractOperationName(ctx.navigatingArgExpCS());
        
        // Collect argument types
        Type[] argTypes = ctx.expCS().stream()
            .map(this::visit)
            .toArray(Type[]::new);
        
        // Try collection operation (works for all types since everything is a collection)
        Type result = TypeResolver.resolveCollectionOperation(sourceType, name, argTypes);
        if (result != Type.ERROR) {
            return result;
        }
        
        // Try object operation (String methods, etc.)
        if (argTypes.length > 0) {
            result = TypeResolver.resolveObjectOperation(sourceType, name, argTypes);
            if (result != Type.ERROR) {
                return result;
            }
        }
        
        // Try property access (metamodel properties)
        if (argTypes.length == 0) {
            result = resolvePropertyAccess(sourceType, name, ctx);
            if (result != Type.ERROR) {
                return result;
            }
        }
        
        // Nothing worked - error
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                "Unknown operation or property '" + name + "' on type " + sourceType,
                ErrorSeverity.ERROR, "type-checker");
        return Type.ERROR;
    }

    /**
     * TODO wenn variablen
     * Handles comma-separated additional arguments
     * Example: operation(arg1, arg2, arg3)
     * 
     * Structure: , arg : type = init
     */
    private Type handleNavigatingCommaArg(VitruvOCLParser.NavigatingCommaArgCSContext ctx, Type sourceType) {
        // Process additional arguments for multi-parameter operations
        // For now, we just validate the expressions exist
        for (VitruvOCLParser.ExpCSContext exp : ctx.expCS()) {
            visit(exp); // Type check each expression
        }
        
        return sourceType;
    }

    /**
     * TODO wenn Variablen
     * Handles semicolon-separated iterator variables
     * Example: collection->select(x | x > 5)
     * 
     * Structure: ; var : type = exp | body
     */
    private Type handleNavigatingSemiArgs(VitruvOCLParser.NavigatingSemiAgrsCSContext ctx, Type sourceType) {
        // Extract iterator variable name

        // TODO: Register iterator variable in symbol table with element type
        // Type varType = sourceType.getElementType();
        // symbolTable.define(varName, varType);
        
        // Type check the body expression if present
        if (ctx.infixedExpCS() != null) {
            visit(ctx.infixedExpCS());
        }
        
        // Type check the collection expression if present
        if (ctx.expCS() != null) {
            visit(ctx.expCS());
        }
        
        return sourceType;
    }

    /**
     * Resolves property access on objects.
     * TODO: Use TypeRegistry to resolve metamodel properties
     */
    private Type resolvePropertyAccess(Type sourceType, String propertyName,
                                      org.antlr.v4.runtime.ParserRuleContext ctx) {
        // For now, return ERROR - will be implemented with TypeRegistry
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Cannot resolve property '" + propertyName + "' on type " + sourceType,
                  ErrorSeverity.ERROR, "type-checker");
        return Type.ERROR;
    }
}