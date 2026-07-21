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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import tools.vitruv.dsls.vitruvocl.evaluator.OCLElement;

/**
 * Composable, pull-based building blocks for lazy {@link OCLElementSource} chains.
 *
 * <p>See the {@link tools.vitruv.dsls.vitruvocl.evaluator.lazy package documentation} for the
 * classification of which VitruvOCL collection operations map to which of these primitives.
 *
 * <p>None of these methods consume anything eagerly: they only build a new {@link
 * OCLElementSource} that, when {@link OCLElementSource#newIterator() traversed}, pulls from its
 * upstream source(s) one element at a time, exactly as far as the caller of {@code next()} asks.
 */
public final class LazyOperations {

  private LazyOperations() {}

  /**
   * Category 1 (fully lazy): keeps only elements matching {@code predicate}, pulling from {@code
   * source} one element at a time. Backs {@code select}/{@code reject} (with the predicate negated
   * for {@code reject}).
   *
   * @param source the upstream source
   * @param predicate test applied to each pulled element; evaluating it may itself trigger
   *     further (nested) evaluation, e.g. an OCL body expression
   * @return a source yielding only the elements for which {@code predicate} holds, in upstream
   *     order
   */
  public static OCLElementSource filter(OCLElementSource source, Predicate<OCLElement> predicate) {
    return () -> new FilterIterator(source.newIterator(), predicate);
  }

  /**
   * Like {@link #filter}, but for predicates that accumulate state across the elements of a single
   * traversal (e.g. "have we already removed one match", "which elements have we seen so far").
   * {@code predicateFactory} is invoked once per {@link OCLElementSource#newIterator()} call, so
   * every independent traversal starts with fresh state — required for {@link
   * OCLElementSource}'s replayability contract, which a predicate holding state shared across
   * traversals would silently violate.
   *
   * @param source the upstream source
   * @param predicateFactory builds a fresh, traversal-local predicate for each new traversal
   * @return a source yielding only the elements accepted by a fresh predicate instance per
   *     traversal
   */
  public static OCLElementSource filterStateful(
      OCLElementSource source, java.util.function.Supplier<Predicate<OCLElement>> predicateFactory) {
    return () -> new FilterIterator(source.newIterator(), predicateFactory.get());
  }

  /**
   * Category 1 (fully lazy): concatenates {@code first} then {@code second}, pulling from {@code
   * second} only once {@code first} is exhausted. Backs {@code union}/{@code merge}/{@code append}
   * on {@code Bag}/{@code Sequence} receivers (plain interleaving/appending, no duplicate check).
   *
   * @param first the source consumed first
   * @param second the source consumed once {@code first} is exhausted
   * @return a source yielding all of {@code first}'s elements followed by all of {@code second}'s
   */
  public static OCLElementSource concat(OCLElementSource first, OCLElementSource second) {
    return () -> new ConcatIterator(first.newIterator(), second.newIterator());
  }

  /**
   * Category 1 (fully lazy): maps each upstream element to zero or more result elements, pulling
   * from {@code source} one element at a time and only expanding the next upstream element once
   * the current expansion is exhausted. Backs {@code collect} (single- and two-variable forms),
   * navigation ({@code e.p}), and the outer level of {@code flatten()}.
   *
   * @param source the upstream source
   * @param expand produces the (possibly lazy) sequence of result elements for one upstream
   *     element; evaluating it may itself trigger further evaluation, e.g. an OCL body expression
   * @return a source yielding the concatenation of {@code expand}'s results for each upstream
   *     element, in upstream order
   */
  public static OCLElementSource flatMap(
      OCLElementSource source, Function<OCLElement, Iterator<OCLElement>> expand) {
    return () -> new FlatMapIterator(source.newIterator(), expand);
  }

