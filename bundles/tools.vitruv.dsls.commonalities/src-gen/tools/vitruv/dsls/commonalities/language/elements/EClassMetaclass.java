/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EClass Metaclass</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#getAttributes <em>Attributes</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getEClassMetaclass()
 * @model
 * @generated
 */
public interface EClassMetaclass extends Metaclass
{
	/**
	 * Returns the value of the '<em><b>Attributes</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.commonalities.language.elements.Attribute}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Attributes</em>' containment reference list.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getEClassMetaclass_Attributes()
	 * @model containment="true" resolveProxies="true" changeable="false" derived="true"
	 * @generated
	 */
	EList<Attribute> getAttributes();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true"
	 * @generated
	 */
	EClassMetaclass forEClass(EClass eClass);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" classifierProviderDataType="tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider"
	 * @generated
	 */
	EClassMetaclass withClassifierProvider(ClassifierProvider classifierProvider);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" domainRequired="true"
	 * @generated
	 */
	EClassMetaclass fromDomain(Domain domain);

} // EClassMetaclass
