/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Routine Override Import Path</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.RoutineOverrideImportPathImpl#getReactionsSegment <em>Reactions Segment</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.RoutineOverrideImportPathImpl#getParent <em>Parent</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RoutineOverrideImportPathImpl extends MinimalEObjectImpl.Container implements RoutineOverrideImportPath
{
	/**
	 * The cached value of the '{@link #getReactionsSegment() <em>Reactions Segment</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReactionsSegment()
	 * @generated
	 * @ordered
	 */
	protected ReactionsSegment reactionsSegment;

	/**
	 * The cached value of the '{@link #getParent() <em>Parent</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected RoutineOverrideImportPath parent;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RoutineOverrideImportPathImpl()
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
		return TopLevelElementsPackage.Literals.ROUTINE_OVERRIDE_IMPORT_PATH;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReactionsSegment getReactionsSegment()
	{
		if (reactionsSegment != null && reactionsSegment.eIsProxy())
		{
			InternalEObject oldReactionsSegment = (InternalEObject)reactionsSegment;
			reactionsSegment = (ReactionsSegment)eResolveProxy(oldReactionsSegment);
			if (reactionsSegment != oldReactionsSegment)
			{
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT, oldReactionsSegment, reactionsSegment));
			}
		}
		return reactionsSegment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReactionsSegment basicGetReactionsSegment()
	{
		return reactionsSegment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReactionsSegment(ReactionsSegment newReactionsSegment)
	{
		ReactionsSegment oldReactionsSegment = reactionsSegment;
		reactionsSegment = newReactionsSegment;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT, oldReactionsSegment, reactionsSegment));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RoutineOverrideImportPath getParent()
	{
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(RoutineOverrideImportPath newParent, NotificationChain msgs)
	{
		RoutineOverrideImportPath oldParent = parent;
		parent = newParent;
		if (eNotificationRequired())
		{
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT, oldParent, newParent);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setParent(RoutineOverrideImportPath newParent)
	{
		if (newParent != parent)
		{
			NotificationChain msgs = null;
			if (parent != null)
				msgs = ((InternalEObject)parent).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT, null, msgs);
			if (newParent != null)
				msgs = ((InternalEObject)newParent).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT, null, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT, newParent, newParent));
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
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT:
				return basicSetParent(null, msgs);
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
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT:
				if (resolve) return getReactionsSegment();
				return basicGetReactionsSegment();
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT:
				return getParent();
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
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT:
				setReactionsSegment((ReactionsSegment)newValue);
				return;
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT:
				setParent((RoutineOverrideImportPath)newValue);
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
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT:
				setReactionsSegment((ReactionsSegment)null);
				return;
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT:
				setParent((RoutineOverrideImportPath)null);
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
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT:
				return reactionsSegment != null;
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH__PARENT:
				return parent != null;
		}
		return super.eIsSet(featureID);
	}

} //RoutineOverrideImportPathImpl
