package tools.vitruv.dsls.vitruvOCL.typechecker;

/**
 * Static Helper-Methoden für Type Resolution und Type Operations.
 * 
 * <p><b>Zweck:</b> Gemeinsame Type-Resolution-Logik, die sowohl TypeCheckVisitor
 * als auch später der Evaluator verwenden können.</p>
 * 
 * <p><b>Funktionen:</b></p>
 * <ul>
 *   <li>Binary Operator Type Resolution: {@code resolveBinaryOp("+", Integer, Integer) -> Integer}</li>
 *   <li>Type Conformance: {@code isConformantTo(subtype, supertype)}</li>
 *   <li>Common Supertype: {@code commonSupertype(Type a, Type b)}</li>
 *   <li>Collection Element Type Extraction</li>
 * </ul>
 * 
 * <p><b>Keine Duplikation:</b> Diese Logik ist gleich für Type Checking und Evaluation,
 * daher zentral in Helper-Klasse.</p>
 */
public class TypeResolver {
    
    private TypeResolver() {} // Pure static utility class
    
    /**
     * Löst den Result-Type einer binären Operation auf.
     * @param operator "+", "-", "*", "/", "=", "<", etc.
     * @param leftType Type des linken Operanden
     * @param rightType Type des rechten Operanden
     * @return Result Type oder Type.ERROR bei Type Mismatch
     */
    public static Type resolveBinaryOp(String operator, Type leftType, Type rightType) {
        // Wenn einer ERROR ist, gib ERROR zurück (verhindert Follow-up Errors)
        if (leftType == Type.ERROR || rightType == Type.ERROR) {
            return Type.ERROR;
        }
        
        // Arithmetic Operations
        if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")) {
            // Integer + Integer -> Integer
            if (leftType == Type.INTEGER && rightType == Type.INTEGER) {
                return Type.INTEGER;
            }
            if (leftType == Type.DOUBLE && rightType == Type.DOUBLE) {
                return Type.DOUBLE;
            }
            if (leftType == Type.INTEGER && rightType == Type.DOUBLE) {
                return Type.DOUBLE;
            }
            if (leftType == Type.DOUBLE && rightType == Type.INTEGER) {
                return Type.DOUBLE;
            }
            return Type.ERROR; // Type mismatch
        }

