/**
 */
package tools.vitruv.dsls.common.elements;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Named Metaclass Reference</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.NamedMetaclassReference#getName <em>Name</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getNamedMetaclassReference()
 * @model
 * @generated
 */
public interface NamedMetaclassReference extends MetaclassReference
{
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getNamedMetaclassReference_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.NamedMetaclassReference#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

} // NamedMetaclassReference
