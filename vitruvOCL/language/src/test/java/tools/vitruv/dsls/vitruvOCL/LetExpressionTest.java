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
 * Tests for let expressions and variable binding.
 *
 * <p>Tests the complete 3-pass pipeline for variable scoping.
 */
public class LetExpressionTest {

  // ==================== Simple Let Expressions ====================

  @Test
  public void testSimpleLet() {
    assertInt("let x = 5 in x + 3", 8);
  }

  @Test
  public void testLetWithArithmetic() {
    assertInt("let x = 10 in x * 2", 20);
  }

  @Test
  public void testLetWithTypeAnnotation() {
    assertInt("let x : Integer = 15 in x - 5", 10);
  }

  @Test
  public void testLetUseVariableMultipleTimes() {
    assertInt("let x = 4 in x + x + x", 12);
  }

  // ==================== Multiple Variables ====================

  @Test
  public void testLetMultipleVariables() {
    assertInt("let x = 5, y = 3 in x + y", 8);
  }

  @Test
  public void testLetMultipleVariablesComplex() {
    assertInt("let x = 10, y = 20, z = 5 in x + y - z", 25);
  }

  @Test
  public void testLetSequentialDependency() {
    // y depends on x (evaluated left-to-right)
    assertInt("let x = 5, y = x * 2 in y + 3", 13);
  }

  // ==================== Nested Let ====================

  @Test
  public void testNestedLet() {
    assertInt("let x = 5 in let y = x * 2 in y + 3", 13);
  }

  @Test
  public void testNestedLetDeep() {
    assertInt("let x = 2 in let y = x * 3 in let z = y + 1 in z * 2", 14);
    // x=2, y=6, z=7, result=14
  }

  @Test
  public void testLetShadowing() {
    // Inner 'x' shadows outer 'x'
    assertInt("let x = 5 in let x = 10 in x", 10);
  }

  @Test
  public void testLetShadowingWithArithmetic() {
    // Outer x=5, inner x=10, use inner x
    assertInt("let x = 5 in let x = x * 2 in x + 1", 11);
    // Inner x = outer x * 2 = 10, result = 11
  }

  @Test
  public void testLetAccessOuterVariable() {
    // Inner scope can access outer scope
    assertInt("let x = 5 in let y = x + 2 in y * 2", 14);
  }

  // ==================== Let with Collections ====================

  @Test
  public void testLetWithSet() {
    assertInt("let nums = Set{1,2,3} in nums.sum()", 6);
  }

  @Test
  public void testLetWithSequence() {
    assertInt("let seq = Sequence{5,10,15} in seq.max()", 15);
  }

  @Test
  public void testLetCollectionOperations() {
    assertInt("let s = Set{1,2,3} in s.including(4).sum()", 10);
  }

  @Test
  public void testLetWithCollectionVariable() {
    assertInt("let nums = Set{1,2,3}, total = nums.sum() in total * 2", 12);
  }

  // ==================== Let with Booleans ====================

  @Test
  public void testLetWithBoolean() {
    assertBool("let flag = true in flag and false", false);
  }

  @Test
  public void testLetWithComparison() {
    assertBool("let x = 10, y = 5 in x > y", true);
  }

  // ==================== Let with Strings ====================

  @Test
  public void testLetWithString() {
    assertString("let s = \"hello\" in s.concat(\" world\")", "hello world");
  }

  @Test
  public void testLetStringOperations() {
    assertString("let s = \"HELLO\" in s.toLower()", "hello");
  }

  // ==================== Let with If-Then-Else ====================

  @Test
  public void testLetInIfCondition() {
    assertInt("let x = 10 in if x > 5 then x * 2 else x endif", 20);
  }

  @Test
  public void testLetInThenBranch() {
    assertInt("if true then let x = 5 in x + 3 else 0 endif", 8);
  }

  @Test
  public void testLetInElseBranch() {
    assertInt("if false then 0 else let x = 7 in x * 2 endif", 14);
  }

  // ==================== Complex Let Expressions ====================

  @Test
  public void testLetComplexExpression() {
    assertInt("let x = 5 in let y = Set{1,2,3}.sum() in x + y", 11);
  }

  @Test
  public void testLetWithNestedCollections() {
    assertInt("let outer = Set{1,2} in let inner = Set{3,4} in outer.union(inner).sum()", 10);
  }

  // ==================== Helper Methods ====================

  private Value compile(String input) {
    // Parse
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    tokens.fill();

    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

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

    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ErrorCollector errors = new ErrorCollector();

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);

    // Pass 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);

    Value result = evaluator.visit(tree);

    return result;
  }

  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.infixedExpCS();
  }

  private void assertInt(String input, int expected) {
    Value result = compile(input);

    if (result.size() == 0) {
      fail("Result is empty, expected singleton with value " + expected);
    }

    assertEquals(1, result.size(), "Result should be singleton");

    OCLElement elem = result.getElements().get(0);

    if (!(elem instanceof OCLElement.IntValue)) {
      fail("Expected IntValue but got " + elem.getClass().getSimpleName());
    }

    int actual = ((OCLElement.IntValue) elem).value();

    assertEquals(expected, actual, "Expected " + expected + " but got " + actual);
  }

  private void assertBool(String input, boolean expected) {
    Value result = compile(input);
    assertEquals(1, result.size(), "Result should be singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(expected, ((OCLElement.BoolValue) elem).value());
  }

  private void assertString(String input, String expected) {
    Value result = compile(input);
    assertEquals(1, result.size(), "Result should be singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(expected, ((OCLElement.StringValue) elem).value());
  }
}