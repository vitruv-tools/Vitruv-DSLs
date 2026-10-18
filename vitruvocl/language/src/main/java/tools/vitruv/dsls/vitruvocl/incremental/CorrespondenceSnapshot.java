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

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.vitruvocl.incremental.CorrespondenceDelta.Pair;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * The correspondence relation at one point in time (step B1 of {@code
 * docs/correspondence-incremental-plan.md}): for every object in some correspondence, its
 * single-object lookup result per tag, exactly as the evaluator's {@code ~} sees it.
 *
 * <p>Capturing costs one lookup per object in a correspondence. With Vitruvius each lookup scans
 * the whole correspondence model, so a capture is quadratic in its size; see {@code
 * CorrespondenceSnapshotBenchmark}. In return the snapshot answers every lookup of the same state
 * by hash lookups (step C4, {@link SnapshotCorrespondenceWrapper}). It is immutable after capture
 * and may be read from several threads.
 */
final class CorrespondenceSnapshot {

  /** Object → tag (may be {@code null}) → objects its lookup returns. Lookups are directional. */
  private final Map<EObject, Map<String, Set<EObject>>> lookups;

  /** Object → union of its lookups over all tags (the untagged lookup). */
  private final Map<EObject, Set<EObject>> untagged;

  /** Every object on some side of some correspondence, as the wrapper enumerated it. */
  private final Set<EObject> participants;

  private CorrespondenceSnapshot(
      Map<EObject, Map<String, Set<EObject>>> lookups, Set<EObject> participants) {
    this.lookups = lookups;
    this.participants = Collections.unmodifiableSet(participants);
    this.untagged = new IdentityHashMap<>();
    lookups.forEach(
        (o, byTag) -> {
          Set<EObject> all = identitySet(Set.of());
          byTag.values().forEach(all::addAll);
          untagged.put(o, Collections.unmodifiableSet(all));
        });
  }

  /**
   * Captures the current relation, or returns empty if {@code wrapper} cannot enumerate the objects
   * in a correspondence.
   */
  static Optional<CorrespondenceSnapshot> capture(MetamodelWrapperInterface wrapper) {
    Optional<Set<EObject>> objects = wrapper.getAllObjectsInACorrespondence();
    if (objects.isEmpty()) {
      return Optional.empty();
    }
    Map<EObject, Map<String, Set<EObject>>> lookups = new IdentityHashMap<>();
    for (EObject o : objects.get()) {
      Map<String, Set<EObject>> byTag = wrapper.getCorrespondingObjectsByTag(o);
      if (byTag != null && !byTag.isEmpty()) {
        Map<String, Set<EObject>> copy = new HashMap<>();
        byTag.forEach(
            (tag, partners) -> copy.put(tag, Collections.unmodifiableSet(identitySet(partners))));
        lookups.put(o, copy);
      }
    }
    return Optional.of(new CorrespondenceSnapshot(lookups, identitySet(objects.get())));
  }

  /** What {@code getCorrespondingObjects(o)} returned at capture time. */
  Set<EObject> correspondents(EObject o) {
    return untagged.getOrDefault(o, Collections.emptySet());
  }

  /**
   * What {@code getCorrespondingObjectsWithTag(o, tag)} returned at capture time; as in Vitruvius,
   * a {@code null} tag means any tag.
   */
  Set<EObject> correspondents(EObject o, String tag) {
    if (tag == null) {
      return correspondents(o);
    }
    Map<String, Set<EObject>> byTag = lookups.get(o);
    return byTag == null ? Collections.emptySet() : byTag.getOrDefault(tag, Collections.emptySet());
  }

  /** What {@code getCorrespondingObjectsByTag(o)} returned at capture time. */
  Map<String, Set<EObject>> correspondentsByTag(EObject o) {
    Map<String, Set<EObject>> byTag = lookups.get(o);
    return byTag == null ? Collections.emptyMap() : Collections.unmodifiableMap(byTag);
  }

  /** Every object that took part in some correspondence at capture time. */
  Set<EObject> participants() {
    return participants;
  }

  /** The change from this (earlier) snapshot to {@code later}. */
  CorrespondenceDelta diff(CorrespondenceSnapshot later) {
    Set<Pair> candidates = new LinkedHashSet<>();
    Set<EObject> objects = identitySet(lookups.keySet());
    objects.addAll(later.lookups.keySet());
    for (EObject o : objects) {
      Map<String, Set<EObject>> before = lookups.getOrDefault(o, Collections.emptyMap());
      Map<String, Set<EObject>> after = later.lookups.getOrDefault(o, Collections.emptyMap());
      Set<String> tags = new LinkedHashSet<>(before.keySet());
      tags.addAll(after.keySet());
      for (String tag : tags) {
        Set<EObject> was = before.getOrDefault(tag, Collections.emptySet());
        Set<EObject> is = after.getOrDefault(tag, Collections.emptySet());
        for (EObject partner : was) {
          if (!is.contains(partner)) {
            candidates.add(new Pair(o, partner, tag));
          }
        }
        for (EObject partner : is) {
          if (!was.contains(partner)) {
            candidates.add(new Pair(o, partner, tag));
          }
        }
      }
    }
    Set<Pair> added = new LinkedHashSet<>();
    Set<Pair> removed = new LinkedHashSet<>();
    for (Pair p : candidates) {
      boolean held = relates(p);
      boolean holds = later.relates(p);
      if (holds && !held) {
        added.add(p);
      } else if (held && !holds) {
        removed.add(p);
      }
    }
    return CorrespondenceDelta.of(added, removed);
  }

  /** The evaluator's two-way relation: {@code b ∈ corr(a, t) ∨ a ∈ corr(b, t)}. */
  private boolean relates(Pair p) {
    return sees(p.first(), p.second(), p.tag()) || sees(p.second(), p.first(), p.tag());
  }

  private boolean sees(EObject from, EObject to, String tag) {
    Map<String, Set<EObject>> byTag = lookups.get(from);
    return byTag != null && byTag.getOrDefault(tag, Collections.emptySet()).contains(to);
  }

  /** Number of objects with at least one correspondent (for statistics and benchmarks). */
  int size() {
    return lookups.size();
  }

  private static Set<EObject> identitySet(Set<EObject> objects) {
    Set<EObject> result = Collections.newSetFromMap(new IdentityHashMap<>());
    if (objects != null) {
      result.addAll(objects);
    }
    return result;
  }
}
