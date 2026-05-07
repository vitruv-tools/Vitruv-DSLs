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

/** Unit tests for {@link ErrorSeverity}. */
public class ErrorSeverityTest {

  /** Tests that ERROR and WARNING are distinct values. */
  @Test
  public void testDistinctValues() {
    assertNotEquals(ErrorSeverity.ERROR, ErrorSeverity.WARNING);
  }

  /** Tests enum names. */
  @Test
  public void testEnumNames() {
    assertEquals("ERROR", ErrorSeverity.ERROR.name());
    assertEquals("WARNING", ErrorSeverity.WARNING.name());
  }

  /** Tests enum identity (same reference). */
  @Test
  public void testEnumIdentity() {
    assertSame(ErrorSeverity.ERROR, ErrorSeverity.ERROR);
    assertSame(ErrorSeverity.WARNING, ErrorSeverity.WARNING);
  }

  /** Tests valueOf works correctly. */
  @Test
  public void testValueOf() {
    assertEquals(ErrorSeverity.ERROR, ErrorSeverity.valueOf("ERROR"));
    assertEquals(ErrorSeverity.WARNING, ErrorSeverity.valueOf("WARNING"));
  }

  /** Tests values() returns both severities. */
  @Test
  public void testValues() {
    ErrorSeverity[] values = ErrorSeverity.values();
    assertEquals(2, values.length);
  }

  /** Tests ordinal ordering. */
  @Test
  public void testOrdinal() {
    assertEquals(0, ErrorSeverity.ERROR.ordinal());
    assertEquals(1, ErrorSeverity.WARNING.ordinal());
  }
}