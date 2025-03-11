/**
 */
package tools.vitruv.dsls.commonalities.language.elements.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.EDataTypeClassifier;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>EData Type Classifier</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class EDataTypeClassifierImpl extends MinimalEObjectImpl.Container implements EDataTypeClassifier
{
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EDataTypeClassifierImpl()
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
		return LanguageElementsPackage.Literals.EDATA_TYPE_CLASSIFIER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataTypeClassifier forEDataType(EDataType eDataType)
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
			case LanguageElementsPackage.EDATA_TYPE_CLASSIFIER___FOR_EDATA_TYPE__EDATATYPE:
				return forEDataType((EDataType)arguments.get(0));
			case LanguageElementsPackage.EDATA_TYPE_CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER:
				return isSuperTypeOf((Classifier)arguments.get(0));
			case LanguageElementsPackage.EDATA_TYPE_CLASSIFIER___GET_NAME:
				return getName();
		}
		return super.eInvoke(operationID, arguments);
	}

} //EDataTypeClassifierImpl
