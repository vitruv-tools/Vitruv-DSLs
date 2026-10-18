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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.incremental.support.ConstraintCorpus;
import tools.vitruv.dsls.vitruvocl.incremental.support.LiveModel;
import tools.vitruv.dsls.vitruvocl.incremental.support.ModelSet;
import tools.vitruv.dsls.vitruvocl.pipeline.ConstraintEvaluations;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;

/**
 * Differential check of steps A3/A4 of {@code docs/correspondence-incremental-plan.md}: every
 * corpus constraint gives the same outcome (failure flag, errors and violation records in order)
 * with and without the correspondence fast paths, on every model set.
 */
class CorrespondenceFastPathEquivalenceTest {

  @AfterEach
  void resetProperty() {
    System.clearProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS);
  }

  private static String describe(List<CompileError> errors) {
    return errors.stream()
        .map(e -> e.getLine() + ":" + e.getColumn() + " " + e.getSeverity() + " " + e.getMessage())
        .toList()
        .toString();
  }

  static Stream<ModelSet> modelSets() {
    return ModelSet.all().stream();
  }

  @ParameterizedTest
  @MethodSource("modelSets")
  void fastPathsDoNotChangeAnyOutcome(ModelSet set) {
    LiveModel model = LiveModel.load(set);
    List<String> constraints = ConstraintCorpus.forModelSet(set);
    assertTrue(!constraints.isEmpty(), "no corpus constraints for " + set);
    StringBuilder diffs = new StringBuilder();
    for (String constraint : constraints) {
      System.clearProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS);
      EvaluationOutcome fast =
          ConstraintEvaluations.evaluateFresh(constraint, model.wrapper(), List.of(), false);
      System.setProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS, "true");
      EvaluationOutcome plain =
          ConstraintEvaluations.evaluateFresh(constraint, model.wrapper(), List.of(), false);
      if (fast.failed() != plain.failed()
          || !describe(fast.errors()).equals(describe(plain.errors()))
          || !fast.records().equals(plain.records())) {
        diffs.append("\n").append(constraint).append("\n  fast:  ").append(fast).append("\n  plain: ").append(plain);
      }
    }
    assertEquals("", diffs.toString(), "outcomes differ on " + set);
  }
}
