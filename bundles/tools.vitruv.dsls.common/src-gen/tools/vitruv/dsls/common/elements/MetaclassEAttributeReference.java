/**
 */
package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EAttribute;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metaclass EAttribute Reference</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetaclassEAttributeReference#getFeature <em>Feature</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassEAttributeReference()
 * @model
 * @generated
 */
public interface MetaclassEAttributeReference extends MetaclassReference
{
	/**
	 * Returns the value of the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Feature</em>' reference.
	 * @see #setFeature(EAttribute)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassEAttributeReference_Feature()
	 * @model
	 * @generated
	 */
	EAttribute getFeature();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetaclassEAttributeReference#getFeature <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature</em>' reference.
	 * @see #getFeature()
	 * @generated
	 */
	void setFeature(EAttribute value);

} // MetaclassEAttributeReference
