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

/** Type Matrix: oclAsType(T) — recv x target type x post-cast property access */
class OclAsTypeMetamodelTest {
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
  @MethodSource("cadSatisfiedConstraints")
  void testCadConstraintSatisfied(String c) {
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> cadSatisfiedConstraints() {
    return Stream.of(
        // Parameter → Coordinate: .x .y .z
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclAsType(cad::Coordinate).x >= 0
                       or p.oclAsType(cad::Coordinate).x < 0)""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .collect(p | p.oclAsType(cad::Coordinate).y).size() >= 0""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .collect(p | p.oclAsType(cad::Coordinate).z).size() >= 0""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .collect(p | p.oclAsType(cad::Coordinate).x)
            .forAll(x | x >= 0 or x < 0)""",
        // Shape → Sphere: .radius .center
        """
        context cad::Namespace inv:
          self.shapes.select(s | s.oclIsTypeOf(cad::Sphere))
            .forAll(s | s.oclAsType(cad::Sphere).radius > 0)""",
        """
        context cad::Namespace inv:
          self.shapes.select(s | s.oclIsTypeOf(cad::Sphere))
            .collect(s | s.oclAsType(cad::Sphere).center.x).size() >= 0""",
        // Shape → Cylinder: .radius .bottomCenter .topCenter
        """
        context cad::Namespace inv:
          self.shapes.select(s | s.oclIsTypeOf(cad::Cylinder))
            .forAll(s | s.oclAsType(cad::Cylinder).radius > 0)""",
        """
        context cad::Namespace inv:
          self.shapes.select(s | s.oclIsTypeOf(cad::Cylinder))
            .forAll(s |
              s.oclAsType(cad::Cylinder).bottomCenter.x
                != s.oclAsType(cad::Cylinder).topCenter.x or
              s.oclAsType(cad::Cylinder).bottomCenter.y
                != s.oclAsType(cad::Cylinder).topCenter.y or
              s.oclAsType(cad::Cylinder).bottomCenter.z
                != s.oclAsType(cad::Cylinder).topCenter.z)""",
        // Shape → Tube: .outerRadius .innerRadius
        """
        context cad::Namespace inv:
          self.shapes.select(s | s.oclIsTypeOf(cad::Tube))
            .forAll(s | s.oclAsType(cad::Tube).outerRadius > 0)""",
        """
        context cad::Namespace inv:
          self.shapes.select(s | s.oclIsTypeOf(cad::Tube))
            .forAll(s | s.oclAsType(cad::Tube).outerRadius
                       > s.oclAsType(cad::Tube).innerRadius)""",
        // Cast result in arithmetic
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .collect(p | p.oclAsType(cad::Coordinate).x).sum() >= 0
          or
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .collect(p | p.oclAsType(cad::Coordinate).x).sum() < 0""");
  }

  // ── Singleton cast: ¡T!.oclAsType(U) → ¡U! ──────────────────

  @Test
  void testSingletonCastPreservesMultiplicity() {
    String c =
        """
        context cad::Namespace inv:
          let firstSphere = self.shapes.select(s | s.oclIsTypeOf(cad::Sphere)).first() in
          firstSphere.oclAsType(cad::Sphere).radius > 0""";
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }

  // ── Cast result in cross-metamodel arithmetic ────────────────

  @Test
  void testCastCoordinateXConstraintFails() {
    String c =
        """
        context brakesystem::BrakeDisk inv:
          let radius = self.diameterInMM / 2 in
          cad::Namespace.allInstances()
            .select(ns | ns.id == brakesystem::BrakeCaliper.allInstances().first().id)
            .parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclAsType(cad::Coordinate).x <= radius)""";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertFalse(r.isSatisfied(), "x=175 > radius=165 should fail");
  }

  @Test
  void testCastCoordinateXConstraintSatisfied() {
    String c =
        """
        context brakesystem::BrakeDisk inv:
          let radius = self.diameterInMM / 2 in
          cad::Namespace.allInstances()
            .select(ns | ns.id == brakesystem::BrakeCaliper.allInstances().first().id)
            .parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclAsType(cad::Coordinate).x >= radius)""";
    ConstraintResult r = eval(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "x=165 and x=175 both >= radius=165");
  }
}
