/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.xtext.xbase.XExpression;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Trigger</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.Trigger#getPrecondition <em>Precondition</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getTrigger()
 * @model
 * @generated
 */
public interface Trigger extends EObject
{
	/**
	 * Returns the value of the '<em><b>Precondition</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Precondition</em>' containment reference.
	 * @see #setPrecondition(XExpression)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getTrigger_Precondition()
	 * @model containment="true"
	 * @generated
	 */
	XExpression getPrecondition();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.Trigger#getPrecondition <em>Precondition</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Precondition</em>' containment reference.
	 * @see #getPrecondition()
	 * @generated
	 */
	void setPrecondition(XExpression value);

} // Trigger
