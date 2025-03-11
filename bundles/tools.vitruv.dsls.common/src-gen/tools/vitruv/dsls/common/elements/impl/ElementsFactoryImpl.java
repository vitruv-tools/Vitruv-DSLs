/**
 */
package tools.vitruv.dsls.common.elements.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import tools.vitruv.dsls.common.elements.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ElementsFactoryImpl extends EFactoryImpl implements ElementsFactory
{
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ElementsFactory init()
	{
		try
		{
			ElementsFactory theElementsFactory = (ElementsFactory)EPackage.Registry.INSTANCE.getEFactory(ElementsPackage.eNS_URI);
			if (theElementsFactory != null)
			{
				return theElementsFactory;
			}
		}
		catch (Exception exception)
		{
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ElementsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ElementsFactoryImpl()
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
			case ElementsPackage.METAMODEL_IMPORT: return createMetamodelImport();
			case ElementsPackage.METACLASS_REFERENCE: return createMetaclassReference();
			case ElementsPackage.NAMED_METACLASS_REFERENCE: return createNamedMetaclassReference();
			case ElementsPackage.METACLASS_FEATURE_REFERENCE: return createMetaclassFeatureReference();
			case ElementsPackage.METACLASS_EATTRIBUTE_REFERENCE: return createMetaclassEAttributeReference();
			case ElementsPackage.METACLASS_EREFERENCE_REFERENCE: return createMetaclassEReferenceReference();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetamodelImport createMetamodelImport()
	{
		MetamodelImportImpl metamodelImport = new MetamodelImportImpl();
		return metamodelImport;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetaclassReference createMetaclassReference()
	{
		MetaclassReferenceImpl metaclassReference = new MetaclassReferenceImpl();
		return metaclassReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NamedMetaclassReference createNamedMetaclassReference()
	{
		NamedMetaclassReferenceImpl namedMetaclassReference = new NamedMetaclassReferenceImpl();
		return namedMetaclassReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetaclassFeatureReference createMetaclassFeatureReference()
	{
		MetaclassFeatureReferenceImpl metaclassFeatureReference = new MetaclassFeatureReferenceImpl();
		return metaclassFeatureReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetaclassEAttributeReference createMetaclassEAttributeReference()
	{
		MetaclassEAttributeReferenceImpl metaclassEAttributeReference = new MetaclassEAttributeReferenceImpl();
		return metaclassEAttributeReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MetaclassEReferenceReference createMetaclassEReferenceReference()
	{
		MetaclassEReferenceReferenceImpl metaclassEReferenceReference = new MetaclassEReferenceReferenceImpl();
		return metaclassEReferenceReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ElementsPackage getElementsPackage()
	{
		return (ElementsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ElementsPackage getPackage()
	{
		return ElementsPackage.eINSTANCE;
	}

} //ElementsFactoryImpl
