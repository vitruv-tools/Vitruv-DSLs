package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for String operations in VitruvOCL.
 *
 * <p>This test class validates the complete implementation of OCL string manipulation operations,
 * covering standard string methods, concatenation, substring extraction, case transformation,
 * searching, and comparison operations.
 *
 * @see Value Runtime value representation
 * @see OCLElement.StringValue String element wrapper
 * @see EvaluationVisitor Evaluates string operations
 * @see TypeCheckVisitor Type checks string expressions
 */
public class StringOperationsTest {

  // ==================== String Literals ====================

  /**
   * Tests basic string literal evaluation.
   *
   * <p><b>Input:</b> {@code "Hello"}
   *
   * <p><b>Expected:</b> {@code ["Hello"]} (singleton)
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>String literal parsing
   *   <li>Singleton collection wrapping
   *   <li>String value preservation
   * </ul>
   */
  @Test
  public void testStringLiteral() {
    Value result = compile("\"Hello\"");

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertEquals("Hello", ((OCLElement.StringValue) elem).value());
  }

  // ==================== CONCAT ====================

  /**
   * Tests string concatenation operation.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code "Hello".concat(" World")} → {@code ["Hello World"]}
   *   <li>{@code "".concat("empty")} → {@code ["empty"]} (empty string concatenation)
   *   <li>{@code "Test".concat("")} → {@code ["Test"]} (concatenating empty string)
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic concatenation functionality
   *   <li>Concatenation with empty strings
   *   <li>Order preservation (receiver + argument)
   * </ul>
   */
  @Test
  public void testConcat() {
    assertString("\"Hello\".concat(\" World\")", "Hello World");
    assertString("\"OCL\".concat(\"#\")", "OCL#");
    assertString("\"\".concat(\"empty\")", "empty");
    assertString("\"Test\".concat(\"\")", "Test");
  }

  // ==================== SUBSTRING ====================

  /**
   * Tests substring extraction with valid indices.
   *
   * <p><b>Input:</b> {@code "Hello".substring(1, 3)}
   *
   * <p><b>Expected:</b> {@code ["Hel"]} (characters at positions 1, 2, 3)
   *
   * <p><b>Indexing:</b> 1-based, inclusive range [start, end]
   *
   * <p><b>Validates:</b> Basic substring extraction with valid range.
   */
  @Test
  public void testSubstring() {
    assertString("\"Hello\".substring(1, 3)", "Hel");
  }

  /**
   * Tests complex string expressions with method chaining.
   *
   * <p><b>Example 1:</b> {@code "Hello".substring(1, 2).concat("i").toUpper()}
   *
   * <ul>
   *   <li>{@code "Hello".substring(1, 2)} → {@code ["He"]}
   *   <li>{@code .concat("i")} → {@code ["Hei"]}
   *   <li>{@code .toUpper()} → {@code ["HEI"]}
   * </ul>
   *
   * <p><b>Example 2:</b> {@code "Hello".substring(1, 1).concat("i").toUpper()}
   *
   * <ul>
   *   <li>{@code "Hello".substring(1, 1)} → {@code ["H"]} (single character)
   *   <li>{@code .concat("i")} → {@code ["Hi"]}
   *   <li>{@code .toUpper()} → {@code ["HI"]}
   * </ul>
   *
   * <p><b>Integration tests:</b>
   *
   * <ul>
   *   <li>String operations in if-then-else conditions
   *   <li>indexOf results in boolean expressions
   * </ul>
   *
   * <p><b>Validates:</b> Complex operation chaining and integration with conditionals.
   */
  @Test
  public void testComplexStringExpressions() {
    // substring(1, 2) returns "He" (positions 1 and 2)
    assertString("\"Hello\".substring(1, 2).concat(\"i\").toUpper()", "HEI");

    // substring(1, 1) returns only "H" (position 1)
    assertString("\"Hello\".substring(1, 1).concat(\"i\").toUpper()", "HI");

    // String operation in conditional
    assertString("if \"test\".toUpper() == \"TEST\" then \"OK\" else \"FAIL\" endif", "OK");

    // indexOf in boolean expression
    assertBool("if \"Hello World\".indexOf(\"World\") > 0 then true else false endif", true);
  }

