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
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for if-then-else conditional expressions in VitruvOCL.
 *
 * <p>This test class validates the complete implementation of OCL conditional control flow, testing
 * both type checking and runtime evaluation of if-then-else expressions through the full
 * compilation pipeline.
 *
 * <h2>OCL Conditional Syntax</h2>
 *
 * <pre>{@code
 * if <condition> then <then-expression> else <else-expression> endif
 * }</pre>
 *
 * <p>Where:
 *
 * <ul>
 *   <li><b>condition:</b> Expression evaluating to Boolean (singleton)
 *   <li><b>then-expression:</b> Expression evaluated if condition is true
 *   <li><b>else-expression:</b> Expression evaluated if condition is false
 *   <li><b>endif:</b> Required terminator keyword
 * </ul>
 *
 * @see Value Runtime value representation
 * @see Type Type system representation
 * @see EvaluationVisitor Evaluates if-then-else expressions
 * @see TypeCheckVisitor Type checks conditional branches
 */
public class IfThenElseTest {

  // ==================== Basic If-Then-Else Tests ====================

  /**
   * Tests basic if-then-else with true condition selecting then-branch.
   *
   * <p><b>Input:</b> {@code if true then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [1]} (then-branch)
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic conditional syntax parsing
   *   <li>True condition selects then-branch
   *   <li>Else-branch is not evaluated (lazy evaluation)
   * </ul>
   */
  @Test
  public void testSimpleIfThenElse_TrueBranch() {
    String input = "if true then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertTrue(result.getElements().get(0) instanceof OCLElement.IntValue);
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests basic if-then-else with false condition selecting else-branch.
   *
   * <p><b>Input:</b> {@code if false then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [2]} (else-branch)
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>False condition selects else-branch
   *   <li>Then-branch is not evaluated (lazy evaluation)
   * </ul>
   */
  @Test
  public void testSimpleIfThenElse_FalseBranch() {
    String input = "if false then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with comparison expression as condition.
   *
   * <p><b>Input:</b> {@code if 5 > 3 then 10 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [10]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>{@code 5 > 3} → {@code [true]}
   *   <li>Condition is true → evaluate then-branch
   *   <li>Result: {@code [10]}
   * </ol>
   */
  @Test
  public void testIfThenElse_BooleanCondition() {
    String input = "if 5 > 3 then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with complex arithmetic condition.
   *
   * <p><b>Input:</b> {@code if (3 + 2) == 5 then 100 else 200 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [100]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>{@code 3 + 2} → {@code [5]}
   *   <li>{@code [5] == 5} → {@code [true]}
   *   <li>Result: {@code [100]}
   * </ol>
   */
  @Test
  public void testIfThenElse_ComplexCondition() {
    String input = "if (3 + 2) == 5 then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(100, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with false comparison condition.
   *
   * <p><b>Input:</b> {@code if 5 > 10 then 100 else 200 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [200]} (else-branch)
   *
   * <p><b>Validates:</b> Comparison evaluates to false, selecting else-branch.
   */
  @Test
  public void testIfThenElse_FalseCondition() {
    String input = "if 5 > 10 then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(200, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== Type Checking Tests ====================

  /**
   * Tests type inference for if-then-else with Integer branches.
   *
   * <p><b>Input:</b> {@code if true then 1 else 2 endif}
   *
   * <p><b>Expected Type:</b> {@code Collection(Integer, 1, 1)} (singleton Integer)
   *
   * <p><b>Type checking logic:</b> Both branches are Integer, so the unified type is Integer.
   */
  @Test
  public void testIfThenElse_TypeCheck_Integer() {
    String input = "if true then 1 else 2 endif";
    Type type = typeCheck(input);

    assertEquals(Type.INTEGER, type);
  }

  /**
   * Tests type inference for if-then-else with Boolean branches.
   *
   * <p><b>Input:</b> {@code if true then true else false endif}
   *
   * <p><b>Expected Type:</b> {@code Collection(Boolean, 1, 1)} (singleton Boolean)
   */
  @Test
  public void testIfThenElse_TypeCheck_Boolean() {
    String input = "if true then true else false endif";
    Type type = typeCheck(input);

    assertEquals(Type.BOOLEAN, type);
  }

  /**
   * Tests type inference for if-then-else with String branches.
   *
   * <p><b>Input:</b> {@code if true then "hello" else "world" endif}
   *
   * <p><b>Expected Type:</b> {@code Collection(String, 1, 1)} (singleton String)
   */
  @Test
  public void testIfThenElse_TypeCheck_String() {
    String input = "if true then \"hello\" else \"world\" endif";
    Type type = typeCheck(input);

    assertEquals(Type.STRING, type);
  }

  /**
   * Tests evaluation of if-then-else with Boolean branches (true condition).
   *
   * <p><b>Input:</b> {@code if true then true else false endif}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   */
  @Test
  public void testIfThenElse_BooleanBranches() {
    String input = "if true then true else false endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /**
   * Tests evaluation of if-then-else with String branches (true condition).
   *
   * <p><b>Input:</b> {@code if true then "hello" else "world" endif}
   *
   * <p><b>Expected:</b> Singleton {@code ["hello"]}
   */
  @Test
  public void testIfThenElse_StringBranches() {
    String input = "if true then \"hello\" else \"world\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("hello", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  /**
   * Tests evaluation of if-then-else with String branches (false condition).
   *
   * <p><b>Input:</b> {@code if false then "hello" else "world" endif}
   *
   * <p><b>Expected:</b> Singleton {@code ["world"]} (else-branch)
   */
  @Test
  public void testIfThenElse_StringBranches_FalseBranch() {
    String input = "if false then \"hello\" else \"world\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("world", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  // ==================== Nested If-Then-Else Tests ====================

  /**
   * Tests nested if-then-else in the then-branch.
   *
   * <p><b>Input:</b> {@code if true then (if true then 1 else 2 endif) else 3 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [1]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Outer condition {@code true} → evaluate then-branch
   *   <li>Inner condition {@code true} → evaluate inner then-branch
   *   <li>Result: {@code [1]}
   * </ol>
   *
   * <p><b>Validates:</b> Proper nesting and scope management for nested conditionals.
   */
  @Test
  public void testNestedIfThenElse_InThenBranch() {
    String input = "if true then (if true then 1 else 2 endif) else 3 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests nested if-then-else in the else-branch.
   *
   * <p><b>Input:</b> {@code if false then 1 else (if true then 2 else 3 endif) endif}
   *
   * <p><b>Expected:</b> Singleton {@code [2]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Outer condition {@code false} → evaluate else-branch
   *   <li>Inner condition {@code true} → evaluate inner then-branch
   *   <li>Result: {@code [2]}
   * </ol>
   */
  @Test
  public void testNestedIfThenElse_InElseBranch() {
    String input = "if false then 1 else (if true then 2 else 3 endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests nested if-then-else in the condition.
   *
   * <p><b>Input:</b> {@code if (if true then true else false endif) then 10 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [10]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Inner condition {@code true} → {@code [true]}
   *   <li>Outer condition uses result {@code [true]} → evaluate then-branch
   *   <li>Result: {@code [10]}
   * </ol>
   *
   * <p><b>Validates:</b> Conditional expressions can be used as condition values themselves.
   */
  @Test
  public void testNestedIfThenElse_InCondition() {
    String input = "if (if true then true else false endif) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Debug test for nested if-then-else in condition.
   *
   * <p><b>Purpose:</b> Validates type checking for complex nested conditionals without full
   * evaluation. Used for debugging token stream handling.
   *
   * <p><b>Input:</b> {@code if (if true then true else false endif) then 10 else 20 endif}
   */
  @Test
  public void testNestedIfThenElse_InCondition_Debug() {
    String input = "if (if true then true else false endif) then 10 else 20 endif";

    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
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

    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.setTokenStream(tokens);
    Type type = typeChecker.visit(tree);
  }

  /**
   * Tests deeply nested if-then-else (3 levels deep).
   *
   * <p><b>Input:</b> {@code if true then (if false then 1 else (if true then 2 else 3 endif) endif)
   * else 4 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [2]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>Level 1: {@code true} → evaluate then-branch
   *   <li>Level 2: {@code false} → evaluate else-branch
   *   <li>Level 3: {@code true} → evaluate then-branch → {@code [2]}
   * </ol>
   */
  @Test
  public void testDeeplyNestedIfThenElse() {
    String input =
        "if true then (if false then 1 else (if true then 2 else 3 endif) endif) else 4 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests deeply nested if-then-else with all false conditions.
   *
   * <p><b>Input:</b> {@code if false then 1 else (if false then 2 else (if false then 3 else 4
   * endif) endif) endif}
   *
   * <p><b>Expected:</b> Singleton {@code [4]} (deepest else-branch)
   *
   * <p><b>Validates:</b> All conditions are false, so the deepest else-branch is selected.
   */
  @Test
  public void testDeeplyNestedIfThenElse_AllFalse() {
    String input =
        "if false then 1 else (if false then 2 else (if false then 3 else 4 endif) endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(4, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== If-Then-Else with Operations ====================

  /**
   * Tests if-then-else with arithmetic operations in branches (true condition).
   *
   * <p><b>Input:</b> {@code if true then (5 + 3) else (10 * 2) endif}
   *
   * <p><b>Expected:</b> Singleton {@code [8]}
   *
   * <p><b>Validates:</b> Arithmetic operations are evaluated within the selected branch.
   */
  @Test
  public void testIfThenElse_ArithmeticInBranches() {
    String input = "if true then (5 + 3) else (10 * 2) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(8, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with arithmetic operations in branches (false condition).
   *
   * <p><b>Input:</b> {@code if false then (5 + 3) else (10 * 2) endif}
   *
   * <p><b>Expected:</b> Singleton {@code [20]}
   *
   * <p><b>Validates:</b> Lazy evaluation - only the else-branch is computed.
   */
  @Test
  public void testIfThenElse_ArithmeticInBranches_FalseBranch() {
    String input = "if false then (5 + 3) else (10 * 2) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(20, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with boolean AND operation in condition.
   *
   * <p><b>Input:</b> {@code if (true and false) then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [2]}
   *
   * <p><b>Logic:</b> {@code true and false} → {@code false} → else-branch
   */
  @Test
  public void testIfThenElse_BooleanOperationsInCondition() {
    String input = "if (true and false) then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with boolean AND operation in condition (true case).
   *
   * <p><b>Input:</b> {@code if (true and true) then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [1]}
   *
   * <p><b>Logic:</b> {@code true and true} → {@code true} → then-branch
   */
  @Test
  public void testIfThenElse_BooleanOperationsInCondition_True() {
    String input = "if (true and true) then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with boolean OR operation in condition.
   *
   * <p><b>Input:</b> {@code if (false or true) then 10 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [10]}
   *
   * <p><b>Logic:</b> {@code false or true} → {@code true} → then-branch
   */
  @Test
  public void testIfThenElse_OrInCondition() {
    String input = "if (false or true) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with implication in condition (false case).
   *
   * <p><b>Input:</b> {@code if (true implies false) then 10 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [20]}
   *
   * <p><b>Logic:</b> {@code true implies false} → {@code false} (only false case for implication) →
   * else-branch
   */
  @Test
  public void testIfThenElse_ImpliesInCondition() {
    String input = "if (true implies false) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(20, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with implication in condition (true case).
   *
   * <p><b>Input:</b> {@code if (false implies true) then 10 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [10]}
   *
   * <p><b>Logic:</b> {@code false implies true} → {@code true} (vacuous truth) → then-branch
   */
  @Test
  public void testIfThenElse_ImpliesInCondition_TrueCase() {
    String input = "if (false implies true) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with comparison operation in condition (true case).
   *
   * <p><b>Input:</b> {@code if (10 >= 5) then "yes" else "no" endif}
   *
   * <p><b>Expected:</b> Singleton {@code ["yes"]}
   */
  @Test
  public void testIfThenElse_ComparisonInCondition() {
    String input = "if (10 >= 5) then \"yes\" else \"no\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("yes", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with comparison operation in condition (false case).
   *
   * <p><b>Input:</b> {@code if (10 <= 5) then "yes" else "no" endif}
   *
   * <p><b>Expected:</b> Singleton {@code ["no"]}
   */
  @Test
  public void testIfThenElse_ComparisonInCondition_False() {
    String input = "if (10 <= 5) then \"yes\" else \"no\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("no", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  // ==================== If-Then-Else with Collections ====================

  /**
   * Tests if-then-else with collection literals in branches (true condition).
   *
   * <p><b>Input:</b> {@code if true then Set{1, 2, 3} else Set{4, 5, 6} endif}
   *
   * <p><b>Expected:</b> Collection with 3 elements: {1, 2, 3}
   *
   * <p><b>Validates:</b> Collections can be returned from conditional branches.
   */
  @Test
  public void testIfThenElse_CollectionBranches() {
    String input = "if true then Set{1, 2, 3} else Set{4, 5, 6} endif";
    Value result = compile(input);

    assertEquals(3, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  /**
   * Tests if-then-else with collection literals in branches (false condition).
   *
   * <p><b>Input:</b> {@code if false then Set{1, 2, 3} else Set{4, 5, 6} endif}
   *
   * <p><b>Expected:</b> Collection with 3 elements: {4, 5, 6}
   */
  @Test
  public void testIfThenElse_CollectionBranches_FalseBranch() {
    String input = "if false then Set{1, 2, 3} else Set{4, 5, 6} endif";
    Value result = compile(input);

    assertEquals(3, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
  }

  /**
   * Tests if-then-else with collection operation in then-branch.
   *
   * <p><b>Input:</b> {@code if true then Set{1, 2}.including(3) else Set{5, 6} endif}
   *
   * <p><b>Expected:</b> Collection with 3 elements: {1, 2, 3}
   *
   * <p><b>Validates:</b> Collection operations can be evaluated within branches.
   */
  @Test
  public void testIfThenElse_CollectionOperation() {
    String input = "if true then Set{1, 2}.including(3) else Set{5, 6} endif";
    Value result = compile(input);

    assertEquals(3, result.size());
  }

  /**
   * Tests multiple simple if-then-else scenarios with operations.
   *
   * <p><b>Scenario 1:</b> Operation in condition → {@code if Set{1, 2}.includes(1) then 100 else
   * 200 endif}
   *
   * <p><b>Scenario 2:</b> Simple literal branches → {@code if true then 100 else 200 endif}
   *
   * <p><b>Scenario 3:</b> Operation in then-branch → {@code if true then Set{1, 2}.including(3)
   * else Set{5} endif}
   *
   * <p><b>Purpose:</b> Validates different patterns of operation placement in conditionals.
   */
  @Test
  public void testSimpleIfThenElseWithOperation() {
    // Operation in condition - should work
    String input1 = "if Set{1, 2}.includes(1) then 100 else 200 endif";
    Value result1 = compile(input1);
    assertEquals(100, ((OCLElement.IntValue) result1.getElements().get(0)).value());

    // Simple branches - should work
    String input2 = "if true then 100 else 200 endif";
    Value result2 = compile(input2);
    assertEquals(100, ((OCLElement.IntValue) result2.getElements().get(0)).value());

    // Operation in then-branch - should work
    String input3 = "if true then Set{1, 2}.including(3) else Set{5} endif";
    // Validates that operations in branches are properly evaluated
  }

  /**
   * Tests if-then-else with collection operation in condition (true case).
   *
   * <p><b>Input:</b> {@code if Set{1, 2}.includes(1) then 100 else 200 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [100]}
   *
   * <p><b>Evaluation flow:</b>
   *
   * <ol>
   *   <li>{@code Set{1, 2}.includes(1)} → {@code [true]}
   *   <li>Condition true → then-branch → {@code [100]}
   * </ol>
   */
  @Test
  public void testIfThenElse_CollectionOperationInCondition() {
    String input = "if Set{1, 2}.includes(1) then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(100, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with collection operation in condition (false case).
   *
   * <p><b>Input:</b> {@code if Set{1, 2}.includes(5) then 100 else 200 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [200]}
   *
   * <p><b>Logic:</b> {@code Set{1, 2}.includes(5)} → {@code [false]} → else-branch
   */
  @Test
  public void testIfThenElse_CollectionOperationInCondition_False() {
    String input = "if Set{1, 2}.includes(5) then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(200, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with collection size comparison in condition (true case).
   *
   * <p><b>Input:</b> {@code if Sequence{1, 2, 3}.size() > 2 then "large" else "small" endif}
   *
   * <p><b>Expected:</b> Singleton {@code ["large"]}
   *
   * <p><b>Logic:</b> {@code size()} → {@code [3]}, {@code 3 > 2} → {@code [true]}
   */
  @Test
  public void testIfThenElse_CollectionSizeInCondition() {
    String input = "if Sequence{1, 2, 3}.size() > 2 then \"large\" else \"small\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("large", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with collection size comparison in condition (false case).
   *
   * <p><b>Input:</b> {@code if Sequence{1}.size() > 2 then "large" else "small" endif}
   *
   * <p><b>Expected:</b> Singleton {@code ["small"]}
   */
  @Test
  public void testIfThenElse_CollectionSizeInCondition_False() {
    String input = "if Sequence{1}.size() > 2 then \"large\" else \"small\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("small", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with isEmpty() check in condition.
   *
   * <p><b>Input:</b> {@code if Set{}.isEmpty() then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [1]}
   *
   * <p><b>Logic:</b> {@code Set{}.isEmpty()} → {@code [true]} → then-branch
   */
  @Test
  public void testIfThenElse_EmptyCollectionInCondition() {
    String input = "if Set{}.isEmpty() then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with notEmpty() check in condition.
   *
   * <p><b>Input:</b> {@code if Set{1, 2}.notEmpty() then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [1]}
   *
   * <p><b>Logic:</b> {@code Set{1, 2}.notEmpty()} → {@code [true]} → then-branch
   */
  @Test
  public void testIfThenElse_NotEmptyCollectionInCondition() {
    String input = "if Set{1, 2}.notEmpty() then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== Edge Cases ====================

  /**
   * Tests if-then-else with unary NOT in condition (true case).
   *
   * <p><b>Input:</b> {@code if not false then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [1]}
   *
   * <p><b>Logic:</b> {@code not false} → {@code [true]} → then-branch
   */
  @Test
  public void testIfThenElse_UnaryNotInCondition() {
    String input = "if not false then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with unary NOT in condition (false case).
   *
   * <p><b>Input:</b> {@code if not true then 1 else 2 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [2]}
   *
   * <p><b>Logic:</b> {@code not true} → {@code [false]} → else-branch
   */
  @Test
  public void testIfThenElse_UnaryNotInCondition_False() {
    String input = "if not true then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with unary minus in branches (true condition).
   *
   * <p><b>Input:</b> {@code if true then -5 else -10 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [-5]}
   *
   * <p><b>Validates:</b> Unary operators are evaluated within branches.
   */
  @Test
  public void testIfThenElse_UnaryMinusInBranches() {
    String input = "if true then -5 else -10 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(-5, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with unary minus in branches (false condition).
   *
   * <p><b>Input:</b> {@code if false then -5 else -10 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [-10]}
   */
  @Test
  public void testIfThenElse_UnaryMinusInBranches_FalseBranch() {
    String input = "if false then -5 else -10 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(-10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with empty collection in else-branch.
   *
   * <p><b>Input:</b> {@code if false then Set{} else Set{1} endif}
   *
   * <p><b>Expected:</b> Singleton collection {@code [1]}
   *
   * <p><b>Edge case:</b> Validates that non-empty collection is selected.
   */
  @Test
  public void testIfThenElse_EmptyCollectionBranches() {
    String input = "if false then Set{} else Set{1} endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with empty collection in then-branch.
   *
   * <p><b>Input:</b> {@code if true then Set{} else Set{1} endif}
   *
   * <p><b>Expected:</b> Empty collection
   *
   * <p><b>Edge case:</b> Validates that empty collections can be returned from conditionals.
   */
  @Test
  public void testIfThenElse_EmptyCollectionBranches_TrueBranch() {
    String input = "if true then Set{} else Set{1} endif";
    Value result = compile(input);

    assertEquals(0, result.size());
    assertTrue(result.isEmpty());
  }

  /**
   * Tests if-then-else with multiple expressions in condition.
   *
   * <p><b>Input:</b> {@code if (1 + 1 2 == 2) then 10 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [10]}
   *
   * <p><b>OCL semantics:</b> Multiple expressions in parentheses - the last expression ({@code 2 ==
   * 2} → {@code true}) is used as the condition value.
   */
  @Test
  public void testIfThenElse_MultipleExpressionsInCondition() {
    String input = "if (1 + 1 2 == 2) then 10 else 20 endif";
    Value result = compile(input);

    // Multiple expressions: last one (2 == 2 = true) is used
    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with multiple expressions in branches (true condition).
   *
   * <p><b>Input:</b> {@code if true then (1 2 3) else (4 5 6) endif}
   *
   * <p><b>Expected:</b> Singleton {@code [3]}
   *
   * <p><b>OCL semantics:</b> Multiple expressions - the last one (3) is the result value.
   */
  @Test
  public void testIfThenElse_MultipleExpressionsInBranches() {
    String input = "if true then (1 2 3) else (4 5 6) endif";
    Value result = compile(input);

    // Multiple expressions: last one (3) is used
    assertEquals(1, result.size());
    assertEquals(3, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests if-then-else with multiple expressions in branches (false condition).
   *
   * <p><b>Input:</b> {@code if false then (1 2 3) else (4 5 6) endif}
   *
   * <p><b>Expected:</b> Singleton {@code [6]}
   *
   * <p><b>OCL semantics:</b> Multiple expressions - the last one (6) is the result value.
   */
  @Test
  public void testIfThenElse_MultipleExpressionsInBranches_FalseBranch() {
    String input = "if false then (1 2 3) else (4 5 6) endif";
    Value result = compile(input);

    // Multiple expressions: last one (6) is used
    assertEquals(1, result.size());
    assertEquals(6, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== Complex Real-World-Like Tests ====================

  /**
   * Tests realistic grade calculation using nested conditionals.
   *
   * <p><b>Input:</b> {@code if 85 >= 90 then "A" else (if 85 >= 80 then "B" else "C" endif) endif}
   *
   * <p><b>Expected:</b> Singleton {@code ["B"]}
   *
   * <p><b>Grading logic:</b>
   *
   * <ul>
   *   <li>≥ 90: A
   *   <li>≥ 80: B
   *   <li>< 80: C
   * </ul>
   *
   * <p><b>Real-world pattern:</b> Cascading if-then-else for classification tasks.
   */
  @Test
  public void testIfThenElse_GradeCalculation() {
    String input = "if 85 >= 90 then \"A\" else (if 85 >= 80 then \"B\" else \"C\" endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("B", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  /**
   * Tests grade calculation with A grade.
   *
   * <p><b>Input:</b> Score 95 (≥ 90)
   *
   * <p><b>Expected:</b> {@code ["A"]}
   */
  @Test
  public void testIfThenElse_GradeCalculation_A() {
    String input = "if 95 >= 90 then \"A\" else (if 95 >= 80 then \"B\" else \"C\" endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("A", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  /**
   * Tests grade calculation with C grade.
   *
   * <p><b>Input:</b> Score 70 (< 80)
   *
   * <p><b>Expected:</b> {@code ["C"]}
   */
  @Test
  public void testIfThenElse_GradeCalculation_C() {
    String input = "if 70 >= 90 then \"A\" else (if 70 >= 80 then \"B\" else \"C\" endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("C", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  /**
   * Tests max function implementation using if-then-else.
   *
   * <p><b>Input:</b> {@code if 10 > 20 then 10 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [20]} (maximum value)
   *
   * <p><b>Pattern:</b> {@code max(a, b) = if a > b then a else b}
   */
  @Test
  public void testIfThenElse_MaxFunction() {
    String input = "if 10 > 20 then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(20, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests max function with first value larger.
   *
   * <p><b>Input:</b> {@code if 30 > 20 then 30 else 20 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [30]}
   */
  @Test
  public void testIfThenElse_MaxFunction_FirstLarger() {
    String input = "if 30 > 20 then 30 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(30, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests absolute value implementation using if-then-else.
   *
   * <p><b>Input:</b> {@code if -5 < 0 then (-1 * -5) else -5 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [5]}
   *
   * <p><b>Pattern:</b> {@code abs(x) = if x < 0 then -x else x}
   */
  @Test
  public void testIfThenElse_AbsoluteValue() {
    String input = "if -5 < 0 then (-1 * -5) else -5 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(5, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests absolute value with positive input.
   *
   * <p><b>Input:</b> {@code if 5 < 0 then (-1 * 5) else 5 endif}
   *
   * <p><b>Expected:</b> Singleton {@code [5]} (already positive)
   */
  @Test
  public void testIfThenElse_AbsoluteValue_Positive() {
    String input = "if 5 < 0 then (-1 * 5) else 5 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(5, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests conditional collection filtering pattern.
   *
   * <p><b>Input:</b> {@code if Set{1, 2, 3}.size() > 0 then Set{1, 2, 3}.including(4) else Set{}
   * endif}
   *
   * <p><b>Expected:</b> Collection {1, 2, 3, 4}
   *
   * <p><b>Pattern:</b> Conditional collection modification based on collection properties.
   */
  @Test
  public void testIfThenElse_CollectionFiltering() {
    String input = "if Set{1, 2, 3}.size() > 0 then Set{1, 2, 3}.including(4) else Set{} endif";
    Value result = compile(input);

    assertEquals(4, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
  }

  /**
   * Tests collection filtering with empty input.
   *
   * <p><b>Input:</b> {@code if Set{}.size() > 0 then Set{1, 2, 3}.including(4) else Set{} endif}
   *
   * <p><b>Expected:</b> Empty collection (condition is false)
   */
  @Test
  public void testIfThenElse_CollectionFiltering_EmptyCase() {
    String input = "if Set{}.size() > 0 then Set{1, 2, 3}.including(4) else Set{} endif";
    Value result = compile(input);

    assertEquals(0, result.size());
    assertTrue(result.isEmpty());
  }

  /**
   * Tests conditional collection union operation.
   *
   * <p><b>Input:</b> {@code if true then Set{1, 2}.union(Set{3, 4}) else Set{5, 6} endif}
   *
   * <p><b>Expected:</b> Collection {1, 2, 3, 4}
   *
   * <p><b>Validates:</b> Complex collection operations within branches.
   */
  @Test
  public void testIfThenElse_CollectionUnion() {
    String input = "if true then Set{1, 2}.union(Set{3, 4}) else Set{5, 6} endif";
    Value result = compile(input);

    assertEquals(4, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
  }

  /**
   * Debug test for collection union in if-then-else.
   *
   * <p><b>Purpose:</b> Low-level validation of token stream handling for complex collection
   * operations within conditionals. Tests type checking and evaluation separately.
   *
   * <p><b>Input:</b> {@code if true then Set{1, 2}.union(Set{3, 4}) else Set{5, 6} endif}
   */
  @Test
  public void testIfThenElse_CollectionUnion_Debug() {
    String input = "if true then Set{1, 2}.union(Set{3, 4}) else Set{5, 6} endif";

    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
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

    // Type check phase
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.setTokenStream(tokens);
    Type type = typeChecker.visit(tree);

    // Reset token stream for evaluation
    tokens.seek(0);

    // Evaluation phase
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);
    Value result = evaluator.visit(tree);

    // Verify results
    assertEquals(4, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL if-then-else expression through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process with special handling for
   * if-then-else expressions that require token stream access:
   *
   * <ol>
   *   <li><b>Phase 1 - Parsing:</b> Converts input to parse tree using {@code infixedExpCS} entry
   *       point
   *   <li><b>Phase 2 - Type Checking:</b> Validates condition and branch types, sets token stream
   *       for keyword detection
   *   <li><b>Phase 3 - Evaluation:</b> Evaluates the selected branch (lazy evaluation), resets
   *       token stream before evaluation
   * </ol>
   *
   * <p><b>Token stream handling:</b> The token stream must be set for both type checker and
   * evaluator via {@code setTokenStream()} to enable proper parsing of {@code then}, {@code else},
   * and {@code endif} keywords. After type checking, the token stream is reset to position 0 via
   * {@code tokens.seek(0)} before evaluation.
   *
   * <p><b>Error handling:</b> Fails immediately if type checking or evaluation encounters errors.
   *
   * @param input The OCL if-then-else expression to compile and evaluate
   * @return The evaluated result as a {@link Value}
   * @throws AssertionError if any compilation phase fails
   */
  private Value compile(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // Create dummy specification (no metamodels needed for if-then-else tests)
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

    // Phase 2: Type Checking (with token stream for keyword detection)
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Reset token stream for evaluator
    tokens.seek(0);

    // Phase 3: Evaluation (with reset token stream)
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);
    Value result = evaluator.visit(tree);

    if (evaluator.hasErrors()) {
      fail("Evaluation failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  /**
   * Type checks an OCL if-then-else expression and returns the inferred type.
   *
   * <p>This method performs only type checking (Phase 2) without evaluation. Used for validating
   * type inference rules for conditional expressions.
   *
   * <p><b>Type inference for if-then-else:</b> The result type is the unified type of both
   * branches. Both branches must have compatible types that can be unified (e.g., both Integer, or
   * both Collection(Integer)).
   *
   * <p><b>Token stream handling:</b> The token stream is set via {@code setTokenStream()} to enable
   * keyword detection during type checking.
   *
   * @param input The OCL if-then-else expression to type check
   * @return The inferred {@link Type} of the expression
   * @throws AssertionError if type checking fails
   */
  private Type typeCheck(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // Create dummy specification
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

    // Type check only
    TypeCheckVisitor typeChecker = new TypeCheckVisitor(symbolTable, dummySpec, errors);
    typeChecker.setTokenStream(tokens);
    Type result = typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    return result;
  }
}