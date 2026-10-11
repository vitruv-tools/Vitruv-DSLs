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

import java.util.List;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;

/**
 * Raw, not yet rendered outcome of compiling and evaluating one constraint text: either the errors
 * that made it fail, or the violation records and skip notices of a successful evaluation.
 *
 * <p>Kept separate from {@link ConstraintResult} (the rendered, string-based form) so that callers
 * that need the violating {@code EObject}s themselves — the incremental evaluator's violation set
 * and its oracle — can work with instance identities instead of formatted text.
 *
 * @param failed whether compilation or evaluation reported errors (or the parser failed)
 * @param errors the errors reported by any pass; empty for a parser failure, which reports none
 * @param records violation records in evaluation order; empty if {@code failed}
 * @param skipped pre/post blocks skipped for lack of a transaction context; empty if {@code failed}
 * @param evaluatedInstances number of (context instance, inv/pre/post block set) evaluations the
 *     evaluator performed; used only for test statistics
 */
public record EvaluationOutcome(
    boolean failed,
    List<CompileError> errors,
    List<EvaluationVisitor.ViolationRecord> records,
    List<EvaluationVisitor.SkippedConstraint> skipped,
    int evaluatedInstances) {

  public EvaluationOutcome {
    errors = List.copyOf(errors);
    records = List.copyOf(records);
    skipped = List.copyOf(skipped);
  }

  /** Outcome of a constraint that failed to compile or evaluate. */
  public static EvaluationOutcome failure(List<CompileError> errors) {
    return new EvaluationOutcome(true, errors, List.of(), List.of(), 0);
  }
}
