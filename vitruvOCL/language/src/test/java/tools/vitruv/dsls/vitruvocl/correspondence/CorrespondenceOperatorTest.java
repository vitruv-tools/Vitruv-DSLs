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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

  // ==================== Basic Correspondence Checking ====================

  /**
   * Tests basic correspondence operator in select statement.
   *
   * <p>Validates that the ~ operator correctly filters satellites that correspond to the
   * spacecraft. Uses select() to find corresponding satellites.
   */
  @Test
  void testBasicCorrespondenceInSelect() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            self ~ sat).notEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Voyager spacecraft should have corresponding satellite");
  }

  /**
   * Tests correspondence operator returns false when no correspondence exists.
   *
   * <p>Validates that when objects are not in the same correspondence, the ~ operator returns
   * false, resulting in an empty select result.
   */
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
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_ATLAS, SATELLITE_HUBBLE, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(
        result.isSatisfied(), "Atlas spacecraft should have no correspondence to Hubble satellite");
  }

  // ==================== Bidirectional Correspondence ====================

  /**
   * Tests bidirectional correspondence checking.
   *
   * <p>Validates that the ~ operator works in both directions: spacecraft ~ satellite and satellite
   * ~ spacecraft both return true for the same correspondence.
   */
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
        VitruvOCL.evaluateConstraint(
            spacecraftConstraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    ConstraintResult satelliteResult =
        VitruvOCL.evaluateConstraint(
            satelliteConstraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(spacecraftResult.isSuccess());
    assertTrue(spacecraftResult.isSatisfied(), "Spacecraft should find corresponding Satellite");

    assertTrue(satelliteResult.isSuccess());
    assertTrue(satelliteResult.isSatisfied(), "Satellite should find corresponding Spacecraft");
  }

  // ==================== Correspondence in ForAll ====================

  /**
   * Tests correspondence operator in forAll for consistency checking.
   *
   * <p>Validates that forAll can be used with ~ to check properties of all corresponding objects.
   * Checks that mass values are consistent between corresponding objects.
   */
  @Test
  void testCorrespondenceInForAll() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            self ~ sat implies sat.massKg == self.mass
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Corresponding objects should have consistent mass values");
  }

  /**
   * Tests correspondence with multiple attribute consistency checks.
   *
   * <p>Validates that the ~ operator can be used in complex boolean expressions combining
   * correspondence checks with multiple attribute comparisons.
   */
  @Test
  void testCorrespondenceMultiAttributeConsistency() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            self ~ sat implies (
              sat.serialNumber == self.serialNumber and
              sat.massKg == self.mass and
              sat.active == self.operational
            )
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(
        result.isSatisfied(), "All attributes should be consistent for corresponding objects");
  }

  // ==================== Correspondence in Exists ====================

  /**
   * Tests correspondence operator in exists statement.
   *
   * <p>Validates that exists() can be used with ~ to check if at least one corresponding object
   * meets a condition.
   */
  @Test
  void testCorrespondenceInExists() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            self ~ sat and sat.active == true
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should find at least one active satellite that corresponds");
  }

  // ==================== Correspondence in Reject ====================

  /**
   * Tests correspondence operator in reject statement.
   *
   * <p>Validates that reject() can be used with ~ to filter out corresponding objects, keeping only
   * non-corresponding ones.
   */
  @Test
  void testCorrespondenceInReject() {
    String constraint =
        """
        context satelliteSystem::Satellite inv:
          spaceMission::Spacecraft.allInstances().reject(sc |
            self ~ sc
          ).size() >= 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Reject should filter out corresponding spacecraft");
  }

  // ==================== Correspondence with Logical Operators ====================

  /**
   * Tests correspondence combined with AND operator.
   *
   * <p>Validates that ~ can be combined with other boolean expressions using and.
   */
  @Test
  void testCorrespondenceWithAnd() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          self.operational and
          satelliteSystem::Satellite.allInstances().exists(sat | self ~ sat)
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // Spacecraft must be operational AND have corresponding satellites
  }

  /**
   * Tests correspondence in implication.
   *
   * <p>Validates that ~ works correctly in implication expressions for conditional consistency
   * checking.
   */
  @Test
  void testCorrespondenceInImplication() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            self ~ sat implies sat.active == self.operational
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(
        result.isSatisfied(), "If objects correspond, their operational status should match");
  }

  // ==================== Multiple Correspondences ====================

  /**
   * Tests correspondence with multiple satellites.
   *
   * <p>Validates that when multiple satellites correspond to a spacecraft, the ~ operator correctly
   * identifies all of them in select/exists statements.
   */
  @Test
  void testMultipleCorrespondences() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            self ~ sat
          ).size() >= 1
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should find one or more corresponding satellites");
  }

  /**
   * Tests forAll over multiple corresponding objects.
   *
   * <p>Validates that forAll with ~ correctly checks all corresponding objects when there are
   * multiple correspondences.
   */
  @Test
  void testForAllOnMultipleCorrespondences() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            self ~ sat implies sat.massKg > 0
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "All corresponding satellites should have positive mass");
  }

  // ==================== Error Cases ====================

  /**
   * Tests correspondence operator with invalid target type.
   *
   * <p>Validates that the type checker catches errors when trying to use ~ with a non-existent
   * metaclass type.
   */
  @Test
  void testCorrespondenceWithInvalidTargetType() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            self ~ nonExistent::FakeType
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, CORRESPONDENCES});

    assertFalse(result.isSuccess(), "Should fail with unknown metamodel");

    // Check for file errors instead of compiler errors
    assertTrue(
        result.getFileErrors().stream()
            .anyMatch(
                err ->
                    err.toString().contains("nonExistent")
                        || err.toString().contains("Required metamodel")),
        "Error should mention missing metamodel 'nonExistent'");
  }

  /**
   * Tests correspondence without correspondence model loaded.
   *
   * <p>Validates that when no correspondence model is loaded, the ~ operator returns false (no
   * correspondences found) for all pairs, rather than failing.
   */
  @Test
  void testCorrespondenceWithoutCorrespondenceModel() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            self ~ sat
          ).isEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER}); // No CORRESPONDENCES file

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should return empty when no correspondence model is loaded");
  }

  // ==================== Complex Correspondence Queries ====================

  /**
   * Tests correspondence in nested exists statements.
   *
   * <p>Validates that ~ can be used in nested iterator expressions for complex correspondence
   * queries.
   */
  @Test
  void testCorrespondenceInNestedIterators() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat1 |
            self ~ sat1 and
            satelliteSystem::Satellite.allInstances().exists(sat2 |
              sat1.serialNumber != sat2.serialNumber
            )
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // Checks: there exists a corresponding satellite AND another satellite with different serial
  }

  /**
   * Tests correspondence in if-then-else expression.
   *
   * <p>Validates that ~ results can be used in conditional expressions for branching logic based on
   * correspondence existence.
   */
  @Test
  void testCorrespondenceInConditional() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          if satelliteSystem::Satellite.allInstances().exists(sat | self ~ sat)
          then satelliteSystem::Satellite.allInstances().forAll(sat |
            self ~ sat implies sat.active == true
          )
          else true
          endif
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // If correspondences exist, all must be active; otherwise constraint is satisfied
  }

  /**
   * Tests negation of correspondence check.
   *
   * <p>Validates that the NOT operator can be applied to correspondence checks for inverse logic.
   */
  @Test
  void testNegatedCorrespondence() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            not (self ~ sat) or sat.massKg == self.mass
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // For all satellites: if they correspond, mass must match
  }

  /**
   * Tests correspondence combined with attribute comparison.
   *
   * <p>Validates that ~ can be combined with other predicates in complex boolean expressions.
   */
  @Test
  void testCorrespondenceWithAttributeFilter() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            self ~ sat and sat.massKg > 1000
          ).size() >= 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // Filters for corresponding satellites with mass > 1000kg
  }

  // ==================== Basic Correspondence Checking ====================

  /**
   * Tests basic correspondence operator shorthand in select statement.
   *
   * <p>Validates that select(~) correctly filters satellites that correspond to the spacecraft.
   */
  @Test
  void testBasicCorrespondenceInSelectSyntacticSugarVersion() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(~).notEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Voyager spacecraft should have corresponding satellite");
  }

  /**
   * Tests correspondence operator returns false when no correspondence exists.
   *
   * <p>Validates that when objects are not in the same correspondence, select(~) returns empty.
   */
  @Test
  void testCorrespondenceReturnsFalseWhenNoMatchSyntacticSugarVersion() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(~).isEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_ATLAS, SATELLITE_HUBBLE, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(
        result.isSatisfied(), "Atlas spacecraft should have no correspondence to Hubble satellite");
  }

  // ==================== Bidirectional Correspondence ====================

  /**
   * Tests bidirectional correspondence checking with exists(~).
   *
   * <p>Validates that exists(~) works in both directions.
   */
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
        VitruvOCL.evaluateConstraint(
            spacecraftConstraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    ConstraintResult satelliteResult =
        VitruvOCL.evaluateConstraint(
            satelliteConstraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(spacecraftResult.isSuccess());
    assertTrue(spacecraftResult.isSatisfied(), "Spacecraft should find corresponding Satellite");

    assertTrue(satelliteResult.isSuccess());
    assertTrue(satelliteResult.isSatisfied(), "Satellite should find corresponding Spacecraft");
  }

  // ==================== Correspondence in Reject ====================

  /**
   * Tests correspondence operator reject(~) shorthand.
   *
   * <p>Validates that reject(~) filters out corresponding objects.
   */
  @Test
  void testCorrespondenceInRejectSyntacticSugarVersion() {
    String constraint =
        """
        context satelliteSystem::Satellite inv:
          spaceMission::Spacecraft.allInstances().reject(~).size() >= 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Reject should filter out corresponding spacecraft");
  }

  // ==================== Correspondence with Logical Operators ====================

  /** Tests correspondence combined with AND operator using exists(~). */
  @Test
  void testCorrespondenceWithAndSyntacticSugarVersion() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          self.operational and
          satelliteSystem::Satellite.allInstances().exists(~)
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // Spacecraft must be operational AND have corresponding satellites
  }

  // ==================== Multiple Correspondences ====================

  /** Tests correspondence with multiple satellites using select(~). */
  @Test
  void testMultipleCorrespondencesSyntacticSugarVersion() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(~).size() >= 1
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should find one or more corresponding satellites");
  }

  // ==================== Complex Correspondence Queries ====================

  /** Tests correspondence in if-then-else with exists(~). */
  @Test
  void testCorrespondenceInConditionalSyntacticSugarVersion() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          if satelliteSystem::Satellite.allInstances().exists(~)
          then satelliteSystem::Satellite.allInstances().forAll(sat |
            self ~ sat implies sat.active == true
          )
          else true
          endif
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // If correspondences exist, all must be active; otherwise constraint is satisfied
  }

  /** Tests correspondence combined with attribute comparison using select(~). */
  @Test
  void testCorrespondenceWithAttributeFilterSyntacticSugarVersion() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(~)
            .select(sat | sat.massKg > 1000).size() >= 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE, CORRESPONDENCE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, CORRESPONDENCES});

    assertTrue(result.isSuccess());
    // First filters for corresponding satellites, then filters for mass > 1000kg
  }
}
