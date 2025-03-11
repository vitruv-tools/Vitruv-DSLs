/**
 */
package tools.vitruv.dsls.commonalities.language.elements;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.commonalities.language.elements.LanguageElementsFactory
 * @model kind="package"
 * @generated
 */
public interface LanguageElementsPackage extends EPackage
{
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "elements";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://vitruv.tools/dsls/commonalities/elements";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "languagelements";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	LanguageElementsPackage eINSTANCE = tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl.init();

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.NamedElement <em>Named Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.NamedElement
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getNamedElement()
	 * @generated
	 */
	int NAMED_ELEMENT = 3;

	/**
	 * The number of structural features of the '<em>Named Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_ELEMENT_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_ELEMENT___GET_NAME = 0;

	/**
	 * The number of operations of the '<em>Named Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_ELEMENT_OPERATION_COUNT = 1;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.PackageLike <em>Package Like</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.PackageLike
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getPackageLike()
	 * @generated
	 */
	int PACKAGE_LIKE = 0;

	/**
	 * The number of structural features of the '<em>Package Like</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_LIKE_FEATURE_COUNT = NAMED_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_LIKE___GET_NAME = NAMED_ELEMENT___GET_NAME;

	/**
	 * The number of operations of the '<em>Package Like</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_LIKE_OPERATION_COUNT = NAMED_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.ClassLike <em>Class Like</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.ClassLike
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getClassLike()
	 * @generated
	 */
	int CLASS_LIKE = 1;

	/**
	 * The feature id for the '<em><b>Package Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_LIKE__PACKAGE_LIKE_CONTAINER = NAMED_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Class Like</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_LIKE_FEATURE_COUNT = NAMED_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_LIKE___GET_NAME = NAMED_ELEMENT___GET_NAME;

	/**
	 * The number of operations of the '<em>Class Like</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_LIKE_OPERATION_COUNT = NAMED_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.MemberLike <em>Member Like</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.MemberLike
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMemberLike()
	 * @generated
	 */
	int MEMBER_LIKE = 2;

	/**
	 * The feature id for the '<em><b>Class Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMBER_LIKE__CLASS_LIKE_CONTAINER = NAMED_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Member Like</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMBER_LIKE_FEATURE_COUNT = NAMED_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMBER_LIKE___GET_NAME = NAMED_ELEMENT___GET_NAME;

	/**
	 * The number of operations of the '<em>Member Like</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MEMBER_LIKE_OPERATION_COUNT = NAMED_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.Domain <em>Domain</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.Domain
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getDomain()
	 * @generated
	 */
	int DOMAIN = 4;

	/**
	 * The number of structural features of the '<em>Domain</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOMAIN_FEATURE_COUNT = PACKAGE_LIKE_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOMAIN___GET_NAME = PACKAGE_LIKE___GET_NAME;

	/**
	 * The number of operations of the '<em>Domain</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DOMAIN_OPERATION_COUNT = PACKAGE_LIKE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.Classifier <em>Classifier</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.Classifier
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getClassifier()
	 * @generated
	 */
	int CLASSIFIER = 5;

	/**
	 * The number of structural features of the '<em>Classifier</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASSIFIER_FEATURE_COUNT = NAMED_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASSIFIER___GET_NAME = NAMED_ELEMENT___GET_NAME;

	/**
	 * The operation id for the '<em>Is Super Type Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER = NAMED_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Classifier</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASSIFIER_OPERATION_COUNT = NAMED_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass <em>Metaclass</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metaclass
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMetaclass()
	 * @generated
	 */
	int METACLASS = 6;

