package tools.vitruv.dsls.vitruvOCL.typechecker;

import java.util.ArrayList;
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
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
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

    @Override
    public Type visitBoolean(VitruvOCLParser.BooleanContext ctx) {
        Type type = Type.BOOLEAN;
        nodeTypes.put(ctx, type);
        return type;
    }
    
    @Override
    public Type visitString(VitruvOCLParser.StringContext ctx) {
        Type type = Type.STRING;
        nodeTypes.put(ctx, type);
        return type;
    }

    @Override
    public Type visitNestedExpCS(VitruvOCLParser.NestedExpCSContext ctx) {
        // Evaluate the expression inside the parentheses
        // nestedExpCS can have multiple expressions, we take the last one
        List<VitruvOCLParser.ExpCSContext> exps = ctx.expCS();
        
        if (exps.isEmpty()) {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Empty nested expression",
                    ErrorSeverity.ERROR, "type-checker");
            nodeTypes.put(ctx, Type.ERROR);
            return Type.ERROR;
        }
        
        // Type-check all expressions, return type of last one
        Type resultType = null;
        for (VitruvOCLParser.ExpCSContext exp : exps) {
            resultType = visit(exp);
        }
        
        nodeTypes.put(ctx, resultType);
        return resultType;
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

    /**
     * Type-checks 'implies' operator.
     */
    @Override
    public Type visitImplies(VitruvOCLParser.ImpliesContext ctx) {
        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);
        
        Type resultType = Type.BOOLEAN;
        
        // Both operands must be Boolean
        if (!leftType.isConformantTo(Type.BOOLEAN)) {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Left operand of 'implies' must be Boolean, got " + leftType,
                    ErrorSeverity.ERROR, "type-checker");
            resultType = Type.ERROR;
        }
        
        if (!rightType.isConformantTo(Type.BOOLEAN)) {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Right operand of 'implies' must be Boolean, got " + rightType,
                    ErrorSeverity.ERROR, "type-checker");
            resultType = Type.ERROR;
        }
        
        nodeTypes.put(ctx, resultType);
        return resultType;
    }


    // ==================== Comparison Operators ====================

    /**
     * Type-checks equality and ordering operations: =, <>, <, <=, >, >=
     */
    @Override
    public Type visitEqualOperations(VitruvOCLParser.EqualOperationsContext ctx) {
        Type leftType = visit(ctx.left);
        Type rightType = visit(ctx.right);
        
        String operator = ctx.op.getText();
        
        // All comparison operators return Boolean
        Type resultType = Type.BOOLEAN;
        
        // Type check: operands must be comparable
        if (!areComparable(leftType, rightType)) {
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Cannot compare incompatible types: " + leftType + " and " + rightType,
                    ErrorSeverity.ERROR, "type-checker");
            resultType = Type.ERROR;
        }
        
        nodeTypes.put(ctx, resultType);
        return resultType;
    }

    /**
     * Checks if two types are comparable for equality/ordering.
     */
    private boolean areComparable(Type t1, Type t2) {
        // ERROR types are always compatible (error recovery)
        if (t1 == Type.ERROR || t2 == Type.ERROR) return true;
        
        // Same types are always comparable
        if (t1.equals(t2)) return true;
        
        // Check conformance in both directions
        if (t1.isConformantTo(t2) || t2.isConformantTo(t1)) return true;
        
        return false;
    }
    
    // ==================== Collections ====================
    
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
    // Visit the collection type to get the collection kind (Set, Sequence, etc.)
    Type collectionType = visit(ctx.collectionTypeCS());
    
    if (collectionType == null || collectionType == Type.ERROR) {
        errors.add(ctx.getStart().getLine(),
                   ctx.getStart().getCharPositionInLine(),
                   "Unable to determine collection type",
                   ErrorSeverity.ERROR, "type-checker");
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
    }
    
    // Empty collection: Set{} - keep ANY as element type
    if (ctx.argument == null) {
        nodeTypes.put(ctx, collectionType);
        return collectionType;
    }
    
    // Type-check arguments and infer actual element type
    Type inferredElementType = visit(ctx.argument);
    
    if (inferredElementType == null || inferredElementType == Type.ERROR) {
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
    }
    
    // Create NEW collection type with INFERRED element type
    Type resultType;
    if (collectionType.isUnique() && collectionType.isOrdered()) {
        resultType = Type.orderedSet(inferredElementType);
    } else if (collectionType.isUnique()) {
        resultType = Type.set(inferredElementType);
    } else if (collectionType.isOrdered()) {
        resultType = Type.sequence(inferredElementType);
    } else {
        resultType = Type.bag(inferredElementType);
    }
    
    nodeTypes.put(ctx, resultType);
    return resultType;
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
                    continue;
                }
                
                if (commonType == null) {
                    commonType = partType;
                } else if (!partType.equals(commonType)) {
                    // Types differ - find common supertype
                    Type superType = Type.commonSuperType(commonType, partType);
                    
                    if (superType != null) {
                        commonType = superType;
                    } else {
                        // No common type - fall back to ANY
                        commonType = Type.ANY;
                    }
                }
            }
            
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
/**
 * Handles navigation chains and unary operators: -5, not true, Set{1,2}.size()
 * 
 * For Collection Operations like including/excluding, we need the receiver type
 * which is only available at this level of the parse tree.
 */
