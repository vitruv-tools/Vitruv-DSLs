/**
 */
package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metaclass EReference Reference</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetaclassEReferenceReference#getFeature <em>Feature</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassEReferenceReference()
 * @model
 * @generated
 */
public interface MetaclassEReferenceReference extends MetaclassReference
{
	/**
	 * Returns the value of the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Feature</em>' reference.
	 * @see #setFeature(EReference)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassEReferenceReference_Feature()
	 * @model
	 * @generated
	 */
	EReference getFeature();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetaclassEReferenceReference#getFeature <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature</em>' reference.
	 * @see #getFeature()
	 * @generated
	 */
	void setFeature(EReference value);

} // MetaclassEReferenceReference
