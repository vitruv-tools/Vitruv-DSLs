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
 *******************************************************************************/
package tools.vitruv.dsls.vitruvocl.correspondence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/**
 * Tests for the correspondence operator (~) in VitruvOCL.
 *
 * <p>This test suite validates the correspondence operator that enables checking correspondence
 * relationships between model elements across metamodels. The operator uses explicit Correspondence
 * model instances to determine if two objects are related, supporting Vitruvius-style model
 * consistency checking.
 *
 * <p>The ~ operator is a BINARY PREDICATE that returns Boolean:
 *
 * <ul>
 *   <li>Syntax: {@code obj1 ~ obj2}
 *   <li>Returns: {@code true} if obj1 and obj2 are in the same Correspondence, {@code false}
 *       otherwise
 *   <li>Main usage: In {@code select()}, {@code forAll()}, {@code exists()} for filtering
 * </ul>
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Basic correspondence checking (obj1 ~ obj2 returns Boolean)
 *   <li>Use in select statements for filtering
 *   <li>Use in forAll/exists for consistency checking
 *   <li>Bidirectional correspondence lookup
 *   <li>Error cases (collections, primitives, invalid types)
 * </ul>
 *
 * <h2>Test Setup</h2>
 *
 * Uses three metamodels:
 *
 * <ul>
 *   <li>spaceMission: Contains Spacecraft objects
 *   <li>satelliteSystem: Contains Satellite objects
 *   <li>correspondence: Vitruvius correspondence model linking Spacecraft ↔ Satellite
 * </ul>
 */
class CorrespondenceOperatorTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");
  private static final Path CORRESPONDENCE_ECORE =
      Path.of("src/test/resources/test-metamodels/correspondence.ecore");
  private static final Path[] ECORES = {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE};

  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ATLAS = Path.of("spacecraft-atlas.spacemission");
  private static final Path SATELLITE_VOYAGER = Path.of("satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS = Path.of("satellite-atlas.satellitesystem");
  private static final Path SATELLITE_HUBBLE = Path.of("satellite-hubble.satellitesystem");
  private static final Path CORRESPONDENCES = Path.of("correspondences.correspondence");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  private static ConstraintResult eval(String c, Path... instances) {
    return VitruvOCL.evaluateConstraint(c, ECORES, instances);
  }

  // ── Parameterized: satisfied constraints ─────────────────────────────────

  @ParameterizedTest
  @MethodSource("satisfiedConstraints")
  void testConstraintSatisfied(String c, Path[] instances) {
    ConstraintResult r = VitruvOCL.evaluateConstraint(c, ECORES, instances);
    assertTrue(r.isSuccess(), "Evaluation should succeed");
    assertTrue(r.isSatisfied());
  }

  static Stream<Arguments> satisfiedConstraints() {
    Path[] voyager = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES};
    Path[] multiSat = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, CORRESPONDENCES};
    Path[] noCorr = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER};
    return Stream.of(
        // Basic correspondence in select
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(sat |
                self ~ sat).notEmpty()
            """,
            voyager),
        // Correspondence in forAll - mass consistency
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                self ~ sat implies sat.massKg == self.mass
              )
            """,
            voyager),
        // Multi-attribute consistency
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                self ~ sat implies (
                  sat.serialNumber == self.serialNumber and
                  sat.massKg == self.mass and
                  sat.active == self.operational
                )
              )
            """,
            voyager),
        // Correspondence in exists
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                self ~ sat and sat.active == true
              )
            """,
            voyager),
        // Implication
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                self ~ sat implies sat.active == self.operational
              )
            """,
            voyager),
        // Reject shorthand
        Arguments.of(
            """
            context satelliteSystem::Satellite inv:
              spaceMission::Spacecraft.allInstances().reject(sc |
                self ~ sc
              ).size() >= 0
            """,
            voyager),
        // Syntactic sugar: basic select(~)
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(~).notEmpty()
            """,
            voyager),
        // Syntactic sugar: reject(~)
        Arguments.of(
            """
            context satelliteSystem::Satellite inv:
              spaceMission::Spacecraft.allInstances().reject(~).size() >= 0
            """,
            voyager),
        // Multiple correspondences: size >= 1
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(sat |
                self ~ sat
              ).size() >= 1
            """,
            multiSat),
        // Multiple correspondences: forAll mass > 0
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                self ~ sat implies sat.massKg > 0
              )
            """,
            multiSat),
        // Syntactic sugar: select(~) size >= 1
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(~).size() >= 1
            """,
            multiSat),
        // No correspondence model → empty result
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(sat |
                self ~ sat
              ).isEmpty()
            """,
            noCorr)
    );
  }

  // ── Parameterized: success-only constraints ───────────────────────────────

  @ParameterizedTest
  @MethodSource("successOnlyConstraints")
  void testConstraintSuccess(String c, Path[] instances) {
    ConstraintResult r = VitruvOCL.evaluateConstraint(c, ECORES, instances);
    assertTrue(r.isSuccess(), "Evaluation should succeed");
  }

  static Stream<Arguments> successOnlyConstraints() {
    Path[] voyager = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES};
    Path[] multiSat = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, CORRESPONDENCES};
    return Stream.of(
        // AND combination
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              self.operational and
              satelliteSystem::Satellite.allInstances().exists(sat | self ~ sat)
            """,
            voyager),
        // Conditional
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              if satelliteSystem::Satellite.allInstances().exists(sat | self ~ sat)
              then satelliteSystem::Satellite.allInstances().forAll(sat |
                self ~ sat implies sat.active == true
              )
              else true
              endif
            """,
            voyager),
        // Negated correspondence
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                not (self ~ sat) or sat.massKg == self.mass
              )
            """,
            voyager),
        // Attribute filter
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(sat |
                self ~ sat and sat.massKg > 1000
              ).size() >= 0
            """,
            voyager),
        // Syntactic sugar: AND
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              self.operational and
              satelliteSystem::Satellite.allInstances().exists(~)
            """,
            voyager),
        // Syntactic sugar: conditional
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              if satelliteSystem::Satellite.allInstances().exists(~)
              then satelliteSystem::Satellite.allInstances().forAll(sat |
                self ~ sat implies sat.active == true
              )
              else true
              endif
            """,
            voyager),
        // Syntactic sugar: attribute filter
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(~)
                .select(sat | sat.massKg > 1000).size() >= 0
            """,
            voyager),
        // Nested iterators
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat1 |
                self ~ sat1 and
                satelliteSystem::Satellite.allInstances().exists(sat2 |
                  sat1.serialNumber != sat2.serialNumber
                )
              )
            """,
            multiSat)
    );
  }

  // ── Standalone tests ──────────────────────────────────────────────────────

  /** Tests correspondence returns false (no match): SC_ATLAS has no match with SAT_HUBBLE. */
  @Test
  void testCorrespondenceReturnsFalseWhenNoMatch() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            self ~ sat
          ).isEmpty()
        """;
    ConstraintResult result =
        eval(constraint, SPACECRAFT_ATLAS, SATELLITE_HUBBLE, CORRESPONDENCES);
    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Atlas spacecraft should have no correspondence to Hubble satellite");
  }

  /** Same as above but with select(~) shorthand. */
  @Test
  void testCorrespondenceReturnsFalseWhenNoMatchSyntacticSugarVersion() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(~).isEmpty()
        """;
    ConstraintResult result =
        eval(constraint, SPACECRAFT_ATLAS, SATELLITE_HUBBLE, CORRESPONDENCES);
    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Atlas spacecraft should have no correspondence to Hubble satellite");
  }

  /** Tests bidirectional correspondence: spacecraft → satellite AND satellite → spacecraft. */
  @Test
  void testBidirectionalCorrespondence() {
    String spacecraftConstraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            self ~ sat
          )
        """;
    String satelliteConstraint =
        """
        context satelliteSystem::Satellite inv:
          spaceMission::Spacecraft.allInstances().exists(sc |
            self ~ sc
          )
        """;
    ConstraintResult spacecraftResult =
        eval(spacecraftConstraint, SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES);
    ConstraintResult satelliteResult =
        eval(satelliteConstraint, SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES);
    assertTrue(spacecraftResult.isSuccess());
    assertTrue(spacecraftResult.isSatisfied(), "Spacecraft should find corresponding Satellite");
    assertTrue(satelliteResult.isSuccess());
    assertTrue(satelliteResult.isSatisfied(), "Satellite should find corresponding Spacecraft");
  }

  /** Tests bidirectional with exists(~) shorthand. */
  @Test
  void testBidirectionalCorrespondenceSyntacticSugarVersion() {
    String spacecraftConstraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(~)
        """;
    String satelliteConstraint =
        """
        context satelliteSystem::Satellite inv:
          spaceMission::Spacecraft.allInstances().exists(~)
        """;
    ConstraintResult spacecraftResult =
        eval(spacecraftConstraint, SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES);
    ConstraintResult satelliteResult =
        eval(satelliteConstraint, SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES);
    assertTrue(spacecraftResult.isSuccess());
    assertTrue(spacecraftResult.isSatisfied(), "Spacecraft should find corresponding Satellite");
    assertTrue(satelliteResult.isSuccess());
    assertTrue(satelliteResult.isSatisfied(), "Satellite should find corresponding Spacecraft");
  }

  /** Tests that ~ with a non-existent metamodel fails with a file error. */
  @Test
  void testCorrespondenceWithInvalidTargetType() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            self ~ nonExistent::FakeType
          )
        """;
    ConstraintResult result = eval(constraint, SPACECRAFT_VOYAGER, CORRESPONDENCES);
    assertFalse(result.isSuccess(), "Should fail with unknown metamodel");
    assertTrue(
        result.getFileErrors().stream()
            .anyMatch(
                err ->
                    err.toString().contains("nonExistent")
                        || err.toString().contains("Required metamodel")),
        "Error should mention missing metamodel 'nonExistent'");
  }
}
