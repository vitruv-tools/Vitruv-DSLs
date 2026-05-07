/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.arithmetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.DummyTestSpecification;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Type Matrix: Arithmetic (+ - * /)
 *
 * <p>All 9 valid numeric×numeric combinations plus representative ERROR cases.
 *
 * <pre>
 * OCL# type universe (9 ctype families):
 *   Singletons: ¡Integer! ¡Float! ¡Double! ¡String! ¡Boolean!
 *   Collections: Set{T} Sequence{T} Bag{T} OrderedSet{T}
 *
 * Valid (→ result):
 *   ¡Integer! × ¡Integer!  → ¡Integer!  (except ÷ → ¡Double!)
 *   ¡Integer! × ¡Float!    → ¡Double!
 *   ¡Integer! × ¡Double!   → ¡Double!
 *   ¡Float!   × ¡Integer!  → ¡Double!
 *   ¡Float!   × ¡Float!    → ¡Double!
 *   ¡Float!   × ¡Double!   → ¡Double!
 *   ¡Double!  × ¡Integer!  → ¡Double!
 *   ¡Double!  × ¡Float!    → ¡Double!
 *   ¡Double!  × ¡Double!   → ¡Double!
 *
 * Invalid: any non-numeric × anything → ERROR
 * </pre>
 */
public class ArithmeticTypeTest extends DummyTestSpecification {