@Override
public Type visitPrefixedExpCS(VitruvOCLParser.PrefixedExpCSContext ctx) {
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
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Empty expression", ErrorSeverity.ERROR, "type-checker");
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
    }
    
    // Start with first primary
    Type currentType = visit(primaries.get(0));
    
    // Type-check unary operators
    for (int i = 0; i < unaryOpCount; i++) {
        String op = ctx.getChild(i).getText();
        
        if (op.equals("-")) {
            if (!currentType.isConformantTo(Type.INTEGER)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Unary minus requires numeric type, got " + currentType,
                          ErrorSeverity.ERROR, "type-checker");
                currentType = Type.ERROR;
            }
            // Result type stays INTEGER
        } else if (op.equals("not")) {
            if (!currentType.isConformantTo(Type.BOOLEAN)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Unary not requires boolean type, got " + currentType,
                          ErrorSeverity.ERROR, "type-checker");
                currentType = Type.ERROR;
            }
            // Result type stays BOOLEAN
        }
    }
    
    // Handle navigation chain (if there are more primaries)
    for (int i = 1; i < primaries.size(); i++) {
        VitruvOCLParser.PrimaryExpCSContext operationCtx = primaries.get(i);
        
        // Check if this is a navigatingExpCS (method call)
        if (operationCtx.navigatingExpCS() != null) {
            VitruvOCLParser.NavigatingExpCSContext navCtx = operationCtx.navigatingExpCS();
            
            // Check if operation name is a collectionOperationName OR stringOperationName
            if (navCtx.opName != null && navCtx.opName.nameExpCS() != null) {
                VitruvOCLParser.NameExpCSContext nameExpCtx = navCtx.opName.nameExpCS();
                
                // CRITICAL: Cast to NameContext to access collectionOperationName()
                if (nameExpCtx instanceof VitruvOCLParser.NameContext nameContext) {
                    // Collection operations
                    if (nameContext.collectionOperationName() != null) {
                        String opName = nameContext.collectionOperationName().getText();
                        currentType = typeCheckCollectionOperation(navCtx, opName, currentType);
                        continue;
                    }
                    
                    // String operations (NEW!)
                    if (nameContext.stringOperationName() != null) {
                        String opName = nameContext.stringOperationName().getText();
                        currentType = typeCheckStringOperation(navCtx, opName, currentType);
                        continue;
                    }
                }
            }
            
            // Check for no-arg operations (size, isEmpty, etc.)
            if (navCtx.navigatingArgCS().isEmpty() && navCtx.opName != null) {
                String opName = navCtx.opName.nameExpCS().getText();
                
                switch (opName) {
                    case "size":
                        currentType = Type.INTEGER;
                        break;
                    case "isEmpty":
                    case "notEmpty":
                        currentType = Type.BOOLEAN;
                        break;
                    case "first":
                    case "last":
                        if (currentType.isCollection()) {
                            currentType = currentType.getElementType();
                        } else {
                            errors.add(navCtx.getStart().getLine(), navCtx.getStart().getCharPositionInLine(),
                                      "Operation '" + opName + "' requires collection type",
                                      ErrorSeverity.ERROR, "type-checker");
                            currentType = Type.ERROR;
                        }
                        break;
                    case "reverse":
                        if (!currentType.isCollection() || !currentType.isOrdered()) {
                            errors.add(navCtx.getStart().getLine(), navCtx.getStart().getCharPositionInLine(),
                                      "Operation 'reverse' requires ordered collection",
                                      ErrorSeverity.ERROR, "type-checker");
                            currentType = Type.ERROR;
                        }
                        // Type stays the same
                        break;
                    case "flatten":
                        if (!currentType.isCollection()) {
                            errors.add(navCtx.getStart().getLine(), navCtx.getStart().getCharPositionInLine(),
                                      "Operation 'flatten' requires collection",
                                      ErrorSeverity.ERROR, "type-checker");
                            currentType = Type.ERROR;
                        } else {
                            Type innerType = currentType.getElementType();
                            if (innerType.isCollection()) {
                                currentType = innerType; // Flatten one level
                            }
                        }
                        break;
                    default:
                        // Normal navigation - delegate to existing logic
                        currentType = visit(operationCtx);
                }
            } else {
                // Has arguments or not a special operation - delegate
                currentType = visit(operationCtx);
            }
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
        case "sum":
case "max":
case "min":
case "avg":
    if (!receiverType.isCollection()) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation '" + opName + "' requires collection receiver",
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    Type elemTypes = receiverType.getElementType();
    
    // Allow ANY for empty collections
    if (elemTypes != Type.ANY && !elemTypes.isConformantTo(Type.INTEGER)) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation '" + opName + "' requires numeric collection, got " + receiverType,
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    resultType = Type.set(Type.INTEGER);
    break;

case "abs":
case "floor":
case "ceil":
case "round":
    // Per-element numeric operations: Collection(Number) → Collection(Number)
    if (!receiverType.isCollection()) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation '" + opName + "' requires collection receiver",
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    Type elem = receiverType.getElementType();
    if (!elem.isConformantTo(Type.INTEGER)) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation '" + opName + "' requires numeric collection, got " + receiverType,
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    // Preserve collection type
    resultType = receiverType;
    break;

case "oclIsKindOf":
    // oclIsKindOf(TypeName) : Collection(T) → Collection(Boolean)
    
    if (!receiverType.isCollection()) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation 'oclIsKindOf' requires collection receiver",
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    List<VitruvOCLParser.NavigatingArgCSContext> argsKindOF = ctx.navigatingArgCS();
    if (argsKindOF.isEmpty()) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation 'oclIsKindOf' requires 1 type argument",
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    if (argsKindOF.size() > 1) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation 'oclIsKindOf' expects exactly 1 argument, got " + argsKindOF.size(),
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    // Argument ist ein Type Name (Integer, String, Boolean, etc.)
    // Validation dass Type existiert kommt später mit Symbol Table
    
    // Result: Collection(Boolean) - preserve collection kind
    if (receiverType.isUnique() && receiverType.isOrdered()) {
        resultType = Type.orderedSet(Type.BOOLEAN);
    } else if (receiverType.isUnique()) {
        resultType = Type.set(Type.BOOLEAN);
    } else if (receiverType.isOrdered()) {
        resultType = Type.sequence(Type.BOOLEAN);
    } else {
        resultType = Type.bag(Type.BOOLEAN);
    }
    break;

case "lift":
    // lift(): Collection(T) → Collection(Collection(T))
    if (!receiverType.isCollection()) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "Operation 'lift' requires collection receiver",
                  ErrorSeverity.ERROR, "type-checker");
        resultType = Type.ERROR;
        break;
    }
    
    // Wrap collection in another collection of same kind
    if (receiverType.isUnique() && receiverType.isOrdered()) {
        resultType = Type.orderedSet(receiverType);
    } else if (receiverType.isUnique()) {
        resultType = Type.set(receiverType);
    } else if (receiverType.isOrdered()) {
        resultType = Type.sequence(receiverType);
    } else {
        resultType = Type.bag(receiverType);
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


/**
 * Type-checks String operations: concat, substring, toUpper, toLower, etc.
 * Similar structure to typeCheckCollectionOperation.
 * 
 * @param ctx Navigation context containing the operation
 * @param opName Operation name (concat, substring, etc.)
 * @param receiverType Type of the receiver (should be String)
 * @return Result type of the operation
 */
private Type typeCheckStringOperation(
    VitruvOCLParser.NavigatingExpCSContext ctx,
    String opName,
    Type receiverType
) {
    // Verify receiver is String
    if (!receiverType.isConformantTo(Type.STRING)) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                  "String operation '" + opName + "' requires String receiver, got " + receiverType,
                  ErrorSeverity.ERROR, "type-checker");
        return Type.ERROR;
    }
    
    Type resultType;
    
    // WICHTIG: Argumente sind in navigatingArgCS() + navigatingCommaArgCS()
    List<VitruvOCLParser.NavigatingArgCSContext> regularArgs = ctx.navigatingArgCS();
    List<VitruvOCLParser.NavigatingCommaArgCSContext> commaArgs = ctx.navigatingCommaArgCS();
    
    // Collect ALL argument types
    List<Type> argTypes = new ArrayList<>();
    
    // First argument (if exists)
    if (!regularArgs.isEmpty()) {
        Type argType = visit(regularArgs.get(0).navigatingArgExpCS());
        argTypes.add(argType);
    }
    
    // Additional comma arguments
    for (VitruvOCLParser.NavigatingCommaArgCSContext commaArg : commaArgs) {
        Type argType = visit(commaArg.navigatingArgExpCS());
        argTypes.add(argType);
    }
    
    int totalArgs = argTypes.size();
    
    switch (opName) {
        case "concat":
            // concat(String) : String
            if (totalArgs != 1) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation 'concat' requires 1 argument, got " + totalArgs,
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            if (!argTypes.get(0).isConformantTo(Type.STRING)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "concat() requires String argument, got " + argTypes.get(0),
                          ErrorSeverity.ERROR, "type-checker");
            }
            
            resultType = Type.STRING;
            break;
            
        case "substring":
            // substring(Integer, Integer) : String
            if (totalArgs != 2) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation 'substring' requires 2 arguments (start, end), got " + totalArgs,
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            if (!argTypes.get(0).isConformantTo(Type.INTEGER)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "substring() start index must be Integer, got " + argTypes.get(0),
                          ErrorSeverity.ERROR, "type-checker");
            }
            
            if (!argTypes.get(1).isConformantTo(Type.INTEGER)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "substring() end index must be Integer, got " + argTypes.get(1),
                          ErrorSeverity.ERROR, "type-checker");
            }
            
            resultType = Type.STRING;
            break;
            
        case "toUpper":
        case "toLower":
            // toUpper() : String, toLower() : String
            if (totalArgs != 0) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation '" + opName + "' takes no arguments, got " + totalArgs,
                          ErrorSeverity.ERROR, "type-checker");
            }
            
            resultType = Type.STRING;
            break;
            
        case "indexOf":
            // indexOf(String) : Integer
            if (totalArgs != 1) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation 'indexOf' requires 1 argument, got " + totalArgs,
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            if (!argTypes.get(0).isConformantTo(Type.STRING)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "indexOf() requires String argument, got " + argTypes.get(0),
                          ErrorSeverity.ERROR, "type-checker");
            }
            
            resultType = Type.INTEGER;
            break;
            
        case "equalsIgnoreCase":
            // equalsIgnoreCase(String) : Boolean
            if (totalArgs != 1) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "Operation 'equalsIgnoreCase' requires 1 argument, got " + totalArgs,
                          ErrorSeverity.ERROR, "type-checker");
                resultType = Type.ERROR;
                break;
            }
            
            if (!argTypes.get(0).isConformantTo(Type.STRING)) {
                errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                          "equalsIgnoreCase() requires String argument, got " + argTypes.get(0),
                          ErrorSeverity.ERROR, "type-checker");
            }
            
            resultType = Type.BOOLEAN;
            break;
            
        default:
            errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                      "Unknown string operation: " + opName,
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

    // ==================== If-Then-Else ====================
    private org.antlr.v4.runtime.TokenStream tokens;

    public void setTokenStream(org.antlr.v4.runtime.TokenStream tokens) {
        this.tokens = tokens;
    }

