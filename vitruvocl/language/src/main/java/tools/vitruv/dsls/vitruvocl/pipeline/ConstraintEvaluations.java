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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.common.ErrorSeverity;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.evaluator.Value;

/**
 * The two halves of evaluating a single constraint text: {@link #evaluateFresh} (compile and
 * evaluate from scratch, exactly as every batch/single-constraint entry point of {@link VitruvOCL}
 * does) and {@link #render} (turn the raw {@link EvaluationOutcome} into the user-facing {@link
 * ConstraintResult}).
 *
 * <p>Split out of {@code VitruvOCL#compileAndEvaluate} without behavioural change, so that the
 * incremental evaluator ({@code tools.vitruv.dsls.vitruvocl.incremental}) can render outcomes it
 * assembled itself in exactly the same format, and its oracle can compare against a from-scratch
 * evaluation at the level of instance identities.
 */
public final class ConstraintEvaluations {

  private static final String VIOLATION_SEP = "-".repeat(57);

  private ConstraintEvaluations() {}

  /**
   * Compiles and evaluates {@code constraint} from scratch with a fresh {@link VitruvOCLCompiler}.
   *
   * @param transactionSupported whether {@code pre}/{@code post} blocks are evaluated against
   *     {@code transaction} ({@code true}) or skipped ({@code false}) — see {@link
   *     EvaluationVisitor}'s field of the same name
   */
  public static EvaluationOutcome evaluateFresh(
      String constraint,
      MetamodelWrapperInterface wrapper,
      List<EChange<EObject>> transaction,
      boolean transactionSupported) {
    // The transaction-less VitruvOCLCompiler constructor (not the 3-arg one with an empty list)
    // must be used when the caller has no notion of a transaction at all — otherwise pre/post
    // blocks would be evaluated with vacuous empty-transaction semantics instead of being skipped.
    // See EvaluationVisitor#transactionSupported.
    VitruvOCLCompiler compiler =
        transactionSupported
            ? new VitruvOCLCompiler(wrapper, null, transaction)
            : new VitruvOCLCompiler(wrapper, null);
    Value result = compiler.compile(constraint);
    List<CompileError> errors =
        compiler.hasErrors() ? compiler.getErrors().getErrors() : List.of();

    if (result == null || !errors.isEmpty()) {
      return EvaluationOutcome.failure(errors);
    }

    EvaluationVisitor evaluator = compiler.getLastEvaluator();
    if (evaluator == null) {
      return new EvaluationOutcome(false, List.of(), List.of(), List.of(), 0);
    }
    return new EvaluationOutcome(
        false,
        List.of(),
        evaluator.getViolationRecords(),
        evaluator.getSkippedConstraints(),
        evaluator.getEvaluatedInstanceCount());
  }

  /**
   * Renders {@code outcome} into the user-facing result: failures become a non-successful result
   * carrying the errors (a parser failure, which reports no errors, becomes a generic syntax
   * error); successes carry one {@code PRE_POST_SKIPPED} warning per skipped block and one {@code
   * CONSTRAINT_VIOLATION} warning per violation record, in record order.
   *
   * <p>Instance labels and source files are computed from the instances' <em>current</em> state,
   * i.e. at render time.
   */
  public static ConstraintResult render(
      String constraint,
      MetamodelWrapperInterface wrapper,
      List<Warning> loaderWarnings,
      EvaluationOutcome outcome) {
    if (outcome.failed()) {
      List<CompileError> passErrors = outcome.errors();
      if (passErrors.isEmpty()) {
        passErrors =
            List.of(
                new CompileError(
                    1, 0, "Syntax error in constraint", ErrorSeverity.ERROR, constraint));
      }
      return new ConstraintResult(constraint, false, passErrors, List.of(), loaderWarnings);
    }

    List<Warning> warnings = new ArrayList<>(loaderWarnings);
    for (EvaluationVisitor.SkippedConstraint skipped : outcome.skipped()) {
      warnings.add(new Warning(Warning.WarningType.PRE_POST_SKIPPED, formatSkipNotice(skipped)));
    }

    List<EvaluationVisitor.ViolationRecord> records = outcome.records();
    boolean satisfied = records.isEmpty();
    String constraintName = extractConstraintName(constraint);
    for (EvaluationVisitor.ViolationRecord violation : records) {
      String sourceFile = wrapper.getSourceFileForInstance(violation.instance());
      String filename = sourceFile != null ? sourceFile : "unknown";
      String instanceLabel = describeInstance(violation.instance());
      String message =
          violation.customMessage() != null ? violation.customMessage() : instanceLabel;
      String block =
          formatViolationBlock(
              violation.severity(), constraintName, filename, instanceLabel, message);
      warnings.add(new Warning(Warning.WarningType.CONSTRAINT_VIOLATION, block));
    }

    return new ConstraintResult(constraint, satisfied, List.of(), List.of(), warnings);
  }

  /** The result reported for every repeated occurrence of a constraint text within one batch. */
  public static ConstraintResult duplicateResult(String constraint) {
    return new ConstraintResult(
        constraint,
        false,
        List.of(),
        List.of(),
        List.of(
            new Warning(
                Warning.WarningType.DUPLICATE_CONSTRAINT, "Constraint specified multiple times")));
  }

  /** Extracts the name of the first {@code inv}/{@code pre}/{@code post} block, or "unnamed". */
  public static String extractConstraintName(String constraint) {
    java.util.regex.Matcher m =
        java.util.regex.Pattern.compile("(?<!@)\\b(?:inv|pre|post)\\s+(\\w+)\\s*:")
            .matcher(constraint);
    return m.find() ? m.group(1) : "unnamed";
  }

  private static String describeInstance(EObject instance) {
    StringBuilder sb = new StringBuilder(instance.eClass().getName()).append("(");
    List<String> parts = new ArrayList<>();
    for (EStructuralFeature feature : instance.eClass().getEAllStructuralFeatures()) {
      if (!feature.isMany()) {
        Object value = instance.eGet(feature);
        if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
          parts.add(feature.getName() + "=\"" + value + "\"");
        }
      }
      if (parts.size() >= 3) {
        break;
      }
    }
    sb.append(String.join(", ", parts)).append(")");
    return sb.toString();
  }

  private static String formatViolationBlock(
      String severity, String constraintName, String model, String object, String message) {
    return VIOLATION_SEP
        + "\n"
        + "["
        + severity
        + "] "
        + constraintName
        + "\n"
        + "Model   : "
        + model
        + "\n"
        + "Object  : "
        + object
        + "\n"
        + "Message : "
        + message
        + "\n"
        + VIOLATION_SEP;
  }

  private static String formatSkipNotice(EvaluationVisitor.SkippedConstraint skipped) {
    String label =
        skipped.blockName() != null ? skipped.kind() + " " + skipped.blockName() : skipped.kind();
    return VitruvOCL.PRE_POST_SKIPPED_PREFIX
        + " '"
        + label
        + "' block in context "
        + skipped.contextName()
        + " was not evaluated — pre/post constraints require a transaction, which this"
        + " evaluation mode does not provide. Only invariants (inv) were checked.";
  }
}
