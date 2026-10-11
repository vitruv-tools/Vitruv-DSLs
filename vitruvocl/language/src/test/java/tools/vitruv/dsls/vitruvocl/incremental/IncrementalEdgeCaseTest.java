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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.incremental.support.ConstraintCorpus;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.incremental.support.ModelMutator;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;

/**
 * Targeted edge cases (task item 3) on the {@link EdgeFixture} model, each run in oracle mode for
 * every implemented level: a wrong skip or a wrong partial evaluation throws {@link
 * IncrementalMismatchException}. Where it matters, the test also checks that the scenario really
 * changed the outcome (so that the oracle had something to compare).
 */
class IncrementalEdgeCaseTest {

  private static final List<String> CONSTRAINTS =
      ConstraintCorpus.all().stream().filter(c -> c.contains("edge::")).toList();

  static Stream<IncrementalLevel> levels() {
    return IncrementalOracleTest.levels().stream();
  }

  /** Runs {@code edit} as one transaction under oracle mode and returns the session result. */
  private static IncrementalResult run(EdgeFixture fx, IncrementalLevel level, Runnable... edits) {
    IncrementalResult last = null;
    try (IncrementalSession session =
        new IncrementalSession(
            CONSTRAINTS, fx.model().wrapper(), IncrementalConfig.of(level).withVerify(true))) {
      session.initialize();
      for (Runnable edit : edits) {
        List<EChange<EObject>> tx = fx.model().record(edit);
        last = session.onTransaction(tx);
      }
    }
    return last;
  }

  private static boolean violated(IncrementalResult result, String constraintName) {
    for (ConstraintResult r : result.result().getResults()) {
      if (r.getConstraint().contains(" " + constraintName + ":")
          || r.getConstraint().contains(" " + constraintName + "(")) {
        return r.isSuccess() && !r.isSatisfied();
      }
    }
    throw new IllegalArgumentException("unknown constraint " + constraintName);
  }

  private static void set(EObject o, String feature, Object value) {
    o.eSet(EdgeFixture.feature(o, feature), value);
  }

  @Test
  void edgeCorpusCompiles() {
    EdgeFixture fx = EdgeFixture.create();
    try (IncrementalSession session =
        new IncrementalSession(CONSTRAINTS, fx.model().wrapper(), IncrementalConfig.of(IncrementalLevel.READ_SET))) {
      IncrementalResult result = session.initialize();
      List<String> failed =
          result.result().getResults().stream()
              .filter(r -> !r.isSuccess())
              .map(r -> r.getConstraint() + " -> " + r.getCompilerErrors().get(0).getMessage())
              .toList();
      assertTrue(failed.isEmpty(), "edge constraints must compile: " + failed);
    }
  }

