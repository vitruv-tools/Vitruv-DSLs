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
package tools.vitruv.dsls.vitruvocl.metamodel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;

/**
 * Tests for single-metamodel OCL constraints using the spaceMission metamodel.
 *
 * <p>Validates fundamental OCL features including attribute access, reference navigation,
 * collection operations, and iterator expressions using the spaceMission metamodel.
 */
class SingleMetamodelConstraintTest {

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
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  // ── helpers ──────────────────────────────────────────────────────────────────

  private static ConstraintResult eval(String c, Path instance) {
    return VitruvOCL.evaluateConstraint(c, new Path[] {SPACEMISSION_ECORE}, new Path[] {instance});
  }

  // ── Satisfied constraints (isSuccess + isSatisfied both true) ─────────────

  @ParameterizedTest
  @MethodSource("satisfiedConstraints")
  void testConstraintSatisfied(String constraint, Path instance) {
    ConstraintResult r = eval(constraint, instance);
    assertTrue(r.isSuccess(), "Evaluation should succeed");
    assertTrue(r.isSatisfied(), "Constraint should be satisfied");
  }

  static Stream<Arguments> satisfiedConstraints() {
    return Stream.of(
        // Voyager: basic attribute + collection
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.name == \"Voyager\"", SPACECRAFT_VOYAGER),
        Arguments.of("context spaceMission::Spacecraft inv: self.mass >= 500", SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.name != \"Unknown\"", SPACECRAFT_VOYAGER),
        Arguments.of("context spaceMission::Spacecraft inv: self.mass >= 0", SPACECRAFT_VOYAGER),
        Arguments.of("context spaceMission::Spacecraft inv: self.mass > -1", SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.name.concat(\"-1\") != \"\"",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.operational or not self.operational",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.mass == self.mass", SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: Set{100, 200, 300}.includes(100)",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: Sequence{1, 2, 2, 3}.size() == 4",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption >"
                + " 1000)",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: not self.payloads.exists(p | p.powerConsumption"
                + " > 0)",
            SPACECRAFT_VOYAGER),
        // Heavy: arithmetic
        Arguments.of("context spaceMission::Spacecraft inv: self.mass < 2000", SPACECRAFT_HEAVY),
        Arguments.of(
            "context spaceMission::Spacecraft inv: not (self.mass > 10000)", SPACECRAFT_HEAVY),
        Arguments.of(
            "context spaceMission::Spacecraft inv: (self.mass * 2) - 1000 > 0", SPACECRAFT_HEAVY),
        Arguments.of("context spaceMission::Spacecraft inv: self.mass / 2 > 0", SPACECRAFT_HEAVY),
        Arguments.of(
            "context spaceMission::Spacecraft inv: (self.mass + 100) * 2 == self.mass * 2 + 200",
            SPACECRAFT_HEAVY),
        // Operational: boolean
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.operational", SPACECRAFT_OPERATIONAL),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.operational and self.mass < 2000",
            SPACECRAFT_OPERATIONAL),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.mass > 5000 or self.operational",
            SPACECRAFT_OPERATIONAL),
        Arguments.of(
            "context spaceMission::Spacecraft inv: not (not self.operational) == self.operational",
            SPACECRAFT_OPERATIONAL),
        Arguments.of(
            "context spaceMission::Spacecraft inv: ((self.operational and true) or false) =="
                + " self.operational",
            SPACECRAFT_OPERATIONAL),
        // Mission Apollo: reference navigation
        Arguments.of(
            "context spaceMission::Mission inv: self.spacecraft.exists(s | s.name == \"Apollo\")",
            MISSION_APOLLO),
        Arguments.of(
            "context spaceMission::Mission inv: self.spacecraft.isEmpty() == false",
            MISSION_APOLLO),
        Arguments.of(
            "context spaceMission::Mission inv: self.spacecraft.size() >= 1", MISSION_APOLLO),
        // With payloads: collection operations
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.size() == 2",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.exists(p | p.powerConsumption >"
                + " 50)",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.select(p | p.powerConsumption >"
                + " 30).size() >= 1",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.reject(p | p.powerConsumption >"
                + " 200).notEmpty()",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.excludes(999)",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.size() <= 10",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.notEmpty()",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.collect(p | 1).sum() =="
                + " self.payloads.size()",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.select(p | true).size() =="
                + " self.payloads.size()",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.select(p | false).isEmpty()",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.select(p | p.powerConsumption >"
                + " 50).size() + self.payloads.reject(p | p.powerConsumption > 50).size() =="
                + " self.payloads.size()",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.exists(p | p.powerConsumption >"
                + " 30) == self.payloads.select(p | p.powerConsumption > 30).notEmpty()",
            SPACECRAFT_WITH_PAYLOADS),
        // Power sum: aggregation
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.sum() < 300",
            SPACECRAFT_POWER_SUM),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.collect(p |"
                + " p.powerConsumption).sum() < 500",
            SPACECRAFT_POWER_SUM),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.select(p | p.powerConsumption >"
                + " 30).collect(p | p.powerConsumption).sum() < 400",
            SPACECRAFT_POWER_SUM));
  }

