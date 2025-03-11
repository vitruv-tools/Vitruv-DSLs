/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.xtext.common.types.TypesPackage;

import org.eclipse.xtext.xbase.XbasePackage;

import org.eclipse.xtext.xtype.XtypePackage;

import tools.vitruv.dsls.common.elements.ElementsPackage;

import tools.vitruv.dsls.reactions.language.toplevelelements.CodeExecutionBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchStatement;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsImport;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineInput;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;
import tools.vitruv.dsls.reactions.language.toplevelelements.UpdateBlock;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class TopLevelElementsPackageImpl extends EPackageImpl implements TopLevelElementsPackage
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass reactionsFileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass reactionsSegmentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass reactionsImportEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass reactionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass triggerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass routineEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass routineOverrideImportPathEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass routineInputEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass matchBlockEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass matchStatementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass createBlockEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass namedJavaElementReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass routineCallEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass updateBlockEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass codeExecutionBlockEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private TopLevelElementsPackageImpl()
	{
		super(eNS_URI, TopLevelElementsFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link TopLevelElementsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static TopLevelElementsPackage init()
	{
		if (isInited) return (TopLevelElementsPackage)EPackage.Registry.INSTANCE.getEPackage(TopLevelElementsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredTopLevelElementsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		TopLevelElementsPackageImpl theTopLevelElementsPackage = registeredTopLevelElementsPackage instanceof TopLevelElementsPackageImpl ? (TopLevelElementsPackageImpl)registeredTopLevelElementsPackage : new TopLevelElementsPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ElementsPackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();
		TypesPackage.eINSTANCE.eClass();
		XtypePackage.eINSTANCE.eClass();
		XbasePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theTopLevelElementsPackage.createPackageContents();

		// Initialize created meta-data
		theTopLevelElementsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theTopLevelElementsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(TopLevelElementsPackage.eNS_URI, theTopLevelElementsPackage);
		return theTopLevelElementsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReactionsFile()
	{
		return reactionsFileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsFile_NamespaceImports()
	{
		return (EReference)reactionsFileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsFile_MetamodelImports()
	{
		return (EReference)reactionsFileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsFile_ReactionsSegments()
	{
		return (EReference)reactionsFileEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReactionsSegment()
	{
		return reactionsSegmentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReactionsSegment_Name()
	{
		return (EAttribute)reactionsSegmentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsSegment_FromMetamodels()
	{
		return (EReference)reactionsSegmentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsSegment_ToMetamodels()
	{
		return (EReference)reactionsSegmentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsSegment_ReactionsImports()
	{
		return (EReference)reactionsSegmentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsSegment_Reactions()
	{
		return (EReference)reactionsSegmentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsSegment_Routines()
	{
		return (EReference)reactionsSegmentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReactionsImport()
	{
		return reactionsImportEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReactionsImport_RoutinesOnly()
	{
		return (EAttribute)reactionsImportEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReactionsImport_ImportedReactionsSegment()
	{
		return (EReference)reactionsImportEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReactionsImport_UseQualifiedNames()
	{
		return (EAttribute)reactionsImportEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReaction()
	{
		return reactionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReaction_Documentation()
	{
		return (EAttribute)reactionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReaction_OverriddenReactionsSegment()
	{
		return (EReference)reactionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReaction_Name()
	{
		return (EAttribute)reactionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReaction_Trigger()
	{
		return (EReference)reactionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReaction_CallRoutine()
	{
		return (EReference)reactionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getReaction_ReactionsSegment()
	{
		return (EReference)reactionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTrigger()
	{
		return triggerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTrigger_Precondition()
	{
		return (EReference)triggerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRoutine()
	{
		return routineEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRoutine_Documentation()
	{
		return (EAttribute)routineEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutine_OverrideImportPath()
	{
		return (EReference)routineEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRoutine_Name()
	{
		return (EAttribute)routineEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutine_Input()
	{
		return (EReference)routineEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutine_MatchBlock()
	{
		return (EReference)routineEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutine_CreateBlock()
	{
		return (EReference)routineEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutine_ReactionsSegment()
	{
		return (EReference)routineEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutine_UpdateBlock()
	{
		return (EReference)routineEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRoutineOverrideImportPath()
	{
		return routineOverrideImportPathEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutineOverrideImportPath_ReactionsSegment()
	{
		return (EReference)routineOverrideImportPathEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutineOverrideImportPath_Parent()
	{
		return (EReference)routineOverrideImportPathEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRoutineInput()
	{
		return routineInputEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutineInput_ModelInputElements()
	{
		return (EReference)routineInputEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoutineInput_JavaInputElements()
	{
		return (EReference)routineInputEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMatchBlock()
	{
		return matchBlockEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMatchBlock_MatchStatements()
	{
		return (EReference)matchBlockEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMatchStatement()
	{
		return matchStatementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCreateBlock()
	{
		return createBlockEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCreateBlock_CreateStatements()
	{
		return (EReference)createBlockEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getNamedJavaElementReference()
	{
		return namedJavaElementReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getNamedJavaElementReference_Name()
	{
		return (EAttribute)namedJavaElementReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getNamedJavaElementReference_Type()
	{
		return (EReference)namedJavaElementReferenceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRoutineCall()
	{
		return routineCallEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getUpdateBlock()
	{
		return updateBlockEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCodeExecutionBlock()
	{
		return codeExecutionBlockEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCodeExecutionBlock_Code()
	{
		return (EReference)codeExecutionBlockEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TopLevelElementsFactory getTopLevelElementsFactory()
	{
		return (TopLevelElementsFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents()
	{
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		reactionsFileEClass = createEClass(REACTIONS_FILE);
		createEReference(reactionsFileEClass, REACTIONS_FILE__NAMESPACE_IMPORTS);
		createEReference(reactionsFileEClass, REACTIONS_FILE__METAMODEL_IMPORTS);
		createEReference(reactionsFileEClass, REACTIONS_FILE__REACTIONS_SEGMENTS);

		reactionsSegmentEClass = createEClass(REACTIONS_SEGMENT);
		createEAttribute(reactionsSegmentEClass, REACTIONS_SEGMENT__NAME);
		createEReference(reactionsSegmentEClass, REACTIONS_SEGMENT__FROM_METAMODELS);
		createEReference(reactionsSegmentEClass, REACTIONS_SEGMENT__TO_METAMODELS);
		createEReference(reactionsSegmentEClass, REACTIONS_SEGMENT__REACTIONS_IMPORTS);
		createEReference(reactionsSegmentEClass, REACTIONS_SEGMENT__REACTIONS);
		createEReference(reactionsSegmentEClass, REACTIONS_SEGMENT__ROUTINES);

		reactionsImportEClass = createEClass(REACTIONS_IMPORT);
		createEAttribute(reactionsImportEClass, REACTIONS_IMPORT__ROUTINES_ONLY);
		createEReference(reactionsImportEClass, REACTIONS_IMPORT__IMPORTED_REACTIONS_SEGMENT);
		createEAttribute(reactionsImportEClass, REACTIONS_IMPORT__USE_QUALIFIED_NAMES);

		reactionEClass = createEClass(REACTION);
		createEAttribute(reactionEClass, REACTION__DOCUMENTATION);
		createEReference(reactionEClass, REACTION__OVERRIDDEN_REACTIONS_SEGMENT);
		createEAttribute(reactionEClass, REACTION__NAME);
		createEReference(reactionEClass, REACTION__TRIGGER);
		createEReference(reactionEClass, REACTION__CALL_ROUTINE);
		createEReference(reactionEClass, REACTION__REACTIONS_SEGMENT);

		triggerEClass = createEClass(TRIGGER);
		createEReference(triggerEClass, TRIGGER__PRECONDITION);

		routineEClass = createEClass(ROUTINE);
		createEAttribute(routineEClass, ROUTINE__DOCUMENTATION);
		createEReference(routineEClass, ROUTINE__OVERRIDE_IMPORT_PATH);
		createEAttribute(routineEClass, ROUTINE__NAME);
		createEReference(routineEClass, ROUTINE__INPUT);
		createEReference(routineEClass, ROUTINE__MATCH_BLOCK);
		createEReference(routineEClass, ROUTINE__CREATE_BLOCK);
		createEReference(routineEClass, ROUTINE__REACTIONS_SEGMENT);
		createEReference(routineEClass, ROUTINE__UPDATE_BLOCK);

		routineOverrideImportPathEClass = createEClass(ROUTINE_OVERRIDE_IMPORT_PATH);
		createEReference(routineOverrideImportPathEClass, ROUTINE_OVERRIDE_IMPORT_PATH__REACTIONS_SEGMENT);
		createEReference(routineOverrideImportPathEClass, ROUTINE_OVERRIDE_IMPORT_PATH__PARENT);

		routineInputEClass = createEClass(ROUTINE_INPUT);
		createEReference(routineInputEClass, ROUTINE_INPUT__MODEL_INPUT_ELEMENTS);
		createEReference(routineInputEClass, ROUTINE_INPUT__JAVA_INPUT_ELEMENTS);

		matchBlockEClass = createEClass(MATCH_BLOCK);
		createEReference(matchBlockEClass, MATCH_BLOCK__MATCH_STATEMENTS);

		matchStatementEClass = createEClass(MATCH_STATEMENT);

		createBlockEClass = createEClass(CREATE_BLOCK);
		createEReference(createBlockEClass, CREATE_BLOCK__CREATE_STATEMENTS);

		namedJavaElementReferenceEClass = createEClass(NAMED_JAVA_ELEMENT_REFERENCE);
		createEAttribute(namedJavaElementReferenceEClass, NAMED_JAVA_ELEMENT_REFERENCE__NAME);
		createEReference(namedJavaElementReferenceEClass, NAMED_JAVA_ELEMENT_REFERENCE__TYPE);

		routineCallEClass = createEClass(ROUTINE_CALL);

		updateBlockEClass = createEClass(UPDATE_BLOCK);

		codeExecutionBlockEClass = createEClass(CODE_EXECUTION_BLOCK);
		createEReference(codeExecutionBlockEClass, CODE_EXECUTION_BLOCK__CODE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents()
	{
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		XtypePackage theXtypePackage = (XtypePackage)EPackage.Registry.INSTANCE.getEPackage(XtypePackage.eNS_URI);
		ElementsPackage theElementsPackage = (ElementsPackage)EPackage.Registry.INSTANCE.getEPackage(ElementsPackage.eNS_URI);
		XbasePackage theXbasePackage = (XbasePackage)EPackage.Registry.INSTANCE.getEPackage(XbasePackage.eNS_URI);
		TypesPackage theTypesPackage = (TypesPackage)EPackage.Registry.INSTANCE.getEPackage(TypesPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		routineCallEClass.getESuperTypes().add(this.getCodeExecutionBlock());
		updateBlockEClass.getESuperTypes().add(this.getCodeExecutionBlock());

		// Initialize classes, features, and operations; add parameters
		initEClass(reactionsFileEClass, ReactionsFile.class, "ReactionsFile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getReactionsFile_NamespaceImports(), theXtypePackage.getXImportSection(), null, "namespaceImports", null, 0, 1, ReactionsFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsFile_MetamodelImports(), theElementsPackage.getMetamodelImport(), null, "metamodelImports", null, 0, -1, ReactionsFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsFile_ReactionsSegments(), this.getReactionsSegment(), null, "reactionsSegments", null, 0, -1, ReactionsFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(reactionsSegmentEClass, ReactionsSegment.class, "ReactionsSegment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReactionsSegment_Name(), ecorePackage.getEString(), "name", null, 0, 1, ReactionsSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsSegment_FromMetamodels(), theElementsPackage.getMetamodelImport(), null, "fromMetamodels", null, 0, -1, ReactionsSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsSegment_ToMetamodels(), theElementsPackage.getMetamodelImport(), null, "toMetamodels", null, 0, -1, ReactionsSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsSegment_ReactionsImports(), this.getReactionsImport(), null, "reactionsImports", null, 0, -1, ReactionsSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsSegment_Reactions(), this.getReaction(), this.getReaction_ReactionsSegment(), "reactions", null, 0, -1, ReactionsSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsSegment_Routines(), this.getRoutine(), this.getRoutine_ReactionsSegment(), "routines", null, 0, -1, ReactionsSegment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(reactionsImportEClass, ReactionsImport.class, "ReactionsImport", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReactionsImport_RoutinesOnly(), ecorePackage.getEBoolean(), "routinesOnly", null, 0, 1, ReactionsImport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReactionsImport_ImportedReactionsSegment(), this.getReactionsSegment(), null, "importedReactionsSegment", null, 0, 1, ReactionsImport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReactionsImport_UseQualifiedNames(), ecorePackage.getEBoolean(), "useQualifiedNames", null, 0, 1, ReactionsImport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(reactionEClass, Reaction.class, "Reaction", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReaction_Documentation(), ecorePackage.getEString(), "documentation", null, 0, 1, Reaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReaction_OverriddenReactionsSegment(), this.getReactionsSegment(), null, "overriddenReactionsSegment", null, 0, 1, Reaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReaction_Name(), ecorePackage.getEString(), "name", null, 0, 1, Reaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReaction_Trigger(), this.getTrigger(), null, "trigger", null, 0, 1, Reaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReaction_CallRoutine(), this.getRoutineCall(), null, "callRoutine", null, 0, 1, Reaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getReaction_ReactionsSegment(), this.getReactionsSegment(), this.getReactionsSegment_Reactions(), "reactionsSegment", null, 1, 1, Reaction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(triggerEClass, Trigger.class, "Trigger", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getTrigger_Precondition(), theXbasePackage.getXExpression(), null, "precondition", null, 0, 1, Trigger.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(routineEClass, Routine.class, "Routine", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRoutine_Documentation(), ecorePackage.getEString(), "documentation", null, 0, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutine_OverrideImportPath(), this.getRoutineOverrideImportPath(), null, "overrideImportPath", null, 0, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRoutine_Name(), ecorePackage.getEString(), "name", null, 0, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutine_Input(), this.getRoutineInput(), null, "input", null, 0, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutine_MatchBlock(), this.getMatchBlock(), null, "matchBlock", null, 0, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutine_CreateBlock(), this.getCreateBlock(), null, "createBlock", null, 0, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutine_ReactionsSegment(), this.getReactionsSegment(), this.getReactionsSegment_Routines(), "reactionsSegment", null, 1, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutine_UpdateBlock(), this.getUpdateBlock(), null, "updateBlock", null, 0, 1, Routine.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(routineOverrideImportPathEClass, RoutineOverrideImportPath.class, "RoutineOverrideImportPath", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRoutineOverrideImportPath_ReactionsSegment(), this.getReactionsSegment(), null, "reactionsSegment", null, 0, 1, RoutineOverrideImportPath.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutineOverrideImportPath_Parent(), this.getRoutineOverrideImportPath(), null, "parent", null, 0, 1, RoutineOverrideImportPath.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(routineInputEClass, RoutineInput.class, "RoutineInput", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRoutineInput_ModelInputElements(), theElementsPackage.getNamedMetaclassReference(), null, "modelInputElements", null, 0, -1, RoutineInput.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRoutineInput_JavaInputElements(), this.getNamedJavaElementReference(), null, "javaInputElements", null, 0, -1, RoutineInput.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(matchBlockEClass, MatchBlock.class, "MatchBlock", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMatchBlock_MatchStatements(), this.getMatchStatement(), null, "matchStatements", null, 0, -1, MatchBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(matchStatementEClass, MatchStatement.class, "MatchStatement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(createBlockEClass, CreateBlock.class, "CreateBlock", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCreateBlock_CreateStatements(), theElementsPackage.getNamedMetaclassReference(), null, "createStatements", null, 0, -1, CreateBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(namedJavaElementReferenceEClass, NamedJavaElementReference.class, "NamedJavaElementReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNamedJavaElementReference_Name(), ecorePackage.getEString(), "name", null, 0, 1, NamedJavaElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getNamedJavaElementReference_Type(), theTypesPackage.getJvmTypeReference(), null, "type", null, 0, 1, NamedJavaElementReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(routineCallEClass, RoutineCall.class, "RoutineCall", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(updateBlockEClass, UpdateBlock.class, "UpdateBlock", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(codeExecutionBlockEClass, CodeExecutionBlock.class, "CodeExecutionBlock", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCodeExecutionBlock_Code(), theXbasePackage.getXExpression(), null, "code", null, 0, 1, CodeExecutionBlock.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //TopLevelElementsPackageImpl
