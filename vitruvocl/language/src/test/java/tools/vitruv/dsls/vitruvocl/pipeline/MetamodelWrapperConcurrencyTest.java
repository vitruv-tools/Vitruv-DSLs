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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

/**
 * Stress tests for the shared {@code queriedAllInstancesTypes} / {@code allInstancesEngine}
 * allInstances() cache in {@link MetamodelWrapper} (mirrored in {@link VsumWrapper}).
 *
 * <p>This cache is the one piece of state that {@link ConstraintListEvaluator}-driven parallel
 * constraint evaluation genuinely shares across worker threads (every {@link
 * tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCLCompiler} instance is otherwise independent — see
 * {@link ConstraintListEvaluatorTest}). Before {@link MetamodelWrapper#getAllInstances} synchronized
 * its cache bookkeeping, concurrent first-time queries for different {@link EClass}es could race on
 * the shared {@code LinkedHashSet} and the lazily-rebuilt engine reference.
 */
class MetamodelWrapperConcurrencyTest {

  private static final Path SPACEMISSION_ECORE =
      Path.of("src/test/resources/test-metamodels/spaceMission.ecore");
  private static final Path SATELLITE_ECORE =
      Path.of("src/test/resources/test-metamodels/satelliteSystem.ecore");

  private static final Path SPACECRAFT_VOYAGER =
      Path.of("src/test/resources/test-models/spacecraft-voyager.spacemission");
  private static final Path SPACECRAFT_ACTIVE =
      Path.of("src/test/resources/test-models/spacecraft-active.spacemission");
  private static final Path SPACECRAFT_INACTIVE =
      Path.of("src/test/resources/test-models/spacecraft-inactive.spacemission");
  private static final Path SATELLITE_VOYAGER =
      Path.of("src/test/resources/test-models/satellite-voyager.satellitesystem");
  private static final Path SATELLITE_ATLAS =
      Path.of("src/test/resources/test-models/satellite-atlas.satellitesystem");
  private static final Path SATELLITE_HUBBLE =
      Path.of("src/test/resources/test-models/satellite-hubble.satellitesystem");

  private static MetamodelWrapper loadWrapper() throws IOException {
    MetamodelWrapper wrapper = new MetamodelWrapper();
    wrapper.loadMetamodel(SPACEMISSION_ECORE);
    wrapper.loadMetamodel(SATELLITE_ECORE);
    wrapper.loadModelInstance(SPACECRAFT_VOYAGER);
    wrapper.loadModelInstance(SPACECRAFT_ACTIVE);
    wrapper.loadModelInstance(SPACECRAFT_INACTIVE);
    wrapper.loadModelInstance(SATELLITE_VOYAGER);
    wrapper.loadModelInstance(SATELLITE_ATLAS);
    wrapper.loadModelInstance(SATELLITE_HUBBLE);
    return wrapper;
  }

