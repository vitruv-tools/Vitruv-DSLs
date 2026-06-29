/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Type Matrix: Collection Operations (no argument)
 *
 * <p>Valid receiver: Set{T}, Sequence{T}, Bag{T}, OrderedSet{T}.
 * first()/last()/at()/subSequence()/insertAt()/prepend(): ordered collections only (Sequence and
 * OrderedSet). abs/floor/ceil/ceiling/round: ¡Numeric! singletons only. All singleton receivers for
 * collection ops → ERROR. All collection receivers for scalar ops → ERROR.
 */
public class CollectionNullaryOpsTypeTest extends DummyTestSpecification {

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
  public void testSetIntSize() {
    assertSingleInt(compile("Set{1, 2, 3}.size()"), 3);
  }

  @Test
  public void testEmptySetSize() {
    assertSingleInt(compile("Set{}.size()"), 0);
  }

  @Test
  public void testSequenceIntSize() {
    assertSingleInt(compile("Sequence{1, 2, 3}.size()"), 3);
  }

  @Test
  public void testBagIntSize() {
    assertSingleInt(compile("Bag{1, 1, 2}.size()"), 3);
  }

  @Test
  public void testOrderedSetIntSize() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.size()"), 3);
  }

  @Test
  public void testIntegerSizeOnSingleton() {
    // ¡Integer! scalar — size() requires explicit collection → error
    compileExpectError("1.size()");
  }

  @Test
  public void testFloatSizeOnSingleton() {
    compileExpectError("1.5.size()");
  }

  @Test
  public void testDoubleSizeOnSingleton() {
    compileExpectError("2.5.size()");
  }

  @Test
  public void testBooleanSizeOnSingleton() {
    compileExpectError("true.size()");
  }

  // ══════════════════════════════════════════════════════════════
  // isEmpty() / notEmpty()
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testEmptySetIsEmpty() {
    assertSingleBool(compile("Set{}.isEmpty()"), true);
  }

  @Test
  public void testNonEmptySetIsEmpty() {
    assertSingleBool(compile("Set{1}.isEmpty()"), false);
  }

  @Test
  public void testNonEmptySetNotEmpty() {
    assertSingleBool(compile("Set{1}.notEmpty()"), true);
  }

  @Test
  public void testEmptySetNotEmpty() {
    assertSingleBool(compile("Set{}.notEmpty()"), false);
  }

  @Test
  public void testSequenceIsEmpty() {
    assertSingleBool(compile("Sequence{}.isEmpty()"), true);
  }

  @Test
  public void testSequenceNotEmpty() {
    assertSingleBool(compile("Sequence{1, 2}.notEmpty()"), true);
  }

  @Test
  public void testBagIsEmpty() {
    assertSingleBool(compile("Bag{}.isEmpty()"), true);
  }

  @Test
  public void testOrderedSetIsEmpty() {
    assertSingleBool(compile("OrderedSet{}.isEmpty()"), true);
  }

  @Test
  public void testIntegerIsEmptyOnSingleton() {
    // ¡Integer! scalar — isEmpty() requires explicit collection → error
    compileExpectError("1.isEmpty()");
  }

  @Test
  public void testBooleanIsEmptyOnSingleton() {
    compileExpectError("true.isEmpty()");
  }

  @Test
  public void testStringIsEmptyOnSingleton() {
    compileExpectError("\"hello\".isEmpty()");
  }

  // ══════════════════════════════════════════════════════════════
  // first() / last()  (ordered collections only)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceFirst() {
    Value r = compile("Sequence{10, 20, 30}.first()");
    assertEquals(1, r.size());
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceLast() {
    Value r = compile("Sequence{10, 20, 30}.last()");
    assertEquals(1, r.size());
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetFirst() {
    Value r = compile("OrderedSet{10, 20, 30}.first()");
    assertEquals(1, r.size());
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetLast() {
    Value r = compile("OrderedSet{10, 20, 30}.last()");
    assertEquals(1, r.size());
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetFirstFails() {
    compileExpectError("Set{1, 2}.first()");
  }

  @Test
  public void testSetLastFails() {
    compileExpectError("Set{1, 2}.last()");
  }

  @Test
  public void testBagFirstFails() {
    compileExpectError("Bag{1, 2}.first()");
  }

  @Test
  public void testBagLastFails() {
    compileExpectError("Bag{1, 2}.last()");
  }

  // ══════════════════════════════════════════════════════════════
  // reverse()
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceReverse() {
    Value r = compile("Sequence{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  public void testOrderedSetReverse() {
    Value r = compile("OrderedSet{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetReverse() {
    Value r = compile("Set{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
  }

  @Test
  public void testBagReverse() {
    Value r = compile("Bag{1, 2, 3}.reverse()");
    assertEquals(3, r.size());
  }

  @Test
  public void testIntegerReverseFails() {
    compileExpectError("1.reverse()");
  }

  @Test
  public void testBooleanReverseFails() {
    compileExpectError("true.reverse()");
  }

  // ══════════════════════════════════════════════════════════════
  // sum()
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIntSum() {
    assertSingleInt(compile("Set{1, 2, 3}.sum()"), 6);
  }

  @Test
  public void testSequenceIntSum() {
    assertSingleInt(compile("Sequence{1, 2, 3}.sum()"), 6);
  }

  @Test
  public void testBagIntSum() {
    assertSingleInt(compile("Bag{1, 1, 2}.sum()"), 4);
  }

  @Test
  public void testOrderedSetIntSum() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.sum()"), 6);
  }

  @Test
  public void testSetDoubleSum() {
    assertSingleDouble(compile("Set{1.5, 2.5}.sum()"), 4.0);
  }

  @Test
  public void testEmptySetSum() {
    assertSingleInt(compile("Set{}.sum()"), 0);
  }

  @Test
  public void testIntegerSumFails() {
    compileExpectError("1.sum()");
  }

  @Test
  public void testBooleanSumFails() {
    compileExpectError("true.sum()");
  }

  // ══════════════════════════════════════════════════════════════
  // max() / min()
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIntMax() {
    Value r = compile("Set{1, 5, 3}.max()");
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetIntMin() {
    Value r = compile("Set{1, 5, 3}.min()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceIntMax() {
    Value r = compile("Sequence{3, 1, 4, 1, 5}.max()");
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceIntMin() {
    Value r = compile("Sequence{3, 1, 4, 1, 5}.min()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testBagIntMax() {
    Value r = compile("Bag{2, 7, 3}.max()");
    assertEquals(7, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetIntMax() {
    Value r = compile("OrderedSet{1, 2, 3}.max()");
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetDoubleMax() {
    assertSingleDouble(compile("Set{1.5, 3.5, 2.5}.max()"), 3.5);
  }

  @Test
  public void testSetDoubleMin() {
    assertSingleDouble(compile("Set{1.5, 3.5, 2.5}.min()"), 1.5);
  }

  @Test
  public void testIntegerMaxFails() {
    compileExpectError("1.max()");
  }

  @Test
  public void testBooleanMinFails() {
    compileExpectError("true.min()");
  }

  // ══════════════════════════════════════════════════════════════
  // avg()
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIntAvg() {
    assertSingleDouble(compile("Set{1, 2, 3}.avg()"), 2.0);
  }

  @Test
  public void testSequenceIntAvg() {
    assertSingleDouble(compile("Sequence{1, 2, 3, 4}.avg()"), 2.5);
  }

  @Test
  public void testBagIntAvg() {
    assertSingleDouble(compile("Bag{2, 4}.avg()"), 3.0);
  }

  @Test
  public void testOrderedSetAvg() {
    assertSingleDouble(compile("OrderedSet{1, 2, 3}.avg()"), 2.0);
  }

  @Test
  public void testIntegerAvgFails() {
    compileExpectError("1.avg()");
  }

  // ══════════════════════════════════════════════════════════════
  // asSet() / asBag() / asSequence() / asOrderedSet() ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetAsSet() {
    Value r = compile("Set{1, 2, 3}.asSet()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSequenceAsSetDedup() {
    Value r = compile("Sequence{1, 2, 2, 3}.asSet()");
    assertEquals(3, r.size());
  }

  @Test
  public void testBagAsSetDedup() {
    Value r = compile("Bag{1, 1, 2}.asSet()");
    assertEquals(2, r.size());
  }

  @Test
  public void testOrderedSetAsSet() {
    Value r = compile("OrderedSet{1, 2, 3}.asSet()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSetAsBag() {
    Value r = compile("Set{1, 2, 3}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSequenceAsBag() {
    Value r = compile("Sequence{1, 2, 3}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  public void testBagAsBag() {
    Value r = compile("Bag{1, 1, 2}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  public void testOrderedSetAsBag() {
    Value r = compile("OrderedSet{1, 2, 3}.asBag()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSetAsSequence() {
    Value r = compile("Set{1, 2, 3}.asSequence()");
    assertEquals(3, r.size());
  }

  @Test
  public void testBagAsSequence() {
    Value r = compile("Bag{1, 1, 2}.asSequence()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSequenceAsSequence() {
    Value r = compile("Sequence{1, 2}.asSequence()");
    assertEquals(2, r.size());
  }

  @Test
  public void testOrderedSetAsSequence() {
    Value r = compile("OrderedSet{1, 2, 3}.asSequence()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSetAsOrderedSet() {
    Value r = compile("Set{1, 2, 3}.asOrderedSet()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSequenceAsOrderedSetDedup() {
    Value r = compile("Sequence{1, 2, 2, 3}.asOrderedSet()");
    assertEquals(3, r.size());
  }

  @Test
  public void testBagAsOrderedSetDedup() {
    Value r = compile("Bag{1, 1, 2}.asOrderedSet()");
    assertEquals(2, r.size());
  }

  @Test
  public void testOrderedSetAsOrderedSet() {
    Value r = compile("OrderedSet{1, 2, 3}.asOrderedSet()");
    assertEquals(3, r.size());
  }

  @Test
  public void testIntegerAsSetFails() {
    compileExpectError("1.asSet()");
  }

  @Test
  public void testBooleanAsBagFails() {
    compileExpectError("true.asBag()");
  }

  @Test
  public void testStringAsSequenceFails() {
    compileExpectError("\"hello\".asSequence()");
  }

  @Test
  public void testIntegerAsOrderedSetFails() {
    compileExpectError("1.asOrderedSet()");
  }

  // ══════════════════════════════════════════════════════════════
  // at(i) ★ (ordered collections)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceAtFirst() {
    Value r = compile("Sequence{10, 20, 30}.at(1)");
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceAtMiddle() {
    Value r = compile("Sequence{10, 20, 30}.at(2)");
    assertEquals(20, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceAtLast() {
    Value r = compile("Sequence{10, 20, 30}.at(3)");
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetAt() {
    Value r = compile("OrderedSet{10, 20, 30}.at(2)");
    assertEquals(20, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetAtFails() {
    compileExpectError("Set{1, 2}.at(1)");
  }

  @Test
  public void testBagAtFails() {
    compileExpectError("Bag{1, 2}.at(1)");
  }

  @Test
  public void testIntegerAtFails() {
    compileExpectError("1.at(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // subSequence(i, j) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceSubSequenceMiddle() {
    Value r = compile("Sequence{1, 2, 3, 4}.subSequence(2, 3)");
    assertEquals(2, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  public void testSequenceSubSequenceFull() {
    Value r = compile("Sequence{1, 2, 3}.subSequence(1, 3)");
    assertEquals(3, r.size());
  }

  @Test
  public void testSequenceSubSequenceSingle() {
    Value r = compile("Sequence{1, 2, 3}.subSequence(2, 2)");
    assertEquals(1, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetSubSequence() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.subSequence(1, 2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testSetSubSequenceFails() {
    compileExpectError("Set{1, 2}.subSequence(1, 2)");
  }

  @Test
  public void testBagSubSequenceFails() {
    compileExpectError("Bag{1, 2}.subSequence(1, 2)");
  }

  @Test
  public void testIntegerSubSequenceFails() {
    compileExpectError("1.subSequence(1, 1)");
  }

  // ══════════════════════════════════════════════════════════════
  // abs() / floor() / ceil() / ceiling() / round()
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testIntegerAbsPositive() {
    assertSingleInt(compile("(-3).abs()"), 3);
  }

  @Test
  public void testIntegerAbsNegative() {
    assertSingleInt(compile("(-5).abs()"), 5);
  }

  @Test
  public void testIntegerAbsZero() {
    assertSingleInt(compile("0.abs()"), 0);
  }

  @Test
  public void testFloatAbs() {
    assertSingleDouble(compile("(-1.5).abs()"), 1.5);
  }

  @Test
  public void testDoubleAbs() {
    assertSingleDouble(compile("(-2.5).abs()"), 2.5);
  }

  @Test
  public void testStringAbsFails() {
    compileExpectError("\"hello\".abs()");
  }

  @Test
  public void testBooleanAbsFails() {
    compileExpectError("true.abs()");
  }

  @Test
  public void testSetAbsFails() {
    compileExpectError("Set{1, 2}.abs()");
  }

  @Test
  public void testSequenceAbsFails() {
    compileExpectError("Sequence{1, 2}.abs()");
  }

  @Test
  public void testIntegerFloor() {
    assertSingleInt(compile("3.floor()"), 3);
  }

  @Test
  public void testFloatFloor() {
    assertSingleDouble(compile("2.7.floor()"), 2.0);
  }

  @Test
  public void testDoubleFloor() {
    assertSingleDouble(compile("2.9.floor()"), 2.0);
  }

  @Test
  public void testStringFloorFails() {
    compileExpectError("\"hello\".floor()");
  }

  @Test
  public void testSetFloorFails() {
    compileExpectError("Set{1, 2}.floor()");
  }

  @Test
  public void testIntegerCeil() {
    assertSingleInt(compile("3.ceil()"), 3);
  }

  @Test
  public void testFloatCeil() {
    assertSingleDouble(compile("2.3.ceil()"), 3.0);
  }

  @Test
  public void testDoubleCeil() {
    assertSingleDouble(compile("2.1.ceil()"), 3.0);
  }

  @Test
  public void testStringCeilFails() {
    compileExpectError("\"hello\".ceil()");
  }

  @Test
  public void testSetCeilFails() {
    compileExpectError("Set{1}.ceil()");
  }

  @Test
  public void testIntegerCeiling() {
    assertSingleInt(compile("3.ceiling()"), 3);
  }

  @Test
  public void testFloatCeiling() {
    assertSingleDouble(compile("2.3.ceiling()"), 3.0);
  }

  @Test
  public void testDoubleCeilingAlreadyWhole() {
    assertSingleDouble(compile("3.0.ceiling()"), 3.0);
  }

  @Test
  public void testStringCeilingFails() {
    compileExpectError("\"hello\".ceiling()");
  }

  @Test
  public void testIntegerRound() {
    assertSingleInt(compile("3.round()"), 3);
  }

  @Test
  public void testFloatRoundUp() {
    assertSingleDouble(compile("2.5.round()"), 3.0);
  }

  @Test
  public void testDoubleRoundDown() {
    assertSingleDouble(compile("2.4.round()"), 2.0);
  }

  @Test
  public void testStringRoundFails() {
    compileExpectError("\"hello\".round()");
  }

  @Test
  public void testSetRoundFails() {
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
  public void testFloatAbsNegativeValue() {
    assertSingleDouble(compile("(-2.5).abs()"), 2.5);
  }

  @Test
  public void testFloatAbsAlreadyPositive() {
    assertSingleDouble(compile("(2.5).abs()"), 2.5);
  }

  @Test
  public void testFloatAbsZero() {
    assertSingleDouble(compile("(0.0).abs()"), 0.0);
  }

  // ── floor() on ¡Float! → ¡Double! (promotion documented) ──────

  @Test
  public void testFloatFloorNegative() {
    assertSingleDouble(compile("(-2.3).floor()"), -3.0);
  }

  @Test
  public void testFloatFloorAlreadyWhole() {
    assertSingleDouble(compile("(3.0).floor()"), 3.0);
  }

  // ── ceil() / ceiling() on ¡Float! → ¡Double! ──────────────────

  @Test
  public void testFloatCeilNegative() {
    assertSingleDouble(compile("(-2.7).ceil()"), -2.0);
  }

  @Test
  public void testFloatCeilingNegative() {
    assertSingleDouble(compile("(-2.7).ceiling()"), -2.0);
  }

  // ── round() on ¡Float! → ¡Double! ─────────────────────────────

  @Test
  public void testFloatRoundDown() {
    assertSingleDouble(compile("(2.3).round()"), 2.0);
  }

  @Test
  public void testFloatRoundHalfUp() {
    assertSingleDouble(compile("(2.5).round()"), 3.0);
  }

  @Test
  public void testFloatRoundNegativeHalf() {
    assertSingleDouble(compile("(-2.5).round()"), -2.0);
  }

  // ── Contrast: Integer unary ops stay ¡Integer! ─────────────────

  @Test
  public void testIntegerAbsKeepsIntegerType() {
    // ¡Integer!.abs() → ¡Integer! (no promotion)
    assertSingleInt(compile("(-5).abs()"), 5);
  }

  @Test
  public void testIntegerFloorKeepsIntegerType() {
    // ¡Integer!.floor() → ¡Integer! (no promotion needed)
    assertSingleInt(compile("(5).floor()"), 5);
  }
}
