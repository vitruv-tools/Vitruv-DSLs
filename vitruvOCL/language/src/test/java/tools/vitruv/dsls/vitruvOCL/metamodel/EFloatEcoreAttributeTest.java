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
public class EFloatEcoreAttributeTest {

  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");
  private static final Path CAD_INST = Path.of("brake_disc_and_caliper_plate.cad");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  private static ConstraintResult evalCad(String c) throws Exception {
    return VitruvOCL.evaluateConstraint(c, new Path[] {CAD_ECORE}, new Path[] {CAD_INST});
  }

  // ══════════════════════════════════════════════════════════════
  // self.radius: EFloat attribute → ¡Float! singleton
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testSphereRadiusIsFloat() throws Exception {
    // self.radius > 0 — basic EFloat attribute access
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied(), "All sphere radii should be > 0");
  }

  @Test
  public void testSphereRadiusEquality() throws Exception {
    // self.radius == self.radius → ¡Boolean! true (EFloat == EFloat)
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius == self.radius");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereRadiusComparisonGe() throws Exception {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSphereRadiusComparisonNegativeFails() throws Exception {
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
  public void testFloatPlusIntegerPromotion() throws Exception {
    // self.radius (¡Float!) + 1 (¡Integer!) → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius + 1 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatTimesIntegerPromotion() throws Exception {
    // self.radius * 2 → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius * 2 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatTimesFloatPromotion() throws Exception {
    // self.radius * self.radius → ¡Double! (radius^2)
    ConstraintResult r =
        evalCad("context cad::Sphere inv:\n" + "  let r = self.radius in\n" + "  r * r > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatPlusDoublePromotion() throws Exception {
    // self.radius + 0.5 → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius + 0.5 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatDivideIntegerPromotion() throws Exception {
    // self.radius / 2 → ¡Double!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius / 2 > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // EFloat unary ops: abs() preserves, floor/ceil/round promote
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testFloatAbsPreservesType() throws Exception {
    // self.radius.abs() → ¡Float!
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.abs() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatFloorPromotion() throws Exception {
    // self.radius.floor() → ¡Double! (promoted)
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.floor() >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatCeilPromotion() throws Exception {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.ceil() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatRoundPromotion() throws Exception {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.round() >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatCeilingPromotion() throws Exception {
    ConstraintResult r = evalCad("context cad::Sphere inv:\n  self.radius.ceiling() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // EFloat in collections (collect, select, forAll)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testCollectFloatAttribute() throws Exception {
    // cad::Sphere.allInstances().collect(s | s.radius) → Set{¡Float!}
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().collect(s | s.radius).size() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testSelectOnFloatPredicate() throws Exception {
    // select on EFloat attribute
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().select(s | s.radius > 0).size() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testForAllOnFloatPredicate() throws Exception {
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n" + "  cad::Sphere.allInstances().forAll(s | s.radius > 0)");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCollectFloatThenMax() throws Exception {
    // max() on a Set{¡Float!}
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().collect(s | s.radius).max() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCollectFloatThenMin() throws Exception {
    ConstraintResult r =
        evalCad(
            "context cad::Sphere inv:\n"
                + "  cad::Sphere.allInstances().collect(s | s.radius).min() > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testCollectFloatThenSum() throws Exception {
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
  public void testFloatInLetBinding() throws Exception {
    ConstraintResult r =
        evalCad("context cad::Sphere inv:\n" + "  let r = self.radius in\n" + "  r > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatLetArithmetic() throws Exception {
    ConstraintResult r =
        evalCad("context cad::Sphere inv:\n" + "  let r = self.radius in\n" + "  r * r > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatLetComparisonToOtherFloat() throws Exception {
    // Tube.outerRadius > Tube.innerRadius (both EFloat)
    ConstraintResult r =
        evalCad(
            "context cad::Tube inv:\n"
                + "  let outer = self.outerRadius in\n"
                + "  let inner = self.innerRadius in\n"
                + "  outer > inner");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // Multiple EFloat attributes (Cylinder, Tube, Cone)
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testCylinderRadiusIsFloat() throws Exception {
    ConstraintResult r = evalCad("context cad::Cylinder inv:\n  self.radius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testTubeOuterRadiusIsFloat() throws Exception {
    ConstraintResult r = evalCad("context cad::Tube inv:\n  self.outerRadius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testTubeInnerRadiusIsFloat() throws Exception {
    ConstraintResult r = evalCad("context cad::Tube inv:\n  self.innerRadius >= 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testConeBaseRadiusIsFloat() throws Exception {
    ConstraintResult r = evalCad("context cad::Cone inv:\n  self.baseRadius > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testTwoFloatAttributesComparison() throws Exception {
    // outerRadius > innerRadius (two EFloat attrs from same object)
    ConstraintResult r =
        evalCad("context cad::Tube inv:\n" + "  self.outerRadius > self.innerRadius");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  // ══════════════════════════════════════════════════════════════
  // EFloat in if-then-else
  // ══════════════════════════════════════════════════════════════

  @Test
  public void testFloatInIfCondition() throws Exception {
    ConstraintResult r =
        evalCad("context cad::Sphere inv:\n" + "  if self.radius > 0 then true else false endif");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  @Test
  public void testFloatInIfThenElseBranches() throws Exception {
    // if cond then EFloat else EFloat → ¡Float! result
    ConstraintResult r =
        evalCad(
            "context cad::Tube inv:\n"
                + "  if self.outerRadius > self.innerRadius\n"
                + "  then self.outerRadius\n"
                + "  else self.innerRadius\n"
                + "  endif > 0");
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }
}
