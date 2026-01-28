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
 * Tests for arithmetic and comparison operations.
 *
 * <p>Tests the complete 3-pass pipeline for mathematical expressions. OCL# semantics: All values
 * are collections. "1+2" results in singleton [3].
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

  // ==================== Collection Arithmetic Operations ====================

  @Test
  public void testSum() {
    assertInt("Set{1,2,3,4,5}.sum()", 15);
    assertInt("Set{10,20,30}.sum()", 60);
    assertInt("Sequence{1,2,3}.sum()", 6);
  }

  @Test
  public void testSumEmptyCollection() {
    assertInt("Set{}.sum()", 0);
  }

  @Test
  public void testMax() {
    assertInt("Set{1,5,3,9,2}.max()", 9);
    assertInt("Sequence{100,50,200,75}.max()", 200);
    assertInt("Set{-5,-10,-1}.max()", -1);
  }

  @Test
  public void testMaxEmptyCollection() {
    assertEmpty("Set{}.max()");
  }

  @Test
  public void testMin() {
    assertInt("Set{1,5,3,9,2}.min()", 1);
    assertInt("Sequence{100,50,200,75}.min()", 50);
    assertInt("Set{-5,-10,-1}.min()", -10);
  }

  @Test
  public void testMinEmptyCollection() {
    assertEmpty("Set{}.min()");
  }

  @Test
  public void testAvg() {
    assertInt("Set{1,2,3,4,5}.avg()", 3);
    assertInt("Sequence{10,20,30}.avg()", 20);
    assertInt("Set{100,200}.avg()", 150);
  }

  @Test
  public void testAvgEmptyCollection() {
    assertEmpty("Set{}.avg()");
  }

  @Test
  public void testAbs() {
    assertCollection("Set{-1,-2,-3}.abs()", 1, 2, 3);
    assertCollection("Sequence{-5,10,-15}.abs()", 5, 10, 15);
    assertCollection("Set{1,2,3}.abs()", 1, 2, 3);
  }

  @Test
  public void testFloor() {
    // For integers, floor is no-op
    assertCollection("Set{1,2,3}.floor()", 1, 2, 3);
    assertCollection("Sequence{10,20,30}.floor()", 10, 20, 30);
  }

  @Test
  public void testCeil() {
    // For integers, ceil is no-op
    assertCollection("Set{1,2,3}.ceil()", 1, 2, 3);
    assertCollection("Sequence{10,20,30}.ceil()", 10, 20, 30);
  }

  @Test
  public void testRound() {
    // For integers, round is no-op
    assertCollection("Set{1,2,3}.round()", 1, 2, 3);
    assertCollection("Sequence{10,20,30}.round()", 10, 20, 30);
  }

  @Test
  public void testLift() {
    // {1,2,3}.lift() → {{1,2,3}}
    Value result = compile("Set{1,2,3}.lift()");

    assertEquals(1, result.size(), "lift() should create singleton containing collection");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.NestedCollection, "Element should be NestedCollection");

    OCLElement.NestedCollection nested = (OCLElement.NestedCollection) elem;
    assertEquals(3, nested.value().size(), "Inner collection should have 3 elements");
  }

  @Test
  public void testLiftThenFlatten() {
    // {1,2,3}.lift().flatten() → {1,2,3}
    assertCollection("Set{1,2,3}.lift().flatten()", 1, 2, 3);
  }

  @Test
  public void testArithmeticChaining() {
    assertInt("Set{1,2,3,4,5}.sum() + Set{10,20}.sum()", 45); // 15 + 30
    assertInt("Set{10,20,30}.max() - Set{1,2,3}.min()", 29); // 30 - 1
  }

  @Test
  public void testComplexArithmetic() {
    assertInt("Set{-5,-10,15,20}.abs().max()", 20);
    assertInt("Set{-1,-2,-3}.abs().sum()", 6);
    assertInt("Sequence{5,10,15,20}.sum() / Set{2,4}.max()", 12); // 50 / 4
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

  private void assertInt(String input, int expected) {
    Value result = compile(input);
    assertEquals(1, result.size(), "Result should be singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(
        expected,
        ((OCLElement.IntValue) elem).value(),
        "Expected " + expected + " but got " + ((OCLElement.IntValue) elem).value());
  }

  private void assertEmpty(String input) {
    Value result = compile(input);
    assertEquals(0, result.size(), "Result should be empty");
  }

  private void assertCollection(String input, int... expected) {
    Value result = compile(input);
    assertEquals(expected.length, result.size(), "Collection size mismatch");

    List<Integer> actual =
        result.getElements().stream().map(e -> ((OCLElement.IntValue) e).value()).sorted().toList();

    List<Integer> expectedList = java.util.Arrays.stream(expected).boxed().sorted().toList();

    assertEquals(expectedList, actual, "Collection content mismatch");
  }
}