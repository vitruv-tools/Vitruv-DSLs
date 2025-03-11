/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reaction</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getDocumentation <em>Documentation</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getOverriddenReactionsSegment <em>Overridden Reactions Segment</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getName <em>Name</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getTrigger <em>Trigger</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getCallRoutine <em>Call Routine</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getReactionsSegment <em>Reactions Segment</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReaction()
 * @model
 * @generated
 */
public interface Reaction extends EObject
{
	/**
	 * Returns the value of the '<em><b>Documentation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Documentation</em>' attribute.
	 * @see #setDocumentation(String)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReaction_Documentation()
	 * @model
	 * @generated
	 */
	String getDocumentation();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getDocumentation <em>Documentation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Documentation</em>' attribute.
	 * @see #getDocumentation()
	 * @generated
	 */
	void setDocumentation(String value);

	/**
	 * Returns the value of the '<em><b>Overridden Reactions Segment</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Overridden Reactions Segment</em>' reference.
	 * @see #setOverriddenReactionsSegment(ReactionsSegment)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReaction_OverriddenReactionsSegment()
	 * @model
	 * @generated
	 */
	ReactionsSegment getOverriddenReactionsSegment();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getOverriddenReactionsSegment <em>Overridden Reactions Segment</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Overridden Reactions Segment</em>' reference.
	 * @see #getOverriddenReactionsSegment()
	 * @generated
	 */
	void setOverriddenReactionsSegment(ReactionsSegment value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReaction_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Trigger</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Trigger</em>' containment reference.
	 * @see #setTrigger(Trigger)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReaction_Trigger()
	 * @model containment="true"
	 * @generated
	 */
	Trigger getTrigger();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getTrigger <em>Trigger</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Trigger</em>' containment reference.
	 * @see #getTrigger()
	 * @generated
	 */
	void setTrigger(Trigger value);

	/**
	 * Returns the value of the '<em><b>Call Routine</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Call Routine</em>' containment reference.
	 * @see #setCallRoutine(RoutineCall)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReaction_CallRoutine()
	 * @model containment="true"
	 * @generated
	 */
	RoutineCall getCallRoutine();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getCallRoutine <em>Call Routine</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Call Routine</em>' containment reference.
	 * @see #getCallRoutine()
	 * @generated
	 */
	void setCallRoutine(RoutineCall value);

	/**
	 * Returns the value of the '<em><b>Reactions Segment</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment#getReactions <em>Reactions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reactions Segment</em>' container reference.
	 * @see #setReactionsSegment(ReactionsSegment)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReaction_ReactionsSegment()
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment#getReactions
	 * @model opposite="reactions" required="true" transient="false"
	 * @generated
	 */
	ReactionsSegment getReactionsSegment();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Reaction#getReactionsSegment <em>Reactions Segment</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reactions Segment</em>' container reference.
	 * @see #getReactionsSegment()
	 * @generated
	 */
	void setReactionsSegment(ReactionsSegment value);

} // Reaction
