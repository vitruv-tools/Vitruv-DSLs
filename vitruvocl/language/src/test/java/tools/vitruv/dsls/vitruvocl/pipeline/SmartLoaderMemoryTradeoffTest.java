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

package tools.vitruv.dsls.vitruvocl.pipeline;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Measures — without passing performance judgment — the memory-footprint tradeoff introduced by
 * union-based dependency loading in {@link SmartLoader#loadForConstraints}.
 *
 * <p><b>The tradeoff:</b> the old per-constraint {@code evalBatch} loop only ever holds one
 * <em>transient</em>, single-package {@link MetamodelWrapper} in memory at a time — each is
 * discarded once its constraint is evaluated, so peak memory stays roughly constant regardless of
 * how many distinct metamodels the whole batch spans. {@code loadForConstraints}, by contrast,
 * builds <em>one</em> wrapper holding the <b>union</b> of every referenced package, and keeps it
 * alive for the entire (parallel) batch run. For a batch whose constraints are homogeneous (the
 * common case — one metamodel per {@code .ocl} file) this makes no difference. For a batch spanning
 * many unrelated metamodels, the union wrapper is necessarily larger than any single constraint's
 * own needs.
 *
 * <p><b>Methodology:</b> two independent metrics are reported:
 *
 * <ol>
 *   <li><b>Loaded-element count</b> (deterministic, zero measurement noise): the number of EObjects
 *       reachable from every root object in the wrapper's resource set (metamodel classifiers +
 *       instance elements). This is the structural, always-reproducible proxy for "how much is
 *       resident" and is asserted on directly.
 *   <li><b>JVM heap delta</b> ({@code Runtime.totalMemory() - freeMemory()} before/after, with
 *       {@code System.gc()} requested in between): a best-effort, portable approximation of real
 *       memory impact. This is <em>not</em> OS-level Peak RSS — the JVM heap is one part of a
 *       process's RSS, and {@code System.gc()} is only a hint — but it is the standard portable
 *       proxy available from within a JUnit test without OS-specific tooling. Reported for
 *       visibility only; not asserted on, since GC/JIT timing makes byte-level thresholds flaky.
 * </ol>
 */
class SmartLoaderMemoryTradeoffTest {

  private static final Path METAMODELS = Path.of("src/test/resources/test-metamodels");
  private static final Path MODELS = Path.of("src/test/resources/test-models");

  /** 10 metamodels with no relationship to one another — a deliberately heterogeneous batch. */
  private static final Path[] ALL_ECORES = {
    METAMODELS.resolve("brakesystem.ecore"),
    METAMODELS.resolve("cad.ecore"),
    METAMODELS.resolve("family.ecore"),
    METAMODELS.resolve("idlabelgraph1.ecore"),
    METAMODELS.resolve("idlabelgraph2.ecore"),
    METAMODELS.resolve("model.ecore"),
    METAMODELS.resolve("model2.ecore"),
    METAMODELS.resolve("persons.ecore"),
    METAMODELS.resolve("spaceMission.ecore"),
    METAMODELS.resolve("satelliteSystem.ecore"),
  };

  private static final Path[] ALL_XMIS = {
    MODELS.resolve("brakesystem.brakesystem"),
    MODELS.resolve("Intersecting.cad"),
    MODELS.resolve("testmodel.family"),
    MODELS.resolve("base.labelgraph1"),
    MODELS.resolve("base.labelgraph2"),
    MODELS.resolve("component-no-protocol.model"),
    MODELS.resolve("entity-valid.model2"),
    MODELS.resolve("testmodel.persons"),
    MODELS.resolve("spacecraft-voyager.spacemission"),
    MODELS.resolve("satellite-voyager.satellitesystem"),
  };

