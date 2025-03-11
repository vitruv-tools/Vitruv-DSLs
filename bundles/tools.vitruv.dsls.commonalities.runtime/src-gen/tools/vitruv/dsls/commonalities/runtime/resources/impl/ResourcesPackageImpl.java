/**
 */
package tools.vitruv.dsls.commonalities.runtime.resources.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;

import tools.vitruv.change.propagation.ResourceAccess;

import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;

import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateModelBasePackageImpl;

import tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge;
import tools.vitruv.dsls.commonalities.runtime.resources.Resource;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesFactory;
import tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage;

import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ResourcesPackageImpl extends EPackageImpl implements ResourcesPackage
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass resourceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass intermediateResourceBridgeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType resourceAccessEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType editableCorrespondenceModelViewEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType uriEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType emfResourceEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType reactionsCorrespondenceEDataType = null;

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
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ResourcesPackageImpl()
	{
		super(eNS_URI, ResourcesFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ResourcesPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ResourcesPackage init()
	{
		if (isInited) return (ResourcesPackage)EPackage.Registry.INSTANCE.getEPackage(ResourcesPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredResourcesPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ResourcesPackageImpl theResourcesPackage = registeredResourcesPackage instanceof ResourcesPackageImpl ? (ResourcesPackageImpl)registeredResourcesPackage : new ResourcesPackageImpl();

		isInited = true;

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(IntermediateModelBasePackage.eNS_URI);
		IntermediateModelBasePackageImpl theIntermediateModelBasePackage = (IntermediateModelBasePackageImpl)(registeredPackage instanceof IntermediateModelBasePackageImpl ? registeredPackage : IntermediateModelBasePackage.eINSTANCE);

		// Create package meta-data objects
		theResourcesPackage.createPackageContents();
		theIntermediateModelBasePackage.createPackageContents();

		// Initialize created meta-data
		theResourcesPackage.initializePackageContents();
		theIntermediateModelBasePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theResourcesPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ResourcesPackage.eNS_URI, theResourcesPackage);
		return theResourcesPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getResource()
	{
		return resourceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResource_Name()
	{
		return (EAttribute)resourceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResource_FileExtension()
	{
		return (EAttribute)resourceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResource_Path()
	{
		return (EAttribute)resourceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getResource_Content()
	{
		return (EReference)resourceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getResource_FullPath()
	{
		return (EAttribute)resourceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIntermediateResourceBridge()
	{
		return intermediateResourceBridgeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_BaseURI()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_IsPersisted()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_IntermediateId()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_ResourceAccess()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_CorrespondenceModel()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_IntermediateNS()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_EmfResource()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIntermediateResourceBridge_IsPersistenceEnabled()
	{
		return (EAttribute)intermediateResourceBridgeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getIntermediateResourceBridge__Remove()
	{
		return intermediateResourceBridgeEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getIntermediateResourceBridge__InitialiseForModelElement__EObject()
	{
		return intermediateResourceBridgeEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getResourceAccess()
	{
		return resourceAccessEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getEditableCorrespondenceModelView()
	{
		return editableCorrespondenceModelViewEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getURI()
	{
		return uriEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getEmfResource()
	{
		return emfResourceEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getReactionsCorrespondence()
	{
		return reactionsCorrespondenceEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourcesFactory getResourcesFactory()
	{
		return (ResourcesFactory)getEFactoryInstance();
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
		resourceEClass = createEClass(RESOURCE);
		createEAttribute(resourceEClass, RESOURCE__NAME);
		createEAttribute(resourceEClass, RESOURCE__FILE_EXTENSION);
		createEAttribute(resourceEClass, RESOURCE__PATH);
		createEReference(resourceEClass, RESOURCE__CONTENT);
		createEAttribute(resourceEClass, RESOURCE__FULL_PATH);

		intermediateResourceBridgeEClass = createEClass(INTERMEDIATE_RESOURCE_BRIDGE);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__BASE_URI);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTED);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_ID);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__EMF_RESOURCE);
		createEAttribute(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED);
		createEOperation(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE___REMOVE);
		createEOperation(intermediateResourceBridgeEClass, INTERMEDIATE_RESOURCE_BRIDGE___INITIALISE_FOR_MODEL_ELEMENT__EOBJECT);

		// Create data types
		resourceAccessEDataType = createEDataType(RESOURCE_ACCESS);
		editableCorrespondenceModelViewEDataType = createEDataType(EDITABLE_CORRESPONDENCE_MODEL_VIEW);
		uriEDataType = createEDataType(URI);
		emfResourceEDataType = createEDataType(EMF_RESOURCE);
		reactionsCorrespondenceEDataType = createEDataType(REACTIONS_CORRESPONDENCE);
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

		// Create type parameters
		ETypeParameter editableCorrespondenceModelViewEDataType_C = addETypeParameter(editableCorrespondenceModelViewEDataType, "C");

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(this.getReactionsCorrespondence());
		editableCorrespondenceModelViewEDataType_C.getEBounds().add(g1);

		// Add supertypes to classes
		intermediateResourceBridgeEClass.getESuperTypes().add(this.getResource());

		// Initialize classes, features, and operations; add parameters
		initEClass(resourceEClass, Resource.class, "Resource", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getResource_Name(), ecorePackage.getEString(), "name", null, 0, 1, Resource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getResource_FileExtension(), ecorePackage.getEString(), "fileExtension", null, 0, 1, Resource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getResource_Path(), ecorePackage.getEString(), "path", "", 0, 1, Resource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getResource_Content(), ecorePackage.getEObject(), null, "content", null, 0, 1, Resource.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getResource_FullPath(), ecorePackage.getEString(), "fullPath", null, 0, 1, Resource.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(intermediateResourceBridgeEClass, IntermediateResourceBridge.class, "IntermediateResourceBridge", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIntermediateResourceBridge_BaseURI(), this.getURI(), "baseURI", null, 0, 1, IntermediateResourceBridge.class, IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntermediateResourceBridge_IsPersisted(), ecorePackage.getEBoolean(), "isPersisted", null, 0, 1, IntermediateResourceBridge.class, IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntermediateResourceBridge_IntermediateId(), ecorePackage.getEString(), "intermediateId", null, 1, 1, IntermediateResourceBridge.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntermediateResourceBridge_ResourceAccess(), this.getResourceAccess(), "resourceAccess", null, 0, 1, IntermediateResourceBridge.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		g1 = createEGenericType(this.getEditableCorrespondenceModelView());
		EGenericType g2 = createEGenericType(this.getReactionsCorrespondence());
		g1.getETypeArguments().add(g2);
		initEAttribute(getIntermediateResourceBridge_CorrespondenceModel(), g1, "correspondenceModel", null, 0, 1, IntermediateResourceBridge.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIntermediateResourceBridge_IntermediateNS(), ecorePackage.getEString(), "intermediateNS", null, 1, 1, IntermediateResourceBridge.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);
		initEAttribute(getIntermediateResourceBridge_EmfResource(), this.getEmfResource(), "emfResource", null, 0, 1, IntermediateResourceBridge.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, IS_DERIVED, !IS_ORDERED);
		initEAttribute(getIntermediateResourceBridge_IsPersistenceEnabled(), ecorePackage.getEBoolean(), "isPersistenceEnabled", "true", 1, 1, IntermediateResourceBridge.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, !IS_ORDERED);

		initEOperation(getIntermediateResourceBridge__Remove(), null, "remove", 0, 1, IS_UNIQUE, IS_ORDERED);

		EOperation op = initEOperation(getIntermediateResourceBridge__InitialiseForModelElement__EObject(), null, "initialiseForModelElement", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEObject(), "eObject", 0, 1, IS_UNIQUE, IS_ORDERED);

		// Initialize data types
		initEDataType(resourceAccessEDataType, ResourceAccess.class, "ResourceAccess", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(editableCorrespondenceModelViewEDataType, EditableCorrespondenceModelView.class, "EditableCorrespondenceModelView", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(uriEDataType, org.eclipse.emf.common.util.URI.class, "URI", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(emfResourceEDataType, org.eclipse.emf.ecore.resource.Resource.class, "EmfResource", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(reactionsCorrespondenceEDataType, ReactionsCorrespondence.class, "ReactionsCorrespondence", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //ResourcesPackageImpl
