/* ******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package tools.vitruv.dsls.vitruvocl.metamodel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/** Type Matrix: self — context type x property access */
class SelfContextMetamodelTest {
  private static final Path BS_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path BS_INST = Path.of("brakesystem.brakesystem");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  private static ConstraintResult evalBrake(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {BS_ECORE}, new Path[] {BS_INST});
  }

  @ParameterizedTest
  @MethodSource("cadConstraints")
  void testCadConstraintSatisfied(String constraint) {
    ConstraintResult r = evalCad(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> cadConstraints() {
    return Stream.of(
        // Sphere
        "context cad::Sphere inv:\n  self.radius > 0",
        "context cad::Sphere inv:\n  self.radius >= 0",
        "context cad::Sphere inv:\n  self.center.x >= 0 or self.center.x < 0",
        "context cad::Sphere inv:\n  self.center.y >= 0 or self.center.y < 0",
        "context cad::Sphere inv:\n  self.center.z >= 0 or self.center.z < 0",
        "context cad::Sphere inv:\n  self.radius * 2 > 0",
        // Cylinder
        "context cad::Cylinder inv:\n  self.radius > 0",
        """
        context cad::Cylinder inv:
          self.bottomCenter.x != self.topCenter.x or
          self.bottomCenter.y != self.topCenter.y or
          self.bottomCenter.z != self.topCenter.z""",
        "context cad::Cylinder inv:\n  self.radius * 2 > 0",
        // Tube
        "context cad::Tube inv:\n  self.outerRadius > 0",
        "context cad::Tube inv:\n  self.outerRadius > self.innerRadius",
        "context cad::Tube inv:\n  self.innerRadius >= 0",
        // Cone
        "context cad::Cone inv:\n  self.baseRadius > 0",
        """
        context cad::Cone inv:
          self.baseCenter.x != self.apex.x or
          self.baseCenter.y != self.apex.y or
          self.baseCenter.z != self.apex.z""",
        // Namespace
        "context cad::Namespace inv:\n  self.shapes.size() >= 0",
        "context cad::Namespace inv:\n  self.parameters.size() >= 0",
        "context cad::Namespace inv:\n  self.shapes.forAll(s | s.notEmpty())",
        // self in allInstances
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().includes(self)",
        """
        context cad::Sphere inv:
          cad::Namespace.allInstances().forAll(ns | self.radius > 0)""",
        """
        context cad::Sphere inv:
          cad::Namespace.allInstances()
            .reject(ns | ns.shapes.includes(self))
            .size() >= 0""");
  }

  @ParameterizedTest
  @MethodSource("brakeConstraints")
  void testBrakeConstraintSatisfied(String constraint) {
    ConstraintResult r = evalBrake(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> brakeConstraints() {
    return Stream.of(
        "context brakesystem::BrakeDisk inv:\n  self.diameterInMM > 0",
        "context brakesystem::BrakeDisk inv:\n  self.diameterInMM / 2 > 0",
        """
        context brakesystem::BrakeDisk inv:
          let radius = self.diameterInMM / 2 in
          radius > 0""",
        "context brakesystem::BrakeCaliper inv:\n  self.id.length() > 0");
  }
}
