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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.evaluator.lazy.OCLElementSource;
import tools.vitruv.dsls.vitruvocl.typechecker.Type;

/**
 * Tests {@link Value}'s lazy backing: materialize-on-demand {@link Value#getElements()}, the
 * non-materializing {@link Value#elementIterator()} pull path, and the lazy implementations of
 * {@code merge}/{@code excluding}/{@code intersection}/{@code flatten} built on top of {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperations}.
 */
class ValueLazyTest {

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

  private static int intOf(OCLElement e) {
    return ((OCLElement.IntValue) e).value();
  }

  /** Counts every element actually pulled (every {@code next()} call) across all traversals. */
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

  @Test
  void getElementsMaterializesALazyValueExactlyOnceEvenWhenCalledRepeatedly() {
    CountingSource source = new CountingSource(ints(0, 50));
    Value lazyValue = Value.lazy(source, Type.bag(Type.INTEGER));

    List<OCLElement> first = lazyValue.getElements();
    List<OCLElement> second = lazyValue.getElements();

    assertEquals(50, first.size());
    assertEquals(first, second);
    assertEquals(50, source.totalPulls.get()); // second getElements() call used the cache
  }

  @Test
  void elementIteratorDoesNotMaterializeOrCacheAPartiallyConsumedLazyValue() {
    CountingSource source = new CountingSource(ints(0, 10));
    Value lazyValue = Value.lazy(source, Type.bag(Type.INTEGER));

    Iterator<OCLElement> it = lazyValue.elementIterator();
    assertEquals(0, intOf(it.next()));
    assertEquals(1, intOf(it.next())); // partial pull, then abandoned

    // A later, independent full materialization must still see all 10 elements - the partial
    // elementIterator() traversal must not have poisoned the Value's state.
    assertEquals(10, lazyValue.getElements().size());
  }

  @Test
  void isEmptyOnALazyValueNeverConsumesAnElement() {
    // isEmpty()/notEmpty() only need to peek hasNext() on a fresh iterator - since the counting
    // spy's hasNext() doesn't advance (only next() does), this proves the check never actually
    // consumes (evaluates) any element, regardless of how large the source is.
    CountingSource source = new CountingSource(ints(0, 1_000_000));
    Value lazyValue = Value.lazy(source, Type.bag(Type.INTEGER));

    assertTrue(lazyValue.notEmpty());
    assertEquals(0, source.totalPulls.get());
  }

  @Test
  void isEmptyOnAnEmptyLazyValuePullsNothingBeyondTheFailedProbe() {
    CountingSource source = new CountingSource(List.of());
    Value lazyValue = Value.lazy(source, Type.bag(Type.INTEGER));

    assertTrue(lazyValue.isEmpty());
    assertEquals(0, source.totalPulls.get());
  }

  @Test
  void mergeOnUniqueTypeProducesADuplicateFreeUnion() {
    Value set1 = Value.of(List.of(v(1), v(2), v(3)), Type.set(Type.INTEGER));
    Value set2 = Value.of(List.of(v(2), v(3), v(4)), Type.set(Type.INTEGER));

    Value merged = set1.merge(set2);
    List<Integer> values = merged.getElements().stream().map(ValueLazyTest::intOf).sorted().toList();
    assertEquals(List.of(1, 2, 3, 4), values);
  }

  @Test
  void mergeOnBagTypeKeepsAllDuplicates() {
    Value bag1 = Value.of(List.of(v(1), v(2)), Type.bag(Type.INTEGER));
    Value bag2 = Value.of(List.of(v(2), v(2)), Type.bag(Type.INTEGER));

    Value merged = bag1.merge(bag2);
    List<Integer> values = merged.getElements().stream().map(ValueLazyTest::intOf).sorted().toList();
    assertEquals(List.of(1, 2, 2, 2), values);
  }

  @Test
  void excludingOnSetRemovesAllOccurrences() {
    Value set = Value.of(List.of(v(1), v(2), v(3)), Type.set(Type.INTEGER));
    Value result = set.excluding(v(2));
    List<Integer> values = result.getElements().stream().map(ValueLazyTest::intOf).sorted().toList();
    assertEquals(List.of(1, 3), values);
  }

  @Test
  void excludingOnBagRemovesOnlyTheFirstOccurrence() {
    Value bag = Value.of(List.of(v(1), v(2), v(2), v(3)), Type.bag(Type.INTEGER));
    Value result = bag.excluding(v(2));
    List<Integer> values = result.getElements().stream().map(ValueLazyTest::intOf).toList();
    assertEquals(List.of(1, 2, 3), values); // one `2` remains
  }

  @Test
  void excludingOnBagIsIndependentlyReplayable() {
    // The "already removed one" flag must be per-traversal state, not shared - otherwise a first
    // traversal would consume the "remove one" budget and a second, independent traversal would
    // incorrectly find nothing left to remove.
    Value bag = Value.of(List.of(v(1), v(2), v(2)), Type.bag(Type.INTEGER));
    Value result = bag.excluding(v(2));

    List<Integer> firstTraversal = drain(result);
    List<Integer> secondTraversal = drain(result);
    assertEquals(firstTraversal, secondTraversal);
    assertEquals(List.of(1, 2), firstTraversal);
  }

  private static List<Integer> drain(Value value) {
    List<Integer> result = new ArrayList<>();
    value.elementIterator().forEachRemaining(e -> result.add(intOf(e)));
    return result;
  }

  @Test
  void intersectionMatchesSimpleMembershipFilterForSetInputs() {
    Value set1 = Value.of(ints(0, 20), Type.set(Type.INTEGER));
    Value set2 = Value.of(ints(10, 30), Type.set(Type.INTEGER));

    Value result = set1.intersection(set2);
    List<Integer> values = result.getElements().stream().map(ValueLazyTest::intOf).sorted().toList();
    assertEquals(List.of(10, 11, 12, 13, 14, 15, 16, 17, 18, 19), values);
  }

  @Test
  void flattenKeepsInnerNestedCollectionsLazy() {
    CountingSource innerSource = new CountingSource(ints(0, 1000));
    Value innerLazyValue = Value.lazy(innerSource, Type.bag(Type.INTEGER));

    Value outer =
        Value.of(List.of(new OCLElement.NestedCollection(innerLazyValue)), Type.bag(Type.ANY));
    Value flattened = outer.flatten();

    Iterator<OCLElement> it = flattened.elementIterator();
    assertEquals(0, intOf(it.next()));
    assertEquals(1, intOf(it.next()));

    // Only pulling the first two flattened elements must only pull two elements from the inner
    // nested collection's own lazy source - the inner collection was never forced to fully
    // materialize just because it sits inside an outer flatten().
    assertEquals(2, innerSource.totalPulls.get());
  }
}
