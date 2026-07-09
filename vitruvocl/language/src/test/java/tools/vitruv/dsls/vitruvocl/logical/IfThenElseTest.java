/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.logical;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for if-then-else conditional expressions in VitruvOCL.
 *
 * <h2>OCL Conditional Syntax</h2>
 *
 * <pre>{@code
 * if <condition> then <then-expression> else <else-expression> endif
 * }</pre>
 *
 * @see Value Runtime value representation
 * @see Type Type system representation
 * @see tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor Evaluates if-then-else expressions
 * @see tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor Type checks conditional branches
 */
class IfThenElseTest extends DummyTestSpecification {

  // ==================== Basic If-Then-Else Tests ====================

  /**
   * Tests true condition selects then-branch: {@code if true then 1 else 2 endif} → {@code [1]}.
   */
  @Test
  void testSimpleIfThenElse_TrueBranch() {
    assertSingleInt(compile("if true then 1 else 2 endif"), 1);
  }

  /**
   * Tests false condition selects else-branch: {@code if false then 1 else 2 endif} → {@code [2]}.
   */
  @Test
  void testSimpleIfThenElse_FalseBranch() {
    assertSingleInt(compile("if false then 1 else 2 endif"), 2);
  }

  /** Tests comparison as condition: {@code if 5 > 3 then 10 else 20 endif} → {@code [10]}. */
  @Test
  void testIfThenElse_BooleanCondition() {
    assertSingleInt(compile("if 5 > 3 then 10 else 20 endif"), 10);
  }

  /**
   * Tests arithmetic condition: {@code if (3 + 2) == 5 then 100 else 200 endif} → {@code [100]}.
   */
  @Test
  void testIfThenElse_ComplexCondition() {
    assertSingleInt(compile("if (3 + 2) == 5 then 100 else 200 endif"), 100);
  }

  /**
   * Tests false comparison selects else-branch: {@code if 5 > 10 then 100 else 200 endif} → {@code
   * [200]}.
   */
  @Test
  void testIfThenElse_FalseCondition() {
    assertSingleInt(compile("if 5 > 10 then 100 else 200 endif"), 200);
  }

  // ==================== Type Checking Tests ====================

  /** Tests type inference for Integer branches. */
  @Test
  void testIfThenElse_TypeCheck_Integer() {
    assertEquals(Type.INTEGER, typeCheck("if true then 1 else 2 endif"));
  }

  /** Tests type inference for Boolean branches. */
  @Test
  void testIfThenElse_TypeCheck_Boolean() {
    assertEquals(Type.BOOLEAN, typeCheck("if true then true else false endif"));
  }

  /** Tests type inference for String branches. */
  @Test
  void testIfThenElse_TypeCheck_String() {
    assertEquals(Type.STRING, typeCheck("if true then \"hello\" else \"world\" endif"));
  }

  /** Tests Boolean branches with true condition → {@code [true]}. */
  @Test
  void testIfThenElse_BooleanBranches() {
    assertSingleBool(compile("if true then true else false endif"), true);
  }

  /** Tests String branches with true condition → {@code ["hello"]}. */
  @Test
  void testIfThenElse_StringBranches() {
    assertSingleString(compile("if true then \"hello\" else \"world\" endif"), "hello");
  }

  /** Tests String branches with false condition → {@code ["world"]}. */
  @Test
  void testIfThenElse_StringBranches_FalseBranch() {
    assertSingleString(compile("if false then \"hello\" else \"world\" endif"), "world");
  }

  // ==================== Nested If-Then-Else Tests ====================

  /** Tests nested if in then-branch: outer true, inner true → {@code [1]}. */
  @Test
  void testNestedIfThenElse_InThenBranch() {
    assertSingleInt(compile("if true then (if true then 1 else 2 endif) else 3 endif"), 1);
  }

  /** Tests nested if in else-branch: outer false, inner true → {@code [2]}. */
  @Test
  void testNestedIfThenElse_InElseBranch() {
    assertSingleInt(compile("if false then 1 else (if true then 2 else 3 endif) endif"), 2);
  }

  /** Tests nested if as condition: inner true → outer then-branch → {@code [10]}. */
  @Test
  void testNestedIfThenElse_InCondition() {
    assertSingleInt(compile("if (if true then true else false endif) then 10 else 20 endif"), 10);
  }

