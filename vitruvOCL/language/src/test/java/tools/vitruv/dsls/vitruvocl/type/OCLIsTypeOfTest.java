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
package tools.vitruv.dsls.vitruvocl.type;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;
import tools.vitruv.dsls.vitruvocl.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvocl.VitruvOCLParser;
import tools.vitruv.dsls.vitruvocl.common.ErrorCollector;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.dsls.vitruvocl.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvocl.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;
import tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for the {@code oclIsTypeOf} type checking operation in VitruvOCL.
 *
 * <p>Unlike {@code oclIsKindOf}, {@code oclIsTypeOf} checks for exact type match (no subtype
 * inheritance).
 *
 * @see Value Runtime collection representation
 * @see tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor Evaluates oclIsTypeOf operations
 * @see tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor Type checks oclIsTypeOf expressions
 */
class OCLIsTypeOfTest extends DummyTestSpecification {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ATLAS = Path.of("spacecraft-atlas.spacemission");
  private static final Path SATELLITE_VOYAGER = Path.of("satellite-voyager.satellitesystem");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  // ==================== Single-element type checking ====================

  @ParameterizedTest
  @MethodSource("singleElementTrueExpressions")
  void testSingleElementIsTypeOfSelf(String expression) {
    assertSingleBool(compile(expression), true);
  }

  static Stream<String> singleElementTrueExpressions() {
    return Stream.of(
        "Set{5}.oclIsTypeOf(Integer)",
        "Set{\"hello\"}.oclIsTypeOf(String)",
        "Set{true}.oclIsTypeOf(Boolean)"
    );
  }

  @ParameterizedTest
  @MethodSource("singleElementFalseExpressions")
  void testSingleElementIsNotTypeOf(String expression) {
    assertSingleBool(compile(expression), false);
  }

  static Stream<String> singleElementFalseExpressions() {
    return Stream.of(
        "Set{5}.oclIsTypeOf(String)",
        "Set{5}.oclIsTypeOf(Boolean)",
        "Set{\"hello\"}.oclIsTypeOf(Integer)",
        "Set{\"hello\"}.oclIsTypeOf(Boolean)",
        "Set{true}.oclIsTypeOf(Integer)",
        "Set{false}.oclIsTypeOf(String)"
    );
  }

  // ==================== Multiple Elements ====================

  @ParameterizedTest
  @MethodSource("multipleElementExpressions")
  void testMultipleElementsTypeOf(String expr, int expectedSize, boolean expectedAll) {
    Value result = compile(expr);
    assertSize(result, expectedSize);
    for (OCLElement elem : result.getElements()) {
      assertEquals(expectedAll, ((OCLElement.BoolValue) elem).value());
    }
  }

  static Stream<Arguments> multipleElementExpressions() {
    return Stream.of(
        Arguments.of("Set{1, 2, 3}.oclIsTypeOf(Integer)", 3, true),
        Arguments.of("Set{1, 2, 3}.oclIsTypeOf(String)", 3, false),
        Arguments.of("Set{\"a\", \"b\", \"c\"}.oclIsTypeOf(String)", 3, true),
        Arguments.of("Set{true, false, true}.oclIsTypeOf(Boolean)", 2, true),
        Arguments.of("Sequence{1, 2, 3}.oclIsTypeOf(Integer)", 3, true)
    );
  }

  // ==================== Empty Collection ====================

  /** Tests empty collection → empty result. */
  @Test
  void testEmptyCollectionIsTypeOf() {
    assertSize(compile("Set{}.oclIsTypeOf(Integer)"), 0);
  }

  // ==================== Type Checking ====================

  /** Tests type checker infers Collection(Boolean) as result type. */
  @Test
  void testTypeCheckReturnsBoolean() {
    ParseTree tree = parse("Set{5}.oclIsTypeOf(Integer)");
    MetamodelWrapperInterface dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator).visit(tree);
    assertFalse(errors.hasErrors(), "Pass 1 should not have errors");

    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    Type resultType = typeChecker.visit(tree);

