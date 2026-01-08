package tools.vitruv.dsls.vitruvOCL.symboltable;

/**
 * base Interface for single Scope 
 */
public interface Scope {
    

    void define(Symbol symbol);
    
    Symbol resolve(String name);
    
    Scope getEnclosingScope();
}