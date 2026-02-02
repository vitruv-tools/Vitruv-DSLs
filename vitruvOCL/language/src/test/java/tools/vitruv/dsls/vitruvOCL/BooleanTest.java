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
 * Comprehensive test suite for boolean operations and logical expressions in VitruvOCL.
 *
 * <h2>Tested Operations</h2>
 *
 * <ul>
 *   <li><b>Literals:</b> {@code true}, {@code false}
 *   <li><b>Unary:</b> {@code not}
 *   <li><b>Binary:</b> {@code and}, {@code or}, {@code xor}, {@code implies}
 *   <li><b>Complex:</b> Nested expressions with precedence and parentheses
 * </ul>
 *
 * @see Value Runtime value representation (collections)
 * @see OCLElement.BoolValue Boolean element wrapper
 * @see EvaluationVisitor Evaluates boolean expressions
 */
public class BooleanTest {

  // ==================== Boolean Literals ====================

  /**
   * Tests evaluation of the {@code true} literal.
   *
   * <p><b>Input:</b> {@code true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   */
  @Test
  public void testTrueLiteral() {
    String input = "true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "true should evaluate to true");
  }

  /**
   * Tests evaluation of the {@code false} literal.
   *
   * <p><b>Input:</b> {@code false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   */
  @Test
  public void testFalseLiteral() {
    String input = "false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "false should evaluate to false");
  }

  // ==================== Unary NOT ====================

  /**
   * Tests negation of {@code true}.
   *
   * <p><b>Input:</b> {@code not true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   *
   * <p><b>Truth table:</b> ¬T = F
   */
  @Test
  public void testNotTrue() {
    String input = "not true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "not true should be false");
  }

  /**
   * Tests negation of {@code false}.
   *
   * <p><b>Input:</b> {@code not false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> ¬F = T
   */
  @Test
  public void testNotFalse() {
    String input = "not false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "not false should be true");
  }

  /**
   * Tests double negation (idempotence).
   *
   * <p><b>Input:</b> {@code not not true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Logical law:</b> ¬¬A = A (double negation elimination)
   */
  @Test
  public void testDoubleNegation() {
    String input = "not not true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "not not true should be true");
  }

  // ==================== AND ====================

  /**
   * Tests conjunction of two {@code true} values.
   *
   * <p><b>Input:</b> {@code true and true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> T ∧ T = T
   */
  @Test
  public void testTrueAndTrue() {
    String input = "true and true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "true and true should be true");
  }

  /**
   * Tests conjunction of {@code true} and {@code false}.
   *
   * <p><b>Input:</b> {@code true and false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   *
   * <p><b>Truth table:</b> T ∧ F = F
   */
  @Test
  public void testTrueAndFalse() {
    String input = "true and false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "true and false should be false");
  }

  /**
   * Tests conjunction of two {@code false} values.
   *
   * <p><b>Input:</b> {@code false and false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   *
   * <p><b>Truth table:</b> F ∧ F = F
   */
  @Test
  public void testFalseAndFalse() {
    String input = "false and false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "false and false should be false");
  }

  // ==================== OR ====================

  /**
   * Tests disjunction of {@code true} and {@code false}.
   *
   * <p><b>Input:</b> {@code true or false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> T ∨ F = T
   */
  @Test
  public void testTrueOrFalse() {
    String input = "true or false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "true or false should be true");
  }

  /**
   * Tests disjunction of two {@code false} values.
   *
   * <p><b>Input:</b> {@code false or false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   *
   * <p><b>Truth table:</b> F ∨ F = F
   */
  @Test
  public void testFalseOrFalse() {
    String input = "false or false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "false or false should be false");
  }

  /**
   * Tests disjunction of two {@code true} values.
   *
   * <p><b>Input:</b> {@code true or true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> T ∨ T = T
   */
  @Test
  public void testTrueOrTrue() {
    String input = "true or true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "true or true should be true");
  }

  // ==================== XOR ====================

