package tools.vitruv.dsls.vitruvOCL;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import tools.vitruv.dsls.vitruvOCL.common.VSUMWrapper;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test für einfache mathematische Operationen.
 * 
 * <p>Testet die komplette 3-Pass-Pipeline für simple Expressions wie "1+2".</p>
 * 
 * <p>OCL# Semantik: Alle Werte sind Collections. "1+2" ergibt ein Singleton [3].</p>
 */
public class SimpleMathTest {
    
    /**
     * Test für "1+2" - kompletter Durchlauf durch alle 3 Passes.
     * OCL#: Result ist ein Singleton [3].
     */
    @Test
    public void testOnePlusTwo() {
        // Given: OCL Expression "1+2"
        String input = "1+2";
        
        // When: Parse, Type Check, Evaluate
        Value result = compile(input);
        
        // Then: Result ist Singleton [3]
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Result should be a singleton");
        
        // Extract the single element
        OCLElement elem = result.getElements().get(0);
        assertTrue(elem instanceof OCLElement.IntValue, "Element should be IntValue");
        assertEquals(3, ((OCLElement.IntValue) elem).value(), "1+2 should equal 3");
        
        assertEquals(Type.INTEGER, result.getRuntimeType(), "Result type should be Integer");
    }
    
    /**
     * Test für "5-3" - Subtraktion.
     */
    @Test
    public void testFiveMinusThree() {
        String input = "5-3";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(2, ((OCLElement.IntValue) elem).value(), "5-3 should equal 2");
        assertEquals(Type.INTEGER, result.getRuntimeType());
    }
    
    /**
     * Test für komplexere Expression "10+20-5".
     */
    @Test
    public void testChainedOperations() {
        String input = "10+20-5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(25, ((OCLElement.IntValue) elem).value(), "10+20-5 should equal 25");
        assertEquals(Type.INTEGER, result.getRuntimeType());
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Kompiliert und evaluiert OCL Expression durch alle 3 Passes.
     * 
     * @param input OCL Source Code
     * @return Evaluation Result
     */
    private Value compile(String input) {
        // Parse
        ParseTree tree = parse(input);
        
        // Pass 1: Symbol Table (für "1+2" leer)
        SymbolTable symbolTable = new SymbolTableImpl();
        
        // Pass 2: Type Checking
        VSUMWrapper vsumWrapper = null; // Brauchen wir für "1+2" nicht
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, vsumWrapper);
        typeChecker.visit(tree);
        
        // Prüfe auf Type Errors
        if (typeChecker.hasErrors()) {
            fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
        }
        
        // Pass 3: Evaluation
        EvaluationVisitor evaluator = new EvaluationVisitor(
            symbolTable, 
            vsumWrapper, 
            typeChecker.getNodeTypes()
        );
        Value result = evaluator.visit(tree);
        
        // Prüfe auf Evaluation Errors
        if (evaluator.hasErrors()) {
            fail("Evaluation failed: " + evaluator.getErrorCollector().getErrors());
        }
        
        return result;
    }
    
    /**
     * Parst OCL Input zu Parse Tree.
     * 
     * @param input OCL Source Code
     * @return Parse Tree
     */
    private ParseTree parse(String input) {
        // Lexer
        VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // Parser
        VitruvOCLParser parser = new VitruvOCLParser(tokens);
        
        // Parse als Expression (start rule muss angepasst werden je nach Grammatik)
        // Für "1+2" starten wir mit infixedExpCS
        return parser.infixedExpCS();
    }
}