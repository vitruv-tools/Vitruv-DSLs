/**
 */
package tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class IntermediateModelBaseFactoryImpl extends EFactoryImpl implements IntermediateModelBaseFactory
{
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static IntermediateModelBaseFactory init()
	{
		try
		{
			IntermediateModelBaseFactory theIntermediateModelBaseFactory = (IntermediateModelBaseFactory)EPackage.Registry.INSTANCE.getEFactory(IntermediateModelBasePackage.eNS_URI);
			if (theIntermediateModelBaseFactory != null)
			{
				return theIntermediateModelBaseFactory;
			}
		}
		catch (Exception exception)
		{
			EcorePlugin.INSTANCE.log(exception);
		}
		return new IntermediateModelBaseFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IntermediateModelBaseFactoryImpl()
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
			case IntermediateModelBasePackage.ROOT: return createRoot();
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
	public Root createRoot()
	{
		RootImpl root = new RootImpl();
		return root;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IntermediateModelBasePackage getIntermediateModelBasePackage()
	{
		return (IntermediateModelBasePackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static IntermediateModelBasePackage getPackage()
	{
		return IntermediateModelBasePackage.eINSTANCE;
	}

} //IntermediateModelBaseFactoryImpl
