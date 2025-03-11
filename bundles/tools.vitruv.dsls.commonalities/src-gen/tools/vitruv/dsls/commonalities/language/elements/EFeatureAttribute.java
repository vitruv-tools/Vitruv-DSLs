/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EFeature Attribute</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getEFeatureAttribute()
 * @model
 * @generated
 */
public interface EFeatureAttribute extends Attribute
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true"
	 * @generated
	 */
	EFeatureAttribute forEFeature(EStructuralFeature eFeature);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" classifierProviderDataType="tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider"
	 * @generated
	 */
	EFeatureAttribute withClassifierProvider(ClassifierProvider classifierProvider);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" metaclassRequired="true"
	 * @generated
	 */
	EFeatureAttribute fromMetaclass(Metaclass metaclass);

} // EFeatureAttribute
