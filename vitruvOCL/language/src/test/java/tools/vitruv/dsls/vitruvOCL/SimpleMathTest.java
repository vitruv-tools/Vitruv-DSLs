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
 * Tests for arithmetic and comparison operations.
 * 
 * Tests the complete 3-pass pipeline for mathematical expressions.
 * OCL# semantics: All values are collections. "1+2" results in singleton [3].
 */
public class SimpleMathTest {
    
    // ==================== Arithmetic Operations ====================
    
    @Test
    public void testOnePlusTwo() {
        String input = "1+2";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(3, ((OCLElement.IntValue) elem).value(), "1+2 should equal 3");
    }
    
    @Test
    public void testFiveMinusThree() {
        String input = "5-3";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(2, ((OCLElement.IntValue) elem).value(), "5-3 should equal 2");
    }
    
    @Test
    public void testMultiplication() {
        String input = "4*5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(20, ((OCLElement.IntValue) elem).value(), "4*5 should equal 20");
    }
    
    @Test
    public void testDivision() {
        String input = "20/4";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(5, ((OCLElement.IntValue) elem).value(), "20/4 should equal 5");
    }
    
    @Test
    public void testChainedOperations() {
        String input = "10+20-5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(25, ((OCLElement.IntValue) elem).value(), "10+20-5 should equal 25");
    }
    
    @Test
    public void testOperatorPrecedence() {
        String input = "2+3*4";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(14, ((OCLElement.IntValue) elem).value(), "2+3*4 should equal 14 (precedence)");
    }
    
    @Test
    public void testUnaryMinus() {
        String input = "-5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(-5, ((OCLElement.IntValue) elem).value(), "-5 should equal -5");
    }
    
    @Test
    public void testUnaryMinusInExpression() {
        String input = "10 + -5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(5, ((OCLElement.IntValue) elem).value(), "10 + -5 should equal 5");
    }
    
    // ==================== Comparison Operations ====================
    
    @Test
    public void testLessThan() {
        String input = "3 < 5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "3 < 5 should be true");
    }
    
    @Test
    public void testLessThanFalse() {
        String input = "5 < 3";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "5 < 3 should be false");
    }
    
    @Test
    public void testLessThanOrEqual() {
        String input = "3 <= 3";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "3 <= 3 should be true");
    }
    
    @Test
    public void testGreaterThan() {
        String input = "10 > 5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "10 > 5 should be true");
    }
    
    @Test
    public void testGreaterThanOrEqual() {
        String input = "5 >= 5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "5 >= 5 should be true");
    }
    
    @Test
    public void testEquality() {
        String input = "5 == 5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "5 == 5 should be true");
    }
    
    @Test
    public void testInequality() {
        String input = "5 != 3";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "5 != 3 should be true");
    }
    
    @Test
    public void testInequalityFalse() {
        String input = "5 != 5";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "5 != 5 should be false");
    }
    
    // ==================== Helper Methods ====================
    
    private Value compile(String input) {
        ParseTree tree = parse(input);
        SymbolTable symbolTable = new SymbolTableImpl();
        VSUMWrapper vsumWrapper = null;
        
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, vsumWrapper);
        typeChecker.visit(tree);
        
        if (typeChecker.hasErrors()) {
            fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
        }
        
        EvaluationVisitor evaluator = new EvaluationVisitor(
            symbolTable, 
            vsumWrapper, 
            typeChecker.getNodeTypes()
        );
        Value result = evaluator.visit(tree);
        
        if (evaluator.hasErrors()) {
            fail("Evaluation failed: " + evaluator.getErrorCollector().getErrors());
        }
        
        return result;
    }
    
    private ParseTree parse(String input) {
        VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        VitruvOCLParser parser = new VitruvOCLParser(tokens);
        return parser.infixedExpCS();
    }
}