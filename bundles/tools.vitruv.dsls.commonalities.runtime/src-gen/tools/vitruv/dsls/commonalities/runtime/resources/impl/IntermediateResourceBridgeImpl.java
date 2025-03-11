/**
 */
package tools.vitruv.dsls.commonalities.runtime.resources.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.resource.Resource;

import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;

import tools.vitruv.change.propagation.ResourceAccess;

import tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;

import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Intermediate Resource Bridge</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#getBaseURI <em>Base URI</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#isIsPersisted <em>Is Persisted</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#getIntermediateId <em>Intermediate Id</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#getResourceAccess <em>Resource Access</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#getCorrespondenceModel <em>Correspondence Model</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#getIntermediateNS <em>Intermediate NS</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#getEmfResource <em>Emf Resource</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl#isIsPersistenceEnabled <em>Is Persistence Enabled</em>}</li>
 * </ul>
 *
 * @generated
 */
public class IntermediateResourceBridgeImpl extends ResourceImpl implements IntermediateResourceBridge
{
	/**
	 * The default value of the '{@link #getBaseURI() <em>Base URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseURI()
	 * @generated
	 * @ordered
	 */
	protected static final URI BASE_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBaseURI() <em>Base URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseURI()
	 * @generated
	 * @ordered
	 */
	protected URI baseURI = BASE_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #isIsPersisted() <em>Is Persisted</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsPersisted()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_PERSISTED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsPersisted() <em>Is Persisted</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsPersisted()
	 * @generated
	 * @ordered
	 */
	protected boolean isPersisted = IS_PERSISTED_EDEFAULT;

	/**
	 * The default value of the '{@link #getIntermediateId() <em>Intermediate Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntermediateId()
	 * @generated
	 * @ordered
	 */
	protected static final String INTERMEDIATE_ID_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getResourceAccess() <em>Resource Access</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResourceAccess()
	 * @generated
	 * @ordered
	 */
	protected static final ResourceAccess RESOURCE_ACCESS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getResourceAccess() <em>Resource Access</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResourceAccess()
	 * @generated
	 * @ordered
	 */
	protected ResourceAccess resourceAccess = RESOURCE_ACCESS_EDEFAULT;

	/**
	 * The cached value of the '{@link #getCorrespondenceModel() <em>Correspondence Model</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCorrespondenceModel()
	 * @generated
	 * @ordered
	 */
	protected EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel;

	/**
	 * The default value of the '{@link #getIntermediateNS() <em>Intermediate NS</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntermediateNS()
	 * @generated
	 * @ordered
	 */
	protected static final String INTERMEDIATE_NS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIntermediateNS() <em>Intermediate NS</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntermediateNS()
	 * @generated
	 * @ordered
	 */
	protected String intermediateNS = INTERMEDIATE_NS_EDEFAULT;

	/**
	 * The default value of the '{@link #getEmfResource() <em>Emf Resource</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmfResource()
	 * @generated
	 * @ordered
	 */
	protected static final Resource EMF_RESOURCE_EDEFAULT = null;

