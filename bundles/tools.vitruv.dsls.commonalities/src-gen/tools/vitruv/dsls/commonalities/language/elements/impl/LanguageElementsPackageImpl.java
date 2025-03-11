/**
 */
package tools.vitruv.dsls.commonalities.language.elements.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass;
import tools.vitruv.dsls.commonalities.language.elements.EDataTypeClassifier;
import tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsFactory;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage;
import tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType;
import tools.vitruv.dsls.commonalities.language.elements.MemberLike;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;
import tools.vitruv.dsls.commonalities.language.elements.MetaclassMember;
import tools.vitruv.dsls.commonalities.language.elements.Metamodel;
import tools.vitruv.dsls.commonalities.language.elements.MostSpecificType;
import tools.vitruv.dsls.commonalities.language.elements.NamedElement;
import tools.vitruv.dsls.commonalities.language.elements.PackageLike;
import tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class LanguageElementsPackageImpl extends EPackageImpl implements LanguageElementsPackage
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packageLikeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass classLikeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass memberLikeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass namedElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass domainEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass classifierEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metaclassEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metaclassMemberEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attributeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eClassMetaclassEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eDataTypeClassifierEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass metamodelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass resourceMetaclassEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eFeatureAttributeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mostSpecificTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass leastSpecificTypeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType classifierProviderEDataType = null;

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
	 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private LanguageElementsPackageImpl()
	{
		super(eNS_URI, LanguageElementsFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link LanguageElementsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static LanguageElementsPackage init()
	{
		if (isInited) return (LanguageElementsPackage)EPackage.Registry.INSTANCE.getEPackage(LanguageElementsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredLanguageElementsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		LanguageElementsPackageImpl theLanguageElementsPackage = registeredLanguageElementsPackage instanceof LanguageElementsPackageImpl ? (LanguageElementsPackageImpl)registeredLanguageElementsPackage : new LanguageElementsPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theLanguageElementsPackage.createPackageContents();

		// Initialize created meta-data
		theLanguageElementsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theLanguageElementsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(LanguageElementsPackage.eNS_URI, theLanguageElementsPackage);
		return theLanguageElementsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackageLike()
	{
		return packageLikeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClassLike()
	{
		return classLikeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getClassLike_PackageLikeContainer()
	{
		return (EReference)classLikeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMemberLike()
	{
		return memberLikeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMemberLike_ClassLikeContainer()
	{
		return (EReference)memberLikeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getNamedElement()
	{
		return namedElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getNamedElement__GetName()
	{
		return namedElementEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDomain()
	{
		return domainEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClassifier()
	{
		return classifierEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getClassifier__IsSuperTypeOf__Classifier()
	{
		return classifierEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMetaclass()
	{
		return metaclassEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMetaclass_Domain()
	{
		return (EReference)metaclassEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMetaclass_AllMembers()
	{
		return (EReference)metaclassEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMetaclass_Abstract()
	{
		return (EAttribute)metaclassEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getMetaclass__GetAttributes()
	{
		return metaclassEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMetaclassMember()
	{
		return metaclassMemberEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getMetaclassMember__GetType()
	{
		return metaclassMemberEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttribute()
	{
		return attributeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttribute_MultiValued()
	{
		return (EAttribute)attributeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEClassMetaclass()
	{
		return eClassMetaclassEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEClassMetaclass_Attributes()
	{
		return (EReference)eClassMetaclassEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEClassMetaclass__ForEClass__EClass()
	{
		return eClassMetaclassEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEClassMetaclass__WithClassifierProvider__ClassifierProvider()
	{
		return eClassMetaclassEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEClassMetaclass__FromDomain__Domain()
	{
		return eClassMetaclassEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEDataTypeClassifier()
	{
		return eDataTypeClassifierEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEDataTypeClassifier__ForEDataType__EDataType()
	{
		return eDataTypeClassifierEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMetamodel()
	{
		return metamodelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getMetamodel_Metaclasses()
	{
		return (EReference)metamodelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getMetamodel__ForEPackage__EPackage()
	{
		return metamodelEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getMetamodel__WithClassifierProvider__ClassifierProvider()
	{
		return metamodelEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getResourceMetaclass()
	{
		return resourceMetaclassEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getResourceMetaclass_Attributes()
	{
		return (EReference)resourceMetaclassEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getResourceMetaclass__WithClassifierProvider__ClassifierProvider()
	{
		return resourceMetaclassEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getResourceMetaclass__FromDomain__Domain()
	{
		return resourceMetaclassEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEFeatureAttribute()
	{
		return eFeatureAttributeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEFeatureAttribute__ForEFeature__EStructuralFeature()
	{
		return eFeatureAttributeEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEFeatureAttribute__WithClassifierProvider__ClassifierProvider()
	{
		return eFeatureAttributeEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEFeatureAttribute__FromMetaclass__Metaclass()
	{
		return eFeatureAttributeEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMostSpecificType()
	{
		return mostSpecificTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLeastSpecificType()
	{
		return leastSpecificTypeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getClassifierProvider()
	{
		return classifierProviderEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public LanguageElementsFactory getLanguageElementsFactory()
	{
		return (LanguageElementsFactory)getEFactoryInstance();
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
		packageLikeEClass = createEClass(PACKAGE_LIKE);

		classLikeEClass = createEClass(CLASS_LIKE);
		createEReference(classLikeEClass, CLASS_LIKE__PACKAGE_LIKE_CONTAINER);

		memberLikeEClass = createEClass(MEMBER_LIKE);
		createEReference(memberLikeEClass, MEMBER_LIKE__CLASS_LIKE_CONTAINER);

		namedElementEClass = createEClass(NAMED_ELEMENT);
		createEOperation(namedElementEClass, NAMED_ELEMENT___GET_NAME);

		domainEClass = createEClass(DOMAIN);

		classifierEClass = createEClass(CLASSIFIER);
		createEOperation(classifierEClass, CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER);

		metaclassEClass = createEClass(METACLASS);
		createEReference(metaclassEClass, METACLASS__DOMAIN);
		createEReference(metaclassEClass, METACLASS__ALL_MEMBERS);
		createEAttribute(metaclassEClass, METACLASS__ABSTRACT);
		createEOperation(metaclassEClass, METACLASS___GET_ATTRIBUTES);

		metaclassMemberEClass = createEClass(METACLASS_MEMBER);
		createEOperation(metaclassMemberEClass, METACLASS_MEMBER___GET_TYPE);

		attributeEClass = createEClass(ATTRIBUTE);
		createEAttribute(attributeEClass, ATTRIBUTE__MULTI_VALUED);

		eClassMetaclassEClass = createEClass(ECLASS_METACLASS);
		createEReference(eClassMetaclassEClass, ECLASS_METACLASS__ATTRIBUTES);
		createEOperation(eClassMetaclassEClass, ECLASS_METACLASS___FOR_ECLASS__ECLASS);
		createEOperation(eClassMetaclassEClass, ECLASS_METACLASS___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER);
		createEOperation(eClassMetaclassEClass, ECLASS_METACLASS___FROM_DOMAIN__DOMAIN);

		eDataTypeClassifierEClass = createEClass(EDATA_TYPE_CLASSIFIER);
		createEOperation(eDataTypeClassifierEClass, EDATA_TYPE_CLASSIFIER___FOR_EDATA_TYPE__EDATATYPE);

		metamodelEClass = createEClass(METAMODEL);
		createEReference(metamodelEClass, METAMODEL__METACLASSES);
		createEOperation(metamodelEClass, METAMODEL___FOR_EPACKAGE__EPACKAGE);
		createEOperation(metamodelEClass, METAMODEL___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER);

		resourceMetaclassEClass = createEClass(RESOURCE_METACLASS);
		createEReference(resourceMetaclassEClass, RESOURCE_METACLASS__ATTRIBUTES);
		createEOperation(resourceMetaclassEClass, RESOURCE_METACLASS___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER);
		createEOperation(resourceMetaclassEClass, RESOURCE_METACLASS___FROM_DOMAIN__DOMAIN);

		eFeatureAttributeEClass = createEClass(EFEATURE_ATTRIBUTE);
		createEOperation(eFeatureAttributeEClass, EFEATURE_ATTRIBUTE___FOR_EFEATURE__ESTRUCTURALFEATURE);
		createEOperation(eFeatureAttributeEClass, EFEATURE_ATTRIBUTE___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER);
		createEOperation(eFeatureAttributeEClass, EFEATURE_ATTRIBUTE___FROM_METACLASS__METACLASS);

		mostSpecificTypeEClass = createEClass(MOST_SPECIFIC_TYPE);

		leastSpecificTypeEClass = createEClass(LEAST_SPECIFIC_TYPE);

		// Create data types
		classifierProviderEDataType = createEDataType(CLASSIFIER_PROVIDER);
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

		// Set bounds for type parameters

		// Add supertypes to classes
		packageLikeEClass.getESuperTypes().add(this.getNamedElement());
		classLikeEClass.getESuperTypes().add(this.getNamedElement());
		memberLikeEClass.getESuperTypes().add(this.getNamedElement());
		domainEClass.getESuperTypes().add(this.getPackageLike());
		classifierEClass.getESuperTypes().add(this.getNamedElement());
		metaclassEClass.getESuperTypes().add(this.getClassLike());
		metaclassEClass.getESuperTypes().add(this.getClassifier());
		metaclassMemberEClass.getESuperTypes().add(this.getMemberLike());
		attributeEClass.getESuperTypes().add(this.getMetaclassMember());
		eClassMetaclassEClass.getESuperTypes().add(this.getMetaclass());
		eDataTypeClassifierEClass.getESuperTypes().add(this.getClassifier());
		metamodelEClass.getESuperTypes().add(this.getDomain());
		resourceMetaclassEClass.getESuperTypes().add(this.getMetaclass());
		eFeatureAttributeEClass.getESuperTypes().add(this.getAttribute());
		mostSpecificTypeEClass.getESuperTypes().add(this.getClassifier());
		leastSpecificTypeEClass.getESuperTypes().add(this.getClassifier());

		// Initialize classes, features, and operations; add parameters
		initEClass(packageLikeEClass, PackageLike.class, "PackageLike", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(classLikeEClass, ClassLike.class, "ClassLike", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getClassLike_PackageLikeContainer(), this.getPackageLike(), null, "packageLikeContainer", null, 1, 1, ClassLike.class, !IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(memberLikeEClass, MemberLike.class, "MemberLike", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMemberLike_ClassLikeContainer(), this.getClassLike(), null, "classLikeContainer", null, 1, 1, MemberLike.class, !IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(namedElementEClass, NamedElement.class, "NamedElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEOperation(getNamedElement__GetName(), ecorePackage.getEString(), "getName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(domainEClass, Domain.class, "Domain", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(classifierEClass, Classifier.class, "Classifier", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		EOperation op = initEOperation(getClassifier__IsSuperTypeOf__Classifier(), ecorePackage.getEBoolean(), "isSuperTypeOf", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getClassifier(), "subType", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(metaclassEClass, Metaclass.class, "Metaclass", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetaclass_Domain(), this.getDomain(), null, "domain", null, 1, 1, Metaclass.class, !IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEReference(getMetaclass_AllMembers(), this.getMetaclassMember(), null, "allMembers", null, 0, -1, Metaclass.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEAttribute(getMetaclass_Abstract(), ecorePackage.getEBoolean(), "abstract", null, 1, 1, Metaclass.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		op = initEOperation(getMetaclass__GetAttributes(), null, "getAttributes", 0, -1, IS_UNIQUE, IS_ORDERED);
		ETypeParameter t1 = addETypeParameter(op, "A");
		EGenericType g1 = createEGenericType(this.getAttribute());
		t1.getEBounds().add(g1);
		g1 = createEGenericType(t1);
		initEOperation(op, g1);

		initEClass(metaclassMemberEClass, MetaclassMember.class, "MetaclassMember", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEOperation(getMetaclassMember__GetType(), this.getClassifier(), "getType", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(attributeEClass, Attribute.class, "Attribute", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAttribute_MultiValued(), ecorePackage.getEBoolean(), "multiValued", null, 1, 1, Attribute.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		initEClass(eClassMetaclassEClass, EClassMetaclass.class, "EClassMetaclass", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getEClassMetaclass_Attributes(), this.getAttribute(), null, "attributes", null, 0, -1, EClassMetaclass.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		op = initEOperation(getEClassMetaclass__ForEClass__EClass(), this.getEClassMetaclass(), "forEClass", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEClass(), "eClass", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEClassMetaclass__WithClassifierProvider__ClassifierProvider(), this.getEClassMetaclass(), "withClassifierProvider", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getClassifierProvider(), "classifierProvider", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEClassMetaclass__FromDomain__Domain(), this.getEClassMetaclass(), "fromDomain", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getDomain(), "domain", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(eDataTypeClassifierEClass, EDataTypeClassifier.class, "EDataTypeClassifier", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getEDataTypeClassifier__ForEDataType__EDataType(), this.getEDataTypeClassifier(), "forEDataType", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEDataType(), "eDataType", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(metamodelEClass, Metamodel.class, "Metamodel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getMetamodel_Metaclasses(), this.getMetaclass(), null, "metaclasses", null, 0, -1, Metamodel.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, !IS_ORDERED);

		op = initEOperation(getMetamodel__ForEPackage__EPackage(), this.getMetamodel(), "forEPackage", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEPackage(), "ePackage", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getMetamodel__WithClassifierProvider__ClassifierProvider(), this.getMetamodel(), "withClassifierProvider", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getClassifierProvider(), "classifierProvider", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(resourceMetaclassEClass, ResourceMetaclass.class, "ResourceMetaclass", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getResourceMetaclass_Attributes(), this.getAttribute(), null, "attributes", null, 0, -1, ResourceMetaclass.class, !IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);

		op = initEOperation(getResourceMetaclass__WithClassifierProvider__ClassifierProvider(), this.getResourceMetaclass(), "withClassifierProvider", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getClassifierProvider(), "classifierProvider", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getResourceMetaclass__FromDomain__Domain(), this.getResourceMetaclass(), "fromDomain", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getDomain(), "domain", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(eFeatureAttributeEClass, EFeatureAttribute.class, "EFeatureAttribute", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getEFeatureAttribute__ForEFeature__EStructuralFeature(), this.getEFeatureAttribute(), "forEFeature", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEStructuralFeature(), "eFeature", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEFeatureAttribute__WithClassifierProvider__ClassifierProvider(), this.getEFeatureAttribute(), "withClassifierProvider", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getClassifierProvider(), "classifierProvider", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEFeatureAttribute__FromMetaclass__Metaclass(), this.getEFeatureAttribute(), "fromMetaclass", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getMetaclass(), "metaclass", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(mostSpecificTypeEClass, MostSpecificType.class, "MostSpecificType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(leastSpecificTypeEClass, LeastSpecificType.class, "LeastSpecificType", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Initialize data types
		initEDataType(classifierProviderEDataType, ClassifierProvider.class, "ClassifierProvider", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //LanguageElementsPackageImpl
