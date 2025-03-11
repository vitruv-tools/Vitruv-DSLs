/**
 */
package tools.vitruv.dsls.commonalities.language.elements.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage;
import tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Least Specific Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class LeastSpecificTypeImpl extends MinimalEObjectImpl.Container implements LeastSpecificType
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LeastSpecificTypeImpl()
	{
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass()
	{
		return LanguageElementsPackage.Literals.LEAST_SPECIFIC_TYPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSuperTypeOf(Classifier subType)
	{
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName()
	{
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException
	{
		switch (operationID)
		{
			case LanguageElementsPackage.LEAST_SPECIFIC_TYPE___IS_SUPER_TYPE_OF__CLASSIFIER:
				return isSuperTypeOf((Classifier)arguments.get(0));
			case LanguageElementsPackage.LEAST_SPECIFIC_TYPE___GET_NAME:
				return getName();
		}
		return super.eInvoke(operationID, arguments);
	}

} //LeastSpecificTypeImpl
