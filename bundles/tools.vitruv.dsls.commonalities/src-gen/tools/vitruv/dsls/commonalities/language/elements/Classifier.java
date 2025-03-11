/**
 */
package tools.vitruv.dsls.commonalities.language.elements;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Classifier</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getClassifier()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface Classifier extends NamedElement
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" subTypeRequired="true"
	 * @generated
	 */
	boolean isSuperTypeOf(Classifier subType);

} // Classifier
