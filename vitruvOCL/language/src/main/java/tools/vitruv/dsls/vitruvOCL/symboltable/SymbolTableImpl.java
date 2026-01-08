package tools.vitruv.dsls.vitruvOCL.symboltable;

/**
 * Implementation for SymbolTable
 * 
 * starts with GLobalScope as currentScope
 * 
 * @see SymbolTable interface with all operations
 * @see Scope single scope
 */
public class SymbolTableImpl implements SymbolTable {
    
    private final GlobalScope globalScope;
    private Scope currentScope;
    
    /**
     * constructor
     */
    public SymbolTableImpl() {
        this.globalScope = new GlobalScope();
        this.currentScope = globalScope;
    }
    
    @Override
    public void enterScope(Scope newScope) {
        currentScope = newScope;
    }
    
    @Override
    public void exitScope() {
        if (currentScope.getEnclosingScope() != null) {
            currentScope = currentScope.getEnclosingScope();
        }
    }
    
    @Override
    public void define(Symbol symbol) {
        currentScope.define(symbol);
    }
    
    @Override
    public Symbol resolve(String name) {
        return currentScope.resolve(name);
    }
    
    @Override
    public Scope getCurrentScope() {
        return currentScope;
    }
    
    @Override
    public Scope getGlobalScope() {
        return globalScope;
    }
}