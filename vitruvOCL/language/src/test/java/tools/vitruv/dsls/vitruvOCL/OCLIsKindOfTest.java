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

import java.util.List;

/**
 * Tests for oclIsKindOf operation.
 * 
 * oclIsKindOf(TypeName): Collection(T) → Collection(Boolean)
 * Tests type checking for primitive types: Integer, String, Boolean
 */
public class OCLIsKindOfTest {
    
    // ==================== Integer Type Checking ====================
    
    @Test
    public void testIntegerIsKindOfInteger() {
        String input = "Set{5}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value());
    }
    
    @Test
    public void testIntegerIsKindOfString() {
        String input = "Set{5}.oclIsKindOf(String)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value());
    }
    
    @Test
    public void testIntegerIsKindOfBoolean() {
        String input = "Set{5}.oclIsKindOf(Boolean)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value());
    }
    
    // ==================== String Type Checking ====================
    
    @Test
    public void testStringIsKindOfString() {
        String input = "Set{\"hello\"}.oclIsKindOf(String)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value());
    }
    
    @Test
    public void testStringIsKindOfInteger() {
        String input = "Set{\"hello\"}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value());
    }
    
    @Test
    public void testStringIsKindOfBoolean() {
        String input = "Set{\"hello\"}.oclIsKindOf(Boolean)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value());
    }
    
    // ==================== Boolean Type Checking ====================
    
    @Test
    public void testBooleanIsKindOfBoolean() {
        String input = "Set{true}.oclIsKindOf(Boolean)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertTrue(((OCLElement.BoolValue) elem).value());
    }
    
    @Test
    public void testBooleanIsKindOfInteger() {
        String input = "Set{true}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value());
    }
    
    @Test
    public void testBooleanIsKindOfString() {
        String input = "Set{false}.oclIsKindOf(String)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value());
    }
    
    // ==================== Multiple Elements ====================
    
    @Test
    public void testMultipleIntegersIsKindOfInteger() {
        String input = "Set{1, 2, 3}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(3, result.size());
        for (OCLElement elem : result.getElements()) {
            assertTrue(((OCLElement.BoolValue) elem).value());
        }
    }
    
    @Test
    public void testMultipleIntegersIsKindOfString() {
        String input = "Set{1, 2, 3}.oclIsKindOf(String)";
        Value result = compile(input);
        
        assertEquals(3, result.size());
        for (OCLElement elem : result.getElements()) {
            assertFalse(((OCLElement.BoolValue) elem).value());
        }
    }
    
    @Test
    public void testMultipleStringsIsKindOfString() {
        String input = "Set{\"a\", \"b\", \"c\"}.oclIsKindOf(String)";
        Value result = compile(input);
        
        assertEquals(3, result.size());
        for (OCLElement elem : result.getElements()) {
            assertTrue(((OCLElement.BoolValue) elem).value());
        }
    }
    
    @Test
    public void testMultipleBooleansIsKindOfBoolean() {
        String input = "Set{true, false, true}.oclIsKindOf(Boolean)";
        Value result = compile(input);
        
        assertEquals(2, result.size()); // Set removes duplicates: {true, false}
        for (OCLElement elem : result.getElements()) {
            assertTrue(((OCLElement.BoolValue) elem).value());
        }
    }
    
    // ==================== Empty Collection ====================
    
    @Test
    public void testEmptyCollectionIsKindOf() {
        String input = "Set{}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(0, result.size());
    }
    
    // ==================== Sequence Preservation ====================
    
    @Test
    public void testSequencePreservesOrder() {
        String input = "Sequence{1, 2, 3}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(3, result.size());
        List<OCLElement> elements = result.getElements();
        
        // All should be true
        for (OCLElement elem : elements) {
            assertTrue(((OCLElement.BoolValue) elem).value());
        }
    }
    
    // ==================== Type Checking ====================
    
    @Test
    public void testTypeCheckReturnsBoolean() {
        String input = "Set{5}.oclIsKindOf(Integer)";
        ParseTree tree = parse(input);
        
        SymbolTable symbolTable = new SymbolTableImpl();
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, null);
        Type resultType = typeChecker.visit(tree);
        
