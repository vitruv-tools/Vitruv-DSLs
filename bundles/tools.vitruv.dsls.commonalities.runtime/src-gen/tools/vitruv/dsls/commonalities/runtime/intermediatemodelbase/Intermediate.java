/**
 */
package tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Intermediate</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate#getIntermediateId <em>Intermediate Id</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage#getIntermediate()
 * @model abstract="true"
 * @generated
 */
public interface Intermediate extends EObject
{
	/**
	 * Returns the value of the '<em><b>Intermediate Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Intermediate Id</em>' attribute.
	 * @see #setIntermediateId(String)
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage#getIntermediate_IntermediateId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getIntermediateId();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate#getIntermediateId <em>Intermediate Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Intermediate Id</em>' attribute.
	 * @see #getIntermediateId()
	 * @generated
	 */
	void setIntermediateId(String value);

} // Intermediate
