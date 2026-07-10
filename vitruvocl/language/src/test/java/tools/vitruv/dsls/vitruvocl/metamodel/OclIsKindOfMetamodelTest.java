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

/** Type Matrix: oclIsKindOf(T) — recv x target type (inheritance) */
class OclIsKindOfMetamodelTest {
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
  @MethodSource("kindOfConstraints")
  void testKindOfConstraintSatisfied(String constraint) {
    ConstraintResult r = evalCad(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> kindOfConstraints() {
    return Stream.of(
        // Same type → true
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsKindOf(cad::Coordinate))""",
        "context cad::Sphere inv:\n  self.oclIsKindOf(cad::Sphere)",
        "context cad::Cylinder inv:\n  self.oclIsKindOf(cad::Cylinder)",
        "context cad::Namespace inv:\n  self.oclIsKindOf(cad::Namespace)",
        // Subtype → supertype: true
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsKindOf(cad::Parameter))""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::NumericParameter))
            .forAll(p | p.oclIsKindOf(cad::Parameter))""",
        "context cad::Sphere inv:\n  self.oclIsKindOf(cad::Shape)",
        "context cad::Cylinder inv:\n  self.oclIsKindOf(cad::Shape)",
        "context cad::Cone inv:\n  self.oclIsKindOf(cad::Shape)",
        "context cad::Tube inv:\n  self.oclIsKindOf(cad::Shape)",
        // Sibling / unrelated → false
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsKindOf(cad::NumericParameter) == false)""",
        "context cad::Sphere inv:\n  self.oclIsKindOf(cad::Cylinder) == false",
        "context cad::Sphere inv:\n  self.oclIsKindOf(cad::Namespace) == false",
        "context cad::Namespace inv:\n  self.oclIsKindOf(cad::Shape) == false",
        // Collection usage
        """
        context cad::Namespace inv:
          self.parameters.forAll(p | p.oclIsKindOf(cad::Parameter))""",
        """
        context cad::Namespace inv:
          self.shapes.forAll(s | s.oclIsKindOf(cad::Shape))""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()
            == self.parameters.size()""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()
            >= self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size()""");
  }
}
