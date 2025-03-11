package tools.vitruv.dsls.reactions.codegen.classgenerators;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.common.ClassNameGenerator;
import tools.vitruv.dsls.common.elements.MetamodelImport;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsImportsHelper;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class ChangePropagationSpecificationClassGenerator extends ClassGenerator {
  private final ReactionsSegment reactionsSegment;

  private JvmGenericType generatedClass;

  public ChangePropagationSpecificationClassGenerator(final ReactionsSegment reactionsSegment, final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    super(typesBuilderExtensionProvider);
    Preconditions.checkState(ReactionsElementsCompletionChecker.isReferenceable(reactionsSegment), "reactions segment is incomplete");
    this.reactionsSegment = reactionsSegment;
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
    };
    return this.generatedClass = this._typesBuilder.toClass(this.reactionsSegment, 
      ClassNamesGenerators.getChangePropagationSpecificationClassNameGenerator(this.reactionsSegment).getQualifiedName(), _function);
  }

  @Override
  public JvmGenericType generateBody() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      EList<JvmTypeReference> _superTypes = it.getSuperTypes();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractReactionsChangePropagationSpecification.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
      EList<JvmTypeReference> _superTypes_1 = it.getSuperTypes();
      JvmTypeReference _typeRef_1 = this._typeReferenceBuilder.typeRef(ChangePropagationSpecification.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_superTypes_1, _typeRef_1);
      EList<JvmMember> _members = it.getMembers();
      final Procedure1<JvmConstructor> _function_1 = (JvmConstructor it_1) -> {
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("super(");
            _builder.append(MetamodelDescriptor.class);
            _builder.append(".with(");
            _builder.append(Set.class);
            _builder.append(".of(");
            {
              final Function1<MetamodelImport, String> _function = (MetamodelImport it_2) -> {
                return it_2.getPackage().getNsURI();
              };
              List<String> _map = ListExtensions.<MetamodelImport, String>map(ChangePropagationSpecificationClassGenerator.this.reactionsSegment.getFromMetamodels(), _function);
              boolean _hasElements = false;
              for(final String namespaceUri : _map) {
                if (!_hasElements) {
                  _hasElements = true;
                } else {
                  _builder.appendImmediate(",", "");
                }
                _builder.append("\"");
                _builder.append(namespaceUri);
                _builder.append("\"");
              }
            }
            _builder.append(")), ");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append(MetamodelDescriptor.class, "\t");
            _builder.append(".with(");
            _builder.append(Set.class, "\t");
            _builder.append(".of(");
            {
              final Function1<MetamodelImport, String> _function_1 = (MetamodelImport it_2) -> {
                return it_2.getPackage().getNsURI();
              };
              List<String> _map_1 = ListExtensions.<MetamodelImport, String>map(ChangePropagationSpecificationClassGenerator.this.reactionsSegment.getToMetamodels(), _function_1);
              boolean _hasElements_1 = false;
              for(final String namespaceUri_1 : _map_1) {
                if (!_hasElements_1) {
                  _hasElements_1 = true;
                } else {
                  _builder.appendImmediate(",", "\t");
                }
                _builder.append("\"");
                _builder.append(namespaceUri_1, "\t");
                _builder.append("\"");
              }
            }
            _builder.append(")));");
          }
        };
        this._typesBuilder.setBody(it_1, _client);
      };
      JvmConstructor _constructor = this._typesBuilder.toConstructor(this.reactionsSegment, _function_1);
      this._typesBuilder.<JvmConstructor>operator_add(_members, _constructor);
      EList<JvmMember> _members_1 = it.getMembers();
      final Procedure1<JvmOperation> _function_2 = (JvmOperation it_1) -> {
        it_1.setVisibility(JvmVisibility.PROTECTED);
        AccessibleElement _accessibleElement = new AccessibleElement("executionState", ReactionExecutionState.class);
        final JvmFormalParameter executionStateParameter = this._parameterGenerator.generateParameter(it_1, _accessibleElement);
        EList<JvmFormalParameter> _parameters = it_1.getParameters();
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, executionStateParameter);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("return new ");
            String _qualifiedName = ClassNamesGenerators.getRoutinesFacadesProviderClassNameGenerator(ChangePropagationSpecificationClassGenerator.this.reactionsSegment).getQualifiedName();
            _builder.append(_qualifiedName);
            _builder.append("(");
            String _name = executionStateParameter.getName();
            _builder.append(_name);
            _builder.append(");");
            _builder.newLineIfNotEmpty();
          }
        };
        this._typesBuilder.setBody(it_1, _client);
      };
      JvmOperation _method = this._typesBuilder.toMethod(this.reactionsSegment, "createRoutinesFacadesProvider", this._typeReferenceBuilder.typeRef(RoutinesFacadesProvider.class), _function_2);
      this._typesBuilder.<JvmOperation>operator_add(_members_1, _method);
      EList<JvmMember> _members_2 = it.getMembers();
      final Procedure1<JvmOperation> _function_3 = (JvmOperation it_1) -> {
        it_1.setVisibility(JvmVisibility.PROTECTED);
        EList<MetamodelImport> _fromMetamodels = this.reactionsSegment.getFromMetamodels();
        EList<MetamodelImport> _toMetamodels = this.reactionsSegment.getToMetamodels();
        final Function1<MetamodelImport, Class<? extends EPackage>> _function_4 = (MetamodelImport it_2) -> {
          return it_2.getPackage().getClass();
        };
        final Function1<Class<? extends EPackage>, Boolean> _function_5 = (Class<? extends EPackage> it_2) -> {
          return Boolean.valueOf((it_2 != EPackageImpl.class));
        };
        final Function1<Class<? extends EPackage>, String> _function_6 = (Class<? extends EPackage> it_2) -> {
          return it_2.getName();
        };
        final Iterable<String> metamodelPackageClassQualifiedNames = IterableExtensions.<Class<? extends EPackage>, String>map(IterableExtensions.<Class<? extends EPackage>>filter(IterableExtensions.<MetamodelImport, Class<? extends EPackage>>map(Iterables.<MetamodelImport>concat(_fromMetamodels, _toMetamodels), _function_4), _function_5), _function_6);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            {
              for(final String metamodelPackageClassQualifiedName : metamodelPackageClassQualifiedNames) {
                _builder.append("org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(");
                _builder.append(metamodelPackageClassQualifiedName);
                _builder.append(".eNS_URI, ");
                _builder.append(metamodelPackageClassQualifiedName);
                _builder.append(".eINSTANCE);");
                _builder.newLineIfNotEmpty();
              }
            }
            {
              final Function1<Map.Entry<Reaction, ReactionsImportPath>, Boolean> _function = (Map.Entry<Reaction, ReactionsImportPath> it_2) -> {
                return Boolean.valueOf(ReactionsElementsCompletionChecker.isReferenceable(it_2.getKey()));
              };
              Iterable<Map.Entry<Reaction, ReactionsImportPath>> _filter = IterableExtensions.<Map.Entry<Reaction, ReactionsImportPath>>filter(ReactionsImportsHelper.getIncludedReactions(ChangePropagationSpecificationClassGenerator.this.reactionsSegment).entrySet(), _function);
              for(final Map.Entry<Reaction, ReactionsImportPath> reactionEntry : _filter) {
                final Reaction reaction = reactionEntry.getKey();
                _builder.newLineIfNotEmpty();
                final ReactionsImportPath reactionsImportPath = reactionEntry.getValue();
                _builder.newLineIfNotEmpty();
                final ClassNameGenerator reactionsNameGenerator = ClassNamesGenerators.getReactionClassNameGenerator(reaction);
                _builder.newLineIfNotEmpty();
                _builder.append("addReaction(new ");
                String _qualifiedName = reactionsNameGenerator.getQualifiedName();
                _builder.append(_qualifiedName);
                _builder.append("((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(");
                _builder.append(ReactionsImportPath.class);
                _builder.append(".fromPathString(\"");
                String _pathString = reactionsImportPath.getPathString();
                _builder.append(_pathString);
                _builder.append("\"))));");
                _builder.newLineIfNotEmpty();
              }
            }
          }
        };
        this._typesBuilder.setBody(it_1, _client);
      };
      JvmOperation _method_1 = this._typesBuilder.toMethod(this.reactionsSegment, "setup", this._typeReferenceBuilder.typeRef(Void.TYPE), _function_3);
      this._typesBuilder.<JvmOperation>operator_add(_members_2, _method_1);
    };
    return ObjectExtensions.<JvmGenericType>operator_doubleArrow(
      this.generatedClass, _function);
  }
}
