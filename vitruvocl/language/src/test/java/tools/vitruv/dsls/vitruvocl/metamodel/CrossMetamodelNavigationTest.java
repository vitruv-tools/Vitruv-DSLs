/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package tools.vitruv.dsls.vitruvocl.metamodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Type Matrix: cross-metamodel navigation (allInstances + let + oclAsType) */
class CrossMetamodelNavigationTest {
  private static final Path BS_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path BS_INST = Path.of("brakesystem.brakesystem");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

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

  @ParameterizedTest
  @MethodSource("crossMetamodelSatisfiedConstraints")
  void testCrossMetamodelConstraintSatisfied(String constraint) {
    ConstraintResult r = eval(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> crossMetamodelSatisfiedConstraints() {
    return Stream.of(
        // id-based joins
        """
        context brakesystem::BrakeDisk inv:
          cad::Namespace.allInstances().select(ns | ns.id == self.id).notEmpty()""",
        """
        context brakesystem::BrakeCaliper inv:
          cad::Namespace.allInstances().select(ns | ns.id == self.id).notEmpty()""",
        """
        context brakesystem::BrakeDisk inv:
          cad::Namespace.allInstances().select(ns | ns.id == self.id).size() == 1""",
        // let bindings
        """
        context brakesystem::BrakeDisk inv:
          let cadDisk = cad::Namespace.allInstances().select(ns | ns.id == self.id) in
          cadDisk.size() == 1""",
        """
        context brakesystem::BrakeDisk inv:
          let cadDisk = cad::Namespace.allInstances().select(ns | ns.id == self.id) in
          cadDisk.shapes.size() >= 0""",
        """
        context brakesystem::BrakeDisk inv:
          let caliper = brakesystem::BrakeCaliper.allInstances().first() in
          let cadCaliper = cad::Namespace.allInstances().select(ns | ns.id == caliper.id) in
          cadCaliper.notEmpty()""",
        // property navigation
        """
        context brakesystem::BrakeDisk inv:
          cad::Namespace.allInstances().select(ns | ns.id == self.id)
            .shapes.forAll(s | s.notEmpty())""",
        """
        context brakesystem::BrakeDisk inv:
          let cadCaliper = cad::Namespace.allInstances()
            .select(ns | ns.id == brakesystem::BrakeCaliper.allInstances().first().id) in
          cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size() == 4""",
        """
        context brakesystem::BrakeDisk inv:
          let cadCaliper = cad::Namespace.allInstances()
            .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in
          cadCaliper.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size() == 5""",
        // arithmetic + both instances
        """
        context brakesystem::BrakeDisk inv:
          let radius = self.diameterInMM / 2 in
          radius > 0""",
        """
        context brakesystem::BrakeDisk inv:
          brakesystem::BrakeDisk.allInstances().size() > 0 and
          cad::Namespace.allInstances().size() > 0""",
        // full pipeline: satisfied case
        """
        context brakesystem::BrakeDisk inv:
          let cadCaliper = cad::Namespace.allInstances()
            .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in
          cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclAsType(cad::Coordinate).x >= self.diameterInMM / 2)""");
  }

  @Test
  void testFullPipelineCrossMetamodelFails() {
    String c =
        """
        context brakesystem::BrakeDisk inv:
          let cadCaliper = cad::Namespace.allInstances()
            .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in
          cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclAsType(cad::Coordinate).x <= self.diameterInMM / 2)""";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertFalse(r.isSatisfied(), "x=175 > radius=165 should fail");
  }

  @ParameterizedTest
  @MethodSource("cadSatisfiedConstraints")
  void testCadConstraintSatisfied(String constraint) {
    ConstraintResult r = evalCad(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> cadSatisfiedConstraints() {
    return Stream.of(
        """
        context cad::Sphere inv noIntersectWithSphere:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Sphere))
            .forAll(other |
              let o = other.oclAsType(cad::Sphere) in
              let dx = self.center.x - o.center.x in
              let dy = self.center.y - o.center.y in
              let dz = self.center.z - o.center.z in
              let rSum = self.radius + o.radius in
              dx*dx + dy*dy + dz*dz >= rSum * rSum
            )""",
        """
        context cad::Sphere inv noIntersectWithCylinder:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Cylinder))
            .forAll(other |
              let o = other.oclAsType(cad::Cylinder) in
              let dx = self.center.x - o.bottomCenter.x in
              let dy = self.center.y - o.bottomCenter.y in
              let dz = self.center.z - o.bottomCenter.z in
              let rSum = self.radius + o.radius in
              dx*dx + dy*dy + dz*dz >= rSum * rSum
            )""");
  }
}
