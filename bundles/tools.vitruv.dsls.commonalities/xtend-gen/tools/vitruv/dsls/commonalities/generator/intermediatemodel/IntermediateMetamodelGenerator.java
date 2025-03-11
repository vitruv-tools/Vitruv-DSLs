package tools.vitruv.dsls.commonalities.generator.intermediatemodel;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.common.ClassNameGenerator;
import tools.vitruv.dsls.commonalities.generator.GenerationContext;
import tools.vitruv.dsls.commonalities.generator.SubGenerator;
import tools.vitruv.dsls.commonalities.generator.util.guice.GenerationScoped;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.elements.Classifier;
import tools.vitruv.dsls.commonalities.language.elements.EClassAdapter;
import tools.vitruv.dsls.commonalities.language.elements.EDataTypeAdapter;
import tools.vitruv.dsls.commonalities.language.elements.LeastSpecificType;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.reactions.api.generator.ReferenceClassNameAdapter;

@GenerationScoped
@SuppressWarnings("all")
public class IntermediateMetamodelGenerator implements SubGenerator {
  @FinalFieldsConstructor
  private static class EPackageGenerator {
    private final String conceptName;

    private final Iterable<CommonalityFile> commonalityFiles;

    @Extension
    private final GenerationContext _generationContext;

    private final EPackage generatedEPackage = EcoreFactory.eINSTANCE.createEPackage();

    private final List<Runnable> linkCallbacks = new ArrayList<Runnable>();

    private <T extends Object> T whenLinking(final T object, final Consumer<T> linker) {
      final Runnable _function = () -> {
        linker.accept(object);
      };
      this.linkCallbacks.add(_function);
      return object;
    }

    public void link() {
      final Consumer<Runnable> _function = (Runnable it) -> {
        it.run();
      };
      this.linkCallbacks.forEach(_function);
    }

    private EPackage generateEPackage() {
      final Procedure1<EPackage> _function = (EPackage it) -> {
        it.setNsURI(IntermediateMetamodelGenerator.NS_URI_PREFIX.appendSegment(this.conceptName).toString());
        it.setNsPrefix(IntermediateModelConstants.getIntermediateMetamodelPackageSimpleName(this.conceptName));
        it.setName(IntermediateModelConstants.getIntermediateMetamodelPackageSimpleName(this.conceptName));
        EList<EClassifier> _eClassifiers = it.getEClassifiers();
        EClass _generateRootClass = this.generateRootClass();
        _eClassifiers.add(_generateRootClass);
        EList<EClassifier> _eClassifiers_1 = it.getEClassifiers();
        final Function1<CommonalityFile, Commonality> _function_1 = (CommonalityFile it_1) -> {
          return it_1.getCommonality();
        };
        final Function1<Commonality, EClass> _function_2 = (Commonality it_1) -> {
          return this.generateEClass(it_1);
        };
        Iterable<EClass> _map = IterableExtensions.<Commonality, EClass>map(IterableExtensions.<CommonalityFile, Commonality>map(this.commonalityFiles, _function_1), _function_2);
        Iterables.<EClassifier>addAll(_eClassifiers_1, _map);
        IntermediateMetamodelGenerator.referencedAs(it.getEFactoryInstance(), IntermediateModelConstants.getIntermediateMetamodelFactoryClassName(this.conceptName));
      };
      return ObjectExtensions.<EPackage>operator_doubleArrow(
        this.generatedEPackage, _function);
    }

