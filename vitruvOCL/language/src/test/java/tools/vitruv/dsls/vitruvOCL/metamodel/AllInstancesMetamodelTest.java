/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.metamodel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import tools.vitruv.dsls.vitruvOCL.cli.VitruvOCLCLI;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/** Type Matrix: allInstances() on metaclass receiver → Set{T} */
public class AllInstancesMetamodelTest {
  private static final Path BS_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path BS_INST = Path.of("brakesystem.brakesystem");
  private static final Path CAD_INST = Path.of("Intersecting.cad");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  private static ConstraintResult eval(String c) throws Exception {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {BS_ECORE, CAD_ECORE}, new Path[] {BS_INST, CAD_INST});
  }

  private static ConstraintResult evalCad(String c) throws Exception {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  private static ConstraintResult evalBrake(String c) throws Exception {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {BS_ECORE}, new Path[] {BS_INST});
  }

  // ── cad metaclass receivers ──────────────────────────────────

  @Test
  public void testSphereAllInstancesNotEmpty() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  cad::Sphere.allInstances().size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCylinderAllInstancesNotEmpty() throws Exception {
    String c = "context cad::Cylinder inv:\n" + "  cad::Cylinder.allInstances().size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testConeAllInstancesNotEmpty() throws Exception {
    String c = "context cad::Cone inv:\n" + "  cad::Cone.allInstances().notEmpty()";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testTubeAllInstancesNotEmpty() throws Exception {
    String c = "context cad::Tube inv:\n" + "  cad::Tube.allInstances().size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testBoxAllInstancesEvaluates() throws Exception {
    String c = "context cad::Box inv:\n" + "  cad::Box.allInstances().size() >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  public void testPrismAllInstancesEvaluates() throws Exception {
    String c = "context cad::Prism inv:\n" + "  cad::Prism.allInstances().size() >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  public void testPyramidAllInstancesEvaluates() throws Exception {
    String c = "context cad::Pyramid inv:\n" + "  cad::Pyramid.allInstances().size() >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  public void testNamespaceAllInstancesNotEmpty() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  cad::Namespace.allInstances().size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testShapeSupertypeAllInstances() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  cad::Shape.allInstances().size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Shape.allInstances() includes all concrete shapes");
  }

  @Test
  public void testParameterSupertypeAllInstances() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  cad::Parameter.allInstances().size() >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── brakesystem receivers ────────────────────────────────────

  @Test
  public void testBrakeDiskAllInstancesNotEmpty() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  brakesystem::BrakeDisk.allInstances().size() > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testBrakeCaliperAllInstancesNotEmpty() throws Exception {
    String c =
        "context brakesystem::BrakeCaliper inv:\n"
            + "  brakesystem::BrakeCaliper.allInstances().size() > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testBrakePadAllInstancesEvaluates() throws Exception {
    String c =
        "context brakesystem::BrakePad inv:\n"
            + "  brakesystem::BrakePad.allInstances().size() >= 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  // ── allInstances() result → chaining ────────────────────────

  @Test
  public void testAllInstancesThenSelect() throws Exception {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().select(s | s.radius > 0).size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenForAll() throws Exception {
    String c =
        "context cad::Sphere inv:\n" + "  cad::Sphere.allInstances().forAll(s | s.radius > 0)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenExists() throws Exception {
    String c =
        "context cad::Sphere inv:\n" + "  cad::Sphere.allInstances().exists(s | s.radius > 0)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenSize() throws Exception {
    String c = "context cad::Cylinder inv:\n" + "  cad::Cylinder.allInstances().size() >= 1";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenIsEmpty() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  cad::Sphere.allInstances().isEmpty() == false";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenCollect() throws Exception {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().collect(s | s.radius).size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenFirst() throws Exception {
    String c = "context cad::Cylinder inv:\n" + "  cad::Cylinder.allInstances().first().notEmpty()";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  @Test
  public void testAllInstancesCrossMetamodel() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  brakesystem::BrakeDisk.allInstances().size() > 0 and\n"
            + "  cad::Namespace.allInstances().size() > 0";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesPropertyNavigation() throws Exception {
    String c =
        "context cad::Sphere inv:\n" + "  cad::Sphere.allInstances().forAll(s | s.radius > 0)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenSortedBy() throws Exception {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().sortedBy(s | s.radius).size() > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllInstancesThenOne() throws Exception {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().one(s | s.radius > 0) or\n"
            + "  cad::Sphere.allInstances().size() != 1";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
