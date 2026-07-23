package tools.vitruv.dsls.vitruvocl.allinstances.analysis;

import tools.vitruv.dsls.vitruvocl.allinstances.cache.CacheConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * Computes, for a given metamodel and {@link CacheConfiguration}, the minimal set of containment
 * references a model traversal must descend into in order to reach every instance that some query
 * actually needs.
 *
 * <p>This is the core contribution of Wei &amp; Kolovos, "An Efficient Computation Strategy for
 * allInstances()" (BigMDE 2015): Algorithm 1 in Section 3.2, generalized here (Section 3.3/3.4) to
 * cover both {@code allInstances()}/{@code allOfKind()} semantics (subtype-inclusive) and {@code
 * allOfType()} semantics (exact type only).
 *
 * <p>A containment reference {@code r} is traversable if and only if either
 *
 * <ul>
 *   <li>its reference type (or one of its subtypes) is itself requested by some query, or
 *   <li>traversing into it can transitively reach an EClass that satisfies the above, through some
 *       chain of further containment references.
 * </ul>
 *
 * <p>References that lead only to irrelevant subtrees (e.g. {@code Department.modules} when no
 * query ever needs a {@code Module} or anything reachable from it) are pruned — a traversal never
 * needs to step into them, which is where the performance win over "traverse everything" comes
 * from.
 */
public class ContainmentReachabilityAnalyzer {

  /**
   * Runs Algorithm 1 over a single-package metamodel.
   *
   * <p>Matches the paper's algorithm signature directly: iterate over every concrete EClass
   * reachable from {@code rootPackage}, and for each of its (inherited) containment references,
   * decide via {@link #shouldBeTraversed} whether it must be kept.
   *
   * @param rootPackage the root of the metamodel to analyze (its classifiers and all classifiers
   *     of its transitively nested subpackages)
   * @param cacheConfig the query analysis result (Section 3.1); its {@code traverse} field is
   *     updated in place with the result
   * @return the pruned set of containment references that must be traversed
   */
  public Set<EReference> computeTraversableReferences(
      EPackage rootPackage, CacheConfiguration cacheConfig) {
    return computeTraversableReferences(List.of(rootPackage), cacheConfig);
  }

  /**
   * Overload of {@link #computeTraversableReferences(EPackage, CacheConfiguration)} for metamodels
   * split across several {@link EPackage}s (e.g. cross-metamodel references) that are not nested
   * as subpackages of one another. Subtype search (see {@link #transitiveClosureOfSubtypes}) scans
   * all classifiers of all given packages, not just {@code rootPackage}'s own tree — subtypes of a
   * reference's declared type can legitimately live in a different package.
   *
   * @param rootPackages every {@link EPackage} that is part of the metamodel under analysis
   * @param cacheConfig the query analysis result; its {@code traverse} field is updated in place
   * @return the pruned set of containment references that must be traversed
   */
  public Set<EReference> computeTraversableReferences(
      Collection<EPackage> rootPackages, CacheConfiguration cacheConfig) {
    List<EClass> allClasses = new ArrayList<>();
    for (EPackage rootPackage : rootPackages) {
      collectClassifiersRecursively(rootPackage, allClasses);
    }

    Set<EReference> refs = new LinkedHashSet<>();
    Map<EReference, Boolean> memo = new HashMap<>();
    Map<EClass, Set<EClass>> subtypeMemo = new HashMap<>();

    for (EClass eClass : allClasses) {
      if (eClass.isAbstract()) {
        continue;
      }
      for (EReference ref : eClass.getEAllContainments()) {
        shouldBeTraversed(ref, cacheConfig, refs, memo, allClasses, subtypeMemo);
      }
    }

    cacheConfig.setTraverse(refs);
    return refs;
  }

  /**
   * Overload that additionally consults an {@link EPackage.Registry} to discover subtypes defined
   * in metamodels other than {@code rootPackage} (e.g. metamodels loaded into a {@code
   * ResourceSet}'s package registry). Registry descriptors are resolved defensively — a descriptor
   * that fails to resolve (e.g. a stale/unavailable entry) is skipped rather than propagating the
   * failure, since it does not belong to the metamodel actually being analyzed.
   *
   * @param rootPackage the primary root package of the metamodel under analysis
   * @param cacheConfig the query analysis result; its {@code traverse} field is updated in place
   * @param registry a package registry (e.g. {@code resourceSet.getPackageRegistry()}) to search
   *     for subtypes beyond {@code rootPackage}'s own tree
   * @return the pruned set of containment references that must be traversed
   */
  public Set<EReference> computeTraversableReferences(
      EPackage rootPackage, CacheConfiguration cacheConfig, EPackage.Registry registry) {
    Set<EPackage> packages = new LinkedHashSet<>();
    packages.add(rootPackage);
    packages.addAll(resolveRegistryPackages(registry));
    return computeTraversableReferences(packages, cacheConfig);
  }

