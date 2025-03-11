/**
 */
package tools.vitruv.dsls.commonalities.runtime.resources;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.EObject;

import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;

import tools.vitruv.change.propagation.ResourceAccess;

import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Intermediate Resource Bridge</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getBaseURI <em>Base URI</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#isIsPersisted <em>Is Persisted</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getIntermediateId <em>Intermediate Id</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getResourceAccess <em>Resource Access</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getCorrespondenceModel <em>Correspondence Model</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getIntermediateNS <em>Intermediate NS</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getEmfResource <em>Emf Resource</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#isIsPersistenceEnabled <em>Is Persistence Enabled</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge()
 * @model
 * @generated
 */
public interface IntermediateResourceBridge extends Resource
{
	/**
	 * Returns the value of the '<em><b>Base URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Base URI</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_BaseURI()
	 * @model dataType="tools.vitruv.dsls.commonalities.runtime.resources.URI" transient="true" changeable="false"
	 * @generated
	 */
	URI getBaseURI();

	/**
	 * Returns the value of the '<em><b>Is Persisted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Persisted</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_IsPersisted()
	 * @model transient="true" changeable="false"
	 * @generated
	 */
	boolean isIsPersisted();

	/**
	 * Returns the value of the '<em><b>Intermediate Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Intermediate Id</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_IntermediateId()
	 * @model id="true" required="true" transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getIntermediateId();

	/**
	 * Returns the value of the '<em><b>Resource Access</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Resource Access</em>' attribute.
	 * @see #setResourceAccess(ResourceAccess)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_ResourceAccess()
	 * @model dataType="tools.vitruv.dsls.commonalities.runtime.resources.ResourceAccess" transient="true"
	 * @generated
	 */
	ResourceAccess getResourceAccess();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getResourceAccess <em>Resource Access</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Resource Access</em>' attribute.
	 * @see #getResourceAccess()
	 * @generated
	 */
	void setResourceAccess(ResourceAccess value);

	/**
	 * Returns the value of the '<em><b>Correspondence Model</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Correspondence Model</em>' attribute.
	 * @see #setCorrespondenceModel(EditableCorrespondenceModelView)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_CorrespondenceModel()
	 * @model dataType="tools.vitruv.dsls.commonalities.runtime.resources.EditableCorrespondenceModelView&lt;tools.vitruv.dsls.commonalities.runtime.resources.ReactionsCorrespondence&gt;" transient="true"
	 * @generated
	 */
	EditableCorrespondenceModelView<ReactionsCorrespondence> getCorrespondenceModel();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getCorrespondenceModel <em>Correspondence Model</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Correspondence Model</em>' attribute.
	 * @see #getCorrespondenceModel()
	 * @generated
	 */
	void setCorrespondenceModel(EditableCorrespondenceModelView<ReactionsCorrespondence> value);

	/**
	 * Returns the value of the '<em><b>Intermediate NS</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Intermediate NS</em>' attribute.
	 * @see #setIntermediateNS(String)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_IntermediateNS()
	 * @model unique="false" required="true" ordered="false"
	 * @generated
	 */
	String getIntermediateNS();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getIntermediateNS <em>Intermediate NS</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Intermediate NS</em>' attribute.
	 * @see #getIntermediateNS()
	 * @generated
	 */
	void setIntermediateNS(String value);

	/**
	 * Returns the value of the '<em><b>Emf Resource</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Emf Resource</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_EmfResource()
	 * @model unique="false" dataType="tools.vitruv.dsls.commonalities.runtime.resources.EmfResource" transient="true" changeable="false" volatile="true" derived="true" ordered="false"
	 * @generated
	 */
	org.eclipse.emf.ecore.resource.Resource getEmfResource();

	/**
	 * Returns the value of the '<em><b>Is Persistence Enabled</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Persistence Enabled</em>' attribute.
	 * @see #setIsPersistenceEnabled(boolean)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getIntermediateResourceBridge_IsPersistenceEnabled()
	 * @model default="true" unique="false" required="true" ordered="false"
	 * @generated
	 */
	boolean isIsPersistenceEnabled();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#isIsPersistenceEnabled <em>Is Persistence Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Persistence Enabled</em>' attribute.
	 * @see #isIsPersistenceEnabled()
	 * @generated
	 */
	void setIsPersistenceEnabled(boolean value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void remove();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void initialiseForModelElement(EObject eObject);

} // IntermediateResourceBridge
