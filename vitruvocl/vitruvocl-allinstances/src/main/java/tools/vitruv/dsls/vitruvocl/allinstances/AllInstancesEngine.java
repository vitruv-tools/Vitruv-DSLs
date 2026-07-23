package tools.vitruv.dsls.vitruvocl.allinstances;

import tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallCollector;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.CacheConfiguration;
import tools.vitruv.dsls.vitruvocl.allinstances.collect.SinglePassInstanceCollector;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * High-level facade implementing the full allInstances() computation strategy of Wei &amp;
 * Kolovos, "An Efficient Computation Strategy for allInstances()" (BigMDE 2015): static query
 * analysis (Section 3.1) followed by metamodel-level containment reachability pruning (Section
 * 3.2, Algorithm 1), so that instance collection (Section 3.4) needs only a single traversal that
 * touches exclusively the containment references that matter.
 *
 * <p><b>Cache validity across {@link #compute} calls:</b> the constructor computes {@code
 * cacheConfig} and {@code traversableRefs} once, from the query call sites and the metamodel
 * alone — neither depends on any concrete instance model. This means an {@code AllInstancesEngine}
 * instance may be reused across many {@link #compute} calls even if the underlying instance model
 * changes between them (objects added, removed, or re-parented): the metamodel itself is assumed
 * stable for the engine's lifetime, so the pruning decision does not need to be redone. What must
 * be recomputed on every call is the instance collection itself — {@link #compute} always performs
 * a fresh traversal and never reuses instance results from a previous call.
 *
 * <p>Within a single {@link #compute} call, the instance model is assumed not to be mutated
 * concurrently with the traversal.
 */
public class AllInstancesEngine {

  private final CacheConfiguration cacheConfig;
  private final Set<EReference> traversableRefs;

  /**
   * Analyzes {@code callSites} and prunes {@code metamodel}'s containment references accordingly.
   *
   * @param metamodel the root {@link EPackage} of the metamodel the queries run against
   * @param callSites every statically-discovered {@code allInstances()}-style call site across the
   *     query set to be supported
   */
  public AllInstancesEngine(EPackage metamodel, List<AllInstancesCallSite> callSites) {
    this.cacheConfig = new AllInstancesCallCollector().analyzeQueries(callSites);
    this.traversableRefs =
        new ContainmentReachabilityAnalyzer().computeTraversableReferences(metamodel, cacheConfig);
  }

  /**
   * Overload for metamodels split across several independently-loaded {@link EPackage}s (e.g. a
   * project that loads multiple {@code .ecore} files whose classes cross-reference one another,
   * as opposed to one root package with nested subpackages).
   *
   * @param metamodels every {@link EPackage} that is part of the metamodel the queries run
   *     against; subtype search considers classifiers across all of them
   * @param callSites every statically-discovered {@code allInstances()}-style call site across the
   *     query set to be supported
   */
  public AllInstancesEngine(Collection<EPackage> metamodels, List<AllInstancesCallSite> callSites) {
    this.cacheConfig = new AllInstancesCallCollector().analyzeQueries(callSites);
    this.traversableRefs =
        new ContainmentReachabilityAnalyzer().computeTraversableReferences(metamodels, cacheConfig);
  }

  /**
   * Runs the single-pass instance collection over {@code modelRoots}.
   *
   * @param modelRoots the root {@link EObject}s of the instance model(s) to collect from
   * @return a map from each requested {@link EClass} to its collected instances, as produced by
   *     {@link SinglePassInstanceCollector#collectInstances}
   */
  public Map<EClass, List<EObject>> compute(Collection<? extends EObject> modelRoots) {
    return new SinglePassInstanceCollector().collectInstances(modelRoots, cacheConfig, traversableRefs);
  }

  /** Returns the query analysis result this engine was built with, mainly useful for testing. */
  public CacheConfiguration getCacheConfiguration() {
    return cacheConfig;
  }

  /** Returns the pruned set of traversable containment references, mainly useful for testing. */
  public Set<EReference> getTraversableRefs() {
    return traversableRefs;
  }
}
