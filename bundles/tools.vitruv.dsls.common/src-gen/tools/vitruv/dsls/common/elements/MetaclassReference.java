/**
 */
package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metaclass Reference</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetaclassReference#getMetamodel <em>Metamodel</em>}</li>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetaclassReference#getMetaclass <em>Metaclass</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassReference()
 * @model
 * @generated
 */
public interface MetaclassReference extends EObject
{
	/**
	 * Returns the value of the '<em><b>Metamodel</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Metamodel</em>' reference.
	 * @see #setMetamodel(MetamodelImport)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassReference_Metamodel()
	 * @model
	 * @generated
	 */
	MetamodelImport getMetamodel();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetaclassReference#getMetamodel <em>Metamodel</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Metamodel</em>' reference.
	 * @see #getMetamodel()
	 * @generated
	 */
	void setMetamodel(MetamodelImport value);

	/**
	 * Returns the value of the '<em><b>Metaclass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Metaclass</em>' reference.
	 * @see #setMetaclass(EClassifier)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetaclassReference_Metaclass()
	 * @model
	 * @generated
	 */
	EClassifier getMetaclass();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetaclassReference#getMetaclass <em>Metaclass</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Metaclass</em>' reference.
	 * @see #getMetaclass()
	 * @generated
	 */
	void setMetaclass(EClassifier value);

} // MetaclassReference
