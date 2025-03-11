/**
 */
package tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Intermediate</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateImpl#getIntermediateId <em>Intermediate Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class IntermediateImpl extends MinimalEObjectImpl.Container implements Intermediate
{
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
	 * The cached value of the '{@link #getIntermediateId() <em>Intermediate Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntermediateId()
	 * @generated
	 * @ordered
	 */
	protected String intermediateId = INTERMEDIATE_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IntermediateImpl()
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
		return IntermediateModelBasePackage.Literals.INTERMEDIATE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getIntermediateId()
	{
		return intermediateId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIntermediateId(String newIntermediateId)
	{
		String oldIntermediateId = intermediateId;
		intermediateId = newIntermediateId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IntermediateModelBasePackage.INTERMEDIATE__INTERMEDIATE_ID, oldIntermediateId, intermediateId));
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
			case IntermediateModelBasePackage.INTERMEDIATE__INTERMEDIATE_ID:
				return getIntermediateId();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue)
	{
		switch (featureID)
		{
			case IntermediateModelBasePackage.INTERMEDIATE__INTERMEDIATE_ID:
				setIntermediateId((String)newValue);
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
			case IntermediateModelBasePackage.INTERMEDIATE__INTERMEDIATE_ID:
				setIntermediateId(INTERMEDIATE_ID_EDEFAULT);
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
			case IntermediateModelBasePackage.INTERMEDIATE__INTERMEDIATE_ID:
				return INTERMEDIATE_ID_EDEFAULT == null ? intermediateId != null : !INTERMEDIATE_ID_EDEFAULT.equals(intermediateId);
		}
		return super.eIsSet(featureID);
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
		result.append(" (intermediateId: ");
		result.append(intermediateId);
		result.append(')');
		return result.toString();
	}

} //IntermediateImpl
