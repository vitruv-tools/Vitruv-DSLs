package tools.vitruv.dsls.reactions.runtime.locking;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.CreateEObject;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.change.atomic.feature.FeatureEChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.atomic.root.RemoveRootEObject;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.Reaction;

/**
 * Lazily computed, cached {@link PropagationFootprint footprints} of change propagations through
 * reactions, for deriving the lock set of a transaction before it runs (conservative 2PL).
 *
 * <p>When a change arrives, {@link #footprintOf(EChange)} looks up its kind (EChange type, class of
 * the affected element, affected feature). On the first change of a kind, it determines the
 * reactions whose trigger matches, analyzes their bytecode (see {@link BytecodeAccessAnalyzer}),
 * and follows what their writes may trigger in all specifications until nothing new is reached.
 * The result is cached per kind; reaction summaries and triggers are cached per reaction class.
 *
 * <p>Specifications that are not reactions are not analyzed: if one of them may react, the
 * footprint contains write keys for all of its metamodels.
 */
public final class ReactionsPropagationFootprints {
  /**
   * What trigger matching looks at: change type, class of the affected element, feature, and
   * the classes of new and old value (reaction triggers may check them with {@code instanceof}).
   */
  private record ChangeKind(Class<?> changeClass, EClass affectedClass, EStructuralFeature feature,
      EClass newValueClass, EClass oldValueClass) {
  }

  private record AnalyzedReaction(ChangePropagationSpecification specification, Reaction reaction,
      ReactionTrigger trigger, AccessSummary summary) {
  }

  private final List<ChangePropagationSpecification> specifications;
  private final EmfModelIndex index = new EmfModelIndex();
  private final Map<ClassLoader, BytecodeAccessAnalyzer> analyzers = new ConcurrentHashMap<>();
  private final Map<Class<?>, ReactionTrigger> triggers = new ConcurrentHashMap<>();
  private final Map<Class<?>, AccessSummary> summaries = new ConcurrentHashMap<>();
  private final Map<ChangeKind, PropagationFootprint> footprints = new ConcurrentHashMap<>();
  private final Map<ChangeKind, PropagationFootprint> stepFootprints = new ConcurrentHashMap<>();
  private final AtomicLong hits = new AtomicLong();
  private final AtomicLong misses = new AtomicLong();
  private final AtomicLong analysisNanos = new AtomicLong();

  public ReactionsPropagationFootprints(
      Iterable<? extends ChangePropagationSpecification> specifications) {
    this.specifications = new ArrayList<>();
    specifications.forEach(this.specifications::add);
  }

  /** The footprint of propagating {@code change}; computed on the first change of its kind. */
  public PropagationFootprint footprintOf(EChange<EObject> change) {
    ChangeKind kind = kindOf(change);
    PropagationFootprint cached = footprints.get(kind);
    if (cached != null) {
      hits.incrementAndGet();
      return cached;
    }
    misses.incrementAndGet();
    long start = System.nanoTime();
    PropagationFootprint computed = footprints.computeIfAbsent(kind, k -> compute(change, true));
    analysisNanos.addAndGet(System.nanoTime() - start);
    return computed;
  }

  /**
   * The footprint of one propagation step for {@code change}: what the reactions that react to
   * this change directly may access, without what their changes trigger in turn (those reactions
   * run in later steps, with their own concrete change). For strict 2PL, where a transaction
   * acquires the locks of each step before it runs. Cached per change kind.
   */
  public PropagationFootprint stepFootprintOf(EChange<EObject> change) {
    ChangeKind kind = kindOf(change);
    PropagationFootprint cached = stepFootprints.get(kind);
    if (cached != null) {
      hits.incrementAndGet();
      return cached;
    }
    misses.incrementAndGet();
    long start = System.nanoTime();
    PropagationFootprint computed =
        stepFootprints.computeIfAbsent(kind, k -> compute(change, false));
    analysisNanos.addAndGet(System.nanoTime() - start);
    return computed;
  }

  public long cacheHits() {
    return hits.get();
  }

  public long cacheMisses() {
    return misses.get();
  }

  public long analysisMillis() {
    return analysisNanos.get() / 1_000_000;
  }

  /** The access summary of a single reaction (cached), e.g. for inspection. */
  public AccessSummary summaryOf(Reaction reaction) {
    return summaries.computeIfAbsent(reaction.getClass(),
        c -> analyzer(c).analyzeReaction(c));
  }

