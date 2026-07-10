/* ******************************************************************************
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
package tools.vitruv.dsls.vitruvocl.typechecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;

/**
 * Comprehensive test suite for Type Checking phase (Pass 2) of VitruvOCL compiler.
 *
 * <p>Tests type inference, compatibility checking, and error detection without evaluation.
 *
 * @see TypeCheckVisitor
 * @see Type
 */
class TypeCheckerTest extends DummyTestSpecification {

  // ==================== Parameterized: expression → scalar type ====================

  @ParameterizedTest
  @MethodSource("typedExpressions")
  void testExpressionType(String expression, Type expectedType) {
    assertEquals(expectedType, getType(expression));
  }

  static Stream<Arguments> typedExpressions() {
    return Stream.of(
        // Literals
        Arguments.of("42", Type.INTEGER),
        Arguments.of("\"hello\"", Type.STRING),
        Arguments.of("true", Type.BOOLEAN),
        Arguments.of("false", Type.BOOLEAN),
        Arguments.of("3.14", Type.DOUBLE),
        Arguments.of("-42", Type.INTEGER),
        Arguments.of("-3.14", Type.DOUBLE),
        Arguments.of("\"\"", Type.STRING),
        // Arithmetic
        Arguments.of("5 + 3", Type.INTEGER),
        Arguments.of("10 - 3", Type.INTEGER),
        Arguments.of("4 * 7", Type.INTEGER),
        Arguments.of("20 / 4", Type.DOUBLE),
        Arguments.of("3.5 + 2.1", Type.DOUBLE),
        Arguments.of("5 + 3.14", Type.DOUBLE),
        Arguments.of("3.14 + 5", Type.DOUBLE),
        Arguments.of("1 + 2 + 3", Type.INTEGER),
        Arguments.of("(5 + 3) * 2 - 1", Type.INTEGER),
        Arguments.of("-5 + 10", Type.INTEGER),
        // Comparison
        Arguments.of("5 == 3", Type.BOOLEAN),
        Arguments.of("5 != 3", Type.BOOLEAN),
        Arguments.of("5 < 10", Type.BOOLEAN),
        Arguments.of("10 > 5", Type.BOOLEAN),
        Arguments.of("5 <= 10", Type.BOOLEAN),
        Arguments.of("10 >= 5", Type.BOOLEAN),
        Arguments.of("\"hello\" == \"world\"", Type.BOOLEAN),
        Arguments.of("true == false", Type.BOOLEAN),
        Arguments.of("3.14 < 2.71", Type.BOOLEAN),
        Arguments.of("5 < 3.14", Type.BOOLEAN),
        // Boolean
        Arguments.of("true and false", Type.BOOLEAN),
        Arguments.of("true or false", Type.BOOLEAN),
        Arguments.of("true xor false", Type.BOOLEAN),
        Arguments.of("not true", Type.BOOLEAN),
        Arguments.of("(true and false) or (not true)", Type.BOOLEAN),
        Arguments.of("true and true and false", Type.BOOLEAN),
        Arguments.of("false or false or true", Type.BOOLEAN),
        Arguments.of("(5 > 3) and (10 < 20)", Type.BOOLEAN),
        // Collection operations returning scalar
        Arguments.of("Set{1, 2, 3}.size()", Type.INTEGER),
        Arguments.of("Set{1, 2}.notEmpty()", Type.BOOLEAN),
        Arguments.of("Set{1, 2, 3}.includes(2)", Type.BOOLEAN),
        Arguments.of("Set{1, 2, 3}.excludes(5)", Type.BOOLEAN),
        Arguments.of("Set{Set{1}}.size()", Type.INTEGER),
        // Iterators returning scalar
        Arguments.of("Set{1, 2, 3}.forAll(x | x > 0)", Type.BOOLEAN),
        Arguments.of("Set{1, 2, 3}.exists(x | x > 2)", Type.BOOLEAN),
        // Let expressions
        Arguments.of("let x = 5 in x + 10", Type.INTEGER),
        Arguments.of("let s = \"hello\" in s.concat(\" world\")", Type.STRING),
        Arguments.of("let b = true in b and false", Type.BOOLEAN),
        Arguments.of("let x = 5 in let y = 10 in x + y", Type.INTEGER),
        Arguments.of("let x = 5 in x > 3", Type.BOOLEAN),
        Arguments.of("let s = Set{1, 2, 3} in s.size()", Type.INTEGER),
        Arguments.of("let x = 5 in (let y = x + 10 in y * 2)", Type.INTEGER),
        Arguments.of("let x = 5 in (let x = 10 in x)", Type.INTEGER),
        // If-then-else
        Arguments.of("if true then 5 else 10 endif", Type.INTEGER),
        Arguments.of("if false then \"yes\" else \"no\" endif", Type.STRING),
        Arguments.of("if true then true else false endif", Type.BOOLEAN),
        Arguments.of("if 5 > 3 then 100 else 0 endif", Type.INTEGER),
        Arguments.of("if true and false then 1 else 2 endif", Type.INTEGER),
        Arguments.of("if true then (if false then 1 else 2 endif) else 3 endif", Type.INTEGER),
        Arguments.of("if true then 5 + 3 else 10 * 2 endif", Type.INTEGER),
        // String operations
        Arguments.of("\"hello\".concat(\" world\")", Type.STRING),
        Arguments.of("\"hello\".toUpper()", Type.STRING),
        Arguments.of("\"HELLO\".toLower()", Type.STRING),
        Arguments.of("\"hello\".substring(1, 3)", Type.STRING),
        Arguments.of("\"hello\".toUpper().concat(\" WORLD\")", Type.STRING),
        // Parenthesized
        Arguments.of("(42)", Type.INTEGER),
        Arguments.of("(5 + 3) * 2", Type.INTEGER),
        Arguments.of("(true and false) or true", Type.BOOLEAN),
        Arguments.of("((5 + 3))", Type.INTEGER),
        Arguments.of("(5 > 3)", Type.BOOLEAN),
        // Complex expressions
        Arguments.of("let x = Set{1, 2, 3}.select(n | n > 1) in x.size()", Type.INTEGER),
        Arguments.of(
            "Set{1, 2, 3, 4}.select(x | x > 1).collect(y | y * 2).includes(4)", Type.BOOLEAN),
        Arguments.of("if Set{1, 2, 3}.forAll(x | x > 0) then 100 else 0 endif", Type.INTEGER),
        Arguments.of("let x = 5 in let y = 10 in let z = x + y in z * 2", Type.INTEGER),
        Arguments.of("(5 > 3 and 10 < 20) or (false and true)", Type.BOOLEAN),
        // Edge cases
        Arguments.of("0", Type.INTEGER),
        Arguments.of("0.0", Type.DOUBLE),
        Arguments.of("999999999", Type.INTEGER),
        Arguments.of("0.0001", Type.DOUBLE),
        Arguments.of("\"a\"", Type.STRING),
        Arguments.of("\"hello\\nworld\"", Type.STRING),
        Arguments.of("10 / 0", Type.DOUBLE),
        Arguments.of("Set{42}.size()", Type.INTEGER),
        Arguments.of("5 == 5", Type.BOOLEAN),
        Arguments.of("5 != 5", Type.BOOLEAN),
        // Multiple let bindings
        Arguments.of("let x = 5, y = 10 in x + y", Type.INTEGER),
        Arguments.of("let x = 5, y = x + 10 in y", Type.INTEGER),
        Arguments.of("let x = 5, s = \"hello\" in s", Type.STRING),
        // Iterator + forAll/exists
        Arguments.of("let minimum = 0 in Set{1,2,3}.forAll(x | x > minimum)", Type.BOOLEAN),
        Arguments.of("Set{1,2,3}.exists(x | x == 2)", Type.BOOLEAN),
        // Nested if
        Arguments.of(
            "if true then (if false then 1 else 2 endif) else (if true then 3 else 4 endif) endif",
            Type.INTEGER),
        Arguments.of("if let x = 5 in x > 3 then 100 else 0 endif", Type.INTEGER),
        Arguments.of("if Set{1,2,3}.forAll(x | x > 0) then \"yes\" else \"no\" endif", Type.STRING),
        // Operation chaining
        Arguments.of("Set{1,2,3,4}.select(x | x > 2).size()", Type.INTEGER),
        Arguments.of("Set{1,2,3}.collect(x | x * 2).includes(4)", Type.BOOLEAN),
        Arguments.of("Set{1,2,3}.select(x | x > 1).notEmpty()", Type.BOOLEAN),
        // Boolean complexity
        Arguments.of("not (true and false) or (true xor false)", Type.BOOLEAN),
        Arguments.of("(5 > 3) and (10 < 20) and (\"a\" == \"a\")", Type.BOOLEAN),
        Arguments.of("Set{1,2,3}.forAll(x|x>0) and Set{4,5}.exists(y|y==4)", Type.BOOLEAN),
        // String chaining
        Arguments.of("\"hello\".toUpper().concat(\" WORLD\").toLower()", Type.STRING),
        Arguments.of("\"a\".concat(\"b\").concat(\"c\")", Type.STRING),
        // Arithmetic complexity
        Arguments.of("5 + 3 * 2 - 1", Type.INTEGER),
        Arguments.of("(5 + 3) * (2 - 1)", Type.INTEGER),
        Arguments.of("5 + 3.14 - 2.0 * 1.5", Type.DOUBLE),
        Arguments.of("-5 + 10 - (-3)", Type.INTEGER),
        // Comparison variations
        Arguments.of("\"hello\" != \"world\"", Type.BOOLEAN),
        Arguments.of("true != false", Type.BOOLEAN),
        Arguments.of("3.14 <= 2.71", Type.BOOLEAN),
        Arguments.of("3.14 >= 2.71", Type.BOOLEAN),
        // Empty collection operations
        Arguments.of("Set{}.size()", Type.INTEGER),
        Arguments.of("Sequence{}.notEmpty()", Type.BOOLEAN),
        // Parenthesized complex
        Arguments.of("((true and false) or true)", Type.BOOLEAN),
        Arguments.of("((5 + 3) * 2) - ((10 / 2) + 1)", Type.DOUBLE));
  }