    private EClass generateEClass(final Commonality commonality) {
      EClass _createEClass = EcoreFactory.eINSTANCE.createEClass();
      final Procedure1<EClass> _function = (EClass it) -> {
        it.setName(IntermediateModelConstants.getIntermediateMetamodelClassName(commonality).getSimpleName());
        EList<EClass> _eSuperTypes = it.getESuperTypes();
        EClass _intermediate = IntermediateModelBasePackage.eINSTANCE.getIntermediate();
        _eSuperTypes.add(_intermediate);
        EList<EStructuralFeature> _eStructuralFeatures = it.getEStructuralFeatures();
        final Function1<CommonalityAttribute, EStructuralFeature> _function_1 = (CommonalityAttribute it_1) -> {
          return this.generateEFeature(it_1);
        };
        List<EStructuralFeature> _map = ListExtensions.<CommonalityAttribute, EStructuralFeature>map(commonality.getAttributes(), _function_1);
        Iterables.<EStructuralFeature>addAll(_eStructuralFeatures, _map);
        EList<EStructuralFeature> _eStructuralFeatures_1 = it.getEStructuralFeatures();
        final Function1<CommonalityReference, EReference> _function_2 = (CommonalityReference it_1) -> {
          return this.generateEReference(it_1);
        };
        List<EReference> _map_1 = ListExtensions.<CommonalityReference, EReference>map(commonality.getReferences(), _function_2);
        Iterables.<EStructuralFeature>addAll(_eStructuralFeatures_1, _map_1);
        IntermediateMetamodelGenerator.referencedAs(it, IntermediateModelConstants.getIntermediateMetamodelClassName(commonality));
      };
      return ObjectExtensions.<EClass>operator_doubleArrow(_createEClass, _function);
    }

    private EStructuralFeature generateEFeature(final CommonalityAttribute attribute) {
      EStructuralFeature _switchResult = null;
      Classifier _type = attribute.getType();
      final Classifier attributeType = _type;
      boolean _matched = false;
      if (attributeType instanceof EDataTypeAdapter) {
        _matched=true;
        EAttribute _createEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        final Procedure1<EAttribute> _function = (EAttribute it) -> {
          it.setEType(this.getOrGenerateEDataType(((EDataTypeAdapter)attributeType)));
        };
        _switchResult = ObjectExtensions.<EAttribute>operator_doubleArrow(_createEAttribute, _function);
      }
      if (!_matched) {
        if (attributeType instanceof EClassAdapter) {
          _matched=true;
          EReference _createEReference = EcoreFactory.eINSTANCE.createEReference();
          final Procedure1<EReference> _function = (EReference it) -> {
            it.setEType(((EClassAdapter)attributeType).getWrapped());
          };
          _switchResult = ObjectExtensions.<EReference>operator_doubleArrow(_createEReference, _function);
        }
      }
      if (!_matched) {
        if (attributeType instanceof LeastSpecificType) {
          _matched=true;
          EReference _createEReference = EcoreFactory.eINSTANCE.createEReference();
          final Procedure1<EReference> _function = (EReference it) -> {
            it.setEType(EcorePackage.Literals.EOBJECT);
          };
          _switchResult = ObjectExtensions.<EReference>operator_doubleArrow(_createEReference, _function);
        }
      }
      if (!_matched) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("The Attribute declaration ‹");
        _builder.append(attribute);
        _builder.append("› has the type ");
        _builder.append("‹");
        _builder.append(attributeType);
        _builder.append("› which does not correspond to an EClassifier!");
        throw new IllegalStateException(_builder.toString());
      }
      final Procedure1<EStructuralFeature> _function = (EStructuralFeature it) -> {
        it.setName(attribute.getName());
        int _xifexpression = (int) 0;
        boolean _isMultiValued = attribute.isMultiValued();
        if (_isMultiValued) {
          _xifexpression = ETypedElement.UNBOUNDED_MULTIPLICITY;
        } else {
          _xifexpression = 1;
        }
        it.setUpperBound(_xifexpression);
      };
      return ObjectExtensions.<EStructuralFeature>operator_doubleArrow(_switchResult, _function);
    }

    private EReference generateEReference(final CommonalityReference reference) {
      EReference _createEReference = EcoreFactory.eINSTANCE.createEReference();
      final Procedure1<EReference> _function = (EReference it) -> {
        it.setName(reference.getName());
        int _xifexpression = (int) 0;
        boolean _isMultiValued = reference.isMultiValued();
        if (_isMultiValued) {
          _xifexpression = ETypedElement.UNBOUNDED_MULTIPLICITY;
        } else {
          _xifexpression = 1;
        }
        it.setUpperBound(_xifexpression);
        it.setContainment(true);
      };
      final Consumer<EReference> _function_1 = (EReference it) -> {
        it.setEType(this._generationContext.getIntermediateMetamodelClass(reference.getReferenceType()));
      };
      return this.<EReference>whenLinking(ObjectExtensions.<EReference>operator_doubleArrow(_createEReference, _function), _function_1);
    }

