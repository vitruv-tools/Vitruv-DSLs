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
package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
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
 * @see Value Runtime value representation
 * @see tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor Evaluates let-expressions
 * @see tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor Type checks let-expressions
 */
public class LetExpressionTest extends DummyTestSpecification {

  // ==================== Simple Let Expressions ====================

  /** Tests basic let: {@code let x = 5 in x + 3} → 8. */
  @Test
  public void testSimpleLet() {
    assertSingleInt(compile("let x = 5 in x + 3"), 8);
  }

  /** Tests let with multiplication: {@code let x = 10 in x * 2} → 20. */
  @Test
  public void testLetWithArithmetic() {
    assertSingleInt(compile("let x = 10 in x * 2"), 20);
  }

  /** Tests let with type annotation: {@code let x : Integer = 15 in x - 5} → 10. */
  @Test
  public void testLetWithTypeAnnotation() {
    assertSingleInt(compile("let x : Integer = 15 in x - 5"), 10);
  }

  /** Tests variable used multiple times: {@code let x = 4 in x + x + x} → 12. */
  @Test
  public void testLetUseVariableMultipleTimes() {
    assertSingleInt(compile("let x = 4 in x + x + x"), 12);
  }

  // ==================== Multiple Variables ====================

  /** Tests two variables: {@code let x = 5, y = 3 in x + y} → 8. */
  @Test
  public void testLetMultipleVariables() {
    assertSingleInt(compile("let x = 5, y = 3 in x + y"), 8);
  }

  /** Tests three variables: {@code let x = 10, y = 20, z = 5 in x + y - z} → 25. */
  @Test
  public void testLetMultipleVariablesComplex() {
    assertSingleInt(compile("let x = 10, y = 20, z = 5 in x + y - z"), 25);
  }

  /** Tests sequential dependency: {@code let x = 5, y = x * 2 in y + 3} → 13. */
  @Test
  public void testLetSequentialDependency() {
    assertSingleInt(compile("let x = 5, y = x * 2 in y + 3"), 13);
  }

  // ==================== Nested Let ====================

  /** Tests nested let: {@code let x = 5 in let y = x * 2 in y + 3} → 13. */
  @Test
  public void testNestedLet() {
    assertSingleInt(compile("let x = 5 in let y = x * 2 in y + 3"), 13);
  }

  /** Tests 3-level nesting: x=2, y=x*3=6, z=y+1=7, z*2 → 14. */
  @Test
  public void testNestedLetDeep() {
    assertSingleInt(compile("let x = 2 in let y = x * 3 in let z = y + 1 in z * 2"), 14);
  }

  /** Tests variable shadowing: inner x=10 shadows outer x=5 → 10. */
  @Test
  public void testLetShadowing() {
    assertSingleInt(compile("let x = 5 in let x = 10 in x"), 10);
  }

  /** Tests shadowing with arithmetic: outer x=5, inner x=x*2=10, x+1 → 11. */
  @Test
  public void testLetShadowingWithArithmetic() {
    assertSingleInt(compile("let x = 5 in let x = x * 2 in x + 1"), 11);
  }

  /** Tests inner scope accessing outer variable: y=x+2=7, y*2 → 14. */
  @Test
  public void testLetAccessOuterVariable() {
    assertSingleInt(compile("let x = 5 in let y = x + 2 in y * 2"), 14);
  }

  // ==================== Let with Collections ====================

  /** Tests let binding Set: {@code let nums = Set{1,2,3} in nums.sum()} → 6. */
  @Test
  public void testLetWithSet() {
    assertSingleInt(compile("let nums = Set{1,2,3} in nums.sum()"), 6);
  }

  /** Tests let binding Sequence: {@code let seq = Sequence{5,10,15} in seq.max()} → 15. */
  @Test
  public void testLetWithSequence() {
    assertSingleInt(compile("let seq = Sequence{5,10,15} in seq.max()"), 15);
  }

  /** Tests chained collection ops on bound variable: {@code s.including(4).sum()} → 10. */
  @Test
  public void testLetCollectionOperations() {
    assertSingleInt(compile("let s = Set{1,2,3} in s.including(4).sum()"), 10);
  }

  /** Tests collection then derived value: nums.sum()=6, total*2 → 12. */
  @Test
  public void testLetWithCollectionVariable() {
    assertSingleInt(compile("let nums = Set{1,2,3}, total = nums.sum() in total * 2"), 12);
  }

  // ==================== Let with Booleans ====================

  /** Tests let binding Boolean: {@code let flag = true in flag and false} → false. */
  @Test
  public void testLetWithBoolean() {
    assertSingleBool(compile("let flag = true in flag and false"), false);
  }

  /** Tests let with comparison: {@code let x = 10, y = 5 in x > y} → true. */
  @Test
  public void testLetWithComparison() {
    assertSingleBool(compile("let x = 10, y = 5 in x > y"), true);
  }

  // ==================== Let with Strings ====================

  /** Tests let binding String: {@code let s = "hello" in s.concat(" world")} → "hello world". */
  @Test
  public void testLetWithString() {
    assertSingleString(compile("let s = \"hello\" in s.concat(\" world\")"), "hello world");
  }

  /** Tests String operation on bound variable: {@code let s = "HELLO" in s.toLower()} → "hello". */
  @Test
  public void testLetStringOperations() {
    assertSingleString(compile("let s = \"HELLO\" in s.toLower()"), "hello");
  }

  // ==================== Let with If-Then-Else ====================

  /** Tests let variable in if-condition: x=10, x>5 true → x*2 → 20. */
  @Test
  public void testLetInIfCondition() {
    assertSingleInt(compile("let x = 10 in if x > 5 then x * 2 else x endif"), 20);
  }

  /** Tests let inside then-branch: {@code if true then let x = 5 in x + 3 else 0 endif} → 8. */
  @Test
  public void testLetInThenBranch() {
    assertSingleInt(compile("if true then let x = 5 in x + 3 else 0 endif"), 8);
  }

  /** Tests let inside else-branch: {@code if false then 0 else let x = 7 in x * 2 endif} → 14. */
  @Test
  public void testLetInElseBranch() {
    assertSingleInt(compile("if false then 0 else let x = 7 in x * 2 endif"), 14);
  }

  // ==================== Complex Let Expressions ====================

  /** Tests nested let with collection: x=5, y=Set{1,2,3}.sum()=6, x+y → 11. */
  @Test
  public void testLetComplexExpression() {
    assertSingleInt(compile("let x = 5 in let y = Set{1,2,3}.sum() in x + y"), 11);
  }

  /** Tests two collection variables with union: outer.union(inner).sum() → 10. */
  @Test
  public void testLetWithNestedCollections() {
    assertSingleInt(
        compile("let outer = Set{1,2} in let inner = Set{3,4} in outer.union(inner).sum()"), 10);
  }

  // ==================== Compiler Infrastructure ====================

  /**
   * Overrides compile() to use {@code infixedExpCS()} entry point with {@code tokens.fill()} and
   * token stream injection for let-expression keyword detection.
   */
  @Override
  protected Value compile(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    tokens.fill();

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

    if (errors.hasErrors()) {
      fail("Pass 2 (Type Checking) failed: " + errors.getErrors());
    }

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);
    Value result = evaluator.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 3 (Evaluation) failed: " + errors.getErrors());
    }

    return result;
  }
}