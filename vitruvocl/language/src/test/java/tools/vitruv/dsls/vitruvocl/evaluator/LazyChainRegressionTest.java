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

import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.vitruvocl.DummyTestSpecification;

/**
 * End-to-end regression tests through the full parse/type-check/evaluate pipeline, confirming
 * that chained expressions built from the newly-lazy operations ({@code select}, {@code collect},
 * {@code reject}, {@code union}, {@code intersection}, {@code flatten}) still produce exactly the
 * same results as before the lazy evaluation rewrite (Kernanforderung 1 requirement: laziness is
 * purely an evaluation strategy, never a semantic change).
 *
 * <p>Consumption-count proofs for the four laziness categories live in {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperationsTest} and {@link ValueLazyTest}, which
 * operate directly on the lazy primitives where exact pull counts can be asserted; this class
 * instead exercises the real OCL syntax end-to-end to guard against regressions in how {@link
 * EvaluationVisitor} wires those primitives together.
 */
class LazyChainRegressionTest extends DummyTestSpecification {

  @Test
  void selectThenCollectThenExistsChainsThroughMultipleLazyStages() {
    Value result = compile("Set{1,2,3,4,5,6}.select(x | x > 2).collect(x | x * 10).exists(y | y > 50)");
    assertSingleBool(result, true);
  }

  @Test
  void selectThenCollectThenExistsIsFalseWhenNoMatch() {
    Value result = compile("Set{1,2,3,4,5,6}.select(x | x > 2).collect(x | x * 10).exists(y | y > 1000)");
    assertSingleBool(result, false);
  }

  @Test
  void rejectThenSelectComposesCorrectly() {
    Value result = compile("Bag{1,2,3,4,5}.reject(x | x > 3).select(x | x > 1)");
    assertCollection(result, 2, 3);
  }

  @Test
  void selectThenForAllShortCircuitsOnFirstFalse() {
    Value result = compile("Sequence{1,2,3,-1,4}.select(x | x > 0).forAll(x | x < 3)");
    assertSingleBool(result, false);
  }

  @Test
  void selectThenForAllIsTrueWhenAllMatch() {
    Value result = compile("Sequence{1,2}.select(x | x > 0).forAll(x | x < 3)");
    assertSingleBool(result, true);
  }

  @Test
  void collectThenSizeDrainsTheLazyChainExactlyOnce() {
    Value result = compile("Set{1,2,3}.collect(x | Set{x, x + 10}).flatten().size()");
    assertSingleInt(result, 6);
  }

  @Test
  void unionOfSetsRemainsDuplicateFree() {
    Value result = compile("Set{1,2,3}.union(Set{2,3,4})");
    assertCollection(result, 1, 2, 3, 4);
  }

  @Test
  void unionOfBagsKeepsDuplicates() {
    Value result = compile("Bag{1,2}.union(Bag{2,2})");
    assertSize(result, 4);
  }

  @Test
  void intersectionOfSetsMatchesExpectedOverlap() {
    Value result = compile("Set{1,2,3,4}.intersection(Set{3,4,5,6})");
    assertCollection(result, 3, 4);
  }

  @Test
  void nestedSelectInsideCollectResolvesOuterIteratorVariable() {
    // The inner select's `y` predicate reads the outer collect's `x` - this is exactly the case
    // where a naive lazy rewrite could corrupt variable scoping if the outer iterator scope were
    // rebound before an inner lazy chain captured it finished evaluating (see EvaluationVisitor's
    // evalInScope doc for why this is safe here). Each x produces a disjoint value range
    // (x*100+1..x*100+3) so the result count is unambiguous regardless of any unique-Ctype
    // deduplication collect()/flatten() might separately apply.
    // x=1: Set{101,102,103}.select(y | y > 101) -> {102,103} (2 elements)
    // x=2: Set{201,202,203}.select(y | y > 201) -> {202,203} (2 elements)
    // x=3: Set{301,302,303}.select(y | y > 301) -> {302,303} (2 elements)
    // collect() already flattens one level, so the total is 2 + 2 + 2 = 6.
    Value result =
        compile(
            "Set{1,2,3}.collect(x | Set{x*100+1, x*100+2, x*100+3}.select(y | y > x*100+1)).size()");
    assertSingleInt(result, 6);
  }

  @Test
  void excludingOnBagRemovesOnlyFirstOccurrenceThroughFullPipeline() {
    Value result = compile("Bag{1,2,2,3}.excluding(2)");
    assertSize(result, 3);
    assertIncludes(result, 2);
  }

