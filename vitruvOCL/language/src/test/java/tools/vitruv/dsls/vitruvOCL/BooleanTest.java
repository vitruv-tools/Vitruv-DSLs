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
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for boolean operations and logic.
 * 
 * Tests: and, or, xor, not, implies
 * OCL# semantics: All values are collections. "true and false" results in singleton [false].
 */
public class BooleanTest {
    
    // ==================== Boolean Literals ====================
    
    @Test
    public void testTrueLiteral() {
        String input = "true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "true should evaluate to true");
    }
    
    @Test
    public void testFalseLiteral() {
        String input = "false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "false should evaluate to false");
    }
    
    // ==================== Unary NOT ====================
    
    @Test
    public void testNotTrue() {
        String input = "not true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "not true should be false");
    }
    
    @Test
    public void testNotFalse() {
        String input = "not false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "not false should be true");
    }
    
    @Test
    public void testDoubleNegation() {
        String input = "not not true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "not not true should be true");
    }
    
    // ==================== AND ====================
    
    @Test
    public void testTrueAndTrue() {
        String input = "true and true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "true and true should be true");
    }
    
    @Test
    public void testTrueAndFalse() {
        String input = "true and false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "true and false should be false");
    }
    
    @Test
    public void testFalseAndFalse() {
        String input = "false and false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "false and false should be false");
    }
    
    // ==================== OR ====================
    
    @Test
    public void testTrueOrFalse() {
        String input = "true or false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "true or false should be true");
    }
    
    @Test
    public void testFalseOrFalse() {
        String input = "false or false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "false or false should be false");
    }
    
    @Test
    public void testTrueOrTrue() {
        String input = "true or true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "true or true should be true");
    }
    
    // ==================== XOR ====================
    
    @Test
    public void testTrueXorFalse() {
        String input = "true xor false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "true xor false should be true");
    }
    
    @Test
    public void testTrueXorTrue() {
        String input = "true xor true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "true xor true should be false");
    }
    
    @Test
    public void testFalseXorFalse() {
        String input = "false xor false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "false xor false should be false");
    }
    
    // ==================== IMPLIES ====================
    
    @Test
    public void testTrueImpliesTrue() {
        String input = "true implies true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "true implies true should be true");
    }
    
    @Test
    public void testTrueImpliesFalse() {
        String input = "true implies false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "true implies false should be false");
    }
    
    @Test
    public void testFalseImpliesTrue() {
        String input = "false implies true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "false implies true should be true");
    }
    
    @Test
    public void testFalseImpliesFalse() {
        String input = "false implies false";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "false implies false should be true");
    }
    
    // ==================== Complex Expressions ====================
    
@Test
public void testComplexBooleanExpressionDebug() {
    String input = "true and (false or true)";
    
    ParseTree tree = parse(input);
    SymbolTable symbolTable = new SymbolTableImpl();
    VSUMWrapper vsumWrapper = null;
    
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, vsumWrapper);
    typeChecker.visit(tree);

    
    if (typeChecker.hasErrors()) {
        fail("Type checking failed");
    }
}
    
    @Test
    public void testNotAndOr() {
        String input = "not (true and false) or true";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "not (true and false) or true should be true");
    }
    
    @Test
    public void testComparisonInBoolean() {
        String input = "(5 > 3) and (10 < 20)";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "Result should be a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "(5 > 3) and (10 < 20) should be true");
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