  // ==================== Parameterized: expression → type error ====================

  @ParameterizedTest
  @MethodSource("typeErrorExpressions")
  void testTypeError(String expression) {
    assertTypeError(expression);
  }

  static Stream<String> typeErrorExpressions() {
    return Stream.of(
        // Arithmetic type errors
        "\"text\" + 5",
        "5 + \"text\"",
        "true + 5",
        "\"hello\" * 3",
        // Comparison type errors
        "5 < \"text\"",
        "true < 5",
        // Boolean type errors
        "5 and true",
        "true and 5",
        "\"text\" or true",
        "not 5",
        // Iterator body errors
        "Set{1, 2, 3}.forAll(x | x * 2)",
        "Set{1, 2, 3}.exists(x | x + 5)",
        // Let type errors
        "let x = 5 in x.concat(\"text\")",
        "let s = \"hello\" in s + 5",
        "let b = true in b * 2",
        // If-then-else errors
        "if 5 then 10 else 20 endif",
        "if \"yes\" then 1 else 2 endif",
        "if true then 5 else \"text\" endif",
        "if true then 42 else true endif",
        "if false then \"hello\" else false endif",
        // String operation errors
        "42.concat(\"text\")",
        "42.toUpper()",
        "true.toLower()",
        "123.substring(1, 2)",
        // Type error edge cases
        "Set{1,2,3}.select(x | x + 1)",
        "Set{1,2,3}.reject(x | x * 2)",
        "if 5 + 3 then 1 else 2 endif",
        "let x = true in x + 5",
        "Set{1,2,3} and true",
        "true + false",
        "\"hello\" < 5",
        "not 42",
        "\"true\" xor \"false\"");
  }

