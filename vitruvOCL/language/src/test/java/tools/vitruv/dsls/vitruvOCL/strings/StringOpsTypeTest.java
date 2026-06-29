/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.strings;

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
 * Type Matrix: String Operations
 *
 * <p>Valid receiver: ¡String! only. All collection types and other singleton types → ERROR.
 *
 * <p>Unary ops (receiver only): toUpper, toLower, size, substring(i,j), at(i), characters,
 * toInteger, toReal, substituteAll(p,r), substituteFirst(p,r)
 *
 * <p>Binary ops (receiver × ¡String! arg): concat, indexOf, equalsIgnoreCase, matches, tokenize
 */
public class StringOpsTypeTest extends DummyTestSpecification {

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
  public void testStringToUpperLowercase() {
    assertSingleString(compile("\"hello\".toUpper()"), "HELLO");
  }

  @Test
  public void testStringToUpperMixed() {
    assertSingleString(compile("\"hElLo\".toUpper()"), "HELLO");
  }

  @Test
  public void testStringToUpperAlreadyUpper() {
    assertSingleString(compile("\"HELLO\".toUpper()"), "HELLO");
  }

  @Test
  public void testStringToUpperEmpty() {
    assertSingleString(compile("\"\".toUpper()"), "");
  }

  @Test
  public void testIntegerToUpperFails() {
    compileExpectError("1.toUpper()");
  }

  @Test
  public void testFloatToUpperFails() {
    compileExpectError("1.5.toUpper()");
  }

  @Test
  public void testBooleanToUpperFails() {
    compileExpectError("true.toUpper()");
  }

  @Test
  public void testSetToUpperFails() {
    compileExpectError("Set{\"a\"}.toUpper()");
  }

  @Test
  public void testSequenceToUpperFails() {
    compileExpectError("Sequence{\"a\"}.toUpper()");
  }

  // ── toLower() ─────────────────────────────────────────────────

  @Test
  public void testStringToLowerUppercase() {
    assertSingleString(compile("\"HELLO\".toLower()"), "hello");
  }

  @Test
  public void testStringToLowerMixed() {
    assertSingleString(compile("\"HeLLo\".toLower()"), "hello");
  }

  @Test
  public void testStringToLowerEmpty() {
    assertSingleString(compile("\"\".toLower()"), "");
  }

  @Test
  public void testIntegerToLowerFails() {
    compileExpectError("1.toLower()");
  }

  @Test
  public void testBooleanToLowerFails() {
    compileExpectError("true.toLower()");
  }

  @Test
  public void testSetToLowerFails() {
    compileExpectError("Set{\"a\"}.toLower()");
  }

  // ── size() on scalar — must fail (size() requires a multi-valued collection) ──

  @Test
  public void testStringSizeOnSingleton() {
    compileExpectError("\"hello\".size()");
  }

  @Test
  public void testIntegerSizeOnSingleton() {
    compileExpectError("1.size()");
  }

  @Test
  public void testBooleanSizeOnSingleton() {
    compileExpectError("true.size()");
  }

  // ── substring(i, j) ───────────────────────────────────────────

  @Test
  public void testStringSubstringMiddle() {
    assertSingleString(compile("\"hello\".substring(2, 4)"), "ell");
  }

  @Test
  public void testStringSubstringFull() {
    assertSingleString(compile("\"hello\".substring(1, 5)"), "hello");
  }

  @Test
  public void testStringSubstringSingleChar() {
    assertSingleString(compile("\"hello\".substring(1, 1)"), "h");
  }

  @Test
  public void testIntegerSubstringFails() {
    compileExpectError("1.substring(1, 2)");
  }

  @Test
  public void testBooleanSubstringFails() {
    compileExpectError("true.substring(1, 2)");
  }

  @Test
  public void testSetSubstringFails() {
    compileExpectError("Set{\"a\"}.substring(1, 1)");
  }

  // ── at(i) on String ★ ─────────────────────────────────────────

  @Test
  public void testStringAtFirst() {
    assertSingleString(compile("\"hello\".at(1)"), "h");
  }

  @Test
  public void testStringAtMiddle() {
    assertSingleString(compile("\"hello\".at(3)"), "l");
  }

  @Test
  public void testStringAtLast() {
    assertSingleString(compile("\"hello\".at(5)"), "o");
  }

  @Test
  public void testIntegerAtFails() {
    compileExpectError("1.at(1)");
  }

  @Test
  public void testBooleanAtFails() {
    compileExpectError("true.at(1)");
  }

  @Test
  public void testSetAtStringFails() {
    compileExpectError("Set{\"a\"}.at(1)");
  }

