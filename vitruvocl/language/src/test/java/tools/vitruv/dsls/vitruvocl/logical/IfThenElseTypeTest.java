/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.logical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Type Matrix: if-then-else expression
 *
 * <pre>
 * Condition must be ¡Boolean!. All other condition types → ERROR.
 *
 * Branch type matrix (condition = true, both branches evaluated for type):
 *   then:¡Integer! else:¡Integer!  → ¡Integer!
 *   then:¡Integer! else:¡Float!    → ¡Double!  (numeric promotion)
 *   then:¡Integer! else:¡Double!   → ¡Double!
 *   then:¡Float!   else:¡Double!   → ¡Double!
 *   then:¡Double!  else:¡Double!   → ¡Double!
 *   then:¡String!  else:¡String!   → ¡String!
 *   then:¡Boolean! else:¡Boolean!  → ¡Boolean!
 *   then:Set{T}  else:Set{T}   → Set{T}
 *   then:¡Integer! else:¡String!   → ERROR (incompatible branches)
 *   then:¡Boolean! else:¡Integer!  → ERROR
 *   then:Set{T}  else:¡Integer!  → ERROR
 * </pre>
 */
class IfThenElseTypeTest extends DummyTestSpecification {

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
    assertTrue(tc.hasErrors() || errors.hasErrors(), "Expected type error for: " + input);
  }

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.expCS();
  }

  // ══════════════════════════════════════════════════════════════
  // Valid condition: ¡Boolean! × compatible branch types
  // ══════════════════════════════════════════════════════════════

  // ── then:¡Integer! else:¡Integer! → ¡Integer! ────────────────

  @Test
  void testBoolCondIntegerThenIntegerElse() {
    assertSingleInt(compile("if true then 1 else 2 endif"), 1);
  }

  @Test
  void testBoolCondIntegerThenIntegerElseFalse() {
    assertSingleInt(compile("if false then 1 else 2 endif"), 2);
  }

  @Test
  void testBoolCondIntegerThenIntegerElseArith() {
    assertSingleInt(compile("if true then 3 + 4 else 0 endif"), 7);
  }

  // ── then:¡Integer! else:¡Float! → ¡Double! ───────────────────

  @Test
  void testBoolCondIntegerThenFloatElseTrueBranch() {
    assertSingleDouble(compile("if true then 1 else 1.5 endif"), 1.0);
  }

  @Test
  void testBoolCondIntegerThenFloatElseFalseBranch() {
    assertSingleDouble(compile("if false then 1 else 1.5 endif"), 1.5);
  }

  // ── then:¡Integer! else:¡Double! → ¡Double! ──────────────────

  @Test
  void testBoolCondIntegerThenDoublElseTrueBranch() {
    assertSingleDouble(compile("if true then 2 else 2.5 endif"), 2.0);
  }

  @Test
  void testBoolCondIntegerThenDoubleElseFalseBranch() {
    assertSingleDouble(compile("if false then 2 else 2.5 endif"), 2.5);
  }

  // ── then:¡Float! else:¡Float! → ¡Double! ─────────────────────

  @Test
  void testBoolCondFloatThenFloatElse() {
    assertSingleDouble(compile("if true then 1.5 else 2.5 endif"), 1.5);
  }

  // ── then:¡Float! else:¡Double! → ¡Double! ────────────────────

  @Test
  void testBoolCondFloatThenDoubleElse() {
    assertSingleDouble(compile("if false then 1.5 else 2.5 endif"), 2.5);
  }

  // ── then:¡Double! else:¡Double! → ¡Double! ───────────────────

  @Test
  void testBoolCondDoubleThenDoubleElse() {
    assertSingleDouble(compile("if true then 1.5 else 2.5 endif"), 1.5);
  }

  // ── then:¡String! else:¡String! → ¡String! ───────────────────

  @Test
  void testBoolCondStringThenStringElseTrueBranch() {
    assertSingleString(compile("if true then \"yes\" else \"no\" endif"), "yes");
  }

  @Test
  void testBoolCondStringThenStringElseFalseBranch() {
    assertSingleString(compile("if false then \"yes\" else \"no\" endif"), "no");
  }

  @Test
  void testBoolCondStringThenStringElseEmpty() {
    assertSingleString(compile("if true then \"hello\" else \"\" endif"), "hello");
  }

  // ── then:¡Boolean! else:¡Boolean! → ¡Boolean! ────────────────

  @Test
  void testBoolCondBooleanThenBooleanElseTrueBranch() {
    assertSingleBool(compile("if true then true else false endif"), true);
  }

  @Test
  void testBoolCondBooleanThenBooleanElseFalseBranch() {
    assertSingleBool(compile("if false then true else false endif"), false);
  }

  // ── then:Set{T} else:Set{T} → Set{T} ───────────────────

  @ParameterizedTest
  @MethodSource("twoElementCollectionIfExpressions")
  void testTwoElementCollectionIf(String expr) {
    assertEquals(2, compile(expr).size());
  }

  static Stream<String> twoElementCollectionIfExpressions() {
    return Stream.of(
        "if true then Set{1, 2} else Set{3, 4} endif",
        "if false then Set{1, 2} else Set{3, 4} endif",
        "if true then Sequence{1, 2} else Sequence{3, 4} endif",
        "if true then Bag{1, 1} else Bag{2, 2} endif",
        "if false then OrderedSet{1, 2} else OrderedSet{3, 4} endif");
  }

  // ── condition derived from expression ─────────────────────────

  @Test
  void testArithConditionTrueBranch() {
    assertSingleInt(compile("if 3 > 2 then 1 else 0 endif"), 1);
  }

  @Test
  void testArithConditionFalseBranch() {
    assertSingleInt(compile("if 2 > 3 then 1 else 0 endif"), 0);
  }

  @Test
  void testLogicalCondition() {
    assertSingleBool(compile("if true and false then true else false endif"), false);
  }

  @Test
  void testNestedIfThenElse() {
    assertSingleInt(compile("if true then (if false then 1 else 2 endif) else 3 endif"), 2);
  }

  // ══════════════════════════════════════════════════════════════
  // ERROR: non-Boolean condition
  // ══════════════════════════════════════════════════════════════

  @Test
  void testIntegerConditionFails() {
    compileExpectError("if 1 then 1 else 2 endif");
  }

  @Test
  void testFloatConditionFails() {
    compileExpectError("if 1.5 then 1 else 2 endif");
  }

  @Test
  void testDoubleConditionFails() {
    compileExpectError("if 2.5 then 1 else 2 endif");
  }

  @Test
  void testStringConditionFails() {
    compileExpectError("if \"hello\" then 1 else 2 endif");
  }

  @Test
  void testIntegerCollectionConditionFails() {
    compileExpectError("if Set{1, 2} then 1 else 2 endif");
  }

  @Test
  void testSequenceConditionFails() {
    compileExpectError("if Sequence{true} then 1 else 2 endif");
  }

  // ══════════════════════════════════════════════════════════════
  // ERROR: incompatible branch types
  // ══════════════════════════════════════════════════════════════

  @Test
  void testIntegerThenStringElseFails() {
    compileExpectError("if true then 1 else \"hello\" endif");
  }

  @Test
  void testStringThenIntegerElseFails() {
    compileExpectError("if true then \"hello\" else 1 endif");
  }

  @Test
  void testBooleanThenIntegerElseFails() {
    compileExpectError("if true then true else 1 endif");
  }

  @Test
  void testIntegerThenBooleanElseFails() {
    compileExpectError("if true then 1 else true endif");
  }

  @Test
  void testStringThenBooleanElseFails() {
    compileExpectError("if true then \"hello\" else true endif");
  }

  @Test
  void testSetThenIntegerElseFails() {
    compileExpectError("if true then Set{1} else 1 endif");
  }

  @Test
  void testIntegerThenSetElseFails() {
    compileExpectError("if true then 1 else Set{1} endif");
  }

  @Test
  void testSetThenBooleanElseFails() {
    compileExpectError("if true then Set{1} else true endif");
  }

  @Test
  void testSetThenStringElseFails() {
    compileExpectError("if true then Set{1} else \"hello\" endif");
  }

  @Test
  void testSequenceThenSetElseFails() {
    // different collection kinds — incompatible unless type system allows it
    compileExpectError("if true then Sequence{1} else Set{1} endif");
  }
}
