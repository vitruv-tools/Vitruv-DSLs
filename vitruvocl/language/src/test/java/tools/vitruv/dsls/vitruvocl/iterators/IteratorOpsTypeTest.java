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
 * Type Matrix: Iterator Operations
 *
 * <p>All 11 iterator operations (5 existing + 6 new ★) on all 4 collection types. Singleton
 * receivers → ERROR for all iterators.
 *
 * <p>select/reject: body must return ¡Boolean!, result = same collection type collect: body any
 * expr, result = same collection type forAll/exists/one/isUnique: result = ¡Boolean! any: result =
 * ¡T! (one element) sortedBy: result = OrderedSet{T} collectNested: result = same coll kind, no
 * flattening iterate: result = accumulator type
 */
class IteratorOpsTypeTest extends DummyTestSpecification {

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
  // select(v | pred)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetSelectFilter() {
    Value r = compile("Set{1, 2, 3, 4}.select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testSetSelectEmpty() {
    Value r = compile("Set{1, 2, 3}.select(x | x > 10)");
    assertEquals(0, r.size());
  }

  @Test
  void testSetSelectAll() {
    Value r = compile("Set{1, 2, 3}.select(x | x > 0)");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceSelectFilter() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testBagSelectFilter() {
    Value r = compile("Bag{1, 1, 2, 3}.select(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  void testOrderedSetSelectFilter() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testIntegerSelectOnSingleton() {
    compileExpectError("1.select(x | x > 0)");
  }

  @Test
  void testBooleanSelectOnSingleton() {
    compileExpectError("true.select(x | x)");
  }

  @Test
  void testStringSelectOnSingleton() {
    compileExpectError("\"hello\".select(x | x == \"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // reject(v | pred)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetRejectFilter() {
    Value r = compile("Set{1, 2, 3, 4}.reject(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testSetRejectAll() {
    Value r = compile("Set{1, 2, 3}.reject(x | x > 0)");
    assertEquals(0, r.size());
  }

  @Test
  void testSequenceRejectFilter() {
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testBagRejectFilter() {
    Value r = compile("Bag{1, 1, 2, 3}.reject(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  void testOrderedSetRejectFilter() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.reject(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testIntegerRejectOnSingleton() {
    compileExpectError("1.reject(x | x > 0)");
  }

  // ══════════════════════════════════════════════════════════════
  // collect(v | expr)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetCollectInt() {
    Value r = compile("Set{1, 2, 3}.collect(x | x * 2)");
    assertEquals(3, r.size());
  }

  @Test
  void testSetCollectString() {
    Value r = compile("Set{1, 2}.collect(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceCollectInt() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 2)");
    assertEquals(3, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  void testBagCollectInt() {
    Value r = compile("Bag{1, 2, 3}.collect(x | x + 10)");
    assertEquals(3, r.size());
  }

  @Test
  void testOrderedSetCollectInt() {
    Value r = compile("OrderedSet{1, 2, 3}.collect(x | x * 3)");
    assertEquals(3, r.size());
  }

  @Test
  void testIntegerCollectOnSingleton() {
    compileExpectError("1.collect(x | x)");
  }

  // ══════════════════════════════════════════════════════════════
  // forAll(v | pred)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetForAllTrue() {
    assertSingleBool(compile("Set{2, 4, 6}.forAll(x | x > 0)"), true);
  }

  @Test
  void testSetForAllFalse() {
    assertSingleBool(compile("Set{1, 2, 3}.forAll(x | x > 2)"), false);
  }

  @Test
  void testSetForAllEmpty() {
    assertSingleBool(compile("Set{}.forAll(x | x > 0)"), true);
  }

  @Test
  void testSequenceForAllTrue() {
    assertSingleBool(compile("Sequence{1, 2, 3}.forAll(x | x > 0)"), true);
  }

  @Test
  void testBagForAllTrue() {
    assertSingleBool(compile("Bag{2, 4, 6}.forAll(x | x > 0)"), true);
  }

  @Test
  void testOrderedSetForAllTrue() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.forAll(x | x > 0)"), true);
  }

  @Test
  void testIntegerForAllOnSingleton() {
    compileExpectError("1.forAll(x | x > 0)");
  }

  // ══════════════════════════════════════════════════════════════
  // exists(v | pred)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetExistsTrue() {
    assertSingleBool(compile("Set{1, 2, 3}.exists(x | x > 2)"), true);
  }

  @Test
  void testSetExistsFalse() {
    assertSingleBool(compile("Set{1, 2, 3}.exists(x | x > 10)"), false);
  }

  @Test
  void testSetExistsEmpty() {
    assertSingleBool(compile("Set{}.exists(x | x > 0)"), false);
  }

  @Test
  void testSequenceExistsTrue() {
    assertSingleBool(compile("Sequence{1, 2, 3}.exists(x | x == 2)"), true);
  }

  @Test
  void testBagExistsTrue() {
    assertSingleBool(compile("Bag{1, 1, 2}.exists(x | x > 1)"), true);
  }

  @Test
  void testOrderedSetExistsTrue() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.exists(x | x > 2)"), true);
  }

  @Test
  void testIntegerExistsOnSingleton() {
    compileExpectError("1.exists(x | x > 0)");
  }

  // ══════════════════════════════════════════════════════════════
  // one(v | pred) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetOneMatch() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 2)"), true);
  }

  @Test
  void testSetOneNoMatch() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 10)"), false);
  }