	/**
	 * The feature id for the '<em><b>Package Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS__PACKAGE_LIKE_CONTAINER = CLASS_LIKE__PACKAGE_LIKE_CONTAINER;

	/**
	 * The feature id for the '<em><b>Domain</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS__DOMAIN = CLASS_LIKE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>All Members</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS__ALL_MEMBERS = CLASS_LIKE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS__ABSTRACT = CLASS_LIKE_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Metaclass</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_FEATURE_COUNT = CLASS_LIKE_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS___GET_NAME = CLASS_LIKE___GET_NAME;

	/**
	 * The operation id for the '<em>Is Super Type Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS___IS_SUPER_TYPE_OF__CLASSIFIER = CLASS_LIKE_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Attributes</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS___GET_ATTRIBUTES = CLASS_LIKE_OPERATION_COUNT + 1;

	/**
	 * The number of operations of the '<em>Metaclass</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_OPERATION_COUNT = CLASS_LIKE_OPERATION_COUNT + 2;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.MetaclassMember <em>Metaclass Member</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.MetaclassMember
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMetaclassMember()
	 * @generated
	 */
	int METACLASS_MEMBER = 7;

	/**
	 * The feature id for the '<em><b>Class Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_MEMBER__CLASS_LIKE_CONTAINER = MEMBER_LIKE__CLASS_LIKE_CONTAINER;

	/**
	 * The number of structural features of the '<em>Metaclass Member</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_MEMBER_FEATURE_COUNT = MEMBER_LIKE_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_MEMBER___GET_NAME = MEMBER_LIKE___GET_NAME;

	/**
	 * The operation id for the '<em>Get Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_MEMBER___GET_TYPE = MEMBER_LIKE_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Metaclass Member</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_MEMBER_OPERATION_COUNT = MEMBER_LIKE_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.Attribute <em>Attribute</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.Attribute
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getAttribute()
	 * @generated
	 */
	int ATTRIBUTE = 8;

	/**
	 * The feature id for the '<em><b>Class Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE__CLASS_LIKE_CONTAINER = METACLASS_MEMBER__CLASS_LIKE_CONTAINER;

	/**
	 * The feature id for the '<em><b>Multi Valued</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE__MULTI_VALUED = METACLASS_MEMBER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Attribute</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_FEATURE_COUNT = METACLASS_MEMBER_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE___GET_NAME = METACLASS_MEMBER___GET_NAME;

	/**
	 * The operation id for the '<em>Get Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE___GET_TYPE = METACLASS_MEMBER___GET_TYPE;

	/**
	 * The number of operations of the '<em>Attribute</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATTRIBUTE_OPERATION_COUNT = METACLASS_MEMBER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.EClassMetaclassImpl <em>EClass Metaclass</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.EClassMetaclassImpl
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getEClassMetaclass()
	 * @generated
	 */
	int ECLASS_METACLASS = 9;

	/**
	 * The feature id for the '<em><b>Package Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS__PACKAGE_LIKE_CONTAINER = METACLASS__PACKAGE_LIKE_CONTAINER;

	/**
	 * The feature id for the '<em><b>Domain</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS__DOMAIN = METACLASS__DOMAIN;

	/**
	 * The feature id for the '<em><b>All Members</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS__ALL_MEMBERS = METACLASS__ALL_MEMBERS;

	/**
	 * The feature id for the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS__ABSTRACT = METACLASS__ABSTRACT;

	/**
	 * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS__ATTRIBUTES = METACLASS_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>EClass Metaclass</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS_FEATURE_COUNT = METACLASS_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS___GET_NAME = METACLASS___GET_NAME;

	/**
	 * The operation id for the '<em>Is Super Type Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS___IS_SUPER_TYPE_OF__CLASSIFIER = METACLASS___IS_SUPER_TYPE_OF__CLASSIFIER;

	/**
	 * The operation id for the '<em>Get Attributes</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS___GET_ATTRIBUTES = METACLASS___GET_ATTRIBUTES;

	/**
	 * The operation id for the '<em>For EClass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS___FOR_ECLASS__ECLASS = METACLASS_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>With Classifier Provider</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = METACLASS_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>From Domain</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS___FROM_DOMAIN__DOMAIN = METACLASS_OPERATION_COUNT + 2;

	/**
	 * The number of operations of the '<em>EClass Metaclass</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ECLASS_METACLASS_OPERATION_COUNT = METACLASS_OPERATION_COUNT + 3;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.EDataTypeClassifierImpl <em>EData Type Classifier</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.EDataTypeClassifierImpl
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getEDataTypeClassifier()
	 * @generated
	 */
	int EDATA_TYPE_CLASSIFIER = 10;

