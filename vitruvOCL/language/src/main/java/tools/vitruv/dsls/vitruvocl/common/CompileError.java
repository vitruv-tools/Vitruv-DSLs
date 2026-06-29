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
package tools.vitruv.dsls.vitruvocl.common;

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

  /** End line of the erroneous range (1-based); -1 when unknown. */
  private final int endLine;

  /** Exclusive end column of the erroneous range (0-based); -1 when unknown. */
  private final int endColumn;

  /** Human-readable error description */
  private final String message;

  /** Severity level (ERROR, WARNING, INFO) */
  private final ErrorSeverity severity;

  /** Source file or context identifier where error occurred */
  private final String source;

  /** Optional error code for programmatic error categorization */
  private final String errorCode;

  /**
   * Optional replacement text for a "did you mean?" quick fix.
   *
   * <p>When non-null, the LSP server will offer a {@code QuickFix} code action that replaces the
   * erroneous token range with this string.
   */
  private final String suggestion;

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
    this(line, column, -1, -1, message, severity, source, null);
  }

  public CompileError(
      int line,
      int column,
      String message,
      ErrorSeverity severity,
      String source,
      String errorCode) {
    this(line, column, -1, -1, message, severity, source, errorCode);
  }

  /**
   * Creates a compile error with full span (start + end position) and optional error code.
   *
   * @param line Start line (1-based)
   * @param column Start column (0-based)
   * @param endLine End line (1-based); pass -1 when unknown
   * @param endColumn Exclusive end column (0-based); pass -1 when unknown
   * @param message Error description
   * @param severity Error severity level
   * @param source Source file or context identifier
   * @param errorCode Optional error code (may be null)
   */
  @SuppressWarnings("java:S107")
  public CompileError(
      int line,
      int column,
      int endLine,
      int endColumn,
      String message,
      ErrorSeverity severity,
      String source,
      String errorCode) {
    this(line, column, endLine, endColumn, message, severity, source, errorCode, null);
  }

  /**
   * Creates a compile error with a full span, error code, and quick-fix suggestion.
   *
   * @param suggestion Replacement text for a "did you mean?" quick fix, or {@code null}
   */
  @SuppressWarnings("java:S107")
  public CompileError(
      int line,
      int column,
      int endLine,
      int endColumn,
      String message,
      ErrorSeverity severity,
      String source,
      String errorCode,
      String suggestion) {
    this.line = line;
    this.column = column;
    this.endLine = endLine;
    this.endColumn = endColumn;
    this.message = message;
    this.severity = severity;
    this.source = source;
    this.errorCode = errorCode;
    this.suggestion = suggestion;
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

  /** End line of the erroneous range (1-based); -1 when not set. */
  public int getEndLine() {
    return endLine;
  }

  /** Exclusive end column of the erroneous range (0-based); -1 when not set. */
  public int getEndColumn() {
    return endColumn;
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

  /**
   * Returns the quick-fix replacement text, or {@code null} if no suggestion is available.
   *
   * <p>When non-null the LSP server will offer a blue-highlighted "Replace with '...'" code action
   * on the squiggled range so the user can apply the fix with a single click.
   */
  public String getSuggestion() {
    return suggestion;
  }
}
