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
 * Type Matrix: let-in expression
 *
 * <pre>
 * Syntax: let x : DeclaredType = initExpr in bodyExpr
 *
 * Rules:
 *   initExpr type must conform to DeclaredType (or be omitted → inferred)
 *   Variable x has DeclaredType inside body
 *   Result type = type of bodyExpr
 *
 * Declared × Init compatibility matrix:
 *   Integer   = Integer    → OK
 *   Integer   = Float      → ERROR (Float does not conform to Integer)
 *   Integer   = Double     → ERROR
 *   Double    = Integer    → OK (Integer conforms to Double)
 *   Double    = Float      → OK (Float conforms to Double)
 *   String    = String     → OK
 *   Boolean   = Boolean    → OK
 *   Set{T}   = Set{T}   → OK
 *   Integer   = String     → ERROR
 *   (no type) = any        → inferred from initializer
 * </pre>
 */
class LetInTypeTest extends DummyTestSpecification {

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
  // No explicit type — inferred from initializer
  // ══════════════════════════════════════════════════════════════

  @Test
  void testLetIntegerInferred() {
    assertSingleInt(compile("let x = 5 in x + 3"), 8);
  }

  @Test
  void testLetFloatInferred() {
    assertSingleDouble(compile("let x = 1.5 in x + 1.5"), 3.0);
  }

  @Test
  void testLetDoubleInferred() {
    assertSingleDouble(compile("let x = 2.5 in x * 2"), 5.0);
  }

  @Test
  void testLetStringInferred() {
    assertSingleString(compile("let s = \"hello\" in s.toUpper()"), "HELLO");
  }

  @Test
  void testLetBooleanInferred() {
    assertSingleBool(compile("let b = true in not b"), false);
  }

  @Test
  void testLetCollectionInferred() {
    Value r = compile("let c = Set{1, 2, 3} in c.size()");
    assertSingleInt(r, 3);
  }

  @Test
  void testLetSequenceInferred() {
    Value r = compile("let s = Sequence{1, 2, 3} in s.first()");
    assertEquals(1, r.size());
    assertEquals(1, ((OCLElement.IntValue) r.getElements().get(0)).value());
  }

  // ══════════════════════════════════════════════════════════════
  // Explicit type: DeclaredType = same type → OK
  // ══════════════════════════════════════════════════════════════

  @Test
  void testLetIntegerDeclaredIntegerInit() {
    assertSingleInt(compile("let x : Integer = 5 in x * 2"), 10);
  }

  @Test
  void testLetStringDeclaredStringInit() {
    assertSingleString(compile("let s : String = \"world\" in s.toUpper()"), "WORLD");
  }

  @Test
  void testLetBooleanDeclaredBooleanInit() {
    assertSingleBool(compile("let b : Boolean = false in not b"), true);
  }

  @Test
  void testLetSetDeclaredSetInit() {
    assertSingleInt(compile("let c : Set(Integer) = Set{1, 2, 3} in c.size()"), 3);
  }

  @Test
  void testLetSequenceDeclaredSequenceInit() {
    assertSingleInt(compile("let s : Sequence(Integer) = Sequence{1, 2} in s.size()"), 2);
  }

  // ══════════════════════════════════════════════════════════════
  // Explicit type: init conforms to declared → OK
  // ══════════════════════════════════════════════════════════════

  @Test
  void testLetDoubleDeclaredIntegerInit() {
    // Integer conforms to Double
    assertSingleDouble(compile("let x : Double = 5 in x + 0.5"), 5.5);
  }

  // ══════════════════════════════════════════════════════════════
  // Multiple let bindings
  // ══════════════════════════════════════════════════════════════

  @Test
  void testLetTwoIntegerBindings() {
    assertSingleInt(compile("let x = 3, y = 4 in x + y"), 7);
  }

  @Test
  void testLetMixedTypeBindings() {
    assertSingleString(compile("let n = 42, s = \"hello\" in s.toUpper()"), "HELLO");
  }

  @Test
  void testLetBindingUsedInCondition() {
    assertSingleInt(compile("let x = 5 in if x > 3 then 1 else 0 endif"), 1);
  }

  @Test
  void testLetBindingUsedInIterator() {
    Value r = compile("let c = Set{1, 2, 3} in c.select(x | x > 1)");
    assertEquals(2, r.size());
  }

  @Test
  void testLetBindingChained() {
    assertSingleInt(compile("let x = 3 in let y = x * 2 in y + 1"), 7);
  }

  // ══════════════════════════════════════════════════════════════
  // Body expression types
  // ══════════════════════════════════════════════════════════════

  @Test
  void testLetBodyReturnsInteger() {
    assertSingleInt(compile("let x = 10 in x - 3"), 7);
  }

  @Test
  void testLetBodyReturnsBoolean() {
    assertSingleBool(compile("let x = 5 in x > 3"), true);
  }

  @Test
  void testLetBodyReturnsString() {
    assertSingleString(compile("let s = \"hello\" in s.concat(\" world\")"), "hello world");
  }

  @Test
  void testLetBodyReturnsCollection() {
    Value r = compile("let c = Set{1, 2, 3} in c.reject(x | x > 1)");
    assertEquals(1, r.size());
  }

  @Test
  void testLetBodyReturnsCollectionSize() {
    assertSingleInt(compile("let c = Bag{1, 1, 2} in c.size()"), 3);
  }

  // ══════════════════════════════════════════════════════════════
  // ERROR: declared type incompatible with initializer
  // ══════════════════════════════════════════════════════════════

  @Test
  void testLetIntegerDeclaredStringInitFails() {
    compileExpectError("let x : Integer = \"hello\" in x");
  }

  @Test
  void testLetStringDeclaredIntegerInitFails() {
    compileExpectError("let s : String = 42 in s");
  }

  @Test
  void testLetBooleanDeclaredIntegerInitFails() {
    compileExpectError("let b : Boolean = 1 in b");
  }

  @Test
  void testLetIntegerDeclaredBooleanInitFails() {
    compileExpectError("let x : Integer = true in x");
  }

  @Test
  void testLetStringDeclaredBooleanInitFails() {
    compileExpectError("let s : String = false in s");
  }

  @Test
  void testLetIntegerDeclaredSetInitFails() {
    compileExpectError("let x : Integer = Set{1, 2} in x");
  }

  @Test
  void testLetSetDeclaredIntegerInitFails() {
    compileExpectError("let c : Set(Integer) = 42 in c");
  }
}