	/**
	 * The number of structural features of the '<em>EData Type Classifier</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDATA_TYPE_CLASSIFIER_FEATURE_COUNT = CLASSIFIER_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDATA_TYPE_CLASSIFIER___GET_NAME = CLASSIFIER___GET_NAME;

	/**
	 * The operation id for the '<em>Is Super Type Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDATA_TYPE_CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER = CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER;

	/**
	 * The operation id for the '<em>For EData Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDATA_TYPE_CLASSIFIER___FOR_EDATA_TYPE__EDATATYPE = CLASSIFIER_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>EData Type Classifier</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EDATA_TYPE_CLASSIFIER_OPERATION_COUNT = CLASSIFIER_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.MetamodelImpl <em>Metamodel</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.MetamodelImpl
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMetamodel()
	 * @generated
	 */
	int METAMODEL = 11;

	/**
	 * The feature id for the '<em><b>Metaclasses</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL__METACLASSES = DOMAIN_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Metamodel</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL_FEATURE_COUNT = DOMAIN_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL___GET_NAME = DOMAIN___GET_NAME;

	/**
	 * The operation id for the '<em>For EPackage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL___FOR_EPACKAGE__EPACKAGE = DOMAIN_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>With Classifier Provider</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = DOMAIN_OPERATION_COUNT + 1;

	/**
	 * The number of operations of the '<em>Metamodel</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL_OPERATION_COUNT = DOMAIN_OPERATION_COUNT + 2;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl <em>Resource Metaclass</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getResourceMetaclass()
	 * @generated
	 */
	int RESOURCE_METACLASS = 12;

	/**
	 * The feature id for the '<em><b>Package Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS__PACKAGE_LIKE_CONTAINER = METACLASS__PACKAGE_LIKE_CONTAINER;

	/**
	 * The feature id for the '<em><b>Domain</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS__DOMAIN = METACLASS__DOMAIN;

	/**
	 * The feature id for the '<em><b>All Members</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS__ALL_MEMBERS = METACLASS__ALL_MEMBERS;

	/**
	 * The feature id for the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS__ABSTRACT = METACLASS__ABSTRACT;

	/**
	 * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS__ATTRIBUTES = METACLASS_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Resource Metaclass</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS_FEATURE_COUNT = METACLASS_FEATURE_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS___GET_NAME = METACLASS___GET_NAME;

	/**
	 * The operation id for the '<em>Is Super Type Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS___IS_SUPER_TYPE_OF__CLASSIFIER = METACLASS___IS_SUPER_TYPE_OF__CLASSIFIER;

	/**
	 * The operation id for the '<em>Get Attributes</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS___GET_ATTRIBUTES = METACLASS___GET_ATTRIBUTES;

	/**
	 * The operation id for the '<em>With Classifier Provider</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = METACLASS_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>From Domain</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS___FROM_DOMAIN__DOMAIN = METACLASS_OPERATION_COUNT + 1;

	/**
	 * The number of operations of the '<em>Resource Metaclass</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_METACLASS_OPERATION_COUNT = METACLASS_OPERATION_COUNT + 2;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.EFeatureAttributeImpl <em>EFeature Attribute</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.EFeatureAttributeImpl
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getEFeatureAttribute()
	 * @generated
	 */
	int EFEATURE_ATTRIBUTE = 13;

