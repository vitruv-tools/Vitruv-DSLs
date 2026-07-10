/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.collections;

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
 * Type Matrix: Collection Operations (no argument)
 *
 * <p>Valid receiver: Set{T}, Sequence{T}, Bag{T}, OrderedSet{T}.
 * first()/last()/at()/subSequence()/insertAt()/prepend(): ordered collections only (Sequence and
 * OrderedSet). abs/floor/ceil/ceiling/round: ¡Numeric! singletons only. All singleton receivers for
 * collection ops → ERROR. All collection receivers for scalar ops → ERROR.
 */
class CollectionNullaryOpsTypeTest extends DummyTestSpecification {

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
    return parser.prefixedExpCS();
  }

  // ══════════════════════════════════════════════════════════════
  // size()
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIntSize() {
    assertSingleInt(compile("Set{1, 2, 3}.size()"), 3);
  }

  @Test
  void testEmptySetSize() {
    assertSingleInt(compile("Set{}.size()"), 0);
  }

  @Test
  void testSequenceIntSize() {
    assertSingleInt(compile("Sequence{1, 2, 3}.size()"), 3);
  }

  @Test
  void testBagIntSize() {
    assertSingleInt(compile("Bag{1, 1, 2}.size()"), 3);
  }

  @Test
  void testOrderedSetIntSize() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.size()"), 3);
  }

  @Test
  void testIntegerSizeOnSingleton() {
    // ¡Integer! scalar — size() requires explicit collection → error
    compileExpectError("1.size()");
  }

  @Test
  void testFloatSizeOnSingleton() {
    compileExpectError("1.5.size()");
  }

  @Test
  void testDoubleSizeOnSingleton() {
    compileExpectError("2.5.size()");
  }

  @Test
  void testBooleanSizeOnSingleton() {
    compileExpectError("true.size()");
  }

  // ══════════════════════════════════════════════════════════════
  // isEmpty() / notEmpty()
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEmptySetIsEmpty() {
    assertSingleBool(compile("Set{}.isEmpty()"), true);
  }

  @Test
  void testNonEmptySetIsEmpty() {
    assertSingleBool(compile("Set{1}.isEmpty()"), false);
  }

  @Test
  void testNonEmptySetNotEmpty() {
    assertSingleBool(compile("Set{1}.notEmpty()"), true);
  }

  @Test
  void testEmptySetNotEmpty() {
    assertSingleBool(compile("Set{}.notEmpty()"), false);
  }

  @Test
  void testSequenceIsEmpty() {
    assertSingleBool(compile("Sequence{}.isEmpty()"), true);
  }

  @Test
  void testSequenceNotEmpty() {
    assertSingleBool(compile("Sequence{1, 2}.notEmpty()"), true);
  }

  @Test
  void testBagIsEmpty() {
    assertSingleBool(compile("Bag{}.isEmpty()"), true);
  }

  @Test
  void testOrderedSetIsEmpty() {
    assertSingleBool(compile("OrderedSet{}.isEmpty()"), true);
  }

  @Test
  void testIntegerIsEmptyOnSingleton() {
    // ¡Integer! scalar — isEmpty() requires explicit collection → error
    compileExpectError("1.isEmpty()");
  }

  @Test
  void testBooleanIsEmptyOnSingleton() {
    compileExpectError("true.isEmpty()");
  }

  @Test
  void testStringIsEmptyOnSingleton() {
    compileExpectError("\"hello\".isEmpty()");
  }

  // ══════════════════════════════════════════════════════════════
  // first() / last()  (ordered collections only)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceFirst() {
    Value r = compile("Sequence{10, 20, 30}.first()");
    assertEquals(1, r.size());
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceLast() {
    Value r = compile("Sequence{10, 20, 30}.last()");
    assertEquals(1, r.size());
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetFirst() {
    Value r = compile("OrderedSet{10, 20, 30}.first()");
    assertEquals(1, r.size());
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetLast() {
    Value r = compile("OrderedSet{10, 20, 30}.last()");
    assertEquals(1, r.size());
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetFirstFails() {
    compileExpectError("Set{1, 2}.first()");
  }

  @Test
  void testSetLastFails() {
    compileExpectError("Set{1, 2}.last()");
  }

  @Test
  void testBagFirstFails() {
    compileExpectError("Bag{1, 2}.first()");
  }

  @Test
  void testBagLastFails() {
    compileExpectError("Bag{1, 2}.last()");
  }

  // ══════════════════════════════════════════════════════════════
  // reverse()
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceReverse() {
    Value r = compile("Sequence{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testOrderedSetReverse() {
    Value r = compile("OrderedSet{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetReverse() {
    Value r = compile("Set{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
  }

  @Test
  void testBagReverse() {
    Value r = compile("Bag{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
  }

  @Test
  void testIntegerReverseFails() {
    compileExpectError("1.reverse()");
  }

  @Test
  void testBooleanReverseFails() {
    compileExpectError("true.reverse()");
  }

  // ══════════════════════════════════════════════════════════════
  // sum()
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIntSum() {
    assertSingleInt(compile("Set{1, 2, 3}.sum()"), 6);
  }

  @Test
  void testSequenceIntSum() {
    assertSingleInt(compile("Sequence{1, 2, 3}.sum()"), 6);
  }

  @Test
  void testBagIntSum() {
    assertSingleInt(compile("Bag{1, 1, 2}.sum()"), 4);
  }

  @Test
  void testOrderedSetIntSum() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.sum()"), 6);
  }

  @Test
  void testSetDoubleSum() {
    assertSingleDouble(compile("Set{1.5, 2.5}.sum()"), 4.0);
  }

  @Test
  void testEmptySetSum() {
    assertSingleInt(compile("Set{}.sum()"), 0);
  }

  @Test
  void testIntegerSumFails() {
    compileExpectError("1.sum()");
  }

  @Test
  void testBooleanSumFails() {
    compileExpectError("true.sum()");
  }

  // ══════════════════════════════════════════════════════════════
  // max() / min()
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIntMax() {
    Value r = compile("Set{1, 5, 3}.max()");
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetIntMin() {
    Value r = compile("Set{1, 5, 3}.min()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceIntMax() {
    Value r = compile("Sequence{3, 1, 4, 1, 5}.max()");
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceIntMin() {
    Value r = compile("Sequence{3, 1, 4, 1, 5}.min()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testBagIntMax() {
    Value r = compile("Bag{2, 7, 3}.max()");
    assertEquals(7, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetIntMax() {
    Value r = compile("OrderedSet{1, 2, 3}.max()");
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetDoubleMax() {
    assertSingleDouble(compile("Set{1.5, 3.5, 2.5}.max()"), 3.5);
  }

  @Test
  void testSetDoubleMin() {
    assertSingleDouble(compile("Set{1.5, 3.5, 2.5}.min()"), 1.5);
  }

  @Test
  void testIntegerMaxFails() {
    compileExpectError("1.max()");
  }

  @Test
  void testBooleanMinFails() {
    compileExpectError("true.min()");
  }

  // ══════════════════════════════════════════════════════════════
  // avg()
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIntAvg() {
    assertSingleDouble(compile("Set{1, 2, 3}.avg()"), 2.0);
  }

  @Test
  void testSequenceIntAvg() {
    assertSingleDouble(compile("Sequence{1, 2, 3, 4}.avg()"), 2.5);
  }

  @Test
  void testBagIntAvg() {
    assertSingleDouble(compile("Bag{2, 4}.avg()"), 3.0);
  }

  @Test
  void testOrderedSetAvg() {
    assertSingleDouble(compile("OrderedSet{1, 2, 3}.avg()"), 2.0);
  }

  @Test
  void testIntegerAvgFails() {
    compileExpectError("1.avg()");
  }

  // ══════════════════════════════════════════════════════════════
  // asSet() / asBag() / asSequence() / asOrderedSet() ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetAsSet() {
    Value r = compile("Set{1, 2, 3}.asSet()");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceAsSetDedup() {
    Value r = compile("Sequence{1, 2, 2, 3}.asSet()");
    assertEquals(3, r.size());
  }

  @Test
  void testBagAsSetDedup() {
    Value r = compile("Bag{1, 1, 2}.asSet()");
    assertEquals(2, r.size());
  }

  @Test
  void testOrderedSetAsSet() {
    Value r = compile("OrderedSet{1, 2, 3}.asSet()");
    assertEquals(3, r.size());
  }

  @Test
  void testSetAsBag() {
    Value r = compile("Set{1, 2, 3}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceAsBag() {
    Value r = compile("Sequence{1, 2, 3}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  void testBagAsBag() {
    Value r = compile("Bag{1, 1, 2}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  void testOrderedSetAsBag() {
    Value r = compile("OrderedSet{1, 2, 3}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  void testSetAsSequence() {
    Value r = compile("Set{1, 2, 3}.asSequence()");
    assertEquals(3, r.size());
  }

  @Test
  void testBagAsSequence() {
    Value r = compile("Bag{1, 1, 2}.asSequence()");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceAsSequence() {
    Value r = compile("Sequence{1, 2}.asSequence()");
    assertEquals(2, r.size());
  }

  @Test
  void testOrderedSetAsSequence() {
    Value r = compile("OrderedSet{1, 2, 3}.asSequence()");
    assertEquals(3, r.size());
  }

  @Test
  void testSetAsOrderedSet() {
    Value r = compile("Set{1, 2, 3}.asOrderedSet()");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceAsOrderedSetDedup() {
    Value r = compile("Sequence{1, 2, 2, 3}.asOrderedSet()");
    assertEquals(3, r.size());
  }

  @Test
  void testBagAsOrderedSetDedup() {
    Value r = compile("Bag{1, 1, 2}.asOrderedSet()");
    assertEquals(2, r.size());
  }

  @Test
  void testOrderedSetAsOrderedSet() {
    Value r = compile("OrderedSet{1, 2, 3}.asOrderedSet()");
    assertEquals(3, r.size());
  }

  @Test
  void testIntegerAsSetFails() {
    compileExpectError("1.asSet()");
  }

  @Test
  void testBooleanAsBagFails() {
    compileExpectError("true.asBag()");
  }

  @Test
  void testStringAsSequenceFails() {
    compileExpectError("\"hello\".asSequence()");
  }

  @Test
  void testIntegerAsOrderedSetFails() {
    compileExpectError("1.asOrderedSet()");
  }

  // ══════════════════════════════════════════════════════════════
  // at(i) ★ (ordered collections)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceAtFirst() {
    Value r = compile("Sequence{10, 20, 30}.at(1)");
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceAtMiddle() {
    Value r = compile("Sequence{10, 20, 30}.at(2)");
    assertEquals(20, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceAtLast() {
    Value r = compile("Sequence{10, 20, 30}.at(3)");
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetAt() {
    Value r = compile("OrderedSet{10, 20, 30}.at(2)");
    assertEquals(20, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetAtFails() {
    compileExpectError("Set{1, 2}.at(1)");
  }

  @Test
  void testBagAtFails() {
    compileExpectError("Bag{1, 2}.at(1)");
  }

  @Test
  void testIntegerAtFails() {
    compileExpectError("1.at(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // subSequence(i, j) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceSubSequenceMiddle() {
    Value r = compile("Sequence{1, 2, 3, 4}.subSequence(2, 3)");
    assertEquals(2, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  void testSequenceSubSequenceFull() {
    Value r = compile("Sequence{1, 2, 3}.subSequence(1, 3)");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceSubSequenceSingle() {
    Value r = compile("Sequence{1, 2, 3}.subSequence(2, 2)");
    assertEquals(1, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetSubSequence() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.subSequence(1, 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testSetSubSequenceFails() {
    compileExpectError("Set{1, 2}.subSequence(1, 2)");
  }

  @Test
  void testBagSubSequenceFails() {
    compileExpectError("Bag{1, 2}.subSequence(1, 2)");
  }

  @Test
  void testIntegerSubSequenceFails() {
    compileExpectError("1.subSequence(1, 1)");
  }

  // ══════════════════════════════════════════════════════════════
  // abs() / floor() / ceil() / ceiling() / round()
  // ══════════════════════════════════════════════════════════════

  @Test
  void testIntegerAbsPositive() {
    assertSingleInt(compile("(-3).abs()"), 3);
  }

  @Test
  void testIntegerAbsNegative() {
    assertSingleInt(compile("(-5).abs()"), 5);
  }

  @Test
  void testIntegerAbsZero() {
    assertSingleInt(compile("0.abs()"), 0);
  }

  @Test
  void testFloatAbs() {
    assertSingleDouble(compile("(-1.5).abs()"), 1.5);
  }

  @Test
  void testDoubleAbs() {
    assertSingleDouble(compile("(-2.5).abs()"), 2.5);
  }

  @Test
  void testStringAbsFails() {
    compileExpectError("\"hello\".abs()");
  }

  @Test
  void testBooleanAbsFails() {
    compileExpectError("true.abs()");
  }

  @Test
  void testSetAbsFails() {
    compileExpectError("Set{1, 2}.abs()");
  }

  @Test
  void testSequenceAbsFails() {
    compileExpectError("Sequence{1, 2}.abs()");
  }

  @Test
  void testIntegerFloor() {
    assertSingleInt(compile("3.floor()"), 3);
  }

  @Test
  void testFloatFloor() {
    assertSingleDouble(compile("2.7.floor()"), 2.0);
  }

  @Test
  void testDoubleFloor() {
    assertSingleDouble(compile("2.9.floor()"), 2.0);
  }

  @Test
  void testStringFloorFails() {
    compileExpectError("\"hello\".floor()");
  }

  @Test
  void testSetFloorFails() {
    compileExpectError("Set{1, 2}.floor()");
  }

  @Test
  void testIntegerCeil() {
    assertSingleInt(compile("3.ceil()"), 3);
  }

  @Test
  void testFloatCeil() {
    assertSingleDouble(compile("2.3.ceil()"), 3.0);
  }

  @Test
  void testDoubleCeil() {
    assertSingleDouble(compile("2.1.ceil()"), 3.0);
  }

  @Test
  void testStringCeilFails() {
    compileExpectError("\"hello\".ceil()");
  }

  @Test
  void testSetCeilFails() {
    compileExpectError("Set{1}.ceil()");
  }

  @Test
  void testIntegerCeiling() {
    assertSingleInt(compile("3.ceiling()"), 3);
  }

  @Test
  void testFloatCeiling() {
    assertSingleDouble(compile("2.3.ceiling()"), 3.0);
  }

  @Test
  void testDoubleCeilingAlreadyWhole() {
    assertSingleDouble(compile("3.0.ceiling()"), 3.0);
  }

  @Test
  void testStringCeilingFails() {
    compileExpectError("\"hello\".ceiling()");
  }

  @Test
  void testIntegerRound() {
    assertSingleInt(compile("3.round()"), 3);
  }

  @Test
  void testFloatRoundUp() {
    assertSingleDouble(compile("2.5.round()"), 3.0);
  }

  @Test
  void testDoubleRoundDown() {
    assertSingleDouble(compile("2.4.round()"), 2.0);
  }

  @Test
  void testStringRoundFails() {
    compileExpectError("\"hello\".round()");
  }

  @Test
  void testSetRoundFails() {
    compileExpectError("Set{1.5}.round()");
  }

  // ══════════════════════════════════════════════════════════════
  // §2b EXTENSION: EFloat unary collection ops — type preservation
  //
  // abs() preserves the singleton type:
  //   ¡Integer!.abs() → ¡Integer!
  //   ¡Float!.abs()   → ¡Float!    ← type preserved
  //   ¡Double!.abs()  → ¡Double!
  //
  // floor() / ceil() / ceiling() / round() always promote:
  //   ¡Integer!.floor() → ¡Integer!
  //   ¡Float!.floor()   → ¡Double!  ← widening (lost precision info)
  //   ¡Double!.floor()  → ¡Double!
  //
  // These tests verify the promotion semantics explicitly.
  // ══════════════════════════════════════════════════════════════

  // ── abs() on ¡Float! grammar literal (parsed as Double) ───────

  @Test
  void testFloatAbsNegativeValue() {
    assertSingleDouble(compile("(-2.5).abs()"), 2.5);
  }

  @Test
  void testFloatAbsAlreadyPositive() {
    assertSingleDouble(compile("(2.5).abs()"), 2.5);
  }

  @Test
  void testFloatAbsZero() {
    assertSingleDouble(compile("(0.0).abs()"), 0.0);
  }

  // ── floor() on ¡Float! → ¡Double! (promotion documented) ──────

  @Test
  void testFloatFloorNegative() {
    assertSingleDouble(compile("(-2.3).floor()"), -3.0);
  }

  @Test
  void testFloatFloorAlreadyWhole() {
    assertSingleDouble(compile("(3.0).floor()"), 3.0);
  }

  // ── ceil() / ceiling() on ¡Float! → ¡Double! ──────────────────

  @Test
  void testFloatCeilNegative() {
    assertSingleDouble(compile("(-2.7).ceil()"), -2.0);
  }

  @Test
  void testFloatCeilingNegative() {
    assertSingleDouble(compile("(-2.7).ceiling()"), -2.0);
  }

  // ── round() on ¡Float! → ¡Double! ─────────────────────────────

  @Test
  void testFloatRoundDown() {
    assertSingleDouble(compile("(2.3).round()"), 2.0);
  }

  @Test
  void testFloatRoundHalfUp() {
    assertSingleDouble(compile("(2.5).round()"), 3.0);
  }

  @Test
  void testFloatRoundNegativeHalf() {
    assertSingleDouble(compile("(-2.5).round()"), -2.0);
  }

  // ── Contrast: Integer unary ops stay ¡Integer! ─────────────────

  @Test
  void testIntegerAbsKeepsIntegerType() {
    // ¡Integer!.abs() → ¡Integer! (no promotion)
    assertSingleInt(compile("(-5).abs()"), 5);
  }

  @Test
  void testIntegerFloorKeepsIntegerType() {
    // ¡Integer!.floor() → ¡Integer! (no promotion needed)
    assertSingleInt(compile("(5).floor()"), 5);
  }
}
