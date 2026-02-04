package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;

/**
 * Result of OCL constraint validation containing errors and violations.
 *
 * <p>Distinguishes between:
 *
 * <ul>
 *   <li><b>Errors</b>: Compilation or evaluation failures preventing constraint checking
 *   <li><b>Violations</b>: Successfully evaluated constraints that were not satisfied
 * </ul>
 */
public class ValidationResult {

  /** Compilation or runtime errors */
  private final List<CompileError> errors;

  /** Constraint violations with context */
  private final List<ConstraintViolation> violations;

  /**
   * Creates validation result.
   *
   * @param errors Compilation/evaluation errors
   * @param violations Constraint violations
   */
  public ValidationResult(List<CompileError> errors, List<ConstraintViolation> violations) {
    this.errors = new ArrayList<>(errors);
    this.violations = new ArrayList<>(violations);
  }

  /**
   * @return {@code true} if compilation or evaluation errors occurred
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  /**
   * @return {@code true} if any constraints were violated
   */
  public boolean hasViolations() {
    return !violations.isEmpty();
  }

  /**
   * @return Unmodifiable list of errors
   */
  public List<CompileError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  /**
   * @return Unmodifiable list of violations
   */
  public List<ConstraintViolation> getViolations() {
    return Collections.unmodifiableList(violations);
  }

  /** Represents a single constraint violation with context. */
  public static class ConstraintViolation {
    /** Name of violated constraint */
    private final String constraintName;

    /** Human-readable violation description */
    private final String message;

    /** Model object that violated the constraint */
    private final Object violatingObject;

    /**
     * Creates constraint violation record.
     *
     * @param constraintName Name of violated constraint
     * @param message Violation description
     * @param violatingObject Object that violated constraint
     */
    public ConstraintViolation(String constraintName, String message, Object violatingObject) {
      this.constraintName = constraintName;
      this.message = message;
      this.violatingObject = violatingObject;
    }

    /**
     * @return Name of violated constraint
     */
    public String getConstraintName() {
      return constraintName;
    }

    /**
     * @return Violation description
     */
    public String getMessage() {
      return message;
    }

    /**
     * @return Object that violated constraint
     */
    public Object getViolatingObject() {
      return violatingObject;
    }

    @Override
    public String toString() {
      return String.format(
          "Constraint '%s' violated: %s (object: %s)", constraintName, message, violatingObject);
    }
  }
}