  /**
   * Hammers {@link MetamodelWrapper#getAllInstances} for two different, previously-unqueried
   * EClasses from many threads at once — the exact scenario that used to race on the shared,
   * lazily-(re)built {@code AllInstancesEngine}: each new type first seen invalidates the cached
   * engine, so concurrent first-time lookups for different types contend on rebuilding it.
   *
   * <p>Every worker thread re-queries repeatedly (not just once) so that, even after the cache
   * settles, concurrent readers keep hitting the shared engine reference while it could still be
   * getting swapped out from under them by a straggling invalidation on another thread.
   */
  @Test
  void getAllInstances_isRaceFreeUnderConcurrentFirstTimeQueriesForDifferentTypes()
      throws Exception {
    // Reference results computed sequentially against an independent wrapper instance, so the
    // concurrent wrapper below starts with a completely cold (empty) cache.
    MetamodelWrapper referenceWrapper = loadWrapper();
    EClass spacecraftRef = referenceWrapper.resolveEClass("spaceMission", "Spacecraft");
    EClass satelliteRef = referenceWrapper.resolveEClass("satelliteSystem", "Satellite");
    List<EObject> expectedSpacecraft = referenceWrapper.getAllInstances(spacecraftRef);
    List<EObject> expectedSatellite = referenceWrapper.getAllInstances(satelliteRef);
    assertEquals(3, expectedSpacecraft.size(), "sanity check on test fixture");
    assertEquals(3, expectedSatellite.size(), "sanity check on test fixture");

    MetamodelWrapper wrapper = loadWrapper();
    EClass spacecraft = wrapper.resolveEClass("spaceMission", "Spacecraft");
    EClass satellite = wrapper.resolveEClass("satelliteSystem", "Satellite");

    int threadCount = 32;
    int queriesPerThread = 200;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
    List<Future<List<Integer>>> futures = new ArrayList<>();

    try {
      for (int t = 0; t < threadCount; t++) {
        EClass target = (t % 2 == 0) ? spacecraft : satellite;
        futures.add(
            executor.submit(
                () -> {
                  startBarrier.await(10, TimeUnit.SECONDS);
                  List<Integer> sizesSeen = new ArrayList<>(queriesPerThread);
                  for (int i = 0; i < queriesPerThread; i++) {
                    sizesSeen.add(wrapper.getAllInstances(target).size());
                  }
                  return sizesSeen;
                }));
      }

      for (int t = 0; t < threadCount; t++) {
        List<Integer> sizesSeen = futures.get(t).get(30, TimeUnit.SECONDS);
        int expectedSize = (t % 2 == 0) ? expectedSpacecraft.size() : expectedSatellite.size();
        for (int size : sizesSeen) {
          assertEquals(
              expectedSize,
              size,
              "Every concurrent getAllInstances() call must see the full, correct instance set,"
                  + " never a partial one from a racing cache rebuild");
        }
      }
    } finally {
      executor.shutdown();
    }

    // The cache must also have converged to a single, correct engine afterwards.
    assertEquals(expectedSpacecraft.size(), wrapper.getAllInstances(spacecraft).size());
    assertEquals(expectedSatellite.size(), wrapper.getAllInstances(satellite).size());
  }

  /**
   * Same idea as above but with every thread querying a distinct, never-before-queried type
   * simultaneously (as opposed to only two shared types), maximizing the number of concurrent
   * first-seen-callsite cache invalidations.
   */
  @Test
  void getAllInstances_isRaceFreeWhenManyThreadsFirstQueryDistinctTypesSimultaneously()
      throws Exception {
    MetamodelWrapper wrapper = loadWrapper();
    List<EClass> types =
        List.of(
            wrapper.resolveEClass("spaceMission", "Spacecraft"),
            wrapper.resolveEClass("spaceMission", "Mission"),
            wrapper.resolveEClass("spaceMission", "Payload"),
            wrapper.resolveEClass("spaceMission", "Astronaut"),
            wrapper.resolveEClass("satelliteSystem", "Satellite"));

    int repeats = 40;
    ExecutorService executor = Executors.newFixedThreadPool(types.size());
    CyclicBarrier startBarrier = new CyclicBarrier(types.size());
    List<Future<Set<Integer>>> futures = new ArrayList<>();

    try {
      for (EClass type : types) {
        futures.add(
            executor.submit(
                () -> {
                  startBarrier.await(10, TimeUnit.SECONDS);
                  Set<Integer> sizesSeen = new java.util.HashSet<>();
                  for (int i = 0; i < repeats; i++) {
                    sizesSeen.add(wrapper.getAllInstances(type).size());
                  }
                  return sizesSeen;
                }));
      }

      for (Future<Set<Integer>> future : futures) {
        Set<Integer> sizesSeen = future.get(30, TimeUnit.SECONDS);
        assertEquals(
            1,
            sizesSeen.size(),
            "A single type's instance count must be stable across all concurrent calls, got: "
                + sizesSeen);
      }
    } finally {
      executor.shutdown();
    }

    assertTrue(true, "No exception means no race condition surfaced");
  }
}