  // ── Standalone tests that don't fit the parameterized pattern ─────────────

  @Test
  void testForAll() {
    ConstraintResult r =
        eval(
            "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption <"
                + " 100)",
            SPACECRAFT_FORALL);
    assertTrue(r.isSuccess());
    assertTrue(r.isSatisfied());
  }

  @Test
  void testMultipleInstances() {
    ConstraintResult r =
        VitruvOCL.evaluateConstraint(
            "context spaceMission::Spacecraft inv: self.operational",
            new Path[] {SPACEMISSION_ECORE},
            new Path[] {SPACECRAFT_ACTIVE, SPACECRAFT_INACTIVE});
    assertTrue(r.isSuccess());
  }

  @ParameterizedTest
  @MethodSource("successOnlyConstraints")
  void testSuccessOnly(String constraint, Path instance) {
    assertTrue(eval(constraint, instance).isSuccess(), "Evaluation should succeed");
  }

  static Stream<Arguments> successOnlyConstraints() {
    return Stream.of(
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.max() < 150",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.min() > 0",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.isEmpty() or self.payloads.size()"
                + " == 0",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.powerConsumption.includes(50)",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Mission inv: self.spacecraft.forAll(s | s.payloads.forAll(p |"
                + " p.powerConsumption > 0))",
            MISSION_APOLLO),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.operational implies"
                + " self.payloads.notEmpty()",
            SPACECRAFT_ACTIVE),
        Arguments.of(
            "context spaceMission::Spacecraft inv: (self.mass > 0 and 0 < 2000) implies self.mass <"
                + " 2000",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: Set{self.mass}.min() == self.mass",
            SPACECRAFT_VOYAGER),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.forAll(p | p.powerConsumption > 0)"
                + " == (self.payloads.select(p | p.powerConsumption > 0).size() =="
                + " self.payloads.size())",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.collect(p |"
                + " p.powerConsumption).includes(self.payloads.first().powerConsumption)",
            SPACECRAFT_WITH_PAYLOADS),
        Arguments.of(
            "context spaceMission::Mission inv: self.spacecraft.first().payloads.notEmpty()",
            MISSION_APOLLO),
        Arguments.of(
            "context spaceMission::Spacecraft inv: if self.mass >= 0 then self.mass else -1 *"
                + " self.mass endif > 0",
            SPACECRAFT_HEAVY),
        Arguments.of(
            "context spaceMission::Spacecraft inv: self.payloads.forAll(p | self.operational"
                + " implies p.powerConsumption > 0)",
            SPACECRAFT_ACTIVE));
  }

  @Test
  void testMaxOnSingleElement() {
    ConstraintResult r =
        eval(
            "context spaceMission::Spacecraft inv: Set{self.mass}.max() == self.mass",
            SPACECRAFT_VOYAGER);
    assertTrue(r.isSuccess(), r.toDetailedErrorString());
  }
}
