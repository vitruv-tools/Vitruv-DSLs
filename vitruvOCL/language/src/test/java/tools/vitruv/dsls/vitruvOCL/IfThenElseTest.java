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
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintSpecification;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Test suite for if-then-else expressions in OCL#.
 *
 * <p>Tests both type checking and evaluation of conditional expressions.
 */
public class IfThenElseTest {

  // ==================== Basic If-Then-Else Tests ====================

  @Test
  public void testSimpleIfThenElse_TrueBranch() {
    String input = "if true then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertTrue(result.getElements().get(0) instanceof OCLElement.IntValue);
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testSimpleIfThenElse_FalseBranch() {
    String input = "if false then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_BooleanCondition() {
    String input = "if 5 > 3 then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_ComplexCondition() {
    String input = "if (3 + 2) == 5 then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(100, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_FalseCondition() {
    String input = "if 5 > 10 then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(200, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== Type Checking Tests ====================

  @Test
  public void testIfThenElse_TypeCheck_Integer() {
    String input = "if true then 1 else 2 endif";
    Type type = typeCheck(input);

    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIfThenElse_TypeCheck_Boolean() {
    String input = "if true then true else false endif";
    Type type = typeCheck(input);

    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testIfThenElse_TypeCheck_String() {
    String input = "if true then \"hello\" else \"world\" endif";
    Type type = typeCheck(input);

    assertEquals(Type.STRING, type);
  }

  @Test
  public void testIfThenElse_BooleanBranches() {
    String input = "if true then true else false endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_StringBranches() {
    String input = "if true then \"hello\" else \"world\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("hello", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_StringBranches_FalseBranch() {
    String input = "if false then \"hello\" else \"world\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("world", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  // ==================== Nested If-Then-Else Tests ====================

  @Test
  public void testNestedIfThenElse_InThenBranch() {
    String input = "if true then (if true then 1 else 2 endif) else 3 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testNestedIfThenElse_InElseBranch() {
    String input = "if false then 1 else (if true then 2 else 3 endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testNestedIfThenElse_InCondition() {
    String input = "if (if true then true else false endif) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testNestedIfThenElse_InCondition_Debug() {
    String input = "if (if true then true else false endif) then 10 else 20 endif";

    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // Dummy specification
    ConstraintSpecification dummySpec =
        new ConstraintSpecification() {
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

  @Test
  public void testDeeplyNestedIfThenElse() {
    String input =
        "if true then (if false then 1 else (if true then 2 else 3 endif) endif) else 4 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testDeeplyNestedIfThenElse_AllFalse() {
    String input =
        "if false then 1 else (if false then 2 else (if false then 3 else 4 endif) endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(4, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== If-Then-Else with Operations ====================

  @Test
  public void testIfThenElse_ArithmeticInBranches() {
    String input = "if true then (5 + 3) else (10 * 2) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(8, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_ArithmeticInBranches_FalseBranch() {
    String input = "if false then (5 + 3) else (10 * 2) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(20, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_BooleanOperationsInCondition() {
    String input = "if (true and false) then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_BooleanOperationsInCondition_True() {
    String input = "if (true and true) then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_OrInCondition() {
    String input = "if (false or true) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_ImpliesInCondition() {
    String input = "if (true implies false) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(20, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_ImpliesInCondition_TrueCase() {
    String input = "if (false implies true) then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_ComparisonInCondition() {
    String input = "if (10 >= 5) then \"yes\" else \"no\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("yes", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_ComparisonInCondition_False() {
    String input = "if (10 <= 5) then \"yes\" else \"no\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("no", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  // ==================== If-Then-Else with Collections ====================

  @Test
  public void testIfThenElse_CollectionBranches() {
    String input = "if true then Set{1, 2, 3} else Set{4, 5, 6} endif";
    Value result = compile(input);

    assertEquals(3, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  @Test
  public void testIfThenElse_CollectionBranches_FalseBranch() {
    String input = "if false then Set{1, 2, 3} else Set{4, 5, 6} endif";
    Value result = compile(input);

    assertEquals(3, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
  }

  @Test
  public void testIfThenElse_CollectionOperation() {
    String input = "if true then Set{1, 2}.including(3) else Set{5, 6} endif";
    Value result = compile(input);

    assertEquals(3, result.size());
  }

  @Test
  public void testSimpleIfThenElseWithOperation() {
    // Dieser sollte funktionieren - Operation in Condition
    String input1 = "if Set{1, 2}.includes(1) then 100 else 200 endif";
    Value result1 = compile(input1);
    assertEquals(100, ((OCLElement.IntValue) result1.getElements().get(0)).value());

    // Dieser sollte auch funktionieren - einfache Then-Branch
    String input2 = "if true then 100 else 200 endif";
    Value result2 = compile(input2);
    assertEquals(100, ((OCLElement.IntValue) result2.getElements().get(0)).value());

    // Dieser sollte NICHT funktionieren - Operation im Then-Branch
    String input3 = "if true then Set{1, 2}.including(3) else Set{5} endif";
    // Was passiert hier?
  }

  @Test
  public void testIfThenElse_CollectionOperationInCondition() {
    String input = "if Set{1, 2}.includes(1) then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(100, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_CollectionOperationInCondition_False() {
    String input = "if Set{1, 2}.includes(5) then 100 else 200 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(200, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_CollectionSizeInCondition() {
    String input = "if Sequence{1, 2, 3}.size() > 2 then \"large\" else \"small\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("large", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_CollectionSizeInCondition_False() {
    String input = "if Sequence{1}.size() > 2 then \"large\" else \"small\" endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("small", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_EmptyCollectionInCondition() {
    String input = "if Set{}.isEmpty() then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_NotEmptyCollectionInCondition() {
    String input = "if Set{1, 2}.notEmpty() then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== Edge Cases ====================

  @Test
  public void testIfThenElse_UnaryNotInCondition() {
    String input = "if not false then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_UnaryNotInCondition_False() {
    String input = "if not true then 1 else 2 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(2, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_UnaryMinusInBranches() {
    String input = "if true then -5 else -10 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(-5, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_UnaryMinusInBranches_FalseBranch() {
    String input = "if false then -5 else -10 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(-10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_EmptyCollectionBranches() {
    String input = "if false then Set{} else Set{1} endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_EmptyCollectionBranches_TrueBranch() {
    String input = "if true then Set{} else Set{1} endif";
    Value result = compile(input);

    assertEquals(0, result.size());
    assertTrue(result.isEmpty());
  }

  @Test
  public void testIfThenElse_MultipleExpressionsInCondition() {
    String input = "if (1 + 1 2 == 2) then 10 else 20 endif";
    Value result = compile(input);

    // Multiple expressions: last one (2 == 2 = true) is used
    assertEquals(1, result.size());
    assertEquals(10, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_MultipleExpressionsInBranches() {
    String input = "if true then (1 2 3) else (4 5 6) endif";
    Value result = compile(input);

    // Multiple expressions: last one (3) is used
    assertEquals(1, result.size());
    assertEquals(3, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_MultipleExpressionsInBranches_FalseBranch() {
    String input = "if false then (1 2 3) else (4 5 6) endif";
    Value result = compile(input);

    // Multiple expressions: last one (6) is used
    assertEquals(1, result.size());
    assertEquals(6, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  // ==================== Complex Real-World-Like Tests ====================

  @Test
  public void testIfThenElse_GradeCalculation() {
    String input = "if 85 >= 90 then \"A\" else (if 85 >= 80 then \"B\" else \"C\" endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("B", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_GradeCalculation_A() {
    String input = "if 95 >= 90 then \"A\" else (if 95 >= 80 then \"B\" else \"C\" endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("A", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_GradeCalculation_C() {
    String input = "if 70 >= 90 then \"A\" else (if 70 >= 80 then \"B\" else \"C\" endif) endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals("C", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_MaxFunction() {
    String input = "if 10 > 20 then 10 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(20, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_MaxFunction_FirstLarger() {
    String input = "if 30 > 20 then 30 else 20 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(30, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_AbsoluteValue() {
    String input = "if -5 < 0 then (-1 * -5) else -5 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(5, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIfThenElse_AbsoluteValue_Positive() {
    String input = "if 5 < 0 then (-1 * 5) else 5 endif";
    Value result = compile(input);

    assertEquals(1, result.size());
    assertEquals(5, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

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

  @Test
  public void testIfThenElse_CollectionFiltering_EmptyCase() {
    String input = "if Set{}.size() > 0 then Set{1, 2, 3}.including(4) else Set{} endif";
    Value result = compile(input);

    assertEquals(0, result.size());
    assertTrue(result.isEmpty());
  }

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

  @Test
  public void testIfThenElse_CollectionUnion_Debug() {
    String input = "if true then Set{1, 2}.union(Set{3, 4}) else Set{5, 6} endif";

    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // Dummy specification
    ConstraintSpecification dummySpec =
        new ConstraintSpecification() {
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

    tokens.seek(0);

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);
    Value result = evaluator.visit(tree);

    // Now test the assertions
    assertEquals(4, result.size());
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
  }

  // ==================== Helper Methods ====================

  private Value compile(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // Dummy specification
    ConstraintSpecification dummySpec =
        new ConstraintSpecification() {
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
    typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Reset token stream for evaluator
    tokens.seek(0);

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);
    Value result = evaluator.visit(tree);

    if (evaluator.hasErrors()) {
      fail("Evaluation failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  private Type typeCheck(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    // Dummy specification
    ConstraintSpecification dummySpec =
        new ConstraintSpecification() {
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
    Type result = typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Type checking failed: " + typeChecker.getErrorCollector().getErrors());
    }

    return result;
  }
}