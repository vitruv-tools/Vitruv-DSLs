package tools.vitruv.dsls.vitruvocl.allinstances.fixtures;

import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;

/**
 * A minimal, artificially cyclic metamodel used to prove that {@link
 * tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer} terminates on cyclic
 * containment structures: {@code A} containment-references {@code B} via {@code b}, and {@code B}
 * containment-references {@code A} back via {@code a}.
 *
 * <p>Note this does not describe a valid *instance* model (EMF would reject actually containing an
 * object within its own containment ancestry at runtime) — it only needs to exist at the
 * metamodel/type level, since {@link
 * tools.vitruv.dsls.vitruvocl.allinstances.analysis.ContainmentReachabilityAnalyzer} operates purely on {@link
 * EClass}/{@link EReference} declarations, never on instance data.
 */
public final class CyclicMetamodelFixture {

  public final EPackage cyclicPackage;
  public final EClass classA;
  public final EClass classB;
  public final EReference aToB;
  public final EReference bToA;

  public CyclicMetamodelFixture() {
    EcoreFactory ecoreFactory = EcoreFactory.eINSTANCE;

    cyclicPackage = ecoreFactory.createEPackage();
    cyclicPackage.setName("cyclic");
    cyclicPackage.setNsPrefix("cyc");
    cyclicPackage.setNsURI("http://example.de/cyclic");

    classA = ecoreFactory.createEClass();
    classA.setName("A");
    classB = ecoreFactory.createEClass();
    classB.setName("B");

    cyclicPackage.getEClassifiers().addAll(List.of(classA, classB));

    aToB = ecoreFactory.createEReference();
    aToB.setName("b");
    aToB.setContainment(true);
    aToB.setEType(classB);
    aToB.setUpperBound(1);
    classA.getEStructuralFeatures().add(aToB);

    bToA = ecoreFactory.createEReference();
    bToA.setName("a");
    bToA.setContainment(true);
    bToA.setEType(classA);
    bToA.setUpperBound(1);
    classB.getEStructuralFeatures().add(bToA);
  }

  /** Creates a new instance of {@code eClass} using this metamodel's dynamic EFactory. */
  public EObject create(EClass eClass) {
    return cyclicPackage.getEFactoryInstance().create(eClass);
  }
}
