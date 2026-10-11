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
import java.util.function.Consumer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;

/**
 * Level-2 skip tests (task item 4): with the statistics counters, show that a transaction that
 * touches the read set only in the "safe" direction is evaluated at level 1 but skipped at level
 * 2, and that events in the violating direction (and constraints with current violations) are
 * still evaluated. All sessions run in oracle mode.
 */
class PolaritySkipTest {

  /** Statistics of the last of {@code edits}, each one transaction. */
  @SafeVarargs
  private static IncrementalStats statsAfter(
      String constraint, IncrementalLevel level, Consumer<EdgeFixture>... edits) {
    EdgeFixture fx = EdgeFixture.create();
    IncrementalStats last = null;
    try (IncrementalSession session =
        new IncrementalSession(
            List.of(constraint), fx.model().wrapper(), IncrementalConfig.of(level).withVerify(true))) {
      session.initialize();
      for (Consumer<EdgeFixture> edit : edits) {
        last = session.onTransaction(fx.model().record(() -> edit.accept(fx))).stats();
      }
    }
    return last;
  }

  /** Evaluated at level 1, skipped at level 2. */
  @SafeVarargs
  private static void assertSkippedOnlyByPolarity(String constraint, Consumer<EdgeFixture>... edits) {
    IncrementalStats l1 = statsAfter(constraint, IncrementalLevel.READ_SET, edits);
    assertEquals(1, l1.constraintsEvaluated(), "level 1 must evaluate: " + l1);
    IncrementalStats l2 = statsAfter(constraint, IncrementalLevel.POLARITY, edits);
    assertEquals(1, l2.constraintsSkipped(), "level 2 must skip: " + l2);
  }

  @SafeVarargs
  private static void assertEvaluatedAtLevelTwo(String constraint, Consumer<EdgeFixture>... edits) {
    IncrementalStats l2 = statsAfter(constraint, IncrementalLevel.POLARITY, edits);
    assertEquals(1, l2.constraintsEvaluated(), "level 2 must evaluate: " + l2);
  }

  private static final String AT_MOST_THREE = "context edge::Root inv: self.items.size() <= 3";
  private static final String REFS_FORALL = "context edge::Item inv: self.refs.forAll(r | r.value >= 0)";
  // b2 has no refs; the disjunct keeps the initial state free of violations
  private static final String REFS_NOT_EMPTY =
      "context edge::SubB inv: self.refs.notEmpty() or self.name == \"b2\"";
  private static final String FEW_OTHERS = "context edge::Root inv: edge::Other.allInstances().size() <= 5";
  private static final String VALUE = "context edge::Item inv: self.value >= 0";
  private static final String NEXT_VALUE = "context edge::Item inv: self.next.value > 0";

  private static void removeItem(EdgeFixture fx) {
    EcoreUtil.remove(fx.find("a2"));
  }

  private static void addItem(EdgeFixture fx) {
    EdgeFixture.list(fx.root(1), "items").add(EdgeFixture.item("SubA", "n", 1));
  }

  @Test
  void removingFromABoundedCollectionIsNotAPse() {
    assertSkippedOnlyByPolarity(AT_MOST_THREE, PolaritySkipTest::removeItem);
  }

  @Test
  void addingToABoundedCollectionIsAPse() {
    assertEvaluatedAtLevelTwo(AT_MOST_THREE, PolaritySkipTest::addItem);
  }

  @Test
  void aCurrentViolationDisablesThePolarityFilter() {
    // first transaction violates (4 items), second removes one: must be re-evaluated to repair
    assertEvaluatedAtLevelTwo(AT_MOST_THREE, PolaritySkipTest::addItem, PolaritySkipTest::removeItem);
  }

  @Test
  void forAllSourceShrinkingIsNotAPse() {
    assertSkippedOnlyByPolarity(
        REFS_FORALL, fx -> EdgeFixture.list(fx.find("b1"), "refs").remove(0));
    assertEvaluatedAtLevelTwo(
        REFS_FORALL, fx -> EdgeFixture.list(fx.find("b1"), "refs").add(fx.find("b2")));
  }

  @Test
  void notEmptySourceGrowingIsNotAPse() {
    assertSkippedOnlyByPolarity(
        REFS_NOT_EMPTY, fx -> EdgeFixture.list(fx.find("b1"), "refs").add(fx.find("b2")));
    assertEvaluatedAtLevelTwo(
        REFS_NOT_EMPTY, fx -> EdgeFixture.list(fx.find("b1"), "refs").remove(0));
  }

  @Test
  void allInstancesUnderPlusIsSensitiveToInsertionOnly() {
    assertSkippedOnlyByPolarity(FEW_OTHERS, fx -> EcoreUtil.delete(fx.find("x"), true));
    assertEvaluatedAtLevelTwo(
        FEW_OTHERS, fx -> EdgeFixture.list(fx.root(1), "others").add(EdgeFixture.create("Other")));
  }

  @Test
  void deletingAContextInstanceIsNotAPse() {
    // Tab. 3.4, "Access self variable": only InsertET of the context type
    assertSkippedOnlyByPolarity(VALUE, PolaritySkipTest::removeItem);
    assertEvaluatedAtLevelTwo(VALUE, PolaritySkipTest::addItem);
  }

  @Test
  void attributeChangesAreAlwaysPses() {
    assertEvaluatedAtLevelTwo(VALUE, fx -> fx.find("a1").eSet(EdgeFixture.feature(fx.find("a1"), "value"), 9));
  }

  @Test
  void possiblyEmptyOperandIsAnnotatedBoth() {
    // 'self.next.value' may be empty, so the operand of '>' is conservatively annotated ±
    // (VitruvOCL's handling of empty operands), and unsetting 'next' counts as a PSE
    EObject[] a1 = new EObject[1];
    assertEvaluatedAtLevelTwo(
        NEXT_VALUE,
        fx -> {
          a1[0] = fx.find("a1");
          a1[0].eUnset(EdgeFixture.feature(a1[0], "next"));
        });
  }
}
