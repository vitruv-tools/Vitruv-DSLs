/*******************************************************************************.
 * Copyright (c) 2026 Max Oesterle
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvOCL.type;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.DummyTestSpecification;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;

/**
 * Cross-product tests for comparison operators (==, !=, &lt;, &gt;, &lt;=, &gt;=).
 *
 * <pre>
 * == and != valid combinations:
 *   Integer × Integer → Boolean
 *   Integer × Float   → Boolean  (numeric conformance)
 *   Integer × Double  → Boolean  (numeric conformance)
 *   Float   × Integer → Boolean
 *   Float   × Float   → Boolean
 *   Float   × Double  → Boolean
 *   Double  × Integer → Boolean
 *   Double  × Float   → Boolean
 *   Double  × Double  → Boolean
 *   String  × String  → Boolean
 *   Boolean × Boolean → Boolean
 *
 * &lt; &gt; &lt;= &gt;= valid combinations (numeric only):
 *   All Integer/Float/Double combinations above → Boolean
 *
 * Invalid:
 *   String × numeric, Boolean × numeric, String × Boolean,
 *   numeric × String, numeric × Boolean, Collection × anything
 * </pre>
 */
class ComparisonTypeTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== == : Integer × {Integer, Float, Double} ====================

  @Test
  void testIntEqualsInt() {
    assertSingleBool(compile("3 == 3"), true);
  }

  @Test
  void testIntEqualsIntFalse() {
    assertSingleBool(compile("3 == 4"), false);
  }

  @Test
  void testIntEqualsFloat() {
    assertSingleBool(compile("3 == 3.0"), true);
  }

  @Test
  void testIntEqualsDouble() {
    assertSingleBool(compile("3 == 3.0"), true);
  }

  // ==================== == : Float × {Integer, Float, Double} ====================

  @Test
  void testFloatEqualsInt() {
    assertSingleBool(compile("3.0 == 3"), true);
  }

  @Test
  void testFloatEqualsFloat() {
    assertSingleBool(compile("1.5 == 1.5"), true);
  }

  @Test
  void testFloatEqualsDouble() {
    assertSingleBool(compile("1.5 == 1.5"), true);
  }

  // ==================== == : Double × {Integer, Float, Double} ====================

  @Test
  void testDoubleEqualsInt() {
    assertSingleBool(compile("3.0 == 3"), true);
  }

  @Test
  void testDoubleEqualsFloat() {
    assertSingleBool(compile("2.5 == 2.5"), true);
  }

  @Test
  void testDoubleEqualsDouble() {
    assertSingleBool(compile("2.5 == 2.5"), true);
  }

  // ==================== == : String × String ====================

  @Test
  void testStringEqualsString() {
    assertSingleBool(compile("\"hello\" == \"hello\""), true);
  }

  @Test
  void testStringEqualsStringFalse() {
    assertSingleBool(compile("\"hello\" == \"world\""), false);
  }

  // ==================== == : Boolean × Boolean ====================

  @Test
  void testBoolEqualsBool() {
    assertSingleBool(compile("true == true"), true);
  }

  @Test
  void testBoolEqualsBoolFalse() {
    assertSingleBool(compile("true == false"), false);
  }

  // ==================== != across all valid combinations ====================

  @Test
  void testIntNotEqualsInt() {
    assertSingleBool(compile("3 != 4"), true);
  }

  @Test
  void testIntNotEqualsFloat() {
    assertSingleBool(compile("3 != 3.0"), false);
  }

  @Test
  void testFloatNotEqualsInt() {
    assertSingleBool(compile("1.5 != 2"), true);
  }

  @Test
  void testStringNotEqualsString() {
    assertSingleBool(compile("\"a\" != \"b\""), true);
  }

  @Test
  void testBoolNotEqualsBool() {
    assertSingleBool(compile("true != false"), true);
  }

  // ==================== < : Integer × {Integer, Float, Double} ====================

  @Test
  void testIntLessThanInt() {
    assertSingleBool(compile("3 < 4"), true);
  }

  @Test
  void testIntLessThanIntFalse() {
    assertSingleBool(compile("4 < 3"), false);
  }

  @Test
  void testIntLessThanFloat() {
    assertSingleBool(compile("2 < 2.5"), true);
  }

  @Test
  void testIntLessThanDouble() {
    assertSingleBool(compile("2 < 2.5"), true);
  }

  // ==================== < : Float × {Integer, Float, Double} ====================

  @Test
  void testFloatLessThanInt() {
    assertSingleBool(compile("1.5 < 2"), true);
  }

  @Test
  void testFloatLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  void testFloatLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  // ==================== < : Double × {Integer, Float, Double} ====================

  @Test
  void testDoubleLessThanInt() {
    assertSingleBool(compile("1.5 < 2"), true);
  }

  @Test
  void testDoubleLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.0"), true);
  }

  @Test
  void testDoubleLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  // ==================== > ====================

  @Test
  void testIntGreaterThanInt() {
    assertSingleBool(compile("4 > 3"), true);
  }

  @Test
  void testIntGreaterThanFloat() {
    assertSingleBool(compile("3 > 2.5"), true);
  }

  @Test
  void testFloatGreaterThanInt() {
    assertSingleBool(compile("2.5 > 2"), true);
  }

  @Test
  void testDoubleGreaterThanDouble() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  // ==================== <= ====================

  @Test
  void testIntLessOrEqualIntequal() {
    assertSingleBool(compile("3 <= 3"), true);
  }

  @Test
  void testIntLessOrEqualIntless() {
    assertSingleBool(compile("2 <= 3"), true);
  }

  @Test
  void testIntLessOrEqualIntgreater() {
    assertSingleBool(compile("4 <= 3"), false);
  }

  @Test
  void testFloatLessOrEqualDouble() {
    assertSingleBool(compile("1.5 <= 1.5"), true);
  }

  @Test
  void testIntLessOrEqualFloat() {
    assertSingleBool(compile("2 <= 2.5"), true);
  }

  // ==================== >= ====================

  @Test
  void testIntGreaterOrEqualIntequal() {
    assertSingleBool(compile("3 >= 3"), true);
  }

  @Test
  void testIntGreaterOrEqualIntgreater() {
    assertSingleBool(compile("4 >= 3"), true);
  }

  @Test
  void testFloatGreaterOrEqualInt() {
    assertSingleBool(compile("3.0 >= 3"), true);
  }

  @Test
  void testDoubleGreaterOrEqualDouble() {
    assertSingleBool(compile("2.5 >= 2.5"), true);
  }

  // ==================== Invalid: == across incompatible types → ERROR ====================

  @Test
  void testIntEqualsStringFails() {
    assertTypeError("3 == \"hello\"");
  }

  @Test
  void testIntEqualsBoolFails() {
    assertTypeError("3 == true");
  }

  @Test
  void testStringEqualsIntFails() {
    assertTypeError("\"hello\" == 3");
  }

  @Test
  void testStringEqualsBoolFails() {
    assertTypeError("\"hello\" == true");
  }

  @Test
  void testBoolEqualsIntFails() {
    assertTypeError("true == 3");
  }

  @Test
  void testBoolEqualsStringFails() {
    assertTypeError("true == \"hello\"");
  }

  @Test
  void testFloatEqualsStringFails() {
    assertTypeError("1.5 == \"hello\"");
  }

  @Test
  void testDoubleEqualsBoolFails() {
    assertTypeError("2.5 == true");
  }

  // ==================== Invalid: < > <= >= on non-numeric → ERROR ====================

  @Test
  void testStringLessThanStringFails() {
    assertTypeError("\"a\" < \"b\"");
  }

  @Test
  void testBoolLessThanBoolFails() {
    assertTypeError("true < false");
  }

  @Test
  void testIntLessThanStringFails() {
    assertTypeError("3 < \"hello\"");
  }

  @Test
  void testIntLessThanBoolFails() {
    assertTypeError("3 < true");
  }

  @Test
  void testStringGreaterThanIntFails() {
    assertTypeError("\"hello\" > 3");
  }

  @Test
  void testBoolLessOrEqualIntFails() {
    assertTypeError("true <= 3");
  }

  @Test
  void testFloatGreaterThanStringFails() {
    assertTypeError("1.5 > \"hello\"");
  }

  // ==================== Invalid: Collection comparisons → ERROR ====================

  @Test
  void testSetEqualsIntFails() {
    assertTypeError("Set{1,2} == 3");
  }

  @Test
  void testSetLessThanSetFails() {
    assertTypeError("Set{1,2} < Set{3,4}");
  }

  @Test
  void testSetEqualsSetSameType() {
    // Set{T} == Set{T} currently fails at Pass 3 — not supported
    assertTypeError("Set{1,2} == Set{1,2}");
  }

  @Test
  void testSetEqualsSetDifferentType() {
    // Set{Integer} == Set{String} — incompatible element types → ERROR
    assertTypeError("Set{1,2} == Set{\"a\",\"b\"}");
  }

  // ==================== Invalid: == missing combinations → ERROR ====================

  @Test
  void testFloatEqualsBoolFails() {
    assertTypeError("1.5 == true");
  }

  @Test
  void testFloatEqualsSetFails() {
    assertTypeError("1.5 == Set{1,2}");
  }

  @Test
  void testDoubleEqualsSetFails() {
    assertTypeError("2.5 == Set{1,2}");
  }

  @Test
  void testStringEqualsFloatFails() {
    assertTypeError("\"hello\" == 1.5");
  }

  @Test
  void testStringEqualsDoubleFails() {
    assertTypeError("\"hello\" == 2.5");
  }

  @Test
  void testStringEqualsSetFails() {
    assertTypeError("\"hello\" == Set{1,2}");
  }

  @Test
  void testBoolEqualsFloatFails() {
    assertTypeError("true == 1.5");
  }

  @Test
  void testBoolEqualsDoubleFails() {
    assertTypeError("true == 2.5");
  }

  @Test
  void testBoolEqualsSetFails() {
    assertTypeError("true == Set{1,2}");
  }

  // ==================== Invalid: < > <= >= missing combinations → ERROR ====================

  @Test
  void testFloatLessThanBoolFails() {
    assertTypeError("1.5 < true");
  }

  @Test
  void testFloatLessThanSetFails() {
    assertTypeError("1.5 < Set{1,2}");
  }

  @Test
  void testDoubleLessThanBoolFails() {
    assertTypeError("2.5 < true");
  }

  @Test
  void testDoubleLessThanSetFails() {
    assertTypeError("2.5 < Set{1,2}");
  }

  @Test
  void testStringLessThanFloatFails() {
    assertTypeError("\"a\" < 1.5");
  }

  @Test
  void testStringLessThanDoubleFails() {
    assertTypeError("\"a\" < 2.5");
  }

  @Test
  void testStringLessThanBoolFails() {
    assertTypeError("\"a\" < true");
  }

  @Test
  void testStringLessThanSetFails() {
    assertTypeError("\"a\" < Set{1,2}");
  }

  @Test
  void testBoolLessThanFloatFails() {
    assertTypeError("true < 1.5");
  }

  @Test
  void testBoolLessThanDoubleFails() {
    assertTypeError("true < 2.5");
  }

  @Test
  void testBoolLessThanStringFails() {
    assertTypeError("true < \"hello\"");
  }

  @Test
  void testBoolLessThanSetFails() {
    assertTypeError("true < Set{1,2}");
  }

  // ==================== Helper ====================

  private void assertTypeError(String input) {
    assertThrows(AssertionError.class, () -> compile(input),
        "Expected type error for: " + input);
  }
}
