package tools.vitruv.dsls.vitruvOCL.symboltable;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLBaseListener;
import tools.vitruv.dsls.vitruvOCL.common.VSUMWrapper;

/**
 * builds the symbol table by tree walking
 * 
 * @see SymbolTable the symbol table created during the first pass
 * @see VitruvOCLBaseListener ANTLR4-genberated Listener 
 */
public class SymbolTableBuilder extends VitruvOCLBaseListener {
    
    private final VSUMWrapper vsumWrapper;
    private SymbolTable symbolTable;
    
    public SymbolTableBuilder(VSUMWrapper vsumWrapper) {
        this.vsumWrapper = vsumWrapper;
    }
    

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    // TODO
}