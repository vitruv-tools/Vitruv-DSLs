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

/** Type Matrix: oclIsTypeOf(T) — recv x target type (exact match) */
class OclIsTypeOfMetamodelTest {
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
  @MethodSource("typeOfConstraints")
  void testTypeOfConstraintSatisfied(String constraint) {
    ConstraintResult r = evalCad(constraint);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
    assertTrue(r.isSatisfied());
  }

  static Stream<String> typeOfConstraints() {
    return Stream.of(
        // Exact type match → true
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsTypeOf(cad::Coordinate))""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::NumericParameter))
            .forAll(p | p.oclIsTypeOf(cad::NumericParameter))""",
        "context cad::Sphere inv:\n  self.oclIsTypeOf(cad::Sphere)",
        "context cad::Cylinder inv:\n  self.oclIsTypeOf(cad::Cylinder)",
        "context cad::Namespace inv:\n  self.oclIsTypeOf(cad::Namespace)",
        // Supertype target → false
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsTypeOf(cad::Parameter) == false)""",
        "context cad::Sphere inv:\n  self.oclIsTypeOf(cad::Shape) == false",
        "context cad::Cylinder inv:\n  self.oclIsTypeOf(cad::Shape) == false",
        // Sibling type → false
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .forAll(p | p.oclIsTypeOf(cad::NumericParameter) == false)""",
        "context cad::Sphere inv:\n  self.oclIsTypeOf(cad::Cylinder) == false",
        // Filtering exact type from mixed collection
        """
        context cad::Namespace inv:
          self.name == "id3_front_left_caliper_namespace" implies
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size() == 4""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate)).size()
            < self.parameters.select(p | p.oclIsKindOf(cad::Parameter)).size()""",
        """
        context cad::Namespace inv:
          self.parameters.select(p | p.oclIsTypeOf(cad::Coordinate))
            .collect(p | p.oclAsType(cad::Coordinate).x)
            .forAll(x | x >= 0 or x < 0)""",
        """
        context cad::Namespace inv:
          self.shapes.select(s | s.oclIsTypeOf(cad::Sphere))
            .forAll(s | s.oclIsTypeOf(cad::Cylinder) == false)""");
  }
}
