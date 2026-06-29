/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package tools.vitruv.dsls.vitruvocl.metamodel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  private static ConstraintResult eval(String c) {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {BS_ECORE, CAD_ECORE}, new Path[] {BS_INST, CAD_INST});
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  private static ConstraintResult evalBrake(String c) {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {BS_ECORE}, new Path[] {BS_INST});
  }

  // ── cad metaclass receivers ──────────────────────────────────

  @Test
  void testSphereAllInstancesNotEmpty() {
    String c = """
        context cad::Sphere inv:
          cad::Sphere.allInstances().size() > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testCylinderAllInstancesNotEmpty() {
    String c = """
        context cad::Cylinder inv:
          cad::Cylinder.allInstances().size() > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testConeAllInstancesNotEmpty() {
    String c = """
        context cad::Cone inv:
          cad::Cone.allInstances().notEmpty()""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testTubeAllInstancesNotEmpty() {
    String c = """
        context cad::Tube inv:
          cad::Tube.allInstances().size() > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testBoxAllInstancesEvaluates() {
    String c = """
        context cad::Box inv:
          cad::Box.allInstances().size() >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  void testPrismAllInstancesEvaluates() {
    String c = """
        context cad::Prism inv:
          cad::Prism.allInstances().size() >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  void testPyramidAllInstancesEvaluates() {
    String c = """
        context cad::Pyramid inv:
          cad::Pyramid.allInstances().size() >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  void testNamespaceAllInstancesNotEmpty() {
    String c = """
        context cad::Namespace inv:
          cad::Namespace.allInstances().size() > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testShapeSupertypeAllInstances() {
    String c = """
        context cad::Namespace inv:
          cad::Shape.allInstances().size() > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Shape.allInstances() includes all concrete shapes");
  }

  @Test
  void testParameterSupertypeAllInstances() {
    String c = """
        context cad::Namespace inv:
          cad::Parameter.allInstances().size() >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── brakesystem receivers ────────────────────────────────────

  @Test
  void testBrakeDiskAllInstancesNotEmpty() {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  brakesystem::BrakeDisk.allInstances().size() > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testBrakeCaliperAllInstancesNotEmpty() {
    String c =
        "context brakesystem::BrakeCaliper inv:\n"
            + "  brakesystem::BrakeCaliper.allInstances().size() > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testBrakePadAllInstancesEvaluates() {
    String c =
        "context brakesystem::BrakePad inv:\n"
            + "  brakesystem::BrakePad.allInstances().size() >= 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  // ── allInstances() result → chaining ────────────────────────

  @Test
  void testAllInstancesThenSelect() {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().select(s | s.radius > 0).size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenForAll() {
    String c =
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().forAll(s | s.radius > 0)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenExists() {
    String c =
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().exists(s | s.radius > 0)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenSize() {
    String c = """
        context cad::Cylinder inv:
          cad::Cylinder.allInstances().size() >= 1""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenIsEmpty() {
    String c = """
        context cad::Sphere inv:
          cad::Sphere.allInstances().isEmpty() == false""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenCollect() {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().collect(s | s.radius).size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenFirst() {
    String c = """
        context cad::Cylinder inv:
          cad::Cylinder.allInstances().first().notEmpty()""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  void testAllInstancesCrossMetamodel() {
    String c = """
        context brakesystem::BrakeDisk inv:
          brakesystem::BrakeDisk.allInstances().size() > 0 and
          cad::Namespace.allInstances().size() > 0""";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesPropertyNavigation() {
    String c =
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().collect(s | s.radius).size() > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenSortedBy() {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().sortedBy(s | s.radius).size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllInstancesThenOne() {
    String c = """
        context cad::Sphere inv:
          cad::Sphere.allInstances().one(s | s.radius > 0) or
          cad::Sphere.allInstances().size() != 1""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
