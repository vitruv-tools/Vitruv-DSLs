package tools.vitruv.dsls.vitruvocl.allinstances.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite.Kind;
import tools.vitruv.dsls.vitruvocl.allinstances.fixtures.UniversityMetamodelFixture;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AllInstancesCallCollectorTest {

  @Test
  void analyzeQueries_bucketsCallSitesByKind() {
    UniversityMetamodelFixture fixture = new UniversityMetamodelFixture();
    List<AllInstancesCallSite> callSites =
        List.of(
            new AllInstancesCallSite(fixture.memberClass, Kind.ALL_OF_KIND),
            new AllInstancesCallSite(fixture.webPageClass, Kind.ALL_OF_TYPE));

    CacheConfiguration cacheConfig = new AllInstancesCallCollector().analyzeQueries(callSites);

    assertEquals(Set.of(fixture.memberClass), cacheConfig.getAllOfKind());
    assertEquals(Set.of(fixture.webPageClass), cacheConfig.getAllOfType());
    assertTrue(cacheConfig.getTraverse().isEmpty(), "traverse is only populated by the reachability analyzer");
  }

  @Test
  void analyzeQueries_deduplicatesRepeatedCallSitesForTheSameType() {
    UniversityMetamodelFixture fixture = new UniversityMetamodelFixture();
    List<AllInstancesCallSite> callSites =
        List.of(
            new AllInstancesCallSite(fixture.studentClass, Kind.ALL_OF_KIND),
            new AllInstancesCallSite(fixture.studentClass, Kind.ALL_OF_KIND));

    CacheConfiguration cacheConfig = new AllInstancesCallCollector().analyzeQueries(callSites);

    assertEquals(Set.of(fixture.studentClass), cacheConfig.getAllOfKind());
  }
}
