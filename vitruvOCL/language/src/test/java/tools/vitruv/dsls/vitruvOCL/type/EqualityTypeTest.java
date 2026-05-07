/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.type;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.DummyTestSpecification;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Type Matrix: Equality (== !=)
 *
 * <pre>
 * Valid (→ ¡Boolean!):
 *   ¡Integer! == ¡Integer!, ¡Float!, ¡Double!
 *   ¡Float!   == ¡Integer!, ¡Float!, ¡Double!
 *   ¡Double!  == ¡Integer!, ¡Float!, ¡Double!
 *   ¡String!  == ¡String!
 *   ¡Boolean! == ¡Boolean!
 *
 * Invalid: any collection × anything, cross-kind comparisons → ERROR
 * </pre>
 */
public class EqualityTypeTest extends DummyTestSpecification {

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
    if (tc.hasErrors() || errors.hasErrors()) {
      return;
    }
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, tc.getNodeTypes());
    evaluator.visit(tree);
    assertTrue(
        evaluator.hasErrors() || errors.hasErrors(),
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
  public void testIntegerEqualsInteger() {
    assertSingleBool(compile("5 == 5"), true);
  }

  @Test
  public void testIntegerNotEqualsInteger() {
    assertSingleBool(compile("5 != 3"), true);
  }

  @Test
  public void testIntegerEqualsIntegerFalse() {
    assertSingleBool(compile("5 == 3"), false);
  }

  // ── ¡Integer! × ¡Float! ──────────────────────────────────────

  @Test
  public void testIntegerEqualsFloat() {
    assertSingleBool(compile("3 == 3.0"), true);
  }

  @Test
  public void testIntegerNotEqualsFloat() {
    assertSingleBool(compile("3 != 3.5"), true);
  }

  // ── ¡Integer! × ¡Double! ─────────────────────────────────────

  @Test
  public void testIntegerEqualsDouble() {
    assertSingleBool(compile("3 == 3.0"), true);
  }

  @Test
  public void testIntegerNotEqualsDouble() {
    assertSingleBool(compile("3 != 4.0"), true);
  }

  // ── ¡Float! × ¡Integer! ──────────────────────────────────────

  @Test
  public void testFloatEqualsInteger() {
    assertSingleBool(compile("3.0 == 3"), true);
  }

  @Test
  public void testFloatNotEqualsInteger() {
    assertSingleBool(compile("3.5 != 3"), true);
  }

  // ── ¡Float! × ¡Float! ────────────────────────────────────────

  @Test
  public void testFloatEqualsFloat() {
    assertSingleBool(compile("1.5 == 1.5"), true);
  }

  @Test
  public void testFloatNotEqualsFloat() {
    assertSingleBool(compile("1.5 != 2.5"), true);
  }

  // ── ¡Float! × ¡Double! ───────────────────────────────────────

  @Test
  public void testFloatEqualsDouble() {
    assertSingleBool(compile("1.5 == 1.5"), true);
  }

  @Test
  public void testFloatNotEqualsDouble() {
    assertSingleBool(compile("1.5 != 2.5"), true);
  }

  // ── ¡Double! × ¡Integer! ─────────────────────────────────────

  @Test
  public void testDoubleEqualsInteger() {
    assertSingleBool(compile("3.0 == 3"), true);
  }

  @Test
  public void testDoubleNotEqualsInteger() {
    assertSingleBool(compile("3.5 != 3"), true);
  }

  // ── ¡Double! × ¡Float! ───────────────────────────────────────

  @Test
  public void testDoubleEqualsFloat() {
    assertSingleBool(compile("2.5 == 2.5"), true);
  }

  @Test
  public void testDoubleNotEqualsFloat() {
    assertSingleBool(compile("2.5 != 1.5"), true);
  }

  // ── ¡Double! × ¡Double! ──────────────────────────────────────

  @Test
  public void testDoubleEqualsDouble() {
    assertSingleBool(compile("2.5 == 2.5"), true);
  }

  @Test
  public void testDoubleNotEqualsDouble() {
    assertSingleBool(compile("2.5 != 3.5"), true);
  }

  // ── ¡String! × ¡String! ──────────────────────────────────────

  @Test
  public void testStringEqualsString() {
    assertSingleBool(compile("\"hello\" == \"hello\""), true);
  }

  @Test
  public void testStringNotEqualsString() {
    assertSingleBool(compile("\"hello\" != \"world\""), true);
  }

  @Test
  public void testStringEqualsStringFalse() {
    assertSingleBool(compile("\"hello\" == \"world\""), false);
  }

  // ── ¡Boolean! × ¡Boolean! ────────────────────────────────────

  @Test
  public void testBooleanEqualsBoolean() {
    assertSingleBool(compile("true == true"), true);
  }

  @Test
  public void testBooleanNotEqualsBoolean() {
    assertSingleBool(compile("true != false"), true);
  }

  @Test
  public void testBooleanEqualsBooleanFalse() {
    assertSingleBool(compile("true == false"), false);
  }

  // ── ERROR: cross-kind ─────────────────────────────────────────

  @Test
  public void testIntegerEqualsStringFails() {
    compileExpectError("1 == \"hello\"");
  }

  @Test
  public void testIntegerEqualsBooleanFails() {
    compileExpectError("1 == true");
  }

  @Test
  public void testStringEqualsIntegerFails() {
    compileExpectError("\"hello\" == 1");
  }

  @Test
  public void testStringEqualsFloatFails() {
    compileExpectError("\"hello\" == 1.5");
  }

  @Test
  public void testStringEqualsDoubleFails() {
    compileExpectError("\"hello\" == 2.5");
  }

  @Test
  public void testStringEqualsBooleanFails() {
    compileExpectError("\"hello\" == true");
  }

  @Test
  public void testBooleanEqualsIntegerFails() {
    compileExpectError("true == 1");
  }

  @Test
  public void testBooleanEqualsStringFails() {
    compileExpectError("true == \"hello\"");
  }

  @Test
  public void testBooleanEqualsFloatFails() {
    compileExpectError("true == 1.5");
  }

  // ── ERROR: collection operands ────────────────────────────────

  @Test
  public void testSetEqualsIntegerFails() {
    compileExpectError("Set{1, 2} == 1");
  }

  @Test
  public void testSetEqualsSetFails() {
    compileExpectError("Set{1, 2} == Set{1, 2}");
  }

  @Test
  public void testSequenceEqualsIntegerFails() {
    compileExpectError("Sequence{1, 2} == 1");
  }

  @Test
  public void testBagEqualsIntegerFails() {
    compileExpectError("Bag{1, 2} == 1");
  }

  @Test
  public void testOrderedSetEqualsIntegerFails() {
    compileExpectError("OrderedSet{1, 2} == 1");
  }

  @Test
  public void testIntegerEqualsSetFails() {
    compileExpectError("1 == Set{1, 2}");
  }

  @Test
  public void testIntegerEqualsSequenceFails() {
    compileExpectError("1 == Sequence{1, 2}");
  }

  @Test
  public void testIntegerEqualsBagFails() {
    compileExpectError("1 == Bag{1, 2}");
  }

  @Test
  public void testIntegerEqualsOrderedSetFails() {
    compileExpectError("1 == OrderedSet{1, 2}");
  }
}
