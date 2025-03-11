/**
 */
package tools.vitruv.dsls.commonalities.runtime.resources.impl;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;

import tools.vitruv.change.propagation.ResourceAccess;

import tools.vitruv.dsls.commonalities.runtime.resources.*;

import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ResourcesFactoryImpl extends EFactoryImpl implements ResourcesFactory
{
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ResourcesFactory init()
	{
		try
		{
			ResourcesFactory theResourcesFactory = (ResourcesFactory)EPackage.Registry.INSTANCE.getEFactory(ResourcesPackage.eNS_URI);
			if (theResourcesFactory != null)
			{
				return theResourcesFactory;
			}
		}
		catch (Exception exception)
		{
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ResourcesFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ResourcesFactoryImpl()
	{
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass)
	{
		switch (eClass.getClassifierID())
		{
			case ResourcesPackage.RESOURCE: return createResource();
			case ResourcesPackage.INTERMEDIATE_RESOURCE_BRIDGE: return createIntermediateResourceBridge();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue)
	{
		switch (eDataType.getClassifierID())
		{
			case ResourcesPackage.RESOURCE_ACCESS:
				return createResourceAccessFromString(eDataType, initialValue);
			case ResourcesPackage.EDITABLE_CORRESPONDENCE_MODEL_VIEW:
				return createEditableCorrespondenceModelViewFromString(eDataType, initialValue);
			case ResourcesPackage.URI:
				return createURIFromString(eDataType, initialValue);
			case ResourcesPackage.REACTIONS_CORRESPONDENCE:
				return createReactionsCorrespondenceFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue)
	{
		switch (eDataType.getClassifierID())
		{
			case ResourcesPackage.RESOURCE_ACCESS:
				return convertResourceAccessToString(eDataType, instanceValue);
			case ResourcesPackage.EDITABLE_CORRESPONDENCE_MODEL_VIEW:
				return convertEditableCorrespondenceModelViewToString(eDataType, instanceValue);
			case ResourcesPackage.URI:
				return convertURIToString(eDataType, instanceValue);
			case ResourcesPackage.REACTIONS_CORRESPONDENCE:
				return convertReactionsCorrespondenceToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource createResource()
	{
		ResourceImpl resource = new ResourceImpl();
		return resource;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IntermediateResourceBridge createIntermediateResourceBridge()
	{
		IntermediateResourceBridgeImpl intermediateResourceBridge = new IntermediateResourceBridgeImpl();
		return intermediateResourceBridge;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ResourceAccess createResourceAccessFromString(EDataType eDataType, String initialValue)
	{
		return (ResourceAccess)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertResourceAccessToString(EDataType eDataType, Object instanceValue)
	{
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EditableCorrespondenceModelView<?> createEditableCorrespondenceModelViewFromString(EDataType eDataType, String initialValue)
	{
		return (EditableCorrespondenceModelView<?>)super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEditableCorrespondenceModelViewToString(EDataType eDataType, Object instanceValue)
	{
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public URI createURIFromString(EDataType eDataType, String initialValue)
	{
		return (URI)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertURIToString(EDataType eDataType, Object instanceValue)
	{
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ReactionsCorrespondence createReactionsCorrespondenceFromString(EDataType eDataType, String initialValue)
	{
		return (ReactionsCorrespondence)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertReactionsCorrespondenceToString(EDataType eDataType, Object instanceValue)
	{
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourcesPackage getResourcesPackage()
	{
		return (ResourcesPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ResourcesPackage getPackage()
	{
		return ResourcesPackage.eINSTANCE;
	}

} //ResourcesFactoryImpl
