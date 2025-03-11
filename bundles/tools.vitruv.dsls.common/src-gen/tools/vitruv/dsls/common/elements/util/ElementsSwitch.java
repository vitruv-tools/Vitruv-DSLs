/**
 */
package tools.vitruv.dsls.common.elements.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import tools.vitruv.dsls.common.elements.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see tools.vitruv.dsls.common.elements.ElementsPackage
 * @generated
 */
public class ElementsSwitch<T> extends Switch<T>
{
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ElementsPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ElementsSwitch()
	{
		if (modelPackage == null)
		{
			modelPackage = ElementsPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage)
	{
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject)
	{
		switch (classifierID)
		{
			case ElementsPackage.METAMODEL_IMPORT:
			{
				MetamodelImport metamodelImport = (MetamodelImport)theEObject;
				T result = caseMetamodelImport(metamodelImport);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ElementsPackage.METACLASS_REFERENCE:
			{
				MetaclassReference metaclassReference = (MetaclassReference)theEObject;
				T result = caseMetaclassReference(metaclassReference);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ElementsPackage.NAMED_METACLASS_REFERENCE:
			{
				NamedMetaclassReference namedMetaclassReference = (NamedMetaclassReference)theEObject;
				T result = caseNamedMetaclassReference(namedMetaclassReference);
				if (result == null) result = caseMetaclassReference(namedMetaclassReference);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ElementsPackage.METACLASS_FEATURE_REFERENCE:
			{
				MetaclassFeatureReference metaclassFeatureReference = (MetaclassFeatureReference)theEObject;
				T result = caseMetaclassFeatureReference(metaclassFeatureReference);
				if (result == null) result = caseMetaclassReference(metaclassFeatureReference);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ElementsPackage.METACLASS_EATTRIBUTE_REFERENCE:
			{
				MetaclassEAttributeReference metaclassEAttributeReference = (MetaclassEAttributeReference)theEObject;
				T result = caseMetaclassEAttributeReference(metaclassEAttributeReference);
				if (result == null) result = caseMetaclassReference(metaclassEAttributeReference);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ElementsPackage.METACLASS_EREFERENCE_REFERENCE:
			{
				MetaclassEReferenceReference metaclassEReferenceReference = (MetaclassEReferenceReference)theEObject;
				T result = caseMetaclassEReferenceReference(metaclassEReferenceReference);
				if (result == null) result = caseMetaclassReference(metaclassEReferenceReference);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Metamodel Import</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Metamodel Import</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMetamodelImport(MetamodelImport object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Metaclass Reference</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Metaclass Reference</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMetaclassReference(MetaclassReference object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Named Metaclass Reference</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Named Metaclass Reference</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNamedMetaclassReference(NamedMetaclassReference object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Metaclass Feature Reference</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Metaclass Feature Reference</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMetaclassFeatureReference(MetaclassFeatureReference object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Metaclass EAttribute Reference</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Metaclass EAttribute Reference</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMetaclassEAttributeReference(MetaclassEAttributeReference object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Metaclass EReference Reference</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Metaclass EReference Reference</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMetaclassEReferenceReference(MetaclassEReferenceReference object)
	{
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object)
	{
		return null;
	}

} //ElementsSwitch
