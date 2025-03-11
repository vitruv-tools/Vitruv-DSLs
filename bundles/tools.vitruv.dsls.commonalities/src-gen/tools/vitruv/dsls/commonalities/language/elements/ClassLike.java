/**
 */
package tools.vitruv.dsls.commonalities.language.elements;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Class Like</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.ClassLike#getPackageLikeContainer <em>Package Like Container</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getClassLike()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface ClassLike extends NamedElement
{
	/**
	 * Returns the value of the '<em><b>Package Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Package Like Container</em>' reference.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getClassLike_PackageLikeContainer()
	 * @model required="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	PackageLike getPackageLikeContainer();

} // ClassLike
