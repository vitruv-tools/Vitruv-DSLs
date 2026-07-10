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
package tools.vitruv.dsls.vitruvocl.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ErrorCollector}. */
class ErrorCollectorTest {

  /** Tests that new ErrorCollector has no errors. */
  @Test
  void testEmptyCollector() {
    ErrorCollector collector = new ErrorCollector();
    assertFalse(collector.hasErrors());
    assertEquals(0, collector.getErrorCount());
    assertTrue(collector.getErrors().isEmpty());
  }

  /** Tests adding an ERROR-level error. */
  @Test
  void testAddError() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(1, 0, "Test error", ErrorSeverity.ERROR, "test");
    assertTrue(collector.hasErrors());
    assertEquals(1, collector.getErrorCount());
    assertEquals(1, collector.getErrors().size());
  }

  /** Tests that WARNING does not count as error in hasErrors(). */
  @Test
  void testWarningNotCountedAsError() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(1, 0, "Warning", ErrorSeverity.WARNING, "test");
    assertFalse(collector.hasErrors());
    assertEquals(0, collector.getErrorCount());
    assertEquals(1, collector.getErrors().size());
  }

  /** Tests adding multiple errors of different severities. */
  @Test
  void testMultipleErrors() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(1, 0, "Error 1", ErrorSeverity.ERROR, "test");
    collector.add(2, 5, "Error 2", ErrorSeverity.ERROR, "test");
    collector.add(3, 2, "Warning", ErrorSeverity.WARNING, "test");
    assertTrue(collector.hasErrors());
    assertEquals(2, collector.getErrorCount());
    assertEquals(3, collector.getErrors().size());
  }

  /** Tests adding a pre-constructed CompileError. */
  @Test
  void testAddCompileError() {
    ErrorCollector collector = new ErrorCollector();
    CompileError error = new CompileError(5, 3, "Direct error", ErrorSeverity.ERROR, "evaluator");
    collector.add(error);
    assertTrue(collector.hasErrors());
    assertEquals(1, collector.getErrorCount());
    assertEquals("Direct error", collector.getErrors().get(0).getMessage());
  }

  /** Tests that getErrors() returns a defensive copy. */
  @Test
  void testDefensiveCopy() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(1, 0, "Error", ErrorSeverity.ERROR, "test");
    List<CompileError> copy1 = collector.getErrors();
    List<CompileError> copy2 = collector.getErrors();
    assertNotSame(copy1, copy2);
    assertEquals(copy1.size(), copy2.size());
  }

  /** Tests that modifying returned list does not affect collector. */
  @Test
  void testReturnedListIsImmutable() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(1, 0, "Error", ErrorSeverity.ERROR, "test");
    collector.getErrors().clear();
    assertEquals(
        1, collector.getErrors().size(), "Clearing returned list should not affect collector");
  }

  /** Tests error content is preserved correctly. */
  @Test
  void testErrorContentPreserved() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(7, 12, "Specific message", ErrorSeverity.WARNING, "symbol-table");
    CompileError stored = collector.getErrors().get(0);
    assertEquals(7, stored.getLine());
    assertEquals(12, stored.getColumn());
    assertEquals("Specific message", stored.getMessage());
    assertEquals(ErrorSeverity.WARNING, stored.getSeverity());
    assertEquals("symbol-table", stored.getSource());
  }

  /** Tests getErrorCount with mixed severities. */
  @Test
  void testErrorCountOnlyCountsErrors() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(1, 0, "E1", ErrorSeverity.ERROR, "t");
    collector.add(2, 0, "W1", ErrorSeverity.WARNING, "t");
    collector.add(3, 0, "E2", ErrorSeverity.ERROR, "t");
    collector.add(4, 0, "W2", ErrorSeverity.WARNING, "t");
    assertEquals(2, collector.getErrorCount());
    assertEquals(4, collector.getErrors().size());
  }

  /** Tests that only warnings leaves hasErrors false. */
  @Test
  void testOnlyWarnings() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(1, 0, "W1", ErrorSeverity.WARNING, "t");
    collector.add(2, 0, "W2", ErrorSeverity.WARNING, "t");
    assertFalse(collector.hasErrors());
    assertEquals(0, collector.getErrorCount());
    assertEquals(2, collector.getErrors().size());
  }

  /** Tests error order is preserved (insertion order). */
  @Test
  void testErrorOrderPreserved() {
    ErrorCollector collector = new ErrorCollector();
    collector.add(3, 0, "Third", ErrorSeverity.ERROR, "t");
    collector.add(1, 0, "First", ErrorSeverity.ERROR, "t");
    collector.add(2, 0, "Second", ErrorSeverity.ERROR, "t");
    List<CompileError> errors = collector.getErrors();
    assertEquals("Third", errors.get(0).getMessage());
    assertEquals("First", errors.get(1).getMessage());
    assertEquals("Second", errors.get(2).getMessage());
  }
}
