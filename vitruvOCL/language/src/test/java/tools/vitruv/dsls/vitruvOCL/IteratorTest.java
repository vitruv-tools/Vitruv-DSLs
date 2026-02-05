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
 * Comprehensive test suite for iterator operations and lambda expressions.
 *
 * <p>This test class validates the complete implementation of OCL iterator operations (also called
 * collection iteration expressions), which are fundamental functional programming constructs for
 * operating on collections with lambda-style predicates and transformations.
 *
 * <h2>Iterator Operations Syntax</h2>
 *
 * OCL iterators use the following syntax:
 *
 * <pre>{@code
 * collection.operation(variable | expression)
 * }</pre>
 *
 * <p>Where:
 *
 * <ul>
 *   <li><b>collection:</b> The source collection to iterate over
 *   <li><b>operation:</b> The iterator operation (select, reject, collect, forAll, exists)
 *   <li><b>variable:</b> Iterator variable (binds to each element)
 *   <li><b>expression:</b> Predicate or transformation using the iterator variable
 * </ul>
 *
 * <h2>Tested Iterator Operations</h2>
 *
 * <ul>
 *   <li><b>select(var | predicate):</b> Filters elements that satisfy the predicate → {@code
 *       Set{1,2,3,4,5}.select(x | x > 2)} → {@code {3,4,5}}
 *   <li><b>reject(var | predicate):</b> Filters elements that do NOT satisfy the predicate → {@code
 *       Set{1,2,3,4,5}.reject(x | x <= 2)} → {@code {3,4,5}}
 *   <li><b>collect(var | expression):</b> Transforms each element using the expression → {@code
 *       Set{1,2,3}.collect(x | x * 2)} → {@code {2,4,6}}
 *   <li><b>forAll(var | predicate):</b> Checks if all elements satisfy the predicate → {@code
 *       Set{1,2,3}.forAll(x | x > 0)} → {@code [true]}
 *   <li><b>exists(var | predicate):</b> Checks if at least one element satisfies the predicate →
 *       {@code Set{1,2,3}.exists(x | x == 2)} → {@code [true]}
 * </ul>
 *
 * @see Value Runtime collection representation
 * @see OCLElement Collection element types
 * @see EvaluationVisitor Evaluates iterator operations with variable binding
 * @see TypeCheckVisitor Type checks iterator expressions with scoping
 */
public class IteratorTest {

  // ==================== SELECT ====================

  /**
   * Tests basic select operation with simple comparison predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.select(x | x > 2)}
   *
   * <p><b>Expected:</b> Collection {3, 4, 5}
   *
   * <p><b>Semantics:</b> {@code select} keeps only elements that satisfy the predicate {@code x >
   * 2}.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic select syntax parsing
   *   <li>Iterator variable binding (x)
   *   <li>Predicate evaluation for each element
   *   <li>Correct filtering result
   * </ul>
   */
  @Test
  public void testSelectBasic() {
    String input = "Set{1,2,3,4,5}.select(x | x > 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "select(x | x > 2) should keep 3, 4, 5");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  /**
   * Tests select operation where no elements match the predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.select(x | x > 10)}
   *
   * <p><b>Expected:</b> Empty collection
   *
   * <p><b>Edge case:</b> When no elements satisfy the predicate, select returns an empty collection
   * (not null).
   */
  @Test
  public void testSelectNoneMatch() {
    String input = "Set{1,2,3}.select(x | x > 10)";
    Value result = compile(input);

    assertTrue(result.isEmpty(), "select with no matches should return empty set");
    assertEquals(0, result.size());
  }