  /**
   * Tests substring operation with invalid indices.
   *
   * <p><b>Invalid cases:</b>
   *
   * <ul>
   *   <li>{@code "Hi".substring(5, 10)} → {@code []} (start beyond length)
   *   <li>{@code "Test".substring(0, 2)} → {@code []} (0-based index not valid in OCL)
   *   <li>{@code "Test".substring(3, 2)} → {@code []} (start > end)
   *   <li>{@code "Hi".substring(1, 10)} → {@code []} (end beyond length)
   * </ul>
   *
   * <p><b>Error handling:</b> Invalid indices return empty collection rather than throwing
   * exceptions.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Bounds checking
   *   <li>1-based index validation
   *   <li>Range validation (start ≤ end)
   *   <li>Graceful error handling
   * </ul>
   */
  @Test
  public void testSubstringInvalidIndices() {
    assertEmpty("\"Hi\".substring(5, 10)");
    assertEmpty("\"Test\".substring(0, 2)");
    assertEmpty("\"Test\".substring(3, 2)");
    assertEmpty("\"Hi\".substring(1, 10)");
  }

  // ==================== TO UPPER ====================

  /**
   * Tests uppercase transformation operation.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code "hello".toUpper()} → {@code ["HELLO"]}
   *   <li>{@code "OCL".toUpper()} → {@code ["OCL"]} (already uppercase)
   *   <li>{@code "MiXeD".toUpper()} → {@code ["MIXED"]}
   *   <li>{@code ""} → {@code [""]} (empty string unchanged)
   *   <li>{@code "123abc".toUpper()} → {@code ["123ABC"]} (numbers unchanged, letters uppercase)
   * </ul>
   *
   * <p><b>Behavior:</b> Converts all lowercase letters to uppercase, preserves digits and
   * punctuation.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Lowercase to uppercase conversion
   *   <li>Already uppercase strings unchanged
   *   <li>Mixed case handling
   *   <li>Empty string handling
   *   <li>Alphanumeric string handling
   * </ul>
   */
  @Test
  public void testToUpper() {
    assertString("\"hello\".toUpper()", "HELLO");
    assertString("\"OCL\".toUpper()", "OCL");
    assertString("\"MiXeD\".toUpper()", "MIXED");
    assertString("\"\"", "");
    assertString("\"123abc\".toUpper()", "123ABC");
  }

  // ==================== TO LOWER ====================

  /**
   * Tests lowercase transformation operation.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code "HELLO".toLower()} → {@code ["hello"]}
   *   <li>{@code "ocl".toLower()} → {@code ["ocl"]} (already lowercase)
   *   <li>{@code "MiXeD".toLower()} → {@code ["mixed"]}
   *   <li>{@code ""} → {@code [""]} (empty string unchanged)
   *   <li>{@code "ABC123".toLower()} → {@code ["abc123"]} (numbers unchanged, letters lowercase)
   * </ul>
   *
   * <p><b>Behavior:</b> Converts all uppercase letters to lowercase, preserves digits and
   * punctuation.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Uppercase to lowercase conversion
   *   <li>Already lowercase strings unchanged
   *   <li>Mixed case handling
   *   <li>Empty string handling
   *   <li>Alphanumeric string handling
   * </ul>
   */
  @Test
  public void testToLower() {
    assertString("\"HELLO\".toLower()", "hello");
    assertString("\"ocl\".toLower()", "ocl");
    assertString("\"MiXeD\".toLower()", "mixed");
    assertString("\"\"", "");
    assertString("\"ABC123\".toLower()", "abc123");
  }

  // ==================== INDEX OF ====================

  /**
   * Tests substring search operation (indexOf).
   *
   * <p><b>Index convention:</b> 1-based indexing; returns 0 if not found
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code "Hello World".indexOf("World")} → {@code [7]} (position of 'W')
   *   <li>{@code "Hello".indexOf("H")} → {@code [1]} (first position)
   *   <li>{@code "Hello".indexOf("e")} → {@code [2]}
   *   <li>{@code "Hello".indexOf("o")} → {@code [5]} (last position)
   *   <li>{@code "Hello".indexOf("x")} → {@code [0]} (not found)
   *   <li>{@code "Test Test".indexOf("Test")} → {@code [1]} (first occurrence only)
   *   <li>{@code "".indexOf("x")} → {@code [0]} (not found in empty string)
   * </ul>
   *
   * <p><b>Behavior:</b>
   *
   * <ul>
   *   <li>Returns 1-based index of first occurrence
   *   <li>Returns 0 if substring not found
   *   <li>Only finds first occurrence (not all occurrences)
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Substring position finding
   *   <li>1-based index return
   *   <li>Not found case (returns 0)
   *   <li>First occurrence behavior
   *   <li>Empty string search
   * </ul>
   */
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

