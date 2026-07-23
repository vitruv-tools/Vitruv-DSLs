package tools.vitruv.dsls.vitruvocl.allinstances.analysis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallCollector;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite.Kind;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.CacheConfiguration;
import tools.vitruv.dsls.vitruvocl.allinstances.fixtures.CyclicMetamodelFixture;
import tools.vitruv.dsls.vitruvocl.allinstances.fixtures.UniversityMetamodelFixture;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.Test;

/**
 * Verifies Algorithm 1 (Wei &amp; Kolovos, BigMDE 2015, Section 3.2) against the paper's own
 * University example, including its Fig. 5/6 pruning scenario, and against an artificially cyclic
 * metamodel to prove termination.
 */
class ContainmentReachabilityAnalyzerTest {

  @Test
  void memberAndWebPageQuery_prunesModulesAndLecturesReferences() {
    UniversityMetamodelFixture fixture = new UniversityMetamodelFixture();
    CacheConfiguration cacheConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(
                List.of(
                    new AllInstancesCallSite(fixture.memberClass, Kind.ALL_OF_KIND),
                    new AllInstancesCallSite(fixture.webPageClass, Kind.ALL_OF_TYPE)));

    Set<EReference> traversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(fixture.universityPackage, cacheConfig);

    // Needed to reach Member (via Department.members) and WebPage (via Staff.webpage)
    assertTrue(traversable.contains(fixture.universityDepartments));
    assertTrue(traversable.contains(fixture.departmentMembers));
    assertTrue(traversable.contains(fixture.staffWebpage));

    // Fig. 5/6: Department.modules only leads to Module -> Lecture, which nothing needs -> pruned
    assertFalse(traversable.contains(fixture.departmentModules));
    assertFalse(traversable.contains(fixture.moduleLectures));

    assertEquals(traversable, cacheConfig.getTraverse());
  }

  /**
   * Regression test for a bug where the recursive branch of {@code shouldBeTraversed} only
   * inspected {@code r.getEReferenceType().getEAllContainments()} instead of the containments of
   * every type in {@code reachableTypes} (declared type <em>and</em> all its subtypes), as Algorithm
   * 1 requires ("foreach containment EReference tr of each of the types").
   *
   * <p>Fixture: {@code Department.members} is declared with the abstract type {@code Member},
   * which has no containments of its own. Only its subtype {@code Staff} declares an additional
   * containment ({@code webpage}) that {@code Member} does not have. Querying only {@code WebPage}
   * (not {@code Member}/{@code Student}/{@code Staff}) means {@code departmentMembers} is not
   * directly relevant — the <em>only</em> way it can become traversable is by inspecting {@code
   * Staff}'s own containments during the recursive scan.
   *
   * <p>Note this is deliberately not about whether {@code staffWebpage} itself ends up in {@code
   * traversableRefs} — since {@code Staff} is a concrete class, Algorithm 1's outer loop visits
   * {@code Staff.webpage} directly regardless of reachability from anywhere else, so that alone
   * would never have exposed the bug. The discriminating assertion is on {@code departmentMembers}:
   * with the bug, {@code Member.getEAllContainments()} is empty (Member declares nothing itself),
   * so the recursive loop body never runs and {@code departmentMembers} is wrongly left out of
   * {@code traversableRefs}; with the fix, {@code Staff.getEAllContainments()} contributes {@code
   * webpage} to the scan, correctly marking {@code departmentMembers} traversable too.
   */
  @Test
  void webPageOnlyQuery_marksAbstractlyTypedReferenceTraversableViaSubtypeOnlyContainment() {
    UniversityMetamodelFixture fixture = new UniversityMetamodelFixture();
    CacheConfiguration cacheConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(
                List.of(new AllInstancesCallSite(fixture.webPageClass, Kind.ALL_OF_TYPE)));

    Set<EReference> traversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(fixture.universityPackage, cacheConfig);

    // Bug-exposing assertion: Department.members (declared type Member, which has no containments
    // of its own) is only reachable-to-WebPage through its subtype Staff's own "webpage"
    // containment — a correct scan of reachableTypes' containments is required to find this.
    assertTrue(
        traversable.contains(fixture.departmentMembers),
        "Department.members must be traversable: WebPage is only reachable through its subtype "
            + "Staff's own containment, not through Member itself");

    // Sanity check, not discriminating (Staff.webpage is always visited directly by the outer
    // loop since Staff is a concrete class, independent of this bug).
    assertTrue(traversable.contains(fixture.staffWebpage));

    // Nothing needs Module/Lecture in this query -> still correctly pruned.
    assertFalse(traversable.contains(fixture.departmentModules));
    assertFalse(traversable.contains(fixture.moduleLectures));
  }

  @Test
  void lectureQuery_includesModulesAndLecturesReferences() {
    UniversityMetamodelFixture fixture = new UniversityMetamodelFixture();
    CacheConfiguration cacheConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(
                List.of(new AllInstancesCallSite(fixture.lectureClass, Kind.ALL_OF_KIND)));

    Set<EReference> traversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(fixture.universityPackage, cacheConfig);

    // Now needed, since Lecture is only reachable through Department.modules -> Module.lectures
    assertTrue(traversable.contains(fixture.universityDepartments));
    assertTrue(traversable.contains(fixture.departmentModules));
    assertTrue(traversable.contains(fixture.moduleLectures));

    // Nothing needs a Member or WebPage in this query -> pruned
    assertFalse(traversable.contains(fixture.departmentMembers));
    assertFalse(traversable.contains(fixture.staffWebpage));
  }

  @Test
  void cyclicContainment_withNoRelevantTypes_terminatesAndPrunesEverything() {
    CyclicMetamodelFixture fixture = new CyclicMetamodelFixture();
    CacheConfiguration cacheConfig = new CacheConfiguration(); // nothing requested at all

    Set<EReference> traversable =
        assertDoesNotThrow(
            () ->
                new ContainmentReachabilityAnalyzer()
                    .computeTraversableReferences(fixture.cyclicPackage, cacheConfig),
            "cyclic containment (A <-> B) must not cause a StackOverflowError");

    assertTrue(traversable.isEmpty());
  }

  @Test
  void cyclicContainment_withARequested_terminatesAndFindsBothReferences() {
    CyclicMetamodelFixture fixture = new CyclicMetamodelFixture();
    CacheConfiguration cacheConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(List.of(new AllInstancesCallSite(fixture.classA, Kind.ALL_OF_KIND)));

    Set<EReference> traversable =
        assertDoesNotThrow(
            () ->
                new ContainmentReachabilityAnalyzer()
                    .computeTraversableReferences(fixture.cyclicPackage, cacheConfig),
            "cyclic containment (A <-> B) must not cause a StackOverflowError");

    // B.a leads directly to A (requested); A.b leads to B.a, which transitively leads to A too
    assertTrue(traversable.contains(fixture.bToA));
    assertTrue(traversable.contains(fixture.aToB));
  }
}