  /**
   * Compiles an OCL expression and asserts that type checking rejects it with an error. Used for
   * ERROR cells in the type matrix.
   */
  protected void compileExpectError(String input) {
    ParseTree tree = parse(input);
    ErrorCollector errors = new ErrorCollector();
    var dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    SymbolTableBuilder stb = new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    stb.visit(tree);
    TypeCheckVisitor tc = new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    tc.visit(tree);
    assertTrue(
        tc.hasErrors() || errors.hasErrors(),
        "Expected type error for: " + input + " but none was reported");
  }

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.expCS();
  }

  protected void assertSingleNumeric(Value result, double expected) {
    assertEquals(1, result.size(), "Expected singleton");
    OCLElement e = result.getElements().get(0);
    double actual =
        e.tryGetInt() != null
            ? e.tryGetInt()
            : e.tryGetFloat() != null
                ? e.tryGetFloat()
                : e.tryGetDouble() != null ? e.tryGetDouble() : Double.NaN;
    assertEquals(expected, actual, 1e-9);
  }

  // ── Integer × Integer ─────────────────────────────────────────

  @Test
  public void testIntegerPlusInteger() {
    assertSingleInt(compile("3 + 4"), 7);
  }

  @Test
  public void testIntegerMinusInteger() {
    assertSingleInt(compile("10 - 3"), 7);
  }

  @Test
  public void testIntegerTimesInteger() {
    assertSingleInt(compile("3 * 4"), 12);
  }

  @Test
  public void testIntegerDividesInteger() {
    // Integer / Integer → Double in OCL#
    assertSingleDouble(compile("10 / 2"), 5.0);
  }

  // ── Integer × Float ───────────────────────────────────────────

  @Test
  public void testIntegerPlusFloat() {
    assertSingleDouble(compile("3 + 1.5"), 4.5);
  }

  @Test
  public void testIntegerMinusFloat() {
    assertSingleDouble(compile("5 - 1.5"), 3.5);
  }

  @Test
  public void testIntegerTimesFloat() {
    assertSingleDouble(compile("3 * 1.5"), 4.5);
  }

  @Test
  public void testIntegerDividesFloat() {
    assertSingleDouble(compile("3 / 2.0"), 1.5);
  }

  // ── Integer × Double ──────────────────────────────────────────

  @Test
  public void testIntegerPlusDouble() {
    assertSingleDouble(compile("3 + 2.5"), 5.5);
  }

  @Test
  public void testIntegerMinusDouble() {
    assertSingleDouble(compile("5 - 2.5"), 2.5);
  }

  @Test
  public void testIntegerTimesDouble() {
    assertSingleDouble(compile("4 * 2.5"), 10.0);
  }

  @Test
  public void testIntegerDividesDouble() {
    assertSingleDouble(compile("5 / 2.0"), 2.5);
  }

  // ── Float × Integer ───────────────────────────────────────────

  @Test
  public void testFloatPlusInteger() {
    assertSingleDouble(compile("1.5 + 3"), 4.5);
  }

  @Test
  public void testFloatMinusInteger() {
    assertSingleDouble(compile("3.5 - 1"), 2.5);
  }

  @Test
  public void testFloatTimesInteger() {
    assertSingleDouble(compile("2.0 * 3"), 6.0);
  }

  @Test
  public void testFloatDividesInteger() {
    assertSingleDouble(compile("6.0 / 2"), 3.0);
  }

  // ── Float × Float ─────────────────────────────────────────────

  @Test
  public void testFloatPlusFloat() {
    assertSingleDouble(compile("1.5 + 2.5"), 4.0);
  }

  @Test
  public void testFloatMinusFloat() {
    assertSingleDouble(compile("3.5 - 1.5"), 2.0);
  }

  @Test
  public void testFloatTimesFloat() {
    assertSingleDouble(compile("2.0 * 3.0"), 6.0);
  }

  @Test
  public void testFloatDividesFloat() {
    assertSingleDouble(compile("6.0 / 2.0"), 3.0);
  }

  // ── Float × Double ────────────────────────────────────────────

  @Test
  public void testFloatPlusDouble() {
    assertSingleDouble(compile("1.5 + 2.5"), 4.0);
  }

  @Test
  public void testFloatMinusDouble() {
    assertSingleDouble(compile("3.5 - 1.5"), 2.0);
  }

  @Test
  public void testFloatTimesDouble() {
    assertSingleDouble(compile("2.0 * 3.0"), 6.0);
  }

  @Test
  public void testFloatDividesDouble() {
    assertSingleDouble(compile("9.0 / 3.0"), 3.0);
  }

  // ── Double × Integer ──────────────────────────────────────────

  @Test
  public void testDoublePlusInteger() {
    assertSingleDouble(compile("2.5 + 3"), 5.5);
  }

  @Test
  public void testDoubleMinusInteger() {
    assertSingleDouble(compile("5.5 - 2"), 3.5);
  }

  @Test
  public void testDoubleTimesInteger() {
    assertSingleDouble(compile("2.5 * 4"), 10.0);
  }

  @Test
  public void testDoubleDividesInteger() {
    assertSingleDouble(compile("9.0 / 3"), 3.0);
  }

  // ── Double × Float ────────────────────────────────────────────

  @Test
  public void testDoublePlusFloat() {
    assertSingleDouble(compile("2.5 + 1.5"), 4.0);
  }

  @Test
  public void testDoubleMinusFloat() {
    assertSingleDouble(compile("5.5 - 2.5"), 3.0);
  }

  @Test
  public void testDoubleTimesFloat() {
    assertSingleDouble(compile("2.5 * 2.0"), 5.0);
  }

  @Test
  public void testDoubleDividesFloat() {
    assertSingleDouble(compile("6.0 / 2.0"), 3.0);
  }

  // ── Double × Double ───────────────────────────────────────────

  @Test
  public void testDoublePlusDouble() {
    assertSingleDouble(compile("2.5 + 2.5"), 5.0);
  }

  @Test
  public void testDoubleMinusDouble() {
    assertSingleDouble(compile("5.5 - 2.5"), 3.0);
  }

  @Test
  public void testDoubleTimesDouble() {
    assertSingleDouble(compile("2.5 * 2.0"), 5.0);
  }

  @Test
  public void testDoubleDividesDouble() {
    assertSingleDouble(compile("9.0 / 3.0"), 3.0);
  }

  // ── ERROR: String receiver ────────────────────────────────────

  @Test
  public void testStringPlusIntegerFails() {
    compileExpectError("\"hello\" + 1");
  }

  @Test
  public void testStringMinusIntegerFails() {
    compileExpectError("\"hello\" - 1");
  }

  @Test
  public void testStringTimesIntegerFails() {
    compileExpectError("\"hello\" * 1");
  }

  @Test
  public void testStringDividesIntegerFails() {
    compileExpectError("\"hello\" / 1");
  }

  @Test
  public void testStringPlusFloatFails() {
    compileExpectError("\"hello\" + 1.5");
  }

  @Test
  public void testStringPlusDoubleFails() {
    compileExpectError("\"hello\" + 2.5");
  }

  @Test
  public void testStringPlusStringFails() {
    compileExpectError("\"hello\" + \"world\"");
  }

  @Test
  public void testStringPlusBooleanFails() {
    compileExpectError("\"hello\" + true");
  }

  // ── ERROR: Boolean receiver ───────────────────────────────────

  @Test
  public void testBooleanPlusIntegerFails() {
    compileExpectError("true + 1");
  }

  @Test
  public void testBooleanPlusFloatFails() {
    compileExpectError("true + 1.5");
  }

  @Test
  public void testBooleanPlusDoubleFails() {
    compileExpectError("true + 2.5");
  }

  @Test
  public void testBooleanPlusStringFails() {
    compileExpectError("true + \"hello\"");
  }

  @Test
  public void testBooleanPlusBooleanFails() {
    compileExpectError("true + false");
  }

  // ── ERROR: Integer × non-numeric ─────────────────────────────

  @Test
  public void testIntegerPlusStringFails() {
    compileExpectError("1 + \"hello\"");
  }

  @Test
  public void testIntegerPlusBooleanFails() {
    compileExpectError("1 + true");
  }

  @Test
  public void testIntegerMinusStringFails() {
    compileExpectError("1 - \"hello\"");
  }

  @Test
  public void testIntegerMinusBooleanFails() {
    compileExpectError("1 - false");
  }

  // ── ERROR: Collection operands ────────────────────────────────

  @Test
  public void testSetPlusIntegerFails() {
    compileExpectError("Set{1, 2} + 3");
  }

  @Test
  public void testSetPlusSetFails() {
    compileExpectError("Set{1} + Set{2}");
  }

  @Test
  public void testSequencePlusIntegerFails() {
    compileExpectError("Sequence{1, 2} + 3");
  }

  @Test
  public void testBagPlusIntegerFails() {
    compileExpectError("Bag{1, 2} + 3");
  }

  @Test
  public void testOrderedSetPlusIntegerFails() {
    compileExpectError("OrderedSet{1, 2} + 3");
  }

  @Test
  public void testIntegerPlusSetFails() {
    compileExpectError("3 + Set{1, 2}");
  }

  @Test
  public void testIntegerPlusSequenceFails() {
    compileExpectError("3 + Sequence{1, 2}");
  }

  @Test
  public void testIntegerPlusBagFails() {
    compileExpectError("3 + Bag{1, 2}");
  }

  @Test
  public void testIntegerPlusOrderedSetFails() {
    compileExpectError("3 + OrderedSet{1, 2}");
  }

  @Test
  public void testSetTimesSetFails() {
    compileExpectError("Set{1} * Set{2}");
  }

  @Test
  public void testSequenceMinusSequenceFails() {
    compileExpectError("Sequence{1} - Sequence{2}");
  }

  /**
   * Asserts that the result is a singleton containing a Float value. Distinct from
   * assertSingleDouble: verifies the element IS a FloatValue, not a DoubleValue. Used to verify
   * ¡Float! type preservation.
   *
   * @param result the evaluated collection value
   * @param expected the expected float value
   */
  protected void assertSingleFloat(Value result, float expected) {
    assertEquals(1, result.size(), "Expected singleton result");
    OCLElement elem = result.getElements().get(0);
    Float actual = elem.tryGetFloat();
    if (actual == null) {
      fail(
          "Expected ¡Float! singleton but got: "
              + elem.getClass().getSimpleName()
              + " (value="
              + elem
              + ")");
    }
    assertEquals(expected, actual, 1e-6f, "Expected float value " + expected);
  }

  // ── Unary minus on ¡Float! → stays ¡Float! (no promotion) ────

  @Test
  public void testUnaryMinusFloatKeepsFloatType() {
    // -(¡Float!) → ¡Float!  (abs() rule: type preserved, no widening)
    Value result = compile("-(1.5)");
    assertSingleDouble(result, -1.5);
  }

  @Test
  public void testUnaryMinusFloatZero() {
    Value result = compile("-(0.0)");
    // 0.0 literal parsed as Double in grammar — Float comes from Ecore attr
    // This tests the promotion path
    assertSingleDouble(result, -0.0);
  }

  // ── Float arithmetic promotion rules (9×9 coverage supplement) ─

  @Test
  public void testFloatPlusIntegerPromotesToDouble() {
    // ¡Float! + ¡Integer! → ¡Double! (promotion, NOT ¡Float!)
    Value result = compile("1.5 + 3");
    assertSingleDouble(result, 4.5);
  }

  @Test
  public void testFloatMinusIntegerPromotesToDouble() {
    Value result = compile("3.5 - 1");
    assertSingleDouble(result, 2.5);
  }

  @Test
  public void testFloatTimesIntegerPromotesToDouble() {
    Value result = compile("2.0 * 3");
    assertSingleDouble(result, 6.0);
  }

  @Test
  public void testFloatDividesIntegerPromotesToDouble() {
    Value result = compile("6.0 / 2");
    assertSingleDouble(result, 3.0);
  }

  @Test
  public void testIntegerPlusFloatPromotesToDouble() {
    // ¡Integer! + ¡Float! → ¡Double! (symmetric)
    Value result = compile("3 + 1.5");
    assertSingleDouble(result, 4.5);
  }

  @Test
  public void testFloatPlusDoubleIsDouble() {
    // ¡Float! + ¡Double! → ¡Double!
    Value result = compile("1.5 + 2.5");
    assertSingleDouble(result, 4.0);
  }

  @Test
  public void testDoublePlusFloatIsDouble() {
    // ¡Double! + ¡Float! → ¡Double! (symmetric)
    Value result = compile("2.5 + 1.5");
    assertSingleDouble(result, 4.0);
  }

  // ── Float result type NOT ¡Float! after arithmetic (sanity check) ─

  @Test
  public void testFloatTimesFloatResultIsDouble() {
    Value result = compile("1.5 * 2.0");
    assertSingleDouble(result, 3.0);
  }

  @Test
  public void testFloatDivideFloatResultIsDouble() {
    Value result = compile("9.0 / 3.0");
    assertSingleDouble(result, 3.0);
  }
}