    private EClassifier getOrGenerateEDataType(final EDataTypeAdapter dataTypeAdapter) {
      EClassifier _elvis = null;
      final Function1<EPackage, EList<EClassifier>> _function = (EPackage it) -> {
        return it.getEClassifiers();
      };
      final Function1<EClassifier, Boolean> _function_1 = (EClassifier it) -> {
        Class<?> _instanceClass = it.getInstanceClass();
        Class<?> _instanceClass_1 = dataTypeAdapter.getWrapped().getInstanceClass();
        return Boolean.valueOf(Objects.equal(_instanceClass, _instanceClass_1));
      };
      EClassifier _findFirst = IterableExtensions.<EClassifier>findFirst(IterableExtensions.flatMap(Collections.<EPackage>unmodifiableList(CollectionLiterals.<EPackage>newArrayList(this.generatedEPackage, EcorePackage.eINSTANCE)), _function), _function_1);
      if (_findFirst != null) {
        _elvis = _findFirst;
      } else {
        EDataType _generateEDataType = this.generateEDataType(dataTypeAdapter.getWrapped());
        _elvis = _generateEDataType;
      }
      return _elvis;
    }

    private EDataType generateEDataType(final EDataType classifier) {
      EDataType _createEDataType = EcoreFactory.eINSTANCE.createEDataType();
      final Procedure1<EDataType> _function = (EDataType it) -> {
        it.setName(classifier.getInstanceClass().getName().replace(".", "_"));
        it.setInstanceClass(classifier.getInstanceClass());
      };
      final EDataType newDataType = ObjectExtensions.<EDataType>operator_doubleArrow(_createEDataType, _function);
      EList<EClassifier> _eClassifiers = this.generatedEPackage.getEClassifiers();
      _eClassifiers.add(newDataType);
      return newDataType;
    }

    private EClass generateRootClass() {
      EClass _createEClass = EcoreFactory.eINSTANCE.createEClass();
      final Procedure1<EClass> _function = (EClass it) -> {
        it.setName(IntermediateModelConstants.getIntermediateMetamodelRootClassName(this.conceptName).getSimpleName());
        EList<EClass> _eSuperTypes = it.getESuperTypes();
        EClass _root = IntermediateModelBasePackage.eINSTANCE.getRoot();
        _eSuperTypes.add(_root);
        IntermediateMetamodelGenerator.referencedAs(it, IntermediateModelConstants.getIntermediateMetamodelRootClassName(this.conceptName));
      };
      return ObjectExtensions.<EClass>operator_doubleArrow(_createEClass, _function);
    }

