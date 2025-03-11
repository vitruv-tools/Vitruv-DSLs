/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Resource Metaclass</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass#getAttributes <em>Attributes</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getResourceMetaclass()
 * @model
 * @generated
 */
public interface ResourceMetaclass extends Metaclass
{
	/**
	 * Returns the value of the '<em><b>Attributes</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.commonalities.language.elements.Attribute}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Attributes</em>' containment reference list.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getResourceMetaclass_Attributes()
	 * @model containment="true" resolveProxies="true" changeable="false" derived="true"
	 * @generated
	 */
	EList<Attribute> getAttributes();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" classifierProviderDataType="tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider"
	 * @generated
	 */
	ResourceMetaclass withClassifierProvider(ClassifierProvider classifierProvider);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" domainRequired="true"
	 * @generated
	 */
	ResourceMetaclass fromDomain(Domain domain);

} // ResourceMetaclass
