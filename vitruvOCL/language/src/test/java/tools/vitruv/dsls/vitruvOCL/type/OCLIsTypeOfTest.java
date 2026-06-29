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
package tools.vitruv.dsls.vitruvOCL.type;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.DummyTestSpecification;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLLexer;
import tools.vitruv.dsls.vitruvOCL.VitruvOCLParser;
import tools.vitruv.dsls.vitruvOCL.common.ErrorCollector;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;
import tools.vitruv.dsls.vitruvOCL.symboltable.ScopeAnnotator;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTable;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableBuilder;
import tools.vitruv.dsls.vitruvOCL.symboltable.SymbolTableImpl;
import tools.vitruv.dsls.vitruvOCL.typechecker.Type;
import tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor;

/**
 * Comprehensive test suite for the {@code oclIsTypeOf} type checking operation in VitruvOCL.
 *
 * <p>Unlike {@code oclIsKindOf}, {@code oclIsTypeOf} checks for exact type match (no subtype
 * inheritance).
 *
 * @see Value Runtime collection representation
 * @see tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor Evaluates oclIsTypeOf operations
 * @see tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor Type checks oclIsTypeOf expressions
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
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  // ==================== Integer Type Checking ====================

  /** Tests Integer is type of Integer → {@code [true]}. */
  @Test
  void testIntegerIsTypeOfInteger() {
    assertSingleBool(compile("Set{5}.oclIsTypeOf(Integer)"), true);
  }

  /** Tests Integer is NOT type of String → {@code [false]}. */
  @Test
  void testIntegerIsTypeOfString() {
    assertSingleBool(compile("Set{5}.oclIsTypeOf(String)"), false);
  }

  /** Tests Integer is NOT type of Boolean → {@code [false]}. */
  @Test
  void testIntegerIsTypeOfBoolean() {
    assertSingleBool(compile("Set{5}.oclIsTypeOf(Boolean)"), false);
  }

  // ==================== String Type Checking ====================

  /** Tests String is type of String → {@code [true]}. */
  @Test
  void testStringIsTypeOfString() {
    assertSingleBool(compile("Set{\"hello\"}.oclIsTypeOf(String)"), true);
  }

  /** Tests String is NOT type of Integer → {@code [false]}. */
  @Test
  void testStringIsTypeOfInteger() {
    assertSingleBool(compile("Set{\"hello\"}.oclIsTypeOf(Integer)"), false);
  }

  /** Tests String is NOT type of Boolean → {@code [false]}. */
  @Test
  void testStringIsTypeOfBoolean() {
    assertSingleBool(compile("Set{\"hello\"}.oclIsTypeOf(Boolean)"), false);
  }

  // ==================== Boolean Type Checking ====================

  /** Tests Boolean is type of Boolean → {@code [true]}. */
  @Test
  void testBooleanIsTypeOfBoolean() {
    assertSingleBool(compile("Set{true}.oclIsTypeOf(Boolean)"), true);
  }

  /** Tests Boolean is NOT type of Integer → {@code [false]}. */
  @Test
  void testBooleanIsTypeOfInteger() {
    assertSingleBool(compile("Set{true}.oclIsTypeOf(Integer)"), false);
  }

  /** Tests Boolean is NOT type of String → {@code [false]}. */
  @Test
  void testBooleanIsTypeOfString() {
    assertSingleBool(compile("Set{false}.oclIsTypeOf(String)"), false);
  }

  // ==================== Multiple Elements ====================

  /** Tests all Integer elements → all true: {@code Set{1,2,3}.oclIsTypeOf(Integer)}. */
  @Test
  void testMultipleIntegersIsTypeOfInteger() {
    Value result = compile("Set{1, 2, 3}.oclIsTypeOf(Integer)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  /** Tests Integer elements checked against String → all false. */
  @Test
  void testMultipleIntegersIsTypeOfString() {
    Value result = compile("Set{1, 2, 3}.oclIsTypeOf(String)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertFalse(((OCLElement.BoolValue) elem).value());
    }
  }

  /** Tests all String elements → all true. */
  @Test
  void testMultipleStringsIsTypeOfString() {
    Value result = compile("Set{\"a\", \"b\", \"c\"}.oclIsTypeOf(String)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  /** Tests Boolean Set with duplicates: {true,false,true} → 2 elements, both true. */
  @Test
  void testMultipleBooleansIsTypeOfBoolean() {
    Value result = compile("Set{true, false, true}.oclIsTypeOf(Boolean)");
    assertSize(result, 2);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  // ==================== Empty Collection ====================

  /** Tests empty collection → empty result. */
  @Test
  void testEmptyCollectionIsTypeOf() {
    assertSize(compile("Set{}.oclIsTypeOf(Integer)"), 0);
  }

  // ==================== Sequence Preservation ====================

  /** Tests Sequence order preserved: {@code Sequence{1,2,3}.oclIsTypeOf(Integer)} → all true. */
  @Test
  void testSequencePreservesOrder() {
    Value result = compile("Sequence{1, 2, 3}.oclIsTypeOf(Integer)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
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

  /** Tests Spacecraft instance is exactly of type Spacecraft → {@code [true]}. */
  @Test
  void testSpacecraftIsTypeOfSpacecraft() {
    String constraint =
        """
context spaceMission::Spacecraft inv typeOfSpacecraft:
  spaceMission::Spacecraft.allInstances().oclIsTypeOf(spaceMission::Spacecraft).forAll(b | b)
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Spacecraft instances should be exactly of type Spacecraft");
  }

  /** Tests Spacecraft instance is NOT exactly of type Satellite → all false, constraint fails. */
  @Test
  void testSpacecraftIsNotTypeOfSatellite() {
    String constraint =
        """
context spaceMission::Spacecraft inv typeOfSatellite:
  spaceMission::Spacecraft.allInstances().oclIsTypeOf(satelliteSystem::Satellite).forAll(b | b)
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertFalse(result.isSatisfied(), "Spacecraft is not of type Satellite");
  }

  /** Tests Satellite instances are exactly of type Satellite → all true. */
  @Test
  void testSatelliteIsTypeOfSatellite() {
    String constraint =
        """
context satelliteSystem::Satellite inv typeOfSatellite:
  satelliteSystem::Satellite.allInstances().oclIsTypeOf(satelliteSystem::Satellite).forAll(b | b)
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Satellite instances should be exactly of type Satellite");
  }

  /**
   * Tests mixed allInstances() results filtered by oclIsTypeOf. Only Spacecraft instances should
   * pass oclIsTypeOf(Spacecraft).
   */
  @Test
  void testOclIsTypeOfUsedInSelect() {
    String constraint =
        """
        context spaceMission::Spacecraft inv typeOfInSelect:
          spaceMission::Spacecraft.allInstances().select(sc |
            sc.oclIsTypeOf(spaceMission::Spacecraft)
          ).size() > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Should find Spacecraft instances via oclIsTypeOf");
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
