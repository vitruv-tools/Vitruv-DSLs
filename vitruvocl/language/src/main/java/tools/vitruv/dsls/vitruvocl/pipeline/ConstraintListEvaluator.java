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

package tools.vitruv.dsls.vitruvocl.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Distributes evaluation of a constraint list across a configurable-size {@link ExecutorService}
 * thread pool, while preserving the caller-visible ordering of results.
 *
 * <p>Each constraint is handed to {@code perConstraintEvaluator} exactly once, on some pool
 * thread; {@code VitruvOCL}'s production callback creates a fresh {@link VitruvOCLCompiler} — and
 * therefore a fresh {@link tools.vitruv.dsls.vitruvocl.common.ErrorCollector} — per invocation, so
 * no compiler-level mutable state is ever shared between tasks. The one piece of state genuinely
 * shared across tasks is the {@link MetamodelWrapperInterface} passed into every compiler; see
 * {@link MetamodelWrapper#getAllInstances} and {@link VsumWrapper#getAllInstances} for how their
 * internal allInstances() cache stays correct under this concurrent access.
 *
 * <p>Results are returned in the same order as the input {@code constraints} list regardless of
 * which worker thread finished first — callers submit by index and read back {@link Future#get()}
 * in submission order, which blocks on stragglers but never reorders finished results.
 */
final class ConstraintListEvaluator {

  private ConstraintListEvaluator() {}

  /**
   * Evaluates every constraint in {@code constraints}, distributing the work across {@code
   * threadPoolSize} worker threads.
   *
   * @param constraints the constraints to evaluate, in the order results should be returned
   * @param perConstraintEvaluator evaluates a single constraint in isolation; invoked once per
   *     constraint, possibly concurrently from multiple threads for different constraints
   * @param threadPoolSize number of worker threads to use; must be at least 1
   * @return evaluation results in the same order as {@code constraints}
   */
  static List<ConstraintResult> evaluate(
      List<String> constraints,
      Function<String, ConstraintResult> perConstraintEvaluator,
      int threadPoolSize) {
    if (threadPoolSize < 1) {
      throw new IllegalArgumentException(
          "threadPoolSize must be at least 1, was " + threadPoolSize);
    }
    if (constraints.isEmpty()) {
      return List.of();
    }

    ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
    try {
      List<Future<ConstraintResult>> futures = new ArrayList<>(constraints.size());
      for (String constraint : constraints) {
        Callable<ConstraintResult> task = () -> perConstraintEvaluator.apply(constraint);
        futures.add(executor.submit(task));
      }

      List<ConstraintResult> results = new ArrayList<>(constraints.size());
      for (Future<ConstraintResult> future : futures) {
        results.add(awaitResult(future));
      }
      return results;
    } finally {
      executor.shutdown();
    }
  }

  private static ConstraintResult awaitResult(Future<ConstraintResult> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for constraint evaluation", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Constraint evaluation failed", e.getCause());
    }
  }
}
