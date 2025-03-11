/**
 */
package tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root;

import tools.vitruv.dsls.commonalities.runtime.resources.Resource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Root</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.RootImpl#getIntermediates <em>Intermediates</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.RootImpl#getResourceBridges <em>Resource Bridges</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.RootImpl#getIntermediateId <em>Intermediate Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RootImpl extends MinimalEObjectImpl.Container implements Root
{
	/**
	 * The cached value of the '{@link #getIntermediates() <em>Intermediates</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIntermediates()
	 * @generated
	 * @ordered
	 */
	protected EList<Intermediate> intermediates;

	/**
	 * The cached value of the '{@link #getResourceBridges() <em>Resource Bridges</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResourceBridges()
	 * @generated
	 * @ordered
	 */
	protected EList<Resource> resourceBridges;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RootImpl()
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
		return IntermediateModelBasePackage.Literals.ROOT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Intermediate> getIntermediates()
	{
		if (intermediates == null)
		{
			intermediates = new EObjectContainmentEList<Intermediate>(Intermediate.class, this, IntermediateModelBasePackage.ROOT__INTERMEDIATES);
		}
		return intermediates;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Resource> getResourceBridges()
	{
		if (resourceBridges == null)
		{
			resourceBridges = new EObjectContainmentEList<Resource>(Resource.class, this, IntermediateModelBasePackage.ROOT__RESOURCE_BRIDGES);
		}
		return resourceBridges;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getIntermediateId()
	{
		return "root";
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
	{
		switch (featureID)
		{
			case IntermediateModelBasePackage.ROOT__INTERMEDIATES:
				return ((InternalEList<?>)getIntermediates()).basicRemove(otherEnd, msgs);
			case IntermediateModelBasePackage.ROOT__RESOURCE_BRIDGES:
				return ((InternalEList<?>)getResourceBridges()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
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
			case IntermediateModelBasePackage.ROOT__INTERMEDIATES:
				return getIntermediates();
			case IntermediateModelBasePackage.ROOT__RESOURCE_BRIDGES:
				return getResourceBridges();
			case IntermediateModelBasePackage.ROOT__INTERMEDIATE_ID:
				return getIntermediateId();
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
			case IntermediateModelBasePackage.ROOT__INTERMEDIATES:
				getIntermediates().clear();
				getIntermediates().addAll((Collection<? extends Intermediate>)newValue);
				return;
			case IntermediateModelBasePackage.ROOT__RESOURCE_BRIDGES:
				getResourceBridges().clear();
				getResourceBridges().addAll((Collection<? extends Resource>)newValue);
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
			case IntermediateModelBasePackage.ROOT__INTERMEDIATES:
				getIntermediates().clear();
				return;
			case IntermediateModelBasePackage.ROOT__RESOURCE_BRIDGES:
				getResourceBridges().clear();
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
			case IntermediateModelBasePackage.ROOT__INTERMEDIATES:
				return intermediates != null && !intermediates.isEmpty();
			case IntermediateModelBasePackage.ROOT__RESOURCE_BRIDGES:
				return resourceBridges != null && !resourceBridges.isEmpty();
			case IntermediateModelBasePackage.ROOT__INTERMEDIATE_ID:
				return INTERMEDIATE_ID_EDEFAULT == null ? getIntermediateId() != null : !INTERMEDIATE_ID_EDEFAULT.equals(getIntermediateId());
		}
		return super.eIsSet(featureID);
	}

} //RootImpl
