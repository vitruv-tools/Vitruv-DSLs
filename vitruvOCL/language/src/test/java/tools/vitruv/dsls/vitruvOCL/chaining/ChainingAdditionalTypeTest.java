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
 * Type Matrix: Operation Chaining — Additional Cases
 *
 * <p>Covers gaps left in ChainingTypeMatrixTest: - reject → first/last (ordered result only) -
 * collect → first/last - asOrderedSet → first (ordered, valid) - asBag → first (unordered, ERROR) -
 * any → arithmetic - iterate → collect (ERROR — accType is scalar) - collectNested → flatten is
 * ERROR (wrong nesting level) - reject → asSet / asSequence - collect → asSet / asSequence (dedup)
 */
public class ChainingAdditionalTypeTest extends DummyTestSpecification {

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

  // ── reject → first/last ───────────────────────────────────────

  @Test
  public void testRejectThenFirstOnSequence() {
    // reject on Sequence → Sequence → first() valid
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 2).first()");
    assertEquals(1, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testRejectThenLastOnSequence() {
    Value r = compile("Sequence{1, 2, 3, 4}.reject(x | x > 2).last()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testRejectThenFirstOnSetFails() {
    // Set is unordered → reject returns Set → first() not allowed
    compileExpectError("Set{1, 2, 3}.reject(x | x > 1).first()");
  }

  @Test
  public void testRejectThenLastOnBagFails() {
    // Bag is unordered → reject returns Bag → last() not allowed
    compileExpectError("Bag{1, 2, 3}.reject(x | x > 1).last()");
  }

  @Test
  public void testRejectThenFirstOnOrderedSet() {
    Value r = compile("OrderedSet{1, 2, 3, 4}.reject(x | x > 2).first()");
    assertEquals(1, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testRejectThenAsSet() {
    Value r = compile("Sequence{1, 2, 2, 3}.reject(x | x > 2).asSet()");
    // reject keeps 1,2,2 → asSet dedup → 1,2
    assertEquals(2, r.size());
  }

  @Test
  public void testRejectThenAsSequence() {
    Value r = compile("Set{1, 2, 3}.reject(x | x > 1).asSequence()");
    assertEquals(1, r.size());
  }

  @Test
  public void testRejectThenSortedBy() {
    Value r = compile("Set{3, 1, 2}.reject(x | x > 2).sortedBy(x | x)");
    assertEquals(2, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ── collect → first/last ──────────────────────────────────────

  @Test
  public void testCollectThenFirstOnSequence() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 10).first()");
    assertEquals(10, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testCollectThenLastOnSequence() {
    Value r = compile("Sequence{1, 2, 3}.collect(x | x * 10).last()");
    assertEquals(30, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testCollectThenFirstOnSetFails() {
    // Set.collect → Set (unordered) → first() not allowed
    compileExpectError("Set{1, 2}.collect(x | x * 2).first()");
  }

  @Test
  public void testCollectThenLastOnBagFails() {
    // Bag.collect → Bag (unordered) → last() not allowed
    compileExpectError("Bag{1, 2}.collect(x | x * 2).last()");
  }

  @Test
  public void testCollectThenFirstOnOrderedSet() {
    Value r = compile("OrderedSet{1, 2, 3}.collect(x | x * 2).first()");
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testCollectThenAsSet() {
    // collect on Sequence{1,2,2} → Sequence{2,4,4} → asSet → {2,4}
    Value r = compile("Sequence{1, 2, 2}.collect(x | x * 2).asSet()");
    assertEquals(2, r.size());
  }

  @Test
  public void testCollectThenAsSequence() {
    Value r = compile("Set{1, 2, 3}.collect(x | x + 1).asSequence()");
    assertEquals(3, r.size());
  }

  // ── asOrderedSet → first/last (ordered → valid) ★ ─────────────

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

  @Test
  public void testAsOrderedSetThenAt() {
    Value r = compile("Set{1, 2, 3}.asOrderedSet().at(1)");
    assertEquals(1, r.size());
  }

  // ── asBag → first/last (unordered → ERROR) ★ ─────────────────

  @Test
  public void testAsBagThenFirstFails() {
    compileExpectError("Set{1, 2, 3}.asBag().first()");
  }

  @Test
  public void testAsBagThenLastFails() {
    compileExpectError("Sequence{1, 2, 3}.asBag().last()");
  }

  @Test
  public void testAsBagThenAtFails() {
    compileExpectError("Set{1, 2}.asBag().at(1)");
  }

  // ── asSet → first/last (unordered → ERROR) ─────────────────────

  @Test
  public void testAsSetThenFirstFails() {
    compileExpectError("Sequence{1, 2, 3}.asSet().first()");
  }

  @Test
  public void testAsSetThenLastFails() {
    compileExpectError("Sequence{1, 2, 3}.asSet().last()");
  }

  // ── asSequence → first/last (ordered → valid) ★ ───────────────

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

  // ── any → arithmetic / comparison ★ ───────────────────────────

  @Test
  public void testAnyThenComparison() {
    assertSingleBool(compile("Set{1, 2, 3}.any(x | x > 2) == 3"), true);
  }

  @Test
  public void testAnyThenArithmetic() {
    assertSingleInt(compile("Set{1, 2, 3}.any(x | x > 2) + 10"), 13);
  }

  @Test
  public void testAnyThenArithmeticNested() {
    assertSingleInt(compile("Set{2, 4, 6}.any(x | x > 3) + Set{1, 2}.any(x | x == 1)"), 5);
  }

  @Test
  public void testAnyThenSelectValid() {
    // any() → ¿T?, select on ¿T? is valid per spec
    Value r = compile("Set{1, 2, 3}.any(x | x > 1).select(x | x > 1)");
    assertEquals(1, r.size()); // the matched element passes the filter
  }

  @Test
  public void testAnyThenSizeValid() {
    // any() → ¿T?, size() on ¿T? is valid per spec
    assertSingleInt(compile("Set{1, 2, 3}.any(x | x > 1).size()"), 1);
  }

  // ── iterate → collect/select/size: valid per spec ★ ───────────
  // iterate() → ¡T!, collection ops on ¡T! are allowed per spec

  @Test
  public void testIterateThenCollectValid() {
    Value r = compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x).collect(x | x)");
    assertEquals(1, r.size());
  }

  @Test
  public void testIterateThenSelectValid() {
    Value r = compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x).select(x | x > 2)");
    assertEquals(1, r.size()); // sum=6 > 2 → kept
  }

  @Test
  public void testIterateThenSizeValid() {
    assertSingleInt(compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x).size()"), 1);
  }

  @Test
  public void testIterateThenArithmetic() {
    // iterate → ¡Integer! → arithmetic valid
    assertSingleInt(compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x) * 2"), 12);
  }

  @Test
  public void testIterateThenComparison() {
    assertSingleBool(compile("Set{1, 2, 3}.iterate(x; acc : Integer = 0 | acc + x) > 5"), true);
  }

  // ── collectNested → size / flatten ★ ─────────────────────────

  @Test
  public void testCollectNestedThenSize() {
    // collectNested produces a collection of nested values — size = num elements
    assertSingleInt(compile("Set{1, 2, 3}.collectNested(x | x * 2).size()"), 3);
  }

  @Test
  public void testCollectNestedThenIsEmpty() {
    assertSingleBool(compile("Set{}.collectNested(x | x).isEmpty()"), true);
  }

  // ── one → logical / comparison ★ ─────────────────────────────

  @Test
  public void testOneThenAndLogical() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 2) and true"), true);
  }

  @Test
  public void testOneThenOrLogical() {
    assertSingleBool(compile("Set{1, 2}.one(x | x > 5) or true"), true);
  }

  @Test
  public void testOneThenEquality() {
    assertSingleBool(compile("Set{1, 2, 3}.one(x | x > 2) == true"), true);
  }

  @Test
  public void testOneThenSelectValid() {
    // one() → ¡Boolean!, select on ¡Boolean! is valid per spec
    // Set{1,2} has 2 elements > 0 → one() returns false → select(x|x) filters out false → empty
    Value r = compile("Set{1, 2}.one(x | x > 0).select(x | x)");
    assertEquals(0, r.size());
  }

  @Test
  public void testOneThenSizeValid() {
    // one() → ¡Boolean!, size() on ¡Boolean! is valid per spec → always 1
    assertSingleInt(compile("Set{1, 2}.one(x | x > 0).size()"), 1);
  }

  // ── isUnique → logical ★ ─────────────────────────────────────

  @Test
  public void testIsUniqueThenAndLogical() {
    assertSingleBool(compile("Sequence{1, 2, 3}.isUnique(x | x) and true"), true);
  }

  @Test
  public void testIsUniqueThenNotLogical() {
    assertSingleBool(compile("not Sequence{1, 1, 2}.isUnique(x | x)"), true);
  }

  @Test
  public void testIsUniqueThenSelectValid() {
    // isUnique() → ¡Boolean!, select on ¡Boolean! is valid per spec
    Value r = compile("Sequence{1, 2}.isUnique(x | x).select(x | x)");
    assertEquals(1, r.size()); // Sequence{1,2} unique → isUnique=true → select(x|x) keeps true
  }

  // ── sortedBy → subSequence (ordered result) ★ ─────────────────

  @Test
  public void testSortedByThenSubSequence() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).subSequence(1, 2)");
    assertEquals(2, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(1)).value());
  }

  @Test
  public void testSortedByThenAt() {
    Value r = compile("Set{3, 1, 2}.sortedBy(x | x).at(1)");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  public void testSortedByThenReverse() {
    Value r = compile("Set{1, 2, 3}.sortedBy(x | x).reverse()");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }
}
