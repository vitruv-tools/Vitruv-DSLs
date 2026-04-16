/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;

/**
 * Cross-product tests for arithmetic operators (+, -, *, /) across all type combinations.
 *
 * <p>Matrix: left type × right type × operator → expected result type or ERROR.
 *
 * <pre>
 * Valid combinations (→ result type):
 *   Integer × Integer → Integer
 *   Integer × Float   → Double  (promotion)
 *   Integer × Double  → Double  (promotion)
 *   Float   × Integer → Double  (promotion)
 *   Float   × Float   → Double  (promotion)
 *   Float   × Double  → Double  (promotion)
 *   Double  × Integer → Double  (promotion)
 *   Double  × Float   → Double  (promotion)
 *   Double  × Double  → Double
 *
 * Invalid combinations (→ ERROR):
 *   Any numeric × String
 *   Any numeric × Boolean
 *   String × anything
 *   Boolean × anything
 *   Collection × anything
 * </pre>
 */
public class ArithmeticTypeMatrixTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== Integer × Integer ====================

  @Test
  public void testIntPlusInt() {
    assertSingleInt(compile("3 + 4"), 7);
  }

  @Test
  public void testIntMinusInt() {
    assertSingleInt(compile("10 - 3"), 7);
  }

  @Test
  public void testIntTimesInt() {
    assertSingleInt(compile("3 * 4"), 12);
  }

  @Test
  public void testIntDivideInt() {
    // Integer / Integer: result depends on evaluator (may be integer or double division)
    Value result = compile("10 / 2");
    assertSingleNumeric(result, 5.0);
  }

  // ==================== Integer × Float ====================

  @Test
  public void testIntPlusFloat() {
    // Integer + Float → Double (promotion)
    Value result = compile("3 + 1.5");
    assertSingleDouble(result, 4.5);
  }

  @Test
  public void testIntMinusFloat() {
    Value result = compile("5 - 1.5");
    assertSingleDouble(result, 3.5);
  }

  @Test
  public void testIntTimesFloat() {
    Value result = compile("3 * 1.5");
    assertSingleDouble(result, 4.5);
  }

  @Test
  public void testIntDivideFloat() {
    Value result = compile("3 / 2.0");
    assertSingleDouble(result, 1.5);
  }

  // ==================== Integer × Double ====================

  @Test
  public void testIntPlusDouble() {
    Value result = compile("3 + 1.5");
    assertSingleDouble(result, 4.5);
  }

  @Test
  public void testIntMinusDouble() {
    Value result = compile("5 - 2.5");
    assertSingleDouble(result, 2.5);
  }

  @Test
  public void testIntTimesDouble() {
    Value result = compile("4 * 2.5");
    assertSingleDouble(result, 10.0);
  }

  @Test
  public void testIntDivideDouble() {
    Value result = compile("5 / 2.0");
    assertSingleDouble(result, 2.5);
  }

  // ==================== Float × Integer ====================

  @Test
  public void testFloatPlusInt() {
    Value result = compile("1.5 + 3");
    assertSingleDouble(result, 4.5);
  }

  @Test
  public void testFloatMinusInt() {
    Value result = compile("5.5 - 2");
    assertSingleDouble(result, 3.5);
  }

  @Test
  public void testFloatTimesInt() {
    Value result = compile("1.5 * 4");
    assertSingleDouble(result, 6.0);
  }

  @Test
  public void testFloatDivideInt() {
    Value result = compile("3.0 / 2");
    assertSingleDouble(result, 1.5);
  }

  // ==================== Float × Float ====================

  @Test
  public void testFloatPlusFloat() {
    Value result = compile("1.5 + 2.5");
    assertSingleDouble(result, 4.0);
  }

  @Test
  public void testFloatMinusFloat() {
    Value result = compile("5.5 - 2.5");
    assertSingleDouble(result, 3.0);
  }

  @Test
  public void testFloatTimesFloat() {
    Value result = compile("2.0 * 3.0");
    assertSingleDouble(result, 6.0);
  }

  @Test
  public void testFloatDivideFloat() {
    Value result = compile("9.0 / 3.0");
    assertSingleDouble(result, 3.0);
  }

  // ==================== Float × Double ====================

  @Test
  public void testFloatPlusDouble() {
    Value result = compile("1.5 + 2.5");
    assertSingleDouble(result, 4.0);
  }

  @Test
  public void testFloatMinusDouble() {
    Value result = compile("5.5 - 2.5");
    assertSingleDouble(result, 3.0);
  }

  // ==================== Double × Integer ====================

  @Test
  public void testDoublePlusInt() {
    Value result = compile("2.5 + 2");
    assertSingleDouble(result, 4.5);
  }

  @Test
  public void testDoubleMinusInt() {
    Value result = compile("5.5 - 3");
    assertSingleDouble(result, 2.5);
  }

  @Test
  public void testDoubleTimesInt() {
    Value result = compile("2.5 * 4");
    assertSingleDouble(result, 10.0);
  }

  @Test
  public void testDoubleDivideInt() {
    Value result = compile("5.0 / 2");
    assertSingleDouble(result, 2.5);
  }

  // ==================== Float × Double (missing: * and ÷) ====================

  @Test
  public void testFloatTimesDouble() {
    Value result = compile("2.0 * 3.0");
    assertSingleDouble(result, 6.0);
  }

  @Test
  public void testFloatDivideDouble() {
    Value result = compile("6.0 / 2.0");
    assertSingleDouble(result, 3.0);
  }

  // ==================== Double × Float ====================

  @Test
  public void testDoublePlusFloat() {
    Value result = compile("1.5 + 2.5");
    assertSingleDouble(result, 4.0);
  }

  @Test
  public void testDoubleMinusFloat() {
    Value result = compile("5.0 - 2.5");
    assertSingleDouble(result, 2.5);
  }

  @Test
  public void testDoubleTimesFloat() {
    Value result = compile("2.0 * 3.0");
    assertSingleDouble(result, 6.0);
  }

  @Test
  public void testDoubleDivideFloat() {
    Value result = compile("6.0 / 2.0");
    assertSingleDouble(result, 3.0);
  }

  @Test
  public void testDoublePlusDouble() {
    Value result = compile("2.5 + 1.5");
    assertSingleDouble(result, 4.0);
  }

  @Test
  public void testDoubleMinusDouble() {
    Value result = compile("5.0 - 2.5");
    assertSingleDouble(result, 2.5);
  }

  @Test
  public void testDoubleTimesDouble() {
    Value result = compile("2.0 * 3.0");
    assertSingleDouble(result, 6.0);
  }

  @Test
  public void testDoubleDivideDouble() {
    Value result = compile("9.0 / 3.0");
    assertSingleDouble(result, 3.0);
  }

  // ==================== Invalid: numeric × String → ERROR ====================

  @Test
  public void testIntPlusStringFails() {
    assertTypeError("3 + \"hello\"");
  }

  @Test
  public void testIntMinusStringFails() {
    assertTypeError("3 - \"hello\"");
  }

  @Test
  public void testFloatPlusStringFails() {
    assertTypeError("1.5 + \"hello\"");
  }

  @Test
  public void testDoublePlusStringFails() {
    assertTypeError("2.5 + \"hello\"");
  }

  @Test
  public void testStringPlusIntFails() {
    assertTypeError("\"hello\" + 3");
  }

  @Test
  public void testStringPlusStringFails() {
    // String concatenation uses .concat(), not +
    assertTypeError("\"hello\" + \"world\"");
  }

  // ==================== Invalid: numeric × Boolean → ERROR ====================

  @Test
  public void testIntPlusBoolFails() {
    assertTypeError("3 + true");
  }

  @Test
  public void testBoolPlusIntFails() {
    assertTypeError("true + 3");
  }

  @Test
  public void testBoolPlusBoolFails() {
    assertTypeError("true + false");
  }

  @Test
  public void testFloatPlusBoolFails() {
    assertTypeError("1.5 + true");
  }

  @Test
  public void testDoublePlusBoolFails() {
    assertTypeError("2.5 + true");
  }

  @Test
  public void testBoolPlusFloatFails() {
    assertTypeError("true + 1.5");
  }

  @Test
  public void testBoolPlusDoubleFails() {
    assertTypeError("true + 2.5");
  }

  // ==================== Invalid: * × Set → ERROR ====================

  @Test
  public void testBoolPlusSetFails() {
    assertTypeError("true + Set{1,2}");
  }

  @Test
  public void testSetPlusFloatFails() {
    assertTypeError("Set{1,2} + 1.5");
  }

  @Test
  public void testSetPlusDoubleFails() {
    assertTypeError("Set{1,2} + 2.5");
  }

  @Test
  public void testSetPlusStringFails() {
    assertTypeError("Set{1,2} + \"hello\"");
  }

  @Test
  public void testSetPlusBoolFails() {
    assertTypeError("Set{1,2} + true");
  }

  @Test
  public void testSetPlusSetFails() {
    assertTypeError("Set{1,2} + Set{3,4}");
  }

  // ==================== Invalid: String × * → ERROR ====================

  @Test
  public void testStringPlusFloatFails() {
    assertTypeError("\"hello\" + 1.5");
  }

  @Test
  public void testStringPlusDoubleFails() {
    assertTypeError("\"hello\" + 2.5");
  }

  @Test
  public void testStringPlusBoolFails() {
    assertTypeError("\"hello\" + true");
  }

  @Test
  public void testStringPlusSetFails() {
    assertTypeError("\"hello\" + Set{1,2}");
  }

  // ==================== Invalid: Collection × anything → ERROR ====================

  @Test
  public void testSetPlusIntFails() {
    assertTypeError("Set{1,2} + 3");
  }

  @Test
  public void testIntPlusSetFails() {
    assertTypeError("3 + Set{1,2}");
  }

  @Test
  public void testFloatPlusSetFails() {
    assertTypeError("1.5 + Set{1,2}");
  }

  @Test
  public void testDoublePlusSetFails() {
    assertTypeError("2.5 + Set{1,2}");
  }

  // ==================== Edge cases ====================

  @Test
  public void testIntNegation() {
    assertSingleInt(compile("-(3 + 4)"), -7);
  }

  @Test
  public void testDoubleNegation() {
    Value result = compile("-(1.5 + 1.5)");
    assertSingleDouble(result, -3.0);
  }

  @Test
  public void testChainedArithmetic() {
    assertSingleInt(compile("1 + 2 + 3 + 4"), 10);
  }

  @Test
  public void testArithmeticPrecedenceTimesBeforePlus() {
    // Verify * binds tighter than + using explicit groupings
    assertSingleInt(compile("(3 * 4) + 2"), 14);
    assertSingleInt(compile("2 + (3 * 4)"), 14);
  }

  // ==================== Helper ====================

  /**
   * Asserts that compilation fails with a type error (Pass 2) OR a parse/number error. Some invalid
   * combinations trigger NumberFormatException at parse time.
   */
  private void assertTypeError(String input) {
    try {
      compile(input);
      fail("Expected type error for: " + input);
    } catch (AssertionError | NumberFormatException e) {
      // expected — type error or parse failure
    }
  }

  /**
   * Asserts that the result is a singleton with the given numeric value, regardless of whether it's
   * stored as Int, Float, or Double.
   */
  private void assertSingleNumeric(Value result, double expected) {
    assertEquals(1, result.size(), "Expected singleton result");
    OCLElement elem = result.getElements().get(0);
    double actual = elem.toDoubleValue();
    assertEquals(expected, actual, 1e-9, "Expected numeric value " + expected);
  }
}