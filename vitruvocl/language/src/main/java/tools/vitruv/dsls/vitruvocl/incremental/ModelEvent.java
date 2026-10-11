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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * One structural event derived from a recorded Vitruvius {@code EChange}, in the vocabulary of
 * Cabot &amp; Teniente 2009, §2.1 (InsertET/DeleteET, UpdateAttribute, InsertRT/DeleteRT), extended
 * by the change kinds Vitruvius has and the paper does not (multi-valued attributes, root
 * insertion/removal, single-valued reference replacement).
 *
 * <p>Equality is by <em>identity</em> of the involved objects and values (so two structurally
 * equal but distinct EObjects never collapse); see {@link #sameAs}.
 *
 * @param kind the event kind
 * @param object the affected object: the created/deleted/root object, or the object whose feature
 *     changed
 * @param feature the changed feature, {@code null} for existence and root events
 * @param oldValue the removed/replaced value, {@code null} if none
 * @param newValue the inserted/new value, {@code null} if none
 * @param containment whether the changed feature is a containment reference
 */
public record ModelEvent(
    Kind kind,
    EObject object,
    EStructuralFeature feature,
    Object oldValue,
    Object newValue,
    boolean containment) {

  /** Event kinds. */
  public enum Kind {
    /** {@code CreateEObject} — InsertET. The object usually has no container yet. */
    CREATE,
    /** {@code DeleteEObject} — DeleteET. */
    DELETE,
    /** {@code InsertRootEObject} — InsertET from the extension's point of view. */
    INSERT_ROOT,
    /** {@code RemoveRootEObject} — DeleteET from the extension's point of view. */
    REMOVE_ROOT,
    /** {@code ReplaceSingleValuedEAttribute} (including set/unset) — UpdateAttribute. */
    UPDATE_ATTRIBUTE,
    /** {@code InsertEAttributeValue} — not in Cabot &amp; Teniente; treated like an insertion. */
    INSERT_ATTRIBUTE_VALUE,
    /** {@code RemoveEAttributeValue} — not in Cabot &amp; Teniente; treated like a deletion. */
    REMOVE_ATTRIBUTE_VALUE,
    /** {@code InsertEReference} — InsertRT. */
    INSERT_REFERENCE,
    /** {@code RemoveEReference} — DeleteRT. */
    REMOVE_REFERENCE,
    /** {@code ReplaceSingleValuedEReference} — DeleteRT(old) and InsertRT(new) in one event. */
    REPLACE_REFERENCE,
    /** Any other feature change; treated as touching the feature in every direction. */
    OTHER_FEATURE_CHANGE,
    /** An EChange of a type this classifier does not know; forces full evaluation. */
    UNKNOWN
  }

  /** Whether this event can change which objects are contained in (reachable from) a root. */
  public boolean isStructural() {
    return switch (kind) {
      case CREATE, DELETE, INSERT_ROOT, REMOVE_ROOT, UNKNOWN -> true;
      case INSERT_REFERENCE, REMOVE_REFERENCE, REPLACE_REFERENCE, OTHER_FEATURE_CHANGE ->
          containment;
      default -> false;
    };
  }

  /** Identity-based equality used for deduplicating events within a transaction. */
  public boolean sameAs(ModelEvent other) {
    return kind == other.kind
        && object == other.object
        && feature == other.feature
        && oldValue == other.oldValue
        && newValue == other.newValue
        && containment == other.containment;
  }

  /** Identity-based hash consistent with {@link #sameAs}. */
  public int identityHash() {
    int h = kind.hashCode();
    h = 31 * h + System.identityHashCode(object);
    h = 31 * h + System.identityHashCode(feature);
    h = 31 * h + System.identityHashCode(oldValue);
    h = 31 * h + System.identityHashCode(newValue);
    return 31 * h + Boolean.hashCode(containment);
  }
}
