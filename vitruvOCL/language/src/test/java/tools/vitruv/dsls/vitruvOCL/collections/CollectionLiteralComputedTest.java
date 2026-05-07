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
 * Collection literals with computed element expressions.
 *
 * <p>Collection literal elements can be any valid OCL# expression, not just literal values. Element
 * type is inferred from the expression result.
 *
 * <pre>
 * Set{1+2, 3*4}           → Set{¡Integer!} (elements: 3, 12)
 * Sequence{true and false} → Sequence{¡Boolean!} (elements: false)
 * Set{Set{1,2}, Set{3}}→ Set{Set{T}} (nested — used with flatten)
 * </pre>
 */
public class CollectionLiteralComputedTest extends DummyTestSpecification {

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
  // Set{...} with computed Integer elements
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetComputedArithElements() {
    Value r = compile("Set{1 + 2, 3 * 4}");
    assertEquals(2, r.size());
    assertTrue(r.includes(new OCLElement.IntValue(3)));
    assertTrue(r.includes(new OCLElement.IntValue(12)));
  }

  @Test
  public void testSetComputedArithSingleElement() {
    Value r = compile("Set{5 * 5}");
    assertEquals(1, r.size());
    assertEquals(25, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetComputedArithDedup() {
    // If computed values are equal, Set deduplicates
    Value r = compile("Set{2 + 3, 1 + 4}");
    assertEquals(1, r.size()); // both = 5
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSetComputedMixedLiteralAndArith() {
    Value r = compile("Set{1, 2 + 3, 4}");
    assertEquals(3, r.size()); // 1, 5, 4
  }

  // ══════════════════════════════════════════════════════════════
  // Sequence{...} with computed elements
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSequenceComputedArithPreservesOrder() {
    Value r = compile("Sequence{3 * 2, 1 + 1, 10 - 5}");
    assertEquals(3, r.size());
    assertEquals(6, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  public void testSequenceComputedArithDuplicatesKept() {
    Value r = compile("Sequence{2 + 3, 1 + 4}");
    assertEquals(2, r.size()); // both = 5, but Sequence keeps duplicates
  }

  @Test
  public void testSequenceComputedStringOp() {
    Value r = compile("Sequence{\"hello\".toUpper(), \"world\".toLower()}");
    assertEquals(2, r.size());
    assertEquals("HELLO", ((OCLElement.StringValue) r.getElements().get(0)).value());
    assertEquals("world", ((OCLElement.StringValue) r.getElements().get(1)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Bag{...} with computed elements
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testBagComputedArith() {
    Value r = compile("Bag{1 + 1, 1 + 1, 3}");
    assertEquals(3, r.size()); // Bag keeps duplicates: 2, 2, 3
  }

  @Test
  public void testBagComputedBoolExpr() {
    Value r = compile("Bag{1 > 0, 2 > 3, true}");
    assertEquals(3, r.size());
    assertEquals(true, ((OCLElement.BoolValue) r.getElements().get(0)).value());
    assertEquals(false, ((OCLElement.BoolValue) r.getElements().get(1)).value());
    assertEquals(true, ((OCLElement.BoolValue) r.getElements().get(2)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // OrderedSet{...} with computed elements
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testOrderedSetComputedArith() {
    Value r = compile("OrderedSet{3 - 1, 1 + 1, 5}");
    // 2, 2, 5 → dedup → 2, 5
    assertEquals(2, r.size());
  }

  @Test
  public void testOrderedSetComputedArithPreservesFirstOccurrence() {
    Value r = compile("OrderedSet{5 * 2, 1, 2 + 8}");
    // 10, 1, 10 → dedup → 10, 1
    assertEquals(2, r.size());
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Boolean expression elements
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetComputedBoolExpr() {
    Value r = compile("Set{1 > 0, 2 < 1}");
    assertEquals(2, r.size());
  }

  @Test
  public void testSequenceComputedBoolExpr() {
    Value r = compile("Sequence{true and false, true or false}");
    assertEquals(2, r.size());
    assertEquals(false, ((OCLElement.BoolValue) r.getElements().get(0)).value());
    assertEquals(true, ((OCLElement.BoolValue) r.getElements().get(1)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Nested collection literals (used with flatten)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSetNestedLiteralCreation() {
    // nested Set literal, valid
    Value r = compile("Set{Set{1, 2}, Set{3}}.size()");
    assertSingleInt(r, 2); // 2 inner sets
  }

  @Test
  public void testSequenceNestedLiteralCreation() {
    Value r = compile("Sequence{Sequence{1, 2}, Sequence{3, 4}}.size()");
    assertSingleInt(r, 2);
  }

  @Test
  public void testBagNestedLiteralCreation() {
    Value r = compile("Bag{Bag{1}, Bag{1}}.size()");
    assertSingleInt(r, 2); // Bag: both inner bags kept
  }

  @Test
  public void testOrderedSetNestedLiteralCreation() {
    Value r = compile("OrderedSet{OrderedSet{1, 2}, OrderedSet{3}}.size()");
    assertSingleInt(r, 2);
  }

  @Test
  public void testSetNestedLiteralThenFlatten() {
    Value r = compile("Set{Set{1, 2}, Set{3}}.flatten()");
    assertEquals(3, r.size());
  }

  @Test
  public void testSequenceNestedLiteralThenFlattenThenSum() {
    assertSingleInt(compile("Sequence{Sequence{1, 2}, Sequence{3, 4}}.flatten().sum()"), 10);
  }

  @Test
  public void testNestedLiteralThenFlattenThenSelect() {
    Value r = compile("Sequence{Sequence{1, 2, 3}, Sequence{4, 5}}.flatten().select(x | x > 3)");
    assertEquals(2, r.size());
    assertEquals(4, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(5, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Computed elements in combination with other ops
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testComputedElementsThenSize() {
    assertSingleInt(compile("Set{1+1, 2+2, 3+3}.size()"), 3);
  }

  @Test
  public void testComputedElementsThenSum() {
    assertSingleInt(compile("Set{1+1, 2+2, 3+3}.sum()"), 12); // 2+4+6
  }

  @Test
  public void testComputedElementsThenSelect() {
    Value r = compile("Set{1+1, 2+2, 3+3}.select(x | x > 3)");
    assertEquals(2, r.size()); // 4, 6
  }

  @Test
  public void testComputedElementsThenForAll() {
    assertSingleBool(compile("Set{1+1, 2+2, 3+3}.forAll(x | x > 0)"), true);
  }

  @Test
  public void testLetInComputedCollectionElement() {
    // let x = 5 in Set with computed elements
    Value r = compile("let x = 5 in Set{x, x + 1, x + 2}");
    assertEquals(3, r.size());
    assertTrue(r.includes(new OCLElement.IntValue(5)));
    assertTrue(r.includes(new OCLElement.IntValue(6)));
    assertTrue(r.includes(new OCLElement.IntValue(7)));
  }
}
