/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.strings;

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
 * Type Matrix: String Operations
 *
 * <p>Valid receiver: ¡String! only. All collection types and other singleton types → ERROR.
 *
 * <p>Unary ops (receiver only): toUpper, toLower, size, substring(i,j), at(i), characters,
 * toInteger, toReal, substituteAll(p,r), substituteFirst(p,r)
 *
 * <p>Binary ops (receiver × ¡String! arg): concat, indexOf, equalsIgnoreCase, matches, tokenize
 */
class StringOpsTypeTest extends DummyTestSpecification {

  /**
   * Compiles an OCL expression and asserts that type checking rejects it with an error. Used for
   * ERROR cells in the type matrix.
   */
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
    assertTrue(
        tc.hasErrors() || errors.hasErrors(),
        "Expected type error for: " + input + " but none was reported");
  }

  @Override
  protected ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.prefixedExpCS();
  }

  // ── toUpper() ─────────────────────────────────────────────────

  @Test
  void testStringToUpperLowercase() {
    assertSingleString(compile("\"hello\".toUpper()"), "HELLO");
  }

  @Test
  void testStringToUpperMixed() {
    assertSingleString(compile("\"hElLo\".toUpper()"), "HELLO");
  }

  @Test
  void testStringToUpperAlreadyUpper() {
    assertSingleString(compile("\"HELLO\".toUpper()"), "HELLO");
  }

  @Test
  void testStringToUpperEmpty() {
    assertSingleString(compile("\"\".toUpper()"), "");
  }

  @Test
  void testIntegerToUpperFails() {
    compileExpectError("1.toUpper()");
  }

  @Test
  void testFloatToUpperFails() {
    compileExpectError("1.5.toUpper()");
  }

  @Test
  void testBooleanToUpperFails() {
    compileExpectError("true.toUpper()");
  }

  @Test
  void testSetToUpperFails() {
    compileExpectError("Set{\"a\"}.toUpper()");
  }

  @Test
  void testSequenceToUpperFails() {
    compileExpectError("Sequence{\"a\"}.toUpper()");
  }

  // ── toLower() ─────────────────────────────────────────────────

  @Test
  void testStringToLowerUppercase() {
    assertSingleString(compile("\"HELLO\".toLower()"), "hello");
  }

  @Test
  void testStringToLowerMixed() {
    assertSingleString(compile("\"HeLLo\".toLower()"), "hello");
  }

  @Test
  void testStringToLowerEmpty() {
    assertSingleString(compile("\"\".toLower()"), "");
  }

  @Test
  void testIntegerToLowerFails() {
    compileExpectError("1.toLower()");
  }

  @Test
  void testBooleanToLowerFails() {
    compileExpectError("true.toLower()");
  }

  @Test
  void testSetToLowerFails() {
    compileExpectError("Set{\"a\"}.toLower()");
  }

  // ── size() on scalar — must fail (size() requires a multi-valued collection) ──

  @Test
  void testStringSizeOnSingleton() {
    compileExpectError("\"hello\".size()");
  }

  @Test
  void testIntegerSizeOnSingleton() {
    compileExpectError("1.size()");
  }

  @Test
  void testBooleanSizeOnSingleton() {
    compileExpectError("true.size()");
  }

  // ── substring(i, j) ───────────────────────────────────────────

  @Test
  void testStringSubstringMiddle() {
    assertSingleString(compile("\"hello\".substring(2, 4)"), "ell");
  }

  @Test
  void testStringSubstringFull() {
    assertSingleString(compile("\"hello\".substring(1, 5)"), "hello");
  }

  @Test
  void testStringSubstringSingleChar() {
    assertSingleString(compile("\"hello\".substring(1, 1)"), "h");
  }

  @Test
  void testIntegerSubstringFails() {
    compileExpectError("1.substring(1, 2)");
  }

  @Test
  void testBooleanSubstringFails() {
    compileExpectError("true.substring(1, 2)");
  }

  @Test
  void testSetSubstringFails() {
    compileExpectError("Set{\"a\"}.substring(1, 1)");
  }

  // ── at(i) on String ★ ─────────────────────────────────────────

  @Test
  void testStringAtFirst() {
    assertSingleString(compile("\"hello\".at(1)"), "h");
  }

  @Test
  void testStringAtMiddle() {
    assertSingleString(compile("\"hello\".at(3)"), "l");
  }

  @Test
  void testStringAtLast() {
    assertSingleString(compile("\"hello\".at(5)"), "o");
  }

  @Test
  void testIntegerAtFails() {
    compileExpectError("1.at(1)");
  }

  @Test
  void testBooleanAtFails() {
    compileExpectError("true.at(1)");
  }

  @Test
  void testSetAtStringFails() {
    compileExpectError("Set{\"a\"}.at(1)");
  }

  // ── characters() ★ ────────────────────────────────────────────

  @Test
  void testStringCharactersBasic() {
    Value result = compile("\"abc\".characters()");
    assertEquals(3, result.size());
    assertEquals("a", ((OCLElement.StringValue) result.getElements().get(0)).value());
    assertEquals("b", ((OCLElement.StringValue) result.getElements().get(1)).value());
    assertEquals("c", ((OCLElement.StringValue) result.getElements().get(2)).value());
  }

  @Test
  void testStringCharactersEmpty() {
    Value result = compile("\"\".characters()");
    assertEquals(0, result.size());
  }

  @Test
  void testStringCharactersSingleChar() {
    Value result = compile("\"x\".characters()");
    assertEquals(1, result.size());
    assertEquals("x", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  void testIntegerCharactersFails() {
    compileExpectError("1.characters()");
  }

  @Test
  void testBooleanCharactersFails() {
    compileExpectError("true.characters()");
  }

  @Test
  void testSetCharactersFails() {
    compileExpectError("Set{\"a\"}.characters()");
  }

  // ── toInteger() ★ ─────────────────────────────────────────────

  @Test
  void testStringToIntegerBasic() {
    assertSingleInt(compile("\"42\".toInteger()"), 42);
  }

  @Test
  void testStringToIntegerNegative() {
    assertSingleInt(compile("\"-7\".toInteger()"), -7);
  }

  @Test
  void testIntegerToIntegerFails() {
    compileExpectError("42.toInteger()");
  }

  @Test
  void testBooleanToIntegerFails() {
    compileExpectError("true.toInteger()");
  }

  @Test
  void testSetToIntegerFails() {
    compileExpectError("Set{\"1\"}.toInteger()");
  }

  // ── toReal() ★ ────────────────────────────────────────────────

  @Test
  void testStringToRealBasic() {
    assertSingleDouble(compile("\"3.14\".toReal()"), 3.14);
  }

  @Test
  void testStringToRealInteger() {
    assertSingleDouble(compile("\"42\".toReal()"), 42.0);
  }

  @Test
  void testIntegerToRealFails() {
    compileExpectError("42.toReal()");
  }

  @Test
  void testBooleanToRealFails() {
    compileExpectError("true.toReal()");
  }

  @Test
  void testSetToRealFails() {
    compileExpectError("Set{\"3.14\"}.toReal()");
  }

  // ── substituteAll(p, r) ★ ─────────────────────────────────────

  @Test
  void testStringSubstituteAllBasic() {
    assertSingleString(compile("\"aabbcc\".substituteAll(\"b\", \"x\")"), "aaxxcc");
  }

  @Test
  void testStringSubstituteAllNotFound() {
    assertSingleString(compile("\"hello\".substituteAll(\"z\", \"x\")"), "hello");
  }

  @Test
  void testStringSubstituteAllMultiple() {
    assertSingleString(compile("\"abab\".substituteAll(\"ab\", \"c\")"), "cc");
  }

  @Test
  void testIntegerSubstituteAllFails() {
    compileExpectError("1.substituteAll(\"a\", \"b\")");
  }

  @Test
  void testBooleanSubstituteAllFails() {
    compileExpectError("true.substituteAll(\"t\", \"f\")");
  }

  @Test
  void testSetSubstituteAllFails() {
    compileExpectError("Set{\"a\"}.substituteAll(\"a\", \"b\")");
  }

  // ── substituteFirst(p, r) ★ ───────────────────────────────────

  @Test
  void testStringSubstituteFirstBasic() {
    assertSingleString(compile("\"aabbcc\".substituteFirst(\"b\", \"x\")"), "aaxbcc");
  }

  @Test
  void testStringSubstituteFirstOnlyFirst() {
    assertSingleString(compile("\"aaaa\".substituteFirst(\"a\", \"b\")"), "baaa");
  }

  @Test
  void testIntegerSubstituteFirstFails() {
    compileExpectError("1.substituteFirst(\"a\", \"b\")");
  }

  @Test
  void testSetSubstituteFirstFails() {
    compileExpectError("Set{\"a\"}.substituteFirst(\"a\", \"b\")");
  }

  // ── concat(String) ────────────────────────────────────────────

  @Test
  void testStringConcatString() {
    assertSingleString(compile("\"hello\".concat(\", world\")"), "hello, world");
  }

  @Test
  void testStringConcatEmpty() {
    assertSingleString(compile("\"hello\".concat(\"\")"), "hello");
  }

  @Test
  void testEmptyConcatString() {
    assertSingleString(compile("\"\".concat(\"hello\")"), "hello");
  }

  @Test
  void testIntegerConcatFails() {
    compileExpectError("1.concat(\"hello\")");
  }

  @Test
  void testBooleanConcatFails() {
    compileExpectError("true.concat(\"hello\")");
  }

  @Test
  void testSetConcatFails() {
    compileExpectError("Set{\"a\"}.concat(\"b\")");
  }

  // ── indexOf(String) ───────────────────────────────────────────

  @Test
  void testStringIndexOfFound() {
    assertSingleInt(compile("\"hello\".indexOf(\"ll\")"), 3);
  }

  @Test
  void testStringIndexOfNotFound() {
    assertSingleInt(compile("\"hello\".indexOf(\"xyz\")"), 0);
  }

  @Test
  void testStringIndexOfFirst() {
    assertSingleInt(compile("\"hello\".indexOf(\"h\")"), 1);
  }

  @Test
  void testIntegerIndexOfFails() {
    compileExpectError("1.indexOf(\"1\")");
  }

  @Test
  void testBooleanIndexOfFails() {
    compileExpectError("true.indexOf(\"t\")");
  }

  @Test
  void testSetIndexOfFails() {
    compileExpectError("Set{\"a\"}.indexOf(\"a\")");
  }

  // ── equalsIgnoreCase(String) ──────────────────────────────────

  @Test
  void testStringEqualsIgnoreCaseTrue() {
    assertSingleBool(compile("\"Hello\".equalsIgnoreCase(\"HELLO\")"), true);
  }

  @Test
  void testStringEqualsIgnoreCaseFalse() {
    assertSingleBool(compile("\"Hello\".equalsIgnoreCase(\"World\")"), false);
  }

  @Test
  void testStringEqualsIgnoreCaseSame() {
    assertSingleBool(compile("\"hello\".equalsIgnoreCase(\"hello\")"), true);
  }

  @Test
  void testIntegerEqualsIgnoreCaseFails() {
    compileExpectError("1.equalsIgnoreCase(\"1\")");
  }

  @Test
  void testBooleanEqualsIgnoreCaseFails() {
    compileExpectError("true.equalsIgnoreCase(\"true\")");
  }

  @Test
  void testSetEqualsIgnoreCaseFails() {
    compileExpectError("Set{\"a\"}.equalsIgnoreCase(\"a\")");
  }

  // ── matches(String) ★ ─────────────────────────────────────────

  @Test
  void testStringMatchesTrue() {
    assertSingleBool(compile("\"hello123\".matches(\"[a-z]+[0-9]+\")"), true);
  }

  @Test
  void testStringMatchesFalse() {
    assertSingleBool(compile("\"hello\".matches(\"[0-9]+\")"), false);
  }

  @Test
  void testStringMatchesExact() {
    assertSingleBool(compile("\"abc\".matches(\"abc\")"), true);
  }

  @Test
  void testIntegerMatchesFails() {
    compileExpectError("1.matches(\"[0-9]+\")");
  }

  @Test
  void testBooleanMatchesFails() {
    compileExpectError("true.matches(\"true\")");
  }

  @Test
  void testSetMatchesFails() {
    compileExpectError("Set{\"a\"}.matches(\"a\")");
  }

  // ── tokenize(String) ★ ────────────────────────────────────────

  @Test
  void testStringTokenizeComma() {
    Value result = compile("\"a,b,c\".tokenize(\",\")");
    assertEquals(3, result.size());
    assertEquals("a", ((OCLElement.StringValue) result.getElements().get(0)).value());
    assertEquals("b", ((OCLElement.StringValue) result.getElements().get(1)).value());
    assertEquals("c", ((OCLElement.StringValue) result.getElements().get(2)).value());
  }

  @Test
  void testStringTokenizeNoDelimiter() {
    Value result = compile("\"hello\".tokenize(\"z\")");
    assertEquals(1, result.size());
    assertEquals("hello", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  void testStringTokenizeSpace() {
    Value result = compile("\"a b c\".tokenize(\" \")");
    assertEquals(3, result.size());
  }

  @Test
  void testIntegerTokenizeFails() {
    compileExpectError("1.tokenize(\",\")");
  }

  @Test
  void testBooleanTokenizeFails() {
    compileExpectError("true.tokenize(\",\")");
  }

  @Test
  void testSetTokenizeFails() {
    compileExpectError("Set{\"a,b\"}.tokenize(\",\")");
  }
}
