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
 * Tests for string operations.
 *
 * <p>Tests string literals and string comparisons. OCL# semantics: All values are collections.
 * "hello" results in singleton ["hello"].
 */
public class StringTest {

  // ==================== String Literals ====================

  @Test
  public void testSimpleString() {
    String input = "\"hello\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.StringValue, "Element should be StringValue");
    assertEquals("hello", ((OCLElement.StringValue) elem).value(), "String should be 'hello'");
  }

  @Test
  public void testEmptyString() {
    String input = "\"\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals("", ((OCLElement.StringValue) elem).value(), "String should be empty");
  }

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

  @Test
  public void testStringEquality() {
    String input = "\"hello\" == \"hello\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "\"hello\" == \"hello\" should be true");
  }

  @Test
  public void testStringInequalityTrue() {
    String input = "\"hello\" != \"world\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "\"hello\" != \"world\" should be true");
  }

  @Test
  public void testStringInequalityFalse() {
    String input = "\"test\" != \"test\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "\"test\" != \"test\" should be false");
  }

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

  @Test
  public void testStringGreaterThan() {
    String input = "\"zebra\" > \"apple\"";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "\"zebra\" > \"apple\" should be true");
  }

  // ==================== String in Collections ====================

  @Test
  public void testStringSet() {
    String input = "Set{\"a\", \"b\", \"c\"}";
    Value result = compile(input);

    assertEquals(3, result.size(), "Set should have 3 elements");
    assertTrue(result.includes(new OCLElement.StringValue("a")), "Set should contain 'a'");
    assertTrue(result.includes(new OCLElement.StringValue("b")), "Set should contain 'b'");
    assertTrue(result.includes(new OCLElement.StringValue("c")), "Set should contain 'c'");
  }

  @Test
  public void testStringSetSize() {
    String input = "Set{\"hello\", \"world\"}.size()";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(
        2, ((OCLElement.IntValue) elem).value(), "Set{\"hello\", \"world\"}.size() should be 2");
  }

  @Test
  public void testStringSetIncludes() {
    String input = "Set{\"apple\", \"banana\"}.includes(\"apple\")";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Set should include \"apple\"");
  }

  // ==================== Helper Methods ====================

  private Value compile(String input) {
    ParseTree tree = parse(input);
    // Dummy specification
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

    // Pass 1: Symbol Table
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ErrorCollector errors = new ErrorCollector();

    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.visit(tree);

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    return result;
  }

  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.infixedExpCS();
  }
}