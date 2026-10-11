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

import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import tools.vitruv.dsls.vitruvocl.incremental.TransactionDelta;

/**
 * The static, transaction-independent analysis of one constraint text, computed once and cached by
 * the incremental session. Matching it against a {@link TransactionDelta} decides whether the
 * constraint must be re-evaluated.
 *
 * <p>A "skip" decision means only: no change in <em>this observed transaction</em> touches anything
 * the constraint reads. It is not a statement that the consistency-preservation rules can never
 * violate the constraint.
 */
public final class ConstraintAnalysis {

  /** Outcome of matching the analysis against one transaction. */
  public enum Decision {
    /** Re-evaluate: the constraint depends on the transaction itself or could not be analysed. */
    EVALUATE_ALWAYS,
    /** Re-evaluate: uses correspondences, which can change without an observed model event. */
    EVALUATE_CORRESPONDENCE,
    /** Re-evaluate: the transaction contains a change of an unknown kind. */
    EVALUATE_UNKNOWN_CHANGE,
    /** Re-evaluate: the transaction touches the read set. */
    EVALUATE_READ_SET_HIT,
    /** Skip: nothing the constraint reads was touched by this transaction. */
    SKIP;

    /** Whether the constraint has to be evaluated. */
    public boolean evaluate() {
      return this != SKIP;
    }
  }

  private final ReadSet readSet;

  /** Analysis of a constraint that failed to compile: always re-evaluated. */
  public static ConstraintAnalysis forUncompilable() {
    return new ConstraintAnalysis(null);
  }

  public ConstraintAnalysis(ReadSet readSet) {
    this.readSet = readSet;
  }

  /** The read set, or {@code null} for a constraint that failed to compile. */
  public ReadSet readSet() {
    return readSet;
  }

  /**
   * Level-1 decision: evaluate iff the constraint depends on the transaction (pre/post, {@code
   * @pre}, lifecycle predicates, error nodes, failed compilation), uses correspondences and the
   * transaction is non-empty, the transaction contains an unclassifiable change, or some change
   * touches a (feature, class) pair or an extension type the constraint reads.
   */
  public Decision decideByReadSet(TransactionDelta delta) {
    if (readSet == null || readSet.requiresFullEvaluation()) {
      return Decision.EVALUATE_ALWAYS;
    }
    if (delta.isEmpty()) {
      return Decision.SKIP;
    }
    if (delta.hasUnknownChanges()) {
      return Decision.EVALUATE_UNKNOWN_CHANGE;
    }
    if (readSet.usesCorrespondence()) {
      return Decision.EVALUATE_CORRESPONDENCE;
    }
    return touchesReadSet(delta) ? Decision.EVALUATE_READ_SET_HIT : Decision.SKIP;
  }

  private boolean touchesReadSet(TransactionDelta delta) {
    for (String name : readSet.untypedFeatureReads()) {
      if (delta.touchedFeatures().containsKey(name)) {
        return true;
      }
    }
    for (Map.Entry<String, Set<EClass>> read : readSet.typedFeatureReads().entrySet()) {
      if (delta.touchesFeature(read.getKey(), read.getValue())) {
        return true;
      }
    }
    if (!delta.structurallyAffectedClasses().isEmpty() && readSet.unknownExtension()) {
      return true;
    }
    for (EClass type : readSet.extensionTypes()) {
      if (delta.affectsExtensionOf(type)) {
        return true;
      }
    }
    return false;
  }
}