  @ParameterizedTest
  @MethodSource("levels")
  void polymorphicFeatureDefinedOnlyInSubclass(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r = run(fx, level, () -> set(fx.find("a1"), "extra", -1));
    assertTrue(violated(r, "polyExtra"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void featureNameSharedByUnrelatedClasses(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(
            fx,
            level,
            () -> set(fx.find("y"), "label", "z"),
            () -> set(fx.find("b1"), "label", "forbidden"));
    assertTrue(violated(r, "sharedLabelOther"));
    assertTrue(violated(r, "sharedLabelSub"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void containmentMoveWithoutCreateOrDelete(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(fx, level, () -> EdgeFixture.list(fx.root(1), "items").add(fx.find("b2")));
    assertTrue(violated(r, "atMostThreeItems"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void createWithoutContainment(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    run(
        fx,
        level,
        () -> {
          EObject orphan = EdgeFixture.item("SubA", "orphan", -100);
          set(orphan, "next", fx.find("a1"));
        },
        () -> set(fx.find("a1"), "value", 6));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void deleteKeepingDanglingCrossReferences(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    run(fx, level, () -> EcoreUtil.remove(fx.find("b1")), () -> set(fx.find("a1"), "value", 6));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void deleteRemovingCrossReferences(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    run(fx, level, () -> EcoreUtil.delete(fx.find("a2"), true));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void firstAndAtAfterPermutation(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(
            fx,
            level,
            () -> ((org.eclipse.emf.common.util.EList<Object>) EdgeFixture.list(fx.root(1), "items")).move(0, 1));
    assertTrue(violated(r, "firstPositive"), "b1 (value 3) is now first");
  }

  @ParameterizedTest
  @MethodSource("levels")
  void notSelectRejectExists(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(
            fx,
            level,
            () -> set(fx.find("a2"), "value", 9),
            () -> set(fx.find("a1"), "value", -1),
            () -> set(fx.find("a1"), "value", 5));
    assertTrue(violated(r, "notExists"), "b1.refs contains a2 with value 9");
  }

  @ParameterizedTest
  @MethodSource("levels")
  void impliesWithEmptyAntecedent(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    run(
        fx,
        level,
        () -> fx.find("a1").eUnset(EdgeFixture.feature(fx.find("a1"), "next")),
        () -> set(fx.find("b1"), "next", fx.find("a2")),
        () -> set(fx.find("b1"), "value", -3));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void letAndIterate(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r = run(fx, level, () -> set(fx.find("a2"), "value", 30));
    assertTrue(violated(r, "letIterate"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void preAndPostAreAlwaysEvaluated(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r = run(fx, level, () -> set(fx.find("a1"), "value", 1));
    assertTrue(violated(r, "valueNotDecreasing"));

    EdgeFixture fx2 = EdgeFixture.create();
    IncrementalResult created =
        run(
            fx2,
            level,
            () -> EdgeFixture.list(fx2.root(2), "items").add(EdgeFixture.item("SubB", "neg", -4)));
    assertTrue(violated(created, "newItemsPositive"));
    // the next transaction does not create anything: the post block must be re-evaluated anyway
    assertFalse(created.stats().constraintsEvaluated() == 0);
  }

  @ParameterizedTest
  @MethodSource("levels")
  void correspondenceChangeWithoutPartnerEvent(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(
            fx,
            level,
            () -> {
              fx.model().correspondences().addCorrespondenceBetween(List.of(fx.find("a1")), List.of(fx.find("x")), "t");
              fx.model().correspondences().addCorrespondenceBetween(List.of(fx.find("a1")), List.of(fx.find("y")), "t");
              // the only recorded change touches nothing the correspondence constraints read
              EdgeFixture.list(fx.root(2), "tags").add("unrelated");
            });
    assertTrue(violated(r, "corrTagged"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void linkAndUnlinkInSameCommitOnNonUniqueList(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(
            fx,
            level,
            () -> {
              List<Object> refs = EdgeFixture.list(fx.find("b1"), "refs");
              refs.add(fx.find("a1"));
              refs.add(fx.find("a1"));
              refs.remove(0);
            });
    assertTrue(violated(r, "nonUniqueRefs"), "a1 is referenced twice after link/link/unlink");
  }

  @ParameterizedTest
  @MethodSource("levels")
  void multipleSpecifications(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    EObject a1 = fx.find("a1");
    run(fx, level, () -> set(a1, "name", ""), () -> set(a1, "value", -2));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void multiValuedAttribute(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(fx, level, () -> EdgeFixture.list(fx.root(1), "tags").addAll(List.of("t", "t", "u")));
    assertTrue(violated(r, "tagsBounded"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void messageTemplateIsRefreshedWhenOnlyTheTemplateAttributeChanges(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    EObject a1 = fx.find("a1");
    IncrementalResult r = run(fx, level, () -> set(a1, "value", -5), () -> set(a1, "name", "renamed"));
    String warnings =
        r.result().getResults().stream()
            .filter(c -> c.getConstraint().contains("messageTemplate"))
            .findFirst()
            .orElseThrow()
            .getWarnings()
            .toString();
    assertTrue(warnings.contains("Item \"renamed\" has a negative value"), warnings);
  }

  @ParameterizedTest
  @MethodSource("levels")
  void containerFeatureFollowsContainmentMoves(IncrementalLevel level) {
    // 'owner' is transient (opposite of the containment Root.items): its change is not recorded
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r =
        run(
            fx,
            level,
            () -> EdgeFixture.list(fx.root(2), "tags").addAll(List.of("a", "b", "c")),
            () -> EdgeFixture.list(fx.root(2), "items").add(fx.find("a1")));
    assertTrue(violated(r, "ownerTags"), "a1 now belongs to r2, which has three tags");
    // context SubB is not structurally affected by moving the SubA a1; only the container
    // feature read r.owner (via b1.refs) connects the two
    assertTrue(violated(r, "refsOwnerTags"), "b1.refs contains a1, now owned by r2");
  }

  @ParameterizedTest
  @MethodSource("levels")
  void transientAttributeIsAlwaysEvaluated(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    EObject a1 = fx.find("a1");
    IncrementalResult r = run(fx, level, () -> set(a1, "cache", -1), () -> set(a1, "value", 6));
    assertTrue(violated(r, "transientCache"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void appendOfAnElementOutsideTheStaticElementType(IncrementalLevel level) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalResult r = run(fx, level, () -> set(fx.find("x"), "label", "zz"));
    assertTrue(violated(r, "appendForeignElement"), "the appended Other now has label zz");
  }

  @ParameterizedTest
  @MethodSource("levels")
  void possiblyEmptyComparisonOperandBecomesDefined(IncrementalLevel level) {
    // b2.next is unset ('self.next.value >= 0' is vacuously true); setting it to an item with a
    // negative value violates the constraint although 'next' only grows
    EdgeFixture fx = EdgeFixture.create();
    EObject negative = EdgeFixture.item("SubA", "neg", -5);
    IncrementalResult r =
        run(
            fx,
            level,
            () -> EdgeFixture.list(fx.root(2), "items").add(negative),
            () -> set(fx.find("b2"), "next", negative));
    assertTrue(violated(r, "nextNonNegative"));
  }

  @ParameterizedTest
  @MethodSource("levels")
  void randomMutationsOnEdgeModel(IncrementalLevel level) {
    // more seeds for a larger sweep: -Dvitruvocl.incremental.edgeSeeds=N
    int seeds = Integer.getInteger("vitruvocl.incremental.edgeSeeds", 4);
    for (long seed = 1; seed <= seeds; seed++) {
      EdgeFixture fx = EdgeFixture.create();
      ModelMutator mutator = new ModelMutator(fx.model(), seed);
      try (IncrementalSession session =
          new IncrementalSession(
              CONSTRAINTS, fx.model().wrapper(), IncrementalConfig.of(level).withVerify(true))) {
        session.initialize();
        for (int step = 0; step < 40; step++) {
          int perTransaction = 1 + step % 3;
          List<EChange<EObject>> tx =
              fx.model().record(
                  () -> {
                    for (int i = 0; i < perTransaction; i++) {
                      mutator.mutate();
                    }
                  });
          if (tx.isEmpty()) {
            continue;
          }
          try {
            session.onTransaction(tx);
          } catch (IncrementalMismatchException e) {
            throw new AssertionError("seed " + seed + " after " + mutator.log() + "\n" + e.getMessage(), e);
          }
        }
      }
    }
  }
}
