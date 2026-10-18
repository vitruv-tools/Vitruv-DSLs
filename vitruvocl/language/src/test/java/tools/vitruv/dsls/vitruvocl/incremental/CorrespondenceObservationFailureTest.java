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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;

/**
 * A failing correspondence snapshot must not break the session: the transaction falls back to an
 * unknown correspondence delta (the behaviour before step B1), and observation resumes later.
 */
class CorrespondenceObservationFailureTest {

  private static final String CORR =
      "context edge::Item inv: edge::Other.allInstances().select(~).size() <= 1";
  private static final String VALUE = "context edge::Item inv: self.value >= 0";

  private static void correspond(EdgeFixture fx, String left, String right) {
    fx.model()
        .correspondences()
        .addCorrespondenceBetween(List.of(fx.find(left)), List.of(fx.find(right)), null);
  }

  private static VsumWrapper failingWrapper(EdgeFixture fx, AtomicBoolean fail) {
    VsumWrapper wrapper = spy(fx.model().wrapper());
    doAnswer(
            inv -> {
              if (fail.get()) {
                throw new IllegalStateException("simulated lookup failure");
              }
              return inv.callRealMethod();
            })
        .when(wrapper)
        .getCorrespondingObjectsByTag(any(EObject.class));
    return wrapper;
  }

  @Test
  void failingCaptureDuringATransactionFallsBackToAnUnknownDelta() {
    EdgeFixture fx = EdgeFixture.create();
    correspond(fx, "a1", "x");
    AtomicBoolean fail = new AtomicBoolean();
    try (IncrementalSession session =
        new IncrementalSession(
            List.of(CORR, VALUE),
            failingWrapper(fx, fail),
            IncrementalConfig.of(IncrementalLevel.READ_SET).withVerify(true))) {
      session.initialize();

      fail.set(true);
      IncrementalStats failed =
          session
              .onTransaction(fx.model().record(() -> EdgeFixture.list(fx.root(1), "tags").add("t")))
              .stats();
      assertEquals(1, failed.forcedEvaluations(), "unknown delta forces CORR as before: " + failed);

      fail.set(false);
      // no snapshot to diff against: still unknown, but the session keeps working
      session.onTransaction(fx.model().record(() -> EdgeFixture.list(fx.root(1), "tags").add("u")));
    }
  }

  @Test
  void failingCaptureDuringInitializeDisablesObservation() {
    EdgeFixture fx = EdgeFixture.create();
    correspond(fx, "a1", "x");
    AtomicBoolean fail = new AtomicBoolean(true);
    try (IncrementalSession session =
        new IncrementalSession(
            List.of(CORR, VALUE),
            failingWrapper(fx, fail),
            IncrementalConfig.of(IncrementalLevel.READ_SET).withVerify(true))) {
      session.initialize();
      fail.set(false);

      IncrementalStats stats =
          session
              .onTransaction(fx.model().record(() -> EdgeFixture.list(fx.root(1), "tags").add("t")))
              .stats();
      assertEquals(1, stats.forcedEvaluations(), stats.toString());
    }
  }
}
