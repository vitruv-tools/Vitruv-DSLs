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
 * Tests for String operations in OCL#.
 *
 * Covered operations:
 * concat, substring, toUpper, toLower, indexOf, equalsIgnoreCase
 *
 * OCL# semantics:
 * - All values are collections
 * - A String literal evaluates to a singleton collection
 * - Invalid string operations yield an empty collection
 */
public class StringOperationsTest {

    // ==================== String Literals ====================

    @Test
    public void testStringLiteral() {
        Value result = compile("\"Hello\"");

        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertEquals("Hello", ((OCLElement.StringValue) elem).value());
    }

    // ==================== CONCAT ====================

    @Test
    public void testConcat() {
        assertString("\"Hello\".concat(\" World\")", "Hello World");
        assertString("\"OCL\".concat(\"#\")", "OCL#");
        assertString("\"\".concat(\"empty\")", "empty");
        assertString("\"Test\".concat(\"\")", "Test");
    }

    // ==================== SUBSTRING ====================

    @Test
    public void testSubstring() {
     
            assertString("\"Hello\".substring(1, 3)", "Hel");
       
    }

    @Test
    public void testComplexStringExpressions() {
        // Fix 1: substring(1, 2) gibt "He" zurück, nicht "H"
        assertString(
            "\"Hello\".substring(1, 2).concat(\"i\").toUpper()",
            "HEI"  // ← Korrigiert von "HI" zu "HEI"
        );
        
        // Fix 2: Wenn du wirklich "HI" willst, nimm nur Position 1:
        assertString(
            "\"Hello\".substring(1, 1).concat(\"i\").toUpper()",
            "HI"
        );
        
        // Andere Tests...
        assertString(
            "if \"test\".toUpper() == \"TEST\" then \"OK\" else \"FAIL\" endif",
            "OK"
        );
        
        assertBool(
            "if \"Hello World\".indexOf(\"World\") > 0 then true else false endif",
            true
        );
    }


    @Test
    public void testSubstringInvalidIndices() {
        assertEmpty("\"Hi\".substring(5, 10)");
        assertEmpty("\"Test\".substring(0, 2)");
        assertEmpty("\"Test\".substring(3, 2)");
        assertEmpty("\"Hi\".substring(1, 10)");
    }

    // ==================== TO UPPER ====================

    @Test
    public void testToUpper() {
        assertString("\"hello\".toUpper()", "HELLO");
        assertString("\"OCL\".toUpper()", "OCL");
        assertString("\"MiXeD\".toUpper()", "MIXED");
        assertString("\"\"", "");
        assertString("\"123abc\".toUpper()", "123ABC");
    }

    // ==================== TO LOWER ====================

    @Test
    public void testToLower() {
        assertString("\"HELLO\".toLower()", "hello");
        assertString("\"ocl\".toLower()", "ocl");
        assertString("\"MiXeD\".toLower()", "mixed");
        assertString("\"\"", "");
        assertString("\"ABC123\".toLower()", "abc123");
    }

    // ==================== INDEX OF ====================

    @Test
    public void testIndexOf() {
        // OCL: 1-based index, 0 if not found
        assertInt("\"Hello World\".indexOf(\"World\")", 7);
        assertInt("\"Hello\".indexOf(\"H\")", 1);
        assertInt("\"Hello\".indexOf(\"e\")", 2);
        assertInt("\"Hello\".indexOf(\"o\")", 5);
        assertInt("\"Hello\".indexOf(\"x\")", 0);
        assertInt("\"Test Test\".indexOf(\"Test\")", 1);
        assertInt("\"\".indexOf(\"x\")", 0);
    }

    // ==================== EQUALS IGNORE CASE ====================

    @Test
    public void testEqualsIgnoreCase() {
        assertBool("\"hello\".equalsIgnoreCase(\"HELLO\")", true);
        assertBool("\"OCL\".equalsIgnoreCase(\"ocl\")", true);
        assertBool("\"test\".equalsIgnoreCase(\"different\")", false);
        assertBool("\"Test123\".equalsIgnoreCase(\"test123\")", true);
        assertBool("\"\".equalsIgnoreCase(\"\")", true);
    }

    // ==================== STRING CHAINING ====================

    @Test
    public void testStringChaining() {
        assertString("\"hello\".toUpper().concat(\" WORLD\")", "HELLO WORLD");
        assertString("\"  TEST  \".toUpper().substring(3, 6)", "TEST");
        assertString("\"OCL\".concat(\"#\").toLower()", "ocl#");
        assertString("\"Hello\".concat(\" \").concat(\"World\")", "Hello World");
    }

    // ==================== STRING COMPARISON ====================

    @Test
    public void testStringComparison() {
        assertBool("\"abc\" == \"abc\"", true);
        assertBool("\"abc\" != \"xyz\"", true);
        assertBool("\"abc\" < \"xyz\"", true);
    }

    // ==================== COMPLEX EXPRESSIONS ====================

    // ==================== Helper Assertions ====================

    private void assertString(String input, String expected) {
        Value result = compile(input);

        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertEquals(expected, ((OCLElement.StringValue) elem).value());
    }

    private void assertBool(String input, boolean expected) {
        Value result = compile(input);

        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertEquals(expected, ((OCLElement.BoolValue) elem).value());
    }

    private void assertInt(String input, int expected) {
        Value result = compile(input);

        assertEquals(1, result.size());
        OCLElement elem = result.getElements().get(0);
        assertEquals(expected, ((OCLElement.IntValue) elem).value());
    }

    private void assertEmpty(String input) {
        Value result = compile(input);
        assertEquals(0, result.size());
    }

    // ==================== Compile / Parse ====================
private Value compile(String input) {
    // Create lexer and token stream
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    
    // Parse
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();
    
    // After parsing, reset to start
    tokens.seek(0);
    
    SymbolTable symbolTable = new SymbolTableImpl();
    VSUMWrapper vsumWrapper = null;
    
    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, vsumWrapper);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);
    
    if (!typeChecker.getErrorCollector().getErrors().isEmpty()) {
        System.out.println("TYPE ERRORS:");
        typeChecker.getErrorCollector().getErrors().forEach(System.out::println);
        fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }
    
    // Pass 3: Evaluation  
    EvaluationVisitor evaluator = new EvaluationVisitor(
        symbolTable, 
        vsumWrapper, 
        typeChecker.getNodeTypes()
    );
    evaluator.setTokenStream(tokens);
    
    Value result = evaluator.visit(tree);
    
    if (!evaluator.getErrorCollector().getErrors().isEmpty()) {
        System.out.println("EVALUATION ERRORS:");
        evaluator.getErrorCollector().getErrors().forEach(System.out::println);
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
