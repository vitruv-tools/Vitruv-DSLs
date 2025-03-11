/**
 */
package tools.vitruv.dsls.reactions.runtime.correspondence;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondencePackage
 * @generated
 */
public interface CorrespondenceFactory extends EFactory
{
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	CorrespondenceFactory eINSTANCE = tools.vitruv.dsls.reactions.runtime.correspondence.impl.CorrespondenceFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Reactions Correspondence</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reactions Correspondence</em>'.
	 * @generated
	 */
	ReactionsCorrespondence createReactionsCorrespondence();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	CorrespondencePackage getCorrespondencePackage();

} //CorrespondenceFactory
