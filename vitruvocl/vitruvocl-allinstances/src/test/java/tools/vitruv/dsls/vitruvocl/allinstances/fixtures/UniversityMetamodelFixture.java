package tools.vitruv.dsls.vitruvocl.allinstances.fixtures;

import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;

/**
 * Builds, programmatically via {@link EcoreFactory}, the "University" example metamodel used
 * throughout Wei &amp; Kolovos (BigMDE 2015) to illustrate the allInstances() pruning strategy
 * (see Fig. 5/6 in the paper):
 *
 * <pre>
 * University --*departments--&gt; Department
 * Department --*members--&gt; Member (abstract)
 * Department --*modules--&gt; Module
 * Member &lt;|-- Student
 * Member &lt;|-- Staff
 * Staff --0..1 webpage--&gt; WebPage
 * Module --*lectures--&gt; Lecture
 * </pre>
 *
 * <p>All references shown above are containment references.
 */
public final class UniversityMetamodelFixture {

  public final EPackage universityPackage;

  public final EClass universityClass;
  public final EClass departmentClass;
  public final EClass memberClass;
  public final EClass studentClass;
  public final EClass staffClass;
  public final EClass moduleClass;
  public final EClass lectureClass;
  public final EClass webPageClass;

  public final EReference universityDepartments;
  public final EReference departmentMembers;
  public final EReference departmentModules;
  public final EReference staffWebpage;
  public final EReference moduleLectures;

  private final EFactory factory;

  public UniversityMetamodelFixture() {
    EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;

    universityPackage = ecoreFactory.createEPackage();
    universityPackage.setName("university");
    universityPackage.setNsPrefix("univ");
    universityPackage.setNsURI("http://example.de/university");

    universityClass = newClass(ecoreFactory, "University", false);
    departmentClass = newClass(ecoreFactory, "Department", false);
    memberClass = newClass(ecoreFactory, "Member", true);
    studentClass = newClass(ecoreFactory, "Student", false);
    staffClass = newClass(ecoreFactory, "Staff", false);
    moduleClass = newClass(ecoreFactory, "Module", false);
    lectureClass = newClass(ecoreFactory, "Lecture", false);
    webPageClass = newClass(ecoreFactory, "WebPage", false);

    studentClass.getESuperTypes().add(memberClass);
    staffClass.getESuperTypes().add(memberClass);

    universityPackage
        .getEClassifiers()
        .addAll(
            List.of(
                universityClass,
                departmentClass,
                memberClass,
                studentClass,
                staffClass,
                moduleClass,
                lectureClass,
                webPageClass));

    universityDepartments =
        addContainment(ecoreFactory, universityClass, "departments", departmentClass, true);
    departmentMembers =
        addContainment(ecoreFactory, departmentClass, "members", memberClass, true);
    departmentModules =
        addContainment(ecoreFactory, departmentClass, "modules", moduleClass, true);
    staffWebpage = addContainment(ecoreFactory, staffClass, "webpage", webPageClass, false);
    moduleLectures = addContainment(ecoreFactory, moduleClass, "lectures", lectureClass, true);

    factory = universityPackage.getEFactoryInstance();
  }

  /** Creates a new instance of {@code eClass} using this metamodel's dynamic EFactory. */
  public EObject create(EClass eClass) {
    return factory.create(eClass);
  }

  private static EClass newClass(EcoreFactory ecoreFactory, String name, boolean isAbstract) {
    EClass eClass = ecoreFactory.createEClass();
    eClass.setName(name);
    eClass.setAbstract(isAbstract);
    return eClass;
  }

  private static EReference addContainment(
      EcoreFactory ecoreFactory, EClass owner, String name, EClass type, boolean many) {
    EReference reference = ecoreFactory.createEReference();
    reference.setName(name);
    reference.setContainment(true);
    reference.setEType(type);
    reference.setUpperBound(many ? -1 : 1);
    owner.getEStructuralFeatures().add(reference);
    return reference;
  }
}
