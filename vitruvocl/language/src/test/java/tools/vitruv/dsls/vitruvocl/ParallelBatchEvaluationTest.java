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

package tools.vitruv.dsls.vitruvocl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.pipeline.BatchValidationResult;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCL;
import tools.vitruv.dsls.vitruvocl.pipeline.Warning;

/**
 * End-to-end tests for the {@code threadPoolSize}-configurable batch evaluation entry points on
 * {@link VitruvOCL}, which distribute {@link VitruvOCL#evaluateConstraints(List, Path[], Path[],
 * int) constraint-list evaluation} across an {@code ExecutorService}.
 *
 * <p>Covers the three properties this parallelization must preserve relative to the previous
 * sequential {@code for} loop:
 *
 * <ul>
 *   <li>identical outcomes regardless of thread pool size ({@link
 *       #sequentialAndParallelEvaluationProduceIdenticalResults()})
 *   <li>no race condition on the shared allInstances() cache under concurrent evaluation ({@link
 *       #concurrentAllInstancesQueriesAcrossManyConstraintsStayCorrect()}; the lower-level cache
 *       mechanics are covered directly in {@code MetamodelWrapperConcurrencyTest})
 *   <li>output order matches input order regardless of worker completion order ({@link
 *       #parallelEvaluationPreservesConstraintOrder()})
 * </ul>
 */
class ParallelBatchEvaluationTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  private static final Path SPACECRAFT_ACTIVE = Path.of("spacecraft-active.spacemission");
  private static final Path SPACECRAFT_VOYAGER = Path.of("spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_INACTIVE = Path.of("spacecraft-inactive.spacemission");
  private static final Path SATELLITE_VOYAGER =
      Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS =
      Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem");

  private static final Path[] BOTH_ECORES = {SPACEMISSION_ECORE, SATELLITE_ECORE};
  private static final Path[] ALL_MODELS = {
    SPACECRAFT_ACTIVE, SPACECRAFT_VOYAGER, SPACECRAFT_INACTIVE, SATELLITE_VOYAGER, SATELLITE_ATLAS
  };

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(Path.of("src/test/resources/test-models"));
  }

  /**
   * A deliberately mixed batch: satisfied / violated (with varying violation counts) / a compiler
   * error / a syntax error, plus several distinct {@code allInstances()}-based constraints so the
   * shared wrapper cache (see {@code MetamodelWrapperConcurrencyTest}) is exercised concurrently.
   */
  private static List<String> mixedConstraintBatch() {
    return List.of(
        "context spaceMission::Spacecraft inv c0: true",
        "context spaceMission::Spacecraft inv c1: false",
        "context spaceMission::Spacecraft inv c2: self.operational",
        "context spaceMission::Spacecraft inv c3: self.mass > 0",
        "context spaceMission::Spacecraft inv c4: self.nonExistentAttribute == 5",
        """
        context spaceMission::Spacecraft inv serialNumberMatch:
          satelliteSystem::Satellite.allInstances().exists(sat |
            sat.serialNumber == self.serialNumber
          )
        """,
        "context spaceMission::Spacecraft inv c6: satelliteSystem::Satellite.allInstances().size() == 2",
        """
        context spaceMission::Spacecraft inv c7:
          satelliteSystem::Satellite.allInstances().forAll(sat | sat.massKg > 0)
        """,
        "context spaceMission::Spacecraft inv c8: self.mass > 5000",
        "context spaceMission::Spacecraft inv c9: $$$ invalid @@@");
  }

  private static void assertSameOutcome(ConstraintResult expected, ConstraintResult actual) {
    assertEquals(expected.getConstraint(), actual.getConstraint());
    assertEquals(
        expected.isSuccess(),
        actual.isSuccess(),
        "success mismatch for: " + expected.getConstraint());
    if (expected.isSuccess()) {
      assertEquals(
          expected.isSatisfied(),
          actual.isSatisfied(),
          "satisfaction mismatch for: " + expected.getConstraint());
    }
    assertEquals(
        expected.getCompilerErrors().size(),
        actual.getCompilerErrors().size(),
        "compiler error count mismatch for: " + expected.getConstraint());
    long expectedViolations = countViolations(expected);
    long actualViolations = countViolations(actual);
    assertEquals(
        expectedViolations,
        actualViolations,
        "violation count mismatch for: " + expected.getConstraint());
  }

  private static long countViolations(ConstraintResult result) {
    return result.getWarnings().stream()
        .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
        .count();
  }

  /**
   * Evaluates the same mixed batch sequentially (pool size 1) and then repeats it at several
   * parallel pool sizes, asserting every constraint's outcome is byte-for-byte identical to the
   * sequential baseline at every pool size.
   */
  @Test
  void sequentialAndParallelEvaluationProduceIdenticalResults() {
    List<String> constraints = mixedConstraintBatch();

    BatchValidationResult sequential =
        VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS, 1);
    assertEquals(constraints.size(), sequential.getResults().size());

    for (int poolSize : new int[] {2, 4, 8, 16}) {
      BatchValidationResult parallel =
          VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS, poolSize);

      assertEquals(
          sequential.getResults().size(),
          parallel.getResults().size(),
          "result count mismatch at pool size " + poolSize);
      for (int i = 0; i < constraints.size(); i++) {
        assertSameOutcome(sequential.getResults().get(i), parallel.getResults().get(i));
      }
      assertEquals(
          sequential.getSummary(), parallel.getSummary(), "summary mismatch at pool size " + poolSize);
    }
  }

  /**
   * Runs the mixed batch (which includes three distinct {@code allInstances()} call sites) at high
   * parallelism repeatedly, comparing every run's violation/satisfaction outcome for the two
   * {@code allInstances()}-based constraints against a sequential baseline. Before {@code
   * MetamodelWrapper}/{@code VsumWrapper} synchronized their cache bookkeeping, concurrent
   * first-time queries for {@code Satellite} from many parallel constraint-evaluation tasks could
   * race; running many repetitions makes such a race very likely to surface as a flaky mismatch if
   * it still existed.
   */
  @Test
  void concurrentAllInstancesQueriesAcrossManyConstraintsStayCorrect() {
    List<String> constraints = mixedConstraintBatch();
    // indices of the allInstances()-based constraints in mixedConstraintBatch()
    int serialNumberMatchIndex = 5;
    int sizeCheckIndex = 6;
    int forAllIndex = 7;

    BatchValidationResult baseline =
        VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS, 1);
    ConstraintResult expectedSerialMatch = baseline.getResults().get(serialNumberMatchIndex);
    ConstraintResult expectedSizeCheck = baseline.getResults().get(sizeCheckIndex);
    ConstraintResult expectedForAll = baseline.getResults().get(forAllIndex);

    for (int run = 0; run < 20; run++) {
      BatchValidationResult parallel =
          VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS, 16);
      assertSameOutcome(expectedSerialMatch, parallel.getResults().get(serialNumberMatchIndex));
      assertSameOutcome(expectedSizeCheck, parallel.getResults().get(sizeCheckIndex));
      assertSameOutcome(expectedForAll, parallel.getResults().get(forAllIndex));
    }
  }

  /**
   * Builds a larger batch of uniquely-worded constraints and evaluates it with a worker count
   * close to the batch size (encouraging genuine interleaving), asserting the returned {@link
   * ConstraintResult} list matches the input order index-for-index.
   */
  @Test
  void parallelEvaluationPreservesConstraintOrder() {
    List<String> constraints =
        IntStream.range(0, 20)
            .mapToObj(i -> "context spaceMission::Spacecraft inv order" + i + ": self.mass > " + i)
            .toList();

    BatchValidationResult result =
        VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS, 16);

    assertEquals(constraints.size(), result.getResults().size());
    for (int i = 0; i < constraints.size(); i++) {
      assertEquals(
          constraints.get(i),
          result.getResults().get(i).getConstraint(),
          "result at index " + i + " does not correspond to the constraint submitted at that index");
    }
  }

  /** Duplicate detection (a sequential pre-pass) must still work correctly once pool size > 1. */
  @Test
  void duplicateConstraintDetectionStillWorksWithParallelEvaluation() {
    List<String> constraints =
        List.of(
            "context spaceMission::Spacecraft inv dup: true",
            "context spaceMission::Spacecraft inv dup: true",
            "context spaceMission::Spacecraft inv other: true");

    BatchValidationResult result =
        VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS, 8);

    assertEquals(3, result.getResults().size());
    assertTrue(
        result.getResults().get(1).getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.DUPLICATE_CONSTRAINT),
        "Second occurrence of a duplicate constraint should be flagged");
    assertFalse(
        result.getResults().get(0).getWarnings().stream()
            .anyMatch(w -> w.getType() == Warning.WarningType.DUPLICATE_CONSTRAINT),
        "First occurrence must not itself be flagged as a duplicate");
  }

  /** The default (no explicit pool size) overload must keep behaving exactly as before. */
  @Test
  void defaultThreadPoolSizeOverloadStillWorks() {
    List<String> constraints = mixedConstraintBatch();
    BatchValidationResult result = VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS);
    assertEquals(constraints.size(), result.getResults().size());
  }

  /**
   * Regression test for the union-dependency-analysis fix in {@code
   * SmartLoader#loadForConstraints}: {@link VitruvOCL#evaluateConstraints(List, Path[], Path[],
   * int)} used to load only the <em>first</em> constraint's metamodel dependencies for the whole
   * batch, so a later constraint referencing a package the first constraint doesn't need would fail
   * to resolve it. Directly mirrors {@code
   * VitruvOclCliTest#testEvalBatchHandlesConstraintsWithDifferentMetamodelDependencies}, but calls
   * {@code evaluateConstraints(...)} directly instead of going through the CLI subprocess-style
   * loop, at several pool sizes.
   */
  @Test
  void evaluateConstraintsLoadsUnionOfDependenciesAcrossTheWholeBatch() {
    List<String> constraints =
        List.of(
            "context spaceMission::Spacecraft inv spaceMissionOnly: true",
            "context spaceMission::Spacecraft inv usesSatelliteSystemToo:"
                + " satelliteSystem::Satellite.allInstances().size() >= 0");

    for (int poolSize : new int[] {1, 4}) {
      BatchValidationResult result =
          VitruvOCL.evaluateConstraints(constraints, BOTH_ECORES, ALL_MODELS, poolSize);

      assertEquals(2, result.getResults().size());
      ConstraintResult first = result.getResults().get(0);
      ConstraintResult second = result.getResults().get(1);
      assertTrue(
          first.isSuccess(),
          "first constraint (spaceMission only) should succeed at pool size " + poolSize);
      assertTrue(
          second.isSuccess(),
          "second constraint (needs satelliteSystem, not referenced by the first constraint)"
              + " must also succeed at pool size "
              + poolSize
              + " — errors: "
              + second.getCompilerErrors());
    }
  }
}
