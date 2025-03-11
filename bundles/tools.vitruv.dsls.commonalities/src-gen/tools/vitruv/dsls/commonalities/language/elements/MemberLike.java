/**
 */
package tools.vitruv.dsls.commonalities.language.elements;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Member Like</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.MemberLike#getClassLikeContainer <em>Class Like Container</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMemberLike()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MemberLike extends NamedElement
{
	/**
	 * Returns the value of the '<em><b>Class Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Class Like Container</em>' reference.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getMemberLike_ClassLikeContainer()
	 * @model required="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	ClassLike getClassLikeContainer();

} // MemberLike