	/**
	 * The feature id for the '<em><b>Class Like Container</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE__CLASS_LIKE_CONTAINER = ATTRIBUTE__CLASS_LIKE_CONTAINER;

	/**
	 * The feature id for the '<em><b>Multi Valued</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE__MULTI_VALUED = ATTRIBUTE__MULTI_VALUED;

	/**
	 * The number of structural features of the '<em>EFeature Attribute</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE_FEATURE_COUNT = ATTRIBUTE_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE___GET_NAME = ATTRIBUTE___GET_NAME;

	/**
	 * The operation id for the '<em>Get Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE___GET_TYPE = ATTRIBUTE___GET_TYPE;

	/**
	 * The operation id for the '<em>For EFeature</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE___FOR_EFEATURE__ESTRUCTURALFEATURE = ATTRIBUTE_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>With Classifier Provider</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = ATTRIBUTE_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>From Metaclass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE___FROM_METACLASS__METACLASS = ATTRIBUTE_OPERATION_COUNT + 2;

	/**
	 * The number of operations of the '<em>EFeature Attribute</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EFEATURE_ATTRIBUTE_OPERATION_COUNT = ATTRIBUTE_OPERATION_COUNT + 3;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.MostSpecificTypeImpl <em>Most Specific Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.MostSpecificTypeImpl
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMostSpecificType()
	 * @generated
	 */
	int MOST_SPECIFIC_TYPE = 14;

	/**
	 * The number of structural features of the '<em>Most Specific Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOST_SPECIFIC_TYPE_FEATURE_COUNT = CLASSIFIER_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOST_SPECIFIC_TYPE___GET_NAME = CLASSIFIER___GET_NAME;

	/**
	 * The operation id for the '<em>Is Super Type Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOST_SPECIFIC_TYPE___IS_SUPER_TYPE_OF__CLASSIFIER = CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER;

	/**
	 * The number of operations of the '<em>Most Specific Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MOST_SPECIFIC_TYPE_OPERATION_COUNT = CLASSIFIER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.LeastSpecificTypeImpl <em>Least Specific Type</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LeastSpecificTypeImpl
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getLeastSpecificType()
	 * @generated
	 */
	int LEAST_SPECIFIC_TYPE = 15;

	/**
	 * The number of structural features of the '<em>Least Specific Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAST_SPECIFIC_TYPE_FEATURE_COUNT = CLASSIFIER_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAST_SPECIFIC_TYPE___GET_NAME = CLASSIFIER___GET_NAME;

	/**
	 * The operation id for the '<em>Is Super Type Of</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAST_SPECIFIC_TYPE___IS_SUPER_TYPE_OF__CLASSIFIER = CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER;

	/**
	 * The number of operations of the '<em>Least Specific Type</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAST_SPECIFIC_TYPE_OPERATION_COUNT = CLASSIFIER_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '<em>Classifier Provider</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider
	 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getClassifierProvider()
	 * @generated
	 */
	int CLASSIFIER_PROVIDER = 16;


	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.PackageLike <em>Package Like</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Package Like</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.PackageLike
	 * @generated
	 */
	EClass getPackageLike();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.ClassLike <em>Class Like</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Class Like</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.ClassLike
	 * @generated
	 */
	EClass getClassLike();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.commonalities.language.elements.ClassLike#getPackageLikeContainer <em>Package Like Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Package Like Container</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.ClassLike#getPackageLikeContainer()
	 * @see #getClassLike()
	 * @generated
	 */
	EReference getClassLike_PackageLikeContainer();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.MemberLike <em>Member Like</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Member Like</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.MemberLike
	 * @generated
	 */
	EClass getMemberLike();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.commonalities.language.elements.MemberLike#getClassLikeContainer <em>Class Like Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Class Like Container</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.MemberLike#getClassLikeContainer()
	 * @see #getMemberLike()
	 * @generated
	 */
	EReference getMemberLike_ClassLikeContainer();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.NamedElement <em>Named Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Named Element</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.NamedElement
	 * @generated
	 */
	EClass getNamedElement();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.NamedElement#getName() <em>Get Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Name</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.NamedElement#getName()
	 * @generated
	 */
	EOperation getNamedElement__GetName();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.Domain <em>Domain</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Domain</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Domain
	 * @generated
	 */
	EClass getDomain();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.Classifier <em>Classifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Classifier</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Classifier
	 * @generated
	 */
	EClass getClassifier();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.Classifier#isSuperTypeOf(tools.vitruv.dsls.commonalities.language.elements.Classifier) <em>Is Super Type Of</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Super Type Of</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Classifier#isSuperTypeOf(tools.vitruv.dsls.commonalities.language.elements.Classifier)
	 * @generated
	 */
	EOperation getClassifier__IsSuperTypeOf__Classifier();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass <em>Metaclass</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metaclass</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metaclass
	 * @generated
	 */
	EClass getMetaclass();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass#getDomain <em>Domain</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Domain</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metaclass#getDomain()
	 * @see #getMetaclass()
	 * @generated
	 */
	EReference getMetaclass_Domain();