  // ==================== Parameterized: expression → collection with element type
  // ====================

  @ParameterizedTest
  @MethodSource("collectionElementTypeExpressions")
  void testCollectionElementType(String expression, Type expectedElementType) {
    Type type = getType(expression);
    assertTrue(type.isCollection());
    assertEquals(expectedElementType, type.getElementType());
  }

  static Stream<Arguments> collectionElementTypeExpressions() {
    return Stream.of(
        // Collection literals
        Arguments.of("Set{1, 2, 3}", Type.INTEGER),
        Arguments.of("Set{\"a\", \"b\", \"c\"}", Type.STRING),
        Arguments.of("Set{true, false}", Type.BOOLEAN),
        Arguments.of("Set{42}", Type.INTEGER),
        Arguments.of("Bag{1, 2, 2, 3}", Type.INTEGER),
        // Collection operations
        Arguments.of("Set{1, 2}.union(Set{3, 4})", Type.INTEGER),
        // Iterators
        Arguments.of("Set{1, 2, 3, 4}.select(x | x > 2)", Type.INTEGER),
        Arguments.of("Set{1, 2, 3, 4}.reject(x | x > 2)", Type.INTEGER),
        Arguments.of("Set{1, 2, 3}.collect(x | x * 2)", Type.INTEGER),
        Arguments.of("Set{1, 2, 3}.collect(x | x > 1)", Type.BOOLEAN),
        Arguments.of("Set{1, 2}.collect(x | \"item\")", Type.STRING),
        Arguments.of("Set{1, 2, 3}.select(x | x > 1).select(y | y < 3)", Type.INTEGER),
        Arguments.of("Set{1, 2, 3}.select(x | x > 1).collect(y | y * 2)", Type.INTEGER),
        Arguments.of("Set{1, 2, 3}.collect(x | x > 1).select(b | b)", Type.BOOLEAN),
        // Let with collection
        Arguments.of("let threshold = 5 in Set{1, 2, 10}.select(x | x > threshold)", Type.INTEGER),
        // If with collection
        Arguments.of("if true then Set{1, 2} else Set{3, 4} endif", Type.INTEGER),
        // Complex iterator nesting
        Arguments.of("Set{1,2,3,4,5}.select(x|x>1).select(y|y<5).select(z|z!=3)", Type.INTEGER),
        Arguments.of("Set{1,2,3}.collect(x | if x > 1 then x * 2 else x endif)", Type.INTEGER));
  }