  /** The trigger of a single reaction (cached). */
  public ReactionTrigger triggerOf(Reaction reaction) {
    return triggers.computeIfAbsent(reaction.getClass(), c -> analyzer(c).analyzeTrigger(c));
  }

  // ------------------------------------------------------------------ computation

  private PropagationFootprint compute(EChange<EObject> change, boolean transitive) {
    Set<String> unanalyzedMetamodels = new TreeSet<>();
    ArrayDeque<AnalyzedReaction> todo = new ArrayDeque<>();

    String changedNsUri = nsUriOf(change);
    for (ChangePropagationSpecification specification : specifications) {
      if (changedNsUri != null && !handles(specification, changedNsUri)) {
        continue;
      }
      if (specification instanceof AbstractReactionsChangePropagationSpecification reactions) {
        for (Reaction reaction : reactions.getReactions()) {
          if (triggerOf(reaction).matches(change)) {
            todo.add(analyzed(specification, reaction));
          }
        }
      } else {
        unanalyzedMetamodels.addAll(metamodelsOf(specification));
      }
    }

    return transitive ? closure(todo, unanalyzedMetamodels)
        : toFootprint(new LinkedHashSet<>(todo), unanalyzedMetamodels);
  }

  /**
   * The footprint of a propagation that starts with running {@code reaction} (and everything it
   * may trigger), independent of a concrete change; e.g. to estimate how much can run in parallel.
   */
  public PropagationFootprint footprintOfReaction(Reaction reaction) {
    for (ChangePropagationSpecification specification : specifications) {
      if (specification instanceof AbstractReactionsChangePropagationSpecification reactions
          && reactions.getReactions().contains(reaction)) {
        ArrayDeque<AnalyzedReaction> start = new ArrayDeque<>();
        start.add(analyzed(specification, reaction));
        return closure(start, new TreeSet<>());
      }
    }
    throw new IllegalArgumentException("unknown reaction " + reaction);
  }

  /** All reactions of all reactions specifications. */
  public List<Reaction> allReactions() {
    List<Reaction> result = new ArrayList<>();
    for (ChangePropagationSpecification specification : specifications) {
      if (specification instanceof AbstractReactionsChangePropagationSpecification reactions) {
        result.addAll(reactions.getReactions());
      }
    }
    return result;
  }

  private PropagationFootprint closure(ArrayDeque<AnalyzedReaction> todo,
      Set<String> unanalyzedMetamodels) {
    Set<AnalyzedReaction> reached = new LinkedHashSet<>();
    while (!todo.isEmpty()) {
      AnalyzedReaction next = todo.pop();
      if (!reached.add(next)) {
        continue;
      }
      for (ChangePropagationSpecification specification : specifications) {
        if (!mayReactTo(specification, next)) {
          continue;
        }
        if (specification instanceof AbstractReactionsChangePropagationSpecification reactions) {
          for (Reaction reaction : reactions.getReactions()) {
            AnalyzedReaction candidate = analyzed(specification, reaction);
            if (!reached.contains(candidate) && mayTrigger(next, candidate)) {
              todo.add(candidate);
            }
          }
        } else {
          unanalyzedMetamodels.addAll(metamodelsOf(specification));
        }
      }
    }
    return toFootprint(reached, unanalyzedMetamodels);
  }

  private AnalyzedReaction analyzed(ChangePropagationSpecification specification,
      Reaction reaction) {
    return new AnalyzedReaction(specification, reaction, triggerOf(reaction),
        summaryOf(reaction));
  }

  /** Whether the writes of {@code writer} touch a metamodel {@code specification} reacts to. */
  private boolean mayReactTo(ChangePropagationSpecification specification,
      AnalyzedReaction writer) {
    Set<String> written = writtenMetamodels(writer);
    return written.contains("*")
        || specification.getSourceMetamodelDescriptor().getNsUris().stream()
            .anyMatch(written::contains);
  }

  private Set<String> writtenMetamodels(AnalyzedReaction writer) {
    AccessSummary s = writer.summary();
    Set<String> result = new TreeSet<>();
    s.writtenFeatures.forEach(f -> result.add(f.getEContainingClass().getEPackage().getNsURI()));
    s.writtenClasses.forEach(c -> result.add(c.getEPackage().getNsURI()));
    s.createdClasses.forEach(c -> result.add(c.getEPackage().getNsURI()));
    s.deletedClasses.forEach(c -> result.add(c.getEPackage().getNsURI()));
    if (s.unknownWrite || s.unknownDelete || s.persists) {
      result.addAll(metamodelsOf(writer.specification()));
    }
    return result;
  }

