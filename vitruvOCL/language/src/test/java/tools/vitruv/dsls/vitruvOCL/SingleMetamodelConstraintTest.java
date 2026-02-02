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
 * Integration test suite for single-metamodel OCL constraints in VitruvOCL.
 *
 * <p>This test class validates end-to-end constraint evaluation on model instances from a single
 * metamodel (spaceMission). Tests use the {@link VitruvOCL} public API to evaluate constraints
 * against real EMF model instances, exercising the complete compilation and evaluation pipeline.
 *
 * <h2>Test Metamodel: spaceMission</h2>
 *
 * The spaceMission metamodel defines concepts for space mission modeling:
 *
 * <ul>
 *   <li><b>Spacecraft:</b> Main entity with attributes:
 *       <ul>
 *         <li>name: String - spacecraft identifier
 *         <li>mass: Integer - spacecraft mass in kg
 *         <li>operational: Boolean - operational status
 *         <li>payloads: Payload[*] - collection of payload instruments
 *       </ul>
 *   <li><b>Payload:</b> Instrument carried by spacecraft:
 *       <ul>
 *         <li>name: String - payload identifier
 *         <li>powerConsumption: Integer - power consumption in watts
 *         <li>dataRate: Integer - data transmission rate
 *       </ul>
 *   <li><b>Mission:</b> Mission definition:
 *       <ul>
 *         <li>name: String - mission identifier
 *         <li>spacecraft: Spacecraft[*] - collection of mission spacecraft
 *       </ul>
 * </ul>
 *
 * <h2>Test Model Instances</h2>
 *
 * Located in {@code src/test/resources/test-models/}:
 *
 * <ul>
 *   <li><b>spacecraft-voyager.spacemission:</b> Spacecraft named "Voyager"
 *   <li><b>spacecraft-heavy.spacemission:</b> Heavy spacecraft (mass < 2000)
 *   <li><b>spacecraft-operational.spacemission:</b> Operational spacecraft (operational=true)
 *   <li><b>mission-apollo.spacemission:</b> Apollo mission with associated spacecraft
 *   <li><b>spacecraft-with-payloads.spacemission:</b> Spacecraft with 2 payloads
 *   <li><b>spacecraft-power-sum.spacemission:</b> Spacecraft with payloads totaling < 300W
 *   <li><b>spacecraft-forall.spacemission:</b> Spacecraft where all payloads < 100W
 *   <li><b>spacecraft-active.spacemission:</b> Active operational spacecraft
 *   <li><b>spacecraft-inactive.spacemission:</b> Inactive spacecraft (operational=false)
 * </ul>
 *
 * @see VitruvOCL Main public API for constraint evaluation
 * @see Value Result representation
 * @see MetamodelWrapper Metamodel loading and management
 * @see CrossMetamodelConstraintTest Tests for multi-metamodel constraints
 */
public class SingleMetamodelConstraintTest {

  /**
   * Path to the spaceMission metamodel definition.
   *
   * <p>Ecore file defining the spaceMission metamodel with Spacecraft, Payload, and Mission
   * metaclasses.
   */
  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spacemission.ecore");

  /** Model instance: Spacecraft named "Voyager". */
  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");

  /** Model instance: Heavy spacecraft with mass < 2000 kg. */
  private static final Path SPACECRAFT_HEAVY =
      Path.of("src/test/resources/test-models/spacecraft-heavy.spacemission");

  /** Model instance: Operational spacecraft with operational=true. */
  private static final Path SPACECRAFT_OPERATIONAL =
      Path.of("src/test/resources/test-models/spacecraft-operational.spacemission");

  /** Model instance: Apollo mission with associated spacecraft. */
  private static final Path MISSION_APOLLO =
      Path.of("src/test/resources/test-models/mission-apollo.spacemission");

  /** Model instance: Spacecraft with 2 payload instruments. */
  private static final Path SPACECRAFT_WITH_PAYLOADS =
      Path.of("src/test/resources/test-models/spacecraft-with-payloads.spacemission");

  /** Model instance: Spacecraft with payloads totaling < 300W power consumption. */
  private static final Path SPACECRAFT_POWER_SUM =
      Path.of("src/test/resources/test-models/spacecraft-power-sum.spacemission");

  /** Model instance: Spacecraft where all payloads consume < 100W each. */
  private static final Path SPACECRAFT_FORALL =
      Path.of("src/test/resources/test-models/spacecraft-forall.spacemission");

  /** Model instance: Active operational spacecraft. */
  private static final Path SPACECRAFT_ACTIVE =
      Path.of("src/test/resources/test-models/spacecraft-active.spacemission");

  /** Model instance: Inactive spacecraft with operational=false. */
  private static final Path SPACECRAFT_INACTIVE =
      Path.of("src/test/resources/test-models/spacecraft-inactive.spacemission");

  /**
   * Configures the test environment before all tests.
   *
   * <p>Sets the {@code TEST_MODELS_PATH} in {@link MetamodelWrapper} to enable model instance
   * loading from the test resources directory.
   */
  @BeforeAll
  public static void setupPaths() {
    MetamodelWrapper.TEST_MODELS_PATH = Path.of("src/test/resources/test-models");
  }

