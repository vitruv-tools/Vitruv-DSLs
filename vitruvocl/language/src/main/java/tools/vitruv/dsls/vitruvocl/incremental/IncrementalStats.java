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

/**
 * Counters describing what one incremental evaluation did (building block F). They exist so that
 * tests can check that constraints and instances are actually skipped; they contain no timing
 * information and are not meant for reporting.
 *
 * @param constraints distinct constraint texts in the session
 * @param constraintsEvaluated constraints (re-)evaluated, fully or on a subset of instances
 * @param constraintsSkipped constraints not evaluated at all
 * @param forcedEvaluations evaluations forced regardless of the transaction content (pre/post,
 *     {@code @pre}, lifecycle predicates, error nodes, correspondences, unknown changes, level 0)
 * @param instancesEvaluated (constraint, context instance) pairs evaluated
 * @param classEventFallbacks constraints evaluated on their full extension at level 3 because the
 *     relevant instances could not be narrowed down
 * @param partialEvaluations constraints evaluated on a strict subset of their extension (level 3)
 */
public record IncrementalStats(
    int constraints,
    int constraintsEvaluated,
    int constraintsSkipped,
    int forcedEvaluations,
    int instancesEvaluated,
    int classEventFallbacks,
    int partialEvaluations) {

  /** Mutable accumulator; not thread-safe, used by the session's coordinating thread only. */
  static final class Builder {
    private int constraints;
    private int evaluated;
    private int skipped;
    private int forced;
    private int instances;
    private int classEventFallbacks;
    private int partial;

    Builder constraints(int count) {
      constraints = count;
      return this;
    }

    void evaluated(boolean wasForced, int instanceCount) {
      evaluated++;
      instances += instanceCount;
      if (wasForced) {
        forced++;
      }
    }

    void skipped() {
      skipped++;
    }

    void classEventFallback() {
      classEventFallbacks++;
    }

    void partial() {
      partial++;
    }

    IncrementalStats build() {
      return new IncrementalStats(
          constraints, evaluated, skipped, forced, instances, classEventFallbacks, partial);
    }
  }
}
