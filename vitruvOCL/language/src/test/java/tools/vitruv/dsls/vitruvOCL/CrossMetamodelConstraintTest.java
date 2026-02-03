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
  public void testdoppelSelect() throws Exception {
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

  /**
   * Tests nested select operations with multi-variable iterator expressions.
   *
   * <p><b>Constraint:</b> Find all pairs of Satellites where the two satellites are different
   * (testing multi-variable select with inequality condition).
   *
   * <p><b>Pattern:</b> {@code collection.select(e1, e2 | e1 != e2)} selects pairs of distinct
   * elements, useful for checking uniqueness constraints or finding conflicting entities.
   *
   * <p><b>Expected:</b> With 3 satellites, there should be 6 distinct pairs (3×2, excluding
   * self-pairs).
   *
   * @throws Exception if constraint evaluation fails
   */
  @Test
  public void testSelectWithTwoVariables() throws Exception {
    String constraint =
        """
        context satelliteSystem::Satellite inv:
          satelliteSystem::Satellite.allInstances().select(s1, s2 |
            s1.serialNumber != s2.serialNumber
          ).size() > 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SATELLITE_ECORE},
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS,
            SATELLITE_HUBBLE);

    if (result == null) {
      fail("Compilation failed - check console for errors");
    }

    assertNotNull(result);
    assertEquals(3, result.size(), "Should evaluate for all 3 satellite instances");

    for (OCLElement elem : result.getElements()) {
      assertTrue(((OCLElement.BoolValue) elem).value(), "Should find distinct pairs");
    }
  }

  /**
   * Tests size() operation on cross-metamodel allInstances().
   *
   * <p><b>Scenario:</b> Count total number of satellites and check if it exceeds spacecraft count.
   *
   * <p><b>Expected:</b> Returns true when there are more satellites than the threshold.
   */
  @Test
  public void testCrossMetamodelSizeComparison() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().size() >= 2
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS,
            SATELLITE_HUBBLE);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Should have at least 2 satellites");
  }

  /**
   * Tests isEmpty() on cross-metamodel collection.
   *
   * <p><b>Scenario:</b> Check if there are NO satellites matching a specific condition.
   *
   * <p><b>Expected:</b> isEmpty() returns true when no satellites match the filter.
   */
  @Test
  public void testCrossMetamodelIsEmpty() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            sat.serialNumber == \"NONEXISTENT-999\"
          ).isEmpty()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "No satellites with NONEXISTENT-999 should exist");
  }

  /**
   * Tests notEmpty() ensuring at least one cross-metamodel match exists.
   *
   * <p><b>Expected:</b> notEmpty() returns true when at least one satellite exists.
   */
  @Test
  public void testCrossMetamodelNotEmpty() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().notEmpty()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Should have at least one satellite");
  }

  // ==================== Arithmetic & Aggregation Across Metamodels ====================

  /**
   * Tests sum() operation across metamodels.
   *
   * <p><b>Scenario:</b> Calculate total mass of all satellites and compare with spacecraft.
   *
   * <p><b>Expected:</b> Sum of satellite masses can be compared with spacecraft mass.
   */
  @Test
  public void testCrossMetamodelSumOperation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().collect(sat |
            sat.massKg
          ).sum() > 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Total satellite mass should be positive");
  }

  /**
   * Tests avg() operation for cross-metamodel average calculation.
   *
   * <p><b>Scenario:</b> Calculate average satellite mass.
   *
   * <p><b>Expected:</b> Average should be calculable and positive.
   */
  @Test
  public void testCrossMetamodelAvgOperation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().collect(sat |
            sat.massKg
          ).avg() > 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS,
            SATELLITE_HUBBLE);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Average satellite mass should be positive");
  }

  /**
   * Tests max() operation to find heaviest satellite.
   *
   * <p><b>Scenario:</b> Check if spacecraft mass is less than the maximum satellite mass.
   */
  @Test
  public void testCrossMetamodelMaxOperation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          self.mass <= satelliteSystem::Satellite.allInstances().collect(sat |
            sat.massKg
          ).max()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    // Result depends on model data
  }

  /** Tests min() operation to find lightest satellite. */
  @Test
  public void testCrossMetamodelMinOperation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().collect(sat |
            sat.massKg
          ).min() > 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Minimum satellite mass should be positive");
  }

  // ==================== Complex Nested Operations ====================

  /**
   * Tests nested collect operations across metamodels.
   *
   * <p><b>Scenario:</b> Collect serial numbers from satellites, then check if spacecraft serial is
   * in that collection.
   */
  @Test
  public void testNestedCollectWithIncludes() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().collect(sat |
            sat.serialNumber
          ).includes(self.serialNumber)
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Satellite serial numbers should include spacecraft serial");
  }

  /**
   * Tests excludes() operation in cross-metamodel context.
   *
   * <p><b>Expected:</b> Verify that a specific serial number is NOT in the collection.
   */
  @Test
  public void testCrossMetamodelExcludes() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().collect(sat |
            sat.serialNumber
          ).excludes(\"DEFINITELY-NOT-THERE\")
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Should not include non-existent serial number");
  }

  /**
   * Tests flatten() operation on nested collections.
   *
   * <p><b>Scenario:</b> If metamodel has nested structures, flatten should work across metamodels.
   */
  @Test
  public void testCrossMetamodelFlatten() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          Sequence{
            satelliteSystem::Satellite.allInstances(),
            satelliteSystem::Satellite.allInstances()
          }.flatten().size() >= 2
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== Multiple Iterator Variables ====================

  /**
   * Tests forAll with two variables comparing different entities.
   *
   * <p><b>Scenario:</b> For all pairs of satellites, their serial numbers should be different
   * (uniqueness check).
   */
  @Test
  public void testForAllWithTwoVariables() throws Exception {
    String constraint =
        """
        context satelliteSystem::Satellite inv:
          satelliteSystem::Satellite.allInstances().forAll(s1, s2 |
            s1.serialNumber == s2.serialNumber implies s1 == s2
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SATELLITE_ECORE},
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS,
            SATELLITE_HUBBLE);

    assertNotNull(result);
    // Each satellite evaluates the constraint
    assertEquals(3, result.size());
  }

  /**
   * Tests exists with two variables.
   *
   * <p><b>Scenario:</b> Check if there exist two satellites where one's mass is greater than the
   * other's.
   */
  @Test
  public void testExistsWithTwoVariables() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(s1, s2 |
            s1.massKg > s2.massKg
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS,
            SATELLITE_HUBBLE);

    assertNotNull(result);
  }

  /**
   * Tests collect with two iterator variables.
   *
   * <p><b>Scenario:</b> Create pairs of satellites and collect their combined mass.
   */
  @Test
  public void testCollectWithTwoVariables() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().collect(s1, s2 |
            s1.massKg + s2.massKg
          ).size() > 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== String Operations Across Metamodels ====================

  /**
   * Tests string concatenation in cross-metamodel context.
   *
   * <p><b>Scenario:</b> Check if any satellite serial number contains spacecraft serial as
   * substring.
   */
  @Test
  public void testCrossMetamodelStringConcat() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber.concat(\"-SUFFIX\") != \"\"
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "String concatenation should work");
  }

  /** Tests toUpper() in cross-metamodel comparison. */
  @Test
  public void testCrossMetamodelToUpper() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber.toUpper() == self.serialNumber.toUpper()
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
  }

  /** Tests substring operation in cross-metamodel context. */
  @Test
  public void testCrossMetamodelSubstring() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber.substring(1, 2) == \"SC\"
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    System.err.println(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Substring of SC-001 at positions 1-2 should be 'SC'");
  }

  // ==================== Edge Cases & Boundary Conditions ====================

  /**
   * Tests constraint when NO satellites exist.
   *
   * <p><b>Scenario:</b> Spacecraft context with zero satellite instances.
   *
   * <p><b>Expected:</b> allInstances().isEmpty() should return true.
   */
  @Test
  public void testNoSatellitesAvailable() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().isEmpty()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER); // No satellites provided

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Should be empty when no satellites exist");
  }

  /**
   * Tests forAll on empty collection.
   *
   * <p><b>Expected:</b> forAll on empty collection should return true (vacuous truth).
   */
  @Test
  public void testForAllOnEmptyCollection() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            sat.serialNumber == \"NONEXISTENT\"
          ).forAll(sat | sat.massKg < 0)
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "forAll on empty collection should be vacuously true");
  }

  /**
   * Tests exists on empty collection.
   *
   * <p><b>Expected:</b> exists on empty collection should return false.
   */
  @Test
  public void testExistsOnEmptyCollection() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            sat.serialNumber == \"NONEXISTENT\"
          ).exists(sat | sat.active)
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertFalse(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "exists on empty collection should be false");
  }

  // ==================== Logical Operations Across Metamodels ====================

  /**
   * Tests complex AND logic across metamodels.
   *
   * <p><b>Scenario:</b> Multiple conditions must ALL be true.
   */
  @Test
  public void testCrossMetamodelAndLogic() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          ) and self.operational
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
  }

  /**
   * Tests OR logic across metamodels.
   *
   * <p><b>Scenario:</b> Either condition can be true.
   */
  @Test
  public void testCrossMetamodelOrLogic() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          ) or self.mass > 1000
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
  }

  /** Tests XOR logic across metamodels. */
  @Test
  public void testCrossMetamodelXorLogic() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().size() > 5 xor self.operational
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== Chained Operations ====================

  /**
   * Tests long chain of operations across metamodels.
   *
   * <p><b>Scenario:</b> select().collect().select().size() chaining.
   */
  @Test
  public void testLongOperationChain() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances()
            .select(sat | sat.active)
            .collect(sat | sat.massKg)
            .select(mass | mass > 100)
            .size() >= 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS,
            SATELLITE_HUBBLE);

    assertNotNull(result);
  }

  /** Tests reject followed by collect. */
  @Test
  public void testRejectThenCollect() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances()
            .reject(sat | sat.active)
            .collect(sat | sat.serialNumber)
            .notEmpty()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== Nested Iterations ====================

  /**
   * Tests nested exists within exists.
   *
   * <p><b>Scenario:</b> For spacecraft, check if there exists a satellite such that there exists
   * another spacecraft matching certain criteria.
   */
  @Test
  public void testNestedExistsAcrossMetamodels() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            spaceMission::Spacecraft.allInstances().exists(sc |
              sc.serialNumber == sat.serialNumber and sc.operational
            )
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SPACECRAFT_ATLAS,
            SATELLITE_VOYAGER);

    assertNotNull(result);
  }

  /** Tests nested forAll with cross-metamodel references. */
  @Test
  public void testNestedForAllAcrossMetamodels() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat1 |
            satelliteSystem::Satellite.allInstances().forAll(sat2 |
              sat1.serialNumber == sat2.serialNumber implies sat1.massKg == sat2.massKg
            )
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== Arithmetic Expressions ====================

  /** Tests arithmetic operations in cross-metamodel context. */
  @Test
  public void testCrossMetamodelArithmetic() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            (sat.massKg * 2) > self.mass
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  /** Tests division and comparison. */
  @Test
  public void testCrossMetamodelDivision() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.massKg / 2 > 0
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
  }

  // ==================== First/Last Operations ====================

  /** Tests first() operation on cross-metamodel collection. */
  @Test
  public void testCrossMetamodelFirst() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().first().serialNumber != \"\"
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  /** Tests last() operation on cross-metamodel collection. */
  @Test
  public void testCrossMetamodelLast() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().last().massKg > 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== Reverse Operation ====================

  /** Tests reverse() on cross-metamodel collection. */
  @Test
  public void testCrossMetamodelReverse() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().reverse().size() ==
          satelliteSystem::Satellite.allInstances().size()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Reversed collection should have same size");
  }

  // ==================== Including/Excluding Operations ====================

  /** Tests including() operation with cross-metamodel elements. */
  @Test
  public void testCrossMetamodelIncluding() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          Sequence{1, 2, 3}.including(4).size() == 4
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint, new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE}, SPACECRAFT_VOYAGER);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /** Tests excluding() operation. */
  @Test
  public void testCrossMetamodelExcluding() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          Sequence{1, 2, 3, 4}.excluding(4).size() == 3
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint, new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE}, SPACECRAFT_VOYAGER);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  // ==================== Union Operation ====================

  /** Tests union() across metamodel collections. */
  @Test
  public void testCrossMetamodelUnion() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().union(
            satelliteSystem::Satellite.allInstances()
          ).size() >= satelliteSystem::Satellite.allInstances().size()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== Let Expressions ====================

  /** Tests let expressions in cross-metamodel context. */
  @Test
  public void testCrossMetamodelLetExpression() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          let satellites = satelliteSystem::Satellite.allInstances() in
            satellites.size() >= 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /** Tests multiple let bindings with cross-metamodel references. */
  @Test
  public void testMultipleLetBindings() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          let sats = satelliteSystem::Satellite.allInstances(),
              count = sats.size() in
            count >= 0
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  // ==================== If-Then-Else ====================

  /** Tests conditional expressions in cross-metamodel context. */
  @Test
  public void testCrossMetamodelIfThenElse() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          if satelliteSystem::Satellite.allInstances().size() > 2
          then self.operational
          else self.mass > 0
          endif
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  // ==================== Comparison Operators ====================

  /** Tests less-than comparison across metamodels. */
  @Test
  public void testCrossMetamodelLessThan() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          self.mass < satelliteSystem::Satellite.allInstances().collect(sat |
            sat.massKg
          ).sum()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
  }

  /** Tests not-equal comparison across metamodels. */
  @Test
  public void testCrossMetamodelNotEqual() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.serialNumber != \"\"
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
  }

  // ==================== Prefix Operations ====================

  /** Tests negation in cross-metamodel context. */
  @Test
  public void testCrossMetamodelNegation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          not satelliteSystem::Satellite.allInstances().isEmpty()
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    System.err.println(result);
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Should have satellites (negation of isEmpty)");
  }

  /** Tests unary minus in cross-metamodel arithmetic. */
  @Test
  public void testCrossMetamodelUnaryMinus() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            -sat.massKg < 0
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
  }

  // ==================== Expected Failures / Error Cases ====================

  /**
   * Tests that constraint correctly fails when condition is violated.
   *
   * <p><b>Scenario:</b> Deliberately create a constraint that should evaluate to false.
   */
  @Test
  public void testConstraintViolation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.massKg < 0
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertFalse(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "All satellites having negative mass should be false");
  }

  /** Tests that non-matching conditions correctly return false. */
  @Test
  public void testNoMatchingCondition() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == \"IMPOSSIBLE-SERIAL-999999\"
          )
        """;

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER,
            SATELLITE_ATLAS);

    assertNotNull(result);
    assertFalse(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Should not find impossible serial number");
  }
}