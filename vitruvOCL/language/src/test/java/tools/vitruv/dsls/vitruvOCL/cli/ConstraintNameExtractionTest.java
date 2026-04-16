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
package tools.vitruv.dsls.vitruvOCL.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for the CLI constraint name extraction logic.
 *
 * <p>Covers named and unnamed invariants, as well as edge cases like whitespace variants.
 */
public class ConstraintNameExtractionTest {

  /**
   * Named constraint: "inv overlapping:" should extract "overlapping". This is the baseline case —
   * should already work.
   */
  @Test
  public void testNamedInvariantIsExtracted() {
    String constraint =
        "context brakesystem::BrakeDisk inv overlapping:\n"
            + "  cadCaliper.parameters.forAll(p | p.oclAsType(cad::Coordinate).x >= 500)";

    assertEquals(
        "overlapping",
        VitruvOCLCLI.extractConstraintName(constraint),
        "Named invariant 'overlapping' should be extracted correctly");
  }

  /**
   * Reproduces the bug reported by Arne/Nathan: "inv:" without a name returns "unknown" because the
   * regex only matches " inv " (with trailing space), not " inv:".
   *
   * <p>This is the verbatim constraint structure from overlapping.ocl.
   */
  @Test
  public void testUnnamedInvariantReturnsUnknown() {
    String constraint =
        "context brakesystem::BrakeDisk inv:\n"
            + "  cadCaliper.parameters.forAll(p | p.oclAsType(cad::Coordinate).x >= 500)";

    // Bug: " inv:" does not match " inv " so the method falls through to "unknown"
    // This test documents the current (buggy) behavior and will drive the fix.
    assertEquals(
        "unknown",
        VitruvOCLCLI.extractConstraintName(constraint),
        "Unnamed invariant should return 'unknown' as fallback");
  }

  /**
   * Named constraint with extra whitespace before colon: "inv myName :" should still extract
   * "myName".
   */
  @Test
  public void testNamedInvariantWithWhitespaceBeforeColon() {
    String constraint = "context brakesystem::BrakeDisk inv myName :\n" + "  true";

    assertEquals(
        "myName",
        VitruvOCLCLI.extractConstraintName(constraint),
        "Named invariant with whitespace before colon should be extracted correctly");
  }

  /** Named constraint on a single line (no newline after colon). */
  @Test
  public void testNamedInvariantSingleLine() {
    String constraint = "context brakesystem::BrakeDisk inv singleLine: true";

    assertEquals(
        "singleLine",
        VitruvOCLCLI.extractConstraintName(constraint),
        "Named invariant on single line should be extracted correctly");
  }

  /** Unnamed constraint on a single line. */
  @Test
  public void testUnnamedInvariantSingleLine() {
    String constraint = "context brakesystem::BrakeDisk inv: true";

    assertEquals(
        "unknown",
        VitruvOCLCLI.extractConstraintName(constraint),
        "Unnamed single-line invariant should return 'unknown'");
  }
}