/**
 */
package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metaclass Feature Reference</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetaclassFeatureReference#getFeature <em>Feature</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassFeatureReference()
 * @model
 * @generated
 */
public interface MetaclassFeatureReference extends MetaclassReference
{
	/**
	 * Returns the value of the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Feature</em>' reference.
	 * @see #setFeature(EStructuralFeature)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassFeatureReference_Feature()
	 * @model
	 * @generated
	 */
	EStructuralFeature getFeature();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetaclassFeatureReference#getFeature <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature</em>' reference.
	 * @see #getFeature()
	 * @generated
	 */
	void setFeature(EStructuralFeature value);

} // MetaclassFeatureReference