  /** Debug test: type-checks nested if in condition without evaluation. */
  @Test
  void testNestedIfThenElse_InCondition_Debug() {
    typeCheck("if (if true then true else false endif) then 10 else 20 endif");
  }

  /** Tests 3 levels of nesting: true → false → true → {@code [2]}. */
  @Test
  void testDeeplyNestedIfThenElse() {
    assertSingleInt(
        compile(
            "if true then (if false then 1 else (if true then 2 else 3 endif) endif) else 4 endif"),
        2);
  }

  /** Tests 3 levels all false → deepest else → {@code [4]}. */
  @Test
  void testDeeplyNestedIfThenElse_AllFalse() {
    assertSingleInt(
        compile(
            "if false then 1 else (if false then 2 else (if false then 3 else 4 endif) endif)"
                + " endif"),
        4);
  }

  // ==================== If-Then-Else with Operations ====================

  /**
   * Tests arithmetic in then-branch: {@code if true then (5 + 3) else (10 * 2) endif} → {@code
   * [8]}.
   */
  @Test
  void testIfThenElse_ArithmeticInBranches() {
    assertSingleInt(compile("if true then (5 + 3) else (10 * 2) endif"), 8);
  }

  /**
   * Tests arithmetic in else-branch: {@code if false then (5 + 3) else (10 * 2) endif} → {@code
   * [20]}.
   */
  @Test
  void testIfThenElse_ArithmeticInBranches_FalseBranch() {
    assertSingleInt(compile("if false then (5 + 3) else (10 * 2) endif"), 20);
  }

  /**
   * Tests AND in condition (false): {@code if (true and false) then 1 else 2 endif} → {@code [2]}.
   */
  @Test
  void testIfThenElse_BooleanOperationsInCondition() {
    assertSingleInt(compile("if (true and false) then 1 else 2 endif"), 2);
  }

  /**
   * Tests AND in condition (true): {@code if (true and true) then 1 else 2 endif} → {@code [1]}.
   */
  @Test
  void testIfThenElse_BooleanOperationsInCondition_True() {
    assertSingleInt(compile("if (true and true) then 1 else 2 endif"), 1);
  }

  /** Tests OR in condition: {@code if (false or true) then 10 else 20 endif} → {@code [10]}. */
  @Test
  void testIfThenElse_OrInCondition() {
    assertSingleInt(compile("if (false or true) then 10 else 20 endif"), 10);
  }

  /**
   * Tests implies in condition (false): {@code if (true implies false) then 10 else 20 endif} →
   * {@code [20]}.
   */
  @Test
  void testIfThenElse_ImpliesInCondition() {
    assertSingleInt(compile("if (true implies false) then 10 else 20 endif"), 20);
  }

  /**
   * Tests implies in condition (vacuous truth): {@code if (false implies true) then 10 else 20
   * endif} → {@code [10]}.
   */
  @Test
  void testIfThenElse_ImpliesInCondition_TrueCase() {
    assertSingleInt(compile("if (false implies true) then 10 else 20 endif"), 10);
  }

  /**
   * Tests comparison in condition (true): {@code if (10 >= 5) then "yes" else "no" endif} → {@code
   * ["yes"]}.
   */
  @Test
  void testIfThenElse_ComparisonInCondition() {
    assertSingleString(compile("if (10 >= 5) then \"yes\" else \"no\" endif"), "yes");
  }

  /**
   * Tests comparison in condition (false): {@code if (10 <= 5) then "yes" else "no" endif} → {@code
   * ["no"]}.
   */
  @Test
  void testIfThenElse_ComparisonInCondition_False() {
    assertSingleString(compile("if (10 <= 5) then \"yes\" else \"no\" endif"), "no");
  }

  // ==================== If-Then-Else with Collections ====================

  /** Tests collection in then-branch (true): returns {1,2,3}. */
  @Test
  void testIfThenElse_CollectionBranches() {
    Value result = compile("if true then Set{1, 2, 3} else Set{4, 5, 6} endif");
    assertSize(result, 3);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
  }

