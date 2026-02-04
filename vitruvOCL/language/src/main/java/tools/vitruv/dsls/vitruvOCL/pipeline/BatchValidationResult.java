package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.util.*;

/**
 * Aggregated results from validating multiple VitruvOCL constraints.
 *
 * <p>Provides comprehensive analysis of batch validation including:
 *
 * <ul>
 *   <li>Overall success/failure statistics
 *   <li>Filtering by constraint status (satisfied, violated, failed)
 *   <li>Summary reporting for diagnostic output
 * </ul>
 *
 * <p>Distinguishes between:
 *
 * <ul>
 *   <li><b>Satisfied</b>: Constraint evaluated successfully and holds true
 *   <li><b>Violated</b>: Constraint evaluated successfully but holds false
 *   <li><b>Failed</b>: Constraint could not be evaluated due to errors
 * </ul>
 *
 * @see ConstraintResult for individual constraint evaluation results
 */
public class BatchValidationResult {
  /** Individual results for each validated constraint */
  private final List<ConstraintResult> results;

  /**
   * Creates batch validation result from individual constraint results.
   *
   * @param results List of constraint evaluation results
   */
  public BatchValidationResult(List<ConstraintResult> results) {
    this.results = new ArrayList<>(results);
  }

  /**
   * Returns all constraint results.
   *
   * @return Unmodifiable view of all results
   */
  public List<ConstraintResult> getResults() {
    return Collections.unmodifiableList(results);
  }

  /**
   * Checks if all constraints evaluated without errors.
   *
   * <p>Returns true only if no compilation or runtime errors occurred. Does not consider constraint
   * satisfaction status.
   *
   * @return {@code true} if all constraints evaluated successfully
   */
  public boolean allSucceeded() {
    return results.stream().allMatch(ConstraintResult::isSuccess);
  }

  /**
   * Checks if all constraints are both successful and satisfied.
   *
   * <p>Stricter than {@link #allSucceeded()}: requires constraints to evaluate without errors AND
   * hold true.
   *
   * @return {@code true} if all constraints evaluated and were satisfied
   */
  public boolean allSatisfied() {
    return results.stream()
        .filter(ConstraintResult::isSuccess)
        .allMatch(ConstraintResult::isSatisfied);
  }

  /**
   * Returns constraints that failed to evaluate.
   *
   * <p>Includes constraints with compilation errors, type errors, or runtime exceptions preventing
   * evaluation.
   *
   * @return List of failed constraint results
   */
  public List<ConstraintResult> getFailedConstraints() {
    return results.stream().filter(r -> !r.isSuccess()).toList();
  }

  /**
   * Returns constraints that evaluated successfully but were not satisfied.
   *
   * <p>These represent actual constraint violations in the model.
   *
   * @return List of violated constraint results
   */
  public List<ConstraintResult> getViolatedConstraints() {
    return results.stream()
        .filter(ConstraintResult::isSuccess)
        .filter(r -> !r.isSatisfied())
        .toList();
  }

  /**
   * Returns constraints that evaluated successfully and were satisfied.
   *
   * @return List of satisfied constraint results
   */
  public List<ConstraintResult> getSatisfiedConstraints() {
    return results.stream()
        .filter(ConstraintResult::isSuccess)
        .filter(ConstraintResult::isSatisfied)
        .toList();
  }

  /**
   * Generates human-readable summary of validation results.
   *
   * @return Summary string showing counts of satisfied, violated, and failed constraints
   */
  public String getSummary() {
    long satisfied = getSatisfiedConstraints().size();
    long violated = getViolatedConstraints().size();
    long failed = getFailedConstraints().size();
    long total = results.size();

    return String.format(
        "%d/%d constraints satisfied, %d violated, %d failed to evaluate",
        satisfied, total, violated, failed);
  }

  /**
   * Generates detailed report including summary and individual constraint results.
   *
   * @return Multi-line string with summary and all constraint details
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getSummary()).append("\n\n");

    for (int i = 0; i < results.size(); i++) {
      ConstraintResult result = results.get(i);
      sb.append(String.format("Constraint %d:\n", i + 1));
      sb.append(result.toString());
      if (i < results.size() - 1) {
        sb.append("\n\n");
      }
    }

    return sb.toString();
  }
}