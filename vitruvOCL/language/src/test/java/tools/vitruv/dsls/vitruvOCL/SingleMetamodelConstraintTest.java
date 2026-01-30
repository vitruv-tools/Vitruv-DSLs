package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.evaluator.OCLElement;
import tools.vitruv.dsls.vitruvOCL.evaluator.Value;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

public class SingleMetamodelConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spacemission.ecore");

  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_HEAVY =
      Path.of("src/test/resources/test-models/spacecraft-heavy.spacemission");
  private static final Path SPACECRAFT_OPERATIONAL =
      Path.of("src/test/resources/test-models/spacecraft-operational.spacemission");
  private static final Path MISSION_APOLLO =
      Path.of("src/test/resources/test-models/mission-apollo.spacemission");
  private static final Path SPACECRAFT_WITH_PAYLOADS =
      Path.of("src/test/resources/test-models/spacecraft-with-payloads.spacemission");
  private static final Path SPACECRAFT_POWER_SUM =
      Path.of("src/test/resources/test-models/spacecraft-power-sum.spacemission");
  private static final Path SPACECRAFT_FORALL =
      Path.of("src/test/resources/test-models/spacecraft-forall.spacemission");
  private static final Path SPACECRAFT_ACTIVE =
      Path.of("src/test/resources/test-models/spacecraft-active.spacemission");
  private static final Path SPACECRAFT_INACTIVE =
      Path.of("src/test/resources/test-models/spacecraft-inactive.spacemission");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  @Test
  public void testAttributeAccess() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.name == \"Voyager\"";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_VOYAGER);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  @Test
  public void testNumericAttributeConstraint() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass < 2000";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_HEAVY);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  @Test
  public void testBooleanAttributeConstraint() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.operational";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_OPERATIONAL);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  @Test
  public void testSingleReferenceNavigation() throws Exception {
    String constraint =
        "context spaceMission::Mission inv: self.spacecraft.exists(s | s.name == \"Apollo\")";
    Value result = VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, MISSION_APOLLO);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  @Test
  public void testCollectionSum() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.sum() < 300";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_POWER_SUM);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  @Test
  public void testForAll() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption < 100)";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_FORALL);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  @Test
  public void testMultipleInstances() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.operational";
    Value result =
        VitruvOCL.evaluate(
            constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_ACTIVE, SPACECRAFT_INACTIVE);

    assertNotNull(result);
    assertEquals(2, result.size(), "Should evaluate constraint on both instances");
    assertTrue(
        ((OCLElement.BoolValue) result.getElements().get(0)).value(), "First should be true");
    assertFalse(
        ((OCLElement.BoolValue) result.getElements().get(1)).value(), "Second should be false");
  }

  @Test
  public void testCollectionSize() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.payloads.size() == 2";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_WITH_PAYLOADS);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }
}