  /**
   * Tests basic attribute access and string equality.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Spacecraft inv: self.name == "Voyager"}
   *
   * <p><b>Model:</b> spacecraft-voyager.spacemission (name = "Voyager")
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Attribute access using {@code self}
   *   <li>String comparison with literal
   *   <li>Single instance evaluation
   * </ul>
   *
   * @throws Exception if metamodel/model loading or evaluation fails
   */
  @Test
  public void testAttributeAccess() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.name == \"Voyager\"";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_VOYAGER);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /**
   * Tests numeric attribute constraints with comparison.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Spacecraft inv: self.mass < 2000}
   *
   * <p><b>Model:</b> spacecraft-heavy.spacemission (mass < 2000)
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Numeric attribute access
   *   <li>Less-than comparison with integer literal
   * </ul>
   *
   * @throws Exception if evaluation fails
   */
  @Test
  public void testNumericAttributeConstraint() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass < 2000";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_HEAVY);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /**
   * Tests boolean attribute access without explicit comparison.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Spacecraft inv: self.operational}
   *
   * <p><b>Model:</b> spacecraft-operational.spacemission (operational = true)
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Direct boolean attribute usage
   *   <li>Boolean as constraint result (no explicit comparison needed)
   * </ul>
   *
   * @throws Exception if evaluation fails
   */
  @Test
  public void testBooleanAttributeConstraint() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.operational";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_OPERATIONAL);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /**
   * Tests navigation through single-valued reference with exists operation.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Mission inv: self.spacecraft.exists(s |
   * s.name == "Apollo")}
   *
   * <p><b>Model:</b> mission-apollo.spacemission (mission with Apollo spacecraft)
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Reference navigation ({@code self.spacecraft})
   *   <li>Iterator operation ({@code exists}) on referenced collection
   *   <li>Lambda expression with iterator variable
   * </ul>
   *
   * @throws Exception if evaluation fails
   */
  @Test
  public void testSingleReferenceNavigation() throws Exception {
    String constraint =
        "context spaceMission::Mission inv: self.spacecraft.exists(s | s.name == \"Apollo\")";
    Value result = VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, MISSION_APOLLO);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /**
   * Tests aggregate sum operation on nested collection navigation.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Spacecraft inv:
   * self.payloads.powerConsumption.sum() < 300}
   *
   * <p><b>Model:</b> spacecraft-power-sum.spacemission (payloads sum < 300W)
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Navigation chain:</b>
   *
   * <ol>
   *   <li>{@code self.payloads} → collection of Payload objects
   *   <li>{@code .powerConsumption} → collection of Integer values (implicit collect)
   *   <li>{@code .sum()} → single Integer (sum of all values)
   *   <li>{@code < 300} → Boolean comparison
   * </ol>
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Multi-valued reference navigation
   *   <li>Implicit collect on attribute access
   *   <li>Aggregate sum operation
   *   <li>Comparison on aggregate result
   * </ul>
   *
   * @throws Exception if evaluation fails
   */
  @Test
  public void testCollectionSum() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.sum() < 300";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_POWER_SUM);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /**
   * Tests universal quantification with forAll iterator.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Spacecraft inv: self.payloads.forAll(p |
   * p.powerConsumption < 100)}
   *
   * <p><b>Model:</b> spacecraft-forall.spacemission (all payloads < 100W)
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Semantics:</b> Returns true if ALL payloads satisfy the predicate {@code
   * p.powerConsumption < 100}.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Universal quantification ({@code forAll})
   *   <li>Iterator variable binding ({@code p})
   *   <li>Predicate evaluation over collection elements
   * </ul>
   *
   * @throws Exception if evaluation fails
   */
  @Test
  public void testForAll() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption < 100)";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_FORALL);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }

  /**
   * Tests constraint evaluation on multiple model instances.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Spacecraft inv: self.operational}
   *
   * <p><b>Models:</b>
   *
   * <ul>
   *   <li>spacecraft-active.spacemission (operational = true)
   *   <li>spacecraft-inactive.spacemission (operational = false)
   * </ul>
   *
   * <p><b>Expected:</b> {@code [true, false]} (two results, one per instance)
   *
   * <p><b>Multi-instance semantics:</b> When multiple model instances are provided, the constraint
   * is evaluated once for each instance, producing a collection of results.
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Multiple instance evaluation
   *   <li>Independent constraint evaluation per instance
   *   <li>Result collection with correct size and values
   * </ul>
   *
   * @throws Exception if evaluation fails
   */
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

  /**
   * Tests collection size query operation.
   *
   * <p><b>Constraint:</b> {@code context spaceMission::Spacecraft inv: self.payloads.size() == 2}
   *
   * <p><b>Model:</b> spacecraft-with-payloads.spacemission (2 payloads)
   *
   * <p><b>Expected:</b> {@code [true]}
   *
   * <p><b>Validates:</b>
   *
   * <ul>
   *   <li>Collection size operation
   *   <li>Equality comparison with integer
   *   <li>Multi-valued reference handling
   * </ul>
   *
   * @throws Exception if evaluation fails
   */
  @Test
  public void testCollectionSize() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.payloads.size() == 2";
    Value result =
        VitruvOCL.evaluate(constraint, new Path[] {SPACEMISSION_ECORE}, SPACECRAFT_WITH_PAYLOADS);

    assertNotNull(result);
    assertTrue(((OCLElement.BoolValue) result.getElements().get(0)).value());
  }
}