  @Test
  void testSetOneMultipleMatches() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 1)"), false);
  }

  @Test
  void testSequenceOneMatch() {
    assertSingleBool(compile("Sequence{1, 2, 3}.one(x | x == 2)"), true);
  }

  @Test
  void testBagOneMatch() {
    assertSingleBool(compile("Bag{1, 2, 3}.one(x | x > 2)"), true);
  }

  @Test
  void testOrderedSetOneMatch() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.one(x | x > 2)"), true);
  }

  @Test
  void testIntegerOneFails() {
    compileExpectError("1.one(x | x > 0)");
  }

  @Test
  void testStringOneFails() {
    compileExpectError("\"hello\".one(x | x == \"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // any(v | pred) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetAnyFound() {
    Value r = compile("Set{1, 2, 3}.any(x | x > 2)");
    assertEquals(1, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceAnyFound() {
    Value r = compile("Sequence{1, 2, 3}.any(x | x > 1)");
    assertEquals(1, r.size());
  }

  @Test
  void testBagAnyFound() {
    Value r = compile("Bag{1, 2, 3}.any(x | x > 0)");
    assertEquals(1, r.size());
  }

  @Test
  void testOrderedSetAnyFound() {
    Value r = compile("OrderedSet{1, 2, 3}.any(x | x == 2)");
    assertEquals(1, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testIntegerAnyOnSingleton() {
    compileExpectError("1.any(x | x > 0)");
  }

  @Test
  void testStringAnyOnSingleton() {
    compileExpectError("\"hello\".any(x | x == \"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // isUnique(v | expr) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIsUniqueTrue() {
    assertSingleBool(compile("Set{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  void testSequenceIsUniqueTrue() {
    assertSingleBool(compile("Sequence{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  void testSequenceIsUniqueFalse() {
    assertSingleBool(compile("Sequence{1, 2, 1}.isUnique(x | x)"), false);
  }

  @Test
  void testBagIsUniqueTrue() {
    assertSingleBool(compile("Bag{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  void testBagIsUniqueFalse() {
    assertSingleBool(compile("Bag{1, 1, 2}.isUnique(x | x)"), false);
  }

  @Test
  void testOrderedSetIsUniqueTrue() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  void testIntegerIsUniqueFails() {
    compileExpectError("1.isUnique(x | x)");
  }

  // ══════════════════════════════════════════════════════════════
  // sortedBy(v | expr) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetSortedByInt() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testSequenceSortedByInt() {
    Value r = compile("Sequence{3, 1, 4, 1, 5}.sortedBy(x | x)");
    assertEquals(5, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testBagSortedByInt() {
    Value r = compile("Bag{3, 1, 2}.sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetSortedByInt() {
    Value r = compile("OrderedSet{3, 1, 2}.sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testIntegerSortedByFails() {
    compileExpectError("1.sortedBy(x | x)");
  }

  @Test
  void testStringSortedByFails() {
    compileExpectError("\"hello\".sortedBy(x | x)");
  }

  // ══════════════════════════════════════════════════════════════
  // collectNested(v | expr) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetCollectNested() {
    Value r = compile("Set{1, 2, 3}.collectNested(x | x * 2)");
    assertEquals(3, r.size());
    // Each element is a NestedCollection wrapping a single value
  }

  @Test
  void testSequenceCollectNested() {
    Value r = compile("Sequence{1, 2}.collectNested(x | x + 10)");
    assertEquals(2, r.size());
  }

  @Test
  void testBagCollectNested() {
    Value r = compile("Bag{1, 2}.collectNested(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  void testOrderedSetCollectNested() {
    Value r = compile("OrderedSet{1, 2, 3}.collectNested(x | x)");
    assertEquals(3, r.size());
  }

  @Test
  void testIntegerCollectNestedFails() {
    compileExpectError("1.collectNested(x | x)");
  }

  // ══════════════════════════════════════════════════════════════
  // iterate(elem; acc = init | body) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIterateSum() {
    assertSingleInt(compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x)"), 6);
  }

  @Test
  void testSetIterateConcat() {
    Value r = compile("Set{\"a\"}.iterate(x; acc : String = \"\" | acc.concat(x))");
    assertEquals(1, r.size());
  }

  @Test
  void testSequenceIterateProduct() {
    assertSingleInt(compile("Sequence{1, 2, 3, 4}.iterate(x; acc : Integer = 1 | acc * x)"), 24);
  }

  @Test
  void testBagIterateSum() {
    assertSingleInt(compile("Bag{1, 1, 2}.iterate(x; acc : Integer = 0 | acc + x)"), 4);
  }

  @Test
  void testOrderedSetIterateSum() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x)"), 6);
  }

  @Test
  void testIntegerIterateFails() {
    compileExpectError("1.iterate(x; acc : Integer = 0 | acc + x)");
  }

  @Test
  void testStringIterateFails() {
    compileExpectError("\"hello\".iterate(x; acc : Integer = 0 | acc + 1)");
  }
}
