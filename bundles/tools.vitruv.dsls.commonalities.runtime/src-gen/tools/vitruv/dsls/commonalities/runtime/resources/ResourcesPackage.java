/**
 */
package tools.vitruv.dsls.commonalities.runtime.resources;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EOperation;
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
 * @see tools.vitruv.dsls.commonalities.runtime.resources.ResourcesFactory
 * @model kind="package"
 * @generated
 */
public interface ResourcesPackage extends EPackage
{
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "resources";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://vitruv.tools/metamodels/dsls/commonalities/runtime/resources";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "resources";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ResourcesPackage eINSTANCE = tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl.init();

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourceImpl <em>Resource</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourceImpl
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getResource()
	 * @generated
	 */
	int RESOURCE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE__NAME = 0;

	/**
	 * The feature id for the '<em><b>File Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE__FILE_EXTENSION = 1;

	/**
	 * The feature id for the '<em><b>Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE__PATH = 2;

	/**
	 * The feature id for the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE__CONTENT = 3;

	/**
	 * The feature id for the '<em><b>Full Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE__FULL_PATH = 4;

	/**
	 * The number of structural features of the '<em>Resource</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Resource</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RESOURCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl <em>Intermediate Resource Bridge</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getIntermediateResourceBridge()
	 * @generated
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__NAME = RESOURCE__NAME;

	/**
	 * The feature id for the '<em><b>File Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__FILE_EXTENSION = RESOURCE__FILE_EXTENSION;

	/**
	 * The feature id for the '<em><b>Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__PATH = RESOURCE__PATH;

	/**
	 * The feature id for the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__CONTENT = RESOURCE__CONTENT;

	/**
	 * The feature id for the '<em><b>Full Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__FULL_PATH = RESOURCE__FULL_PATH;

	/**
	 * The feature id for the '<em><b>Base URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__BASE_URI = RESOURCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Is Persisted</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTED = RESOURCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Intermediate Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_ID = RESOURCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Resource Access</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS = RESOURCE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Correspondence Model</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL = RESOURCE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Intermediate NS</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS = RESOURCE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Emf Resource</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__EMF_RESOURCE = RESOURCE_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Is Persistence Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED = RESOURCE_FEATURE_COUNT + 7;

	/**
	 * The number of structural features of the '<em>Intermediate Resource Bridge</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE_FEATURE_COUNT = RESOURCE_FEATURE_COUNT + 8;

	/**
	 * The operation id for the '<em>Remove</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE___REMOVE = RESOURCE_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Initialise For Model Element</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE___INITIALISE_FOR_MODEL_ELEMENT__EOBJECT = RESOURCE_OPERATION_COUNT + 1;

	/**
	 * The number of operations of the '<em>Intermediate Resource Bridge</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INTERMEDIATE_RESOURCE_BRIDGE_OPERATION_COUNT = RESOURCE_OPERATION_COUNT + 2;

	/**
	 * The meta object id for the '<em>Resource Access</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.change.propagation.ResourceAccess
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getResourceAccess()
	 * @generated
	 */
	int RESOURCE_ACCESS = 2;

	/**
	 * The meta object id for the '<em>Editable Correspondence Model View</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getEditableCorrespondenceModelView()
	 * @generated
	 */
	int EDITABLE_CORRESPONDENCE_MODEL_VIEW = 3;

	/**
	 * The meta object id for the '<em>URI</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.common.util.URI
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getURI()
	 * @generated
	 */
	int URI = 4;

	/**
	 * The meta object id for the '<em>Emf Resource</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.resource.Resource
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getEmfResource()
	 * @generated
	 */
	int EMF_RESOURCE = 5;

	/**
	 * The meta object id for the '<em>Reactions Correspondence</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getReactionsCorrespondence()
	 * @generated
	 */
	int REACTIONS_CORRESPONDENCE = 6;


	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource <em>Resource</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Resource</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.Resource
	 * @generated
	 */
	EClass getResource();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.Resource#getName()
	 * @see #getResource()
	 * @generated
	 */
	EAttribute getResource_Name();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getFileExtension <em>File Extension</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>File Extension</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.Resource#getFileExtension()
	 * @see #getResource()
	 * @generated
	 */
	EAttribute getResource_FileExtension();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getPath <em>Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Path</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.Resource#getPath()
	 * @see #getResource()
	 * @generated
	 */
	EAttribute getResource_Path();

