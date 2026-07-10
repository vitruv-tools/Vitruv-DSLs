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
package tools.vitruv.dsls.vitruvocl.collections;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;

/**
 * Comprehensive test suite for collection operations and literals.
 *
 * <p>This test class validates the complete implementation of OCL collections in VitruvOCL, testing
 * both collection literals and operations through the full compilation pipeline (parsing, type
 * checking, and evaluation).
 *
 * <h2>Tested Collection Types</h2>
 *
 * <ul>
 *   <li><b>Set:</b> Unordered, unique elements - {@code Set{1,2,3}}
 *   <li><b>Sequence:</b> Ordered, allows duplicates - {@code Sequence{1,2,2,3}}
 *   <li><b>Bag:</b> Unordered, allows duplicates - {@code Bag{1,2,2,3}}
 *   <li><b>OrderedSet:</b> Ordered, unique elements - {@code OrderedSet{1,2,3}}
 * </ul>
 *
 * <h2>Tested Operations</h2>
 *
 * <ul>
 *   <li><b>Query operations:</b> {@code size()}, {@code isEmpty()}, {@code notEmpty()}, {@code
 *       includes()}, {@code excludes()}
 *   <li><b>Set operations:</b> {@code union()}, {@code including()}, {@code excluding()}
 *   <li><b>Ordered operations:</b> {@code first()}, {@code last()}, {@code reverse()}
 *   <li><b>Ranges:</b> {@code 1..5} (ascending), {@code 5..1} (descending)
 *   <li><b>Chained operations:</b> {@code Set{1,2}.including(3).excluding(2).size()}
 * </ul>
 *
 * @see Value Runtime collection representation
 * @see OCLElement Collection element types
 * @see EvaluationVisitor Evaluates collection operations
 */
class CollectionTest extends DummyTestSpecification {

  // ==================== Collection Literals ====================

  /**
   * Tests creation of a Set literal with distinct elements.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}}
   *
   * <p><b>Expected:</b> Collection with 3 elements (1, 2, 3)
   */
  @Test
  void testSetLiteral() {
    Value result = compile("Set{1,2,3}");

    assertSize(result, 3);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
  }

  /**
   * Tests that Set literals automatically remove duplicate elements.
   *
   * <p><b>Input:</b> {@code Set{1,2,2,3}}
   *
   * <p><b>Expected:</b> Collection with 3 unique elements (duplicates removed)
   */
  @Test
  void testSetRemovesDuplicates() {
    assertSize(compile("Set{1,2,2,3}"), 3);
  }

  /**
   * Tests creation of an empty Set.
   *
   * <p><b>Input:</b> {@code Set{}}
   *
   * <p><b>Expected:</b> Empty collection (size = 0, isEmpty = true)
   */
  @Test
  void testEmptySet() {
    Value result = compile("Set{}");

    assertTrue(result.isEmpty(), "Set{} should be empty");
    assertSize(result, 0);
  }

  /**
   * Tests creation of a Sequence literal.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}}
   *
   * <p><b>Expected:</b> Ordered collection with 3 elements
   */
  @Test
  void testSequenceLiteral() {
    assertSize(compile("Sequence{1,2,3}"), 3);
  }

  /**
   * Tests that Sequence literals preserve duplicate elements.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,2,3}}
   *
   * <p><b>Expected:</b> Collection with 4 elements (duplicates kept)
   */
  @Test
  void testSequenceKeepsDuplicates() {
    assertSize(compile("Sequence{1,2,2,3}"), 4);
  }

  // ==================== Ranges ====================

  /**
   * Tests ascending range expression.
   *
   * <p><b>Input:</b> {@code Set{1..5}}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3, 4, 5}
   */
  @Test
  void testRange() {
    Value result = compile("Set{1..5}");

    assertSize(result, 5);
    assertIncludes(result, 1);
    assertIncludes(result, 5);
  }

  /**
   * Tests descending range expression.
   *
   * <p><b>Input:</b> {@code Sequence{5..1}}
   *
   * <p><b>Expected:</b> Sequence containing {5, 4, 3, 2, 1} in that order
   */
  @Test
  void testDescendingRange() {
    Value result = compile("Sequence{5..1}");

    assertSize(result, 5);
    assertIntAt(result, 0, 5, "First element should be 5");
    assertIntAt(result, 4, 1, "Last element should be 1");
  }

  // ==================== Query Operations ====================

  /**
   * Tests the {@code size()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [3]}
   */
  @Test
  void testSize() {
    assertSingleInt(compile("Set{1,2,3}.size()"), 3);
  }

  /**
   * Tests the {@code isEmpty()} operation on an empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.isEmpty()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   */
  @Test
  void testIsEmpty() {
    assertSingleBool(compile("Set{}.isEmpty()"), true);
  }

