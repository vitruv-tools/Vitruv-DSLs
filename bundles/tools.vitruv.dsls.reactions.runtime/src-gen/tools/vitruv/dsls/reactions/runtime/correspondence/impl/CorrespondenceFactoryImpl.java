/**
 */
package tools.vitruv.dsls.reactions.runtime.correspondence.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import tools.vitruv.dsls.reactions.runtime.correspondence.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CorrespondenceFactoryImpl extends EFactoryImpl implements CorrespondenceFactory
{
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static CorrespondenceFactory init()
	{
		try
		{
			CorrespondenceFactory theCorrespondenceFactory = (CorrespondenceFactory)EPackage.Registry.INSTANCE.getEFactory(CorrespondencePackage.eNS_URI);
			if (theCorrespondenceFactory != null)
			{
				return theCorrespondenceFactory;
			}
		}
		catch (Exception exception)
		{
			EcorePlugin.INSTANCE.log(exception);
		}
		return new CorrespondenceFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CorrespondenceFactoryImpl()
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
			case CorrespondencePackage.REACTIONS_CORRESPONDENCE: return createReactionsCorrespondence();
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
	public ReactionsCorrespondence createReactionsCorrespondence()
	{
		ReactionsCorrespondenceImpl reactionsCorrespondence = new ReactionsCorrespondenceImpl();
		return reactionsCorrespondence;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CorrespondencePackage getCorrespondencePackage()
	{
		return (CorrespondencePackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static CorrespondencePackage getPackage()
	{
		return CorrespondencePackage.eINSTANCE;
	}

} //CorrespondenceFactoryImpl
