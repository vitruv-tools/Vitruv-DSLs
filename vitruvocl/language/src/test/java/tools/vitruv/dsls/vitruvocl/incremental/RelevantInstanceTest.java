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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;

/**
 * Level-3 tests (task item 4 for level 3): with the statistics counters, show that only the
 * relevant context instances are re-evaluated — via self paths, containment, the reverse index,
 * {@code eOpposite}, inserted and removed instances — and that untraceable reads fall back to
 * the whole extension (class event). All sessions run in oracle mode.
 *
 * <p>The edge model has four {@code Item}s (a1, b1, a2 in r1; b2 in r2) and two {@code Root}s.
 */
class RelevantInstanceTest {

  @SafeVarargs
  private static IncrementalStats statsAfter(
      String constraint, IncrementalConfig config, Consumer<EdgeFixture> setup, Consumer<EdgeFixture>... edits) {
    EdgeFixture fx = EdgeFixture.create();
    setup.accept(fx);
    IncrementalStats last = null;
    try (IncrementalSession session =
        new IncrementalSession(List.of(constraint), fx.model().wrapper(), config.withVerify(true))) {
      session.initialize();
      for (Consumer<EdgeFixture> edit : edits) {
        last = session.onTransaction(fx.model().record(() -> edit.accept(fx))).stats();
      }
    }
    return last;
  }

  @SafeVarargs
  private static IncrementalStats level3(String constraint, Consumer<EdgeFixture>... edits) {
    return statsAfter(constraint, IncrementalConfig.of(IncrementalLevel.RELEVANT_INSTANCES), fx -> {}, edits);
  }

  private static void set(EdgeFixture fx, String object, String feature, Object value) {
    EObject o = fx.find(object);
    o.eSet(EdgeFixture.feature(o, feature), value);
  }

  private static void assertPartial(IncrementalStats stats, int instances) {
    assertEquals(1, stats.partialEvaluations(), stats.toString());
    assertEquals(0, stats.classEventFallbacks(), stats.toString());
    assertEquals(instances, stats.instancesEvaluated(), stats.toString());
  }

  @Test
  void selfAttributeChangeEvaluatesOnlyThatInstance() {
    String c = "context edge::Item inv: self.value >= 0";
    assertPartial(level3(c, fx -> set(fx, "a1", "value", 9)), 1);
    IncrementalStats l2 =
        statsAfter(c, IncrementalConfig.of(IncrementalLevel.POLARITY), fx -> {}, fx -> set(fx, "a1", "value", 9));
    assertEquals(4, l2.instancesEvaluated(), "level 2 evaluates the whole extension: " + l2);
  }

  @Test
  void containmentPathIsInvertedViaEContainer() {
    String c = "context edge::Root inv: self.items.forAll(i | i.value >= 0)";
    assertPartial(level3(c, fx -> set(fx, "a1", "value", 9)), 1);
  }

  @Test
  void referenceWithoutOppositeIsInvertedViaTheReverseIndex() {
    // a1.next = b1: a change of b1.value concerns a1 only
    String c = "context edge::Item inv: self.next.value >= 0";
    assertPartial(level3(c, fx -> set(fx, "b1", "value", 4)), 1);
  }

  @Test
  void reverseIndexFollowsLinkChangesAcrossTransactions() {
    String c = "context edge::Item inv: self.next.value >= 0";
    IncrementalStats stats =
        level3(
            c,
            fx -> set(fx, "b1", "value", 4), // builds the index for 'next'
            fx -> set(fx, "a2", "next", fx.find("b2")), // index maintained from the event
            fx -> set(fx, "b2", "value", -1)); // now a2 is relevant (and violated)
    assertPartial(stats, 1);
  }

  @Test
  void containerFeatureIsInvertedViaItsContainmentOpposite() {
    String c = "context edge::Item inv: self.owner.tags.size() <= 2";
    assertPartial(level3(c, fx -> EdgeFixture.list(fx.root(2), "tags").add("t")), 1);
  }

  @Test
  void insertedContextInstanceIsEvaluatedAlone() {
    String c = "context edge::Item inv: self.value >= 0";
    assertPartial(
        level3(c, fx -> EdgeFixture.list(fx.root(2), "items").add(EdgeFixture.item("SubA", "n", 1))), 1);
  }

