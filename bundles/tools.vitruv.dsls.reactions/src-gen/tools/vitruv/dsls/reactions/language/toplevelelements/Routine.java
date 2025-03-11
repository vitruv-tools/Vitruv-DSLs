/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Routine</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getDocumentation <em>Documentation</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getOverrideImportPath <em>Override Import Path</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getName <em>Name</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getInput <em>Input</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getMatchBlock <em>Match Block</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getCreateBlock <em>Create Block</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getReactionsSegment <em>Reactions Segment</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getUpdateBlock <em>Update Block</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine()
 * @model
 * @generated
 */
public interface Routine extends EObject
{
	/**
	 * Returns the value of the '<em><b>Documentation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Documentation</em>' attribute.
	 * @see #setDocumentation(String)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_Documentation()
	 * @model
	 * @generated
	 */
	String getDocumentation();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getDocumentation <em>Documentation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Documentation</em>' attribute.
	 * @see #getDocumentation()
	 * @generated
	 */
	void setDocumentation(String value);

	/**
	 * Returns the value of the '<em><b>Override Import Path</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Override Import Path</em>' containment reference.
	 * @see #setOverrideImportPath(RoutineOverrideImportPath)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_OverrideImportPath()
	 * @model containment="true"
	 * @generated
	 */
	RoutineOverrideImportPath getOverrideImportPath();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getOverrideImportPath <em>Override Import Path</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Override Import Path</em>' containment reference.
	 * @see #getOverrideImportPath()
	 * @generated
	 */
	void setOverrideImportPath(RoutineOverrideImportPath value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Input</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Input</em>' containment reference.
	 * @see #setInput(RoutineInput)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_Input()
	 * @model containment="true"
	 * @generated
	 */
	RoutineInput getInput();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getInput <em>Input</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input</em>' containment reference.
	 * @see #getInput()
	 * @generated
	 */
	void setInput(RoutineInput value);

	/**
	 * Returns the value of the '<em><b>Match Block</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Match Block</em>' containment reference.
	 * @see #setMatchBlock(MatchBlock)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_MatchBlock()
	 * @model containment="true"
	 * @generated
	 */
	MatchBlock getMatchBlock();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getMatchBlock <em>Match Block</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Match Block</em>' containment reference.
	 * @see #getMatchBlock()
	 * @generated
	 */
	void setMatchBlock(MatchBlock value);

	/**
	 * Returns the value of the '<em><b>Create Block</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Create Block</em>' containment reference.
	 * @see #setCreateBlock(CreateBlock)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_CreateBlock()
	 * @model containment="true"
	 * @generated
	 */
	CreateBlock getCreateBlock();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getCreateBlock <em>Create Block</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Create Block</em>' containment reference.
	 * @see #getCreateBlock()
	 * @generated
	 */
	void setCreateBlock(CreateBlock value);

	/**
	 * Returns the value of the '<em><b>Reactions Segment</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment#getRoutines <em>Routines</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reactions Segment</em>' container reference.
	 * @see #setReactionsSegment(ReactionsSegment)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_ReactionsSegment()
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment#getRoutines
	 * @model opposite="routines" required="true" transient="false"
	 * @generated
	 */
	ReactionsSegment getReactionsSegment();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getReactionsSegment <em>Reactions Segment</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reactions Segment</em>' container reference.
	 * @see #getReactionsSegment()
	 * @generated
	 */
	void setReactionsSegment(ReactionsSegment value);

	/**
	 * Returns the value of the '<em><b>Update Block</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Update Block</em>' containment reference.
	 * @see #setUpdateBlock(UpdateBlock)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutine_UpdateBlock()
	 * @model containment="true"
	 * @generated
	 */
	UpdateBlock getUpdateBlock();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Routine#getUpdateBlock <em>Update Block</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Update Block</em>' containment reference.
	 * @see #getUpdateBlock()
	 * @generated
	 */
	void setUpdateBlock(UpdateBlock value);

} // Routine
