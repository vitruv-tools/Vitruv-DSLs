/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import tools.vitruv.dsls.reactions.language.toplevelelements.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TopLevelElementsFactoryImpl extends EFactoryImpl implements TopLevelElementsFactory
{
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static TopLevelElementsFactory init()
	{
		try
		{
			TopLevelElementsFactory theTopLevelElementsFactory = (TopLevelElementsFactory)EPackage.Registry.INSTANCE.getEFactory(TopLevelElementsPackage.eNS_URI);
			if (theTopLevelElementsFactory != null)
			{
				return theTopLevelElementsFactory;
			}
		}
		catch (Exception exception)
		{
			EcorePlugin.INSTANCE.log(exception);
		}
		return new TopLevelElementsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TopLevelElementsFactoryImpl()
	{
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass)
	{
		switch (eClass.getClassifierID())
		{
			case TopLevelElementsPackage.REACTIONS_FILE: return createReactionsFile();
			case TopLevelElementsPackage.REACTIONS_SEGMENT: return createReactionsSegment();
			case TopLevelElementsPackage.REACTIONS_IMPORT: return createReactionsImport();
			case TopLevelElementsPackage.REACTION: return createReaction();
			case TopLevelElementsPackage.TRIGGER: return createTrigger();
			case TopLevelElementsPackage.ROUTINE: return createRoutine();
			case TopLevelElementsPackage.ROUTINE_OVERRIDE_IMPORT_PATH: return createRoutineOverrideImportPath();
			case TopLevelElementsPackage.ROUTINE_INPUT: return createRoutineInput();
			case TopLevelElementsPackage.MATCH_BLOCK: return createMatchBlock();
			case TopLevelElementsPackage.MATCH_STATEMENT: return createMatchStatement();
			case TopLevelElementsPackage.CREATE_BLOCK: return createCreateBlock();
			case TopLevelElementsPackage.NAMED_JAVA_ELEMENT_REFERENCE: return createNamedJavaElementReference();
			case TopLevelElementsPackage.ROUTINE_CALL: return createRoutineCall();
			case TopLevelElementsPackage.UPDATE_BLOCK: return createUpdateBlock();
			case TopLevelElementsPackage.CODE_EXECUTION_BLOCK: return createCodeExecutionBlock();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReactionsFile createReactionsFile()
	{
		ReactionsFileImpl reactionsFile = new ReactionsFileImpl();
		return reactionsFile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReactionsSegment createReactionsSegment()
	{
		ReactionsSegmentImpl reactionsSegment = new ReactionsSegmentImpl();
		return reactionsSegment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReactionsImport createReactionsImport()
	{
		ReactionsImportImpl reactionsImport = new ReactionsImportImpl();
		return reactionsImport;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Reaction createReaction()
	{
		ReactionImpl reaction = new ReactionImpl();
		return reaction;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Trigger createTrigger()
	{
		TriggerImpl trigger = new TriggerImpl();
		return trigger;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Routine createRoutine()
	{
		RoutineImpl routine = new RoutineImpl();
		return routine;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RoutineOverrideImportPath createRoutineOverrideImportPath()
	{
		RoutineOverrideImportPathImpl routineOverrideImportPath = new RoutineOverrideImportPathImpl();
		return routineOverrideImportPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RoutineInput createRoutineInput()
	{
		RoutineInputImpl routineInput = new RoutineInputImpl();
		return routineInput;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MatchBlock createMatchBlock()
	{
		MatchBlockImpl matchBlock = new MatchBlockImpl();
		return matchBlock;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MatchStatement createMatchStatement()
	{
		MatchStatementImpl matchStatement = new MatchStatementImpl();
		return matchStatement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CreateBlock createCreateBlock()
	{
		CreateBlockImpl createBlock = new CreateBlockImpl();
		return createBlock;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NamedJavaElementReference createNamedJavaElementReference()
	{
		NamedJavaElementReferenceImpl namedJavaElementReference = new NamedJavaElementReferenceImpl();
		return namedJavaElementReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RoutineCall createRoutineCall()
	{
		RoutineCallImpl routineCall = new RoutineCallImpl();
		return routineCall;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public UpdateBlock createUpdateBlock()
	{
		UpdateBlockImpl updateBlock = new UpdateBlockImpl();
		return updateBlock;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CodeExecutionBlock createCodeExecutionBlock()
	{
		CodeExecutionBlockImpl codeExecutionBlock = new CodeExecutionBlockImpl();
		return codeExecutionBlock;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TopLevelElementsPackage getTopLevelElementsPackage()
	{
		return (TopLevelElementsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static TopLevelElementsPackage getPackage()
	{
		return TopLevelElementsPackage.eINSTANCE;
	}

} //TopLevelElementsFactoryImpl
