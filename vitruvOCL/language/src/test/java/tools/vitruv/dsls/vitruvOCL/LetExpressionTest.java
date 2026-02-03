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
 * Comprehensive test suite for let-expressions and variable binding in VitruvOCL.
 *
 * <p>This test class validates the complete implementation of OCL let-expressions, which provide
 * local variable bindings with lexical scoping. Let-expressions are fundamental for writing
 * readable, maintainable OCL constraints by allowing intermediate value computation and reuse.
 *
 * @author Max
 * @see Value Runtime value representation
 * @see OCLElement Value element types
 * @see EvaluationVisitor Evaluates let-expressions with scope management
 * @see TypeCheckVisitor Type checks let-expressions with scope tracking
 */
public class LetExpressionTest {

  // ==================== Simple Let Expressions ====================

  /**
   * Tests basic let-expression with single variable binding.
   *
   * <p><b>Input:</b> {@code let x = 5 in x + 3}
   *
   * <p><b>Expected:</b> 8
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Bind {@code x} to {@code [5]}
   *   <li>Evaluate body {@code x + 3} with {@code x} in scope
   *   <li>Result: {@code [5] + [3]} → {@code [8]}
   * </ol>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic let syntax parsing
   *   <li>Variable binding
   *   <li>Variable reference in body
   *   <li>Scope creation and cleanup
   * </ul>
   */
  @Test
  public void testSimpleLet() {
    assertInt("let x = 5 in x + 3", 8);
  }

  /**
   * Tests let-expression with arithmetic in binding.
   *
   * <p><b>Input:</b> {@code let x = 10 in x * 2}
   *
   * <p><b>Expected:</b> 20
   *
   * <p><b>Validates:</b> Variable can be used in multiplication operations.
   */
  @Test
  public void testLetWithArithmetic() {
    assertInt("let x = 10 in x * 2", 20);
  }

  /**
   * Tests let-expression with explicit type annotation.
   *
   * <p><b>Input:</b> {@code let x : Integer = 15 in x - 5}
   *
   * <p><b>Expected:</b> 10
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Type annotation syntax parsing
   *   <li>Type checking validates annotation matches actual type
   *   <li>Variable binding works with type annotations
   * </ul>
   */
  @Test
  public void testLetWithTypeAnnotation() {
    assertInt("let x : Integer = 15 in x - 5", 10);
  }

  /**
   * Tests using a bound variable multiple times in the body.
   *
   * <p><b>Input:</b> {@code let x = 4 in x + x + x}
   *
   * <p><b>Expected:</b> 12
   *
   * <p><b>Evaluation:</b> {@code 4 + 4 + 4} = {@code 12}
   *
   * <p><b>Validates:</b> Variables can be referenced multiple times within the same expression.
   */
  @Test
  public void testLetUseVariableMultipleTimes() {
    assertInt("let x = 4 in x + x + x", 12);
  }

  // ==================== Multiple Variables ====================

  /**
   * Tests let-expression with multiple variable bindings.
   *
   * <p><b>Input:</b> {@code let x = 5, y = 3 in x + y}
   *
   * <p><b>Expected:</b> 8
   *
   * <p><b>Syntax:</b> Multiple bindings are separated by commas, all bound before body evaluation.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Multiple variable binding syntax
   *   <li>Both variables accessible in body
   *   <li>Simultaneous scope extension
   * </ul>
   */
  @Test
  public void testLetMultipleVariables() {
    assertInt("let x = 5, y = 3 in x + y", 8);
  }

  /**
   * Tests let-expression with three variables.
   *
   * <p><b>Input:</b> {@code let x = 10, y = 20, z = 5 in x + y - z}
   *
   * <p><b>Expected:</b> 25
   *
   * <p><b>Evaluation:</b> {@code 10 + 20 - 5} = {@code 25}
   *
   * <p><b>Validates:</b> Multiple variables (more than two) can be bound and used.
   */
  @Test
  public void testLetMultipleVariablesComplex() {
    assertInt("let x = 10, y = 20, z = 5 in x + y - z", 25);
  }

  /**
   * Tests sequential dependency between multiple variables.
   *
   * <p><b>Input:</b> {@code let x = 5, y = x * 2 in y + 3}
   *
   * <p><b>Expected:</b> 13
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Bind {@code x} to {@code [5]}
   *   <li>Bind {@code y} to {@code x * 2} = {@code [10]} (can reference {@code x})
   *   <li>Evaluate body: {@code y + 3} = {@code [13]}
   * </ol>
   *
   * <p><b>Left-to-right semantics:</b> Later bindings can reference earlier bindings in the same
   * let-expression.
   */
  @Test
  public void testLetSequentialDependency() {
    // y depends on x (evaluated left-to-right)
    assertInt("let x = 5, y = x * 2 in y + 3", 13);
  }

