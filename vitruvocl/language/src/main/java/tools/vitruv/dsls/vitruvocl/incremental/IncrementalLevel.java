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

package tools.vitruv.dsls.vitruvocl.incremental;

/**
 * How much of the incremental machinery an {@link IncrementalSession} uses after each transaction.
 * Every level produces the same violation set as {@link #FULL}; higher levels only re-evaluate
 * less.
 */
public enum IncrementalLevel {

  /** Level 0: full evaluation of every constraint after every transaction (baseline). */
  FULL(0),

  /**
   * Level 1: per-constraint read-set filter without event direction — a constraint is skipped if
   * no change of the transaction touches anything it reads.
   */
  READ_SET(1),

  /**
   * Level 2: additionally filters by event polarity (potentially satisfaction-violating events,
   * Cabot &amp; Teniente 2009, §3.1).
   */
  POLARITY(2),

  /**
   * Level 3: additionally restricts re-evaluation to the relevant context instances (Cabot &amp;
   * Teniente 2009, §3.2); falls back to the full extension where that is not possible.
   */
  RELEVANT_INSTANCES(3);

  private final int number;

  IncrementalLevel(int number) {
    this.number = number;
  }

  /** The numeric level 0–3, as used on the command line and in system properties. */
  public int number() {
    return number;
  }

  /** Whether this level includes everything {@code other} does. */
  public boolean atLeast(IncrementalLevel other) {
    return number >= other.number;
  }

  /**
   * Parses a numeric level ({@code "0"}–{@code "3"}) or a constant name.
   *
   * @throws IllegalArgumentException for anything else
   */
  public static IncrementalLevel parse(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Incremental level must not be null");
    }
    String trimmed = value.trim();
    for (IncrementalLevel level : values()) {
      if (trimmed.equals(Integer.toString(level.number)) || trimmed.equalsIgnoreCase(level.name())) {
        return level;
      }
    }
    throw new IllegalArgumentException(
        "Invalid incremental level '" + value + "' (expected 0, 1, 2 or 3)");
  }
}
