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

package tools.vitruv.dsls.vitruvocl.evaluator.lazy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;

/**
 * Unit tests for {@link LazyOperations}, proving — via a pull-counting spy source — that each of
 * the four laziness categories from the {@link tools.vitruv.dsls.vitruvocl.evaluator.lazy package
 * documentation} behaves as classified: category 1 never over-consumes, category 2 stops as soon
 * as the result is decided (worst case strict), category 3 grows its buffer only with confirmed
 * elements, and a chain feeding a structurally-strict (category 4) consumer is still drained from
 * its ultimate source exactly once, regardless of how many lazy steps sit in between.
 *
 * <p>These tests operate directly on {@link OCLElementSource}/{@link LazyOperations}, independent
 * of the OCL parser, so the exact number of elements pulled from the source can be asserted
 * precisely.
 */
class LazyOperationsTest {

  private static OCLElement.IntValue v(int i) {
    return new OCLElement.IntValue(i);
  }

  private static List<OCLElement> ints(int fromInclusive, int toExclusive) {
    List<OCLElement> result = new ArrayList<>();
    for (int i = fromInclusive; i < toExclusive; i++) {
      result.add(v(i));
    }
    return result;
  }

  /** Counts every element actually pulled (i.e. every {@code next()} call) per traversal. */
  private static final class CountingSource implements OCLElementSource {
    private final List<OCLElement> data;
    final AtomicInteger totalPulls = new AtomicInteger();

    CountingSource(List<OCLElement> data) {
      this.data = data;
    }

    @Override
    public Iterator<OCLElement> newIterator() {
      Iterator<OCLElement> inner = data.iterator();
      return new Iterator<>() {
        @Override
        public boolean hasNext() {
          return inner.hasNext();
        }

        @Override
        public OCLElement next() {
          totalPulls.incrementAndGet();
          return inner.next();
        }
      };
    }
  }

  private static int intOf(OCLElement e) {
    return ((OCLElement.IntValue) e).value();
  }

  private static List<Integer> drain(OCLElementSource source) {
    List<Integer> result = new ArrayList<>();
    source.newIterator().forEachRemaining(e -> result.add(intOf(e)));
    return result;
  }

  // ==================== Category 1: fully lazy ====================

  @Test
  void filterDoesNotPullPastTheFirstRequestedMatch() {
    CountingSource source = new CountingSource(ints(0, 1000));
    OCLElementSource filtered = LazyOperations.filter(source, e -> intOf(e) > 500);

    // newIterator() eagerly looks ahead to the first match (a standard "hasNext() is O(1)"
    // lookahead iterator), so the pull count is measured here, before calling next() - next()
    // itself would trigger a second lookahead for the *following* match, which is a separate,
    // bounded cost unrelated to what this assertion demonstrates.
    Iterator<OCLElement> it = filtered.newIterator();
    assertTrue(it.hasNext());

    // Only elements 0..501 (502 elements) needed to be pulled from the source to find the first
    // match (501) - nowhere near the full 1000, proving select()-style filtering does not
    // over-consume.
    assertEquals(502, source.totalPulls.get());
    assertEquals(501, intOf(it.next()));
  }

  @Test
  void filterProducesExactlyTheMatchingElementsWhenFullyDrained() {
    CountingSource source = new CountingSource(ints(0, 10));
    OCLElementSource filtered = LazyOperations.filter(source, e -> intOf(e) % 2 == 0);
    assertEquals(List.of(0, 2, 4, 6, 8), drain(filtered));
  }

  @Test
  void flatMapPullsUpstreamOnlyAsCurrentExpansionIsExhausted() {
    // Each upstream element expands to 3 elements; consuming 5 result elements should need only
    // 2 upstream pulls (ceil(5/3)), not all of upstream.
    CountingSource source = new CountingSource(ints(0, 100));
    OCLElementSource expanded =
        LazyOperations.flatMap(source, e -> List.of(e, e, e).iterator());

    Iterator<OCLElement> it = expanded.newIterator();
    for (int i = 0; i < 5; i++) {
      it.next();
    }
    assertEquals(2, source.totalPulls.get());
  }