@Override
public Type visitIfExpCS(VitruvOCLParser.IfExpCSContext ctx) {
    // Partition expressions by keywords: if...then...else...endif
    List<VitruvOCLParser.ExpCSContext> allExps = ctx.expCS();
    
    if (allExps.isEmpty()) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                "If expression requires condition, then branch, and else branch",
                ErrorSeverity.ERROR, "type-checker");
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
    }
    
    // Find 'then', 'else', 'endif' token positions in the token stream
    List<VitruvOCLParser.ExpCSContext> condExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> thenExps = new ArrayList<>();
    List<VitruvOCLParser.ExpCSContext> elseExps = new ArrayList<>();
    
    // Parse structure by finding keyword positions
    int thenTokenIndex = findKeywordToken(ctx, "then");
    int elseTokenIndex = findKeywordToken(ctx, "else");
    
    // Partition expressions based on token positions
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
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                "If expression missing condition, then, or else branch",
                ErrorSeverity.ERROR, "type-checker");
        nodeTypes.put(ctx, Type.ERROR);
        return Type.ERROR;
    }
    
    // Type-check all expressions in each section, use last one for branch type
    Type condType = null;
    for (VitruvOCLParser.ExpCSContext exp : condExps) {
        condType = visit(exp);
    }
    
    Type thenType = null;
    for (VitruvOCLParser.ExpCSContext exp : thenExps) {
        thenType = visit(exp);
    }
    
    Type elseType = null;
    for (VitruvOCLParser.ExpCSContext exp : elseExps) {
        elseType = visit(exp);
    }
    
    // Condition must be Boolean
    if (condType != null && !condType.isConformantTo(Type.BOOLEAN)) {
        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                "If condition must be Boolean, got: " + condType,
                ErrorSeverity.ERROR, "type-checker");
    }
    
    // Both branches must have compatible types
    if (thenType != null && elseType != null) {
        Type resultType;
        
        // Special case: if one branch is a collection with Any element type,
        // use the other branch's type (prefer more specific type)
        if (thenType.isCollection() && elseType.isCollection()) {
            Type thenElemType = thenType.getElementType();
            Type elseElemType = elseType.getElementType();
            
            if (thenElemType == Type.ANY && elseElemType != Type.ANY) {
                // Then branch is {Any}, else branch is specific → use else type
                resultType = elseType;
            } else if (elseElemType == Type.ANY && thenElemType != Type.ANY) {
                // Else branch is {Any}, then branch is specific → use then type
                resultType = thenType;
            } else {
                // Both have concrete element types (or both have Any)
                // Check if they're compatible
                if (!thenType.equals(elseType)) {
                    // Try common supertype
                    if (thenType.isConformantTo(elseType)) {
                        resultType = elseType;
                    } else if (elseType.isConformantTo(thenType)) {
                        resultType = thenType;
                    } else {
                        errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                                "If branches have incompatible types: then=" + thenType + ", else=" + elseType,
                                ErrorSeverity.ERROR, "type-checker");
                        nodeTypes.put(ctx, Type.ERROR);
                        return Type.ERROR;
                    }
                } else {
                    resultType = thenType;
                }
            }
        } else {
            // Non-collections or mixed types
            if (!thenType.equals(elseType)) {
                // Check if types are conformant
                if (thenType.isConformantTo(elseType)) {
                    resultType = elseType;
                } else if (elseType.isConformantTo(thenType)) {
                    resultType = thenType;
                } else {
                    errors.add(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                            "If branches have incompatible types: then=" + thenType + ", else=" + elseType,
                            ErrorSeverity.ERROR, "type-checker");
                    nodeTypes.put(ctx, Type.ERROR);
                    return Type.ERROR;
                }
            } else {
                resultType = thenType;
            }
        }
        
        nodeTypes.put(ctx, resultType);
        return resultType;
    }
    
    Type resultType = thenType != null ? thenType : Type.ERROR;
    nodeTypes.put(ctx, resultType);
    return resultType;
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