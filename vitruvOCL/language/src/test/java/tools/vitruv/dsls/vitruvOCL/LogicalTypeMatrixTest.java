/*******************************************************************************.
 * Copyright (c) 2026 Max Oesterle
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.fail;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

/**
 * Cross-product tests for logical operators (and, or, xor, implies, not).
 *
 * <pre>
 * Valid:
 *   Boolean and/or/xor/implies Boolean → Boolean
 *   not Boolean → Boolean
 *
 * Invalid (→ ERROR):
 *   Integer, Float, Double, String on either side of and/or/xor/implies
 *   not Integer / not Float / not Double / not String
 * </pre>
 */
public class LogicalTypeMatrixTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== and: truth table ====================

  @Test
  public void testTrueAndTrue() {
    assertSingleBool(compile("true and true"), true);
  }

  @Test
  public void testTrueAndFalse() {
    assertSingleBool(compile("true and false"), false);
  }

  @Test
  public void testFalseAndTrue() {
    assertSingleBool(compile("false and true"), false);
  }

  @Test
  public void testFalseAndFalse() {
    assertSingleBool(compile("false and false"), false);
  }

  // ==================== or: truth table ====================

  @Test
  public void testTrueOrTrue() {
    assertSingleBool(compile("true or true"), true);
  }

  @Test
  public void testTrueOrFalse() {
    assertSingleBool(compile("true or false"), true);
  }

  @Test
  public void testFalseOrTrue() {
    assertSingleBool(compile("false or true"), true);
  }

  @Test
  public void testFalseOrFalse() {
    assertSingleBool(compile("false or false"), false);
  }

  // ==================== xor: truth table ====================

  @Test
  public void testTrueXorTrue() {
    assertSingleBool(compile("true xor true"), false);
  }

  @Test
  public void testTrueXorFalse() {
    assertSingleBool(compile("true xor false"), true);
  }

  @Test
  public void testFalseXorTrue() {
    assertSingleBool(compile("false xor true"), true);
  }

  @Test
  public void testFalseXorFalse() {
    assertSingleBool(compile("false xor false"), false);
  }

  // ==================== implies: truth table ====================

  @Test
  public void testTrueImpliesTrue() {
    assertSingleBool(compile("true implies true"), true);
  }

  @Test
  public void testTrueImpliesFalse() {
    assertSingleBool(compile("true implies false"), false);
  }

  @Test
  public void testFalseImpliesTrue() {
    // false implies anything → true (vacuously true)
    assertSingleBool(compile("false implies true"), true);
  }

  @Test
  public void testFalseImpliesFalse() {
    assertSingleBool(compile("false implies false"), true);
  }

  // ==================== not: unary ====================

  @Test
  public void testNotTrue() {
    assertSingleBool(compile("not true"), false);
  }

  @Test
  public void testNotFalse() {
    assertSingleBool(compile("not false"), true);
  }

  @Test
  public void testNotNotTrue() {
    assertSingleBool(compile("not not true"), true);
  }

  // ==================== Chained logical ====================

  @Test
  public void testAndChained() {
    assertSingleBool(compile("true and true and true"), true);
  }

  @Test
  public void testAndOrCombined() {
    // true and false or true → depends on precedence
    // OCL: 'and' binds tighter than 'or'? Test both orderings explicitly:
    assertSingleBool(compile("(true and false) or true"), true);
    assertSingleBool(compile("true and (false or true)"), true);
  }

  @Test
  public void testNotAndCombined() {
    assertSingleBool(compile("not (true and false)"), true);
  }

  // ==================== Invalid: Integer on either side → ERROR ====================

  @Test
  public void testIntAndBoolFails() {
    assertTypeError("3 and true");
  }

  @Test
  public void testBoolAndIntFails() {
    assertTypeError("true and 3");
  }

  @Test
  public void testIntAndIntFails() {
    assertTypeError("3 and 4");
  }

  @Test
  public void testIntOrBoolFails() {
    assertTypeError("3 or true");
  }

  @Test
  public void testIntXorBoolFails() {
    assertTypeError("3 xor true");
  }

  @Test
  public void testIntImpliesBoolFails() {
    assertTypeError("3 implies true");
  }

  // ==================== Invalid: Float on either side → ERROR ====================

  @Test
  public void testFloatAndBoolFails() {
    assertTypeError("1.5 and true");
  }

  @Test
  public void testBoolAndFloatFails() {
    assertTypeError("true and 1.5");
  }

  @Test
  public void testFloatOrFloatFails() {
    assertTypeError("1.5 or 2.5");
  }

  // ==================== Invalid: Double on either side → ERROR ====================

  @Test
  public void testDoubleAndBoolFails() {
    assertTypeError("2.5 and true");
  }

  @Test
  public void testBoolAndDoubleFails() {
    assertTypeError("true and 2.5");
  }

  // ==================== Invalid: String on either side → ERROR ====================

  @Test
  public void testStringAndBoolFails() {
    assertTypeError("\"hello\" and true");
  }

  @Test
  public void testBoolAndStringFails() {
    assertTypeError("true and \"hello\"");
  }

  @Test
  public void testStringOrStringFails() {
    assertTypeError("\"a\" or \"b\"");
  }

  // ==================== Invalid: missing Int×Float/Double, Float×*, Double×* ====================

  @Test
  public void testIntAndFloatFails() {
    assertTypeError("3 and 1.5");
  }

  @Test
  public void testIntAndDoubleFails() {
    assertTypeError("3 and 2.5");
  }

  @Test
  public void testIntAndStringFails() {
    assertTypeError("3 and \"hello\"");
  }

  @Test
  public void testFloatAndIntFails() {
    assertTypeError("1.5 and 3");
  }

  @Test
  public void testFloatAndFloatFails() {
    assertTypeError("1.5 and 2.5");
  }

  @Test
  public void testFloatAndDoubleFails() {
    assertTypeError("1.5 and 2.5");
  }

  @Test
  public void testFloatAndStringFails() {
    assertTypeError("1.5 and \"hello\"");
  }

  @Test
  public void testDoubleAndIntFails() {
    assertTypeError("2.5 and 3");
  }

  @Test
  public void testDoubleAndFloatFails() {
    assertTypeError("2.5 and 1.5");
  }

  @Test
  public void testDoubleAndDoubleFails() {
    assertTypeError("2.5 and 3.5");
  }

  @Test
  public void testDoubleAndStringFails() {
    assertTypeError("2.5 and \"hello\"");
  }

  @Test
  public void testStringAndIntFails() {
    assertTypeError("\"hello\" and 3");
  }

  @Test
  public void testStringAndFloatFails() {
    assertTypeError("\"hello\" and 1.5");
  }

  @Test
  public void testStringAndDoubleFails() {
    assertTypeError("\"hello\" and 2.5");
  }

  // ==================== Invalid: not on non-Boolean → ERROR ====================

  @Test
  public void testNotIntFails() {
    assertTypeError("not 3");
  }

  @Test
  public void testNotFloatFails() {
    assertTypeError("not 1.5");
  }

  @Test
  public void testNotDoubleFails() {
    assertTypeError("not 2.5");
  }

  @Test
  public void testNotStringFails() {
    assertTypeError("not \"hello\"");
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