  /**
   * Tests exclusive or of {@code true} and {@code false}.
   *
   * <p><b>Input:</b> {@code true xor false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> T ⊕ F = T (one but not both)
   */
  @Test
  public void testTrueXorFalse() {
    String input = "true xor false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "true xor false should be true");
  }

  /**
   * Tests exclusive or of two {@code true} values.
   *
   * <p><b>Input:</b> {@code true xor true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   *
   * <p><b>Truth table:</b> T ⊕ T = F (both are true, so xor is false)
   */
  @Test
  public void testTrueXorTrue() {
    String input = "true xor true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "true xor true should be false");
  }

  /**
   * Tests exclusive or of two {@code false} values.
   *
   * <p><b>Input:</b> {@code false xor false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   *
   * <p><b>Truth table:</b> F ⊕ F = F (neither is true, so xor is false)
   */
  @Test
  public void testFalseXorFalse() {
    String input = "false xor false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "false xor false should be false");
  }

  // ==================== IMPLIES ====================

  /**
   * Tests implication where antecedent and consequent are both {@code true}.
   *
   * <p><b>Input:</b> {@code true implies true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> T → T = T
   *
   * <p><b>Logical meaning:</b> If true is true, then true is true (valid)
   */
  @Test
  public void testTrueImpliesTrue() {
    String input = "true implies true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "true implies true should be true");
  }

  /**
   * Tests implication where antecedent is {@code true} and consequent is {@code false}.
   *
   * <p><b>Input:</b> {@code true implies false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code false}
   *
   * <p><b>Truth table:</b> T → F = F
   *
   * <p><b>Logical meaning:</b> If true is true, then false is true (invalid - this is the only
   * false case)
   */
  @Test
  public void testTrueImpliesFalse() {
    String input = "true implies false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "true implies false should be false");
  }

  /**
   * Tests implication where antecedent is {@code false} and consequent is {@code true}.
   *
   * <p><b>Input:</b> {@code false implies true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> F → T = T (vacuous truth)
   *
   * <p><b>Logical meaning:</b> If false is true (never happens), then anything follows. This is
   * vacuously true.
   */
  @Test
  public void testFalseImpliesTrue() {
    String input = "false implies true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "false implies true should be true");
  }

