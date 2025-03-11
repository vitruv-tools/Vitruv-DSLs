/**
 */
package tools.vitruv.dsls.reactions.runtime.correspondence;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondenceFactory
 * @model kind="package"
 * @generated
 */
public interface CorrespondencePackage extends EPackage
{
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "correspondence";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "correspondence";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CorrespondencePackage eINSTANCE = tools.vitruv.dsls.reactions.runtime.correspondence.impl.CorrespondencePackageImpl.init();

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.reactions.runtime.correspondence.impl.ReactionsCorrespondenceImpl <em>Reactions Correspondence</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.reactions.runtime.correspondence.impl.ReactionsCorrespondenceImpl
	 * @see tools.vitruv.dsls.reactions.runtime.correspondence.impl.CorrespondencePackageImpl#getReactionsCorrespondence()
	 * @generated
	 */
	int REACTIONS_CORRESPONDENCE = 0;

	/**
	 * The feature id for the '<em><b>Tag</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REACTIONS_CORRESPONDENCE__TAG = tools.vitruv.change.correspondence.CorrespondencePackage.CORRESPONDENCE__TAG;

	/**
	 * The feature id for the '<em><b>Left EObjects</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REACTIONS_CORRESPONDENCE__LEFT_EOBJECTS = tools.vitruv.change.correspondence.CorrespondencePackage.CORRESPONDENCE__LEFT_EOBJECTS;

	/**
	 * The feature id for the '<em><b>Right EObjects</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REACTIONS_CORRESPONDENCE__RIGHT_EOBJECTS = tools.vitruv.change.correspondence.CorrespondencePackage.CORRESPONDENCE__RIGHT_EOBJECTS;

	/**
	 * The number of structural features of the '<em>Reactions Correspondence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REACTIONS_CORRESPONDENCE_FEATURE_COUNT = tools.vitruv.change.correspondence.CorrespondencePackage.CORRESPONDENCE_FEATURE_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence <em>Reactions Correspondence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Reactions Correspondence</em>'.
	 * @see tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence
	 * @generated
	 */
	EClass getReactionsCorrespondence();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	CorrespondenceFactory getCorrespondenceFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals
	{
		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.reactions.runtime.correspondence.impl.ReactionsCorrespondenceImpl <em>Reactions Correspondence</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.reactions.runtime.correspondence.impl.ReactionsCorrespondenceImpl
		 * @see tools.vitruv.dsls.reactions.runtime.correspondence.impl.CorrespondencePackageImpl#getReactionsCorrespondence()
		 * @generated
		 */
		EClass REACTIONS_CORRESPONDENCE = eINSTANCE.getReactionsCorrespondence();

	}

} //CorrespondencePackage
