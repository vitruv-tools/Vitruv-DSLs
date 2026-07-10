/* ******************************************************************************
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

import static org.junit.jupiter.api.Assertions.*;

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
class CrossMetamodelConstraintTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");
  private static final Path[] SM_SAT_ECORES = {SPACEMISSION_ECORE, SATELLITE_ECORE};
  private static final Path[] SAT_ONLY_ECORES = {SATELLITE_ECORE};

  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ATLAS = Path.of("spacecraft-atlas.spacemission");
  private static final Path SATELLITE_VOYAGER = Path.of("satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS = Path.of("satellite-atlas.satellitesystem");
  private static final Path SATELLITE_HUBBLE = Path.of("satellite-hubble.satellitesystem");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  // ── Parameterized: satisfied constraints (isSuccess + isSatisfied) ────────

  @ParameterizedTest
  @MethodSource("satisfiedConstraints")
  void testConstraintSatisfied(String c, Path[] instances) {
    ConstraintResult result = VitruvOCL.evaluateConstraint(c, SM_SAT_ECORES, instances);
    assertTrue(result.isSuccess(), "Evaluation should succeed");
    assertTrue(result.isSatisfied());
  }

  static Stream<Arguments> satisfiedConstraints() {
    Path[] voy = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER};
    Path[] voy2 = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS};
    Path[] voy3 = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE};
    Path[] atl = {SPACECRAFT_ATLAS, SATELLITE_ATLAS};
    return Stream.of(
        // Serial number match
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              )
            """,
            voy),
        // Mass consistency implication
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                sat.massKg == self.mass implies sat.active == self.operational
              )
            """,
            atl),
        // Bidirectional: satellite references spacecraft
        Arguments.of(
            """
            context satelliteSystem::Satellite inv:
              spaceMission::Spacecraft.allInstances().exists(sc |
                sc.serialNumber == self.serialNumber and sc.mass == self.massKg
              )
            """,
            voy),
        // Size >= 2
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().size() >= 2
            """,
            voy3),
        // isEmpty when no match
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(sat |
                sat.serialNumber == \"NONEXISTENT-999\"
              ).isEmpty()
            """,
            voy),
        // notEmpty
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().notEmpty()
            """,
            voy2),
        // sum > 0
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().collect(sat |
                sat.massKg
              ).sum() > 0
            """,
            voy2),
        // avg > 0
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().collect(sat |
                sat.massKg
              ).avg() > 0
            """,
            voy3),
        // includes serialNumber
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().collect(sat |
                sat.serialNumber
              ).includes(self.serialNumber)
            """,
            voy2),
        // excludes non-existent
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().collect(sat |
                sat.serialNumber
              ).excludes(\"DEFINITELY-NOT-THERE\")
            """,
            voy),
        // reverse same size
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().reverse().size() ==
              satelliteSystem::Satellite.allInstances().size()
            """,
            voy2),
        // including
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              Sequence{1, 2, 3}.including(4).size() == 4
            """,
            voy),
        // excluding
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              Sequence{1, 2, 3, 4}.excluding(4).size() == 3
            """,
            voy),
        // let expression
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              let satellites = satelliteSystem::Satellite.allInstances() in
                satellites.size() >= 0
            """,
            voy),
        // multiple let bindings
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              let sats = satelliteSystem::Satellite.allInstances(),
                  countSat = sats.size() in
                countSat >= 0
            """,
            voy2),
        // negation of isEmpty
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              not satelliteSystem::Satellite.allInstances().isEmpty()
            """,
            voy),
        // string concat
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber.concat(\"-SUFFIX\") != \"\"
              )
            """,
            voy),
        // substring
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber.substring(1, 2) == \"SC\"
              )
            """,
            voy),
        // noSatellitesAvailable
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().isEmpty()
            """,
            new Path[] {SPACECRAFT_VOYAGER}),
        // forAll on empty collection (vacuous truth)
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(sat |
                sat.serialNumber == \"NONEXISTENT\"
              ).forAll(sat | sat.massKg < 0)
            """,
            voy),
        // Atlas: serial number match
        Arguments.of(
            """
            context spaceMission::Spacecraft inv serialNumberMatch:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              )
            """,
            new Path[] {
              Path.of("src/test/resources/test-models/spacecraft-atlas.spacemission"),
              Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem"),
              Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem"),
              Path.of("src/test/resources/test-models/satellite-hubble.satellitesystem")
            }),
        // Atlas: serial inclusion
        Arguments.of(
            """
            context spaceMission::Spacecraft inv serialInclusion:
              satelliteSystem::Satellite.allInstances().collect(sat |
                sat.serialNumber
              ).includes(self.serialNumber)
            """,
            new Path[] {
              Path.of("src/test/resources/test-models/spacecraft-atlas.spacemission"),
              Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem"),
              Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem"),
              Path.of("src/test/resources/test-models/satellite-hubble.satellitesystem")
            }),
        // Atlas: andLogic
        Arguments.of(
            """
            context spaceMission::Spacecraft inv andLogic:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              ) and self.operational
            """,
            new Path[] {
              Path.of("src/test/resources/test-models/spacecraft-atlas.spacemission"),
              Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem"),
              Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem"),
              Path.of("src/test/resources/test-models/satellite-hubble.satellitesystem")
            }),
        // Int/Double comparison
        Arguments.of(
            """
            context satelliteSystem::Satellite inv:
              spaceMission::Spacecraft.allInstances().exists(sc |
                sc.serialNumber == self.serialNumber and sc.mass == self.massKg
              )
            """,
            new Path[] {
              Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission"),
              Path.of("src/test/resources/test-models/spacecraft-atlas.spacemission"),
              Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem"),
              Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem")
            }));
  }

  // ── Parameterized: success-only constraints ───────────────────────────────

  @ParameterizedTest
  @MethodSource("successOnlyConstraints")
  void testConstraintSuccess(String c, Path[] instances) {
    ConstraintResult result = VitruvOCL.evaluateConstraint(c, SM_SAT_ECORES, instances);
    assertTrue(result.isSuccess(), "Evaluation should succeed");
  }

  static Stream<Arguments> successOnlyConstraints() {
    Path[] voy = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER};
    Path[] voy2 = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS};
    Path[] voy3 = {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE};
    Path[] sc2sat3 = {
      SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE
    };
    return Stream.of(
        // Multiple models aggregation
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().select(sat |
                sat.serialNumber == self.serialNumber
              ).size() >= 1
            """,
            sc2sat3),
        // max
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              self.mass <= satelliteSystem::Satellite.allInstances().collect(sat |
                sat.massKg
              ).max()
            """,
            voy2),
        // min > 0
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().collect(sat |
                sat.massKg
              ).min() > 0
            """,
            voy2),
        // union
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().union(
                satelliteSystem::Satellite.allInstances()
              ).size() >= satelliteSystem::Satellite.allInstances().size()
            """,
            voy2),
        // flatten
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              Sequence{
                satelliteSystem::Satellite.allInstances(),
                satelliteSystem::Satellite.allInstances()
              }.flatten().size() >= 2
            """,
            voy2),
        // and logic
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              ) and self.operational
            """,
            voy),
        // or logic
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber == self.serialNumber
              ) or self.mass > 1000
            """,
            voy),
        // xor logic
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().size() > 5 xor self.operational
            """,
            voy2),
        // if-then-else
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              if satelliteSystem::Satellite.allInstances().size() > 2
              then self.operational
              else self.mass > 0
              endif
            """,
            voy2),
        // less than sum
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              self.mass < satelliteSystem::Satellite.allInstances().collect(sat |
                sat.massKg
              ).sum()
            """,
            voy2),
        // not equal serial numbers
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                sat.serialNumber != \"\"
              )
            """,
            voy),
        // unary minus
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                -sat.massKg < 0
              )
            """,
            voy),
        // long chain
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances()
                .select(sat | sat.active)
                .collect(sat | sat.massKg)
                .select(mass | mass > 100)
                .size() >= 0
            """,
            voy3),
        // reject then collect
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances()
                .reject(sat | sat.active)
                .collect(sat | sat.serialNumber)
                .notEmpty()
            """,
            voy2),
        // nested exists across metamodels
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                spaceMission::Spacecraft.allInstances().exists(sc |
                  sc.serialNumber == sat.serialNumber and sc.operational
                )
              )
            """,
            new Path[] {SPACECRAFT_VOYAGER, SPACECRAFT_ATLAS, SATELLITE_VOYAGER}),
        // nested forAll across metamodels
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat1 |
                satelliteSystem::Satellite.allInstances().forAll(sat2 |
                  sat1.serialNumber == sat2.serialNumber implies sat1.massKg == sat2.massKg
                )
              )
            """,
            voy2),
        // arithmetic
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                (sat.massKg * 2) > self.mass
              )
            """,
            voy2),
        // division
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().forAll(sat |
                sat.massKg / 2 > 0
              )
            """,
            voy),
        // first
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().first().serialNumber != \"\"
            """,
            voy2),
        // last
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().last().massKg > 0
            """,
            voy2),
        // toUpper
        Arguments.of(
            """
            context spaceMission::Spacecraft inv:
              satelliteSystem::Satellite.allInstances().exists(sat |
                sat.serialNumber.toUpper() == self.serialNumber.toUpper()
              )
            """,
            voy));
  }

  // ── Tests with satellite-only ecores (two-variable iterators) ─────────────

  /** Tests select with two variables (Cartesian product). */
  @Test
  void testSelectWithTwoVariables() {
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
            SAT_ONLY_ECORES,
            new Path[] {SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});
    if (!result.isSuccess()) {
      fail("Compilation failed: " + result.toDetailedErrorString());
    }
    assertTrue(result.isSatisfied(), "Should find distinct pairs");
  }

  /** Tests forAll with two variables: same serialNumber implies same object. */
  @Test
  void testForAllWithTwoVariables() {
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
            SAT_ONLY_ECORES,
            new Path[] {SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});
    assertTrue(result.isSuccess());
  }

  /** Tests exists with two variables: at least one pair with different masses. */
  @Test
  void testExistsWithTwoVariables() {
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
            SM_SAT_ECORES,
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS, SATELLITE_HUBBLE});
    assertTrue(result.isSuccess());
  }

  /** Tests collect with two variables: sums of pairs. */
  @Test
  void testCollectWithTwoVariables() {
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
            SM_SAT_ECORES,
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});
    assertTrue(result.isSuccess());
  }

  // ── Tests with negative outcomes (isSatisfied == false) ──────────────────

  /** Tests constraint violation: no matching serial number. */
  @Test
  void testCrossMetamodelNoMatchingSerialNumber() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          )
        """;
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, SM_SAT_ECORES, new Path[] {SPACECRAFT_VOYAGER, SATELLITE_HUBBLE});
    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "Spacecraft SC-001 should NOT match Satellite SAT-099");
  }

  /** Tests exists on empty collection → false. */
  @Test
  void testExistsOnEmptyCollection() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().select(sat |
            sat.serialNumber == \"NONEXISTENT\"
          ).exists(sat | sat.active)
        """;
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, SM_SAT_ECORES, new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});
    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "exists on empty collection should be false");
  }

  /** Tests constraint violation: all satellites having negative mass → false. */
  @Test
  void testConstraintViolation() {
    String constraint =
        """
        context spaceMission::Spacecraft inv:
          satelliteSystem::Satellite.allInstances().forAll(sat |
            sat.massKg < 0
          )
        """;
    ConstraintResult result =
        VitruvOCL.evaluateConstraint(
            constraint, SM_SAT_ECORES, new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER});
    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "All satellites having negative mass should be false");
  }

  /** Tests exists returning false: impossible serial number. */
  @Test
  void testNoMatchingCondition() {
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
            SM_SAT_ECORES,
            new Path[] {SPACECRAFT_VOYAGER, SATELLITE_VOYAGER, SATELLITE_ATLAS});
    assertTrue(result.isSuccess());
    assertFalse(result.isSatisfied(), "Should not find impossible serial number");
  }
}
