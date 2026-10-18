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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.incremental.CorrespondenceDelta.Pair;
import tools.vitruv.dsls.vitruvocl.incremental.support.FakeCorrespondenceModel;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * Unit tests for {@link CorrespondenceSnapshot} and {@link CorrespondenceDelta} (step B1 of {@code
 * docs/correspondence-incremental-plan.md}): the diff of two snapshots reports exactly the
 * (unordered, tagged) pairs whose two-way relation {@code b ∈ corr(a, t) ∨ a ∈ corr(b, t)} changed.
 */
class CorrespondenceSnapshotTest {

  private FakeCorrespondenceModel model;
  private MetamodelWrapperInterface wrapper;
  private EObject a;
  private EObject b;
  private EObject c;
  private EObject x;

  @BeforeEach
  void setUp() {
    model = new FakeCorrespondenceModel();
    wrapper = mock(MetamodelWrapperInterface.class);
    when(wrapper.getAllObjectsInACorrespondence())
        .thenAnswer(inv -> Optional.of(model.getAllEObjectsInACorrespondence()));
    when(wrapper.getCorrespondingObjectsByTag(any(EObject.class)))
        .thenAnswer(inv -> model.getCorrespondingEObjectsWithTag((EObject) inv.getArgument(0)));
    a = EcoreFactory.eINSTANCE.createEObject();
    b = EcoreFactory.eINSTANCE.createEObject();
    c = EcoreFactory.eINSTANCE.createEObject();
    x = EcoreFactory.eINSTANCE.createEObject();
  }

  private CorrespondenceSnapshot capture() {
    return CorrespondenceSnapshot.capture(wrapper).orElseThrow();
  }

  @Test
  void unsupportedWrapperYieldsNoSnapshot() {
    MetamodelWrapperInterface plain = mock(MetamodelWrapperInterface.class);
    when(plain.getAllObjectsInACorrespondence()).thenReturn(Optional.empty());

    assertTrue(CorrespondenceSnapshot.capture(plain).isEmpty());
    assertFalse(CorrespondenceDelta.unknown().isKnown());
    assertFalse(CorrespondenceDelta.unknown().isKnownEmpty());
    assertFalse(CorrespondenceDelta.unknown().hasChanges());
  }

  @Test
  void unchangedModelGivesAKnownEmptyDelta() {
    model.addCorrespondenceBetween(List.of(a), List.of(b), "elem");
    CorrespondenceSnapshot before = capture();

    CorrespondenceDelta delta = before.diff(capture());

    assertTrue(delta.isKnownEmpty());
    assertFalse(delta.hasChanges());
  }

  @Test
  void addedAndRemovedCorrespondencesAreReportedAsTaggedPairs() {
    model.addCorrespondenceBetween(List.of(a), List.of(b), "elem");
    CorrespondenceSnapshot before = capture();
    model.removeCorrespondencesBetween(List.of(a), List.of(b), "elem");
    model.addCorrespondenceBetween(List.of(c), List.of(a), "late");

    CorrespondenceDelta delta = before.diff(capture());

    assertEquals(Set.of(new Pair(a, c, "late")), delta.added());
    assertEquals(Set.of(new Pair(b, a, "elem")), delta.removed());
    assertTrue(delta.hasChanges());
    assertEquals(Set.of(a, b, c), delta.affectedObjects());
  }

  @Test
  void changingTheTagOfAPairIsARemovalAndAnAddition() {
    model.addCorrespondenceBetween(List.of(a), List.of(b), "one");
    CorrespondenceSnapshot before = capture();
    model.removeCorrespondencesBetween(List.of(a), List.of(b), "one");
    model.addCorrespondenceBetween(List.of(a), List.of(b), "two");

    CorrespondenceDelta delta = before.diff(capture());

    assertEquals(Set.of(new Pair(a, b, "two")), delta.added());
    assertEquals(Set.of(new Pair(a, b, "one")), delta.removed());
  }

  @Test
  void multiElementCorrespondenceIsFoundFromItsSingleObjectSide() {
    CorrespondenceSnapshot before = capture();
    // [a, x] ~ [b]: a lookup of a or x does not see it, a lookup of b does
    model.addCorrespondenceBetween(List.of(a, x), List.of(b), "multi");

    CorrespondenceDelta delta = before.diff(capture());

    assertEquals(Set.of(new Pair(a, b, "multi"), new Pair(x, b, "multi")), delta.added());
    assertEquals(Set.of(), delta.removed());
  }

  @Test
  void aSecondCorrespondenceForTheSamePairChangesNothing() {
    model.addCorrespondenceBetween(List.of(a), List.of(b), "elem");
    model.addCorrespondenceBetween(List.of(b), List.of(a), "elem");
    CorrespondenceSnapshot before = capture();
    model.removeCorrespondencesBetween(List.of(a), List.of(b), "elem");

    assertTrue(before.diff(capture()).isKnownEmpty(), "b ~ a (elem) still holds");
  }

  @Test
  void anObjectThatLeftTheCorrespondenceModelReportsItsPairsAsRemoved() {
    model.addCorrespondenceBetween(List.of(a), List.of(b), null);
    CorrespondenceSnapshot before = capture();
    model.removeCorrespondencesBetween(List.of(a), List.of(b), null);

    CorrespondenceDelta delta = before.diff(capture());

    assertEquals(Set.of(new Pair(a, b, null)), delta.removed());
    assertEquals(Set.of(), delta.added());
  }

  @Test
  void untaggedCorrespondencesAreTrackedUnderTheNullTag() {
    CorrespondenceSnapshot before = capture();
    model.addCorrespondenceBetween(List.of(a), List.of(b), null);
    model.addCorrespondenceBetween(List.of(a), List.of(c), "elem");

    CorrespondenceDelta delta = before.diff(capture());

    assertEquals(Set.of(new Pair(a, b, null), new Pair(a, c, "elem")), delta.added());
  }

  @Test
  void lookupsAnswerExactlyLikeTheWrapper() {
    model.addCorrespondenceBetween(List.of(a), List.of(b), "elem");
    model.addCorrespondenceBetween(List.of(a), List.of(c), null);
    model.addCorrespondenceBetween(List.of(a, x), List.of(b), "multi");
    CorrespondenceSnapshot snapshot = capture();

    for (EObject o : List.of(a, b, c, x)) {
      assertEquals(model.getCorrespondingEObjects(o), snapshot.correspondents(o), "untagged " + o);
      for (String tag : java.util.Arrays.asList("elem", "multi", "none", null)) {
        assertEquals(
            model.getCorrespondingEObjects(o, tag), snapshot.correspondents(o, tag), tag + " " + o);
      }
      assertEquals(model.getCorrespondingEObjectsWithTag(o), snapshot.correspondentsByTag(o));
    }
    // x is in a correspondence only on a multi-element side: no lookup sees it, but it participates
    assertEquals(Set.of(), snapshot.correspondents(x));
    assertEquals(Set.of(a, b, c, x), snapshot.participants());
  }

  @Test
  void pairsAreUnorderedAndIdentityBased() {
    assertEquals(new Pair(a, b, "t"), new Pair(b, a, "t"));
    assertEquals(new Pair(a, b, "t").hashCode(), new Pair(b, a, "t").hashCode());
    assertNotEquals(new Pair(a, b, "t"), new Pair(a, b, "u"));
    assertNotEquals(new Pair(a, b, null), new Pair(a, c, null));
    assertEquals(new Pair(a, b, null), new Pair(b, a, null));
  }
}
