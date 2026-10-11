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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.uuid.Uuid;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;

/**
 * Transaction hook (building block B): turns one {@code finishedChangePropagation} callback — one
 * {@code commitChanges()} — into one transaction, i.e. the chronological EChange list built from,
 * per {@link PropagatedChange} in callback order, its original change followed by its consequential
 * changes.
 */
public final class TransactionCollector implements ChangePropagationListener {

  private final Function<List<EChange<EObject>>, IncrementalResult> processor;
  private final Consumer<IncrementalResult> onResult;
  private final Consumer<RuntimeException> onFailure;

  TransactionCollector(
      Function<List<EChange<EObject>>, IncrementalResult> processor,
      Consumer<IncrementalResult> onResult,
      Consumer<RuntimeException> onFailure) {
    this.processor = processor;
    this.onResult = onResult;
    this.onFailure = onFailure;
  }

  /** Flattens the callback's propagated changes into one chronological list. */
  public static List<EChange<EObject>> flatten(Iterable<PropagatedChange> propagatedChanges) {
    List<EChange<EObject>> changes = new ArrayList<>();
    for (PropagatedChange propagated : propagatedChanges) {
      changes.addAll(propagated.getOriginalChange().getEChanges());
      changes.addAll(propagated.getConsequentialChanges().getEChanges());
    }
    return changes;
  }

  @Override
  public void startedChangePropagation(VitruviusChange<Uuid> changeToPropagate) {
    // The transaction is only evaluated once propagation has finished (post-state).
  }

  @Override
  public void finishedChangePropagation(Iterable<PropagatedChange> propagatedChanges) {
    IncrementalResult result;
    try {
      result = processor.apply(flatten(propagatedChanges));
    } catch (RuntimeException e) {
      if (onFailure != null) {
        onFailure.accept(e);
      }
      return;
    }
    if (onResult != null) {
      onResult.accept(result);
    }
  }
}