  /**
   * Decides whether containment reference {@code r} must be traversed, per Algorithm 1.
   *
   * <p>Memoization ({@code memo}) is mandatory, not an optimization detail: metamodels commonly
   * contain cyclic containment structures (e.g. two classes containing one another), and without
   * memoization the recursion into the containments of {@code reachableTypes} would never
   * terminate. The placeholder {@code memo.put(r, false)} written before recursing breaks the
   * cycle — a reference is never considered to depend on its own relevance.
   *
   * <p>Per the paper, once a recursive call into a sibling containment reference of the same
   * target type has established that {@code r} should be traversed, the loop still visits the
   * remaining sibling references rather than returning early: each sibling has its own
   * independent memo entry and may itself need to be added to {@code refs}, regardless of what was
   * just concluded about {@code r}.
   */
  private boolean shouldBeTraversed(
      EReference r,
      CacheConfiguration cacheConfig,
      Set<EReference> refs,
      Map<EReference, Boolean> memo,
      List<EClass> allClasses,
      Map<EClass, Set<EClass>> subtypeMemo) {
    if (memo.containsKey(r)) {
      return memo.get(r);
    }
    memo.put(r, false); // cycle guard: a reference cannot depend on its own relevance

    Set<EClass> reachableTypes =
        transitiveClosureOfSubtypes(r.getEReferenceType(), allClasses, subtypeMemo);
    boolean relevant =
        reachableTypes.stream()
            .anyMatch(t -> cacheConfig.getAllOfKind().contains(t) || cacheConfig.getAllOfType().contains(t));

    boolean shouldTraverse = relevant;
    if (!relevant) {
      // Algorithm 1: "foreach containment EReference tr of each of the types" — a subtype may
      // declare containment references the declared/supertype does not have (e.g. Member is
      // abstract with no containments of its own, but its subtype Staff has its own "webpage"
      // containment), so every type in reachableTypes must contribute its own containments here,
      // not just r.getEReferenceType() alone. A Set dedupes references inherited by more than one
      // type in the closure (e.g. a containment declared on Member is reported by both Student
      // and Staff via getEAllContainments()).
      Set<EReference> candidateContainments = new LinkedHashSet<>();
      for (EClass type : reachableTypes) {
        candidateContainments.addAll(type.getEAllContainments());
      }
      for (EReference transitive : candidateContainments) {
        if (shouldBeTraversed(transitive, cacheConfig, refs, memo, allClasses, subtypeMemo)) {
          shouldTraverse = true;
          // Intentionally no early break: every sibling containment must still be visited so its
          // own memo entry (and possible refs.add) gets computed, independent of r's outcome.
        }
      }
    }

    if (shouldTraverse) {
      refs.add(r);
    }
    memo.put(r, shouldTraverse);
    return shouldTraverse;
  }

  /**
   * Computes {@code type} plus every subtype of {@code type} found among {@code allClasses}
   * (Section 3.3: a containment reference declared with an abstract or non-final type may hold
   * instances of any of its subtypes at runtime, so relevance must be checked against the whole
   * subtype closure, not just the declared type).
   *
   * <p>{@link EClass#isSuperTypeOf(EClass)} is reflexive (a class is a supertype of itself), so
   * {@code type} itself is included in the result without needing to be added separately.
   */
  private Set<EClass> transitiveClosureOfSubtypes(
      EClass type, List<EClass> allClasses, Map<EClass, Set<EClass>> subtypeMemo) {
    Set<EClass> cached = subtypeMemo.get(type);
    if (cached != null) {
      return cached;
    }
    Set<EClass> result = new LinkedHashSet<>();
    for (EClass candidate : allClasses) {
      if (type.isSuperTypeOf(candidate)) {
        result.add(candidate);
      }
    }
    result.add(type);
    subtypeMemo.put(type, result);
    return result;
  }

  /** Recursively collects every {@link EClass} classifier of {@code ePackage} and its subpackages. */
  private void collectClassifiersRecursively(EPackage ePackage, List<EClass> out) {
    for (EClassifier classifier : ePackage.getEClassifiers()) {
      if (classifier instanceof EClass eClass) {
        out.add(eClass);
      }
    }
    for (EPackage subPackage : ePackage.getESubpackages()) {
      collectClassifiersRecursively(subPackage, out);
    }
  }

  /**
   * Resolves every {@link EPackage} reachable from a registry, tolerating unresolvable descriptors.
   */
  private Set<EPackage> resolveRegistryPackages(EPackage.Registry registry) {
    Set<EPackage> result = new LinkedHashSet<>();
    if (registry == null) {
      return Collections.emptySet();
    }
    for (Object value : registry.values()) {
      try {
        if (value instanceof EPackage ePackage) {
          result.add(ePackage);
        } else if (value instanceof EPackage.Descriptor descriptor) {
          EPackage resolved = descriptor.getEPackage();
          if (resolved != null) {
            result.add(resolved);
          }
        }
      } catch (RuntimeException resolutionFailure) {
        // A stale or unavailable registry entry does not belong to the metamodel being analyzed;
        // skip it rather than letting an unrelated resolution failure abort the whole analysis.
      }
    }
    return result;
  }
}
