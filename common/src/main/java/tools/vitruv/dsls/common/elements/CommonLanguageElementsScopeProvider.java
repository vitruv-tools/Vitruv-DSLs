package tools.vitruv.dsls.common.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.SimpleScope;

import com.google.inject.Inject;
import com.google.inject.Provider;

import static tools.vitruv.dsls.common.elements.ElementsPackage.Literals.METACLASS_EATTRIBUTE_REFERENCE__FEATURE;
import static tools.vitruv.dsls.common.elements.ElementsPackage.Literals.METACLASS_EREFERENCE_REFERENCE__FEATURE;
import static tools.vitruv.dsls.common.elements.ElementsPackage.Literals.METACLASS_FEATURE_REFERENCE__FEATURE;
import static tools.vitruv.dsls.common.elements.ElementsPackage.Literals.METACLASS_REFERENCE__METACLASS;
import static tools.vitruv.dsls.common.elements.ElementsPackage.Literals.METACLASS_REFERENCE__METAMODEL;
import static tools.vitruv.dsls.common.elements.ElementsPackage.Literals.METAMODEL_IMPORT__PACKAGE;

/**
 * Provides scopes for common language elements like metamodel imports and
 * metaclasses.
 */
public class CommonLanguageElementsScopeProvider {
  @Inject
  IQualifiedNameProvider qualifiedNameProvider;
  @Inject
  Provider<EPackageRegistryScope> packagesScope;

  /**
   * Creates a scope from the given elements using the provided description
   * creation function.
   *
   * @param <T>                 the element type
   * @param parentScope         the parent scope
   * @param elements            the elements
   * @param descriptionCreation the description creation function
   * @return the created scope
   */
  public <T> IScope createScope(
      IScope parentScope,
      Iterator<? extends T> elements,
      Function<T, IEObjectDescription> descriptionCreation) {
    List<IEObjectDescription> descriptions = new ArrayList<>();
    elements.forEachRemaining(
        e -> {
          IEObjectDescription desc = descriptionCreation.apply(e);
          if (desc != null) {
            descriptions.add(desc);
          }
        });
    return new SimpleScope(parentScope, descriptions);
  }

  /**
   * Gets the scope for the given context and reference.
   *
   * @param context   the context
   * @param reference the reference
   * @return the scope
   */
  public IScope getScope(EObject context, EReference reference) {
    if (reference.equals(METAMODEL_IMPORT__PACKAGE)) {
      return packagesScope.get();
    } else if (reference.equals(METACLASS_FEATURE_REFERENCE__FEATURE)) {
      return createEStructuralFeatureScope(
          (EClass) ((MetaclassFeatureReference) context).getMetaclass());
    } else if (reference.equals(METACLASS_EATTRIBUTE_REFERENCE__FEATURE)) {
      return createEAttributeScope(
          (EClass) ((MetaclassEAttributeReference) context).getMetaclass());
    } else if (reference.equals(METACLASS_EREFERENCE_REFERENCE__FEATURE)) {
      return createEReferenceScope(
          (EClass) ((MetaclassEReferenceReference) context).getMetaclass());
    } else if (reference.equals(METACLASS_REFERENCE__METAMODEL)) {
      return createImportsScope(context.eResource());
    } else if (reference.equals(METACLASS_REFERENCE__METACLASS)) {
      MetaclassReference potentialMetaclassReference = (context instanceof MetaclassReference)
          ? (MetaclassReference) context
          : null;
      return createQualifiedEClassifierScope(
          potentialMetaclassReference != null ? potentialMetaclassReference.getMetamodel() : null);
    }
    return null;
  }

  private IScope createImportsScope(Resource resource) {
    if (resource == null) {
      return IScope.NULLSCOPE;
    }
    Iterator<MetamodelImport> importsIterator = getMetamodelImports(resource).iterator();
    return createScope(
        IScope.NULLSCOPE, importsIterator, it -> EObjectDescription.create(it.getName(), it));
  }

  private List<MetamodelImport> getMetamodelImports(Resource res) {
    List<EObject> contents = getAllContentsOfEClass(res, ElementsPackage.eINSTANCE.getMetamodelImport(), true);
    return contents.stream()
        .filter(MetamodelImport.class::isInstance)
        .map(MetamodelImport.class::cast)
        .filter(mi -> mi.getPackage() != null)
        .peek(mi -> mi.setName(mi.getName() != null ? mi.getName() : mi.getPackage().getName()))
        .collect(Collectors.toList());
  }

  private IScope createEStructuralFeatureScope(EClass eClass) {
    return createEStructuralFeatureScope(
        eClass != null ? eClass.getEAllStructuralFeatures().iterator() : null);
  }