  // ── characters() ★ ────────────────────────────────────────────

  @Test
  public void testStringCharactersBasic() {
    Value result = compile("\"abc\".characters()");
    assertEquals(3, result.size());
    assertEquals("a", ((OCLElement.StringValue) result.getElements().get(0)).value());
    assertEquals("b", ((OCLElement.StringValue) result.getElements().get(1)).value());
    assertEquals("c", ((OCLElement.StringValue) result.getElements().get(2)).value());
  }

  @Test
  public void testStringCharactersEmpty() {
    Value result = compile("\"\".characters()");
    assertEquals(0, result.size());
  }

  @Test
  public void testStringCharactersSingleChar() {
    Value result = compile("\"x\".characters()");
    assertEquals(1, result.size());
    assertEquals("x", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testIntegerCharactersFails() {
    compileExpectError("1.characters()");
  }

  @Test
  public void testBooleanCharactersFails() {
    compileExpectError("true.characters()");
  }

  @Test
  public void testSetCharactersFails() {
    compileExpectError("Set{\"a\"}.characters()");
  }

  // ── toInteger() ★ ─────────────────────────────────────────────

  @Test
  public void testStringToIntegerBasic() {
    assertSingleInt(compile("\"42\".toInteger()"), 42);
  }

  @Test
  public void testStringToIntegerNegative() {
    assertSingleInt(compile("\"-7\".toInteger()"), -7);
  }

  @Test
  public void testIntegerToIntegerFails() {
    compileExpectError("42.toInteger()");
  }

  @Test
  public void testBooleanToIntegerFails() {
    compileExpectError("true.toInteger()");
  }

  @Test
  public void testSetToIntegerFails() {
    compileExpectError("Set{\"1\"}.toInteger()");
  }

  // ── toReal() ★ ────────────────────────────────────────────────

  @Test
  public void testStringToRealBasic() {
    assertSingleDouble(compile("\"3.14\".toReal()"), 3.14);
  }

  @Test
  public void testStringToRealInteger() {
    assertSingleDouble(compile("\"42\".toReal()"), 42.0);
  }

  @Test
  public void testIntegerToRealFails() {
    compileExpectError("42.toReal()");
  }

  @Test
  public void testBooleanToRealFails() {
    compileExpectError("true.toReal()");
  }

  @Test
  public void testSetToRealFails() {
    compileExpectError("Set{\"3.14\"}.toReal()");
  }

  // ── substituteAll(p, r) ★ ─────────────────────────────────────

  @Test
  public void testStringSubstituteAllBasic() {
    assertSingleString(compile("\"aabbcc\".substituteAll(\"b\", \"x\")"), "aaxxcc");
  }

  @Test
  public void testStringSubstituteAllNotFound() {
    assertSingleString(compile("\"hello\".substituteAll(\"z\", \"x\")"), "hello");
  }

  @Test
  public void testStringSubstituteAllMultiple() {
    assertSingleString(compile("\"abab\".substituteAll(\"ab\", \"c\")"), "cc");
  }

  @Test
  public void testIntegerSubstituteAllFails() {
    compileExpectError("1.substituteAll(\"a\", \"b\")");
  }

  @Test
  public void testBooleanSubstituteAllFails() {
    compileExpectError("true.substituteAll(\"t\", \"f\")");
  }

  @Test
  public void testSetSubstituteAllFails() {
    compileExpectError("Set{\"a\"}.substituteAll(\"a\", \"b\")");
  }

  // ── substituteFirst(p, r) ★ ───────────────────────────────────

  @Test
  public void testStringSubstituteFirstBasic() {
    assertSingleString(compile("\"aabbcc\".substituteFirst(\"b\", \"x\")"), "aaxbcc");
  }

  @Test
  public void testStringSubstituteFirstOnlyFirst() {
    assertSingleString(compile("\"aaaa\".substituteFirst(\"a\", \"b\")"), "baaa");
  }

  @Test
  public void testIntegerSubstituteFirstFails() {
    compileExpectError("1.substituteFirst(\"a\", \"b\")");
  }

  @Test
  public void testSetSubstituteFirstFails() {
    compileExpectError("Set{\"a\"}.substituteFirst(\"a\", \"b\")");
  }

  // ── concat(String) ────────────────────────────────────────────

  @Test
  public void testStringConcatString() {
    assertSingleString(compile("\"hello\".concat(\", world\")"), "hello, world");
  }

  @Test
  public void testStringConcatEmpty() {
    assertSingleString(compile("\"hello\".concat(\"\")"), "hello");
  }

  @Test
  public void testEmptyConcatString() {
    assertSingleString(compile("\"\".concat(\"hello\")"), "hello");
  }

  @Test
  public void testIntegerConcatFails() {
    compileExpectError("1.concat(\"hello\")");
  }

  @Test
  public void testBooleanConcatFails() {
    compileExpectError("true.concat(\"hello\")");
  }

  @Test
  public void testSetConcatFails() {
    compileExpectError("Set{\"a\"}.concat(\"b\")");
  }

  // ── indexOf(String) ───────────────────────────────────────────

  @Test
  public void testStringIndexOfFound() {
    assertSingleInt(compile("\"hello\".indexOf(\"ll\")"), 3);
  }

  @Test
  public void testStringIndexOfNotFound() {
    assertSingleInt(compile("\"hello\".indexOf(\"xyz\")"), 0);
  }

  @Test
  public void testStringIndexOfFirst() {
    assertSingleInt(compile("\"hello\".indexOf(\"h\")"), 1);
  }

  @Test
  public void testIntegerIndexOfFails() {
    compileExpectError("1.indexOf(\"1\")");
  }

  @Test
  public void testBooleanIndexOfFails() {
    compileExpectError("true.indexOf(\"t\")");
  }

  @Test
  public void testSetIndexOfFails() {
    compileExpectError("Set{\"a\"}.indexOf(\"a\")");
  }

  // ── equalsIgnoreCase(String) ──────────────────────────────────

  @Test
  public void testStringEqualsIgnoreCaseTrue() {
    assertSingleBool(compile("\"Hello\".equalsIgnoreCase(\"HELLO\")"), true);
  }

  @Test
  public void testStringEqualsIgnoreCaseFalse() {
    assertSingleBool(compile("\"Hello\".equalsIgnoreCase(\"World\")"), false);
  }

  @Test
  public void testStringEqualsIgnoreCaseSame() {
    assertSingleBool(compile("\"hello\".equalsIgnoreCase(\"hello\")"), true);
  }

  @Test
  public void testIntegerEqualsIgnoreCaseFails() {
    compileExpectError("1.equalsIgnoreCase(\"1\")");
  }

  @Test
  public void testBooleanEqualsIgnoreCaseFails() {
    compileExpectError("true.equalsIgnoreCase(\"true\")");
  }

  @Test
  public void testSetEqualsIgnoreCaseFails() {
    compileExpectError("Set{\"a\"}.equalsIgnoreCase(\"a\")");
  }

  // ── matches(String) ★ ─────────────────────────────────────────

  @Test
  public void testStringMatchesTrue() {
    assertSingleBool(compile("\"hello123\".matches(\"[a-z]+[0-9]+\")"), true);
  }

  @Test
  public void testStringMatchesFalse() {
    assertSingleBool(compile("\"hello\".matches(\"[0-9]+\")"), false);
  }

  @Test
  public void testStringMatchesExact() {
    assertSingleBool(compile("\"abc\".matches(\"abc\")"), true);
  }

  @Test
  public void testIntegerMatchesFails() {
    compileExpectError("1.matches(\"[0-9]+\")");
  }

  @Test
  public void testBooleanMatchesFails() {
    compileExpectError("true.matches(\"true\")");
  }

  @Test
  public void testSetMatchesFails() {
    compileExpectError("Set{\"a\"}.matches(\"a\")");
  }

  // ── tokenize(String) ★ ────────────────────────────────────────

  @Test
  public void testStringTokenizeComma() {
    Value result = compile("\"a,b,c\".tokenize(\",\")");
    assertEquals(3, result.size());
    assertEquals("a", ((OCLElement.StringValue) result.getElements().get(0)).value());
    assertEquals("b", ((OCLElement.StringValue) result.getElements().get(1)).value());
    assertEquals("c", ((OCLElement.StringValue) result.getElements().get(2)).value());
  }

  @Test
  public void testStringTokenizeNoDelimiter() {
    Value result = compile("\"hello\".tokenize(\"z\")");
    assertEquals(1, result.size());
    assertEquals("hello", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  @Test
  public void testStringTokenizeSpace() {
    Value result = compile("\"a b c\".tokenize(\" \")");
    assertEquals(3, result.size());
  }

  @Test
  public void testIntegerTokenizeFails() {
    compileExpectError("1.tokenize(\",\")");
  }

  @Test
  public void testBooleanTokenizeFails() {
    compileExpectError("true.tokenize(\",\")");
  }

  @Test
  public void testSetTokenizeFails() {
    compileExpectError("Set{\"a,b\"}.tokenize(\",\")");
  }
}