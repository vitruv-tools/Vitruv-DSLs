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
package tools.vitruv.dsls.vitruvocl.arithmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;

/**
 * Comprehensive test suite for arithmetic and comparison operations in VitruvOCL.
 *
 * @see Value Runtime value representation
 * @see tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor Evaluates arithmetic expressions
 * @see tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor Type checks arithmetic expressions
 */
class SimpleMathTest extends DummyTestSpecification {

  // ==================== Arithmetic Operations ====================

  /** Tests addition: {@code 1+2} → {@code [3]}. */
  @Test
  void testOnePlusTwo() {
    assertSingleInt(compile("1+2"), 3);
  }

  /** Tests subtraction: {@code 5-3} → {@code [2]}. */
  @Test
  void testFiveMinusThree() {
    assertSingleInt(compile("5-3"), 2);
  }

  /** Tests multiplication: {@code 4*5} → {@code [20]}. */
  @Test
  void testMultiplication() {
    assertSingleInt(compile("4*5"), 20);
  }

  /** Tests division: {@code 20/4} → {@code [5.0]} (Real result). */
  @Test
  void testDivision() {
    assertSingleReal(compile("20/4"), 5.0);
  }

  /** Tests chained operations: {@code 10+20-5} → {@code [25]}. */
  @Test
  void testChainedOperations() {
    assertSingleInt(compile("10+20-5"), 25);
  }

  /** Tests operator precedence: {@code 2+3*4} → {@code [14]} (multiplication first). */
  @Test
  void testOperatorPrecedence() {
    assertSingleInt(compile("2+3*4"), 14);
  }

  /** Tests unary minus: {@code -5} → {@code [-5]}. */
  @Test
  void testUnaryMinus() {
    assertSingleInt(compile("-5"), -5);
  }

  /** Tests unary minus in expression: {@code 10 + -5} → {@code [5]}. */
  @Test
  void testUnaryMinusInExpression() {
    assertSingleInt(compile("10 + -5"), 5);
  }

  // ==================== Comparison Operations ====================

  /** Tests less-than (true): {@code 3 < 5} → {@code [true]}. */
  @Test
  void testLessThan() {
    assertSingleBool(compile("3 < 5"), true);
  }

  /** Tests less-than (false): {@code 5 < 3} → {@code [false]}. */
  @Test
  void testLessThanFalse() {
    assertSingleBool(compile("5 < 3"), false);
  }

  /** Tests less-than-or-equal (equality case): {@code 3 <= 3} → {@code [true]}. */
  @Test
  void testLessThanOrEqual() {
    assertSingleBool(compile("3 <= 3"), true);
  }

  /** Tests greater-than (true): {@code 10 > 5} → {@code [true]}. */
  @Test
  void testGreaterThan() {
    assertSingleBool(compile("10 > 5"), true);
  }

  /** Tests greater-than-or-equal (equality case): {@code 5 >= 5} → {@code [true]}. */
  @Test
  void testGreaterThanOrEqual() {
    assertSingleBool(compile("5 >= 5"), true);
  }

  /** Tests real division in comparison: {@code 331/2 <= 165} → {@code [false]}. */
  @Test
  void testRealDivisionInComparison() {
    assertSingleBool(compile("331/2 <= 165"), false);
  }

  /** Tests equality (true): {@code 5 == 5} → {@code [true]}. */
  @Test
  void testEquality() {
    assertSingleBool(compile("5 == 5"), true);
  }

  /** Tests inequality (true): {@code 5 != 3} → {@code [true]}. */
  @Test
  void testInequality() {
    assertSingleBool(compile("5 != 3"), true);
  }

  /** Tests inequality (false): {@code 5 != 5} → {@code [false]}. */
  @Test
  void testInequalityFalse() {
    assertSingleBool(compile("5 != 5"), false);
  }

  // ==================== Collection Arithmetic Operations ====================

  /** Tests sum() on various collections. */
  @Test
  void testSum() {
    assertSingleInt(compile("Set{1,2,3,4,5}.sum()"), 15);
    assertSingleInt(compile("Set{10,20,30}.sum()"), 60);
    assertSingleInt(compile("Sequence{1,2,3}.sum()"), 6);
  }

  /** Tests sum() on empty collection → {@code [0]}. */
  @Test
  void testSumEmptyCollection() {
    assertSingleInt(compile("Set{}.sum()"), 0);
  }

  /** Tests max() on various collections. */
  @Test
  void testMax() {
    assertSingleInt(compile("Set{1,5,3,9,2}.max()"), 9);
    assertSingleInt(compile("Sequence{100,50,200,75}.max()"), 200);
    assertSingleInt(compile("Set{-5,-10,-1}.max()"), -1);
  }

