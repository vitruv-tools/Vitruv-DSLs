/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package tools.vitruv.dsls.vitruvocl.iterators;

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
 * Type Matrix: Collection Operations with Argument
 *
 * <p>Covers: includes, excludes, count, including, excluding, prepend, includesAll, excludesAll,
 * union, intersection, symmetricDifference, div, mod.
 *
 * <p>includes/excludes/count/including/excluding/prepend: recv = any collection, arg = any
 * singleton → valid
 *
 * <p>includesAll/excludesAll/union/intersection/symmetricDiff: recv = collection, arg = collection
 * → valid
 *
 * <p>div/mod: ¡Integer! × ¡Integer! → ¡Integer! only
 */
@SuppressWarnings("java:S125")
class CollectionArgOpsTypeTest extends DummyTestSpecification {

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
  // includes(elem) / excludes(elem)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIncludesIntegerFound() {
    assertSingleBool(compile("Set{1, 2, 3}.includes(2)"), true);
  }

  @Test
  void testSetIncludesIntegerNotFound() {
    assertSingleBool(compile("Set{1, 2, 3}.includes(5)"), false);
  }

  @Test
  void testSequenceIncludesInteger() {
    assertSingleBool(compile("Sequence{1, 2, 3}.includes(2)"), true);
  }

  @Test
  void testBagIncludesInteger() {
    assertSingleBool(compile("Bag{1, 1, 2}.includes(1)"), true);
  }

  @Test
  void testOrderedSetIncludesInteger() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.includes(3)"), true);
  }

  @Test
  void testSetIncludesString() {
    assertSingleBool(compile("Set{\"a\", \"b\"}.includes(\"a\")"), true);
  }

  @Test
  void testSetExcludesIntegerFound() {
    assertSingleBool(compile("Set{1, 2, 3}.excludes(5)"), true);
  }

  @Test
  void testSetExcludesIntegerNotFound() {
    assertSingleBool(compile("Set{1, 2, 3}.excludes(2)"), false);
  }

  @Test
  void testIntegerIncludesOnSingleton() {
    // ¡Integer! scalar — includes() requires explicit collection → error
    compileExpectError("1.includes(1)");
  }

  @Test
  void testBooleanExcludesOnSingleton() {
    // ¡Boolean! scalar — excludes() requires explicit collection → error
    compileExpectError("true.excludes(false)");
  }

  @Test
  void testStringIncludesOnSingleton() {
    // ¡String! scalar — includes() requires explicit collection → error
    compileExpectError("\"hello\".includes(\"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // includesAll(coll) / excludesAll(coll) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIncludesAllSetTrue() {
    assertSingleBool(compile("Set{1, 2, 3}.includesAll(Set{1, 2})"), true);
  }

  @Test
  void testSetIncludesAllSetFalse() {
    assertSingleBool(compile("Set{1, 2}.includesAll(Set{1, 2, 3})"), false);
  }

  @Test
  void testSetIncludesAllEmpty() {
    assertSingleBool(compile("Set{1, 2}.includesAll(Set{})"), true);
  }

  @Test
  void testSequenceIncludesAllSequence() {
    assertSingleBool(compile("Sequence{1, 2, 3}.includesAll(Sequence{1, 3})"), true);
  }

  @Test
  void testBagIncludesAllBag() {
    assertSingleBool(compile("Bag{1, 2, 3}.includesAll(Bag{2})"), true);
  }

  @Test
  void testOrderedSetIncludesAllSet() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.includesAll(Set{1, 2})"), true);
  }

  @Test
  void testSetExcludesAllSetTrue() {
    assertSingleBool(compile("Set{1, 2}.excludesAll(Set{3, 4})"), true);
  }

  @Test
  void testSetExcludesAllSetFalse() {
    assertSingleBool(compile("Set{1, 2, 3}.excludesAll(Set{2, 4})"), false);
  }

  @Test
  void testIntegerIncludesAllFails() {
    compileExpectError("1.includesAll(Set{1})");
  }

  @Test
  void testSetIncludesAllIntegerFails() {
    compileExpectError("Set{1}.includesAll(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // count(elem) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetCountPresent() {
    assertSingleInt(compile("Set{1, 2, 3}.count(2)"), 1);
  }

  @Test
  void testSetCountAbsent() {
    assertSingleInt(compile("Set{1, 2, 3}.count(5)"), 0);
  }

  @Test
  void testBagCountDuplicates() {
    assertSingleInt(compile("Bag{1, 1, 1, 2}.count(1)"), 3);
  }

  @Test
  void testBagCountAbsent() {
    assertSingleInt(compile("Bag{1, 2}.count(5)"), 0);
  }

  @Test
  void testSequenceCountDuplicates() {
    assertSingleInt(compile("Sequence{1, 2, 1, 3}.count(1)"), 2);
  }

  @Test
  void testOrderedSetCount() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.count(2)"), 1);
  }

  @Test
  void testIntegerCountFails() {
    compileExpectError("1.count(1)");
  }

  @Test
  void testStringCountFails() {
    compileExpectError("\"hello\".count(\"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // including(elem) / excluding(elem)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIncluding() {
    Value r = compile("Set{1, 2}.including(3)");
    assertEquals(3, r.size());
  }

  @Test
  void testSetExcluding() {
    Value r = compile("Set{1, 2, 3}.excluding(2)");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceIncluding() {
    Value r = compile("Sequence{1, 2}.including(3)");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testSequenceExcluding() {
    Value r = compile("Sequence{1, 2, 3}.excluding(2)");
    assertEquals(2, r.size());
  }

  @Test
  void testBagIncluding() {
    Value r = compile("Bag{1, 1}.including(1)");
    assertEquals(3, r.size());
  }

  @Test
  void testOrderedSetIncluding() {
    Value r = compile("OrderedSet{1, 2}.including(3)");
    assertEquals(3, r.size());
  }

  @Test
  void testIntegerIncludingFails() {
    compileExpectError("1.including(2)");
  }

  @Test
  void testBooleanExcludingFails() {
    compileExpectError("true.excluding(false)");
  }

  // ══════════════════════════════════════════════════════════════
  // prepend(elem) ★  (ordered collections only)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequencePrepend() {
    Value r = compile("Sequence{2, 3}.prepend(1)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  void testOrderedSetPrepend() {
    Value r = compile("OrderedSet{2, 3}.prepend(1)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetPrependFails() {
    compileExpectError("Set{1, 2}.prepend(0)");
  }

  @Test
  void testBagPrependFails() {
    compileExpectError("Bag{1, 2}.prepend(0)");
  }

  @Test
  void testIntegerPrependFails() {
    compileExpectError("1.prepend(0)");
  }

  // ══════════════════════════════════════════════════════════════
  // insertAt(i, elem) ★ (ordered collections only)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceInsertAtStart() {
    Value r = compile("Sequence{2, 3}.insertAt(1, 1)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceInsertAtMiddle() {
    Value r = compile("Sequence{1, 3}.insertAt(2, 2)");
    assertEquals(3, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  void testSequenceInsertAtEnd() {
    Value r = compile("Sequence{1, 2}.insertAt(3, 3)");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testOrderedSetInsertAt() {
    Value r = compile("OrderedSet{1, 3}.insertAt(2, 2)");
    assertEquals(3, r.size());
  }

  @Test
  void testSetInsertAtFails() {
    compileExpectError("Set{1, 2}.insertAt(1, 0)");
  }

  @Test
  void testBagInsertAtFails() {
    compileExpectError("Bag{1, 2}.insertAt(1, 0)");
  }

  // ══════════════════════════════════════════════════════════════
  // union(coll)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetUnionSet() {
    Value r = compile("Set{1, 2}.union(Set{2, 3})");
    assertEquals(3, r.size()); // Set removes duplicate 2
  }

  @Test
  void testSetUnionBag() {
    Value r = compile("Set{1, 2}.union(Bag{2, 3})");
    assertTrue(r.size() >= 3);
  }

  @Test
  void testSequenceUnionSequence() {
    Value r = compile("Sequence{1, 2}.union(Sequence{3, 4})");
    assertEquals(4, r.size());
  }

  @Test
  void testBagUnionBag() {
    Value r = compile("Bag{1, 1}.union(Bag{1, 2})");
    assertEquals(4, r.size());
  }

  @Test
  void testOrderedSetUnionSet() {
    Value r = compile("OrderedSet{1, 2}.union(Set{3})");
    assertTrue(r.size() >= 3);
  }

  @Test
  void testIntegerUnionFails() {
    compileExpectError("1.union(Set{1})");
  }

  @Test
  void testSetUnionIntegerFails() {
    compileExpectError("Set{1}.union(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // intersection(coll) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIntersectionSetOverlap() {
    Value r = compile("Set{1, 2, 3}.intersection(Set{2, 3, 4})");
    assertEquals(2, r.size());
  }

  @Test
  void testSetIntersectionSetEmpty() {
    Value r = compile("Set{1, 2}.intersection(Set{3, 4})");
    assertEquals(0, r.size());
  }

  @Test
  void testSequenceIntersectionSequence() {
    Value r = compile("Sequence{1, 2, 3}.intersection(Sequence{2, 3, 4})");
    assertEquals(2, r.size());
  }

  @Test
  void testBagIntersectionBag() {
    Value r = compile("Bag{1, 1, 2}.intersection(Bag{1, 2, 2})");
    assertTrue(r.size() >= 1);
  }

  @Test
  void testOrderedSetIntersectionSet() {
    Value r = compile("OrderedSet{1, 2, 3}.intersection(Set{2, 3})");
    assertEquals(2, r.size());
  }

  @Test
  void testIntegerIntersectionFails() {
    compileExpectError("1.intersection(Set{1})");
  }

  @Test
  void testSetIntersectionIntegerFails() {
    compileExpectError("Set{1}.intersection(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // symmetricDifference(coll) ★  (Sets only)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetSymmetricDifferenceSetOverlap() {
    Value r = compile("Set{1, 2, 3}.symmetricDifference(Set{2, 3, 4})");
    assertEquals(2, r.size()); // {1, 4}
  }

  @Test
  void testSetSymmetricDifferenceSetDisjoint() {
    Value r = compile("Set{1, 2}.symmetricDifference(Set{3, 4})");
    assertEquals(4, r.size());
  }

  @Test
  void testSetSymmetricDifferenceSetEmpty() {
    Value r = compile("Set{1, 2}.symmetricDifference(Set{1, 2})");
    assertEquals(0, r.size());
  }

  @Test
  void testOrderedSetSymmetricDifferenceSetFails() {
    // symmetricDifference defined for Sets only (both must be unique)
    compileExpectError("Sequence{1, 2}.symmetricDifference(Sequence{2, 3})");
  }

  @Test
  void testBagSymmetricDifferenceFails() {
    compileExpectError("Bag{1, 2}.symmetricDifference(Bag{2, 3})");
  }

  @Test
  void testIntegerSymmetricDifferenceFails() {
    compileExpectError("1.symmetricDifference(Set{1})");
  }

  // ══════════════════════════════════════════════════════════════
  // div(¡Integer!) ★ / mod(¡Integer!) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testIntegerDivBasic() {
    assertSingleInt(compile("7.div(2)"), 3);
  }

  @Test
  void testIntegerDivTruncates() {
    assertSingleInt(compile("10.div(3)"), 3);
  }

  @Test
  void testIntegerDivExact() {
    assertSingleInt(compile("6.div(2)"), 3);
  }

  @Test
  void testIntegerDivNegative() {
    assertSingleInt(compile("(-7).div(2)"), -3);
  }

  @Test
  void testFloatDivFails() {
    compileExpectError("1.5.div(2)");
  }

  @Test
  void testDoubleDivFails() {
    compileExpectError("2.5.div(2)");
  }

  @Test
  void testIntegerDivFloatFails() {
    compileExpectError("7.div(2.0)");
  }

  @Test
  void testStringDivFails() {
    compileExpectError("\"hello\".div(1)");
  }

  @Test
  void testSetDivFails() {
    compileExpectError("Set{1}.div(1)");
  }

  @Test
  void testIntegerModBasic() {
    assertSingleInt(compile("7.mod(3)"), 1);
  }

  @Test
  void testIntegerModZero() {
    assertSingleInt(compile("6.mod(3)"), 0);
  }

  @Test
  void testIntegerModNegative() {
    assertSingleInt(compile("(-7).mod(3)"), -1);
  }

  @Test
  void testFloatModFails() {
    compileExpectError("1.5.mod(2)");
  }

  @Test
  void testIntegerModFloatFails() {
    compileExpectError("7.mod(2.0)");
  }

  @Test
  void testStringModFails() {
    compileExpectError("\"hello\".mod(2)");
  }

  @Test
  void testSetModFails() {
    compileExpectError("Set{1}.mod(1)");
  }
}
