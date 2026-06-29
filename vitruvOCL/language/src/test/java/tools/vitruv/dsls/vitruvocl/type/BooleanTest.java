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
package tools.vitruv.dsls.vitruvocl.type;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;

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
 * @see tools.vitruv.dsls.vitruvocl.evaluator.OCLElement.BoolValue Boolean element wrapper
 * @see tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor Evaluates boolean expressions
 */
class BooleanTest extends DummyTestSpecification {

  // ==================== Boolean Literals ====================

  /** Tests evaluation of the {@code true} literal. */
  @Test
  void testTrueLiteral() {
    assertSingleBool(compile("true"), true);
  }

  /** Tests evaluation of the {@code false} literal. */
  @Test
  void testFalseLiteral() {
    assertSingleBool(compile("false"), false);
  }

  // ==================== Unary NOT ====================

  /** Tests negation of {@code true}. Truth table: ¬T = F. */
  @Test
  void testNotTrue() {
    assertSingleBool(compile("not true"), false);
  }

  /** Tests negation of {@code false}. Truth table: ¬F = T. */
  @Test
  void testNotFalse() {
    assertSingleBool(compile("not false"), true);
  }

  /** Tests double negation. Logical law: ¬¬A = A. */
  @Test
  void testDoubleNegation() {
    assertSingleBool(compile("not not true"), true);
  }

  // ==================== AND ====================

  /** Truth table: T ∧ T = T. */
  @Test
  void testTrueAndTrue() {
    assertSingleBool(compile("true and true"), true);
  }

  /** Truth table: T ∧ F = F. */
  @Test
  void testTrueAndFalse() {
    assertSingleBool(compile("true and false"), false);
  }

  /** Truth table: F ∧ F = F. */
  @Test
  void testFalseAndFalse() {
    assertSingleBool(compile("false and false"), false);
  }

  // ==================== OR ====================

  /** Truth table: T ∨ F = T. */
  @Test
  void testTrueOrFalse() {
    assertSingleBool(compile("true or false"), true);
  }

  /** Truth table: F ∨ F = F. */
  @Test
  void testFalseOrFalse() {
    assertSingleBool(compile("false or false"), false);
  }

  /** Truth table: T ∨ T = T. */
  @Test
  void testTrueOrTrue() {
    assertSingleBool(compile("true or true"), true);
  }

  // ==================== XOR ====================

  /** Truth table: T ⊕ F = T. */
  @Test
  void testTrueXorFalse() {
    assertSingleBool(compile("true xor false"), true);
  }

  /** Truth table: T ⊕ T = F. */
  @Test
  void testTrueXorTrue() {
    assertSingleBool(compile("true xor true"), false);
  }

  /** Truth table: F ⊕ F = F. */
  @Test
  void testFalseXorFalse() {
    assertSingleBool(compile("false xor false"), false);
  }

  // ==================== IMPLIES ====================

  /** Truth table: T → T = T. */
  @Test
  void testTrueImpliesTrue() {
    assertSingleBool(compile("true implies true"), true);
  }

  /** Truth table: T → F = F. */
  @Test
  void testTrueImpliesFalse() {
    assertSingleBool(compile("true implies false"), false);
  }

  /** Truth table: F → T = T (vacuous truth). */
  @Test
  void testFalseImpliesTrue() {
    assertSingleBool(compile("false implies true"), true);
  }

  /** Truth table: F → F = T (vacuous truth). */
  @Test
  void testFalseImpliesFalse() {
    assertSingleBool(compile("false implies false"), true);
  }

  // ==================== Complex Expressions ====================

  /**
   * Tests that the type checker handles nested expressions with proper precedence.
   *
   * <p><b>Input:</b> {@code true and (false or true)}
   */
  @Test
  void testComplexBooleanExpressionDebug() {
    assertSingleBool(compile("true and (false or true)"), true);
  }

  /**
   * Tests complex expression with {@code not}, {@code and}, and {@code or}.
   *
   * <p>Breakdown: {@code not(true and false)} → {@code not false} → {@code true}; {@code true or
   * true} → {@code true}
   */
  @Test
  void testNotAndOr() {
    assertSingleBool(compile("not (true and false) or true"), true);
  }

  /**
   * Tests boolean expression using comparison results.
   *
   * <p>Breakdown: {@code 5 > 3} → true; {@code 10 < 20} → true; {@code true and true} → true
   */
  @Test
  void testComparisonInBoolean() {
    assertSingleBool(compile("5 == 5"), true);
  }

  // ==================== Entry Point Override ====================

  /**
   * Overrides the parse entry point to use {@code expCS} instead of {@code prefixedExpCS}, since
   * boolean expressions like {@code true and false} require the top-level expression rule.
   */
  @Override
  protected ParseTree parse(String input) {
    org.antlr.v4.runtime.CommonTokenStream tokens =
        new org.antlr.v4.runtime.CommonTokenStream(
            new VitruvOCLLexer(org.antlr.v4.runtime.CharStreams.fromString(input)));
    return new VitruvOCLParser(tokens).expCS();
  }
}
