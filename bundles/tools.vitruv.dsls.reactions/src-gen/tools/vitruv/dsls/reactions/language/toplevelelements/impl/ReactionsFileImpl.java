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
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.xtext.xtype.XImportSection;

import tools.vitruv.dsls.common.elements.MetamodelImport;

import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reactions File</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsFileImpl#getNamespaceImports <em>Namespace Imports</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsFileImpl#getMetamodelImports <em>Metamodel Imports</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.impl.ReactionsFileImpl#getReactionsSegments <em>Reactions Segments</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReactionsFileImpl extends MinimalEObjectImpl.Container implements ReactionsFile
{
	/**
	 * The cached value of the '{@link #getNamespaceImports() <em>Namespace Imports</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespaceImports()
	 * @generated
	 * @ordered
	 */
	protected XImportSection namespaceImports;

	/**
	 * The cached value of the '{@link #getMetamodelImports() <em>Metamodel Imports</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMetamodelImports()
	 * @generated
	 * @ordered
	 */
	protected EList<MetamodelImport> metamodelImports;

	/**
	 * The cached value of the '{@link #getReactionsSegments() <em>Reactions Segments</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReactionsSegments()
	 * @generated
	 * @ordered
	 */
	protected EList<ReactionsSegment> reactionsSegments;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReactionsFileImpl()
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
		return TopLevelElementsPackage.Literals.REACTIONS_FILE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public XImportSection getNamespaceImports()
	{
		return namespaceImports;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNamespaceImports(XImportSection newNamespaceImports, NotificationChain msgs)
	{
		XImportSection oldNamespaceImports = namespaceImports;
		namespaceImports = newNamespaceImports;
		if (eNotificationRequired())
		{
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS, oldNamespaceImports, newNamespaceImports);
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
	public void setNamespaceImports(XImportSection newNamespaceImports)
	{
		if (newNamespaceImports != namespaceImports)
		{
			NotificationChain msgs = null;
			if (namespaceImports != null)
				msgs = ((InternalEObject)namespaceImports).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS, null, msgs);
			if (newNamespaceImports != null)
				msgs = ((InternalEObject)newNamespaceImports).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS, null, msgs);
			msgs = basicSetNamespaceImports(newNamespaceImports, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS, newNamespaceImports, newNamespaceImports));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<MetamodelImport> getMetamodelImports()
	{
		if (metamodelImports == null)
		{
			metamodelImports = new EObjectContainmentEList<MetamodelImport>(MetamodelImport.class, this, TopLevelElementsPackage.REACTIONS_FILE__METAMODEL_IMPORTS);
		}
		return metamodelImports;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ReactionsSegment> getReactionsSegments()
	{
		if (reactionsSegments == null)
		{
			reactionsSegments = new EObjectContainmentEList<ReactionsSegment>(ReactionsSegment.class, this, TopLevelElementsPackage.REACTIONS_FILE__REACTIONS_SEGMENTS);
		}
		return reactionsSegments;
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
			case TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS:
				return basicSetNamespaceImports(null, msgs);
			case TopLevelElementsPackage.REACTIONS_FILE__METAMODEL_IMPORTS:
				return ((InternalEList<?>)getMetamodelImports()).basicRemove(otherEnd, msgs);
			case TopLevelElementsPackage.REACTIONS_FILE__REACTIONS_SEGMENTS:
				return ((InternalEList<?>)getReactionsSegments()).basicRemove(otherEnd, msgs);
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
			case TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS:
				return getNamespaceImports();
			case TopLevelElementsPackage.REACTIONS_FILE__METAMODEL_IMPORTS:
				return getMetamodelImports();
			case TopLevelElementsPackage.REACTIONS_FILE__REACTIONS_SEGMENTS:
				return getReactionsSegments();
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
			case TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS:
				setNamespaceImports((XImportSection)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_FILE__METAMODEL_IMPORTS:
				getMetamodelImports().clear();
				getMetamodelImports().addAll((Collection<? extends MetamodelImport>)newValue);
				return;
			case TopLevelElementsPackage.REACTIONS_FILE__REACTIONS_SEGMENTS:
				getReactionsSegments().clear();
				getReactionsSegments().addAll((Collection<? extends ReactionsSegment>)newValue);
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
			case TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS:
				setNamespaceImports((XImportSection)null);
				return;
			case TopLevelElementsPackage.REACTIONS_FILE__METAMODEL_IMPORTS:
				getMetamodelImports().clear();
				return;
			case TopLevelElementsPackage.REACTIONS_FILE__REACTIONS_SEGMENTS:
				getReactionsSegments().clear();
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
			case TopLevelElementsPackage.REACTIONS_FILE__NAMESPACE_IMPORTS:
				return namespaceImports != null;
			case TopLevelElementsPackage.REACTIONS_FILE__METAMODEL_IMPORTS:
				return metamodelImports != null && !metamodelImports.isEmpty();
			case TopLevelElementsPackage.REACTIONS_FILE__REACTIONS_SEGMENTS:
				return reactionsSegments != null && !reactionsSegments.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //ReactionsFileImpl
