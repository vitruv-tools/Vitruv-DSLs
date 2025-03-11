/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import tools.vitruv.dsls.common.elements.NamedMetaclassReference;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Create Block</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock#getCreateStatements <em>Create Statements</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getCreateBlock()
 * @model
 * @generated
 */
public interface CreateBlock extends EObject
{
	/**
	 * Returns the value of the '<em><b>Create Statements</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.common.elements.NamedMetaclassReference}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Create Statements</em>' containment reference list.
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getCreateBlock_CreateStatements()
	 * @model containment="true"
	 * @generated
	 */
	EList<NamedMetaclassReference> getCreateStatements();

} // CreateBlock
