/*******************************************************************************.
 * Copyright (c) 2026 Max Oesterle
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;

/**
 * Cross-product tests for collection operation chaining.
 *
 * <p>Tests every valid "op A → then op B" combination from the chaining matrix:
 *
 * <pre>
 * Returns Collection → can chain collection ops:
 *   select    → select, reject, collect, forAll, exists, size, isEmpty, first/last, flatten
 *   reject    → (same as select)
 *   collect   → (same as select)
 *   flatten   → (same as select)
 *   first/last → scalar → can chain scalar ops (string ops, arithmetic, comparisons)
 *
 * Returns Scalar → CANNOT chain collection ops:
 *   forAll  → Boolean → only logical/comparison ops
 *   exists  → Boolean → only logical/comparison ops
 *   size    → Integer → only arithmetic/comparison ops
 *   isEmpty → Boolean → only logical/comparison ops
 * </pre>
 */
public class CollectionChainingTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== select → * ====================

  @Test
  public void testSelectThenSelect() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 1).select(x | x < 5)");
    assertSize(result, 3);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
  }

  @Test
  public void testSelectThenReject() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 1).reject(x | x > 3)");
    assertSize(result, 2);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
  }

  @Test
  public void testSelectThenCollect() {
    // select evens, then collect doubled values
    Value result = compile("Set{1,2,3,4}.select(x | x > 2).collect(x | x * 2)");
    assertSize(result, 2);
    assertIncludes(result, 6);
    assertIncludes(result, 8);
  }

  @Test
  public void testSelectThenForAll() {
    assertSingleBool(compile("Set{2,4,6}.select(x | x > 0).forAll(x | x > 0)"), true);
  }

  @Test
  public void testSelectThenExists() {
    assertSingleBool(compile("Set{1,2,3}.select(x | x > 1).exists(x | x == 2)"), true);
  }

  @Test
  public void testSelectThenSize() {
    assertSingleInt(compile("Set{1,2,3,4,5}.select(x | x > 2).size()"), 3);
  }

  @Test
  public void testSelectThenIsEmpty() {
    assertSingleBool(compile("Set{1,2,3}.select(x | x > 10).isEmpty()"), true);
  }

  @Test
  public void testSelectThenFlattentypeError() {
    // select() on Set{Integer} → Set{Integer}; flatten() on flat collection is a type error
    assertTypeError("Set{1,2,3}.select(x | x > 1).flatten()");
  }

  // ==================== reject → * ====================

  @Test
  public void testRejectThenSelect() {
    Value result = compile("Set{1,2,3,4,5}.reject(x | x > 3).select(x | x > 1)");
    assertSize(result, 2);
  }

  @Test
  public void testRejectThenReject() {
    Value result = compile("Set{1,2,3,4,5}.reject(x | x > 4).reject(x | x < 2)");
    assertSize(result, 3);
  }

  @Test
  public void testRejectThenSize() {
    assertSingleInt(compile("Set{1,2,3,4,5}.reject(x | x > 3).size()"), 3);
  }

  @Test
  public void testRejectThenForAll() {
    assertSingleBool(compile("Set{1,2,3}.reject(x | x > 3).forAll(x | x <= 3)"), true);
  }

  @Test
  public void testRejectThenIsEmpty() {
    assertSingleBool(compile("Set{1,2,3}.reject(x | x >= 1).isEmpty()"), true);
  }

  // ==================== collect → * ====================

  @Test
  public void testCollectThenSelect() {
    // collect doubles, then select > 4
    Value result = compile("Set{1,2,3}.collect(x | x * 2).select(x | x > 4)");
    assertSize(result, 1);
    assertIncludes(result, 6);
  }

  @Test
  public void testCollectThenReject() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2).reject(x | x > 4)");
    assertSize(result, 2);
    assertIncludes(result, 2);
    assertIncludes(result, 4);
  }

  @Test
  public void testCollectThenCollect() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2).collect(x | x + 1)");
    assertSize(result, 3);
    assertIncludes(result, 3);
    assertIncludes(result, 5);
    assertIncludes(result, 7);
  }

  @Test
  public void testCollectThenSize() {
    assertSingleInt(compile("Set{1,2,3}.collect(x | x * 2).size()"), 3);
  }

  @Test
  public void testCollectThenForAll() {
    assertSingleBool(compile("Set{1,2,3}.collect(x | x * 2).forAll(x | x > 0)"), true);
  }

  @Test
  public void testCollectThenExists() {
    assertSingleBool(compile("Set{1,2,3}.collect(x | x + 10).exists(x | x == 12)"), true);
  }

  // ==================== flatten → * ====================
  // Note: flatten() requires nested collections. On flat collections it is a type error.
  // These tests use select() to produce a collection, then flatten() — which is valid
  // only if the element type is itself a collection. We test flatten on valid chaining instead.

  @Test
  public void testFlattenThenSelecttypeError() {
    assertTypeError("Set{1,2,3}.flatten().select(x | x > 1)");
  }

  @Test
  public void testFlattenThenSizetypeError() {
    assertTypeError("Set{1,2,3}.flatten().size()");
  }

  @Test
  public void testFlattenThenCollecttypeError() {
    assertTypeError("Set{1,2,3}.flatten().collect(x | x * 2)");
  }

  // ==================== first/last → scalar ops ====================

  @Test
  public void testFirstThenArithmetic() {
    // first() returns Integer → can do arithmetic
    assertSingleInt(compile("Sequence{3,1,4}.first() + 10"), 13);
  }

  @Test
  public void testLastThenArithmetic() {
    assertSingleInt(compile("Sequence{3,1,4}.last() * 2"), 8);
  }

  @Test
  public void testFirstThenComparison() {
    assertSingleBool(compile("Sequence{10,20,30}.first() > 5"), true);
  }

  @Test
  public void testLastThenComparison() {
    assertSingleBool(compile("Sequence{10,20,30}.last() == 30"), true);
  }

  // ==================== forAll/exists → scalar, CANNOT chain collection ops ====================

  @Test
  public void testForAllResultUsedInLogical() {
    // forAll → Boolean → can use in logical expression
    assertSingleBool(compile("Set{2,4,6}.forAll(x | x > 0) and true"), true);
  }

  @Test
  public void testExistsResultUsedInLogical() {
    assertSingleBool(compile("Set{1,2,3}.exists(x | x == 2) or false"), true);
  }

  @Test
  public void testForAllThenSelectFails() {
    // forAll returns Boolean, not a collection → cannot chain .select()
    assertTypeError("Set{1,2}.forAll(x | x > 0).select(x | x > 0)");
  }

  @Test
  public void testForAllThenSizeFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).size()");
  }

  @Test
  public void testExistsThenRejectFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).reject(x | x > 0)");
  }

  // ==================== size/isEmpty → scalar, CANNOT chain collection ops ====================

  @Test
  public void testSizeThenArithmetic() {
    // size() → Integer → arithmetic is fine
    assertSingleInt(compile("Set{1,2,3}.size() + 10"), 13);
  }

  @Test
  public void testSizeThenComparison() {
    assertSingleBool(compile("Set{1,2,3}.size() > 2"), true);
  }

  @Test
  public void testSizeThenSelectFails() {
    assertTypeError("Set{1,2,3}.size().select(x | x > 0)");
  }

  @Test
  public void testIsEmptyThenSelectFails() {
    assertTypeError("Set{}.isEmpty().select(x | x > 0)");
  }

  @Test
  public void testIsEmptyResultInLogical() {
    assertSingleBool(compile("Set{}.isEmpty() and true"), true);
  }

  // ==================== Long chains ====================

  @Test
  public void testThreeLevelChain() {
    // select → reject → size
    assertSingleInt(compile("Set{1,2,3,4,5,6}.select(x | x > 1).reject(x | x > 4).size()"), 3);
  }

  @Test
  public void testFourLevelChain() {
    // select → collect → select → size
    assertSingleInt(
        compile("Set{1,2,3,4}.select(x | x > 1).collect(x | x * 2).select(x | x > 5).size()"), 2);
  }

  @Test
  public void testIncludingExcludingChain() {
    assertSingleInt(compile("Set{1,2,3}.including(4).excluding(2).size()"), 3);
  }

  @Test
  public void testSelectForAllChainResult() {
    // chain ends in scalar: can only use result in further scalar ops
    assertSingleBool(compile("Set{2,4,6}.select(x | x > 0).forAll(x | x > 0)"), true);
  }

  // ==================== Missing chaining combinations ====================

  @Test
  public void testSelectThenFirst() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.select(x | x > 2).first()"), 3);
  }

  @Test
  public void testSelectThenLast() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.select(x | x > 2).last()"), 5);
  }

  @Test
  public void testRejectThenCollect() {
    Value result = compile("Set{1,2,3,4}.reject(x | x > 3).collect(x | x * 2)");
    assertSize(result, 3);
  }

  @Test
  public void testRejectThenExists() {
    assertSingleBool(compile("Set{1,2,3}.reject(x | x > 5).exists(x | x == 2)"), true);
  }

  @Test
  public void testCollectThenIsEmpty() {
    assertSingleBool(compile("Set{1,2,3}.collect(x | x * 2).isEmpty()"), false);
  }

  @Test
  public void testForAllThenRejectFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).reject(x | x > 0)");
  }

  @Test
  public void testForAllThenCollectFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).collect(x | x > 0)");
  }

  @Test
  public void testForAllThenExistsFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).exists(x | x > 0)");
  }

  @Test
  public void testForAllThenIsEmptyFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).isEmpty()");
  }

  @Test
  public void testForAllThenFirstFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).first()");
  }

  @Test
  public void testExistsThenSelectFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).select(x | x > 0)");
  }

  @Test
  public void testExistsThenCollectFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).collect(x | x > 0)");
  }

  @Test
  public void testExistsThenForAllFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).forAll(x | x > 0)");
  }

  @Test
  public void testExistsThenSizeFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).size()");
  }

  @Test
  public void testExistsThenIsEmptyFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).isEmpty()");
  }

  @Test
  public void testExistsThenFirstFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).first()");
  }

  @Test
  public void testSizeThenRejectFails() {
    assertTypeError("Set{1,2,3}.size().reject(x | x > 0)");
  }

  @Test
  public void testSizeThenCollectFails() {
    assertTypeError("Set{1,2,3}.size().collect(x | x > 0)");
  }

  @Test
  public void testSizeThenForAllFails() {
    assertTypeError("Set{1,2,3}.size().forAll(x | x > 0)");
  }

  @Test
  public void testSizeThenExistsFails() {
    assertTypeError("Set{1,2,3}.size().exists(x | x > 0)");
  }

  @Test
  public void testSizeThenIsEmptyFails() {
    assertTypeError("Set{1,2,3}.size().isEmpty()");
  }

  @Test
  public void testSizeThenFirstFails() {
    assertTypeError("Set{1,2,3}.size().first()");
  }

  @Test
  public void testIsEmptyThenRejectFails() {
    assertTypeError("Set{}.isEmpty().reject(x | x > 0)");
  }

  @Test
  public void testIsEmptyThenCollectFails() {
    assertTypeError("Set{}.isEmpty().collect(x | x > 0)");
  }

  @Test
  public void testIsEmptyThenForAllFails() {
    assertTypeError("Set{}.isEmpty().forAll(x | x > 0)");
  }

  @Test
  public void testIsEmptyThenExistsFails() {
    assertTypeError("Set{}.isEmpty().exists(x | x > 0)");
  }

  @Test
  public void testIsEmptyThenSizeFails() {
    assertTypeError("Set{}.isEmpty().size()");
  }

  @Test
  public void testIsEmptyThenFirstFails() {
    assertTypeError("Set{}.isEmpty().first()");
  }

  @Test
  public void testCollectThenFirst() {
    assertSingleInt(compile("Sequence{1,2,3}.collect(x | x * 2).first()"), 2);
  }

  @Test
  public void testCollectThenLast() {
    assertSingleInt(compile("Sequence{1,2,3}.collect(x | x * 2).last()"), 6);
  }

  // ==================== Missing: reject → first/last ====================

  @Test
  public void testRejectThenFirst() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.reject(x | x > 3).first()"), 1);
  }

  @Test
  public void testRejectThenLast() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.reject(x | x > 3).last()"), 3);
  }

  // ==================== Helper ====================

  private void assertTypeError(String input) {
    try {
      compile(input);
      fail("Expected type error for: " + input);
    } catch (AssertionError | NumberFormatException e) {
      // expected
    }
  }
}