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
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for arithmetic and comparison operations in VitruvOCL.
 *
 * <p>This test class validates the complete implementation of mathematical expressions in the
 * VitruvOCL compiler, testing both basic arithmetic operations and collection-based aggregate
 * functions through the full three-phase compilation pipeline (parsing, type checking, evaluation).
 *
 * @see Value Runtime value representation (collections)
 * @see OCLElement.IntValue Integer element wrapper
 * @see OCLElement.BoolValue Boolean element wrapper
 * @see EvaluationVisitor Evaluates arithmetic expressions
 * @see TypeCheckVisitor Type checks arithmetic expressions
 */
public class SimpleMathTest {

  // ==================== Arithmetic Operations ====================

  /**
   * Tests basic addition operation.
   *
   * <p><b>Input:</b> {@code 1 + 2}
   *
   * <p><b>Expected:</b> {@code [3]} (singleton)
   *
   * <p><b>OCL# semantics:</b> Binary operators extract values from singleton operands, compute
   * result, and wrap in singleton.
   *
   * <p><b>Validates:</b> Basic addition functionality and singleton result wrapping.
   */
  @Test
  public void testOnePlusTwo() {
    String input = "1+2";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(3, ((OCLElement.IntValue) elem).value(), "1+2 should equal 3");
  }

  /**
   * Tests basic subtraction operation.
   *
   * <p><b>Input:</b> {@code 5 - 3}
   *
   * <p><b>Expected:</b> {@code [2]}
   */
  @Test
  public void testFiveMinusThree() {
    String input = "5-3";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(2, ((OCLElement.IntValue) elem).value(), "5-3 should equal 2");
  }

  /**
   * Tests basic multiplication operation.
   *
   * <p><b>Input:</b> {@code 4 * 5}
   *
   * <p><b>Expected:</b> {@code [20]}
   */
  @Test
  public void testMultiplication() {
    String input = "4*5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(20, ((OCLElement.IntValue) elem).value(), "4*5 should equal 20");
  }

  /**
   * Tests basic division operation.
   *
   * <p><b>Input:</b> {@code 20 / 4}
   *
   * <p><b>Expected:</b> {@code [5]}
   *
   * <p><b>Note:</b> Integer division (truncates towards zero).
   */
  @Test
  public void testDivision() {
    String input = "20/4";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(5, ((OCLElement.IntValue) elem).value(), "20/4 should equal 5");
  }

  /**
   * Tests chained operations with same precedence (left-to-right evaluation).
   *
   * <p><b>Input:</b> {@code 10 + 20 - 5}
   *
   * <p><b>Expected:</b> {@code [25]}
   *
   * <p><b>Evaluation order:</b> {@code (10 + 20) - 5} = {@code 30 - 5} = {@code 25}
   */
  @Test
  public void testChainedOperations() {
    String input = "10+20-5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(25, ((OCLElement.IntValue) elem).value(), "10+20-5 should equal 25");
  }

  /**
   * Tests operator precedence (multiplication before addition).
   *
   * <p><b>Input:</b> {@code 2 + 3 * 4}
   *
   * <p><b>Expected:</b> {@code [14]}
   *
   * <p><b>Evaluation order:</b> {@code 2 + (3 * 4)} = {@code 2 + 12} = {@code 14}
   *
   * <p><b>Validates:</b> Multiplication has higher precedence than addition.
   */
  @Test
  public void testOperatorPrecedence() {
    String input = "2+3*4";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(14, ((OCLElement.IntValue) elem).value(), "2+3*4 should equal 14 (precedence)");
  }

  /**
   * Tests unary minus operator on literal.
   *
   * <p><b>Input:</b> {@code -5}
   *
   * <p><b>Expected:</b> {@code [-5]}
   *
   * <p><b>Validates:</b> Unary negation operator functionality.
   */
  @Test
  public void testUnaryMinus() {
    String input = "-5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(-5, ((OCLElement.IntValue) elem).value(), "-5 should equal -5");
  }

