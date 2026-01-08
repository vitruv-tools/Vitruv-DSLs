package tools.vitruv.dsls.vitruvOCL.symboltable;

import java.util.HashMap;
import java.util.Map;

/**
 * global scope - contains built-in VitrucOCL types and standard libary operations
>
 * 
 * @see Scope inertface for Scope-Operationen
 */
public class GlobalScope implements Scope {
    
    private final Map<String, Symbol> symbols = new HashMap<>();
    
    public GlobalScope() {
        // TODO: initializeBuiltInTypes();
        // TODO: initializeStandardLibrary();
    }
    
    @Override
    public void define(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }
    
    @Override
    public Symbol resolve(String name) {
        return symbols.get(name);
    }
    
    @Override
    public Scope getEnclosingScope() {
        return null;
    }
    
}