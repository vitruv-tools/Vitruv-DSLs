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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.ConstraintAnalysis;
import tools.vitruv.dsls.vitruvocl.incremental.analysis.FeatureIndex;
import tools.vitruv.dsls.vitruvocl.pipeline.BatchValidationResult;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintEvaluations;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintResult;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;
import tools.vitruv.dsls.vitruvocl.pipeline.MetamodelWrapperInterface;
import tools.vitruv.dsls.vitruvocl.pipeline.VsumWrapper;

/**
 * Stateful incremental evaluation of a fixed list of constraints against one model (VSUM or
 * wrapper).
 *
 * <p>Life cycle: {@link #initialize()} compiles every constraint once and runs a full evaluation
 * (exactly like a batch evaluation without transaction: pre/post blocks are skipped and reported).
 * Afterwards every {@link #onTransaction} call classifies the transaction's changes, re-evaluates
 * the constraints the configured {@link IncrementalLevel} selects (with transaction support, so
 * pre/post blocks are evaluated), updates the {@link ViolationSet} and returns <em>all current</em>
 * violations. With {@link IncrementalConfig#verify()} every result is checked against a full
 * from-scratch evaluation of the same state.
 *
 * <p>A transaction must be passed in after it has been applied to the model (post-state), with its
 * changes in chronological order. Not thread-safe: transactions must be delivered one at a time.
 */
public final class IncrementalSession implements AutoCloseable {

  private final List<String> constraints;
  private final List<String> distinctConstraints;
  private final MetamodelWrapperInterface wrapper;
  private final IncrementalConfig config;
  private final Map<String, CompiledConstraint> compiled = new LinkedHashMap<>();
  private final ViolationSet violations = new ViolationSet();
  private final ExecutorService executor;
  private IncrementalResult latest;
  private TransactionCollector attachedCollector;

  /**
   * Set while a transaction is being processed and cleared only after it completed (including the
   * oracle check). If processing fails, the transaction's delta is lost; the flag then makes the
   * next transaction re-evaluate every constraint, so that no constraint keeps a stale outcome.
   */
  private boolean forceFullEvaluation;
  private VsumWrapper attachedWrapper;

  /**
   * Creates a session; call {@link #initialize()} before the first transaction.
   *
   * @param constraints constraint texts in result order (duplicates are reported as duplicates,
   *     exactly as in a batch evaluation)
   */
  public IncrementalSession(
      List<String> constraints, MetamodelWrapperInterface wrapper, IncrementalConfig config) {
    if (constraints == null || wrapper == null || config == null) {
      throw new IllegalArgumentException("constraints, wrapper and config must not be null");
    }
    this.constraints = List.copyOf(constraints);
    this.distinctConstraints = List.copyOf(new LinkedHashSet<>(constraints));
    this.wrapper = wrapper;
    this.config = config;
    this.executor = config.threads() > 1 ? Executors.newFixedThreadPool(config.threads()) : null;
  }

  /** The configuration this session runs with. */
  public IncrementalConfig config() {
    return config;
  }

  /** The most recent result, or {@code null} before {@link #initialize()}. */
  public IncrementalResult latestResult() {
    return latest;
  }

  /** Compiles all constraints and performs the initial full evaluation. */
  public IncrementalResult initialize() {
    IncrementalStats.Builder stats = new IncrementalStats.Builder().constraints(distinctConstraints.size());
    FeatureIndex features = FeatureIndex.forWrapper(wrapper);
    for (String constraint : distinctConstraints) {
      compiled.put(constraint, CompiledConstraint.compile(constraint, wrapper, features));
    }
    List<EvaluationOutcome> outcomes =
        evaluateAll(distinctConstraints, c -> compiled.get(c).evaluate(List.of(), false, null));
    for (int i = 0; i < distinctConstraints.size(); i++) {
      violations.replaceAll(distinctConstraints.get(i), outcomes.get(i));
      stats.evaluated(true, outcomes.get(i).evaluatedInstances());
    }
    if (config.verify()) {
      verify(List.of(), false);
    }
    latest = new IncrementalResult(render(), stats.build());
    return latest;
  }