  /**
   * Tests select operation where all elements match the predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.select(x | x > 0)}
   *
   * <p><b>Expected:</b> Collection {1, 2, 3} (all elements)
   *
   * <p><b>Edge case:</b> When all elements satisfy the predicate, the entire collection is
   * returned.
   */
  @Test
  public void testSelectAllMatch() {
    String input = "Set{1,2,3}.select(x | x > 0)";
    Value result = compile(input);

    assertEquals(3, result.size(), "select with all matches should return all elements");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  /**
   * Tests select with complex boolean predicate combining multiple conditions.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3 and x < 8)}
   *
   * <p><b>Expected:</b> Collection {4, 5, 6, 7}
   *
   * <p><b>Validates:</b> Compound boolean expressions in predicates (AND operation).
   */
  @Test
  public void testSelectComplexPredicate() {
    String input = "Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3 and x < 8)";
    Value result = compile(input);

    assertEquals(4, result.size(), "Should select elements between 3 and 8");
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(7)));
  }

  /**
   * Tests select with arithmetic expression in predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.select(x | x * 2 > 5)}
   *
   * <p><b>Expected:</b> Collection {3, 4, 5}
   *
   * <p><b>Predicate logic:</b> {@code x * 2 > 5} is satisfied when {@code x >= 3}.
   *
   * <p><b>Validates:</b> Arithmetic operations can be used in predicates.
   */
  @Test
  public void testSelectWithArithmetic() {
    String input = "Set{1,2,3,4,5}.select(x | x * 2 > 5)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should select 3, 4, 5 (where x*2 > 5)");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  /**
   * Tests select operation on a Sequence (ordered collection).
   *
   * <p><b>Input:</b> {@code Sequence{5,2,8,1,9,3}.select(x | x > 4)}
   *
   * <p><b>Expected:</b> Collection {5, 8, 9}
   *
   * <p><b>Note:</b> Order preservation depends on implementation. This test validates that select
   * works on Sequences, not just Sets.
   */
  @Test
  public void testSelectOnSequence() {
    String input = "Sequence{5,2,8,1,9,3}.select(x | x > 4)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should select 5, 8, 9");
    // Note: Order might not be preserved depending on implementation
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
  }

  // ==================== REJECT ====================

  /**
   * Tests basic reject operation (inverse of select).
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.reject(x | x <= 2)}
   *
   * <p><b>Expected:</b> Collection {3, 4, 5}
   *
   * <p><b>Semantics:</b> {@code reject} keeps only elements that do NOT satisfy the predicate
   * {@code x <= 2}.
   *
   * <p><b>Relationship to select:</b> {@code reject(p)} = {@code select(not p)}
   */
  @Test
  public void testRejectBasic() {
    String input = "Set{1,2,3,4,5}.reject(x | x <= 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "reject(x | x <= 2) should keep 3, 4, 5");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  /**
   * Tests reject operation where no elements are rejected.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.reject(x | x > 10)}
   *
   * <p><b>Expected:</b> Collection {1, 2, 3} (all elements kept)
   *
   * <p><b>Edge case:</b> When no elements satisfy the rejection predicate, all elements are
   * retained.
   */
  @Test
  public void testRejectNoneRejected() {
    String input = "Set{1,2,3}.reject(x | x > 10)";
    Value result = compile(input);

    assertEquals(3, result.size(), "reject with no rejections should return all elements");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
  }

  /**
   * Tests reject operation where all elements are rejected.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.reject(x | x > 0)}
   *
   * <p><b>Expected:</b> Empty collection
   *
   * <p><b>Edge case:</b> When all elements satisfy the rejection predicate, an empty collection is
   * returned.
   */
  @Test
  public void testRejectAllRejected() {
    String input = "Set{1,2,3}.reject(x | x > 0)";
    Value result = compile(input);

    assertTrue(result.isEmpty(), "reject with all rejected should return empty set");
    assertEquals(0, result.size());
  }

  /**
   * Tests reject with equality predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.reject(x | x == 3)}
   *
   * <p><b>Expected:</b> Collection {1, 2, 4, 5} (element 3 removed)
   *
   * <p><b>Validates:</b> Reject can filter out specific values using equality.
   */
  @Test
  public void testRejectEquality() {
    String input = "Set{1,2,3,4,5}.reject(x | x == 3)";
    Value result = compile(input);

    assertEquals(4, result.size(), "Should reject only element 3");
    assertFalse(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  /**
   * Tests that select and reject are complementary operations.
   *
   * <p><b>Select input:</b> {@code Set{1,2,3,4,5}.select(x | x > 2)}
   *
   * <p><b>Reject input:</b> {@code Set{1,2,3,4,5}.reject(x | x <= 2)}
   *
   * <p><b>Expected:</b> Both should produce {3, 4, 5}
   *
   * <p><b>Property:</b> {@code select(p)} and {@code reject(not p)} are equivalent.
   */
  @Test
  public void testSelectVsReject() {
    // select and reject should be complementary
    String selectInput = "Set{1,2,3,4,5}.select(x | x > 2)";
    String rejectInput = "Set{1,2,3,4,5}.reject(x | x <= 2)";

    Value selectResult = compile(selectInput);
    Value rejectResult = compile(rejectInput);

    assertEquals(
        selectResult.size(),
        rejectResult.size(),
        "select(x | x > 2) and reject(x | x <= 2) should produce same result");
  }

  // ==================== COLLECT ====================

  /**
   * Tests basic collect operation with simple transformation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.collect(x | x * 2)}
   *
   * <p><b>Expected:</b> Collection {2, 4, 6}
   *
   * <p><b>Semantics:</b> {@code collect} applies the transformation {@code x * 2} to each element.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Basic collect syntax parsing
   *   <li>Iterator variable binding
   *   <li>Expression evaluation for each element
   *   <li>Collection of transformed results
   * </ul>
   */
  @Test
  public void testCollectBasic() {
    String input = "Set{1,2,3}.collect(x | x * 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "collect should have 3 transformed elements");
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
  }

  /**
   * Tests collect with addition transformation.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.collect(x | x + 10)}
   *
   * <p><b>Expected:</b> Collection {11, 12, 13}
   *
   * <p><b>Validates:</b> Addition operations in collect transformations.
   */
  @Test
  public void testCollectWithAddition() {
    String input = "Set{1,2,3}.collect(x | x + 10)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should add 10 to each element");
    assertTrue(result.includes(new OCLElement.IntValue(11)));
    assertTrue(result.includes(new OCLElement.IntValue(12)));
    assertTrue(result.includes(new OCLElement.IntValue(13)));
  }

  /**
   * Tests collect with complex arithmetic expression.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.collect(x | x * 2 + x)}
   *
   * <p><b>Expected:</b> Collection {3, 6, 9}
   *
   * <p><b>Transformation:</b> {@code x * 2 + x} = {@code 3x}
   *
   * <ul>
   *   <li>1 * 2 + 1 = 3
   *   <li>2 * 2 + 2 = 6
   *   <li>3 * 2 + 3 = 9
   * </ul>
   */
  @Test
  public void testCollectComplexExpression() {
    String input = "Set{1,2,3}.collect(x | x * 2 + x)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should compute x * 2 + x for each element");
    assertTrue(result.includes(new OCLElement.IntValue(3))); // 1*2+1 = 3
    assertTrue(result.includes(new OCLElement.IntValue(6))); // 2*2+2 = 6
    assertTrue(result.includes(new OCLElement.IntValue(9))); // 3*2+3 = 9
  }

  /**
   * Tests collect with division transformation.
   *
   * <p><b>Input:</b> {@code Set{2,4,6,8,10}.collect(x | x / 2)}
   *
   * <p><b>Expected:</b> Collection {1, 2, 3, 4, 5}
   *
   * <p><b>Validates:</b> Division operations in collect transformations.
   */
  @Test
  public void testCollectWithDivision() {
    String input = "Set{2,4,6,8,10}.collect(x | x / 2)";
    Value result = compile(input);

    assertEquals(5, result.size(), "Should divide each element by 2");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(2)));
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  /**
   * Tests collect on an empty set.
   *
   * <p><b>Input:</b> {@code Set{}.collect(x | x * 2)}
   *
   * <p><b>Expected:</b> Empty collection
   *
   * <p><b>Edge case:</b> Collecting over an empty set produces an empty result (no elements to
   * transform).
   */
  @Test
  public void testCollectOnEmptySet() {
    String input = "Set{}.collect(x | x * 2)";
    Value result = compile(input);

    assertTrue(result.isEmpty(), "collect on empty set should return empty set");
    assertEquals(0, result.size());
  }

  /**
   * Tests that collect automatically flattens nested collections.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.collect(x | x * 2)}
   *
   * <p><b>Without auto-flatten:</b> {@code [[2], [4], [6]]} (nested)
   *
   * <p><b>With auto-flatten:</b> {@code [2, 4, 6]} (flat)
   *
   * <p><b>Validates:</b> Auto-flattening behavior of collect operation.
   */
  @Test
  public void testCollectAutoFlatten() {
    // collect automatically flattens results
    // Even though x * 2 returns [2], [4], [6], collect flattens to [2, 4, 6]
    String input = "Set{1,2,3}.collect(x | x * 2)";
    Value result = compile(input);

    // Check that result is flat, not nested
    for (OCLElement elem : result.getElements()) {
      assertFalse(
          elem instanceof OCLElement.NestedCollection, "collect should auto-flatten results");
    }
  }

  // ==================== FORALL ====================

  /**
   * Tests forAll operation with all elements satisfying the predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.forAll(x | x > 0)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Semantics:</b> {@code forAll} returns true if the predicate holds for every element.
   *
   * <p><b>Universal quantification:</b> ∀x ∈ {1,2,3}: x > 0 (true)
   */
  @Test
  public void testForAllTrue() {
    String input = "Set{1,2,3}.forAll(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertTrue(((OCLElement.BoolValue) elem).value(), "forAll should be true when all satisfy");
  }

  /**
   * Tests forAll operation with some elements not satisfying the predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.forAll(x | x > 2)}
   *
   * <p><b>Expected:</b> Singleton {@code [false]}
   *
   * <p><b>Semantics:</b> {@code forAll} returns false if even one element fails the predicate.
   *
   * <p><b>Universal quantification:</b> ∀x ∈ {1,2,3}: x > 2 (false, because 1 and 2 fail)
   */
  @Test
  public void testForAllFalse() {
    String input = "Set{1,2,3}.forAll(x | x > 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertFalse(
        ((OCLElement.BoolValue) elem).value(), "forAll should be false when not all satisfy");
  }

  /**
   * Tests forAll on an empty set (vacuous truth).
   *
   * <p><b>Input:</b> {@code Set{}.forAll(x | x > 100)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Logical principle:</b> Vacuous truth - a universal quantification over an empty set is
   * always true because there are no elements to falsify it.
   *
   * <p><b>Formal logic:</b> ∀x ∈ ∅: P(x) is vacuously true for any predicate P.
   */
  @Test
  public void testForAllOnEmptySet() {
    String input = "Set{}.forAll(x | x > 100)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertTrue(((OCLElement.BoolValue) elem).value(), "forAll on empty set is vacuously true");
  }

  /**
   * Tests forAll with complex compound predicate.
   *
   * <p><b>Input:</b> {@code Set{2,4,6,8}.forAll(x | x > 0 and x < 10)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Validates:</b> Compound boolean expressions work in forAll predicates.
   */
  @Test
  public void testForAllComplexPredicate() {
    String input = "Set{2,4,6,8}.forAll(x | x > 0 and x < 10)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All elements satisfy 0 < x < 10");
  }

  /**
   * Tests forAll with arithmetic expression in predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.forAll(x | x * 2 <= 10)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Predicate logic:</b> {@code x * 2 <= 10} is true for all x in {1,2,3,4,5}.
   */
  @Test
  public void testForAllWithArithmetic() {
    String input = "Set{1,2,3,4,5}.forAll(x | x * 2 <= 10)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All x*2 <= 10");
  }

  /**
   * Tests forAll with equality predicate on uniform set.
   *
   * <p><b>Input:</b> {@code Set{5,5,5}.forAll(x | x == 5)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Note:</b> In a Set, duplicates are removed, so this is actually {@code Set{5}}.
   */
  @Test
  public void testForAllEquality() {
    String input = "Set{5,5,5}.forAll(x | x == 5)";
    Value result = compile(input);

    assertEquals(1, result.size(), "forAll should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All elements equal 5");
  }

  // ==================== EXISTS ====================

  /**
   * Tests exists operation with one element satisfying the predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.exists(x | x == 2)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Semantics:</b> {@code exists} returns true if at least one element satisfies the
   * predicate.
   *
   * <p><b>Existential quantification:</b> ∃x ∈ {1,2,3}: x == 2 (true)
   */
  @Test
  public void testExistsTrue() {
    String input = "Set{1,2,3}.exists(x | x == 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertTrue(
        ((OCLElement.BoolValue) elem).value(), "exists should be true when at least one satisfies");
  }

  /**
   * Tests exists operation with no elements satisfying the predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.exists(x | x > 10)}
   *
   * <p><b>Expected:</b> Singleton {@code [false]}
   *
   * <p><b>Existential quantification:</b> ∃x ∈ {1,2,3}: x > 10 (false)
   */
  @Test
  public void testExistsFalse() {
    String input = "Set{1,2,3}.exists(x | x > 10)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertFalse(((OCLElement.BoolValue) elem).value(), "exists should be false when none satisfy");
  }

  /**
   * Tests exists on an empty set.
   *
   * <p><b>Input:</b> {@code Set{}.exists(x | x > 0)}
   *
   * <p><b>Expected:</b> Singleton {@code [false]}
   *
   * <p><b>Logical principle:</b> Existential quantification over an empty set is always false
   * because there are no elements to satisfy the predicate.
   *
   * <p><b>Formal logic:</b> ∃x ∈ ∅: P(x) is always false for any predicate P.
   */
  @Test
  public void testExistsOnEmptySet() {
    String input = "Set{}.exists(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.BoolValue);
    assertFalse(((OCLElement.BoolValue) elem).value(), "exists on empty set is false");
  }

  /**
   * Tests exists with multiple elements satisfying the predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.exists(x | x > 2)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Note:</b> Even though multiple elements (3, 4, 5) satisfy the predicate, {@code exists}
   * still returns a single boolean true.
   */
  @Test
  public void testExistsMultipleMatch() {
    String input = "Set{1,2,3,4,5}.exists(x | x > 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(
        ((OCLElement.BoolValue) elem).value(), "exists should be true when multiple satisfy");
  }

  /**
   * Tests exists with complex compound predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.exists(x | x > 3 and x < 5)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Predicate logic:</b> Element 4 satisfies {@code x > 3 and x < 5}.
   */
  @Test
  public void testExistsComplexPredicate() {
    String input = "Set{1,2,3,4,5}.exists(x | x > 3 and x < 5)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Element 4 satisfies predicate");
  }

  /**
   * Tests exists with arithmetic expression in predicate.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.exists(x | x * 2 == 8)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Predicate logic:</b> Element 4 satisfies {@code x * 2 == 8}.
   */
  @Test
  public void testExistsWithArithmetic() {
    String input = "Set{1,2,3,4,5}.exists(x | x * 2 == 8)";
    Value result = compile(input);

    assertEquals(1, result.size(), "exists should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "Element 4 satisfies x*2 == 8");
  }

  // ==================== CHAINING ====================

  /**
   * Tests chaining select followed by collect.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.select(x | x > 2).collect(x | x * 2)}
   *
   * <p><b>Expected:</b> Collection {6, 8, 10}
   *
   * <p><b>Execution flow:</b>
   *
   * <ol>
   *   <li>{@code select(x | x > 2)} → {3, 4, 5}
   *   <li>{@code collect(x | x * 2)} → {6, 8, 10}
   * </ol>
   *
   * <p><b>Validates:</b> Filter-then-transform pattern (common in functional programming).
   */
  @Test
  public void testSelectThenCollect() {
    String input = "Set{1,2,3,4,5}.select(x | x > 2).collect(x | x * 2)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should select then transform");
    assertTrue(result.includes(new OCLElement.IntValue(6))); // 3*2
    assertTrue(result.includes(new OCLElement.IntValue(8))); // 4*2
    assertTrue(result.includes(new OCLElement.IntValue(10))); // 5*2
  }

  /**
   * Tests chaining collect followed by select.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.collect(x | x * 2).select(x | x > 5)}
   *
   * <p><b>Expected:</b> Collection {6, 8, 10}
   *
   * <p><b>Execution flow:</b>
   *
   * <ol>
   *   <li>{@code collect(x | x * 2)} → {2, 4, 6, 8, 10}
   *   <li>{@code select(x | x > 5)} → {6, 8, 10}
   * </ol>
   *
   * <p><b>Validates:</b> Transform-then-filter pattern.
   */
  @Test
  public void testCollectThenSelect() {
    String input = "Set{1,2,3,4,5}.collect(x | x * 2).select(x | x > 5)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should transform then select");
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(10)));
  }

  /**
   * Tests chaining select followed by reject.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).reject(x | x > 7)}
   *
   * <p><b>Expected:</b> Collection {4, 5, 6, 7}
   *
   * <p><b>Execution flow:</b>
   *
   * <ol>
   *   <li>{@code select(x | x > 3)} → {4, 5, 6, 7, 8, 9, 10}
   *   <li>{@code reject(x | x > 7)} → {4, 5, 6, 7}
   * </ol>
   *
   * <p><b>Validates:</b> Combining positive and negative filters.
   */
  @Test
  public void testSelectThenReject() {
    String input = "Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).reject(x | x > 7)";
    Value result = compile(input);

    assertEquals(4, result.size(), "Should select x > 3, then reject x > 7");
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(7)));
  }

  /**
   * Tests chaining collect followed by forAll.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.collect(x | x * 2).forAll(x | x > 0)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Execution flow:</b>
   *
   * <ol>
   *   <li>{@code collect(x | x * 2)} → {2, 4, 6}
   *   <li>{@code forAll(x | x > 0)} → true
   * </ol>
   *
   * <p><b>Validates:</b> Transform-then-quantify pattern.
   */
  @Test
  public void testCollectThenForAll() {
    String input = "Set{1,2,3}.collect(x | x * 2).forAll(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton boolean");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All transformed elements > 0");
  }

  /**
   * Tests chaining select followed by exists.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.select(x | x > 2).exists(x | x == 4)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Execution flow:</b>
   *
   * <ol>
   *   <li>{@code select(x | x > 2)} → {3, 4, 5}
   *   <li>{@code exists(x | x == 4)} → true
   * </ol>
   *
   * <p><b>Validates:</b> Filter-then-quantify pattern.
   */
  @Test
  public void testSelectThenExists() {
    String input = "Set{1,2,3,4,5}.select(x | x > 2).exists(x | x == 4)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton boolean");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "4 exists in selected set");
  }

  /**
   * Tests triple chaining of iterator operations.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).collect(x | x * 2).reject(x
   * | x > 15)}
   *
   * <p><b>Expected:</b> Collection {8, 10, 12, 14}
   *
   * <p><b>Execution flow:</b>
   *
   * <ol>
   *   <li>{@code select(x | x > 3)} → {4, 5, 6, 7, 8, 9, 10}
   *   <li>{@code collect(x | x * 2)} → {8, 10, 12, 14, 16, 18, 20}
   *   <li>{@code reject(x | x > 15)} → {8, 10, 12, 14}
   * </ol>
   *
   * <p><b>Validates:</b> Complex chaining with proper intermediate result handling.
   */
  @Test
  public void testTripleChaining() {
    String input =
        "Set{1,2,3,4,5,6,7,8,9,10}.select(x | x > 3).collect(x | x * 2).reject(x | x > 15)";
    Value result = compile(input);

    // select: {4,5,6,7,8,9,10}
    // collect: {8,10,12,14,16,18,20}
    // reject x > 15: {8,10,12,14}
    assertEquals(4, result.size(), "Should apply all three operations");
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(10)));
    assertTrue(result.includes(new OCLElement.IntValue(12)));
    assertTrue(result.includes(new OCLElement.IntValue(14)));
  }

  // ==================== WITH LET EXPRESSIONS ====================

  /**
   * Tests select with let-expression providing threshold parameter.
   *
   * <p><b>Input:</b> {@code let threshold = 3 in Set{1,2,3,4,5}.select(x | x > threshold)}
   *
   * <p><b>Expected:</b> Collection {4, 5}
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Integration of let-expressions with iterator operations
   *   <li>Proper scoping: {@code threshold} is visible inside the select predicate
   *   <li>No shadowing issues between let variable and iterator variable
   * </ul>
   */
  @Test
  public void testSelectWithLet() {
    String input = "let threshold = 3 in Set{1,2,3,4,5}.select(x | x > threshold)";
    Value result = compile(input);

    assertEquals(2, result.size(), "Should select elements > threshold");
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(5)));
  }

  /**
   * Tests collect with let-expression providing multiplier parameter.
   *
   * <p><b>Input:</b> {@code let multiplier = 3 in Set{1,2,3}.collect(x | x * multiplier)}
   *
   * <p><b>Expected:</b> Collection {3, 6, 9}
   *
   * <p><b>Validates:</b> Let variables can be used in collect transformation expressions.
   */
  @Test
  public void testCollectWithLet() {
    String input = "let multiplier = 3 in Set{1,2,3}.collect(x | x * multiplier)";
    Value result = compile(input);

    assertEquals(3, result.size(), "Should multiply by 3");
    assertTrue(result.includes(new OCLElement.IntValue(3)));
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
  }

  /**
   * Tests forAll with let-expression providing bound parameter.
   *
   * <p><b>Input:</b> {@code let maxValue = 10 in Set{1,2,3,4,5}.forAll(x | x < maxValue)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Validates:</b> Let variables can be used in forAll predicates.
   */
  @Test
  public void testForAllWithLet() {
    String input = "let maxValue = 10 in Set{1,2,3,4,5}.forAll(x | x < maxValue)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "All elements < max");
  }

  // ==================== EDGE CASES ====================

  /**
   * Tests select on a singleton set (matching).
   *
   * <p><b>Input:</b> {@code Set{42}.select(x | x > 40)}
   *
   * <p><b>Expected:</b> Singleton {42}
   *
   * <p><b>Edge case:</b> Select on singleton where element matches predicate.
   */
  @Test
  public void testSelectOnSingletonSet() {
    String input = "Set{42}.select(x | x > 40)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should keep the single element");
    assertTrue(result.includes(new OCLElement.IntValue(42)));
  }

  /**
   * Tests collect on a singleton set.
   *
   * <p><b>Input:</b> {@code Set{5}.collect(x | x * 2)}
   *
   * <p><b>Expected:</b> Singleton {10}
   *
   * <p><b>Edge case:</b> Collect on singleton produces singleton result.
   */
  @Test
  public void testCollectOnSingletonSet() {
    String input = "Set{5}.collect(x | x * 2)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should transform single element");
    assertTrue(result.includes(new OCLElement.IntValue(10)));
  }

  /**
   * Tests forAll on a singleton set.
   *
   * <p><b>Input:</b> {@code Set{42}.forAll(x | x > 0)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Edge case:</b> ForAll on singleton checks single element.
   */
  @Test
  public void testForAllOnSingletonSet() {
    String input = "Set{42}.forAll(x | x > 0)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests exists on a singleton set.
   *
   * <p><b>Input:</b> {@code Set{42}.exists(x | x == 42)}
   *
   * <p><b>Expected:</b> Singleton {@code [true]}
   *
   * <p><b>Edge case:</b> Exists on singleton checks single element.
   */
  @Test
  public void testExistsOnSingletonSet() {
    String input = "Set{42}.exists(x | x == 42)";
    Value result = compile(input);

    assertEquals(1, result.size(), "Should return singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value());
  }

  /**
   * Tests select on a range expression.
   *
   * <p><b>Input:</b> {@code Set{1..10}.select(x | x > 5)}
   *
   * <p><b>Expected:</b> Collection {6, 7, 8, 9, 10}
   *
   * <p><b>Validates:</b> Iterator operations work with range-generated collections.
   */
  @Test
  public void testSelectWithRange() {
    String input = "Set{1..10}.select(x | x > 5)";
    Value result = compile(input);

    assertEquals(5, result.size(), "Should select 6,7,8,9,10");
    assertTrue(result.includes(new OCLElement.IntValue(6)));
    assertTrue(result.includes(new OCLElement.IntValue(7)));
    assertTrue(result.includes(new OCLElement.IntValue(8)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
    assertTrue(result.includes(new OCLElement.IntValue(10)));
  }

  /**
   * Tests collect on a range expression with squaring transformation.
   *
   * <p><b>Input:</b> {@code Set{1..5}.collect(x | x * x)}
   *
   * <p><b>Expected:</b> Collection {1, 4, 9, 16, 25}
   *
   * <p><b>Transformation:</b> Computes squares of elements 1 through 5.
   */
  @Test
  public void testCollectWithRange() {
    String input = "Set{1..5}.collect(x | x * x)";
    Value result = compile(input);

    assertEquals(5, result.size(), "Should compute squares");
    assertTrue(result.includes(new OCLElement.IntValue(1)));
    assertTrue(result.includes(new OCLElement.IntValue(4)));
    assertTrue(result.includes(new OCLElement.IntValue(9)));
    assertTrue(result.includes(new OCLElement.IntValue(16)));
    assertTrue(result.includes(new OCLElement.IntValue(25)));
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL iterator expression through the complete pipeline.
   *
   * <p>This method orchestrates the three-phase compilation process with enhanced error reporting
   * for debugging iterator operations:
   *
   * <ol>
   *   <li><b>Phase 1 - Parsing:</b> Converts input to parse tree using {@code prefixedExpCS} entry
   *       point
   *   <li><b>Phase 2 - Type Checking:</b> Validates iterator variable scoping and type inference
   *       for lambda expressions
   *   <li><b>Phase 3 - Evaluation:</b> Evaluates iterator operations with proper variable binding
   *       and scope management
   * </ol>
   *
   * <p><b>Iterator-specific handling:</b> The compilation process includes special handling for:
   *
   * <ul>
   *   <li>Variable scoping: Iterator variables create new local scopes
   *   <li>Type inference: Predicate and transformation expression types are inferred
   *   <li>Lambda evaluation: Predicates/expressions are evaluated for each element
   * </ul>
   *
   * <p><b>Enhanced error reporting:</b> This method prints detailed error messages with line and
   * column information to aid in debugging iterator expression failures.
   *
   * @param input The OCL iterator expression to compile and evaluate
   * @return The evaluated result as a {@link Value}
   * @throws AssertionError if any compilation phase fails
   */
  private Value compile(String input) {
    // Parse
    ParseTree tree = parse(input);

    // Create dummy specification (no metamodels needed for iterator tests)
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

          @Override
          public String getInstanceNameByIndex(int index) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException(
                "Unimplemented method 'getInstanceNameByIndex'");
          }
        };

    // Initialize 3-pass architecture
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    // Pass 1: Symbol Table Construction (creates scopes for iterator variables)
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    // Check for Symbol Table Errors
    if (errors.hasErrors()) {
      System.err.println("=== SYMBOL TABLE CONSTRUCTION ERRORS ===");
      errors
          .getErrors()
          .forEach(
              error -> {
                System.err.println(
                    "  Line "
                        + error.getLine()
                        + ":"
                        + error.getColumn()
                        + " - "
                        + error.getMessage());
              });
      System.err.println("========================================");
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    // Pass 2: Type Checking (validates scoping and type inference)
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.visit(tree);

    // Check for Type Errors - PRINT THEM for debugging!
    if (typeChecker.hasErrors()) {
      System.err.println("=== TYPE CHECKING ERRORS ===");
      typeChecker
          .getErrorCollector()
          .getErrors()
          .forEach(
              error -> {
                System.err.println(
                    "  Line "
                        + error.getLine()
                        + ":"
                        + error.getColumn()
                        + " - "
                        + error.getMessage());
              });
      System.err.println("============================");
      fail("Pass 2 (Type checking) failed: " + typeChecker.getErrorCollector().getErrors());
    }

    // Pass 3: Evaluation (evaluates iterator operations with variable binding)
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    // Check for Evaluation Errors - PRINT THEM for debugging!
    if (evaluator.hasErrors()) {
      System.err.println("=== EVALUATION ERRORS ===");
      evaluator
          .getErrorCollector()
          .getErrors()
          .forEach(
              error -> {
                System.err.println(
                    "  Line "
                        + error.getLine()
                        + ":"
                        + error.getColumn()
                        + " - "
                        + error.getMessage());
              });
      System.err.println("=========================");
      fail("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }

  /**
   * Parses an OCL iterator expression string into an ANTLR parse tree.
   *
   * <p>Uses {@code prefixedExpCS} as the entry point, which handles:
   *
   * <ul>
   *   <li>Collection literals and operations
   *   <li>Iterator operations (select, reject, collect, forAll, exists)
   *   <li>Navigation chains and method calls
   * </ul>
   *
   * @param input The OCL iterator expression string to parse
   * @return The ANTLR parse tree representing the expression
   */
  private ParseTree parse(String input) {
    // Lexer
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // Parser
    VitruvOCLParser parser = new VitruvOCLParser(tokens);

    // Parse as prefixed expression (covers iterator operations)
    return parser.prefixedExpCS();
  }
}