	/**
	 * The default value of the '{@link #isIsPersistenceEnabled() <em>Is Persistence Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsPersistenceEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_PERSISTENCE_ENABLED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isIsPersistenceEnabled() <em>Is Persistence Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsPersistenceEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean isPersistenceEnabled = IS_PERSISTENCE_ENABLED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IntermediateResourceBridgeImpl()
	{
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass()
	{
		return ResourcesPackage.Literals.INTERMEDIATE_RESOURCE_BRIDGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public URI getBaseURI()
	{
		return baseURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIsPersisted()
	{
		return isPersisted;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getIntermediateId()
	{
		// TODO: implement this method to return the 'Intermediate Id' attribute
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceAccess getResourceAccess()
	{
		return resourceAccess;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResourceAccess(ResourceAccess newResourceAccess)
	{
		ResourceAccess oldResourceAccess = resourceAccess;
		resourceAccess = newResourceAccess;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS, oldResourceAccess, resourceAccess));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EditableCorrespondenceModelView<ReactionsCorrespondence> getCorrespondenceModel()
	{
		return correspondenceModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCorrespondenceModel(EditableCorrespondenceModelView<ReactionsCorrespondence> newCorrespondenceModel)
	{
		EditableCorrespondenceModelView<ReactionsCorrespondence> oldCorrespondenceModel = correspondenceModel;
		correspondenceModel = newCorrespondenceModel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL, oldCorrespondenceModel, correspondenceModel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getIntermediateNS()
	{
		return intermediateNS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIntermediateNS(String newIntermediateNS)
	{
		String oldIntermediateNS = intermediateNS;
		intermediateNS = newIntermediateNS;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS, oldIntermediateNS, intermediateNS));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource getEmfResource()
	{
		// TODO: implement this method to return the 'Emf Resource' attribute
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIsPersistenceEnabled()
	{
		return isPersistenceEnabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIsPersistenceEnabled(boolean newIsPersistenceEnabled)
	{
		boolean oldIsPersistenceEnabled = isPersistenceEnabled;
		isPersistenceEnabled = newIsPersistenceEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED, oldIsPersistenceEnabled, isPersistenceEnabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void remove()
	{
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void initialiseForModelElement(EObject eObject)
	{
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType)
	{
		switch (featureID)
		{
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__BASE_URI:
				return getBaseURI();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTED:
				return isIsPersisted();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_ID:
				return getIntermediateId();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS:
				return getResourceAccess();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL:
				return getCorrespondenceModel();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS:
				return getIntermediateNS();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__EMF_RESOURCE:
				return getEmfResource();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED:
				return isIsPersistenceEnabled();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue)
	{
		switch (featureID)
		{
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS:
				setResourceAccess((ResourceAccess)newValue);
				return;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL:
				setCorrespondenceModel((EditableCorrespondenceModelView<ReactionsCorrespondence>)newValue);
				return;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS:
				setIntermediateNS((String)newValue);
				return;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED:
				setIsPersistenceEnabled((Boolean)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID)
	{
		switch (featureID)
		{
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS:
				setResourceAccess(RESOURCE_ACCESS_EDEFAULT);
				return;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL:
				setCorrespondenceModel((EditableCorrespondenceModelView<ReactionsCorrespondence>)null);
				return;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS:
				setIntermediateNS(INTERMEDIATE_NS_EDEFAULT);
				return;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED:
				setIsPersistenceEnabled(IS_PERSISTENCE_ENABLED_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID)
	{
		switch (featureID)
		{
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__BASE_URI:
				return BASE_URI_EDEFAULT == null ? baseURI != null : !BASE_URI_EDEFAULT.equals(baseURI);
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTED:
				return isPersisted != IS_PERSISTED_EDEFAULT;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_ID:
				return INTERMEDIATE_ID_EDEFAULT == null ? getIntermediateId() != null : !INTERMEDIATE_ID_EDEFAULT.equals(getIntermediateId());
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS:
				return RESOURCE_ACCESS_EDEFAULT == null ? resourceAccess != null : !RESOURCE_ACCESS_EDEFAULT.equals(resourceAccess);
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL:
				return correspondenceModel != null;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS:
				return INTERMEDIATE_NS_EDEFAULT == null ? intermediateNS != null : !INTERMEDIATE_NS_EDEFAULT.equals(intermediateNS);
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__EMF_RESOURCE:
				return EMF_RESOURCE_EDEFAULT == null ? getEmfResource() != null : !EMF_RESOURCE_EDEFAULT.equals(getEmfResource());
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED:
				return isPersistenceEnabled != IS_PERSISTENCE_ENABLED_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException
	{
		switch (operationID)
		{
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE___REMOVE:
				remove();
				return null;
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE___INITIALISE_FOR_MODEL_ELEMENT__EOBJECT:
				initialiseForModelElement((EObject)arguments.get(0));
				return null;
		}
		return super.eInvoke(operationID, arguments);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString()
	{
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (baseURI: ");
		result.append(baseURI);
		result.append(", isPersisted: ");
		result.append(isPersisted);
		result.append(", resourceAccess: ");
		result.append(resourceAccess);
		result.append(", correspondenceModel: ");
		result.append(correspondenceModel);
		result.append(", intermediateNS: ");
		result.append(intermediateNS);
		result.append(", isPersistenceEnabled: ");
		result.append(isPersistenceEnabled);
		result.append(')');
		return result.toString();
	}

} //IntermediateResourceBridgeImpl
