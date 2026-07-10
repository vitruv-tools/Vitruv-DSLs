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
 * Comprehensive test suite for the {@code oclAsType} cast operation in VitruvOCL.
 *
 * <p>Unlike {@code oclIsKindOf}/{@code oclIsTypeOf}, {@code oclAsType} casts elements to a target
 * type, preserving the collection structure but changing the element type annotation.
 *
 * @see Value Runtime collection representation
 * @see tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor Evaluates oclAsType operations
 * @see tools.vitruv.dsls.vitruvocl.typechecker.TypeCheckVisitor Type checks oclAsType expressions
 */
class OCLAsTypeTest extends DummyTestSpecification {

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

  /** Tests casting Integer collection to Integer → same elements preserved. */
  @Test
  void testIntegerAsTypeInteger() {
    Value result = compile("Set{5}.oclAsType(Integer)");
    assertSize(result, 1);
    assertEquals(5, ((OCLElement.IntValue) result.getElements().get(0)).value());
  }

  @ParameterizedTest
  @MethodSource("threeElementCastExpressions")
  void testThreeElementCast(String expr) {
    assertSize(compile(expr), 3);
  }

  static Stream<String> threeElementCastExpressions() {
    return Stream.of(
        "Set{1, 2, 3}.oclAsType(Integer)",
        "Set{\"a\", \"b\", \"c\"}.oclAsType(String)",
        "Sequence{1, 2, 3}.collect(p | p.oclAsType(Integer))");
  }

  // ==================== String Cast ====================

  /** Tests casting String collection to String → same elements preserved. */
  @Test
  void testStringAsTypeString() {
    Value result = compile("Set{\"hello\"}.oclAsType(String)");
    assertSize(result, 1);
    assertEquals("hello", ((OCLElement.StringValue) result.getElements().get(0)).value());
  }

  // ==================== Boolean Cast ====================

  /** Tests casting Boolean collection to Boolean → same elements preserved. */
  @Test
  void testBooleanAsTypeBoolean() {
    Value result = compile("Set{true}.oclAsType(Boolean)");
    assertSize(result, 1);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  // ==================== Empty Collection ====================

  /** Tests casting empty collection → empty result. */
  @Test
  void testEmptyCollectionAsType() {
    assertSize(compile("Set{}.oclAsType(Integer)"), 0);
  }

  // ==================== Sequence Preservation ====================

  /** Tests Sequence order preserved after cast. */
  @Test
  void testSequencePreservesOrder() {
    Value result = compile("Sequence{1, 2, 3}.oclAsType(Integer)");
    assertSize(result, 3);
    List<OCLElement> elements = result.getElements();
    assertEquals(1, ((OCLElement.IntValue) elements.get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) elements.get(1)).value());
    assertEquals(3, ((OCLElement.IntValue) elements.get(2)).value());
  }

  // ==================== Type Checking ====================

  /** Tests type checker infers Collection(Integer) as result type. */
  @Test
  void testTypeCheckReturnsInteger() {
    ParseTree tree = parse("Set{5}.oclAsType(Integer)");
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
    assertEquals(Type.INTEGER, resultType.getElementType());
  }

  /** Tests type checker preserves Sequence collection kind. */
  @Test
  void testTypeCheckPreservesCollectionKind() {
    ParseTree tree = parse("Sequence{1, 2}.oclAsType(Integer)");
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
    assertEquals(Type.INTEGER, resultType.getElementType());
  }

  // ==================== Pipeline: select + oclAsType ====================

  /**
   * Tests oclIsKindOf filter followed by oclAsType cast. Mirrors the typical use pattern: filter by
   * type, then cast for property access.
   */
  @Test
  void testSelectKindOfThenAsType() {
    Value result =
        compile("Sequence{1, \"hello\", 2}.select(p | p.oclIsKindOf(Integer)).oclAsType(Integer)");
    assertSize(result, 2);
    List<OCLElement> elements = result.getElements();
    assertEquals(1, ((OCLElement.IntValue) elements.get(0)).value());
    assertEquals(2, ((OCLElement.IntValue) elements.get(1)).value());
  }

  // ==================== Metamodel Type Checking ====================

  /** Tests casting Spacecraft collection to Spacecraft → same instances preserved. */
  @Test
  void testSpacecraftAsTypeSpacecraft() {
    String constraint =
        """
context spaceMission::Spacecraft inv asTypeSpacecraft:
  spaceMission::Spacecraft.allInstances().oclAsType(spaceMission::Spacecraft).size() == spaceMission::Spacecraft.allInstances().size()
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    result
        .getCompilerErrors()
        .forEach(error -> System.err.println("Compiler error: " + error.getMessage()));
    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Cast should preserve all Spacecraft instances");
  }

  /** Tests oclAsType used after select in metamodel context. */
  @Test
  void testSelectThenAsTypeInMetamodel() {
    String constraint =
        """
context spaceMission::Spacecraft inv selectThenAsType:
  spaceMission::Spacecraft.allInstances()
    .select(sc | sc.oclIsKindOf(spaceMission::Spacecraft))
    .oclAsType(spaceMission::Spacecraft)
    .size() > 0
""";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER});

    result
        .getCompilerErrors()
        .forEach(error -> System.err.println("Compiler error: " + error.getMessage()));
    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Filtered and cast collection should be non-empty");
  }

  // ==================== Entry Point Override ====================

  /** Overrides parse entry point to use {@code infixedExpCS()} for oclAsType expressions. */
  @Override
  protected ParseTree parse(String input) {
    CommonTokenStream tokens =
        new CommonTokenStream(new VitruvOCLLexer(CharStreams.fromString(input)));
    return new VitruvOCLParser(tokens).infixedExpCS();
  }
}
