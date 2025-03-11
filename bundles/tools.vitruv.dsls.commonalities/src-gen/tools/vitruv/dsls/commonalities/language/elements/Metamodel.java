/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metamodel</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.Metamodel#getMetaclasses <em>Metaclasses</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMetamodel()
 * @model
 * @generated
 */
public interface Metamodel extends Domain
{
	/**
	 * Returns the value of the '<em><b>Metaclasses</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.commonalities.language.elements.Metaclass}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Metaclasses</em>' containment reference list.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMetamodel_Metaclasses()
	 * @model containment="true" resolveProxies="true" changeable="false" derived="true" ordered="false"
	 * @generated
	 */
	EList<Metaclass> getMetaclasses();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true"
	 * @generated
	 */
	Metamodel forEPackage(EPackage ePackage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" classifierProviderDataType="tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider"
	 * @generated
	 */
	Metamodel withClassifierProvider(ClassifierProvider classifierProvider);

} // Metamodel
