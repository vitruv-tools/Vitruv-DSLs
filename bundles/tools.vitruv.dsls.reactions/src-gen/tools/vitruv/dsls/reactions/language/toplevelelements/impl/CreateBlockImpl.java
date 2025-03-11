/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import tools.vitruv.dsls.common.elements.NamedMetaclassReference;

import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Create Block</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.CreateBlockImpl#getCreateStatements <em>Create Statements</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CreateBlockImpl extends MinimalEObjectImpl.Container implements CreateBlock
{
	/**
	 * The cached value of the '{@link #getCreateStatements() <em>Create Statements</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreateStatements()
	 * @generated
	 * @ordered
	 */
	protected EList<NamedMetaclassReference> createStatements;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CreateBlockImpl()
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
		return TopLevelElementsPackage.Literals.CREATE_BLOCK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<NamedMetaclassReference> getCreateStatements()
	{
		if (createStatements == null)
		{
			createStatements = new EObjectContainmentEList<NamedMetaclassReference>(NamedMetaclassReference.class, this, TopLevelElementsPackage.CREATE_BLOCK__CREATE_STATEMENTS);
		}
		return createStatements;
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
			case TopLevelElementsPackage.CREATE_BLOCK__CREATE_STATEMENTS:
				return ((InternalEList<?>)getCreateStatements()).basicRemove(otherEnd, msgs);
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
			case TopLevelElementsPackage.CREATE_BLOCK__CREATE_STATEMENTS:
				return getCreateStatements();
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
			case TopLevelElementsPackage.CREATE_BLOCK__CREATE_STATEMENTS:
				getCreateStatements().clear();
				getCreateStatements().addAll((Collection<? extends NamedMetaclassReference>)newValue);
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
			case TopLevelElementsPackage.CREATE_BLOCK__CREATE_STATEMENTS:
				getCreateStatements().clear();
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
			case TopLevelElementsPackage.CREATE_BLOCK__CREATE_STATEMENTS:
				return createStatements != null && !createStatements.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //CreateBlockImpl