  /** Tests max() on Real collections. */
  @Test
  void testMaxReal() {
    assertSingleReal(compile("Bag{1.5, 3.0, 2.7}.max()"), 3.0);
    assertSingleReal(compile("Sequence{-1.1, -0.5, -2.8}.max()"), -0.5);
  }

  /** Tests max() on empty collection → empty. */
  @Test
  void testMaxEmptyCollection() {
    assertSize(compile("Set{}.max()"), 0);
  }

  /** Tests min() on various collections. */
  @Test
  void testMin() {
    assertSingleInt(compile("Set{1,5,3,9,2}.min()"), 1);
    assertSingleInt(compile("Sequence{100,50,200,75}.min()"), 50);
    assertSingleInt(compile("Set{-5,-10,-1}.min()"), -10);
  }

  /** Tests min() on Real collections. */
  @Test
  void testMinReal() {
    assertSingleReal(compile("Bag{1.5, 3.0, 2.7}.min()"), 1.5);
    assertSingleReal(compile("Sequence{-1.1, -0.5, -2.8}.min()"), -2.8);
  }

  /** Tests min() on empty collection → empty. */
  @Test
  void testMinEmptyCollection() {
    assertSize(compile("Set{}.min()"), 0);
  }

  /** Tests avg() on various collections. */
  @Test
  void testAvg() {
    assertSingleDouble(compile("Set{1,2,3,4,5}.avg()"), 3.0);
    assertSingleDouble(compile("Sequence{10,20,30}.avg()"), 20.0);
    assertSingleDouble(compile("Set{100,200}.avg()"), 150.0);
  }

  /** Tests avg() on empty collection → empty. */
  @Test
  void testAvgEmptyCollection() {
    assertSize(compile("Set{}.avg()"), 0);
  }

  /** Tests abs() element-wise transformation. */
  @Test
  void testAbs() {
    assertCollection(compile("Set{-1,-2,-3}.collect(x | x.abs())"), 1, 2, 3);
    assertCollection(compile("Sequence{-5,10,-15}.collect(x | x.abs())"), 5, 10, 15);
    assertCollection(compile("Set{1,2,3}..collect(x | x.abs())"), 1, 2, 3);
  }

  /** Tests floor() on integers (no-op). */
  @Test
  void testFloor() {
    assertCollection(compile("Set{1,2,3}.collect(x | x.floor())"), 1, 2, 3);
    assertCollection(compile("Sequence{10,20,30}.collect(x | x.floor())"), 10, 20, 30);
  }

  /** Tests ceil() on integers (no-op). */
  @Test
  void testCeil() {
    assertCollection(compile("Set{1,2,3}.collect(x | x.ceil())"), 1, 2, 3);
    assertCollection(compile("Sequence{10,20,30}.collect(x | x.ceil())"), 10, 20, 30);
  }

  /** Tests round() on integers (no-op). */
  @Test
  void testRound() {
    assertCollection(compile("Set{1,2,3}.collect(x | x.round())"), 1, 2, 3);
    assertCollection(compile("Sequence{10,20,30}.collect(x | x.round())"), 10, 20, 30);
  }

  /** Tests lift(): {@code Set{1,2,3}.lift()} → singleton containing collection of size 3. */
  @Test
  void testLift() {
    Value result = compile("Set{1,2,3}.lift()");
    assertSize(result, 1);
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.NestedCollection, "Element should be NestedCollection");
    assertEquals(
        3,
        ((OCLElement.NestedCollection) elem).value().size(),
        "Inner collection should have 3 elements");
  }

  /** Tests lift().flatten() is identity: → {1,2,3}. */
  @Test
  void testLiftThenFlatten() {
    assertCollection(compile("Set{1,2,3}.lift().flatten()"), 1, 2, 3);
  }

  /** Tests arithmetic chaining across aggregate results. */
  @Test
  void testArithmeticChaining() {
    assertSingleInt(compile("Set{1,2,3,4,5}.sum() + Set{10,20}.sum()"), 45);
    assertSingleInt(compile("Set{10,20,30}.max() - Set{1,2,3}.min()"), 29);
  }

  /** Tests complex combinations of transformations and aggregations. */
  @Test
  void testComplexArithmetic() {
    assertSingleInt(compile("Set{-5,-10,15,20}.collect(x | x.abs()).max()"), 20);
    assertSingleInt(compile("Set{-1,-2,-3}.collect(x | x.abs()).sum()"), 6);
    assertSingleReal(compile("Sequence{5,10,15,20}.sum() / Set{2,4}.max()"), 12.5);
  }

  // ==================== Entry Point Override ====================

  /** Overrides parse entry point to use {@code infixedExpCS()} for arithmetic expressions. */
  @Override
  protected ParseTree parse(String input) {
    CommonTokenStream tokens = new CommonTokenStream(new VitruvOCLLexer(CharStreams.fromString(input)));
    return new VitruvOCLParser(tokens).infixedExpCS();
  }
}
