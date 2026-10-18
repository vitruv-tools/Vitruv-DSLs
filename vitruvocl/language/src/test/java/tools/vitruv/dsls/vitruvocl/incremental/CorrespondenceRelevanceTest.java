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
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;

/**
 * Step C3 of {@code docs/correspondence-incremental-plan.md}: an observed correspondence change
 * {@code (a, b)} re-evaluates only the context instances that reach {@code a} or {@code b} through a
 * traceable operand of some {@code ~} (binary in either operand order, {@code select/reject/
 * exists(~)}), instead of forcing the whole extension. Every session runs in oracle mode, so each
 * test also checks that the narrowed result equals a full evaluation.
 */
class CorrespondenceRelevanceTest {

  @SafeVarargs
  private static IncrementalStats statsAfter(
      String constraint, IncrementalLevel level, Consumer<EdgeFixture> setup, Consumer<EdgeFixture>... edits) {
    return statsAfter(constraint, level, false, setup, edits);
  }

  @SafeVarargs
  private static IncrementalStats statsAfter(
      String constraint,
      IncrementalLevel level,
      boolean idiom,
      Consumer<EdgeFixture> setup,
      Consumer<EdgeFixture>... edits) {
    EdgeFixture fx = EdgeFixture.create();
    setup.accept(fx);
    IncrementalStats last = null;
    IncrementalConfig config =
        IncrementalConfig.of(level).withVerify(true).withCorrespondenceIdiom(idiom);
    try (IncrementalSession session =
        new IncrementalSession(List.of(constraint), fx.model().wrapper(), config)) {
      session.initialize();
      for (Consumer<EdgeFixture> edit : edits) {
        last = session.onTransaction(fx.model().record(() -> edit.accept(fx))).stats();
      }
    }
    return last;
  }

  @SafeVarargs
  private static IncrementalStats level3(String constraint, Consumer<EdgeFixture>... edits) {
    return statsAfter(constraint, IncrementalLevel.RELEVANT_INSTANCES, fx -> {}, edits);
  }

  private static void correspond(EdgeFixture fx, String left, String right, String tag) {
    fx.model()
        .correspondences()
        .addCorrespondenceBetween(List.of(fx.find(left)), List.of(fx.find(right)), tag);
  }

  private static void uncorrespond(EdgeFixture fx, String left, String right) {
    fx.model()
        .correspondences()
        .removeCorrespondencesBetween(List.of(fx.find(left)), List.of(fx.find(right)), null);
  }

  private static void set(EdgeFixture fx, String object, String feature, Object value) {
    EObject o = fx.find(object);
    o.eSet(EdgeFixture.feature(o, feature), value);
  }

  private static void assertPartial(IncrementalStats stats, int instances) {
    assertEquals(0, stats.forcedEvaluations(), stats.toString());
    assertEquals(1, stats.partialEvaluations(), stats.toString());
    assertEquals(0, stats.classEventFallbacks(), stats.toString());
    assertEquals(instances, stats.instancesEvaluated(), stats.toString());
  }

  @Test
  void selectCorrespondenceReevaluatesOnlyThePartner() {
    String c = "context edge::Item inv: edge::Other.allInstances().select(~).size() <= 1";
    assertPartial(level3(c, fx -> correspond(fx, "a1", "x", null)), 1);
  }

  @Test
  void binaryWithSelfOnTheLeft() {
    String c =
        "context edge::SubA inv: edge::Other.allInstances().forAll(o | self ~ o implies o.amount > 1)";
    assertPartial(level3(c, fx -> correspond(fx, "a1", "x", null)), 1);
  }

  @Test
  void binaryWithSelfOnTheRight() {
    String c =
        "context edge::SubA inv: edge::Other.allInstances().forAll(o | o ~ self implies o.amount > 1)";
    assertPartial(level3(c, fx -> correspond(fx, "x", "a1", null)), 1);
  }

