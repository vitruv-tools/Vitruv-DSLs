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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;

/**
 * The classified, deduplicated content of one transaction (one {@code finishedChangePropagation}
 * callback, or one explicitly supplied change list), in the shape the per-constraint filters need.
 *
 * <p>Built by {@link EChangeClassifier}; immutable afterwards. All summaries are derived from the
 * same deduplicated {@link #events()} list (Cabot &amp; Teniente 2006, Prop. 4.2: the relevant
 * instances of a set of events are the union over the events, so duplicates add nothing).
 */
public final class TransactionDelta {

  private final List<EChange<EObject>> changes;
  private final List<ModelEvent> events;
  private final Map<String, Set<EClass>> touchedFeatures;
  private final Set<EClass> structurallyAffectedClasses;
  private final Set<EClass> structurallyInsertedClasses;
  private final Set<EClass> structurallyRemovedClasses;
  private final List<EObject> structurallyInsertedObjects;
  private final List<EObject> structurallyRemovedObjects;
  private final boolean hasUnknownChanges;
  private final CorrespondenceDelta correspondenceDelta;

  TransactionDelta(
      List<EChange<EObject>> changes,
      List<ModelEvent> events,
      Map<String, Set<EClass>> touchedFeatures,
      List<EObject> structurallyInsertedObjects,
      List<EObject> structurallyRemovedObjects,
      boolean hasUnknownChanges) {
    this(
        changes,
        events,
        touchedFeatures,
        structurallyInsertedObjects,
        structurallyRemovedObjects,
        hasUnknownChanges,
        CorrespondenceDelta.unknown());
  }

  private TransactionDelta(
      List<EChange<EObject>> changes,
      List<ModelEvent> events,
      Map<String, Set<EClass>> touchedFeatures,
      List<EObject> structurallyInsertedObjects,
      List<EObject> structurallyRemovedObjects,
      boolean hasUnknownChanges,
      CorrespondenceDelta correspondenceDelta) {
    this.changes = List.copyOf(changes);
    this.events = List.copyOf(events);
    Map<String, Set<EClass>> copy = new HashMap<>();
    touchedFeatures.forEach((k, v) -> copy.put(k, Collections.unmodifiableSet(new LinkedHashSet<>(v))));
    this.touchedFeatures = Collections.unmodifiableMap(copy);
    this.structurallyInsertedObjects = List.copyOf(structurallyInsertedObjects);
    this.structurallyRemovedObjects = List.copyOf(structurallyRemovedObjects);
    this.structurallyInsertedClasses = Collections.unmodifiableSet(classesOf(structurallyInsertedObjects));
    this.structurallyRemovedClasses = Collections.unmodifiableSet(classesOf(structurallyRemovedObjects));
    Set<EClass> all = new LinkedHashSet<>(structurallyInsertedClasses);
    all.addAll(structurallyRemovedClasses);
    this.structurallyAffectedClasses = Collections.unmodifiableSet(all);
    this.hasUnknownChanges = hasUnknownChanges;
    this.correspondenceDelta = correspondenceDelta;
  }

  /** A copy of this delta that carries the observed change of the correspondence relation. */
  TransactionDelta withCorrespondenceDelta(CorrespondenceDelta delta) {
    return new TransactionDelta(
        changes,
        events,
        touchedFeatures,
        structurallyInsertedObjects,
        structurallyRemovedObjects,
        hasUnknownChanges,
        delta);
  }

  /**
   * How the correspondence relation changed; {@link CorrespondenceDelta#unknown()} unless the
   * session observed it (correspondence edits never appear in {@link #changes()}).
   */
  public CorrespondenceDelta correspondenceDelta() {
    return correspondenceDelta;
  }

  /** The raw changes in chronological order, as passed to {@code @pre}/lifecycle evaluation. */
  public List<EChange<EObject>> changes() {
    return changes;
  }

  /** The deduplicated events in first-occurrence order. */
  public List<ModelEvent> events() {
    return events;
  }

  /** Whether the transaction contained no model change (correspondence changes are separate). */
  public boolean isEmpty() {
    return changes.isEmpty();
  }

  /**
   * Feature name → runtime classes of the objects whose feature of that name changed. Keyed by
   * name because the evaluator resolves features by name on each object's runtime class.
   */
  public Map<String, Set<EClass>> touchedFeatures() {
    return touchedFeatures;
  }

  /**
   * Runtime classes of every object whose membership in (or position within) some type extension
   * may have changed: the objects of all existence, root and containment events together with all
   * of their (post-state) transitive contents.
   */
  public Set<EClass> structurallyAffectedClasses() {
    return structurallyAffectedClasses;
  }

  /**
   * Runtime classes of the objects (and their post-state contents) that were created, inserted as
   * root or inserted into a containment reference — InsertET in the sense of Cabot &amp; Teniente.
   */
  public Set<EClass> structurallyInsertedClasses() {
    return structurallyInsertedClasses;
  }

  /**
   * Runtime classes of the objects (and their post-state contents) that were deleted, removed as
   * root or removed from a containment reference — DeleteET. A move is both an insertion and a
   * removal.
   */
  public Set<EClass> structurallyRemovedClasses() {
    return structurallyRemovedClasses;
  }

  /**
   * The objects (each once, with their post-state transitive contents) that were structurally
   * inserted; see {@link #structurallyInsertedClasses()}.
   */
  public List<EObject> structurallyInsertedObjects() {
    return structurallyInsertedObjects;
  }

  /**
   * The objects (each once, with their post-state transitive contents) that were structurally
   * removed; see {@link #structurallyRemovedClasses()}.
   */
  public List<EObject> structurallyRemovedObjects() {
    return structurallyRemovedObjects;
  }

  private static Set<EClass> classesOf(List<EObject> objects) {
    Set<EClass> classes = new LinkedHashSet<>();
    for (EObject o : objects) {
      classes.add(o.eClass());
    }
    return classes;
  }

  /** Whether an object of a class conforming to {@code type} was structurally inserted. */
  public boolean insertsInstanceOf(EClass type) {
    return structurallyInsertedClasses.stream().anyMatch(c -> conforms(c, type));
  }

  /** Whether an object of a class conforming to {@code type} was structurally removed. */
  public boolean removesInstanceOf(EClass type) {
    return structurallyRemovedClasses.stream().anyMatch(c -> conforms(c, type));
  }

  /** Whether some change could not be classified; such a transaction re-evaluates everything. */
  public boolean hasUnknownChanges() {
    return hasUnknownChanges;
  }

  /** Whether some object of a class conforming to one of {@code staticTypes} touched {@code feature}. */
  public boolean touchesFeature(String featureName, Set<EClass> staticTypes) {
    Set<EClass> runtimeClasses = touchedFeatures.get(featureName);
    if (runtimeClasses == null) {
      return false;
    }
    for (EClass runtime : runtimeClasses) {
      for (EClass staticType : staticTypes) {
        if (conforms(runtime, staticType)) {
          return true;
        }
      }
    }
    return false;
  }

  /** Whether an object of a class conforming to {@code type} was structurally affected. */
  public boolean affectsExtensionOf(EClass type) {
    for (EClass runtime : structurallyAffectedClasses) {
      if (conforms(runtime, type)) {
        return true;
      }
    }
    return false;
  }

  /** {@code runtime} conforms to {@code type}: equal, a subclass, or {@code type} is EObject. */
  public static boolean conforms(EClass runtime, EClass type) {
    return type == runtime
        || type.isSuperTypeOf(runtime)
        || "EObject".equals(type.getName())
            && "http://www.eclipse.org/emf/2002/Ecore".equals(type.getEPackage().getNsURI());
  }
}
