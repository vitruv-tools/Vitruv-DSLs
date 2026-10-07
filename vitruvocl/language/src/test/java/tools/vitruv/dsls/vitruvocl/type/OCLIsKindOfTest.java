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

package tools.vitruv.dsls.vitruvocl.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
 * Comprehensive test suite for the {@code oclIsKindOf} type checking operation in VitruvOCL.
 *
 * @see Value Runtime collection representation
 * @see tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor Evaluates oclIsKindOf operations
 * @see tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor Type checks oclIsKindOf expressions
 */
class OCLIsKindOfTest extends DummyTestSpecification {

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
  void testSingleElementIsKindOfSelf(String expression) {
    assertSingleBool(compile(expression), true);
  }

  static Stream<String> singleElementTrueExpressions() {
    return Stream.of(
        "Set{5}.oclIsKindOf(Integer)",
        "Set{\"hello\"}.oclIsKindOf(String)", "Set{true}.oclIsKindOf(Boolean)");
  }

  @ParameterizedTest
  @MethodSource("singleElementFalseExpressions")
  void testSingleElementIsNotKindOf(String expression) {
    assertSingleBool(compile(expression), false);
  }

  static Stream<String> singleElementFalseExpressions() {
    return Stream.of(
        "Set{5}.oclIsKindOf(String)",
        "Set{5}.oclIsKindOf(Boolean)",
        "Set{\"hello\"}.oclIsKindOf(Integer)",
        "Set{\"hello\"}.oclIsKindOf(Boolean)",
        "Set{true}.oclIsKindOf(Integer)",
        "Set{false}.oclIsKindOf(String)");
  }

  // ==================== Multiple Elements ====================

  @ParameterizedTest
  @MethodSource("multipleElementExpressions")
  void testMultipleElementsKindOf(String expr, int expectedSize, boolean expectedAll) {
    Value result = compile(expr);
    assertSize(result, expectedSize);
    for (OCLElement elem : result.getElements()) {
      assertEquals(expectedAll, ((OCLElement.BoolValue) elem).value());
    }
  }

  static Stream<Arguments> multipleElementExpressions() {
    return Stream.of(
        Arguments.of("Set{1, 2, 3}.oclIsKindOf(Integer)", 3, true),
        Arguments.of("Set{1, 2, 3}.oclIsKindOf(String)", 3, false),
        Arguments.of("Set{\"a\", \"b\", \"c\"}.oclIsKindOf(String)", 3, true),
        Arguments.of("Set{true, false, true}.oclIsKindOf(Boolean)", 2, true),
        Arguments.of("Sequence{1, 2, 3}.oclIsKindOf(Integer)", 3, true));
  }

  // ==================== Empty Collection ====================

  /** Tests empty collection → empty result. */
  @Test
  void testEmptyCollectionIsKindOf() {
    assertSize(compile("Set{}.oclIsKindOf(Integer)"), 0);
  }

  // ==================== Type Checking ====================

  /** Tests type checker infers Collection(Boolean) as result type. */
  @Test
  void testTypeCheckReturnsBoolean() {
    ParseTree tree = parse("Set{5}.oclIsKindOf(Integer)");
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
    ParseTree tree = parse("Sequence{1, 2}.oclIsKindOf(Integer)");
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
    Value result = compile("Sequence{1, \"hello\", true}.oclIsKindOf(Integer)");
    assertSize(result, 3);
    List<OCLElement> elements = result.getElements();
    assertTrue(((OCLElement.BoolValue) elements.get(0)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(1)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
  }

  /**
   * Tests mixed types checking for String: {1,"hello",true,"world"} → {false,true,false,true}.
   */
  @Test
  void testMixedTypesCheckingForString() {
    Value result = compile("Sequence{1, \"hello\", true, \"world\"}.oclIsKindOf(String)");
    assertSize(result, 4);
    List<OCLElement> elements = result.getElements();
    assertFalse(((OCLElement.BoolValue) elements.get(0)).value());
    assertTrue(((OCLElement.BoolValue) elements.get(1)).value());
    assertFalse(((OCLElement.BoolValue) elements.get(2)).value());
    assertTrue(((OCLElement.BoolValue) elements.get(3)).value());
  }

  /** Tests {1,"test",true}.oclIsKindOf(Boolean) → exactly one true. */
  @Test
  void testAllDifferentTypesCheckBoolean() {
    Value result = compile("Set{1, \"test\", true}.oclIsKindOf(Boolean)");
    assertSize(result, 3);
    int trueCount = 0;
    for (OCLElement elem : result.getElements()) {
      if (((OCLElement.BoolValue) elem).value()) {
        trueCount++;
      }
    }
    assertEquals(1, trueCount, "Exactly one element should be Boolean");
  }

  /** Tests all Strings checked against Integer → all false. */
  @Test
  void testEmptyResultFromMixedCollection() {
    Value result = compile("Set{\"hello\", \"world\", \"test\"}.oclIsKindOf(Integer)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertFalse(((OCLElement.BoolValue) elem).value());
    }
  }

  /**
   * Tests flatten then oclIsKindOf on nested mixed collection →
   * {true,true,false,false,false,false}.
   */
  @Test
  void testNestedCollectionsWithMixedTypes() {
    Value result =
        compile(
            "Sequence{Set{1, 2}, Set{\"a\", \"b\"}, Set{true,"
                + " false}}.flatten().oclIsKindOf(Integer)");
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
  @MethodSource("metamodelKindOfSatisfiedConstraints")
  void testMetamodelKindOfConstraintSatisfied(String constraint) {
    ConstraintResult r =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});
    assertTrue(r.isSuccess(), "Evaluation should succeed");
    assertTrue(r.isSatisfied());
  }

  static Stream<String> metamodelKindOfSatisfiedConstraints() {
    return Stream.of(
        """
context spaceMission::Spacecraft inv kindOfSpacecraft:
  spaceMission::Spacecraft.allInstances().oclIsKindOf(spaceMission::Spacecraft).select(b | b).size()
    == spaceMission::Spacecraft.allInstances().size()
        """,
        """
context spaceMission::Spacecraft inv kindOfSatellite:
  spaceMission::Spacecraft.allInstances()
    .oclIsKindOf(satelliteSystem::Satellite).select(b | b).size()
    == 0
        """,
        """
context satelliteSystem::Satellite inv kindOfSatellite:
  satelliteSystem::Satellite.allInstances()
    .oclIsKindOf(satelliteSystem::Satellite).select(b | b).size()
    == satelliteSystem::Satellite.allInstances().size()
        """);
  }

  /** Tests oclIsKindOf used inside select iterator (uses extra instance file SPACECRAFT_ATLAS). */
  @Test
  void testOclIsKindOfUsedInSelect() {
    String constraint =
        """
        context spaceMission::Spacecraft inv kindOfInSelect:
          spaceMission::Spacecraft.allInstances().select(sc |
            sc.oclIsKindOf(spaceMission::Spacecraft)
          ).size() > 0
        """;
    ConstraintResult r =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER});
    assertTrue(r.isSuccess(), "Evaluation should succeed: " + r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Should find Spacecraft instances via oclIsKindOf");
  }

  // ==================== Entry Point Override ====================

  /** Overrides parse entry point to use {@code infixedExpCS()} for oclIsKindOf expressions. */
  @Override
  protected ParseTree parse(String input) {
    CommonTokenStream tokens =
        new CommonTokenStream(new VitruvOCLLexer(CharStreams.fromString(input)));
    return new VitruvOCLParser(tokens).infixedExpCS();
  }
}
