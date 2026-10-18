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

package tools.vitruv.dsls.vitruvocl.evaluator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * Unit tests for {@link CorrespondenceLookup} (step A1 of {@code
 * docs/correspondence-incremental-plan.md}): the answers equal the uncached bidirectional check
 * {@code b ∈ corr(a) ∨ a ∈ corr(b)} (also for one-directional lookups, as Vitruvius produces for
 * correspondences with several objects on a side), and every distinct object is looked up at most
 * once per tag.
 */
class CorrespondenceLookupTest {

  private EObject self;
  private List<EObject> partners;
  private List<EObject> strangers;

  /**
   * Directed edges {from, to, tag}: a lookup of {@code from} returns {@code to}, a lookup of {@code
   * to} does not return {@code from} — the asymmetry of a Vitruvius correspondence {@code [from, x]
   * ~ [to]} looked up per single object.
   */
  private final List<Object[]> edges = new ArrayList<>();

  @BeforeEach
  void setUp() {
    self = EcoreFactory.eINSTANCE.createEObject();
    partners = new ArrayList<>();
    strangers = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      partners.add(EcoreFactory.eINSTANCE.createEObject());
      strangers.add(EcoreFactory.eINSTANCE.createEObject());
    }
    edges.add(new Object[] {self, partners.get(0), "elem"});
    edges.add(new Object[] {self, partners.get(1), "late"});
    edges.add(new Object[] {self, partners.get(2), null});
    // only visible from the partner's side
    edges.add(new Object[] {partners.get(3), self, "elem"});
    edges.add(new Object[] {partners.get(4), self, null});
  }

  private MetamodelWrapperInterface wrapper(boolean taggedLookup) {
    MetamodelWrapperInterface wrapper = mock(MetamodelWrapperInterface.class);
    when(wrapper.hasTaggedCorrespondenceLookup()).thenReturn(taggedLookup);
    when(wrapper.getCorrespondingObjects(any(EObject.class)))
        .thenAnswer(inv -> lookup(inv.getArgument(0), null));
    when(wrapper.getCorrespondingObjectsWithTag(any(EObject.class), anyString()))
        .thenAnswer(inv -> lookup(inv.getArgument(0), inv.getArgument(1)));
    when(wrapper.correspondenceHasTag(any(EObject.class), any(EObject.class), anyString()))
        .thenAnswer(
            inv ->
                lookup(inv.getArgument(0), inv.getArgument(2)).contains(inv.getArgument(1))
                    || lookup(inv.getArgument(1), inv.getArgument(2))
                        .contains(inv.getArgument(0)));
    return wrapper;
  }

  /** One-directional lookup; {@code tag == null} means untagged (all edges). */
  private Set<EObject> lookup(EObject o, String tag) {
    Set<EObject> result = new LinkedHashSet<>();
    for (Object[] e : edges) {
      if (e[0] == o && (tag == null || tag.equals(e[2]))) {
        result.add((EObject) e[1]);
      }
    }
    return result;
  }

  private List<EObject> all() {
    List<EObject> all = new ArrayList<>(partners);
    all.addAll(strangers);
    all.add(self);
    return all;
  }

  @Test
  void untagged_findsCorrespondencesVisibleFromEitherSide_inBothArgumentOrders() {
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper(false));

    for (EObject p : partners) {
      assertTrue(lookup.corresponds(self, p), "self ~ partner");
      assertTrue(lookup.corresponds(p, self), "partner ~ self");
    }
    for (EObject s : strangers) {
      assertFalse(lookup.corresponds(self, s));
      assertFalse(lookup.corresponds(s, self));
    }
  }

  @Test
  void untagged_looksUpEveryDistinctObjectAtMostOnce() {
    MetamodelWrapperInterface wrapper = wrapper(false);
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper);

    for (int round = 0; round < 3; round++) {
      for (EObject o : all()) {
        lookup.corresponds(self, o);
        lookup.corresponds(o, self);
      }
    }

    for (EObject o : all()) {
      verify(wrapper, atMostOnce()).getCorrespondingObjects(o);
    }
  }

  @Test
  void untagged_skipsTheReverseLookupWhenTheForwardOneMatches() {
    MetamodelWrapperInterface wrapper = wrapper(false);
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper);

    assertTrue(lookup.corresponds(self, partners.get(0)));

    verify(wrapper, times(1)).getCorrespondingObjects(self);
    verify(wrapper, never()).getCorrespondingObjects(partners.get(0));
  }

  @Test
  void tagged_withTaggedLookup_isExactAndCachedPerTag() {
    MetamodelWrapperInterface wrapper = wrapper(true);
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper);

    for (int round = 0; round < 3; round++) {
      assertTrue(lookup.corresponds(self, partners.get(0), "elem"));
      assertTrue(lookup.corresponds(partners.get(0), self, "elem"));
      assertTrue(lookup.corresponds(self, partners.get(3), "elem"), "visible from partner only");
      assertFalse(lookup.corresponds(self, partners.get(1), "elem"));
      assertTrue(lookup.corresponds(partners.get(1), self, "late"));
      assertFalse(lookup.corresponds(self, partners.get(2), "late"), "untagged edge");
      assertFalse(lookup.corresponds(self, strangers.get(0), "elem"));
    }

    for (EObject o : all()) {
      verify(wrapper, atMostOnce()).getCorrespondingObjectsWithTag(o, "elem");
      verify(wrapper, atMostOnce()).getCorrespondingObjectsWithTag(o, "late");
    }
    verify(wrapper, never()).correspondenceHasTag(any(), any(), any());
  }

  @Test
  void tagged_withoutTaggedLookup_delegatesToTheWrapperUnchanged() {
    MetamodelWrapperInterface wrapper = wrapper(false);
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper);

    assertTrue(lookup.corresponds(partners.get(0), self, "elem"));
    assertFalse(lookup.corresponds(self, partners.get(0), "late"));

    verify(wrapper, times(2)).correspondenceHasTag(any(), any(), any());
    verify(wrapper, never()).getCorrespondingObjectsWithTag(any(), any());
  }

  private MetamodelWrapperInterface wrapperWithParticipants() {
    MetamodelWrapperInterface wrapper = wrapper(true);
    Set<EObject> participants = new LinkedHashSet<>();
    for (Object[] e : edges) {
      participants.add((EObject) e[0]);
      participants.add((EObject) e[1]);
    }
    when(wrapper.getAllObjectsInACorrespondence()).thenReturn(Optional.of(participants));
    return wrapper;
  }

  @Test
  void participantFilter_stopsLookingUpObjectsOutsideEveryCorrespondence() {
    MetamodelWrapperInterface wrapper = wrapperWithParticipants();
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper, true);
    List<EObject> many = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      many.add(EcoreFactory.eINSTANCE.createEObject());
    }

    for (EObject s : many) {
      assertFalse(lookup.corresponds(self, s));
      assertFalse(lookup.corresponds(s, self, "elem"));
    }
    for (EObject p : partners) {
      assertTrue(lookup.corresponds(self, p), "still exact, also for partner-side-only pairs");
    }

    verify(wrapper, times(1)).getAllObjectsInACorrespondence();
    int strangerLookups = 0;
    for (EObject s : many) {
      strangerLookups +=
          org.mockito.Mockito.mockingDetails(wrapper).getInvocations().stream()
              .filter(inv -> inv.getArguments().length > 0 && inv.getArguments()[0] == s)
              .filter(inv -> inv.getMethod().getName().startsWith("getCorrespondingObjects"))
              .count();
    }
    assertTrue(
        strangerLookups <= CorrespondenceLookup.LOOKUPS_BEFORE_PARTICIPANT_FILTER,
        "lookups of non-participants: " + strangerLookups);
  }

  @Test
  void participantFilter_isNotUsedWhenDisabledOrUnsupported() {
    MetamodelWrapperInterface disabled = wrapperWithParticipants();
    CorrespondenceLookup off = new CorrespondenceLookup(disabled, false);
    for (EObject s : strangers) {
      off.corresponds(self, s);
      off.corresponds(s, self);
    }
    verify(disabled, never()).getAllObjectsInACorrespondence();

    MetamodelWrapperInterface unsupported = wrapper(true); // Optional.empty() by default
    CorrespondenceLookup lookup = new CorrespondenceLookup(unsupported, true);
    for (int round = 0; round < 3; round++) {
      for (EObject p : partners) {
        assertTrue(lookup.corresponds(p, self));
      }
      for (EObject s : strangers) {
        assertFalse(lookup.corresponds(self, s));
      }
    }
  }

  @Test
  void participantFilter_keepsTaggedChecksExact_withAndWithoutTaggedLookup() {
    for (boolean taggedLookup : List.of(true, false)) {
      MetamodelWrapperInterface wrapper = wrapperWithParticipants();
      when(wrapper.hasTaggedCorrespondenceLookup()).thenReturn(taggedLookup);
      CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper, true);
      for (int round = 0; round < 3; round++) { // the filter is active from the second round on
        for (EObject s : strangers) {
          assertFalse(lookup.corresponds(self, s, "elem"));
        }
        assertTrue(lookup.corresponds(self, partners.get(0), "elem"));
        assertTrue(lookup.corresponds(self, partners.get(3), "elem"), "visible from partner only");
        assertFalse(lookup.corresponds(self, partners.get(1), "elem"));
        assertTrue(lookup.corresponds(partners.get(1), self, "late"));
      }
    }
  }

  @Test
  void participantFilter_isDroppedWhenEnumerationFails() {
    MetamodelWrapperInterface wrapper = wrapper(true);
    when(wrapper.getAllObjectsInACorrespondence()).thenThrow(new IllegalStateException("broken"));
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper, true);
    for (int round = 0; round < 3; round++) {
      for (EObject p : partners) {
        assertTrue(lookup.corresponds(p, self));
      }
      for (EObject s : strangers) {
        assertFalse(lookup.corresponds(self, s));
      }
    }
    verify(wrapper, times(1)).getAllObjectsInACorrespondence();
  }

  @Test
  void nullFromTheWrapperCountsAsNoCorrespondents() {
    MetamodelWrapperInterface wrapper = mock(MetamodelWrapperInterface.class);
    when(wrapper.hasTaggedCorrespondenceLookup()).thenReturn(true);
    when(wrapper.getCorrespondingObjects(any(EObject.class))).thenReturn(null);
    when(wrapper.getCorrespondingObjectsWithTag(any(EObject.class), anyString())).thenReturn(null);
    CorrespondenceLookup lookup = new CorrespondenceLookup(wrapper);

    assertFalse(lookup.corresponds(self, partners.get(0)));
    assertFalse(lookup.corresponds(self, partners.get(0), "elem"));
  }
}
