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

/** Type Matrix: oclIsTypeOf(T) — recv x target type (exact match) */
public class OclIsTypeOfMetamodelTest {
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  // ── Exact type match → true ──────────────────────────────────

  @Test
  public void testCoordinateIsTypeOfCoordinate() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclIsTypeOf(cad::Coordinate))";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testNumericParameterIsTypeOfNumericParameter() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::NumericParameter))\n"
            + "    .forAll(p | p.oclIsTypeOf(cad::NumericParameter))";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereIsTypeOfSphere() {
    String c = "context cad::Sphere inv:\n" + "  self.oclIsTypeOf(cad::Sphere)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCylinderIsTypeOfCylinder() {
    String c = "context cad::Cylinder inv:\n" + "  self.oclIsTypeOf(cad::Cylinder)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testNamespaceIsTypeOfNamespace() {
    String c = "context cad::Namespace inv:\n" + "  self.oclIsTypeOf(cad::Namespace)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Supertype target → FALSE (key difference from oclIsKindOf) ─

  @Test
  public void testCoordinateIsNotTypeOfParameter() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclIsTypeOf(cad::Parameter) == false)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Coordinate.oclIsTypeOf(Parameter) = false (not exact)");
  }

  @Test
  public void testSphereIsNotTypeOfShape() {
    String c = "context cad::Sphere inv:\n" + "  self.oclIsTypeOf(cad::Shape) == false";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Sphere.oclIsTypeOf(Shape) = false");
  }

  @Test
  public void testCylinderIsNotTypeOfShape() {
    String c = "context cad::Cylinder inv:\n" + "  self.oclIsTypeOf(cad::Shape) == false";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Sibling type → false ─────────────────────────────────────

  @Test
  public void testCoordinateIsNotTypeOfNumericParameter() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclIsTypeOf(cad::NumericParameter) == false)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereIsNotTypeOfCylinder() {
    String c = "context cad::Sphere inv:\n" + "  self.oclIsTypeOf(cad::Cylinder) == false";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Filtering exact type from mixed collection ───────────────

  @Test
  public void testFilterExactCoordinateCount() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.name == \"id3_front_left_caliper_namespace\" implies\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size() == 4";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Caliper namespace has exactly 4 Coordinates");
  }

  @Test
  public void testTypeOfCountLessThanKindOfCount() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size()\n"
            + "    < self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testTypeOfThenOclAsTypePipeline() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .collect(p | p.oclAsType(cad::Coordinate).x)\n"
            + "    .forAll(x | x >= 0 or x < 0)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereFilterByTypeOfFromShapes() {
    String c =
        "context cad::Namespace inv:\n"
            + "  self.shapes.select(s | s.oclIsTypeOf(cad::Sphere))\n"
            + "    .forAll(s | s.oclIsTypeOf(cad::Cylinder) == false)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
