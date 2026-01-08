package tools.vitruv.dsls.vitruvOCL.typechecker;

/**
 * OCL# Multiplicity.
 * 
 * Represents the multiplicity of a type:
 * - SINGLETON (!T!): Exactly one element
 * - OPTIONAL (?T?): Zero or one element
 * - SET {T}: Unordered, unique
 * - SEQUENCE [T]: Ordered, non-unique
 * - BAG {{T}}: Unordered, non-unique
 * - ORDERED_SET <T>: Ordered, unique
 */
public enum Multiplicity {
    SINGLETON("!", "!"),
    OPTIONAL("?", "?"),
    SET("{", "}"),
    SEQUENCE("[", "]"),
    BAG("{{", "}}"),
    ORDERED_SET("<", ">");
    
    private final String symbol;
    private final String closingSymbol;
    
    Multiplicity(String symbol, String closingSymbol) {
        this.symbol = symbol;
        this.closingSymbol = closingSymbol;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getClosingSymbol() {
        return closingSymbol;
    }
    
    public boolean isCollection() {
        return this == SET || this == SEQUENCE || this == BAG || this == ORDERED_SET;
    }
    
    public boolean isUnique() {
        return this == SET || this == ORDERED_SET;
    }
    
    public boolean isOrdered() {
        return this == SEQUENCE || this == ORDERED_SET;
    }
    
    /**
     * OCL# Subtyping: !T! <: ?T? <: {T}
     */
    public boolean isConformantTo(Multiplicity other) {
        if (this == other) return true;
        
        // Singleton conforms to everything
        if (this == SINGLETON) return true;
        
        // Optional conforms to collections
        if (this == OPTIONAL && other.isCollection()) return true;
        
        return false;
    }
}