  /**
   * Category 3 (incremental buffer): keeps only the first occurrence of each semantically-distinct
   * element, pulling from {@code source} one element at a time. The buffer of already-accepted
   * elements grows only with elements that were actually pulled and confirmed unique — never with
   * the full size of {@code source}. Backs the uniqueness check for {@code union}/{@code merge} on
   * {@code Set}/{@code OrderedSet} receivers, and unique-Ctype navigation results.
   *
   * @param source the upstream source
   * @param semanticEquals equality test used to compare a candidate against the buffer
   * @return a source yielding each semantically-distinct element exactly once, in first-seen order
   */
  public static OCLElementSource dedupe(
      OCLElementSource source, BiPredicate<OCLElement, OCLElement> semanticEquals) {
    // The "seen" buffer is per-traversal state (built fresh in the predicateFactory below), not
    // shared across independent newIterator() calls - otherwise a partially-consumed-then-
    // abandoned traversal (e.g. from an early-terminating exists()) would corrupt a later, fresh
    // traversal of the same lazy Value.
    return filterStateful(
        source,
        () -> {
          List<OCLElement> seen = new ArrayList<>();
          return elem -> {
            for (OCLElement s : seen) {
              if (semanticEquals.test(s, elem)) {
                return false;
              }
            }
            seen.add(elem);
            return true;
          };
        });
  }

  /**
   * Category 3 (incremental buffer): computes the intersection of {@code left} and {@code right}
   * by pulling from both sides in alternation instead of fully materializing either one first.
   *
   * <p>Algorithm: starting on the left side, each step pulls the next element from the current
   * side and checks it against the buffer of already-seen (and not-yet-matched) elements of the
   * <em>other</em> side. On a match, the element is emitted and the next step switches sides. On a
   * miss, the element is added to its own side's buffer and the next step switches sides. Once one
   * side is exhausted, its buffer is complete (equivalent to full membership), so the remaining
   * side is drained against it directly.
   *
   * <p>Note this is a multiset (pairwise-matching) intersection: each match consumes one element
   * from each side's buffer. This coincides exactly with a simple "keep receiver elements that
   * have some match in the argument" filter whenever at least one side has no internal duplicate
   * values (always true for a {@code Set}/{@code OrderedSet} operand) — the standard case — but
   * can differ when both sides contain duplicates of the same value; see the caller for details.
   *
   * @param left the receiver-side source
   * @param right the argument-side source; whichever side's copy of a matching value was pulled at
   *     match time is the one emitted (for primitive values this is indistinguishable; for
   *     metaclass values both sides typically already reference the same underlying instance)
   * @param semanticEquals equality test used to compare elements across sides
   * @return a source yielding one element per pairwise match between the two sides
   */
  public static OCLElementSource intersect(
      OCLElementSource left, OCLElementSource right, BiPredicate<OCLElement, OCLElement> semanticEquals) {
    return () -> new IntersectIterator(left.newIterator(), right.newIterator(), semanticEquals);
  }

  // ==================== Iterator implementations ====================

  /** Lookahead filter: computes the next matching element eagerly so {@code hasNext()} is cheap. */
  private static final class FilterIterator implements Iterator<OCLElement> {
    private final Iterator<OCLElement> upstream;
    private final Predicate<OCLElement> predicate;
    private OCLElement pending;
    private boolean hasPending;

    FilterIterator(Iterator<OCLElement> upstream, Predicate<OCLElement> predicate) {
      this.upstream = upstream;
      this.predicate = predicate;
      advance();
    }

    private void advance() {
      hasPending = false;
      while (upstream.hasNext()) {
        OCLElement candidate = upstream.next();
        if (predicate.test(candidate)) {
          pending = candidate;
          hasPending = true;
          return;
        }
      }
      pending = null;
    }

    @Override
    public boolean hasNext() {
      return hasPending;
    }

    @Override
    public OCLElement next() {
      if (!hasPending) {
        throw new NoSuchElementException();
      }
      OCLElement result = pending;
      advance();
      return result;
    }
  }

