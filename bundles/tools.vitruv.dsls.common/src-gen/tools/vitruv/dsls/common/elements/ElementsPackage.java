/**
 */
package tools.vitruv.dsls.common.elements;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
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
 * @see tools.vitruv.dsls.common.elements.ElementsFactory
 * @model kind="package"
 * @generated
 */
public interface ElementsPackage extends EPackage
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
	String eNS_URI = "http://vitruv.tools/dsls/common/elements";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "elements";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ElementsPackage eINSTANCE = tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl.init();

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.common.elements.impl.MetamodelImportImpl <em>Metamodel Import</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.common.elements.impl.MetamodelImportImpl
	 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetamodelImport()
	 * @generated
	 */
	int METAMODEL_IMPORT = 0;

	/**
	 * The feature id for the '<em><b>Package</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL_IMPORT__PACKAGE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL_IMPORT__NAME = 1;

	/**
	 * The feature id for the '<em><b>Use Qualified Names</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL_IMPORT__USE_QUALIFIED_NAMES = 2;

	/**
	 * The number of structural features of the '<em>Metamodel Import</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL_IMPORT_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Metamodel Import</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METAMODEL_IMPORT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassReferenceImpl <em>Metaclass Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.common.elements.impl.MetaclassReferenceImpl
	 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassReference()
	 * @generated
	 */
	int METACLASS_REFERENCE = 1;

	/**
	 * The feature id for the '<em><b>Metamodel</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_REFERENCE__METAMODEL = 0;

	/**
	 * The feature id for the '<em><b>Metaclass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_REFERENCE__METACLASS = 1;

	/**
	 * The number of structural features of the '<em>Metaclass Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_REFERENCE_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Metaclass Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_REFERENCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.common.elements.impl.NamedMetaclassReferenceImpl <em>Named Metaclass Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.common.elements.impl.NamedMetaclassReferenceImpl
	 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getNamedMetaclassReference()
	 * @generated
	 */
	int NAMED_METACLASS_REFERENCE = 2;

	/**
	 * The feature id for the '<em><b>Metamodel</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_METACLASS_REFERENCE__METAMODEL = METACLASS_REFERENCE__METAMODEL;

	/**
	 * The feature id for the '<em><b>Metaclass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_METACLASS_REFERENCE__METACLASS = METACLASS_REFERENCE__METACLASS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_METACLASS_REFERENCE__NAME = METACLASS_REFERENCE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Named Metaclass Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_METACLASS_REFERENCE_FEATURE_COUNT = METACLASS_REFERENCE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Named Metaclass Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int NAMED_METACLASS_REFERENCE_OPERATION_COUNT = METACLASS_REFERENCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassFeatureReferenceImpl <em>Metaclass Feature Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.common.elements.impl.MetaclassFeatureReferenceImpl
	 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassFeatureReference()
	 * @generated
	 */
	int METACLASS_FEATURE_REFERENCE = 3;

	/**
	 * The feature id for the '<em><b>Metamodel</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_FEATURE_REFERENCE__METAMODEL = METACLASS_REFERENCE__METAMODEL;

	/**
	 * The feature id for the '<em><b>Metaclass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_FEATURE_REFERENCE__METACLASS = METACLASS_REFERENCE__METACLASS;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_FEATURE_REFERENCE__FEATURE = METACLASS_REFERENCE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Metaclass Feature Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_FEATURE_REFERENCE_FEATURE_COUNT = METACLASS_REFERENCE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Metaclass Feature Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_FEATURE_REFERENCE_OPERATION_COUNT = METACLASS_REFERENCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassEAttributeReferenceImpl <em>Metaclass EAttribute Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.common.elements.impl.MetaclassEAttributeReferenceImpl
	 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassEAttributeReference()
	 * @generated
	 */
	int METACLASS_EATTRIBUTE_REFERENCE = 4;

	/**
	 * The feature id for the '<em><b>Metamodel</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EATTRIBUTE_REFERENCE__METAMODEL = METACLASS_REFERENCE__METAMODEL;

	/**
	 * The feature id for the '<em><b>Metaclass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EATTRIBUTE_REFERENCE__METACLASS = METACLASS_REFERENCE__METACLASS;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EATTRIBUTE_REFERENCE__FEATURE = METACLASS_REFERENCE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Metaclass EAttribute Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EATTRIBUTE_REFERENCE_FEATURE_COUNT = METACLASS_REFERENCE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Metaclass EAttribute Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EATTRIBUTE_REFERENCE_OPERATION_COUNT = METACLASS_REFERENCE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassEReferenceReferenceImpl <em>Metaclass EReference Reference</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.common.elements.impl.MetaclassEReferenceReferenceImpl
	 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassEReferenceReference()
	 * @generated
	 */
	int METACLASS_EREFERENCE_REFERENCE = 5;

	/**
	 * The feature id for the '<em><b>Metamodel</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EREFERENCE_REFERENCE__METAMODEL = METACLASS_REFERENCE__METAMODEL;

	/**
	 * The feature id for the '<em><b>Metaclass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EREFERENCE_REFERENCE__METACLASS = METACLASS_REFERENCE__METACLASS;

	/**
	 * The feature id for the '<em><b>Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EREFERENCE_REFERENCE__FEATURE = METACLASS_REFERENCE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Metaclass EReference Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EREFERENCE_REFERENCE_FEATURE_COUNT = METACLASS_REFERENCE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Metaclass EReference Reference</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int METACLASS_EREFERENCE_REFERENCE_OPERATION_COUNT = METACLASS_REFERENCE_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.common.elements.MetamodelImport <em>Metamodel Import</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metamodel Import</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetamodelImport
	 * @generated
	 */
	EClass getMetamodelImport();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.common.elements.MetamodelImport#getPackage <em>Package</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Package</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetamodelImport#getPackage()
	 * @see #getMetamodelImport()
	 * @generated
	 */
	EReference getMetamodelImport_Package();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.common.elements.MetamodelImport#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetamodelImport#getName()
	 * @see #getMetamodelImport()
	 * @generated
	 */
	EAttribute getMetamodelImport_Name();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.common.elements.MetamodelImport#isUseQualifiedNames <em>Use Qualified Names</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Use Qualified Names</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetamodelImport#isUseQualifiedNames()
	 * @see #getMetamodelImport()
	 * @generated
	 */
	EAttribute getMetamodelImport_UseQualifiedNames();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.common.elements.MetaclassReference <em>Metaclass Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metaclass Reference</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassReference
	 * @generated
	 */
	EClass getMetaclassReference();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.common.elements.MetaclassReference#getMetamodel <em>Metamodel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Metamodel</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassReference#getMetamodel()
	 * @see #getMetaclassReference()
	 * @generated
	 */
	EReference getMetaclassReference_Metamodel();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.common.elements.MetaclassReference#getMetaclass <em>Metaclass</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Metaclass</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassReference#getMetaclass()
	 * @see #getMetaclassReference()
	 * @generated
	 */
	EReference getMetaclassReference_Metaclass();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.common.elements.NamedMetaclassReference <em>Named Metaclass Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Named Metaclass Reference</em>'.
	 * @see tools.vitruv.dsls.common.elements.NamedMetaclassReference
	 * @generated
	 */
	EClass getNamedMetaclassReference();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.common.elements.NamedMetaclassReference#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see tools.vitruv.dsls.common.elements.NamedMetaclassReference#getName()
	 * @see #getNamedMetaclassReference()
	 * @generated
	 */
	EAttribute getNamedMetaclassReference_Name();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.common.elements.MetaclassFeatureReference <em>Metaclass Feature Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metaclass Feature Reference</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassFeatureReference
	 * @generated
	 */
	EClass getMetaclassFeatureReference();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.common.elements.MetaclassFeatureReference#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassFeatureReference#getFeature()
	 * @see #getMetaclassFeatureReference()
	 * @generated
	 */
	EReference getMetaclassFeatureReference_Feature();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.common.elements.MetaclassEAttributeReference <em>Metaclass EAttribute Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metaclass EAttribute Reference</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassEAttributeReference
	 * @generated
	 */
	EClass getMetaclassEAttributeReference();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.common.elements.MetaclassEAttributeReference#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassEAttributeReference#getFeature()
	 * @see #getMetaclassEAttributeReference()
	 * @generated
	 */
	EReference getMetaclassEAttributeReference_Feature();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.common.elements.MetaclassEReferenceReference <em>Metaclass EReference Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Metaclass EReference Reference</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassEReferenceReference
	 * @generated
	 */
	EClass getMetaclassEReferenceReference();

	/**
	 * Returns the meta object for the reference '{@link tools.vitruv.dsls.common.elements.MetaclassEReferenceReference#getFeature <em>Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Feature</em>'.
	 * @see tools.vitruv.dsls.common.elements.MetaclassEReferenceReference#getFeature()
	 * @see #getMetaclassEReferenceReference()
	 * @generated
	 */
	EReference getMetaclassEReferenceReference_Feature();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ElementsFactory getElementsFactory();

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
		 * The meta object literal for the '{@link tools.vitruv.dsls.common.elements.impl.MetamodelImportImpl <em>Metamodel Import</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.common.elements.impl.MetamodelImportImpl
		 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetamodelImport()
		 * @generated
		 */
		EClass METAMODEL_IMPORT = eINSTANCE.getMetamodelImport();

		/**
		 * The meta object literal for the '<em><b>Package</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METAMODEL_IMPORT__PACKAGE = eINSTANCE.getMetamodelImport_Package();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute METAMODEL_IMPORT__NAME = eINSTANCE.getMetamodelImport_Name();

		/**
		 * The meta object literal for the '<em><b>Use Qualified Names</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute METAMODEL_IMPORT__USE_QUALIFIED_NAMES = eINSTANCE.getMetamodelImport_UseQualifiedNames();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassReferenceImpl <em>Metaclass Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.common.elements.impl.MetaclassReferenceImpl
		 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassReference()
		 * @generated
		 */
		EClass METACLASS_REFERENCE = eINSTANCE.getMetaclassReference();

		/**
		 * The meta object literal for the '<em><b>Metamodel</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METACLASS_REFERENCE__METAMODEL = eINSTANCE.getMetaclassReference_Metamodel();

		/**
		 * The meta object literal for the '<em><b>Metaclass</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METACLASS_REFERENCE__METACLASS = eINSTANCE.getMetaclassReference_Metaclass();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.common.elements.impl.NamedMetaclassReferenceImpl <em>Named Metaclass Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.common.elements.impl.NamedMetaclassReferenceImpl
		 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getNamedMetaclassReference()
		 * @generated
		 */
		EClass NAMED_METACLASS_REFERENCE = eINSTANCE.getNamedMetaclassReference();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute NAMED_METACLASS_REFERENCE__NAME = eINSTANCE.getNamedMetaclassReference_Name();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassFeatureReferenceImpl <em>Metaclass Feature Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.common.elements.impl.MetaclassFeatureReferenceImpl
		 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassFeatureReference()
		 * @generated
		 */
		EClass METACLASS_FEATURE_REFERENCE = eINSTANCE.getMetaclassFeatureReference();

		/**
		 * The meta object literal for the '<em><b>Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METACLASS_FEATURE_REFERENCE__FEATURE = eINSTANCE.getMetaclassFeatureReference_Feature();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassEAttributeReferenceImpl <em>Metaclass EAttribute Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.common.elements.impl.MetaclassEAttributeReferenceImpl
		 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassEAttributeReference()
		 * @generated
		 */
		EClass METACLASS_EATTRIBUTE_REFERENCE = eINSTANCE.getMetaclassEAttributeReference();

		/**
		 * The meta object literal for the '<em><b>Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METACLASS_EATTRIBUTE_REFERENCE__FEATURE = eINSTANCE.getMetaclassEAttributeReference_Feature();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.common.elements.impl.MetaclassEReferenceReferenceImpl <em>Metaclass EReference Reference</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.common.elements.impl.MetaclassEReferenceReferenceImpl
		 * @see tools.vitruv.dsls.common.elements.impl.ElementsPackageImpl#getMetaclassEReferenceReference()
		 * @generated
		 */
		EClass METACLASS_EREFERENCE_REFERENCE = eINSTANCE.getMetaclassEReferenceReference();

		/**
		 * The meta object literal for the '<em><b>Feature</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference METACLASS_EREFERENCE_REFERENCE__FEATURE = eINSTANCE.getMetaclassEReferenceReference_Feature();

	}

} //ElementsPackage
