/**
 */
package tools.vitruv.dsls.commonalities.language.elements.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import tools.vitruv.dsls.commonalities.language.elements.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class LanguageElementsFactoryImpl extends EFactoryImpl implements LanguageElementsFactory
{
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static LanguageElementsFactory init()
	{
		try
		{
			LanguageElementsFactory theLanguageElementsFactory = (LanguageElementsFactory)EPackage.Registry.INSTANCE.getEFactory(LanguageElementsPackage.eNS_URI);
			if (theLanguageElementsFactory != null)
			{
				return theLanguageElementsFactory;
			}
		}
		catch (Exception exception)
		{
			EcorePlugin.INSTANCE.log(exception);
		}
		return new LanguageElementsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LanguageElementsFactoryImpl()
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
			case LanguageElementsPackage.ECLASS_METACLASS: return createEClassMetaclass();
			case LanguageElementsPackage.EDATA_TYPE_CLASSIFIER: return createEDataTypeClassifier();
			case LanguageElementsPackage.METAMODEL: return createMetamodel();
			case LanguageElementsPackage.RESOURCE_METACLASS: return createResourceMetaclass();
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE: return createEFeatureAttribute();
			case LanguageElementsPackage.MOST_SPECIFIC_TYPE: return createMostSpecificType();
			case LanguageElementsPackage.LEAST_SPECIFIC_TYPE: return createLeastSpecificType();
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
			case LanguageElementsPackage.CLASSIFIER_PROVIDER:
				return createClassifierProviderFromString(eDataType, initialValue);
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
			case LanguageElementsPackage.CLASSIFIER_PROVIDER:
				return convertClassifierProviderToString(eDataType, instanceValue);
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
	public EClassMetaclass createEClassMetaclass()
	{
		EClassMetaclassImpl eClassMetaclass = new EClassMetaclassImpl();
		return eClassMetaclass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataTypeClassifier createEDataTypeClassifier()
	{
		EDataTypeClassifierImpl eDataTypeClassifier = new EDataTypeClassifierImpl();
		return eDataTypeClassifier;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Metamodel createMetamodel()
	{
		MetamodelImpl metamodel = new MetamodelImpl();
		return metamodel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceMetaclass createResourceMetaclass()
	{
		ResourceMetaclassImpl resourceMetaclass = new ResourceMetaclassImpl();
		return resourceMetaclass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EFeatureAttribute createEFeatureAttribute()
	{
		EFeatureAttributeImpl eFeatureAttribute = new EFeatureAttributeImpl();
		return eFeatureAttribute;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MostSpecificType createMostSpecificType()
	{
		MostSpecificTypeImpl mostSpecificType = new MostSpecificTypeImpl();
		return mostSpecificType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public LeastSpecificType createLeastSpecificType()
	{
		LeastSpecificTypeImpl leastSpecificType = new LeastSpecificTypeImpl();
		return leastSpecificType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ClassifierProvider createClassifierProviderFromString(EDataType eDataType, String initialValue)
	{
		return (ClassifierProvider)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertClassifierProviderToString(EDataType eDataType, Object instanceValue)
	{
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public LanguageElementsPackage getLanguageElementsPackage()
	{
		return (LanguageElementsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static LanguageElementsPackage getPackage()
	{
		return LanguageElementsPackage.eINSTANCE;
	}

} //LanguageElementsFactoryImpl
