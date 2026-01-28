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
 * Tests for OCL# Iterator Operations.
 *
 * <p>Tests the complete pipeline for Iterator Expressions: - select(x | predicate) - reject(x |
 * predicate) - collect(x | expression) - forAll(x | predicate) - exists(x | predicate)
 */
public class IteratorTest {

  // ==================== SELECT ====================

  @Test
  public void testSelectBasic() {
    String input = "Set{1,2,3,4,5}.select(x | x > 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "select(x | x > 2) should keep 3, 4, 5");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  @Test
  public void testSelectNoneMatch() {
    String input = "Set{1,2,3}.select(x | x > 10)";
    Value result = compile(input);

    assertTrue(result.isEmpty(), "select with no matches should return empty set");
    assertEquals(0, result.size());
  }

  @Test
  public void testSelectAllMatch() {
    String input = "Set{1,2,3}.select(x | x > 0)";
    Value result = compile(input);

    assertEquals(3, result.size(), "select with all matches should return all elements");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  @Test
  public void testSelectComplexPredicate() {
    String input = "Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3 and x < 8)";
    Value result = compile(input);

    assertEquals(4, result.size(), "Should select elements between 3 and 8");
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(7)));
  }

  @Test
  public void testSelectWithArithmetic() {
    String input = "Set{1,2,3,4,5}.select(x | x * 2 > 5)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should select 3, 4, 5 (where x*2 > 5)");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  @Test
  public void testSelectOnSequence() {
    String input = "Sequence{5,2,8,1,9,3}.select(x | x > 4)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should select 5, 8, 9");
    // Note: Order might not be preserved depending on implementation
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
  }

  // ==================== REJECT ====================

  @Test
  public void testRejectBasic() {
    String input = "Set{1,2,3,4,5}.reject(x | x <= 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "reject(x | x <= 2) should keep 3, 4, 5");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  @Test
  public void testRejectNoneRejected() {
    String input = "Set{1,2,3}.reject(x | x > 10)";
    Value result = compile(input);

    assertEquals(3, result.size(), "reject with no rejections should return all elements");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  @Test
  public void testRejectAllRejected() {
    String input = "Set{1,2,3}.reject(x | x > 0)";
    Value result = compile(input);

    assertTrue(result.isEmpty(), "reject with all rejected should return empty set");
    assertEquals(0, result.size());
  }

  @Test
  public void testRejectEquality() {
    String input = "Set{1,2,3,4,5}.reject(x | x == 3)";
    Value result = compile(input);

    assertEquals(4, result.size(), "Should reject only element 3");
    assertFalse(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  @Test
  public void testSelectVsReject() {
    // select and reject should be complementary
    String selectInput = "Set{1,2,3,4,5}.select(x | x > 2)";
    String rejectInput = "Set{1,2,3,4,5}.reject(x | x <= 2)";

    Value selectResult = compile(selectInput);
    Value rejectResult = compile(rejectInput);

    assertEquals(
        selectResult.size(),
        rejectResult.size(),
        "select(x | x > 2) and reject(x | x <= 2) should produce same result");
  }

  // ==================== COLLECT ====================

  @Test
  public void testCollectBasic() {
    String input = "Set{1,2,3}.collect(x | x * 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "collect should have 3 transformed elements");
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
  }

  @Test
  public void testCollectWithAddition() {
    String input = "Set{1,2,3}.collect(x | x + 10)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should add 10 to each element");
    assertTrue(result.includes(new OCLElement.IntValue(11)));
    assertTrue(result.includes(new OCLElement.IntValue(12)));
    assertTrue(result.includes(new OCLElement.IntValue(13)));
  }

  @Test
  public void testCollectComplexExpression() {
    String input = "Set{1,2,3}.collect(x | x * 2 + x)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should compute x * 2 + x for each element");
    assertTrue(result.includes(new OCLElement.IntValue(3))); // 1*2+1 = 3
    assertTrue(result.includes(new OCLElement.IntValue(6))); // 2*2+2 = 6
    assertTrue(result.includes(new OCLElement.IntValue(9))); // 3*2+3 = 9
  }

  @Test
  public void testCollectWithDivision() {
    String input = "Set{2,4,6,8,10}.collect(x | x / 2)";
    Value result = compile(input);

    assertEquals(5, result.size(), "Should divide each element by 2");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  @Test
  public void testCollectOnEmptySet() {
    String input = "Set{}.collect(x | x * 2)";
    Value result = compile(input);

    assertTrue(result.isEmpty(), "collect on empty set should return empty set");
    assertEquals(0, result.size());
  }

  @Test
  public void testCollectAutoFlatten() {
    // collect automatically flattens results
    // Even though x * 2 returns [2], [4], [6], collect flattens to [2, 4, 6]
    String input = "Set{1,2,3}.collect(x | x * 2)";
    Value result = compile(input);

    // Check that result is flat, not nested
    for (OCLElement elem : result.getElements()) {
      assertFalse(
          elem instanceof OCLElement.NestedCollection, "collect should auto-flatten results");
    }
  }

  // ==================== FORALL ====================

  @Test
  public void testForAllTrue() {
    String input = "Set{1,2,3}.forAll(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertTrue(((OCLElement.BoolValue) elem).value(), "forAll should be true when all satisfy");
  }

  @Test
  public void testForAllFalse() {
    String input = "Set{1,2,3}.forAll(x | x > 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertFalse(
        ((OCLElement.BoolValue) elem).value(), "forAll should be false when not all satisfy");
  }

  @Test
  public void testForAllOnEmptySet() {
    String input = "Set{}.forAll(x | x > 100)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertTrue(((OCLElement.BoolValue) elem).value(), "forAll on empty set is vacuously true");
  }

  @Test
  public void testForAllComplexPredicate() {
    String input = "Set{2,4,6,8}.forAll(x | x > 0 and x < 10)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All elements satisfy 0 < x < 10");
  }

  @Test
  public void testForAllWithArithmetic() {
    String input = "Set{1,2,3,4,5}.forAll(x | x * 2 <= 10)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All x*2 <= 10");
  }

  @Test
  public void testForAllEquality() {
    String input = "Set{5,5,5}.forAll(x | x == 5)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All elements equal 5");
  }

  // ==================== EXISTS ====================

  @Test
  public void testExistsTrue() {
    String input = "Set{1,2,3}.exists(x | x == 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertTrue(
        ((OCLElement.BoolValue) elem).value(), "exists should be true when at least one satisfies");
  }

  @Test
  public void testExistsFalse() {
    String input = "Set{1,2,3}.exists(x | x > 10)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertFalse(((OCLElement.BoolValue) elem).value(), "exists should be false when none satisfy");
  }

  @Test
  public void testExistsOnEmptySet() {
    String input = "Set{}.exists(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertFalse(((OCLElement.BoolValue) elem).value(), "exists on empty set is false");
  }

  @Test
  public void testExistsMultipleMatch() {
    String input = "Set{1,2,3,4,5}.exists(x | x > 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(
        ((OCLElement.BoolValue) elem).value(), "exists should be true when multiple satisfy");
  }

  @Test
  public void testExistsComplexPredicate() {
    String input = "Set{1,2,3,4,5}.exists(x | x > 3 and x < 5)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Element 4 satisfies predicate");
  }

  @Test
  public void testExistsWithArithmetic() {
    String input = "Set{1,2,3,4,5}.exists(x | x * 2 == 8)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Element 4 satisfies x*2 == 8");
  }

  // ==================== CHAINING ====================

  @Test
  public void testSelectThenCollect() {
    String input = "Set{1,2,3,4,5}.select(x | x > 2).collect(x | x * 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should select then transform");
    assertTrue(result.includes(new OCLElement.IntValue(6))); // 3*2
    assertTrue(result.includes(new OCLElement.IntValue(8))); // 4*2
    assertTrue(result.includes(new OCLElement.IntValue(10))); // 5*2
  }

  @Test
  public void testCollectThenSelect() {
    String input = "Set{1,2,3,4,5}.collect(x | x * 2).select(x | x > 5)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should transform then select");
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(10)));
  }

  @Test
  public void testSelectThenReject() {
    String input = "Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).reject(x | x > 7)";
    Value result = compile(input);

    assertEquals(4, result.size(), "Should select x > 3, then reject x > 7");
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(7)));
  }

  @Test
  public void testCollectThenForAll() {
    String input = "Set{1,2,3}.collect(x | x * 2).forAll(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton boolean");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All transformed elements > 0");
  }

  @Test
  public void testSelectThenExists() {
    String input = "Set{1,2,3,4,5}.select(x | x > 2).exists(x | x == 4)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton boolean");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "4 exists in selected set");
  }

  @Test
  public void testTripleChaining() {
    String input =
        "Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).collect(x | x * 2).reject(x | x > 15)";
    Value result = compile(input);

    // select: {4,5,6,7,8,9,10}
    // collect: {8,10,12,14,16,18,20}
    // reject x > 15: {8,10,12,14}
    assertEquals(4, result.size(), "Should apply all three operations");
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(10)));
    assertTrue(result.includes(new OCLElement.IntValue(12)));
    assertTrue(result.includes(new OCLElement.IntValue(14)));
  }

  // ==================== WITH LET EXPRESSIONS ====================

  @Test
  public void testSelectWithLet() {
    String input = "let threshold = 3 in Set{1,2,3,4,5}.select(x | x > threshold)";
    Value result = compile(input);

    assertEquals(2, result.size(), "Should select elements > threshold");
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  @Test
  public void testCollectWithLet() {
    String input = "let multiplier = 3 in Set{1,2,3}.collect(x | x * multiplier)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should multiply by 3");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
  }

  @Test
  public void testForAllWithLet() {
    String input = "let maxValue = 10 in Set{1,2,3,4,5}.forAll(x | x < maxValue)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All elements < max");
  }

  // ==================== EDGE CASES ====================

  @Test
  public void testSelectOnSingletonSet() {
    String input = "Set{42}.select(x | x > 40)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should keep the single element");
    assertTrue(result.includes(new OCLElement.IntValue(42)));
  }

  @Test
  public void testCollectOnSingletonSet() {
    String input = "Set{5}.collect(x | x * 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should transform single element");
    assertTrue(result.includes(new OCLElement.IntValue(10)));
  }

  @Test
  public void testForAllOnSingletonSet() {
    String input = "Set{42}.forAll(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testExistsOnSingletonSet() {
    String input = "Set{42}.exists(x | x == 42)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  @Test
  public void testSelectWithRange() {
    String input = "Set{1..10}.select(x | x > 5)";
    Value result = compile(input);

    assertEquals(5, result.size(), "Should select 6,7,8,9,10");
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(7)));
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
    assertTrue(result.includes(new OCLElement.IntValue(10)));
  }

  @Test
  public void testCollectWithRange() {
    String input = "Set{1..5}.collect(x | x * x)";
    Value result = compile(input);

    assertEquals(5, result.size(), "Should compute squares");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
    assertTrue(result.includes(new OCLElement.IntValue(16)));
    assertTrue(result.includes(new OCLElement.IntValue(25)));
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates OCL expressions through all 3 passes.
   *
   * @param input OCL Source Code
   * @return Evaluation Result
   */
  private Value compile(String input) {
    // Parse
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

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.visit(tree);

    // Check for Type Errors - PRINT THEM!
    if (typeChecker.hasErrors()) {
      System.err.println("=== TYPE CHECKING ERRORS ===");
      typeChecker
          .getErrorCollector()
          .getErrors()
          .forEach(
              error -> {
                System.err.println(
                    "  Line "
                        + error.getLine()
                        + ":"
                        + error.getColumn()
                        + " - "
                        + error.getMessage());
              });
      System.err.println("============================");
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Pass 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    // Check for Evaluation Errors
    if (evaluator.hasErrors()) {
      System.err.println("=== EVALUATION ERRORS ===");
      evaluator
          .getErrorCollector()
          .getErrors()
          .forEach(
              error -> {
                System.err.println(
                    "  Line "
                        + error.getLine()
                        + ":"
                        + error.getColumn()
                        + " - "
                        + error.getMessage());
              });
      System.err.println("=========================");
      fail("Evaluation failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  /**
   * Parses OCL Input to Parse Tree.
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

    // Parse as Expression
    return parser.prefixedExpCS();
  }
}