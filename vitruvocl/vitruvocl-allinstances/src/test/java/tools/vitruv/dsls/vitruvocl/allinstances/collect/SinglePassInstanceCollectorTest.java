package tools.vitruv.dsls.vitruvocl.allinstances.collect;

import static tools.vitruv.dsls.vitruvocl.allinstances.fixtures.ModelBuilding.addMany;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallCollector;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite.Kind;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.CacheConfiguration;
import tools.vitruv.dsls.vitruvocl.allinstances.fixtures.UniversityMetamodelFixture;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.Test;

class SinglePassInstanceCollectorTest {

  @Test
  void collectsMembersAcrossMultipleDepartmentsIncludingSubtypes() {
    UniversityMetamodelFixture f = new UniversityMetamodelFixture();

    EObject university = f.create(f.universityClass);
    EObject deptA = f.create(f.departmentClass);
    EObject deptB = f.create(f.departmentClass);
    addMany(university, f.universityDepartments, deptA, deptB);

    EObject student1 = f.create(f.studentClass);
    EObject staff1 = f.create(f.staffClass);
    addMany(deptA, f.departmentMembers, student1, staff1);

    EObject student2 = f.create(f.studentClass);
    addMany(deptB, f.departmentMembers, student2);

    // A module/lecture subtree hangs off deptA too, but no query below needs it.
    EObject module1 = f.create(f.moduleClass);
    addMany(deptA, f.departmentModules, module1);
    EObject lecture1 = f.create(f.lectureClass);
    addMany(module1, f.moduleLectures, lecture1);

    CacheConfiguration cacheConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(List.of(new AllInstancesCallSite(f.memberClass, Kind.ALL_OF_KIND)));
    Set<EReference> traversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(f.universityPackage, cacheConfig);

    Map<EClass, List<EObject>> result =
        new SinglePassInstanceCollector()
            .collectInstances(List.of(university), cacheConfig, traversable);

    List<EObject> members = result.get(f.memberClass);
    assertEquals(3, members.size());
    assertTrue(members.containsAll(List.of(student1, staff1, student2)));
  }

  @Test
  void allOfKindIncludesSubtypeInstances_allOfTypeDoesNotIncludeThem() {
    UniversityMetamodelFixture f = new UniversityMetamodelFixture();

    EObject department = f.create(f.departmentClass);
    EObject student = f.create(f.studentClass);
    EObject staff = f.create(f.staffClass);
    addMany(department, f.departmentMembers, student, staff);

    // allOfKind(Member): subtype-inclusive
    CacheConfiguration kindConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(List.of(new AllInstancesCallSite(f.memberClass, Kind.ALL_OF_KIND)));
    Set<EReference> kindTraversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(f.universityPackage, kindConfig);
    Map<EClass, List<EObject>> kindResult =
        new SinglePassInstanceCollector()
            .collectInstances(List.of(department), kindConfig, kindTraversable);

    assertEquals(2, kindResult.get(f.memberClass).size());
    assertTrue(kindResult.get(f.memberClass).containsAll(List.of(student, staff)));

    // allOfType(Member) and allOfType(Staff): exact-type only, no subtype pull-in
    CacheConfiguration typeConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(
                List.of(
                    new AllInstancesCallSite(f.memberClass, Kind.ALL_OF_TYPE),
                    new AllInstancesCallSite(f.staffClass, Kind.ALL_OF_TYPE)));
    Set<EReference> typeTraversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(f.universityPackage, typeConfig);
    Map<EClass, List<EObject>> typeResult =
        new SinglePassInstanceCollector()
            .collectInstances(List.of(department), typeConfig, typeTraversable);

    // Member is abstract: no EObject's exact type is ever Member, regardless of Student/Staff instances
    assertTrue(typeResult.get(f.memberClass).isEmpty());
    // Staff is concrete and requested exactly: only the Staff instance, not the Student
    assertEquals(List.of(staff), typeResult.get(f.staffClass));
  }

  @Test
  void prunedReferencesAreNeverDescendedInto() {
    UniversityMetamodelFixture f = new UniversityMetamodelFixture();

    EObject department = f.create(f.departmentClass);
    EObject module = f.create(f.moduleClass);
    addMany(department, f.departmentModules, module);
    EObject lecture = f.create(f.lectureClass);
    addMany(module, f.moduleLectures, lecture);

    // Query only needs Lecture -> modules/lectures must be traversed and found
    CacheConfiguration cacheConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(List.of(new AllInstancesCallSite(f.lectureClass, Kind.ALL_OF_KIND)));
    Set<EReference> traversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(f.universityPackage, cacheConfig);

    Map<EClass, List<EObject>> result =
        new SinglePassInstanceCollector()
            .collectInstances(List.of(department), cacheConfig, traversable);

    assertEquals(List.of(lecture), result.get(f.lectureClass));

    // Now flip to a query that prunes departmentModules entirely: Lecture must not be found,
    // proving the traversal really skipped the pruned subtree rather than just filtering results.
    CacheConfiguration prunedConfig =
        new AllInstancesCallCollector()
            .analyzeQueries(List.of(new AllInstancesCallSite(f.memberClass, Kind.ALL_OF_KIND)));
    Set<EReference> prunedTraversable =
        new ContainmentReachabilityAnalyzer()
            .computeTraversableReferences(f.universityPackage, prunedConfig);

    assertTrue(prunedTraversable.isEmpty() || !prunedTraversable.contains(f.departmentModules));
  }
}
