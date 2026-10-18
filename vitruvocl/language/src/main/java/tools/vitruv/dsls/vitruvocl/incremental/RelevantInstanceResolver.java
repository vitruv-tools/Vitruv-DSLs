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
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.Origin;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.PseSet;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.RelevancePlan;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;

/**
 * Level 3: the context instances of a constraint that a transaction can affect (Cabot &amp;
 * Teniente 2009, §3.2; union over all events, Cabot &amp; Teniente 2006, Prop. 4.2).
 *
 * <p>For a change of feature {@code f} on object {@code o}, every read of {@code f} whose receiver
 * class matches {@code o} contributes {@code Inverse(origin)(o)}: the origin's reference path is
 * walked backwards from {@code o} in the post-state — containment via {@code eContainer()},
 * container features via their containment opposite, references with {@code eOpposite} via the
 * opposite, all others via the {@link ReverseIndex}; correspondents (idiom rule) via the
 * correspondence model. A read with an untraceable origin turns the event into a class event (the
 * whole extension is re-evaluated). Inserted objects of a context type are relevant themselves;
 * removed ones are dropped from the violation set. Structural changes of {@code allInstances()}
 * types and container-feature owners are class events.
 *
 * <p>Post-state inversion is sufficient: an instance whose result changed has, on its pre- or
 * post-state path, a first changed link or value; the unchanged prefix before it is the same in
 * both states, so inverting it in the post-state from the changed object reaches the instance.
 * [Argument; checked by the oracle on the tested transactions, not proven.]
 */
final class RelevantInstanceResolver {

  /**
   * Outcome for one constraint and transaction.
   *
   * @param classEvent whether the whole extension must be re-evaluated
   * @param relevant context instances to re-evaluate (identity set), if not a class event
   * @param removed context instances that left the model; their entries are dropped
   */
  record Relevance(boolean classEvent, Set<EObject> relevant, Set<EObject> removed) {
    static Relevance fullExtension() {
      return new Relevance(true, Set.of(), Set.of());
    }
  }

  /**
   * The wrapper of the current state, read per call: the session's evaluation view, which answers
   * correspondence queries from the transaction's snapshot (step C4).
   */
  private final Supplier<MetamodelWrapperInterface> wrapper;

  private final ReverseIndex reverseIndex;

  RelevantInstanceResolver(MetamodelWrapperInterface wrapper, ReverseIndex reverseIndex) {
    this(() -> wrapper, reverseIndex);
  }

  RelevantInstanceResolver(Supplier<MetamodelWrapperInterface> wrapper, ReverseIndex reverseIndex) {
    this.wrapper = wrapper;
    this.reverseIndex = reverseIndex;
  }

  /**
   * Computes the relevant instances.
   *
   * @param plan the constraint's level-3 plan
   * @param pse the constraint's PSE set, used to ignore non-violating events while the constraint
   *     has no violation; {@code null} to consider every event that touches a read
   * @param useCorrespondenceIdiom whether rule 3c may be applied
   */
  Relevance resolve(
      RelevancePlan plan, TransactionDelta delta, PseSet pse, boolean useCorrespondenceIdiom) {
    Map<EObject, Boolean> relevant = new IdentityHashMap<>();
    Map<EObject, Boolean> removed = new IdentityHashMap<>();
    if (plan.unknownExtension()
        && !(delta.structurallyInsertedObjects().isEmpty()
            && delta.structurallyRemovedObjects().isEmpty())) {
      return Relevance.fullExtension();
    }

    for (ModelEvent event : delta.events()) {
      if (event.feature() == null || event.object() == null) {
        continue;
      }
      if (pse != null && !pse.isPse(event)) {
        continue;
      }
      for (RelevancePlan.Read read : plan.reads()) {
        if (!read.matches(event.feature().getName(), event.object())) {
          continue;
        }
        if (!addInverse(read.origin(), event.object(), plan, useCorrespondenceIdiom, relevant)) {
          return Relevance.fullExtension();
        }
      }
    }

    CorrespondenceDelta correspondences = delta.correspondenceDelta();
    if (correspondences.hasChanges()
        && !addCorrespondenceInverses(plan, correspondences, useCorrespondenceIdiom, relevant)) {
      return Relevance.fullExtension();
    }

    for (EObject inserted : delta.structurallyInsertedObjects()) {
      if (conformsToAny(inserted, plan.classEventTypes())) {
        return Relevance.fullExtension();
      }
      if (conformsToAny(inserted, plan.correspondentTypes())) {
        if (!useCorrespondenceIdiom) {
          return Relevance.fullExtension();
        }
        addCorrespondents(inserted, plan, relevant);
      }
      if (conformsToAny(inserted, plan.contextTypes())) {
        relevant.put(inserted, Boolean.TRUE);
      }
    }
    for (EObject gone : delta.structurallyRemovedObjects()) {
      if (conformsToAny(gone, plan.classEventTypes())
          || conformsToAny(gone, plan.correspondentTypes())) {
        return Relevance.fullExtension();
      }
      if (conformsToAny(gone, plan.contextTypes())) {
        removed.put(gone, Boolean.TRUE);
      }
    }

    Set<EObject> relevantInModel = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    Set<EObject> removedSet = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    removedSet.addAll(removed.keySet());
    Set<EObject> rootSet = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    rootSet.addAll(wrapper.get().getAllRootObjects());
    for (EObject candidate : relevant.keySet()) {
      if (!conformsToAny(candidate, plan.contextTypes())) {
        continue;
      }
      if (rootSet.contains(EcoreUtil.getRootContainer(candidate))) {
        relevantInModel.add(candidate);
        removedSet.remove(candidate); // moved, not removed
      } else {
        removedSet.add(candidate);
      }
    }
    for (EObject r : List.copyOf(removedSet)) {
      if (rootSet.contains(EcoreUtil.getRootContainer(r))) {
        removedSet.remove(r); // removed from one place but still in the model (move)
        relevantInModel.add(r);
      }
    }
    return new Relevance(false, relevantInModel, removedSet);
  }