	/**
	 * Returns the meta object for the containment reference '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Content</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.Resource#getContent()
	 * @see #getResource()
	 * @generated
	 */
	EReference getResource_Content();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.Resource#getFullPath <em>Full Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Full Path</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.Resource#getFullPath()
	 * @see #getResource()
	 * @generated
	 */
	EAttribute getResource_FullPath();

	/**
	 * Returns the meta object for class '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge <em>Intermediate Resource Bridge</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Intermediate Resource Bridge</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge
	 * @generated
	 */
	EClass getIntermediateResourceBridge();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getBaseURI <em>Base URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Base URI</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getBaseURI()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_BaseURI();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#isIsPersisted <em>Is Persisted</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Persisted</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#isIsPersisted()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_IsPersisted();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getIntermediateId <em>Intermediate Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Intermediate Id</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getIntermediateId()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_IntermediateId();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getResourceAccess <em>Resource Access</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Resource Access</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getResourceAccess()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_ResourceAccess();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getCorrespondenceModel <em>Correspondence Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Correspondence Model</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getCorrespondenceModel()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_CorrespondenceModel();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getIntermediateNS <em>Intermediate NS</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Intermediate NS</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getIntermediateNS()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_IntermediateNS();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getEmfResource <em>Emf Resource</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Emf Resource</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#getEmfResource()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_EmfResource();

	/**
	 * Returns the meta object for the attribute '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#isIsPersistenceEnabled <em>Is Persistence Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Persistence Enabled</em>'.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#isIsPersistenceEnabled()
	 * @see #getIntermediateResourceBridge()
	 * @generated
	 */
	EAttribute getIntermediateResourceBridge_IsPersistenceEnabled();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#remove() <em>Remove</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Remove</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#remove()
	 * @generated
	 */
	EOperation getIntermediateResourceBridge__Remove();

	/**
	 * Returns the meta object for the '{@link tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#initialiseForModelElement(org.eclipse.emf.ecore.EObject) <em>Initialise For Model Element</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Initialise For Model Element</em>' operation.
	 * @see tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge#initialiseForModelElement(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	EOperation getIntermediateResourceBridge__InitialiseForModelElement__EObject();

	/**
	 * Returns the meta object for data type '{@link tools.vitruv.change.propagation.ResourceAccess <em>Resource Access</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Resource Access</em>'.
	 * @see tools.vitruv.change.propagation.ResourceAccess
	 * @model instanceClass="tools.vitruv.change.propagation.ResourceAccess"
	 * @generated
	 */
	EDataType getResourceAccess();

	/**
	 * Returns the meta object for data type '{@link tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView <em>Editable Correspondence Model View</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Editable Correspondence Model View</em>'.
	 * @see tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView
	 * @model instanceClass="tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView" typeParameters="C" CBounds="tools.vitruv.dsls.commonalities.runtime.resources.ReactionsCorrespondence"
	 * @generated
	 */
	EDataType getEditableCorrespondenceModelView();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.emf.common.util.URI <em>URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>URI</em>'.
	 * @see org.eclipse.emf.common.util.URI
	 * @model instanceClass="org.eclipse.emf.common.util.URI"
	 * @generated
	 */
	EDataType getURI();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.emf.ecore.resource.Resource <em>Emf Resource</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Emf Resource</em>'.
	 * @see org.eclipse.emf.ecore.resource.Resource
	 * @model instanceClass="org.eclipse.emf.ecore.resource.Resource" serializeable="false"
	 * @generated
	 */
	EDataType getEmfResource();

	/**
	 * Returns the meta object for data type '{@link tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence <em>Reactions Correspondence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Reactions Correspondence</em>'.
	 * @see tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence
	 * @model instanceClass="tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence"
	 * @generated
	 */
	EDataType getReactionsCorrespondence();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ResourcesFactory getResourcesFactory();

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
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourceImpl <em>Resource</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourceImpl
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getResource()
		 * @generated
		 */
		EClass RESOURCE = eINSTANCE.getResource();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE__NAME = eINSTANCE.getResource_Name();

