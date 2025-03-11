/**
 */
package tools.vitruv.dsls.common.elements.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.common.elements.ElementsPackage;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.common.elements.MetamodelImport;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Metaclass Reference</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.impl.MetaclassReferenceImpl#getMetamodel <em>Metamodel</em>}</li>
 *   <li>{@link tools.vitruv.dsls.common.elements.impl.MetaclassReferenceImpl#getMetaclass <em>Metaclass</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MetaclassReferenceImpl extends MinimalEObjectImpl.Container implements MetaclassReference
{
	/**
	 * The cached value of the '{@link #getMetamodel() <em>Metamodel</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMetamodel()
	 * @generated
	 * @ordered
	 */
	protected MetamodelImport metamodel;

	/**
	 * The cached value of the '{@link #getMetaclass() <em>Metaclass</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMetaclass()
	 * @generated
	 * @ordered
	 */
	protected EClassifier metaclass;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MetaclassReferenceImpl()
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
		return ElementsPackage.Literals.METACLASS_REFERENCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetamodelImport getMetamodel()
	{
		if (metamodel != null && metamodel.eIsProxy())
		{
			InternalEObject oldMetamodel = (InternalEObject)metamodel;
			metamodel = (MetamodelImport)eResolveProxy(oldMetamodel);
			if (metamodel != oldMetamodel)
			{
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ElementsPackage.METACLASS_REFERENCE__METAMODEL, oldMetamodel, metamodel));
			}
		}
		return metamodel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetamodelImport basicGetMetamodel()
	{
		return metamodel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMetamodel(MetamodelImport newMetamodel)
	{
		MetamodelImport oldMetamodel = metamodel;
		metamodel = newMetamodel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ElementsPackage.METACLASS_REFERENCE__METAMODEL, oldMetamodel, metamodel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClassifier getMetaclass()
	{
		if (metaclass != null && metaclass.eIsProxy())
		{
			InternalEObject oldMetaclass = (InternalEObject)metaclass;
			metaclass = (EClassifier)eResolveProxy(oldMetaclass);
			if (metaclass != oldMetaclass)
			{
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ElementsPackage.METACLASS_REFERENCE__METACLASS, oldMetaclass, metaclass));
			}
		}
		return metaclass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClassifier basicGetMetaclass()
	{
		return metaclass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMetaclass(EClassifier newMetaclass)
	{
		EClassifier oldMetaclass = metaclass;
		metaclass = newMetaclass;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ElementsPackage.METACLASS_REFERENCE__METACLASS, oldMetaclass, metaclass));
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
			case ElementsPackage.METACLASS_REFERENCE__METAMODEL:
				if (resolve) return getMetamodel();
				return basicGetMetamodel();
			case ElementsPackage.METACLASS_REFERENCE__METACLASS:
				if (resolve) return getMetaclass();
				return basicGetMetaclass();
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
			case ElementsPackage.METACLASS_REFERENCE__METAMODEL:
				setMetamodel((MetamodelImport)newValue);
				return;
			case ElementsPackage.METACLASS_REFERENCE__METACLASS:
				setMetaclass((EClassifier)newValue);
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
			case ElementsPackage.METACLASS_REFERENCE__METAMODEL:
				setMetamodel((MetamodelImport)null);
				return;
			case ElementsPackage.METACLASS_REFERENCE__METACLASS:
				setMetaclass((EClassifier)null);
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
			case ElementsPackage.METACLASS_REFERENCE__METAMODEL:
				return metamodel != null;
			case ElementsPackage.METACLASS_REFERENCE__METACLASS:
				return metaclass != null;
		}
		return super.eIsSet(featureID);
	}

} //MetaclassReferenceImpl