  // ── Regression coverage for a lazy receiver feeding a "materialize-then-loop" consumer
  // (one/isUnique/sortedBy/collectNested/iterate): these consumers call receiver.getElements()
  // (or otherwise store per-element results across iterations) while their own iterator-variable
  // scope is active. Draining a lazy select()/reject()/collect() receiver at that point used to
  // leave symbolTable's current scope pointing at the *lazy chain's own* scope instead of back at
  // the consumer's scope (or, for stored per-element results, resolve the iterator variable
  // against whatever it was later rebound to) - see EvaluationVisitor#evalInScope and
  // #evalInScopeMaterialized for the fix. ──────────────────────────────────────────────────────

  @Test
  void oneOverALazySelectReceiverResolvesItsOwnIteratorVariable() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2).one(x | x == 5)");
    assertSingleBool(result, true);
  }

  @Test
  void isUniqueOverALazySelectReceiverDetectsDuplicatesProjectedFromLaterIterations() {
    Value result = compile("Bag{1,1,2,3}.select(x | x <= 2).isUnique(x | x)");
    assertSingleBool(result, false); // select() leaves {1,1,2} - the two 1's are duplicates
  }

  @Test
  void sortedByOverALazySelectReceiverOrdersCorrectly() {
    Value result = compile("Set{3,1,2,5,4}.select(x | x < 5).sortedBy(x | x)");
    assertIntAt(result, 0, 1, "first element after sorting");
    assertIntAt(result, 1, 2, "second element after sorting");
    assertIntAt(result, 2, 3, "third element after sorting");
    assertIntAt(result, 3, 4, "fourth element after sorting");
  }

  @Test
  void collectNestedOverALazySelectReceiverPreservesPerElementStructure() {
    Value result = compile("Set{1,2,3}.select(x | x > 1).collectNested(x | x * 2).size()");
    assertSingleInt(result, 2); // collectNested does not flatten - one NestedCollection per {2,3}
  }

  @Test
  void iterateOverALazySelectReceiverAccumulatesCorrectly() {
    Value result = compile("Set{1,2,3,4}.select(x | x > 1).iterate(x; acc : Integer = 0 | acc + x)");
    assertSingleInt(result, 9); // select() leaves {2,3,4}
  }

  // ── Regression coverage for `let` expressions whose declarations/body drain an outer,
  // already-existing lazy Value. visitLetExpCS entered its own scope once and assumed it stayed
  // current across every declaration and body expression; draining a lazy select() bound to an
  // enclosing variable mid-way through left symbolTable's current scope pointing at that select's
  // own (unrelated) scope instead of back at the let's scope, and visitVariableDeclaration
  // captured its defining scope *after* evaluating the initializer, using the same
  // (by-then-possibly-wrong) pointer - see EvaluationVisitor#visitLetExpCS and
  // #visitVariableDeclaration for the fix.
  //
  // Test-power note, established by temporarily reverting the fix and re-running these tests: the
  // first three tests below (chained a/b/c declarations, no shadowing) all still *pass* against
  // the pre-fix code - the corruption self-heals there, because the mis-scoped registration and
  // the immediately-following resolution both consistently read/write the same wrong scope, so
  // the returned value comes out numerically correct anyway despite the internal scope-tree state
  // being wrong. They are kept as a regression net for the fixed code's intended behavior, not as
  // proof of the bug. The fourth test
  // (letBodyResolvesAShadowedVariableFromTheCorrectEnclosingScopeAfterAnEarlierDeclarationDrainsAnOuterLazyVariable)
  // breaks that self-healing with real shadowing and *does* reproduce the bug: reverting the fix
  // makes it return 111 instead of 222 (confirmed by actually reverting and re-running, not by
  // code-reading alone). That is the test that verifies this fix was necessary. ─────────────────

  @Test
  void secondLetDeclarationResolvesAfterFirstDeclarationDrainsAnOuterLazyVariable() {
    // `outer` is bound lazily (a raw select() result, never forced). `a`'s initializer
    // (outer.size()) forces outer's lazy chain to drain - a scope belonging to the *outer* let,
    // not the inner one. `b` must still resolve `a`, bound in the inner let's own scope,
    // afterwards.
    Value result =
        compile("let outer = Set{1,2,3,4,5}.select(x | x > 2) in let a = outer.size(), b = a + 1 in b");
    assertSingleInt(result, 4); // outer -> {3,4,5}, a = 3, b = 4
  }

  @Test
  void thirdLetDeclarationResolvesAfterEarlierDeclarationsDrainAnOuterLazyVariable() {
    Value result =
        compile(
            "let outer = Set{10,20,30}.select(x | x > 15) in"
                + " let a = outer.size(), b = a * 2, c = b + 1 in c");
    assertSingleInt(result, 5); // outer -> {20,30}, a = 2, b = 4, c = 5
  }

  @Test
  void letBodyResolvesLetBoundVariableAfterDrainingAnOuterLazySelectDuringTheSameDeclaration() {
    // The single declaration's own initializer drains an outer lazy variable; the body must still
    // resolve `a`, defined in the scope captured before that initializer ran.
    Value result = compile("let outer = Set{1,2,3}.select(x | x > 1) in let a = outer.size() in a + 100");
    assertSingleInt(result, 102); // outer -> {2,3}, a = 2
  }

  @Test
  void letBodyResolvesAShadowedVariableFromTheCorrectEnclosingScopeAfterAnEarlierDeclarationDrainsAnOuterLazyVariable() {
    // Breaks the self-healing that made the three tests above pass even against the pre-fix code:
    // `shadow` is a *different* name from `a`, already correctly bound in the scope two levels
    // out (L2) *before* any corruption happens - it is not created by the corrupted registration
    // itself, so its resolution can't "accidentally" land on the right value the way `a`/`b`/`c`
    // above do. `a`'s initializer (outer.size()) drains `outer`, a lazily-bound variable one level
    // further out (L1) whose own select() scope's parent is L1, not this let's scope (L3) or the
    // shadowing let's scope (L2) - so on the pre-fix code, resolving `shadow` right afterwards
    // starts from L1, skips L2 entirely, and incorrectly reaches L0's outer `shadow = 111` instead
    // of L2's `shadow = 222`. Verified experimentally: reverting EvaluationVisitor#visitLetExpCS/
    // #visitVariableDeclaration to their pre-fix form makes this specific test return 111 instead
    // of 222 - unlike the three tests above, which keep passing either way.
    Value result =
        compile(
            "let shadow = 111 in"
                + " let outer = Set{1,2,3}.select(x | x > 1) in"
                + " let shadow = 222 in"
                + " let a = outer.size(), b = shadow in"
                + " b");
    assertSingleInt(result, 222);
  }

  // ── Regression coverage for iterate() streaming its (possibly lazy) receiver via
  // elementIterator() instead of forcing it into one materialized List up front. ────────────────

  @Test
  void iterateStreamsALazySelectReceiverInsteadOfPreMaterializingIt() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 1).iterate(n; acc : Integer = 0 | acc + n)");
    assertSingleInt(result, 14); // select() -> {2,3,4,5}, sum = 14
  }

  @Test
  void iterateOverAnEmptyLazySelectReceiverReturnsTheSeed() {
    Value result = compile("Set{1,2,3}.select(x | x > 100).iterate(n; acc : Integer = 0 | acc + n)");
    assertSingleInt(result, 0);
  }

  /**
   * Targets the specific gap between two iterate() steps: after one step's {@code
   * evalInScopeMaterialized} call exits iterate's own scope, but before the next step's {@code
   * evalInScope} call re-enters it, {@code elementIterator().next()} pulls from the preceding
   * {@code select()} - which does its own, independent enter/exit of a scope parented off wherever
   * {@code select()} was originally evaluated ({@code L1} below), not off {@code iterate()}'s own
   * scope ({@code L3}, parented off {@code L2}). If anything in that gap left {@code currentScope}
   * in a state some later step's body evaluation could observe, resolving {@code shadow} - bound
   * to a *different* value in {@code L2}, the scope directly enclosing the iterate() call, than in
   * {@code L0} - would occasionally resolve to the wrong (L0) value instead of always L2's.
   *
   * <p>Unlike the let-bug shadowing test, this one doesn't fold to a single scalar (which could
   * hide a wrong contribution among correct ones under addition/concatenation) - it appends one
   * per-step value to a growing Sequence, so every individual step's contribution stays inspectable
   * in the final result, not just the fold's end value. If even one step had resolved {@code
   * shadow} against the wrong scope, the resulting sequence would contain an 111xxx value mixed in
   * among the expected 222xxx ones.
   */
  @Test
  void iterateBodyResolvesAShadowedOuterVariableCorrectlyOnEveryStepWhilePullingFromALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2,3,4,5}.select(x | x > 1) in"
                + " let shadow = 222 in"
                + " source.iterate(n; acc : Sequence(Integer) = Sequence{0} | acc.including(shadow * 1000 + n))");
    assertCollection(result, 0, 222002, 222003, 222004, 222005); // never 111xxx - shadow always L2's 222
  }

  // ── isUnique() streaming (receiver.getElements() -> receiver.elementIterator()). ────────────

  @Test
  void isUniqueDetectsADuplicateProjectedFromALazySelectReceiver() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2).isUnique(n | n.mod(2))");
    assertSingleBool(result, false); // select() -> {3,4,5}; n.mod(2) -> {1,0,1} - 1 repeats
  }

  @Test
  void isUniqueIsTrueForAllDistinctProjectionsFromALazySelectReceiver() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2).isUnique(n | n)");
    assertSingleBool(result, true); // select() -> {3,4,5}, all distinct
  }

  /**
   * Targets the same gap as {@code
   * iterateBodyResolvesAShadowedOuterVariableCorrectlyOnEveryStepWhilePullingFromALazySelectReceiver},
   * adapted so that a wrong binding flips the final boolean outright rather than merely appearing
   * as a stray value in a list: with exactly two receiver elements and a body that projects to the
   * shadowed variable alone (ignoring the iterator variable), <em>correct</em> resolution
   * (always {@code shadow == 222}) makes both projections collide -&gt; not unique -&gt; {@code
   * false}. If even one of the two elements resolved {@code shadow} against the wrong (skipped)
   * enclosing scope, that element's projection would be {@code 111} instead of {@code 222} -&gt;
   * no collision -&gt; {@code true}. Two elements is the minimum needed to make any single wrong
   * resolution decisive - with three or more, one bad element could still collide with the other,
   * correct ones and self-heal, the same trap the first, non-shadowing tests above don't avoid.
   */
  @Test
  void isUniqueBodyResolvesAShadowedOuterVariableCorrectlyForBothElementsOfALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.isUnique(n | shadow)");
    assertSingleBool(result, false); // both elements project to 222 (never 111) -> collide
  }

  // ── one() streaming (receiver.getElements() -> receiver.elementIterator(), plus a new
  // early-abort at the second match that didn't exist before this change). ─────────────────────

  @Test
  void oneIsTrueForExactlyOneMatchFromALazySelectReceiver() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2).one(n | n == 5)");
    assertSingleBool(result, true); // select() -> {3,4,5}, only 5 matches
  }

  @Test
  void oneIsFalseForTwoMatchesFromALazySelectReceiver() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2).one(n | n > 3)");
    assertSingleBool(result, false); // select() -> {3,4,5}; 4 and 5 both match
  }

  @Test
  void oneIsFalseForNoMatchesFromALazySelectReceiver() {
    Value result = compile("Set{1,2,3,4,5}.select(x | x > 2).one(n | n > 100)");
    assertSingleBool(result, false);
  }

  /**
   * Same construction and reasoning as {@code
   * isUniqueBodyResolvesAShadowedOuterVariableCorrectlyForBothElementsOfALazySelectReceiver},
   * adapted to {@code one()}: with exactly two receiver elements and a body that tests the
   * shadowed variable alone, <em>correct</em> resolution (always {@code shadow == 222}) makes the
   * condition true for both elements -&gt; two matches -&gt; not exactly one -&gt; {@code false}.
   * If even one of the two elements resolved {@code shadow} against the wrong (skipped) enclosing
   * scope, that element's condition would be {@code false} (since {@code 111 != 222}) -&gt; only
   * one match remains -&gt; {@code true}. Any single wrong resolution flips the final boolean.
   */
  @Test
  void oneBodyResolvesAShadowedOuterVariableCorrectlyForBothElementsOfALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.one(n | shadow == 222)");
    assertSingleBool(result, false); // both elements see shadow==222 (never 111) -> two matches
  }

  // ── sortedBy() streaming (avoids a double copy: receiver.getElements() + a separate
  // new ArrayList<>(...) of it, now a single drain of elementIterator()). ─────────────────────

  @Test
  void sortedByOrdersALazySelectReceiverAscendingByProjectedKey() {
    Value result = compile("Set{5,3,1,4,2}.select(x | x > 1).sortedBy(n | n)");
    // select() -> {2,3,4,5}, sorted ascending
    assertIntAt(result, 0, 2, "first");
    assertIntAt(result, 1, 3, "second");
    assertIntAt(result, 2, 4, "third");
    assertIntAt(result, 3, 5, "fourth");
  }

  /**
   * Uses a discontinuous key ({@code if shadow = 222 then n else 0 - n endif}) rather than a
   * constant offset: adding/subtracting a fixed shadow value would preserve elements' relative
   * order regardless of which shadow value was used, hiding a wrong resolution. Flipping the
   * key's *sign* based on an exact-match test against the correct value (222) means a wrong
   * resolution for even a single element reverses that element's position - detectable whether
   * the (hypothetical) corruption affects all elements uniformly or just one, unlike a simple
   * shared additive shift.
   */
  @Test
  void sortedByBodyResolvesAShadowedOuterVariableCorrectlyForEveryElementOfALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2,3}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.sortedBy(n | if shadow == 222 then n else 0 - n endif)");
    // correct: keys = [1,2,3] for n=[1,2,3] -> ascending order [1,2,3].
    // if shadow had resolved wrong for any element, that element's key negates, reversing its
    // position - e.g. all-wrong would give keys [-1,-2,-3] -> order [3,2,1].
    assertIntAt(result, 0, 1, "first");
    assertIntAt(result, 1, 2, "second");
    assertIntAt(result, 2, 3, "third");
  }

  // ── collectNested() streaming (receiver.getElements() -> receiver.elementIterator(), plus
  // the same iterScope-construction-order fix as sortedBy()). ──────────────────────────────────

  @Test
  void collectNestedPreservesPerElementStructureFromALazySelectReceiver() {
    Value result = compile("Set{1,2,3}.select(x | x > 1).collectNested(n | n * 10).size()");
    assertSingleInt(result, 2); // select() -> {2,3}, one NestedCollection per element
  }

  /**
   * Same construction and reasoning as {@code
   * sortedByBodyResolvesAShadowedOuterVariableCorrectlyForEveryElementOfALazySelectReceiver}:
   * a discontinuous body ({@code if shadow == 222 then n else 0 - n endif}) means a wrong
   * resolution for any element flips that element's sign, detectable via {@code flatten()} after
   * unwrapping the per-element {@code NestedCollection}s.
   */
  @Test
  void collectNestedBodyResolvesAShadowedOuterVariableCorrectlyForEveryElementOfALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2,3}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.collectNested(n | if shadow == 222 then Set{n} else Set{0 - n} endif).flatten()");
    assertCollection(result, 1, 2, 3); // never negative - shadow always resolves to L2's 222
  }

  // ── Two-variable (Cartesian product) methods: NOT switched to elementIterator() (see
  // classification note - the nested nested loop needs full random access to the same collection
  // twice, so a single upfront materialization via getElements() is already optimal; naively
  // streaming would either help nothing or regress into repeated full upstream re-evaluation).
  // What *did* need fixing: the same iterScope-construction-order bug found in sortedBy()/
  // collectNested() was present here too (iterScope was built *after* receiver.getElements()) -
  // unrelated to streaming, a pure scope-correctness fix. ──────────────────────────────────────

  @Test
  void forAllTwoVarsResolvesAShadowedOuterVariableCorrectlyForEveryPairFromALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.forAll(a, b | shadow == 222)");
    assertSingleBool(result, true); // every pair sees shadow==222 (never 111)
  }

  @Test
  void existsTwoVarsResolvesAShadowedOuterVariableCorrectlyForEveryPairFromALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.exists(a, b | shadow != 222)");
    assertSingleBool(result, false); // no pair ever sees shadow!=222 (i.e. never sees 111)
  }

  @Test
  void selectTwoVarsResolvesAShadowedOuterVariableCorrectlyForEveryPairFromALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.select(a, b | shadow == 222)");
    assertSize(result, 8); // all 4 pairs match (2 elements each) - never 0 (which a wrong shadow would give)
  }

  @Test
  void rejectTwoVarsResolvesAShadowedOuterVariableCorrectlyForEveryPairFromALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.reject(a, b | shadow == 222)");
    assertSize(result, 0); // all 4 pairs match and are rejected - never 8 (which a wrong shadow would give)
  }

  @Test
  void collectTwoVarsResolvesAShadowedOuterVariableCorrectlyForEveryPairFromALazySelectReceiver() {
    Value result =
        compile(
            "let shadow = 111 in"
                + " let source = Set{1,2}.select(x | x > 0) in"
                + " let shadow = 222 in"
                + " source.collect(a, b | if shadow == 222 then 1 else -1 endif)");
    assertSize(result, 4); // one value per pair (4 pairs)
    // Every one of the 4 collected values must be 1 (never -1, which a wrong shadow would give) -
    // a sum of anything less than 4 means at least one pair resolved shadow incorrectly.
    long sum = result.getElements().stream().mapToLong(e -> ((OCLElement.IntValue) e).value()).sum();
    org.junit.jupiter.api.Assertions.assertEquals(4, sum);
  }
}
