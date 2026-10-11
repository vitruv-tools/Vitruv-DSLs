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

package tools.vitruv.dsls.vitruvocl.incremental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.incremental.support.ConstraintCorpus;
import tools.vitruv.dsls.vitruvocl.incremental.support.LiveModel;
import tools.vitruv.dsls.vitruvocl.incremental.support.ModelMutator;
import tools.vitruv.dsls.vitruvocl.incremental.support.ModelSet;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapper;

/**
 * Differential (oracle) tests of the incremental evaluator against full evaluation, over the
 * constraint corpus extracted from the existing tests and the existing test models.
 *
 * <ul>
 *   <li>{@code compiledPathEqualsFreshCompilation}: building block A — evaluating the once-compiled
 *       constraint equals compiling it from scratch (oracle of {@link IncrementalSession#initialize}).
 *   <li>{@code buildAsOneTransaction} (strategy 2a): start from empty resources, add the whole model
 *       as one recorded transaction.
 *   <li>{@code randomMutations} (strategy 2b): sequences of random atomic mutations, one and several
 *       per transaction, compared after every transaction.
 * </ul>
 *
 * <p>Every session runs in oracle mode, so any difference throws {@link
 * IncrementalMismatchException} with a diff. The oracle (a full from-scratch evaluation after every
 * transaction) dominates the run time, so the defaults are small; larger sweeps use {@code
 * -Dvitruvocl.incremental.mutations=N} and {@code -Dvitruvocl.incremental.manySeeds=true}.
 */
class IncrementalOracleTest {

  private static final int MUTATIONS = Integer.getInteger("vitruvocl.incremental.mutations", 6);
  private static final long[] SEEDS =
      Boolean.getBoolean("vitruvocl.incremental.manySeeds")
          ? new long[] {1L, 7L, 42L, 1234L, 99991L}
          : new long[] {7L, 42L};

  @BeforeAll
  static void setupPaths() {
    MetamodelWrapper.setTestModelsPath(java.nio.file.Path.of("src/test/resources/test-models"));
  }

  /** The levels implemented so far; every oracle test runs for each. */
  static List<IncrementalLevel> levels() {
    return List.of(
        IncrementalLevel.FULL,
        IncrementalLevel.READ_SET,
        IncrementalLevel.POLARITY,
        IncrementalLevel.RELEVANT_INSTANCES);
  }

  /** The implemented levels except level 0 (which re-evaluates everything by definition). */
  static List<IncrementalLevel> incrementalLevels() {
    return levels().stream().filter(l -> l != IncrementalLevel.FULL).toList();
  }

  static Stream<Arguments> setsAndLevels() {
    List<Arguments> args = new ArrayList<>();
    for (ModelSet set : ModelSet.all()) {
      for (IncrementalLevel level : levels()) {
        args.add(Arguments.of(set, level));
      }
    }
    return args.stream();
  }

  static Stream<Arguments> setsLevelsSeeds() {
    List<Arguments> args = new ArrayList<>();
    for (ModelSet set : ModelSet.all()) {
      for (IncrementalLevel level : incrementalLevels()) {
        for (long seed : SEEDS) {
          args.add(Arguments.of(set, level, seed));
        }
      }
    }
    return args.stream();
  }

  static Stream<ModelSet> sets() {
    return ModelSet.all().stream();
  }

  private static IncrementalConfig oracle(IncrementalLevel level) {
    return IncrementalConfig.of(level).withVerify(true);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sets")
  void compiledPathEqualsFreshCompilation(ModelSet set) {
    List<String> constraints = ConstraintCorpus.forModelSet(set);
    assertFalse(constraints.isEmpty(), "corpus must contain constraints for " + set);
    LiveModel model = LiveModel.load(set);
    try (IncrementalSession session =
        new IncrementalSession(constraints, model.wrapper(), oracle(IncrementalLevel.READ_SET))) {
      session.initialize(); // throws IncrementalMismatchException on any difference
      assertEquals(constraints.size(), session.latestResult().result().getResults().size());
    }
  }

  @ParameterizedTest(name = "{0} level {1}")
  @MethodSource("setsAndLevels")
  void buildAsOneTransaction(ModelSet set, IncrementalLevel level) {
    List<String> constraints = ConstraintCorpus.forModelSet(set);
    LiveModel source = LiveModel.load(set);
    LiveModel target = LiveModel.empty(set);
    try (IncrementalSession session = new IncrementalSession(constraints, target.wrapper(), oracle(level))) {
      session.initialize();
      List<EChange<EObject>> transaction = target.addAllContentOf(source);
      assertFalse(transaction.isEmpty());
      session.onTransaction(transaction);
    }
  }

  @ParameterizedTest(name = "{0} level {1} seed {2}")
  @MethodSource("setsLevelsSeeds")
  void randomMutations(ModelSet set, IncrementalLevel level, long seed) {
    List<String> constraints = ConstraintCorpus.forModelSet(set);
    LiveModel model = LiveModel.load(set);
    ModelMutator mutator = new ModelMutator(model, seed);
    try (IncrementalSession session = new IncrementalSession(constraints, model.wrapper(), oracle(level))) {
      session.initialize();
      for (int step = 0; step < MUTATIONS; step++) {
        int perTransaction = step < MUTATIONS / 2 ? 1 : 1 + (step % 4);
        List<EChange<EObject>> transaction =
            model.record(
                () -> {
                  for (int i = 0; i < perTransaction; i++) {
                    mutator.mutate();
                  }
                });
        if (transaction.isEmpty()) {
          continue; // a commit without model changes does not trigger a propagation
        }
        try {
          session.onTransaction(transaction);
        } catch (IncrementalMismatchException e) {
          fail("after mutations " + mutator.log() + "\n" + e.getMessage(), e);
        }
      }
    }
  }
}
