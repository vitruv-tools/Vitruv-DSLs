/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.logical;

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
 * Type Matrix: Logical (and or xor implies not)
 *
 * <pre>
 * Valid (→ ¡Boolean!): ¡Boolean! × ¡Boolean! only (1 valid cell per binary op)
 * not: ¡Boolean! → ¡Boolean! (1 valid receiver)
 * All other combinations → ERROR
 * </pre>
 */
class LogicalTypeTest extends DummyTestSpecification {

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

  // ── and ───────────────────────────────────────────────────────

  @Test
  void testBooleanAndBooleanTrueTrue() {
    assertSingleBool(compile("true and true"), true);
  }

  @Test
  void testBooleanAndBooleanTrueFalse() {
    assertSingleBool(compile("true and false"), false);
  }

  @Test
  void testBooleanAndBooleanFalseTrue() {
    assertSingleBool(compile("false and true"), false);
  }

  @Test
  void testBooleanAndBooleanFalseFalse() {
    assertSingleBool(compile("false and false"), false);
  }

  // ── or ────────────────────────────────────────────────────────

  @Test
  void testBooleanOrBooleanTrueTrue() {
    assertSingleBool(compile("true or true"), true);
  }

  @Test
  void testBooleanOrBooleanTrueFalse() {
    assertSingleBool(compile("true or false"), true);
  }

  @Test
  void testBooleanOrBooleanFalseTrue() {
    assertSingleBool(compile("false or true"), true);
  }

  @Test
  void testBooleanOrBooleanFalseFalse() {
    assertSingleBool(compile("false or false"), false);
  }

  // ── xor ───────────────────────────────────────────────────────

  @Test
  void testBooleanXorBooleanTrueTrue() {
    assertSingleBool(compile("true xor true"), false);
  }

  @Test
  void testBooleanXorBooleanTrueFalse() {
    assertSingleBool(compile("true xor false"), true);
  }

  @Test
  void testBooleanXorBooleanFalseTrue() {
    assertSingleBool(compile("false xor true"), true);
  }

  @Test
  void testBooleanXorBooleanFalseFalse() {
    assertSingleBool(compile("false xor false"), false);
  }

  // ── implies ───────────────────────────────────────────────────

  @Test
  void testBooleanImpliesBooleanTrueTrue() {
    assertSingleBool(compile("true implies true"), true);
  }

  @Test
  void testBooleanImpliesBooleanTrueFalse() {
    assertSingleBool(compile("true implies false"), false);
  }

  @Test
  void testBooleanImpliesBooleanFalseTrue() {
    assertSingleBool(compile("false implies true"), true);
  }

  @Test
  void testBooleanImpliesBooleanFalseFalse() {
    assertSingleBool(compile("false implies false"), true);
  }

  // ── not (unary) ───────────────────────────────────────────────

  @Test
  void testNotBooleanTrue() {
    assertSingleBool(compile("not true"), false);
  }

  @Test
  void testNotBooleanFalse() {
    assertSingleBool(compile("not false"), true);
  }

  // ── ERROR: non-Boolean operands for binary ops ────────────────

  @Test
  void testIntegerAndIntegerFails() {
    compileExpectError("1 and 2");
  }

  @Test
  void testIntegerAndBooleanFails() {
    compileExpectError("1 and true");
  }

  @Test
  void testBooleanAndIntegerFails() {
    compileExpectError("true and 1");
  }

  @Test
  void testFloatAndBooleanFails() {
    compileExpectError("1.5 and true");
  }

  @Test
  void testDoubleAndBooleanFails() {
    compileExpectError("2.5 and true");
  }

  @Test
  void testStringAndBooleanFails() {
    compileExpectError("\"hello\" and true");
  }

  @Test
  void testBooleanAndStringFails() {
    compileExpectError("true and \"hello\"");
  }

  @Test
  void testStringOrStringFails() {
    compileExpectError("\"hello\" or \"world\"");
  }

  @Test
  void testIntegerOrBooleanFails() {
    compileExpectError("1 or true");
  }

  @Test
  void testIntegerXorBooleanFails() {
    compileExpectError("1 xor true");
  }

  @Test
  void testIntegerImpliesBooleanFails() {
    compileExpectError("1 implies true");
  }

  @Test
  void testSetAndBooleanFails() {
    compileExpectError("Set{true} and true");
  }

  @Test
  void testBooleanAndSetFails() {
    compileExpectError("true and Set{true}");
  }

  // ── ERROR: not on non-Boolean ─────────────────────────────────

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
    compileExpectError("not Set{true}");
  }
}
