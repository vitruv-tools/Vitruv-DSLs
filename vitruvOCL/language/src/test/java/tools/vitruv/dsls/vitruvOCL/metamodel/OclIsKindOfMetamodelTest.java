/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.metamodel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/** Type Matrix: oclIsKindOf(T) — recv x target type (inheritance) */
public class OclIsKindOfMetamodelTest {
  private static final Path BS_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path BS_INST = Path.of("brakesystem.brakesystem");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  private static ConstraintResult eval(String c) throws Exception {
    return VitruvOCL.evaluateConstraint(
        c, new Path[] {BS_ECORE, CAD_ECORE}, new Path[] {BS_INST, CAD_INST});
  }

  private static ConstraintResult evalCad(String c) throws Exception {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  private static ConstraintResult evalBrake(String c) throws Exception {
    return VitruvOCL.evaluateConstraint(c, new Path[] {BS_ECORE}, new Path[] {BS_INST});
  }

  // ── Same type → true ─────────────────────────────────────────

  @Test
  public void testCoordinateIsKindOfCoordinate() throws Exception {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclIsKindOf(cad::Coordinate))";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereIsKindOfSphere() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.oclIsKindOf(cad::Sphere)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCylinderIsKindOfCylinder() throws Exception {
    String c = "context cad::Cylinder inv:\n" + "  self.oclIsKindOf(cad::Cylinder)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testNamespaceIsKindOfNamespace() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  self.oclIsKindOf(cad::Namespace)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Subtype → supertype: true ────────────────────────────────

  @Test
  public void testCoordinateIsKindOfParameter() throws Exception {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclIsKindOf(cad::Parameter))";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Coordinate is kindOf Parameter (supertype)");
  }

  @Test
  public void testNumericParameterIsKindOfParameter() throws Exception {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::NumericParameter))\n"
            + "    .forAll(p | p.oclIsKindOf(cad::Parameter))";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereIsKindOfShape() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.oclIsKindOf(cad::Shape)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Sphere is kindOf Shape (supertype)");
  }

  @Test
  public void testCylinderIsKindOfShape() throws Exception {
    String c = "context cad::Cylinder inv:\n" + "  self.oclIsKindOf(cad::Shape)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testConeIsKindOfShape() throws Exception {
    String c = "context cad::Cone inv:\n" + "  self.oclIsKindOf(cad::Shape)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testTubeIsKindOfShape() throws Exception {
    String c = "context cad::Tube inv:\n" + "  self.oclIsKindOf(cad::Shape)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Sibling / unrelated type → false ─────────────────────────

  @Test
  public void testCoordinateIsNotKindOfNumericParameter() throws Exception {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclIsKindOf(cad::NumericParameter) == false)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Coordinate NOT kindOf NumericParameter (sibling)");
  }

  @Test
  public void testSphereIsNotKindOfCylinder() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.oclIsKindOf(cad::Cylinder) == false";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereIsNotKindOfNamespace() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.oclIsKindOf(cad::Namespace) == false";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testNamespaceIsNotKindOfShape() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  self.oclIsKindOf(cad::Shape) == false";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Collection usage ─────────────────────────────────────────

  @Test
  public void testAllParametersAreKindOfParameter() throws Exception {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.forAll(p | p.oclIsKindOf(cad::Parameter))";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testAllShapesAreKindOfShape() throws Exception {
    String c =
        "context cad::Namespace inv:\n" + "  self.shapes.forAll(s | s.oclIsKindOf(cad::Shape))";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testKindOfFilterEqualsAllParameters() throws Exception {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()\n"
            + "    == self.parameters.size()";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testKindOfCountGeTypeOfCount() throws Exception {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()\n"
            + "    >= self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size()";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