  /**
   * Tests case-insensitive string equality comparison.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code "hello".equalsIgnoreCase("HELLO")} → {@code [true]}
   *   <li>{@code "OCL".equalsIgnoreCase("ocl")} → {@code [true]}
   *   <li>{@code "test".equalsIgnoreCase("different")} → {@code [false]}
   *   <li>{@code "Test123".equalsIgnoreCase("test123")} → {@code [true]}
   *   <li>{@code "".equalsIgnoreCase("")} → {@code [true]} (empty strings equal)
   * </ul>
   *
   * <p><b>Behavior:</b> Compares strings ignoring case differences (case-insensitive comparison).
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Case-insensitive equality check
   *   <li>Different case same content returns true
   *   <li>Different content returns false
   *   <li>Alphanumeric string handling
   *   <li>Empty string comparison
   * </ul>
   */
  @Test
  public void testEqualsIgnoreCase() {
    assertBool("\"hello\".equalsIgnoreCase(\"HELLO\")", true);
    assertBool("\"OCL\".equalsIgnoreCase(\"ocl\")", true);
    assertBool("\"test\".equalsIgnoreCase(\"different\")", false);
    assertBool("\"Test123\".equalsIgnoreCase(\"test123\")", true);
    assertBool("\"\".equalsIgnoreCase(\"\")", true);
  }

  // ==================== STRING CHAINING ====================

  /**
   * Tests chaining multiple string operations.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code "hello".toUpper().concat(" WORLD")} → {@code ["HELLO WORLD"]}
   *   <li>{@code " TEST ".toUpper().substring(3, 6)} → {@code ["TEST"]} (trimming via substring)
   *   <li>{@code "OCL".concat("#").toLower()} → {@code ["ocl#"]}
   *   <li>{@code "Hello".concat(" ").concat("World")} → {@code ["Hello World"]} (multiple concats)
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Method chaining works correctly
   *   <li>Operations apply in left-to-right order
   *   <li>Multiple operations can be combined
   *   <li>Complex transformations produce correct results
   * </ul>
   */
  @Test
  public void testStringChaining() {
    assertString("\"hello\".toUpper().concat(\" WORLD\")", "HELLO WORLD");
    assertString("\"  TEST  \".toUpper().substring(3, 6)", "TEST");
    assertString("\"OCL\".concat(\"#\").toLower()", "ocl#");
    assertString("\"Hello\".concat(\" \").concat(\"World\")", "Hello World");
  }

  // ==================== STRING COMPARISON ====================

  /**
   * Tests string comparison operations.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code "abc" == "abc"} → {@code [true]} (equality)
   *   <li>{@code "abc" != "xyz"} → {@code [true]} (inequality)
   *   <li>{@code "abc" < "xyz"} → {@code [true]} (lexicographic ordering)
   * </ul>
   *
   * <p><b>Comparison semantics:</b>
   *
   * <ul>
   *   <li><b>Equality (==):</b> Character-by-character comparison
   *   <li><b>Inequality (!=):</b> Negation of equality
   *   <li><b>Ordering (<, >):</b> Lexicographic (dictionary) order
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>String equality comparison
   *   <li>String inequality comparison
   *   <li>Lexicographic ordering
   * </ul>
   */
  @Test
  public void testStringComparison() {
    assertBool("\"abc\" == \"abc\"", true);
    assertBool("\"abc\" != \"xyz\"", true);
    assertBool("\"abc\" < \"xyz\"", true);
  }

  // ==================== COMPLEX EXPRESSIONS ====================
  // (Covered in testComplexStringExpressions above)

  // ==================== Helper Assertions ====================

