/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reactions Import</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsImportImpl#isRoutinesOnly <em>Routines Only</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsImportImpl#getImportedReactionsSegment <em>Imported Reactions Segment</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsImportImpl#isUseQualifiedNames <em>Use Qualified Names</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReactionsImportImpl extends MinimalEObjectImpl.Container implements ReactionsImport
{
	/**
	 * The default value of the '{@link #isRoutinesOnly() <em>Routines Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isRoutinesOnly()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ROUTINES_ONLY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isRoutinesOnly() <em>Routines Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isRoutinesOnly()
	 * @generated
	 * @ordered
	 */
	protected boolean routinesOnly = ROUTINES_ONLY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getImportedReactionsSegment() <em>Imported Reactions Segment</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImportedReactionsSegment()
	 * @generated
	 * @ordered
	 */
	protected ReactionsSegment importedReactionsSegment;

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
	protected ReactionsImportImpl()
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
		return TopLevelElementsPackage.Literals.REACTIONS_IMPORT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isRoutinesOnly()
	{
		return routinesOnly;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRoutinesOnly(boolean newRoutinesOnly)
	{
		boolean oldRoutinesOnly = routinesOnly;
		routinesOnly = newRoutinesOnly;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTIONS_IMPORT__ROUTINES_ONLY, oldRoutinesOnly, routinesOnly));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReactionsSegment getImportedReactionsSegment()
	{
		if (importedReactionsSegment != null && importedReactionsSegment.eIsProxy())
		{
			InternalEObject oldImportedReactionsSegment = (InternalEObject)importedReactionsSegment;
			importedReactionsSegment = (ReactionsSegment)eResolveProxy(oldImportedReactionsSegment);
			if (importedReactionsSegment != oldImportedReactionsSegment)
			{
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, TopLevelElementsPackage.REACTIONS_IMPORT__IMPORTED_REACTIONS_SEGMENT, oldImportedReactionsSegment, importedReactionsSegment));
			}
		}
		return importedReactionsSegment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReactionsSegment basicGetImportedReactionsSegment()
	{
		return importedReactionsSegment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImportedReactionsSegment(ReactionsSegment newImportedReactionsSegment)
	{
		ReactionsSegment oldImportedReactionsSegment = importedReactionsSegment;
		importedReactionsSegment = newImportedReactionsSegment;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTIONS_IMPORT__IMPORTED_REACTIONS_SEGMENT, oldImportedReactionsSegment, importedReactionsSegment));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isUseQualifiedNames()
	{
		return useQualifiedNames;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUseQualifiedNames(boolean newUseQualifiedNames)
	{
		boolean oldUseQualifiedNames = useQualifiedNames;
		useQualifiedNames = newUseQualifiedNames;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTIONS_IMPORT__USE_QUALIFIED_NAMES, oldUseQualifiedNames, useQualifiedNames));
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
			case TopLevelElementsPackage.REACTIONS_IMPORT__ROUTINES_ONLY:
				return isRoutinesOnly();
			case TopLevelElementsPackage.REACTIONS_IMPORT__IMPORTED_REACTIONS_SEGMENT:
				if (resolve) return getImportedReactionsSegment();
				return basicGetImportedReactionsSegment();
			case TopLevelElementsPackage.REACTIONS_IMPORT__USE_QUALIFIED_NAMES:
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
			case TopLevelElementsPackage.REACTIONS_IMPORT__ROUTINES_ONLY:
				setRoutinesOnly((Boolean)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_IMPORT__IMPORTED_REACTIONS_SEGMENT:
				setImportedReactionsSegment((ReactionsSegment)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_IMPORT__USE_QUALIFIED_NAMES:
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
			case TopLevelElementsPackage.REACTIONS_IMPORT__ROUTINES_ONLY:
				setRoutinesOnly(ROUTINES_ONLY_EDEFAULT);
				return;
			case TopLevelElementsPackage.REACTIONS_IMPORT__IMPORTED_REACTIONS_SEGMENT:
				setImportedReactionsSegment((ReactionsSegment)null);
				return;
			case TopLevelElementsPackage.REACTIONS_IMPORT__USE_QUALIFIED_NAMES:
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
			case TopLevelElementsPackage.REACTIONS_IMPORT__ROUTINES_ONLY:
				return routinesOnly != ROUTINES_ONLY_EDEFAULT;
			case TopLevelElementsPackage.REACTIONS_IMPORT__IMPORTED_REACTIONS_SEGMENT:
				return importedReactionsSegment != null;
			case TopLevelElementsPackage.REACTIONS_IMPORT__USE_QUALIFIED_NAMES:
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
		result.append(" (routinesOnly: ");
		result.append(routinesOnly);
		result.append(", useQualifiedNames: ");
		result.append(useQualifiedNames);
		result.append(')');
		return result.toString();
	}

} //ReactionsImportImpl