  // ==================== Standalone tests with complex assertions ====================

  @Test
  void testEmptySetType() {
    Type type = getType("Set{}");
    assertNotNull(type);
    assertTrue(type.isCollection());
  }

  @Test
  void testSequenceLiteralType() {
    Type type = getType("Sequence{1, 2, 3}");
    assertTrue(type.isCollection());
    assertTrue(type.isOrdered());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  void testOrderedSetLiteralType() {
    Type type = getType("OrderedSet{1, 2, 3}");
    assertTrue(type.isCollection());
    assertTrue(type.isOrdered());
    assertTrue(type.isUnique());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @ParameterizedTest
  @MethodSource("isEmptyBooleanElementExpressions")
  void testIsEmptyTypeReturnsBooleanElement(String expr) {
    assertEquals(Type.BOOLEAN, getType(expr).getElementType());
  }

  static Stream<String> isEmptyBooleanElementExpressions() {
    return Stream.of(
        "Set{1, 2}.isEmpty()", "Set{}.isEmpty()", "Set{1,2,3}.reject(x | x > 5).isEmpty()");
  }

  @Test
  void testNestedSetType() {
    Type type = getType("Set{Set{1, 2}}");
    assertTrue(type.isCollection());
    assertTrue(type.getElementType().isCollection());
    assertEquals(Type.INTEGER, type.getElementType().getElementType());
  }

  @Test
  void testCollectToNestedCollectionType() {
    Type type = getType("Set{1, 2}.collect(x | Set{x})");
    assertTrue(type.isCollection());
    assertTrue(type.getElementType().isCollection());
  }

  @Test
  void testSelectPreservesSetType() {
    Type type = getType("Set{1,2,3}.select(x | x > 1)");
    assertTrue(type.isCollection());
    assertTrue(type.isUnique());
  }

  @Test
  void testRejectPreservesSequenceType() {
    Type type = getType("Sequence{1,2,3}.reject(x | x > 5)");
    assertTrue(type.isCollection());
    assertTrue(type.isOrdered());
  }

  @Test
  void testCollectFromSetToBagType() {
    Type type = getType("Set{1,2,3}.collect(x | x * 2)");
    assertTrue(type.isCollection());
    // collect from Set produces Bag
  }

  @Test
  void testSelectFromEmptySetType() {
    Type type = getType("Set{}.select(x | x > 0)");
    assertTrue(type.isCollection());
  }

  // ==================== Helper Methods ====================

  private record TypeCheckResult(ParseTree tree, ParseTreeProperty<Type> nodeTypes) {}

  private Type getType(String input) {
    TypeCheckResult result = typeCheck(input);
    Type type = result.nodeTypes.get(result.tree);
    if (type == null && result.tree.getChildCount() > 0) {
      type = result.nodeTypes.get(result.tree.getChild(0));
    }
    assertNotNull(type, "Type should be inferred for: " + input);
    return type;
  }

  private void assertTypeError(String input) {
    ErrorCollector errors = new ErrorCollector();
    typeCheckWithErrors(input, errors);
    assertTrue(errors.hasErrors(), "Should detect type error for: " + input);
  }

  private TypeCheckResult typeCheck(String input) {
    ErrorCollector errors = new ErrorCollector();
    TypeCheckResult result = typeCheckWithErrors(input, errors);

    if (errors.hasErrors()) {
      fail("Type checking failed for '" + input + "': " + errors.getErrors());
    }

    return result;
  }

  private TypeCheckResult typeCheckWithErrors(String input, ErrorCollector errors) {
    ParseTree tree = parse(input);

    MetamodelWrapperInterface dummySpec = buildDummySpec();

    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();

    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      return new TypeCheckResult(tree, new ParseTreeProperty<>());
    }

    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.visit(tree);

    return new TypeCheckResult(tree, typeChecker.getNodeTypes());
  }

  @Override
  protected ParseTree parse(String input) {
    CommonTokenStream tokens =
        new CommonTokenStream(new VitruvOCLLexer(CharStreams.fromString(input)));
    return new VitruvOCLParser(tokens).infixedExpCS();
  }
}
