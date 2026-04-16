/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
 *******************************************************************************/
package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;

/**
 * Fundamental test suite for String literals and comparison operations in VitruvOCL.
 *
 * @see Value Runtime value representation
 * @see OCLElement.StringValue String element wrapper
 * @see StringOperationsTest Advanced string manipulation operations
 * @see tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor Evaluates string expressions
 * @see tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor Type checks string expressions
 */
public class StringTest extends DummyTestSpecification {

  // ==================== String Literals ====================

  /** Tests basic string literal: {@code "hello"} → {@code ["hello"]}. */
  @Test
  public void testSimpleString() {
    assertSingleString(compile("\"hello\""), "hello");
  }

  /** Tests empty string literal: {@code ""} → singleton {@code [""]} (not empty collection). */
  @Test
  public void testEmptyString() {
    assertSingleString(compile("\"\""), "");
  }

  /** Tests string with spaces: {@code "hello world"} → {@code ["hello world"]}. */
  @Test
  public void testStringWithSpaces() {
    assertSingleString(compile("\"hello world\""), "hello world");
  }

  // ==================== String Comparison ====================

  /** Tests string equality (true): {@code "hello" == "hello"} → {@code [true]}. */
  @Test
  public void testStringEquality() {
    assertSingleBool(compile("\"hello\" == \"hello\""), true);
  }

  /** Tests string inequality (true): {@code "hello" != "world"} → {@code [true]}. */
  @Test
  public void testStringInequalityTrue() {
    assertSingleBool(compile("\"hello\" != \"world\""), true);
  }

  /** Tests string inequality (false): {@code "test" != "test"} → {@code [false]}. */
  @Test
  public void testStringInequalityFalse() {
    assertSingleBool(compile("\"test\" != \"test\""), false);
  }

  /** Tests lexicographic less-than: {@code "apple" < "banana"} → {@code [true]}. */
  @Test
  public void testStringLessThan() {
    assertSingleBool(compile("\"apple\" < \"banana\""), true);
  }

  /** Tests lexicographic greater-than: {@code "zebra" > "apple"} → {@code [true]}. */
  @Test
  public void testStringGreaterThan() {
    assertSingleBool(compile("\"zebra\" > \"apple\""), true);
  }

  // ==================== String in Collections ====================

  /** Tests Set with string elements: {@code Set{"a","b","c"}} → 3 elements. */
  @Test
  public void testStringSet() {
    Value result = compile("Set{\"a\", \"b\", \"c\"}");
    assertSize(result, 3);
    assertTrue(result.includes(new OCLElement.StringValue("a")));
    assertTrue(result.includes(new OCLElement.StringValue("b")));
    assertTrue(result.includes(new OCLElement.StringValue("c")));
  }

  /** Tests size() on string Set: {@code Set{"hello","world"}.size()} → {@code [2]}. */
  @Test
  public void testStringSetSize() {
    assertSingleInt(compile("Set{\"hello\", \"world\"}.size()"), 2);
  }

  /**
   * Tests includes() on string Set: {@code Set{"apple","banana"}.includes("apple")} → {@code
   * [true]}.
   */
  @Test
  public void testStringSetIncludes() {
    assertSingleBool(compile("Set{\"apple\", \"banana\"}.includes(\"apple\")"), true);
  }

  // ==================== Entry Point Override ====================

  /** Overrides parse entry point to use {@code infixedExpCS()} for string expressions. */
  @Override
  protected ParseTree parse(String input) {
    CommonTokenStream tokens =
        new CommonTokenStream(new VitruvOCLLexer(CharStreams.fromString(input)));
    return new VitruvOCLParser(tokens).infixedExpCS();
  }
}