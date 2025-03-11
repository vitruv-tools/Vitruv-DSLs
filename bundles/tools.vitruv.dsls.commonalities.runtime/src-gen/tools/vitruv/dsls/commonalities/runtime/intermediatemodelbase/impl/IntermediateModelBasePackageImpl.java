/**
 */
package tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBaseFactory;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root;

import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;

import tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class IntermediateModelBasePackageImpl extends EPackageImpl implements IntermediateModelBasePackage
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass rootEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass intermediateEClass = null;

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
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private IntermediateModelBasePackageImpl()
	{
		super(eNS_URI, IntermediateModelBaseFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link IntermediateModelBasePackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static IntermediateModelBasePackage init()
	{
		if (isInited) return (IntermediateModelBasePackage)EPackage.Registry.INSTANCE.getEPackage(IntermediateModelBasePackage.eNS_URI);

		// Obtain or create and register package
		Object registeredIntermediateModelBasePackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		IntermediateModelBasePackageImpl theIntermediateModelBasePackage = registeredIntermediateModelBasePackage instanceof IntermediateModelBasePackageImpl ? (IntermediateModelBasePackageImpl)registeredIntermediateModelBasePackage : new IntermediateModelBasePackageImpl();

		isInited = true;

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ResourcesPackage.eNS_URI);
		ResourcesPackageImpl theResourcesPackage = (ResourcesPackageImpl)(registeredPackage instanceof ResourcesPackageImpl ? registeredPackage : ResourcesPackage.eINSTANCE);

		// Create package meta-data objects
		theIntermediateModelBasePackage.createPackageContents();
		theResourcesPackage.createPackageContents();

		// Initialize created meta-data
		theIntermediateModelBasePackage.initializePackageContents();
		theResourcesPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theIntermediateModelBasePackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(IntermediateModelBasePackage.eNS_URI, theIntermediateModelBasePackage);
		return theIntermediateModelBasePackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRoot()
	{
		return rootEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoot_Intermediates()
	{
		return (EReference)rootEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRoot_ResourceBridges()
	{
		return (EReference)rootEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRoot_IntermediateId()
	{
		return (EAttribute)rootEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIntermediate()
	{
		return intermediateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediate_IntermediateId()
	{
		return (EAttribute)intermediateEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IntermediateModelBaseFactory getIntermediateModelBaseFactory()
	{
		return (IntermediateModelBaseFactory)getEFactoryInstance();
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
		rootEClass = createEClass(ROOT);
		createEReference(rootEClass, ROOT__INTERMEDIATES);
		createEReference(rootEClass, ROOT__RESOURCE_BRIDGES);
		createEAttribute(rootEClass, ROOT__INTERMEDIATE_ID);

		intermediateEClass = createEClass(INTERMEDIATE);
		createEAttribute(intermediateEClass, INTERMEDIATE__INTERMEDIATE_ID);
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
		ResourcesPackage theResourcesPackage = (ResourcesPackage)EPackage.Registry.INSTANCE.getEPackage(ResourcesPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(rootEClass, Root.class, "Root", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRoot_Intermediates(), this.getIntermediate(), null, "intermediates", null, 0, -1, Root.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEReference(getRoot_ResourceBridges(), theResourcesPackage.getResource(), null, "resourceBridges", null, 0, -1, Root.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getRoot_IntermediateId(), ecorePackage.getEString(), "intermediateId", null, 1, 1, Root.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(intermediateEClass, Intermediate.class, "Intermediate", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIntermediate_IntermediateId(), ecorePackage.getEString(), "intermediateId", null, 1, 1, Intermediate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //IntermediateModelBasePackageImpl
