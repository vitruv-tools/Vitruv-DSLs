package tools.vitruv.dsls.common.elements;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
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
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class CommonLanguageElementsScopeProvider {
  @Inject
  private IQualifiedNameProvider qualifiedNameProvider;

  @Inject
  private Provider<EPackageRegistryScope> packagesScope;

  public <T extends Object> IScope createScope(final IScope parentScope, final Iterator<? extends T> elements, final Function<T, IEObjectDescription> descriptionCreation) {
    final Function1<T, IEObjectDescription> _function = (T it) -> {
      return descriptionCreation.apply(it);
    };
    List<IEObjectDescription> _list = IteratorExtensions.<IEObjectDescription>toList(IteratorExtensions.<IEObjectDescription>filterNull(IteratorExtensions.map(elements, _function)));
    return new SimpleScope(parentScope, _list);
  }

  public IScope getScope(final EObject context, final EReference reference) {
    boolean _equals = reference.equals(ElementsPackage.Literals.METAMODEL_IMPORT__PACKAGE);
    if (_equals) {
      return this.packagesScope.get();
    } else {
      boolean _equals_1 = reference.equals(ElementsPackage.Literals.METACLASS_FEATURE_REFERENCE__FEATURE);
      if (_equals_1) {
        EClassifier _metaclass = null;
        if (((MetaclassFeatureReference) context)!=null) {
          _metaclass=((MetaclassFeatureReference) context).getMetaclass();
        }
        return this.createEStructuralFeatureScope(((EClass) _metaclass));
      } else {
        boolean _equals_2 = reference.equals(ElementsPackage.Literals.METACLASS_EATTRIBUTE_REFERENCE__FEATURE);
        if (_equals_2) {
          EClassifier _metaclass_1 = null;
          if (((MetaclassEAttributeReference) context)!=null) {
            _metaclass_1=((MetaclassEAttributeReference) context).getMetaclass();
          }
          return this.createEAttributeScope(((EClass) _metaclass_1));
        } else {
          boolean _equals_3 = reference.equals(ElementsPackage.Literals.METACLASS_EREFERENCE_REFERENCE__FEATURE);
          if (_equals_3) {
            EClassifier _metaclass_2 = null;
            if (((MetaclassEReferenceReference) context)!=null) {
              _metaclass_2=((MetaclassEReferenceReference) context).getMetaclass();
            }
            return this.createEReferenceScope(((EClass) _metaclass_2));
          } else {
            boolean _equals_4 = reference.equals(ElementsPackage.Literals.METACLASS_REFERENCE__METAMODEL);
            if (_equals_4) {
              return this.createImportsScope(context.eResource());
            } else {
              boolean _equals_5 = reference.equals(ElementsPackage.Literals.METACLASS_REFERENCE__METACLASS);
              if (_equals_5) {
                MetaclassReference _xifexpression = null;
                if ((context instanceof MetaclassReference)) {
                  _xifexpression = ((MetaclassReference)context);
                }
                final MetaclassReference potentialMetaclassReference = _xifexpression;
                MetamodelImport _metamodel = null;
                if (potentialMetaclassReference!=null) {
                  _metamodel=potentialMetaclassReference.getMetamodel();
                }
                return this.createQualifiedEClassifierScope(_metamodel);
              }
            }
          }
        }
      }
    }
    return null;
  }

  private IScope createImportsScope(final Resource resource) {
    IScope _xblockexpression = null;
    {
      if ((resource == null)) {
        return IScope.NULLSCOPE;
      }
      final Function<MetamodelImport, IEObjectDescription> _function = (MetamodelImport it) -> {
        return EObjectDescription.create(it.getName(), it);
      };
      _xblockexpression = this.<MetamodelImport>createScope(IScope.NULLSCOPE, this.getMetamodelImports(resource).iterator(), _function);
    }
    return _xblockexpression;
  }

  /**
   * Returns all packages that have been imported by import statements
   * in the given resource.
   */
  private Iterable<MetamodelImport> getMetamodelImports(final Resource res) {
    List<EObject> contents = IterableExtensions.<EObject>toList(this.getAllContentsOfEClass(res, ElementsPackage.eINSTANCE.getMetamodelImport(), true));
    final Function1<MetamodelImport, Boolean> _function = (MetamodelImport it) -> {
      EPackage _package = it.getPackage();
      return Boolean.valueOf((_package != null));
    };
    final Function1<MetamodelImport, MetamodelImport> _function_1 = (MetamodelImport it) -> {
      MetamodelImport _xblockexpression = null;
      {
        String _elvis = null;
        String _name = it.getName();
        if (_name != null) {
          _elvis = _name;
        } else {
          String _name_1 = it.getPackage().getName();
          _elvis = _name_1;
        }
        it.setName(_elvis);
        _xblockexpression = it;
      }
      return _xblockexpression;
    };
    final Iterable<MetamodelImport> validImports = IterableExtensions.<MetamodelImport, MetamodelImport>map(IterableExtensions.<MetamodelImport>filter(Iterables.<MetamodelImport>filter(contents, MetamodelImport.class), _function), _function_1);
    return validImports;
  }

  private IScope createEStructuralFeatureScope(final EClass eClass) {
    EList<EStructuralFeature> _eAllStructuralFeatures = null;
    if (eClass!=null) {
      _eAllStructuralFeatures=eClass.getEAllStructuralFeatures();
    }
    return this.createEStructuralFeatureScope(_eAllStructuralFeatures.iterator());
  }

  private IScope createEAttributeScope(final EClass eClass) {
    EList<EAttribute> _eAllAttributes = null;
    if (eClass!=null) {
      _eAllAttributes=eClass.getEAllAttributes();
    }
    return this.createEStructuralFeatureScope(_eAllAttributes.iterator());
  }

  private IScope createEReferenceScope(final EClass eClass) {
    EList<EReference> _eAllReferences = null;
    if (eClass!=null) {
      _eAllReferences=eClass.getEAllReferences();
    }
    return this.createEStructuralFeatureScope(_eAllReferences.iterator());
  }

  private IScope createEStructuralFeatureScope(final Iterator<? extends EStructuralFeature> featuresIterator) {
    IScope _xifexpression = null;
    if ((featuresIterator != null)) {
      final Function<EStructuralFeature, IEObjectDescription> _function = (EStructuralFeature it) -> {
        return EObjectDescription.create(it.getName(), it);
      };
      _xifexpression = this.<EStructuralFeature>createScope(IScope.NULLSCOPE, featuresIterator, _function);
    } else {
      return IScope.NULLSCOPE;
    }
    return _xifexpression;
  }

  /**
   * Returns all elements with the given EClass inside the Resource res.
   */
  public Iterable<EObject> getAllContentsOfEClass(final Resource res, final EClass namedParent, final boolean allContents) {
    List<EObject> _xifexpression = null;
    if (allContents) {
      _xifexpression = IteratorExtensions.<EObject>toList(res.getAllContents());
    } else {
      _xifexpression = res.getContents();
    }
    List<EObject> contents = _xifexpression;
    final Function1<EObject, Boolean> _function = (EObject it) -> {
      return Boolean.valueOf(it.eClass().equals(namedParent));
    };
    return IterableExtensions.<EObject>filter(contents, _function);
  }

  /**
   * Creates an {@link IScope} that represents all {@link EClass}es
   * that are provided by the metamodel of the given {@link MetamodelImport}
   * by a fully qualified name.
   * 
   * @param metamodelImport - the metamodel to provide all classes of
   */
  public SimpleScope createQualifiedEClassifierScope(final MetamodelImport metamodelImport) {
    return this.createQualifiedEClassifierScope(metamodelImport, false, null, EcorePackage.Literals.EOBJECT);
  }

  /**
   * Creates an {@link IScope} that represents all {@link EClass}es
   * that are provided by the metamodel of the given {@link MetamodelImport}
   * by a fully qualified name, and {@link EObject} if the given import does not
   * reference a proper metamodel.
   * 
   * @param metamodelImport - the metamodel to provide the classes of
   */
  public SimpleScope createQualifiedEClassScopeWithEObject(final MetamodelImport metamodelImport) {
    return this.createQualifiedEClassifierScope(metamodelImport, true, null, EcorePackage.Literals.EOBJECT);
  }

  /**
   * Creates an {@link IScope} that represents all non-abstract {@link EClass}es
   * that are provided by the metamodel of the given {@link MetamodelImport}
   * by a fully qualified name.
   * 
   * @param metamodelImport - the metamodel to provide the non-abstract classes of
   */
  public SimpleScope createQualifiedEClassScopeWithoutAbstract(final MetamodelImport metamodelImport) {
    final Function<EClassifier, Boolean> _function = (EClassifier it) -> {
      boolean _xifexpression = false;
      if ((it instanceof EClass)) {
        boolean _isAbstract = ((EClass)it).isAbstract();
        _xifexpression = (!_isAbstract);
      } else {
        _xifexpression = false;
      }
      return Boolean.valueOf(_xifexpression);
    };
    return this.createQualifiedEClassifierScope(metamodelImport, false, _function, EcorePackage.Literals.EOBJECT);
  }

  /**
   * Create an {@link IScope} that represents all {@link EClassifier}s
   * that are referencable inside the {@link Resource} via {@link Import}s
   * by a fully qualified name and have the type of the given {@link #type}.
   * 
   * @see #createQualifiedEClassifierScope(Resource)
   */
  private SimpleScope createQualifiedEClassifierScope(final MetamodelImport metamodelImport, final boolean includeEObject, final Function<EClassifier, Boolean> filter, final EClass type) {
    Iterable<IEObjectDescription> _xifexpression = null;
    if (((metamodelImport == null) || (metamodelImport.getPackage() == null))) {
      List<IEObjectDescription> _xifexpression_1 = null;
      if (includeEObject) {
        IEObjectDescription _createEObjectDescription = this.createEObjectDescription(type, false);
        _xifexpression_1 = Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList(_createEObjectDescription));
      } else {
        _xifexpression_1 = Collections.<IEObjectDescription>unmodifiableList(CollectionLiterals.<IEObjectDescription>newArrayList());
      }
      _xifexpression = _xifexpression_1;
    } else {
      _xifexpression = this.collectObjectDescriptions(metamodelImport.getPackage(), true, metamodelImport.isUseQualifiedNames(), filter);
    }
    final Iterable<IEObjectDescription> classifierDescriptions = _xifexpression;
    SimpleScope resultScope = new SimpleScope(IScope.NULLSCOPE, classifierDescriptions);
    return resultScope;
  }

  /**
   * Creates and returns a {@link EObjectDescription} with simple name
   * or in case of a qualified name with the given package prefix.
   */
  private IEObjectDescription createEObjectDescription(final EClassifier classifier, final boolean useQualifiedNames) {
    QualifiedName qualifiedName = null;
    if (useQualifiedNames) {
      qualifiedName = this.qualifiedNameProvider.getFullyQualifiedName(classifier).skipFirst(1);
    } else {
      qualifiedName = QualifiedName.create(classifier.getName());
    }
    return EObjectDescription.create(qualifiedName, classifier);
  }

  private Iterable<IEObjectDescription> collectObjectDescriptions(final EPackage pckg, final boolean includeSubpackages, final boolean useQualifiedNames, final Function<EClassifier, Boolean> filter) {
    Iterable<IEObjectDescription> _xblockexpression = null;
    {
      Iterable<EClassifier> classes = this.collectEClasses(pckg, includeSubpackages);
      if ((filter != null)) {
        classes = IterableExtensions.<EClassifier>filter(classes, new Function1<EClassifier, Boolean>() {
            public Boolean apply(EClassifier arg0) {
              return filter.apply(arg0);
            }
        });
      }
      final Function1<EClassifier, IEObjectDescription> _function = (EClassifier it) -> {
        return this.createEObjectDescription(it, useQualifiedNames);
      };
      _xblockexpression = IterableExtensions.<EClassifier, IEObjectDescription>map(classes, _function);
    }
    return _xblockexpression;
  }

  private Iterable<EClassifier> collectEClasses(final EPackage pckg, final boolean includeSubpackages) {
    ArrayList<EClassifier> recursiveResult = CollectionLiterals.<EClassifier>newArrayList();
    if (includeSubpackages) {
      final Function1<EPackage, Iterable<EClassifier>> _function = (EPackage it) -> {
        return this.collectEClasses(it, includeSubpackages);
      };
      Iterable<EClassifier> _flatten = Iterables.<EClassifier>concat(ListExtensions.<EPackage, Iterable<EClassifier>>map(pckg.getESubpackages(), _function));
      Iterables.<EClassifier>addAll(recursiveResult, _flatten);
    }
    final Function1<EClassifier, Boolean> _function_1 = (EClassifier it) -> {
      return Boolean.valueOf(((it instanceof EClass) || (it instanceof EEnum)));
    };
    final Iterable<EClassifier> result = IterableExtensions.<EClassifier>filter(pckg.getEClassifiers(), _function_1);
    return Iterables.<EClassifier>concat(recursiveResult, result);
  }
}
