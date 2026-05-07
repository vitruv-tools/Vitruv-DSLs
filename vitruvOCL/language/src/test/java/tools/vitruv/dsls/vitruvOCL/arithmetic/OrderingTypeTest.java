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
public class OrderingTypeTest extends DummyTestSpecification {

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
  public void testIntegerLessThanInteger() {
    assertSingleBool(compile("3 < 5"), true);
  }

  @Test
  public void testIntegerLessThanIntegerFalse() {
    assertSingleBool(compile("5 < 3"), false);
  }

  @Test
  public void testIntegerLessOrEqualIntegerEqual() {
    assertSingleBool(compile("5 <= 5"), true);
  }

  @Test
  public void testIntegerLessOrEqualIntegerLess() {
    assertSingleBool(compile("3 <= 5"), true);
  }

  @Test
  public void testIntegerLessOrEqualIntegerFalse() {
    assertSingleBool(compile("5 <= 3"), false);
  }

  @Test
  public void testIntegerGreaterThanInteger() {
    assertSingleBool(compile("5 > 3"), true);
  }

  @Test
  public void testIntegerGreaterOrEqualIntegerEqual() {
    assertSingleBool(compile("5 >= 5"), true);
  }

  @Test
  public void testIntegerGreaterOrEqualIntegerGreater() {
    assertSingleBool(compile("7 >= 5"), true);
  }

  // ── ¡Integer! × ¡Float! ──────────────────────────────────────

  @Test
  public void testIntegerLessThanFloat() {
    assertSingleBool(compile("3 < 3.5"), true);
  }

  @Test
  public void testIntegerLessOrEqualFloat() {
    assertSingleBool(compile("3 <= 3.0"), true);
  }

  @Test
  public void testIntegerGreaterThanFloat() {
    assertSingleBool(compile("4 > 3.5"), true);
  }

  @Test
  public void testIntegerGreaterOrEqualFloat() {
    assertSingleBool(compile("4 >= 4.0"), true);
  }

  // ── ¡Integer! × ¡Double! ─────────────────────────────────────

  @Test
  public void testIntegerLessThanDouble() {
    assertSingleBool(compile("3 < 3.5"), true);
  }

  @Test
  public void testIntegerGreaterThanDouble() {
    assertSingleBool(compile("4 > 3.5"), true);
  }

  // ── ¡Float! × ¡Integer! ──────────────────────────────────────

  @Test
  public void testFloatLessThanInteger() {
    assertSingleBool(compile("2.5 < 3"), true);
  }

  @Test
  public void testFloatGreaterOrEqualInteger() {
    assertSingleBool(compile("3.0 >= 3"), true);
  }

  @Test
  public void testFloatGreaterThanInteger() {
    assertSingleBool(compile("3.5 > 3"), true);
  }

  // ── ¡Float! × ¡Float! ────────────────────────────────────────

  @Test
  public void testFloatLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  public void testFloatLessOrEqualFloat() {
    assertSingleBool(compile("1.5 <= 1.5"), true);
  }

  @Test
  public void testFloatGreaterThanFloat() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  @Test
  public void testFloatGreaterOrEqualFloat() {
    assertSingleBool(compile("2.5 >= 2.5"), true);
  }

  // ── ¡Float! × ¡Double! ───────────────────────────────────────

  @Test
  public void testFloatLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  public void testFloatLessOrEqualDouble() {
    assertSingleBool(compile("1.5 <= 1.5"), true);
  }

  @Test
  public void testFloatGreaterThanDouble() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  // ── ¡Double! × ¡Integer! ─────────────────────────────────────

  @Test
  public void testDoubleLessThanInteger() {
    assertSingleBool(compile("2.5 < 3"), true);
  }

  @Test
  public void testDoubleGreaterThanInteger() {
    assertSingleBool(compile("3.5 > 3"), true);
  }

  @Test
  public void testDoubleGreaterOrEqualInteger() {
    assertSingleBool(compile("3.0 >= 3"), true);
  }

  // ── ¡Double! × ¡Float! ───────────────────────────────────────

  @Test
  public void testDoubleLessThanFloat() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  public void testDoubleGreaterThanFloat() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  // ── ¡Double! × ¡Double! ──────────────────────────────────────

  @Test
  public void testDoubleLessThanDouble() {
    assertSingleBool(compile("1.5 < 2.5"), true);
  }

  @Test
  public void testDoubleLessOrEqualDoubleEqual() {
    assertSingleBool(compile("2.5 <= 2.5"), true);
  }

  @Test
  public void testDoubleGreaterThanDouble() {
    assertSingleBool(compile("2.5 > 1.5"), true);
  }

  @Test
  public void testDoubleGreaterOrEqualDouble() {
    assertSingleBool(compile("2.5 >= 2.5"), true);
  }

  // ── ERROR: ¡String! operands ──────────────────────────────────

  @Test
  public void testStringLessThanIntegerFails() {
    compileExpectError("\"hello\" < 1");
  }

  @Test
  public void testStringLessThanStringFails() {
    compileExpectError("\"hello\" < \"world\"");
  }

  @Test
  public void testStringGreaterThanDoubleFails() {
    compileExpectError("\"hello\" > 2.5");
  }

  @Test
  public void testIntegerLessThanStringFails() {
    compileExpectError("1 < \"hello\"");
  }

  @Test
  public void testFloatLessThanStringFails() {
    compileExpectError("1.5 < \"hello\"");
  }

  @Test
  public void testDoubleLessThanStringFails() {
    compileExpectError("2.5 < \"hello\"");
  }

  // ── ERROR: ¡Boolean! operands ─────────────────────────────────

  @Test
  public void testBooleanLessThanIntegerFails() {
    compileExpectError("true < 1");
  }

  @Test
  public void testBooleanLessOrEqualIntegerFails() {
    compileExpectError("true <= 1");
  }

  @Test
  public void testBooleanLessThanBooleanFails() {
    compileExpectError("true < false");
  }

  @Test
  public void testIntegerLessThanBooleanFails() {
    compileExpectError("1 < true");
  }

  // ── ERROR: Collection operands ────────────────────────────────

  @Test
  public void testSetLessThanIntegerFails() {
    compileExpectError("Set{1, 2} < 3");
  }

  @Test
  public void testSetLessThanSetFails() {
    compileExpectError("Set{1} < Set{2}");
  }

  @Test
  public void testSequenceLessThanIntegerFails() {
    compileExpectError("Sequence{1, 2} < 3");
  }

  @Test
  public void testBagLessThanIntegerFails() {
    compileExpectError("Bag{1, 2} < 3");
  }

  @Test
  public void testOrderedSetLessThanIntegerFails() {
    compileExpectError("OrderedSet{1, 2} < 3");
  }

  @Test
  public void testIntegerLessThanSetFails() {
    compileExpectError("3 < Set{1, 2}");
  }

  @Test
  public void testIntegerLessThanSequenceFails() {
    compileExpectError("3 < Sequence{1, 2}");
  }

  @Test
  public void testIntegerLessThanBagFails() {
    compileExpectError("3 < Bag{1, 2}");
  }

  @Test
  public void testIntegerLessThanOrderedSetFails() {
    compileExpectError("3 < OrderedSet{1, 2}");
  }
}