        // Logical Operations
        if (operator.equals("and") || operator.equals("or") || operator.equals("xor")) {
            if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
                return Type.BOOLEAN;
            }
            return Type.ERROR; // Type mismatch
        }

        
        
        // Logical and Numerical Comparison Operations
        if (operator.equals("==") || operator.equals("!=")) {
            // Integer + Integer -> Integer
            if (leftType == Type.INTEGER && rightType == Type.INTEGER) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.INTEGER && rightType == Type.DOUBLE) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.DOUBLE && rightType == Type.INTEGER) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.STRING && rightType == Type.STRING) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.STRING && rightType == Type.DOUBLE) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.STRING && rightType == Type.INTEGER) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.STRING && rightType == Type.BOOLEAN) {
                return Type.BOOLEAN;
            }
             if (leftType == Type.DOUBLE && rightType == Type.STRING) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.INTEGER && rightType == Type.STRING) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.BOOLEAN && rightType == Type.STRING) {
                return Type.BOOLEAN;
            }
            return Type.ERROR; // Type mismatch
        }

        // Numerical Comparison Operations
        if (operator.equals("<") || operator.equals(">") || operator.equals(">=") || operator.equals("<=")) {
            // Integer + Integer -> Integer
            if (leftType == Type.INTEGER && rightType == Type.INTEGER) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.INTEGER && rightType == Type.DOUBLE) {
                return Type.BOOLEAN;
            }
            if (leftType == Type.DOUBLE && rightType == Type.INTEGER) {
                return Type.BOOLEAN;
            }
            return Type.ERROR; // Type mismatch
        }
        
        return Type.ERROR; // Unknown operator
    }

    






    /**
     * resolves the result-Type of a unary Operation auf.
     * @param operator "-", "not", etc.
     * @param operandType Type des Operanden
     * @return Result Type or Type.ERROR at Type Mismatch
     */
    public static Type resolveUnaryOp(String operator, Type operandType) {
        return switch (operator) {
            case "-" -> operandType.isConformantTo(Type.INTEGER) ? Type.INTEGER : Type.ERROR;
            case "not" -> operandType.isConformantTo(Type.BOOLEAN) ? Type.BOOLEAN : Type.ERROR;
            default -> Type.ERROR;
        };
    }









    /**
     * Resolves the result type of a collection operation.
     * 
     * @param sourceType The collection type
     * @param operationName The operation name (includes, size, select, etc.)
     * @param argumentTypes The types of operation arguments
     * @return The result type or Type.ERROR
     */
    public static Type resolveCollectionOperation(Type sourceType, String operationName, 
                                                Type... argumentTypes) {
        if (!sourceType.isCollection()) {
            return Type.ERROR;
        }
        
        Type elementType = sourceType.getElementType();
        
        switch (operationName) {
            // Query operations -> Boolean
            case "includes":
            case "excludes":
            case "isEmpty":
            case "notEmpty":
                return Type.BOOLEAN;
            
            // Size -> Integer
            case "size":
                return Type.INTEGER;
            
            // Including/Excluding -> Same collection type
            case "including":
            case "excluding":
                // Validate argument type if provided
                if (argumentTypes.length > 0) {
                    if (!argumentTypes[0].isConformantTo(elementType)) {
                        return Type.ERROR;
                    }
                }
                return sourceType;
            
            // Filter operations -> Same collection type
            case "select":
            case "reject":
                return sourceType;
            
            // Collect -> Collection of transformed type
            case "collect":
                // Requires lambda type inference (simplified for now)
                return Type.set(Type.ANY);
            
            // Element extraction -> Optional of element type
            case "first":
            case "last":
            case "any":
                return Type.optional(elementType);
            
            // Flatten -> Unwrap nested collections
            case "flatten":
                if (elementType.isCollection()) {
                    return Type.set(elementType.getElementType());
                }
                return sourceType;
            
            // Sum -> Integer or Double
            case "sum":
                if (elementType == Type.INTEGER) {
                    return Type.INTEGER;
                }
                if (elementType == Type.DOUBLE) {
                    return Type.DOUBLE;
                }
                return Type.ERROR;
            
            // Type conversion operations
            case "asSet":
                return Type.set(elementType);
            
            case "asSequence":
            case "asBag":
            case "asOrderedSet":
                return Type.sequence(elementType);
            
            // Union/intersection -> Same collection type
            case "union":
            case "intersection":
                if (argumentTypes.length > 0 && argumentTypes[0].isCollection()) {
                    // Check element type compatibility
                    Type otherElement = argumentTypes[0].getElementType();
                    if (elementType.isConformantTo(otherElement) || 
                        otherElement.isConformantTo(elementType)) {
                        return sourceType;
                    }
                }
                return Type.ERROR;
            
            default:
                return Type.ERROR;
        }
    }

    /**
     * Checks if an operation is a valid collection operation.
     */
    public static boolean isCollectionOperation(String operationName) {
        return switch (operationName) {
            case "includes", "excludes", "isEmpty", "notEmpty", "size",
                "including", "excluding",  // <- ADDED!
                "select", "reject", "collect", "first", "last", "any",
                "union", "intersection", "flatten", "sum", 
                "asSet", "asSequence", "asBag", "asOrderedSet"
                -> true;
            default -> false;
        };
    }




    /**
     * Resolves object operations (non-collection methods).
     * 
     * @param sourceType The type of the object
     * @param operationName The operation name (toUpper, size, etc.)
     * @param argumentTypes The types of operation arguments
     * @return The result type or Type.ERROR
     */
    public static Type resolveObjectOperation(Type sourceType, String operationName, 
                                            Type... argumentTypes) {
        // String operations
        if (sourceType == Type.STRING) {
            switch (operationName) {
                case "size":
                case "length":
                    return Type.INTEGER;
                
                case "toUpper":
                case "toLower":
                case "trim":
                case "substring":
                    return Type.STRING;
                
                case "startsWith":
                case "endsWith":
                case "contains":
                case "equalsIgnoreCase":
                    return Type.BOOLEAN;
                
                case "concat":
                    // concat requires a String argument
                    if (argumentTypes.length > 0 && argumentTypes[0] == Type.STRING) {
                        return Type.STRING;
                    }
                    return Type.ERROR;
                
                default:
                    return Type.ERROR;
            }
        }
        
        // Integer/Double operations
        if (sourceType == Type.INTEGER || sourceType == Type.DOUBLE) {
            switch (operationName) {
                case "abs":
                case "max":
                case "min":
                    return sourceType;
                
                case "toString":
                    return Type.STRING;
                
                default:
                    return Type.ERROR;
            }
        }
        
        // Boolean operations
        if (sourceType == Type.BOOLEAN) {
            switch (operationName) {
                case "toString":
                    return Type.STRING;
                
                default:
                    return Type.ERROR;
            }
        }
        
        // Unknown type or operation
        return Type.ERROR;
    }

    /**
     * Checks if an operation is a valid object operation for the given type.
     */
    public static boolean isObjectOperation(Type sourceType, String operationName) {
        return resolveObjectOperation(sourceType, operationName) != Type.ERROR;
    }










    
    
    /**
     * Prüft ob subtype konform zu supertype ist.
     */
    public static boolean isConformantTo(Type subtype, Type supertype) {
        // Wird implementiert
        return false;
    }
    
    /**
     * Findet den gemeinsamen Supertype zweier Types.
     */
    public static Type commonSupertype(Type a, Type b) {
        // Wird implementiert
        return null;
    }
}