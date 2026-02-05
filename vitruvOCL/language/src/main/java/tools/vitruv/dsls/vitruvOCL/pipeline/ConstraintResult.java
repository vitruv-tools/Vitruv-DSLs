/*******************************************************************************
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
package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.util.*;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;

/**
 * Result of evaluating a single VitruvOCL constraint.
 *
 * <p>Captures the complete evaluation outcome including:
 *
 * <ul>
 *   <li>Whether constraint was satisfied (true/false)
 *   <li>Compilation errors preventing evaluation
 *   <li>File system errors (missing files, I/O failures)
 *   <li>Non-fatal warnings about constraint structure
 * </ul>
 *
 * <p>A result is considered successful ({@link #isSuccess()}) only if no file or compiler errors
 * occurred. Satisfaction status ({@link #isSatisfied()}) is only meaningful for successful
 * evaluations.
 *
 * @see BatchValidationResult for aggregated multi-constraint results
 */
public class ConstraintResult {
  /** The constraint expression that was evaluated */
  private final String constraint;

  /** Whether the constraint evaluated to true */
  private final boolean satisfied;

  /** Compilation errors from parsing or type checking */
  private final List<CompileError> compilerErrors;

  /** File system errors preventing constraint loading */
  private final List<FileError> fileErrors;

  /** Non-fatal warnings about the constraint */
  private final List<Warning> warnings;

  /**
   * Creates a constraint evaluation result.
   *
   * @param constraint The constraint expression
   * @param satisfied Whether constraint was satisfied
   * @param compilerErrors Compilation errors (empty if successful)
   * @param fileErrors File system errors (empty if successful)
   * @param warnings Non-fatal warnings
   */
  public ConstraintResult(
      String constraint,
      boolean satisfied,
      List<CompileError> compilerErrors,
      List<FileError> fileErrors,
      List<Warning> warnings) {
    this.constraint = constraint;
    this.satisfied = satisfied;
    this.compilerErrors = new ArrayList<>(compilerErrors);
    this.fileErrors = new ArrayList<>(fileErrors);
    this.warnings = new ArrayList<>(warnings);
  }

  /**
   * Checks if constraint was evaluated without errors.
   *
   * <p>Returns true only if no file or compiler errors occurred. Does not consider satisfaction
   * status or warnings.
   *
   * @return {@code true} if evaluation succeeded
   */
  public boolean isSuccess() {
    return compilerErrors.isEmpty() && fileErrors.isEmpty();
  }

  /**
   * Returns constraint satisfaction status.
   *
   * <p>Only meaningful when {@link #isSuccess()} returns true. If evaluation failed, this value
   * should be ignored.
   *
   * @return {@code true} if constraint was satisfied
   */
  public boolean isSatisfied() {
    return satisfied;
  }

  /**
   * @return The constraint expression that was evaluated
   */
  public String getConstraint() {
    return constraint;
  }

  /**
   * @return Unmodifiable list of compilation errors
   */
  public List<CompileError> getCompilerErrors() {
    return Collections.unmodifiableList(compilerErrors);
  }

  /**
   * @return Unmodifiable list of file system errors
   */
  public List<FileError> getFileErrors() {
    return Collections.unmodifiableList(fileErrors);
  }

  /**
   * @return Unmodifiable list of warnings
   */
  public List<Warning> getWarnings() {
    return Collections.unmodifiableList(warnings);
  }

  /**
   * @return {@code true} if any warnings were recorded
   */
  public boolean hasWarnings() {
    return !warnings.isEmpty();
  }

  /**
   * Generates human-readable summary.
   *
   * <p>Shows satisfaction status for successful evaluations, error details for failures, and
   * warnings when present.
   *
   * @return Summary string with status and diagnostics
   */
  @Override
  public String toString() {
    if (!isSuccess()) {
      return toDetailedErrorString();
    }

    StringBuilder sb = new StringBuilder();
    sb.append(satisfied ? "✓ SATISFIED" : "✗ VIOLATED");

    if (hasWarnings()) {
      sb.append("\nWARNINGS (").append(warnings.size()).append("):");
      warnings.forEach(w -> sb.append("\n  ").append(w));
    }

    return sb.toString();
  }

  /**
   * Generates detailed error report for failed evaluations.
   *
   * <p>Lists all file errors, compiler errors with line numbers, and warnings. Used internally by
   * {@link #toString()} when evaluation failed.
   *
   * @return Detailed multi-line error report
   */
  public String toDetailedErrorString() {
    StringBuilder sb = new StringBuilder("✗ EVALUATION FAILED\n");

    if (!fileErrors.isEmpty()) {
      sb.append("\nFILE ERRORS (").append(fileErrors.size()).append("):");
      fileErrors.forEach(e -> sb.append("\n  ").append(e));
    }

    if (!compilerErrors.isEmpty()) {
      sb.append("\nCOMPILER ERRORS (").append(compilerErrors.size()).append("):");
      compilerErrors.forEach(
          e -> sb.append("\n  Line ").append(e.getLine()).append(": ").append(e.getMessage()));
    }

    if (hasWarnings()) {
      sb.append("\nWARNINGS (").append(warnings.size()).append("):");
      warnings.forEach(w -> sb.append("\n  ").append(w));
    }

    return sb.toString();
  }

  /**
   * Returns a human-readable summary of warnings and errors.
   *
   * <p>Provides a concise status message indicating whether the constraint evaluation encountered
   * any issues:
   *
   * <ul>
   *   <li>If compilation failed: Lists compiler errors
   *   <li>If constraints violated: Reports number of violated instances
   *   <li>If all clear: Confirms successful evaluation
   * </ul>
   *
   * @return Human-readable warning/error summary
   */
  public String getWarningsSummary() {
    if (!isSuccess()) {
      return "Compilation failed: " + compilerErrors.size() + " error(s) detected";
    }

    if (!isSatisfied()) {
      long violationCount =
          warnings.stream()
              .filter(w -> w.getType() == Warning.WarningType.CONSTRAINT_VIOLATION)
              .count();
      return "Warnings: " + violationCount + " constraint violation(s) detected";
    }

    return "No warnings - constraint satisfied";
  }
}