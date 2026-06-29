/*******************************************************************************.
 * Copyright (c) 2026 Max Oesterle
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.chaining;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;

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
class CollectionChainingTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== select → * ====================

  @Test
  void testSelectThenSelect() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 1).select(x | x < 5)");
    assertSize(result, 3);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
    assertIncludes(result, 4);
  }

  @Test
  void testSelectThenReject() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 1).reject(x | x > 3)");
    assertSize(result, 2);
    assertIncludes(result, 2);
    assertIncludes(result, 3);
  }

  @Test
  void testSelectThenCollect() {
    // select evens, then collect doubled values
    Value result = compile("Set{1,2,3,4}.select(x | x > 2).collect(x | x * 2)");
    assertSize(result, 2);
    assertIncludes(result, 6);
    assertIncludes(result, 8);
  }

  @Test
  void testSelectThenForAll() {
    assertSingleBool(compile("Set{2,4,6}.select(x | x > 0).forAll(x | x > 0)"), true);
  }

  @Test
  void testSelectThenExists() {
    assertSingleBool(compile("Set{1,2,3}.select(x | x > 1).exists(x | x == 2)"), true);
  }

  @Test
  void testSelectThenSize() {
    assertSingleInt(compile("Set{1,2,3,4,5}.select(x | x > 2).size()"), 3);
  }

  @Test
  void testSelectThenIsEmpty() {
    assertSingleBool(compile("Set{1,2,3}.select(x | x > 10).isEmpty()"), true);
  }

  @Test
  void testSelectThenFlattentypeError() {
    // select() on Set{Integer} → Set{Integer}; flatten() on flat collection is a type error
    assertTypeError("Set{1,2,3}.select(x | x > 1).flatten()");
  }

  // ==================== reject → * ====================

  @Test
  void testRejectThenSelect() {
    Value result = compile("Set{1,2,3,4,5}.reject(x | x > 3).select(x | x > 1)");
    assertSize(result, 2);
  }

  @Test
  void testRejectThenReject() {
    Value result = compile("Set{1,2,3,4,5}.reject(x | x > 4).reject(x | x < 2)");
    assertSize(result, 3);
  }

  @Test
  void testRejectThenSize() {
    assertSingleInt(compile("Set{1,2,3,4,5}.reject(x | x > 3).size()"), 3);
  }

  @Test
  void testRejectThenForAll() {
    assertSingleBool(compile("Set{1,2,3}.reject(x | x > 3).forAll(x | x <= 3)"), true);
  }

  @Test
  void testRejectThenIsEmpty() {
    assertSingleBool(compile("Set{1,2,3}.reject(x | x >= 1).isEmpty()"), true);
  }

  // ==================== collect → * ====================

  @Test
  void testCollectThenSelect() {
    // collect doubles, then select > 4
    Value result = compile("Set{1,2,3}.collect(x | x * 2).select(x | x > 4)");
    assertSize(result, 1);
    assertIncludes(result, 6);
  }

  @Test
  void testCollectThenReject() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2).reject(x | x > 4)");
    assertSize(result, 2);
    assertIncludes(result, 2);
    assertIncludes(result, 4);
  }

  @Test
  void testCollectThenCollect() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2).collect(x | x + 1)");
    assertSize(result, 3);
    assertIncludes(result, 3);
    assertIncludes(result, 5);
    assertIncludes(result, 7);
  }

  @Test
  void testCollectThenSize() {
    assertSingleInt(compile("Set{1,2,3}.collect(x | x * 2).size()"), 3);
  }

  @Test
  void testCollectThenForAll() {
    assertSingleBool(compile("Set{1,2,3}.collect(x | x * 2).forAll(x | x > 0)"), true);
  }

  @Test
  void testCollectThenExists() {
    assertSingleBool(compile("Set{1,2,3}.collect(x | x + 10).exists(x | x == 12)"), true);
  }

  // ==================== flatten → * ====================
  // Note: flatten() requires nested collections. On flat collections it is a type error.
  // These tests use select() to produce a collection, then flatten() — which is valid
  // only if the element type is itself a collection. We test flatten on valid chaining instead.

  @Test
  void testFlattenThenSelecttypeError() {
    assertTypeError("Set{1,2,3}.flatten().select(x | x > 1)");
  }

  @Test
  void testFlattenThenSizetypeError() {
    assertTypeError("Set{1,2,3}.flatten().size()");
  }

  @Test
  void testFlattenThenCollecttypeError() {
    assertTypeError("Set{1,2,3}.flatten().collect(x | x * 2)");
  }

  // ==================== first/last → scalar ops ====================

  @Test
  void testFirstThenArithmetic() {
    // first() returns Integer → can do arithmetic
    assertSingleInt(compile("Sequence{3,1,4}.first() + 10"), 13);
  }

  @Test
  void testLastThenArithmetic() {
    assertSingleInt(compile("Sequence{3,1,4}.last() * 2"), 8);
  }

  @Test
  void testFirstThenComparison() {
    assertSingleBool(compile("Sequence{10,20,30}.first() > 5"), true);
  }

  @Test
  void testLastThenComparison() {
    assertSingleBool(compile("Sequence{10,20,30}.last() == 30"), true);
  }

  // ==================== forAll/exists → scalar, CANNOT chain collection ops ====================

  @Test
  void testForAllResultUsedInLogical() {
    // forAll → Boolean → can use in logical expression
    assertSingleBool(compile("Set{2,4,6}.forAll(x | x > 0) and true"), true);
  }

  @Test
  void testExistsResultUsedInLogical() {
    assertSingleBool(compile("Set{1,2,3}.exists(x | x == 2) or false"), true);
  }

  @Test
  void testForAllThenSelectFails() {
    // forAll returns Boolean, not a collection → cannot chain .select()
    assertTypeError("Set{1,2}.forAll(x | x > 0).select(x | x > 0)");
  }

  @Test
  void testForAllThenSizeFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).size()");
  }

  @Test
  void testExistsThenRejectFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).reject(x | x > 0)");
  }

  // ==================== size/isEmpty → scalar, CANNOT chain collection ops ====================

  @Test
  void testSizeThenArithmetic() {
    // size() → Integer → arithmetic is fine
    assertSingleInt(compile("Set{1,2,3}.size() + 10"), 13);
  }

  @Test
  void testSizeThenComparison() {
    assertSingleBool(compile("Set{1,2,3}.size() > 2"), true);
  }

  @Test
  void testSizeThenSelectFails() {
    assertTypeError("Set{1,2,3}.size().select(x | x > 0)");
  }

  @Test
  void testIsEmptyThenSelectFails() {
    assertTypeError("Set{}.isEmpty().select(x | x > 0)");
  }

  @Test
  void testIsEmptyResultInLogical() {
    assertSingleBool(compile("Set{}.isEmpty() and true"), true);
  }

  // ==================== Long chains ====================

  @Test
  void testThreeLevelChain() {
    // select → reject → size
    assertSingleInt(compile("Set{1,2,3,4,5,6}.select(x | x > 1).reject(x | x > 4).size()"), 3);
  }

  @Test
  void testFourLevelChain() {
    // select → collect → select → size
    assertSingleInt(
        compile("Set{1,2,3,4}.select(x | x > 1).collect(x | x * 2).select(x | x > 5).size()"), 2);
  }

  @Test
  void testIncludingExcludingChain() {
    assertSingleInt(compile("Set{1,2,3}.including(4).excluding(2).size()"), 3);
  }

  @Test
  void testSelectForAllChainResult() {
    // chain ends in scalar: can only use result in further scalar ops
    assertSingleBool(compile("Set{2,4,6}.select(x | x > 0).forAll(x | x > 0)"), true);
  }

  // ==================== Missing chaining combinations ====================

  @Test
  void testSelectThenFirst() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.select(x | x > 2).first()"), 3);
  }

  @Test
  void testSelectThenLast() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.select(x | x > 2).last()"), 5);
  }

  @Test
  void testRejectThenCollect() {
    Value result = compile("Set{1,2,3,4}.reject(x | x > 3).collect(x | x * 2)");
    assertSize(result, 3);
  }

  @Test
  void testRejectThenExists() {
    assertSingleBool(compile("Set{1,2,3}.reject(x | x > 5).exists(x | x == 2)"), true);
  }

  @Test
  void testCollectThenIsEmpty() {
    assertSingleBool(compile("Set{1,2,3}.collect(x | x * 2).isEmpty()"), false);
  }

  @Test
  void testForAllThenRejectFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).reject(x | x > 0)");
  }

  @Test
  void testForAllThenCollectFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).collect(x | x > 0)");
  }

  @Test
  void testForAllThenExistsFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).exists(x | x > 0)");
  }

  @Test
  void testForAllThenIsEmptyFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).isEmpty()");
  }

  @Test
  void testForAllThenFirstFails() {
    assertTypeError("Set{1,2}.forAll(x | x > 0).first()");
  }

  @Test
  void testExistsThenSelectFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).select(x | x > 0)");
  }

  @Test
  void testExistsThenCollectFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).collect(x | x > 0)");
  }

  @Test
  void testExistsThenForAllFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).forAll(x | x > 0)");
  }

  @Test
  void testExistsThenSizeFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).size()");
  }

  @Test
  void testExistsThenIsEmptyFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).isEmpty()");
  }

  @Test
  void testExistsThenFirstFails() {
    assertTypeError("Set{1,2}.exists(x | x > 0).first()");
  }

  @Test
  void testSizeThenRejectFails() {
    assertTypeError("Set{1,2,3}.size().reject(x | x > 0)");
  }

  @Test
  void testSizeThenCollectFails() {
    assertTypeError("Set{1,2,3}.size().collect(x | x > 0)");
  }

  @Test
  void testSizeThenForAllFails() {
    assertTypeError("Set{1,2,3}.size().forAll(x | x > 0)");
  }

  @Test
  void testSizeThenExistsFails() {
    assertTypeError("Set{1,2,3}.size().exists(x | x > 0)");
  }

  @Test
  void testSizeThenIsEmptyFails() {
    assertTypeError("Set{1,2,3}.size().isEmpty()");
  }

  @Test
  void testSizeThenFirstFails() {
    assertTypeError("Set{1,2,3}.size().first()");
  }

  @Test
  void testIsEmptyThenRejectFails() {
    assertTypeError("Set{}.isEmpty().reject(x | x > 0)");
  }

  @Test
  void testIsEmptyThenCollectFails() {
    assertTypeError("Set{}.isEmpty().collect(x | x > 0)");
  }

  @Test
  void testIsEmptyThenForAllFails() {
    assertTypeError("Set{}.isEmpty().forAll(x | x > 0)");
  }

  @Test
  void testIsEmptyThenExistsFails() {
    assertTypeError("Set{}.isEmpty().exists(x | x > 0)");
  }

  @Test
  void testIsEmptyThenSizeFails() {
    assertTypeError("Set{}.isEmpty().size()");
  }

  @Test
  void testIsEmptyThenFirstFails() {
    assertTypeError("Set{}.isEmpty().first()");
  }

  @Test
  void testCollectThenFirst() {
    assertSingleInt(compile("Sequence{1,2,3}.collect(x | x * 2).first()"), 2);
  }

  @Test
  void testCollectThenLast() {
    assertSingleInt(compile("Sequence{1,2,3}.collect(x | x * 2).last()"), 6);
  }

  // ==================== Missing: reject → first/last ====================

  @Test
  void testRejectThenFirst() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.reject(x | x > 3).first()"), 1);
  }

  @Test
  void testRejectThenLast() {
    assertSingleInt(compile("Sequence{1,2,3,4,5}.reject(x | x > 3).last()"), 3);
  }

  // ==================== Helper ====================

  private void assertTypeError(String input) {
    assertThrows(AssertionError.class, () -> compile(input),
        "Expected type error for: " + input);
  }
}
