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
 * flatten() — valid cases on nested collection literals.
 *
 * <p>flatten() requires a Collection(Collection(T)) receiver. These tests cover the valid (green)
 * cells that were missing from CollectionNullaryOpsTypeMatrixTest, which only tested ERROR cases
 * (flatten on flat collections).
 *
 * <pre>
 * Valid:
 *   Sequence{Sequence{T}, Sequence{T}}.flatten() → Sequence{T}
 *   Set{Set{T}, Set{T}}.flatten()               → Set{T}
 *   Bag{Bag{T}, Bag{T}}.flatten()               → Bag{T}
 *   OrderedSet{OrderedSet{T}}.flatten()           → OrderedSet{T}
 *   flatten().select/size/sum etc. (chaining)
 *
 * Still ERROR (flat collection, not nested):
 *   Set{1, 2}.flatten()     → ERROR
 *   Sequence{1}.flatten()   → ERROR
 * </pre>
 */
class FlattenValidCasesTest extends DummyTestSpecification {

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
  // Sequence(Sequence(T)).flatten() → Sequence(T)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceOfSequenceFlattenBasic() {
    Value r = compile("Sequence{Sequence{1, 2}, Sequence{3, 4}}.flatten()");
    assertEquals(4, r.size());
  }

  @Test
  void testSequenceOfSequenceFlattenOrder() {
    Value r = compile("Sequence{Sequence{1, 2}, Sequence{3, 4}}.flatten()");
    assertEquals(4, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(3)).value());
  }

  @Test
  void testSequenceOfSequenceFlattenSingle() {
    Value r = compile("Sequence{Sequence{42}}.flatten()");
    assertEquals(1, r.size());
    assertEquals(42, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceOfSequenceFlattenEmpty() {
    Value r = compile("Sequence{Sequence{}, Sequence{1}}.flatten()");
    assertEquals(1, r.size());
  }

  @Test
  void testSequenceOfSequenceFlattenThreeLevelsOnce() {
    // flatten() only flattens one level
    Value r = compile("Sequence{Sequence{1, 2}, Sequence{3}}.flatten()");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceOfSequenceFlattenStrings() {
    Value r = compile("Sequence{Sequence{\"a\", \"b\"}, Sequence{\"c\"}}.flatten()");
    assertEquals(3, r.size());
    assertEquals("a", ((OCLElement.StringValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Set(Set(T)).flatten() → Set(T)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetOfSetFlattenBasic() {
    Value r = compile("Set{Set{1, 2}, Set{3, 4}}.flatten()");
    assertEquals(4, r.size());
  }

  @Test
  void testSetOfSetFlattenDedup() {
    // flattened Set should still deduplicate
    Value r = compile("Set{Set{1, 2}, Set{2, 3}}.flatten()");
    assertEquals(3, r.size()); // 1, 2, 3 (2 appears only once)
  }

  @Test
  void testSetOfSetFlattenEmpty() {
    Value r = compile("Set{Set{}, Set{1}}.flatten()");
    assertEquals(1, r.size());
  }

  @Test
  void testSetOfSetFlattenSingleElement() {
    Value r = compile("Set{Set{99}}.flatten()");
    assertEquals(1, r.size());
    assertEquals(99, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Bag(Bag(T)).flatten() → Bag(T)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testBagOfBagFlattenBasic() {
    Value r = compile("Bag{Bag{1, 1}, Bag{2}}.flatten()");
    assertEquals(3, r.size()); // Bag keeps duplicates
  }

  @Test
  void testBagOfBagFlattenDuplicatesKept() {
    Value r = compile("Bag{Bag{1, 2}, Bag{1, 2}}.flatten()");
    assertEquals(4, r.size()); // Bag: 1,2,1,2
  }

  // ══════════════════════════════════════════════════════════════
  // OrderedSet(OrderedSet(T)).flatten() → OrderedSet(T)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testOrderedSetOfOrderedSetFlattenBasic() {
    Value r = compile("OrderedSet{OrderedSet{1, 2}, OrderedSet{3}}.flatten()");
    assertEquals(3, r.size());
  }

  @Test
  void testOrderedSetOfOrderedSetFlattenOrder() {
    Value r = compile("OrderedSet{OrderedSet{1, 2}, OrderedSet{3, 4}}.flatten()");
    assertEquals(4, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // flatten() chaining (valid result used in further ops)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testFlattenThenSize() {
    assertSingleInt(compile("Sequence{Sequence{1, 2}, Sequence{3}}.flatten().size()"), 3);
  }

  @Test
  void testFlattenThenSelect() {
    Value r = compile("Sequence{Sequence{1, 2}, Sequence{3, 4}}.flatten().select(x | x > 2)");
    assertEquals(2, r.size());
  }

  @Test
  void testFlattenThenForAll() {
    assertSingleBool(
        compile("Sequence{Sequence{1, 2}, Sequence{3}}.flatten().forAll(x | x > 0)"), true);
  }

  @Test
  void testFlattenThenSum() {
    assertSingleInt(compile("Sequence{Sequence{1, 2}, Sequence{3, 4}}.flatten().sum()"), 10);
  }

  @Test
  void testFlattenThenIsEmpty() {
    assertSingleBool(compile("Sequence{Sequence{}, Sequence{}}.flatten().isEmpty()"), true);
  }

  @Test
  void testFlattenThenFirst() {
    Value r = compile("Sequence{Sequence{10, 20}, Sequence{30}}.flatten().first()");
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testFlattenThenCollect() {
    Value r = compile("Sequence{Sequence{1, 2}, Sequence{3}}.flatten().collect(x | x * 2)");
    assertEquals(3, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testFlattenThenSortedBy() {
    Value r = compile("Set{Set{3, 1}, Set{2}}.flatten().sortedBy(x | x)");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // lift() + flatten() roundtrip
  // ══════════════════════════════════════════════════════════════

  @Test
  void testLiftThenFlattenRoundtrip() {
    // lift wraps collection, flatten unwraps it
    Value r = compile("Set{1, 2, 3}.lift().flatten()");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceLiftThenFlattenRoundtrip() {
    Value r = compile("Sequence{1, 2, 3}.lift().flatten()");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Still ERROR: flatten on flat (non-nested) collections
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetFlattenOnFlatFails() {
    compileExpectError("Set{1, 2, 3}.flatten()");
  }

  @Test
  void testSequenceFlattenOnFlatFails() {
    compileExpectError("Sequence{1, 2, 3}.flatten()");
  }

  @Test
  void testBagFlattenOnFlatFails() {
    compileExpectError("Bag{1, 2, 3}.flatten()");
  }

  @Test
  void testOrderedSetFlattenOnFlatFails() {
    compileExpectError("OrderedSet{1, 2, 3}.flatten()");
  }

  @Test
  void testIntegerFlattenFails() {
    compileExpectError("1.flatten()");
  }

  @Test
  void testStringFlattenFails() {
    compileExpectError("\"hello\".flatten()");
  }
}
