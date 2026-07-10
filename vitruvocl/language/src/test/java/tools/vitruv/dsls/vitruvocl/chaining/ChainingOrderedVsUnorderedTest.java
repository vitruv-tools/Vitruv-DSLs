/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.chaining;

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
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Chaining: ordered vs unordered receiver kind matters for first/last.
 *
 * <p>select/reject/collect PRESERVE the input collection kind. Therefore: Set.select(...) → Set →
 * first() allowed (non-deterministic) Sequence.select(...) → Sequence → first() OK (ordered)
 * Bag.reject(...) → Bag → last() allowed (non-deterministic) OrderedSet.collect(…) → OrderedSet →
 * first() OK (ordered)
 */
class ChainingOrderedVsUnorderedTest extends DummyTestSpecification {

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
  void testSequenceSelectThenFirst() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 1).first()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceSelectThenLast() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 1).last()");
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetSelectThenFirst() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.select(x | x > 2).first()");
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetSelectThenLast() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.select(x | x > 2).last()");
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetSelectThenFirstFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 1).first()");
  }

  @Test
  void testSetSelectThenLastFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 1).last()");
  }

  @Test
  void testBagSelectThenFirstFails() {
    compileExpectError("Bag{1, 2, 3}.select(x | x > 1).first()");
  }

  @Test
  void testBagSelectThenLastFails() {
    compileExpectError("Bag{1, 2, 3}.select(x | x > 1).last()");
  }

  // ══════════════════════════════════════════════════════════════
  // reject → first/last
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceRejectThenFirst() {
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 3).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceRejectThenLast() {
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 2).last()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetRejectThenFirst() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.reject(x | x > 3).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetRejectThenLast() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.reject(x | x > 2).last()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetRejectThenFirstFails() {
    compileExpectError("Set{1, 2, 3}.reject(x | x > 2).first()");
  }

  @Test
  void testBagRejectThenLastFails() {
    compileExpectError("Bag{1, 2, 3}.reject(x | x > 2).last()");
  }

  // ══════════════════════════════════════════════════════════════
  // collect → first/last
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceCollectThenFirst() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 10).first()");
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceCollectThenLast() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 10).last()");
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetCollectThenFirst() {
    Value r = compile("OrderedSet{1, 2, 3}.collect(x | x * 2).first()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetCollectThenLast() {
    Value r = compile("OrderedSet{1, 2, 3}.collect(x | x * 2).last()");
    assertEquals(6, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetCollectThenFirstFails() {
    compileExpectError("Set{1, 2, 3}.collect(x | x * 2).first()");
  }

  @Test
  void testSetCollectThenLastFails() {
    compileExpectError("Set{1, 2, 3}.collect(x | x * 2).last()");
  }

  @Test
  void testBagCollectThenFirstFails() {
    compileExpectError("Bag{1, 2, 3}.collect(x | x * 2).first()");
  }

  @Test
  void testBagCollectThenLastFails() {
    compileExpectError("Bag{1, 2, 3}.collect(x | x * 2).last()");
  }

  // ══════════════════════════════════════════════════════════════
  // asSet/asBag → first/last (unordered → TYPE ERROR)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testAsSetThenFirstFails() {
    compileExpectError("Sequence{1, 2, 3}.asSet().first()");
  }

  @Test
  void testAsSetThenLastFails() {
    compileExpectError("Sequence{1, 2, 3}.asSet().last()");
  }

  @Test
  void testAsBagThenFirstFails() {
    compileExpectError("Sequence{1, 2, 3}.asBag().first()");
  }

  @Test
  void testAsBagThenLastFails() {
    compileExpectError("Sequence{1, 2, 3}.asBag().last()");
  }

  // ══════════════════════════════════════════════════════════════
  // asSequence/asOrderedSet → first/last (ordered → OK)
  // ══════════════════════════════════════════════════════════════

  @ParameterizedTest
  @MethodSource("asConversionFirstLastExpressions")
  void testAsConversionFirstOrLast(String expr) {
    assertEquals(1, compile(expr).size());
  }

  static Stream<String> asConversionFirstLastExpressions() {
    return Stream.of(
        "Set{1, 2, 3}.asSequence().first()",
        "Set{1, 2, 3}.asSequence().last()",
        "Bag{3, 1, 2}.asOrderedSet().first()",
        "Bag{3, 1, 2}.asOrderedSet().last()");
  }

  // ══════════════════════════════════════════════════════════════
  // sortedBy always returns OrderedSet → first/last always OK
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSortedByOnSetThenFirst() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSortedByOnSetThenLast() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).last()");
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSortedByOnBagThenFirst() {
    Value r = compile("Bag{3, 1, 2}.sortedBy(x | x).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSortedByOnSequenceThenFirst() {
    Value r = compile("Sequence{3, 1, 2}.sortedBy(x | x).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // at(i) — ordered only
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceSelectThenAt() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 1).at(1)");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetSelectThenAtFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 1).at(1)");
  }

  @Test
  void testSortedByThenAt() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).at(2)");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // subSequence — ordered only
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceSelectThenSubSequence() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 0).subSequence(2, 3)");
    assertEquals(2, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetSelectThenSubSequenceFails() {
    compileExpectError("Set{1, 2, 3}.select(x | x > 0).subSequence(1, 2)");
  }

  @Test
  void testSortedByThenSubSequence() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).subSequence(1, 2)");
    assertEquals(2, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }
}
