/**
 */
package tools.vitruv.dsls.common.elements.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.xtext.common.types.TypesPackage;

import tools.vitruv.dsls.common.elements.ElementsFactory;
import tools.vitruv.dsls.common.elements.ElementsPackage;
import tools.vitruv.dsls.common.elements.MetaclassEAttributeReference;
import tools.vitruv.dsls.common.elements.MetaclassEReferenceReference;
import tools.vitruv.dsls.common.elements.MetaclassFeatureReference;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ElementsPackageImpl extends EPackageImpl implements ElementsPackage
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metamodelImportEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metaclassReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass namedMetaclassReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metaclassFeatureReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metaclassEAttributeReferenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metaclassEReferenceReferenceEClass = null;

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
	 * @see tools.vitruv.dsls.common.elements.ElementsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ElementsPackageImpl()
	{
		super(eNS_URI, ElementsFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ElementsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ElementsPackage init()
	{
		if (isInited) return (ElementsPackage)EPackage.Registry.INSTANCE.getEPackage(ElementsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredElementsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ElementsPackageImpl theElementsPackage = registeredElementsPackage instanceof ElementsPackageImpl ? (ElementsPackageImpl)registeredElementsPackage : new ElementsPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();
		TypesPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theElementsPackage.createPackageContents();

		// Initialize created meta-data
		theElementsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theElementsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ElementsPackage.eNS_URI, theElementsPackage);
		return theElementsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMetamodelImport()
	{
		return metamodelImportEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMetamodelImport_Package()
	{
		return (EReference)metamodelImportEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMetamodelImport_Name()
	{
		return (EAttribute)metamodelImportEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMetamodelImport_UseQualifiedNames()
	{
		return (EAttribute)metamodelImportEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMetaclassReference()
	{
		return metaclassReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMetaclassReference_Metamodel()
	{
		return (EReference)metaclassReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMetaclassReference_Metaclass()
	{
		return (EReference)metaclassReferenceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getNamedMetaclassReference()
	{
		return namedMetaclassReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getNamedMetaclassReference_Name()
	{
		return (EAttribute)namedMetaclassReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMetaclassFeatureReference()
	{
		return metaclassFeatureReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMetaclassFeatureReference_Feature()
	{
		return (EReference)metaclassFeatureReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMetaclassEAttributeReference()
	{
		return metaclassEAttributeReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMetaclassEAttributeReference_Feature()
	{
		return (EReference)metaclassEAttributeReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMetaclassEReferenceReference()
	{
		return metaclassEReferenceReferenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMetaclassEReferenceReference_Feature()
	{
		return (EReference)metaclassEReferenceReferenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ElementsFactory getElementsFactory()
	{
		return (ElementsFactory)getEFactoryInstance();
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
		metamodelImportEClass = createEClass(METAMODEL_IMPORT);
		createEReference(metamodelImportEClass, METAMODEL_IMPORT__PACKAGE);
		createEAttribute(metamodelImportEClass, METAMODEL_IMPORT__NAME);
		createEAttribute(metamodelImportEClass, METAMODEL_IMPORT__USE_QUALIFIED_NAMES);

		metaclassReferenceEClass = createEClass(METACLASS_REFERENCE);
		createEReference(metaclassReferenceEClass, METACLASS_REFERENCE__METAMODEL);
		createEReference(metaclassReferenceEClass, METACLASS_REFERENCE__METACLASS);

		namedMetaclassReferenceEClass = createEClass(NAMED_METACLASS_REFERENCE);
		createEAttribute(namedMetaclassReferenceEClass, NAMED_METACLASS_REFERENCE__NAME);

		metaclassFeatureReferenceEClass = createEClass(METACLASS_FEATURE_REFERENCE);
		createEReference(metaclassFeatureReferenceEClass, METACLASS_FEATURE_REFERENCE__FEATURE);

		metaclassEAttributeReferenceEClass = createEClass(METACLASS_EATTRIBUTE_REFERENCE);
		createEReference(metaclassEAttributeReferenceEClass, METACLASS_EATTRIBUTE_REFERENCE__FEATURE);

		metaclassEReferenceReferenceEClass = createEClass(METACLASS_EREFERENCE_REFERENCE);
		createEReference(metaclassEReferenceReferenceEClass, METACLASS_EREFERENCE_REFERENCE__FEATURE);
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
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		namedMetaclassReferenceEClass.getESuperTypes().add(this.getMetaclassReference());
		metaclassFeatureReferenceEClass.getESuperTypes().add(this.getMetaclassReference());
		metaclassEAttributeReferenceEClass.getESuperTypes().add(this.getMetaclassReference());
		metaclassEReferenceReferenceEClass.getESuperTypes().add(this.getMetaclassReference());

		// Initialize classes, features, and operations; add parameters
		initEClass(metamodelImportEClass, MetamodelImport.class, "MetamodelImport", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetamodelImport_Package(), theEcorePackage.getEPackage(), null, "package", null, 0, 1, MetamodelImport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMetamodelImport_Name(), theEcorePackage.getEString(), "name", null, 0, 1, MetamodelImport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMetamodelImport_UseQualifiedNames(), theEcorePackage.getEBoolean(), "useQualifiedNames", null, 0, 1, MetamodelImport.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(metaclassReferenceEClass, MetaclassReference.class, "MetaclassReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetaclassReference_Metamodel(), this.getMetamodelImport(), null, "metamodel", null, 0, 1, MetaclassReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getMetaclassReference_Metaclass(), theEcorePackage.getEClassifier(), null, "metaclass", null, 0, 1, MetaclassReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(namedMetaclassReferenceEClass, NamedMetaclassReference.class, "NamedMetaclassReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getNamedMetaclassReference_Name(), ecorePackage.getEString(), "name", null, 0, 1, NamedMetaclassReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(metaclassFeatureReferenceEClass, MetaclassFeatureReference.class, "MetaclassFeatureReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetaclassFeatureReference_Feature(), theEcorePackage.getEStructuralFeature(), null, "feature", null, 0, 1, MetaclassFeatureReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(metaclassEAttributeReferenceEClass, MetaclassEAttributeReference.class, "MetaclassEAttributeReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetaclassEAttributeReference_Feature(), theEcorePackage.getEAttribute(), null, "feature", null, 0, 1, MetaclassEAttributeReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(metaclassEReferenceReferenceEClass, MetaclassEReferenceReference.class, "MetaclassEReferenceReference", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetaclassEReferenceReference_Feature(), theEcorePackage.getEReference(), null, "feature", null, 0, 1, MetaclassEReferenceReference.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //ElementsPackageImpl
