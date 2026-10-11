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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.vitruvocl.common.CompileError;
import tools.vitruv.dsls.vitruvocl.evaluator.EvaluationVisitor;
import tools.vitruv.dsls.vitruvocl.pipeline.EvaluationOutcome;

/**
 * Oracle of the incremental evaluator: compares an incrementally maintained outcome with the
 * outcome of a from-scratch evaluation of the same model state.
 *
 * <p>Compared, as multisets: violations by (instance identity, severity, message), skipped pre/post
 * blocks, and — for failed evaluations — the error messages. If the incremental outcome is
 * expected to preserve the evaluation order (no instance-subset update), the order of the
 * violations is compared as well.
 */
public final class OracleVerifier {

  private OracleVerifier() {}

  private record ViolationKey(ViolationSet.InstanceKey instance, String severity, String message) {}

  /**
   * Returns a human-readable diff, or {@code null} if both outcomes are equivalent.
   *
   * @param constraint the constraint text, used only in the diff message
   * @param ordered whether the order of the violation records must match as well
   */
  public static String diff(
      String constraint, EvaluationOutcome incremental, EvaluationOutcome full, boolean ordered) {
    StringBuilder diff = new StringBuilder();
    if (incremental.failed() != full.failed()) {
      diff.append("  failed: incremental=")
          .append(incremental.failed())
          .append(", full=")
          .append(full.failed())
          .append(" (full errors: ")
          .append(messages(full.errors()))
          .append(", incremental errors: ")
          .append(messages(incremental.errors()))
          .append(")\n");
    } else if (full.failed()) {
      compareMultisets(
          "error", incremental.errors(), full.errors(), CompileError::getMessage, diff);
    } else {
      compareMultisets(
          "violation",
          incremental.records(),
          full.records(),
          r -> new ViolationKey(new ViolationSet.InstanceKey(r.instance()), r.severity(), r.customMessage()),
          diff);
      compareMultisets(
          "skipped block",
          incremental.skipped(),
          full.skipped(),
          Function.identity(),
          diff);
      if (ordered && diff.isEmpty() && !sameOrder(incremental.records(), full.records())) {
        diff.append("  violations are reported in a different order than by full evaluation\n");
      }
    }
    if (diff.isEmpty()) {
      return null;
    }
    return "Constraint: " + firstLine(constraint) + "\n" + diff;
  }

  private static <T, K> void compareMultisets(
      String what, List<T> incremental, List<T> full, Function<T, K> key, StringBuilder diff) {
    Map<K, Integer> counts = new HashMap<>();
    Map<K, T> examples = new HashMap<>();
    for (T t : full) {
      K k = key.apply(t);
      counts.merge(k, 1, Integer::sum);
      examples.putIfAbsent(k, t);
    }
    for (T t : incremental) {
      K k = key.apply(t);
      counts.merge(k, -1, Integer::sum);
      examples.putIfAbsent(k, t);
    }
    counts.forEach(
        (k, count) -> {
          if (count > 0) {
            diff.append("  missing ").append(what).append(" (only in full): ")
                .append(describe(examples.get(k))).append('\n');
          } else if (count < 0) {
            diff.append("  extra ").append(what).append(" (only in incremental): ")
                .append(describe(examples.get(k))).append('\n');
          }
        });
  }

  private static boolean sameOrder(
      List<EvaluationVisitor.ViolationRecord> a, List<EvaluationVisitor.ViolationRecord> b) {
    for (int i = 0; i < a.size(); i++) {
      EvaluationVisitor.ViolationRecord x = a.get(i);
      EvaluationVisitor.ViolationRecord y = b.get(i);
      if (x.instance() != y.instance()
          || !Objects.equals(x.severity(), y.severity())
          || !Objects.equals(x.customMessage(), y.customMessage())) {
        return false;
      }
    }
    return true;
  }

  private static String describe(Object o) {
    if (o instanceof EvaluationVisitor.ViolationRecord r) {
      return describeInstance(r.instance()) + " [" + r.severity() + "] " + Objects.toString(r.customMessage());
    }
    if (o instanceof CompileError e) {
      return e.getMessage();
    }
    return String.valueOf(o);
  }

  private static String describeInstance(EObject instance) {
    return instance.eClass().getName() + "@" + Integer.toHexString(System.identityHashCode(instance));
  }

  private static List<String> messages(List<CompileError> errors) {
    return errors.stream().map(CompileError::getMessage).toList();
  }

  private static String firstLine(String constraint) {
    String trimmed = constraint.strip().replaceAll("\\s+", " ");
    return trimmed.length() > 160 ? trimmed.substring(0, 160) + "…" : trimmed;
  }
}