  /** Tests collection in else-branch (false): returns {4,5,6}. */
  @Test
  void testIfThenElse_CollectionBranches_FalseBranch() {
    Value result = compile("if false then Set{1, 2, 3} else Set{4, 5, 6} endif");
    assertSize(result, 3);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
    assertIncludes(result, 6);
  }

  /** Tests collection operation in then-branch: {@code Set{1,2}.including(3)} → 3 elements. */
  @Test
  void testIfThenElse_CollectionOperation() {
    assertSize(compile("if true then Set{1, 2}.including(3) else Set{5, 6} endif"), 3);
  }

  /** Tests collection includes() in condition (true): → {@code [100]}. */
  @Test
  void testSimpleIfThenElseWithOperation() {
    assertSingleInt(compile("if Set{1, 2}.includes(1) then 100 else 200 endif"), 100);
  }

  /** Tests collection includes() in condition (true): → {@code [100]}. */
  @Test
  void testIfThenElse_CollectionOperationInCondition() {
    assertSingleInt(compile("if Set{1, 2}.includes(1) then 100 else 200 endif"), 100);
  }

  /** Tests collection includes() in condition (false): → {@code [200]}. */
  @Test
  void testIfThenElse_CollectionOperationInCondition_False() {
    assertSingleInt(compile("if Set{1, 2}.includes(5) then 100 else 200 endif"), 200);
  }

  /** Tests size() > 2 in condition (true): → {@code ["large"]}. */
  @Test
  void testIfThenElse_CollectionSizeInCondition() {
    assertSingleString(
        compile("if Sequence{1, 2, 3}.size() > 2 then \"large\" else \"small\" endif"), "large");
  }

  /** Tests size() > 2 in condition (false): → {@code ["small"]}. */
  @Test
  void testIfThenElse_CollectionSizeInCondition_False() {
    assertSingleString(
        compile("if Sequence{1}.size() > 2 then \"large\" else \"small\" endif"), "small");
  }

  /** Tests isEmpty() in condition (true): → {@code [1]}. */
  @Test
  void testIfThenElse_EmptyCollectionInCondition() {
    assertSingleInt(compile("if Set{}.isEmpty() then 1 else 2 endif"), 1);
  }

  /** Tests notEmpty() in condition (true): → {@code [1]}. */
  @Test
  void testIfThenElse_NotEmptyCollectionInCondition() {
    assertSingleInt(compile("if Set{1, 2}.notEmpty() then 1 else 2 endif"), 1);
  }

  // ==================== Edge Cases ====================

  /** Tests {@code not false} in condition → {@code [1]}. */
  @Test
  void testIfThenElse_UnaryNotInCondition() {
    assertSingleInt(compile("if not false then 1 else 2 endif"), 1);
  }

  /** Tests {@code not true} in condition → {@code [2]}. */
  @Test
  void testIfThenElse_UnaryNotInCondition_False() {
    assertSingleInt(compile("if not true then 1 else 2 endif"), 2);
  }

  /** Tests unary minus in then-branch: {@code if true then -5 else -10 endif} → {@code [-5]}. */
  @Test
  void testIfThenElse_UnaryMinusInBranches() {
    assertSingleInt(compile("if true then -5 else -10 endif"), -5);
  }

  /** Tests unary minus in else-branch: {@code if false then -5 else -10 endif} → {@code [-10]}. */
  @Test
  void testIfThenElse_UnaryMinusInBranches_FalseBranch() {
    assertSingleInt(compile("if false then -5 else -10 endif"), -10);
  }

  /** Tests empty collection in else-branch (false): → {@code [1]}. */
  @Test
  void testIfThenElse_EmptyCollectionBranches() {
    assertSingleInt(compile("if false then Set{} else Set{1} endif"), 1);
  }

  /** Tests empty collection in then-branch (true): → empty. */
  @Test
  void testIfThenElse_EmptyCollectionBranches_TrueBranch() {
    Value result = compile("if true then Set{} else Set{1} endif");
    assertSize(result, 0);
    assertTrue(result.isEmpty());
  }

  /** Tests multiple expressions in condition: last expression (2==2) used → {@code [10]}. */
  @Test
  void testIfThenElse_MultipleExpressionsInCondition() {
    assertSingleInt(compile("if (1 + 1 2 == 2) then 10 else 20 endif"), 10);
  }