		/**
		 * The meta object literal for the '<em><b>File Extension</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE__FILE_EXTENSION = eINSTANCE.getResource_FileExtension();

		/**
		 * The meta object literal for the '<em><b>Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE__PATH = eINSTANCE.getResource_Path();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RESOURCE__CONTENT = eINSTANCE.getResource_Content();

		/**
		 * The meta object literal for the '<em><b>Full Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RESOURCE__FULL_PATH = eINSTANCE.getResource_FullPath();

		/**
		 * The meta object literal for the '{@link tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl <em>Intermediate Resource Bridge</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.IntermediateResourceBridgeImpl
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getIntermediateResourceBridge()
		 * @generated
		 */
		EClass INTERMEDIATE_RESOURCE_BRIDGE = eINSTANCE.getIntermediateResourceBridge();

		/**
		 * The meta object literal for the '<em><b>Base URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__BASE_URI = eINSTANCE.getIntermediateResourceBridge_BaseURI();

		/**
		 * The meta object literal for the '<em><b>Is Persisted</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTED = eINSTANCE.getIntermediateResourceBridge_IsPersisted();

		/**
		 * The meta object literal for the '<em><b>Intermediate Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_ID = eINSTANCE.getIntermediateResourceBridge_IntermediateId();

		/**
		 * The meta object literal for the '<em><b>Resource Access</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__RESOURCE_ACCESS = eINSTANCE.getIntermediateResourceBridge_ResourceAccess();

		/**
		 * The meta object literal for the '<em><b>Correspondence Model</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__CORRESPONDENCE_MODEL = eINSTANCE.getIntermediateResourceBridge_CorrespondenceModel();

		/**
		 * The meta object literal for the '<em><b>Intermediate NS</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__INTERMEDIATE_NS = eINSTANCE.getIntermediateResourceBridge_IntermediateNS();

		/**
		 * The meta object literal for the '<em><b>Emf Resource</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__EMF_RESOURCE = eINSTANCE.getIntermediateResourceBridge_EmfResource();

		/**
		 * The meta object literal for the '<em><b>Is Persistence Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INTERMEDIATE_RESOURCE_BRIDGE__IS_PERSISTENCE_ENABLED = eINSTANCE.getIntermediateResourceBridge_IsPersistenceEnabled();

		/**
		 * The meta object literal for the '<em><b>Remove</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation INTERMEDIATE_RESOURCE_BRIDGE___REMOVE = eINSTANCE.getIntermediateResourceBridge__Remove();

		/**
		 * The meta object literal for the '<em><b>Initialise For Model Element</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation INTERMEDIATE_RESOURCE_BRIDGE___INITIALISE_FOR_MODEL_ELEMENT__EOBJECT = eINSTANCE.getIntermediateResourceBridge__InitialiseForModelElement__EObject();

		/**
		 * The meta object literal for the '<em>Resource Access</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.change.propagation.ResourceAccess
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getResourceAccess()
		 * @generated
		 */
		EDataType RESOURCE_ACCESS = eINSTANCE.getResourceAccess();

		/**
		 * The meta object literal for the '<em>Editable Correspondence Model View</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getEditableCorrespondenceModelView()
		 * @generated
		 */
		EDataType EDITABLE_CORRESPONDENCE_MODEL_VIEW = eINSTANCE.getEditableCorrespondenceModelView();

		/**
		 * The meta object literal for the '<em>URI</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.emf.common.util.URI
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getURI()
		 * @generated
		 */
		EDataType URI = eINSTANCE.getURI();

		/**
		 * The meta object literal for the '<em>Emf Resource</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.emf.ecore.resource.Resource
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getEmfResource()
		 * @generated
		 */
		EDataType EMF_RESOURCE = eINSTANCE.getEmfResource();

		/**
		 * The meta object literal for the '<em>Reactions Correspondence</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence
		 * @see tools.vitruv.dsls.commonalities.runtime.resources.impl.ResourcesPackageImpl#getReactionsCorrespondence()
		 * @generated
		 */
		EDataType REACTIONS_CORRESPONDENCE = eINSTANCE.getReactionsCorrespondence();

	}

} //ResourcesPackage
