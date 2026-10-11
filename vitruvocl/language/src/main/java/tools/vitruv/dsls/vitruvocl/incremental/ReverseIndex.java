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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

/**
 * Inverse of non-containment references without {@code eOpposite} (level 3b): for an indexed
 * reference {@code r} and an object {@code y}, the objects {@code x} with {@code y ∈ x.r}.
 *
 * <p>Only references that occur on an inverse path of some constraint are indexed. A reference is
 * indexed lazily, by a scan, the first time it is needed; afterwards the index is maintained from
 * the reference events of every transaction ({@link #apply}). The scan covers every object
 * reachable from the roots through containment <em>and</em> through references, so holders that
 * were removed from the model but are still referenced (dangling references) are found as well.
 * Counts are kept per (target, holder) pair because lists may be non-unique.
 *
 * <p>The index may contain holders that are no longer part of the model; inverse navigation
 * therefore yields a superset, which is safe (it can only add instances to re-evaluate).
 */
final class ReverseIndex {

  private final Supplier<List<EObject>> roots;
  private final Map<EReference, Map<EObject, Map<EObject, Integer>>> index = new HashMap<>();

  ReverseIndex(Supplier<List<EObject>> roots) {
    this.roots = roots;
  }

  /** The holders {@code x} with {@code target ∈ x.reference}; builds the index on first use. */
  List<EObject> holders(EReference reference, EObject target) {
    Map<EObject, Map<EObject, Integer>> byTarget = index.get(reference);
    if (byTarget == null) {
      byTarget = build(reference);
      index.put(reference, byTarget);
    }
    Map<EObject, Integer> holders = byTarget.get(target);
    return holders == null ? List.of() : new ArrayList<>(holders.keySet());
  }

  /** Whether {@code reference} is already indexed. */
  boolean isIndexed(EReference reference) {
    return index.containsKey(reference);
  }

  /**
   * Updates the indexed references with the reference events of a transaction that has already
   * been applied to the model. Must be called before the transaction's relevant instances are
   * computed; references indexed for the first time afterwards are built from the post-state.
   */
  void apply(TransactionDelta delta) {
    for (ModelEvent event : delta.events()) {
      if (!(event.feature() instanceof EReference ref) || !index.containsKey(ref)) {
        continue;
      }
      Map<EObject, Map<EObject, Integer>> byTarget = index.get(ref);
      switch (event.kind()) {
        case INSERT_REFERENCE -> add(byTarget, event.newValue(), event.object());
        case REMOVE_REFERENCE -> remove(byTarget, event.oldValue(), event.object());
        case REPLACE_REFERENCE -> {
          remove(byTarget, event.oldValue(), event.object());
          add(byTarget, event.newValue(), event.object());
        }
        default -> index.remove(ref); // unknown change of an indexed reference: rebuild lazily
      }
    }
  }

  private Map<EObject, Map<EObject, Integer>> build(EReference reference) {
    Map<EObject, Map<EObject, Integer>> byTarget = new IdentityHashMap<>();
    for (EObject holder : reachableObjects()) {
      if (!reference.getEContainingClass().isSuperTypeOf(holder.eClass())) {
        continue;
      }
      for (Object target : values(holder, reference)) {
        add(byTarget, target, holder);
      }
    }
    return byTarget;
  }

  /** All objects reachable from the roots via containment and via (non-proxy) references. */
  private List<EObject> reachableObjects() {
    Map<EObject, Boolean> seen = new IdentityHashMap<>();
    List<EObject> result = new ArrayList<>();
    Deque<EObject> work = new ArrayDeque<>(roots.get());
    while (!work.isEmpty()) {
      EObject o = work.pop();
      if (o == null || o.eIsProxy() || seen.put(o, Boolean.TRUE) != null) {
        continue;
      }
      result.add(o);
      work.addAll(o.eContents());
      for (EReference r : o.eClass().getEAllReferences()) {
        if (!r.isContainment() && !r.isContainer()) {
          for (Object v : values(o, r)) {
            if (v instanceof EObject eo) {
              work.push(eo);
            }
          }
        }
      }
    }
    return result;
  }

  private static Collection<?> values(EObject holder, EReference reference) {
    Object value = holder.eGet(reference, false);
    if (value instanceof Collection<?> c) {
      return c;
    }
    return value == null ? List.of() : List.of(value);
  }

  private static void add(Map<EObject, Map<EObject, Integer>> byTarget, Object target, EObject holder) {
    if (target instanceof EObject t) {
      byTarget.computeIfAbsent(t, k -> new IdentityHashMap<>()).merge(holder, 1, Integer::sum);
    }
  }

  private static void remove(Map<EObject, Map<EObject, Integer>> byTarget, Object target, EObject holder) {
    if (!(target instanceof EObject t)) {
      return;
    }
    Map<EObject, Integer> holders = byTarget.get(t);
    if (holders == null) {
      return;
    }
    holders.computeIfPresent(holder, (k, n) -> n > 1 ? n - 1 : null);
    if (holders.isEmpty()) {
      byTarget.remove(t);
    }
  }
}