  // ==================== Nested Let ====================

  /**
   * Tests nested let-expressions.
   *
   * <p><b>Input:</b> {@code let x = 5 in let y = x * 2 in y + 3}
   *
   * <p><b>Expected:</b> 13
   *
   * <p><b>Scope hierarchy:</b>
   *
   * <pre>
   * Scope 1: [x = 5]
   *   Scope 2: [y = 10] (can access x)
   *     Body: y + 3 = 13
   * </pre>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Nested let-expression syntax
   *   <li>Inner scope can access outer variables
   *   <li>Proper scope stacking and cleanup
   * </ul>
   */
  @Test
  public void testNestedLet() {
    assertInt("let x = 5 in let y = x * 2 in y + 3", 13);
  }

  /**
   * Tests deeply nested let-expressions (3 levels).
   *
   * <p><b>Input:</b> {@code let x = 2 in let y = x * 3 in let z = y + 1 in z * 2}
   *
   * <p><b>Expected:</b> 14
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>x = 2
   *   <li>y = x * 3 = 6
   *   <li>z = y + 1 = 7
   *   <li>Result: z * 2 = 14
   * </ol>
   *
   * <p><b>Validates:</b> Multiple levels of nesting with proper scope chain traversal.
   */
  @Test
  public void testNestedLetDeep() {
    assertInt("let x = 2 in let y = x * 3 in let z = y + 1 in z * 2", 14);
    // x=2, y=6, z=7, result=14
  }

  /**
   * Tests variable shadowing in nested let-expressions.
   *
   * <p><b>Input:</b> {@code let x = 5 in let x = 10 in x}
   *
   * <p><b>Expected:</b> 10 (inner x shadows outer x)
   *
   * <p><b>Scoping rule:</b> When a variable name is rebound in an inner scope, the inner binding
   * shadows (hides) the outer binding. The outer binding becomes inaccessible within the inner
   * scope.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Variable shadowing semantics
   *   <li>Inner binding takes precedence
   *   <li>Outer binding is not modified
   * </ul>
   */
  @Test
  public void testLetShadowing() {
    // Inner 'x' shadows outer 'x'
    assertInt("let x = 5 in let x = 10 in x", 10);
  }

  /**
   * Tests shadowing where inner binding references outer variable.
   *
   * <p><b>Input:</b> {@code let x = 5 in let x = x * 2 in x + 1}
   *
   * <p><b>Expected:</b> 11
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Outer x = 5
   *   <li>Inner x = (outer x) * 2 = 10 (references outer x before shadowing)
   *   <li>Body: (inner x) + 1 = 11
   * </ol>
   *
   * <p><b>Scoping subtlety:</b> When evaluating the inner binding expression {@code x * 2}, the
   * name {@code x} still refers to the outer binding. Only after the inner binding is complete does
   * the inner {@code x} shadow the outer.
   */
  @Test
  public void testLetShadowingWithArithmetic() {
    // Outer x=5, inner x=10, use inner x
    assertInt("let x = 5 in let x = x * 2 in x + 1", 11);
    // Inner x = outer x * 2 = 10, result = 11
  }

  /**
   * Tests inner scope accessing outer scope variable.
   *
   * <p><b>Input:</b> {@code let x = 5 in let y = x + 2 in y * 2}
   *
   * <p><b>Expected:</b> 14
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Outer scope: x = 5
   *   <li>Inner scope: y = x + 2 = 7 (accesses outer x)
   *   <li>Body: y * 2 = 14
   * </ol>
   *
   * <p><b>Validates:</b> Lexical scoping allows inner scopes to access outer scope variables.
   */
  @Test
  public void testLetAccessOuterVariable() {
    // Inner scope can access outer scope
    assertInt("let x = 5 in let y = x + 2 in y * 2", 14);
  }

  // ==================== Let with Collections ====================

  /**
   * Tests let-expression binding a Set collection.
   *
   * <p><b>Input:</b> {@code let nums = Set{1,2,3} in nums.sum()}
   *
   * <p><b>Expected:</b> 6
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Binding collection values
   *   <li>Using collection operations on bound variables
   *   <li>Collection type inference
   * </ul>
   */
  @Test
  public void testLetWithSet() {
    assertInt("let nums = Set{1,2,3} in nums.sum()", 6);
  }

  /**
   * Tests let-expression binding a Sequence collection.
   *
   * <p><b>Input:</b> {@code let seq = Sequence{5,10,15} in seq.max()}
   *
   * <p><b>Expected:</b> 15
   *
   * <p><b>Validates:</b> Sequence collections can be bound and operated on.
   */
  @Test
  public void testLetWithSequence() {
    assertInt("let seq = Sequence{5,10,15} in seq.max()", 15);
  }