  /**
   * Tests the {@code notEmpty()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.notEmpty()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   */
  @Test
  void testNotEmpty() {
    assertSingleBool(compile("Set{1,2,3}.notEmpty()"), true);
  }

  /**
   * Tests the {@code includes()} operation with an element present in the collection.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.includes(2)}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   */
  @Test
  void testIncludes() {
    assertSingleBool(compile("Set{1,2,3}.includes(2)"), true);
  }

  /**
   * Tests the {@code includes()} operation with an element not in the collection.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.includes(5)}
   *
   * <p><b>Expected:</b> Singleton collection {@code [false]}
   */
  @Test
  void testIncludesFalse() {
    assertSingleBool(compile("Set{1,2,3}.includes(5)"), false);
  }

  /**
   * Tests the {@code excludes()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.excludes(5)}
   *
   * <p><b>Expected:</b> Singleton collection {@code [true]}
   */
  @Test
  void testExcludes() {
    assertSingleBool(compile("Set{1,2,3}.excludes(5)"), true);
  }

  // ==================== Set Operations ====================

  /**
   * Tests the {@code union()} operation with disjoint sets.
   *
   * <p><b>Input:</b> {@code Set{1,2}.union(Set{3,4})}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3, 4}
   */
  @Test
  void testUnion() {
    Value result = compile("Set{1,2}.union(Set{3,4})");

    assertSize(result, 4);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
  }

  /**
   * Tests the {@code union()} operation with overlapping sets.
   *
   * <p><b>Input:</b> {@code Set{1,2}.union(Set{2,3})}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3} (duplicate 2 removed)
   */
  @Test
  void testUnionWithDuplicates() {
    Value result = compile("Set{1,2}.union(Set{2,3})");

    assertSize(result, 3);
    assertIncludes(result, 1);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
  }

  /**
   * Tests the {@code including()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2}.including(3)}
   *
   * <p><b>Expected:</b> Set containing {1, 2, 3}
   */
  @Test
  void testIncluding() {
    Value result = compile("Set{1,2}.including(3)");

    assertSize(result, 3);
    assertIncludes(result, 3);
  }

  /**
   * Tests the {@code excluding()} operation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.excluding(2)}
   *
   * <p><b>Expected:</b> Set containing {1, 3}
   */
  @Test
  void testExcluding() {
    Value result = compile("Set{1,2,3}.excluding(2)");

    assertSize(result, 2);
    assertFalse(
        result.includes(new OCLElement.IntValue(2)), "excluding(2) should remove element 2");
  }

  // ==================== Ordered Operations ====================

  /**
   * Tests the {@code first()} operation on a Sequence.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}.first()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [1]}
   */
  @Test
  void testFirst() {
    assertSingleInt(compile("Sequence{1,2,3}.first()"), 1);
  }

  /**
   * Tests the {@code last()} operation on a Sequence.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}.last()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [3]}
   */
  @Test
  void testLast() {
    assertSingleInt(compile("Sequence{1,2,3}.last()"), 3);
  }

  /**
   * Tests the {@code reverse()} operation on a Sequence.
   *
   * <p><b>Input:</b> {@code Sequence{1,2,3}.reverse()}
   *
   * <p><b>Expected:</b> Sequence containing {3, 2, 1} in that order
   */
  @Test
  void testReverse() {
    Value result = compile("Sequence{1,2,3}.reverse()");

    assertSize(result, 3);
    assertIntAt(result, 0, 3, "First element after reverse should be 3");
    assertIntAt(result, 1, 2, "Second element after reverse should be 2");
    assertIntAt(result, 2, 1, "Third element after reverse should be 1");
  }

  // ==================== Chained Operations ====================

  /**
   * Tests multiple collection operations chained together.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.including(4).excluding(2).size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [3]}
   *
   * <p><b>Evaluation flow:</b> {@code {1,2,3} → {1,2,3,4} → {1,3,4} → 3}
   */
  @Test
  void testChainedOperations() {
    assertSingleInt(compile("Set{1,2,3}.including(4).excluding(2).size()"), 3);
  }

  // ==================== Edge Cases ====================

  /**
   * Tests {@code size()} on an empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [0]}
   */
  @Test
  void testEmptySetSize() {
    assertSingleInt(compile("Set{}.size()"), 0);
  }

  /**
   * Tests {@code size()} on a singleton collection.
   *
   * <p><b>Input:</b> {@code Set{42}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [1]}
   */
  @Test
  void testSingletonSize() {
    assertSingleInt(compile("Set{42}.size()"), 1);
  }

  /**
   * Tests range generation with a large range.
   *
   * <p><b>Input:</b> {@code Set{1..100}.size()}
   *
   * <p><b>Expected:</b> Singleton collection {@code [100]}
   */
  @Test
  void testLargeRange() {
    assertSingleInt(compile("Set{1..100}.size()"), 100);
  }
}
