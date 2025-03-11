/**
 */
package tools.vitruv.dsls.common.elements.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import tools.vitruv.dsls.common.elements.ElementsPackage;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Metaclass EReference Reference</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.impl.MetaclassEReferenceReferenceImpl#getFeature <em>Feature</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MetaclassEReferenceReferenceImpl extends MetaclassReferenceImpl implements MetaclassEReferenceReference
{
	/**
	 * The cached value of the '{@link #getFeature() <em>Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeature()
	 * @generated
	 * @ordered
	 */
	protected EReference feature;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MetaclassEReferenceReferenceImpl()
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
		return ElementsPackage.Literals.METACLASS_EREFERENCE_REFERENCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getFeature()
	{
		if (feature != null && feature.eIsProxy())
		{
			InternalEObject oldFeature = (InternalEObject)feature;
			feature = (EReference)eResolveProxy(oldFeature);
			if (feature != oldFeature)
			{
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ElementsPackage.METACLASS_EREFERENCE_REFERENCE__FEATURE, oldFeature, feature));
			}
		}
		return feature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference basicGetFeature()
	{
		return feature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFeature(EReference newFeature)
	{
		EReference oldFeature = feature;
		feature = newFeature;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ElementsPackage.METACLASS_EREFERENCE_REFERENCE__FEATURE, oldFeature, feature));
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
			case ElementsPackage.METACLASS_EREFERENCE_REFERENCE__FEATURE:
				if (resolve) return getFeature();
				return basicGetFeature();
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
			case ElementsPackage.METACLASS_EREFERENCE_REFERENCE__FEATURE:
				setFeature((EReference)newValue);
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
			case ElementsPackage.METACLASS_EREFERENCE_REFERENCE__FEATURE:
				setFeature((EReference)null);
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
			case ElementsPackage.METACLASS_EREFERENCE_REFERENCE__FEATURE:
				return feature != null;
		}
		return super.eIsSet(featureID);
	}

} //MetaclassEReferenceReferenceImpl
