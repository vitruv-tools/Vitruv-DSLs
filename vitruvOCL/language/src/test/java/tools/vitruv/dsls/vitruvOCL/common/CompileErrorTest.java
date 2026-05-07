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
package tools.vitruv.dsls.vitruvOCL.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link CompileError}. */
public class CompileErrorTest {

  /** Tests basic constructor with line, column, message, severity, source. */
  @Test
  public void testBasicConstructor() {
    CompileError error = new CompileError(1, 5, "Test error", ErrorSeverity.ERROR, "type-checker");
    assertEquals(1, error.getLine());
    assertEquals(5, error.getColumn());
    assertEquals("Test error", error.getMessage());
    assertEquals(ErrorSeverity.ERROR, error.getSeverity());
    assertEquals("type-checker", error.getSource());
    assertNull(error.getErrorCode());
    assertEquals(-1, error.getEndLine());
    assertEquals(-1, error.getEndColumn());
  }

  /** Tests constructor with error code. */
  @Test
  public void testConstructorWithErrorCode() {
    CompileError error =
        new CompileError(
            2, 10, "Type mismatch", ErrorSeverity.ERROR, "type-checker", "TYPE_MISMATCH");
    assertEquals("TYPE_MISMATCH", error.getErrorCode());
    assertEquals(2, error.getLine());
    assertEquals(10, error.getColumn());
  }

  /** Tests full constructor with span information. */
  @Test
  public void testFullConstructorWithSpan() {
    CompileError error =
        new CompileError(3, 4, 3, 15, "Span error", ErrorSeverity.WARNING, "evaluator", "WARN_001");
    assertEquals(3, error.getLine());
    assertEquals(4, error.getColumn());
    assertEquals(3, error.getEndLine());
    assertEquals(15, error.getEndColumn());
    assertEquals("Span error", error.getMessage());
    assertEquals(ErrorSeverity.WARNING, error.getSeverity());
    assertEquals("evaluator", error.getSource());
    assertEquals("WARN_001", error.getErrorCode());
  }

  /** Tests that WARNING severity is stored correctly. */
  @Test
  public void testWarningSeverity() {
    CompileError warning =
        new CompileError(1, 0, "Potential issue", ErrorSeverity.WARNING, "type-checker");
    assertEquals(ErrorSeverity.WARNING, warning.getSeverity());
  }

  /** Tests that null error code is handled correctly. */
  @Test
  public void testNullErrorCode() {
    CompileError error = new CompileError(1, 0, 1, 5, "msg", ErrorSeverity.ERROR, "src", null);
    assertNull(error.getErrorCode());
  }

  /** Tests that end position -1 means unknown. */
  @Test
  public void testUnknownEndPosition() {
    CompileError error = new CompileError(5, 3, "msg", ErrorSeverity.ERROR, "src");
    assertEquals(-1, error.getEndLine());
    assertEquals(-1, error.getEndColumn());
  }

  /** Tests that line 0 is stored correctly. */
  @Test
  public void testLineZero() {
    CompileError error = new CompileError(0, 0, "msg", ErrorSeverity.ERROR, "src");
    assertEquals(0, error.getLine());
    assertEquals(0, error.getColumn());
  }

  /** Tests that source identifier is stored correctly. */
  @Test
  public void testSourceIdentifier() {
    CompileError error = new CompileError(1, 0, "msg", ErrorSeverity.ERROR, "symbol-table");
    assertEquals("symbol-table", error.getSource());
  }

  /** Tests multiline span where endLine differs from startLine. */
  @Test
  public void testMultilineSpan() {
    CompileError error =
        new CompileError(1, 0, 3, 10, "Multiline error", ErrorSeverity.ERROR, "src", null);
    assertEquals(1, error.getLine());
    assertEquals(3, error.getEndLine());
    assertEquals(10, error.getEndColumn());
  }
}