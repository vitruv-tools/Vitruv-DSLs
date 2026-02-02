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
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Fundamental test suite for String literals and comparison operations in VitruvOCL.
 *
 * <p>This test class validates the basic string functionality in the VitruvOCL compiler, focusing
 * on string literal evaluation, comparison operations, and string elements in collections. This
 * complements {@link StringOperationsTest} which covers more advanced string manipulation
 * operations.
 *
 * @see Value Runtime value representation
 * @see OCLElement.StringValue String element wrapper
 * @see StringOperationsTest Advanced string manipulation operations
 * @see EvaluationVisitor Evaluates string expressions
 * @see TypeCheckVisitor Type checks string expressions
 */
public class StringTest {

  // ==================== String Literals ====================

  /**
   * Tests basic string literal evaluation.
   *
   * <p><b>Input:</b> {@code "hello"}
   *
   * <p><b>Expected:</b> {@code ["hello"]} (singleton)
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>String literal parsing
   *   <li>Singleton collection wrapping
   *   <li>StringValue element type
   *   <li>Character sequence preservation
   * </ul>
   */
  @Test
  public void testSimpleString() {
    String input = "\"hello\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.StringValue, "Element should be StringValue");
    assertEquals("hello", ((OCLElement.StringValue) elem).value(), "String should be 'hello'");
  }

  /**
   * Tests empty string literal.
   *
   * <p><b>Input:</b> {@code ""}
   *
   * <p><b>Expected:</b> {@code [""]} (singleton containing empty string)
   *
   * <p><b>Important distinction:</b> This produces a singleton collection containing an empty
   * string, NOT an empty collection. {@code [""]} has size 1, while {@code []} has size 0.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Empty string literal parsing
   *   <li>Empty string is a valid value (not null)
   *   <li>Singleton wrapping applies to empty strings
   * </ul>
   */
  @Test
  public void testEmptyString() {
    String input = "\"\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals("", ((OCLElement.StringValue) elem).value(), "String should be empty");
  }

