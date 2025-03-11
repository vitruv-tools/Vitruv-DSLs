/**
 */
package tools.vitruv.dsls.commonalities.language.elements.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import tools.vitruv.dsls.commonalities.language.elements.ClassLike;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider;
import tools.vitruv.dsls.commonalities.language.elements.EFeatureAttribute;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>EFeature Attribute</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.EFeatureAttributeImpl#getClassLikeContainer <em>Class Like Container</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.EFeatureAttributeImpl#isMultiValued <em>Multi Valued</em>}</li>
 * </ul>
 *
 * @generated
 */
public class EFeatureAttributeImpl extends MinimalEObjectImpl.Container implements EFeatureAttribute
{
	/**
	 * The default value of the '{@link #isMultiValued() <em>Multi Valued</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isMultiValued()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MULTI_VALUED_EDEFAULT = false;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EFeatureAttributeImpl()
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
		return LanguageElementsPackage.Literals.EFEATURE_ATTRIBUTE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ClassLike getClassLikeContainer()
	{
		ClassLike classLikeContainer = basicGetClassLikeContainer();
		return classLikeContainer != null && classLikeContainer.eIsProxy() ? (ClassLike)eResolveProxy((InternalEObject)classLikeContainer) : classLikeContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ClassLike basicGetClassLikeContainer()
	{
		// TODO: implement this method to return the 'Class Like Container' reference
		// -> do not perform proxy resolution
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isMultiValued()
	{
		// TODO: implement this method to return the 'Multi Valued' attribute
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EFeatureAttribute forEFeature(EStructuralFeature eFeature)
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
	public EFeatureAttribute withClassifierProvider(ClassifierProvider classifierProvider)
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
	public EFeatureAttribute fromMetaclass(Metaclass metaclass)
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
	public Classifier getType()
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
	public Object eGet(int featureID, boolean resolve, boolean coreType)
	{
		switch (featureID)
		{
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE__CLASS_LIKE_CONTAINER:
				if (resolve) return getClassLikeContainer();
				return basicGetClassLikeContainer();
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE__MULTI_VALUED:
				return isMultiValued();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID)
	{
		switch (featureID)
		{
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE__CLASS_LIKE_CONTAINER:
				return basicGetClassLikeContainer() != null;
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE__MULTI_VALUED:
				return isMultiValued() != MULTI_VALUED_EDEFAULT;
		}
		return super.eIsSet(featureID);
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
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE___FOR_EFEATURE__ESTRUCTURALFEATURE:
				return forEFeature((EStructuralFeature)arguments.get(0));
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER:
				return withClassifierProvider((ClassifierProvider)arguments.get(0));
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE___FROM_METACLASS__METACLASS:
				return fromMetaclass((Metaclass)arguments.get(0));
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE___GET_TYPE:
				return getType();
			case LanguageElementsPackage.EFEATURE_ATTRIBUTE___GET_NAME:
				return getName();
		}
		return super.eInvoke(operationID, arguments);
	}

} //EFeatureAttributeImpl