    public EPackageGenerator(final String conceptName, final Iterable<CommonalityFile> commonalityFiles, final GenerationContext _generationContext) {
      super();
      this.conceptName = conceptName;
      this.commonalityFiles = commonalityFiles;
      this._generationContext = _generationContext;
    }
  }

  private static final Logger log = Logger.getLogger(IntermediateMetamodelGenerator.class);

  private static final URI NS_URI_PREFIX = URI.createURI("http://vitruv.tools/commonalities");

  @Inject
  @Extension
  private GenerationContext generationContext;

  @Override
  public void beforeGenerate() {
    boolean _isNewResourceSet = this.generationContext.isNewResourceSet();
    if (_isNewResourceSet) {
      final ResourceSet resourceSet = this.generationContext.getResourceSet();
      final Function1<Resource, CommonalityFile> _function = (Resource it) -> {
        return CommonalitiesLanguageModelExtensions.getOptionalContainedCommonalityFile(it);
      };
      final Function1<CommonalityFile, String> _function_1 = (CommonalityFile it) -> {
        return it.getConcept().getName();
      };
      final Map<String, List<CommonalityFile>> conceptToCommonalityFiles = IterableExtensions.<String, CommonalityFile>groupBy(IterableExtensions.<CommonalityFile>filterNull(ListExtensions.<Resource, CommonalityFile>map(resourceSet.getResources(), _function)), _function_1);
      final Function<String, Object> _function_2 = (String it) -> {
        return new XMLResourceFactoryImpl();
      };
      resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().computeIfAbsent("ecore", _function_2);
      final BiConsumer<String, List<CommonalityFile>> _function_3 = (String concept, List<CommonalityFile> commonalityFiles) -> {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Generating intermediate metamodel for concept ‹");
        _builder.append(concept);
        _builder.append("› and commonalities ");
        final Function1<CommonalityFile, CharSequence> _function_4 = (CommonalityFile it) -> {
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("‹");
          String _name = it.getCommonality().getName();
          _builder_1.append(_name);
          _builder_1.append("›");
          return _builder_1.toString();
        };
        String _join = IterableExtensions.<CommonalityFile>join(commonalityFiles, ", ", _function_4);
        _builder.append(_join);
        IntermediateMetamodelGenerator.log.debug(_builder);
        final IntermediateMetamodelGenerator.EPackageGenerator packageGenerator = this.generateCommonalityEPackage(concept, commonalityFiles, resourceSet);
        this.generationContext.reportGeneratedIntermediateMetamodel(concept, packageGenerator.generatedEPackage);
        packageGenerator.link();
      };
      conceptToCommonalityFiles.forEach(_function_3);
      Set<String> _keySet = conceptToCommonalityFiles.keySet();
      HashSet<String> _hashSet = new HashSet<String>(_keySet);
      this.generationContext.setGeneratedConcepts(_hashSet);
    }
  }

  @Override
  public void generate() {
    try {
      if ((this.generationContext.isNewResourceSet() && this.generationContext.getSettings().isCreateEcoreFiles())) {
        Set<String> _generatedConcepts = this.generationContext.getGeneratedConcepts();
        for (final String conceptName : _generatedConcepts) {
          {
            final URI intermediateModelUri = this.generationContext.getIntermediateMetamodelUri(conceptName);
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Saving generated intermediate metamodel at: ");
            _builder.append(intermediateModelUri);
            IntermediateMetamodelGenerator.log.debug(_builder);
            this.generationContext.getResourceSet().getResource(intermediateModelUri, false).save(CollectionLiterals.<Object, Object>emptyMap());
          }
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private IntermediateMetamodelGenerator.EPackageGenerator generateCommonalityEPackage(final String conceptName, final Iterable<CommonalityFile> commonalityFiles, final ResourceSet resourceSet) {
    final URI outputUri = this.generationContext.getIntermediateMetamodelUri(conceptName);
    Resource _elvis = null;
    Resource _resource = resourceSet.getResource(outputUri, false);
    if (_resource != null) {
      _elvis = _resource;
    } else {
      Resource _createResource = resourceSet.createResource(outputUri);
      _elvis = _createResource;
    }
    final Resource outputResource = _elvis;
    outputResource.getContents().clear();
    final IntermediateMetamodelGenerator.EPackageGenerator packageGenerator = new IntermediateMetamodelGenerator.EPackageGenerator(conceptName, commonalityFiles, this.generationContext);
    final EPackage generatedPackage = packageGenerator.generateEPackage();
    EList<EObject> _contents = outputResource.getContents();
    _contents.add(generatedPackage);
    return packageGenerator;
  }

  private static boolean referencedAs(final EObject element, final ClassNameGenerator className) {
    EList<Adapter> _eAdapters = element.eAdapters();
    String _qualifiedName = className.getQualifiedName();
    ReferenceClassNameAdapter _referenceClassNameAdapter = new ReferenceClassNameAdapter(_qualifiedName);
    return _eAdapters.add(_referenceClassNameAdapter);
  }
}
