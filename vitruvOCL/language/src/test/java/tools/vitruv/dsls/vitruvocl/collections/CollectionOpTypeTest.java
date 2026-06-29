/*******************************************************************************.
 * Copyright (c) 2026 Max Oesterle
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;

/**
 * Cross-product tests for collection operations across all receiver and element types.
 *
 * <pre>
 * Receiver × Operation:
 *   Set{T}      → size, isEmpty, notEmpty, includes, excluding,
 *                    including, union, sum*, max*, min*, avg*, flatten
 *   Sequence{T} → size, isEmpty, notEmpty, includes, excluding,
 *                 including, first, last, reverse, sum*, max*, min*, avg*, flatten
 *   Bag{T}      → size, isEmpty, notEmpty, includes,
 *                    excluding, including, sum*, max*, min*, avg*, flatten
 *   Integer     → abs, floor, ceil, round (scalar numeric ops in collectionOpCS)
 *   Float       → abs, floor, ceil, round
 *   Double      → abs, floor, ceil, round
 *   String      → size (via stringOpCS)
 *
 * * = only valid when T is numeric (Integer, Float, Double)
 * </pre>
 */
class CollectionOpTypeTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== size() ====================

  @Test
  void testSetIntSize() {
    assertSingleInt(compile("Set{1,2,3}.size()"), 3);
  }

  @Test
  void testSequenceIntSize() {
    assertSingleInt(compile("Sequence{1,2,3}.size()"), 3);
  }

  @Test
  void testBagIntSize() {
    assertSingleInt(compile("Bag{1,2,2,3}.size()"), 4);
  }

  @Test
  void testSetStringSize() {
    assertSingleInt(compile("Set{\"a\",\"b\",\"c\"}.size()"), 3);
  }

  @Test
  void testSetBoolSize() {
    assertSingleInt(compile("Set{true,false}.size()"), 2);
  }

  @Test
  void testEmptySetSize() {
    assertSingleInt(compile("Set{}.size()"), 0);
  }

  @Test
  void testIntSizeOnScalarFails() {
    assertTypeError("3.size()");
  }

  // ==================== isEmpty / notEmpty ====================

  @Test
  void testEmptySetIsEmpty() {
    assertSingleBool(compile("Set{}.isEmpty()"), true);
  }

  @Test
  void testNonEmptySetIsEmpty() {
    assertSingleBool(compile("Set{1}.isEmpty()"), false);
  }

  @Test
  void testEmptySetNotEmpty() {
    assertSingleBool(compile("Set{}.notEmpty()"), false);
  }

  @Test
  void testNonEmptySetNotEmpty() {
    assertSingleBool(compile("Set{1,2}.notEmpty()"), true);
  }

  @Test
  void testSequenceIsEmpty() {
    assertSingleBool(compile("Sequence{}.isEmpty()"), true);
  }

  @Test
  void testBagIsEmpty() {
    assertSingleBool(compile("Bag{}.isEmpty()"), true);
  }

  @Test
  void testIntIsEmptyFails() {
    assertTypeError("3.isEmpty()");
  }

  @Test
  void testStringIsEmptyFails() {
    assertTypeError("\"hello\".isEmpty()");
  }

  // ==================== includes / excludes ====================

  @Test
  void testSetIncludesIntFound() {
    assertSingleBool(compile("Set{1,2,3}.includes(2)"), true);
  }

  @Test
  void testSetIncludesIntNotFound() {
    assertSingleBool(compile("Set{1,2,3}.includes(5)"), false);
  }

  @Test
  void testSequenceIncludesInt() {
    assertSingleBool(compile("Sequence{1,2,3}.includes(3)"), true);
  }

  @Test
  void testBagIncludesInt() {
    assertSingleBool(compile("Bag{1,2,2}.includes(2)"), true);
  }

  @Test
  void testSetIncludesStringFound() {
    assertSingleBool(compile("Set{\"a\",\"b\"}.includes(\"a\")"), true);
  }

  @Test
  void testSetExcludesIntFound() {
    assertSingleBool(compile("Set{1,2,3}.excludes(5)"), true);
  }

  @Test
  void testSetExcludesIntNotFound() {
    assertSingleBool(compile("Set{1,2,3}.excludes(2)"), false);
  }

  // ==================== including / excluding ====================

  @Test
  void testSetIncluding() {
    Value result = compile("Set{1,2}.including(3)");
    assertSize(result, 3);
    assertIncludes(result, 3);
  }

  @Test
  void testSetIncludingDuplicate() {
    // Set: adding duplicate should not increase size
    Value result = compile("Set{1,2}.including(2)");
    assertSize(result, 2);
  }

  @Test
  void testSequenceIncluding() {
    Value result = compile("Sequence{1,2}.including(3)");
    assertSize(result, 3);
  }

  @Test
  void testBagIncluding() {
    Value result = compile("Bag{1,2}.including(2)");
    assertSize(result, 3); // Bag allows duplicates
  }

  @Test
  void testSetExcluding() {
    Value result = compile("Set{1,2,3}.excluding(2)");
    assertSize(result, 2);
    assertExcludes(result, 2);
  }

  @Test
  void testSetExcludingNonExistent() {
    // excluding element not in set → unchanged
    Value result = compile("Set{1,2,3}.excluding(5)");
    assertSize(result, 3);
  }

  @Test
  void testSequenceExcluding() {
    Value result = compile("Sequence{1,2,3}.excluding(2)");
    assertSize(result, 2);
  }

  // ==================== first / last (Sequence only) ====================

  @Test
  void testSequenceFirst() {
    assertSingleInt(compile("Sequence{10,20,30}.first()"), 10);
  }

  @Test
  void testSequenceLast() {
    assertSingleInt(compile("Sequence{10,20,30}.last()"), 30);
  }

  @Test
  void testSequenceFirstSingleton() {
    assertSingleInt(compile("Sequence{42}.first()"), 42);
  }

  // ==================== sum (numeric collections only) ====================

  @Test
  void testSetIntSum() {
    assertSingleInt(compile("Set{1,2,3}.sum()"), 6);
  }

  @Test
  void testSequenceIntSum() {
    assertSingleInt(compile("Sequence{1,2,3,4}.sum()"), 10);
  }

  @Test
  void testBagIntSum() {
    assertSingleInt(compile("Bag{1,2,2}.sum()"), 5);
  }

  @Test
  void testEmptySetSum() {
    assertSingleInt(compile("Set{}.sum()"), 0);
  }

  @Test
  void testSetStringSumFails() {
    assertTypeError("Set{\"a\",\"b\"}.sum()");
  }

  @Test
  void testSetBoolSumFails() {
    assertTypeError("Set{true,false}.sum()");
  }

  // ==================== max / min (numeric collections only) ====================

  @Test
  void testSetIntMax() {
    assertSingleInt(compile("Set{3,1,4,1,5}.max()"), 5);
  }

  @Test
  void testSetIntMin() {
    assertSingleInt(compile("Set{3,1,4,1,5}.min()"), 1);
  }

  @Test
  void testSequenceIntMax() {
    assertSingleInt(compile("Sequence{10,30,20}.max()"), 30);
  }

  @Test
  void testSequenceIntMin() {
    assertSingleInt(compile("Sequence{10,30,20}.min()"), 10);
  }

  @Test
  void testSetStringMaxFails() {
    assertTypeError("Set{\"a\",\"b\"}.max()");
  }

  @Test
  void testSetBoolMinFails() {
    assertTypeError("Set{true,false}.min()");
  }

  // ==================== avg (numeric, returns Double) ====================

  @Test
  void testSetIntAvg() {
    assertSingleDouble(compile("Set{2,4,6}.avg()"), 4.0);
  }

  @Test
  void testSequenceIntAvg() {
    assertSingleDouble(compile("Sequence{1,2,3,4}.avg()"), 2.5);
  }

  @Test
  void testSetStringAvgFails() {
    assertTypeError("Set{\"a\",\"b\"}.avg()");
  }

  // ==================== flatten ====================
  // Note: flatten() in this implementation requires nested collections as input.
  // Calling flatten() on a flat collection (Set{Integer}) is a type error.

  @Test
  void testSetFlattenOnNestedFails() {
    assertTypeError("Set{1,2,3}.flatten()");
  }

  @Test
  void testSequenceFlattenOnNestedFails() {
    assertTypeError("Sequence{1,2,3}.flatten()");
  }

  @Test
  void testBagFlattenOnNestedFails() {
    assertTypeError("Bag{1,2,2}.flatten()");
  }

  // ==================== abs / floor / ceil / round (scalar numeric) ====================

  @Test
  void testIntAbsPositive() {
    assertSingleInt(compile("5.abs()"), 5);
  }

  @Test
  void testIntAbsNegative() {
    assertSingleInt(compile("(-5).abs()"), 5);
  }

  @Test
  void testIntFloor() {
    assertSingleInt(compile("5.floor()"), 5);
  }

  @Test
  void testIntCeil() {
    assertSingleInt(compile("5.ceil()"), 5);
  }

  @Test
  void testIntRound() {
    assertSingleInt(compile("5.round()"), 5);
  }

  @Test
  void testDoubleFloor() {
    assertSingleDouble(compile("2.7.floor()"), 2.0);
  }

  @Test
  void testDoubleCeil() {
    assertSingleDouble(compile("2.3.ceil()"), 3.0);
  }

  @Test
  void testDoubleRoundUp() {
    assertSingleDouble(compile("2.5.round()"), 3.0);
  }

  @Test
  void testDoubleRoundDown() {
    assertSingleDouble(compile("2.4.round()"), 2.0);
  }

  @Test
  void testDoubleAbs() {
    assertSingleDouble(compile("2.5.abs()"), 2.5);
  }

  @Test
  void testStringAbsFails() {
    assertTypeError("\"hello\".abs()");
  }

  @Test
  void testBoolFloorFails() {
    assertTypeError("true.floor()");
  }

  @Test
  void testSetAbsFails() {
    assertTypeError("Set{1,2}.abs()");
  }

  // ==================== sum/max/min on Float/Double collections ====================

  @Test
  void testSetDoubleSum() {
    assertSingleDouble(compile("Set{1.0,2.0,3.0}.sum()"), 6.0);
  }

  @Test
  void testSequenceDoubleSum() {
    assertSingleDouble(compile("Sequence{1.0,2.0,3.0}.sum()"), 6.0);
  }

  @Test
  void testSetDoubleMax() {
    assertSingleDouble(compile("Set{1.5,3.5,2.0}.max()"), 3.5);
  }

  @Test
  void testSetDoubleMin() {
    assertSingleDouble(compile("Set{1.5,3.5,2.0}.min()"), 1.5);
  }

  // ==================== including/excluding with wrong arg type → ERROR ====================

  @Test
  void testSetIntIncludingStringFails() {
    assertTypeError("Set{1,2}.including(\"hello\")");
  }

  @Test
  void testSetIntIncludingBoolFails() {
    assertTypeError("Set{1,2}.including(true)");
  }

  @Test
  void testSetStringIncludingIntFails() {
    assertTypeError("Set{\"a\",\"b\"}.including(3)");
  }

  // ==================== size() on Float/Double receiver → ERROR ====================

  @Test
  void testFloatSizeOnScalarFails() {
    assertTypeError("1.5.size()");
  }

  @Test
  void testDoubleSizeOnScalarFails() {
    assertTypeError("2.5.size()");
  }

  // ==================== Bag max/min/avg ====================

  @Test
  void testBagIntMax() {
    assertSingleInt(compile("Bag{3,1,4,1,5}.max()"), 5);
  }

  @Test
  void testBagIntMin() {
    assertSingleInt(compile("Bag{3,1,4,1,5}.min()"), 1);
  }

  @Test
  void testBagIntAvg() {
    assertSingleDouble(compile("Bag{2,4,6}.avg()"), 4.0);
  }

  // ==================== first/last on Set/Bag → ERROR ====================

  @Test
  void testSetFirstFails() {
    assertTypeError("Set{1,2,3}.first()");
  }

  @Test
  void testSetLastFails() {
    assertTypeError("Set{1,2,3}.last()");
  }

  @Test
  void testBagFirstFails() {
    assertTypeError("Bag{1,2,3}.first()");
  }

  @Test
  void testBagLastFails() {
    assertTypeError("Bag{1,2,3}.last()");
  }

  // ==================== abs/floor/ceil/round on Collection → ERROR ====================

  @Test
  void testSequenceAbsFails() {
    assertTypeError("Sequence{1,2,3}.abs()");
  }

  @Test
  void testBagAbsFails() {
    assertTypeError("Bag{1,2,3}.abs()");
  }

  @Test
  void testSequenceFloorFails() {
    assertTypeError("Sequence{1,2,3}.floor()");
  }

  @Test
  void testBagCeilFails() {
    assertTypeError("Bag{1,2,3}.ceil()");
  }

  @Test
  void testSequenceRoundFails() {
    assertTypeError("Sequence{1,2,3}.round()");
  }

  // ==================== Helper ====================

  private void assertTypeError(String input) {
    assertThrows(AssertionError.class, () -> compile(input),
        "Expected type error for: " + input);
  }
}