  /**
   * Tests unary minus within binary expression.
   *
   * <p><b>Input:</b> {@code 10 + -5}
   *
   * <p><b>Expected:</b> {@code [5]}
   *
   * <p><b>Evaluation:</b> {@code 10 + (-5)} = {@code 5}
   *
   * <p><b>Validates:</b> Unary minus can be used within larger expressions.
   */
  @Test
  public void testUnaryMinusInExpression() {
    String input = "10 + -5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(5, ((OCLElement.IntValue) elem).value(), "10 + -5 should equal 5");
  }

  // ==================== Comparison Operations ====================

  /**
   * Tests less-than comparison (true case).
   *
   * <p><b>Input:</b> {@code 3 < 5}
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Validates:</b> Less-than operator returns Boolean singleton.
   */
  @Test
  public void testLessThan() {
    String input = "3 < 5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "3 < 5 should be true");
  }

  /**
   * Tests less-than comparison (false case).
   *
   * <p><b>Input:</b> {@code 5 < 3}
   *
   * <p><b>Expected:</b> {@code [false]}
   */
  @Test
  public void testLessThanFalse() {
    String input = "5 < 3";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "5 < 3 should be false");
  }

  /**
   * Tests less-than-or-equal comparison (equality case).
   *
   * <p><b>Input:</b> {@code 3 <= 3}
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Validates:</b> Equality satisfies less-than-or-equal.
   */
  @Test
  public void testLessThanOrEqual() {
    String input = "3 <= 3";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "3 <= 3 should be true");
  }

  /**
   * Tests greater-than comparison (true case).
   *
   * <p><b>Input:</b> {@code 10 > 5}
   *
   * <p><b>Expected:</b> {@code [true]}
   */
  @Test
  public void testGreaterThan() {
    String input = "10 > 5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "10 > 5 should be true");
  }

  /**
   * Tests greater-than-or-equal comparison (equality case).
   *
   * <p><b>Input:</b> {@code 5 >= 5}
   *
   * <p><b>Expected:</b> {@code [true]}
   */
  @Test
  public void testGreaterThanOrEqual() {
    String input = "5 >= 5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "5 >= 5 should be true");
  }

  /**
   * Tests equality comparison (true case).
   *
   * <p><b>Input:</b> {@code 5 == 5}
   *
   * <p><b>Expected:</b> {@code [true]}
   */
  @Test
  public void testEquality() {
    String input = "5 == 5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "5 == 5 should be true");
  }

  /**
   * Tests inequality comparison (true case).
   *
   * <p><b>Input:</b> {@code 5 != 3}
   *
   * <p><b>Expected:</b> {@code [true]}
   */
  @Test
  public void testInequality() {
    String input = "5 != 3";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertTrue(((OCLElement.BoolValue) elem).value(), "5 != 3 should be true");
  }

  /**
   * Tests inequality comparison (false case - values are equal).
   *
   * <p><b>Input:</b> {@code 5 != 5}
   *
   * <p><b>Expected:</b> {@code [false]}
   */
  @Test
  public void testInequalityFalse() {
    String input = "5 != 5";
    Value result = compile(input);

    assertEquals(1, result.size(), "Result should be a singleton");
    OCLElement elem = result.getElements().get(0);
    assertFalse(((OCLElement.BoolValue) elem).value(), "5 != 5 should be false");
  }

  // ==================== Collection Arithmetic Operations ====================

  /**
   * Tests sum() aggregate operation on various collections.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Set{1,2,3,4,5}.sum()} → {@code [15]}
   *   <li>{@code Set{10,20,30}.sum()} → {@code [60]}
   *   <li>{@code Sequence{1,2,3}.sum()} → {@code [6]}
   * </ul>
   *
   * <p><b>Validates:</b> Sum aggregation works on different collection types.
   */
  @Test
  public void testSum() {
    assertInt("Set{1,2,3,4,5}.sum()", 15);
    assertInt("Set{10,20,30}.sum()", 60);
    assertInt("Sequence{1,2,3}.sum()", 6);
  }

  /**
   * Tests sum() on empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.sum()}
   *
   * <p><b>Expected:</b> {@code [0]} (additive identity)
   *
   * <p><b>Mathematical property:</b> Sum of empty collection is 0.
   */
  @Test
  public void testSumEmptyCollection() {
    assertInt("Set{}.sum()", 0);
  }

  /**
   * Tests max() aggregate operation on various collections.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Set{1,5,3,9,2}.max()} → {@code [9]}
   *   <li>{@code Sequence{100,50,200,75}.max()} → {@code [200]}
   *   <li>{@code Set{-5,-10,-1}.max()} → {@code [-1]} (least negative)
   * </ul>
   */
  @Test
  public void testMax() {
    assertInt("Set{1,5,3,9,2}.max()", 9);
    assertInt("Sequence{100,50,200,75}.max()", 200);
    assertInt("Set{-5,-10,-1}.max()", -1);
  }

  /**
   * Tests max() on empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.max()}
   *
   * <p><b>Expected:</b> Empty collection (no maximum exists)
   *
   * <p><b>Mathematical property:</b> Maximum is undefined for empty sets.
   */
  @Test
  public void testMaxEmptyCollection() {
    assertEmpty("Set{}.max()");
  }

  /**
   * Tests min() aggregate operation on various collections.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Set{1,5,3,9,2}.min()} → {@code [1]}
   *   <li>{@code Sequence{100,50,200,75}.min()} → {@code [50]}
   *   <li>{@code Set{-5,-10,-1}.min()} → {@code [-10]} (most negative)
   * </ul>
   */
  @Test
  public void testMin() {
    assertInt("Set{1,5,3,9,2}.min()", 1);
    assertInt("Sequence{100,50,200,75}.min()", 50);
    assertInt("Set{-5,-10,-1}.min()", -10);
  }

  /**
   * Tests min() on empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.min()}
   *
   * <p><b>Expected:</b> Empty collection (no minimum exists)
   */
  @Test
  public void testMinEmptyCollection() {
    assertEmpty("Set{}.min()");
  }

  /**
   * Tests avg() (average) aggregate operation on various collections.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Set{1,2,3,4,5}.avg()} → {@code [3]} (15/5)
   *   <li>{@code Sequence{10,20,30}.avg()} → {@code [20]} (60/3)
   *   <li>{@code Set{100,200}.avg()} → {@code [150]} (300/2)
   * </ul>
   *
   * <p><b>Note:</b> Integer division for integer collections.
   */
  @Test
  public void testAvg() {
    assertInt("Set{1,2,3,4,5}.avg()", 3);
    assertInt("Sequence{10,20,30}.avg()", 20);
    assertInt("Set{100,200}.avg()", 150);
  }

  /**
   * Tests avg() on empty collection.
   *
   * <p><b>Input:</b> {@code Set{}.avg()}
   *
   * <p><b>Expected:</b> Empty collection (average undefined for empty set)
   */
  @Test
  public void testAvgEmptyCollection() {
    assertEmpty("Set{}.avg()");
  }

  /**
   * Tests abs() (absolute value) transformation on collections.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Set{-1,-2,-3}.abs()} → {@code {1,2,3}}
   *   <li>{@code Sequence{-5,10,-15}.abs()} → {@code {5,10,15}}
   *   <li>{@code Set{1,2,3}.abs()} → {@code {1,2,3}} (already positive)
   * </ul>
   *
   * <p><b>Operation type:</b> Element-wise transformation (maps each element).
   */
  @Test
  public void testAbs() {
    assertCollection("Set{-1,-2,-3}.abs()", 1, 2, 3);
    assertCollection("Sequence{-5,10,-15}.abs()", 5, 10, 15);
    assertCollection("Set{1,2,3}.abs()", 1, 2, 3);
  }

  /**
   * Tests floor() operation on integer collections.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.floor()}
   *
   * <p><b>Expected:</b> {@code {1,2,3}} (no-op for integers)
   *
   * <p><b>Note:</b> Floor function has no effect on integer values.
   */
  @Test
  public void testFloor() {
    // For integers, floor is no-op
    assertCollection("Set{1,2,3}.floor()", 1, 2, 3);
    assertCollection("Sequence{10,20,30}.floor()", 10, 20, 30);
  }

  /**
   * Tests ceil() (ceiling) operation on integer collections.
   *
   * <p><b>Note:</b> Ceiling function has no effect on integer values.
   */
  @Test
  public void testCeil() {
    // For integers, ceil is no-op
    assertCollection("Set{1,2,3}.ceil()", 1, 2, 3);
    assertCollection("Sequence{10,20,30}.ceil()", 10, 20, 30);
  }

  /**
   * Tests round() operation on integer collections.
   *
   * <p><b>Note:</b> Rounding function has no effect on integer values.
   */
  @Test
  public void testRound() {
    // For integers, round is no-op
    assertCollection("Set{1,2,3}.round()", 1, 2, 3);
    assertCollection("Sequence{10,20,30}.round()", 10, 20, 30);
  }

  /**
   * Tests lift() operation that wraps a collection in a singleton.
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.lift()}
   *
   * <p><b>Expected:</b> {@code {{1,2,3}}} (singleton containing the original collection)
   *
   * <p><b>Structure:</b> Creates a NestedCollection element wrapping the original collection.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Result is singleton (size = 1)
   *   <li>Element is NestedCollection type
   *   <li>Inner collection has correct size
   * </ul>
   */
  @Test
  public void testLift() {
    // {1,2,3}.lift() → {{1,2,3}}
    Value result = compile("Set{1,2,3}.lift()");

    assertEquals(1, result.size(), "lift() should create singleton containing collection");
    OCLElement elem = result.getElements().get(0);
    assertTrue(elem instanceof OCLElement.NestedCollection, "Element should be NestedCollection");

    OCLElement.NestedCollection nested = (OCLElement.NestedCollection) elem;
    assertEquals(3, nested.value().size(), "Inner collection should have 3 elements");
  }

  /**
   * Tests lift() followed by flatten() (identity operation).
   *
   * <p><b>Input:</b> {@code Set{1,2,3}.lift().flatten()}
   *
   * <p><b>Expected:</b> {@code {1,2,3}} (back to original collection)
   *
   * <p><b>Property:</b> {@code lift().flatten()} is the identity operation.
   */
  @Test
  public void testLiftThenFlatten() {
    // {1,2,3}.lift().flatten() → {1,2,3}
    assertCollection("Set{1,2,3}.lift().flatten()", 1, 2, 3);
  }

  /**
   * Tests chaining aggregate operations from different collections.
   *
   * <p><b>Input:</b> {@code Set{1,2,3,4,5}.sum() + Set{10,20}.sum()}
   *
   * <p><b>Expected:</b> {@code [45]} (15 + 30)
   *
   * <p><b>Validates:</b> Aggregate results can be used in further arithmetic.
   */
  @Test
  public void testArithmeticChaining() {
    assertInt("Set{1,2,3,4,5}.sum() + Set{10,20}.sum()", 45); // 15 + 30
    assertInt("Set{10,20,30}.max() - Set{1,2,3}.min()", 29); // 30 - 1
  }

  /**
   * Tests complex expressions combining transformations and aggregations.
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li>{@code Set{-5,-10,15,20}.abs().max()} → {@code [20]}
   *   <li>{@code Set{-1,-2,-3}.abs().sum()} → {@code [6]}
   *   <li>{@code Sequence{5,10,15,20}.sum() / Set{2,4}.max()} → {@code [12]} (50/4)
   * </ul>
   *
   * <p><b>Validates:</b> Complex operation chaining works correctly.
   */
  @Test
  public void testComplexArithmetic() {
    assertInt("Set{-5,-10,15,20}.abs().max()", 20);
    assertInt("Set{-1,-2,-3}.abs().sum()", 6);
    assertInt("Sequence{5,10,15,20}.sum() / Set{2,4}.max()", 12); // 50 / 4
  }

  // ==================== Helper Methods ====================

  /**
   * Compiles and evaluates an OCL arithmetic expression through the complete pipeline.
   *
   * <p>Orchestrates the three-phase compilation:
   *
   * <ol>
   *   <li><b>Parsing:</b> Converts input to parse tree
   *   <li><b>Type Checking:</b> Validates arithmetic types and operations
   *   <li><b>Evaluation:</b> Computes numeric results
   * </ol>
   *
   * @param input The OCL arithmetic expression to compile
   * @return The evaluated result as a {@link Value}
   */
  private Value compile(String input) {
    ParseTree tree = parse(input);

    // Dummy specification (no metamodels needed for arithmetic)
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

    // Initialize 3-pass architecture
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    // Pass 1: Symbol Table Construction
    SymbolTableBuilder symbolTableBuilder =
        new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator);
    symbolTableBuilder.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 1 (Symbol Table) failed: " + errors.getErrors());
    }

    // Pass 2: Type Checking
    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    typeChecker.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 2 (Type Checking) failed: " + errors.getErrors());
    }

    // Pass 3: Evaluation
    EvaluationVisitor evaluator =
        new EvaluationVisitor(symbolTable, dummySpec, errors, typeChecker.getNodeTypes());
    Value result = evaluator.visit(tree);

    if (errors.hasErrors()) {
      fail("Pass 3 (Evaluation) failed: " + errors.getErrors());
    }

    return result;
  }

  /**
   * Parses an OCL expression into an ANTLR parse tree.
   *
   * <p>Uses {@code infixedExpCS} entry point for arithmetic and infix expressions.
   *
   * @param input The OCL expression string
   * @return The parse tree
   */
  private ParseTree parse(String input) {
    VitruvOCLLexer lexer = new VitruvOCLLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    VitruvOCLParser parser = new VitruvOCLParser(tokens);
    return parser.infixedExpCS();
  }

  /**
   * Assertion helper for singleton Integer results.
   *
   * <p>Validates that:
   *
   * <ul>
   *   <li>Result is singleton collection
   *   <li>Element is Integer type
   *   <li>Value matches expected
   * </ul>
   *
   * @param input OCL expression to evaluate
   * @param expected Expected integer value
   */
  private void assertInt(String input, int expected) {
    Value result = compile(input);
    assertEquals(1, result.size(), "Result should be singleton");
    OCLElement elem = result.getElements().get(0);
    assertEquals(
        expected,
        ((OCLElement.IntValue) elem).value(),
        "Expected " + expected + " but got " + ((OCLElement.IntValue) elem).value());
  }

  /**
   * Assertion helper for empty collection results.
   *
   * <p>Validates that result collection has size 0.
   *
   * @param input OCL expression to evaluate
   */
  private void assertEmpty(String input) {
    Value result = compile(input);
    assertEquals(0, result.size(), "Result should be empty");
  }

  /**
   * Assertion helper for collections of integers.
   *
   * <p>Validates that:
   *
   * <ul>
   *   <li>Result has expected number of elements
   *   <li>Elements match expected values (order-independent)
   * </ul>
   *
   * <p><b>Note:</b> Comparison is order-independent (both actual and expected are sorted).
   *
   * @param input OCL expression to evaluate
   * @param expected Expected integer values (varargs)
   */
  private void assertCollection(String input, int... expected) {
    Value result = compile(input);
    assertEquals(expected.length, result.size(), "Collection size mismatch");

    List<Integer> actual =
        result.getElements().stream().map(e -> ((OCLElement.IntValue) e).value()).sorted().toList();

    List<Integer> expectedList = java.util.Arrays.stream(expected).boxed().sorted().toList();

    assertEquals(expectedList, actual, "Collection content mismatch");
  }
}
