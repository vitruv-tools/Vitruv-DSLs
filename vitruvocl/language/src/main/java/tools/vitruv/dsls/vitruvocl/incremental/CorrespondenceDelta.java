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
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;

/**
 * How the correspondence relation changed in one transaction, as observed by diffing two {@link
 * CorrespondenceSnapshot}s (correspondence edits never appear in the EChange stream, see {@code
 * CorrespondenceVisibilityExperimentTest}).
 *
 * <p>The relation is the one the evaluator's {@code ~} uses: {@code a} and {@code b} correspond
 * with tag {@code t} iff {@code b ∈ corr(a, t) ∨ a ∈ corr(b, t)}. A pair is reported as added
 * (removed) iff that relation holds after (before) the transaction but not before (after). A tag
 * change is a removal plus an addition.
 *
 * <p>An {@linkplain #unknown() unknown} delta means the wrapper cannot enumerate its
 * correspondences; consumers must then assume that any correspondence may have changed.
 */
public final class CorrespondenceDelta {

  /** An unordered, identity-based pair of corresponding objects with the correspondence's tag. */
  public static final class Pair {
    private final EObject first;
    private final EObject second;
    private final String tag;

    public Pair(EObject first, EObject second, String tag) {
      this.first = first;
      this.second = second;
      this.tag = tag;
    }

    public EObject first() {
      return first;
    }

    public EObject second() {
      return second;
    }

    /** The tag, or {@code null} for an untagged correspondence. */
    public String tag() {
      return tag;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Pair p
          && Objects.equals(tag, p.tag)
          && (first == p.first && second == p.second || first == p.second && second == p.first);
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(first) + System.identityHashCode(second) + 31 * Objects.hashCode(tag);
    }

    @Override
    public String toString() {
      return first + " ~ " + second + (tag == null ? "" : " [" + tag + "]");
    }
  }

  private static final CorrespondenceDelta UNKNOWN = new CorrespondenceDelta(false, Set.of(), Set.of());

  private final boolean known;
  private final Set<Pair> added;
  private final Set<Pair> removed;

  private CorrespondenceDelta(boolean known, Set<Pair> added, Set<Pair> removed) {
    this.known = known;
    this.added = Collections.unmodifiableSet(new LinkedHashSet<>(added));
    this.removed = Collections.unmodifiableSet(new LinkedHashSet<>(removed));
  }

  /** A delta whose content is not known: every correspondence may have changed. */
  public static CorrespondenceDelta unknown() {
    return UNKNOWN;
  }

  static CorrespondenceDelta of(Set<Pair> added, Set<Pair> removed) {
    return new CorrespondenceDelta(true, added, removed);
  }

  /** Whether the content of this delta was observed (as opposed to {@link #unknown()}). */
  public boolean isKnown() {
    return known;
  }

  /** Whether the relation is known to be unchanged. */
  public boolean isKnownEmpty() {
    return known && added.isEmpty() && removed.isEmpty();
  }

  /** Whether the relation is known to have changed. */
  public boolean hasChanges() {
    return known && !(added.isEmpty() && removed.isEmpty());
  }

  /** Pairs that correspond after the transaction but not before; empty if unknown. */
  public Set<Pair> added() {
    return added;
  }

  /** Pairs that corresponded before the transaction but no longer do; empty if unknown. */
  public Set<Pair> removed() {
    return removed;
  }

  /** Every object of an added or removed pair. */
  public Set<EObject> affectedObjects() {
    Set<EObject> result = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Set<Pair> pairs : List.of(added, removed)) {
      for (Pair p : pairs) {
        result.add(p.first());
        result.add(p.second());
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return known ? "CorrespondenceDelta[added=" + added + ", removed=" + removed + "]" : "CorrespondenceDelta[unknown]";
  }
}
