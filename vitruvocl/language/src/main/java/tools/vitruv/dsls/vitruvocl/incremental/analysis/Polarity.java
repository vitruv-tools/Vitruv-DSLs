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

package tools.vitruv.dsls.vitruvocl.incremental.analysis;

/**
 * Annotation of a parse-tree node in the sense of Cabot &amp; Teniente 2009, §3.1: the direction
 * of change of the node's value that can turn a satisfied constraint into a violated one.
 *
 * <p>{@link #PLUS}: an increase (more elements, larger number) may violate. {@link #MINUS}: a
 * decrease may violate. {@link #BOTH}: either. For Boolean nodes, {@code false < true}; the
 * constraint root is therefore annotated {@link #MINUS} (it is violated when it becomes false).
 * The paper's {@code und} does not occur: a node whose own value carries no direction information
 * is annotated {@link #BOTH} (conservative).
 */
public enum Polarity {
  PLUS,
  MINUS,
  BOTH;

  /** The opposite direction (for {@code not}, the antecedent of {@code implies}, ...). */
  public Polarity flip() {
    return switch (this) {
      case PLUS -> MINUS;
      case MINUS -> PLUS;
      case BOTH -> BOTH;
    };
  }

  /** Least upper bound: {@code PLUS ⊔ MINUS = BOTH}. */
  public Polarity join(Polarity other) {
    return other == null || other == this ? this : BOTH;
  }

  /** Whether an increase of the node's value may violate the constraint. */
  public boolean increaseMayViolate() {
    return this != MINUS;
  }

  /** Whether a decrease of the node's value may violate the constraint. */
  public boolean decreaseMayViolate() {
    return this != PLUS;
  }
}
