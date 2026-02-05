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

  /**
   * Tests exists iterator on collections.
   *
   * <p>Validates exists() returns true when at least one element matches the condition.
   */
  @Test
  public void testExistsIterator() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.exists(p | p.powerConsumption > 50)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests select iterator filtering collections.
   *
   * <p>Validates select() filters elements and returns a subset matching the condition.
   */
  @Test
  public void testSelectIterator() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.select(p | p.powerConsumption >"
            + " 30).size() >= 1";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests reject iterator (inverse of select).
   *
   * <p>Validates reject() filters out elements matching the condition.
   */
  @Test
  public void testRejectIterator() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.reject(p | p.powerConsumption >"
            + " 200).notEmpty()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /**
   * Tests collect iterator for transformation.
   *
   * <p>Validates collect() transforms elements by applying an expression to each.
   */
  @Test
  public void testCollectIterator() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.collect(p | p.powerConsumption).sum()"
            + " < 500";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_POWER_SUM});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests isEmpty() on empty collections. */
  @Test
  public void testIsEmptyOperation() throws Exception {
    String constraint = "context spaceMission::Mission inv: self.spacecraft.isEmpty() == false";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {MISSION_APOLLO});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests notEmpty() on non-empty collections. */
  @Test
  public void testNotEmptyOperation() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.payloads.notEmpty()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests includes() membership check. */
  @Test
  public void testIncludesOperation() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.includes(50)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    // Result depends on whether any payload has exactly 50W consumption
  }

  /** Tests excludes() negative membership check. */
  @Test
  public void testExcludesOperation() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.excludes(999)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests chained iterator operations. */
  @Test
  public void testChainedIterators() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.select(p | p.powerConsumption >"
            + " 30).collect(p | p.powerConsumption).sum() < 400";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_POWER_SUM});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests logical AND in constraints. */
  @Test
  public void testLogicalAnd() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.operational and self.mass < 2000";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_OPERATIONAL});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests logical OR in constraints. */
  @Test
  public void testLogicalOr() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.mass > 5000 or self.operational";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_OPERATIONAL});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests logical NOT in constraints. */
  @Test
  public void testLogicalNot() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: not (self.mass > 10000)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_HEAVY});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests implies operator (logical implication). */
  @Test
  public void testImpliesOperator() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.operational implies self.payloads.notEmpty()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_ACTIVE});

    assertTrue(result.isSuccess());
    // Satisfaction depends on model data
  }

  /** Tests nested forAll iterators. */
  @Test
  public void testNestedForAll() throws Exception {
    String constraint =
        "context spaceMission::Mission inv: self.spacecraft.forAll(s | s.payloads.forAll(p |"
            + " p.powerConsumption > 0))";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {MISSION_APOLLO});

    assertTrue(result.isSuccess());
  }

  /** Tests arithmetic in constraints. */
  @Test
  public void testArithmeticOperations() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: (self.mass * 2) - 1000 > 0";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_HEAVY});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests greater-or-equal comparison. */
  @Test
  public void testGreaterOrEqualComparison() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass >= 500";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests less-or-equal comparison. */
  @Test
  public void testLessOrEqualComparison() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.payloads.size() <= 10";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests not-equal comparison. */
  @Test
  public void testNotEqualComparison() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.name != \"Unknown\"";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests max() operation on numeric collections. */
  @Test
  public void testMaxOperation() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.max() < 150";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
  }

  /** Tests min() operation on numeric collections. */
  @Test
  public void testMinOperation() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.min() > 0";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
  }

  /** Tests constraint on empty payload collection. */
  @Test
  public void testEmptyPayloadsCollection() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.isEmpty() or self.payloads.size() =="
            + " 0";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /** Tests forAll on empty collection (vacuous truth). */
  @Test
  public void testForAllOnEmptyCollection() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption > 1000)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "forAll on empty collection should be true");
  }

  /** Tests exists on empty collection. */
  @Test
  public void testExistsOnEmptyCollection() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: not self.payloads.exists(p | p.powerConsumption >"
            + " 0)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied(), "exists on empty collection should be false");
  }

  /** Tests zero mass edge case. */
  @Test
  public void testZeroMass() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass >= 0";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests division by positive number. */
  @Test
  public void testDivisionOperation() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass / 2 > 0";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_HEAVY});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests negative mass constraint (boundary). */
  @Test
  public void testNegativeMassBoundary() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass > -1";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests string concatenation in constraint. */
  @Test
  public void testStringConcatenation() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.name.concat(\"-1\") != \"\"";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests size of single-element collection. */
  @Test
  public void testSingleElementCollectionSize() throws Exception {
    String constraint = "context spaceMission::Mission inv: self.spacecraft.size() >= 1";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {MISSION_APOLLO});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests collect with constant expression. */
  @Test
  public void testCollectConstant() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.collect(p | 1).sum() =="
            + " self.payloads.size()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests select with always-true condition. */
  @Test
  public void testSelectAlwaysTrue() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.select(p | true).size() =="
            + " self.payloads.size()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests select with always-false condition. */
  @Test
  public void testSelectAlwaysFalse() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.select(p | false).isEmpty()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests equality reflexivity (self == self). */
  @Test
  public void testEqualityReflexivity() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: self.mass == self.mass";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests double negation. */
  @Test
  public void testDoubleNegation() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: not (not self.operational) == self.operational";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_OPERATIONAL});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests tautology (always true). */
  @Test
  public void testTautology() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.operational or not self.operational";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests complex nested boolean expression. */
  @Test
  public void testComplexBooleanNesting() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: ((self.operational and true) or false) =="
            + " self.operational";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_OPERATIONAL});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests parenthesized arithmetic precedence. */
  @Test
  public void testArithmeticPrecedenceWithParentheses() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: (self.mass + 100) * 2 == self.mass * 2 + 200";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_HEAVY});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests chained comparisons (transitive property). */
  @Test
  public void testChainedComparisons() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: (self.mass > 0 and 0 < 2000) implies self.mass <"
            + " 2000";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /** Tests max on single-element collection. */
  @Test
  public void testMaxOnSingleElement() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: Set{self.mass}.max() == self.mass";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /** Tests min on single-element collection. */
  @Test
  public void testMinOnSingleElement() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: Set{self.mass}.min() == self.mass";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
  }

  /** Tests size invariant under select-reject complement. */
  @Test
  public void testSelectRejectComplement() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.select(p | p.powerConsumption >"
            + " 50).size() + self.payloads.reject(p | p.powerConsumption > 50).size() =="
            + " self.payloads.size()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests exists equivalent to select.notEmpty(). */
  @Test
  public void testExistsSelectEquivalence() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.exists(p | p.powerConsumption > 30) =="
            + " self.payloads.select(p | p.powerConsumption > 30).notEmpty()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests forAll equivalent to select.size() == collection.size(). */
  @Test
  public void testForAllSelectEquivalence() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption > 0) =="
            + " (self.payloads.select(p | p.powerConsumption > 0).size() == self.payloads.size())";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
  }

  /** Tests includes on collected values. */
  @Test
  public void testIncludesOnCollectedValues() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.collect(p |"
            + " p.powerConsumption).includes(self.payloads.first().powerConsumption)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_WITH_PAYLOADS});

    assertTrue(result.isSuccess());
  }

  /** Tests attribute access through double navigation. */
  @Test
  public void testDoubleNavigation() throws Exception {
    String constraint =
        "context spaceMission::Mission inv: self.spacecraft.first().payloads.notEmpty()";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {MISSION_APOLLO});

    assertTrue(result.isSuccess());
  }

  /** Tests constraint with literal Set construction. */
  @Test
  public void testLiteralSetConstruction() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: Set{100, 200, 300}.includes(100)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests constraint with literal Sequence construction. */
  @Test
  public void testLiteralSequenceConstruction() throws Exception {
    String constraint = "context spaceMission::Spacecraft inv: Sequence{1, 2, 2, 3}.size() == 4";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_VOYAGER});

    assertTrue(result.isSuccess());
    assertTrue(result.isSatisfied());
  }

  /** Tests absolute value using conditional. */
  @Test
  public void testAbsoluteValuePattern() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: if self.mass >= 0 then self.mass else -1 * self.mass"
            + " endif > 0";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_HEAVY});

    assertTrue(result.isSuccess());
  }

  /** Tests nested iterator with self reference. */
  @Test
  public void testNestedIteratorSelfReference() throws Exception {
    String constraint =
        "context spaceMission::Spacecraft inv: self.payloads.forAll(p | self.operational implies"
            + " p.powerConsumption > 0)";
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, new Path[] {SPACEMISSION_ECORE}, new Path[] {SPACECRAFT_ACTIVE});

    assertTrue(result.isSuccess());
  }
}