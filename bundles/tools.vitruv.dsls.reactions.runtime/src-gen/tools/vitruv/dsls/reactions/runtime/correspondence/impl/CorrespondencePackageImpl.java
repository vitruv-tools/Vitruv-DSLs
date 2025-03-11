/**
 */
package tools.vitruv.dsls.reactions.runtime.correspondence.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondenceFactory;
import tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondencePackage;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CorrespondencePackageImpl extends EPackageImpl implements CorrespondencePackage
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass reactionsCorrespondenceEClass = null;

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
	 * @see tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondencePackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private CorrespondencePackageImpl()
	{
		super(eNS_URI, CorrespondenceFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link CorrespondencePackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static CorrespondencePackage init()
	{
		if (isInited) return (CorrespondencePackage)EPackage.Registry.INSTANCE.getEPackage(CorrespondencePackage.eNS_URI);

		// Obtain or create and register package
		Object registeredCorrespondencePackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		CorrespondencePackageImpl theCorrespondencePackage = registeredCorrespondencePackage instanceof CorrespondencePackageImpl ? (CorrespondencePackageImpl)registeredCorrespondencePackage : new CorrespondencePackageImpl();

		isInited = true;

		// Initialize simple dependencies
		tools.vitruv.change.correspondence.CorrespondencePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theCorrespondencePackage.createPackageContents();

		// Initialize created meta-data
		theCorrespondencePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCorrespondencePackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(CorrespondencePackage.eNS_URI, theCorrespondencePackage);
		return theCorrespondencePackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReactionsCorrespondence()
	{
		return reactionsCorrespondenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CorrespondenceFactory getCorrespondenceFactory()
	{
		return (CorrespondenceFactory)getEFactoryInstance();
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
		reactionsCorrespondenceEClass = createEClass(REACTIONS_CORRESPONDENCE);
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
		tools.vitruv.change.correspondence.CorrespondencePackage theCorrespondencePackage_1 = (tools.vitruv.change.correspondence.CorrespondencePackage)EPackage.Registry.INSTANCE.getEPackage(tools.vitruv.change.correspondence.CorrespondencePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		reactionsCorrespondenceEClass.getESuperTypes().add(theCorrespondencePackage_1.getCorrespondence());

		// Initialize classes and features; add operations and parameters
		initEClass(reactionsCorrespondenceEClass, ReactionsCorrespondence.class, "ReactionsCorrespondence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //CorrespondencePackageImpl
