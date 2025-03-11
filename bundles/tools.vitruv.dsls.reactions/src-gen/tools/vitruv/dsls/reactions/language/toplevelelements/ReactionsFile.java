/**
 */
package tools.vitruv.dsls.reactions.language.toplevelelements;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.xtext.xtype.XImportSection;

import tools.vitruv.dsls.common.elements.MetamodelImport;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reactions File</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile#getNamespaceImports <em>Namespace Imports</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile#getMetamodelImports <em>Metamodel Imports</em>}</li>
 *   <li>{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile#getReactionsSegments <em>Reactions Segments</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsFile()
 * @model
 * @generated
 */
public interface ReactionsFile extends EObject
{
	/**
	 * Returns the value of the '<em><b>Namespace Imports</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Namespace Imports</em>' containment reference.
	 * @see #setNamespaceImports(XImportSection)
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsFile_NamespaceImports()
	 * @model containment="true"
	 * @generated
	 */
	XImportSection getNamespaceImports();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsFile#getNamespaceImports <em>Namespace Imports</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Namespace Imports</em>' containment reference.
	 * @see #getNamespaceImports()
	 * @generated
	 */
	void setNamespaceImports(XImportSection value);

	/**
	 * Returns the value of the '<em><b>Metamodel Imports</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.common.elements.MetamodelImport}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Metamodel Imports</em>' containment reference list.
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsFile_MetamodelImports()
	 * @model containment="true"
	 * @generated
	 */
	EList<MetamodelImport> getMetamodelImports();

	/**
	 * Returns the value of the '<em><b>Reactions Segments</b></em>' containment reference list.
	 * The list contents are of type {@link tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Reactions Segments</em>' containment reference list.
	 * @see tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsPackage#getReactionsFile_ReactionsSegments()
	 * @model containment="true"
	 * @generated
	 */
	EList<ReactionsSegment> getReactionsSegments();

} // ReactionsFile