  /** Tests multiple expressions in then-branch: last value (3) used → {@code [3]}. */
  @Test
  void testIfThenElse_MultipleExpressionsInBranches() {
    assertSingleInt(compile("if true then (1 2 3) else (4 5 6) endif"), 3);
  }

  /** Tests multiple expressions in else-branch: last value (6) used → {@code [6]}. */
  @Test
  void testIfThenElse_MultipleExpressionsInBranches_FalseBranch() {
    assertSingleInt(compile("if false then (1 2 3) else (4 5 6) endif"), 6);
  }

  // ==================== Complex Real-World-Like Tests ====================

  /** Tests grade B: 85 >= 90 false, 85 >= 80 true → {@code ["B"]}. */
  @Test
  void testIfThenElse_GradeCalculation() {
    assertSingleString(
        compile("if 85 >= 90 then \"A\" else (if 85 >= 80 then \"B\" else \"C\" endif) endif"),
        "B");
  }

  /** Tests grade A: 95 >= 90 → {@code ["A"]}. */
  @Test
  void testIfThenElse_GradeCalculation_A() {
    assertSingleString(
        compile("if 95 >= 90 then \"A\" else (if 95 >= 80 then \"B\" else \"C\" endif) endif"),
        "A");
  }

  /** Tests grade C: 70 < 80 → {@code ["C"]}. */
  @Test
  void testIfThenElse_GradeCalculation_C() {
    assertSingleString(
        compile("if 70 >= 90 then \"A\" else (if 70 >= 80 then \"B\" else \"C\" endif) endif"),
        "C");
  }

  /** Tests max(10, 20): 10 > 20 false → {@code [20]}. */
  @Test
  void testIfThenElse_MaxFunction() {
    assertSingleInt(compile("if 10 > 20 then 10 else 20 endif"), 20);
  }

  /** Tests max(30, 20): 30 > 20 true → {@code [30]}. */
  @Test
  void testIfThenElse_MaxFunction_FirstLarger() {
    assertSingleInt(compile("if 30 > 20 then 30 else 20 endif"), 30);
  }

  /** Tests abs(-5): -5 < 0 true → {@code [5]}. */
  @Test
  void testIfThenElse_AbsoluteValue() {
    assertSingleInt(compile("if -5 < 0 then (-1 * -5) else -5 endif"), 5);
  }

  /** Tests abs(5): 5 < 0 false → {@code [5]}. */
  @Test
  void testIfThenElse_AbsoluteValue_Positive() {
    assertSingleInt(compile("if 5 < 0 then (-1 * 5) else 5 endif"), 5);
  }

  /** Tests conditional collection extension: size > 0 → including(4) → {1,2,3,4}. */
  @Test
  void testIfThenElse_CollectionFiltering() {
    Value result =
        compile("if Set{1, 2, 3}.size() > 0 then Set{1, 2, 3}.including(4) else Set{} endif");
    assertSize(result, 4);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
  }

  /** Tests conditional collection extension: empty input → empty output. */
  @Test
  void testIfThenElse_CollectionFiltering_EmptyCase() {
    Value result = compile("if Set{}.size() > 0 then Set{1, 2, 3}.including(4) else Set{} endif");
    assertSize(result, 0);
    assertTrue(result.isEmpty());
  }

  /** Tests collection union in then-branch: → {1,2,3,4}. */
  @Test
  void testIfThenElse_CollectionUnion() {
    Value result = compile("if true then Set{1, 2}.union(Set{3, 4}) else Set{5, 6} endif");
    assertSize(result, 4);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
  }

  // ==================== Compiler Infrastructure ====================

  @Override
  protected Value compile(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    MetamodelWrapperInterface dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Pass 2 (Type checking) failed: " + typeChecker.getErrorCollector().getErrors());
    }

    tokens.seek(0);

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);
    Value result = evaluator.visit(tree);

    if (evaluator.hasErrors()) {
      fail("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  private Type typeCheck(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();

    MetamodelWrapperInterface dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.setTokenStream(tokens);
    Type result = typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Pass 2 (Type checking) failed: " + typeChecker.getErrorCollector().getErrors());
    }

    return result;
  }
}
