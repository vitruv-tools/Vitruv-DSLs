/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.chaining;

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
 * Chaining: ordered vs unordered receiver kind matters for first/last.
 *
 * <p>select/reject/collect PRESERVE the input collection kind. Therefore: Set.select(...) → Set →
 * first() allowed (non-deterministic) Sequence.select(...) → Sequence → first() OK (ordered)
 * Bag.reject(...) → Bag → last() allowed (non-deterministic) OrderedSet.collect(…) → OrderedSet →
 * first() OK (ordered)
 */
public class ChainingOrderedVsUnorderedTest extends DummyTestSpecification {

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
  // select → first/last
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceSelectThenFirst() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 1).first()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceSelectThenLast() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 1).last()");
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetSelectThenFirst() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.select(x | x > 2).first()");
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetSelectThenLast() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.select(x | x > 2).last()");
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetSelectThenFirstFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 1).first()");
  }

  @Test
  public void testSetSelectThenLastFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 1).last()");
  }

  @Test
  public void testBagSelectThenFirstFails() {
    compileExpectError("Bag{1, 2, 3}.select(x | x > 1).first()");
  }

  @Test
  public void testBagSelectThenLastFails() {
    compileExpectError("Bag{1, 2, 3}.select(x | x > 1).last()");
  }

  // ══════════════════════════════════════════════════════════════
  // reject → first/last
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceRejectThenFirst() {
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 3).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceRejectThenLast() {
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 2).last()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetRejectThenFirst() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.reject(x | x > 3).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetRejectThenLast() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.reject(x | x > 2).last()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetRejectThenFirstFails() {
    compileExpectError("Set{1, 2, 3}.reject(x | x > 2).first()");
  }

  @Test
  public void testBagRejectThenLastFails() {
    compileExpectError("Bag{1, 2, 3}.reject(x | x > 2).last()");
  }

  // ══════════════════════════════════════════════════════════════
  // collect → first/last
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceCollectThenFirst() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 10).first()");
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceCollectThenLast() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 10).last()");
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetCollectThenFirst() {
    Value r = compile("OrderedSet{1, 2, 3}.collect(x | x * 2).first()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetCollectThenLast() {
    Value r = compile("OrderedSet{1, 2, 3}.collect(x | x * 2).last()");
    assertEquals(6, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetCollectThenFirstFails() {
    compileExpectError("Set{1, 2, 3}.collect(x | x * 2).first()");
  }

  @Test
  public void testSetCollectThenLastFails() {
    compileExpectError("Set{1, 2, 3}.collect(x | x * 2).last()");
  }

  @Test
  public void testBagCollectThenFirstFails() {
    compileExpectError("Bag{1, 2, 3}.collect(x | x * 2).first()");
  }

  @Test
  public void testBagCollectThenLastFails() {
    compileExpectError("Bag{1, 2, 3}.collect(x | x * 2).last()");
  }

  // ══════════════════════════════════════════════════════════════
  // asSet/asBag → first/last (unordered → TYPE ERROR)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testAsSetThenFirstFails() {
    compileExpectError("Sequence{1, 2, 3}.asSet().first()");
  }

  @Test
  public void testAsSetThenLastFails() {
    compileExpectError("Sequence{1, 2, 3}.asSet().last()");
  }

  @Test
  public void testAsBagThenFirstFails() {
    compileExpectError("Sequence{1, 2, 3}.asBag().first()");
  }

  @Test
  public void testAsBagThenLastFails() {
    compileExpectError("Sequence{1, 2, 3}.asBag().last()");
  }

  // ══════════════════════════════════════════════════════════════
  // asSequence/asOrderedSet → first/last (ordered → OK)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testAsSequenceThenFirst() {
    Value r = compile("Set{1, 2, 3}.asSequence().first()");
    assertEquals(1, r.size());
  }

  @Test
  public void testAsSequenceThenLast() {
    Value r = compile("Set{1, 2, 3}.asSequence().last()");
    assertEquals(1, r.size());
  }

  @Test
  public void testAsOrderedSetThenFirst() {
    Value r = compile("Bag{3, 1, 2}.asOrderedSet().first()");
    assertEquals(1, r.size());
  }

  @Test
  public void testAsOrderedSetThenLast() {
    Value r = compile("Bag{3, 1, 2}.asOrderedSet().last()");
    assertEquals(1, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // sortedBy always returns OrderedSet → first/last always OK
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSortedByOnSetThenFirst() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSortedByOnSetThenLast() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).last()");
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSortedByOnBagThenFirst() {
    Value r = compile("Bag{3, 1, 2}.sortedBy(x | x).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSortedByOnSequenceThenFirst() {
    Value r = compile("Sequence{3, 1, 2}.sortedBy(x | x).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // at(i) — ordered only
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceSelectThenAt() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 1).at(1)");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetSelectThenAtFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 1).at(1)");
  }

  @Test
  public void testSortedByThenAt() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).at(2)");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // subSequence — ordered only
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceSelectThenSubSequence() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 0).subSequence(2, 3)");
    assertEquals(2, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetSelectThenSubSequenceFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 0).subSequence(1, 2)");
  }

  @Test
  public void testSortedByThenSubSequence() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).subSequence(1, 2)");
    assertEquals(2, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }
}