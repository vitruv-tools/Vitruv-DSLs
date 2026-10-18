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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.spy;

import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;

/**
 * Step C4 of {@code docs/correspondence-incremental-plan.md}: the session answers every
 * correspondence check of a transaction's evaluations from its snapshot, so no constraint scans
 * the correspondence model again. Correctness of the snapshot-backed answers is checked by the
 * oracle (verify mode evaluates from scratch on the real wrapper).
 */
class CorrespondenceSharedLookupTest {

  private static final List<String> CONSTRAINTS =
      List.of(
          "context edge::Item inv: edge::Other.allInstances().select(~).size() <= 1",
          "context edge::SubA inv: edge::Other.allInstances().forAll(o | self ~ o implies o.amount > 0)",
          "context edge::Item inv: edge::Other.allInstances().reject(~, Tag = \"t\").size() >= 1",
          "context edge::Item inv: self.value >= 0");

  private static void correspond(EdgeFixture fx, String left, String right, String tag) {
    fx.model()
        .correspondences()
        .addCorrespondenceBetween(List.of(fx.find(left)), List.of(fx.find(right)), tag);
  }

  /** Per-pair lookups on the real wrapper, i.e. everything except the snapshot capture itself. */
  private static long pairLookups(VsumWrapper wrapper) {
    return Mockito.mockingDetails(wrapper).getInvocations().stream()
        .map(inv -> inv.getMethod().getName())
        .filter(
            name ->
                name.equals("getCorrespondingObjects")
                    || name.equals("getCorrespondingObjectsWithTag")
                    || name.equals("correspondenceHasTag"))
        .count();
  }

  private static EdgeFixture fixture() {
    EdgeFixture fx = EdgeFixture.create();
    correspond(fx, "a1", "x", null);
    correspond(fx, "b1", "y", "t");
    return fx;
  }

  @Test
  void evaluationsOfATransactionDoNotLookUpCorrespondencesInTheModel() {
    for (IncrementalLevel level :
        List.of(IncrementalLevel.READ_SET, IncrementalLevel.POLARITY, IncrementalLevel.RELEVANT_INSTANCES)) {
      EdgeFixture fx = fixture();
      VsumWrapper wrapper = spy(fx.model().wrapper());
      try (IncrementalSession session =
          new IncrementalSession(CONSTRAINTS, wrapper, IncrementalConfig.of(level))) {
        session.initialize();
        assertEquals(0, pairLookups(wrapper), "initial evaluation at " + level);

        clearInvocations(wrapper);
        IncrementalStats stats =
            session
                .onTransaction(
                    fx.model()
                        .record(
                            () -> {
                              correspond(fx, "a2", "y", null);
                              EObject o = fx.find("x");
                              o.eSet(EdgeFixture.feature(o, "amount"), 3);
                            }))
                .stats();
        assertTrue(stats.constraintsEvaluated() >= 3, stats.toString());
        assertEquals(0, pairLookups(wrapper), "transaction at " + level + ": " + stats);
      }
    }
  }

  @Test
  void snapshotBackedResultsEqualAFullEvaluation() {
    for (IncrementalLevel level :
        List.of(IncrementalLevel.READ_SET, IncrementalLevel.POLARITY, IncrementalLevel.RELEVANT_INSTANCES)) {
      EdgeFixture fx = fixture();
      try (IncrementalSession session =
          new IncrementalSession(
              CONSTRAINTS, fx.model().wrapper(), IncrementalConfig.of(level).withVerify(true))) {
        session.initialize();
        session.onTransaction(fx.model().record(() -> correspond(fx, "a2", "y", null)));
        session.onTransaction(
            fx.model()
                .record(
                    () ->
                        fx.model()
                            .correspondences()
                            .removeCorrespondencesBetween(
                                List.of(fx.find("a1")), List.of(fx.find("x")), null)));
        session.onTransaction(
            fx.model()
                .record(
                    () ->
                        fx.model()
                            .correspondences()
                            .addCorrespondenceBetween(
                                List.of(fx.find("a1"), fx.find("b1")), List.of(fx.find("y")), "t")));
      }
    }
  }

  @Test
  void sharedLookupWorksWithParallelEvaluation() {
    EdgeFixture fx = fixture();
    VsumWrapper wrapper = spy(fx.model().wrapper());
    try (IncrementalSession session =
        new IncrementalSession(
            CONSTRAINTS,
            wrapper,
            IncrementalConfig.of(IncrementalLevel.RELEVANT_INSTANCES).withThreads(4).withVerify(true))) {
      session.initialize();
      clearInvocations(wrapper);
      session.onTransaction(fx.model().record(() -> correspond(fx, "a2", "y", null)));
      // verify mode evaluates from scratch on the wrapper itself; count only the session's own run
    }
    EdgeFixture fx2 = fixture();
    VsumWrapper counted = spy(fx2.model().wrapper());
    try (IncrementalSession session =
        new IncrementalSession(
            CONSTRAINTS, counted, IncrementalConfig.of(IncrementalLevel.READ_SET).withThreads(4))) {
      session.initialize();
      session.onTransaction(fx2.model().record(() -> correspond(fx2, "a2", "y", null)));
      assertEquals(0, pairLookups(counted), "parallel evaluations use the snapshot too");
    }
  }

  @Test
  void sharedLookupCanBeSwitchedOff() {
    System.setProperty(IncrementalSession.PROP_NO_SHARED_CORRESPONDENCE_LOOKUP, "true");
    try {
      EdgeFixture fx = fixture();
      VsumWrapper wrapper = spy(fx.model().wrapper());
      try (IncrementalSession session =
          new IncrementalSession(CONSTRAINTS, wrapper, IncrementalConfig.of(IncrementalLevel.READ_SET))) {
        session.initialize();
        assertTrue(pairLookups(wrapper) > 0, "switched off: evaluations ask the wrapper");
      }
    } finally {
      System.clearProperty(IncrementalSession.PROP_NO_SHARED_CORRESPONDENCE_LOOKUP);
    }
  }

  @Test
  void idiomResolutionUsesTheSnapshotToo() {
    EdgeFixture fx = fixture();
    VsumWrapper wrapper = spy(fx.model().wrapper());
    String idiom =
        "context edge::SubA inv: edge::Other.allInstances().select(~).forAll(o | o.amount > 0)";
    try (IncrementalSession session =
        new IncrementalSession(
            List.of(idiom),
            wrapper,
            IncrementalConfig.of(IncrementalLevel.RELEVANT_INSTANCES).withCorrespondenceIdiom(true))) {
      session.initialize();
      clearInvocations(wrapper);
      // a model event on a correspondent: resolved through the correspondents of x
      session.onTransaction(
          fx.model()
              .record(
                  () -> {
                    EObject o = fx.find("x");
                    o.eSet(EdgeFixture.feature(o, "amount"), 5);
                  }));
      assertEquals(0, pairLookups(wrapper), "idiom resolution must not scan the model");
    }
  }

  @Test
  void levelZeroKeepsUsingTheWrapper() {
    EdgeFixture fx = fixture();
    VsumWrapper wrapper = spy(fx.model().wrapper());
    try (IncrementalSession session =
        new IncrementalSession(CONSTRAINTS, wrapper, IncrementalConfig.of(IncrementalLevel.FULL))) {
      session.initialize();
      assertTrue(pairLookups(wrapper) > 0, "level 0 observes nothing and asks the wrapper");
    }
  }
}
