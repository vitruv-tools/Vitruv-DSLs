# Incremental Constraint Evaluation for VitruviusOCL — Implementation Report

Status: level 1 implemented (stopped for review/commit); levels 2 and 3 pending. Branch `vitruviusocl_parallel`, base HEAD `39476b07`.
Plan: `docs/incremental-plan.md`. Exploration: `docs/incremental-exploration.md`.

Labels: **[Code]** verified in the code · **[Test]** backed by a test (test name given) · **[Paper n]** taken from paper n · **[Assumption]** not verified.
Papers: 1 = Cabot & Teniente 2009 (JSS 82); 2 = Cabot & Teniente 2006 (CAiSE); 3 = Fernández-Álvarez et al. 2016 (ICSOFT); 4 = Martínez-Lasaca et al. 2026 (ReOCL, delimitation only).
Paths are relative to `language/src/main/java/tools/vitruv/dsls/vitruvocl/` unless stated otherwise; tests are under `language/src/test/java/tools/vitruv/dsls/vitruvocl/`.

Nothing in this report is a performance result. No measurements were taken in this repository.

---

## 1. Motivation and Problem

In Vitruvius, consistency preservation (Reactions) modifies models itself, so every transaction can change models the user never touched. Re-evaluating every constraint on every instance after each transaction therefore does work proportional to the model size even when a transaction changes only a few elements. The goal is to re-evaluate only those (constraint, instance) pairs that the changes recorded during the transaction can affect, while producing exactly the same set of violations as a full evaluation. (Motivation, not a measured result.)

## 2. Approach

The incremental evaluator is organised in levels that can be switched individually (`IncrementalLevel`; system property `vitruvocl.incremental.level`; CLI option `--incremental-level`). Every level must produce the same violation set as level 0.

| Level | Idea | Status |
|---|---|---|
| 0 | Full evaluation after every transaction (baseline) | implemented; same results as the pre-existing batch path [Test: `IncrementalCliTest`, oracle tests] |
| 1 | Per-constraint read-set filter without event direction | implemented (§5.1) |
| 2 | Polarity-based PSE filter | planned |
| 3 | Relevant instances | planned |

| Building block | Source | Classification | Status |
|---|---|---|---|
| PSE computation by AST annotation with `+`/`−`/`und` | [Paper 1, §3.1, Tab. 3.1–3.4] | adapted | planned (level 2) |
| Instance vs. class events, relevant instances via `Inverse(PathVar)` | [Paper 1, §3.2] | adapted | planned (level 3) |
| Union of the effects of all events of a transaction | [Paper 2, Prop. 4.2] | adopted | implemented: one deduplicated event list per transaction (§4) |
| Event simplification / deduplication in a transaction context, collecting all violations | [Paper 3, §5.5] | adapted (only safe rules, §4) | implemented for level 1 |
| Violation set instead of "consistent before" | — | own | implemented (§6, D) |
| Differential testing against full evaluation (oracle) | — | own | implemented (§7) |
| Read-set filter without event direction | — | own | implemented (level 1) |
| Idiom rule for `Y.allInstances()->select(~)` | — | own | planned, disabled by default (§3.1) |

## 3. Adaptation to Reactions

The papers assume that the events of an operation are specified in advance and that the information base is consistent before the operation [Paper 1, §3]. Neither holds here:

