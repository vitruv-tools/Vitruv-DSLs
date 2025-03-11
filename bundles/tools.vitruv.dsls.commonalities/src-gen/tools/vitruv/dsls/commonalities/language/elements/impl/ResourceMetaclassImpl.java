/**
 */
package tools.vitruv.dsls.commonalities.language.elements.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.ClassifierProvider;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.language.elements.LanguageElementsPackage;
import tools.vitruv.dsls.commonalities.language.elements.MetaclassMember;
import tools.vitruv.dsls.commonalities.language.elements.PackageLike;
import tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Resource Metaclass</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl#getPackageLikeContainer <em>Package Like Container</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl#getDomain <em>Domain</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl#getAllMembers <em>All Members</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl#isAbstract <em>Abstract</em>}</li>
 *   <li>{@link tools.vitruv.dsls.commonalities.language.elements.impl.ResourceMetaclassImpl#getAttributes <em>Attributes</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ResourceMetaclassImpl extends MinimalEObjectImpl.Container implements ResourceMetaclass
{
	/**
	 * The default value of the '{@link #isAbstract() <em>Abstract</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAbstract()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ABSTRACT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #getAttributes() <em>Attributes</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAttributes()
	 * @generated
	 * @ordered
	 */
	protected EList<Attribute> attributes;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ResourceMetaclassImpl()
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
		return LanguageElementsPackage.Literals.RESOURCE_METACLASS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PackageLike getPackageLikeContainer()
	{
		PackageLike packageLikeContainer = basicGetPackageLikeContainer();
		return packageLikeContainer != null && packageLikeContainer.eIsProxy() ? (PackageLike)eResolveProxy((InternalEObject)packageLikeContainer) : packageLikeContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PackageLike basicGetPackageLikeContainer()
	{
		// TODO: implement this method to return the 'Package Like Container' reference
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
	public Domain getDomain()
	{
		Domain domain = basicGetDomain();
		return domain != null && domain.eIsProxy() ? (Domain)eResolveProxy((InternalEObject)domain) : domain;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Domain basicGetDomain()
	{
		// TODO: implement this method to return the 'Domain' reference
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
	public EList<MetaclassMember> getAllMembers()
	{
		// TODO: implement this method to return the 'All Members' reference list
		// Ensure that you remove @generated or mark it @generated NOT
		// The list is expected to implement org.eclipse.emf.ecore.util.InternalEList and org.eclipse.emf.ecore.EStructuralFeature.Setting
		// so it's likely that an appropriate subclass of org.eclipse.emf.ecore.util.EcoreEList should be used.
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAbstract()
	{
		// TODO: implement this method to return the 'Abstract' attribute
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Attribute> getAttributes()
	{
		if (attributes == null)
		{
			attributes = new EObjectContainmentEList.Resolving<Attribute>(Attribute.class, this, LanguageElementsPackage.RESOURCE_METACLASS__ATTRIBUTES);
		}
		return attributes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceMetaclass withClassifierProvider(ClassifierProvider classifierProvider)
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
	public ResourceMetaclass fromDomain(Domain domain)
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
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
	{
		switch (featureID)
		{
			case LanguageElementsPackage.RESOURCE_METACLASS__ATTRIBUTES:
				return ((InternalEList<?>)getAttributes()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
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
			case LanguageElementsPackage.RESOURCE_METACLASS__PACKAGE_LIKE_CONTAINER:
				if (resolve) return getPackageLikeContainer();
				return basicGetPackageLikeContainer();
			case LanguageElementsPackage.RESOURCE_METACLASS__DOMAIN:
				if (resolve) return getDomain();
				return basicGetDomain();
			case LanguageElementsPackage.RESOURCE_METACLASS__ALL_MEMBERS:
				return getAllMembers();
			case LanguageElementsPackage.RESOURCE_METACLASS__ABSTRACT:
				return isAbstract();
			case LanguageElementsPackage.RESOURCE_METACLASS__ATTRIBUTES:
				return getAttributes();
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
			case LanguageElementsPackage.RESOURCE_METACLASS__PACKAGE_LIKE_CONTAINER:
				return basicGetPackageLikeContainer() != null;
			case LanguageElementsPackage.RESOURCE_METACLASS__DOMAIN:
				return basicGetDomain() != null;
			case LanguageElementsPackage.RESOURCE_METACLASS__ALL_MEMBERS:
				return !getAllMembers().isEmpty();
			case LanguageElementsPackage.RESOURCE_METACLASS__ABSTRACT:
				return isAbstract() != ABSTRACT_EDEFAULT;
			case LanguageElementsPackage.RESOURCE_METACLASS__ATTRIBUTES:
				return attributes != null && !attributes.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass)
	{
		if (baseClass == Classifier.class)
		{
			switch (baseOperationID)
			{
				case LanguageElementsPackage.CLASSIFIER___IS_SUPER_TYPE_OF__CLASSIFIER: return LanguageElementsPackage.RESOURCE_METACLASS___IS_SUPER_TYPE_OF__CLASSIFIER;
				default: return -1;
			}
		}
		return super.eDerivedOperationID(baseOperationID, baseClass);
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
			case LanguageElementsPackage.RESOURCE_METACLASS___WITH_CLASSIFIER_PROVIDER__CLASSIFIERPROVIDER:
				return withClassifierProvider((ClassifierProvider)arguments.get(0));
			case LanguageElementsPackage.RESOURCE_METACLASS___FROM_DOMAIN__DOMAIN:
				return fromDomain((Domain)arguments.get(0));
			case LanguageElementsPackage.RESOURCE_METACLASS___IS_SUPER_TYPE_OF__CLASSIFIER:
				return isSuperTypeOf((Classifier)arguments.get(0));
			case LanguageElementsPackage.RESOURCE_METACLASS___GET_NAME:
				return getName();
		}
		return super.eInvoke(operationID, arguments);
	}

} //ResourceMetaclassImpl
