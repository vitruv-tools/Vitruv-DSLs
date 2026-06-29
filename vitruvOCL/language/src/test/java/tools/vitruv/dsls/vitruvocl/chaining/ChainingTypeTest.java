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
 * Type Matrix: Operation Chaining
 *
 * <p>Tests that the result of operation A can (or cannot) be used as receiver for operation B.
 *
 * <p>Collection results (select/reject/collect/sortedBy/asSet/asSeq/etc.) → can chain into any
 * collection op.
 *
 * <p>Scalar results (forAll/exists/one/isUnique → ¡Boolean!, size → ¡Integer!, any/first/last →
 * ¡T!) → can only chain into scalar ops (== != < > arithmetic and/or).
 */
class ChainingTypeTest extends DummyTestSpecification {

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

  // ── select → collection ops ───────────────────────────────────

  @Test
  void testSelectThenSelect() {
    Value r = compile("Set{1, 2, 3, 4}.select(x | x > 1).select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testSelectThenReject() {
    Value r = compile("Set{1, 2, 3, 4}.select(x | x > 1).reject(x | x > 3)");
    assertEquals(2, r.size());
  }

  @Test
  void testSelectThenCollect() {
    Value r = compile("Set{1, 2, 3}.select(x | x > 1).collect(x | x * 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testSelectThenForAll() {
    assertSingleBool(compile("Set{2, 3, 4}.select(x | x > 1).forAll(x | x > 0)"), true);
  }

  @Test
  void testSelectThenExists() {
    assertSingleBool(compile("Set{1, 2, 3}.select(x | x > 2).exists(x | x == 3)"), true);
  }

  @Test
  void testSelectThenOne() {
    assertSingleBool(compile("Set{1, 2, 3}.select(x | x > 2).one(x | x == 3)"), true);
  }

  @Test
  void testSelectThenSize() {
    assertSingleInt(compile("Set{1, 2, 3, 4}.select(x | x > 2).size()"), 2);
  }

  @Test
  void testSelectThenIsEmpty() {
    assertSingleBool(compile("Set{1, 2}.select(x | x > 10).isEmpty()"), true);
  }

  @Test
  void testSelectThenFirst() {
    Value r = compile("Sequence{1, 2, 3}.select(x | x > 1).first()");
    assertEquals(1, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSelectThenSortedBy() {
    Value r = compile("Set{3, 1, 2}.select(x | x > 0).sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSelectThenAsSet() {
    Value r = compile("Sequence{1, 2, 2}.select(x | x > 0).asSet()");
    assertEquals(2, r.size());
  }

  @Test
  void testSelectThenAsSequence() {
    Value r = compile("Set{1, 2, 3}.select(x | x > 1).asSequence()");
    assertEquals(2, r.size());
  }

  // ── collect → collection ops ──────────────────────────────────

  @Test
  void testCollectThenSelect() {
    Value r = compile("Set{1, 2, 3}.collect(x | x * 2).select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testCollectThenSize() {
    assertSingleInt(compile("Set{1, 2, 3}.collect(x | x * 2).size()"), 3);
  }

  @Test
  void testCollectThenForAll() {
    assertSingleBool(compile("Set{1, 2, 3}.collect(x | x * 2).forAll(x | x > 0)"), true);
  }

  // ── sortedBy → collection ops ★ ───────────────────────────────

  @Test
  void testSortedByThenSelect() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).select(x | x > 1)");
    assertEquals(2, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSortedByThenCollect() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).collect(x | x * 10)");
    assertEquals(3, r.size());
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSortedByThenSize() {
    assertSingleInt(compile("Set{3, 1, 2}.sortedBy(x | x).size()"), 3);
  }

  @Test
  void testSortedByThenFirst() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSortedByThenSortedBy() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSortedByThenAsSet() {
    Value r = compile("Sequence{1, 2, 2}.sortedBy(x | x).asSet()");
    assertEquals(2, r.size());
  }

  // ── asSet/asSequence/asBag/asOrderedSet → collection ops ★ ────

  @Test
  void testAsSetThenSelect() {
    Value r = compile("Sequence{1, 2, 2}.asSet().select(x | x > 1)");
    assertEquals(1, r.size());
  }

  @Test
  void testAsSetThenSize() {
    assertSingleInt(compile("Sequence{1, 2, 2}.asSet().size()"), 2);
  }

  @Test
  void testAsSetThenForAll() {
    assertSingleBool(compile("Sequence{1, 2, 2}.asSet().forAll(x | x > 0)"), true);
  }

  @Test
  void testAsSetThenIsEmpty() {
    assertSingleBool(compile("Sequence{}.asSet().isEmpty()"), true);
  }

  @Test
  void testAsSequenceThenFirst() {
    Value r = compile("Set{1, 2, 3}.asSequence().first()");
    assertEquals(1, r.size());
  }

  @Test
  void testAsSequenceThenSize() {
    assertSingleInt(compile("Set{1, 2, 3}.asSequence().size()"), 3);
  }

  @Test
  void testAsOrderedSetThenFirst() {
    Value r = compile("Bag{3, 1, 2}.asOrderedSet().first()");
    assertEquals(1, r.size());
  }

  @Test
  void testAsBagThenSize() {
    assertSingleInt(compile("Set{1, 2, 3}.asBag().size()"), 3);
  }

  // ── Boolean results → logical ops ─────────────────────────────

  @Test
  void testForAllResultInLogical() {
    assertSingleBool(
        compile("Set{2, 4, 6}.forAll(x | x > 0) and Set{1, 2}.exists(x | x > 1)"), true);
  }

  @Test
  void testExistsResultInLogical() {
    assertSingleBool(
        compile("Set{1, 2, 3}.exists(x | x > 10) or Set{1, 2}.forAll(x | x > 0)"), true);
  }

  @Test
  void testOneResultInLogical() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 2) and true"), true);
  }

  @Test
  void testIsUniqueResultInLogical() {
    assertSingleBool(compile("Sequence{1, 2, 3}.isUnique(x | x) or false"), true);
  }

  @Test
  void testIsEmptyResultInLogical() {
    assertSingleBool(compile("Set{}.isEmpty() and true"), true);
  }

  // ── Integer results → arithmetic / comparison ─────────────────

  @Test
  void testSizeThenArithmetic() {
    assertSingleInt(compile("Set{1, 2, 3}.size() + 10"), 13);
  }

  @Test
  void testSizeThenComparison() {
    assertSingleBool(compile("Set{1, 2, 3}.size() > 2"), true);
  }

  @Test
  void testSizeThenEquality() {
    assertSingleBool(compile("Set{1, 2, 3}.size() == 3"), true);
  }

  // ── ¡T! results (first/last/any) → comparison ────────────────

  @Test
  void testFirstThenComparison() {
    assertSingleBool(compile("Sequence{10, 20, 30}.first() == 10"), true);
  }

  @Test
  void testLastThenComparison() {
    assertSingleBool(compile("Sequence{10, 20, 30}.last() == 30"), true);
  }

  @Test
  void testFirstThenArithmetic() {
    assertSingleInt(compile("Sequence{5, 10, 15}.first() + 5"), 10);
  }

  @Test
  void testLastThenArithmetic() {
    assertSingleInt(compile("Sequence{5, 10, 15}.last() * 2"), 30);
  }

  @Test
  void testAnyThenComparison() {
    assertSingleBool(compile("Set{1, 2, 3}.any(x | x > 2) == 3"), true);
  }

  // ── ¡Boolean!/¡Integer! → collection ops: valid per spec ─────────
  // Per OCL# spec: collection ops (select, collect, size, etc.) are allowed on ALL types
  // including ¡T! singletons. Boolean results from forAll/exists/one become ¡Boolean!.

  @Test
  void testForAllThenSelectValid() {
    // forAll() → ¡Boolean! (scalar), select requires explicit collection → error
    compileExpectError("Set{1, 2}.forAll(x | x > 0).select(x | x)");
  }

  @Test
  void testExistsThenCollectValid() {
    // exists() → ¡Boolean! (scalar), collect requires explicit collection → error
    compileExpectError("Set{1, 2}.exists(x | x > 0).collect(x | x)");
  }

  @Test
  void testSizeThenSelectValid() {
    // size() → ¡Integer! (scalar), select requires explicit collection → error
    compileExpectError("Set{1, 2}.size().select(x | x > 0)");
  }

  @Test
  void testIsEmptyThenSelectValid() {
    // isEmpty() → ¡Boolean! (scalar), select requires explicit collection → error
    compileExpectError("Set{}.isEmpty().select(x | x)");
  }

  @Test
  void testOneThenSelectValid() {
    // one() → ¡Boolean! (scalar), select requires explicit collection → error
    compileExpectError("Set{1, 2}.one(x | x > 0).select(x | x)");
  }

  // ── characters/tokenize → collection ops ★ ───────────────────

  @Test
  void testCharactersThenSelect() {
    Value r = compile("\"hello\".characters().select(x | x == \"l\")");
    assertEquals(2, r.size());
  }

  @Test
  void testCharactersThenSize() {
    assertSingleInt(compile("\"hello\".characters().size()"), 5);
  }

  @Test
  void testCharactersThenForAll() {
    assertSingleBool(compile("\"aaa\".characters().forAll(x | x == \"a\")"), true);
  }

  @Test
  void testCharactersThenSortedBy() {
    Value r = compile("\"cba\".characters().sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals("a", ((OCLElement.StringValue) r.getElements().get(0)).value());
  }

  @Test
  void testCharactersThenFirst() {
    Value r = compile("\"hello\".characters().first()");
    assertEquals("h", ((OCLElement.StringValue) r.getElements().get(0)).value());
  }

  @Test
  void testTokenizeThenSelect() {
    Value r = compile("\"a,bb,ccc\".tokenize(\",\").select(x | x.length() > 1)");
    assertEquals(2, r.size());
  }

  @Test
  void testTokenizeThenSize() {
    assertSingleInt(compile("\"a,b,c\".tokenize(\",\").size()"), 3);
  }

  @Test
  void testTokenizeThenSortedBy() {
    Value r = compile("\"c,a,b\".tokenize(\",\").sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals("a", ((OCLElement.StringValue) r.getElements().get(0)).value());
  }

  // ── intersection → collection ops ★ ──────────────────────────

  @Test
  void testIntersectionThenSelect() {
    Value r = compile("Set{1, 2, 3}.intersection(Set{2, 3, 4}).select(x | x > 2)");
    assertEquals(1, r.size());
  }

  @Test
  void testIntersectionThenSize() {
    assertSingleInt(compile("Set{1, 2, 3}.intersection(Set{2, 3, 4}).size()"), 2);
  }

  // ── iterate result → arithmetic ★ ─────────────────────────────

  @Test
  void testIterateResultArithmetic() {
    assertSingleInt(compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x) + 10"), 16);
  }

  @Test
  void testIterateResultComparison() {
    assertSingleBool(compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x) == 6"), true);
  }
}
