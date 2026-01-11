package tools.vitruv.dsls.vitruvOCL;

import org.antlr.v4.runtime.CharStream;
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
 * Tests for OCL# Collection Operations.
 * 
 * tests the complete pipeline for Collection Literals and Operations:
 * - Set{1,2,3}
 * - size(), includes(), union(), etc.
 * - Ranges: Set{1..10}
 */
public class CollectionTest {
    
    // ==================== Collection Literals ====================
    
    @Test
    public void testSetLiteral() {
        String input = "Set{1,2,3}";
        Value result = compile(input);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.size(), "Set{1,2,3} should have 3 elements");
        
        // Check that all elements are present
        assertTrue(result.includes(new OCLElement.IntValue(1)), "Set should include 1");
        assertTrue(result.includes(new OCLElement.IntValue(2)), "Set should include 2");
        assertTrue(result.includes(new OCLElement.IntValue(3)), "Set should include 3");
    }
    
    @Test
    public void testSetRemovesDuplicates() {
        String input = "Set{1,2,2,3}";
        Value result = compile(input);
        
        assertEquals(3, result.size(), "Set{1,2,2,3} should have only 3 unique elements");
    }
    
    @Test
    public void testEmptySet() {
        String input = "Set{}";
        Value result = compile(input);
        
        assertTrue(result.isEmpty(), "Set{} should be empty");
        assertEquals(0, result.size(), "Empty set size should be 0");
    }
    
    @Test
    public void testSequenceLiteral() {
        String input = "Sequence{1,2,3}";
        Value result = compile(input);
        
        assertEquals(3, result.size(), "Sequence{1,2,3} should have 3 elements");
    }
    
    @Test
    public void testSequenceKeepsDuplicates() {
        String input = "Sequence{1,2,2,3}";
        Value result = compile(input);
        
        assertEquals(4, result.size(), "Sequence{1,2,2,3} should keep all 4 elements");
    }
    
    // ==================== Ranges ====================
    
    @Test
    public void testRange() {
        String input = "Set{1..5}";
        Value result = compile(input);
        
        assertEquals(5, result.size(), "Set{1..5} should have 5 elements");
        assertTrue(result.includes(new OCLElement.IntValue(1)));
        assertTrue(result.includes(new OCLElement.IntValue(5)));
    }
    
    @Test
    public void testDescendingRange() {
        String input = "Sequence{5..1}";
        Value result = compile(input);
        
        assertEquals(5, result.size(), "Sequence{5..1} should have 5 elements");
        
        // Check order: should be 5, 4, 3, 2, 1
        OCLElement first = result.getElements().get(0);
        assertEquals(5, ((OCLElement.IntValue) first).value(), "First element should be 5");
        
        OCLElement last = result.getElements().get(4);
        assertEquals(1, ((OCLElement.IntValue) last).value(), "Last element should be 1");
    }
    
    // ==================== Query Operations ====================
    
    @Test
    public void testSize() {
        String input = "Set{1,2,3}.size()";
        Value result = compile(input);


        assertEquals(1, result.size(), "size() should return a singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(3, ((OCLElement.IntValue) elem).value(), "Set{1,2,3}.size() should be 3");
    }
    
    @Test
    public void testIsEmpty() {
        String input = "Set{}.isEmpty()";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "isEmpty() should return a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "Set{}.isEmpty() should be true");
    }
    
    @Test
    public void testNotEmpty() {
        String input = "Set{1,2,3}.notEmpty()";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "notEmpty() should return a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.notEmpty() should be true");
    }
    
    @Test
    public void testIncludes() {
        String input = "Set{1,2,3}.includes(2)";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "includes() should return a singleton");
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.includes(2) should be true");
    }
    
    @Test
    public void testIncludesFalse() {
        String input = "Set{1,2,3}.includes(5)";
        Value result = compile(input);
        
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.includes(5) should be false");
    }
    
    @Test
    public void testExcludes() {
        String input = "Set{1,2,3}.excludes(5)";
        Value result = compile(input);
        
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value(), "Set{1,2,3}.excludes(5) should be true");
    }
    
    // ==================== Set Operations ====================
    
    @Test
    public void testUnion() {
        String input = "Set{1,2}.union(Set{3,4})";
        Value result = compile(input);
        
        assertEquals(4, result.size(), "Union should have 4 elements");
        assertTrue(result.includes(new OCLElement.IntValue(1)));
        assertTrue(result.includes(new OCLElement.IntValue(2)));
        assertTrue(result.includes(new OCLElement.IntValue(3)));
        assertTrue(result.includes(new OCLElement.IntValue(4)));
    }
    
    @Test
    public void testUnionWithDuplicates() {
        String input = "Set{1,2}.union(Set{2,3})";
        Value result = compile(input);
        
        assertEquals(3, result.size(), "Union should remove duplicates: {1,2,3}");
        assertTrue(result.includes(new OCLElement.IntValue(1)));
        assertTrue(result.includes(new OCLElement.IntValue(2)));
        assertTrue(result.includes(new OCLElement.IntValue(3)));
    }
    
    
    @Test
    public void testIncluding() {
        String input = "Set{1,2}.including(3)";
        Value result = compile(input);
        
        assertEquals(3, result.size(), "including(3) should add element");
        assertTrue(result.includes(new OCLElement.IntValue(3)));
    }
    
    @Test
    public void testExcluding() {
        String input = "Set{1,2,3}.excluding(2)";
        Value result = compile(input);
        
        assertEquals(2, result.size(), "excluding(2) should remove element");
        assertFalse(result.includes(new OCLElement.IntValue(2)));
    }
    
    // ==================== Ordered Operations ====================
    
    @Test
    public void testFirst() {
        String input = "Sequence{1,2,3}.first()";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "first() should return singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(1, ((OCLElement.IntValue) elem).value(), "first() should be 1");
    }
    
    @Test
    public void testLast() {
        String input = "Sequence{1,2,3}.last()";
        Value result = compile(input);
        
        assertEquals(1, result.size(), "last() should return singleton");
        OCLElement elem = result.getElements().get(0);
        assertEquals(3, ((OCLElement.IntValue) elem).value(), "last() should be 3");
    }
   
    
    @Test
    public void testReverse() {
        String input = "Sequence{1,2,3}.reverse()";
        Value result = compile(input);
        
        assertEquals(3, result.size(), "reverse() should have 3 elements");
        
        // Check reversed order: 3, 2, 1
        assertEquals(3, ((OCLElement.IntValue) result.getElements().get(0)).value());
        assertEquals(2, ((OCLElement.IntValue) result.getElements().get(1)).value());
        assertEquals(1, ((OCLElement.IntValue) result.getElements().get(2)).value());
    }
    

    
    // ==================== Chained Operations ====================
    
    @Test
    public void testChainedOperations() {
        String input = "Set{1,2,3}.including(4).excluding(2).size()";
        Value result = compile(input);
        
        OCLElement elem = result.getElements().get(0);
        assertEquals(3, ((OCLElement.IntValue) elem).value(), 
            "Set{1,2,3}.including(4).excluding(2).size() should be 3");
    }
    
    
    // ==================== Edge Cases ====================
    
    @Test
    public void testEmptySetSize() {
        String input = "Set{}.size()";
        Value result = compile(input);
        
        OCLElement elem = result.getElements().get(0);
        assertEquals(0, ((OCLElement.IntValue) elem).value(), "Empty set size should be 0");
    }
    
    @Test
    public void testSingletonSize() {
        String input = "Set{42}.size()";
        Value result = compile(input);
        
        OCLElement elem = result.getElements().get(0);
        assertEquals(1, ((OCLElement.IntValue) elem).value(), "Singleton set size should be 1");
    }
    
    @Test
    public void testLargeRange() {
        String input = "Set{1..100}.size()";
        Value result = compile(input);
        
        OCLElement elem = result.getElements().get(0);
        assertEquals(100, ((OCLElement.IntValue) elem).value(), "Set{1..100}.size() should be 100");
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * compiles and evaluates OCL expressions durch alle 3 Passes.
     * 
     * @param input OCL Source Code
     * @return Evaluation Result
     */
    private Value compile(String input) {
        // Parse
        ParseTree tree = parse(input);
        // Pass 1: Symbol Table (für Collections leer)
        SymbolTable symbolTable = new SymbolTableImpl();
        // Pass 2: Type Checking
        VSUMWrapper vsumWrapper = null;
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
        
        // Parse als Expression
        // Für Collections starten wir mit prefixedExpCS (deckt collections ab)
        return parser.prefixedExpCS();
    }
}