- **Events are observed, not specified.** The analysis of a constraint depends only on the constraint and is computed once; at run time it is matched against the EChanges recorded during the transaction. "No potentially satisfaction-violating event (PSE) occurred" therefore holds **for the observed transaction only** (for the recorded EChange sequence and its instances); it is not a proof that the Reactions can never violate the constraint. [Design decision]
- **Transaction = one `finishedChangePropagation` callback = one `commitChanges()`.** The EChange list of a transaction is, per `PropagatedChange` in callback order, the original change followed by its consequential changes (`TransactionCollector.flatten`). [Code; Test: `CorrespondenceVisibilityExperimentTest` (one callback per commit)] Whether consequential changes are internally chronological is not verified; it matters only for `@pre`, whose constraints are always evaluated in full. [Assumption]
- **Violation set instead of "consistent before".** Reactions routinely pass through inconsistent states, so the output semantics stays "all current violations". The session initialises a violation set with a full evaluation and afterwards replaces the entries of re-evaluated constraints (§6, D). Skipping is therefore only claimed to preserve a constraint's *previous outcome*, whatever it was, not its satisfaction. [Code: `ViolationSet`]
- **Transaction-dependent constraints are never filtered.** `pre`/`post` blocks, `@pre` and the lifecycle predicates `OCLisNew/OCLisModified/OCLisDeleted` depend on the transaction itself, not only on the post-state, and are evaluated after every transaction, including empty ones. Constraints with error-only grammar alternatives or failed compilation are also always evaluated (decision 6). [Code: `ReadSet.requiresFullEvaluation`, `ConstraintAnalysis.decideByReadSet`; Test: `ReadSetSkipTest.emptyTransactionIsSkippedButPrePostIsAlwaysEvaluated`, `IncrementalEdgeCaseTest.preAndPostAreAlwaysEvaluated`]

### 3.1 Correspondence changes are invisible in the EChange stream (experiment)

**Question (decision 3 of the task).** Do changes to the correspondence model appear in the EChanges of `PropagatedChange`, and can a correspondence appear or disappear without an observed event on its partner objects?

**Setup [Test: `CorrespondenceVisibilityExperimentTest`].** A real (non-mocked) VSUM is built with `VirtualModelBuilder` over two small dynamic metamodels (`incremental/support/RealVsumFixture`). A hand-written `ChangePropagationSpecification` (`incremental/support/CorrespondingElementsSpecification`) plays the role of generated Reactions code and edits the correspondence model through the same `EditableCorrespondenceModelView` API that Reactions use: it creates a partner root plus correspondence (T1), a partner element plus correspondence (T2), **adds a correspondence between two pre-existing objects** in reaction to an attribute change (T3), removes it again (T4), and removes a partner element with its correspondence (T5). A `ChangePropagationListener` records, per `finishedChangePropagation` callback, the original EChanges followed by the consequential EChanges of every `PropagatedChange`.

**Result (Vitruvius `4.0.1-SNAPSHOT` as resolved by the build) [Test].**
1. Exactly one `finishedChangePropagation` callback per `commitChanges()`.
2. No recorded EChange of any of the five transactions mentions a correspondence-model object (neither affected element nor old/new value).
3. In T3 and T4 the correspondence between the changed `AElem` and the untouched spare `BElem` appears and disappears, while the transaction consists of exactly one EChange (the attribute update on the `AElem`); the partner object is not mentioned at all.

This matches the bytecode of the resolved jars: `DefaultChangeRecordingModelRepository` attaches its `ChangeRecorder` to the models' `ResourceSet`, while the correspondence model is created separately via `CorrespondenceModelFactory.createPersistableCorrespondenceModel(URI)` [Code, javap]. The correspondence model is not reachable through the public API (`EditableCorrespondenceModelView` exposes no resource) [Code, javap], so observing it directly would require reflection, which we do not do.

**Consequence.** The default of decision 3 ("evaluate a `~` constraint whenever the transaction touches an involved type") is **not sound in general**: in T3 a correspondence involving a `BElem` changed although no `BElem` was touched. We therefore take the conservative variant: **every constraint that uses correspondence (`~`, `select/reject/exists(~ …)`) is evaluated after every non-empty transaction.** The idiom rule for `Y.allInstances()->select(~)` (level 3c) will be implemented behind its own flag (`vitruvocl.incremental.corrIdiom`), **disabled by default**. Whether real Reactions code exhibits the T3 pattern depends on the methodology; the experiment shows only that the framework permits it. [Test + Assumption]

