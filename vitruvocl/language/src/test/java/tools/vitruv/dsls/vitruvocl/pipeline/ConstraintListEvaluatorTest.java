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

package tools.vitruv.dsls.vitruvocl.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ConstraintListEvaluator}'s generic parallel-map-preserving-order contract,
 * independent of the OCL compiler.
 */
class ConstraintListEvaluatorTest {

  private static ConstraintResult resultFor(String constraint) {
    return new ConstraintResult(constraint, true, List.of(), List.of(), List.of());
  }

  @Test
  void evaluate_returnsEmptyList_forEmptyInput() {
    List<ConstraintResult> results =
        ConstraintListEvaluator.evaluate(List.of(), ConstraintListEvaluatorTest::resultFor, 4);
    assertTrue(results.isEmpty());
  }

  @Test
  void evaluate_rejectsThreadPoolSizeLessThanOne() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ConstraintListEvaluator.evaluate(
                List.of("c0"), ConstraintListEvaluatorTest::resultFor, 0));
  }

  @Test
  void evaluate_invokesEvaluatorExactlyOncePerConstraint() {
    int n = 50;
    List<String> constraints = IntStream.range(0, n).mapToObj(i -> "c" + i).toList();
    ConcurrentHashMap<String, AtomicInteger> callCounts = new ConcurrentHashMap<>();

    ConstraintListEvaluator.evaluate(
        constraints,
        c -> {
          callCounts.computeIfAbsent(c, k -> new AtomicInteger()).incrementAndGet();
          return resultFor(c);
        },
        8);

    assertEquals(n, callCounts.size(), "Every constraint should have been evaluated");
    callCounts.forEach(
        (c, count) ->
            assertEquals(1, count.get(), "Constraint " + c + " should be evaluated exactly once"));
  }

  /**
   * Deliberately makes later-submitted tasks finish first (index {@code n-1} sleeps least, index
   * {@code 0} sleeps most) so completion order is the reverse of submission order. The returned
   * result list must still match input order, proving results are reassembled by submission index
   * rather than by whichever worker thread finishes first.
   */
  @Test
  void evaluate_preservesInputOrderRegardlessOfCompletionOrder() {
    int n = 12;
    List<String> constraints = IntStream.range(0, n).mapToObj(i -> "c" + i).toList();

    List<ConstraintResult> results =
        ConstraintListEvaluator.evaluate(
            constraints,
            c -> {
              int index = Integer.parseInt(c.substring(1));
              try {
                // index 0 sleeps longest, index n-1 sleeps ~0 -> completion order is reversed
                Thread.sleep((long) (n - index) * 15);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              return resultFor(c);
            },
            n);

    List<String> resultOrder = results.stream().map(ConstraintResult::getConstraint).toList();
    assertEquals(constraints, resultOrder, "Results must be returned in submission order");
  }

  /**
   * With a pool size matching the constraint count and all tasks blocking on a shared {@link
   * CyclicBarrier}, the barrier can only trip if every task is actually running concurrently — a
   * pool that silently serialized tasks (or a submission bug that ran everything on one thread)
   * would deadlock and this test would time out.
   */
  @Test
  void evaluate_actuallyRunsTasksConcurrently() throws Exception {
    int poolSize = 6;
    List<String> constraints = IntStream.range(0, poolSize).mapToObj(i -> "c" + i).toList();
    CyclicBarrier barrier = new CyclicBarrier(poolSize);
    Set<Thread> threadsUsed = ConcurrentHashMap.newKeySet();

    List<ConstraintResult> results =
        ConstraintListEvaluator.evaluate(
            constraints,
            c -> {
              threadsUsed.add(Thread.currentThread());
              try {
                barrier.await(10, java.util.concurrent.TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              } catch (java.util.concurrent.BrokenBarrierException | TimeoutException e) {
                throw new IllegalStateException("Tasks did not run concurrently", e);
              }
              return resultFor(c);
            },
            poolSize);

    assertEquals(poolSize, results.size());
    assertEquals(poolSize, threadsUsed.size(), "Every task should have run on its own thread");
  }

  @Test
  void evaluate_withPoolSizeOne_stillCompletesAllTasksInOrder() {
    int n = 8;
    List<String> constraints = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      constraints.add("c" + i);
    }

    List<ConstraintResult> results =
        ConstraintListEvaluator.evaluate(constraints, ConstraintListEvaluatorTest::resultFor, 1);

    assertEquals(n, results.size());
    for (int i = 0; i < n; i++) {
      assertEquals("c" + i, results.get(i).getConstraint());
    }
  }
}
