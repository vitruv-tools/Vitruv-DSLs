/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.chaining;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  // ==================== Parameterized: type errors ====================

  @ParameterizedTest
  @MethodSource("typeErrorExpressions")
  void testTypeError(String expr) {
    assertTypeError(expr);
  }

  static Stream<String> typeErrorExpressions() {
    return Stream.of(
        // flatten on flat collection
        "Set{1,2,3}.select(x | x > 1).flatten()",
        "Set{1,2,3}.flatten().select(x | x > 1)",
        "Set{1,2,3}.flatten().size()",
        "Set{1,2,3}.flatten().collect(x | x * 2)",
        // forAll → Boolean, cannot chain collection ops
        "Set{1,2}.forAll(x | x > 0).select(x | x > 0)",
        "Set{1,2}.forAll(x | x > 0).size()",
        "Set{1,2}.forAll(x | x > 0).reject(x | x > 0)",
        "Set{1,2}.forAll(x | x > 0).collect(x | x > 0)",
        "Set{1,2}.forAll(x | x > 0).exists(x | x > 0)",
        "Set{1,2}.forAll(x | x > 0).isEmpty()",
        "Set{1,2}.forAll(x | x > 0).first()",
        // exists → Boolean, cannot chain collection ops
        "Set{1,2}.exists(x | x > 0).reject(x | x > 0)",
        "Set{1,2}.exists(x | x > 0).select(x | x > 0)",
        "Set{1,2}.exists(x | x > 0).collect(x | x > 0)",
        "Set{1,2}.exists(x | x > 0).forAll(x | x > 0)",
        "Set{1,2}.exists(x | x > 0).size()",
        "Set{1,2}.exists(x | x > 0).isEmpty()",
        "Set{1,2}.exists(x | x > 0).first()",
        // size → Integer, cannot chain collection ops
        "Set{1,2,3}.size().select(x | x > 0)",
        "Set{1,2,3}.size().reject(x | x > 0)",
        "Set{1,2,3}.size().collect(x | x > 0)",
        "Set{1,2,3}.size().forAll(x | x > 0)",
        "Set{1,2,3}.size().exists(x | x > 0)",
        "Set{1,2,3}.size().isEmpty()",
        "Set{1,2,3}.size().first()",
        // isEmpty → Boolean, cannot chain collection ops
        "Set{}.isEmpty().select(x | x > 0)",
        "Set{}.isEmpty().reject(x | x > 0)",
        "Set{}.isEmpty().collect(x | x > 0)",
        "Set{}.isEmpty().forAll(x | x > 0)",
        "Set{}.isEmpty().exists(x | x > 0)",
        "Set{}.isEmpty().size()",
        "Set{}.isEmpty().first()");
  }

  // ==================== Parameterized: assertSingleBool ====================

  @ParameterizedTest
  @MethodSource("singleBoolExpressions")
  void testSingleBool(String expr, boolean expected) {
    assertSingleBool(compile(expr), expected);
  }

  static Stream<Arguments> singleBoolExpressions() {
    return Stream.of(
        Arguments.of("Set{2,4,6}.select(x | x > 0).forAll(x | x > 0)", true),
        Arguments.of("Set{1,2,3}.select(x | x > 1).exists(x | x == 2)", true),
        Arguments.of("Set{1,2,3}.select(x | x > 10).isEmpty()", true),
        Arguments.of("Set{1,2,3}.reject(x | x > 3).forAll(x | x <= 3)", true),
        Arguments.of("Set{1,2,3}.reject(x | x >= 1).isEmpty()", true),
        Arguments.of("Set{1,2,3}.collect(x | x * 2).forAll(x | x > 0)", true),
        Arguments.of("Set{1,2,3}.collect(x | x + 10).exists(x | x == 12)", true),
        Arguments.of("Set{1,2,3}.collect(x | x * 2).isEmpty()", false),
        Arguments.of("Sequence{10,20,30}.first() > 5", true),
        Arguments.of("Sequence{10,20,30}.last() == 30", true),
        Arguments.of("Set{2,4,6}.forAll(x | x > 0) and true", true),
        Arguments.of("Set{1,2,3}.exists(x | x == 2) or false", true),
        Arguments.of("Set{1,2,3}.size() > 2", true),
        Arguments.of("Set{}.isEmpty() and true", true),
        Arguments.of("Set{1,2,3}.reject(x | x > 5).exists(x | x == 2)", true));
  }

  // ==================== Parameterized: assertSingleInt ====================

  @ParameterizedTest
  @MethodSource("singleIntExpressions")
  void testSingleInt(String expr, int expected) {
    assertSingleInt(compile(expr), expected);
  }

  static Stream<Arguments> singleIntExpressions() {
    return Stream.of(
        Arguments.of("Set{1,2,3,4,5}.select(x | x > 2).size()", 3),
        Arguments.of("Set{1,2,3,4,5}.reject(x | x > 3).size()", 3),
        Arguments.of("Set{1,2,3}.collect(x | x * 2).size()", 3),
        Arguments.of("Set{1,2,3,4,5,6}.select(x | x > 1).reject(x | x > 4).size()", 3),
        Arguments.of(
            "Set{1,2,3,4}.select(x | x > 1).collect(x | x * 2).select(x | x > 5).size()", 2),
        Arguments.of("Set{1,2,3}.including(4).excluding(2).size()", 3),
        Arguments.of("Sequence{3,1,4}.first() + 10", 13),
        Arguments.of("Sequence{3,1,4}.last() * 2", 8),
        Arguments.of("Set{1,2,3}.size() + 10", 13),
        Arguments.of("Sequence{1,2,3,4,5}.select(x | x > 2).first()", 3),
        Arguments.of("Sequence{1,2,3,4,5}.select(x | x > 2).last()", 5),
        Arguments.of("Sequence{1,2,3}.collect(x | x * 2).first()", 2),
        Arguments.of("Sequence{1,2,3}.collect(x | x * 2).last()", 6),
        Arguments.of("Sequence{1,2,3,4,5}.reject(x | x > 3).first()", 1),
        Arguments.of("Sequence{1,2,3,4,5}.reject(x | x > 3).last()", 3));
  }

  // ==================== Parameterized: assertSize only ====================

  @ParameterizedTest
  @MethodSource("sizeOnlyExpressions")
  void testSizeOnly(String expr, int expected) {
    assertSize(compile(expr), expected);
  }

  static Stream<Arguments> sizeOnlyExpressions() {
    return Stream.of(
        Arguments.of("Set{1,2,3,4,5}.reject(x | x > 3).select(x | x > 1)", 2),
        Arguments.of("Set{1,2,3,4,5}.reject(x | x > 4).reject(x | x < 2)", 3),
        Arguments.of("Set{1,2,3,4}.reject(x | x > 3).collect(x | x * 2)", 3));
  }

  // ==================== Parameterized: assertSize + 2 includes ====================

  @ParameterizedTest
  @MethodSource("sizeTwoIncludesExpressions")
  void testSizeTwoIncludes(String expr, int size, int a, int b) {
    Value result = compile(expr);
    assertSize(result, size);
    assertIncludes(result, a);
    assertIncludes(result, b);
  }

  static Stream<Arguments> sizeTwoIncludesExpressions() {
    return Stream.of(
        Arguments.of("Set{1,2,3,4,5}.select(x | x > 1).reject(x | x > 3)", 2, 2, 3),
        Arguments.of("Set{1,2,3,4}.select(x | x > 2).collect(x | x * 2)", 2, 6, 8),
        Arguments.of("Set{1,2,3}.collect(x | x * 2).reject(x | x > 4)", 2, 2, 4));
  }

  // ==================== Parameterized: assertSize + 3 includes ====================

  @ParameterizedTest
  @MethodSource("sizeThreeIncludesExpressions")
  void testSizeThreeIncludes(String expr, int size, int a, int b, int c) {
    Value result = compile(expr);
    assertSize(result, size);
    assertIncludes(result, a);
    assertIncludes(result, b);
    assertIncludes(result, c);
  }

  static Stream<Arguments> sizeThreeIncludesExpressions() {
    return Stream.of(
        Arguments.of("Set{1,2,3,4,5}.select(x | x > 1).select(x | x < 5)", 3, 2, 3, 4),
        Arguments.of("Set{1,2,3}.collect(x | x * 2).collect(x | x + 1)", 3, 3, 5, 7));
  }

  // ==================== Standalone ====================

  @Test
  void testCollectThenSelect() {
    Value result = compile("Set{1,2,3}.collect(x | x * 2).select(x | x > 4)");
    assertSize(result, 1);
    assertIncludes(result, 6);
  }

  // ==================== Helper ====================

  private void assertTypeError(String input) {
    assertThrows(AssertionError.class, () -> compile(input), "Expected type error for: " + input);
  }
}