  /**
   * Adds {@code Inverse(origin)(changed)}; returns {@code false} if the origin cannot be inverted
   * (class event).
   */
  private boolean addInverse(
      Origin origin,
      EObject changed,
      RelevancePlan plan,
      boolean useCorrespondenceIdiom,
      Map<EObject, Boolean> sink) {
    if (origin.kind() == Origin.Kind.CORRESPONDENTS && !useCorrespondenceIdiom) {
      return false;
    }
    if (!origin.traceable()) {
      return false;
    }
    List<EObject> current = List.of(changed);
    List<EReference> steps = origin.steps();
    for (int i = steps.size() - 1; i >= 0; i--) {
      List<EObject> next = new ArrayList<>();
      for (EObject x : current) {
        next.addAll(inverseStep(steps.get(i), x));
      }
      current = next;
    }
    if (origin.kind() == Origin.Kind.CORRESPONDENTS) {
      for (EObject y : current) {
        addCorrespondents(y, plan, sink);
      }
    } else {
      for (EObject self : current) {
        sink.put(self, Boolean.TRUE);
      }
    }
    return true;
  }

  /**
   * Adds, for every changed pair {@code (a, b)} and every use of {@code ~}, the inverse of one
   * invertible partner origin from both {@code a} and {@code b} (see {@link
   * RelevancePlan.CorrespondenceRead}); returns {@code false} if some use has no invertible partner
   * (class event). Post-state inversion suffices as for model events: the changed correspondence is
   * the first changed link, the path to it is unchanged or itself changed by a model event.
   */
  private boolean addCorrespondenceInverses(
      RelevancePlan plan,
      CorrespondenceDelta correspondences,
      boolean useCorrespondenceIdiom,
      Map<EObject, Boolean> sink) {
    for (RelevancePlan.CorrespondenceRead read : plan.correspondenceReads()) {
      Origin partner =
          isSelfPath(read.left()) ? read.left() : isSelfPath(read.right()) ? read.right() : null;
      if (partner == null) {
        return false;
      }
      for (Set<CorrespondenceDelta.Pair> pairs :
          List.of(correspondences.added(), correspondences.removed())) {
        for (CorrespondenceDelta.Pair pair : pairs) {
          if (!addInverse(partner, pair.first(), plan, useCorrespondenceIdiom, sink)
              || !addInverse(partner, pair.second(), plan, useCorrespondenceIdiom, sink)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Only reference paths from {@code self} are inverted for correspondence changes. A {@code
   * CORRESPONDENTS} origin is not: {@link #addCorrespondents} follows a single-object lookup, which
   * misses multi-element correspondences visible only from the other side (see {@code
   * CorrespondenceLookupSemanticsExperimentTest}).
   */
  private static boolean isSelfPath(Origin origin) {
    return origin.kind() == Origin.Kind.SELF;
  }

  private void addCorrespondents(EObject y, RelevancePlan plan, Map<EObject, Boolean> sink) {
    for (EObject c : wrapper.get().getCorrespondingObjects(y)) {
      if (conformsToAny(c, plan.contextTypes())) {
        sink.put(c, Boolean.TRUE);
      }
    }
  }

  /** The objects {@code x} with {@code y ∈ x.reference} in the post-state. */
  private List<EObject> inverseStep(EReference reference, EObject y) {
    if (reference.isContainment()) {
      return y.eContainmentFeature() == reference && y.eContainer() != null
          ? List.of(y.eContainer())
          : List.of();
    }
    EReference opposite = reference.getEOpposite();
    if (opposite != null) {
      List<EObject> result = new ArrayList<>();
      Object value = y.eGet(opposite);
      if (value instanceof Collection<?> c) {
        for (Object v : c) {
          if (v instanceof EObject eo) {
            result.add(eo);
          }
        }
      } else if (value instanceof EObject eo) {
        result.add(eo);
      }
      return result;
    }
    return reverseIndex.holders(reference, y);
  }

  private static boolean conformsToAny(EObject o, Set<EClass> types) {
    for (EClass t : types) {
      if (TransactionDelta.conforms(o.eClass(), t)) {
        return true;
      }
    }
    return false;
  }
}
