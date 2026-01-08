package tools.vitruv.dsls.vitruvOCL.symboltable;

import tools.vitruv.dsls.vitruvOCL.typechecker.Type;

/**
 * symbol for types:metaclasses, primitive Types, collection Types.
 * 
 * @see Symbol as parent class for all symbols
 */
public class TypeSymbol extends Symbol {
    
    private final String qualifiedName;
    
    public TypeSymbol(String name, Type type, Scope definingScope, String qualifiedName) {
        super(name, type, definingScope);
        this.qualifiedName = qualifiedName;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }
}