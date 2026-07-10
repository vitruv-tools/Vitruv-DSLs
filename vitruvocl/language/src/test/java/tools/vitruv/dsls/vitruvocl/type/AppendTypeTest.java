/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.type;

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
 * Type Matrix: append() operation
 *
 * <pre>
 * append(elem): adds elem to end of ordered collection.
 * Differs from including() in that it is defined specifically for
 * ordered collections (Sequence, OrderedSet) in OCL standard.
 * In this implementation it behaves like union/including for all collections.
 *
 * Valid: any collection receiver × any compatible element
 * Invalid: scalar receivers → ERROR
 *
 * recv \ arg  | ¡Integer! | ¡Float! | ¡Double! | ¡String! | ¡Boolean! | Collection
 * Set{T}     |   Set{T} |  Set{T} |   Set{T} |    —    |     —    |   ERROR
 * Sequence{T} |  Seq{T} |  Seq{T} |   Seq{T} |    —    |     —    |   ERROR
 * Bag{T}      |  Bag{T} |  Bag{T} |   Bag{T} |    —    |     —    |   ERROR
 * OrderedSet{T}| OrdSet{T}| OrdSet{T}| OrdSet{T}|   —   |     —    |   ERROR
 * ¡Integer!   |    ERROR  |   ERROR  |   ERROR  |  ERROR  |   ERROR  |   ERROR
 * ¡String!    |    ERROR  |   ERROR  |   ERROR  |  ERROR  |   ERROR  |   ERROR
 * ¡Boolean!   |    ERROR  |   ERROR  |   ERROR  |  ERROR  |   ERROR  |   ERROR
 * </pre>
 */
@SuppressWarnings("java:S125")
class AppendTypeTest extends DummyTestSpecification {

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
  // Set{T}.append(elem) → Set{T}
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetAppendInteger() {
    Value r = compile("Set{1, 2}.append(3)");
    assertEquals(3, r.size());
    assertTrue(r.includes(new OCLElement.IntValue(3)));
  }

  @Test
  void testSetAppendIntegerDuplicate() {
    // Set: duplicate → no change in size
    Value r = compile("Set{1, 2}.append(2)");
    assertEquals(2, r.size());
  }

  @Test
  void testSetAppendFloat() {
    Value r = compile("Set{1}.append(1.5)");
    assertTrue(r.size() >= 1);
  }

  @Test
  void testSetAppendBoolean() {
    Value r = compile("Set{true}.append(false)");
    assertEquals(2, r.size());
  }

  @Test
  void testSetAppendString() {
    Value r = compile("Set{\"a\"}.append(\"b\")");
    assertEquals(2, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // Sequence{T}.append(elem) → Sequence{T}
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceAppendInteger() {
    Value r = compile("Sequence{1, 2}.append(3)");
    assertEquals(3, r.size());
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testSequenceAppendIntegerDuplicate() {
    // Sequence: duplicate allowed
    Value r = compile("Sequence{1, 2}.append(2)");
    assertEquals(3, r.size());
    assertEquals(2, ((OCLElement.IntValue) r.getElements().get(2)).value());
  }

  @Test
  void testSequenceAppendFloat() {
    Value r = compile("Sequence{1}.append(1.5)");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceAppendString() {
    Value r = compile("Sequence{\"a\", \"b\"}.append(\"c\")");
    assertEquals(3, r.size());
    assertEquals("c", ((OCLElement.StringValue) r.getElements().get(2)).value());
  }

  @Test
  void testSequenceAppendBoolean() {
    Value r = compile("Sequence{true}.append(false)");
    assertEquals(2, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // Bag{T}.append(elem) → Bag{T}
  // ══════════════════════════════════════════════════════════════

  @Test
  void testBagAppendInteger() {
    Value r = compile("Bag{1, 1}.append(2)");
    assertEquals(3, r.size());
  }

  @Test
  void testBagAppendIntegerDuplicate() {
    // Bag: always adds, even duplicates
    Value r = compile("Bag{1, 2}.append(1)");
    assertEquals(3, r.size());
  }

  @Test
  void testBagAppendString() {
    Value r = compile("Bag{\"a\"}.append(\"a\")");
    assertEquals(2, r.size());
  }

  // ══════════════════════════════════════════════════════════════
  // OrderedSet{T}.append(elem) → OrderedSet{T}
  // ══════════════════════════════════════════════════════════════

  @Test
  void testOrderedSetAppendInteger() {
    Value r = compile("OrderedSet{1, 2}.append(3)");
    assertEquals(3, r.size());
  }

  @Test
  void testOrderedSetAppendIntegerDuplicate() {
    // OrderedSet: no duplicates
    Value r = compile("OrderedSet{1, 2}.append(2)");
    assertEquals(2, r.size());
  }

  @Test
  void testOrderedSetAppendFloat() {
    Value r = compile("OrderedSet{1}.append(1.5)");
    assertTrue(r.size() >= 1);
  }

  // ══════════════════════════════════════════════════════════════
  // ERROR: scalar receivers
  // ══════════════════════════════════════════════════════════════

  @Test
  void testIntegerAppendFails() {
    compileExpectError("1.append(2)");
  }

  @Test
  void testFloatAppendFails() {
    compileExpectError("1.5.append(2)");
  }

  @Test
  void testDoubleAppendFails() {
    compileExpectError("2.5.append(1)");
  }

  @Test
  void testStringAppendFails() {
    compileExpectError("\"hello\".append(\"world\")");
  }

  @Test
  void testBooleanAppendFails() {
    compileExpectError("true.append(false)");
  }

  // ══════════════════════════════════════════════════════════════
  // ERROR: collection as argument (append takes a single element)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSetAppendSetFails() {
    compileExpectError("Set{1}.append(Set{2})");
  }

  @Test
  void testSequenceAppendSequenceFails() {
    compileExpectError("Sequence{1}.append(Sequence{2})");
  }

  @Test
  void testBagAppendBagFails() {
    compileExpectError("Bag{1}.append(Bag{2})");
  }

  @Test
  void testOrderedSetAppendSetFails() {
    compileExpectError("OrderedSet{1}.append(Set{2})");
  }

  // ══════════════════════════════════════════════════════════════
  // append chaining
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSequenceAppendThenSize() {
    assertSingleInt(compile("Sequence{1, 2}.append(3).size()"), 3);
  }

  @Test
  void testSequenceAppendThenFirst() {
    Value r = compile("Sequence{1, 2}.append(3).first()");
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceAppendThenLast() {
    Value r = compile("Sequence{1, 2}.append(3).last()");
    assertEquals(3, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  @Test
  void testSequenceAppendThenSelect() {
    Value r = compile("Sequence{1, 2}.append(3).select(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  void testSequenceAppendThenForAll() {
    assertSingleBool(compile("Sequence{1, 2}.append(3).forAll(x | x > 0)"), true);
  }

  @Test
  void testSequenceMultipleAppend() {
    Value r = compile("Sequence{}.append(1).append(2).append(3)");
    assertEquals(3, r.size());
  }
}
