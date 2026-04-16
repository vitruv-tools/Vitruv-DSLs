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
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for String operations in VitruvOCL.
 *
 * @see Value Runtime value representation
 * @see tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement.StringValue String element wrapper
 * @see tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor Evaluates string operations
 * @see tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor Type checks string expressions
 */
public class StringOperationsTest extends DummyTestSpecification {

  // ==================== String Literals ====================

  /** Tests basic string literal: {@code "Hello"} → {@code ["Hello"]}. */
  @Test
  public void testStringLiteral() {
    assertSingleString(compile("\"Hello\""), "Hello");
  }

  // ==================== CONCAT ====================

  /** Tests string concatenation including empty string cases. */
  @Test
  public void testConcat() {
    assertSingleString(compile("\"Hello\".concat(\" World\")"), "Hello World");
    assertSingleString(compile("\"OCL\".concat(\"#\")"), "OCL#");
    assertSingleString(compile("\"\".concat(\"empty\")"), "empty");
    assertSingleString(compile("\"Test\".concat(\"\")"), "Test");
  }

  // ==================== SUBSTRING ====================

  /** Tests substring extraction: {@code "Hello".substring(1, 3)} → {@code ["Hel"]}. */
  @Test
  public void testSubstring() {
    assertSingleString(compile("\"Hello\".substring(1, 3)"), "Hel");
  }

  /** Tests complex chained string expressions and integration with conditionals. */
  @Test
  public void testComplexStringExpressions() {
    assertSingleString(compile("\"Hello\".substring(1, 2).concat(\"i\").toUpper()"), "HEI");
    assertSingleString(compile("\"Hello\".substring(1, 1).concat(\"i\").toUpper()"), "HI");
    assertSingleString(
        compile("if \"test\".toUpper() == \"TEST\" then \"OK\" else \"FAIL\" endif"), "OK");
    assertSingleBool(
        compile("if \"Hello World\".indexOf(\"World\") > 0 then true else false endif"), true);
  }

  /** Tests substring with invalid indices → empty collection. */
  @Test
  public void testSubstringInvalidIndices() {
    assertSize(compile("\"Hi\".substring(5, 10)"), 0);
    assertSize(compile("\"Test\".substring(0, 2)"), 0);
    assertSize(compile("\"Test\".substring(3, 2)"), 0);
    assertSize(compile("\"Hi\".substring(1, 10)"), 0);
  }

  // ==================== TO UPPER ====================

  /** Tests uppercase transformation. */
  @Test
  public void testToUpper() {
    assertSingleString(compile("\"hello\".toUpper()"), "HELLO");
    assertSingleString(compile("\"OCL\".toUpper()"), "OCL");
    assertSingleString(compile("\"MiXeD\".toUpper()"), "MIXED");
    assertSingleString(compile("\"\""), "");
    assertSingleString(compile("\"123abc\".toUpper()"), "123ABC");
  }

  // ==================== TO LOWER ====================

  /** Tests lowercase transformation. */
  @Test
  public void testToLower() {
    assertSingleString(compile("\"HELLO\".toLower()"), "hello");
    assertSingleString(compile("\"ocl\".toLower()"), "ocl");
    assertSingleString(compile("\"MiXeD\".toLower()"), "mixed");
    assertSingleString(compile("\"\""), "");
    assertSingleString(compile("\"ABC123\".toLower()"), "abc123");
  }

  // ==================== INDEX OF ====================

  /** Tests indexOf (1-based, returns 0 if not found). */
  @Test
  public void testIndexOf() {
    assertSingleInt(compile("\"Hello World\".indexOf(\"World\")"), 7);
    assertSingleInt(compile("\"Hello\".indexOf(\"H\")"), 1);
    assertSingleInt(compile("\"Hello\".indexOf(\"e\")"), 2);
    assertSingleInt(compile("\"Hello\".indexOf(\"o\")"), 5);
    assertSingleInt(compile("\"Hello\".indexOf(\"x\")"), 0);
    assertSingleInt(compile("\"Test Test\".indexOf(\"Test\")"), 1);
    assertSingleInt(compile("\"\".indexOf(\"x\")"), 0);
  }

  // ==================== EQUALS IGNORE CASE ====================

  /** Tests case-insensitive equality comparison. */
  @Test
  public void testEqualsIgnoreCase() {
    assertSingleBool(compile("\"hello\".equalsIgnoreCase(\"HELLO\")"), true);
    assertSingleBool(compile("\"OCL\".equalsIgnoreCase(\"ocl\")"), true);
    assertSingleBool(compile("\"test\".equalsIgnoreCase(\"different\")"), false);
    assertSingleBool(compile("\"Test123\".equalsIgnoreCase(\"test123\")"), true);
    assertSingleBool(compile("\"\".equalsIgnoreCase(\"\")"), true);
  }

  // ==================== STRING CHAINING ====================

  /** Tests chaining multiple string operations. */
  @Test
  public void testStringChaining() {
    assertSingleString(compile("\"hello\".toUpper().concat(\" WORLD\")"), "HELLO WORLD");
    assertSingleString(compile("\"  TEST  \".toUpper().substring(3, 6)"), "TEST");
    assertSingleString(compile("\"OCL\".concat(\"#\").toLower()"), "ocl#");
    assertSingleString(compile("\"Hello\".concat(\" \").concat(\"World\")"), "Hello World");
  }

  // ==================== STRING COMPARISON ====================

  /** Tests string equality, inequality, and lexicographic ordering. */
  @Test
  public void testStringComparison() {
    assertSingleBool(compile("\"abc\" == \"abc\""), true);
    assertSingleBool(compile("\"abc\" != \"xyz\""), true);
    assertSingleBool(compile("\"abc\" < \"xyz\""), true);
  }

  // ==================== Compiler Infrastructure ====================

  /**
   * Overrides compile() to use {@code infixedExpCS()} with token stream reset and injection for
   * string keyword detection in complex expressions.
   */
  @Override
  protected Value compile(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    ParseTree tree = parser.infixedExpCS();
    tokens.seek(0);

    MetamodelWrapperInterface dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.setTokenStream(tokens);
    typeChecker.visit(tree);

    if (typeChecker.hasErrors()) {
      fail("Pass 2 (Type checking) failed: " + typeChecker.getErrorCollector().getErrors());
    }

    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    evaluator.setTokenStream(tokens);
    Value result = evaluator.visit(tree);

    if (evaluator.hasErrors()) {
      fail("Pass 3 (Evaluation) failed: " + evaluator.getErrorCollector().getErrors());
    }

    return result;
  }
}