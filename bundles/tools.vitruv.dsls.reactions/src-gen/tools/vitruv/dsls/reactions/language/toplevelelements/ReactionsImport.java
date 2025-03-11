/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reactions Import</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport#isRoutinesOnly <em>Routines Only</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport#getImportedReactionsSegment <em>Imported Reactions Segment</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport#isUseQualifiedNames <em>Use Qualified Names</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsImport()
 * @model
 * @generated
 */
public interface ReactionsImport extends EObject
{
	/**
	 * Returns the value of the '<em><b>Routines Only</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Routines Only</em>' attribute.
	 * @see #setRoutinesOnly(boolean)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsImport_RoutinesOnly()
	 * @model
	 * @generated
	 */
	boolean isRoutinesOnly();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport#isRoutinesOnly <em>Routines Only</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Routines Only</em>' attribute.
	 * @see #isRoutinesOnly()
	 * @generated
	 */
	void setRoutinesOnly(boolean value);

	/**
	 * Returns the value of the '<em><b>Imported Reactions Segment</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Imported Reactions Segment</em>' reference.
	 * @see #setImportedReactionsSegment(ReactionsSegment)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsImport_ImportedReactionsSegment()
	 * @model
	 * @generated
	 */
	ReactionsSegment getImportedReactionsSegment();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport#getImportedReactionsSegment <em>Imported Reactions Segment</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Imported Reactions Segment</em>' reference.
	 * @see #getImportedReactionsSegment()
	 * @generated
	 */
	void setImportedReactionsSegment(ReactionsSegment value);

	/**
	 * Returns the value of the '<em><b>Use Qualified Names</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Use Qualified Names</em>' attribute.
	 * @see #setUseQualifiedNames(boolean)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsImport_UseQualifiedNames()
	 * @model
	 * @generated
	 */
	boolean isUseQualifiedNames();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport#isUseQualifiedNames <em>Use Qualified Names</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Use Qualified Names</em>' attribute.
	 * @see #isUseQualifiedNames()
	 * @generated
	 */
	void setUseQualifiedNames(boolean value);

} // ReactionsImport
