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
public class ComparisonTypeTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== == : Integer × {Integer, Float, Double} ====================

  @Test
  public void testIntEqualsInt() {
    assertSingleBool(compile("3 == 3"), true);
  }

  @Test
  public void testIntEqualsIntFalse() {
    assertSingleBool(compile("3 == 4"), false);
  }

  @Test
  public void testIntEqualsFloat() {
    assertSingleBool(compile("3 == 3.0"), true);
  }

  @Test
  public void testIntEqualsDouble() {
    assertSingleBool(compile("3 == 3.0"), true);
  }

  // ==================== == : Float × {Integer, Float, Double} ====================

  @Test
  public void testFloatEqualsInt() {
    assertSingleBool(compile("3.0 == 3"), true);
  }

  @Test
  public void testFloatEqualsFloat() {
    assertSingleBool(compile("1.5 == 1.5"), true);
  }

  @Test
  public void testFloatEqualsDouble() {
    assertSingleBool(compile("1.5 == 1.5"), true);
  }

  // ==================== == : Double × {Integer, Float, Double} ====================

  @Test
  public void testDoubleEqualsInt() {
    assertSingleBool(compile("3.0 == 3"), true);
  }

  @Test
  public void testDoubleEqualsFloat() {
    assertSingleBool(compile("2.5 == 2.5"), true);
  }

  @Test
  public void testDoubleEqualsDouble() {
    assertSingleBool(compile("2.5 == 2.5"), true);
  }

  // ==================== == : String × String ====================

  @Test
  public void testStringEqualsString() {
    assertSingleBool(compile("\"hello\" == \"hello\""), true);
  }

  @Test
  public void testStringEqualsStringFalse() {
    assertSingleBool(compile("\"hello\" == \"world\""), false);
  }

  // ==================== == : Boolean × Boolean ====================

  @Test
  public void testBoolEqualsBool() {
    assertSingleBool(compile("true == true"), true);
  }

  @Test
  public void testBoolEqualsBoolFalse() {
    assertSingleBool(compile("true == false"), false);
  }

  // ==================== != across all valid combinations ====================

  @Test
  public void testIntNotEqualsInt() {
    assertSingleBool(compile("3 != 4"), true);
  }

  @Test
  public void testIntNotEqualsFloat() {
    assertSingleBool(compile("3 != 3.0"), false);
  }

  @Test
  public void testFloatNotEqualsInt() {
    assertSingleBool(compile("1.5 != 2"), true);
  }

  @Test
  public void testStringNotEqualsString() {
    assertSingleBool(compile("\"a\" != \"b\""), true);
  }

  @Test
  public void testBoolNotEqualsBool() {
    assertSingleBool(compile("true != false"), true);
  }

  // ==================== < : Integer × {Integer, Float, Double} ====================

  @Test
  public void testIntLessThanInt() {
    assertSingleBool(compile("3 < 4"), true);
  }

  @Test
  public void testIntLessThanIntFalse() {
    assertSingleBool(compile("4 < 3"), false);
  }

  @Test
  public void testIntLessThanFloat() {
    assertSingleBool(compile("2 < 2.5"), true);
  }

  @Test
  public void testIntLessThanDouble() {
    assertSingleBool(compile("2 < 2.5"), true);
  }

  // ==================== < : Float × {Integer, Float, Double} ====================

  @Test
  public void testFloatLessThanInt() {
    assertSingleBool(compile("1.5 < 2"), true);
  }

  @Test
  public void testFloatLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  public void testFloatLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  // ==================== < : Double × {Integer, Float, Double} ====================

  @Test
  public void testDoubleLessThanInt() {
    assertSingleBool(compile("1.5 < 2"), true);
  }

  @Test
  public void testDoubleLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.0"), true);
  }

  @Test
  public void testDoubleLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  // ==================== > ====================

  @Test
  public void testIntGreaterThanInt() {
    assertSingleBool(compile("4 > 3"), true);
  }

  @Test
  public void testIntGreaterThanFloat() {
    assertSingleBool(compile("3 > 2.5"), true);
  }

  @Test
  public void testFloatGreaterThanInt() {
    assertSingleBool(compile("2.5 > 2"), true);
  }

  @Test
  public void testDoubleGreaterThanDouble() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  // ==================== <= ====================

  @Test
  public void testIntLessOrEqualIntequal() {
    assertSingleBool(compile("3 <= 3"), true);
  }

  @Test
  public void testIntLessOrEqualIntless() {
    assertSingleBool(compile("2 <= 3"), true);
  }

  @Test
  public void testIntLessOrEqualIntgreater() {
    assertSingleBool(compile("4 <= 3"), false);
  }

  @Test
  public void testFloatLessOrEqualDouble() {
    assertSingleBool(compile("1.5 <= 1.5"), true);
  }

  @Test
  public void testIntLessOrEqualFloat() {
    assertSingleBool(compile("2 <= 2.5"), true);
  }

  // ==================== >= ====================

  @Test
  public void testIntGreaterOrEqualIntequal() {
    assertSingleBool(compile("3 >= 3"), true);
  }

  @Test
  public void testIntGreaterOrEqualIntgreater() {
    assertSingleBool(compile("4 >= 3"), true);
  }

  @Test
  public void testFloatGreaterOrEqualInt() {
    assertSingleBool(compile("3.0 >= 3"), true);
  }

  @Test
  public void testDoubleGreaterOrEqualDouble() {
    assertSingleBool(compile("2.5 >= 2.5"), true);
  }

  // ==================== Invalid: == across incompatible types → ERROR ====================

  @Test
  public void testIntEqualsStringFails() {
    assertTypeError("3 == \"hello\"");
  }

  @Test
  public void testIntEqualsBoolFails() {
    assertTypeError("3 == true");
  }

  @Test
  public void testStringEqualsIntFails() {
    assertTypeError("\"hello\" == 3");
  }

  @Test
  public void testStringEqualsBoolFails() {
    assertTypeError("\"hello\" == true");
  }

  @Test
  public void testBoolEqualsIntFails() {
    assertTypeError("true == 3");
  }

  @Test
  public void testBoolEqualsStringFails() {
    assertTypeError("true == \"hello\"");
  }

  @Test
  public void testFloatEqualsStringFails() {
    assertTypeError("1.5 == \"hello\"");
  }

  @Test
  public void testDoubleEqualsBoolFails() {
    assertTypeError("2.5 == true");
  }

  // ==================== Invalid: < > <= >= on non-numeric → ERROR ====================

  @Test
  public void testStringLessThanStringFails() {
    assertTypeError("\"a\" < \"b\"");
  }

  @Test
  public void testBoolLessThanBoolFails() {
    assertTypeError("true < false");
  }

  @Test
  public void testIntLessThanStringFails() {
    assertTypeError("3 < \"hello\"");
  }

  @Test
  public void testIntLessThanBoolFails() {
    assertTypeError("3 < true");
  }

  @Test
  public void testStringGreaterThanIntFails() {
    assertTypeError("\"hello\" > 3");
  }

  @Test
  public void testBoolLessOrEqualIntFails() {
    assertTypeError("true <= 3");
  }

  @Test
  public void testFloatGreaterThanStringFails() {
    assertTypeError("1.5 > \"hello\"");
  }

  // ==================== Invalid: Collection comparisons → ERROR ====================

  @Test
  public void testSetEqualsIntFails() {
    assertTypeError("Set{1,2} == 3");
  }

  @Test
  public void testSetLessThanSetFails() {
    assertTypeError("Set{1,2} < Set{3,4}");
  }

  @Test
  public void testSetEqualsSetSameType() {
    // Set{T} == Set{T} currently fails at Pass 3 — not supported
    assertTypeError("Set{1,2} == Set{1,2}");
  }

  @Test
  public void testSetEqualsSetDifferentType() {
    // Set{Integer} == Set{String} — incompatible element types → ERROR
    assertTypeError("Set{1,2} == Set{\"a\",\"b\"}");
  }

  // ==================== Invalid: == missing combinations → ERROR ====================

  @Test
  public void testFloatEqualsBoolFails() {
    assertTypeError("1.5 == true");
  }

  @Test
  public void testFloatEqualsSetFails() {
    assertTypeError("1.5 == Set{1,2}");
  }

  @Test
  public void testDoubleEqualsSetFails() {
    assertTypeError("2.5 == Set{1,2}");
  }

  @Test
  public void testStringEqualsFloatFails() {
    assertTypeError("\"hello\" == 1.5");
  }

  @Test
  public void testStringEqualsDoubleFails() {
    assertTypeError("\"hello\" == 2.5");
  }

  @Test
  public void testStringEqualsSetFails() {
    assertTypeError("\"hello\" == Set{1,2}");
  }

  @Test
  public void testBoolEqualsFloatFails() {
    assertTypeError("true == 1.5");
  }

  @Test
  public void testBoolEqualsDoubleFails() {
    assertTypeError("true == 2.5");
  }

  @Test
  public void testBoolEqualsSetFails() {
    assertTypeError("true == Set{1,2}");
  }

  // ==================== Invalid: < > <= >= missing combinations → ERROR ====================

  @Test
  public void testFloatLessThanBoolFails() {
    assertTypeError("1.5 < true");
  }

  @Test
  public void testFloatLessThanSetFails() {
    assertTypeError("1.5 < Set{1,2}");
  }

  @Test
  public void testDoubleLessThanBoolFails() {
    assertTypeError("2.5 < true");
  }

  @Test
  public void testDoubleLessThanSetFails() {
    assertTypeError("2.5 < Set{1,2}");
  }

  @Test
  public void testStringLessThanFloatFails() {
    assertTypeError("\"a\" < 1.5");
  }

  @Test
  public void testStringLessThanDoubleFails() {
    assertTypeError("\"a\" < 2.5");
  }

  @Test
  public void testStringLessThanBoolFails() {
    assertTypeError("\"a\" < true");
  }

  @Test
  public void testStringLessThanSetFails() {
    assertTypeError("\"a\" < Set{1,2}");
  }

  @Test
  public void testBoolLessThanFloatFails() {
    assertTypeError("true < 1.5");
  }

  @Test
  public void testBoolLessThanDoubleFails() {
    assertTypeError("true < 2.5");
  }

  @Test
  public void testBoolLessThanStringFails() {
    assertTypeError("true < \"hello\"");
  }

  @Test
  public void testBoolLessThanSetFails() {
    assertTypeError("true < Set{1,2}");
  }

  // ==================== Helper ====================

  private void assertTypeError(String input) {
    assertThrows(AssertionError.class, () -> compile(input),
        "Expected type error for: " + input);
  }
}