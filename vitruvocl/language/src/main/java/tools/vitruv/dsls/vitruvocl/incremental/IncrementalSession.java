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
import tools.vitruv.dsls.vitruvocl.incremental.analysis.ReadSet;
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

  /**
   * System property that turns off the shared correspondence lookup (step C4): evaluations then
   * query the wrapper directly, as before. Read once per session.
   */
  public static final String PROP_NO_SHARED_CORRESPONDENCE_LOOKUP =
      "vitruvocl.incremental.noSharedCorrespondenceLookup";

  private final boolean sharedCorrespondenceLookup =
      !Boolean.getBoolean(PROP_NO_SHARED_CORRESPONDENCE_LOOKUP);

  private final List<String> constraints;
  private final List<String> distinctConstraints;
  private final MetamodelWrapperInterface wrapper;
  private final IncrementalConfig config;
  private final Map<String, CompiledConstraint> compiled = new LinkedHashMap<>();
  private final ViolationSet violations = new ViolationSet();
  private final ExecutorService executor;
  private final ReverseIndex reverseIndex;
  private final RelevantInstanceResolver resolver;
  private IncrementalResult latest;
  private TransactionCollector attachedCollector;

  /**
   * Whether correspondence changes are observed at all: a level above 0 and some constraint that is
   * not always evaluated uses {@code ~}. Decided once in {@link #initialize()}.
   */
  private boolean observingCorrespondences;

  /**
   * The correspondence relation after the last processed transaction, or {@code null} if there is
   * no baseline: not observing, the wrapper cannot enumerate correspondences, or the last capture
   * failed. Without a baseline a transaction carries an unknown correspondence delta.
   */
  private CorrespondenceSnapshot correspondenceSnapshot;

  /**
   * The wrapper the current transaction's evaluations use: a {@link SnapshotCorrespondenceWrapper}
   * over the current snapshot if there is one (step C4), else the session's wrapper. Rebuilt
   * whenever the snapshot is replaced.
   */
  private MetamodelWrapperInterface evaluationWrapper;

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
    this.reverseIndex = new ReverseIndex(wrapper::getAllRootObjects);
    this.resolver = new RelevantInstanceResolver(() -> evaluationWrapper, reverseIndex);
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
    observingCorrespondences = observesCorrespondences();
    correspondenceSnapshot = observingCorrespondences ? captureCorrespondences() : null;
    evaluationWrapper = viewOf(correspondenceSnapshot);
    MetamodelWrapperInterface view = evaluationWrapper;
    List<EvaluationOutcome> outcomes =
        evaluateAll(
            distinctConstraints, c -> compiled.get(c).evaluate(view, List.of(), false, null));
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
    TransactionDelta delta =
        EChangeClassifier.classify(transaction).withCorrespondenceDelta(observeCorrespondences());
    IncrementalStats.Builder stats = new IncrementalStats.Builder().constraints(distinctConstraints.size());

    if (config.level().atLeast(IncrementalLevel.RELEVANT_INSTANCES)) {
      reverseIndex.apply(delta);
    }

    List<String> selected = new ArrayList<>();
    List<Boolean> forced = new ArrayList<>();
    Map<String, RelevantInstanceResolver.Relevance> relevance = new LinkedHashMap<>();
    for (String constraint : distinctConstraints) {
      ConstraintAnalysis.Decision decision =
          recovering ? ConstraintAnalysis.Decision.EVALUATE_ALWAYS : decide(constraint, delta);
      if (decision.evaluate()) {
        selected.add(constraint);
        forced.add(decision.forced());
        relevance.put(constraint, relevance(constraint, decision, delta));
      } else {
        stats.skipped();
      }
    }

    List<EvaluationOutcome> outcomes =
        evaluateAll(selected, c -> evaluateSelected(c, relevance.get(c), transaction));
    for (int i = 0; i < selected.size(); i++) {
      String constraint = selected.get(i);
      RelevantInstanceResolver.Relevance r = relevance.get(constraint);
      EvaluationOutcome outcome = outcomes.get(i);
      if (r.classEvent()) {
        violations.replaceAll(constraint, outcome);
        if (config.level().atLeast(IncrementalLevel.RELEVANT_INSTANCES) && !forced.get(i)) {
          stats.classEventFallback();
        }
      } else if (outcome.failed()) {
        // a partial evaluation failed: fall back to a full evaluation of the constraint
        EvaluationOutcome full =
            compiled.get(constraint).evaluate(evaluationWrapper, transaction, true, null);
        violations.replaceAll(constraint, full);
        outcome = full;
      } else {
        violations.replaceInstances(constraint, r.relevant(), r.removed(), outcome);
        stats.partial();
      }
      stats.evaluated(forced.get(i), outcome.evaluatedInstances());
    }

    if (config.verify()) {
      verify(transaction, true);
    }
    forceFullEvaluation = false;
    latest = new IncrementalResult(render(), stats.build());
    return latest;
  }

  /**
   * Whether correspondence changes are worth observing: some constraint uses {@code ~} and is not
   * evaluated after every transaction anyway.
   */
  private boolean observesCorrespondences() {
    if (config.level() == IncrementalLevel.FULL) {
      return false;
    }
    for (CompiledConstraint constraint : compiled.values()) {
      ConstraintAnalysis analysis = constraint.analysis();
      ReadSet readSet = analysis == null ? null : analysis.readSet();
      if (readSet != null && readSet.usesCorrespondence() && !readSet.requiresFullEvaluation()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Diffs the correspondence relation against the previous transaction's. The snapshot is replaced
   * before evaluation, so a failure later in the transaction cannot leave it stale; the recovery
   * flag then re-evaluates everything once. Without a baseline (see {@link #correspondenceSnapshot})
   * the delta is unknown and the current relation becomes the new baseline.
   */
  private CorrespondenceDelta observeCorrespondences() {
    if (!observingCorrespondences) {
      return CorrespondenceDelta.unknown();
    }
    CorrespondenceSnapshot previous = correspondenceSnapshot;
    CorrespondenceSnapshot current = captureCorrespondences();
    correspondenceSnapshot = current;
    evaluationWrapper = viewOf(current);
    return previous == null || current == null
        ? CorrespondenceDelta.unknown()
        : previous.diff(current);
  }

  /** The evaluation view for {@code snapshot}: answers correspondence queries from it (step C4). */
  private MetamodelWrapperInterface viewOf(CorrespondenceSnapshot snapshot) {
    // The snapshot answers correspondenceHasTag as the two-way tagged lookup, which is only the
    // wrapper's own answer under that contract.
    return snapshot == null || !sharedCorrespondenceLookup || !wrapper.hasTaggedCorrespondenceLookup()
        ? wrapper
        : new SnapshotCorrespondenceWrapper(wrapper, snapshot);
  }

  /**
   * Captures the correspondence relation; {@code null} if the wrapper cannot enumerate it or a
   * lookup fails, so that a broken correspondence model degrades to the conservative handling of
   * {@code ~} instead of failing every transaction.
   */
  private CorrespondenceSnapshot captureCorrespondences() {
    try {
      return CorrespondenceSnapshot.capture(wrapper).orElse(null);
    } catch (RuntimeException e) {
      return null;
    }
  }

  private ConstraintAnalysis.Decision decide(String constraint, TransactionDelta delta) {
    if (config.level() == IncrementalLevel.FULL) {
      return ConstraintAnalysis.Decision.EVALUATE_ALWAYS;
    }
    ConstraintAnalysis analysis = compiled.get(constraint).analysis();
    boolean idiom = useCorrespondenceIdiom();
    if (config.level().atLeast(IncrementalLevel.POLARITY)) {
      return analysis.decideByPolarity(delta, violations.hasViolationsOrFailure(constraint), idiom);
    }
    return analysis.decideByReadSet(delta, idiom);
  }

  private boolean useCorrespondenceIdiom() {
    return config.correspondenceIdiom() && config.level().atLeast(IncrementalLevel.RELEVANT_INSTANCES);
  }

  /**
   * Level 3: which instances of an evaluated constraint to re-evaluate. Forced decisions, failed
   * previous evaluations, unclassifiable changes and lower levels re-evaluate the whole extension.
   */
  private RelevantInstanceResolver.Relevance relevance(
      String constraint, ConstraintAnalysis.Decision decision, TransactionDelta delta) {
    ConstraintAnalysis analysis = compiled.get(constraint).analysis();
    if (!config.level().atLeast(IncrementalLevel.RELEVANT_INSTANCES)
        || decision.forced()
        || analysis.relevancePlan() == null
        || violations.lastEvaluationFailed(constraint)) {
      return RelevantInstanceResolver.Relevance.fullExtension();
    }
    // Polarity may only filter events while the constraint has no violation (level 2 rule).
    boolean usePolarity = !violations.hasViolationsOrFailure(constraint);
    return resolver.resolve(
        analysis.relevancePlan(), delta, usePolarity ? analysis.pseSet() : null, useCorrespondenceIdiom());
  }

  private EvaluationOutcome evaluateSelected(
      String constraint, RelevantInstanceResolver.Relevance relevance, List<EChange<EObject>> tx) {
    MetamodelWrapperInterface view = evaluationWrapper;
    if (relevance.classEvent()) {
      return compiled.get(constraint).evaluate(view, tx, true, null);
    }
    List<EObject> instances = List.copyOf(relevance.relevant());
    return compiled
        .get(constraint)
        .evaluate(
            view,
            tx,
            true,
            ctx -> {
              org.eclipse.emf.ecore.EClass type = compiled.get(constraint).contextClass(ctx);
              return type == null
                  ? List.of()
                  : instances.stream().filter(o -> TransactionDelta.conforms(o.eClass(), type)).toList();
            });
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