  /** Flat-maps one upstream element at a time into a (possibly empty) sub-sequence. */
  private static final class FlatMapIterator implements Iterator<OCLElement> {
    private final Iterator<OCLElement> upstream;
    private final Function<OCLElement, Iterator<OCLElement>> expand;
    private Iterator<OCLElement> current = java.util.Collections.emptyIterator();

    FlatMapIterator(Iterator<OCLElement> upstream, Function<OCLElement, Iterator<OCLElement>> expand) {
      this.upstream = upstream;
      this.expand = expand;
    }

    private void advance() {
      while (!current.hasNext() && upstream.hasNext()) {
        current = expand.apply(upstream.next());
      }
    }

    @Override
    public boolean hasNext() {
      advance();
      return current.hasNext();
    }

    @Override
    public OCLElement next() {
      advance();
      return current.next();
    }
  }

  /** Yields {@code first}'s elements, then {@code second}'s, pulling {@code second} lazily. */
  private static final class ConcatIterator implements Iterator<OCLElement> {
    private final Iterator<OCLElement> first;
    private final Iterator<OCLElement> second;

    ConcatIterator(Iterator<OCLElement> first, Iterator<OCLElement> second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public boolean hasNext() {
      return first.hasNext() || second.hasNext();
    }

    @Override
    public OCLElement next() {
      if (first.hasNext()) {
        return first.next();
      }
      if (second.hasNext()) {
        return second.next();
      }
      throw new NoSuchElementException();
    }
  }

  /** Alternating dual-buffer intersection; see {@link #intersect}. */
  private static final class IntersectIterator implements Iterator<OCLElement> {
    private final Iterator<OCLElement> left;
    private final Iterator<OCLElement> right;
    private final BiPredicate<OCLElement, OCLElement> semanticEquals;
    private final List<OCLElement> leftSeen = new ArrayList<>();
    private final List<OCLElement> rightSeen = new ArrayList<>();
    private boolean onLeft = true;
    private boolean leftExhausted;
    private boolean rightExhausted;
    private OCLElement pending;
    private boolean hasPending;

    IntersectIterator(
        Iterator<OCLElement> left,
        Iterator<OCLElement> right,
        BiPredicate<OCLElement, OCLElement> semanticEquals) {
      this.left = left;
      this.right = right;
      this.semanticEquals = semanticEquals;
      advance();
    }

    private boolean containsSemantically(List<OCLElement> buffer, OCLElement elem) {
      for (OCLElement b : buffer) {
        if (semanticEquals.test(b, elem)) {
          return true;
        }
      }
      return false;
    }

    @SuppressWarnings("java:S3776")
    private void advance() {
      hasPending = false;
      while (!leftExhausted || !rightExhausted) {
        if (onLeft && leftExhausted) {
          onLeft = false;
          continue;
        }
        if (!onLeft && rightExhausted) {
          onLeft = true;
          continue;
        }

        if (onLeft) {
          if (!left.hasNext()) {
            leftExhausted = true;
            continue;
          }
          OCLElement candidate = left.next();
          onLeft = rightExhausted; // switch sides, unless the other side is already drained
          if (containsSemantically(rightSeen, candidate)) {
            pending = candidate;
            hasPending = true;
            return;
          }
          leftSeen.add(candidate);
        } else {
          if (!right.hasNext()) {
            rightExhausted = true;
            continue;
          }
          OCLElement candidate = right.next();
          onLeft = !leftExhausted; // switch sides, unless the other side is already drained
          if (containsSemantically(leftSeen, candidate)) {
            pending = candidate;
            hasPending = true;
            return;
          }
          rightSeen.add(candidate);
        }
      }
    }

    @Override
    public boolean hasNext() {
      return hasPending;
    }

    @Override
    public OCLElement next() {
      if (!hasPending) {
        throw new NoSuchElementException();
      }
      OCLElement result = pending;
      advance();
      return result;
    }
  }
}