  @Test
  void binaryOperandReachedThroughANavigationIsInverted() {
    String c =
        "context edge::Root inv: self.items.forAll(i | edge::Other.allInstances()"
            + ".forAll(o | i ~ o implies o.amount > 1))";
    // a1 is in r1.items: only r1 is relevant, not r2
    assertPartial(level3(c, fx -> correspond(fx, "a1", "x", null)), 1);
  }

  @Test
  void untraceableOperandsOnBothSidesAreAClassEvent() {
    String c =
        "context edge::Root inv: edge::Item.allInstances().forAll(i | edge::Other.allInstances()"
            + ".forAll(o | i ~ o implies o.amount > 1))";
    IncrementalStats stats = level3(c, fx -> correspond(fx, "a1", "x", null));
    assertEquals(1, stats.classEventFallbacks(), stats.toString());
    assertEquals(2, stats.instancesEvaluated(), "both roots: " + stats);
  }

  @Test
  void removedCorrespondenceReevaluatesThePartner() {
    String c =
        "context edge::SubA inv: self.name == \"a1\" implies edge::Other.allInstances().exists(~)";
    IncrementalStats stats =
        statsAfter(
            c,
            IncrementalLevel.RELEVANT_INSTANCES,
            fx -> correspond(fx, "a1", "x", null),
            fx -> uncorrespond(fx, "a1", "x"));
    assertPartial(stats, 1);
  }

  @Test
  void taggedRejectReevaluatesOnlyThePartner() {
    String c = "context edge::Item inv: edge::Other.allInstances().reject(~, Tag = \"t\").size() == 2";
    assertPartial(level3(c, fx -> correspond(fx, "b1", "x", "t")), 1);
  }

  @Test
  void correspondenceAndModelEventsInOneTransactionAreUnited() {
    String c =
        "context edge::SubA inv: self.value < 100 and edge::Other.allInstances().select(~).isEmpty()";
    assertPartial(
        level3(
            c,
            fx -> {
              correspond(fx, "a2", "x", null);
              set(fx, "a1", "value", 200);
            }),
        2);
  }

  @Test
  void multiElementCorrespondenceMakesEverySingleSidePartnerRelevant() {
    String c = "context edge::Item inv: edge::Other.allInstances().select(~).isEmpty()";
    assertPartial(
        level3(
            c,
            fx ->
                fx.model()
                    .correspondences()
                    .addCorrespondenceBetween(
                        List.of(fx.find("a1"), fx.find("b1")), List.of(fx.find("x")), null)),
        2);
  }

  @Test
  void correspondentPartnerIsNotInvertedThroughAOneDirectionalLookup() {
    // With the idiom rule, y has origin CORRESPONDENTS(SubA). [a1, a2] ~ [x] is visible only from
    // x, so inverting y from a1 via a1's lookup would miss x; the new pair a1 ~ b1 violates x.
    String c =
        "context edge::Other inv: edge::SubA.allInstances().select(~)"
            + ".forAll(y | edge::SubB.allInstances().forAll(i | not (y ~ i)))";
    IncrementalStats stats =
        statsAfter(
            c,
            IncrementalLevel.RELEVANT_INSTANCES,
            true,
            fx ->
                fx.model()
                    .correspondences()
                    .addCorrespondenceBetween(
                        List.of(fx.find("a1"), fx.find("a2")), List.of(fx.find("x")), null),
            fx -> correspond(fx, "a1", "b1", null));
    assertEquals(0, stats.forcedEvaluations(), stats.toString());
  }

  @Test
  void belowLevelThreeACorrespondenceChangeEvaluatesTheWholeExtensionUnforced() {
    String c = "context edge::Item inv: edge::Other.allInstances().select(~).size() <= 1";
    for (IncrementalLevel level : List.of(IncrementalLevel.READ_SET, IncrementalLevel.POLARITY)) {
      IncrementalStats stats = statsAfter(c, level, fx -> {}, fx -> correspond(fx, "a1", "x", null));
      assertEquals(0, stats.forcedEvaluations(), stats.toString());
      assertEquals(1, stats.constraintsEvaluated(), stats.toString());
      assertEquals(4, stats.instancesEvaluated(), "all four items: " + stats);
    }
  }
}
