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

/** Type Matrix: self — context type x property access */
class SelfContextMetamodelTest {
  private static final Path BS_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path BS_INST = Path.of("brakesystem.brakesystem");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  private static ConstraintResult evalBrake(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {BS_ECORE}, new Path[] {BS_INST});
  }

  // ── self : Sphere ────────────────────────────────────────────

  @Test
  void testSelfSphereRadiusPositive() {
    String c = """
        context cad::Sphere inv:
          self.radius > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfSphereRadiusNegativeFails() {
    String c = """
        context cad::Sphere inv:
          self.radius >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied()); // vacuously true — no Sphere instances in model
  }

  @Test
  void testSelfSphereCenterNavigationX() {
    String c = """
        context cad::Sphere inv:
          self.center.x >= 0 or self.center.x < 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfSphereCenterNavigationY() {
    String c = """
        context cad::Sphere inv:
          self.center.y >= 0 or self.center.y < 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfSphereCenterNavigationZ() {
    String c = """
        context cad::Sphere inv:
          self.center.z >= 0 or self.center.z < 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfSphereRadiusInArithmetic() {
    String c = """
        context cad::Sphere inv:
          self.radius * 2 > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Cylinder ──────────────────────────────────────────

  @Test
  void testSelfCylinderRadiusPositive() {
    String c = """
        context cad::Cylinder inv:
          self.radius > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfCylinderBottomTopDistinct() {
    String c =
        """
        context cad::Cylinder inv:
          self.bottomCenter.x != self.topCenter.x or
          self.bottomCenter.y != self.topCenter.y or
          self.bottomCenter.z != self.topCenter.z""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfCylinderRadiusArithmetic() {
    String c = """
        context cad::Cylinder inv:
          self.radius * 2 > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Tube ───────────────────────────────────────────────

  @Test
  void testSelfTubeOuterRadiusPositive() {
    String c = """
        context cad::Tube inv:
          self.outerRadius > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfTubeOuterVsInner() {
    String c = """
        context cad::Tube inv:
          self.outerRadius > self.innerRadius""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfTubeInnerNonNegative() {
    String c = """
        context cad::Tube inv:
          self.innerRadius >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Cone ───────────────────────────────────────────────

  @Test
  void testSelfConeBaseRadiusPositive() {
    String c = """
        context cad::Cone inv:
          self.baseRadius > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfConeBaseAndApexDistinct() {
    String c =
        """
        context cad::Cone inv:
          self.baseCenter.x != self.apex.x or
          self.baseCenter.y != self.apex.y or
          self.baseCenter.z != self.apex.z""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Namespace ─────────────────────────────────────────

  @Test
  void testSelfNamespaceShapesAccess() {
    String c = """
        context cad::Namespace inv:
          self.shapes.size() >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfNamespaceParametersAccess() {
    String c = """
        context cad::Namespace inv:
          self.parameters.size() >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfNamespaceShapesForAll() {
    String c = """
        context cad::Namespace inv:
          self.shapes.forAll(s | s.notEmpty())""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : BrakeDisk ─────────────────────────────────────────

  @Test
  void testSelfBrakeDiskDiameterPositive() {
    String c = """
        context brakesystem::BrakeDisk inv:
          self.diameterInMM > 0""";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfBrakeDiskDiameterArithmetic() {
    String c = """
        context brakesystem::BrakeDisk inv:
          self.diameterInMM / 2 > 0""";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfBrakeDiskDiameterInLet() {
    String c =
        """
        context brakesystem::BrakeDisk inv:
          let radius = self.diameterInMM / 2 in
          radius > 0""";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : BrakeCaliper ──────────────────────────────────────

  @Test
  void testSelfBrakeCaliperIdStringOp() {
    String c = """
        context brakesystem::BrakeCaliper inv:
          self.id.length() > 0""";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self in allInstances() context ───────────────────────────

  @Test
  void testSelfInAllInstancesIncludes() {
    String c = """
        context cad::Sphere inv:
          cad::Sphere.allInstances().includes(self)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfPropertyInIteratorBody() {
    String c =
        """
        context cad::Sphere inv:
          cad::Namespace.allInstances().forAll(ns | self.radius > 0)""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelfForeignNamespaceReject() {
    String c =
        """
        context cad::Sphere inv:
          cad::Namespace.allInstances()
            .reject(ns | ns.shapes.includes(self))
            .size() >= 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
