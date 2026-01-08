package tools.vitruv.dsls.vitruvOCL.symboltable;

import java.util.List;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * symbol for operations
 * for example
 * - collection operations
 * 
 * @see Symbol parent class for all symbols
 */
public class OperationSymbol extends Symbol {
    
    private final Type returnType;
    private final List<Type> parameterTypes;
    private final boolean isCollectionOperation;
    
    /**
     * @param name name of operation
     * @param type type of operation
     * @param definingScope scope of operation
     * @param returnType return type
     * @param parameterTypes parameter list
     * @param isCollectionOperation is true for collections else false
     */
    public OperationSymbol(String name, Type type, Scope definingScope, 
                          Type returnType, List<Type> parameterTypes, 
                          boolean isCollectionOperation) {
        super(name, type, definingScope);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.isCollectionOperation = isCollectionOperation;
    }
    
    public Type getReturnType() {
        return returnType;
    }
    
    public List<Type> getParameterTypes() {
        return parameterTypes;
    }
    
    public boolean isCollectionOperation() {
        return isCollectionOperation;
    }
}