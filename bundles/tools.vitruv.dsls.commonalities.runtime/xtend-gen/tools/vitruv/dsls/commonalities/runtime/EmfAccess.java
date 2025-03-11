package tools.vitruv.dsls.commonalities.runtime;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Helper methods for reflectively accessing EMF meta objects at runtime.
 */
@Utility
@SuppressWarnings("all")
public final class EmfAccess {
  public static EPackage getEPackage(final String nsURI) {
    return EPackage.Registry.INSTANCE.getEPackage(nsURI);
  }

  public static EClass getEClass(final String nsURI, final String className) {
    return EmfAccess.getEClass(EmfAccess.getEPackage(nsURI), className);
  }

  public static EClass getEClass(final EPackage ePackage, final String className) {
    EClassifier _eClassifier = ePackage.getEClassifier(className);
    return ((EClass) _eClassifier);
  }

  public static EStructuralFeature getEFeature(final String nsURI, final String className, final String featureName) {
    return EmfAccess.getEFeature(EmfAccess.getEClass(nsURI, className), featureName);
  }

  public static EStructuralFeature getEFeature(final EClass eClass, final String featureName) {
    return eClass.getEStructuralFeature(featureName);
  }

  public static EReference getEReference(final String nsURI, final String className, final String featureName) {
    EStructuralFeature _eFeature = EmfAccess.getEFeature(nsURI, className, featureName);
    return ((EReference) _eFeature);
  }

  public static EReference getEReference(final EClass eClass, final String featureName) {
    EStructuralFeature _eFeature = EmfAccess.getEFeature(eClass, featureName);
    return ((EReference) _eFeature);
  }

  public static EAttribute getEAttribute(final String nsURI, final String className, final String featureName) {
    EStructuralFeature _eFeature = EmfAccess.getEFeature(nsURI, className, featureName);
    return ((EAttribute) _eFeature);
  }

  public static EAttribute getEAttribute(final EClass eClass, final String featureName) {
    EStructuralFeature _eFeature = EmfAccess.getEFeature(eClass, featureName);
    return ((EAttribute) _eFeature);
  }

  private EmfAccess() {
    
  }
}
