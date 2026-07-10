/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package tools.vitruv.dsls.vitruvocl.metamodel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/** Type Matrix: allInstances() on metaclass receiver → Set{T} */
class AllInstancesMetamodelTest {
  private static final Path BS_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path BS_INST = Path.of("brakesystem.brakesystem");
  private static final Path CAD_INST = Path.of("Intersecting.cad");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  private static ConstraintResult eval(String c) {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {BS_ECORE, CAD_ECORE}, new Path[] {BS_INST, CAD_INST});
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  private static ConstraintResult evalBrake(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {BS_ECORE}, new Path[] {BS_INST});
  }

  // ── cad metaclass receivers: satisfied ──────────────────────────

  @ParameterizedTest
  @MethodSource("cadSatisfiedConstraints")
  void testCadConstraintSatisfied(String constraint) {
    ConstraintResult r = evalCad(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> cadSatisfiedConstraints() {
    return Stream.of(
        // basic metaclass receivers
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().size() > 0",
        "context cad::Cylinder inv:\n  cad::Cylinder.allInstances().size() > 0",
        "context cad::Cone inv:\n  cad::Cone.allInstances().notEmpty()",
        "context cad::Tube inv:\n  cad::Tube.allInstances().size() > 0",
        "context cad::Namespace inv:\n  cad::Namespace.allInstances().size() > 0",
        "context cad::Namespace inv:\n  cad::Shape.allInstances().size() > 0",
        "context cad::Namespace inv:\n  cad::Parameter.allInstances().size() >= 0",
        // chaining
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().select(s | s.radius > 0).size() > 0",
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().forAll(s | s.radius > 0)""",
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().exists(s | s.radius > 0)""",
        "context cad::Cylinder inv:\n  cad::Cylinder.allInstances().size() >= 1",
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().isEmpty() == false",
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().collect(s | s.radius).size() > 0",
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().collect(s | s.radius).size() > 0""",
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().sortedBy(s | s.radius).size() > 0",
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().one(s | s.radius > 0) or
          cad::Sphere.allInstances().size() != 1""");
  }

  // ── cad metaclass receivers: success only (no isSatisfied) ──────

  @ParameterizedTest
  @MethodSource("cadSuccessOnlyConstraints")
  void testCadConstraintSuccess(String constraint) {
    ConstraintResult r = evalCad(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  static Stream<String> cadSuccessOnlyConstraints() {
    return Stream.of(
        "context cad::Box inv:\n  cad::Box.allInstances().size() >= 0",
        "context cad::Prism inv:\n  cad::Prism.allInstances().size() >= 0",
        "context cad::Pyramid inv:\n  cad::Pyramid.allInstances().size() >= 0",
        "context cad::Cylinder inv:\n  cad::Cylinder.allInstances().first().notEmpty()");
  }

  // ── brakesystem receivers ────────────────────────────────────────

  @ParameterizedTest
  @MethodSource("brakeSatisfiedConstraints")
  void testBrakeConstraintSatisfied(String constraint) {
    ConstraintResult r = evalBrake(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> brakeSatisfiedConstraints() {
    return Stream.of(
        "context brakesystem::BrakeDisk inv:\n  brakesystem::BrakeDisk.allInstances().size() > 0",
        "context brakesystem::BrakeCaliper inv:\n"
            + "  brakesystem::BrakeCaliper.allInstances().size() > 0");
  }

  @Test
  void testBrakePadAllInstancesEvaluates() {
    ConstraintResult r =
        evalBrake(
            "context brakesystem::BrakePad inv:\n"
                + "  brakesystem::BrakePad.allInstances().size() >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  // ── cross-metamodel ──────────────────────────────────────────────

  @Test
  void testAllInstancesCrossMetamodel() {
    String c =
        """
        context brakesystem::BrakeDisk inv:
          brakesystem::BrakeDisk.allInstances().size() > 0 and
          cad::Namespace.allInstances().size() > 0""";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