  /**
   * Tests collection operations on bound collection variable.
   *
   * <p><b>Input:</b> {@code let s = Set{1,2,3} in s.including(4).sum()}
   *
   * <p><b>Expected:</b> 10
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>s = Set{1,2,3}
   *   <li>s.including(4) → Set{1,2,3,4}
   *   <li>.sum() → 10
   * </ol>
   *
   * <p><b>Validates:</b> Chained operations work on bound collection variables.
   */
  @Test
  public void testLetCollectionOperations() {
    assertInt("let s = Set{1,2,3} in s.including(4).sum()", 10);
  }

  /**
   * Tests binding both collection and derived value.
   *
   * <p><b>Input:</b> {@code let nums = Set{1,2,3}, total = nums.sum() in total * 2}
   *
   * <p><b>Expected:</b> 12
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>nums = Set{1,2,3}
   *   <li>total = nums.sum() = 6
   *   <li>Body: total * 2 = 12
   * </ol>
   *
   * <p><b>Validates:</b> Second variable can reference result of operation on first variable.
   */
  @Test
  public void testLetWithCollectionVariable() {
    assertInt("let nums = Set{1,2,3}, total = nums.sum() in total * 2", 12);
  }

  // ==================== Let with Booleans ====================

  /**
   * Tests let-expression binding a Boolean value.
   *
   * <p><b>Input:</b> {@code let flag = true in flag and false}
   *
   * <p><b>Expected:</b> false
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Binding Boolean values
   *   <li>Using Boolean operations on bound variables
   * </ul>
   */
  @Test
  public void testLetWithBoolean() {
    assertBool("let flag = true in flag and false", false);
  }

  /**
   * Tests let-expression with comparison producing Boolean result.
   *
   * <p><b>Input:</b> {@code let x = 10, y = 5 in x > y}
   *
   * <p><b>Expected:</b> true
   *
   * <p><b>Validates:</b> Bound numeric variables can be used in comparisons.
   */
  @Test
  public void testLetWithComparison() {
    assertBool("let x = 10, y = 5 in x > y", true);
  }

  // ==================== Let with Strings ====================

  /**
   * Tests let-expression binding a String value.
   *
   * <p><b>Input:</b> {@code let s = "hello" in s.concat(" world")}
   *
   * <p><b>Expected:</b> "hello world"
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Binding String values
   *   <li>Using String operations on bound variables
   * </ul>
   */
  @Test
  public void testLetWithString() {
    assertString("let s = \"hello\" in s.concat(\" world\")", "hello world");
  }

  /**
   * Tests String operations on bound String variable.
   *
   * <p><b>Input:</b> {@code let s = "HELLO" in s.toLower()}
   *
   * <p><b>Expected:</b> "hello"
   *
   * <p><b>Validates:</b> String transformation operations work on bound variables.
   */
  @Test
  public void testLetStringOperations() {
    assertString("let s = \"HELLO\" in s.toLower()", "hello");
  }

  // ==================== Let with If-Then-Else ====================

  /**
   * Tests let-expression where variable is used in if-condition.
   *
   * <p><b>Input:</b> {@code let x = 10 in if x > 5 then x * 2 else x endif}
   *
   * <p><b>Expected:</b> 20
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>x = 10
   *   <li>Condition: x > 5 → true
   *   <li>Then-branch: x * 2 = 20
   * </ol>
   *
   * <p><b>Validates:</b> Let-bound variables can be used in conditional expressions.
   */
  @Test
  public void testLetInIfCondition() {
    assertInt("let x = 10 in if x > 5 then x * 2 else x endif", 20);
  }

  /**
   * Tests let-expression within the then-branch of an if-expression.
   *
   * <p><b>Input:</b> {@code if true then let x = 5 in x + 3 else 0 endif}
   *
   * <p><b>Expected:</b> 8
   *
   * <p><b>Scoping:</b> The let-expression creates a scope only within the then-branch.
   *
   * <p><b>Validates:</b> Let-expressions can be nested inside if-then-else branches.
   */
  @Test
  public void testLetInThenBranch() {
    assertInt("if true then let x = 5 in x + 3 else 0 endif", 8);
  }

  /**
   * Tests let-expression within the else-branch of an if-expression.
   *
   * <p><b>Input:</b> {@code if false then 0 else let x = 7 in x * 2 endif}
   *
   * <p><b>Expected:</b> 14
   *
   * <p><b>Validates:</b> Let-expressions work correctly in else-branches.
   */
  @Test
  public void testLetInElseBranch() {
    assertInt("if false then 0 else let x = 7 in x * 2 endif", 14);
  }

