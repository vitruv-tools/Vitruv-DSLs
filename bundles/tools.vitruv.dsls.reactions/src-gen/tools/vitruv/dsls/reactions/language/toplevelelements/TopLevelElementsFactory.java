/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage
 * @generated
 */
public interface TopLevelElementsFactory extends EFactory
{
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	TopLevelElementsFactory eINSTANCE = tools.vitruv.dsls.reactions.language.toplevelelements.impl.TopLevelElementsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Reactions File</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reactions File</em>'.
	 * @generated
	 */
	ReactionsFile createReactionsFile();

	/**
	 * Returns a new object of class '<em>Reactions Segment</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reactions Segment</em>'.
	 * @generated
	 */
	ReactionsSegment createReactionsSegment();

	/**
	 * Returns a new object of class '<em>Reactions Import</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reactions Import</em>'.
	 * @generated
	 */
	ReactionsImport createReactionsImport();

	/**
	 * Returns a new object of class '<em>Reaction</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reaction</em>'.
	 * @generated
	 */
	Reaction createReaction();

	/**
	 * Returns a new object of class '<em>Trigger</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Trigger</em>'.
	 * @generated
	 */
	Trigger createTrigger();

	/**
	 * Returns a new object of class '<em>Routine</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Routine</em>'.
	 * @generated
	 */
	Routine createRoutine();

	/**
	 * Returns a new object of class '<em>Routine Override Import Path</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Routine Override Import Path</em>'.
	 * @generated
	 */
	RoutineOverrideImportPath createRoutineOverrideImportPath();

	/**
	 * Returns a new object of class '<em>Routine Input</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Routine Input</em>'.
	 * @generated
	 */
	RoutineInput createRoutineInput();

	/**
	 * Returns a new object of class '<em>Match Block</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Match Block</em>'.
	 * @generated
	 */
	MatchBlock createMatchBlock();

	/**
	 * Returns a new object of class '<em>Match Statement</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Match Statement</em>'.
	 * @generated
	 */
	MatchStatement createMatchStatement();

	/**
	 * Returns a new object of class '<em>Create Block</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Create Block</em>'.
	 * @generated
	 */
	CreateBlock createCreateBlock();

	/**
	 * Returns a new object of class '<em>Named Java Element Reference</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Named Java Element Reference</em>'.
	 * @generated
	 */
	NamedJavaElementReference createNamedJavaElementReference();

	/**
	 * Returns a new object of class '<em>Routine Call</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Routine Call</em>'.
	 * @generated
	 */
	RoutineCall createRoutineCall();

	/**
	 * Returns a new object of class '<em>Update Block</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Update Block</em>'.
	 * @generated
	 */
	UpdateBlock createUpdateBlock();

	/**
	 * Returns a new object of class '<em>Code Execution Block</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Code Execution Block</em>'.
	 * @generated
	 */
	CodeExecutionBlock createCodeExecutionBlock();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	TopLevelElementsPackage getTopLevelElementsPackage();

} //TopLevelElementsFactory
