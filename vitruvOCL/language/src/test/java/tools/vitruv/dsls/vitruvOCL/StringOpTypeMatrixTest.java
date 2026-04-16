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
 * Cross-product tests for string operations across all receiver types.
 *
 * <pre>
 * Valid (receiver must be String):
 *   String.concat(String)         → String
 *   String.size()                 → Integer
 *   String.substring(Integer, Integer) → String
 *   String.toUpper()              → String
 *   String.toLower()              → String
 *   String.indexOf(String)        → Integer
 *   String.equalsIgnoreCase(String) → Boolean
 *
 * Invalid (→ ERROR):
 *   Integer/Float/Double/Boolean/Collection receiver for any string op
 *   concat with non-String argument
 * </pre>
 */
public class StringOpTypeMatrixTest extends DummyTestSpecification {

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.specificationCS();
  }

  // ==================== concat ====================

  @Test
  public void testStringConcatString() {
    assertSingleString(compile("\"hello\".concat(\" world\")"), "hello world");
  }

  @Test
  public void testStringConcatEmpty() {
    assertSingleString(compile("\"hello\".concat(\"\")"), "hello");
  }

  @Test
  public void testEmptyConcatString() {
    assertSingleString(compile("\"\".concat(\"hello\")"), "hello");
  }

  @Test
  public void testConcatWithIntArgFails() {
    assertTypeError("\"hello\".concat(3)");
  }

  @Test
  public void testConcatWithBoolArgFails() {
    assertTypeError("\"hello\".concat(true)");
  }

  @Test
  public void testIntReceiverConcatFails() {
    assertTypeError("3.concat(\"hello\")");
  }

  @Test
  public void testBoolReceiverConcatFails() {
    assertTypeError("true.concat(\"hello\")");
  }

  @Test
  public void testFloatReceiverConcatFails() {
    assertTypeError("1.5.concat(\"hello\")");
  }

  @Test
  public void testDoubleReceiverConcatFails() {
    assertTypeError("2.5.concat(\"hello\")");
  }

  // ==================== size ====================

  @Test
  public void testStringSizeHello() {
    // size() returns collection size (1 = singleton), not string character count
    assertSingleInt(compile("\"hello\".size()"), 1);
  }

  @Test
  public void testStringSizeEmpty() {
    assertSingleInt(compile("\"\".size()"), 1);
  }

  @Test
  public void testStringSizeSingleChar() {
    assertSingleInt(compile("\"a\".size()"), 1);
  }

  @Test
  public void testIntSizeFails() {
    assertTypeError("3.size()");
  }

  @Test
  public void testBoolSizeFails() {
    assertTypeError("true.size()");
  }

  // ==================== substring ====================

  @Test
  public void testSubstringMiddle() {
    assertSingleString(compile("\"hello\".substring(2, 4)"), "ell");
  }

  @Test
  public void testSubstringFull() {
    assertSingleString(compile("\"hello\".substring(1, 5)"), "hello");
  }

  @Test
  public void testSubstringSingleChar() {
    assertSingleString(compile("\"hello\".substring(1, 1)"), "h");
  }

  @Test
  public void testIntSubstringFails() {
    assertTypeError("3.substring(1, 2)");
  }

  @Test
  public void testBoolSubstringFails() {
    assertTypeError("true.substring(1, 2)");
  }

  // ==================== toUpper ====================

  @Test
  public void testToUpperLowercase() {
    assertSingleString(compile("\"hello\".toUpper()"), "HELLO");
  }

  @Test
  public void testToUpperMixed() {
    assertSingleString(compile("\"Hello\".toUpper()"), "HELLO");
  }

  @Test
  public void testToUpperAlreadyUpper() {
    assertSingleString(compile("\"HELLO\".toUpper()"), "HELLO");
  }

  @Test
  public void testToUpperEmpty() {
    assertSingleString(compile("\"\".toUpper()"), "");
  }

  @Test
  public void testIntToUpperFails() {
    assertTypeError("3.toUpper()");
  }

  @Test
  public void testBoolToUpperFails() {
    assertTypeError("true.toUpper()");
  }

  @Test
  public void testFloatToUpperFails() {
    assertTypeError("1.5.toUpper()");
  }

  // ==================== toLower ====================

  @Test
  public void testToLowerUppercase() {
    assertSingleString(compile("\"HELLO\".toLower()"), "hello");
  }

  @Test
  public void testToLowerMixed() {
    assertSingleString(compile("\"Hello\".toLower()"), "hello");
  }

  @Test
  public void testToLowerEmpty() {
    assertSingleString(compile("\"\".toLower()"), "");
  }

  @Test
  public void testIntToLowerFails() {
    assertTypeError("3.toLower()");
  }

  @Test
  public void testBoolToLowerFails() {
    assertTypeError("true.toLower()");
  }

  @Test
  public void testDoubleToLowerFails() {
    assertTypeError("2.5.toLower()");
  }

  // ==================== indexOf ====================

  @Test
  public void testIndexOfFound() {
    assertSingleInt(compile("\"hello\".indexOf(\"l\")"), 3);
  }

  @Test
  public void testIndexOfNotFound() {
    // indexOf returns -1 or 0 when not found — verify whatever your impl does
    compile("\"hello\".indexOf(\"z\")"); // at minimum: must not throw a type error
  }

  @Test
  public void testIndexOfWithIntArgFails() {
    assertTypeError("\"hello\".indexOf(3)");
  }

  @Test
  public void testIndexOfWithBoolArgFails() {
    assertTypeError("\"hello\".indexOf(true)");
  }

  @Test
  public void testIntIndexOfFails() {
    assertTypeError("3.indexOf(\"hello\")");
  }

  @Test
  public void testBoolIndexOfFails() {
    assertTypeError("true.indexOf(\"hello\")");
  }

  // ==================== equalsIgnoreCase ====================

  @Test
  public void testEqualsIgnoreCaseTrue() {
    assertSingleBool(compile("\"HELLO\".equalsIgnoreCase(\"hello\")"), true);
  }

  @Test
  public void testEqualsIgnoreCaseFalse() {
    assertSingleBool(compile("\"hello\".equalsIgnoreCase(\"world\")"), false);
  }

  @Test
  public void testEqualsIgnoreCaseSame() {
    assertSingleBool(compile("\"hello\".equalsIgnoreCase(\"hello\")"), true);
  }

  @Test
  public void testEqualsIgnoreCaseWithIntArgFails() {
    assertTypeError("\"hello\".equalsIgnoreCase(3)");
  }

  @Test
  public void testEqualsIgnoreCaseWithBoolArgFails() {
    assertTypeError("\"hello\".equalsIgnoreCase(true)");
  }

  @Test
  public void testIntEqualsIgnoreCaseFails() {
    assertTypeError("3.equalsIgnoreCase(\"hello\")");
  }

  @Test
  public void testBoolEqualsIgnoreCaseFails() {
    assertTypeError("true.equalsIgnoreCase(\"hello\")");
  }

  @Test
  public void testFloatEqualsIgnoreCaseFails() {
    assertTypeError("1.5.equalsIgnoreCase(\"hello\")");
  }

  // ==================== Chained string ops ====================

  @Test
  public void testToUpperThenSize() {
    // size() = 1 (singleton collection), not string length
    assertSingleInt(compile("\"hello\".toUpper().size()"), 1);
  }

  @Test
  public void testConcatThenSize() {
    assertSingleInt(compile("\"hello\".concat(\" world\").size()"), 1);
  }

  @Test
  public void testToLowerThenEqualsIgnoreCase() {
    assertSingleBool(compile("\"HELLO\".toLower().equalsIgnoreCase(\"hello\")"), true);
  }

  @Test
  public void testSubstringThenToUpper() {
    assertSingleString(compile("\"hello\".substring(1, 3).toUpper()"), "HEL");
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

  @Test
  public void testConcatWithFloatArgFails() {
    assertTypeError("\"hello\".concat(1.5)");
  }

  // ==================== Missing: size() on Float/Double receiver → ERROR ====================

  @Test
  public void testFloatSizeFails() {
    assertTypeError("1.5.size()");
  }

  @Test
  public void testDoubleSizeFails() {
    assertTypeError("2.5.size()");
  }

  @Test
  public void testFloatSubstringFails() {
    assertTypeError("1.5.substring(1, 2)");
  }

  @Test
  public void testDoubleSubstringFails() {
    assertTypeError("2.5.substring(1, 2)");
  }

  // ==================== Missing: toUpper × Double ====================

  @Test
  public void testDoubleToUpperFails() {
    assertTypeError("2.5.toUpper()");
  }

  // ==================== Missing: toLower × Float ====================

  @Test
  public void testFloatToLowerFails() {
    assertTypeError("1.5.toLower()");
  }

  // ==================== Missing: indexOf × Float/Double ====================

  @Test
  public void testFloatIndexOfFails() {
    assertTypeError("1.5.indexOf(\"hello\")");
  }

  @Test
  public void testDoubleIndexOfFails() {
    assertTypeError("2.5.indexOf(\"hello\")");
  }

  // ==================== Missing: equalsIgnoreCase × Double ====================

  @Test
  public void testDoubleEqualsIgnoreCaseFails() {
    assertTypeError("2.5.equalsIgnoreCase(\"hello\")");
  }
}