package tools.vitruv.dsls.vitruvocl.allinstances.cache;

import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

/**
 * Holds the outcome of the static query analysis (Wei &amp; Kolovos, BigMDE 2015, Section 3.1)
 * and, once computed, the pruned set of containment references that a model traversal actually
 * needs to descend into (Section 3.2, Algorithm 1).
 *
 * <p>A {@code CacheConfiguration} is derived purely from a set of {@code allInstances()}-style
 * call sites and the metamodel — it does not depend on any concrete instance model. This is what
 * allows it to be computed once and reused across many {@code compute()} invocations, even as the
 * underlying instance model changes between calls (see {@link tools.vitruv.dsls.vitruvocl.allinstances.AllInstancesEngine}).
 */
public final class CacheConfiguration {

  /**
   * Types for which direct AND indirect instances (i.e. instances of subtypes too) must be
   * collected — this is the semantics of OCL's {@code allInstances()}.
   */
  private final Set<EClass> allOfKind = new LinkedHashSet<>();

  /** Types for which only direct (exact-type) instances must be collected, subtypes excluded. */
  private final Set<EClass> allOfType = new LinkedHashSet<>();

  /**
   * The containment references that a model traversal must descend into, as computed by {@link
   * tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer}. Empty until that analysis
   * has run.
   */
  private final Set<EReference> traverse = new LinkedHashSet<>();

  public Set<EClass> getAllOfKind() {
    return allOfKind;
  }

  public Set<EClass> getAllOfType() {
    return allOfType;
  }

  public Set<EReference> getTraverse() {
    return traverse;
  }

  /** Registers a type for which subtype-inclusive instance collection is required. */
  public void addAllOfKind(EClass type) {
    allOfKind.add(type);
  }

  /** Registers a type for which exact-type-only instance collection is required. */
  public void addAllOfType(EClass type) {
    allOfType.add(type);
  }

  /**
   * Replaces the traversable-reference set. Called by {@link
   * tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer} after running Algorithm 1.
   */
  public void setTraverse(Set<EReference> traversableRefs) {
    traverse.clear();
    traverse.addAll(traversableRefs);
  }

  /** Returns whether any query at all needs instances of {@code type} (kind- or type-wise). */
  public boolean isRelevant(EClass type) {
    return allOfKind.contains(type) || allOfType.contains(type);
  }
}
