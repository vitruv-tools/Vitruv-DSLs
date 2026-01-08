package tools.vitruv.dsls.vitruvOCL.symboltable;

import java.util.HashMap;
import java.util.Map;

/**
 * local scope for nested declations
 * @see Scope interface for Scope-Operationen
 */
public class LocalScope implements Scope {
    
    private final Scope parent;
    private final Map<String, Symbol> symbols = new HashMap<>();
    
    public LocalScope(Scope parent) {
        this.parent = parent;
    }
    
    @Override
    public void define(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }
    
    @Override
    public Symbol resolve(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return null;
    }
    
    @Override
    public Scope getEnclosingScope() {
        return parent;
    }
    
}