  private boolean mayTrigger(AnalyzedReaction writer, AnalyzedReaction candidate) {
    AccessSummary s = writer.summary();
    ReactionTrigger trigger = candidate.trigger();
    if (s.unknownWrite) {
      return true;
    }
    for (AccessSummary.FeatureWrite write : s.featureWrites) {
      if (trigger.mayBeTriggeredBy(write, index)) {
        return true;
      }
    }
    for (EClass eClass : s.writtenClasses) {
      for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
        if (trigger.mayBeTriggeredBy(new AccessSummary.FeatureWrite(feature, eClass, null,
            AccessSummary.ValueSide.NONE), index)) {
          return true;
        }
      }
    }
    for (EClass created : s.createdClasses) {
      if (trigger.mayBeTriggeredByExistenceChange(created, CreateEObject.class, index)) {
        return true;
      }
    }
    for (EClass deleted : s.deletedClasses) {
      if (trigger.mayBeTriggeredByExistenceChange(deleted, DeleteEObject.class, index)
          || trigger.mayBeTriggeredByExistenceChange(deleted, RemoveRootEObject.class, index)) {
        return true;
      }
    }
    if (s.unknownDelete && (trigger.changeType().isAssignableFrom(DeleteEObject.class)
        || DeleteEObject.class.isAssignableFrom(trigger.changeType()))) {
      return true;
    }
    return s.persists && (trigger.changeType().isAssignableFrom(InsertRootEObject.class)
        || trigger.changeType().isAssignableFrom(RemoveRootEObject.class));
  }

  private PropagationFootprint toFootprint(Set<AnalyzedReaction> reached,
      Set<String> unanalyzedMetamodels) {
    AccessSummary union = new AccessSummary();
    Set<String> reads = new TreeSet<>();
    Set<String> writes = new TreeSet<>();
    List<String> reactionNames = new ArrayList<>();
    for (AnalyzedReaction reaction : reached) {
      reactionNames.add(reaction.reaction().getClass().getName());
      AccessSummary s = reaction.summary();
      union.addAll(s);
      s.readFeatures.forEach(f -> reads.add("f:" + AccessSummary.featureKey(f)));
      s.readClasses.forEach(c -> reads.add("c:" + AccessSummary.classKey(c)));
      s.writtenFeatures.forEach(f -> writes.add("f:" + AccessSummary.featureKey(f)));
      s.writtenClasses.forEach(c -> writes.add("c:" + AccessSummary.classKey(c)));
      // deleting an object writes its features and the references that contained or referenced it
      s.deletedClasses.forEach(c -> writes.add("c:" + AccessSummary.classKey(c)));
      s.correspondenceReads.forEach(t -> reads.add("corr:" + t));
      s.correspondenceWrites.forEach(t -> writes.add("corr:" + t));
      if (s.persists) {
        writes.add("res");
      }
      if (s.unknownRead) {
        metamodelsOf(reaction.specification()).forEach(m -> reads.add("mm:" + m));
      }
      if (s.unknownWrite || s.unknownDelete) {
        metamodelsOf(reaction.specification()).forEach(m -> writes.add("mm:" + m));
        writes.add("corr:*|*|*");
      }
    }
    unanalyzedMetamodels.forEach(m -> writes.add("mm:" + m));
    if (!unanalyzedMetamodels.isEmpty()) {
      writes.add("corr:*|*|*");
    }
    reads.removeAll(writes);
    return new PropagationFootprint(reads, writes, reactionNames, union);
  }

  // ------------------------------------------------------------------ conflicts

  /**
   * Conflict test on footprint keys that takes the class hierarchy into account: a feature
   * conflicts with the same feature and with "any feature" of an overlapping class, correspondence
   * keys conflict if their tags are compatible and their end types overlap (in either direction),
   * {@code mm:} keys conflict with everything in their metamodel.
   */
  public PropagationFootprint.KeyConflict keyConflict() {
    return this::conflicts;
  }

  private boolean conflicts(String a, String b) {
    if (a.equals(b)) {
      return true;
    }
    String kindA = a.substring(0, a.indexOf(':') < 0 ? a.length() : a.indexOf(':'));
    String kindB = b.substring(0, b.indexOf(':') < 0 ? b.length() : b.indexOf(':'));
    if (kindA.equals("corr") || kindB.equals("corr")) {
      return kindA.equals(kindB) && correspondencesConflict(a.substring(5), b.substring(5));
    }
    if (kindA.equals("res") || kindB.equals("res")) {
      return false; // only equal keys, handled above
    }
    if (kindA.equals("mm") || kindB.equals("mm")) {
      return metamodelOf(a).equals(metamodelOf(b));
    }
    // no shortcut on the metamodel for f/c: a class may inherit features from another package
    // (e.g. UML classes from Ecore's EModelElement, PCM classes from core packages)
    if (kindA.equals("f") && kindB.equals("f")) {
      return false; // different features never share a slot
    }
    return index.overlap(classOf(a), classOf(b));
  }

  private boolean correspondencesConflict(String a, String b) {
    String[] x = a.split("\\|");
    String[] y = b.split("\\|");
    if (!x[0].equals("*") && !y[0].equals("*") && !x[0].equals(y[0])) {
      return false;
    }
    return (overlap(x[1], y[1]) && overlap(x[2], y[2]))
        || (overlap(x[1], y[2]) && overlap(x[2], y[1]));
  }

  private boolean overlap(String classKeyA, String classKeyB) {
    if (classKeyA.equals("*") || classKeyB.equals("*")) {
      return true;
    }
    return index.overlap(eClass(classKeyA), eClass(classKeyB));
  }

  private static String metamodelOf(String key) {
    String body = key.substring(key.indexOf(':') + 1);
    int hash = body.lastIndexOf('#');
    return hash < 0 ? body : body.substring(0, hash);
  }

  /** The class of an {@code f:} key (its declaring class) or a {@code c:} key. */
  private static EClass classOf(String key) {
    String body = key.substring(key.indexOf(':') + 1);
    if (key.startsWith("f:")) {
      body = body.substring(0, body.lastIndexOf('.'));
    }
    return eClass(body);
  }

  private static EClass eClass(String classKey) {
    int hash = classKey.lastIndexOf('#');
    var ePackage = org.eclipse.emf.ecore.EPackage.Registry.INSTANCE
        .getEPackage(classKey.substring(0, hash));
    return ePackage == null ? null
        : (EClass) ePackage.getEClassifier(classKey.substring(hash + 1));
  }

  // ------------------------------------------------------------------ helpers

  private BytecodeAccessAnalyzer analyzer(Class<?> reactionClass) {
    ClassLoader loader = reactionClass.getClassLoader();
    return analyzers.computeIfAbsent(loader, l -> new BytecodeAccessAnalyzer(index, l));
  }

  private static boolean handles(ChangePropagationSpecification specification, String nsUri) {
    return specification.getSourceMetamodelDescriptor().getNsUris().contains(nsUri);
  }

  private static Set<String> metamodelsOf(ChangePropagationSpecification specification) {
    Set<String> result = new TreeSet<>(specification.getSourceMetamodelDescriptor().getNsUris());
    result.addAll(specification.getTargetMetamodelDescriptor().getNsUris());
    return result;
  }

  private static ChangeKind kindOf(EChange<EObject> change) {
    EObject affected = null;
    EStructuralFeature feature = null;
    if (change instanceof FeatureEChange<?, ?> featureChange) {
      affected = (EObject) featureChange.getAffectedElement();
      feature = featureChange.getAffectedFeature();
    } else if (change instanceof CreateEObject<EObject> create) {
      affected = create.getAffectedElement();
    } else if (change instanceof DeleteEObject<EObject> delete) {
      affected = delete.getAffectedElement();
    } else if (change instanceof InsertRootEObject<EObject> insert) {
      affected = insert.getNewValue();
    } else if (change instanceof RemoveRootEObject<EObject> remove) {
      affected = remove.getOldValue();
    }
    return new ChangeKind(change.getClass(), affected == null ? null : affected.eClass(), feature,
        eClassOf(ReactionTrigger.newValue(change)), eClassOf(ReactionTrigger.oldValue(change)));
  }

  private static EClass eClassOf(Object value) {
    return value instanceof EObject eObject ? eObject.eClass() : null;
  }

  private static String nsUriOf(EChange<EObject> change) {
    EClass affected = kindOf(change).affectedClass();
    return affected == null ? null : affected.getEPackage().getNsURI();
  }
}