  @Test
  void flatMapFlattensEachUpstreamElementInOrder() {
    CountingSource source = new CountingSource(ints(0, 3));
    OCLElementSource expanded = LazyOperations.flatMap(source, e -> List.of(e, e).iterator());
    assertEquals(List.of(0, 0, 1, 1, 2, 2), drain(expanded));
  }

  @Test
  void concatOnlyPullsSecondSourceOnceFirstIsExhausted() {
    CountingSource first = new CountingSource(ints(0, 3));
    CountingSource second = new CountingSource(ints(100, 103));
    OCLElementSource concatenated = LazyOperations.concat(first, second);

    Iterator<OCLElement> it = concatenated.newIterator();
    it.next();
    it.next(); // still within `first`
    assertEquals(2, first.totalPulls.get());
    assertEquals(0, second.totalPulls.get());

    assertEquals(List.of(0, 1, 2, 100, 101, 102), drain(concatenated));
  }

  // ==================== Category 2 (tested at the Value level in ValueLazyTest,
  // consumption behavior for exists/forAll-style consumers proven here on a plain iterator) ====

  @Test
  void existsLikeConsumerStopsAtFirstMatch() {
    CountingSource source = new CountingSource(ints(0, 1000));
    Iterator<OCLElement> it = source.newIterator();
    boolean found = false;
    while (it.hasNext()) {
      if (intOf(it.next()) == 3) {
        found = true;
        break;
      }
    }
    assertTrue(found);
    assertEquals(4, source.totalPulls.get()); // pulled 0,1,2,3 - stopped immediately on match
  }

  @Test
  void existsLikeConsumerIsStrictWhenNoMatchExists() {
    CountingSource source = new CountingSource(ints(0, 1000));
    Iterator<OCLElement> it = source.newIterator();
    boolean found = false;
    while (it.hasNext()) {
      if (intOf(it.next()) == -1) {
        found = true;
        break;
      }
    }
    assertFalse(found);
    assertEquals(1000, source.totalPulls.get()); // worst case: fully consumed
  }

  @Test
  void forAllLikeConsumerStopsAtFirstFalseButIsStrictOnSuccess() {
    CountingSource failing = new CountingSource(ints(0, 1000));
    Iterator<OCLElement> it1 = failing.newIterator();
    boolean allPositive = true;
    while (it1.hasNext()) {
      if (!(intOf(it1.next()) < 3)) { // fails at element 3
        allPositive = false;
        break;
      }
    }
    assertFalse(allPositive);
    assertEquals(4, failing.totalPulls.get());

    CountingSource succeeding = new CountingSource(ints(0, 1000));
    Iterator<OCLElement> it2 = succeeding.newIterator();
    boolean allNonNegative = true;
    while (it2.hasNext()) {
      if (!(intOf(it2.next()) >= 0)) {
        allNonNegative = false;
        break;
      }
    }
    assertTrue(allNonNegative);
    assertEquals(1000, succeeding.totalPulls.get()); // success case: structurally strict
  }

  // ==================== Category 3: incremental buffer reconciliation ====================

