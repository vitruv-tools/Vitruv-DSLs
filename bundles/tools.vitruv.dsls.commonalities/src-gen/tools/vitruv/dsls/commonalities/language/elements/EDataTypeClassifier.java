/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.ecore.EDataType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EData Type Classifier</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#getEDataTypeClassifier()
 * @model
 * @generated
 */
public interface EDataTypeClassifier extends Classifier
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true"
	 * @generated
	 */
	EDataTypeClassifier forEDataType(EDataType eDataType);

} // EDataTypeClassifier
