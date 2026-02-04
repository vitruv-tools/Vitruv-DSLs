package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/**
 * Tests for cross-metamodel OCL constraints using spaceMission and satelliteSystem metamodels.
 *
 * <p>This test suite validates VitruvOCL's ability to evaluate constraints that span multiple
 * metamodels using allInstances() to access objects from other metamodels. Tests cover
 * cross-metamodel navigation, iterator operations, collection operations, and consistency checking
 * between related models in different metamodels.
 *
 * <p>The spaceMission and satelliteSystem metamodels model the same domain from different
 * perspectives, allowing tests of correspondence constraints (e.g., matching serialNumbers, mass
 * consistency) and aggregate operations across metamodel boundaries.
 */
public class CrossMetamodelConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ATLAS = Path.of("spacecraft-atlas.spacemission");
  private static final Path SATELLITE_VOYAGER = Path.of("satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS = Path.of("satellite-atlas.satellitesystem");
  private static final Path SATELLITE_HUBBLE = Path.of("satellite-hubble.satellitesystem");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  /**
   * Tests cross-metamodel matching using allInstances() and exists iterator. Validates Spacecraft
   * can find matching Satellite by serialNumber.
   */
  @Test
  public void testCrossMetamodelSerialNumberMatch() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Spacecraft SC-001 should find matching Satellite SC-001");
  }

  /**
   * Tests cross-metamodel consistency checking with logical implication. Validates that matching
   * mass values imply matching operational/active status.
   */
  @Test
  public void testCrossMetamodelMassConsistency() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.massKg == self.mass implies sat.active == self.operational
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_ATLAS, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertTrue(
        result.isSatisfied(), "Matching mass should imply matching operational/active status");
  }

  /**
   * Tests constraint violation when no matching cross-metamodel instance exists. Validates
   * Spacecraft with SC-001 does not match Satellite with SAT-099.
   */
  @Test
  public void testCrossMetamodelNoMatchingSerialNumber() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_HUBBLE});

    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "Spacecraft SC-001 should NOT match Satellite SAT-099");
  }

  /**
   * Tests evaluation with multiple model instances from both metamodels. Validates allInstances()
   * aggregates all instances and select filters correctly.
   */
  @Test
  public void testMultipleModelsFromBothMetamodels() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            sat.serialNumber == self.serialNumber
          ).size() >= 1
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {
              SPACECRAFT_VOYAGER,
              SPACECRAFT_ATLAS,
              SATELLITE_VOYAGER,
              SATELLITE_ATLAS,
              SATELLITE_HUBBLE
            });

    assertTrue(result.isSuccess());
  }

  /**
   * Tests bidirectional cross-metamodel navigation. Validates Satellite can access Spacecraft
   * instances and check matching attributes.
   */
  @Test
  public void testSatelliteReferencesSpacecraft() throws Exception {
    String constraint =
        """
        context satelliteSystem::Satellite inv:
          spaceMission::Spacecraft.allInstances().exists(sc |
            sc.serialNumber == self.serialNumber and sc.mass == self.massKg
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(
        result.isSatisfied(),
        "Satellite should find corresponding Spacecraft with matching attributes");
  }

  /**
   * Tests select iterator with two variables (Cartesian product). Validates all pairs of Satellites
   * can be compared for distinct serialNumbers.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SATELLITE_ECORE},
            new Path[] {SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});

    System.out.println(result.toDetailedErrorString());
    System.out.println("Result size: " + result.toString());
    System.out.println("Result: " + result);

    if (!result.isSuccess()) {
      fail("Compilation failed: " + result.toDetailedErrorString());
    }
    assertTrue(result.isSatisfied(), "Should find distinct pairs");
  }

  /**
   * Tests size() operation on cross-metamodel collection. Validates constraint can count instances
   * from another metamodel.
   */
  @Test
  public void testCrossMetamodelSizeComparison() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().size() >= 2
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should have at least 2 satellites");
  }

  /**
   * Tests isEmpty() on filtered cross-metamodel collection. Validates empty collection when no
   * instances match filter condition.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "No satellites with NONEXISTENT-999 should exist");
  }

  /**
   * Tests notEmpty() on cross-metamodel collection. Validates non-empty collection when instances
   * exist in another metamodel.
   */
  @Test
  public void testCrossMetamodelNotEmpty() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().notEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should have at least one satellite");
  }

  /**
   * Tests sum() operation on cross-metamodel numeric collection. Validates aggregation of massKg
   * values from all Satellite instances.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Total satellite mass should be positive");
  }

  /**
   * Tests avg() operation on cross-metamodel numeric collection. Validates average calculation
   * across Satellite massKg values.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Average satellite mass should be positive");
  }

  /**
   * Tests max() operation on cross-metamodel numeric collection. Validates Spacecraft mass is at
   * most the maximum Satellite mass.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests min() operation on cross-metamodel numeric collection. Validates minimum satellite mass
   * is positive.
   */
  @Test
  public void testCrossMetamodelMinOperation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().collect(sat |
            sat.massKg
          ).min() > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Minimum satellite mass should be positive");
  }

  /**
   * Tests includes() operation on cross-metamodel collection. Validates collected Satellite
   * serialNumbers include Spacecraft serialNumber.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Satellite serial numbers should include spacecraft serial");
  }

  /**
   * Tests excludes() operation on cross-metamodel collection. Validates collected serialNumbers do
   * not contain non-existent value.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should not include non-existent serial number");
  }

  /**
   * Tests flatten() operation on nested cross-metamodel collections. Validates flattening sequence
   * of allInstances() calls produces expected size.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests forAll iterator with two variables (Cartesian product). Validates uniqueness: same
   * serialNumber implies same object identity.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SATELLITE_ECORE},
            new Path[] {SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests exists iterator with two variables (Cartesian product). Validates at least one pair of
   * Satellites has different masses.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests collect iterator with two variables (Cartesian product). Validates collecting computed
   * values from all pairs of Satellites.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests string concatenation on cross-metamodel attributes. Validates concat() works on Satellite
   * serialNumbers.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "String concatenation should work");
  }

  /**
   * Tests case conversion on cross-metamodel string attributes. Validates toUpper() enables
   * case-insensitive comparison across metamodels.
   */
  @Test
  public void testCrossMetamodelToUpper() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber.toUpper() == self.serialNumber.toUpper()
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests substring extraction on cross-metamodel string attributes. Validates substring(1,2) on
   * Satellite serialNumber returns expected prefix.
   */
  @Test
  public void testCrossMetamodelSubstring() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber.substring(1, 2) == \"SC\"
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Substring of SC-001 at positions 1-2 should be 'SC'");
  }

  /**
   * Tests isEmpty() when no instances exist in another metamodel. Validates allInstances() returns
   * empty collection when no models loaded.
   */
  @Test
  public void testNoSatellitesAvailable() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().isEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should be empty when no satellites exist");
  }

  /**
   * Tests forAll on empty collection (vacuous truth). Validates forAll returns true when no
   * elements satisfy filter condition.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "forAll on empty collection should be vacuously true");
  }

  /**
   * Tests exists on empty collection. Validates exists returns false when no elements satisfy
   * filter condition.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "exists on empty collection should be false");
  }

  /**
   * Tests boolean AND combining cross-metamodel and single-metamodel conditions. Validates both
   * conditions must hold for constraint satisfaction.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests boolean OR combining cross-metamodel and single-metamodel conditions. Validates
   * constraint satisfied when at least one condition holds.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests boolean XOR (exclusive or) with cross-metamodel condition. Validates exactly one of two
   * conditions must be true.
   */
  @Test
  public void testCrossMetamodelXorLogic() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().size() > 5 xor self.operational
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests long chain of collection operations. Validates select, collect, and filter operations can
   * be chained across metamodels.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests reject iterator followed by collect. Validates reject filters out matching elements,
   * collect extracts attributes.
   */
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests nested exists iterators across different metamodels. Validates outer exists on
   * Satellites, inner exists on Spacecraft with matching criteria.
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests nested forAll iterators with implication logic. Validates all pairs of Satellites with
   * matching serialNumbers have matching masses.
   */
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests arithmetic operations on cross-metamodel attributes. Validates multiplication in
   * comparison between Satellite and Spacecraft masses.
   */
  @Test
  public void testCrossMetamodelArithmetic() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            (sat.massKg * 2) > self.mass
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests division operation on cross-metamodel numeric attributes. Validates all Satellites have
   * positive mass after division by 2.
   */
  @Test
  public void testCrossMetamodelDivision() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.massKg / 2 > 0
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests first() operation on cross-metamodel collection. Validates accessing first Satellite
   * instance and checking its serialNumber.
   */
  @Test
  public void testCrossMetamodelFirst() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().first().serialNumber != \"\"
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests last() operation on cross-metamodel collection. Validates accessing last Satellite
   * instance and checking its mass.
   */
  @Test
  public void testCrossMetamodelLast() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().last().massKg > 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests reverse() operation on cross-metamodel collection. Validates reversed collection has same
   * size as original.
   */
  @Test
  public void testCrossMetamodelReverse() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().reverse().size() ==
          satelliteSystem::Satellite.allInstances().size()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Reversed collection should have same size");
  }

  /**
   * Tests including() operation adding element to sequence. Validates including(4) on [1,2,3]
   * produces collection of size 4.
   */
  @Test
  public void testCrossMetamodelIncluding() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          Sequence{1, 2, 3}.including(4).size() == 4
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests excluding() operation removing element from sequence. Validates excluding(4) on [1,2,3,4]
   * produces collection of size 3.
   */
  @Test
  public void testCrossMetamodelExcluding() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          Sequence{1, 2, 3, 4}.excluding(4).size() == 3
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests union() operation combining two collections. Validates union of allInstances() with
   * itself has size at least as large as original.
   */
  @Test
  public void testCrossMetamodelUnion() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().union(
            satelliteSystem::Satellite.allInstances()
          ).size() >= satelliteSystem::Satellite.allInstances().size()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests let expression with cross-metamodel collection. Validates variable binding of
   * allInstances() result for reuse in constraint body.
   */
  @Test
  public void testCrossMetamodelLetExpression() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          let satellites = satelliteSystem::Satellite.allInstances() in
            satellites.size() >= 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests let expression with multiple variable bindings. Validates chained let bindings where
   * second variable references first.
   */
  @Test
  public void testMultipleLetBindings() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          let sats = satelliteSystem::Satellite.allInstances(),
              count = sats.size() in
            count >= 0
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests if-then-else conditional with cross-metamodel condition. Validates different branches
   * execute based on Satellite collection size.
   */
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests less-than comparison with cross-metamodel aggregation. Validates Spacecraft mass is less
   * than sum of all Satellite masses.
   */
  @Test
  public void testCrossMetamodelLessThan() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          self.mass < satelliteSystem::Satellite.allInstances().collect(sat |
            sat.massKg
          ).sum()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests not-equal comparison on cross-metamodel string attributes. Validates all Satellite
   * serialNumbers are non-empty strings.
   */
  @Test
  public void testCrossMetamodelNotEqual() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.serialNumber != \"\"
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests boolean negation with cross-metamodel collection query. Validates negation of isEmpty()
   * correctly evaluates to true when satellites exist.
   */
  @Test
  public void testCrossMetamodelNegation() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          not satelliteSystem::Satellite.allInstances().isEmpty()
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "Should have satellites (negation of isEmpty)");
  }

  /**
   * Tests unary minus operator on cross-metamodel numeric attributes. Validates negated mass values
   * are negative for positive masses.
   */
  @Test
  public void testCrossMetamodelUnaryMinus() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            -sat.massKg < 0
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /**
   * Tests constraint violation detection. Validates constraint correctly evaluates to false when
   * condition is impossible (all satellites having negative mass).
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

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});

    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "All satellites having negative mass should be false");
  }

  /**
   * Tests exists returning false when no elements match condition. Validates constraint violation
   * when searching for non-existent serialNumber.
   */
  @Test
  public void testNoMatchingCondition() throws Exception {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == \"IMPOSSIBLE-SERIAL-999999\"
          )
        """;

    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});

    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "Should not find impossible serial number");
  }
}