  /**
   * Assertion helper for String results.
   *
   * <p>Compiles the input, validates it produces a singleton String value, and asserts the value
   * matches expected.
   *
   * @param input OCL string expression to evaluate
   * @param expected Expected string value
   * @throws AssertionError if result is not singleton, not String, or value doesn't match
   */
  private void assertString(String input, String expected) {
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertEquals(expected, ((OCLElement.StringValue) elem).value());
  }

  /**
   * Assertion helper for Boolean results.
   *
   * <p>Compiles the input, validates it produces a singleton Boolean value, and asserts the value
   * matches expected.
   *
   * @param input OCL expression to evaluate
   * @param expected Expected boolean value
   * @throws AssertionError if result is not singleton, not Boolean, or value doesn't match
   */
  private void assertBool(String input, boolean expected) {
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertEquals(expected, ((OCLElement.BoolValue) elem).value());
  }

  /**
   * Assertion helper for Integer results (used by indexOf tests).
   *
   * <p>Compiles the input, validates it produces a singleton Integer value, and asserts the value
   * matches expected.
   *
   * @param input OCL expression to evaluate
   * @param expected Expected integer value
   * @throws AssertionError if result is not singleton, not Integer, or value doesn't match
   */
  private void assertInt(String input, int expected) {
    Value result = compile(input);

    assertEquals(1, result.size());
    OCLElement elem = result.getElements().get(0);
    assertEquals(expected, ((OCLElement.IntValue) elem).value());
  }

  /**
   * Assertion helper for empty collection results.
   *
   * <p>Validates that the result collection has size 0.
   *
   * @param input OCL expression to evaluate
   * @throws AssertionError if result is not empty
   */
  private void assertEmpty(String input) {
    Value result = compile(input);
    assertEquals(0, result.size());
  }

  // ==================== Compile / Parse ====================

  /**
   * Compiles and evaluates an OCL string expression through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process with special handling for
   * string operations that may require token stream access for keyword detection:
   *
   * <ol>
   *   <li><b>Phase 1 - Parsing:</b> Converts input to parse tree using {@code infixedExpCS} entry
   *       point
   *   <li><b>Phase 2 - Type Checking:</b> Validates string operation types and signatures
   *   <li><b>Phase 3 - Evaluation:</b> Executes string operations and produces results
   * </ol>
   *
   * <p><b>Token stream handling:</b> The token stream is filled during parsing, reset to start, and
   * set for both type checker and evaluator to enable proper keyword detection in complex
   * expressions (especially conditionals).
   *
   * <p><b>Error handling:</b> Both type checking and evaluation errors are printed and cause test
   * failure via {@code fail()}.
   *
   * @param input The OCL string expression to compile and evaluate
   * @return The evaluated result as a {@link Value}
   * @throws AssertionError if type checking or evaluation fails
   */
  private Value compile(String input) {
    // Create lexer and token stream
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // Parse
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // After parsing, reset to start for type checking and evaluation
    tokens.seek(0);

    // Create dummy specification (no metamodels needed for string operations)
    MetamodelWrapperInterface dummySpec =
        new MetamodelWrapperInterface() {
          @Override
          public EClass resolveEClass(String metamodel, String className) {
            return null;
          }

          @Override
          public List<EObject> getAllInstances(EClass eClass) {
            return List.of();
          }

          @Override
          public Set<String> getAvailableMetamodels() {
            return Set.of();
          }
        };

    // Initialize 3-pass architecture
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    // Pass 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (!errors.getErrors().isEmpty()) {
      System.out.println("PASS 1 (SYMBOL TABLE) ERRORS:");
      errors.getErrors().forEach(System.out::println);
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);

    if (!typeChecker.getErrorCollector().getErrors().isEmpty()) {
      System.out.println("PASS 2 (TYPE CHECKING) ERRORS:");
      typeChecker.getErrorCollector().getErrors().forEach(System.out::println);
      fail("Pass 2 (Type checking) failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Pass 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);

    Value result = evaluator.visit(tree);

    if (!evaluator.getErrorCollector().getErrors().isEmpty()) {
      System.out.println("PASS 3 (EVALUATION) ERRORS:");
      evaluator.getErrorCollector().getErrors().forEach(System.out::println);
      fail("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }
}