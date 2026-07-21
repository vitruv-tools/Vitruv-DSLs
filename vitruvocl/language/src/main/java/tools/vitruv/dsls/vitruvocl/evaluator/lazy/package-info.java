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

/**
 * Lazy, incremental evaluation infrastructure for the VitruvOCL collection operations.
 *
 * <h2>Scope</h2>
 *
 * <p>This package changes <b>evaluation strategy only</b>, never OCL# language semantics. OCL#'s
 * safety theorem (finite collections, non-recursive {@code iterate()}) continues to hold
 * unchanged: no infinite collections are introduced (unlike Tisi et al., "Lazy Evaluation for
 * OCL", which does exactly that for standard OCL). Every collection here is still backed,
 * transitively, by a finite, already-materialized source ({@code allInstances()} results or
 * collection literals) — laziness here means "don't do more work than necessary to answer the
 * question actually asked", not "support unbounded collections". Parallelism, concurrency, and
 * out-of-memory models are explicitly out of scope and are not prepared for here.
 *
 * <h2>Classification of collection operations</h2>
 *
 * <p>Every VitruvOCL collection operation falls into exactly one of four categories with respect
 * to laziness:
 *
 * <h3>1. Fully lazy, unrestricted materialization</h3>
 *
 * <p>{@code select}, {@code reject}, {@code collect} (single- and two-variable forms), implicit
 * navigation ({@code e.p}, both singleton and collection-valued receivers), {@code flatten()}
 * (the outer level is unpacked lazily; inner nested collections stay lazy themselves and are only
 * consumed when the outer consumer actually asks for their elements), and {@code union}/{@code
 * merge}/{@code append} on {@code Bag} and {@code Sequence} receivers (pure interleaving /
 * concatenation, no duplicate check). These operations pass elements through without ever needing
 * to consume the full source themselves — how much is actually pulled is entirely up to the
 * outer, consuming expression. Implemented via {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperations#filter} and {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperations#flatMap}.
 *
 * <h3>2. Lazy with early termination, worst case strict</h3>
 *
 * <p>{@code exists}, {@code any}, {@code notEmpty()}/{@code isEmpty()}, {@code includes}/{@code
 * excludes}, {@code includesAll}/{@code excludesAll}: these stop pulling as soon as the result is
 * logically decided (first hit for {@code exists}/{@code any}/{@code includes}, first element for
 * {@code notEmpty()}), but in the worst case (no hit, or — for {@code excludes}/{@code
 * excludesAll} — no counter-example found) they must consume the entire source. {@code forAll} is
 * the special case: it short-circuits on the first {@code false}, but is structurally strict in
 * the success ({@code true}) case, since every element must be checked. These consumers pull via
 * {@link tools.vitruv.dsls.vitruvocl.evaluator.Value#elementIterator()} instead of {@code
 * getElements()} so that an upstream lazy chain (category 1) can also stop early.
 *
 * <h3>3. Lazy with incremental buffer reconciliation instead of full traversal</h3>
 *
 * <p>{@code union}/{@code merge} on {@code Set}/{@code OrderedSet} receivers (uniqueness check)
 * and {@code intersection}. For the uniqueness check, each newly pulled element is tested against
 * a buffer of already-accepted (confirmed unique) elements; the buffer only grows with elements
 * that have actually been consumed and confirmed unique, never with the full source size. See
 * {@link tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperations#dedupe}. For {@code
 * intersection}, both input collections are pulled in alternation: each newly drawn element from
 * the "current" side is checked against the buffer of already-seen elements on the *other* side;
 * on a match it is emitted to the result, on a miss it is added to its own side's buffer and the
 * next pull switches to the other side. This allows early result elements without requiring
 * either input to be fully materialized up front. See {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperations#intersect}.
 *
 * <h3>4. Structurally strict, no meaningful lazy variant</h3>
 *
 * <p>Aggregations ({@code sum}, {@code max}, {@code min}, {@code avg}), {@code size()}, {@code
 * sortedBy}, {@code isUnique()}, {@code one()}, {@code iterate()}, {@code collectNested()} (see the
 * dedicated caveat below), and semantic equality ({@code Value.semanticEquals}, which relies on
 * full sort/normalization of both sides). These operations must themselves consume their entire
 * source — there is no way to answer "what is the sum" or "are these two bags equal" without
 * seeing every element. They are not converted to lazy producers. However, when their
 * <em>source</em> is itself a chain of lazy category-1/2/3 steps (e.g. {@code
 * select(...)->collect(...)->sum()}), they still benefit: the chain is drained exactly once via
 * {@link tools.vitruv.dsls.vitruvocl.evaluator.Value#getElements()} (which materializes an
 * unmaterialized lazy {@code Value} by draining its {@code OCLElementSource} once and caching the
 * result), instead of each intermediate step allocating and copying its own fully materialized
 * {@code ArrayList}.
 *
 * <h2>Deliberate tradeoffs (not oversights — recorded here so they don't have to be
 * rediscovered)</h2>
 *
 * <h3>{@code collectNested()} forces immediate materialization of each element's projection</h3>
 *
 * <p>Unlike {@code flatten()}, whose javadoc (and category 1 above) promises that an inner nested
 * collection "stays lazy until the outer consumer actually asks for its elements", {@code
 * collectNested()}'s per-element body result is forced to fully materialize (via {@code
 * EvaluationVisitor#evalInScopeMaterialized}) <em>before</em> being wrapped in an {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.OCLElement.NestedCollection}, i.e. before it is stored for
 * later, possibly much later, consumption. This is intentional, not an accident: {@code
 * collectNested}'s per-element scope binding is a single, reused {@link
 * tools.vitruv.dsls.vitruvocl.symboltable.VariableSymbol} that gets rebound to the next element as
 * soon as the loop advances (see the rebind-not-reallocate design in {@code
 * EvaluationVisitor#defineIterVar}/{@code #rebindIterVar}). If the wrapped {@code Value} were left
 * lazy, consuming it later — e.g. via a subsequent {@code flatten()} call — would resolve its
 * iterator variable against whatever the shared symbol has since been rebound to, not the element
 * it was actually computed for. Forcing materialization at wrap time is the price of that reuse.
 * <b>Consequence:</b> a {@code collectNested(...)->flatten()} pipeline does <em>not</em> get the
 * "inner collections stay lazy" benefit that {@code flatten()} otherwise provides for a nested
 * collection sourced some other way (e.g. one already held in a variable) — every {@code
 * collectNested} projection is fully computed up front, only the outer {@code flatten()} step
 * remains lazy. A version of {@code collectNested()} that avoided this would need a fresh, not
 * reused, symbol per element (undoing the rebind optimization for this one operation) — judged not
 * worth the extra allocation given {@code collectNested()} is not on the classification list the
 * lazy evaluation task targeted in the first place.
 *
 * <h3>Two-variable (Cartesian product) iterators remain eager</h3>
 *
 * <p>{@code select}, {@code reject}, {@code collect}, {@code forAll}, and {@code exists} all have a
 * two-iterator-variable form (e.g. {@code s->select(a, b | ...)}, ranging over the Cartesian
 * product of the receiver with itself). These are <em>not</em> converted to lazy producers — they
 * still materialize the receiver into a plain {@code List} and run nested {@code for} loops. A
 * lazy Cartesian product would need its own dedicated iterator (pulling the outer element once per
 * inner exhaustion, symmetric to {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperations}'s {@code FlatMapIterator}) and was
 * judged lower value than the single-variable forms, which are both more common in practice and
 * were the forms the task's classification actually enumerated. The two-variable methods were
 * still touched during this work — for the scope-corruption fix described below, and to
 * materialize their receiver before entering their own scope — but not to make them lazy.
 *
 * <h2>Known divergences this package must not touch</h2>
 *
 * <p>Navigation deduplication for unique combined Ctypes (see {@code
 * EvaluationVisitor#visitPropertyAccessWithReceiver}) and the "no object" propagation for {@code
 * div}/{@code mod}/{@code at}/{@code reverse} are pre-existing, independent semantic fixes. The
 * lazy navigation implementation in this package preserves the former by using {@link
 * tools.vitruv.dsls.vitruvocl.evaluator.lazy.LazyOperations#dedupe} inline instead of a
 * post-hoc {@code removeDuplicates()} call; it does not touch the latter at all.
 *
 * <h2>Symbol-table scope correctness under laziness</h2>
 *
 * <p>Introducing lazy {@code Value}s exposed a correctness hazard that does not exist under strict
 * evaluation: {@code SymbolTableImpl.enterScope}/{@code exitScope} operate on a single mutable
 * "current scope" pointer, not a call stack. Forcing a lazy chain to materialize inside a block
 * that has its own scope entered — e.g. a {@code one()}/{@code isUnique()}/{@code sortedBy()}/
 * {@code collectNested()}/{@code iterate()} consumer draining a lazy {@code select()} receiver, or
 * a {@code let} declaration's initializer draining an outer lazy variable — enters and exits the
 * lazy chain's <em>own</em> scope along the way, leaving the current-scope pointer at that scope's
 * (unrelated) parent instead of back at the caller's scope. See {@code
 * EvaluationVisitor#exitScope(Scope)}'s javadoc for the full explanation and {@code
 * EvaluationVisitor#evalInScopeMaterialized} for the companion fix (forcing materialization of a
 * per-element result *before* it is stored for later consumption, whenever the consuming operation
 * doesn't immediately drain it within the same element's evaluation). Regression tests for this
 * live in {@code LazyChainRegressionTest}.
 */
package tools.vitruv.dsls.vitruvocl.evaluator.lazy;
