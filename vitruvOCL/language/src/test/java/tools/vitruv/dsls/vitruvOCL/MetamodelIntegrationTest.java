package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetamodelIntegrationTest {

  private TestConstraintSpecification specification;

  @BeforeEach
  public void setup() throws Exception {
    TestConstraintSpecification.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");

    specification = new TestConstraintSpecification();
    specification.loadMetamodel(Path.of("src/test/resources/test-metamodels/spaceMission.ecore"));
    specification.loadMetamodel(
        Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore"));
  }

  @Test
  public void testResolveSpacecraftClass() {
    EClass spacecraft = specification.resolveEClass("spaceMission", "Spacecraft");
    assertNotNull(spacecraft, "Should resolve spaceMission::Spacecraft");
    assertEquals("Spacecraft", spacecraft.getName());
  }

  @Test
  public void testResolveSatelliteClass() {
    EClass satellite = specification.resolveEClass("satelliteSystem", "Satellite");
    assertNotNull(satellite, "Should resolve satelliteSystem::Satellite");
    assertEquals("Satellite", satellite.getName());
  }

  @Test
  public void testResolvePayloadClass() {
    EClass payload = specification.resolveEClass("spaceMission", "Payload");
    assertNotNull(payload, "Should resolve spacemission::Payload");
    assertEquals("Payload", payload.getName());

    // Check attributes
    assertNotNull(payload.getEStructuralFeature("powerConsumption"));
  }

  @Test
  public void testResolveUnknownClass() {
    EClass unknown = specification.resolveEClass("spacemission", "UnknownClass");
    assertNull(unknown, "Should return null for unknown class");
  }

  @Test
  public void testResolveUnknownMetamodel() {
    EClass unknown = specification.resolveEClass("unknownmodel", "Spacecraft");
    assertNull(unknown, "Should return null for unknown metamodel");
  }

  @Test
  public void testAvailableMetamodels() {
    var metamodels = specification.getAvailableMetamodels();
    assertTrue(metamodels.contains("spaceMission"), "Should contain spacemission");
    assertTrue(metamodels.contains("satelliteSystem"), "Should contain satellite");
  }
}