  private IScope createEStructuralFeatureScope(
      Iterator<? extends EStructuralFeature> featuresIterator) {
    if (featuresIterator != null) {
      return createScope(
          IScope.NULLSCOPE, featuresIterator, it -> EObjectDescription.create(it.getName(), it));
    } else {
      return IScope.NULLSCOPE;
    }
  }

  private IScope createEAttributeScope(EClass eClass) {
    return createEStructuralFeatureScope(
        eClass != null ? eClass.getEAllAttributes().iterator() : null);
  }

  private IScope createEReferenceScope(EClass eClass) {
    return createEStructuralFeatureScope(
        eClass != null ? eClass.getEAllReferences().iterator() : null);
  }

  /**
   * Gets all contents of the given EClass from the resource.
   *
   * @param res         the resource
   * @param namedParent the named parent EClass
   * @param allContents whether to get all contents or only direct contents
   * @return the list of EObjects
   */
  public List<EObject> getAllContentsOfEClass(
      Resource res, EClass namedParent, boolean allContents) {
    List<EObject> contents = new ArrayList<>();
    if (allContents) {
      res.getAllContents().forEachRemaining(contents::add);
    } else {
      contents = res.getContents();
    }
    return contents.stream()
        .filter(e -> e.eClass().equals(namedParent))
        .collect(Collectors.toList());
  }

  /**
   * Creates a scope for qualified EClassifiers from the given metamodel import.
   *
   * @param metamodelImport the metamodel import
   * @return the created scope
   */
  public IScope createQualifiedEClassifierScope(MetamodelImport metamodelImport) {
    return createQualifiedEClassifierScope(
        metamodelImport, false, null, EcorePackage.Literals.EOBJECT);
  }

  /**
   * Creates a scope for qualified EClassifiers including EObject from the given
   * metamodel import.
   *
   * @param metamodelImport the metamodel import
   * @return the created scope
   */
  public IScope createQualifiedEClassScopeWithEObject(MetamodelImport metamodelImport) {
    return createQualifiedEClassifierScope(
        metamodelImport, true, null, EcorePackage.Literals.EOBJECT);
  }

  private IScope createQualifiedEClassifierScope(
      MetamodelImport metamodelImport,
      boolean includeEObject,
      Function<EClassifier, Boolean> filter,
      EClass type) {
    List<IEObjectDescription> classifierDescriptions;
    if (metamodelImport == null || metamodelImport.getPackage() == null) {
      if (includeEObject) {
        classifierDescriptions = new ArrayList<>();
        classifierDescriptions.add(createEObjectDescription(type, false));
      } else {
        classifierDescriptions = new ArrayList<>();
      }
    } else {
      classifierDescriptions = new ArrayList<>();
      for (EClassifier c : collectEClasses(metamodelImport.getPackage(), true)) {
        if (filter == null || filter.apply(c)) {
          classifierDescriptions.add(
              createEObjectDescription(c, metamodelImport.isUseQualifiedNames()));
        }
      }
    }
    return new SimpleScope(IScope.NULLSCOPE, classifierDescriptions);
  }

  /**
   * Creates a scope for qualified EClasses (excluding abstract ones) from the
   * given metamodel.
   *
   * @param metamodelImport the metamodel import
   * @return the created scope
   */
  public IScope createQualifiedEClassScopeWithoutAbstract(MetamodelImport metamodelImport) {
    return createQualifiedEClassifierScope(
        metamodelImport,
        false,
        c -> (c instanceof EClass) && !((EClass) c).isAbstract(),
        EcorePackage.Literals.EOBJECT);
  }

  private IEObjectDescription createEObjectDescription(
      EClassifier classifier, boolean useQualifiedNames) {
    QualifiedName qualifiedName;
    if (useQualifiedNames) {
      qualifiedName = qualifiedNameProvider.getFullyQualifiedName(classifier).skipFirst(1);
    } else {
      qualifiedName = QualifiedName.create(classifier.getName());
    }
    return EObjectDescription.create(qualifiedName, classifier);
  }

  private List<EClassifier> collectEClasses(EPackage pckg, boolean includeSubpackages) {
    List<EClassifier> recursiveResult = new ArrayList<>();
    if (includeSubpackages) {
      for (EPackage sub : pckg.getESubpackages()) {
        recursiveResult.addAll(collectEClasses(sub, includeSubpackages));
      }
    }
    List<EClassifier> result = pckg.getEClassifiers().stream()
        .filter(c -> c instanceof EClass || c instanceof EEnum)
        .collect(Collectors.toList());
    recursiveResult.addAll(result);
    return recursiveResult;
  }
}
