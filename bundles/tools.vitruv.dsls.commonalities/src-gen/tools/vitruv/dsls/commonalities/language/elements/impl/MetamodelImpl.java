/**
 */
package tools.vitruv.dsls.commonalities.language.elements.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;
import tools.vitruv.dsls.commonalities.language.elements.Metamodel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Metamodel</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.MetamodelImpl#getMetaclasses <em>Metaclasses</em>}</li>
 * </ul>
 *
 * @generated
 */
public class MetamodelImpl extends MinimalEObjectImpl.Container implements Metamodel
{
	/**
	 * The cached value of the '{@link #getMetaclasses() <em>Metaclasses</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMetaclasses()
	 * @generated
	 * @ordered
	 */
	protected EList<Metaclass> metaclasses;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MetamodelImpl()
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
		return LanguageElementsPackage.Literals.METAMODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Metaclass> getMetaclasses()
	{
		if (metaclasses == null)
		{
			metaclasses = new EObjectContainmentEList.Resolving<Metaclass>(Metaclass.class, this, LanguageElementsPackage.METAMODEL__METACLASSES);
		}
		return metaclasses;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Metamodel forEPackage(EPackage ePackage)
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
	public Metamodel withClassifierProvider(ClassifierProvider classifierProvider)
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
	public String getName()
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
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
	{
		switch (featureID)
		{
			case LanguageElementsPackage.METAMODEL__METACLASSES:
				return ((InternalEList<?>)getMetaclasses()).basicRemove(otherEnd, msgs);
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
			case LanguageElementsPackage.METAMODEL__METACLASSES:
				return getMetaclasses();
		}
		return super.eGet(featureID, resolve, coreType);
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
			case LanguageElementsPackage.METAMODEL__METACLASSES:
				return metaclasses != null && !metaclasses.isEmpty();
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
			case LanguageElementsPackage.METAMODEL___FOR_EPACKAGE__EPACKAGE:
				return forEPackage((EPackage)arguments.get(0));
			case LanguageElementsPackage.METAMODEL___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER:
				return withClassifierProvider((ClassifierProvider)arguments.get(0));
			case LanguageElementsPackage.METAMODEL___GET_NAME:
				return getName();
		}
		return super.eInvoke(operationID, arguments);
	}

} //MetamodelImpl