## 4. Event model

Each recorded EChange is mapped to one event (`EChangeClassifier`, `ModelEvent`) [Code; Test: `EChangeClassifierTest`]:

| Vitruvius EChange | Event (`ModelEvent.Kind`) | Paper 1 event | Touches feature | Structural |
|---|---|---|---|---|
| `CreateEObject` | `CREATE` | InsertET | — | yes (object) |
| `DeleteEObject` | `DELETE` | DeleteET | — | yes (object) |
| `InsertRootEObject` / `RemoveRootEObject` | `INSERT_ROOT` / `REMOVE_ROOT` | InsertET / DeleteET (extension view) | — | yes (object) |
| `ReplaceSingleValuedEAttribute` (incl. set/unset) | `UPDATE_ATTRIBUTE` | UpdateAttribute | (class, attribute) | no |
| `InsertEAttributeValue` / `RemoveEAttributeValue` | `INSERT_ATTRIBUTE_VALUE` / `REMOVE_ATTRIBUTE_VALUE` | — (not in Paper 1) | (class, attribute) | no |
| `InsertEReference` / `RemoveEReference` | `INSERT_REFERENCE` / `REMOVE_REFERENCE` | InsertRT / DeleteRT | (class, reference) | iff containment (inserted/removed object) |
| `ReplaceSingleValuedEReference` | `REPLACE_REFERENCE` | DeleteRT(old) + InsertRT(new) | (class, reference) | iff containment (old and new object) |
| other `FeatureEChange` | `OTHER_FEATURE_CHANGE` | — | (class, feature) | iff containment |
| anything else | `UNKNOWN` | — | — | forces evaluation of every constraint |

"Structural" events can change the membership or the order of a type extension (the set iterated by a context or by `allInstances()`, which VitruvOCL computes by containment traversal from the roots). For every structural event, the classes of the affected object **and of all its post-state transitive contents** are recorded (`TransactionDelta.structurallyAffectedClasses`). This covers containment moves without create/delete [Test: `EChangeClassifierTest.containmentMoveIsStructuralWithoutCreateOrDelete`, `IncrementalEdgeCaseTest.containmentMoveWithoutCreateOrDelete`] and deletion of subtrees [Test: `EChangeClassifierTest.deleteIncludesTheRemovedSubtree`]. Argument for completeness: an object whose extension membership or position changes must itself, or one of its pre- or post-state ancestors, be the object of a containment/root/existence change; if it is no longer contained in that ancestor at the end of the transaction, it left it through a further such change on itself or an intermediate ancestor, and the induction ends at the object itself. [Assumption; informal argument, checked by the oracle on the tested transactions]

There is no SpecializeET/GeneralizeET: EMF objects never change their class. [Code]

**Deduplication and net effect** [Paper 3, §5.5, adapted]: identical events (same kind, object, feature, old and new value, by identity) are kept once; this is the union of Paper 2, Prop. 4.2. An insertion and a later removal of the same link are **not** cancelled, because in ordered non-unique lists another occurrence may remain at a different index [Test: `EChangeClassifierTest.duplicateEventsAreKeptOnceButLinkAndUnlinkAreNotCancelled`, `IncrementalEdgeCaseTest.linkAndUnlinkInSameCommitOnNonUniqueList`]. At level 1 the remaining Paper-3 rules ("insert supersedes update", "delete discards earlier checks", "create+delete cancels") have no effect, because level 1 decides per constraint, not per (constraint, instance) pair; they become relevant at level 3.

## 5. Analysis per construct

### 5.1 Level 1: read set (own building block)

`ReadSetAnalyzer` walks the type-checked parse tree once per constraint text and uses the static types the type checker stored (`nodeTypes`) [Code]. Rules:

