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

/**
 * Type Matrix: EFloat as Ecore attribute type.
 *
 * <p>Tests EFloat (32-bit) as a distinct Ecore type, accessed via model attributes. In the
 * cad.ecore metamodel, {@code Sphere.radius} is typed as {@code EFloat}. Reading it produces a
 * ¡Float! singleton in OCL#.
 *
 * <p>Key rules verified:
 *
 * <pre>
 * self.radius (EFloat attr)         → ¡Float!
 * self.radius > 0                   → ¡Boolean!    (comparison: valid)
 * self.radius == self.radius        → ¡Boolean!    (equality: valid)
 * self.radius + 1                   → ¡Double!     (promotion: Float+Int→Double)
 * self.radius + 1.5                 → ¡Double!     (promotion: Float+Double→Double)
 * self.radius * self.radius         → ¡Double!     (Float×Float→Double)
 * self.radius.abs()                 → ¡Float!      (abs preserves type)
 * self.radius.floor()               → ¡Double!     (floor promotes Float→Double)
 * self.radius.ceil()                → ¡Double!     (ceil promotes)
 * self.radius.round()               → ¡Double!     (round promotes)
 * coll.collect(s | s.radius)        → Set{¡Float!} (Float in collection)
 * coll.select(s | s.radius > 0)     → Set{Sphere}  (Float in predicate)
 * coll.forAll(s | s.radius > 0)     → ¡Boolean!
 * let r = self.radius in r > 0      → ¡Boolean!    (Float in let)
 * self.radius + self.radius > 0     → ¡Boolean!    (arithmetic then compare)
 * </pre>
 *
 * <p>Note: Cylinder.radius, Tube.outerRadius, Tube.innerRadius and Cone.baseRadius are also EFloat
 * attributes in cad.ecore.
 */
@SuppressWarnings("java:S125")
class EFloatEcoreAttributeTest {

  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  @ParameterizedTest
  @MethodSource("satisfiedConstraints")
  void testConstraintSatisfied(String c) {
    ConstraintResult r = evalCad(c);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> satisfiedConstraints() {
    return Stream.of(
        // Basic EFloat attribute access
        "context cad::Sphere inv:\n  self.radius > 0",
        "context cad::Sphere inv:\n  self.radius == self.radius",
        "context cad::Sphere inv:\n  self.radius >= 0",
        // EFloat arithmetic promotion rules
        "context cad::Sphere inv:\n  self.radius + 1 > 0",
        "context cad::Sphere inv:\n  self.radius * 2 > 0",
        """
        context cad::Sphere inv:
          let r = self.radius in
          r * r > 0""",
        "context cad::Sphere inv:\n  self.radius + 0.5 > 0",
        "context cad::Sphere inv:\n  self.radius / 2 > 0",
        // EFloat unary ops: abs() preserves, floor/ceil/round promote
        "context cad::Sphere inv:\n  self.radius.abs() > 0",
        "context cad::Sphere inv:\n  self.radius.floor() >= 0",
        "context cad::Sphere inv:\n  self.radius.ceil() > 0",
        "context cad::Sphere inv:\n  self.radius.round() >= 0",
        "context cad::Sphere inv:\n  self.radius.ceiling() > 0",
        // EFloat in collections
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().collect(s | s.radius).size() > 0",
        "context cad::Sphere inv:\n"
            + "  cad::Sphere.allInstances().select(s | s.radius > 0).size() > 0",
        """
        context cad::Sphere inv:
          cad::Sphere.allInstances().forAll(s | s.radius > 0)""",
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().collect(s | s.radius).max() > 0",
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().collect(s | s.radius).min() > 0",
        "context cad::Sphere inv:\n  cad::Sphere.allInstances().collect(s | s.radius).sum() > 0",
        // EFloat in let binding
        """
        context cad::Sphere inv:
          let r = self.radius in
          r > 0""",
        """
        context cad::Sphere inv:
          let r = self.radius in
          r * 2.0 > 0""",
        """
        context cad::Tube inv:
          let outer = self.outerRadius in
          let inner = self.innerRadius in
          outer > inner""",
        // Multiple EFloat attributes (Cylinder, Tube, Cone)
        "context cad::Cylinder inv:\n  self.radius > 0",
        "context cad::Tube inv:\n  self.outerRadius > 0",
        "context cad::Tube inv:\n  self.innerRadius >= 0",
        "context cad::Cone inv:\n  self.baseRadius > 0",
        """
        context cad::Tube inv:
          self.outerRadius > self.innerRadius""",
        // EFloat in if-then-else
        "context cad::Sphere inv:\n  if self.radius > 0 then true else false endif",
        """
        context cad::Tube inv:
          if self.outerRadius > self.innerRadius
          then self.outerRadius
          else self.innerRadius
          endif > 0""");
  }

  @Test
  void testSphereRadiusComparisonNegativeFails() {
    ConstraintResult r =
        VitruvOCL.evaluateConstraint(
            "context cad::Sphere inv:\n  self.radius < 0",
            new Path[] {CAD_ECORE},
            new Path[] {Path.of("Intersecting.cad")});
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertFalse(r.isSatisfied(), "No sphere should have negative radius");
  }
}
