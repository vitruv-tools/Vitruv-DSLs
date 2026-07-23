package tools.vitruv.dsls.vitruvocl.allinstances.collect;

import tools.vitruv.dsls.vitruvocl.allinstances.cache.CacheConfiguration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

/**
 * Performs the single top-down traversal described in Wei &amp; Kolovos (BigMDE 2015, Section
 * 3.4): walk the in-memory containment tree exactly once, classify every visited {@link EObject}
 * against the requested types, and descend only into containment references that {@link
 * tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer} has determined are needed.
 *
 * <p>This is where the strategy's performance win is realized: references outside {@code
 * traversableRefs} are skipped entirely (including everything below them), instead of being
 * walked and discarded, or requiring a separate full-model index of every type up front.
 *
 * <p>The traversal assumes the model is not mutated concurrently with a {@link #collectInstances}
 * call (see {@link tools.vitruv.dsls.vitruvocl.allinstances.AllInstancesEngine} for how this interacts with reusing
 * a {@link CacheConfiguration} across multiple calls).
 */
public class SinglePassInstanceCollector {

  /**
   * Collects instances of every type requested in {@code cacheConfig}, reachable from {@code
   * roots} via containment references in {@code traversableRefs}.
   *
   * @param roots the root {@link EObject}s of the model(s) to traverse (e.g. resource contents)
   * @param cacheConfig identifies which types are wanted, and whether subtypes count ({@code
   *     allOfKind}) or not ({@code allOfType})
   * @param traversableRefs the containment references to descend into; any containment reference
   *     not in this set is skipped, along with its entire subtree
   * @return a map from each requested {@link EClass} (both {@code allOfKind} and {@code
   *     allOfType} entries) to the list of instances found for it; an entry with an empty list is
   *     present for a requested type that had no matches, so callers can rely on {@code
   *     containsKey} rather than defaulting
   */
  public Map<EClass, List<EObject>> collectInstances(
      Collection<? extends EObject> roots,
      CacheConfiguration cacheConfig,
      Set<EReference> traversableRefs) {

    Map<EClass, List<EObject>> result = new LinkedHashMap<>();
    for (EClass type : cacheConfig.getAllOfKind()) {
      result.computeIfAbsent(type, t -> new ArrayList<>());
    }
    for (EClass type : cacheConfig.getAllOfType()) {
      result.computeIfAbsent(type, t -> new ArrayList<>());
    }

    Map<EClass, List<EClass>> kindMatchCache = new LinkedHashMap<>();

    // Explicit-stack pre-order DFS: visits roots and, within each object, its containment
    // children in declaration/list order — the same encounter order a recursive walk over
    // eContents() would produce. Children are pushed in reverse so they pop back out in
    // forward order; this keeps result ordering predictable and independent of traversal
    // implementation details (a plain LIFO push/pop of forward-ordered children would visit
    // both roots and siblings in reverse order instead).
    Deque<EObject> stack = new ArrayDeque<>();
    List<EObject> rootList = new ArrayList<>(roots);
    for (int i = rootList.size() - 1; i >= 0; i--) {
      stack.push(rootList.get(i));
    }

    while (!stack.isEmpty()) {
      EObject current = stack.pop();
      classify(current, cacheConfig, result, kindMatchCache);

      List<EObject> children = new ArrayList<>();
      for (EReference containment : current.eClass().getEAllContainments()) {
        if (!traversableRefs.contains(containment)) {
          continue;
        }
        Object value = current.eGet(containment, false);
        if (containment.isMany()) {
          for (Object child : (List<?>) value) {
            children.add((EObject) child);
          }
        } else if (value != null) {
          children.add((EObject) value);
        }
      }
      for (int i = children.size() - 1; i >= 0; i--) {
        stack.push(children.get(i));
      }
    }

    return result;
  }

  /**
   * Adds {@code instance} to every requested bucket it matches: its exact type if requested via
   * {@code allOfType}, and every requested {@code allOfKind} supertype (including its exact type).
   */
  private void classify(
      EObject instance,
      CacheConfiguration cacheConfig,
      Map<EClass, List<EObject>> result,
      Map<EClass, List<EClass>> kindMatchCache) {
    EClass actualType = instance.eClass();

    if (cacheConfig.getAllOfType().contains(actualType)) {
      result.get(actualType).add(instance);
    }

    for (EClass matchingKindType : matchingKindTypes(actualType, cacheConfig, kindMatchCache)) {
      result.get(matchingKindType).add(instance);
    }
  }

  /**
   * Returns which requested {@code allOfKind} types {@code actualType} satisfies (itself or any of
   * its supertypes), computed once per distinct {@code actualType} and cached for the rest of the
   * traversal.
   */
  private List<EClass> matchingKindTypes(
      EClass actualType, CacheConfiguration cacheConfig, Map<EClass, List<EClass>> kindMatchCache) {
    return kindMatchCache.computeIfAbsent(
        actualType,
        type -> {
          List<EClass> matches = new ArrayList<>();
          for (EClass kindType : cacheConfig.getAllOfKind()) {
            if (kindType.isSuperTypeOf(type)) {
              matches.add(kindType);
            }
          }
          return matches;
        });
  }
}
