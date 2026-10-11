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

import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;

/**
 * Level-1 skip tests (task item 4): with the statistics counters, show that a constraint is
 * skipped when the transaction touches nothing it reads and evaluated when it does. Every session
 * also runs in oracle mode.
 */
class ReadSetSkipTest {

  private static IncrementalStats statsAfter(
      String constraint, IncrementalLevel level, java.util.function.Consumer<EdgeFixture> edit) {
    EdgeFixture fx = EdgeFixture.create();
    try (IncrementalSession session =
        new IncrementalSession(
            List.of(constraint), fx.model().wrapper(), IncrementalConfig.of(level).withVerify(true))) {
      session.initialize();
      List<EChange<EObject>> tx = fx.model().record(() -> edit.accept(fx));
      return session.onTransaction(tx).stats();
    }
  }

  private static void set(EObject o, String feature, Object value) {
    o.eSet(EdgeFixture.feature(o, feature), value);
  }

  private static void assertSkipped(IncrementalStats stats) {
    assertEquals(1, stats.constraintsSkipped(), "expected the constraint to be skipped: " + stats);
    assertEquals(0, stats.constraintsEvaluated(), stats.toString());
    assertEquals(0, stats.instancesEvaluated(), stats.toString());
  }

  private static void assertEvaluated(IncrementalStats stats) {
    assertEquals(0, stats.constraintsSkipped(), "expected the constraint to be evaluated: " + stats);
    assertEquals(1, stats.constraintsEvaluated(), stats.toString());
  }

  private static final String OTHER_LABEL = "context edge::Other inv: self.label != \"z\"";
  private static final String POLY = "context edge::Item inv: self.oclIsKindOf(edge::SubA) implies self.oclAsType(edge::SubA).extra >= 0";
  private static final String ITEM_COUNT = "context edge::Root inv: self.items.size() <= 3";
  private static final String VALUE = "context edge::Item inv: self.value >= 0";
  private static final String POST = "context edge::Item post: self.value >= self.value@pre";
  private static final String CORR = "context edge::Item inv: edge::Other.allInstances().select(~).size() <= 1";

  @Test
  void unrelatedAttributeChangeIsSkipped() {
    assertSkipped(statsAfter(OTHER_LABEL, IncrementalLevel.READ_SET, fx -> set(fx.find("a1"), "value", 9)));
  }

  @Test
  void readAttributeChangeIsEvaluated() {
    assertEvaluated(statsAfter(OTHER_LABEL, IncrementalLevel.READ_SET, fx -> set(fx.find("x"), "label", "q")));
  }

  @Test
  void sameFeatureNameOnUnrelatedClassIsSkipped() {
    // 'label' of SubB is not read by a constraint on Other.label (typed read, Other only)
    assertSkipped(statsAfter(OTHER_LABEL, IncrementalLevel.READ_SET, fx -> set(fx.find("b1"), "label", "q")));
  }

  @Test
  void featureOfSubclassIsMatchedThroughStaticSupertype() {
    assertEvaluated(statsAfter(POLY, IncrementalLevel.READ_SET, fx -> set(fx.find("a1"), "extra", 4)));
    assertSkipped(statsAfter(POLY, IncrementalLevel.READ_SET, fx -> set(fx.find("b1"), "label", "q")));
  }

  @Test
  void attributeChangeDoesNotTouchContainmentList() {
    assertSkipped(statsAfter(ITEM_COUNT, IncrementalLevel.READ_SET, fx -> set(fx.find("a1"), "value", 9)));
  }

  @Test
  void containmentInsertIsEvaluated() {
    assertEvaluated(
        statsAfter(
            ITEM_COUNT,
            IncrementalLevel.READ_SET,
            fx -> EdgeFixture.list(fx.root(2), "items").add(EdgeFixture.item("SubA", "n", 1))));
  }

  @Test
  void insertingAnObjectOfTheContextTypeIsEvaluated() {
    // VALUE reads only Item.value; a new Item changes the context extension
    assertEvaluated(
        statsAfter(
            VALUE,
            IncrementalLevel.READ_SET,
            fx -> EdgeFixture.list(fx.root(2), "items").add(EdgeFixture.create("SubB"))));
  }

  @Test
  void insertingAnObjectOfAnotherTypeIsSkipped() {
    assertSkipped(
        statsAfter(
            VALUE,
            IncrementalLevel.READ_SET,
            fx -> EdgeFixture.list(fx.root(1), "others").add(EdgeFixture.create("Other"))));
  }

  @Test
  void emptyTransactionIsSkippedButPrePostIsAlwaysEvaluated() {
    assertSkipped(statsAfter(VALUE, IncrementalLevel.READ_SET, fx -> {}));
    IncrementalStats post = statsAfter(POST, IncrementalLevel.READ_SET, fx -> {});
    assertEvaluated(post);
    assertEquals(1, post.forcedEvaluations());
  }

  @Test
  void correspondenceConstraintIsEvaluatedOnAnyNonEmptyTransaction() {
    IncrementalStats stats =
        statsAfter(CORR, IncrementalLevel.READ_SET, fx -> EdgeFixture.list(fx.root(1), "tags").add("t"));
    assertEvaluated(stats);
    assertEquals(1, stats.forcedEvaluations());
    assertSkipped(statsAfter(CORR, IncrementalLevel.READ_SET, fx -> {}));
  }

  @Test
  void levelZeroEvaluatesEverything() {
    IncrementalStats stats = statsAfter(OTHER_LABEL, IncrementalLevel.FULL, fx -> set(fx.find("a1"), "value", 9));
    assertEvaluated(stats);
    assertEquals(2, stats.instancesEvaluated(), "both Other instances");
  }
}
