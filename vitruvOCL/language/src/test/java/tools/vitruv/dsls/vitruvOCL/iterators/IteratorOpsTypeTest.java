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
public class IteratorOpsTypeTest extends DummyTestSpecification {

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
  public void testSetSelectFilter() {
    Value r = compile("Set{1, 2, 3, 4}.select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testSetSelectEmpty() {
    Value r = compile("Set{1, 2, 3}.select(x | x > 10)");
    assertEquals(0, r.size());
  }

  @Test
  public void testSetSelectAll() {
    Value r = compile("Set{1, 2, 3}.select(x | x > 0)");
    assertEquals(3, r.size());
  }

  @Test
  public void testSequenceSelectFilter() {
    Value r = compile("Sequence{1, 2, 3, 4}.select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testBagSelectFilter() {
    Value r = compile("Bag{1, 1, 2, 3}.select(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  public void testOrderedSetSelectFilter() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testIntegerSelectOnSingleton() {
    // ¡Integer! select(x | x > 0) → ¿Integer?, 1 > 0 → kept → size=1
    Value r = compile("1.select(x | x > 0)");
    assertEquals(1, r.size());
  }

  @Test
  public void testBooleanSelectOnSingleton() {
    Value r = compile("true.select(x | x)");
    assertEquals(1, r.size());
  }

  @Test
  public void testStringSelectOnSingleton() {
    // "hello" != "h" → filtered out → empty
    Value r = compile("\"hello\".select(x | x == \"h\")");
    assertEquals(0, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // reject(v | pred)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetRejectFilter() {
    Value r = compile("Set{1, 2, 3, 4}.reject(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testSetRejectAll() {
    Value r = compile("Set{1, 2, 3}.reject(x | x > 0)");
    assertEquals(0, r.size());
  }

  @Test
  public void testSequenceRejectFilter() {
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testBagRejectFilter() {
    Value r = compile("Bag{1, 1, 2, 3}.reject(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  public void testOrderedSetRejectFilter() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.reject(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  public void testIntegerRejectOnSingleton() {
    // ¡Integer! reject(x | x > 0) → ¿Integer?, 1 > 0 → rejected → empty
    Value r = compile("1.reject(x | x > 0)");
    assertEquals(0, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // collect(v | expr)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetCollectInt() {
    Value r = compile("Set{1, 2, 3}.collect(x | x * 2)");
    assertEquals(3, r.size());
  }

  @Test
  public void testSetCollectString() {
    Value r = compile("Set{1, 2}.collect(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  public void testSequenceCollectInt() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 2)");
    assertEquals(3, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  public void testBagCollectInt() {
    Value r = compile("Bag{1, 2, 3}.collect(x | x + 10)");
    assertEquals(3, r.size());
  }

  @Test
  public void testOrderedSetCollectInt() {
    Value r = compile("OrderedSet{1, 2, 3}.collect(x | x * 3)");
    assertEquals(3, r.size());
  }

  @Test
  public void testIntegerCollectOnSingleton() {
    // ¡Integer! collect(x | x) → ¡Integer! with same element
    Value r = compile("1.collect(x | x)");
    assertEquals(1, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // forAll(v | pred)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetForAllTrue() {
    assertSingleBool(compile("Set{2, 4, 6}.forAll(x | x > 0)"), true);
  }

  @Test
  public void testSetForAllFalse() {
    assertSingleBool(compile("Set{1, 2, 3}.forAll(x | x > 2)"), false);
  }

  @Test
  public void testSetForAllEmpty() {
    assertSingleBool(compile("Set{}.forAll(x | x > 0)"), true);
  }

  @Test
  public void testSequenceForAllTrue() {
    assertSingleBool(compile("Sequence{1, 2, 3}.forAll(x | x > 0)"), true);
  }

  @Test
  public void testBagForAllTrue() {
    assertSingleBool(compile("Bag{2, 4, 6}.forAll(x | x > 0)"), true);
  }

  @Test
  public void testOrderedSetForAllTrue() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.forAll(x | x > 0)"), true);
  }

  @Test
  public void testIntegerForAllOnSingleton() {
    // ¡Integer! forAll(x | x > 0) → ¡Boolean!, 1 > 0 → true
    assertSingleBool(compile("1.forAll(x | x > 0)"), true);
  }

  // ══════════════════════════════════════════════════════════════
  // exists(v | pred)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetExistsTrue() {
    assertSingleBool(compile("Set{1, 2, 3}.exists(x | x > 2)"), true);
  }

  @Test
  public void testSetExistsFalse() {
    assertSingleBool(compile("Set{1, 2, 3}.exists(x | x > 10)"), false);
  }

  @Test
  public void testSetExistsEmpty() {
    assertSingleBool(compile("Set{}.exists(x | x > 0)"), false);
  }

  @Test
  public void testSequenceExistsTrue() {
    assertSingleBool(compile("Sequence{1, 2, 3}.exists(x | x == 2)"), true);
  }

  @Test
  public void testBagExistsTrue() {
    assertSingleBool(compile("Bag{1, 1, 2}.exists(x | x > 1)"), true);
  }

  @Test
  public void testOrderedSetExistsTrue() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.exists(x | x > 2)"), true);
  }

  @Test
  public void testIntegerExistsOnSingleton() {
    // ¡Integer! exists(x | x > 0) → ¡Boolean!, 1 > 0 → true
    assertSingleBool(compile("1.exists(x | x > 0)"), true);
  }

  // ══════════════════════════════════════════════════════════════
  // one(v | pred) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetOneMatch() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 2)"), true);
  }

  @Test
  public void testSetOneNoMatch() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 10)"), false);
  }

  @Test
  public void testSetOneMultipleMatches() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 1)"), false);
  }

  @Test
  public void testSequenceOneMatch() {
    assertSingleBool(compile("Sequence{1, 2, 3}.one(x | x == 2)"), true);
  }

  @Test
  public void testBagOneMatch() {
    assertSingleBool(compile("Bag{1, 2, 3}.one(x | x > 2)"), true);
  }

  @Test
  public void testOrderedSetOneMatch() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.one(x | x > 2)"), true);
  }

  @Test
  public void testIntegerOneFails() {
    compileExpectError("1.one(x | x > 0)");
  }

  @Test
  public void testStringOneFails() {
    compileExpectError("\"hello\".one(x | x == \"h\")");
  }

  // ══════════════════════════════════════════════════════════════
  // any(v | pred) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetAnyFound() {
    Value r = compile("Set{1, 2, 3}.any(x | x > 2)");
    assertEquals(1, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSequenceAnyFound() {
    Value r = compile("Sequence{1, 2, 3}.any(x | x > 1)");
    assertEquals(1, r.size());
  }

  @Test
  public void testBagAnyFound() {
    Value r = compile("Bag{1, 2, 3}.any(x | x > 0)");
    assertEquals(1, r.size());
  }

  @Test
  public void testOrderedSetAnyFound() {
    Value r = compile("OrderedSet{1, 2, 3}.any(x | x == 2)");
    assertEquals(1, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testIntegerAnyOnSingleton() {
    // ¡Integer! any(x | x > 0) → ¿Integer?, 1 > 0 → present → size=1
    Value r = compile("1.any(x | x > 0)");
    assertEquals(1, r.size());
  }

  @Test
  public void testStringAnyOnSingleton() {
    // "hello" != "h" → pred false → empty result
    Value r = compile("\"hello\".any(x | x == \"h\")");
    assertEquals(0, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // isUnique(v | expr) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIsUniqueTrue() {
    assertSingleBool(compile("Set{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  public void testSequenceIsUniqueTrue() {
    assertSingleBool(compile("Sequence{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  public void testSequenceIsUniqueFalse() {
    assertSingleBool(compile("Sequence{1, 2, 1}.isUnique(x | x)"), false);
  }

  @Test
  public void testBagIsUniqueTrue() {
    assertSingleBool(compile("Bag{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  public void testBagIsUniqueFalse() {
    assertSingleBool(compile("Bag{1, 1, 2}.isUnique(x | x)"), false);
  }

  @Test
  public void testOrderedSetIsUniqueTrue() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.isUnique(x | x)"), true);
  }

  @Test
  public void testIntegerIsUniqueFails() {
    compileExpectError("1.isUnique(x | x)");
  }

  // ══════════════════════════════════════════════════════════════
  // sortedBy(v | expr) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetSortedByInt() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  public void testSequenceSortedByInt() {
    Value r = compile("Sequence{3, 1, 4, 1, 5}.sortedBy(x | x)");
    assertEquals(5, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testBagSortedByInt() {
    Value r = compile("Bag{3, 1, 2}.sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testOrderedSetSortedByInt() {
    Value r = compile("OrderedSet{3, 1, 2}.sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testIntegerSortedByFails() {
    compileExpectError("1.sortedBy(x | x)");
  }

  @Test
  public void testStringSortedByFails() {
    compileExpectError("\"hello\".sortedBy(x | x)");
  }

  // ══════════════════════════════════════════════════════════════
  // collectNested(v | expr) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetCollectNested() {
    Value r = compile("Set{1, 2, 3}.collectNested(x | x * 2)");
    assertEquals(3, r.size());
    // Each element is a NestedCollection wrapping a single value
  }

  @Test
  public void testSequenceCollectNested() {
    Value r = compile("Sequence{1, 2}.collectNested(x | x + 10)");
    assertEquals(2, r.size());
  }

  @Test
  public void testBagCollectNested() {
    Value r = compile("Bag{1, 2}.collectNested(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  public void testOrderedSetCollectNested() {
    Value r = compile("OrderedSet{1, 2, 3}.collectNested(x | x)");
    assertEquals(3, r.size());
  }

  @Test
  public void testIntegerCollectNestedFails() {
    compileExpectError("1.collectNested(x | x)");
  }

  // ══════════════════════════════════════════════════════════════
  // iterate(elem; acc = init | body) ★
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetIterateSum() {
    assertSingleInt(compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x)"), 6);
  }

  @Test
  public void testSetIterateConcat() {
    Value r = compile("Set{\"a\"}.iterate(x; acc : String = \"\" | acc.concat(x))");
    assertEquals(1, r.size());
  }

  @Test
  public void testSequenceIterateProduct() {
    assertSingleInt(compile("Sequence{1, 2, 3, 4}.iterate(x; acc : Integer = 1 | acc * x)"), 24);
  }

  @Test
  public void testBagIterateSum() {
    assertSingleInt(compile("Bag{1, 1, 2}.iterate(x; acc : Integer = 0 | acc + x)"), 4);
  }

  @Test
  public void testOrderedSetIterateSum() {
    assertSingleInt(compile("OrderedSet{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x)"), 6);
  }

  @Test
  public void testIntegerIterateFails() {
    compileExpectError("1.iterate(x; acc : Integer = 0 | acc + x)");
  }

  @Test
  public void testStringIterateFails() {
    compileExpectError("\"hello\".iterate(x; acc : Integer = 0 | acc + 1)");
  }
}
