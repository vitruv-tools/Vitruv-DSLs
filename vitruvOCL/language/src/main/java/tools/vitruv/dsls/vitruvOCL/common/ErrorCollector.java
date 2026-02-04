package tools.vitruv.dsls.vitruvOCL.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects compilation errors across all compiler passes without halting execution.
 *
 * <p>Enables gathering multiple errors in a single compilation run rather than failing at the first
 * error. Supports severity levels (ERROR, WARNING, INFO) to distinguish between fatal issues and
 * informational messages.
 *
 * <p>Used throughout the 3-pass compilation pipeline:
 *
 * <ul>
 *   <li>Pass 1 (Symbol Table Construction): Undefined variable errors
 *   <li>Pass 2 (Type Checking): Type mismatches, invalid operations
 *   <li>Pass 3 (Evaluation): Runtime errors when enabled
 * </ul>
 *
 * <p>This approach improves developer experience by reporting all issues at once rather than
 * requiring iterative fixing of individual errors.
 *
 * @see CompileError for individual error representation with position and severity
 */
public class ErrorCollector {

  /** All collected errors, warnings, and informational messages */
  private final List<CompileError> errors = new ArrayList<>();

  /**
   * Adds an error with explicit position and severity information.
   *
   * @param line Line number where error occurred (1-based)
   * @param column Column number where error occurred (0-based)
   * @param message Human-readable error description
   * @param severity Error severity level
   * @param source Source file or context identifier
   */
  public void add(int line, int column, String message, ErrorSeverity severity, String source) {
    errors.add(new CompileError(line, column, message, severity, source));
  }

  /**
   * Adds a pre-constructed error object.
   *
   * @param error Complete error instance to add
   */
  public void add(CompileError error) {
    errors.add(error);
  }

  /**
   * Checks if any ERROR-level issues were collected.
   *
   * <p>Warnings and informational messages do not count as errors. Used to determine if compilation
   * should proceed to next phase.
   *
   * @return {@code true} if at least one ERROR was recorded, {@code false} otherwise
   */
  public boolean hasErrors() {
    return errors.stream().anyMatch(e -> e.getSeverity() == ErrorSeverity.ERROR);
  }

  /**
   * Returns all collected errors, warnings, and messages.
   *
   * @return Defensive copy of all collected issues
   */
  public List<CompileError> getErrors() {
    return new ArrayList<>(errors);
  }

  /**
   * Counts ERROR-level issues only.
   *
   * @return Number of ERROR-severity issues (excludes warnings and info messages)
   */
  public int getErrorCount() {
    return (int) errors.stream().filter(e -> e.getSeverity() == ErrorSeverity.ERROR).count();
  }
}