  /**
   * Tests implication where both antecedent and consequent are {@code false}.
   *
   * <p><b>Input:</b> {@code false implies false}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Truth table:</b> F → F = T (vacuous truth)
   *
   * <p><b>Logical meaning:</b> If false is true (never happens), then false is true. This is
   * vacuously true.
   */
  @Test
  public void testFalseImpliesFalse() {
    String input = "false implies false";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "false implies false should be true");
  }

  // ==================== Complex Expressions ====================

  /**
   * Debug test for complex boolean expression with parentheses.
   *
   * <p><b>Input:</b> {@code true and (false or true)}
   *
   * <p><b>Purpose:</b> Validates that the type checker handles nested expressions with proper
   * precedence. This test only checks type checking, not evaluation.
   *
   * <p><b>Expression breakdown:</b>
   *
   * <ol>
   *   <li>{@code (false or true)} → {@code true}
   *   <li>{@code true and true} → {@code true}
   * </ol>
   */
  @Test
  public void testComplexBooleanExpressionDebug() {
    String input = "true and (false or true)";

    ParseTree tree = parse(input);

    // Create dummy specification (no metamodels needed for boolean tests)
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

    // Type check only (no evaluation)
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Type checking failed");
    }
  }

  /**
   * Tests complex expression with {@code not}, {@code and}, and {@code or} operators.
   *
   * <p><b>Input:</b> {@code not (true and false) or true}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Expression breakdown:</b>
   *
   * <ol>
   *   <li>{@code true and false} → {@code false}
   *   <li>{@code not false} → {@code true}
   *   <li>{@code true or true} → {@code true}
   * </ol>
   *
   * <p><b>Validates:</b> Operator precedence and parentheses handling
   */
  @Test
  public void testNotAndOr() {
    String input = "not (true and false) or true";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(
        ((OCLElement.BoolValue) elem).value(), "not (true and false) or true should be true");
  }

  /**
   * Tests boolean expression using comparison results.
   *
   * <p><b>Input:</b> {@code (5 > 3) and (10 < 20)}
   *
   * <p><b>Expected:</b> Singleton collection containing {@code true}
   *
   * <p><b>Expression breakdown:</b>
   *
   * <ol>
   *   <li>{@code 5 > 3} → {@code true}
   *   <li>{@code 10 < 20} → {@code true}
   *   <li>{@code true and true} → {@code true}
   * </ol>
   *
   * <p><b>Validates:</b> Integration between comparison operators and boolean logic
   */
  @Test
  public void testComparisonInBoolean() {
    String input = "(5 > 3) and (10 < 20)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "(5 > 3) and (10 < 20) should be true");
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL expression through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process:
   *
   * <ol>
   *   <li><b>Parsing:</b> Converts input string to parse tree using ANTLR
   *   <li><b>Type Checking:</b> Validates types and builds type annotations
   *   <li><b>Evaluation:</b> Computes runtime values
   * </ol>
   *
   * <p>The method includes extensive debug output to trace execution through each phase, making it
   * easier to diagnose failures.
   *
   * <p><b>Error Handling:</b> The method fails the test immediately if any phase encounters errors,
   * printing detailed error messages with line numbers.
   *
   * @param input The OCL expression to compile and evaluate
   * @return The evaluated result as a {@link Value} (collection of {@link OCLElement}s)
   * @throws AssertionError if parsing, type checking, or evaluation fails
   */
  private Value compile(String input) {
    // Phase 1: Parse
    ParseTree tree = parse(input);
    System.out.println("DEBUG: Parsed tree: " + tree.toStringTree());

    // Create dummy metamodel wrapper (no metamodels needed for boolean tests)
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

    // Phase 2: Type Check
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    System.out.println("DEBUG: Starting type checking...");
    typeChecker.visit(tree);
    System.out.println("DEBUG: Type checking done, hasErrors: " + errors.hasErrors());

    // Check for type checking errors
    if (errors.hasErrors()) {
      System.err.println("DEBUG: Errors found in ErrorCollector:");
      errors
          .getErrors()
          .forEach(
              err ->
                  System.err.println("  Error at line " + err.getLine() + ": " + err.getMessage()));
      fail("Type checking failed: " + errors.getErrors());
    }

    if (typeChecker.hasErrors()) {
      System.err.println("DEBUG: Errors found in TypeChecker:");
      typeChecker
          .getErrorCollector()
          .getErrors()
          .forEach(
              err ->
                  System.err.println("  Error at line " + err.getLine() + ": " + err.getMessage()));
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Phase 3: Evaluate
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    System.err.println("DEBUG: Starting evaluation...");
    Value result = evaluator.visit(tree);
    System.err.println("DEBUG: Evaluation done, hasErrors: " + errors.hasErrors());

    // Check for evaluation errors
    if (errors.hasErrors()) {
      System.err.println("DEBUG: Evaluation errors:");
      errors
          .getErrors()
          .forEach(err -> System.err.println("  Line " + err.getLine() + ": " + err.getMessage()));
      fail("Evaluation failed: " + errors.getErrors());
    }

    return result;
  }

  /**
   * Parses an OCL expression string into an ANTLR parse tree.
   *
   * <p>This method creates a lexer and parser for the input string and returns the parse tree
   * starting from the {@code expCS} rule (expression entry point).
   *
   * <p><b>Note:</b> This method uses {@code expCS} as the entry point rather than the full {@code
   * contextDeclCS} since these tests focus on standalone expressions without context declarations.
   *
   * @param input The OCL expression string to parse
   * @return The ANTLR parse tree representing the expression
   */
  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.expCS(); // Top-level expression entry point
  }
}