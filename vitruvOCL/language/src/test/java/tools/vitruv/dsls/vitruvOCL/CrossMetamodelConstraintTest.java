package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

public class CrossMetamodelConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spacemission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  // SpaceMission model instances
  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ATLAS = Path.of("spacecraft-atlas.spacemission");

  // Satellite model instances
  private static final Path SATELLITE_VOYAGER = Path.of("satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS = Path.of("satellite-atlas.satellitesystem");
  private static final Path SATELLITE_HUBBLE = Path.of("satellite-hubble.satellitesystem");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

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

    Value result =
        VitruvOCL.evaluate(
            constraint,
            new Path[] {SPACEMISSION_ECORE, SATELLITE_ECORE},
            SPACECRAFT_VOYAGER,
            SATELLITE_VOYAGER);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(),
        "Spacecraft SC-001 should find matching Satellite SC-001");
  }

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