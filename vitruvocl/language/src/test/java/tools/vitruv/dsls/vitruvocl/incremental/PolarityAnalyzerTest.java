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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EClass;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.Polarity;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.PseSet;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;

/** Unit tests of the level-2 annotation (Cabot &amp; Teniente 2009, Tab. 3.1–3.4, adapted). */
class PolarityAnalyzerTest {

  private static final EdgeFixture FX = EdgeFixture.create();

  private static PseSet pse(String constraint) {
    CompiledConstraint compiled = CompiledConstraint.compile(constraint, FX.model().wrapper());
    assertTrue(compiled.isCompiled(), "must compile: " + constraint);
    return compiled.analysis().pseSet();
  }

  private static EClass cls(String name) {
    return (EClass) EdgeFixture.PACKAGE.getEClassifier(name);
  }

  private static Polarity read(PseSet pse, String feature, String className) {
    return pse.typedReads().get(feature).get(cls(className));
  }

  @Test
  void sizeBoundedFromAbove() {
    // ≤: left +  →  size +  →  navigation +
    assertEquals(Polarity.PLUS, read(pse("context edge::Root inv: self.items.size() <= 3"), "items", "Root"));
  }

  @Test
  void sizeBoundedFromBelow() {
    assertEquals(Polarity.MINUS, read(pse("context edge::Root inv: self.items.size() >= 1"), "items", "Root"));
  }

  @Test
  void notFlips() {
    // not (size > 3) ≡ size <= 3: '>' under 'not' must stay false, so its left operand gets +
    assertEquals(
        Polarity.PLUS, read(pse("context edge::Root inv: not (self.items.size() > 3)"), "items", "Root"));
    assertEquals(
        Polarity.MINUS, read(pse("context edge::Root inv: not (self.items.size() < 1)"), "items", "Root"));
  }

  @Test
  void forAllSourceIsPlusAndExistsSourceIsMinus() {
    assertEquals(
        Polarity.PLUS,
        read(pse("context edge::Item inv: self.refs.forAll(r | r.value >= 0)"), "refs", "Item"));
    assertEquals(
        Polarity.MINUS,
        read(pse("context edge::Item inv: self.refs.exists(r | r.value >= 0)"), "refs", "Item"));
  }

  @Test
  void impliesFlipsTheAntecedent() {
    PseSet pse = pse("context edge::Item inv: self.refs.isEmpty() implies self.value > 0");
    // antecedent must stay false: isEmpty must not become true → refs must not shrink
    assertEquals(Polarity.MINUS, read(pse, "refs", "Item"));
  }

  @Test
  void possiblyEmptyComparisonOperandIsBoth() {
    PseSet pse = pse("context edge::Item inv: self.next.value > 0");
    assertEquals(Polarity.BOTH, read(pse, "next", "Item"));
  }

  @Test
  void selectBodyIsBothAndSourceKeepsTheAnnotation() {
    PseSet pse = pse("context edge::Root inv: self.items.select(i | i.next.value > 4).size() <= 1");
    assertEquals(Polarity.PLUS, read(pse, "items", "Root"));
    assertEquals(Polarity.BOTH, read(pse, "next", "Item"));
  }

  @Test
  void contextTypeIsSensitiveToInsertionOnly() {
    PseSet pse = pse("context edge::Item inv: self.value >= 0");
    assertTrue(pse.insertSensitiveTypes().contains(cls("Item")));
    assertFalse(pse.deleteSensitiveTypes().contains(cls("Item")));
  }

  @Test
  void allInstancesFollowsItsAnnotation() {
    PseSet upper = pse("context edge::Root inv: edge::Other.allInstances().size() <= 5");
    assertTrue(upper.insertSensitiveTypes().contains(cls("Other")));
    assertFalse(upper.deleteSensitiveTypes().contains(cls("Other")));
    PseSet lower = pse("context edge::Root inv: edge::Other.allInstances().notEmpty()");
    assertFalse(lower.insertSensitiveTypes().contains(cls("Other")));
    assertTrue(lower.deleteSensitiveTypes().contains(cls("Other")));
  }

  @Test
  void messageTemplateReadsAreKeptAsBoth() {
    PseSet pse = pse("context edge::Item inv: @message \"{self.name}\" self.refs.size() <= 3");
    assertEquals(Polarity.BOTH, read(pse, "name", "Item"));
  }
}
