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

package tools.vitruv.dsls.vitruvocl.evaluator.transaction;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.CreateEObject;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.change.atomic.feature.FeatureEChange;
import tools.vitruv.change.atomic.feature.list.InsertInListEChange;
import tools.vitruv.change.atomic.feature.list.RemoveFromListEChange;
import tools.vitruv.change.atomic.feature.list.UpdateSingleListEntryEChange;
import tools.vitruv.change.atomic.feature.single.ReplaceSingleValuedFeatureEChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.atomic.root.RemoveRootEObject;

/**
 * Read-only, side-effect-free view over a "transaction" — the ordered list of atomic {@link
 * EChange}s between a pre-state (transaction start) and the current post-state — used to answer
 * {@code @pre}, {@code OCLisNew}, {@code OCLisModified}, and {@code OCLisDeleted} queries during
 * evaluation.
 *
 * <p>An empty transaction (the default for every existing, pre/post-unaware evaluation call) is a
 * safe no-op: every pre-state query returns the current value unchanged, and every lifecycle
 * predicate is {@code false}.
 *
 * <p>Identity ({@code ==}), not {@link Object#equals}, is used throughout to match a specific
 * {@link EObject} instance, consistent with how the rest of the evaluator already treats EObjects
 * (see {@code EvaluationVisitor}'s receiver handling).
 */
public final class TransactionModel {

  private final List<EChange<EObject>> changes;

  public TransactionModel(List<EChange<EObject>> changes) {
    this.changes = changes == null ? List.of() : changes;
  }

  public boolean isEmpty() {
    return changes.isEmpty();
  }

  /** Returns {@code true} if {@code obj} was created (as a new object, or inserted as a resource root) during this transaction. */
  public boolean wasCreated(EObject obj) {
    for (EChange<EObject> c : changes) {
      if (c instanceof CreateEObject<?> ce && (Object) ce.getAffectedElement() == obj) {
        return true;
      }
      if (c instanceof InsertRootEObject<?> ie && (Object) ie.getNewValue() == obj) {
        return true;
      }
    }
    return false;
  }

  /** Returns {@code true} if {@code obj} was deleted (or removed as a resource root) during this transaction. */
  public boolean wasDeleted(EObject obj) {
    for (EChange<EObject> c : changes) {
      if (c instanceof DeleteEObject<?> de && (Object) de.getAffectedElement() == obj) {
        return true;
      }
      if (c instanceof RemoveRootEObject<?> re && (Object) re.getOldValue() == obj) {
        return true;
      }
    }
    return false;
  }

  /** Returns {@code true} if any feature (attribute or reference, single- or multi-valued) of {@code obj} changed during this transaction. */
  public boolean wasModified(EObject obj) {
    for (EChange<EObject> c : changes) {
      if (c instanceof FeatureEChange<?, ?> fc && (Object) fc.getAffectedElement() == obj) {
        return true;
      }
    }
    return false;
  }

  /**
   * Reconstructs the pre-state value of a single-valued {@code feature} on {@code obj}.
   *
   * <p>If multiple replaces of the same feature occurred within this transaction, returns the old
   * value of the <em>first</em> one (the changes list is chronologically ordered, so its old value
   * is the value that existed before the whole transaction, not merely before the last replace).
   *
   * @param currentValue the feature's current (post-state) value, returned unchanged if no
   *     matching change exists in this transaction
   */
  public Object getPreStateSingleValue(EObject obj, EStructuralFeature feature, Object currentValue) {
    for (EChange<EObject> c : changes) {
      if (c instanceof ReplaceSingleValuedFeatureEChange<?, ?, ?> rc
          && (Object) rc.getAffectedElement() == obj
          && rc.getAffectedFeature() == feature) {
        return rc.getOldValue();
      }
    }
    return currentValue;
  }

  /**
   * Reconstructs the pre-state value list of a multi-valued {@code feature} on {@code obj}, by
   * starting from the current (post-state) list and replaying this transaction's matching
   * insert/remove changes in <em>reverse</em> chronological order, each inverted (undo an insert
   * by removing that value; undo a remove by re-inserting the removed value at its recorded
   * index). Reverse order is required: forward replay transforms pre-state into post-state one
   * change at a time, so undoing must walk backward from the last change to the first.
   *
   * @param currentValues the feature's current (post-state) values, returned unchanged (as a new
   *     list) if no matching change exists in this transaction
   */
  public List<Object> getPreStateMultiValue(
      EObject obj, EStructuralFeature feature, List<Object> currentValues) {
    List<Object> result = new ArrayList<>(currentValues);
    for (int i = changes.size() - 1; i >= 0; i--) {
      EChange<EObject> c = changes.get(i);
      if (c instanceof InsertInListEChange<?, ?, ?> ic
          && (Object) ic.getAffectedElement() == obj
          && ic.getAffectedFeature() == feature) {
        int idx = ((UpdateSingleListEntryEChange<?, ?>) ic).getIndex();
        int removeAt = Math.max(0, Math.min(idx, result.size() - 1));
        if (!result.isEmpty()) {
          result.remove(removeAt);
        }
      } else if (c instanceof RemoveFromListEChange<?, ?, ?> rc
          && (Object) rc.getAffectedElement() == obj
          && rc.getAffectedFeature() == feature) {
        int idx = ((UpdateSingleListEntryEChange<?, ?>) rc).getIndex();
        int insertAt = Math.max(0, Math.min(idx, result.size()));
        result.add(insertAt, rc.getOldValue());
      }
    }
    return result;
  }
}
