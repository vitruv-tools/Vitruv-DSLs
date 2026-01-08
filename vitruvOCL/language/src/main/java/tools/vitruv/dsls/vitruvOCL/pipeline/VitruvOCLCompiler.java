package tools.vitruv.dsls.vitruvOCL.pipeline;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.common.VSUMWrapper;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * 
 * coordinates 3 pass pipeline, namely symbol table → type checking → evaluation.
 * 
 * 
 * error handling: all errors are collected via the ErrorCollector
 * 
 * goal is separation of concerns
 * - parsing: ANTLR4 generated lexer and parser
 * - symbol resolution: SymbolTableBuilder
 * - type validation: TypeCheckVisitor
 * - execution: EvaluationVisitor
 * 
 * @see SymbolTableBuilder Pass 1
 * @see TypeCheckVisitor Pass 2
 * @see EvaluationVisitor Pass 3
 */
public class VitruvOCLCompiler {
    
    private final VSUMWrapper vsumWrapper;
    private final ErrorCollector allErrors = new ErrorCollector();
    
    public VitruvOCLCompiler(VSUMWrapper vsumWrapper) {
        this.vsumWrapper = vsumWrapper;
    }
    

    public Value compile(String source) {
        // TODO
        return null;
    }
    
    public boolean hasErrors() {
        return allErrors.hasErrors();
    }
    
    public ErrorCollector getErrors() {
        return allErrors;
    }
}