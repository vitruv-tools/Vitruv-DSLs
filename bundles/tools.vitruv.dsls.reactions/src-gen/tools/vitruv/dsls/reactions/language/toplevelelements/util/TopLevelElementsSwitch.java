/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import tools.vitruv.dsls.reactions.language.toplevelelements.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage
 * @generated
 */
public class TopLevelElementsSwitch<T> extends Switch<T>
{
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static TopLevelElementsPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TopLevelElementsSwitch()
	{
		if (modelPackage == null)
		{
			modelPackage = TopLevelElementsPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage)
	{
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject)
	{
		switch (classifierID)
		{
			case TopLevelElementsPackage.REACTIONS_FILE:
			{
				ReactionsFile reactionsFile = (ReactionsFile)theEObject;
				T result = caseReactionsFile(reactionsFile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.REACTIONS_SEGMENT:
			{
				ReactionsSegment reactionsSegment = (ReactionsSegment)theEObject;
				T result = caseReactionsSegment(reactionsSegment);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.REACTIONS_IMPORT:
			{
				ReactionsImport reactionsImport = (ReactionsImport)theEObject;
				T result = caseReactionsImport(reactionsImport);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.REACTION:
			{
				Reaction reaction = (Reaction)theEObject;
				T result = caseReaction(reaction);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.TRIGGER:
			{
				Trigger trigger = (Trigger)theEObject;
				T result = caseTrigger(trigger);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.ROUTINE:
			{
				Routine routine = (Routine)theEObject;
				T result = caseRoutine(routine);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH:
			{
				RoutineOverrideImportPath routineOverrideImportPath = (RoutineOverrideImportPath)theEObject;
				T result = caseRoutineOverrideImportPath(routineOverrideImportPath);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.ROUTINE_INPUT:
			{
				RoutineInput routineInput = (RoutineInput)theEObject;
				T result = caseRoutineInput(routineInput);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.MATCH_BLOCK:
			{
				MatchBlock matchBlock = (MatchBlock)theEObject;
				T result = caseMatchBlock(matchBlock);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.MATCH_STATEMENT:
			{
				MatchStatement matchStatement = (MatchStatement)theEObject;
				T result = caseMatchStatement(matchStatement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.CREATE_BLOCK:
			{
				CreateBlock createBlock = (CreateBlock)theEObject;
				T result = caseCreateBlock(createBlock);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.NAMED_JAVA_ELEMENT_REFERENCE:
			{
				NamedJavaElementReference namedJavaElementReference = (NamedJavaElementReference)theEObject;
				T result = caseNamedJavaElementReference(namedJavaElementReference);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.ROUTINE_CALL:
			{
				RoutineCall routineCall = (RoutineCall)theEObject;
				T result = caseRoutineCall(routineCall);
				if (result == null) result = caseCodeExecutionBlock(routineCall);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.UPDATE_BLOCK:
			{
				UpdateBlock updateBlock = (UpdateBlock)theEObject;
				T result = caseUpdateBlock(updateBlock);
				if (result == null) result = caseCodeExecutionBlock(updateBlock);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case TopLevelElementsPackage.CODE_EXECUTION_BLOCK:
			{
				CodeExecutionBlock codeExecutionBlock = (CodeExecutionBlock)theEObject;
				T result = caseCodeExecutionBlock(codeExecutionBlock);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Reactions File</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Reactions File</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReactionsFile(ReactionsFile object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Reactions Segment</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Reactions Segment</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReactionsSegment(ReactionsSegment object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Reactions Import</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Reactions Import</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReactionsImport(ReactionsImport object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Reaction</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Reaction</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReaction(Reaction object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Trigger</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trigger</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTrigger(Trigger object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Routine</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Routine</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRoutine(Routine object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Routine Override Import Path</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Routine Override Import Path</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRoutineOverrideImportPath(RoutineOverrideImportPath object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Routine Input</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Routine Input</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRoutineInput(RoutineInput object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Match Block</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Match Block</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMatchBlock(MatchBlock object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Match Statement</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Match Statement</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMatchStatement(MatchStatement object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Create Block</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Create Block</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCreateBlock(CreateBlock object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Named Java Element Reference</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Named Java Element Reference</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNamedJavaElementReference(NamedJavaElementReference object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Routine Call</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Routine Call</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRoutineCall(RoutineCall object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Update Block</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Update Block</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseUpdateBlock(UpdateBlock object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Code Execution Block</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Code Execution Block</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCodeExecutionBlock(CodeExecutionBlock object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object)
	{
		return null;
	}

} //TopLevelElementsSwitch
