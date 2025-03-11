/**
 */
package tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase;

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
 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBaseFactory
 * @model kind="package"
 * @generated
 */
public interface IntermediateModelBasePackage extends EPackage
{
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "intermediatemodelbase";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://vitruv.tools/metamodels/dsls/commonalities/runtime/intermediatemodelbase";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "base";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	IntermediateModelBasePackage eINSTANCE = tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateModelBasePackageImpl.init();

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.RootImpl <em>Root</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.RootImpl
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateModelBasePackageImpl#getRoot()
	 * @generated
	 */
	int ROOT = 0;

	/**
	 * The feature id for the '<em><b>Intermediates</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT__INTERMEDIATES = 0;

	/**
	 * The feature id for the '<em><b>Resource Bridges</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT__RESOURCE_BRIDGES = 1;

	/**
	 * The feature id for the '<em><b>Intermediate Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT__INTERMEDIATE_ID = 2;

	/**
	 * The number of structural features of the '<em>Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Root</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROOT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateImpl <em>Intermediate</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateImpl
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateModelBasePackageImpl#getIntermediate()
	 * @generated
	 */
	int INTERMEDIATE = 1;

	/**
	 * The feature id for the '<em><b>Intermediate Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE__INTERMEDIATE_ID = 0;

	/**
	 * The number of structural features of the '<em>Intermediate</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Intermediate</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root <em>Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Root</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root
	 * @generated
	 */
	EClass getRoot();

	/**
	 * Returns the meta object for the containment reference list '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getIntermediates <em>Intermediates</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Intermediates</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getIntermediates()
	 * @see #getRoot()
	 * @generated
	 */
	EReference getRoot_Intermediates();

	/**
	 * Returns the meta object for the containment reference list '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getResourceBridges <em>Resource Bridges</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Resource Bridges</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getResourceBridges()
	 * @see #getRoot()
	 * @generated
	 */
	EReference getRoot_ResourceBridges();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getIntermediateId <em>Intermediate Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Intermediate Id</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root#getIntermediateId()
	 * @see #getRoot()
	 * @generated
	 */
	EAttribute getRoot_IntermediateId();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate <em>Intermediate</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Intermediate</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate
	 * @generated
	 */
	EClass getIntermediate();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate#getIntermediateId <em>Intermediate Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Intermediate Id</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Intermediate#getIntermediateId()
	 * @see #getIntermediate()
	 * @generated
	 */
	EAttribute getIntermediate_IntermediateId();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	IntermediateModelBaseFactory getIntermediateModelBaseFactory();

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
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.RootImpl <em>Root</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.RootImpl
		 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateModelBasePackageImpl#getRoot()
		 * @generated
		 */
		EClass ROOT = eINSTANCE.getRoot();

		/**
		 * The meta object literal for the '<em><b>Intermediates</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ROOT__INTERMEDIATES = eINSTANCE.getRoot_Intermediates();

		/**
		 * The meta object literal for the '<em><b>Resource Bridges</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ROOT__RESOURCE_BRIDGES = eINSTANCE.getRoot_ResourceBridges();

		/**
		 * The meta object literal for the '<em><b>Intermediate Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROOT__INTERMEDIATE_ID = eINSTANCE.getRoot_IntermediateId();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateImpl <em>Intermediate</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateImpl
		 * @see tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl.IntermediateModelBasePackageImpl#getIntermediate()
		 * @generated
		 */
		EClass INTERMEDIATE = eINSTANCE.getIntermediate();

		/**
		 * The meta object literal for the '<em><b>Intermediate Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE__INTERMEDIATE_ID = eINSTANCE.getIntermediate_IntermediateId();

	}

} //IntermediateModelBasePackage
