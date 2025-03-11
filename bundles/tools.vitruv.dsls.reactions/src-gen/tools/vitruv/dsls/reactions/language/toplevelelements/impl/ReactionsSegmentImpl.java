/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

import tools.vitruv.dsls.common.elements.MetamodelImport;

import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reactions Segment</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsSegmentImpl#getName <em>Name</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsSegmentImpl#getFromMetamodels <em>From Metamodels</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsSegmentImpl#getToMetamodels <em>To Metamodels</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsSegmentImpl#getReactionsImports <em>Reactions Imports</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsSegmentImpl#getReactions <em>Reactions</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsSegmentImpl#getRoutines <em>Routines</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReactionsSegmentImpl extends MinimalEObjectImpl.Container implements ReactionsSegment
{
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
	 * The cached value of the '{@link #getFromMetamodels() <em>From Metamodels</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFromMetamodels()
	 * @generated
	 * @ordered
	 */
	protected EList<MetamodelImport> fromMetamodels;

	/**
	 * The cached value of the '{@link #getToMetamodels() <em>To Metamodels</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToMetamodels()
	 * @generated
	 * @ordered
	 */
	protected EList<MetamodelImport> toMetamodels;

	/**
	 * The cached value of the '{@link #getReactionsImports() <em>Reactions Imports</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReactionsImports()
	 * @generated
	 * @ordered
	 */
	protected EList<ReactionsImport> reactionsImports;

	/**
	 * The cached value of the '{@link #getReactions() <em>Reactions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReactions()
	 * @generated
	 * @ordered
	 */
	protected EList<Reaction> reactions;

	/**
	 * The cached value of the '{@link #getRoutines() <em>Routines</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRoutines()
	 * @generated
	 * @ordered
	 */
	protected EList<Routine> routines;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReactionsSegmentImpl()
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
		return TopLevelElementsPackage.Literals.REACTIONS_SEGMENT;
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
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTIONS_SEGMENT__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<MetamodelImport> getFromMetamodels()
	{
		if (fromMetamodels == null)
		{
			fromMetamodels = new EObjectResolvingEList<MetamodelImport>(MetamodelImport.class, this, TopLevelElementsPackage.REACTIONS_SEGMENT__FROM_METAMODELS);
		}
		return fromMetamodels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<MetamodelImport> getToMetamodels()
	{
		if (toMetamodels == null)
		{
			toMetamodels = new EObjectResolvingEList<MetamodelImport>(MetamodelImport.class, this, TopLevelElementsPackage.REACTIONS_SEGMENT__TO_METAMODELS);
		}
		return toMetamodels;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ReactionsImport> getReactionsImports()
	{
		if (reactionsImports == null)
		{
			reactionsImports = new EObjectContainmentEList<ReactionsImport>(ReactionsImport.class, this, TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS_IMPORTS);
		}
		return reactionsImports;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Reaction> getReactions()
	{
		if (reactions == null)
		{
			reactions = new EObjectContainmentWithInverseEList<Reaction>(Reaction.class, this, TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS, TopLevelElementsPackage.REACTION__REACTIONS_SEGMENT);
		}
		return reactions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Routine> getRoutines()
	{
		if (routines == null)
		{
			routines = new EObjectContainmentWithInverseEList<Routine>(Routine.class, this, TopLevelElementsPackage.REACTIONS_SEGMENT__ROUTINES, TopLevelElementsPackage.ROUTINE__REACTIONS_SEGMENT);
		}
		return routines;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs)
	{
		switch (featureID)
		{
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getReactions()).basicAdd(otherEnd, msgs);
			case TopLevelElementsPackage.REACTIONS_SEGMENT__ROUTINES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getRoutines()).basicAdd(otherEnd, msgs);
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
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS_IMPORTS:
				return ((InternalEList<?>)getReactionsImports()).basicRemove(otherEnd, msgs);
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS:
				return ((InternalEList<?>)getReactions()).basicRemove(otherEnd, msgs);
			case TopLevelElementsPackage.REACTIONS_SEGMENT__ROUTINES:
				return ((InternalEList<?>)getRoutines()).basicRemove(otherEnd, msgs);
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
			case TopLevelElementsPackage.REACTIONS_SEGMENT__NAME:
				return getName();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__FROM_METAMODELS:
				return getFromMetamodels();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__TO_METAMODELS:
				return getToMetamodels();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS_IMPORTS:
				return getReactionsImports();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS:
				return getReactions();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__ROUTINES:
				return getRoutines();
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
			case TopLevelElementsPackage.REACTIONS_SEGMENT__NAME:
				setName((String)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__FROM_METAMODELS:
				getFromMetamodels().clear();
				getFromMetamodels().addAll((Collection<? extends MetamodelImport>)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__TO_METAMODELS:
				getToMetamodels().clear();
				getToMetamodels().addAll((Collection<? extends MetamodelImport>)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS_IMPORTS:
				getReactionsImports().clear();
				getReactionsImports().addAll((Collection<? extends ReactionsImport>)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS:
				getReactions().clear();
				getReactions().addAll((Collection<? extends Reaction>)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__ROUTINES:
				getRoutines().clear();
				getRoutines().addAll((Collection<? extends Routine>)newValue);
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
			case TopLevelElementsPackage.REACTIONS_SEGMENT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__FROM_METAMODELS:
				getFromMetamodels().clear();
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__TO_METAMODELS:
				getToMetamodels().clear();
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS_IMPORTS:
				getReactionsImports().clear();
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS:
				getReactions().clear();
				return;
			case TopLevelElementsPackage.REACTIONS_SEGMENT__ROUTINES:
				getRoutines().clear();
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
			case TopLevelElementsPackage.REACTIONS_SEGMENT__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case TopLevelElementsPackage.REACTIONS_SEGMENT__FROM_METAMODELS:
				return fromMetamodels != null && !fromMetamodels.isEmpty();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__TO_METAMODELS:
				return toMetamodels != null && !toMetamodels.isEmpty();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS_IMPORTS:
				return reactionsImports != null && !reactionsImports.isEmpty();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__REACTIONS:
				return reactions != null && !reactions.isEmpty();
			case TopLevelElementsPackage.REACTIONS_SEGMENT__ROUTINES:
				return routines != null && !routines.isEmpty();
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
		result.append(')');
		return result.toString();
	}

} //ReactionsSegmentImpl
