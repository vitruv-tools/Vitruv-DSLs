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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * Memoizing correspondence check for the {@code ~} operator in all its forms ({@code a ~ b} and
 * {@code select/reject/exists(~ [, Type=…] [, Tag=…])}).
 *
 * <p>A Vitruvius correspondence lookup scans the whole correspondence model, and the evaluator
 * checks a correspondence for every (context instance, candidate) pair. This class looks up every
 * distinct object (per tag) at most once and answers each further check with hash lookups.
 *
 * <p>The answers equal the uncached check {@code b ∈ corr(a) ∨ a ∈ corr(b)}. Both directions are
 * needed: Vitruvius matches a single queried object only against a side consisting of exactly that
 * object, so a correspondence {@code [a, x] ~ [b]} is found from {@code b} but not from {@code a}
 * ({@code CorrespondenceLookupSemanticsExperimentTest}). Tagged checks use memoized tagged lookups
 * if the wrapper declares them equivalent to {@code correspondenceHasTag} ({@link
 * MetamodelWrapperInterface#hasTaggedCorrespondenceLookup()}); otherwise they go to the wrapper
 * unchanged. A {@code null} lookup result counts as no correspondents.
 *
 * <p>With the participant filter (step A3), once a few lookups have been made the lookup also
 * fetches the set of all objects that take part in some correspondence (one scan, if the wrapper
 * can enumerate it); an object outside that set has no correspondent, so its checks need no lookup.
 *
 * <p>One instance belongs to one {@link EvaluationVisitor} and is used by one thread. It assumes
 * the correspondence model does not change while the visitor runs, which holds because evaluation
 * happens between change propagations.
 */
final class CorrespondenceLookup {

  /** Lookups after which the participant set is fetched; it costs about one lookup itself. */
  static final int LOOKUPS_BEFORE_PARTICIPANT_FILTER = 4;

  private final MetamodelWrapperInterface wrapper;
  private final boolean participantFilter;
  private int lookups;
  private boolean participantsFetched;

  /** Every object in some correspondence, or {@code null} while unknown or unsupported. */
  private Set<EObject> participants;

  /** Untagged correspondents per object. */
  private final Map<EObject, Set<EObject>> untagged = new HashMap<>();

  /** Tagged correspondents per tag and object (only with a tagged wrapper lookup). */
  private final Map<String, Map<EObject, Set<EObject>>> tagged = new HashMap<>();

  /** Lazily read from the wrapper on first tagged check. */
  private Boolean taggedLookup;

  CorrespondenceLookup(MetamodelWrapperInterface wrapper) {
    this(wrapper, true);
  }

  CorrespondenceLookup(MetamodelWrapperInterface wrapper, boolean participantFilter) {
    this.wrapper = wrapper;
    this.participantFilter = participantFilter;
  }

  /** Whether {@code a} and {@code b} are related by any correspondence, in either direction. */
  boolean corresponds(EObject a, EObject b) {
    if (outsideEveryCorrespondence(a, b)) {
      return false;
    }
    return twoWay(untagged, a, b, this::lookUp);
  }

  /** Whether {@code a} and {@code b} are related by a correspondence carrying {@code tag}. */
  boolean corresponds(EObject a, EObject b, String tag) {
    if (taggedLookup == null) {
      taggedLookup = wrapper.hasTaggedCorrespondenceLookup();
    }
    if (outsideEveryCorrespondence(a, b)) {
      return false;
    }
    if (!taggedLookup) {
      return wrapper.correspondenceHasTag(a, b, tag);
    }
    Map<EObject, Set<EObject>> cache = tagged.computeIfAbsent(tag, k -> new HashMap<>());
    return twoWay(cache, a, b, o -> lookUpTagged(o, tag));
  }

  private Set<EObject> lookUp(EObject o) {
    lookups++;
    return wrapper.getCorrespondingObjects(o);
  }

  private Set<EObject> lookUpTagged(EObject o, String tag) {
    lookups++;
    return wrapper.getCorrespondingObjectsWithTag(o, tag);
  }

  /**
   * Whether {@code a} or {@code b} is known to take part in no correspondence at all; then they
   * cannot correspond, whatever the tag. Fetches the participant set lazily (see class comment),
   * at most once: if the wrapper cannot enumerate or the enumeration fails, it is not retried.
   */
  private boolean outsideEveryCorrespondence(EObject a, EObject b) {
    if (participantFilter && !participantsFetched && lookups >= LOOKUPS_BEFORE_PARTICIPANT_FILTER) {
      participantsFetched = true;
      try {
        // used as returned: EMF objects compare by identity, and copying would cost O(participants)
        // per evaluation (the session's snapshot hands out the same set every time)
        participants = wrapper.getAllObjectsInACorrespondence().orElse(null);
      } catch (RuntimeException e) {
        participants = null; // enumeration failed: keep looking every object up
      }
    }
    return participants != null && !(participants.contains(a) && participants.contains(b));
  }

  private static boolean twoWay(
      Map<EObject, Set<EObject>> cache,
      EObject a,
      EObject b,
      Function<EObject, Set<EObject>> lookup) {
    return correspondents(cache, a, lookup).contains(b)
        || correspondents(cache, b, lookup).contains(a);
  }

  private static Set<EObject> correspondents(
      Map<EObject, Set<EObject>> cache, EObject o, Function<EObject, Set<EObject>> lookup) {
    return cache.computeIfAbsent(
        o,
        k -> {
          Set<EObject> found = lookup.apply(k);
          return found == null ? Set.of() : found;
        });
  }
}
