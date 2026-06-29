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
 * Type Matrix: Collection Literals
 *
 * <pre>
 * Tests collection literal syntax for all 4 collection types:
 *   Set{...}        — unordered, unique
 *   Bag{...}        — unordered, non-unique
 *   Sequence{...}   — ordered, non-unique
 *   OrderedSet{...} — ordered, unique
 *
 * Element type combinations:
 *   Empty literals, ¡Integer! elements, ¡Float! elements, ¡Double! elements,
 *   ¡String! elements, ¡Boolean! elements, mixed-numeric elements,
 *   range literals (Sequence{1..n})
 *
 * Uniqueness semantics:
 *   Set: duplicate integers removed
 *   Bag: duplicate integers kept
 *   OrderedSet: duplicate integers removed, order preserved
 *   Sequence: duplicate integers kept, order preserved
 * </pre>
 */
@SuppressWarnings("java:S125")
class CollectionLiteralTypeTest extends DummyTestSpecification {

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
  // Empty literals — all 4 types
  // ══════════════════════════════════════════════════════════════

  @Test
  void testEmptySetLiteral() {
    Value r = compile("Set{}");
    assertEquals(0, r.size());
  }

  @Test
  void testEmptyBagLiteral() {
    Value r = compile("Bag{}");
    assertEquals(0, r.size());
  }

  @Test
  void testEmptySequenceLiteral() {
    Value r = compile("Sequence{}");
    assertEquals(0, r.size());
  }