  // ==================== Complex Let Expressions ====================

  /**
   * Tests complex let-expression with nested let and collection operations.
   *
   * <p><b>Input:</b> {@code let x = 5 in let y = Set{1,2,3}.sum() in x + y}
   *
   * <p><b>Expected:</b> 11
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>x = 5
   *   <li>y = Set{1,2,3}.sum() = 6
   *   <li>Body: x + y = 11
   * </ol>
   *
   * <p><b>Validates:</b> Combining nested let-expressions with collection operations.
   */
  @Test
  public void testLetComplexExpression() {
    assertInt("let x = 5 in let y = Set{1,2,3}.sum() in x + y", 11);
  }

  /**
   * Tests let-expression with nested collections and union operation.
   *
   * <p><b>Input:</b> {@code let outer = Set{1,2} in let inner = Set{3,4} in
   * outer.union(inner).sum()}
   *
   * <p><b>Expected:</b> 10
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>outer = Set{1,2}
   *   <li>inner = Set{3,4}
   *   <li>outer.union(inner) → Set{1,2,3,4}
   *   <li>.sum() → 10
   * </ol>
   *
   * <p><b>Validates:</b> Complex collection operations with multiple bound collection variables.
   */
  @Test
  public void testLetWithNestedCollections() {
    assertInt("let outer = Set{1,2} in let inner = Set{3,4} in outer.union(inner).sum()", 10);
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL let-expression through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process with special handling for
   * let-expressions that require token stream access and scope management:
   *
   * <ol>
   *   <li><b>Phase 1 - Parsing:</b> Converts input to parse tree using {@code infixedExpCS} entry
   *       point
   *   <li><b>Phase 2 - Type Checking:</b> Validates variable bindings, checks type annotations,
   *       manages scopes
   *   <li><b>Phase 3 - Evaluation:</b> Evaluates bindings and body with proper scope
   *       creation/cleanup
   * </ol>
   *
   * <p><b>Token stream handling:</b> The token stream is filled and set for both type checker and
   * evaluator via {@code setTokenStream()} to enable proper parsing of the {@code in} keyword that
   * separates bindings from the body.
   *
   * <p><b>Scope management:</b> The evaluator maintains a scope stack:
   *
   * <ul>
   *   <li>Push new scope when entering let-expression
   *   <li>Bind variables in the new scope
   *   <li>Evaluate body with extended scope
   *   <li>Pop scope when exiting let-expression
   * </ul>
   *
   * @param input The OCL let-expression to compile and evaluate
   * @return The evaluated result as a {@link Value}
   */
  private Value compile(String input) {
    // Parse
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    tokens.fill();

    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // Create dummy specification (no metamodels needed for let-expression tests)
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

    // Pass 1: Symbol Table Construction (builds scopes for let-variables)
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    // Pass 2: Type Checking (with token stream for keyword detection)
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 2 (Type Checking) failed: " + errors.getErrors());
    }

    // Pass 3: Evaluation (with token stream and scope management)
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);

    Value result = evaluator.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 3 (Evaluation) failed: " + errors.getErrors());
    }

    return result;
  }

  /**
   * Assertion helper for Integer results.
   *
   * <p>Compiles the input expression, validates it produces a singleton Integer value, and asserts
   * the value matches the expected result.
   *
   * @param input The OCL expression to evaluate
   * @param expected The expected Integer value
   * @throws AssertionError if result is not singleton, not Integer, or value doesn't match
   */
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

  /**
   * Assertion helper for Boolean results.
   *
   * <p>Compiles the input expression, validates it produces a singleton Boolean value, and asserts
   * the value matches the expected result.
   *
   * @param input The OCL expression to evaluate
   * @param expected The expected Boolean value
   * @throws AssertionError if result is not singleton, not Boolean, or value doesn't match
   */
  private void assertBool(String input, boolean expected) {
    Value result = compile(input);
    assertEquals(1, result.size(), "Result should be singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(expected, ((OCLElement.BoolValue) elem).value());
  }

  /**
   * Assertion helper for String results.
   *
   * <p>Compiles the input expression, validates it produces a singleton String value, and asserts
   * the value matches the expected result.
   *
   * @param input The OCL expression to evaluate
   * @param expected The expected String value
   * @throws AssertionError if result is not singleton, not String, or value doesn't match
   */
  private void assertString(String input, String expected) {
    Value result = compile(input);
    assertEquals(1, result.size(), "Result should be singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(expected, ((OCLElement.StringValue) elem).value());
  }
}