  /**
   * Tests string literal containing spaces.
   *
   * <p><b>Input:</b> {@code "hello world"}
   *
   * <p><b>Expected:</b> {@code ["hello world"]}
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Whitespace preservation in string literals
   *   <li>Multi-word strings
   * </ul>
   */
  @Test
  public void testStringWithSpaces() {
    String input = "\"hello world\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(
        "hello world", ((OCLElement.StringValue) elem).value(), "String should be 'hello world'");
  }

  // ==================== String Comparison ====================

  /**
   * Tests string equality comparison (true case).
   *
   * <p><b>Input:</b> {@code "hello" == "hello"}
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Equality semantics:</b> Two strings are equal if they have identical character sequences.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>String equality operator
   *   <li>Character-by-character comparison
   *   <li>Boolean result wrapping
   * </ul>
   */
  @Test
  public void testStringEquality() {
    String input = "\"hello\" == \"hello\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "\"hello\" == \"hello\" should be true");
  }

  /**
   * Tests string inequality comparison (true case - different strings).
   *
   * <p><b>Input:</b> {@code "hello" != "world"}
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Inequality semantics:</b> True if strings differ in any character.
   *
   * <p><b>Validates:</b> String inequality operator for different strings.
   */
  @Test
  public void testStringInequalityTrue() {
    String input = "\"hello\" != \"world\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "\"hello\" != \"world\" should be true");
  }

  /**
   * Tests string inequality comparison (false case - identical strings).
   *
   * <p><b>Input:</b> {@code "test" != "test"}
   *
   * <p><b>Expected:</b> {@code [false]}
   *
   * <p><b>Validates:</b> String inequality returns false for identical strings.
   */
  @Test
  public void testStringInequalityFalse() {
    String input = "\"test\" != \"test\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "\"test\" != \"test\" should be false");
  }

  /**
   * Tests lexicographic less-than comparison.
   *
   * <p><b>Input:</b> {@code "apple" < "banana"}
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Lexicographic ordering:</b> Strings are compared character-by-character using character
   * codes. "apple" comes before "banana" in dictionary order because 'a' < 'b'.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Less-than operator on strings
   *   <li>Lexicographic comparison
   *   <li>Dictionary ordering semantics
   * </ul>
   */
  @Test
  public void testStringLessThan() {
    String input = "\"apple\" < \"banana\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(
        ((OCLElement.BoolValue) elem).value(),
        "\"apple\" < \"banana\" should be true (lexicographic)");
  }

  /**
   * Tests lexicographic greater-than comparison.
   *
   * <p><b>Input:</b> {@code "zebra" > "apple"}
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Lexicographic ordering:</b> "zebra" comes after "apple" in dictionary order because 'z' >
   * 'a'.
   *
   * <p><b>Validates:</b> Greater-than operator on strings with lexicographic comparison.
   */
  @Test
  public void testStringGreaterThan() {
    String input = "\"zebra\" > \"apple\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "\"zebra\" > \"apple\" should be true");
  }

  // ==================== String in Collections ====================

  /**
   * Tests creating a Set collection with string elements.
   *
   * <p><b>Input:</b> {@code Set{"a", "b", "c"}}
   *
   * <p><b>Expected:</b> {@code {"a", "b", "c"}} (Set with 3 elements)
   *
   * <p><b>Collection properties:</b>
   *
   * <ul>
   *   <li>Set contains three distinct string elements
   *   <li>Order is not guaranteed (Set is unordered)
   *   <li>No duplicates (Set semantics)
   * </ul>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>String elements in Set collections
   *   <li>Set literal syntax with strings
   *   <li>Multiple string elements
   *   <li>Collection includes() method for membership testing
   * </ul>
   */
  @Test
  public void testStringSet() {
    String input = "Set{\"a\", \"b\", \"c\"}";
    Value result = compile(input);

    assertEquals(3, result.size(), "Set should have 3 elements");
    assertTrue(result.includes(new OCLElement.StringValue("a")), "Set should contain 'a'");
    assertTrue(result.includes(new OCLElement.StringValue("b")), "Set should contain 'b'");
    assertTrue(result.includes(new OCLElement.StringValue("c")), "Set should contain 'c'");
  }

  /**
   * Tests size() operation on String Set.
   *
   * <p><b>Input:</b> {@code Set{"hello", "world"}.size()}
   *
   * <p><b>Expected:</b> {@code [2]}
   *
   * <p><b>Operation:</b> size() returns the number of elements in the collection as a singleton
   * Integer.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>size() operation on string collections
   *   <li>Correct count of string elements
   *   <li>Integer result wrapping
   * </ul>
   */
  @Test
  public void testStringSetSize() {
    String input = "Set{\"hello\", \"world\"}.size()";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(
        2, ((OCLElement.IntValue) elem).value(), "Set{\"hello\", \"world\"}.size() should be 2");
  }

  /**
   * Tests includes() membership test on String Set.
   *
   * <p><b>Input:</b> {@code Set{"apple", "banana"}.includes("apple")}
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Membership test:</b> includes() checks if the collection contains the specified element
   * using equality comparison.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>includes() operation on string collections
   *   <li>String membership testing
   *   <li>String equality in collection context
   * </ul>
   */
  @Test
  public void testStringSetIncludes() {
    String input = "Set{\"apple\", \"banana\"}.includes(\"apple\")";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Set should include \"apple\"");
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL string expression through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process:
   *
   * <ol>
   *   <li><b>Phase 1 - Parsing:</b> Converts input to parse tree using {@code infixedExpCS} entry
   *       point
   *   <li><b>Phase 2 - Type Checking:</b> Validates string types and operations
   *   <li><b>Phase 3 - Evaluation:</b> Produces string values wrapped in collections
   * </ol>
   *
   * <p><b>No token stream handling:</b> Unlike tests involving conditionals or keywords, basic
   * string operations don't require explicit token stream management.
   *
   * @param input The OCL string expression to compile and evaluate
   * @return The evaluated result as a {@link Value}
   */
  private Value compile(String input) {
    ParseTree tree = parse(input);
    // Dummy specification (no metamodels needed for string operations)
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

    // Pass 1: Symbol Table (trivial for string literals)
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ErrorCollector errors = new ErrorCollector();

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.visit(tree);

    // Pass 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    return result;
  }

  /**
   * Parses an OCL expression string into an ANTLR parse tree.
   *
   * <p>Uses {@code infixedExpCS} as the entry point to handle string literals and comparison
   * expressions.
   *
   * @param input The OCL expression string to parse
   * @return The ANTLR parse tree representing the expression
   */
  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.infixedExpCS();
  }
}