	/**
	 * Returns the meta object for the reference list '{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass#getAllMembers <em>All Members</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>All Members</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metaclass#getAllMembers()
	 * @see #getMetaclass()
	 * @generated
	 */
	EReference getMetaclass_AllMembers();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass#isAbstract <em>Abstract</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Abstract</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metaclass#isAbstract()
	 * @see #getMetaclass()
	 * @generated
	 */
	EAttribute getMetaclass_Abstract();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass#getAttributes() <em>Get Attributes</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Attributes</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metaclass#getAttributes()
	 * @generated
	 */
	EOperation getMetaclass__GetAttributes();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.MetaclassMember <em>Metaclass Member</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metaclass Member</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.MetaclassMember
	 * @generated
	 */
	EClass getMetaclassMember();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.MetaclassMember#getType() <em>Get Type</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Type</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.MetaclassMember#getType()
	 * @generated
	 */
	EOperation getMetaclassMember__GetType();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.Attribute <em>Attribute</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Attribute</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Attribute
	 * @generated
	 */
	EClass getAttribute();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.language.elements.Attribute#isMultiValued <em>Multi Valued</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Multi Valued</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Attribute#isMultiValued()
	 * @see #getAttribute()
	 * @generated
	 */
	EAttribute getAttribute_MultiValued();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass <em>EClass Metaclass</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EClass Metaclass</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass
	 * @generated
	 */
	EClass getEClassMetaclass();

