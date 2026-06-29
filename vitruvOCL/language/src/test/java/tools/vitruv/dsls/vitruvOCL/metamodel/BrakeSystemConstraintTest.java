/*******************************************************************************
 * Copyright (c) 2026 Max Oesterle
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Max Oesterle - initial API and implementation
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
 * Tests for brake system cross-metamodel constraints using brakesystem and cad metamodels.
 *
 * <p>Tests the oclAsType cast operation with real inheritance (Coordinate extends Parameter) to
 * verify that property access on the cast type works correctly.
 *
 * <p>Also tests all CAD shape non-intersection constraints across different shape type pairs
 * (Sphere, Cylinder, Cone, Tube, Box, Prism, Pyramid), as well as basic shape validity constraints.
 */
class BrakeSystemConstraintTest {

  private static final Path BRAKESYSTEM_ECORE =
      Path.of("src/test/resources/test-metamodels/brakesystem.ecore");
  private static final Path CAD_ECORE = Path.of("src/test/resources/test-metamodels/cad.ecore");

  private static final Path BRAKESYSTEM_INSTANCE = Path.of("brakesystem.brakesystem");
  private static final Path CAD_INSTANCE = Path.of("brake_disc_and_caliper_plate.cad");

  /** Registers the test model base path before all tests run. */
  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  // ---------------------------------------------------------------------------
  // Cross-metamodel brake system constraints
  // ---------------------------------------------------------------------------

  /**
   * Tests that Coordinate parameters in the caliper namespace can be filtered and cast. Verifies
   * oclIsTypeOf + oclAsType pipeline with real inheritance (Coordinate extends Parameter). The
   * caliper has Coordinates with x=165 and x=175, disk diameter=330 (radius=165). x=175 > 165 →
   * constraint should NOT be satisfied.
   */
  @Test
  void testCaliperCoordinatesWithinDiskRadius() {
    String constraint =
        """
        context brakesystem::BrakeDisk inv coordinatesWithinRadius:
          let cadCaliper = cad::Namespace.allInstances()
            .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in
          cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclAsType(cad::Coordinate).x <= self.diameterInMM / 2)
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {BRAKESYSTEM_ECORE, CAD_ECORE},
            new Path[] {BRAKESYSTEM_INSTANCE, CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertFalse(
        result.isSatisfied(),
        "Constraint should fail: caliper coordinate x=175 exceeds disk radius 165");
  }

  /**
   * Tests that all caliper Coordinate x-values are >= disk radius. The caliper has Coordinates with
   * x=165 and x=175, disk radius=165. x=165 >= 165 and x=175 >= 165 → constraint SHOULD be
   * satisfied.
   */
  @Test
  void testCaliperCoordinatesOutsideDiskRadius() {
    String constraint =
        """
        context brakesystem::BrakeDisk inv:
          let cadDisk = cad::Namespace.allInstances().select(b | b.id == self.id) in
          let brakeCaliper = brakesystem::BrakeCaliper.allInstances().first() in
          let cadCaliper = cad::Namespace.allInstances()
            .select(b | b.id == brakeCaliper.id) in
          cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclAsType(cad::Coordinate).x >= self.diameterInMM / 2)
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {BRAKESYSTEM_ECORE, CAD_ECORE},
            new Path[] {BRAKESYSTEM_INSTANCE, CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    // x=165 >= 165 and x=175 >= 165 → satisfied
    assertTrue(
        result.isSatisfied(),
        "Constraint should pass: all caliper coordinate x-values >= disk radius 165");
  }

  /**
   * Tests that oclIsTypeOf correctly filters only Coordinate instances from mixed Parameter list.
   * The caliper namespace has 4 Coordinates and 1 NumericParameter.
   */
  @Test
  void testFilterCoordinatesFromMixedParameters() {
    String constraint =
        """
        context brakesystem::BrakeDisk inv onlyCoordinates:
          let cadCaliper = cad::Namespace.allInstances()
            .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in
          cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size() == 4
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {BRAKESYSTEM_ECORE, CAD_ECORE},
            new Path[] {BRAKESYSTEM_INSTANCE, CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Should find exactly 4 Coordinate parameters in caliper namespace");
  }

  /**
   * Tests that oclIsKindOf finds all Parameter subtypes including Coordinate and NumericParameter.
   */
  @Test
  void testFilterAllParameterSubtypes() {
    String constraint =
        """
        context brakesystem::BrakeDisk inv allSubtypes:
          let cadCaliper = cad::Namespace.allInstances()
            .select(b | b.id == brakesystem::BrakeCaliper.allInstances().first().id) in
          cadCaliper.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size() == 5
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {BRAKESYSTEM_ECORE, CAD_ECORE},
            new Path[] {BRAKESYSTEM_INSTANCE, CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(),
        "Should find all 5 parameters (Coordinate + NumericParameter) via oclIsKindOf");
  }

  /** Tests that oclAsType property access on x-coordinates returns a collection of size 2. */
  @Test
  void testOclAsTypePropertyAccessX() {
    String constraint =
        """
        context brakesystem::BrakeDisk inv:
          cad::Namespace.allInstances().select(b | b.id == self.id)
            .parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .collect(p | p.oclAsType(cad::Coordinate).x).size() == 2
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {BRAKESYSTEM_ECORE, CAD_ECORE},
            new Path[] {BRAKESYSTEM_INSTANCE, CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
  }

  /** Tests the original constraint using string concatenation to avoid line-length issues. */
  @Test
  void testOriginalConstraintExact() {
    String constraint =
        "context brakesystem::BrakeDisk inv:\n"
            + "  let cadDisk = cad::Namespace.allInstances().select(b | b.id == self.id) in\n"
            + "  let brakeCaliper = brakesystem::BrakeCaliper.allInstances().first() in\n"
            + "  let cadCaliper = cad::Namespace.allInstances()"
            + ".select(b | b.id == brakeCaliper.id) in\n"
            + "  cadCaliper.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).forAll(p |"
            + " p.oclAsType(cad::Coordinate).x <= self.diameterInMM / 2)";

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {BRAKESYSTEM_ECORE, CAD_ECORE},
            new Path[] {BRAKESYSTEM_INSTANCE, CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertFalse(
        result.isSatisfied(),
        "Constraint should fail: caliper coordinate x=175 exceeds disk radius 165");
  }

  // ---------------------------------------------------------------------------
  // CAD basic validity constraints
  // ---------------------------------------------------------------------------

  /** Tests that all Sphere instances have a positive radius. */
  @Test
  void testSphereRadiusPositive() {
    String constraint =
        """
        context cad::Sphere inv radiusPositive:
          self.radius > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All spheres should have positive radius");
  }

  /** Tests that all Cylinder instances have a positive radius. */
  @Test
  void testCylinderRadiusPositive() {
    String constraint =
        """
        context cad::Cylinder inv radiusPositive:
          self.radius > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All cylinders should have positive radius");
  }

  /** Tests that all Cylinder instances have distinct top and bottom center points. */
  @Test
  void testCylinderDistinctEndpoints() {
    String constraint =
        """
        context cad::Cylinder inv distinctEndpoints:
          self.bottomCenter.x != self.topCenter.x or
          self.bottomCenter.y != self.topCenter.y or
          self.bottomCenter.z != self.topCenter.z
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All cylinders should have distinct top/bottom centers");
  }

