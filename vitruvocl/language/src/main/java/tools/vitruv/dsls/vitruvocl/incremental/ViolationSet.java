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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor.ViolationRecord;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;

/**
 * The current outcome of every constraint of a session, kept between transactions (building block
 * D). Violations are keyed by (constraint text, instance identity), so that re-evaluating a subset
 * of a constraint's instances can replace exactly their entries.
 *
 * <p>Replaces the papers' assumption "the model was consistent before the transaction": the set is
 * initialised by a full evaluation and afterwards always reflects all current violations, whatever
 * the state of the model.
 */
final class ViolationSet {

  /** Identity wrapper: two distinct EObjects are distinct keys even if {@code equals} says otherwise. */
  record InstanceKey(EObject instance) {
    @Override
    public boolean equals(Object o) {
      return o instanceof InstanceKey other && other.instance == instance;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(instance);
    }
  }

  /** Mutable per-constraint state. */
  private static final class ConstraintState {
    private EvaluationOutcome lastFullOutcome;
    private boolean partiallyUpdated;
    private final Map<InstanceKey, List<ViolationRecord>> violations = new LinkedHashMap<>();
  }

  private final Map<String, ConstraintState> states = new LinkedHashMap<>();

  /** Replaces everything known about {@code constraint} by a full evaluation's outcome. */
  void replaceAll(String constraint, EvaluationOutcome outcome) {
    ConstraintState state = new ConstraintState();
    state.lastFullOutcome = outcome;
    for (ViolationRecord record : outcome.records()) {
      state.violations
          .computeIfAbsent(new InstanceKey(record.instance()), k -> new ArrayList<>())
          .add(record);
    }
    states.put(constraint, state);
  }

  /**
   * Replaces the entries of the {@code evaluated} instances by the records of a successful partial
   * evaluation over exactly those instances, and drops the entries of {@code removed} instances.
   */
  void replaceInstances(
      String constraint,
      Collection<EObject> evaluated,
      Collection<EObject> removed,
      EvaluationOutcome partialOutcome) {
    ConstraintState state = requireState(constraint);
    state.partiallyUpdated = true;
    for (EObject instance : evaluated) {
      state.violations.remove(new InstanceKey(instance));
    }
    for (EObject instance : removed) {
      state.violations.remove(new InstanceKey(instance));
    }
    for (ViolationRecord record : partialOutcome.records()) {
      state.violations
          .computeIfAbsent(new InstanceKey(record.instance()), k -> new ArrayList<>())
          .add(record);
    }
  }

  /** Whether {@code constraint} currently has violations, or its last full evaluation failed. */
  boolean hasViolationsOrFailure(String constraint) {
    ConstraintState state = requireState(constraint);
    return state.lastFullOutcome.failed() || !state.violations.isEmpty();
  }

  /** Whether the last full evaluation of {@code constraint} failed (compile or run-time error). */
  boolean lastEvaluationFailed(String constraint) {
    return requireState(constraint).lastFullOutcome.failed();
  }

  /** Whether {@code instance} currently violates {@code constraint}. */
  boolean isViolating(String constraint, EObject instance) {
    return requireState(constraint).violations.containsKey(new InstanceKey(instance));
  }

  /** The instances currently violating {@code constraint}, in record order. */
  List<EObject> violatingInstances(String constraint) {
    return requireState(constraint).violations.keySet().stream().map(InstanceKey::instance).toList();
  }

  /** The current outcome of {@code constraint}, assembled from the stored state. */
  EvaluationOutcome currentOutcome(String constraint) {
    ConstraintState state = requireState(constraint);
    EvaluationOutcome full = state.lastFullOutcome;
    if (full.failed() || !state.partiallyUpdated) {
      return full; // records in the evaluator's order, exactly as a full evaluation reports them
    }
    List<ViolationRecord> records = new ArrayList<>();
    state.violations.values().forEach(records::addAll);
    return new EvaluationOutcome(false, List.of(), records, full.skipped(), full.evaluatedInstances());
  }

  /**
   * Whether {@link #currentOutcome} reports violations in the order of a full evaluation, i.e. no
   * partial (instance-subset) update happened since the last full evaluation of the constraint.
   */
  boolean preservesEvaluationOrder(String constraint) {
    return !requireState(constraint).partiallyUpdated;
  }

  private ConstraintState requireState(String constraint) {
    ConstraintState state = states.get(constraint);
    if (state == null) {
      throw new IllegalStateException("No state for constraint (session not initialised?)");
    }
    return state;
  }
}