        assertFalse(typeChecker.hasErrors());
        assertTrue(resultType.isCollection());
        assertEquals(Type.BOOLEAN, resultType.getElementType());
    }
    
    @Test
    public void testTypeCheckPreservesCollectionKind() {
        String input = "Sequence{1, 2}.oclIsKindOf(Integer)";
        ParseTree tree = parse(input);
        
        SymbolTable symbolTable = new SymbolTableImpl();
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, null);
        Type resultType = typeChecker.visit(tree);
        
        assertFalse(typeChecker.hasErrors());
        assertTrue(resultType.isCollection());
        assertTrue(resultType.isOrdered());
        assertEquals(Type.BOOLEAN, resultType.getElementType());
    }
    
    // ==================== Unknown Types ====================
    
    @Test
    public void testUnknownTypeReturnsFalse() {
        String input = "Set{5}.oclIsKindOf(MyCustomClass)";
        Value result = compile(input);
        
        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertFalse(((OCLElement.BoolValue) elem).value());
    }

    // ==================== Mixed Type Collections ====================

    @Test
    public void testMixedTypesInCollection() {
        String input = "Sequence{1, \"hello\", true}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(3, result.size());
        List<OCLElement> elements = result.getElements();
        
        // First element (1) is Integer → true
        assertTrue(((OCLElement.BoolValue) elements.get(0)).value());
        
        // Second element ("hello") is NOT Integer → false
        assertFalse(((OCLElement.BoolValue) elements.get(1)).value());
        
        // Third element (true) is NOT Integer → false
        assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
    }

    @Test
    public void testMixedTypesCheckingForString() {
        String input = "Sequence{1, \"hello\", true, \"world\"}.oclIsKindOf(String)";
        Value result = compile(input);
        
        assertEquals(4, result.size());
        List<OCLElement> elements = result.getElements();
        
        // First element (1) is NOT String → false
        assertFalse(((OCLElement.BoolValue) elements.get(0)).value());
        
        // Second element ("hello") is String → true
        assertTrue(((OCLElement.BoolValue) elements.get(1)).value());
        
        // Third element (true) is NOT String → false
        assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
        
        // Fourth element ("world") is String → true
        assertTrue(((OCLElement.BoolValue) elements.get(3)).value());
    }

    @Test
    public void testAllDifferentTypesCheckBoolean() {
        String input = "Set{1, \"test\", true}.oclIsKindOf(Boolean)";
        Value result = compile(input);
        
        assertEquals(3, result.size());
        
        // Exactly one element should be true (the boolean value)
        int trueCount = 0;
        for (OCLElement elem : result.getElements()) {
            if (((OCLElement.BoolValue) elem).value()) {
                trueCount++;
            }
        }
        
        assertEquals(1, trueCount, "Exactly one element should be Boolean");
    }

    @Test
    public void testEmptyResultFromMixedCollection() {
        String input = "Set{\"hello\", \"world\", \"test\"}.oclIsKindOf(Integer)";
        Value result = compile(input);
        
        assertEquals(3, result.size());
        
        // All elements should be false
        for (OCLElement elem : result.getElements()) {
            assertFalse(((OCLElement.BoolValue) elem).value(),
                "No string should be identified as Integer");
        }
    }


    @Test
    public void testNestedCollectionsWithMixedTypes() {
        String input = "Sequence{Set{1, 2}, Set{\"a\", \"b\"}, Set{true, false}}.flatten().oclIsKindOf(Integer)";
        Value result = compile(input);
        
        // After flatten: {1, 2, "a", "b", true, false}
        assertEquals(6, result.size());
        
        List<OCLElement> elements = result.getElements();
        
        // First two should be true (integers)
        assertTrue(((OCLElement.BoolValue) elements.get(0)).value());
        assertTrue(((OCLElement.BoolValue) elements.get(1)).value());
        
        // Next two should be false (strings)
        assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
        assertFalse(((OCLElement.BoolValue) elements.get(3)).value());
        
        // Last two should be false (booleans)
        assertFalse(((OCLElement.BoolValue) elements.get(4)).value());
        assertFalse(((OCLElement.BoolValue) elements.get(5)).value());
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