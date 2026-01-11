package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;

/**
 * symbol for variables: local variables, iterator variables, parameter
 * 
 * @see Symbol as parent class for all symbols
 */
public class VariableSymbol extends Symbol {
    
    private final boolean isIteratorVariable;
    private Value value;  // Runtime value (null during type checking, set during evaluation)
    
    public VariableSymbol(String name, Type type, Scope definingScope, boolean isIteratorVariable) {
        super(name, type, definingScope);
        this.isIteratorVariable = isIteratorVariable;
        this.value = null;
    }
    
    public boolean isIteratorVariable() {
        return isIteratorVariable;
    }
    
    /**
     * Get runtime value (used during evaluation phase).
     * @return value or null if not set
     */
    public Value getValue() {
        return value;
    }
    
    /**
     * Set runtime value (used during evaluation phase).
     * @param value runtime value
     */
    public void setValue(Value value) {
        this.value = value;
    }
}