| Construct (grammar rule) | Recorded in the read set | Conservative fallback |
|---|---|---|
| Classifier context `context T` | extension type `T` | unresolved type ⇒ `unknownExtension` |
| Property navigation `e.f` (`propertyNav`) | (`f`, static element class of `e`). The evaluator resolves `f` by name on the runtime class, so a change on an object of a subclass of the static class matches via `isSuperTypeOf` | receiver type unknown or not a metaclass (OclAny, primitives) ⇒ `f` counts as read on **every** class |
| `T.allInstances()` / `mm::T.allInstances()` | extension type `T` (subtypes via `isSuperTypeOf`) | receiver unknown ⇒ `unknownExtension` (every structural event counts) |
| `@message "…{self.a}…"` | (`a`, context class) — interpolated at evaluation time | — |
| `~`, `select/reject/exists(~ …)`, `Type=`/`Tag=` | flag `usesCorrespondence` ⇒ evaluated after every non-empty transaction (§3.1) | always conservative |
| `@pre`, `OCLisNew/Modified/Deleted`, `pre`/`post` blocks | flags ⇒ always evaluated | always conservative |
| error-only alternatives (`unknown*`, `invalid*`, `^`, `noArgOpWithArgs`, `opMissingParens`, `iteratorMissingBody`, unknown filter options) | flag `hasErrorNodes` ⇒ always evaluated | always conservative |
| `oclIsKindOf/oclIsTypeOf/oclAsType` | nothing (an object's class never changes) | — |
| literals, `let`, `if`, iterators, collection operations | nothing themselves; their sub-expressions are analysed | — |
| several `specificationCS` per `inv` (`inv n(spec): spec`) | union over all specifications | see §10 |
| a read that resolves (on any class conforming to the static receiver, or any class if untyped) to a **transient, derived, volatile or unchangeable** feature | flag `readsUnrecordedFeature` ⇒ always evaluated, because Vitruvius does not record changes of such features (below) | always conservative |
| a read that resolves to a **container** feature (opposite of a containment) | owner class added as extension type: the container changes exactly when the owner is moved, which is a structural event of the owner | — |
| a reference in a `@message` template (`{self.ref}`) | `readsUnrecordedFeature` (the message contains the referenced object's `toString`) | always conservative |
| any `append` or `iterate` in the constraint | every read of the constraint is recorded untyped (`append` is typed as its receiver type without a conformance check, `TypeCheckVisitor#visitAppendOp`; `iterate` accumulates body values) | always conservative |
| `allInstances()` on a step without stored type | additionally the element class of the whole navigation expression, mirroring the evaluator's fallback in `visitAllInstancesOp` | — |

The receiver type of a navigation step is the type of the previous step. Where the type checker stored no type on the step node itself, the analyzer descends only through "spine" nodes that carry the same value as the step (navigation target → operation call → concrete operation; primary expression → self/variable/literal/nested/if/let/collection or type literal), never into argument or body expressions (`ReadSetAnalyzer.spineType`). If no type is found, the read counts for every class. [Code; Test: `ReadSetAnalyzerTest`]

**Features whose changes are not recorded.** Vitruvius' `NotificationToEChangeConverter` drops notifications of transient features, and `EChangeCreationUtil` skips unchangeable, derived, transient and container features when it creates the initial values of new objects [Code, javap of the resolved `tools.vitruv.change.composite` jar]. Such features never appear in a transaction's touched features, so a level-1 filter based on recorded changes cannot see their changes. `FeatureIndex` resolves every read feature name on all classes of the known metamodels (global `EPackage` registry plus the wrapper's metamodels) and applies the two rules in the table above. [Code: `FeatureIndex`, `ReadSetAnalyzer.checkUnrecordedFeatures`; Test: `ReadSetAnalyzerTest.transientFeatureForcesFullEvaluation`, `containerFeatureMakesTheOwnerClassAnExtensionType`, `IncrementalEdgeCaseTest.transientAttributeIsAlwaysEvaluated`, `containerFeatureFollowsContainmentMoves`]

**Decision rule** (`ConstraintAnalysis.decideByReadSet`), in this order: always-evaluate flag or failed compilation ⇒ evaluate; empty transaction ⇒ skip; unclassifiable change ⇒ evaluate; correspondence ⇒ evaluate; otherwise evaluate iff (i) a changed feature name is read untyped, or (ii) a changed (runtime class, feature) conforms to a typed read, or (iii) a structurally affected class conforms to an extension type (or `unknownExtension` and any structural event). Otherwise the constraint is skipped and keeps its previous outcome. [Code; Test: `ReadSetSkipTest` — 11 cases for skip and evaluate: attributes, a feature name shared by unrelated classes, a subclass-only feature, containment lists, insertion of a context-type object and of an unrelated object, empty transactions, pre/post, correspondences, level 0]

## 6. Implementation

All new production code is in `incremental/` and `incremental/analysis/`; test code and support in the test tree under `incremental/`.

**Data flow.** `VsumWrapper.addTransactionListener` → `TransactionCollector.finishedChangePropagation` (flatten original + consequential EChanges) → `IncrementalSession.onTransaction` → `EChangeClassifier.classify` (→ `TransactionDelta`) → per constraint `ConstraintAnalysis.decideByReadSet` → evaluation of the selected constraints via `CompiledConstraint.evaluate` (sequentially, or on a session-owned pool) → `ViolationSet.replaceAll` → rendering with `ConstraintEvaluations.render` (the format of the batch API) → optional oracle (`OracleVerifier`). [Code]

**Public API** (all additive): `VitruvOCL.openIncrementalSession(List<String> | Path, IncrementalConfig)`, `openIncrementalSession(Path, Path[], Path[], IncrementalConfig)` (files), `evaluateConstraintsIncremental(session, changes)`, `attachIncrementalSession(constraints, config, onResult, onFailure)` (listener on the registered VSUM); `IncrementalSession.initialize/onTransaction/attachTo/close`; `IncrementalConfig.fromSystemProperties()` reading `vitruvocl.incremental.level|verify|corrIdiom|threads`; CLI `eval-batch … --incremental-level <0-3>`. [Code]

**A — compile once, evaluate often** (`CompiledConstraint`). Parse tree and `nodeTypes` are computed once per constraint text and session. The evaluator mutates the symbol table (`self` bindings, iterator/`let` scopes; `EvaluationVisitor.visitClassifierContextCS`), so each evaluation builds a fresh symbol table by re-running pass 1 on the cached tree; pass 2 leaves the scopes balanced and is not re-run. The evaluator never writes `nodeTypes` (no `nodeTypes.put` in `evaluator/`). A compiled constraint is evaluated by at most one task at a time. Constraints that fail to compile are re-run through the unchanged from-scratch pipeline, so their errors are exactly those of a full evaluation. [Code; Test: `IncrementalOracleTest.compiledPathEqualsFreshCompilation` — the once-compiled path equals fresh compilation for all corpus constraints on all five model sets]

**B — transaction hook.** `TransactionCollector` implements `ChangePropagationListener`. *Deviation from the task:* the listener is registered when a session is attached (`IncrementalSession.attachTo` → `VsumWrapper.addTransactionListener`) and removed on `close()`, not in the `VsumWrapper` constructor, so that level 0 / plain evaluation registers and buffers nothing. `VirtualModel` extends `ChangeableModelRepository`, so the API is reachable on every VSUM [Code, javap]. Exceptions during incremental evaluation (including oracle mismatches) go to an `onFailure` callback instead of into the change propagation. [Code; Test: `IncrementalVsumListenerTest` — real VSUM, one oracle-checked result per commit, no result after `close()`]

**D — violation set** (`ViolationSet`). Per constraint text: the last full outcome (errors, skipped pre/post notices) and the violation records keyed by instance identity. Level 1 always replaces a constraint's state completely; partial replacement is prepared for level 3. Instance labels and source files are rendered at output time, as in the batch API. [Code]

**F — test statistics** (`IncrementalStats`): constraints evaluated/skipped, forced evaluations, (constraint, instance) pairs evaluated, class-event fallbacks, partial evaluations. No timing. `EvaluationVisitor` got a pure counter (`getEvaluatedInstanceCount`). [Code]

**Changes to existing code (behaviour-preserving for level 0):**
- `pipeline/ConstraintEvaluations` and `pipeline/EvaluationOutcome`: `VitruvOCL.compileAndEvaluate` was split into `evaluateFresh` (unchanged compiler use) and `render` (unchanged formatting), so that the session and the oracle share the exact output format. [Code; Test: all pre-existing tests, §7]
- `EvaluationVisitor`: evaluated-instance counter; optional `contextInstanceOverride` (default `null` ⇒ unchanged full extension; used from level 3 on). [Code]
- `VsumWrapper`: `add/removeTransactionListener`. `VitruvOclCli`: `--incremental-level`. `language/pom.xml`: test-scoped `org.eclipse.equinox.common` (needed at run time by a real VSUM). [Code]

**Design decisions and rejected alternatives.**
- Decide per constraint at level 1 rather than per (constraint, instance): simpler, needs no inverse navigation. Per-instance filtering is level 3.
- Match features by *name* plus runtime-class conformance, because the evaluator resolves features by name on the runtime class (`navigateOne`); matching `EStructuralFeature` identity would miss same-named features declared in subclasses. [Code]
- Structural events use the actual post-state contents instead of a metamodel containment closure: precise, and no enumeration of all subclasses in all packages is needed; completeness argument in §4.
- Parallelism (decision 8): sequential by default (`threads = 1`); with `threads > 1`, a persistent pool owned by the session evaluates whole constraints. No (constraint, instance-chunk) work packages.

## 7. Correctness

**Baseline [Test]:** before any change, `mvn -o -pl language -am test` executed 2559 tests in `vitruvocl-language` (0 failures, 0 errors, 0 skipped) and 12 tests in `vitruvocl-allinstances`.

**Oracle.** With `IncrementalConfig.verify` (system property `vitruvocl.incremental.verify=true`), every incremental result is compared with a from-scratch evaluation (`ConstraintEvaluations.evaluateFresh`, i.e. the unchanged batch pipeline including fresh compilation) of the same state and transaction. Compared as multisets: violations by (instance identity, severity, message), skipped pre/post notices, and the error messages of failed evaluations. Any difference throws `IncrementalMismatchException` with a diff. [Code: `OracleVerifier`] The order of violations is not compared (§8).

**Test corpus.** 606 distinct constraint strings extracted mechanically from all existing test sources (`language/src/test/resources/incremental/corpus/constraints-from-existing-tests.txt`: string literals, text blocks and `+`-concatenations that start with `context`), plus 31 hand-written edge-case constraints over a small dynamic metamodel with inheritance (`edge-cases.txt`, `EdgeFixture`). The existing tests themselves are unchanged. Model sets from the existing test models: *space* (spaceMission + satelliteSystem + correspondences), *family* (family + persons + correspondences), *cad*, *brake* (brakesystem + cad), *components* (model + model2); the two 789 kB label-graph models are excluded for run time. All model sets go through the production `VsumWrapper` over a mocked VSUM (the pattern of the existing `VsumConstraintTest`); changes are recorded with Vitruvius' `ChangeRecorder`; correspondence edits are silent as in a real VSUM. [Test support: `LiveModel`, `FakeCorrespondenceModel`, `ModelSet`, `ConstraintCorpus`, `ModelMutator`]

**Tests for level 1** (all in oracle mode unless noted):
- `IncrementalOracleTest.buildAsOneTransaction` (strategy 2a): empty resources, then the whole model (including cross-resource references and correspondences) added as one recorded transaction; 5 model sets × levels 0 and 1.
- `IncrementalOracleTest.randomMutations` (strategy 2b): seeded random atomic mutations — attribute set/unset (single/many), reference link/unlink/replace, permutation, containment move, create, delete with and without removing dangling references, silent correspondence add/remove — first one, then 1–4 per transaction; by default 5 model sets × 2 seeds × 6 transactions (`-Dvitruvocl.incremental.mutations=N`, `-Dvitruvocl.incremental.manySeeds=true` for larger sweeps).
- `IncrementalEdgeCaseTest` (task item 3), levels 0 and 1: polymorphic subclass feature; feature name shared by unrelated classes; containment move without create/delete; create without containment; delete keeping/removing dangling references; `first()/at()` after permutation; `not/select/reject/exists`; `implies` with empty antecedent; `let/iterate`; `@pre`/`post`/`OCLisNew`; correspondence change without partner event (`Tag=`); link+unlink in one commit on a non-unique list; several `specificationCS`; multi-valued attributes; `@message` refresh; plus 4 seeds × 40 random transactions on the edge model. Scenarios also assert that they changed the outcome where intended.
- `ReadSetSkipTest` (task item 4, counters), `ReadSetAnalyzerTest`, `EChangeClassifierTest`, `IncrementalVsumListenerTest` (real VSUM), `IncrementalCliTest` (CLI levels 1–3 print the level-0 output byte for byte), `SessionRecoveryTest`.

**Oracle sensitivity [Test, manual run].** To check that the oracle tests can fail at all, level 1 was temporarily changed to skip every constraint whose decision depends on the read set. With 8 mutations per run, 12 of 40 oracle runs failed (space, cad and brake sets, and the build-as-one-transaction runs); the change was reverted. The family and components sets did not detect it with that budget (their corpus constraints are few or mostly correspondence-based, which are always evaluated).

**Errors found and fixed during level 1.** A code review of level 1 (separate review pass over the new code against the evaluator and the Vitruvius bytecode) found the first five; each was verified in the code before fixing.
- **Unrecorded features (soundness).** Reads of transient/derived/volatile/unchangeable and container features were matched only against recorded changes, which never contain them; a constraint navigating, e.g., `x.owner` (container) from an unaffected context could keep a stale outcome after a move. Cause: Vitruvius does not record these features (above). Fix: `FeatureIndex` rules in §5.1. The existing test metamodels contain no such features, which is why the oracle had not found it; the edge metamodel now has a transient attribute and a container reference. A deliberately re-introduced version of the bug makes `transientAttributeIsAlwaysEvaluated`, `containerFeatureFollowsContainmentMoves` and the random edge-model runs fail. [Test]
- **`append` typing (soundness).** `append` is typed as the receiver collection type without checking the argument's conformance (`TypeCheckVisitor#visitAppendOp`, unlike `prepend`/`insertAt`/`including`, which check, and `union`/`if`/collection literals, which use the common supertype, `ANY` for unrelated classes); a typed read after `append` could therefore miss changes on the appended object's class. Fix: constraints using `append` or `iterate` record all reads untyped. The type checker itself was not changed (its behaviour belongs to level 0). [Code; Test: `ReadSetAnalyzerTest.appendMakesAllReadsUntyped`, `IncrementalEdgeCaseTest.appendOfAnElementOutsideTheStaticElementType`]
- **Lost transaction after a failure.** If processing a transaction threw, its delta was discarded and later transactions only checked their own deltas. Fix: the session re-evaluates every constraint after a failed transaction. [Code; Test: `SessionRecoveryTest`]
- **Violation order.** Grouping records by instance changed the order of reported violations when one constraint text has several contexts over the same class. Fix: without instance-subset updates, the session reports the evaluator's order, and the oracle now also compares the order in that case. [Code]
- **CLI with unloadable model files.** `--incremental-level ≥ 1` threw instead of reporting per-constraint file errors like level 0. Fix: fall back to the level-0 path. [Code]
- Not changed, documented: `allInstances()` receiver fallback (now mirrored by the analysis, see table); correspondence constraints are skipped on empty transactions (assumption in §10).
- Test infrastructure: the test metamodel `correspondence.ecore` has the same nsURI as Vitruvius' own correspondence package, and the existing `VsumConstraintTest` registers it globally; a real VSUM created later in the same JVM would then use the wrong package. The new test support registers it only locally, and the real-VSUM fixture re-registers the generated package. [Code]
- Several edge-case scenarios initially did not do what they intended (ambiguous lookup by label; `count` is an operation name); fixed in the tests. No difference between incremental and full evaluation was found at level 1 in the runs reported here.

**Full suite after level 1 including the review fixes [Test]:** `mvn -o -pl language -am test` executed **2660** tests in `vitruvocl-language` — the 2559 pre-existing tests (unchanged; they use only the level-0 path) plus 101 new ones (`IncrementalEdgeCaseTest` 41, `IncrementalOracleTest` 25, `ReadSetAnalyzerTest` 11, `ReadSetSkipTest` 11, `EChangeClassifierTest` 6, `IncrementalCliTest` 3, `IncrementalVsumListenerTest` 2, `CorrespondenceVisibilityExperimentTest` 1, `SessionRecoveryTest` 1) — with 0 failures, 0 errors, 0 skipped; `vitruvocl-allinstances`: 12 tests, green. The CLI fallback for unloadable model files (review fix 5) compiles but is not covered by a test.

## 8. Limitations

- Correspondence changes are not observable (§3.1); constraints using `~` gain nothing from levels 1–3 beyond skipping empty transactions.
- At level 1, a constraint whose context or `allInstances()` type is affected by any create/delete/containment change is re-evaluated in full (class-event behaviour of Paper 1 for all structural events).
- Correctness is established for the tested constraints, models and transactions (oracle), not proven: there is no formal semantics of the Reactions, and the completeness argument for structural events (§4) is informal.
- Additional state: one compiled tree and one violation-record map per constraint text and session.
- The CLI has no transaction; `--incremental-level` only routes the initial full evaluation through a session.

## 9. Related work delta

*(final)*

## 10. Open points and assumptions

- **Type checker finding, not changed:** `append` does not check that its argument conforms to the receiver's element type and keeps the receiver type (`TypeCheckVisitor#visitAppendOp`), whereas `prepend` and `insertAt` report an error. The incremental analysis works around it (§5.1). [Code]
- **Possible bug, not changed (decision 5):** `EvaluationVisitor.evaluateSpecs` returns only the result of the **last** `specificationCS` of an `inv`/`pre`/`post` (`EvaluationVisitor.java:606-612` at `39476b07`), and `visitSpecificationCS` returns only the last expression of a specification. For `inv n(spec1): spec2`, `spec1` is evaluated but its result is ignored. The analysis takes the union of all specifications. [Code; Test: `IncrementalEdgeCaseTest.multipleSpecifications` exercises the current behaviour]
- Assumption: a correspondence never changes in a propagation that records no model change at all (an empty transaction skips `~` constraints). Not verified beyond the experiment. [Assumption]
- Assumption: consequential changes within a `PropagatedChange` are chronological. Only relevant for `@pre` constraints, which are always evaluated. [Assumption]
- Test-only dependency added: `org.eclipse.platform:org.eclipse.equinox.common` (test scope, version from the `tools.vitruv.dsls` parent), required at run time by a real VSUM (`VsumFileSystemLayout` uses `org.eclipse.core.runtime.IPath`). [Code]
- The local Maven repository contains several `4.0.1-SNAPSHOT` builds of the Vitruvius jars; all bytecode statements in this report refer to the jars resolved by the build (`…-4.0.1-SNAPSHOT.jar`). [Code]