	/**
	 * Returns the meta object for the containment reference list '{@link tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#getAttributes <em>Attributes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Attributes</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#getAttributes()
	 * @see #getEClassMetaclass()
	 * @generated
	 */
	EReference getEClassMetaclass_Attributes();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#forEClass(org.eclipse.emf.ecore.EClass) <em>For EClass</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>For EClass</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#forEClass(org.eclipse.emf.ecore.EClass)
	 * @generated
	 */
	EOperation getEClassMetaclass__ForEClass__EClass();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider) <em>With Classifier Provider</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>With Classifier Provider</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider)
	 * @generated
	 */
	EOperation getEClassMetaclass__WithClassifierProvider__ClassifierProvider();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#fromDomain(tools.vitruv.dsls.commonalities.language.elements.Domain) <em>From Domain</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>From Domain</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EClassMetaclass#fromDomain(tools.vitruv.dsls.commonalities.language.elements.Domain)
	 * @generated
	 */
	EOperation getEClassMetaclass__FromDomain__Domain();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.EDataTypeClassifier <em>EData Type Classifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EData Type Classifier</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EDataTypeClassifier
	 * @generated
	 */
	EClass getEDataTypeClassifier();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.EDataTypeClassifier#forEDataType(org.eclipse.emf.ecore.EDataType) <em>For EData Type</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>For EData Type</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EDataTypeClassifier#forEDataType(org.eclipse.emf.ecore.EDataType)
	 * @generated
	 */
	EOperation getEDataTypeClassifier__ForEDataType__EDataType();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.Metamodel <em>Metamodel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metamodel</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metamodel
	 * @generated
	 */
	EClass getMetamodel();

	/**
	 * Returns the meta object for the containment reference list '{@link tools.vitruv.dsls.commonalities.language.elements.Metamodel#getMetaclasses <em>Metaclasses</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Metaclasses</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metamodel#getMetaclasses()
	 * @see #getMetamodel()
	 * @generated
	 */
	EReference getMetamodel_Metaclasses();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.Metamodel#forEPackage(org.eclipse.emf.ecore.EPackage) <em>For EPackage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>For EPackage</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metamodel#forEPackage(org.eclipse.emf.ecore.EPackage)
	 * @generated
	 */
	EOperation getMetamodel__ForEPackage__EPackage();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.Metamodel#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider) <em>With Classifier Provider</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>With Classifier Provider</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.Metamodel#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider)
	 * @generated
	 */
	EOperation getMetamodel__WithClassifierProvider__ClassifierProvider();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass <em>Resource Metaclass</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Resource Metaclass</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass
	 * @generated
	 */
	EClass getResourceMetaclass();

	/**
	 * Returns the meta object for the containment reference list '{@link tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass#getAttributes <em>Attributes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Attributes</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass#getAttributes()
	 * @see #getResourceMetaclass()
	 * @generated
	 */
	EReference getResourceMetaclass_Attributes();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider) <em>With Classifier Provider</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>With Classifier Provider</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider)
	 * @generated
	 */
	EOperation getResourceMetaclass__WithClassifierProvider__ClassifierProvider();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass#fromDomain(tools.vitruv.dsls.commonalities.language.elements.Domain) <em>From Domain</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>From Domain</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass#fromDomain(tools.vitruv.dsls.commonalities.language.elements.Domain)
	 * @generated
	 */
	EOperation getResourceMetaclass__FromDomain__Domain();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute <em>EFeature Attribute</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EFeature Attribute</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute
	 * @generated
	 */
	EClass getEFeatureAttribute();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute#forEFeature(org.eclipse.emf.ecore.EStructuralFeature) <em>For EFeature</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>For EFeature</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute#forEFeature(org.eclipse.emf.ecore.EStructuralFeature)
	 * @generated
	 */
	EOperation getEFeatureAttribute__ForEFeature__EStructuralFeature();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider) <em>With Classifier Provider</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>With Classifier Provider</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute#withClassifierProvider(tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider)
	 * @generated
	 */
	EOperation getEFeatureAttribute__WithClassifierProvider__ClassifierProvider();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute#fromMetaclass(tools.vitruv.dsls.commonalities.language.elements.Metaclass) <em>From Metaclass</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>From Metaclass</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute#fromMetaclass(tools.vitruv.dsls.commonalities.language.elements.Metaclass)
	 * @generated
	 */
	EOperation getEFeatureAttribute__FromMetaclass__Metaclass();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.MostSpecificType <em>Most Specific Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Most Specific Type</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.MostSpecificType
	 * @generated
	 */
	EClass getMostSpecificType();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType <em>Least Specific Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Least Specific Type</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType
	 * @generated
	 */
	EClass getLeastSpecificType();

	/**
	 * Returns the meta object for data type '{@link tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider <em>Classifier Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Classifier Provider</em>'.
	 * @see tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider
	 * @model instanceClass="tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider"
	 * @generated
	 */
	EDataType getClassifierProvider();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	LanguageElementsFactory getLanguageElementsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals
	{
		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.PackageLike <em>Package Like</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.PackageLike
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getPackageLike()
		 * @generated
		 */
		EClass PACKAGE_LIKE = eINSTANCE.getPackageLike();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.ClassLike <em>Class Like</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.ClassLike
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getClassLike()
		 * @generated
		 */
		EClass CLASS_LIKE = eINSTANCE.getClassLike();

		/**
		 * The meta object literal for the '<em><b>Package Like Container</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLASS_LIKE__PACKAGE_LIKE_CONTAINER = eINSTANCE.getClassLike_PackageLikeContainer();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.MemberLike <em>Member Like</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.MemberLike
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMemberLike()
		 * @generated
		 */
		EClass MEMBER_LIKE = eINSTANCE.getMemberLike();

		/**
		 * The meta object literal for the '<em><b>Class Like Container</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MEMBER_LIKE__CLASS_LIKE_CONTAINER = eINSTANCE.getMemberLike_ClassLikeContainer();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.NamedElement <em>Named Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.NamedElement
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getNamedElement()
		 * @generated
		 */
		EClass NAMED_ELEMENT = eINSTANCE.getNamedElement();

		/**
		 * The meta object literal for the '<em><b>Get Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation NAMED_ELEMENT___GET_NAME = eINSTANCE.getNamedElement__GetName();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.Domain <em>Domain</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.Domain
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getDomain()
		 * @generated
		 */
		EClass DOMAIN = eINSTANCE.getDomain();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.Classifier <em>Classifier</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.Classifier
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getClassifier()
		 * @generated
		 */
		EClass CLASSIFIER = eINSTANCE.getClassifier();

		/**
		 * The meta object literal for the '<em><b>Is Super Type Of</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER = eINSTANCE.getClassifier__IsSuperTypeOf__Classifier();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.Metaclass <em>Metaclass</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.Metaclass
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMetaclass()
		 * @generated
		 */
		EClass METACLASS = eINSTANCE.getMetaclass();

		/**
		 * The meta object literal for the '<em><b>Domain</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METACLASS__DOMAIN = eINSTANCE.getMetaclass_Domain();

		/**
		 * The meta object literal for the '<em><b>All Members</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METACLASS__ALL_MEMBERS = eINSTANCE.getMetaclass_AllMembers();

		/**
		 * The meta object literal for the '<em><b>Abstract</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute METACLASS__ABSTRACT = eINSTANCE.getMetaclass_Abstract();

		/**
		 * The meta object literal for the '<em><b>Get Attributes</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation METACLASS___GET_ATTRIBUTES = eINSTANCE.getMetaclass__GetAttributes();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.MetaclassMember <em>Metaclass Member</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.MetaclassMember
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMetaclassMember()
		 * @generated
		 */
		EClass METACLASS_MEMBER = eINSTANCE.getMetaclassMember();

		/**
		 * The meta object literal for the '<em><b>Get Type</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation METACLASS_MEMBER___GET_TYPE = eINSTANCE.getMetaclassMember__GetType();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.Attribute <em>Attribute</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.Attribute
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getAttribute()
		 * @generated
		 */
		EClass ATTRIBUTE = eINSTANCE.getAttribute();

		/**
		 * The meta object literal for the '<em><b>Multi Valued</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATTRIBUTE__MULTI_VALUED = eINSTANCE.getAttribute_MultiValued();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.EClassMetaclassImpl <em>EClass Metaclass</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.EClassMetaclassImpl
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getEClassMetaclass()
		 * @generated
		 */
		EClass ECLASS_METACLASS = eINSTANCE.getEClassMetaclass();

		/**
		 * The meta object literal for the '<em><b>Attributes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ECLASS_METACLASS__ATTRIBUTES = eINSTANCE.getEClassMetaclass_Attributes();

		/**
		 * The meta object literal for the '<em><b>For EClass</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation ECLASS_METACLASS___FOR_ECLASS__ECLASS = eINSTANCE.getEClassMetaclass__ForEClass__EClass();

		/**
		 * The meta object literal for the '<em><b>With Classifier Provider</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation ECLASS_METACLASS___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = eINSTANCE.getEClassMetaclass__WithClassifierProvider__ClassifierProvider();

		/**
		 * The meta object literal for the '<em><b>From Domain</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation ECLASS_METACLASS___FROM_DOMAIN__DOMAIN = eINSTANCE.getEClassMetaclass__FromDomain__Domain();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.EDataTypeClassifierImpl <em>EData Type Classifier</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.EDataTypeClassifierImpl
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getEDataTypeClassifier()
		 * @generated
		 */
		EClass EDATA_TYPE_CLASSIFIER = eINSTANCE.getEDataTypeClassifier();

		/**
		 * The meta object literal for the '<em><b>For EData Type</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EDATA_TYPE_CLASSIFIER___FOR_EDATA_TYPE__EDATATYPE = eINSTANCE.getEDataTypeClassifier__ForEDataType__EDataType();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.MetamodelImpl <em>Metamodel</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.MetamodelImpl
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMetamodel()
		 * @generated
		 */
		EClass METAMODEL = eINSTANCE.getMetamodel();

		/**
		 * The meta object literal for the '<em><b>Metaclasses</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METAMODEL__METACLASSES = eINSTANCE.getMetamodel_Metaclasses();

		/**
		 * The meta object literal for the '<em><b>For EPackage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation METAMODEL___FOR_EPACKAGE__EPACKAGE = eINSTANCE.getMetamodel__ForEPackage__EPackage();

		/**
		 * The meta object literal for the '<em><b>With Classifier Provider</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation METAMODEL___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = eINSTANCE.getMetamodel__WithClassifierProvider__ClassifierProvider();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl <em>Resource Metaclass</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getResourceMetaclass()
		 * @generated
		 */
		EClass RESOURCE_METACLASS = eINSTANCE.getResourceMetaclass();

		/**
		 * The meta object literal for the '<em><b>Attributes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RESOURCE_METACLASS__ATTRIBUTES = eINSTANCE.getResourceMetaclass_Attributes();

		/**
		 * The meta object literal for the '<em><b>With Classifier Provider</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation RESOURCE_METACLASS___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = eINSTANCE.getResourceMetaclass__WithClassifierProvider__ClassifierProvider();

		/**
		 * The meta object literal for the '<em><b>From Domain</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation RESOURCE_METACLASS___FROM_DOMAIN__DOMAIN = eINSTANCE.getResourceMetaclass__FromDomain__Domain();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.EFeatureAttributeImpl <em>EFeature Attribute</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.EFeatureAttributeImpl
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getEFeatureAttribute()
		 * @generated
		 */
		EClass EFEATURE_ATTRIBUTE = eINSTANCE.getEFeatureAttribute();

		/**
		 * The meta object literal for the '<em><b>For EFeature</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EFEATURE_ATTRIBUTE___FOR_EFEATURE__ESTRUCTURALFEATURE = eINSTANCE.getEFeatureAttribute__ForEFeature__EStructuralFeature();

		/**
		 * The meta object literal for the '<em><b>With Classifier Provider</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EFEATURE_ATTRIBUTE___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER = eINSTANCE.getEFeatureAttribute__WithClassifierProvider__ClassifierProvider();

		/**
		 * The meta object literal for the '<em><b>From Metaclass</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EFEATURE_ATTRIBUTE___FROM_METACLASS__METACLASS = eINSTANCE.getEFeatureAttribute__FromMetaclass__Metaclass();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.MostSpecificTypeImpl <em>Most Specific Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.MostSpecificTypeImpl
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getMostSpecificType()
		 * @generated
		 */
		EClass MOST_SPECIFIC_TYPE = eINSTANCE.getMostSpecificType();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.language.elements.impl.LeastSpecificTypeImpl <em>Least Specific Type</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LeastSpecificTypeImpl
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getLeastSpecificType()
		 * @generated
		 */
		EClass LEAST_SPECIFIC_TYPE = eINSTANCE.getLeastSpecificType();

		/**
		 * The meta object literal for the '<em>Classifier Provider</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider
		 * @see tools.vitruv.dsls.commonalities.language.elements.impl.LanguageElementsPackageImpl#getClassifierProvider()
		 * @generated
		 */
		EDataType CLASSIFIER_PROVIDER = eINSTANCE.getClassifierProvider();

	}

} //LanguageElementsPackage
