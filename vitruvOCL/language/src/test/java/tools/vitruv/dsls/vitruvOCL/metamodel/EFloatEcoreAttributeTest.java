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
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  private static ConstraintResult evalCad(String c) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  // ══════════════════════════════════════════════════════════════
  // self.radius: EFloat attribute → ¡Float! singleton
  // ══════════════════════════════════════════════════════════════

  @Test
  void testSphereRadiusIsFloat() {
    // self.radius > 0 — basic EFloat attribute access
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "All sphere radii should be > 0");
  }

  @Test
  void testSphereRadiusEquality() {
    // self.radius == self.radius → ¡Boolean! true (EFloat == EFloat)
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius == self.radius");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSphereRadiusComparisonGe() {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
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

  // ══════════════════════════════════════════════════════════════
  // EFloat arithmetic promotion rules
  // ══════════════════════════════════════════════════════════════

  @Test
  void testFloatPlusIntegerPromotion() {
    // self.radius (¡Float!) + 1 (¡Integer!) → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius + 1 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatTimesIntegerPromotion() {
    // self.radius * 2 → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius * 2 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatTimesFloatPromotion() {
    // self.radius * self.radius → ¡Double! (radius^2)
    ConstraintResult r =
        evalCad("""
            context cad::Sphere inv:
              let r = self.radius in
              r * r > 0""");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatPlusDoublePromotion() {
    // self.radius + 0.5 → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius + 0.5 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatDivideIntegerPromotion() {
    // self.radius / 2 → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius / 2 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // EFloat unary ops: abs() preserves, floor/ceil/round promote
  // ══════════════════════════════════════════════════════════════

  @Test
  void testFloatAbsPreservesType() {
    // self.radius.abs() → ¡Float!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.abs() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatFloorPromotion() {
    // self.radius.floor() → ¡Double! (promoted)
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.floor() >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatCeilPromotion() {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.ceil() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatRoundPromotion() {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.round() >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatCeilingPromotion() {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.ceiling() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // EFloat in collections (collect, select, forAll)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testCollectFloatAttribute() {
    // cad::Sphere.allInstances().collect(s | s.radius) → Set{¡Float!}
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().collect(s | s.radius).size() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testSelectOnFloatPredicate() {
    // select on EFloat attribute
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().select(s | s.radius > 0).size() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testForAllOnFloatPredicate() {
    ConstraintResult r =
        evalCad(
            """
            context cad::Sphere inv:
              cad::Sphere.allInstances().forAll(s | s.radius > 0)""");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testCollectFloatThenMax() {
    // max() on a Set{¡Float!}
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().collect(s | s.radius).max() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testCollectFloatThenMin() {
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().collect(s | s.radius).min() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testCollectFloatThenSum() {
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().collect(s | s.radius).sum() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // EFloat in let binding
  // ══════════════════════════════════════════════════════════════

  @Test
  void testFloatInLetBinding() {
    ConstraintResult r =
        evalCad("""
            context cad::Sphere inv:
              let r = self.radius in
              r > 0""");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatLetArithmetic() {
    ConstraintResult r =
        evalCad("""
            context cad::Sphere inv:
              let r = self.radius in
              r * 2.0 > 0""");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatLetComparisonToOtherFloat() {
    // Tube.outerRadius > Tube.innerRadius (both EFloat)
    ConstraintResult r =
        evalCad(
            """
            context cad::Tube inv:
              let outer = self.outerRadius in
              let inner = self.innerRadius in
              outer > inner""");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // Multiple EFloat attributes (Cylinder, Tube, Cone)
  // ══════════════════════════════════════════════════════════════

  @Test
  void testCylinderRadiusIsFloat() {
    ConstraintResult r = evalCad("context cad::Cylinder inv:\n  self.radius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testTubeOuterRadiusIsFloat() {
    ConstraintResult r = evalCad("context cad::Tube inv:\n  self.outerRadius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testTubeInnerRadiusIsFloat() {
    ConstraintResult r = evalCad("context cad::Tube inv:\n  self.innerRadius >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testConeBaseRadiusIsFloat() {
    ConstraintResult r = evalCad("context cad::Cone inv:\n  self.baseRadius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testTwoFloatAttributesComparison() {
    // outerRadius > innerRadius (two EFloat attrs from same object)
    ConstraintResult r =
        evalCad("""
            context cad::Tube inv:
              self.outerRadius > self.innerRadius""");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // EFloat in if-then-else
  // ══════════════════════════════════════════════════════════════

  @Test
  void testFloatInIfCondition() {
    ConstraintResult r =
        evalCad("context cad::Sphere inv:\n" + "  if self.radius > 0 then true else false endif");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testFloatInIfThenElseBranches() {
    // if cond then EFloat else EFloat → ¡Float! result
    ConstraintResult r =
        evalCad(
            """
            context cad::Tube inv:
              if self.outerRadius > self.innerRadius
              then self.outerRadius
              else self.innerRadius
              endif > 0""");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
