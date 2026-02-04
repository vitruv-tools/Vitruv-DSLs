package tools.vitruv.dsls.vitruvOCL.common;

/**
 * Immutable value object representing a compilation or runtime error with location information.
 *
 * <p>Captures complete error context including source position (line/column), descriptive message,
 * severity level, source identifier, and optional error code for categorization. Used throughout
 * the compilation pipeline to collect and report errors from parsing, type checking, and evaluation
 * phases.
 *
 * <p>Instances are immutable and thread-safe, suitable for collection in {@link ErrorCollector} and
 * propagation across compilation phases.
 */
public class CompileError {

  /** Line number where the error occurred (1-based) */
  private final int line;

  /** Column number where the error occurred (0-based) */
  private final int column;

  /** Human-readable error description */
  private final String message;

  /** Severity level (ERROR, WARNING, INFO) */
  private final ErrorSeverity severity;

  /** Source file or context identifier where error occurred */
  private final String source;

  /** Optional error code for programmatic error categorization */
  private final String errorCode;

  /**
   * Creates a compile error without an error code.
   *
   * @param line Line number (1-based)
   * @param column Column number (0-based)
   * @param message Error description
   * @param severity Error severity level
   * @param source Source file or context identifier
   */
  public CompileError(int line, int column, String message, ErrorSeverity severity, String source) {
    this(line, column, message, severity, source, null);
  }

  /**
   * Creates a compile error with full details including error code.
   *
   * @param line Line number (1-based)
   * @param column Column number (0-based)
   * @param message Error description
   * @param severity Error severity level
   * @param source Source file or context identifier
   * @param errorCode Optional error code for categorization (may be null)
   */
  public CompileError(
      int line,
      int column,
      String message,
      ErrorSeverity severity,
      String source,
      String errorCode) {
    this.line = line;
    this.column = column;
    this.message = message;
    this.severity = severity;
    this.source = source;
    this.errorCode = errorCode;
  }

  /**
   * @return Line number where error occurred (1-based)
   */
  public int getLine() {
    return line;
  }

  /**
   * @return Column number where error occurred (0-based)
   */
  public int getColumn() {
    return column;
  }

  /**
   * @return Human-readable error message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @return Error severity level
   */
  public ErrorSeverity getSeverity() {
    return severity;
  }

  /**
   * @return Source file or context identifier
   */
  public String getSource() {
    return source;
  }

  /**
   * @return Optional error code, or null if not specified
   */
  public String getErrorCode() {
    return errorCode;
  }
}