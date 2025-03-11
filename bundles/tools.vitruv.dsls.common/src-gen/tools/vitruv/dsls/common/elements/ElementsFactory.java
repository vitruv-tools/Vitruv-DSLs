/**
 */
package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.common.elements.ElementsPackage
 * @generated
 */
public interface ElementsFactory extends EFactory
{
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ElementsFactory eINSTANCE = tools.vitruv.dsls.common.elements.impl.ElementsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Metamodel Import</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Metamodel Import</em>'.
	 * @generated
	 */
	MetamodelImport createMetamodelImport();

	/**
	 * Returns a new object of class '<em>Metaclass Reference</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Metaclass Reference</em>'.
	 * @generated
	 */
	MetaclassReference createMetaclassReference();

	/**
	 * Returns a new object of class '<em>Named Metaclass Reference</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Named Metaclass Reference</em>'.
	 * @generated
	 */
	NamedMetaclassReference createNamedMetaclassReference();

	/**
	 * Returns a new object of class '<em>Metaclass Feature Reference</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Metaclass Feature Reference</em>'.
	 * @generated
	 */
	MetaclassFeatureReference createMetaclassFeatureReference();

	/**
	 * Returns a new object of class '<em>Metaclass EAttribute Reference</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Metaclass EAttribute Reference</em>'.
	 * @generated
	 */
	MetaclassEAttributeReference createMetaclassEAttributeReference();

	/**
	 * Returns a new object of class '<em>Metaclass EReference Reference</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Metaclass EReference Reference</em>'.
	 * @generated
	 */
	MetaclassEReferenceReference createMetaclassEReferenceReference();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ElementsPackage getElementsPackage();

} //ElementsFactory