  /** One constraint per metamodel above, each referencing exactly one distinct package. */
  private static final List<String> HETEROGENEOUS_CONSTRAINTS =
      List.of(
          "context brakesystem::Brakesystem inv b: true",
          "context cad::CAD_Model inv c: true",
          "context family::FamilyRegister inv f: true",
          "context Labelgraph1::Graph inv g1: true",
          "context Labelgraph2::Graph inv g2: true",
          "context model::System inv m: true",
          "context model2::Root inv m2: true",
          "context persons::PersonRegister inv p: true",
          "context spaceMission::Spacecraft inv sm: true",
          "context satelliteSystem::Satellite inv ss: true");

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(MODELS);
  }

  /** Counts every EObject reachable from the wrapper's loaded resources (roots + all contents). */
  private static long countLoadedElements(MetamodelWrapper wrapper) {
    long count = 0;
    for (EObject root : wrapper.getAllRootObjects()) {
      count++;
      Iterator<EObject> it = root.eAllContents();
      while (it.hasNext()) {
        it.next();
        count++;
      }
    }
    return count;
  }

  /** Best-effort JVM heap snapshot: requests a GC, then reads used heap. Noisy — see class doc. */
  private static long heapUsedBytes() {
    Runtime runtime = Runtime.getRuntime();
    for (int i = 0; i < 3; i++) {
      System.gc();
    }
    return runtime.totalMemory() - runtime.freeMemory();
  }

  /**
   * Deterministic structural comparison: the union wrapper (one constraint per each of 10 unrelated
   * metamodels) must hold strictly more loaded elements than any single-constraint wrapper could —
   * this is a direct, always-reproducible measurement of the tradeoff, independent of GC/JIT noise.
   */
  @Test
  void unionWrapperHoldsMoreElementsThanAnySingleConstraintWrapperWould() {
    long maxSingleConstraintElementCount = 0;
    long sumOfIndependentSingleConstraintElementCounts = 0;
    for (String constraint : HETEROGENEOUS_CONSTRAINTS) {
      SmartLoader.LoadResult single =
          SmartLoader.loadForConstraint(constraint, ALL_ECORES, ALL_XMIS);
      assertFalse(single.hasErrors(), "single-constraint load failed: " + single.fileErrors);
      long elements = countLoadedElements(single.wrapper);
      maxSingleConstraintElementCount = Math.max(maxSingleConstraintElementCount, elements);
      sumOfIndependentSingleConstraintElementCounts += elements;
    }

    SmartLoader.LoadResult union =
        SmartLoader.loadForConstraints(HETEROGENEOUS_CONSTRAINTS, ALL_ECORES, ALL_XMIS);
    assertFalse(union.hasErrors(), "union load failed: " + union.fileErrors);
    long unionElementCount = countLoadedElements(union.wrapper);

    System.out.println(
        String.format(
            java.util.Locale.ROOT,
            """
            === SmartLoader memory tradeoff: structural element counts (10 unrelated metamodels) ===
            old design  - per-constraint transient wrapper, held one at a time:
              max single-constraint wrapper size : %d elements  (this is the old approach's peak)
              sum across all %d constraints       : %d elements  (never resident simultaneously)
            new design  - one union wrapper, held for the whole (parallel) batch run:
              union wrapper size                 : %d elements
            union / max-single ratio             : %.2fx
            """,
            maxSingleConstraintElementCount,
            HETEROGENEOUS_CONSTRAINTS.size(),
            sumOfIndependentSingleConstraintElementCounts,
            unionElementCount,
            (double) unionElementCount / maxSingleConstraintElementCount));

    assertTrue(
        unionElementCount > maxSingleConstraintElementCount,
        "union wrapper should hold strictly more elements than the biggest single-constraint"
            + " wrapper, since it loads all 10 unrelated metamodels at once");
  }

  /**
   * Best-effort JVM heap-delta measurement for the same scenario, reported for visibility only (see
   * class doc for why this is not asserted on).
   */
  @Test
  void reportsHeapDeltaForUnionVersusSingleConstraintLoading() {
    // Isolated single-constraint scenario.
    long beforeSingle = heapUsedBytes();
    SmartLoader.LoadResult single =
        SmartLoader.loadForConstraint(HETEROGENEOUS_CONSTRAINTS.get(0), ALL_ECORES, ALL_XMIS);
    assertFalse(single.hasErrors());
    long afterSingle = heapUsedBytes();
    long singleDelta = afterSingle - beforeSingle;
    // Keep a strong reference until after the measurement so it can't be collected early.
    assertTrue(countLoadedElements(single.wrapper) > 0);

    // Isolated union scenario (separate JVM state - not directly summed with the above).
    long beforeUnion = heapUsedBytes();
    SmartLoader.LoadResult union =
        SmartLoader.loadForConstraints(HETEROGENEOUS_CONSTRAINTS, ALL_ECORES, ALL_XMIS);
    assertFalse(union.hasErrors());
    long afterUnion = heapUsedBytes();
    long unionDelta = afterUnion - beforeUnion;
    assertTrue(countLoadedElements(union.wrapper) > 0);

    System.out.println(
        String.format(
            java.util.Locale.ROOT,
            """
            === SmartLoader memory tradeoff: JVM heap delta (best-effort, noisy - see class doc) ===
            single-constraint wrapper heap delta : %,d bytes
            union wrapper heap delta             : %,d bytes
            (Not OS-level Peak RSS; System.gc() is only a hint. Reported for documentation, not
            asserted on - GC/JIT timing makes byte-level thresholds unreliable in CI.)
            """,
            singleDelta,
            unionDelta));
  }
}
