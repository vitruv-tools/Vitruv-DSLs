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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for Type Checking phase (Pass 2) of VitruvOCL compiler.
 *
 * <p>Tests type inference, compatibility checking, and error detection without evaluation.
 *
 * @see TypeCheckVisitor
 * @see Type
 */
public class TypeCheckerTest {

  // ==================== Literals ====================

  @Test
  public void testIntegerLiteralType() {
    Type type = getType("42");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testStringLiteralType() {
    Type type = getType("\"hello\"");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testBooleanTrueLiteralType() {
    Type type = getType("true");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testBooleanFalseLiteralType() {
    Type type = getType("false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testDoubleLiteralType() {
    Type type = getType("3.14");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testNegativeIntegerType() {
    Type type = getType("-42");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testNegativeDoubleType() {
    Type type = getType("-3.14");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testEmptyStringType() {
    Type type = getType("\"\"");
    assertEquals(Type.STRING, type);
  }

  // ==================== Arithmetic Operations ====================

  @Test
  public void testIntegerAdditionType() {
    Type type = getType("5 + 3");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIntegerSubtractionType() {
    Type type = getType("10 - 3");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIntegerMultiplicationType() {
    Type type = getType("4 * 7");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIntegerDivisionType() {
    Type type = getType("20 / 4");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testDoubleAdditionType() {
    Type type = getType("3.5 + 2.1");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testMixedIntegerDoubleAdditionType() {
    Type type = getType("5 + 3.14");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testMixedDoubleIntegerAdditionType() {
    Type type = getType("3.14 + 5");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testChainedArithmeticType() {
    Type type = getType("1 + 2 + 3");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testComplexArithmeticType() {
    Type type = getType("(5 + 3) * 2 - 1");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testArithmeticWithNegativesType() {
    Type type = getType("-5 + 10");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testInvalidStringPlusIntegerError() {
    assertTypeError("\"text\" + 5");
  }

  @Test
  public void testInvalidIntegerPlusStringError() {
    assertTypeError("5 + \"text\"");
  }

  @Test
  public void testInvalidBooleanPlusIntegerError() {
    assertTypeError("true + 5");
  }

  @Test
  public void testInvalidStringMultiplyError() {
    assertTypeError("\"hello\" * 3");
  }

  // ==================== Comparison Operations ====================

  @Test
  public void testEqualityComparisonType() {
    Type type = getType("5 == 3");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testInequalityComparisonType() {
    Type type = getType("5 != 3");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testLessThanComparisonType() {
    Type type = getType("5 < 10");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testGreaterThanComparisonType() {
    Type type = getType("10 > 5");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testLessOrEqualComparisonType() {
    Type type = getType("5 <= 10");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testGreaterOrEqualComparisonType() {
    Type type = getType("10 >= 5");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testStringEqualityType() {
    Type type = getType("\"hello\" == \"world\"");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testBooleanEqualityType() {
    Type type = getType("true == false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testDoubleComparisonType() {
    Type type = getType("3.14 < 2.71");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testMixedIntegerDoubleComparisonType() {
    Type type = getType("5 < 3.14");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testInvalidIntegerStringComparisonError() {
    assertTypeError("5 < \"text\"");
  }

  @Test
  public void testInvalidBooleanIntegerComparisonError() {
    assertTypeError("true < 5");
  }

  // ==================== Boolean Operations ====================

  @Test
  public void testLogicalAndType() {
    Type type = getType("true and false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testLogicalOrType() {
    Type type = getType("true or false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testLogicalXorType() {
    Type type = getType("true xor false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testLogicalNotType() {
    Type type = getType("not true");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testComplexBooleanExpressionType() {
    Type type = getType("(true and false) or (not true)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testChainedAndType() {
    Type type = getType("true and true and false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testChainedOrType() {
    Type type = getType("false or false or true");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testComparisonInBooleanExpressionType() {
    Type type = getType("(5 > 3) and (10 < 20)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testInvalidIntegerAndBooleanError() {
    assertTypeError("5 and true");
  }

  @Test
  public void testInvalidBooleanAndIntegerError() {
    assertTypeError("true and 5");
  }

  @Test
  public void testInvalidStringOrBooleanError() {
    assertTypeError("\"text\" or true");
  }

  @Test
  public void testInvalidNotIntegerError() {
    assertTypeError("not 5");
  }

  // ==================== Collection Literals ====================

  @Test
  public void testEmptySetType() {
    Type type = getType("Set{}");
    assertNotNull(type);
    assertTrue(type.isCollection());
  }

  @Test
  public void testIntegerSetType() {
    Type type = getType("Set{1, 2, 3}");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testStringSetType() {
    Type type = getType("Set{\"a\", \"b\", \"c\"}");
    assertTrue(type.isCollection());
    assertEquals(Type.STRING, type.getElementType());
  }

  @Test
  public void testBooleanSetType() {
    Type type = getType("Set{true, false}");
    assertTrue(type.isCollection());
    assertEquals(Type.BOOLEAN, type.getElementType());
  }

  @Test
  public void testSingleElementSetType() {
    Type type = getType("Set{42}");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testSequenceLiteralType() {
    Type type = getType("Sequence{1, 2, 3}");
    assertTrue(type.isCollection());
    assertTrue(type.isOrdered());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testBagLiteralType() {
    Type type = getType("Bag{1, 2, 2, 3}");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testOrderedSetLiteralType() {
    Type type = getType("OrderedSet{1, 2, 3}");
    assertTrue(type.isCollection());
    assertTrue(type.isOrdered());
    assertTrue(type.isUnique());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  // ==================== Collection Operations ====================

  @Test
  public void testSizeOperationType() {
    Type type = getType("Set{1, 2, 3}.size()");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIsEmptyOperationType() {
    Type type = getType("Set{1, 2}.isEmpty()");
    assertEquals(Type.BOOLEAN, type.getElementType());
  }

  @Test
  public void testNotEmptyOperationType() {
    Type type = getType("Set{1, 2}.notEmpty()");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testIncludesOperationType() {
    Type type = getType("Set{1, 2, 3}.includes(2)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testExcludesOperationType() {
    Type type = getType("Set{1, 2, 3}.excludes(5)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testUnionOperationType() {
    Type type = getType("Set{1, 2}.union(Set{3, 4})");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testChainedSizeType() {
    Type type = getType("Set{Set{1}}.size()");
    assertEquals(Type.INTEGER, type);
  }

  // ==================== Iterator Expressions ====================

  @Test
  public void testSelectIteratorType() {
    Type type = getType("Set{1, 2, 3, 4}.select(x | x > 2)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testRejectIteratorType() {
    Type type = getType("Set{1, 2, 3, 4}.reject(x | x > 2)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testCollectIteratorType() {
    Type type = getType("Set{1, 2, 3}.collect(x | x * 2)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testCollectBooleanType() {
    Type type = getType("Set{1, 2, 3}.collect(x | x > 1)");
    assertTrue(type.isCollection());
    assertEquals(Type.BOOLEAN, type.getElementType());
  }

  @Test
  public void testCollectStringType() {
    Type type = getType("Set{1, 2}.collect(x | \"item\")");
    assertTrue(type.isCollection());
    assertEquals(Type.STRING, type.getElementType());
  }

  @Test
  public void testForAllIteratorType() {
    Type type = getType("Set{1, 2, 3}.forAll(x | x > 0)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testExistsIteratorType() {
    Type type = getType("Set{1, 2, 3}.exists(x | x > 2)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testNestedSelectType() {
    Type type = getType("Set{1, 2, 3}.select(x | x > 1).select(y | y < 3)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testSelectThenCollectType() {
    Type type = getType("Set{1, 2, 3}.select(x | x > 1).collect(y | y * 2)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testCollectThenSelectType() {
    Type type = getType("Set{1, 2, 3}.collect(x | x > 1).select(b | b)");
    assertTrue(type.isCollection());
    assertEquals(Type.BOOLEAN, type.getElementType());
  }

  @Test
  public void testForAllNonBooleanBodyError() {
    assertTypeError("Set{1, 2, 3}.forAll(x | x * 2)");
  }

  @Test
  public void testExistsNonBooleanBodyError() {
    assertTypeError("Set{1, 2, 3}.exists(x | x + 5)");
  }

  // ==================== Let Expressions ====================

  @Test
  public void testSimpleLetIntegerType() {
    Type type = getType("let x = 5 in x + 10");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testSimpleLetStringType() {
    Type type = getType("let s = \"hello\" in s.concat(\" world\")");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testSimpleLetBooleanType() {
    Type type = getType("let b = true in b and false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testLetWithArithmeticType() {
    Type type = getType("let x = 5 in let y = 10 in x + y");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testLetWithComparisonType() {
    Type type = getType("let x = 5 in x > 3");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testLetWithCollectionType() {
    Type type = getType("let s = Set{1, 2, 3} in s.size()");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testNestedLetType() {
    Type type = getType("let x = 5 in (let y = x + 10 in y * 2)");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testLetShadowingType() {
    Type type = getType("let x = 5 in (let x = 10 in x)");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testLetInIteratorType() {
    Type type = getType("let threshold = 5 in Set{1, 2, 10}.select(x | x > threshold)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testLetIntegerUsedAsStringError() {
    assertTypeError("let x = 5 in x.concat(\"text\")");
  }

  @Test
  public void testLetStringUsedInArithmeticError() {
    assertTypeError("let s = \"hello\" in s + 5");
  }

  @Test
  public void testLetBooleanUsedInArithmeticError() {
    assertTypeError("let b = true in b * 2");
  }

  // ==================== If-Then-Else ====================

  @Test
  public void testSimpleIfThenElseIntegerType() {
    Type type = getType("if true then 5 else 10 endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testSimpleIfThenElseStringType() {
    Type type = getType("if false then \"yes\" else \"no\" endif");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testSimpleIfThenElseBooleanType() {
    Type type = getType("if true then true else false endif");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testIfWithComparisonConditionType() {
    Type type = getType("if 5 > 3 then 100 else 0 endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIfWithBooleanOperationConditionType() {
    Type type = getType("if true and false then 1 else 2 endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testNestedIfThenElseType() {
    Type type = getType("if true then (if false then 1 else 2 endif) else 3 endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIfWithArithmeticBranchesType() {
    Type type = getType("if true then 5 + 3 else 10 * 2 endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIfWithCollectionType() {
    Type type = getType("if true then Set{1, 2} else Set{3, 4} endif");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testIfNonBooleanConditionError() {
    assertTypeError("if 5 then 10 else 20 endif");
  }

  @Test
  public void testIfStringConditionError() {
    assertTypeError("if \"yes\" then 1 else 2 endif");
  }

  @Test
  public void testIfIncompatibleBranchesError() {
    assertTypeError("if true then 5 else \"text\" endif");
  }

  @Test
  public void testIfIntegerBooleanBranchesError() {
    assertTypeError("if true then 42 else true endif");
  }

  @Test
  public void testIfStringBooleanBranchesError() {
    assertTypeError("if false then \"hello\" else false endif");
  }

  // ==================== String Operations ====================

  @Test
  public void testConcatOperationType() {
    Type type = getType("\"hello\".concat(\" world\")");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testToUpperOperationType() {
    Type type = getType("\"hello\".toUpper()");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testToLowerOperationType() {
    Type type = getType("\"HELLO\".toLower()");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testSubstringOperationType() {
    Type type = getType("\"hello\".substring(1, 3)");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testChainedStringOperationsType() {
    Type type = getType("\"hello\".toUpper().concat(\" WORLD\")");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testConcatOnIntegerError() {
    assertTypeError("42.concat(\"text\")");
  }

  @Test
  public void testToUpperOnIntegerError() {
    assertTypeError("42.toUpper()");
  }

  @Test
  public void testToLowerOnBooleanError() {
    assertTypeError("true.toLower()");
  }

  @Test
  public void testSubstringOnIntegerError() {
    assertTypeError("123.substring(1, 2)");
  }

  // ==================== Parenthesized Expressions ====================

  @Test
  public void testParenthesizedIntegerType() {
    Type type = getType("(42)");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testParenthesizedArithmeticType() {
    Type type = getType("(5 + 3) * 2");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testParenthesizedBooleanType() {
    Type type = getType("(true and false) or true");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testNestedParenthesesType() {
    Type type = getType("((5 + 3))");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testParenthesizedComparisonType() {
    Type type = getType("(5 > 3)");
    assertEquals(Type.BOOLEAN, type);
  }

  // ==================== Complex Expressions ====================

  @Test
  public void testComplexNestedExpression() {
    Type type = getType("let x = Set{1, 2, 3}.select(n | n > 1) in x.size()");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testComplexChainedOperations() {
    Type type = getType("Set{1, 2, 3, 4}.select(x | x > 1).collect(y | y * 2).includes(4)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testComplexIfWithIterators() {
    Type type = getType("if Set{1, 2, 3}.forAll(x | x > 0) then 100 else 0 endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testComplexLetWithMultipleBindings() {
    Type type = getType("let x = 5 in let y = 10 in let z = x + y in z * 2");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testComplexBooleanWithComparisons() {
    Type type = getType("(5 > 3 and 10 < 20) or (false and true)");
    assertEquals(Type.BOOLEAN, type);
  }

  // ==================== Edge Cases ====================

  @Test
  public void testZeroIntegerType() {
    Type type = getType("0");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testZeroDoubleType() {
    Type type = getType("0.0");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testLargeIntegerType() {
    Type type = getType("999999999");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testVerySmallDoubleType() {
    Type type = getType("0.0001");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testSingleCharStringType() {
    Type type = getType("\"a\"");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testStringWithSpecialCharsType() {
    Type type = getType("\"hello\\nworld\"");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testDivisionByZeroType() {
    // Type checking doesn't catch runtime errors, should type correctly
    Type type = getType("10 / 0");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testEmptyCollectionOperationType() {
    Type type = getType("Set{}.isEmpty()");
    assertEquals(Type.BOOLEAN, type.getElementType());
  }

  @Test
  public void testSingleElementCollectionSizeType() {
    Type type = getType("Set{42}.size()");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIdentityComparisonType() {
    Type type = getType("5 == 5");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testNotEqualsIdentityType() {
    Type type = getType("5 != 5");
    assertEquals(Type.BOOLEAN, type);
  }

  // ==================== Nested Collection Types ====================

  @Test
  public void testNestedSetType() {
    Type type = getType("Set{Set{1, 2}}");
    assertTrue(type.isCollection());
    assertTrue(type.getElementType().isCollection());
    assertEquals(Type.INTEGER, type.getElementType().getElementType());
  }

  @Test
  public void testCollectToNestedCollectionType() {
    Type type = getType("Set{1, 2}.collect(x | Set{x})");
    assertTrue(type.isCollection());
    assertTrue(type.getElementType().isCollection());
  }

  // ==================== Multiple Let Bindings ====================

  @Test
  public void testMultipleLetBindingsType() {
    Type type = getType("let x = 5, y = 10 in x + y");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testLetBindingDependencyType() {
    Type type = getType("let x = 5, y = x + 10 in y");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testLetBindingDifferentTypesType() {
    Type type = getType("let x = 5, s = \"hello\" in s");
    assertEquals(Type.STRING, type);
  }

  // ==================== Complex Iterator Nesting ====================

  @Test
  public void testTripleNestedSelectType() {
    Type type = getType("Set{1,2,3,4,5}.select(x|x>1).select(y|y<5).select(z|z!=3)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testSelectWithLetType() {
    Type type = getType("let threshold = 5 in Set{1,2,10}.select(x | x > threshold)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testCollectWithComplexBodyType() {
    Type type = getType("Set{1,2,3}.collect(x | if x > 1 then x * 2 else x endif)");
    assertTrue(type.isCollection());
    assertEquals(Type.INTEGER, type.getElementType());
  }

  @Test
  public void testForAllWithLetType() {
    Type type = getType("let minimum = 0 in Set{1,2,3}.forAll(x | x > minimum)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testExistsWithComparisonType() {
    Type type = getType("Set{1,2,3}.exists(x | x == 2)");
    assertEquals(Type.BOOLEAN, type);
  }

  // ==================== Nested If-Then-Else ====================

  @Test
  public void testDoubleNestedIfType() {
    Type type =
        getType(
            "if true then (if false then 1 else 2 endif) else (if true then 3 else 4 endif) endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIfWithLetInConditionType() {
    Type type = getType("if let x = 5 in x > 3 then 100 else 0 endif");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIfWithIteratorInConditionType() {
    Type type = getType("if Set{1,2,3}.forAll(x | x > 0) then \"yes\" else \"no\" endif");
    assertEquals(Type.STRING, type);
  }

  // ==================== Collection Operation Chaining ====================

  @Test
  public void testSizeAfterSelectType() {
    Type type = getType("Set{1,2,3,4}.select(x | x > 2).size()");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testIsEmptyAfterRejectType() {
    Type type = getType("Set{1,2,3}.reject(x | x > 5).isEmpty()");
    assertEquals(Type.BOOLEAN, type.getElementType());
  }

  @Test
  public void testIncludesAfterCollectType() {
    Type type = getType("Set{1,2,3}.collect(x | x * 2).includes(4)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testNotEmptyAfterSelectType() {
    Type type = getType("Set{1,2,3}.select(x | x > 1).notEmpty()");
    assertEquals(Type.BOOLEAN, type);
  }

  // ==================== Boolean Expression Complexity ====================

  @Test
  public void testComplexBooleanWithNotType() {
    Type type = getType("not (true and false) or (true xor false)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testBooleanWithMultipleComparisonsType() {
    Type type = getType("(5 > 3) and (10 < 20) and (\"a\" == \"a\")");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testBooleanWithIteratorResultsType() {
    Type type = getType("Set{1,2,3}.forAll(x|x>0) and Set{4,5}.exists(y|y==4)");
    assertEquals(Type.BOOLEAN, type);
  }

  // ==================== String Operation Chaining ====================

  @Test
  public void testTripleStringChainType() {
    Type type = getType("\"hello\".toUpper().concat(\" WORLD\").toLower()");
    assertEquals(Type.STRING, type);
  }

  @Test
  public void testStringConcatMultipleType() {
    Type type = getType("\"a\".concat(\"b\").concat(\"c\")");
    assertEquals(Type.STRING, type);
  }

  // ==================== Arithmetic Expression Complexity ====================

  @Test
  public void testComplexArithmeticPrecedenceType() {
    Type type = getType("5 + 3 * 2 - 1");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testArithmeticWithParenthesesType() {
    Type type = getType("(5 + 3) * (2 - 1)");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testMixedArithmeticChainType() {
    Type type = getType("5 + 3.14 - 2.0 * 1.5");
    assertEquals(Type.DOUBLE, type);
  }

  @Test
  public void testNegativeInArithmeticType() {
    Type type = getType("-5 + 10 - (-3)");
    assertEquals(Type.INTEGER, type);
  }

  // ==================== Type Error Edge Cases ====================

  @Test
  public void testSelectNonBooleanBodyError() {
    assertTypeError("Set{1,2,3}.select(x | x + 1)");
  }

  @Test
  public void testRejectNonBooleanBodyError() {
    assertTypeError("Set{1,2,3}.reject(x | x * 2)");
  }

  @Test
  public void testIfConditionFromArithmeticError() {
    assertTypeError("if 5 + 3 then 1 else 2 endif");
  }

  @Test
  public void testLetUsedWithWrongTypeError() {
    assertTypeError("let x = true in x + 5");
  }

  @Test
  public void testBooleanOperationOnCollectionError() {
    assertTypeError("Set{1,2,3} and true");
  }

  @Test
  public void testArithmeticOnBooleanError() {
    assertTypeError("true + false");
  }

  @Test
  public void testStringComparisonWithNumberError() {
    assertTypeError("\"hello\" < 5");
  }

  @Test
  public void testNotOnIntegerError() {
    assertTypeError("not 42");
  }

  @Test
  public void testXorOnStringError() {
    assertTypeError("\"true\" xor \"false\"");
  }

  // ==================== Collection Type Preservation ====================

  @Test
  public void testSelectPreservesSetType() {
    Type type = getType("Set{1,2,3}.select(x | x > 1)");
    assertTrue(type.isCollection());
    assertTrue(type.isUnique());
  }

  @Test
  public void testRejectPreservesSequenceType() {
    Type type = getType("Sequence{1,2,3}.reject(x | x > 5)");
    assertTrue(type.isCollection());
    assertTrue(type.isOrdered());
  }

  @Test
  public void testCollectFromSetToBagType() {
    Type type = getType("Set{1,2,3}.collect(x | x * 2)");
    assertTrue(type.isCollection());
    // collect from Set produces Bag
  }

  // ==================== Comparison Operation Variations ====================

  @Test
  public void testStringInequalityType() {
    Type type = getType("\"hello\" != \"world\"");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testBooleanInequalityType() {
    Type type = getType("true != false");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testDoubleLessOrEqualType() {
    Type type = getType("3.14 <= 2.71");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testDoubleGreaterOrEqualType() {
    Type type = getType("3.14 >= 2.71");
    assertEquals(Type.BOOLEAN, type);
  }

  // ==================== Empty Collection Edge Cases ====================

  @Test
  public void testEmptySetSizeType() {
    Type type = getType("Set{}.size()");
    assertEquals(Type.INTEGER, type);
  }

  @Test
  public void testEmptySequenceNotEmptyType() {
    Type type = getType("Sequence{}.notEmpty()");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testSelectFromEmptySetType() {
    Type type = getType("Set{}.select(x | x > 0)");
    assertTrue(type.isCollection());
  }

  // ==================== Parenthesized Complex Expressions ====================

  @Test
  public void testParenthesizedBooleanComplexType() {
    Type type = getType("((true and false) or true)");
    assertEquals(Type.BOOLEAN, type);
  }

  @Test
  public void testParenthesizedArithmeticComplexType() {
    Type type = getType("((5 + 3) * 2) - ((10 / 2) + 1)");
    assertEquals(Type.INTEGER, type);
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
      System.out.println(
          "Errors: "
              + errors.getErrors().stream()
                  .map(e -> e.getMessage())
                  .collect(Collectors.joining(", ")));
      fail("Type checking failed for '" + input + "': " + errors.getErrors());
    }

    return result;
  }

  private TypeCheckResult typeCheckWithErrors(String input, ErrorCollector errors) {
    ParseTree tree = parse(input);

    MetamodelWrapperInterface dummySpec =
        new MetamodelWrapperInterface() {
          @Override
          public EClass resolveEClass(String metamodel, String className) {
            return null;
          }

          @Override
          public List<EObject> getAllInstances(EClass eClass) {
            return List.of();
          }

          @Override
          public Set<String> getAvailableMetamodels() {
            return Set.of();
          }

          @Override
          public String getInstanceNameByIndex(int index) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException(
                "Unimplemented method 'getInstanceNameByIndex'");
          }
        };

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

  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.infixedExpCS();
  }
}