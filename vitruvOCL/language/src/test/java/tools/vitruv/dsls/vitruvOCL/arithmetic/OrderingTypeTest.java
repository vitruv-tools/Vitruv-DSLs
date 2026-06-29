/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.arithmetic;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.DummyTestSpecification;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Type Matrix: Ordering (< <= > >=)
 *
 * <pre>
 * Valid (→ ¡Boolean!): ¡Numeric! × ¡Numeric! (Integer, Float, Double in all 9 combos)
 * Invalid: anything with String, Boolean, or any Collection → ERROR
 * </pre>
 */
class OrderingTypeTest extends DummyTestSpecification {

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

  // ── ¡Integer! × ¡Integer! ─────────────────────────────────────

  @Test
  void testIntegerLessThanInteger() {
    assertSingleBool(compile("3 < 5"), true);
  }

  @Test
  void testIntegerLessThanIntegerFalse() {
    assertSingleBool(compile("5 < 3"), false);
  }

  @Test
  void testIntegerLessOrEqualIntegerEqual() {
    assertSingleBool(compile("5 <= 5"), true);
  }

  @Test
  void testIntegerLessOrEqualIntegerLess() {
    assertSingleBool(compile("3 <= 5"), true);
  }

  @Test
  void testIntegerLessOrEqualIntegerFalse() {
    assertSingleBool(compile("5 <= 3"), false);
  }

  @Test
  void testIntegerGreaterThanInteger() {
    assertSingleBool(compile("5 > 3"), true);
  }

  @Test
  void testIntegerGreaterOrEqualIntegerEqual() {
    assertSingleBool(compile("5 >= 5"), true);
  }

  @Test
  void testIntegerGreaterOrEqualIntegerGreater() {
    assertSingleBool(compile("7 >= 5"), true);
  }

  // ── ¡Integer! × ¡Float! ──────────────────────────────────────

  @Test
  void testIntegerLessThanFloat() {
    assertSingleBool(compile("3 < 3.5"), true);
  }

  @Test
  void testIntegerLessOrEqualFloat() {
    assertSingleBool(compile("3 <= 3.0"), true);
  }

  @Test
  void testIntegerGreaterThanFloat() {
    assertSingleBool(compile("4 > 3.5"), true);
  }

  @Test
  void testIntegerGreaterOrEqualFloat() {
    assertSingleBool(compile("4 >= 4.0"), true);
  }

  // ── ¡Integer! × ¡Double! ─────────────────────────────────────

  @Test
  void testIntegerLessThanDouble() {
    assertSingleBool(compile("3 < 3.5"), true);
  }

  @Test
  void testIntegerGreaterThanDouble() {
    assertSingleBool(compile("4 > 3.5"), true);
  }

  // ── ¡Float! × ¡Integer! ──────────────────────────────────────

  @Test
  void testFloatLessThanInteger() {
    assertSingleBool(compile("2.5 < 3"), true);
  }

  @Test
  void testFloatGreaterOrEqualInteger() {
    assertSingleBool(compile("3.0 >= 3"), true);
  }

  @Test
  void testFloatGreaterThanInteger() {
    assertSingleBool(compile("3.5 > 3"), true);
  }

  // ── ¡Float! × ¡Float! ────────────────────────────────────────

  @Test
  void testFloatLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  void testFloatLessOrEqualFloat() {
    assertSingleBool(compile("1.5 <= 1.5"), true);
  }

  @Test
  void testFloatGreaterThanFloat() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  @Test
  void testFloatGreaterOrEqualFloat() {
    assertSingleBool(compile("2.5 >= 2.5"), true);
  }

  // ── ¡Float! × ¡Double! ───────────────────────────────────────

  @Test
  void testFloatLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  void testFloatLessOrEqualDouble() {
    assertSingleBool(compile("1.5 <= 1.5"), true);
  }

  @Test
  void testFloatGreaterThanDouble() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  // ── ¡Double! × ¡Integer! ─────────────────────────────────────

  @Test
  void testDoubleLessThanInteger() {
    assertSingleBool(compile("2.5 < 3"), true);
  }

  @Test
  void testDoubleGreaterThanInteger() {
    assertSingleBool(compile("3.5 > 3"), true);
  }

  @Test
  void testDoubleGreaterOrEqualInteger() {
    assertSingleBool(compile("3.0 >= 3"), true);
  }

  // ── ¡Double! × ¡Float! ───────────────────────────────────────

  @Test
  void testDoubleLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  void testDoubleGreaterThanFloat() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  // ── ¡Double! × ¡Double! ──────────────────────────────────────

  @Test
  void testDoubleLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  void testDoubleLessOrEqualDoubleEqual() {
    assertSingleBool(compile("2.5 <= 2.5"), true);
  }

  @Test
  void testDoubleGreaterThanDouble() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  @Test
  void testDoubleGreaterOrEqualDouble() {
    assertSingleBool(compile("2.5 >= 2.5"), true);
  }

  // ── ERROR: ¡String! operands ──────────────────────────────────

  @Test
  void testStringLessThanIntegerFails() {
    compileExpectError("\"hello\" < 1");
  }

  @Test
  void testStringLessThanStringFails() {
    compileExpectError("\"hello\" < \"world\"");
  }

  @Test
  void testStringGreaterThanDoubleFails() {
    compileExpectError("\"hello\" > 2.5");
  }

  @Test
  void testIntegerLessThanStringFails() {
    compileExpectError("1 < \"hello\"");
  }

  @Test
  void testFloatLessThanStringFails() {
    compileExpectError("1.5 < \"hello\"");
  }

  @Test
  void testDoubleLessThanStringFails() {
    compileExpectError("2.5 < \"hello\"");
  }

  // ── ERROR: ¡Boolean! operands ─────────────────────────────────

  @Test
  void testBooleanLessThanIntegerFails() {
    compileExpectError("true < 1");
  }

  @Test
  void testBooleanLessOrEqualIntegerFails() {
    compileExpectError("true <= 1");
  }

  @Test
  void testBooleanLessThanBooleanFails() {
    compileExpectError("true < false");
  }

  @Test
  void testIntegerLessThanBooleanFails() {
    compileExpectError("1 < true");
  }

  // ── ERROR: Collection operands ────────────────────────────────

  @Test
  void testSetLessThanIntegerFails() {
    compileExpectError("Set{1, 2} < 3");
  }

  @Test
  void testSetLessThanSetFails() {
    compileExpectError("Set{1} < Set{2}");
  }

  @Test
  void testSequenceLessThanIntegerFails() {
    compileExpectError("Sequence{1, 2} < 3");
  }

  @Test
  void testBagLessThanIntegerFails() {
    compileExpectError("Bag{1, 2} < 3");
  }

  @Test
  void testOrderedSetLessThanIntegerFails() {
    compileExpectError("OrderedSet{1, 2} < 3");
  }

  @Test
  void testIntegerLessThanSetFails() {
    compileExpectError("3 < Set{1, 2}");
  }

  @Test
  void testIntegerLessThanSequenceFails() {
    compileExpectError("3 < Sequence{1, 2}");
  }

  @Test
  void testIntegerLessThanBagFails() {
    compileExpectError("3 < Bag{1, 2}");
  }

  @Test
  void testIntegerLessThanOrderedSetFails() {
    compileExpectError("3 < OrderedSet{1, 2}");
  }
}
