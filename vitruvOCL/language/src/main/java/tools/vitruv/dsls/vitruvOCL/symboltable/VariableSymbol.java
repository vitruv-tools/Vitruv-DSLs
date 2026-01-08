package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * symbol for variables: local variables, iterator variables, parameter
 * 
 * @see Symbol as parent class for all symbols
 */
public class VariableSymbol extends Symbol {
    
    private final boolean isIteratorVariable;
    
    public VariableSymbol(String name, Type type, Scope definingScope, boolean isIteratorVariable) {
        super(name, type, definingScope);
        this.isIteratorVariable = isIteratorVariable;
    }
    

    public boolean isIteratorVariable() {
        return isIteratorVariable;
    }
}