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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Manual microbenchmark comparing {@code getElements()}+loop against {@code elementIterator()}
 * +loop for a single-pass consumer, in the two cases that matter for deciding whether {@code
 * one()}/{@code isUnique()}/{@code sortedBy()}/{@code collectNested()} can safely switch to {@code
 * elementIterator()} the way {@code iterate()} already did:
 *
 * <ul>
 *   <li><b>Case A - already-materialized receiver</b> (e.g. a literal collection): both paths
 *       should be identical, since {@link Value#elementIterator()} has a direct fast path
 *       ({@code elements.iterator()}) for this case — see the code-analysis note below.
 *   <li><b>Case B - freshly-lazy receiver</b> (e.g. the result of a preceding {@code union()}
 *       that hasn't been consumed yet): both paths must drain the same {@code
 *       LazyOperations} combinator chain once; {@code getElements()} additionally copies the
 *       result into a new {@code ArrayList} that {@code elementIterator()} skips.
 * </ul>
 *
 * <p><b>Code-analysis finding (Schritt 1 of this task) — this is what motivates running Case A
 * and B at all:</b> {@link Value#elementIterator()} (see {@code Value.java}) is:
 *
 * <pre>{@code
 * public Iterator<OCLElement> elementIterator() {
 *   return elements != null ? elements.iterator() : lazySource.newIterator();
 * }
 * }</pre>
 *
 * This is a genuine, unconditional, zero-overhead fast path whenever {@code elements != null}
 * (i.e. the {@link Value} is already materialized) — no {@code OCLElementSource}/{@code
 * LazyOperations} combinator involved, identical to what {@code getElements().iterator()} would
 * do. It covers every {@link Value} constructed via the eager constructors/{@code Value.of}/
 * {@code Value.empty}/{@code Value.singleton} (literal collections, {@code allInstances()}, and
 * every category-4 operation's result: {@code sortedBy}, {@code isUnique}, {@code one}, {@code
 * size}, {@code sum}, ...).
 *
 * <p>The fast path is <b>not</b> hit, however, for a {@link Value} freshly returned by any of the
 * operations that build a {@code Value.lazy(...)} result — {@code select}, {@code reject}, {@code
 * collect}, navigation, {@code merge}/{@code union}/{@code including}/{@code append}, {@code
 * excluding}, {@code intersection}, {@code removeDuplicates}, {@code flatten} — since those all
 * start with {@code elements == null} until something forces materialization. This is a genuine
 * gap relative to "fast path applies to every construction path", confirmed by grepping every
 * {@code return Value.lazy(...)} site in {@code Value.java} and {@code EvaluationVisitor.java}.
 * Case B below measures whether that gap actually costs anything for a single-pass consumer (it
 * structurally shouldn't: both strategies drain the same combinator chain once, and {@code
 * elementIterator()} skips the extra destination-array copy {@code getElements()}'s {@code
 * materialize()} performs) — Case B is the empirical check, not just the structural argument.
 *
 * <p><b>Caveat this benchmark deliberately does not cover:</b> the two-variable (Cartesian
 * product) iterators consume the receiver <em>twice</em> (nested loop over the same collection).
 * Calling {@code elementIterator()} twice on a still-lazy {@link Value} would re-run the entire
 * upstream combinator chain a second time (no caching, by design, for correctness/replay reasons)
 * — a real, avoidable cost that this single-pass benchmark does not exercise and that any future
 * two-variable-method refactor must handle by materializing once (e.g. keep {@code
 * getElements()}, or drain {@code elementIterator()} into a local list once) rather than naively
 * calling {@code elementIterator()} per loop nesting.
 *
 * <p><b>This is a stopgap, not a rigorous benchmark:</b> no JMH is set up in this project (checked
 * {@code pom.xml} — no dependency, no plugin) and adding it is disproportionate for a single
 * question. This hand-rolled harness follows the same warmup/median-of-blocks/same-JVM-session
 * principles JMH would apply automatically, but remains subject to JIT/GC noise a real benchmark
 * harness would control for better. Results are printed, not asserted on, for the same reason the
 * end-to-end {@code iterate()} timing numbers were reported as observations, not hard assertions.
 */
class ElementIteratorFastPathBenchmark {

  /**
   * Sink for every measured result, standing in for JMH's {@code Blackhole}: without consuming
   * the sum somewhere the JIT can't prove is dead, it is free to eliminate part or all of the
   * summation loop, since the local {@code sum} variable would otherwise never escape the helper
   * method. A {@code volatile} field forces a real write the JIT must honor.
   */
  private static volatile long sink;

  private static final int N = 2000;
  private static final int WARMUP_REPS = 2000;
  private static final int MEASURED_BLOCKS = 11; // odd, so "median" is an actual element, not an average of two
  private static final int REPS_PER_BLOCK = 500;

