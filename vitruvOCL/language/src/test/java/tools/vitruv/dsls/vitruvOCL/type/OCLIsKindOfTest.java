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
 * Comprehensive test suite for the {@code oclIsKindOf} type checking operation in VitruvOCL.
 *
 * @see Value Runtime collection representation
 * @see tools.vitruv.dsls.vitruvOCL.evaluator.EvaluationVisitor Evaluates oclIsKindOf operations
 * @see tools.vitruv.dsls.vitruvOCL.typechecker.TypeCheckVisitor Type checks oclIsKindOf expressions
 */
class OCLIsKindOfTest extends DummyTestSpecification {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ATLAS = Path.of("spacecraft-atlas.spacemission");
  private static final Path SATELLITE_VOYAGER = Path.of("satellite-voyager.satellitesystem");

  // ==================== Integer Type Checking ====================

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  /** Tests Integer is kind of Integer → {@code [true]}. */
  @Test
  void testIntegerIsKindOfInteger() {
    assertSingleBool(compile("Set{5}.oclIsKindOf(Integer)"), true);
  }

  /** Tests Integer is NOT kind of String → {@code [false]}. */
  @Test
  void testIntegerIsKindOfString() {
    assertSingleBool(compile("Set{5}.oclIsKindOf(String)"), false);
  }

  /** Tests Integer is NOT kind of Boolean → {@code [false]}. */
  @Test
  void testIntegerIsKindOfBoolean() {
    assertSingleBool(compile("Set{5}.oclIsKindOf(Boolean)"), false);
  }

  // ==================== String Type Checking ====================

  /** Tests String is kind of String → {@code [true]}. */
  @Test
  void testStringIsKindOfString() {
    assertSingleBool(compile("Set{\"hello\"}.oclIsKindOf(String)"), true);
  }

  /** Tests String is NOT kind of Integer → {@code [false]}. */
  @Test
  void testStringIsKindOfInteger() {
    assertSingleBool(compile("Set{\"hello\"}.oclIsKindOf(Integer)"), false);
  }

  /** Tests String is NOT kind of Boolean → {@code [false]}. */
  @Test
  void testStringIsKindOfBoolean() {
    assertSingleBool(compile("Set{\"hello\"}.oclIsKindOf(Boolean)"), false);
  }

  // ==================== Boolean Type Checking ====================

  /** Tests Boolean is kind of Boolean → {@code [true]}. */
  @Test
  void testBooleanIsKindOfBoolean() {
    assertSingleBool(compile("Set{true}.oclIsKindOf(Boolean)"), true);
  }

  /** Tests Boolean is NOT kind of Integer → {@code [false]}. */
  @Test
  void testBooleanIsKindOfInteger() {
    assertSingleBool(compile("Set{true}.oclIsKindOf(Integer)"), false);
  }

  /** Tests Boolean is NOT kind of String → {@code [false]}. */
  @Test
  void testBooleanIsKindOfString() {
    assertSingleBool(compile("Set{false}.oclIsKindOf(String)"), false);
  }

  // ==================== Multiple Elements ====================

  /** Tests all Integer elements → all true: {@code Set{1,2,3}.oclIsKindOf(Integer)}. */
  @Test
  void testMultipleIntegersIsKindOfInteger() {
    Value result = compile("Set{1, 2, 3}.oclIsKindOf(Integer)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  /** Tests Integer elements checked against String → all false. */
  @Test
  void testMultipleIntegersIsKindOfString() {
    Value result = compile("Set{1, 2, 3}.oclIsKindOf(String)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertFalse(((OCLElement.BoolValue) elem).value());
    }
  }

  /** Tests all String elements → all true. */
  @Test
  void testMultipleStringsIsKindOfString() {
    Value result = compile("Set{\"a\", \"b\", \"c\"}.oclIsKindOf(String)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  /** Tests Boolean Set with duplicates: {true,false,true} → 2 elements, both true. */
  @Test
  void testMultipleBooleansIsKindOfBoolean() {
    Value result = compile("Set{true, false, true}.oclIsKindOf(Boolean)");
    assertSize(result, 2);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
  }

  // ==================== Empty Collection ====================

  /** Tests empty collection → empty result. */
  @Test
  void testEmptyCollectionIsKindOf() {
    assertSize(compile("Set{}.oclIsKindOf(Integer)"), 0);
  }

  // ==================== Sequence Preservation ====================

  /** Tests Sequence order preserved: {@code Sequence{1,2,3}.oclIsKindOf(Integer)} → all true. */
  @Test
  void testSequencePreservesOrder() {
    Value result = compile("Sequence{1, 2, 3}.oclIsKindOf(Integer)");
    assertSize(result, 3);
    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value());
    }
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

  /** Tests mixed types checking for String: {1,"hello",true,"world"} → {false,true,false,true}. */
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
      if (((OCLElement.BoolValue) elem).value()) trueCount++;
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

  /** Tests Spacecraft instance is kind of Spacecraft → {@code [true]}. */
  @Test
  void testSpacecraftIsKindOfSpacecraft() {
    String constraint =
        """
context spaceMission::Spacecraft inv kindOfSpacecraft:
  spaceMission::Spacecraft.allInstances().oclIsKindOf(spaceMission::Spacecraft).select(b | b).size() == spaceMission::Spacecraft.allInstances().size()
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Spacecraft instances should be kind of Spacecraft");
  }

  /** Tests Spacecraft instance is NOT kind of Satellite → all false. */
  @Test
  void testSpacecraftIsNotKindOfSatellite() {
    String constraint =
        """
context spaceMission::Spacecraft inv kindOfSatellite:
  spaceMission::Spacecraft.allInstances().oclIsKindOf(satelliteSystem::Satellite).select(b | b).size() == 0
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Spacecraft is not kind of Satellite");
  }

  /** Tests Satellite instances are kind of Satellite → all true. */
  @Test
  void testSatelliteIsKindOfSatellite() {
    String constraint =
        """
context satelliteSystem::Satellite inv kindOfSatellite:
  satelliteSystem::Satellite.allInstances().oclIsKindOf(satelliteSystem::Satellite).select(b | b).size() == satelliteSystem::Satellite.allInstances().size()
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Satellite instances should be kind of Satellite");
  }

  /**
   * Tests oclIsKindOf used inside select iterator. Only Spacecraft instances should pass
   * oclIsKindOf(Spacecraft).
   */
  @Test
  void testOclIsKindOfUsedInSelect() {
    String constraint =
        """
        context spaceMission::Spacecraft inv kindOfInSelect:
          spaceMission::Spacecraft.allInstances().select(sc |
            sc.oclIsKindOf(spaceMission::Spacecraft)
          ).size() > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Should find Spacecraft instances via oclIsKindOf");
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
