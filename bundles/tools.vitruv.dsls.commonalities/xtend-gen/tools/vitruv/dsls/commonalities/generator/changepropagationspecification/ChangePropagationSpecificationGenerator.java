package tools.vitruv.dsls.commonalities.generator.changepropagationspecification;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.xbase.compiler.CompilationTemplateAdapter;
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypeReferenceBuilder;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.propagation.impl.CompositeChangePropagationSpecification;
import tools.vitruv.dsls.commonalities.generator.GenerationContext;
import tools.vitruv.dsls.commonalities.generator.SubGenerator;
import tools.vitruv.dsls.commonalities.generator.intermediatemodel.IntermediateModelConstants;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.language.CommonalityFile;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;

@SuppressWarnings("all")
public class ChangePropagationSpecificationGenerator implements SubGenerator {
  @Inject
  @Extension
  private GenerationContext generationContext;

  @Inject
  private JvmModelGenerator delegate;

  @Inject
  private Provider<XtextResource> resourceProvider;

  @Inject
  private JvmTypeReferenceBuilder.Factory typeReferenceFactory;

  @Override
  public void beforeGenerate() {
    boolean _isNewResourceSet = this.generationContext.isNewResourceSet();
    if (_isNewResourceSet) {
      EList<Resource> _resources = this.generationContext.getResourceSet().getResources();
      final Function1<Pair<EPackage, EPackage>, XtextResource> _function = (Pair<EPackage, EPackage> it) -> {
        return this.newResource(ChangePropagationSpecificationConstants.getChangePropagationSpecificationName(it.getKey(), it.getValue()));
      };
      Iterable<XtextResource> _map = IterableExtensions.<Pair<EPackage, EPackage>, XtextResource>map(this.getMetamodelPairsForChangePropagation(), _function);
      Iterables.<Resource>addAll(_resources, _map);
      EList<Resource> _resources_1 = this.generationContext.getResourceSet().getResources();
      XtextResource _newResource = this.newResource(ChangePropagationSpecificationConstants.getChangePropagationSpecificationProviderName());
      _resources_1.add(_newResource);
    }
  }

  @Override
  public void generate() {
    boolean _isNewResourceSet = this.generationContext.isNewResourceSet();
    if (_isNewResourceSet) {
      final Function1<Pair<EPackage, EPackage>, JvmGenericType> _function = (Pair<EPackage, EPackage> fromToMetamodel) -> {
        return this.generateChangePropagationSpecification(fromToMetamodel.getKey(), fromToMetamodel.getValue());
      };
      final Iterable<JvmGenericType> changePropagationSpecifications = IterableExtensions.<Pair<EPackage, EPackage>, JvmGenericType>map(this.getMetamodelPairsForChangePropagation(), _function);
      final Consumer<JvmGenericType> _function_1 = (JvmGenericType it) -> {
        this.generateType(it);
      };
      changePropagationSpecifications.forEach(_function_1);
      this.generateType(this.generateChangePropagationSpecificationsProvider(changePropagationSpecifications));
    }
  }