  /** Tests that all Cone instances have a positive base radius. */
  @Test
  void testConeBaseRadiusPositive() {
    String constraint =
        """
        context cad::Cone inv baseRadiusPositive:
          self.baseRadius > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All cones should have positive base radius");
  }

  /** Tests that all Cone instances have distinct base center and apex points. */
  @Test
  void testConeDistinctBaseAndApex() {
    String constraint =
        """
        context cad::Cone inv distinctBaseAndApex:
          self.baseCenter.x != self.apex.x or
          self.baseCenter.y != self.apex.y or
          self.baseCenter.z != self.apex.z
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All cones should have distinct base center and apex");
  }

  /** Tests that all Tube instances have a positive outer radius. */
  @Test
  void testTubeOuterRadiusPositive() {
    String constraint =
        """
        context cad::Tube inv outerRadiusPositive:
          self.outerRadius > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All tubes should have positive outer radius");
  }

  /** Tests that all Tube instances have a non-negative inner radius. */
  @Test
  void testTubeInnerRadiusNonNegative() {
    String constraint =
        """
        context cad::Tube inv innerRadiusNonNegative:
          self.innerRadius >= 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All tubes should have non-negative inner radius");
  }

  /** Tests that all Tube instances have outer radius strictly larger than inner radius. */
  @Test
  void testTubeOuterLargerThanInner() {
    String constraint =
        """
        context cad::Tube inv outerLargerThanInner:
          self.outerRadius > self.innerRadius
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All tubes should have outer radius larger than inner radius");
  }

  /** Tests that all Tube instances have distinct top and bottom center points. */
  @Test
  void testTubeDistinctEndpoints() {
    String constraint =
        """
        context cad::Tube inv distinctEndpoints:
          self.bottomCenter.x != self.topCenter.x or
          self.bottomCenter.y != self.topCenter.y or
          self.bottomCenter.z != self.topCenter.z
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All tubes should have distinct top/bottom centers");
  }

  /** Tests that all Prism instances have a non-zero extrusion vector. */
  @Test
  void testPrismExtrusionNonZero() {
    String constraint =
        """
        context cad::Prism inv extrusionNonZero:
          self.extrusion.x != 0 or
          self.extrusion.y != 0 or
          self.extrusion.z != 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All prisms should have a non-zero extrusion vector");
  }

  /** Tests that all Prism base profiles have at least 3 vertices. */
  @Test
  void testPrismBaseProfileHasEnoughVertices() {
    String constraint =
        """
        context cad::Prism inv baseProfileHasEnoughVertices:
          self.baseProfile.vertices.size() >= 3
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All prism base profiles should have at least 3 vertices");
  }

