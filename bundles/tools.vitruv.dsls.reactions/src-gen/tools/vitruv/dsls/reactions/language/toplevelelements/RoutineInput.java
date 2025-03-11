/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import tools.vitruv.dsls.common.elements.NamedMetaclassReference;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Routine Input</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.RoutineInput#getModelInputElements <em>Model Input Elements</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.RoutineInput#getJavaInputElements <em>Java Input Elements</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutineInput()
 * @model
 * @generated
 */
public interface RoutineInput extends EObject
{
	/**
	 * Returns the value of the '<em><b>Model Input Elements</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.common.elements.NamedMetaclassReference}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Model Input Elements</em>' containment reference list.
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutineInput_ModelInputElements()
	 * @model containment="true"
	 * @generated
	 */
	EList<NamedMetaclassReference> getModelInputElements();

	/**
	 * Returns the value of the '<em><b>Java Input Elements</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Java Input Elements</em>' containment reference list.
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getRoutineInput_JavaInputElements()
	 * @model containment="true"
	 * @generated
	 */
	EList<NamedJavaElementReference> getJavaInputElements();

} // RoutineInput
