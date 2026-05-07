/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.iterators;

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
public class CollectionArgOpsTypeTest extends DummyTestSpecification {

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
  public void testSetIncludesIntegerFound() {
    assertSingleBool(compile("Set{1, 2, 3}.includes(2)"), true);
  }

  @Test
  public void testSetIncludesIntegerNotFound() {
    assertSingleBool(compile("Set{1, 2, 3}.includes(5)"), false);
  }

  @Test
  public void testSequenceIncludesInteger() {
    assertSingleBool(compile("Sequence{1, 2, 3}.includes(2)"), true);
  }

  @Test
  public void testBagIncludesInteger() {
    assertSingleBool(compile("Bag{1, 1, 2}.includes(1)"), true);
  }

  @Test
  public void testOrderedSetIncludesInteger() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.includes(3)"), true);
  }

  @Test
  public void testSetIncludesString() {
    assertSingleBool(compile("Set{\"a\", \"b\"}.includes(\"a\")"), true);
  }

  @Test
  public void testSetExcludesIntegerFound() {
    assertSingleBool(compile("Set{1, 2, 3}.excludes(5)"), true);
  }

  @Test
  public void testSetExcludesIntegerNotFound() {
    assertSingleBool(compile("Set{1, 2, 3}.excludes(2)"), false);
  }

  @Test
  public void testIntegerIncludesFails() {
    compileExpectError("1.includes(1)");
  }

  @Test
  public void testBooleanExcludesFails() {
    compileExpectError("true.excludes(false)");
  }

  @Test
  public void testStringIncludesFails() {
    compileExpectError("\"hello\".includes(\"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // includesAll(coll) / excludesAll(coll) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIncludesAllSetTrue() {
    assertSingleBool(compile("Set{1, 2, 3}.includesAll(Set{1, 2})"), true);
  }

  @Test
  public void testSetIncludesAllSetFalse() {
    assertSingleBool(compile("Set{1, 2}.includesAll(Set{1, 2, 3})"), false);
  }

  @Test
  public void testSetIncludesAllEmpty() {
    assertSingleBool(compile("Set{1, 2}.includesAll(Set{})"), true);
  }

  @Test
  public void testSequenceIncludesAllSequence() {
    assertSingleBool(compile("Sequence{1, 2, 3}.includesAll(Sequence{1, 3})"), true);
  }

  @Test
  public void testBagIncludesAllBag() {
    assertSingleBool(compile("Bag{1, 2, 3}.includesAll(Bag{2})"), true);
  }

  @Test
  public void testOrderedSetIncludesAllSet() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.includesAll(Set{1, 2})"), true);
  }

  @Test
  public void testSetExcludesAllSetTrue() {
    assertSingleBool(compile("Set{1, 2}.excludesAll(Set{3, 4})"), true);
  }

  @Test
  public void testSetExcludesAllSetFalse() {
    assertSingleBool(compile("Set{1, 2, 3}.excludesAll(Set{2, 4})"), false);
  }

  @Test
  public void testIntegerIncludesAllFails() {
    compileExpectError("1.includesAll(Set{1})");
  }

  @Test
  public void testSetIncludesAllIntegerFails() {
    compileExpectError("Set{1}.includesAll(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // count(elem) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetCountPresent() {
    assertSingleInt(compile("Set{1, 2, 3}.count(2)"), 1);
  }

  @Test
  public void testSetCountAbsent() {
    assertSingleInt(compile("Set{1, 2, 3}.count(5)"), 0);
  }

  @Test
  public void testBagCountDuplicates() {
    assertSingleInt(compile("Bag{1, 1, 1, 2}.count(1)"), 3);
  }

  @Test
  public void testBagCountAbsent() {
    assertSingleInt(compile("Bag{1, 2}.count(5)"), 0);
  }

  @Test
  public void testSequenceCountDuplicates() {
    assertSingleInt(compile("Sequence{1, 2, 1, 3}.count(1)"), 2);
  }

  @Test
  public void testOrderedSetCount() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.count(2)"), 1);
  }

  @Test
  public void testIntegerCountFails() {
    compileExpectError("1.count(1)");
  }

  @Test
  public void testStringCountFails() {
    compileExpectError("\"hello\".count(\"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // including(elem) / excluding(elem)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIncluding() {
    Value r = compile("Set{1, 2}.including(3)");
    assertEquals(3, r.size());
  }

  @Test
  public void testSetExcluding() {
    Value r = compile("Set{1, 2, 3}.excluding(2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testSequenceIncluding() {
    Value r = compile("Sequence{1, 2}.including(3)");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  public void testSequenceExcluding() {
    Value r = compile("Sequence{1, 2, 3}.excluding(2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testBagIncluding() {
    Value r = compile("Bag{1, 1}.including(1)");
    assertEquals(3, r.size());
  }

  @Test
  public void testOrderedSetIncluding() {
    Value r = compile("OrderedSet{1, 2}.including(3)");
    assertEquals(3, r.size());
  }

  @Test
  public void testIntegerIncludingFails() {
    compileExpectError("1.including(2)");
  }

  @Test
  public void testBooleanExcludingFails() {
    compileExpectError("true.excluding(false)");
  }

  // ══════════════════════════════════════════════════════════════
  // prepend(elem) ★  (ordered collections only)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequencePrepend() {
    Value r = compile("Sequence{2, 3}.prepend(1)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  public void testOrderedSetPrepend() {
    Value r = compile("OrderedSet{2, 3}.prepend(1)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetPrependFails() {
    compileExpectError("Set{1, 2}.prepend(0)");
  }

  @Test
  public void testBagPrependFails() {
    compileExpectError("Bag{1, 2}.prepend(0)");
  }

  @Test
  public void testIntegerPrependFails() {
    compileExpectError("1.prepend(0)");
  }

  // ══════════════════════════════════════════════════════════════
  // insertAt(i, elem) ★ (ordered collections only)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceInsertAtStart() {
    Value r = compile("Sequence{2, 3}.insertAt(1, 1)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceInsertAtMiddle() {
    Value r = compile("Sequence{1, 3}.insertAt(2, 2)");
    assertEquals(3, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  public void testSequenceInsertAtEnd() {
    Value r = compile("Sequence{1, 2}.insertAt(3, 3)");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  public void testOrderedSetInsertAt() {
    Value r = compile("OrderedSet{1, 3}.insertAt(2, 2)");
    assertEquals(3, r.size());
  }

  @Test
  public void testSetInsertAtFails() {
    compileExpectError("Set{1, 2}.insertAt(1, 0)");
  }

  @Test
  public void testBagInsertAtFails() {
    compileExpectError("Bag{1, 2}.insertAt(1, 0)");
  }

  // ══════════════════════════════════════════════════════════════
  // union(coll)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetUnionSet() {
    Value r = compile("Set{1, 2}.union(Set{2, 3})");
    assertEquals(3, r.size()); // Set removes duplicate 2
  }

  @Test
  public void testSetUnionBag() {
    Value r = compile("Set{1, 2}.union(Bag{2, 3})");
    assertTrue(r.size() >= 3);
  }

  @Test
  public void testSequenceUnionSequence() {
    Value r = compile("Sequence{1, 2}.union(Sequence{3, 4})");
    assertEquals(4, r.size());
  }

  @Test
  public void testBagUnionBag() {
    Value r = compile("Bag{1, 1}.union(Bag{1, 2})");
    assertEquals(4, r.size());
  }

  @Test
  public void testOrderedSetUnionSet() {
    Value r = compile("OrderedSet{1, 2}.union(Set{3})");
    assertTrue(r.size() >= 3);
  }

  @Test
  public void testIntegerUnionFails() {
    compileExpectError("1.union(Set{1})");
  }

  @Test
  public void testSetUnionIntegerFails() {
    compileExpectError("Set{1}.union(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // intersection(coll) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIntersectionSetOverlap() {
    Value r = compile("Set{1, 2, 3}.intersection(Set{2, 3, 4})");
    assertEquals(2, r.size());
  }

  @Test
  public void testSetIntersectionSetEmpty() {
    Value r = compile("Set{1, 2}.intersection(Set{3, 4})");
    assertEquals(0, r.size());
  }

  @Test
  public void testSequenceIntersectionSequence() {
    Value r = compile("Sequence{1, 2, 3}.intersection(Sequence{2, 3, 4})");
    assertEquals(2, r.size());
  }

  @Test
  public void testBagIntersectionBag() {
    Value r = compile("Bag{1, 1, 2}.intersection(Bag{1, 2, 2})");
    assertTrue(r.size() >= 1);
  }

  @Test
  public void testOrderedSetIntersectionSet() {
    Value r = compile("OrderedSet{1, 2, 3}.intersection(Set{2, 3})");
    assertEquals(2, r.size());
  }

  @Test
  public void testIntegerIntersectionFails() {
    compileExpectError("1.intersection(Set{1})");
  }

  @Test
  public void testSetIntersectionIntegerFails() {
    compileExpectError("Set{1}.intersection(1)");
  }

  // ══════════════════════════════════════════════════════════════
  // symmetricDifference(coll) ★  (Sets only)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetSymmetricDifferenceSetOverlap() {
    Value r = compile("Set{1, 2, 3}.symmetricDifference(Set{2, 3, 4})");
    assertEquals(2, r.size()); // {1, 4}
  }

  @Test
  public void testSetSymmetricDifferenceSetDisjoint() {
    Value r = compile("Set{1, 2}.symmetricDifference(Set{3, 4})");
    assertEquals(4, r.size());
  }

  @Test
  public void testSetSymmetricDifferenceSetEmpty() {
    Value r = compile("Set{1, 2}.symmetricDifference(Set{1, 2})");
    assertEquals(0, r.size());
  }

  @Test
  public void testOrderedSetSymmetricDifferenceSetFails() {
    // symmetricDifference defined for Sets only (both must be unique)
    compileExpectError("Sequence{1, 2}.symmetricDifference(Sequence{2, 3})");
  }

  @Test
  public void testBagSymmetricDifferenceFails() {
    compileExpectError("Bag{1, 2}.symmetricDifference(Bag{2, 3})");
  }

  @Test
  public void testIntegerSymmetricDifferenceFails() {
    compileExpectError("1.symmetricDifference(Set{1})");
  }

  // ══════════════════════════════════════════════════════════════
  // div(¡Integer!) ★ / mod(¡Integer!) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testIntegerDivBasic() {
    assertSingleInt(compile("7.div(2)"), 3);
  }

  @Test
  public void testIntegerDivTruncates() {
    assertSingleInt(compile("10.div(3)"), 3);
  }

  @Test
  public void testIntegerDivExact() {
    assertSingleInt(compile("6.div(2)"), 3);
  }

  @Test
  public void testIntegerDivNegative() {
    assertSingleInt(compile("(-7).div(2)"), -3);
  }

  @Test
  public void testFloatDivFails() {
    compileExpectError("1.5.div(2)");
  }

  @Test
  public void testDoubleDivFails() {
    compileExpectError("2.5.div(2)");
  }

  @Test
  public void testIntegerDivFloatFails() {
    compileExpectError("7.div(2.0)");
  }

  @Test
  public void testStringDivFails() {
    compileExpectError("\"hello\".div(1)");
  }

  @Test
  public void testSetDivFails() {
    compileExpectError("Set{1}.div(1)");
  }

  @Test
  public void testIntegerModBasic() {
    assertSingleInt(compile("7.mod(3)"), 1);
  }

  @Test
  public void testIntegerModZero() {
    assertSingleInt(compile("6.mod(3)"), 0);
  }

  @Test
  public void testIntegerModNegative() {
    assertSingleInt(compile("(-7).mod(3)"), -1);
  }

  @Test
  public void testFloatModFails() {
    compileExpectError("1.5.mod(2)");
  }

  @Test
  public void testIntegerModFloatFails() {
    compileExpectError("7.mod(2.0)");
  }

  @Test
  public void testStringModFails() {
    compileExpectError("\"hello\".mod(2)");
  }

  @Test
  public void testSetModFails() {
    compileExpectError("Set{1}.mod(1)");
  }
}