  /** Tests that all Pyramid instances have a defined (non-null) apex. */
  @Test
  void testPyramidApexDefined() {
    String constraint =
        """
        context cad::Pyramid inv apexDefined:
          self.apex.notEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "All pyramids should have a defined apex");
  }

  // ---------------------------------------------------------------------------
  // CAD non-intersection constraints: Sphere
  // ---------------------------------------------------------------------------

  /** Tests that spheres from different namespaces do not intersect with other spheres. */
  @Test
  void testSphereNoIntersectWithSphere() {
    String constraint =
        """
        context cad::Sphere inv noIntersectWithSphere:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Sphere))
            .forAll(other |
              let o: cad::Sphere = other.oclAsType(cad::Sphere) in
              let dx: Real = self.center.x - o.center.x in
              let dy: Real = self.center.y - o.center.y in
              let dz: Real = self.center.z - o.center.z in
              let rSum: Real = self.radius + o.radius in
              dx*dx + dy*dy + dz*dz >= rSum * rSum
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Spheres in different namespaces should not intersect");
  }

  /** Tests that spheres from different namespaces do not intersect with cylinders. */
  @Test
  void testSphereNoIntersectWithCylinder() {
    String constraint =
        """
        context cad::Sphere inv noIntersectWithCylinder:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Cylinder))
            .forAll(other |
              let o: cad::Cylinder = other.oclAsType(cad::Cylinder) in
              let cx: Real = (o.bottomCenter.x + o.topCenter.x) / 2.0 in
              let cy: Real = (o.bottomCenter.y + o.topCenter.y) / 2.0 in
              let cz: Real = (o.bottomCenter.z + o.topCenter.z) / 2.0 in
              let axDx: Real = o.topCenter.x - o.bottomCenter.x in
              let axDy: Real = o.topCenter.y - o.bottomCenter.y in
              let axDz: Real = o.topCenter.z - o.bottomCenter.z in
              let halfH: Real = (axDx*axDx + axDy*axDy + axDz*axDz) / 4.0 in
              let dx: Real = self.center.x - cx in
              let dy: Real = self.center.y - cy in
              let dz: Real = self.center.z - cz in
              let rSum: Real = self.radius + o.radius in
              dx*dx + dy*dy + dz*dz >= (rSum + halfH) * (rSum + halfH)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Spheres should not intersect with cylinders in foreign namespaces");
  }

  /** Tests that spheres from different namespaces do not intersect with cones. */
  @Test
  void testSphereNoIntersectWithCone() {
    String constraint =
        """
        context cad::Sphere inv noIntersectWithCone:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Cone))
            .forAll(other |
              let o: cad::Cone = other.oclAsType(cad::Cone) in
              let cx: Real = (o.baseCenter.x + o.apex.x) / 2.0 in
              let cy: Real = (o.baseCenter.y + o.apex.y) / 2.0 in
              let cz: Real = (o.baseCenter.z + o.apex.z) / 2.0 in
              let axDx: Real = o.apex.x - o.baseCenter.x in
              let axDy: Real = o.apex.y - o.baseCenter.y in
              let axDz: Real = o.apex.z - o.baseCenter.z in
              let halfH: Real = (axDx*axDx + axDy*axDy + axDz*axDz) / 4.0 in
              let dx: Real = self.center.x - cx in
              let dy: Real = self.center.y - cy in
              let dz: Real = self.center.z - cz in
              let rSum: Real = self.radius + o.baseRadius in
              dx*dx + dy*dy + dz*dz >= (rSum + halfH) * (rSum + halfH)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Spheres should not intersect with cones in foreign namespaces");
  }

  /** Tests that spheres from different namespaces do not intersect with tubes. */
  @Test
  void testSphereNoIntersectWithTube() {
    String constraint =
        """
        context cad::Sphere inv noIntersectWithTube:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Tube))
            .forAll(other |
              let o: cad::Tube = other.oclAsType(cad::Tube) in
              let cx: Real = (o.bottomCenter.x + o.topCenter.x) / 2.0 in
              let cy: Real = (o.bottomCenter.y + o.topCenter.y) / 2.0 in
              let cz: Real = (o.bottomCenter.z + o.topCenter.z) / 2.0 in
              let axDx: Real = o.topCenter.x - o.bottomCenter.x in
              let axDy: Real = o.topCenter.y - o.bottomCenter.y in
              let axDz: Real = o.topCenter.z - o.bottomCenter.z in
              let halfH: Real = (axDx*axDx + axDy*axDy + axDz*axDz) / 4.0 in
              let dx: Real = self.center.x - cx in
              let dy: Real = self.center.y - cy in
              let dz: Real = self.center.z - cz in
              let rSum: Real = self.radius + o.outerRadius in
              dx*dx + dy*dy + dz*dz >= (rSum + halfH) * (rSum + halfH)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Spheres should not intersect with tubes in foreign namespaces");
  }

  /** Tests that spheres from different namespaces do not intersect with boxes. */
  @Test
  void testSphereNoIntersectWithBox() {
    String constraint =
        """
        context cad::Sphere inv noIntersectWithBox:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Box))
            .forAll(other |
              let o: cad::Box = other.oclAsType(cad::Box) in
              let boxCx: Real = o.origin.x + o.edgeVectorA.x/2.0
                                           + o.edgeVectorB.x/2.0
                                           + o.edgeVectorC.x/2.0 in
              let boxCy: Real = o.origin.y + o.edgeVectorA.y/2.0
                                           + o.edgeVectorB.y/2.0
                                           + o.edgeVectorC.y/2.0 in
              let boxCz: Real = o.origin.z + o.edgeVectorA.z/2.0
                                           + o.edgeVectorB.z/2.0
                                           + o.edgeVectorC.z/2.0 in
              let dx: Real = self.center.x - boxCx in
              let dy: Real = self.center.y - boxCy in
              let dz: Real = self.center.z - boxCz in
              let halfW: Real = (o.edgeVectorA.x*o.edgeVectorA.x +
                                 o.edgeVectorA.y*o.edgeVectorA.y +
                                 o.edgeVectorA.z*o.edgeVectorA.z) / 2.0 in
              let halfH: Real = (o.edgeVectorB.x*o.edgeVectorB.x +
                                 o.edgeVectorB.y*o.edgeVectorB.y +
                                 o.edgeVectorB.z*o.edgeVectorB.z) / 2.0 in
              let halfD: Real = (o.edgeVectorC.x*o.edgeVectorC.x +
                                 o.edgeVectorC.y*o.edgeVectorC.y +
                                 o.edgeVectorC.z*o.edgeVectorC.z) / 2.0 in
              let bound: Real = self.radius + halfW + halfH + halfD in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Spheres should not intersect with boxes in foreign namespaces");
  }

  /** Tests that spheres from different namespaces do not intersect with prisms. */
  @Test
  void testSphereNoIntersectWithPrism() {
    String constraint =
        """
        context cad::Sphere inv noIntersectWithPrism:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Prism))
            .forAll(other |
              let o: cad::Prism = other.oclAsType(cad::Prism) in
              let verts: OrderedSet(Coordinate) = o.baseProfile.vertices in
              let minX: Real = verts.collect(v | v.x).min() in
              let maxX: Real = verts.collect(v | v.x).max() in
              let minY: Real = verts.collect(v | v.y).min() in
              let maxY: Real = verts.collect(v | v.y).max() in
              let minZ: Real = verts.collect(v | v.z).min() in
              let maxZ: Real = verts.collect(v | v.z).max() in
              let extMaxX: Real =
                if o.extrusion.x > 0.0 then maxX + o.extrusion.x else maxX endif in
              let extMinX: Real =
                if o.extrusion.x < 0.0 then minX + o.extrusion.x else minX endif in
              let extMaxY: Real =
                if o.extrusion.y > 0.0 then maxY + o.extrusion.y else maxY endif in
              let extMinY: Real =
                if o.extrusion.y < 0.0 then minY + o.extrusion.y else minY endif in
              let extMaxZ: Real =
                if o.extrusion.z > 0.0 then maxZ + o.extrusion.z else maxZ endif in
              let extMinZ: Real =
                if o.extrusion.z < 0.0 then minZ + o.extrusion.z else minZ endif in
              let pcx: Real = (extMinX + extMaxX) / 2.0 in
              let pcy: Real = (extMinY + extMaxY) / 2.0 in
              let pcz: Real = (extMinZ + extMaxZ) / 2.0 in
              let dx: Real = self.center.x - pcx in
              let dy: Real = self.center.y - pcy in
              let dz: Real = self.center.z - pcz in
              dx.abs() >= self.radius + (extMaxX - extMinX) / 2.0 or
              dy.abs() >= self.radius + (extMaxY - extMinY) / 2.0 or
              dz.abs() >= self.radius + (extMaxZ - extMinZ) / 2.0
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Spheres should not intersect with prisms in foreign namespaces");
  }

  /** Tests that spheres from different namespaces do not intersect with pyramids. */
  @Test
  void testSphereNoIntersectWithPyramid() {
    String constraint =
        """
        context cad::Sphere inv noIntersectWithPyramid:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Pyramid))
            .forAll(other |
              let o: cad::Pyramid = other.oclAsType(cad::Pyramid) in
              let dx: Real = self.center.x - o.apex.x in
              let dy: Real = self.center.y - o.apex.y in
              let dz: Real = self.center.z - o.apex.z in
              dx*dx + dy*dy + dz*dz >= self.radius * self.radius
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Spheres should not intersect with pyramids in foreign namespaces");
  }

  // ---------------------------------------------------------------------------
  // CAD non-intersection constraints: Cylinder
  // ---------------------------------------------------------------------------

  /** Tests that cylinders from different namespaces do not intersect with other cylinders. */
  @Test
  void testCylinderNoIntersectWithCylinder() {
    String constraint =
        """
        context cad::Cylinder inv noIntersectWithCylinder:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Cylinder))
            .forAll(other |
              let o: cad::Cylinder = other.oclAsType(cad::Cylinder) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let cx2: Real = (o.bottomCenter.x + o.topCenter.x) / 2.0 in
              let cy2: Real = (o.bottomCenter.y + o.topCenter.y) / 2.0 in
              let cz2: Real = (o.bottomCenter.z + o.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let ax2x: Real = o.topCenter.x - o.bottomCenter.x in
              let ax2y: Real = o.topCenter.y - o.bottomCenter.y in
              let ax2z: Real = o.topCenter.z - o.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let halfH2: Real = (ax2x*ax2x + ax2y*ax2y + ax2z*ax2z) / 4.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let rSum: Real = self.radius + o.radius in
              dx*dx + dy*dy + dz*dz >=
                (rSum + halfH1 + halfH2) * (rSum + halfH1 + halfH2)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(),
        "Cylinders should not intersect with cylinders in foreign namespaces");
  }

  /** Tests that cylinders from different namespaces do not intersect with cones. */
  @Test
  void testCylinderNoIntersectWithCone() {
    String constraint =
        """
        context cad::Cylinder inv noIntersectWithCone:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Cone))
            .forAll(other |
              let o: cad::Cone = other.oclAsType(cad::Cone) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let cx2: Real = (o.baseCenter.x + o.apex.x) / 2.0 in
              let cy2: Real = (o.baseCenter.y + o.apex.y) / 2.0 in
              let cz2: Real = (o.baseCenter.z + o.apex.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let ax2x: Real = o.apex.x - o.baseCenter.x in
              let ax2y: Real = o.apex.y - o.baseCenter.y in
              let ax2z: Real = o.apex.z - o.baseCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let halfH2: Real = (ax2x*ax2x + ax2y*ax2y + ax2z*ax2z) / 4.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let rSum: Real = self.radius + o.baseRadius in
              dx*dx + dy*dy + dz*dz >=
                (rSum + halfH1 + halfH2) * (rSum + halfH1 + halfH2)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Cylinders should not intersect with cones in foreign namespaces");
  }

  /** Tests that cylinders from different namespaces do not intersect with tubes. */
  @Test
  void testCylinderNoIntersectWithTube() {
    String constraint =
        """
        context cad::Cylinder inv noIntersectWithTube:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Tube))
            .forAll(other |
              let o: cad::Tube = other.oclAsType(cad::Tube) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let cx2: Real = (o.bottomCenter.x + o.topCenter.x) / 2.0 in
              let cy2: Real = (o.bottomCenter.y + o.topCenter.y) / 2.0 in
              let cz2: Real = (o.bottomCenter.z + o.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let ax2x: Real = o.topCenter.x - o.bottomCenter.x in
              let ax2y: Real = o.topCenter.y - o.bottomCenter.y in
              let ax2z: Real = o.topCenter.z - o.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let halfH2: Real = (ax2x*ax2x + ax2y*ax2y + ax2z*ax2z) / 4.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let rSum: Real = self.radius + o.outerRadius in
              dx*dx + dy*dy + dz*dz >=
                (rSum + halfH1 + halfH2) * (rSum + halfH1 + halfH2)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Cylinders should not intersect with tubes in foreign namespaces");
  }

  /** Tests that cylinders from different namespaces do not intersect with boxes. */
  @Test
  void testCylinderNoIntersectWithBox() {
    String constraint =
        """
        context cad::Cylinder inv noIntersectWithBox:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Box))
            .forAll(other |
              let o: cad::Box = other.oclAsType(cad::Box) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let boxCx: Real = o.origin.x + o.edgeVectorA.x/2.0
                                           + o.edgeVectorB.x/2.0
                                           + o.edgeVectorC.x/2.0 in
              let boxCy: Real = o.origin.y + o.edgeVectorA.y/2.0
                                           + o.edgeVectorB.y/2.0
                                           + o.edgeVectorC.y/2.0 in
              let boxCz: Real = o.origin.z + o.edgeVectorA.z/2.0
                                           + o.edgeVectorB.z/2.0
                                           + o.edgeVectorC.z/2.0 in
              let halfW: Real = (o.edgeVectorA.x*o.edgeVectorA.x +
                                 o.edgeVectorA.y*o.edgeVectorA.y +
                                 o.edgeVectorA.z*o.edgeVectorA.z) / 2.0 in
              let halfH2: Real = (o.edgeVectorB.x*o.edgeVectorB.x +
                                  o.edgeVectorB.y*o.edgeVectorB.y +
                                  o.edgeVectorB.z*o.edgeVectorB.z) / 2.0 in
              let halfD: Real = (o.edgeVectorC.x*o.edgeVectorC.x +
                                 o.edgeVectorC.y*o.edgeVectorC.y +
                                 o.edgeVectorC.z*o.edgeVectorC.z) / 2.0 in
              let dx: Real = cx1 - boxCx in
              let dy: Real = cy1 - boxCy in
              let dz: Real = cz1 - boxCz in
              let bound: Real = self.radius + halfH1 + halfW + halfH2 + halfD in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Cylinders should not intersect with boxes in foreign namespaces");
  }

  /** Tests that cylinders from different namespaces do not intersect with prisms. */
  @Test
  void testCylinderNoIntersectWithPrism() {
    String constraint =
        """
        context cad::Cylinder inv noIntersectWithPrism:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Prism))
            .forAll(other |
              let o: cad::Prism = other.oclAsType(cad::Prism) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let verts: OrderedSet(Coordinate) = o.baseProfile.vertices in
              let minX: Real = verts.collect(v | v.x).min() in
              let maxX: Real = verts.collect(v | v.x).max() in
              let minY: Real = verts.collect(v | v.y).min() in
              let maxY: Real = verts.collect(v | v.y).max() in
              let minZ: Real = verts.collect(v | v.z).min() in
              let maxZ: Real = verts.collect(v | v.z).max() in
              let extMaxX: Real =
                if o.extrusion.x > 0.0 then maxX + o.extrusion.x else maxX endif in
              let extMinX: Real =
                if o.extrusion.x < 0.0 then minX + o.extrusion.x else minX endif in
              let extMaxY: Real =
                if o.extrusion.y > 0.0 then maxY + o.extrusion.y else maxY endif in
              let extMinY: Real =
                if o.extrusion.y < 0.0 then minY + o.extrusion.y else minY endif in
              let extMaxZ: Real =
                if o.extrusion.z > 0.0 then maxZ + o.extrusion.z else maxZ endif in
              let extMinZ: Real =
                if o.extrusion.z < 0.0 then minZ + o.extrusion.z else minZ endif in
              let pcx: Real = (extMinX + extMaxX) / 2.0 in
              let pcy: Real = (extMinY + extMaxY) / 2.0 in
              let pcz: Real = (extMinZ + extMaxZ) / 2.0 in
              let dx: Real = cx1 - pcx in
              let dy: Real = cy1 - pcy in
              let dz: Real = cz1 - pcz in
              let bound: Real = self.radius + halfH1 +
                                (extMaxX - extMinX) / 2.0 +
                                (extMaxY - extMinY) / 2.0 +
                                (extMaxZ - extMinZ) / 2.0 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Cylinders should not intersect with prisms in foreign namespaces");
  }

  /** Tests that cylinders from different namespaces do not intersect with pyramids. */
  @Test
  void testCylinderNoIntersectWithPyramid() {
    String constraint =
        """
        context cad::Cylinder inv noIntersectWithPyramid:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Pyramid))
            .forAll(other |
              let o: cad::Pyramid = other.oclAsType(cad::Pyramid) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let dx: Real = cx1 - o.apex.x in
              let dy: Real = cy1 - o.apex.y in
              let dz: Real = cz1 - o.apex.z in
              let bound: Real = self.radius + halfH1 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Cylinders should not intersect with pyramids in foreign namespaces");
  }

  // ---------------------------------------------------------------------------
  // CAD non-intersection constraints: Cone
  // ---------------------------------------------------------------------------

  /** Tests that cones from different namespaces do not intersect with other cones. */
  @Test
  void testConeNoIntersectWithCone() {
    String constraint =
        """
        context cad::Cone inv noIntersectWithCone:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Cone))
            .forAll(other |
              let o: cad::Cone = other.oclAsType(cad::Cone) in
              let cx1: Real = (self.baseCenter.x + self.apex.x) / 2.0 in
              let cy1: Real = (self.baseCenter.y + self.apex.y) / 2.0 in
              let cz1: Real = (self.baseCenter.z + self.apex.z) / 2.0 in
              let cx2: Real = (o.baseCenter.x + o.apex.x) / 2.0 in
              let cy2: Real = (o.baseCenter.y + o.apex.y) / 2.0 in
              let cz2: Real = (o.baseCenter.z + o.apex.z) / 2.0 in
              let ax1x: Real = self.apex.x - self.baseCenter.x in
              let ax1y: Real = self.apex.y - self.baseCenter.y in
              let ax1z: Real = self.apex.z - self.baseCenter.z in
              let ax2x: Real = o.apex.x - o.baseCenter.x in
              let ax2y: Real = o.apex.y - o.baseCenter.y in
              let ax2z: Real = o.apex.z - o.baseCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let halfH2: Real = (ax2x*ax2x + ax2y*ax2y + ax2z*ax2z) / 4.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let rSum: Real = self.baseRadius + o.baseRadius in
              dx*dx + dy*dy + dz*dz >=
                (rSum + halfH1 + halfH2) * (rSum + halfH1 + halfH2)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Cones should not intersect with cones in foreign namespaces");
  }

  /** Tests that cones from different namespaces do not intersect with tubes. */
  @Test
  void testConeNoIntersectWithTube() {
    String constraint =
        """
        context cad::Cone inv noIntersectWithTube:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Tube))
            .forAll(other |
              let o: cad::Tube = other.oclAsType(cad::Tube) in
              let cx1: Real = (self.baseCenter.x + self.apex.x) / 2.0 in
              let cy1: Real = (self.baseCenter.y + self.apex.y) / 2.0 in
              let cz1: Real = (self.baseCenter.z + self.apex.z) / 2.0 in
              let cx2: Real = (o.bottomCenter.x + o.topCenter.x) / 2.0 in
              let cy2: Real = (o.bottomCenter.y + o.topCenter.y) / 2.0 in
              let cz2: Real = (o.bottomCenter.z + o.topCenter.z) / 2.0 in
              let ax1x: Real = self.apex.x - self.baseCenter.x in
              let ax1y: Real = self.apex.y - self.baseCenter.y in
              let ax1z: Real = self.apex.z - self.baseCenter.z in
              let ax2x: Real = o.topCenter.x - o.bottomCenter.x in
              let ax2y: Real = o.topCenter.y - o.bottomCenter.y in
              let ax2z: Real = o.topCenter.z - o.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let halfH2: Real = (ax2x*ax2x + ax2y*ax2y + ax2z*ax2z) / 4.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let rSum: Real = self.baseRadius + o.outerRadius in
              dx*dx + dy*dy + dz*dz >=
                (rSum + halfH1 + halfH2) * (rSum + halfH1 + halfH2)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Cones should not intersect with tubes in foreign namespaces");
  }

  /** Tests that cones from different namespaces do not intersect with boxes. */
  @Test
  void testConeNoIntersectWithBox() {
    String constraint =
        """
        context cad::Cone inv noIntersectWithBox:
          let foreignShapes: Set(Shape) =
            cad::Namespace.allInstances()
              .reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Box))
            .forAll(other |
              let o: cad::Box = other.oclAsType(cad::Box) in
              let cx1: Real = (self.baseCenter.x + self.apex.x) / 2.0 in
              let cy1: Real = (self.baseCenter.y + self.apex.y) / 2.0 in
              let cz1: Real = (self.baseCenter.z + self.apex.z) / 2.0 in
              let ax1x: Real = self.apex.x - self.baseCenter.x in
              let ax1y: Real = self.apex.y - self.baseCenter.y in
              let ax1z: Real = self.apex.z - self.baseCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let boxCx: Real = o.origin.x + o.edgeVectorA.x/2.0
                                           + o.edgeVectorB.x/2.0
                                           + o.edgeVectorC.x/2.0 in
              let boxCy: Real = o.origin.y + o.edgeVectorA.y/2.0
                                           + o.edgeVectorB.y/2.0
                                           + o.edgeVectorC.y/2.0 in
              let boxCz: Real = o.origin.z + o.edgeVectorA.z/2.0
                                           + o.edgeVectorB.z/2.0
                                           + o.edgeVectorC.z/2.0 in
              let halfW: Real = (o.edgeVectorA.x*o.edgeVectorA.x +
                                 o.edgeVectorA.y*o.edgeVectorA.y +
                                 o.edgeVectorA.z*o.edgeVectorA.z) / 2.0 in
              let halfH2: Real = (o.edgeVectorB.x*o.edgeVectorB.x +
                                  o.edgeVectorB.y*o.edgeVectorB.y +
                                  o.edgeVectorB.z*o.edgeVectorB.z) / 2.0 in
              let halfD: Real = (o.edgeVectorC.x*o.edgeVectorC.x +
                                 o.edgeVectorC.y*o.edgeVectorC.y +
                                 o.edgeVectorC.z*o.edgeVectorC.z) / 2.0 in
              let dx: Real = cx1 - boxCx in
              let dy: Real = cy1 - boxCy in
              let dz: Real = cz1 - boxCz in
              let bound: Real = self.baseRadius + halfH1 + halfW + halfH2 + halfD in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Cones should not intersect with boxes in foreign namespaces");
  }

  /** Tests that cones from different namespaces do not intersect with prisms. */
  @Test
  void testConeNoIntersectWithPrism() {
    String constraint =
        """
        context cad::Cone inv noIntersectWithPrism:
          let foreignShapes: Set(Shape) =
            cad::Namespace.allInstances()
              .reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Prism))
            .forAll(other |
              let o: cad::Prism = other.oclAsType(cad::Prism) in
              let cx1: Real = (self.baseCenter.x + self.apex.x) / 2.0 in
              let cy1: Real = (self.baseCenter.y + self.apex.y) / 2.0 in
              let cz1: Real = (self.baseCenter.z + self.apex.z) / 2.0 in
              let ax1x: Real = self.apex.x - self.baseCenter.x in
              let ax1y: Real = self.apex.y - self.baseCenter.y in
              let ax1z: Real = self.apex.z - self.baseCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let verts: OrderedSet(Coordinate) = o.baseProfile.vertices in
              let minX: Real = verts.collect(v | v.x).min() in
              let maxX: Real = verts.collect(v | v.x).max() in
              let minY: Real = verts.collect(v | v.y).min() in
              let maxY: Real = verts.collect(v | v.y).max() in
              let minZ: Real = verts.collect(v | v.z).min() in
              let maxZ: Real = verts.collect(v | v.z).max() in
              let extMaxX: Real =
                if o.extrusion.x > 0.0 then maxX + o.extrusion.x else maxX endif in
              let extMinX: Real =
                if o.extrusion.x < 0.0 then minX + o.extrusion.x else minX endif in
              let extMaxY: Real =
                if o.extrusion.y > 0.0 then maxY + o.extrusion.y else maxY endif in
              let extMinY: Real =
                if o.extrusion.y < 0.0 then minY + o.extrusion.y else minY endif in
              let extMaxZ: Real =
                if o.extrusion.z > 0.0 then maxZ + o.extrusion.z else maxZ endif in
              let extMinZ: Real =
                if o.extrusion.z < 0.0 then minZ + o.extrusion.z else minZ endif in
              let pcx: Real = (extMinX + extMaxX) / 2.0 in
              let pcy: Real = (extMinY + extMaxY) / 2.0 in
              let pcz: Real = (extMinZ + extMaxZ) / 2.0 in
              let dx: Real = cx1 - pcx in
              let dy: Real = cy1 - pcy in
              let dz: Real = cz1 - pcz in
              let bound: Real = self.baseRadius + halfH1 +
                                (extMaxX - extMinX) / 2.0 +
                                (extMaxY - extMinY) / 2.0 +
                                (extMaxZ - extMinZ) / 2.0 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Cones should not intersect with prisms in foreign namespaces");
  }

  /** Tests that cones from different namespaces do not intersect with pyramids. */
  @Test
  void testConeNoIntersectWithPyramid() {
    String constraint =
        """
        context cad::Cone inv noIntersectWithPyramid:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Pyramid))
            .forAll(other |
              let o: cad::Pyramid = other.oclAsType(cad::Pyramid) in
              let cx1: Real = (self.baseCenter.x + self.apex.x) / 2.0 in
              let cy1: Real = (self.baseCenter.y + self.apex.y) / 2.0 in
              let cz1: Real = (self.baseCenter.z + self.apex.z) / 2.0 in
              let ax1x: Real = self.apex.x - self.baseCenter.x in
              let ax1y: Real = self.apex.y - self.baseCenter.y in
              let ax1z: Real = self.apex.z - self.baseCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let dx: Real = cx1 - o.apex.x in
              let dy: Real = cy1 - o.apex.y in
              let dz: Real = cz1 - o.apex.z in
              let bound: Real = self.baseRadius + halfH1 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Cones should not intersect with pyramids in foreign namespaces");
  }

  // ---------------------------------------------------------------------------
  // CAD non-intersection constraints: Tube
  // ---------------------------------------------------------------------------

  /** Tests that tubes from different namespaces do not intersect with other tubes. */
  @Test
  void testTubeNoIntersectWithTube() {
    String constraint =
        """
        context cad::Tube inv noIntersectWithTube:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Tube))
            .forAll(other |
              let o: cad::Tube = other.oclAsType(cad::Tube) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let cx2: Real = (o.bottomCenter.x + o.topCenter.x) / 2.0 in
              let cy2: Real = (o.bottomCenter.y + o.topCenter.y) / 2.0 in
              let cz2: Real = (o.bottomCenter.z + o.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let ax2x: Real = o.topCenter.x - o.bottomCenter.x in
              let ax2y: Real = o.topCenter.y - o.bottomCenter.y in
              let ax2z: Real = o.topCenter.z - o.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let halfH2: Real = (ax2x*ax2x + ax2y*ax2y + ax2z*ax2z) / 4.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let rSum: Real = self.outerRadius + o.outerRadius in
              dx*dx + dy*dy + dz*dz >=
                (rSum + halfH1 + halfH2) * (rSum + halfH1 + halfH2)
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Tubes should not intersect with tubes in foreign namespaces");
  }

  /** Tests that tubes from different namespaces do not intersect with boxes. */
  @Test
  void testTubeNoIntersectWithBox() {
    String constraint =
        """
        context cad::Tube inv noIntersectWithBox:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Box))
            .forAll(other |
              let o: cad::Box = other.oclAsType(cad::Box) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let boxCx: Real = o.origin.x + o.edgeVectorA.x/2.0
                                           + o.edgeVectorB.x/2.0
                                           + o.edgeVectorC.x/2.0 in
              let boxCy: Real = o.origin.y + o.edgeVectorA.y/2.0
                                           + o.edgeVectorB.y/2.0
                                           + o.edgeVectorC.y/2.0 in
              let boxCz: Real = o.origin.z + o.edgeVectorA.z/2.0
                                           + o.edgeVectorB.z/2.0
                                           + o.edgeVectorC.z/2.0 in
              let halfW: Real = (o.edgeVectorA.x*o.edgeVectorA.x +
                                 o.edgeVectorA.y*o.edgeVectorA.y +
                                 o.edgeVectorA.z*o.edgeVectorA.z) / 2.0 in
              let halfH2: Real = (o.edgeVectorB.x*o.edgeVectorB.x +
                                  o.edgeVectorB.y*o.edgeVectorB.y +
                                  o.edgeVectorB.z*o.edgeVectorB.z) / 2.0 in
              let halfD: Real = (o.edgeVectorC.x*o.edgeVectorC.x +
                                 o.edgeVectorC.y*o.edgeVectorC.y +
                                 o.edgeVectorC.z*o.edgeVectorC.z) / 2.0 in
              let dx: Real = cx1 - boxCx in
              let dy: Real = cy1 - boxCy in
              let dz: Real = cz1 - boxCz in
              let bound: Real = self.outerRadius + halfH1 + halfW + halfH2 + halfD in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Tubes should not intersect with boxes in foreign namespaces");
  }

  /** Tests that tubes from different namespaces do not intersect with prisms. */
  @Test
  void testTubeNoIntersectWithPrism() {
    String constraint =
        """
        context cad::Tube inv noIntersectWithPrism:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Prism))
            .forAll(other |
              let o: cad::Prism = other.oclAsType(cad::Prism) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let verts: OrderedSet(Coordinate) = o.baseProfile.vertices in
              let minX: Real = verts.collect(v | v.x).min() in
              let maxX: Real = verts.collect(v | v.x).max() in
              let minY: Real = verts.collect(v | v.y).min() in
              let maxY: Real = verts.collect(v | v.y).max() in
              let minZ: Real = verts.collect(v | v.z).min() in
              let maxZ: Real = verts.collect(v | v.z).max() in
              let extMaxX: Real =
                if o.extrusion.x > 0.0 then maxX + o.extrusion.x else maxX endif in
              let extMinX: Real =
                if o.extrusion.x < 0.0 then minX + o.extrusion.x else minX endif in
              let extMaxY: Real =
                if o.extrusion.y > 0.0 then maxY + o.extrusion.y else maxY endif in
              let extMinY: Real =
                if o.extrusion.y < 0.0 then minY + o.extrusion.y else minY endif in
              let extMaxZ: Real =
                if o.extrusion.z > 0.0 then maxZ + o.extrusion.z else maxZ endif in
              let extMinZ: Real =
                if o.extrusion.z < 0.0 then minZ + o.extrusion.z else minZ endif in
              let pcx: Real = (extMinX + extMaxX) / 2.0 in
              let pcy: Real = (extMinY + extMaxY) / 2.0 in
              let pcz: Real = (extMinZ + extMaxZ) / 2.0 in
              let dx: Real = cx1 - pcx in
              let dy: Real = cy1 - pcy in
              let dz: Real = cz1 - pcz in
              let bound: Real = self.outerRadius + halfH1 +
                                (extMaxX - extMinX) / 2.0 +
                                (extMaxY - extMinY) / 2.0 +
                                (extMaxZ - extMinZ) / 2.0 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Tubes should not intersect with prisms in foreign namespaces");
  }

  /** Tests that tubes from different namespaces do not intersect with pyramids. */
  @Test
  void testTubeNoIntersectWithPyramid() {
    String constraint =
        """
        context cad::Tube inv noIntersectWithPyramid:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Pyramid))
            .forAll(other |
              let o: cad::Pyramid = other.oclAsType(cad::Pyramid) in
              let cx1: Real = (self.bottomCenter.x + self.topCenter.x) / 2.0 in
              let cy1: Real = (self.bottomCenter.y + self.topCenter.y) / 2.0 in
              let cz1: Real = (self.bottomCenter.z + self.topCenter.z) / 2.0 in
              let ax1x: Real = self.topCenter.x - self.bottomCenter.x in
              let ax1y: Real = self.topCenter.y - self.bottomCenter.y in
              let ax1z: Real = self.topCenter.z - self.bottomCenter.z in
              let halfH1: Real = (ax1x*ax1x + ax1y*ax1y + ax1z*ax1z) / 4.0 in
              let dx: Real = cx1 - o.apex.x in
              let dy: Real = cy1 - o.apex.y in
              let dz: Real = cz1 - o.apex.z in
              let bound: Real = self.outerRadius + halfH1 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Tubes should not intersect with pyramids in foreign namespaces");
  }

  // ---------------------------------------------------------------------------
  // CAD non-intersection constraints: Box
  // ---------------------------------------------------------------------------

  /** Tests that boxes from different namespaces do not intersect with other boxes. */
  @Test
  void testBoxNoIntersectWithBox() {
    String constraint =
        """
        context cad::Box inv noIntersectWithBox:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Box))
            .forAll(other |
              let o: cad::Box = other.oclAsType(cad::Box) in
              let cx1: Real = self.origin.x + self.edgeVectorA.x/2.0
                                            + self.edgeVectorB.x/2.0
                                            + self.edgeVectorC.x/2.0 in
              let cy1: Real = self.origin.y + self.edgeVectorA.y/2.0
                                            + self.edgeVectorB.y/2.0
                                            + self.edgeVectorC.y/2.0 in
              let cz1: Real = self.origin.z + self.edgeVectorA.z/2.0
                                            + self.edgeVectorB.z/2.0
                                            + self.edgeVectorC.z/2.0 in
              let cx2: Real = o.origin.x + o.edgeVectorA.x/2.0
                                         + o.edgeVectorB.x/2.0
                                         + o.edgeVectorC.x/2.0 in
              let cy2: Real = o.origin.y + o.edgeVectorA.y/2.0
                                         + o.edgeVectorB.y/2.0
                                         + o.edgeVectorC.y/2.0 in
              let cz2: Real = o.origin.z + o.edgeVectorA.z/2.0
                                         + o.edgeVectorB.z/2.0
                                         + o.edgeVectorC.z/2.0 in
              let half1W: Real = (self.edgeVectorA.x*self.edgeVectorA.x +
                                  self.edgeVectorA.y*self.edgeVectorA.y +
                                  self.edgeVectorA.z*self.edgeVectorA.z) / 2.0 in
              let half1H: Real = (self.edgeVectorB.x*self.edgeVectorB.x +
                                  self.edgeVectorB.y*self.edgeVectorB.y +
                                  self.edgeVectorB.z*self.edgeVectorB.z) / 2.0 in
              let half1D: Real = (self.edgeVectorC.x*self.edgeVectorC.x +
                                  self.edgeVectorC.y*self.edgeVectorC.y +
                                  self.edgeVectorC.z*self.edgeVectorC.z) / 2.0 in
              let half2W: Real = (o.edgeVectorA.x*o.edgeVectorA.x +
                                  o.edgeVectorA.y*o.edgeVectorA.y +
                                  o.edgeVectorA.z*o.edgeVectorA.z) / 2.0 in
              let half2H: Real = (o.edgeVectorB.x*o.edgeVectorB.x +
                                  o.edgeVectorB.y*o.edgeVectorB.y +
                                  o.edgeVectorB.z*o.edgeVectorB.z) / 2.0 in
              let half2D: Real = (o.edgeVectorC.x*o.edgeVectorC.x +
                                  o.edgeVectorC.y*o.edgeVectorC.y +
                                  o.edgeVectorC.z*o.edgeVectorC.z) / 2.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let bound: Real = half1W + half1H + half1D + half2W + half2H + half2D in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(result.isSatisfied(), "Boxes should not intersect with boxes in foreign namespaces");
  }

  /** Tests that boxes from different namespaces do not intersect with prisms. */
  @Test
  void testBoxNoIntersectWithPrism() {
    String constraint =
        """
        context cad::Box inv noIntersectWithPrism:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Prism))
            .forAll(other |
              let o: cad::Prism = other.oclAsType(cad::Prism) in
              let cx1: Real = self.origin.x + self.edgeVectorA.x/2.0
                                            + self.edgeVectorB.x/2.0
                                            + self.edgeVectorC.x/2.0 in
              let cy1: Real = self.origin.y + self.edgeVectorA.y/2.0
                                            + self.edgeVectorB.y/2.0
                                            + self.edgeVectorC.y/2.0 in
              let cz1: Real = self.origin.z + self.edgeVectorA.z/2.0
                                            + self.edgeVectorC.z/2.0
                                            + self.edgeVectorC.z/2.0 in
              let half1W: Real = (self.edgeVectorA.x*self.edgeVectorA.x +
                                  self.edgeVectorA.y*self.edgeVectorA.y +
                                  self.edgeVectorA.z*self.edgeVectorA.z) / 2.0 in
              let half1H: Real = (self.edgeVectorB.x*self.edgeVectorB.x +
                                  self.edgeVectorB.y*self.edgeVectorB.y +
                                  self.edgeVectorB.z*self.edgeVectorB.z) / 2.0 in
              let half1D: Real = (self.edgeVectorC.x*self.edgeVectorC.x +
                                  self.edgeVectorC.y*self.edgeVectorC.y +
                                  self.edgeVectorC.z*self.edgeVectorC.z) / 2.0 in
              let verts: OrderedSet(Coordinate) = o.baseProfile.vertices in
              let minX: Real = verts.collect(v | v.x).min() in
              let maxX: Real = verts.collect(v | v.x).max() in
              let minY: Real = verts.collect(v | v.y).min() in
              let maxY: Real = verts.collect(v | v.y).max() in
              let minZ: Real = verts.collect(v | v.z).min() in
              let maxZ: Real = verts.collect(v | v.z).max() in
              let extMaxX: Real =
                if o.extrusion.x > 0.0 then maxX + o.extrusion.x else maxX endif in
              let extMinX: Real =
                if o.extrusion.x < 0.0 then minX + o.extrusion.x else minX endif in
              let extMaxY: Real =
                if o.extrusion.y > 0.0 then maxY + o.extrusion.y else maxY endif in
              let extMinY: Real =
                if o.extrusion.y < 0.0 then minY + o.extrusion.y else minY endif in
              let extMaxZ: Real =
                if o.extrusion.z > 0.0 then maxZ + o.extrusion.z else maxZ endif in
              let extMinZ: Real =
                if o.extrusion.z < 0.0 then minZ + o.extrusion.z else minZ endif in
              let pcx: Real = (extMinX + extMaxX) / 2.0 in
              let pcy: Real = (extMinY + extMaxY) / 2.0 in
              let pcz: Real = (extMinZ + extMaxZ) / 2.0 in
              let dx: Real = cx1 - pcx in
              let dy: Real = cy1 - pcy in
              let dz: Real = cz1 - pcz in
              let bound: Real = half1W + half1H + half1D +
                                (extMaxX - extMinX) / 2.0 +
                                (extMaxY - extMinY) / 2.0 +
                                (extMaxZ - extMinZ) / 2.0 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Boxes should not intersect with prisms in foreign namespaces");
  }

  /** Tests that boxes from different namespaces do not intersect with pyramids. */
  @Test
  void testBoxNoIntersectWithPyramid() {
    String constraint =
        """
        context cad::Box inv noIntersectWithPyramid:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Pyramid))
            .forAll(other |
              let o: cad::Pyramid = other.oclAsType(cad::Pyramid) in
              let cx1: Real = self.origin.x + self.edgeVectorA.x/2.0
                                            + self.edgeVectorB.x/2.0
                                            + self.edgeVectorC.x/2.0 in
              let cy1: Real = self.origin.y + self.edgeVectorA.y/2.0
                                            + self.edgeVectorB.y/2.0
                                            + self.edgeVectorC.y/2.0 in
              let cz1: Real = self.origin.z + self.edgeVectorA.z/2.0
                                            + self.edgeVectorB.z/2.0
                                            + self.edgeVectorC.z/2.0 in
              let half1W: Real = (self.edgeVectorA.x*self.edgeVectorA.x +
                                  self.edgeVectorA.y*self.edgeVectorA.y +
                                  self.edgeVectorA.z*self.edgeVectorA.z) / 2.0 in
              let half1H: Real = (self.edgeVectorB.x*self.edgeVectorB.x +
                                  self.edgeVectorB.y*self.edgeVectorB.y +
                                  self.edgeVectorB.z*self.edgeVectorB.z) / 2.0 in
              let half1D: Real = (self.edgeVectorC.x*self.edgeVectorC.x +
                                  self.edgeVectorC.y*self.edgeVectorC.y +
                                  self.edgeVectorC.z*self.edgeVectorC.z) / 2.0 in
              let dx: Real = cx1 - o.apex.x in
              let dy: Real = cy1 - o.apex.y in
              let dz: Real = cz1 - o.apex.z in
              let bound: Real = half1W + half1H + half1D in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Boxes should not intersect with pyramids in foreign namespaces");
  }

  // ---------------------------------------------------------------------------
  // CAD non-intersection constraints: Prism
  // ---------------------------------------------------------------------------

  /** Tests that prisms from different namespaces do not intersect with other prisms. */
  @Test
  void testPrismNoIntersectWithPrism() {
    String constraint =
        """
        context cad::Prism inv noIntersectWithPrism:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Prism))
            .forAll(other |
              let o: cad::Prism = other.oclAsType(cad::Prism) in
              let v1: OrderedSet(Coordinate) = self.baseProfile.vertices in
              let minX1: Real = v1.collect(v | v.x).min() in
              let maxX1: Real = v1.collect(v | v.x).max() in
              let minY1: Real = v1.collect(v | v.y).min() in
              let maxY1: Real = v1.collect(v | v.y).max() in
              let minZ1: Real = v1.collect(v | v.z).min() in
              let maxZ1: Real = v1.collect(v | v.z).max() in
              let eMaxX1: Real =
                if self.extrusion.x > 0.0 then maxX1 + self.extrusion.x else maxX1 endif in
              let eMinX1: Real =
                if self.extrusion.x < 0.0 then minX1 + self.extrusion.x else minX1 endif in
              let eMaxY1: Real =
                if self.extrusion.y > 0.0 then maxY1 + self.extrusion.y else maxY1 endif in
              let eMinY1: Real =
                if self.extrusion.y < 0.0 then minY1 + self.extrusion.y else minY1 endif in
              let eMaxZ1: Real =
                if self.extrusion.z > 0.0 then maxZ1 + self.extrusion.z else maxZ1 endif in
              let eMinZ1: Real =
                if self.extrusion.z < 0.0 then minZ1 + self.extrusion.z else minZ1 endif in
              let cx1: Real = (eMinX1 + eMaxX1) / 2.0 in
              let cy1: Real = (eMinY1 + eMaxY1) / 2.0 in
              let cz1: Real = (eMinZ1 + eMaxZ1) / 2.0 in
              let v2: OrderedSet(Coordinate) = o.baseProfile.vertices in
              let minX2: Real = v2.collect(v | v.x).min() in
              let maxX2: Real = v2.collect(v | v.x).max() in
              let minY2: Real = v2.collect(v | v.y).min() in
              let maxY2: Real = v2.collect(v | v.y).max() in
              let minZ2: Real = v2.collect(v | v.z).min() in
              let maxZ2: Real = v2.collect(v | v.z).max() in
              let eMaxX2: Real =
                if o.extrusion.x > 0.0 then maxX2 + o.extrusion.x else maxX2 endif in
              let eMinX2: Real =
                if o.extrusion.x < 0.0 then minX2 + o.extrusion.x else minX2 endif in
              let eMaxY2: Real =
                if o.extrusion.y > 0.0 then maxY2 + o.extrusion.y else maxY2 endif in
              let eMinY2: Real =
                if o.extrusion.y < 0.0 then minY2 + o.extrusion.y else minY2 endif in
              let eMaxZ2: Real =
                if o.extrusion.z > 0.0 then maxZ2 + o.extrusion.z else maxZ2 endif in
              let eMinZ2: Real =
                if o.extrusion.z < 0.0 then minZ2 + o.extrusion.z else minZ2 endif in
              let cx2: Real = (eMinX2 + eMaxX2) / 2.0 in
              let cy2: Real = (eMinY2 + eMaxY2) / 2.0 in
              let cz2: Real = (eMinZ2 + eMaxZ2) / 2.0 in
              let dx: Real = cx1 - cx2 in
              let dy: Real = cy1 - cy2 in
              let dz: Real = cz1 - cz2 in
              let bound: Real =
                (eMaxX1 - eMinX1) / 2.0 + (eMaxY1 - eMinY1) / 2.0 +
                (eMaxZ1 - eMinZ1) / 2.0 + (eMaxX2 - eMinX2) / 2.0 +
                (eMaxY2 - eMinY2) / 2.0 + (eMaxZ2 - eMinZ2) / 2.0 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Prisms should not intersect with prisms in foreign namespaces");
  }

  /** Tests that prisms from different namespaces do not intersect with pyramids. */
  @Test
  void testPrismNoIntersectWithPyramid() {
    String constraint =
        """
        context cad::Prism inv noIntersectWithPyramid:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Pyramid))
            .forAll(other |
              let o: cad::Pyramid = other.oclAsType(cad::Pyramid) in
              let v1: OrderedSet(Coordinate) = self.baseProfile.vertices in
              let minX1: Real = v1.collect(v | v.x).min() in
              let maxX1: Real = v1.collect(v | v.x).max() in
              let minY1: Real = v1.collect(v | v.y).min() in
              let maxY1: Real = v1.collect(v | v.y).max() in
              let minZ1: Real = v1.collect(v | v.z).min() in
              let maxZ1: Real = v1.collect(v | v.z).max() in
              let eMaxX1: Real =
                if self.extrusion.x > 0.0 then maxX1 + self.extrusion.x else maxX1 endif in
              let eMinX1: Real =
                if self.extrusion.x < 0.0 then minX1 + self.extrusion.x else minX1 endif in
              let eMaxY1: Real =
                if self.extrusion.y > 0.0 then maxY1 + self.extrusion.y else maxY1 endif in
              let eMinY1: Real =
                if self.extrusion.y < 0.0 then minY1 + self.extrusion.y else minY1 endif in
              let eMaxZ1: Real =
                if self.extrusion.z > 0.0 then maxZ1 + self.extrusion.z else maxZ1 endif in
              let eMinZ1: Real =
                if self.extrusion.z < 0.0 then minZ1 + self.extrusion.z else minZ1 endif in
              let cx1: Real = (eMinX1 + eMaxX1) / 2.0 in
              let cy1: Real = (eMinY1 + eMaxY1) / 2.0 in
              let cz1: Real = (eMinZ1 + eMaxZ1) / 2.0 in
              let dx: Real = cx1 - o.apex.x in
              let dy: Real = cy1 - o.apex.y in
              let dz: Real = cz1 - o.apex.z in
              let bound: Real =
                (eMaxX1 - eMinX1) / 2.0 +
                (eMaxY1 - eMinY1) / 2.0 +
                (eMaxZ1 - eMinZ1) / 2.0 in
              dx*dx + dy*dy + dz*dz >= bound * bound
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Prisms should not intersect with pyramids in foreign namespaces");
  }

  // ---------------------------------------------------------------------------
  // CAD non-intersection constraints: Pyramid
  // ---------------------------------------------------------------------------

  /** Tests that pyramids from different namespaces do not share the same apex point. */
  @Test
  void testPyramidNoIntersectWithPyramid() {
    String constraint =
        """
        context cad::Pyramid inv noIntersectWithPyramid:
          let foreignShapes =
            cad::Namespace.allInstances().reject(ns | ns.shapes.includes(self))
              .collect(ns | ns.shapes).flatten()
          in
          foreignShapes.select(s | s.oclIsTypeOf(cad::Pyramid))
            .forAll(other |
              let o: cad::Pyramid = other.oclAsType(cad::Pyramid) in
              let dx: Real = self.apex.x - o.apex.x in
              let dy: Real = self.apex.y - o.apex.y in
              let dz: Real = self.apex.z - o.apex.z in
              dx*dx + dy*dy + dz*dz > 0.0
            )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(constraint, new Path[] {CAD_ECORE}, new Path[] {CAD_INSTANCE});

    assertTrue(result.isSuccess(), "Evaluation should succeed: " + result.toDetailedErrorString());
    assertTrue(
        result.isSatisfied(), "Pyramids should not share the same apex in foreign namespaces");
  }
}
