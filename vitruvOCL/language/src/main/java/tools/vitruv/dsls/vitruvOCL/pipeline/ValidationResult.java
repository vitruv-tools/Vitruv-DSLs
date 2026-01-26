package tools.vitruv.dsls.vitruvOCL.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tools.vitruv.dsls.vitruvOCL.common.CompileError;

/** Result of OCL constraint validation. */
public class ValidationResult {

  private final List<CompileError> errors;
  private final List<ConstraintViolation> violations;

  public ValidationResult(List<CompileError> errors, List<ConstraintViolation> violations) {
    this.errors = new ArrayList<>(errors);
    this.violations = new ArrayList<>(violations);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public boolean hasViolations() {
    return !violations.isEmpty();
  }

  public List<CompileError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public List<ConstraintViolation> getViolations() {
    return Collections.unmodifiableList(violations);
  }

  public static class ConstraintViolation {
    private final String constraintName;
    private final String message;
    private final Object violatingObject;

    public ConstraintViolation(String constraintName, String message, Object violatingObject) {
      this.constraintName = constraintName;
      this.message = message;
      this.violatingObject = violatingObject;
    }

    public String getConstraintName() {
      return constraintName;
    }

    public String getMessage() {
      return message;
    }

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