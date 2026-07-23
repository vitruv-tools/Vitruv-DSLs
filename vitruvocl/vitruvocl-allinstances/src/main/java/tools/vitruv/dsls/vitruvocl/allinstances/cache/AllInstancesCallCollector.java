package tools.vitruv.dsls.vitruvocl.allinstances.cache;

import java.util.List;

/**
 * Performs the static query analysis of Wei &amp; Kolovos (BigMDE 2015, Section 3.1): scans a
 * flat list of {@code allInstances()}-style call sites (already extracted from the query AST by
 * the caller) and buckets their target types into {@link CacheConfiguration#getAllOfKind()} or
 * {@link CacheConfiguration#getAllOfType()}.
 *
 * <p>This class deliberately has no dependency on any particular query language's AST — a caller
 * parsing OCL (or any other expression language) walks its own tree, finds every {@code
 * allInstances()} / {@code getAllOfType()} / {@code getAllOfKind()} invocation, and reports it as
 * an {@link AllInstancesCallSite}.
 */
public class AllInstancesCallCollector {

  /**
   * Builds an initial {@link CacheConfiguration} from the given call sites.
   *
   * @param callSites all statically-discovered allInstances()-style call sites in the query set
   * @return a fresh {@link CacheConfiguration} with {@code allOfKind}/{@code allOfType} populated;
   *     {@code traverse} is left empty, to be filled in by {@link
   *     tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer}
   */
  public CacheConfiguration analyzeQueries(List<AllInstancesCallSite> callSites) {
    CacheConfiguration cacheConfig = new CacheConfiguration();
    for (AllInstancesCallSite callSite : callSites) {
      switch (callSite.kind()) {
        case ALL_OF_KIND -> cacheConfig.addAllOfKind(callSite.targetType());
        case ALL_OF_TYPE -> cacheConfig.addAllOfType(callSite.targetType());
      }
    }
    return cacheConfig;
  }
}
