package tools.vitruv.dsls.vitruvOCL;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvOCL.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvOCL.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvOCL.pipeline.VitruvOCL;

/**
 * Tests for single-metamodel OCL constraints using the spaceMission metamodel.
 *
 * <p>This test suite validates that VitruvOCL can correctly evaluate constraints defined within a
 * single metamodel context. Tests cover fundamental OCL features including attribute access,
 * reference navigation, collection operations, and iterator expressions. All constraints use the
 * spaceMission metamodel which models spacecraft, missions, and payloads.
 *
 * <p>The tests verify both successful evaluation (no compilation/runtime errors) and constraint
 * satisfaction (constraint evaluates to true for the given model).
 */
public class SingleMetamodelConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");

  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_HEAVY = Path.of("spacecraft-heavy.spacemission");
  private static final Path SPACECRAFT_OPERATIONAL = Path.of("spacecraft-operational.spacemission");
  private static final Path MISSION_APOLLO = Path.of("mission-apollo.spacemission");
  private static final Path SPACECRAFT_WITH_PAYLOADS =
      Path.of("spacecraft-with-payloads.spacemission");
  private static final Path SPACECRAFT_POWER_SUM = Path.of("spacecraft-power-sum.spacemission");
  private static final Path SPACECRAFT_FORALL = Path.of("spacecraft-forall.spacemission");
  private static final Path SPACECRAFT_ACTIVE = Path.of("spacecraft-active.spacemission");
  private static final Path SPACECRAFT_INACTIVE = Path.of("spacecraft-inactive.spacemission");

  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  /**
   * Tests basic attribute access using equality comparison.
   *
   * <p>Validates that a constraint can access a string attribute and compare it to a literal value.
   * Uses spacecraft with name "Voyager".
   */
  @Test
  public void testAttributeAccess() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.name == \"Voyager\"";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied(), "Constraint should be satisfied");
  }

  /**
   * Tests numeric attribute access with comparison operators.
   *
   * <p>Validates that a constraint can access a numeric attribute and perform less-than comparison.
   * Uses spacecraft with mass under 2000 kg.
   */
  @Test
  public void testNumericAttributeConstraint() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass < 2000";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_HEAVY});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests boolean attribute access.
   *
   * <p>Validates that a constraint can directly access and evaluate a boolean attribute. Uses
   * spacecraft with operational status set to true.
   */
  @Test
  public void testBooleanAttributeConstraint() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.operational";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_OPERATIONAL});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests navigation through single-valued references.
   *
   * <p>Validates that a constraint can navigate from Mission to its referenced Spacecraft and use
   * the exists iterator. Uses mission with spacecraft named "Apollo".
   */
  @Test
  public void testSingleReferenceNavigation() throws Exception {
    String constraint =
        "context spaceMission::Mission inv: self.spacecraft.exists(s | s.name == \"Apollo\")";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {MISSION_APOLLO});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests collection operation chaining with sum().
   *
   * <p>Validates that a constraint can navigate to a collection of payloads, collect their
   * powerConsumption values, and compute the sum. Uses spacecraft with total payload power
   * consumption under 300 watts.
   */
  @Test
  public void testCollectionSum() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.sum() < 300";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_POWER_SUM});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests forAll iterator with lambda expression.
   *
   * <p>Validates that a constraint can use forAll to check that every payload satisfies a
   * condition. Uses spacecraft where all payloads consume less than 100 watts each.
   */
  @Test
  public void testForAll() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption < 100)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_FORALL});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests evaluation with multiple model instances.
   *
   * <p>Validates that the evaluation pipeline can process multiple model instances for the same
   * metaclass. Uses two spacecraft models - one operational and one inactive. Currently tests that
   * evaluation completes without errors; handling of multiple instance results requires API
   * enhancement.
   */
  @Test
  public void testMultipleInstances() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.operational";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint,
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_ACTIVE, SPACECRAFT_INACTIVE});

    assertTrue(result.isSuccess());
    // Note: With multiple instances, we need to update the API to handle this case
    // For now, this tests that evaluation completes without errors
  }

  /**
   * Tests size() operation on collections.
   *
   * <p>Validates that a constraint can query the size of a collection and compare it to a numeric
   * value. Uses spacecraft with exactly 2 payloads.
   */
  @Test
  public void testCollectionSize() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.payloads.size() == 2";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }
}