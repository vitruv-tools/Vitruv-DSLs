/* ******************************************************************************
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

package tools.vitruv.dsls.vitruvocl.iterators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;

/**
 * Comprehensive test suite for iterator operations and lambda expressions.
 *
 * <h2>Iterator Operations Syntax</h2>
 *
 * <pre>{@code
 * collection.operation(variable | expression)
 * }</pre>
 *
 * <h2>Tested Iterator Operations</h2>
 *
 * <ul>
 *   <li><b>select(var | predicate):</b> Filters elements satisfying the predicate
 *   <li><b>reject(var | predicate):</b> Filters elements NOT satisfying the predicate
 *   <li><b>collect(var | expression):</b> Transforms each element
 *   <li><b>forAll(var | predicate):</b> Checks if all elements satisfy the predicate
 *   <li><b>exists(var | predicate):</b> Checks if at least one element satisfies the predicate
 * </ul>
 *
 * @see Value Runtime collection representation
 * @see tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor Evaluates iterator operations
 * @see tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor Type checks iterator expressions
 */
class IteratorTest extends DummyTestSpecification {

  // ==================== SELECT ====================

  /** Tests basic select: {@code Set{1,2,3,4,5}.select(x | x > 2)} → {3,4,5}. */
  @Test
  void testSelectBasic() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2)");
    assertSize(result, 3);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
  }

  @ParameterizedTest
  @MethodSource("emptyResultExpressions")
  void testEmptyResult(String expr) {
    Value result = compile(expr);
    assertTrue(result.isEmpty());
    assertSize(result, 0);
  }

  static Stream<String> emptyResultExpressions() {
    return Stream.of(
        "Set{1,2,3}.select(x | x > 10)",
        "Set{1,2,3}.reject(x | x > 0)",
        "Set{}.collect(x | x * 2)");
  }

  /** Tests select where all elements match → full collection. */
  @Test
  void testSelectAllMatch() {
    Value result = compile("Set{1,2,3}.select(x | x > 0)");
    assertSize(result, 3);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
  }

  /** Tests select with compound AND predicate: {@code x > 3 and x < 8} → {4,5,6,7}. */
  @Test
  void testSelectComplexPredicate() {
    Value result = compile("Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3 and x < 8)");
    assertSize(result, 4);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
    assertIncludes(result, 6);
    assertIncludes(result, 7);
  }

  /** Tests select with arithmetic in predicate: {@code x * 2 > 5} → {3,4,5}. */
  @Test
  void testSelectWithArithmetic() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x * 2 > 5)");
    assertSize(result, 3);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
  }

  /** Tests select on Sequence: {@code Sequence{5,2,8,1,9,3}.select(x | x > 4)} → {5,8,9}. */
  @Test
  void testSelectOnSequence() {
    Value result = compile("Sequence{5,2,8,1,9,3}.select(x | x > 4)");
    assertSize(result, 3);
    assertIncludes(result, 5);
    assertIncludes(result, 8);
    assertIncludes(result, 9);
  }

  // ==================== REJECT ====================

  /** Tests basic reject: {@code Set{1,2,3,4,5}.reject(x | x <= 2)} → {3,4,5}. */
  @Test
  void testRejectBasic() {
    Value result = compile("Set{1,2,3,4,5}.reject(x | x <= 2)");
    assertSize(result, 3);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
  }

  /** Tests reject where nothing is rejected → full collection. */
  @Test
  void testRejectNoneRejected() {
    Value result = compile("Set{1,2,3}.reject(x | x > 10)");
    assertSize(result, 3);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
  }

  /** Tests reject with equality: removes element 3 → {1,2,4,5}. */
  @Test
  void testRejectEquality() {
    Value result = compile("Set{1,2,3,4,5}.reject(x | x == 3)");
    assertSize(result, 4);
    assertExcludes(result, 3);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
  }

  /** Tests that select and reject are complementary for the same predicate. */
  @Test
  void testSelectVsReject() {
    Value selectResult = compile("Set{1,2,3,4,5}.select(x | x > 2)");
    Value rejectResult = compile("Set{1,2,3,4,5}.reject(x | x <= 2)");
    assertEquals(
        selectResult.size(),
        rejectResult.size(),
        "select(x | x > 2) and reject(x | x <= 2) should produce same size");
  }

  // ==================== COLLECT ====================

  /** Tests basic collect: {@code Set{1,2,3}.collect(x | x * 2)} → {2,4,6}. */
  @Test
  void testCollectBasic() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2)");
    assertSize(result, 3);
    assertIncludes(result, 2);
    assertIncludes(result, 4);
    assertIncludes(result, 6);
  }

  /** Tests collect with addition: {@code x + 10} → {11,12,13}. */
  @Test
  void testCollectWithAddition() {
    Value result = compile("Set{1,2,3}.collect(x | x + 10)");
    assertSize(result, 3);
    assertIncludes(result, 11);
    assertIncludes(result, 12);
    assertIncludes(result, 13);
  }

  /** Tests collect with complex expression: {@code x * 2 + x} = 3x → {3,6,9}. */
  @Test
  void testCollectComplexExpression() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2 + x)");
    assertSize(result, 3);
    assertIncludes(result, 3);
    assertIncludes(result, 6);
    assertIncludes(result, 9);
  }

  /** Tests collect with division: {@code x / 2} → {1,2,3,4,5}. */
  @Test
  void testCollectWithDivision() {
    Value result = compile("Set{2,4,6,8,10}.collect(x | x / 2)");
    assertSize(result, 5);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
  }

  /** Tests that collect auto-flattens results — no nested collections in output. */
  @Test
  void testCollectAutoFlatten() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2)");
    for (OCLElement elem : result.getElements()) {
      assertFalse(
          elem instanceof OCLElement.NestedCollection, "collect should auto-flatten results");
    }
  }

  // ==================== FORALL ====================

  /** Tests forAll where all elements satisfy: {@code x > 0} → {@code [true]}. */
  @Test
  void testForAllTrue() {
    assertSingleBool(compile("Set{1,2,3}.forAll(x | x > 0)"), true);
  }

  /** Tests forAll where some elements fail: {@code x > 2} → {@code [false]}. */
  @Test
  void testForAllFalse() {
    assertSingleBool(compile("Set{1,2,3}.forAll(x | x > 2)"), false);
  }

  /** Tests forAll on empty set → vacuously true. */
  @Test
  void testForAllOnEmptySet() {
    assertSingleBool(compile("Set{}.forAll(x | x > 100)"), true);
  }

  /** Tests forAll with compound predicate: {@code x > 0 and x < 10} → {@code [true]}. */
  @Test
  void testForAllComplexPredicate() {
    assertSingleBool(compile("Set{2,4,6,8}.forAll(x | x > 0 and x < 10)"), true);
  }

  /** Tests forAll with arithmetic: {@code x * 2 <= 10} → {@code [true]}. */
  @Test
  void testForAllWithArithmetic() {
    assertSingleBool(compile("Set{1,2,3,4,5}.forAll(x | x * 2 <= 10)"), true);
  }

  /** Tests forAll with equality on uniform set → {@code [true]}. */
  @Test
  void testForAllEquality() {
    assertSingleBool(compile("Set{5,5,5}.forAll(x | x == 5)"), true);
  }

  // ==================== EXISTS ====================

  /** Tests exists where one element satisfies: {@code x == 2} → {@code [true]}. */
  @Test
  void testExistsTrue() {
    assertSingleBool(compile("Set{1,2,3}.exists(x | x == 2)"), true);
  }

  /** Tests exists where no elements satisfy: {@code x > 10} → {@code [false]}. */
  @Test
  void testExistsFalse() {
    assertSingleBool(compile("Set{1,2,3}.exists(x | x > 10)"), false);
  }

  /** Tests exists on empty set → false. */
  @Test
  void testExistsOnEmptySet() {
    assertSingleBool(compile("Set{}.exists(x | x > 0)"), false);
  }

  /** Tests exists where multiple elements satisfy → {@code [true]}. */
  @Test
  void testExistsMultipleMatch() {
    assertSingleBool(compile("Set{1,2,3,4,5}.exists(x | x > 2)"), true);
  }

  /** Tests exists with compound predicate: element 4 satisfies {@code x > 3 and x < 5}. */
  @Test
  void testExistsComplexPredicate() {
    assertSingleBool(compile("Set{1,2,3,4,5}.exists(x | x > 3 and x < 5)"), true);
  }

  /** Tests exists with arithmetic: element 4 satisfies {@code x * 2 == 8}. */
  @Test
  void testExistsWithArithmetic() {
    assertSingleBool(compile("Set{1,2,3,4,5}.exists(x | x * 2 == 8)"), true);
  }

  // ==================== CHAINING ====================

  /** Tests select → collect: {1..5} → select x>2 → {3,4,5} → *2 → {6,8,10}. */
  @Test
  void testSelectThenCollect() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2).collect(x | x * 2)");
    assertSize(result, 3);
    assertIncludes(result, 6);
    assertIncludes(result, 8);
    assertIncludes(result, 10);
  }

  /** Tests collect → select: *2 → {2,4,6,8,10} → select x>5 → {6,8,10}. */
  @Test
  void testCollectThenSelect() {
    Value result = compile("Set{1,2,3,4,5}.collect(x | x * 2).select(x | x > 5)");
    assertSize(result, 3);
    assertIncludes(result, 6);
    assertIncludes(result, 8);
    assertIncludes(result, 10);
  }

  /** Tests select → reject: select x>3 → reject x>7 → {4,5,6,7}. */
  @Test
  void testSelectThenReject() {
    Value result = compile("Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).reject(x | x > 7)");
    assertSize(result, 4);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
    assertIncludes(result, 6);
    assertIncludes(result, 7);
  }

  /** Tests collect → forAll: *2 → {2,4,6} → forAll x>0 → true. */
  @Test
  void testCollectThenForAll() {
    assertSingleBool(compile("Set{1,2,3}.collect(x | x * 2).forAll(x | x > 0)"), true);
  }

  /** Tests select → exists: select x>2 → {3,4,5} → exists x==4 → true. */
  @Test
  void testSelectThenExists() {
    assertSingleBool(compile("Set{1,2,3,4,5}.select(x | x > 2).exists(x | x == 4)"), true);
  }

  /** Tests triple chaining: select x>3 → *2 → reject x>15 → {8,10,12,14}. */
  @Test
  void testTripleChaining() {
    Value result =
        compile(
            "Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).collect(x | x * 2).reject(x | x > 15)");
    assertSize(result, 4);
    assertIncludes(result, 8);
    assertIncludes(result, 10);
    assertIncludes(result, 12);
    assertIncludes(result, 14);
  }

  // ==================== WITH LET EXPRESSIONS ====================

  /**
   * Tests select with let threshold: {@code let threshold = 3 in ...select(x | x > threshold)} →
   * {4,5}.
   */
  @Test
  void testSelectWithLet() {
    Value result = compile("let threshold = 3 in Set{1,2,3,4,5}.select(x | x > threshold)");
    assertSize(result, 2);
    assertIncludes(result, 4);
    assertIncludes(result, 5);
  }

  /**
   * Tests collect with let multiplier: {@code let multiplier = 3 in ...collect(x | x * multiplier)}
   * → {3,6,9}.
   */
  @Test
  void testCollectWithLet() {
    Value result = compile("let multiplier = 3 in Set{1,2,3}.collect(x | x * multiplier)");
    assertSize(result, 3);
    assertIncludes(result, 3);
    assertIncludes(result, 6);
    assertIncludes(result, 9);
  }

  /**
   * Tests forAll with let maxValue: {@code let maxValue = 10 in ...forAll(x | x < maxValue)} →
   * true.
   */
  @Test
  void testForAllWithLet() {
    assertSingleBool(compile("let maxValue = 10 in Set{1,2,3,4,5}.forAll(x | x < maxValue)"), true);
  }

  // ==================== EDGE CASES ====================

  /** Tests select on singleton (matching): {@code Set{42}.select(x | x > 40)} → {42}. */
  @Test
  void testSelectOnSingletonSet() {
    Value result = compile("Set{42}.select(x | x > 40)");
    assertSize(result, 1);
    assertIncludes(result, 42);
  }

  /** Tests collect on singleton: {@code Set{5}.collect(x | x * 2)} → {10}. */
  @Test
  void testCollectOnSingletonSet() {
    Value result = compile("Set{5}.collect(x | x * 2)");
    assertSize(result, 1);
    assertIncludes(result, 10);
  }

  /** Tests forAll on singleton: {@code Set{42}.forAll(x | x > 0)} → true. */
  @Test
  void testForAllOnSingletonSet() {
    assertSingleBool(compile("Set{42}.forAll(x | x > 0)"), true);
  }

  /** Tests exists on singleton: {@code Set{42}.exists(x | x == 42)} → true. */
  @Test
  void testExistsOnSingletonSet() {
    assertSingleBool(compile("Set{42}.exists(x | x == 42)"), true);
  }

  /** Tests select on range: {@code Set{1..10}.select(x | x > 5)} → {6,7,8,9,10}. */
  @Test
  void testSelectWithRange() {
    Value result = compile("Set{1..10}.select(x | x > 5)");
    assertSize(result, 5);
    assertIncludes(result, 6);
    assertIncludes(result, 7);
    assertIncludes(result, 8);
    assertIncludes(result, 9);
    assertIncludes(result, 10);
  }

  /** Tests collect on range with squaring: {@code Set{1..5}.collect(x | x * x)} → {1,4,9,16,25}. */
  @Test
  void testCollectWithRange() {
    Value result = compile("Set{1..5}.collect(x | x * x)");
    assertSize(result, 5);
    assertIncludes(result, 1);
    assertIncludes(result, 4);
    assertIncludes(result, 9);
    assertIncludes(result, 16);
    assertIncludes(result, 25);
  }
}
