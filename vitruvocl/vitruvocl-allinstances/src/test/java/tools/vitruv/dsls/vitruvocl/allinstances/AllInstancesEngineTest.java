package tools.vitruv.dsls.vitruvocl.allinstances;

import static tools.vitruv.dsls.vitruvocl.allinstances.fixtures.ModelBuilding.addMany;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite;
import tools.vitruv.dsls.vitruvocl.allinstances.cache.AllInstancesCallSite.Kind;
import tools.vitruv.dsls.vitruvocl.allinstances.fixtures.UniversityMetamodelFixture;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

/** End-to-end tests tying static analysis, pruning, and single-pass collection together. */
class AllInstancesEngineTest {

  @Test
  void endToEnd_prunesModulesAndCollectsCorrectInstances() {
    UniversityMetamodelFixture f = new UniversityMetamodelFixture();

    EObject university = f.create(f.universityClass);
    EObject department = f.create(f.departmentClass);
    addMany(university, f.universityDepartments, department);

    EObject student = f.create(f.studentClass);
    EObject staff = f.create(f.staffClass);
    addMany(department, f.departmentMembers, student, staff);

    EObject webpage = f.create(f.webPageClass);
    staff.eSet(f.staffWebpage, webpage);

    EObject module = f.create(f.moduleClass);
    addMany(department, f.departmentModules, module);
    EObject lecture = f.create(f.lectureClass);
    addMany(module, f.moduleLectures, lecture);

    AllInstancesEngine engine =
        new AllInstancesEngine(
            f.universityPackage,
            List.of(
                new AllInstancesCallSite(f.memberClass, Kind.ALL_OF_KIND),
                new AllInstancesCallSite(f.webPageClass, Kind.ALL_OF_TYPE)));

    assertFalse(engine.getTraversableRefs().contains(f.departmentModules));
    assertFalse(engine.getTraversableRefs().contains(f.moduleLectures));
    assertTrue(engine.getTraversableRefs().contains(f.departmentMembers));
    assertTrue(engine.getTraversableRefs().contains(f.staffWebpage));

    Map<EClass, List<EObject>> result = engine.compute(List.of(university));

    assertEquals(2, result.get(f.memberClass).size());
    assertTrue(result.get(f.memberClass).containsAll(List.of(student, staff)));
    assertEquals(List.of(webpage), result.get(f.webPageClass));
  }

  @Test
  void engineIsReusableAcrossComputeCallsAsTheInstanceModelChanges() {
    UniversityMetamodelFixture f = new UniversityMetamodelFixture();
    AllInstancesEngine engine =
        new AllInstancesEngine(
            f.universityPackage,
            List.of(new AllInstancesCallSite(f.studentClass, Kind.ALL_OF_TYPE)));

    EObject department = f.create(f.departmentClass);
    EObject student1 = f.create(f.studentClass);
    addMany(department, f.departmentMembers, student1);

    Map<EClass, List<EObject>> firstResult = engine.compute(List.of(department));
    assertEquals(1, firstResult.get(f.studentClass).size());

    // The instance model changes between calls; the same engine (same cacheConfig/traverse) must
    // reflect the new state on the next compute(), since only the metamodel-derived cache is reused.
    EObject student2 = f.create(f.studentClass);
    addMany(department, f.departmentMembers, student2);

    Map<EClass, List<EObject>> secondResult = engine.compute(List.of(department));
    assertEquals(2, secondResult.get(f.studentClass).size());
    assertTrue(secondResult.get(f.studentClass).containsAll(List.of(student1, student2)));
  }
}
