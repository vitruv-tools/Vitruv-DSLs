/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Type Matrix: Unary (- not)
 *
 * <pre>
 * unary -:
 *   ¡Integer! → ¡Integer!
 *   ¡Float!   → ¡Float!
 *   ¡Double!  → ¡Double!
 *   All others → ERROR
 *
 * not:
 *   ¡Boolean! → ¡Boolean!
 *   All others → ERROR
 * </pre>
 */
class UnaryTypeTest extends DummyTestSpecification {

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
    return parser.prefixedExpCS();
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

  // ── unary minus: valid ────────────────────────────────────────

  @Test
  void testUnaryMinusInteger() {
    assertSingleInt(compile("-(3)"), -3);
  }

  @Test
  void testUnaryMinusIntegerPositive() {
    assertSingleInt(compile("-(-5)"), 5);
  }

  @Test
  void testUnaryMinusFloat() {
    assertSingleDouble(compile("-(1.5)"), -1.5);
  }

  @Test
  void testUnaryMinusDouble() {
    assertSingleDouble(compile("-(2.5)"), -2.5);
  }

  // ── unary minus: ERROR ────────────────────────────────────────

  @Test
  void testUnaryMinusStringFails() {
    compileExpectError("-\"hello\"");
  }

  @Test
  void testUnaryMinusBooleanFails() {
    compileExpectError("-true");
  }

  @Test
  void testUnaryMinusSetFails() {
    compileExpectError("-Set{1, 2}");
  }

  @Test
  void testUnaryMinusSequenceFails() {
    compileExpectError("-Sequence{1, 2}");
  }

  @Test
  void testUnaryMinusBagFails() {
    compileExpectError("-Bag{1, 2}");
  }

  @Test
  void testUnaryMinusOrderedSetFails() {
    compileExpectError("-OrderedSet{1, 2}");
  }

  // ── not: valid ────────────────────────────────────────────────

  @Test
  void testNotBooleanTrue() {
    assertSingleBool(compile("not true"), false);
  }

  @Test
  void testNotBooleanFalse() {
    assertSingleBool(compile("not false"), true);
  }

  @Test
  void testNotDoubleNot() {
    assertSingleBool(compile("not (not true)"), true);
  }

  // ── not: ERROR ────────────────────────────────────────────────

  @Test
  void testNotIntegerFails() {
    compileExpectError("not 1");
  }

  @Test
  void testNotFloatFails() {
    compileExpectError("not 1.5");
  }

  @Test
  void testNotDoubleFails() {
    compileExpectError("not 2.5");
  }

  @Test
  void testNotStringFails() {
    compileExpectError("not \"hello\"");
  }

  @Test
  void testNotSetFails() {
    compileExpectError("not Set{true, false}");
  }

  @Test
  void testNotSequenceFails() {
    compileExpectError("not Sequence{true}");
  }

  @Test
  void testNotBagFails() {
    compileExpectError("not Bag{true}");
  }

  @Test
  void testNotOrderedSetFails() {
    compileExpectError("not OrderedSet{true}");
  }

  @Test
  void testUnaryMinusOnFloatLiteralStaysNumeric() {
    // -(1.5) — grammar parses 1.5 as Double → result is ¡Double!
    Value result = compile("-(1.5)");
    assertSingleDouble(result, -1.5);
  }

  @Test
  void testUnaryMinusOnNegativeFloat() {
    Value result = compile("-(-1.5)");
    assertSingleDouble(result, 1.5);
  }

  @Test
  void testUnaryMinusFloatNegated() {
    // Verify -(float expression) evaluates correctly
    Value result = compile("-(2.0 * 1.5)");
    assertSingleDouble(result, -3.0);
  }

  @Test
  void testUnaryMinusOnFloatArithmetic() {
    // -(¡Float! + ¡Float!) → ¡Double! (arithmetic promotes first)
    Value result = compile("-(1.5 + 2.5)");
    assertSingleDouble(result, -4.0);
  }
}
