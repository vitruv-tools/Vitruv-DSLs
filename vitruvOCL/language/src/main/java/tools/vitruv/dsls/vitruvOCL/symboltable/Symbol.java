package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * Parent Class for all scopes
 * example symbols
 * - VariableSymbol: local variables, iterator variables
 * - TypeSymbol: metaclasses
 * - OperationSymbol: ocl operations
 * 
 * @see VariableSymbol for concrete Implementation
 * @see TypeSymbol for concrete Implementation
 * @see OperationSymbol for concrete Implementation
 */
public abstract class Symbol {
    
    protected final String name;
    protected final Type type;
    protected final Scope definingScope;
    

    protected Symbol(String name, Type type, Scope definingScope) {
        this.name = name;
        this.type = type;
        this.definingScope = definingScope;
    }
    

    public String getName() {
        return name;
    }
    

    public Type getType() {
        return type;
    }
    
    
    public Scope getDefiningScope() {
        return definingScope;
    }
}