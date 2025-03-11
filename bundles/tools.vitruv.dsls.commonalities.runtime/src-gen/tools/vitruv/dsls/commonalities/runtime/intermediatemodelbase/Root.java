/**
 */
package tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import tools.vitruv.dsls.commonalities.runtime.resources.Resource;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Root</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getIntermediates <em>Intermediates</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getResourceBridges <em>Resource Bridges</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getIntermediateId <em>Intermediate Id</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage#getRoot()
 * @model
 * @generated
 */
public interface Root extends EObject
{
	/**
	 * Returns the value of the '<em><b>Intermediates</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Intermediates</em>' containment reference list.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage#getRoot_Intermediates()
	 * @model containment="true" ordered="false"
	 * @generated
	 */
	EList<Intermediate> getIntermediates();

	/**
	 * Returns the value of the '<em><b>Resource Bridges</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.commonalities.runtime.resources.Resource}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Resource Bridges</em>' containment reference list.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage#getRoot_ResourceBridges()
	 * @model containment="true" ordered="false"
	 * @generated
	 */
	EList<Resource> getResourceBridges();

	/**
	 * Returns the value of the '<em><b>Intermediate Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Intermediate Id</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage#getRoot_IntermediateId()
	 * @model id="true" required="true" transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getIntermediateId();

} // Root