  /**
   * Processes one transaction that has already been applied to the model.
   *
   * @param changes the transaction's atomic changes in chronological order (for a Vitruvius
   *     propagation: per propagated change, the original change followed by its consequential
   *     changes)
   * @return all current violations and what was re-evaluated
   * @throws IncrementalMismatchException in oracle mode, if the result differs from a full evaluation
   */
  public IncrementalResult onTransaction(List<EChange<EObject>> changes) {
    if (latest == null) {
      throw new IllegalStateException("initialize() must be called before onTransaction()");
    }
    List<EChange<EObject>> transaction = changes == null ? List.of() : List.copyOf(changes);
    boolean recovering = forceFullEvaluation;
    forceFullEvaluation = true;
    TransactionDelta delta = EChangeClassifier.classify(transaction);
    IncrementalStats.Builder stats = new IncrementalStats.Builder().constraints(distinctConstraints.size());

    List<String> selected = new ArrayList<>();
    List<Boolean> forced = new ArrayList<>();
    for (String constraint : distinctConstraints) {
      ConstraintAnalysis.Decision decision =
          recovering ? ConstraintAnalysis.Decision.EVALUATE_ALWAYS : decide(constraint, delta);
      if (decision.evaluate()) {
        selected.add(constraint);
        forced.add(decision.forced());
      } else {
        stats.skipped();
      }
    }

    List<EvaluationOutcome> outcomes =
        evaluateAll(selected, c -> compiled.get(c).evaluate(transaction, true, null));
    for (int i = 0; i < selected.size(); i++) {
      violations.replaceAll(selected.get(i), outcomes.get(i));
      stats.evaluated(forced.get(i), outcomes.get(i).evaluatedInstances());
    }

    if (config.verify()) {
      verify(transaction, true);
    }
    forceFullEvaluation = false;
    latest = new IncrementalResult(render(), stats.build());
    return latest;
  }

  private ConstraintAnalysis.Decision decide(String constraint, TransactionDelta delta) {
    if (config.level() == IncrementalLevel.FULL) {
      return ConstraintAnalysis.Decision.EVALUATE_ALWAYS;
    }
    ConstraintAnalysis analysis = compiled.get(constraint).analysis();
    if (config.level().atLeast(IncrementalLevel.POLARITY)) {
      return analysis.decideByPolarity(delta, violations.hasViolationsOrFailure(constraint));
    }
    return analysis.decideByReadSet(delta);
  }

  /**
   * Registers this session as change-propagation listener on {@code vsumWrapper}'s VSUM: after
   * every {@code finishedChangePropagation} the transaction is processed and the result handed to
   * {@code onResult}. Failures (including oracle mismatches) are handed to {@code onFailure}
   * instead of being thrown into the propagation.
   */
  public void attachTo(
      VsumWrapper vsumWrapper,
      Consumer<IncrementalResult> onResult,
      Consumer<RuntimeException> onFailure) {
    if (attachedCollector != null) {
      throw new IllegalStateException("Session is already attached to a VSUM");
    }
    attachedCollector = new TransactionCollector(this::onTransaction, onResult, onFailure);
    attachedWrapper = vsumWrapper;
    vsumWrapper.addTransactionListener(attachedCollector);
  }

  /** Removes the listener registered by {@link #attachTo}, if any, and stops worker threads. */
  @Override
  public void close() {
    if (attachedCollector != null) {
      attachedWrapper.removeTransactionListener(attachedCollector);
      attachedCollector = null;
      attachedWrapper = null;
    }
    if (executor != null) {
      executor.shutdown();
    }
  }

  private void verify(List<EChange<EObject>> transaction, boolean transactionSupported) {
    StringBuilder diffs = new StringBuilder();
    for (String constraint : distinctConstraints) {
      EvaluationOutcome full =
          ConstraintEvaluations.evaluateFresh(constraint, wrapper, transaction, transactionSupported);
      String diff =
          OracleVerifier.diff(
              constraint,
              violations.currentOutcome(constraint),
              full,
              violations.preservesEvaluationOrder(constraint));
      if (diff != null) {
        diffs.append(diff);
      }
    }
    if (!diffs.isEmpty()) {
      throw new IncrementalMismatchException(
          "Incremental evaluation (level "
              + config.level().number()
              + ") differs from full evaluation:\n"
              + diffs);
    }
  }

  private BatchValidationResult render() {
    List<ConstraintResult> results = new ArrayList<>(constraints.size());
    Set<String> seen = new LinkedHashSet<>();
    for (String constraint : constraints) {
      if (!seen.add(constraint)) {
        results.add(ConstraintEvaluations.duplicateResult(constraint));
      } else {
        results.add(
            ConstraintEvaluations.render(
                constraint, wrapper, List.of(), violations.currentOutcome(constraint)));
      }
    }
    return new BatchValidationResult(results);
  }

  /** Evaluates {@code selected} sequentially or on the session's pool; results in input order. */
  private List<EvaluationOutcome> evaluateAll(
      List<String> selected, java.util.function.Function<String, EvaluationOutcome> task) {
    if (executor == null || selected.size() < 2) {
      return selected.stream().map(task).toList();
    }
    List<Future<EvaluationOutcome>> futures = new ArrayList<>(selected.size());
    for (String constraint : selected) {
      futures.add(executor.submit(() -> task.apply(constraint)));
    }
    List<EvaluationOutcome> results = new ArrayList<>(selected.size());
    for (Future<EvaluationOutcome> future : futures) {
      try {
        results.add(future.get());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while evaluating constraints", e);
      } catch (ExecutionException e) {
        throw new IllegalStateException("Constraint evaluation failed: " + e.getCause(), e.getCause());
      }
    }
    return results;
  }
}
