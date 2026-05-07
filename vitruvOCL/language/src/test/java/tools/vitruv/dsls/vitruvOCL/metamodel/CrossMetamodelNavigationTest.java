/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package tools.vitruv.dsls.vitruvOCL.metamodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/** Type Matrix: cross-metamodel navigation (allInstances + let + oclAsType) */
public class CrossMetamodelNavigationTest {
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

  // ── allInstances() cross-metamodel: id-based join ────────────

  @Test
  public void testDiskIdJoinToNamespace() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  cad::Namespace.allInstances().select(ns | ns.id == self.id).notEmpty()";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "Each BrakeDisk has a matching CAD Namespace");
  }

  @Test
  public void testCaliperIdJoinToNamespace() throws Exception {
    String c =
        "context brakesystem::BrakeCaliper inv:\n"
            + "  cad::Namespace.allInstances().select(ns | ns.id == self.id).notEmpty()";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testDiskIdJoinExactlyOne() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  cad::Namespace.allInstances().select(ns | ns.id == self.id).size() == 1";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── let bindings with cross-metamodel values ─────────────────

  @Test
  public void testLetCadNamespaceFromBrakeDisk() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let cadDisk = cad::Namespace.allInstances().select(ns | ns.id == self.id) in\n"
            + "  cadDisk.size() == 1";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testLetCadShapesFromBrakeDisk() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let cadDisk = cad::Namespace.allInstances().select(ns | ns.id == self.id) in\n"
            + "  cadDisk.shapes.size() >= 0";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testLetChainedCrossMetamodel() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let caliper = brakesystem::BrakeCaliper.allInstances().first() in\n"
            + "  let cadCaliper = cad::Namespace.allInstances().select(ns | ns.id == caliper.id)"
            + " in\n"
            + "  cadCaliper.notEmpty()";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Property navigation across metamodel boundary ────────────

  @Test
  public void testCrossMetamodelShapesAccess() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  cad::Namespace.allInstances().select(ns | ns.id == self.id)\n"
            + "    .shapes.forAll(s | s != null)";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCrossMetamodelParametersCount() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let cadCaliper = cad::Namespace.allInstances()\n"
            + "    .select(ns | ns.id == brakesystem::BrakeCaliper.allInstances().first().id) in\n"
            + "  cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size() == 4";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCrossMetamodelAllParameterSubtypes() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let cadCaliper = cad::Namespace.allInstances()\n"
            + "    .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in\n"
            + "  cadCaliper.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size() == 5";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "5 total parameters (4 Coordinates + 1 NumericParameter)");
  }

  // ── Full pipeline: allInstances + oclIsTypeOf + oclAsType ─────

  @Test
  public void testFullPipelineCrossMetamodelFails() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let cadCaliper = cad::Namespace.allInstances()\n"
            + "    .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in\n"
            + "  cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclAsType(cad::Coordinate).x <= self.diameterInMM / 2)";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertFalse(r.isSatisfied(), "x=175 > radius=165 should fail");
  }

  @Test
  public void testFullPipelineCrossMetamodelSatisfied() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let cadCaliper = cad::Namespace.allInstances()\n"
            + "    .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in\n"
            + "  cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))\n"
            + "    .forAll(p | p.oclAsType(cad::Coordinate).x >= self.diameterInMM / 2)";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "x=165 and x=175 both >= radius=165");
  }

  @Test
  public void testCrossMetamodelArithmeticRadiusComputed() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let radius = self.diameterInMM / 2 in\n"
            + "  radius > 0";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCrossMetamodelBothInstancesNonEmpty() throws Exception {
    String c =
        "context brakesystem::BrakeDisk inv:\n"
            + "  brakesystem::BrakeDisk.allInstances().size() > 0 and\n"
            + "  cad::Namespace.allInstances().size() > 0";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ── Cross-metamodel shape non-intersection ────────────────────

  @Test
  public void testSphereNoIntersectWithSphere() throws Exception {
    String c =
        "context cad::Sphere inv noIntersectWithSphere:\n"
            + "  let foreignShapes =\n"
            + "    cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))\n"
            + "      .collect(ns | ns.shapes).flatten()\n"
            + "  in\n"
            + "  foreignShapes.select(s | s.oclIsTypeOf(cad::Sphere))\n"
            + "    .forAll(other |\n"
            + "      let o = other.oclAsType(cad::Sphere) in\n"
            + "      let dx = self.center.x - o.center.x in\n"
            + "      let dy = self.center.y - o.center.y in\n"
            + "      let dz = self.center.z - o.center.z in\n"
            + "      let rSum = self.radius + o.radius in\n"
            + "      dx*dx + dy*dy + dz*dz >= rSum * rSum\n"
            + "    )";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereNoIntersectWithCylinder() throws Exception {
    String c =
        "context cad::Sphere inv noIntersectWithCylinder:\n"
            + "  let foreignShapes =\n"
            + "    cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))\n"
            + "      .collect(ns | ns.shapes).flatten()\n"
            + "  in\n"
            + "  foreignShapes.select(s | s.oclIsTypeOf(cad::Cylinder))\n"
            + "    .forAll(other |\n"
            + "      let o = other.oclAsType(cad::Cylinder) in\n"
            + "      let dx = self.center.x - o.bottomCenter.x in\n"
            + "      let dy = self.center.y - o.bottomCenter.y in\n"
            + "      let dz = self.center.z - o.bottomCenter.z in\n"
            + "      let rSum = self.radius + o.radius in\n"
            + "      dx*dx + dy*dy + dz*dz >= rSum * rSum\n"
            + "    )";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