  @Test
  void removedContextInstanceIsDroppedWithoutEvaluation() {
    String c = "context edge::Item inv: self.value >= 0";
    IncrementalStats stats =
        level3(c, fx -> set(fx, "a2", "value", -1), fx -> EcoreUtil.delete(fx.find("a2"), true));
    assertPartial(stats, 0);
  }

  @Test
  void readThroughAllInstancesIsAClassEvent() {
    String c = "context edge::Item inv: edge::Item.allInstances().select(i | i.value > 100).isEmpty()";
    IncrementalStats stats = level3(c, fx -> set(fx, "a1", "value", 9));
    assertEquals(1, stats.classEventFallbacks(), stats.toString());
    assertEquals(4, stats.instancesEvaluated(), stats.toString());
  }

  @Test
  void insertionIntoAnAllInstancesExtensionIsAClassEvent() {
    // the new item violates the constraint for every existing item, not only for itself
    String c = "context edge::Item inv: edge::Item.allInstances().select(i | i.value > 100).isEmpty()";
    IncrementalStats stats =
        level3(c, fx -> EdgeFixture.list(fx.root(2), "items").add(EdgeFixture.item("SubA", "big", 500)));
    assertEquals(1, stats.classEventFallbacks(), stats.toString());
    assertEquals(5, stats.instancesEvaluated(), stats.toString());
  }

  @Test
  void insertionIntoACountedExtensionIsAClassEvent() {
    // no feature of the constraint is read: only the structural rule can make this a class event
    String c = "context edge::Item inv: edge::Item.allInstances().size() <= 4";
    IncrementalStats stats =
        level3(c, fx -> EdgeFixture.list(fx.root(2), "items").add(EdgeFixture.create("SubB")));
    assertEquals(1, stats.classEventFallbacks(), stats.toString());
    assertEquals(5, stats.instancesEvaluated(), stats.toString());
  }

  // ------------------------------------------------------------------ idiom rule (3c)

  private static final String IDIOM =
      "context edge::SubA inv: edge::Other.allInstances().select(~).forAll(o | o.amount > 0)";

  private static void correspond(EdgeFixture fx, String a, String b) {
    fx.model().correspondences().addCorrespondenceBetween(List.of(fx.find(a)), List.of(fx.find(b)), null);
  }

  @Test
  void idiomRuleNarrowsToCorrespondentsWhenEnabled() {
    IncrementalConfig on =
        IncrementalConfig.of(IncrementalLevel.RELEVANT_INSTANCES).withCorrespondenceIdiom(true);
    IncrementalStats stats = statsAfter(IDIOM, on, fx -> correspond(fx, "a1", "x"), fx -> set(fx, "x", "amount", -1));
    assertPartial(stats, 1);
  }

  @Test
  void idiomRuleIsOffByDefault() {
    IncrementalStats stats =
        statsAfter(
            IDIOM,
            IncrementalConfig.of(IncrementalLevel.RELEVANT_INSTANCES),
            fx -> correspond(fx, "a1", "x"),
            fx -> set(fx, "x", "amount", -1));
    assertEquals(1, stats.forcedEvaluations(), stats.toString());
    assertEquals(2, stats.instancesEvaluated(), "both SubA instances: " + stats);
  }

  @Test
  void idiomRuleIsUnsoundWhenCorrespondencesChangeWithoutModelEvents() {
    // The reason the rule is off by default (CorrespondenceVisibilityExperimentTest): a
    // correspondence added silently together with an unrelated change is not seen.
    IncrementalConfig on =
        IncrementalConfig.of(IncrementalLevel.RELEVANT_INSTANCES).withCorrespondenceIdiom(true);
    IncrementalMismatchException e =
        assertThrows(
            IncrementalMismatchException.class,
            () ->
                statsAfter(
                    IDIOM,
                    on,
                    fx -> set(fx, "x", "amount", -1),
                    fx -> {
                      correspond(fx, "a2", "x");
                      EdgeFixture.list(fx.root(2), "tags").add("unrelated");
                    }));
    assertTrue(e.getMessage().contains("missing violation"), e.getMessage());
  }
}
