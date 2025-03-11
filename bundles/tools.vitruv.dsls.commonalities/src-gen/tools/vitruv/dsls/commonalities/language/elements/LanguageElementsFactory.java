/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage
 * @generated
 */
public interface LanguageElementsFactory extends EFactory
{
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	LanguageElementsFactory eINSTANCE = tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>EClass Metaclass</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>EClass Metaclass</em>'.
	 * @generated
	 */
	EClassMetaclass createEClassMetaclass();

	/**
	 * Returns a new object of class '<em>EData Type Classifier</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>EData Type Classifier</em>'.
	 * @generated
	 */
	EDataTypeClassifier createEDataTypeClassifier();

	/**
	 * Returns a new object of class '<em>Metamodel</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Metamodel</em>'.
	 * @generated
	 */
	Metamodel createMetamodel();

	/**
	 * Returns a new object of class '<em>Resource Metaclass</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Resource Metaclass</em>'.
	 * @generated
	 */
	ResourceMetaclass createResourceMetaclass();

	/**
	 * Returns a new object of class '<em>EFeature Attribute</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>EFeature Attribute</em>'.
	 * @generated
	 */
	EFeatureAttribute createEFeatureAttribute();

	/**
	 * Returns a new object of class '<em>Most Specific Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Most Specific Type</em>'.
	 * @generated
	 */
	MostSpecificType createMostSpecificType();

	/**
	 * Returns a new object of class '<em>Least Specific Type</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Least Specific Type</em>'.
	 * @generated
	 */
	LeastSpecificType createLeastSpecificType();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	LanguageElementsPackage getLanguageElementsPackage();

} //LanguageElementsFactory
