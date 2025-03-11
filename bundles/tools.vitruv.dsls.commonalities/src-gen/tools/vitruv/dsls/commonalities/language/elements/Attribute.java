/**
 */
package tools.vitruv.dsls.commonalities.language.elements;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Attribute</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.Attribute#isMultiValued <em>Multi Valued</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getAttribute()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface Attribute extends MetaclassMember
{
	/**
	 * Returns the value of the '<em><b>Multi Valued</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Multi Valued</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getAttribute_MultiValued()
	 * @model required="true" transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	boolean isMultiValued();

} // Attribute
