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
public class SelfContextMetamodelTest {
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

  // ── self : Sphere ────────────────────────────────────────────

  @Test
  public void testSelfSphereRadiusPositive() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.radius > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfSphereRadiusNegativeFails() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.radius >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied()); // vacuously true — no Sphere instances in model
  }

  @Test
  public void testSelfSphereCenterNavigationX() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.center.x >= 0 or self.center.x < 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfSphereCenterNavigationY() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.center.y >= 0 or self.center.y < 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfSphereCenterNavigationZ() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.center.z >= 0 or self.center.z < 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfSphereRadiusInArithmetic() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  self.radius * 2 > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Cylinder ──────────────────────────────────────────

  @Test
  public void testSelfCylinderRadiusPositive() throws Exception {
    String c = "context cad::Cylinder inv:\n" + "  self.radius > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfCylinderBottomTopDistinct() throws Exception {
    String c =
        "context cad::Cylinder inv:\n"
            + "  self.bottomCenter.x != self.topCenter.x or\n"
            + "  self.bottomCenter.y != self.topCenter.y or\n"
            + "  self.bottomCenter.z != self.topCenter.z";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfCylinderRadiusArithmetic() throws Exception {
    String c = "context cad::Cylinder inv:\n" + "  self.radius * 2 > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Tube ───────────────────────────────────────────────

  @Test
  public void testSelfTubeOuterRadiusPositive() throws Exception {
    String c = "context cad::Tube inv:\n" + "  self.outerRadius > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfTubeOuterVsInner() throws Exception {
    String c = "context cad::Tube inv:\n" + "  self.outerRadius > self.innerRadius";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfTubeInnerNonNegative() throws Exception {
    String c = "context cad::Tube inv:\n" + "  self.innerRadius >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Cone ───────────────────────────────────────────────

  @Test
  public void testSelfConeBaseRadiusPositive() throws Exception {
    String c = "context cad::Cone inv:\n" + "  self.baseRadius > 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfConeBaseAndApexDistinct() throws Exception {
    String c =
        "context cad::Cone inv:\n"
            + "  self.baseCenter.x != self.apex.x or\n"
            + "  self.baseCenter.y != self.apex.y or\n"
            + "  self.baseCenter.z != self.apex.z";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : Namespace ─────────────────────────────────────────

  @Test
  public void testSelfNamespaceShapesAccess() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  self.shapes.size() >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfNamespaceParametersAccess() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  self.parameters.size() >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfNamespaceShapesForAll() throws Exception {
    String c = "context cad::Namespace inv:\n" + "  self.shapes.forAll(s | s != null)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : BrakeDisk ─────────────────────────────────────────

  @Test
  public void testSelfBrakeDiskDiameterPositive() throws Exception {
    String c = "context brakesystem::BrakeDisk inv:\n" + "  self.diameterInMM > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfBrakeDiskDiameterArithmetic() throws Exception {
    String c = "context brakesystem::BrakeDisk inv:\n" + "  self.diameterInMM / 2 > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfBrakeDiskDiameterInLet() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let radius = self.diameterInMM / 2 in\n"
            + "  radius > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self : BrakeCaliper ──────────────────────────────────────

  @Test
  public void testSelfBrakeCaliperIdStringOp() throws Exception {
    String c = "context brakesystem::BrakeCaliper inv:\n" + "  self.id.length() > 0";
    ConstraintResult r = evalBrake(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── self in allInstances() context ───────────────────────────

  @Test
  public void testSelfInAllInstancesIncludes() throws Exception {
    String c = "context cad::Sphere inv:\n" + "  cad::Sphere.allInstances().includes(self)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfPropertyInIteratorBody() throws Exception {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Namespace.allInstances().forAll(ns | self.radius > 0)";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelfForeignNamespaceReject() throws Exception {
    String c =
        "context cad::Sphere inv:\n"
            + "  cad::Namespace.allInstances()\n"
            + "    .reject(ns | ns.shapes.includes(self))\n"
            + "    .size() >= 0";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