  @Test
  void testEmptyOrderedSetLiteral() {
    Value r = compile("OrderedSet{}");
    assertEquals(0, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // ¡Integer! elements
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetIntegerLiteralSingleElement() {
    Value r = compile("Set{42}");
    assertEquals(1, r.size());
    assertEquals(42, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSetIntegerLiteralMultipleElements() {
    Value r = compile("Set{1, 2, 3}");
    assertEquals(3, r.size());
  }

  @Test
  void testSetIntegerLiteralDeduplication() {
    Value r = compile("Set{1, 2, 2, 3}");
    assertEquals(3, r.size());
  }

  @Test
  void testBagIntegerLiteralKeepsDuplicates() {
    Value r = compile("Bag{1, 1, 2}");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceIntegerLiteralPreservesOrder() {
    Value r = compile("Sequence{3, 1, 2}");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(1)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testSequenceIntegerLiteralKeepsDuplicates() {
    Value r = compile("Sequence{1, 2, 1}");
    assertEquals(3, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testOrderedSetIntegerLiteralDeduplication() {
    Value r = compile("OrderedSet{1, 2, 2, 3}");
    assertEquals(3, r.size());
  }

  @Test
  void testOrderedSetIntegerLiteralPreservesOrder() {
    Value r = compile("OrderedSet{3, 1, 2}");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // ¡Float! / ¡Double! elements
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetFloatLiteral() {
    Value r = compile("Set{1.5, 2.5}");
    assertEquals(2, r.size());
  }

  @Test
  void testSetDoubleLiteral() {
    Value r = compile("Set{1.5, 2.5, 3.5}");
    assertEquals(3, r.size());
  }

  @Test
  void testBagDoubleLiteralKeepsDuplicates() {
    Value r = compile("Bag{1.5, 1.5}");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceDoubleLiteralPreservesOrder() {
    Value r = compile("Sequence{3.0, 1.0, 2.0}");
    assertEquals(3, r.size());
    assertSingleDouble(
        new Value(
            java.util.List.of(r.getElements().get(0)),
            tools.vitruv.dsls.vitruvOCL.typechecker.Type.DOUBLE),
        3.0);
  }

  // ══════════════════════════════════════════════════════════════
  // ¡String! elements
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetStringLiteral() {
    Value r = compile("Set{\"a\", \"b\", \"c\"}");
    assertEquals(3, r.size());
  }

  @Test
  void testSetStringLiteralDeduplication() {
    Value r = compile("Set{\"a\", \"b\", \"a\"}");
    assertEquals(2, r.size());
  }

  @Test
  void testBagStringLiteralKeepsDuplicates() {
    Value r = compile("Bag{\"a\", \"a\"}");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceStringLiteralPreservesOrder() {
    Value r = compile("Sequence{\"c\", \"a\", \"b\"}");
    assertEquals(3, r.size());
    assertEquals("c", ((OCLElement.StringValue) r.getElements().get(0)).value());
  }

  @Test
  void testOrderedSetStringLiteral() {
    Value r = compile("OrderedSet{\"a\", \"b\"}");
    assertEquals(2, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // ¡Boolean! elements
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetBooleanLiteral() {
    Value r = compile("Set{true, false}");
    assertEquals(2, r.size());
  }

  @Test
  void testSetBooleanLiteralDeduplication() {
    Value r = compile("Set{true, true}");
    assertEquals(1, r.size());
  }

  @Test
  void testBagBooleanLiteralKeepsDuplicates() {
    Value r = compile("Bag{true, true, false}");
    assertEquals(3, r.size());
  }

  @Test
  void testSequenceBooleanLiteralPreservesOrder() {
    Value r = compile("Sequence{false, true}");
    assertEquals(2, r.size());
    assertEquals(false, ((OCLElement.BoolValue) r.getElements().get(0)).value());
    assertEquals(true, ((OCLElement.BoolValue) r.getElements().get(1)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Mixed numeric elements (type promotion)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetMixedIntegerAndFloat() {
    Value r = compile("Set{1, 1.5}");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceMixedIntegerAndDouble() {
    Value r = compile("Sequence{1, 2.5, 3}");
    assertEquals(3, r.size());
  }

  @Test
  void testBagMixedNumeric() {
    Value r = compile("Bag{1, 1.5, 2.5}");
    assertEquals(3, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // Range literals: Sequence{start..end}
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceRangeAscending() {
    Value r = compile("Sequence{1..5}");
    assertEquals(5, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(4)).value());
  }

  @Test
  void testSequenceRangeSingleElement() {
    Value r = compile("Sequence{3..3}");
    assertEquals(1, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceRangeFromZero() {
    Value r = compile("Sequence{0..4}");
    assertEquals(5, r.size());
    assertEquals(0, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceRangeNegative() {
    Value r = compile("Sequence{(-2)..2}");
    assertEquals(5, r.size());
  }

  @Test
  void testSetRangeLiteral() {
    Value r = compile("Set{1..5}");
    assertEquals(5, r.size());
  }

  @Test
  void testBagRangeLiteral() {
    Value r = compile("Bag{1..3}");
    assertEquals(3, r.size());
  }

  @Test
  void testOrderedSetRangeLiteral() {
    Value r = compile("OrderedSet{1..4}");
    assertEquals(4, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(3)).value());
  }

  @Test
  void testSequenceRangeThenSize() {
    assertSingleInt(compile("Sequence{1..10}.size()"), 10);
  }

  @Test
  void testSequenceRangeThenSum() {
    assertSingleInt(compile("Sequence{1..4}.sum()"), 10);
  }

  @Test
  void testSequenceRangeThenSelect() {
    Value r = compile("Sequence{1..5}.select(x | x > 3)");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceRangeThenForAll() {
    assertSingleBool(compile("Sequence{1..5}.forAll(x | x > 0)"), true);
  }

  // ══════════════════════════════════════════════════════════════
  // Nested collection literals used in operations
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetLiteralInSelect() {
    Value r = compile("Set{1, 2, 3, 4, 5}.select(x | x > 3)");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceLiteralInCollect() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 2)");
    assertEquals(3, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testBagLiteralInForAll() {
    assertSingleBool(compile("Bag{2, 4, 6}.forAll(x | x > 0)"), true);
  }

  @Test
  void testOrderedSetLiteralInExists() {
    assertSingleBool(compile("OrderedSet{1, 2, 3}.exists(x | x == 2)"), true);
  }
}