  private static List<OCLElement> buildElements(int count) {
    List<OCLElement> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      list.add(new OCLElement.IntValue(i));
    }
    return list;
  }

  private static void sumViaGetElements(Value v) {
    long sum = 0;
    for (OCLElement e : v.getElements()) {
      sum += ((OCLElement.IntValue) e).value();
    }
    sink = sum; // defeat dead-code elimination
  }

  private static void sumViaElementIterator(Value v) {
    long sum = 0;
    Iterator<OCLElement> it = v.elementIterator();
    while (it.hasNext()) {
      sum += ((OCLElement.IntValue) it.next()).value();
    }
    sink = sum; // defeat dead-code elimination
  }

  /** Case A supplier: an already-materialized Value (literal-collection-shaped). */
  private static Value freshEagerValue(List<OCLElement> base) {
    return Value.of(base, Type.set(Type.INTEGER));
  }

  /** Case B supplier: a freshly-lazy Value, as if just returned by a preceding union() call. */
  private static Value freshLazyUnionValue(List<OCLElement> left, List<OCLElement> right) {
    Value a = Value.of(left, Type.set(Type.INTEGER));
    Value b = Value.of(right, Type.set(Type.INTEGER));
    return a.union(b); // Value.lazy(...) under the hood - elements == null until consumed
  }

  private static long median(long[] values) {
    long[] sorted = values.clone();
    java.util.Arrays.sort(sorted);
    return sorted[sorted.length / 2];
  }

  @Test
  void caseA_alreadyMaterializedReceiver_getElementsVsElementIterator() {
    List<OCLElement> base = buildElements(N);

    // Warmup: exercise both variants so the JIT compiles both before timing either.
    for (int i = 0; i < WARMUP_REPS; i++) {
      sumViaGetElements(freshEagerValue(base));
      sumViaElementIterator(freshEagerValue(base));
    }

    long[] getElementsBlockNanos = new long[MEASURED_BLOCKS];
    long[] elementIteratorBlockNanos = new long[MEASURED_BLOCKS];

    for (int block = 0; block < MEASURED_BLOCKS; block++) {
      long t0 = System.nanoTime();
      for (int i = 0; i < REPS_PER_BLOCK; i++) {
        sumViaGetElements(freshEagerValue(base));
      }
      getElementsBlockNanos[block] = System.nanoTime() - t0;

      long t1 = System.nanoTime();
      for (int i = 0; i < REPS_PER_BLOCK; i++) {
        sumViaElementIterator(freshEagerValue(base));
      }
      elementIteratorBlockNanos[block] = System.nanoTime() - t1;
    }

    double medianGetElementsUs = median(getElementsBlockNanos) / 1000.0 / REPS_PER_BLOCK;
    double medianElementIteratorUs = median(elementIteratorBlockNanos) / 1000.0 / REPS_PER_BLOCK;
    System.out.println(
        "[Case A: already-materialized] median per-call: getElements()="
            + medianGetElementsUs
            + " us, elementIterator()="
            + medianElementIteratorUs
            + " us (N="
            + N
            + ", "
            + MEASURED_BLOCKS
            + " blocks x "
            + REPS_PER_BLOCK
            + " reps)");
  }

  @Test
  void caseB_freshlyLazyUnionReceiver_getElementsVsElementIterator() {
    List<OCLElement> left = buildElements(N / 2);
    List<OCLElement> right = buildElements(N / 2);
    // Overlap a quarter of the values so the Set union's dedupe pass has real work to do, not a
    // trivial no-duplicates-ever pass-through.
    for (int i = 0; i < N / 4; i++) {
      right.set(i, left.get(i));
    }

    for (int i = 0; i < WARMUP_REPS; i++) {
      sumViaGetElements(freshLazyUnionValue(left, right));
      sumViaElementIterator(freshLazyUnionValue(left, right));
    }

    long[] getElementsBlockNanos = new long[MEASURED_BLOCKS];
    long[] elementIteratorBlockNanos = new long[MEASURED_BLOCKS];

    for (int block = 0; block < MEASURED_BLOCKS; block++) {
      long t0 = System.nanoTime();
      for (int i = 0; i < REPS_PER_BLOCK; i++) {
        sumViaGetElements(freshLazyUnionValue(left, right));
      }
      getElementsBlockNanos[block] = System.nanoTime() - t0;

      long t1 = System.nanoTime();
      for (int i = 0; i < REPS_PER_BLOCK; i++) {
        sumViaElementIterator(freshLazyUnionValue(left, right));
      }
      elementIteratorBlockNanos[block] = System.nanoTime() - t1;
    }

    double medianGetElementsUs = median(getElementsBlockNanos) / 1000.0 / REPS_PER_BLOCK;
    double medianElementIteratorUs = median(elementIteratorBlockNanos) / 1000.0 / REPS_PER_BLOCK;
    System.out.println(
        "[Case B: freshly-lazy union() receiver] median per-call: getElements()="
            + medianGetElementsUs
            + " us, elementIterator()="
            + medianElementIteratorUs
            + " us (N="
            + N
            + ", "
            + MEASURED_BLOCKS
            + " blocks x "
            + REPS_PER_BLOCK
            + " reps)");
  }
}
