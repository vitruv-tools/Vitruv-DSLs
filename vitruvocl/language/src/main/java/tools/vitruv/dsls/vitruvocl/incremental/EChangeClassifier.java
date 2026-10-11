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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.CreateEObject;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.change.atomic.feature.FeatureEChange;
import tools.vitruv.change.atomic.feature.list.InsertInListEChange;
import tools.vitruv.change.atomic.feature.list.RemoveFromListEChange;
import tools.vitruv.change.atomic.feature.single.ReplaceSingleValuedFeatureEChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.atomic.root.RemoveRootEObject;

/**
 * Maps a recorded {@code EChange} sequence to a {@link TransactionDelta} (event model, see the
 * table in {@code docs/incremental-implementation-report.md}, §4).
 *
 * <p>Deduplication follows Cabot &amp; Teniente 2006, Prop. 4.2 / Fernández-Álvarez et al. 2016,
 * §5.5, restricted to what is safe for ordered, non-unique lists: identical events are kept once,
 * but an insertion and a later removal of the same link are <em>not</em> cancelled against each
 * other (in a non-unique ordered list the surviving entry may sit at a different index).
 */
public final class EChangeClassifier {

  private EChangeClassifier() {}

  /** Classifies {@code changes} (chronological order) into a delta. */
  public static TransactionDelta classify(List<EChange<EObject>> changes) {
    List<ModelEvent> events = new ArrayList<>();
    Map<Integer, List<ModelEvent>> seen = new HashMap<>();
    boolean unknown = false;
    for (EChange<EObject> change : changes) {
      ModelEvent event = toEvent(change);
      if (event.kind() == ModelEvent.Kind.UNKNOWN) {
        unknown = true;
      }
      List<ModelEvent> bucket = seen.computeIfAbsent(event.identityHash(), k -> new ArrayList<>());
      if (bucket.stream().noneMatch(event::sameAs)) {
        bucket.add(event);
        events.add(event);
      }
    }

    Map<String, Set<EClass>> touched = new HashMap<>();
    Map<EObject, Boolean> inserted = new IdentityHashMap<>();
    Map<EObject, Boolean> removed = new IdentityHashMap<>();
    for (ModelEvent event : events) {
      if (event.feature() != null && event.object() != null) {
        touched
            .computeIfAbsent(event.feature().getName(), k -> new LinkedHashSet<>())
            .add(event.object().eClass());
      }
      if (event.kind() == ModelEvent.Kind.OTHER_FEATURE_CHANGE && event.containment()) {
        unknown = true; // a containment change whose moved objects are not known
      }
      if (event.isStructural()) {
        collectStructuralObjects(event, inserted, removed);
      }
    }
    return new TransactionDelta(
        changes, events, touched, withContents(inserted), withContents(removed), unknown);
  }

  /** The given objects and all their (post-state) transitive contents, each once (identity). */
  private static List<EObject> withContents(Map<EObject, Boolean> objects) {
    Map<EObject, Boolean> result = new IdentityHashMap<>();
    List<EObject> ordered = new ArrayList<>();
    for (EObject root : objects.keySet()) {
      if (result.put(root, Boolean.TRUE) == null) {
        ordered.add(root);
      }
      Iterator<EObject> contents = root.eAllContents();
      while (contents.hasNext()) {
        EObject content = contents.next();
        if (result.put(content, Boolean.TRUE) == null) {
          ordered.add(content);
        }
      }
    }
    return ordered;
  }

  private static void collectStructuralObjects(
      ModelEvent event, Map<EObject, Boolean> inserted, Map<EObject, Boolean> removed) {
    switch (event.kind()) {
      case CREATE, INSERT_ROOT -> addIfObject(event.object(), inserted);
      case DELETE, REMOVE_ROOT -> addIfObject(event.object(), removed);
      default -> {
        addIfObject(event.oldValue(), removed);
        addIfObject(event.newValue(), inserted);
      }
    }
  }

  private static void addIfObject(Object value, Map<EObject, Boolean> sink) {
    if (value instanceof EObject eo) {
      sink.put(eo, Boolean.TRUE);
    }
  }

  /** Maps one atomic change to an event; unknown change types map to {@link ModelEvent.Kind#UNKNOWN}. */
  static ModelEvent toEvent(EChange<EObject> change) {
    if (change instanceof CreateEObject<EObject> create) {
      return existence(ModelEvent.Kind.CREATE, create.getAffectedElement());
    }
    if (change instanceof DeleteEObject<EObject> delete) {
      return existence(ModelEvent.Kind.DELETE, delete.getAffectedElement());
    }
    if (change instanceof InsertRootEObject<EObject> insertRoot) {
      return existence(ModelEvent.Kind.INSERT_ROOT, insertRoot.getNewValue());
    }
    if (change instanceof RemoveRootEObject<EObject> removeRoot) {
      return existence(ModelEvent.Kind.REMOVE_ROOT, removeRoot.getOldValue());
    }
    if (change instanceof FeatureEChange<?, ?> featureChange
        && featureChange.getAffectedElement() instanceof EObject affected) {
      return featureEvent(change, affected, featureChange.getAffectedFeature());
    }
    return new ModelEvent(ModelEvent.Kind.UNKNOWN, null, null, null, null, false);
  }

  private static ModelEvent existence(ModelEvent.Kind kind, EObject object) {
    return new ModelEvent(kind, object, null, null, null, false);
  }

  private static ModelEvent featureEvent(
      EChange<EObject> change, EObject affected, EStructuralFeature feature) {
    boolean containment = feature instanceof EReference ref && ref.isContainment();
    if (feature instanceof EAttribute) {
      if (change instanceof InsertInListEChange<?, ?, ?> insert) {
        return new ModelEvent(
            ModelEvent.Kind.INSERT_ATTRIBUTE_VALUE, affected, feature, null, insert.getNewValue(), false);
      }
      if (change instanceof RemoveFromListEChange<?, ?, ?> remove) {
        return new ModelEvent(
            ModelEvent.Kind.REMOVE_ATTRIBUTE_VALUE, affected, feature, remove.getOldValue(), null, false);
      }
      if (change instanceof ReplaceSingleValuedFeatureEChange<?, ?, ?> replace) {
        return new ModelEvent(
            ModelEvent.Kind.UPDATE_ATTRIBUTE,
            affected,
            feature,
            replace.getOldValue(),
            replace.getNewValue(),
            false);
      }
      return new ModelEvent(ModelEvent.Kind.OTHER_FEATURE_CHANGE, affected, feature, null, null, false);
    }
    if (change instanceof InsertInListEChange<?, ?, ?> insert) {
      return new ModelEvent(
          ModelEvent.Kind.INSERT_REFERENCE, affected, feature, null, insert.getNewValue(), containment);
    }
    if (change instanceof RemoveFromListEChange<?, ?, ?> remove) {
      return new ModelEvent(
          ModelEvent.Kind.REMOVE_REFERENCE, affected, feature, remove.getOldValue(), null, containment);
    }
    if (change instanceof ReplaceSingleValuedFeatureEChange<?, ?, ?> replace) {
      return new ModelEvent(
          ModelEvent.Kind.REPLACE_REFERENCE,
          affected,
          feature,
          replace.getOldValue(),
          replace.getNewValue(),
          containment);
    }
    // Unknown reference change: conservatively structural if the reference is a containment.
    return new ModelEvent(
        ModelEvent.Kind.OTHER_FEATURE_CHANGE, affected, feature, null, null, containment);
  }
}
