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

package tools.vitruv.dsls.vitruvocl.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.incremental.support.EdgeFixture;
import tools.vitruv.dsls.vitruvocl.pipeline.VitruvOCLCompiler;

/**
 * Step A4 of {@code docs/correspondence-incremental-plan.md}: in {@code G and R} and {@code G
 * implies R} with a correspondence guard {@code G}, the right side {@code R} is not evaluated when
 * {@code G} is false, if {@code R} cannot report a runtime error (the only observable effect of
 * evaluating it). The result is unchanged in every case.
 */
class CorrespondenceFastPathTest {

  @AfterEach
  void resetProperty() {
    System.clearProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS);
  }

  private static EvaluationVisitor evaluate(EdgeFixture fx, String constraint) {
    VitruvOCLCompiler compiler = new VitruvOCLCompiler(fx.model().wrapper(), null);
    compiler.compile(constraint);
    assertFalse(compiler.hasErrors(), () -> "unexpected errors: " + compiler.getErrors().getErrors());
    return compiler.getLastEvaluator();
  }

  private static EdgeFixture correspondingA1AndX() {
    EdgeFixture fx = EdgeFixture.create();
    fx.model()
        .correspondences()
        .addCorrespondenceBetween(List.of(fx.find("a1")), List.of(fx.find("x")), null);
    return fx;
  }

  @Test
  void impliesSkipsTheConsequentWhenTheGuardIsFalse() {
    EvaluationVisitor ev =
        evaluate(
            correspondingA1AndX(),
            "context edge::SubA inv: edge::Other.allInstances().forAll(o | self ~ o implies o.amount > 0)");
    // 2 SubA × 2 Other, only (a1, x) corresponds
    assertEquals(3, ev.getCorrespondenceShortCircuits());
    assertTrue(ev.getViolationRecords().isEmpty());
  }

  @Test
  void andSkipsTheRightSideWhenTheGuardIsFalse() {
    EvaluationVisitor ev =
        evaluate(
            correspondingA1AndX(),
            "context edge::SubA inv: edge::Other.allInstances().exists(o | o ~ self and o.amount > 5)");
    assertEquals(3, ev.getCorrespondenceShortCircuits());
    assertEquals(2, ev.getViolationRecords().size(), "no Other has amount > 5");
  }

  @Test
  void rightSideThatMayReportAnErrorIsAlwaysEvaluated() {
    // i.refs is a collection: == on it can report "Comparison requires singleton operands"
    String c =
        "context edge::Other inv: edge::Item.allInstances().forAll(i | self ~ i implies i.refs == i.refs)";
    VitruvOCLCompiler compiler = new VitruvOCLCompiler(correspondingA1AndX().model().wrapper(), null);
    compiler.compile(c);
    assertEquals(0, compiler.getLastEvaluator().getCorrespondenceShortCircuits());
    System.setProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS, "true");
    VitruvOCLCompiler reference = new VitruvOCLCompiler(correspondingA1AndX().model().wrapper(), null);
    reference.compile(c);
    assertEquals(
        describe(reference.getErrors().getErrors()),
        describe(compiler.getErrors().getErrors()),
        "errors must not depend on the fast path");
    assertTrue(compiler.hasErrors(), "the comparison on a collection must report its error");
  }

  static String describe(List<CompileError> errors) {
    return errors.stream()
        .map(e -> e.getLine() + ":" + e.getColumn() + " " + e.getSeverity() + " " + e.getMessage())
        .toList()
        .toString();
  }

  /**
   * Outcome of one evaluation as text: errors (or the escaping exception) and violation count, so
   * that fast and reference runs can be compared.
   */
  private static String outcome(String constraint, boolean fast) {
    if (fast) {
      System.clearProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS);
    } else {
      System.setProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS, "true");
    }
    try {
      VitruvOCLCompiler compiler = new VitruvOCLCompiler(correspondingA1AndX().model().wrapper(), null);
      Object result = compiler.compile(constraint);
      EvaluationVisitor ev = compiler.getLastEvaluator();
      return describe(compiler.getErrors().getErrors())
          + " result=" + result
          + " violations=" + (ev == null ? "-" : ev.getViolationRecords().size());
    } catch (RuntimeException e) {
      return "exception " + e.getClass().getName();
    } finally {
      System.clearProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS);
    }
  }

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(
      strings = {
        // a let typed OclAny holds a collection: the comparison reports an error (review H1)
        "context edge::SubA inv: edge::Other.allInstances()"
            + ".forAll(o | self ~ o implies (let x : OclAny = Set{1,2} in x == 1))",
        "context edge::SubA inv: edge::Other.allInstances()"
            + ".forAll(o | self ~ o implies (let x : OclAny = edge::Other.allInstances() in x == o))",
        // an outer lazy let is forced inside the skipped side; its comparison errors (review H2)
        "context edge::SubA inv: let xs = edge::Item.allInstances().select(i | i.refs == i) in"
            + " edge::Other.allInstances().forAll(o | self ~ o implies xs.size() == 0)",
        // an integer literal that does not fit an int throws (review M1); o ~ o is always false
        "context edge::SubA inv: edge::Other.allInstances().forAll(o | o ~ o implies o.amount > 99999999999)"
      })
  void skippingNeverChangesErrorsOrExceptions(String constraint) {
    assertEquals(outcome(constraint, false), outcome(constraint, true), constraint);
  }

  @Test
  void fastPathCanBeSwitchedOff() {
    System.setProperty(EvaluationVisitor.PROP_NO_CORRESPONDENCE_FAST_PATHS, "true");
    EvaluationVisitor ev =
        evaluate(
            correspondingA1AndX(),
            "context edge::SubA inv: edge::Other.allInstances().forAll(o | self ~ o implies o.amount > 0)");
    assertEquals(0, ev.getCorrespondenceShortCircuits());
  }
}
