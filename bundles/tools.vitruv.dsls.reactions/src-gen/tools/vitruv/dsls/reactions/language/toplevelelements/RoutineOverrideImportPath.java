/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Routine Override Import Path</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath#getReactionsSegment <em>Reactions Segment</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath#getParent <em>Parent</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutineOverrideImportPath()
 * @model
 * @generated
 */
public interface RoutineOverrideImportPath extends EObject
{
	/**
	 * Returns the value of the '<em><b>Reactions Segment</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reactions Segment</em>' reference.
	 * @see #setReactionsSegment(ReactionsSegment)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutineOverrideImportPath_ReactionsSegment()
	 * @model
	 * @generated
	 */
	ReactionsSegment getReactionsSegment();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath#getReactionsSegment <em>Reactions Segment</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reactions Segment</em>' reference.
	 * @see #getReactionsSegment()
	 * @generated
	 */
	void setReactionsSegment(ReactionsSegment value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' containment reference.
	 * @see #setParent(RoutineOverrideImportPath)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutineOverrideImportPath_Parent()
	 * @model containment="true"
	 * @generated
	 */
	RoutineOverrideImportPath getParent();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath#getParent <em>Parent</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' containment reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(RoutineOverrideImportPath value);

} // RoutineOverrideImportPath
