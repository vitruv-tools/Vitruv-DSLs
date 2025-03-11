/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Match Block</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock#getMatchStatements <em>Match Statements</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getMatchBlock()
 * @model
 * @generated
 */
public interface MatchBlock extends EObject
{
	/**
	 * Returns the value of the '<em><b>Match Statements</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.reactions.language.toplevelelements.MatchStatement}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Match Statements</em>' containment reference list.
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getMatchBlock_MatchStatements()
	 * @model containment="true"
	 * @generated
	 */
	EList<MatchStatement> getMatchStatements();

} // MatchBlock