    assertFalse(typeChecker.hasErrors());
    assertTrue(resultType.isCollection());
    assertEquals(Type.BOOLEAN, resultType.getElementType());
  }

  /** Tests type checker preserves Sequence collection kind. */
  @Test
  void testTypeCheckPreservesCollectionKind() {
    ParseTree tree = parse("Sequence{1, 2}.oclIsTypeOf(Integer)");
    MetamodelWrapperInterface dummySpec = buildDummySpec();
    SymbolTable symbolTable = new SymbolTableImpl(dummySpec);
    ScopeAnnotator scopeAnnotator = new ScopeAnnotator();
    ErrorCollector errors = new ErrorCollector();

    new SymbolTableBuilder(symbolTable, dummySpec, errors, scopeAnnotator).visit(tree);
    assertFalse(errors.hasErrors(), "Pass 1 should not have errors");

    TypeCheckVisitor typeChecker =
        new TypeCheckVisitor(symbolTable, dummySpec, errors, scopeAnnotator);
    Type resultType = typeChecker.visit(tree);

    assertFalse(typeChecker.hasErrors());
    assertTrue(resultType.isCollection());
    assertTrue(resultType.isOrdered());
    assertEquals(Type.BOOLEAN, resultType.getElementType());
  }

  // ==================== Mixed Type Collections ====================

  /** Tests mixed types checking for Integer: {1,"hello",true} → {true,false,false}. */
  @Test
  void testMixedTypesInCollection() {
    Value result = compile("Sequence{1, \"hello\", true}.oclIsTypeOf(Integer)");
    assertSize(result, 3);
    List<OCLElement> elements = result.getElements();
    assertTrue(((OCLElement.BoolValue) elements.get(0)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(1)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
  }

  /** Tests mixed types checking for String: {1,"hello",true,"world"} → {false,true,false,true}. */
  @Test
  void testMixedTypesCheckingForString() {
    Value result = compile("Sequence{1, \"hello\", true, \"world\"}.oclIsTypeOf(String)");
    assertSize(result, 4);
    List<OCLElement> elements = result.getElements();
    assertFalse(((OCLElement.BoolValue) elements.get(0)).value());
    assertTrue(((OCLElement.BoolValue) elements.get(1)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
    assertTrue(((OCLElement.BoolValue) elements.get(3)).value());
  }

  /** Tests {1,"test",true}.oclIsTypeOf(Boolean) → exactly one true. */
  @Test
  void testAllDifferentTypesCheckBoolean() {
    Value result = compile("Set{1, \"test\", true}.oclIsTypeOf(Boolean)");
    assertSize(result, 3);
    int trueCount = 0;
    for (OCLElement elem : result.getElements()) {
      if (((OCLElement.BoolValue) elem).value()) trueCount++;
    }
    assertEquals(1, trueCount, "Exactly one element should be Boolean");
  }

  /** Tests all Strings checked against Integer → all false. */
  @Test
  void testEmptyResultFromMixedCollection() {
    Value result = compile("Set{\"hello\", \"world\", \"test\"}.oclIsTypeOf(Integer)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertFalse(((OCLElement.BoolValue) elem).value());
    }
  }

  /**
   * Tests flatten then oclIsTypeOf on nested mixed collection →
   * {true,true,false,false,false,false}.
   */
  @Test
  void testNestedCollectionsWithMixedTypes() {
    Value result =
        compile(
            "Sequence{Set{1, 2}, Set{\"a\", \"b\"}, Set{true,"
                + " false}}.flatten().oclIsTypeOf(Integer)");
    assertSize(result, 6);
    List<OCLElement> elements = result.getElements();
    assertTrue(((OCLElement.BoolValue) elements.get(0)).value());
    assertTrue(((OCLElement.BoolValue) elements.get(1)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(3)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(4)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(5)).value());
  }

  // ==================== Metamodel Type Checking ====================

  @ParameterizedTest
  @MethodSource("metamodelTypeOfSatisfiedConstraints")
  void testMetamodelTypeOfConstraintSatisfied(String constraint) {
    ConstraintResult r = VitruvOCL.evaluateConstraint(
        constraint,
        new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
        new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});
    assertTrue(r.isSuccess(), "Evaluation should succeed");
    assertTrue(r.isSatisfied());
  }

  static Stream<String> metamodelTypeOfSatisfiedConstraints() {
    return Stream.of(
        """
context spaceMission::Spacecraft inv typeOfSpacecraft:
  spaceMission::Spacecraft.allInstances().oclIsTypeOf(spaceMission::Spacecraft).forAll(b | b)
""",
        """
context satelliteSystem::Satellite inv typeOfSatellite:
  satelliteSystem::Satellite.allInstances().oclIsTypeOf(satelliteSystem::Satellite).forAll(b | b)
"""
    );
  }

  /** Spacecraft is NOT exactly of type Satellite → constraint fails. */
  @Test
  void testSpacecraftIsNotTypeOfSatellite() {
    String constraint =
        """
context spaceMission::Spacecraft inv typeOfSatellite:
  spaceMission::Spacecraft.allInstances().oclIsTypeOf(satelliteSystem::Satellite).forAll(b | b)
""";
    ConstraintResult r = VitruvOCL.evaluateConstraint(
        constraint,
        new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
        new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});
    assertTrue(r.isSuccess(), "Evaluation should succeed");
    assertFalse(r.isSatisfied(), "Spacecraft is not of type Satellite");
  }

  /** Tests oclIsTypeOf used inside select iterator (uses extra instance file SPACECRAFT_ATLAS). */
  @Test
  void testOclIsTypeOfUsedInSelect() {
    String constraint =
        """
        context spaceMission::Spacecraft inv typeOfInSelect:
          spaceMission::Spacecraft.allInstances().select(sc |
            sc.oclIsTypeOf(spaceMission::Spacecraft)
          ).size() > 0
        """;
    ConstraintResult r = VitruvOCL.evaluateConstraint(
        constraint,
        new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
        new Path[] {SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER});
    assertTrue(r.isSuccess(), "Evaluation should succeed");
    assertTrue(r.isSatisfied(), "Should find Spacecraft instances via oclIsTypeOf");
  }

  /**
   * Tests oclIsTypeOf as select predicate returning Boolean body. select(p |
   * p.oclIsTypeOf(Integer)) should filter correctly.
   */
  @Test
  void testOclIsTypeOfAsSelectPredicate() {
    Value result = compile("Sequence{1, \"hello\", true}.select(p | p.oclIsTypeOf(Integer))");
    assertSize(result, 1);
    assertEquals(1, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  /**
   * Tests oclIsTypeOf as select predicate followed by size(). Mirrors the brakedisk pattern:
   * collection.select(p | p.oclIsTypeOf(T)).size() > 0
   */
  @Test
  void testOclIsTypeOfSelectThenSize() {
    Value result =
        compile("Sequence{1, \"hello\", true, 2}.select(p | p.oclIsTypeOf(Integer)).size()");
    assertSingleInt(result, 2);
  }

  // ==================== Entry Point Override ====================

  /** Overrides parse entry point to use {@code infixedExpCS()} for oclIsTypeOf expressions. */
  @Override
  protected ParseTree parse(String input) {
    CommonTokenStream tokens = new CommonTokenStream(new VitruvOCLLexer(CharStreams.fromString(input)));
    return new VitruvOCLParser(tokens).infixedExpCS();
  }

}
