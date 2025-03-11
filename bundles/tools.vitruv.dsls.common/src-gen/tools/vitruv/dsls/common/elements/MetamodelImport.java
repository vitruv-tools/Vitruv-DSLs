/**
 */
package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metamodel Import</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetamodelImport#getPackage <em>Package</em>}</li>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetamodelImport#getName <em>Name</em>}</li>
 *   <li>{@link tools.vitruv.dsls.common.elements.MetamodelImport#isUseQualifiedNames <em>Use Qualified Names</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetamodelImport()
 * @model
 * @generated
 */
public interface MetamodelImport extends EObject
{
	/**
	 * Returns the value of the '<em><b>Package</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Package</em>' reference.
	 * @see #setPackage(EPackage)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetamodelImport_Package()
	 * @model
	 * @generated
	 */
	EPackage getPackage();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetamodelImport#getPackage <em>Package</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Package</em>' reference.
	 * @see #getPackage()
	 * @generated
	 */
	void setPackage(EPackage value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetamodelImport_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetamodelImport#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Use Qualified Names</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Use Qualified Names</em>' attribute.
	 * @see #setUseQualifiedNames(boolean)
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#getMetamodelImport_UseQualifiedNames()
	 * @model
	 * @generated
	 */
	boolean isUseQualifiedNames();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.common.elements.MetamodelImport#isUseQualifiedNames <em>Use Qualified Names</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Use Qualified Names</em>' attribute.
	 * @see #isUseQualifiedNames()
	 * @generated
	 */
	void setUseQualifiedNames(boolean value);

} // MetamodelImport
