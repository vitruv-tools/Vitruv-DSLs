/**
 */
package tools.vitruv.dsls.commonalities.runtime.resources;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Resource</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getName <em>Name</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getFileExtension <em>File Extension</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getPath <em>Path</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getContent <em>Content</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getFullPath <em>Full Path</em>}</li>
 * </ul>
 *
 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getResource()
 * @model
 * @generated
 */
public interface Resource extends EObject
{
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getResource_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>File Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>File Extension</em>' attribute.
	 * @see #setFileExtension(String)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getResource_FileExtension()
	 * @model
	 * @generated
	 */
	String getFileExtension();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getFileExtension <em>File Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>File Extension</em>' attribute.
	 * @see #getFileExtension()
	 * @generated
	 */
	void setFileExtension(String value);

	/**
	 * Returns the value of the '<em><b>Path</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Path</em>' attribute.
	 * @see #setPath(String)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getResource_Path()
	 * @model default=""
	 * @generated
	 */
	String getPath();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getPath <em>Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Path</em>' attribute.
	 * @see #getPath()
	 * @generated
	 */
	void setPath(String value);

	/**
	 * Returns the value of the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Content</em>' containment reference.
	 * @see #setContent(EObject)
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getResource_Content()
	 * @model containment="true"
	 * @generated
	 */
	EObject getContent();

	/**
	 * Sets the value of the '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getContent <em>Content</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Content</em>' containment reference.
	 * @see #getContent()
	 * @generated
	 */
	void setContent(EObject value);

	/**
	 * Returns the value of the '<em><b>Full Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Full Path</em>' attribute.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesPackage#getResource_FullPath()
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getFullPath();

} // Resource
