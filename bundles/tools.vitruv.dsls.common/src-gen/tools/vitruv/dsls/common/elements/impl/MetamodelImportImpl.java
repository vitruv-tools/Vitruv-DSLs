/**
 */
package tools.vitruv.dsls.common.elements.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.common.elements.ElementsPackage;
import tools.vitruv.dsls.common.elements.MetamodelImport;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Metamodel Import</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.common.elements.impl.MetamodelImportImpl#getPackage <em>Package</em>}</li>
 *   <li>{@link tools.vitruv.dsls.common.elements.impl.MetamodelImportImpl#getName <em>Name</em>}</li>
 *   <li>{@link tools.vitruv.dsls.common.elements.impl.MetamodelImportImpl#isUseQualifiedNames <em>Use Qualified Names</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MetamodelImportImpl extends MinimalEObjectImpl.Container implements MetamodelImport
{
	/**
	 * The cached value of the '{@link #getPackage() <em>Package</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPackage()
	 * @generated
	 * @ordered
	 */
	protected EPackage package_;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #isUseQualifiedNames() <em>Use Qualified Names</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isUseQualifiedNames()
	 * @generated
	 * @ordered
	 */
	protected static final boolean USE_QUALIFIED_NAMES_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isUseQualifiedNames() <em>Use Qualified Names</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isUseQualifiedNames()
	 * @generated
	 * @ordered
	 */
	protected boolean useQualifiedNames = USE_QUALIFIED_NAMES_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MetamodelImportImpl()
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
		return ElementsPackage.Literals.METAMODEL_IMPORT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EPackage getPackage()
	{
		if (package_ != null && package_.eIsProxy())
		{
			InternalEObject oldPackage = (InternalEObject)package_;
			package_ = (EPackage)eResolveProxy(oldPackage);
			if (package_ != oldPackage)
			{
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ElementsPackage.METAMODEL_IMPORT__PACKAGE, oldPackage, package_));
			}
		}
		return package_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EPackage basicGetPackage()
	{
		return package_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPackage(EPackage newPackage)
	{
		EPackage oldPackage = package_;
		package_ = newPackage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ElementsPackage.METAMODEL_IMPORT__PACKAGE, oldPackage, package_));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName)
	{
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ElementsPackage.METAMODEL_IMPORT__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isUseQualifiedNames()
	{
		return useQualifiedNames;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUseQualifiedNames(boolean newUseQualifiedNames)
	{
		boolean oldUseQualifiedNames = useQualifiedNames;
		useQualifiedNames = newUseQualifiedNames;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ElementsPackage.METAMODEL_IMPORT__USE_QUALIFIED_NAMES, oldUseQualifiedNames, useQualifiedNames));
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
			case ElementsPackage.METAMODEL_IMPORT__PACKAGE:
				if (resolve) return getPackage();
				return basicGetPackage();
			case ElementsPackage.METAMODEL_IMPORT__NAME:
				return getName();
			case ElementsPackage.METAMODEL_IMPORT__USE_QUALIFIED_NAMES:
				return isUseQualifiedNames();
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
			case ElementsPackage.METAMODEL_IMPORT__PACKAGE:
				setPackage((EPackage)newValue);
				return;
			case ElementsPackage.METAMODEL_IMPORT__NAME:
				setName((String)newValue);
				return;
			case ElementsPackage.METAMODEL_IMPORT__USE_QUALIFIED_NAMES:
				setUseQualifiedNames((Boolean)newValue);
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
			case ElementsPackage.METAMODEL_IMPORT__PACKAGE:
				setPackage((EPackage)null);
				return;
			case ElementsPackage.METAMODEL_IMPORT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ElementsPackage.METAMODEL_IMPORT__USE_QUALIFIED_NAMES:
				setUseQualifiedNames(USE_QUALIFIED_NAMES_EDEFAULT);
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
			case ElementsPackage.METAMODEL_IMPORT__PACKAGE:
				return package_ != null;
			case ElementsPackage.METAMODEL_IMPORT__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ElementsPackage.METAMODEL_IMPORT__USE_QUALIFIED_NAMES:
				return useQualifiedNames != USE_QUALIFIED_NAMES_EDEFAULT;
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
		result.append(" (name: ");
		result.append(name);
		result.append(", useQualifiedNames: ");
		result.append(useQualifiedNames);
		result.append(')');
		return result.toString();
	}

} //MetamodelImportImpl
