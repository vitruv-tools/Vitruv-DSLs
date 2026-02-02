package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/**
 * Integration tests for cross-metamodel OCL constraints in VitruvOCL.
 *
 * <p>This test suite validates the ability to write and evaluate OCL constraints that span multiple
 * metamodels, a key feature for consistency checking in Vitruvius VSUMs (Virtual Single Underlying
 * Models). The tests use two sample metamodels:
 *
 * <ul>
 *   <li><b>SpaceMission</b>: Contains Spacecraft entities with attributes like serialNumber, mass,
 *       and operational status
 *   <li><b>SatelliteSystem</b>: Contains Satellite entities with attributes like serialNumber,
 *       massKg, and active status
 * </ul>
 *
 * <p>Cross-metamodel constraints are expressed using fully qualified names (e.g., {@code
 * satelliteSystem::Satellite.allInstances()}) and can reference entities from different metamodels
 * within a single constraint expression.
 *
 * <p><b>Example constraint pattern:</b>
 *
 * <pre>{@code
 * context spaceMission::Spacecraft inv:
 *   satelliteSystem::Satellite.allInstances().exists(sat |
 *     sat.serialNumber == self.serialNumber
 *   )
 * }</pre>
 *
 * @see VitruvOCL#evaluate(String, Path[], Path...)
 */
public class CrossMetamodelConstraintTest {

  // ==================== Metamodel Definitions ====================

  /** Path to the SpaceMission metamodel (Ecore file). */
  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spacemission.ecore");

  /** Path to the SatelliteSystem metamodel (Ecore file). */
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  // ==================== SpaceMission Model Instances ====================

  /**
   * SpaceMission model instance: Spacecraft "Voyager" with serialNumber "SC-001". Used to test
   * matching with corresponding Satellite instance.
   */
  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");

  /**
   * SpaceMission model instance: Spacecraft "Atlas" with different attributes. Used to test
   * mass/operational status consistency checks.
   */
  private static final Path SPACECRAFT_ATLAS = Path.of("spacecraft-atlas.spacemission");

  // ==================== Satellite Model Instances ====================

  /**
   * SatelliteSystem model instance: Satellite with serialNumber "SC-001" matching Voyager. Used for
   * positive matching tests.
   */
  private static final Path SATELLITE_VOYAGER = Path.of("satellite-voyager.satellitesystem");

  /** SatelliteSystem model instance: Satellite "Atlas" for mass consistency tests. */
  private static final Path SATELLITE_ATLAS = Path.of("satellite-atlas.satellitesystem");

  /**
   * SatelliteSystem model instance: Satellite "Hubble" with serialNumber "SAT-099". Used for
   * negative matching tests (no corresponding Spacecraft).
   */
  private static final Path SATELLITE_HUBBLE = Path.of("satellite-hubble.satellitesystem");

  // ==================== Setup ====================

  /**
   * Configures the base path for test model resolution.
   *
   * <p>The {@link MetamodelWrapper} uses this path to resolve relative model file paths passed to
   * {@link VitruvOCL#evaluate}.
   */
  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  // ==================== Cross-Metamodel Constraint Tests ====================

  /**
   * Tests basic cross-metamodel entity matching via serial numbers.
   *
   * <p><b>Constraint:</b> A Spacecraft with serialNumber "SC-001" should have a corresponding
   * Satellite with the same serialNumber.
   *
   * <p><b>Expected:</b> The constraint evaluates to {@code true} when both Voyager spacecraft and
   * satellite (both with "SC-001") are present.
   *
   * @throws Exception if constraint evaluation fails
   */
  @Test
  public void testCrossMetamodelSerialNumberMatch() throws Exception {
    // Constraint: Spacecraft with serialNumber "SC-001" corresponds to Satellite with same
    // serialNumber
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          )
        """;

    // Evaluate constraint with both metamodels and matching instances
    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    // Verify result structure and value
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Spacecraft SC-001 should find matching Satellite SC-001");
  }

  /**
   * Tests cross-metamodel consistency checking with implication logic.
   *
   * <p><b>Constraint:</b> For all Satellites, if their {@code massKg} matches a Spacecraft's {@code
   * mass}, then their {@code active} status must match the Spacecraft's {@code operational} status.
   *
   * <p><b>Expected:</b> The constraint evaluates to {@code true} when mass and status fields are
   * consistent across metamodels.
   *
   * @throws Exception if constraint evaluation fails
   */
  @Test
  public void testCrossMetamodelMassConsistency() throws Exception {
    // Constraint: If Spacecraft mass matches Satellite massKg, operational should match active
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.massKg == self.mass implies sat.active == self.operational
          )
        """;

    // Evaluate with Atlas spacecraft and satellite instances
    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_ATLAS,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Matching mass should imply matching operational/active status");
  }

  /**
   * Tests that non-matching serial numbers correctly fail the existence check.
   *
   * <p><b>Constraint:</b> Same as {@link #testCrossMetamodelSerialNumberMatch()}, but with
   * mismatched instances.
   *
   * <p><b>Expected:</b> The constraint evaluates to {@code false} when Voyager spacecraft (SC-001)
   * is checked against Hubble satellite (SAT-099).
   *
   * @throws Exception if constraint evaluation fails
   */
  @Test
  public void testCrossMetamodelNoMatchingSerialNumber() throws Exception {
    // Constraint: Check for non-matching serial numbers
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          )
        """;

    // Evaluate with Voyager spacecraft but Hubble satellite (different serial numbers)
    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_HUBBLE);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertFalse(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Spacecraft SC-001 should NOT match Satellite SAT-099");
  }

  /**
   * Tests constraint evaluation with multiple model instances from both metamodels.
   *
   * <p><b>Constraint:</b> Each Spacecraft should have at least one Satellite with a matching
   * serialNumber (using {@code select} and {@code size()}).
   *
   * <p><b>Expected:</b> The constraint evaluates once per Spacecraft instance (2 evaluations for 2
   * spacecraft), demonstrating proper context iteration with multiple models.
   *
   * @throws Exception if constraint evaluation fails
   */
  @Test
  public void testMultipleModelsFromBothMetamodels() throws Exception {
    // Constraint: Count matching entities across metamodels
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            sat.serialNumber == self.serialNumber
          ).size() >= 1
        """;

    // Evaluate with multiple instances from both metamodels
    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SPACECRAFT_ATLAS,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS,
            SATELLITE_HUBBLE);

    assertNotNull(result);
    assertEquals(2, result.size(), "Should evaluate for both spacecraft instances");
  }

  /**
   * Tests cross-metamodel constraints from the opposite direction (Satellite → Spacecraft).
   *
   * <p><b>Constraint:</b> A Satellite should have a corresponding Spacecraft with matching {@code
   * serialNumber} and {@code mass}/{@code massKg}.
   *
   * <p><b>Expected:</b> The constraint evaluates to {@code true}, demonstrating that
   * cross-metamodel constraints work bidirectionally.
   *
   * <p><b>Note:</b> This test validates that the constraint context can be set to either metamodel,
   * not just the "primary" one.
   *
   * @throws Exception if constraint evaluation fails
   */
  @Test
  public void testSatelliteReferencesSpacecraft() throws Exception {
    // Constraint from Satellite perspective
    String constraint =
        """
        context satelliteSystem::Satellite inv:
          spaceMission::Spacecraft.allInstances().exists(sc |
            sc.serialNumber == self.serialNumber and sc.mass == self.massKg
          )
        """;

    // Evaluate with Satellite as context, referencing Spacecraft
    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Satellite should find corresponding Spacecraft with matching attributes");
  }
}