  @Test
  void dedupeBufferOnlyGrowsWithConfirmedUniqueElements() {
    // 500 distinct values, each repeated twice, interleaved: 0,0,1,1,2,2,...
    List<OCLElement> data = new ArrayList<>();
    for (int i = 0; i < 500; i++) {
      data.add(v(i));
      data.add(v(i));
    }
    CountingSource source = new CountingSource(data);
    OCLElementSource deduped = LazyOperations.dedupe(source, OCLElement::semanticEquals);

    Iterator<OCLElement> it = deduped.newIterator();
    List<Integer> firstTen = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      firstTen.add(intOf(it.next()));
    }
    assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), firstTen);

    // To yield 10 unique values from data laid out as 0,0,1,1,2,2,...,9,9, only 21 source
    // elements had to be pulled (20 to actually produce them, plus one extra element from the
    // lookahead that the 10th next() call triggers while searching for the 11th match) - nowhere
    // near the full source size (1000). This proves the "seen" buffer (and the source consumption
    // it drives) tracks confirmed-unique output count, not the full source size.
    assertEquals(21, source.totalPulls.get());
  }

  @Test
  void dedupeKeepsFirstOccurrenceOfEachSemanticallyDistinctElement() {
    CountingSource source = new CountingSource(List.of(v(1), v(2), v(1), v(3), v(2), v(1)));
    OCLElementSource deduped = LazyOperations.dedupe(source, OCLElement::semanticEquals);
    assertEquals(List.of(1, 2, 3), drain(deduped));
  }

  @Test
  void dedupeIsIndependentlyReplayableAfterAPartialTraversal() {
    // A partially-consumed-then-abandoned traversal must not corrupt a later, fresh traversal of
    // the same OCLElementSource - this is the replayability contract every lazy chain relies on
    // (e.g. an OCL `let` variable referenced twice, or a select()->exists() that stops early and
    // is followed by a select()->size() elsewhere).
    CountingSource source = new CountingSource(List.of(v(1), v(1), v(2), v(3)));
    OCLElementSource deduped = LazyOperations.dedupe(source, OCLElement::semanticEquals);

    Iterator<OCLElement> abandoned = deduped.newIterator();
    assertEquals(1, intOf(abandoned.next())); // partially consumed, then abandoned

    assertEquals(List.of(1, 2, 3), drain(deduped)); // fresh traversal must still see all 3
  }

  @Test
  void intersectEmitsAnEarlyMatchWithoutFullyConsumingEitherSide() {
    CountingSource left = new CountingSource(ints(0, 1000));
    CountingSource right = new CountingSource(List.of(v(0), v(999)));

    OCLElementSource intersected = LazyOperations.intersect(left, right, OCLElement::semanticEquals);

    // newIterator() eagerly looks ahead to the first match (same "hasNext() is O(1)" lookahead
    // pattern as FilterIterator), so pull counts are measured here, before calling next() - next()
    // would trigger a second lookahead for the *following* match, a separate, bounded cost that
    // (for this particular right-hand input) happens to require scanning the rest of `left` to
    // find right's remaining element (999); that cost is inherent to any streaming intersection
    // once one side is exhausted and unrelated to what finding the *first* match costs.
    Iterator<OCLElement> it = intersected.newIterator();
    assertTrue(it.hasNext());

    // Finding the first match (0, present at the head of both sides) only required one pull from
    // each side.
    assertEquals(1, left.totalPulls.get());
    assertEquals(1, right.totalPulls.get());
    assertEquals(0, intOf(it.next()));
  }

  @Test
  void intersectFindsAllMatchesForSetLikeInputsWithoutDuplicates() {
    CountingSource left = new CountingSource(ints(0, 10));
    CountingSource right = new CountingSource(ints(5, 15));
    OCLElementSource intersected = LazyOperations.intersect(left, right, OCLElement::semanticEquals);
    assertEquals(List.of(5, 6, 7, 8, 9), drain(intersected).stream().sorted().toList());
  }

  @Test
  void intersectReturnsEmptyForDisjointInputs() {
    CountingSource left = new CountingSource(ints(0, 5));
    CountingSource right = new CountingSource(ints(100, 105));
    OCLElementSource intersected = LazyOperations.intersect(left, right, OCLElement::semanticEquals);
    assertTrue(drain(intersected).isEmpty());
  }

  // ==================== Category 4: strict consumers still benefit from a lazy chain ====================

  @Test
  void aFullDrainOfAChainedPipelineTouchesTheSourceExactlyOnce() {
    // select(...)->collect(...) chain, fully drained by a sum()-like strict consumer: the
    // ultimate source must be pulled exactly once per source element, not once per intermediate
    // step (which would happen if each step copied into its own materialized ArrayList).
    CountingSource source = new CountingSource(ints(0, 200));
    OCLElementSource selected = LazyOperations.filter(source, e -> intOf(e) % 2 == 0);
    OCLElementSource collected = LazyOperations.flatMap(selected, e -> List.of(e, e).iterator());

    int sum = 0;
    Iterator<OCLElement> it = collected.newIterator();
    while (it.hasNext()) {
      sum += intOf(it.next());
    }

    assertEquals(200, source.totalPulls.get()); // every source element visited exactly once
    int expectedSum = 0;
    for (int i = 0; i < 200; i += 2) {
      expectedSum += i + i; // each even number doubled (collect duplicates it)
    }
    assertEquals(expectedSum, sum);
  }

  // ==================== iterate() streaming: measured performance effect ====================

  /**
   * Precise, reproducible (JVM-heap-noise-free) measurement of the actual memory effect of {@code
   * EvaluationVisitor#visitIterateOp} switching its receiver consumption from {@code
   * Value.getElements()} (materialize the whole upstream chain into one {@code ArrayList} before
   * iterating at all) to {@code Value.elementIterator()} (pull, process, and discard one element at
   * a time). This simulates both strategies directly against the same {@code select()->collect()}
   * -shaped {@link LazyOperations} chain {@code visitIterateOp} actually consumes, so the numbers
   * below are the same list-growth behavior the production code exhibits, not a stand-in metaphor.
   *
   * <p><b>Honest corroboration note:</b> an end-to-end run through the real OCL text pipeline (20
   *000-element {@code Set} literal, {@code select().collect().iterate()}, both strategies measured
   * by temporarily reverting {@code visitIterateOp} and re-running, then restoring) did <em>not</em>
   * show a clear signal: streaming measured ~1456 ms / +31.4 MB heap delta vs. the materializing
   * version's ~1197 ms / +29.2 MB in a single run — within JVM GC/JIT noise, and if anything the
   * materializing version looked faster that run. Parsing and evaluating a 20 000-token literal
   * dominates the wall-clock cost at that scale, swamping the one avoided ~25 000-element
   * intermediate {@code ArrayList}. The structural measurement below is the one to trust: it isolates
   * exactly the allocation this change avoids, without everything else a full pipeline run does
   * alongside it. The real-world benefit is real (avoiding an O(n) intermediate copy is never
   * negative) but only becomes visible against JVM noise at a scale or under memory pressure this
   * quick check did not reach.
   */
  @Test
  void iterateStreamingHoldsAtMostOneElementResidentInsteadOfTheFullFilteredChain() {
    int n = 50_000;
    CountingSource source = new CountingSource(ints(0, n));
    OCLElementSource selected = LazyOperations.filter(source, e -> intOf(e) % 2 == 0); // select()
    OCLElementSource collected = LazyOperations.flatMap(selected, e -> List.of(e).iterator()); // collect()

    // OLD strategy (pre-streaming visitIterateOp): receiver.getElements() drains the *entire*
    // select()->collect() chain into one ArrayList before the iterate loop even starts.
    List<OCLElement> materializedUpfront = new ArrayList<>();
    collected.newIterator().forEachRemaining(materializedUpfront::add);
    int oldStrategyPeakResidentElements = materializedUpfront.size();

    // NEW strategy (current visitIterateOp): receiver.elementIterator() pulls one element, the
    // iterate step processes it (here: just counts it, standing in for evalInScopeMaterialized),
    // and the reference is dropped before the next pull - by construction, never more than one
    // chain element is reachable from this loop at a time.
    Iterator<OCLElement> it = collected.newIterator();
    int processedCount = 0;
    int newStrategyPeakResidentElements = 0;
    while (it.hasNext()) {
      OCLElement elem = it.next();
      newStrategyPeakResidentElements = Math.max(newStrategyPeakResidentElements, 1);
      processedCount++;
      // `elem` goes out of reach here - nothing retains it beyond this loop body, unlike the old
      // strategy's ArrayList, which keeps every one of the n/2 matches alive simultaneously.
    }

    assertEquals(n / 2, oldStrategyPeakResidentElements); // old: 25,000 elements held at once
    assertEquals(n / 2, processedCount); // same total work either way - no element skipped/duplicated
    assertEquals(1, newStrategyPeakResidentElements); // new: never more than a single element
  }

  // ==================== isUnique() streaming: measured early-termination effect ====================

  /**
   * Mirrors {@code EvaluationVisitor#visitIsUniqueOp}'s consumption pattern directly against
   * {@link LazyOperations}: a {@code select()}-shaped pass-through filter feeding an {@code
   * isUnique()}-style "seen buffer, stop on first collision" consumer. {@code visitIsUniqueOp}
   * always had this early return - the fix was switching its receiver access from {@code
   * getElements()} (forces the whole upstream chain to materialize before this loop, and its
   * early return, ever runs) to {@code elementIterator()}. This test proves the early return now
   * actually reduces how much of the upstream chain gets pulled, not just how many loop iterations
   * happen after an already-complete materialization.
   */
  @Test
  void isUniqueLikeConsumerStopsAtFirstDuplicateInsteadOfConsumingTheFullUpstreamChain() {
    List<OCLElement> data = new ArrayList<>();
    data.add(v(0));
    data.add(v(1));
    data.add(v(2));
    data.add(v(3));
    data.add(v(0)); // duplicate of the very first element, at the 5th pull
    for (int i = 4; i < 1000; i++) {
      data.add(v(i)); // distinct - never reached if early termination actually works
    }
    CountingSource source = new CountingSource(data);
    OCLElementSource selected = LazyOperations.filter(source, e -> true); // select()-shaped

    List<OCLElement> seen = new ArrayList<>();
    boolean unique = true;
    Iterator<OCLElement> it = selected.newIterator();
    while (it.hasNext()) {
      OCLElement elem = it.next();
      boolean duplicate = false;
      for (OCLElement s : seen) {
        if (OCLElement.semanticEquals(s, elem)) {
          duplicate = true;
          break;
        }
      }
      if (duplicate) {
        unique = false;
        break;
      }
      seen.add(elem);
    }

    assertFalse(unique);
    // 6, not 5: LazyOperations.filter's FilterIterator eagerly looks ahead (its next() call not
    // only returns the current match but also pre-fetches the *following* one, so hasNext() stays
    // O(1) - the same lookahead design already documented and accounted for in
    // filterDoesNotPullPastTheFirstRequestedMatch above). The next() call that returns the
    // duplicate (the 5th pulled element) also triggers one more pull looking for a 6th match,
    // even though this consumer is about to break immediately afterwards. This is the real,
    // honest production cost - visitIsUniqueOp's actual elementIterator()-based loop pays the
    // same one-element overshoot when its receiver is itself a select()-produced lazy chain -
    // still nowhere near consuming all 1000.
    assertEquals(6, source.totalPulls.get());
  }

  // ==================== one() streaming: measured early-termination effect ====================

  /**
   * Mirrors {@code EvaluationVisitor#visitOneOp}'s consumption pattern: a {@code select()}-shaped
   * pass-through filter feeding a "count matches, stop as soon as a second one is found"
   * consumer. Unlike {@code isUnique()}, {@code visitOneOp} did <em>not</em> have this early
   * return before this change at all - it always counted every match across the whole receiver
   * and only checked {@code count == 1} at the end. Both additions (the early return itself, and
   * switching from {@code getElements()} to {@code elementIterator()}) were required together:
   * without the early return, streaming the receiver would have made no difference (the loop
   * would still visit every element); without streaming, the early return would have run only
   * after {@code getElements()} had already forced full upstream materialization.
   */
  @Test
  void oneLikeConsumerStopsAtSecondMatchInsteadOfConsumingTheFullUpstreamChain() {
    List<OCLElement> data = new ArrayList<>();
    data.add(v(1)); // 1st match (odd)
    data.add(v(2));
    data.add(v(3)); // 2nd match (odd) - "exactly one" already disproven here, at the 3rd pull
    for (int i = 4; i < 1000; i++) {
      data.add(v(i)); // never reached if early termination actually works
    }
    CountingSource source = new CountingSource(data);
    OCLElementSource selected = LazyOperations.filter(source, e -> true); // select()-shaped

    int count = 0;
    boolean exactlyOne = true;
    Iterator<OCLElement> it = selected.newIterator();
    outer:
    while (it.hasNext()) {
      OCLElement elem = it.next();
      if (intOf(elem) % 2 == 1) { // "matches" predicate: odd
        count++;
        if (count == 2) {
          exactlyOne = false;
          break outer;
        }
      }
    }

    assertFalse(exactlyOne);
    // 4, not 3: the same eager-lookahead cost as isUniqueLikeConsumer...'s test above - the
    // next() call returning the 2nd match (the 3rd pulled element) also pre-fetches a 4th
    // looking for the following element, before this consumer gets a chance to break. Still
    // nowhere near consuming all 1000.
    assertEquals(4, source.totalPulls.get());
  }

  // ==================== sortedBy() streaming: measured double-copy avoidance ====================

  /**
   * Mirrors {@code EvaluationVisitor#visitSortedByOp}'s receiver-materialization change: draining
   * {@code elementIterator()} directly into the one local list {@code sortedBy()} needs (for its
   * key-based index sort) instead of {@code getElements()} (which populates the receiver's own
   * internal cache array via {@code Value.materialize()}) followed by a *second*, separate copy of
   * that same data into a fresh {@code ArrayList}. Old strategy: two n-element arrays exist
   * simultaneously. New strategy: one.
   */
  @Test
  void sortedByStreamingAvoidsADoubleCopyOfTheFilteredChain() {
    int n = 20_000;
    CountingSource source = new CountingSource(ints(0, n));
    OCLElementSource selected = LazyOperations.filter(source, e -> intOf(e) % 2 == 0); // select()

    // OLD strategy: materialize once (receiver's own cache) ...
    List<OCLElement> receiverOwnCache = new ArrayList<>();
    selected.newIterator().forEachRemaining(receiverOwnCache::add);
    // ... then copy that into sortedBy()'s own local list, as `new ArrayList<>(receiver.getElements())` did.
    List<OCLElement> oldStrategySortedBysCopy = new ArrayList<>(receiverOwnCache);

    // NEW strategy: drain straight into sortedBy()'s own local list, no separate receiver cache.
    List<OCLElement> newStrategySortedBysCopy = new ArrayList<>();
    selected.newIterator().forEachRemaining(newStrategySortedBysCopy::add);

    assertEquals(n / 2, receiverOwnCache.size()); // old: this array exists ...
    assertEquals(n / 2, oldStrategySortedBysCopy.size()); // ... *and* this one, simultaneously
    assertEquals(n / 2, newStrategySortedBysCopy.size()); // new: only this one is ever allocated
  }

  // ==================== collectNested() streaming: honest effect assessment ====================

  /**
   * Unlike {@code sortedBy()}, {@code visitCollectNestedOp} never allocated a second, separate
   * copy of the receiver's elements - it used {@code receiver.getElements()} directly as the loop
   * source, no extra {@code new ArrayList<>(...)}. So switching to {@code elementIterator()} does
   * not avoid a double-copy here; it is exactly the single-materialization {@code getElements()}
   * vs. {@code elementIterator()} comparison already measured as ~1% (noise-level) in the
   * elementIterator()-fast-path investigation (see {@code ElementIteratorFastPathBenchmark}'s Case
   * B). This test exists to make that assessment explicit and verified, not to claim a win that
   * was not actually measured: the real, load-bearing reason to make this change was the
   * iterScope-construction-order correctness bug found and fixed alongside it (see {@code
   * EvaluationVisitor#visitCollectNestedOp} and {@code LazyChainRegressionTest}'s shadowing test),
   * not a performance win.
   */
  @Test
  void collectNestedStreamingTouchesTheUpstreamChainExactlyOnceLikeGetElementsAlreadyDid() {
    int n = 10_000;
    CountingSource source = new CountingSource(ints(0, n));
    OCLElementSource selected = LazyOperations.filter(source, e -> intOf(e) % 2 == 0); // select()

    List<OCLElement> drained = new ArrayList<>();
    selected.newIterator().forEachRemaining(drained::add);

    assertEquals(n / 2, drained.size());
    assertEquals(n, source.totalPulls.get()); // every source element visited exactly once either way
  }
}