  private JvmGenericType generateChangePropagationSpecification(final EPackage fromNamespace, final EPackage toNamespace) {
    JvmGenericType _xblockexpression = null;
    {
      @Extension
      final JvmTypeReferenceBuilder typeReferenceBuilder = this.typeReferenceFactory.create(this.generationContext.getResourceSet());
      final String intermediateMetamodelPackageQualifiedName = IntermediateModelConstants.getIntermediateMetamodelPackageClassName(this.getConcept(fromNamespace, toNamespace)).getQualifiedName();
      final Function1<String, ReactionsSegment> _function = (String segmentName) -> {
        ReactionsSegment _createReactionsSegment = TopLevelElementsFactory.eINSTANCE.createReactionsSegment();
        final Procedure1<ReactionsSegment> _function_1 = (ReactionsSegment it) -> {
          it.setName(segmentName);
        };
        return ObjectExtensions.<ReactionsSegment>operator_doubleArrow(_createReactionsSegment, _function_1);
      };
      final Iterable<ReactionsSegment> dummyReactionsSegments = IterableExtensions.<String, ReactionsSegment>map(this.getReactionsSegmentNames(fromNamespace, toNamespace), _function);
      JvmGenericType _createJvmGenericType = TypesFactory.eINSTANCE.createJvmGenericType();
      final Procedure1<JvmGenericType> _function_1 = (JvmGenericType it) -> {
        it.setSimpleName(ChangePropagationSpecificationConstants.getChangePropagationSpecificationName(fromNamespace, toNamespace));
        it.setPackageName(ChangePropagationSpecificationConstants.getChangePropagationSpecificationPackageName());
        EList<JvmTypeReference> _superTypes = it.getSuperTypes();
        JvmTypeReference _typeRef = typeReferenceBuilder.typeRef(CompositeChangePropagationSpecification.class);
        _superTypes.add(_typeRef);
        it.setVisibility(JvmVisibility.PUBLIC);
        EList<JvmMember> _members = it.getMembers();
        JvmConstructor _createJvmConstructor = TypesFactory.eINSTANCE.createJvmConstructor();
        final Procedure1<JvmConstructor> _function_2 = (JvmConstructor it_1) -> {
          it_1.setVisibility(JvmVisibility.PUBLIC);
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("super(");
              _builder.append(MetamodelDescriptor.class);
              _builder.append(".with(\"");
              String _nsURI = fromNamespace.getNsURI();
              _builder.append(_nsURI);
              _builder.append("\"),");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append(MetamodelDescriptor.class, "\t");
              _builder.append(".with(\"");
              String _nsURI_1 = toNamespace.getNsURI();
              _builder.append(_nsURI_1, "\t");
              _builder.append("\"));");
              _builder.newLineIfNotEmpty();
              {
                for(final ReactionsSegment reactionsSegment : dummyReactionsSegments) {
                  _builder.append("addChangeMainprocessor(new ");
                  String _qualifiedName = ClassNamesGenerators.getChangePropagationSpecificationClassNameGenerator(reactionsSegment).getQualifiedName();
                  _builder.append(_qualifiedName);
                  _builder.append("());");
                  _builder.newLineIfNotEmpty();
                }
              }
              _builder.append(EPackage.class);
              _builder.append(".Registry.INSTANCE.putIfAbsent(");
              _builder.append(intermediateMetamodelPackageQualifiedName);
              _builder.append(".eNS_URI, ");
              _builder.append(intermediateMetamodelPackageQualifiedName);
              _builder.append(".eINSTANCE);");
            }
          };
          ChangePropagationSpecificationGenerator.setBody(it_1, _client);
        };
        JvmConstructor _doubleArrow = ObjectExtensions.<JvmConstructor>operator_doubleArrow(_createJvmConstructor, _function_2);
        List<JvmMember> _asList = Arrays.<JvmMember>asList(_doubleArrow);
        Iterables.<JvmMember>addAll(_members, _asList);
      };
      _xblockexpression = ObjectExtensions.<JvmGenericType>operator_doubleArrow(_createJvmGenericType, _function_1);
    }
    return _xblockexpression;
  }

  private JvmGenericType generateChangePropagationSpecificationsProvider(final Iterable<JvmGenericType> changePropagationSpecifications) {
    JvmGenericType _xblockexpression = null;
    {
      @Extension
      final JvmTypeReferenceBuilder typeReferenceBuilder = this.typeReferenceFactory.create(this.generationContext.getResourceSet());
      JvmGenericType _createJvmGenericType = TypesFactory.eINSTANCE.createJvmGenericType();
      final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
        it.setSimpleName(ChangePropagationSpecificationConstants.getChangePropagationSpecificationProviderName());
        it.setPackageName(ChangePropagationSpecificationConstants.getChangePropagationSpecificationPackageName());
        it.setVisibility(JvmVisibility.PUBLIC);
        it.setFinal(true);
        EList<JvmMember> _members = it.getMembers();
        JvmConstructor _createJvmConstructor = TypesFactory.eINSTANCE.createJvmConstructor();
        final Procedure1<JvmConstructor> _function_1 = (JvmConstructor it_1) -> {
          it_1.setVisibility(JvmVisibility.PRIVATE);
        };
        JvmConstructor _doubleArrow = ObjectExtensions.<JvmConstructor>operator_doubleArrow(_createJvmConstructor, _function_1);
        JvmField _createJvmField = TypesFactory.eINSTANCE.createJvmField();
        final Procedure1<JvmField> _function_2 = (JvmField it_1) -> {
          it_1.setVisibility(JvmVisibility.PRIVATE);
          it_1.setStatic(true);
          it_1.setType(typeReferenceBuilder.typeRef(Set.class, typeReferenceBuilder.typeRef(ChangePropagationSpecification.class)));
          it_1.setSimpleName("changePropagationSpecifications");
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append(Set.class);
              _builder.append(".of(");
              {
                boolean _hasElements = false;
                for(final JvmGenericType changePropagationSpecification : changePropagationSpecifications) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(", ", "");
                  }
                  _builder.newLineIfNotEmpty();
                  _builder.append("new ");
                  String _qualifiedName = changePropagationSpecification.getQualifiedName();
                  _builder.append(_qualifiedName);
                  _builder.append("()");
                }
              }
              _builder.append(")");
            }
          };
          ChangePropagationSpecificationGenerator.setBody(it_1, _client);
        };
        JvmField _doubleArrow_1 = ObjectExtensions.<JvmField>operator_doubleArrow(_createJvmField, _function_2);
        JvmOperation _createJvmOperation = TypesFactory.eINSTANCE.createJvmOperation();
        final Procedure1<JvmOperation> _function_3 = (JvmOperation it_1) -> {
          it_1.setVisibility(JvmVisibility.PUBLIC);
          it_1.setStatic(true);
          it_1.setReturnType(typeReferenceBuilder.typeRef(Set.class, typeReferenceBuilder.typeRef(ChangePropagationSpecification.class)));
          it_1.setSimpleName("getChangePropagationSpecifications");
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("return new ");
              _builder.append(HashSet.class);
              _builder.append("(changePropagationSpecifications);");
            }
          };
          ChangePropagationSpecificationGenerator.setBody(it_1, _client);
        };
        JvmOperation _doubleArrow_2 = ObjectExtensions.<JvmOperation>operator_doubleArrow(_createJvmOperation, _function_3);
        List<JvmMember> _asList = Arrays.<JvmMember>asList(_doubleArrow, _doubleArrow_1, _doubleArrow_2);
        Iterables.<JvmMember>addAll(_members, _asList);
      };
      _xblockexpression = ObjectExtensions.<JvmGenericType>operator_doubleArrow(_createJvmGenericType, _function);
    }
    return _xblockexpression;
  }

  private void generateType(final JvmDeclaredType type) {
    final Resource typeResource = this.generationContext.getResourceSet().getResource(this.getChangePropagationSpecificationUri(type.getSimpleName()), false);
    Preconditions.checkState((typeResource != null), "there is no resource for type %s", type.getSimpleName());
    EList<EObject> _contents = typeResource.getContents();
    _contents.add(type);
    this.delegate.doGenerate(typeResource, this.generationContext.getFsa());
  }

  private Set<Pair<EPackage, EPackage>> getMetamodelPairsForChangePropagation() {
    final Function1<CommonalityFile, Iterable<Pair<EPackage, EPackage>>> _function = (CommonalityFile file) -> {
      final Function1<Participation, List<Pair<EPackage, EPackage>>> _function_1 = (Participation it) -> {
        List<Pair<EPackage, EPackage>> _xblockexpression = null;
        {
          final EPackage firstPackage = this.generationContext.getMetamodelRootPackage(CommonalitiesLanguageModelExtensions.getDomain(it));
          final EPackage secondPackage = this.generationContext.getMetamodelRootPackage(file.getConcept());
          Pair<EPackage, EPackage> _mappedTo = Pair.<EPackage, EPackage>of(firstPackage, secondPackage);
          Pair<EPackage, EPackage> _mappedTo_1 = Pair.<EPackage, EPackage>of(secondPackage, firstPackage);
          _xblockexpression = Collections.<Pair<EPackage, EPackage>>unmodifiableList(CollectionLiterals.<Pair<EPackage, EPackage>>newArrayList(_mappedTo, _mappedTo_1));
        }
        return _xblockexpression;
      };
      return IterableExtensions.<Participation, Pair<EPackage, EPackage>>flatMap(file.getCommonality().getParticipations(), _function_1);
    };
    return IterableExtensions.<Pair<EPackage, EPackage>>toSet(IterableExtensions.<CommonalityFile, Pair<EPackage, EPackage>>flatMap(this.getCommonalityFiles(), _function));
  }

  private Set<String> getReactionsSegmentNames(final EPackage fromMetamodel, final EPackage toMetamodel) {
    final Function1<CommonalityFile, Boolean> _function = (CommonalityFile file) -> {
      final EPackage commonalityPackage = this.generationContext.getMetamodelRootPackage(file.getConcept());
      return Boolean.valueOf((Objects.equal(fromMetamodel, commonalityPackage) || Objects.equal(toMetamodel, commonalityPackage)));
    };
    final Function1<CommonalityFile, Iterable<String>> _function_1 = (CommonalityFile file) -> {
      final Function1<Participation, String> _function_2 = (Participation it) -> {
        final EPackage participationPackage = this.generationContext.getMetamodelRootPackage(CommonalitiesLanguageModelExtensions.getDomain(it));
        String _switchResult = null;
        boolean _matched = false;
        if (Objects.equal(participationPackage, fromMetamodel)) {
          _matched=true;
          _switchResult = ReactionsGeneratorConventions.getReactionsSegmentFromParticipationToCommonalityName(file.getCommonality(), it);
        }
        if (!_matched) {
          if (Objects.equal(participationPackage, toMetamodel)) {
            _matched=true;
            _switchResult = ReactionsGeneratorConventions.getReactionsSegmentFromCommonalityToParticipationName(file.getCommonality(), it);
          }
        }
        if (!_matched) {
          _switchResult = null;
        }
        return _switchResult;
      };
      return IterableExtensions.<String>filterNull(ListExtensions.<Participation, String>map(file.getCommonality().getParticipations(), _function_2));
    };
    return IterableExtensions.<String>toSet(IterableExtensions.<CommonalityFile, String>flatMap(IterableExtensions.<CommonalityFile>filter(this.getCommonalityFiles(), _function), _function_1));
  }

  private Concept getConcept(final EPackage fromMetamodel, final EPackage toMetamodel) {
    Iterable<CommonalityFile> _commonalityFiles = this.getCommonalityFiles();
    for (final CommonalityFile file : _commonalityFiles) {
      if ((Objects.equal(fromMetamodel, this.generationContext.getMetamodelRootPackage(file.getConcept())) || 
        Objects.equal(toMetamodel, this.generationContext.getMetamodelRootPackage(file.getConcept())))) {
        return file.getConcept();
      }
    }
    return null;
  }

  private Iterable<CommonalityFile> getCommonalityFiles() {
    final Function1<Resource, CommonalityFile> _function = (Resource it) -> {
      return CommonalitiesLanguageModelExtensions.getOptionalContainedCommonalityFile(it);
    };
    return IterableExtensions.<CommonalityFile>filterNull(ListExtensions.<Resource, CommonalityFile>map(this.generationContext.getResourceSet().getResources(), _function));
  }

  private static boolean setBody(final JvmMember member, final StringConcatenationClient body) {
    EList<Adapter> _eAdapters = member.eAdapters();
    CompilationTemplateAdapter _compilationTemplateAdapter = new CompilationTemplateAdapter();
    final Procedure1<CompilationTemplateAdapter> _function = (CompilationTemplateAdapter it) -> {
      it.setCompilationTemplate(body);
    };
    CompilationTemplateAdapter _doubleArrow = ObjectExtensions.<CompilationTemplateAdapter>operator_doubleArrow(_compilationTemplateAdapter, _function);
    return _eAdapters.add(_doubleArrow);
  }

  private XtextResource newResource(final String changePropagationSpecificationName) {
    XtextResource _get = this.resourceProvider.get();
    final Procedure1<XtextResource> _function = (XtextResource it) -> {
      it.setURI(this.getChangePropagationSpecificationUri(changePropagationSpecificationName));
    };
    return ObjectExtensions.<XtextResource>operator_doubleArrow(_get, _function);
  }

  private URI getChangePropagationSpecificationUri(final String changePropagationSpecificationName) {
    URI _ensureHasTrailingSlash = this.ensureHasTrailingSlash(this.generationContext.getFsa().getURI("."));
    String _changePropagationSpecificationPackageName = ChangePropagationSpecificationConstants.getChangePropagationSpecificationPackageName();
    String _plus = (_changePropagationSpecificationPackageName + ".");
    String _plus_1 = (_plus + changePropagationSpecificationName);
    String _plus_2 = (_plus_1 + ".java");
    return _ensureHasTrailingSlash.appendSegment(_plus_2);
  }

  private URI ensureHasTrailingSlash(final URI uri) {
    URI _xblockexpression = null;
    {
      final String uriString = uri.toString();
      URI _xifexpression = null;
      if ((uri.isFile() && (!uriString.endsWith("/")))) {
        _xifexpression = URI.createURI((uriString + "/"));
      } else {
        _xifexpression = uri;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
}
