/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Metaclass</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass#getDomain <em>Domain</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass#getAllMembers <em>All Members</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass#isAbstract <em>Abstract</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMetaclass()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface Metaclass extends ClassLike, Classifier
{
	/**
	 * Returns the value of the '<em><b>Domain</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Domain</em>' reference.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMetaclass_Domain()
	 * @model required="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	Domain getDomain();

	/**
	 * Returns the value of the '<em><b>All Members</b></em>' reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.commonalities.language.elements.MetaclassMember}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>All Members</em>' reference list.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMetaclass_AllMembers()
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	EList<MetaclassMember> getAllMembers();

	/**
	 * Returns the value of the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Abstract</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMetaclass_Abstract()
	 * @model required="true" transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	boolean isAbstract();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	<A extends Attribute> EList<A> getAttributes();

} // Metaclass
