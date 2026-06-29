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

/** Type Matrix: oclIsKindOf(T) — recv x target type (inheritance) */
class OclIsKindOfMetamodelTest {
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  // ── Same type → true ─────────────────────────────────────────

  @Test
  void testCoordinateIsKindOfCoordinate() {
    String c =
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsKindOf(cad::Coordinate))""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSphereIsKindOfSphere() {
    String c = """
        context cad::Sphere inv:
          self.oclIsKindOf(cad::Sphere)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testCylinderIsKindOfCylinder() {
    String c = """
        context cad::Cylinder inv:
          self.oclIsKindOf(cad::Cylinder)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testNamespaceIsKindOfNamespace() {
    String c = """
        context cad::Namespace inv:
          self.oclIsKindOf(cad::Namespace)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Subtype → supertype: true ────────────────────────────────

  @Test
  void testCoordinateIsKindOfParameter() {
    String c =
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsKindOf(cad::Parameter))""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Coordinate is kindOf Parameter (supertype)");
  }

  @Test
  void testNumericParameterIsKindOfParameter() {
    String c =
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::NumericParameter))
            .forAll(p | p.oclIsKindOf(cad::Parameter))""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSphereIsKindOfShape() {
    String c = """
        context cad::Sphere inv:
          self.oclIsKindOf(cad::Shape)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Sphere is kindOf Shape (supertype)");
  }

  @Test
  void testCylinderIsKindOfShape() {
    String c = """
        context cad::Cylinder inv:
          self.oclIsKindOf(cad::Shape)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testConeIsKindOfShape() {
    String c = """
        context cad::Cone inv:
          self.oclIsKindOf(cad::Shape)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testTubeIsKindOfShape() {
    String c = """
        context cad::Tube inv:
          self.oclIsKindOf(cad::Shape)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Sibling / unrelated type → false ─────────────────────────

  @Test
  void testCoordinateIsNotKindOfNumericParameter() {
    String c =
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsKindOf(cad::NumericParameter) == false)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Coordinate NOT kindOf NumericParameter (sibling)");
  }

  @Test
  void testSphereIsNotKindOfCylinder() {
    String c = """
        context cad::Sphere inv:
          self.oclIsKindOf(cad::Cylinder) == false""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSphereIsNotKindOfNamespace() {
    String c = """
        context cad::Sphere inv:
          self.oclIsKindOf(cad::Namespace) == false""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testNamespaceIsNotKindOfShape() {
    String c = """
        context cad::Namespace inv:
          self.oclIsKindOf(cad::Shape) == false""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Collection usage ─────────────────────────────────────────

  @Test
  void testAllParametersAreKindOfParameter() {
    String c =
        """
        context cad::Namespace inv:
          self.parameters.forAll(p | p.oclIsKindOf(cad::Parameter))""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testAllShapesAreKindOfShape() {
    String c =
        """
        context cad::Namespace inv:
          self.shapes.forAll(s | s.oclIsKindOf(cad::Shape))""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testKindOfFilterEqualsAllParameters() {
    String c =
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()
            == self.parameters.size()""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testKindOfCountGeTypeOfCount() {
    String c =
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()
            >= self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size()""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
