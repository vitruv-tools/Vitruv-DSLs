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
    /** Re-evaluate: the transaction contains a potentially satisfaction-violating event (level 2). */
    EVALUATE_PSE_HIT,
    /**
     * Skip (level 2): the read set is touched, but only by events that cannot turn a satisfied
     * constraint into a violated one, and the constraint currently has no violation.
     */
    SKIP_NO_PSE,
    /** Skip: nothing the constraint reads was touched by this transaction. */
    SKIP;

    /** Whether the constraint has to be evaluated. */
    public boolean evaluate() {
      return this != SKIP && this != SKIP_NO_PSE;
    }

    /** Whether the evaluation was forced independently of the transaction's content. */
    public boolean forced() {
      return this == EVALUATE_ALWAYS
          || this == EVALUATE_CORRESPONDENCE
          || this == EVALUATE_UNKNOWN_CHANGE;
    }
  }

  private final ReadSet readSet;
  private final PseSet pseSet;
  private final RelevancePlan relevancePlan;

  /** Analysis of a constraint that failed to compile: always re-evaluated. */
  public static ConstraintAnalysis forUncompilable() {
    return new ConstraintAnalysis(null, null, null);
  }

  public ConstraintAnalysis(ReadSet readSet, PseSet pseSet, RelevancePlan relevancePlan) {
    this.readSet = readSet;
    this.pseSet = pseSet;
    this.relevancePlan = relevancePlan;
  }

  /** The level-3 plan, or {@code null} for a constraint that failed to compile. */
  public RelevancePlan relevancePlan() {
    return relevancePlan;
  }

  /** The PSE set (level 2), or {@code null} for a constraint that failed to compile. */
  public PseSet pseSet() {
    return pseSet;
  }

  /**
   * Level-2 decision. Starts from the level-1 decision; a read-set hit is narrowed to the
   * potentially satisfaction-violating events (PSEs) only if the constraint currently has neither
   * violations nor a failed evaluation. Polarity only excludes "satisfied → violated"
   * transitions; with a current violation, an event in the "safe" direction may still repair it,
   * so the level-1 decision is kept. (Own adaptation; Cabot &amp; Teniente assume a consistent
   * state before every operation.)
   *
   * @param hasViolationsOrFailure whether the constraint currently has a violation or its last
   *     evaluation failed
   */
  public Decision decideByPolarity(TransactionDelta delta, boolean hasViolationsOrFailure) {
    return decideByPolarity(delta, hasViolationsOrFailure, false);
  }

  /** Level-2 decision with optional correspondence idiom rule (see {@link #decideByReadSet}). */
  public Decision decideByPolarity(
      TransactionDelta delta, boolean hasViolationsOrFailure, boolean correspondenceIdiom) {
    Decision levelOne = decideByReadSet(delta, correspondenceIdiom);
    if (levelOne != Decision.EVALUATE_READ_SET_HIT || pseSet == null || hasViolationsOrFailure) {
      return levelOne;
    }
    return pseSet.matches(delta) ? Decision.EVALUATE_PSE_HIT : Decision.SKIP_NO_PSE;
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
    return decideByReadSet(delta, false);
  }

  /**
   * Level-1 decision as above; with {@code correspondenceIdiom}, a constraint whose every use of
   * correspondence is the idiom {@code Y.allInstances()->select/exists(~ …)} is not forced, but
   * decided by its read set (rule 3c; unsound if correspondences change without model events).
   */
  public Decision decideByReadSet(TransactionDelta delta, boolean correspondenceIdiom) {
    if (readSet == null || readSet.requiresFullEvaluation()) {
      return Decision.EVALUATE_ALWAYS;
    }
    if (delta.isEmpty()) {
      return Decision.SKIP;
    }
    if (delta.hasUnknownChanges()) {
      return Decision.EVALUATE_UNKNOWN_CHANGE;
    }
    if (readSet.usesCorrespondence()
        && !(correspondenceIdiom && relevancePlan != null && relevancePlan.correspondenceOnlyInIdiom())) {
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
