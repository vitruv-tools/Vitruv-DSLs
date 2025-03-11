/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EcoreUtil;

import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reaction</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionImpl#getDocumentation <em>Documentation</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionImpl#getOverriddenReactionsSegment <em>Overridden Reactions Segment</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionImpl#getName <em>Name</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionImpl#getTrigger <em>Trigger</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionImpl#getCallRoutine <em>Call Routine</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionImpl#getReactionsSegment <em>Reactions Segment</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReactionImpl extends MinimalEObjectImpl.Container implements Reaction
{
	/**
	 * The default value of the '{@link #getDocumentation() <em>Documentation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDocumentation()
	 * @generated
	 * @ordered
	 */
	protected static final String DOCUMENTATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDocumentation() <em>Documentation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDocumentation()
	 * @generated
	 * @ordered
	 */
	protected String documentation = DOCUMENTATION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getOverriddenReactionsSegment() <em>Overridden Reactions Segment</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOverriddenReactionsSegment()
	 * @generated
	 * @ordered
	 */
	protected ReactionsSegment overriddenReactionsSegment;

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
	 * The cached value of the '{@link #getTrigger() <em>Trigger</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrigger()
	 * @generated
	 * @ordered
	 */
	protected Trigger trigger;

	/**
	 * The cached value of the '{@link #getCallRoutine() <em>Call Routine</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCallRoutine()
	 * @generated
	 * @ordered
	 */
	protected RoutineCall callRoutine;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReactionImpl()
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
		return TopLevelElementsPackage.Literals.REACTION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDocumentation()
	{
		return documentation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDocumentation(String newDocumentation)
	{
		String oldDocumentation = documentation;
		documentation = newDocumentation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__DOCUMENTATION, oldDocumentation, documentation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReactionsSegment getOverriddenReactionsSegment()
	{
		if (overriddenReactionsSegment != null && overriddenReactionsSegment.eIsProxy())
		{
			InternalEObject oldOverriddenReactionsSegment = (InternalEObject)overriddenReactionsSegment;
			overriddenReactionsSegment = (ReactionsSegment)eResolveProxy(oldOverriddenReactionsSegment);
			if (overriddenReactionsSegment != oldOverriddenReactionsSegment)
			{
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TopLevelElementsPackage.REACTION__OVERRIDDEN_REACTIONS_SEGMENT, oldOverriddenReactionsSegment, overriddenReactionsSegment));
			}
		}
		return overriddenReactionsSegment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReactionsSegment basicGetOverriddenReactionsSegment()
	{
		return overriddenReactionsSegment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOverriddenReactionsSegment(ReactionsSegment newOverriddenReactionsSegment)
	{
		ReactionsSegment oldOverriddenReactionsSegment = overriddenReactionsSegment;
		overriddenReactionsSegment = newOverriddenReactionsSegment;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__OVERRIDDEN_REACTIONS_SEGMENT, oldOverriddenReactionsSegment, overriddenReactionsSegment));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName)
	{
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Trigger getTrigger()
	{
		return trigger;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTrigger(Trigger newTrigger, NotificationChain msgs)
	{
		Trigger oldTrigger = trigger;
		trigger = newTrigger;
		if (eNotificationRequired())
		{
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__TRIGGER, oldTrigger, newTrigger);
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
	public void setTrigger(Trigger newTrigger)
	{
		if (newTrigger != trigger)
		{
			NotificationChain msgs = null;
			if (trigger != null)
				msgs = ((InternalEObject)trigger).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.REACTION__TRIGGER, null, msgs);
			if (newTrigger != null)
				msgs = ((InternalEObject)newTrigger).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.REACTION__TRIGGER, null, msgs);
			msgs = basicSetTrigger(newTrigger, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__TRIGGER, newTrigger, newTrigger));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RoutineCall getCallRoutine()
	{
		return callRoutine;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetCallRoutine(RoutineCall newCallRoutine, NotificationChain msgs)
	{
		RoutineCall oldCallRoutine = callRoutine;
		callRoutine = newCallRoutine;
		if (eNotificationRequired())
		{
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__CALL_ROUTINE, oldCallRoutine, newCallRoutine);
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
	public void setCallRoutine(RoutineCall newCallRoutine)
	{
		if (newCallRoutine != callRoutine)
		{
			NotificationChain msgs = null;
			if (callRoutine != null)
				msgs = ((InternalEObject)callRoutine).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.REACTION__CALL_ROUTINE, null, msgs);
			if (newCallRoutine != null)
				msgs = ((InternalEObject)newCallRoutine).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.REACTION__CALL_ROUTINE, null, msgs);
			msgs = basicSetCallRoutine(newCallRoutine, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__CALL_ROUTINE, newCallRoutine, newCallRoutine));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReactionsSegment getReactionsSegment()
	{
		if (eContainerFeatureID() != TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT) return null;
		return (ReactionsSegment)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetReactionsSegment(ReactionsSegment newReactionsSegment, NotificationChain msgs)
	{
		msgs = eBasicSetContainer((InternalEObject)newReactionsSegment, TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReactionsSegment(ReactionsSegment newReactionsSegment)
	{
		if (newReactionsSegment != eInternalContainer() || (eContainerFeatureID() != TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT && newReactionsSegment != null))
		{
			if (EcoreUtil.isAncestor(this, newReactionsSegment))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newReactionsSegment != null)
				msgs = ((InternalEObject)newReactionsSegment).eInverseAdd(this, TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS, ReactionsSegment.class, msgs);
			msgs = basicSetReactionsSegment(newReactionsSegment, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT, newReactionsSegment, newReactionsSegment));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs)
	{
		switch (featureID)
		{
			case TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetReactionsSegment((ReactionsSegment)otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
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
			case TopLevelElementsPackage.REACTION__TRIGGER:
				return basicSetTrigger(null, msgs);
			case TopLevelElementsPackage.REACTION__CALL_ROUTINE:
				return basicSetCallRoutine(null, msgs);
			case TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT:
				return basicSetReactionsSegment(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs)
	{
		switch (eContainerFeatureID())
		{
			case TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT:
				return eInternalContainer().eInverseRemove(this, TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS, ReactionsSegment.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
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
			case TopLevelElementsPackage.REACTION__DOCUMENTATION:
				return getDocumentation();
			case TopLevelElementsPackage.REACTION__OVERRIDDEN_REACTIONS_SEGMENT:
				if (resolve) return getOverriddenReactionsSegment();
				return basicGetOverriddenReactionsSegment();
			case TopLevelElementsPackage.REACTION__NAME:
				return getName();
			case TopLevelElementsPackage.REACTION__TRIGGER:
				return getTrigger();
			case TopLevelElementsPackage.REACTION__CALL_ROUTINE:
				return getCallRoutine();
			case TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT:
				return getReactionsSegment();
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
			case TopLevelElementsPackage.REACTION__DOCUMENTATION:
				setDocumentation((String)newValue);
				return;
			case TopLevelElementsPackage.REACTION__OVERRIDDEN_REACTIONS_SEGMENT:
				setOverriddenReactionsSegment((ReactionsSegment)newValue);
				return;
			case TopLevelElementsPackage.REACTION__NAME:
				setName((String)newValue);
				return;
			case TopLevelElementsPackage.REACTION__TRIGGER:
				setTrigger((Trigger)newValue);
				return;
			case TopLevelElementsPackage.REACTION__CALL_ROUTINE:
				setCallRoutine((RoutineCall)newValue);
				return;
			case TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT:
				setReactionsSegment((ReactionsSegment)newValue);
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
			case TopLevelElementsPackage.REACTION__DOCUMENTATION:
				setDocumentation(DOCUMENTATION_EDEFAULT);
				return;
			case TopLevelElementsPackage.REACTION__OVERRIDDEN_REACTIONS_SEGMENT:
				setOverriddenReactionsSegment((ReactionsSegment)null);
				return;
			case TopLevelElementsPackage.REACTION__NAME:
				setName(NAME_EDEFAULT);
				return;
			case TopLevelElementsPackage.REACTION__TRIGGER:
				setTrigger((Trigger)null);
				return;
			case TopLevelElementsPackage.REACTION__CALL_ROUTINE:
				setCallRoutine((RoutineCall)null);
				return;
			case TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT:
				setReactionsSegment((ReactionsSegment)null);
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
			case TopLevelElementsPackage.REACTION__DOCUMENTATION:
				return DOCUMENTATION_EDEFAULT == null ? documentation != null : !DOCUMENTATION_EDEFAULT.equals(documentation);
			case TopLevelElementsPackage.REACTION__OVERRIDDEN_REACTIONS_SEGMENT:
				return overriddenReactionsSegment != null;
			case TopLevelElementsPackage.REACTION__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case TopLevelElementsPackage.REACTION__TRIGGER:
				return trigger != null;
			case TopLevelElementsPackage.REACTION__CALL_ROUTINE:
				return callRoutine != null;
			case TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT:
				return getReactionsSegment() != null;
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
		result.append(" (documentation: ");
		result.append(documentation);
		result.append(", name: ");
		result.append(name);
		result.append(')');
		return result.toString();
	}

} //ReactionImpl
