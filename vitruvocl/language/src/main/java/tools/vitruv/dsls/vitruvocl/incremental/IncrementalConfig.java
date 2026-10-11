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
 * Configuration of an {@link IncrementalSession}.
 *
 * <p>All settings can also be supplied from outside (for example by a separate benchmarking
 * project) through system properties, see {@link #fromSystemProperties()}.
 *
 * @param level which incremental level to use
 * @param verify oracle mode: after every incremental evaluation, run a full evaluation of the same
 *     state and throw {@link IncrementalMismatchException} if the violation sets differ
 * @param correspondenceIdiom enables the idiom rule for {@code Y.allInstances()->select(~)} (level
 *     3). Off by default because correspondences can change without any observed model event (see
 *     {@code CorrespondenceVisibilityExperimentTest}).
 * @param threads number of worker threads for evaluating the constraints selected after a
 *     transaction; {@code 1} evaluates sequentially
 */
public record IncrementalConfig(
    IncrementalLevel level, boolean verify, boolean correspondenceIdiom, int threads) {

  /** System property selecting the level ({@code 0}–{@code 3}); default {@code 0}. */
  public static final String PROP_LEVEL = "vitruvocl.incremental.level";

  /** System property enabling the oracle mode ({@code true}/{@code false}); default off. */
  public static final String PROP_VERIFY = "vitruvocl.incremental.verify";

  /** System property enabling the correspondence idiom rule; default off. */
  public static final String PROP_CORRESPONDENCE_IDIOM = "vitruvocl.incremental.corrIdiom";

  /** System property for the number of worker threads; default {@code 1}. */
  public static final String PROP_THREADS = "vitruvocl.incremental.threads";

  public IncrementalConfig {
    if (level == null) {
      throw new IllegalArgumentException("level must not be null");
    }
    if (threads < 1) {
      throw new IllegalArgumentException("threads must be at least 1, was " + threads);
    }
  }

  /** Configuration for {@code level} with oracle off, idiom rule off and sequential evaluation. */
  public static IncrementalConfig of(IncrementalLevel level) {
    return new IncrementalConfig(level, false, false, 1);
  }

  /** Returns a copy with the oracle mode switched to {@code enabled}. */
  public IncrementalConfig withVerify(boolean enabled) {
    return new IncrementalConfig(level, enabled, correspondenceIdiom, threads);
  }

  /** Returns a copy with the given level. */
  public IncrementalConfig withLevel(IncrementalLevel newLevel) {
    return new IncrementalConfig(newLevel, verify, correspondenceIdiom, threads);
  }

  /** Returns a copy with the correspondence idiom rule switched to {@code enabled}. */
  public IncrementalConfig withCorrespondenceIdiom(boolean enabled) {
    return new IncrementalConfig(level, verify, enabled, threads);
  }

  /** Returns a copy with {@code count} worker threads. */
  public IncrementalConfig withThreads(int count) {
    return new IncrementalConfig(level, verify, correspondenceIdiom, count);
  }

  /**
   * Reads the configuration from the system properties {@value #PROP_LEVEL}, {@value
   * #PROP_VERIFY}, {@value #PROP_CORRESPONDENCE_IDIOM} and {@value #PROP_THREADS}; unset properties
   * take their defaults (level 0, no oracle, no idiom rule, one thread).
   *
   * @throws IllegalArgumentException if a property is set to an invalid value
   */
  public static IncrementalConfig fromSystemProperties() {
    String levelValue = System.getProperty(PROP_LEVEL);
    IncrementalLevel level =
        levelValue == null ? IncrementalLevel.FULL : IncrementalLevel.parse(levelValue);
    boolean verify = Boolean.parseBoolean(System.getProperty(PROP_VERIFY, "false"));
    boolean idiom = Boolean.parseBoolean(System.getProperty(PROP_CORRESPONDENCE_IDIOM, "false"));
    String threadsValue = System.getProperty(PROP_THREADS, "1");
    int threads;
    try {
      threads = Integer.parseInt(threadsValue.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid value for " + PROP_THREADS + ": '" + threadsValue + "'", e);
    }
    return new IncrementalConfig(level, verify, idiom, threads);
  }
}
