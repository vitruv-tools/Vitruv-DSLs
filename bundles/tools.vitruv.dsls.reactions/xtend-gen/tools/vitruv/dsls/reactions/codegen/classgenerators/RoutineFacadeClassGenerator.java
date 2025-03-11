package tools.vitruv.dsls.reactions.codegen.classgenerators;

import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.common.ClassNameGenerator;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsImportsHelper;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.ReactionsSegment;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class RoutineFacadeClassGenerator extends ClassGenerator {
  protected static final String EXECUTION_STATE_ACCESS_CODE = "_getExecutionState()";

  private final ReactionsSegment reactionsSegment;

  private final ClassNameGenerator routinesFacadeNameGenerator;

  private Map<ReactionsSegment, ReactionsImportPath> includedRoutinesFacades;

  private JvmGenericType generatedClass;

  public RoutineFacadeClassGenerator(final ReactionsSegment reactionsSegment, final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    super(typesBuilderExtensionProvider);
    boolean _isReferenceable = ReactionsElementsCompletionChecker.isReferenceable(reactionsSegment);
    boolean _not = (!_isReferenceable);
    if (_not) {
      throw new IllegalArgumentException("incomplete");
    }
    this.reactionsSegment = reactionsSegment;
    this.routinesFacadeNameGenerator = ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(reactionsSegment);
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
    };
    return this.generatedClass = this._typesBuilder.toClass(this.reactionsSegment, this.routinesFacadeNameGenerator.getQualifiedName(), _function);
  }

  @Override
  public JvmGenericType generateBody() {
    JvmGenericType _xblockexpression = null;
    {
      this.includedRoutinesFacades = ReactionsImportsHelper.getIncludedRoutinesFacades(this.reactionsSegment);
      final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
        EList<JvmTypeReference> _superTypes = it.getSuperTypes();
        JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractRoutinesFacade.class);
        this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
        EList<JvmMember> _members = it.getMembers();
        JvmConstructor _generateConstructor = this.generateConstructor();
        this._typesBuilder.<JvmConstructor>operator_add(_members, _generateConstructor);
        EList<JvmMember> _members_1 = it.getMembers();
        final Function1<Map.Entry<ReactionsSegment, ReactionsImportPath>, JvmField> _function_1 = (Map.Entry<ReactionsSegment, ReactionsImportPath> it_1) -> {
          JvmField _xblockexpression_1 = null;
          {
            final ReactionsSegment includedReactionsSegment = it_1.getKey();
            final String includedRoutinesFacadeFieldName = includedReactionsSegment.getName();
            final String includedRoutinesFacadeClassName = ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(includedReactionsSegment).getQualifiedName();
            final Procedure1<JvmField> _function_2 = (JvmField it_2) -> {
              it_2.setFinal(true);
              it_2.setVisibility(JvmVisibility.PUBLIC);
            };
            _xblockexpression_1 = this._typesBuilder.toField(this.reactionsSegment, includedRoutinesFacadeFieldName, this._typeReferenceBuilder.typeRef(includedRoutinesFacadeClassName), _function_2);
          }
          return _xblockexpression_1;
        };
        Iterable<JvmField> _map = IterableExtensions.<Map.Entry<ReactionsSegment, ReactionsImportPath>, JvmField>map(this.includedRoutinesFacades.entrySet(), _function_1);
        this._typesBuilder.<JvmMember>operator_add(_members_1, _map);
        EList<JvmMember> _members_2 = it.getMembers();
        final Function1<Map.Entry<Routine, ReactionsImportPath>, JvmOperation> _function_2 = (Map.Entry<Routine, ReactionsImportPath> it_1) -> {
          JvmOperation _xblockexpression_1 = null;
          {
            final Routine routine = it_1.getKey();
            final ReactionsImportPath relativeImportPath = it_1.getValue().relativeToRoot();
            JvmOperation _xifexpression = null;
            boolean _isEmpty = relativeImportPath.isEmpty();
            if (_isEmpty) {
              _xifexpression = this.generateCallMethod(routine);
            } else {
              _xifexpression = this.generateIncludedRoutineCallMethod(routine, relativeImportPath);
            }
            _xblockexpression_1 = _xifexpression;
          }
          return _xblockexpression_1;
        };
        Iterable<JvmOperation> _map_1 = IterableExtensions.<Map.Entry<Routine, ReactionsImportPath>, JvmOperation>map(ReactionsImportsHelper.getIncludedRoutines(this.reactionsSegment, true, false).entrySet(), _function_2);
        this._typesBuilder.<JvmMember>operator_add(_members_2, _map_1);
      };
      _xblockexpression = ObjectExtensions.<JvmGenericType>operator_doubleArrow(
        this.generatedClass, _function);
    }
    return _xblockexpression;
  }

  protected JvmConstructor generateConstructor() {
    final Procedure1<JvmConstructor> _function = (JvmConstructor it) -> {
      AccessibleElement _accessibleElement = new AccessibleElement("routinesFacadesProvider", RoutinesFacadesProvider.class);
      final JvmFormalParameter routinesFacadesProviderParameter = this._parameterGenerator.generateParameter(it, _accessibleElement);
      AccessibleElement _accessibleElement_1 = new AccessibleElement("reactionsImportPath", ReactionsImportPath.class);
      final JvmFormalParameter reactionsImportPathParameter = this._parameterGenerator.generateParameter(it, _accessibleElement_1);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, routinesFacadesProviderParameter);
      EList<JvmFormalParameter> _parameters_1 = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_1, reactionsImportPathParameter);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("super(");
          String _name = routinesFacadesProviderParameter.getName();
          _builder.append(_name);
          _builder.append(", ");
          String _name_1 = reactionsImportPathParameter.getName();
          _builder.append(_name_1);
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          StringConcatenationClient _extendedConstructorBody = RoutineFacadeClassGenerator.this.getExtendedConstructorBody();
          _builder.append(_extendedConstructorBody);
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.toConstructor(this.reactionsSegment, _function);
  }

  protected StringConcatenationClient getExtendedConstructorBody() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          Set<Map.Entry<ReactionsSegment, ReactionsImportPath>> _entrySet = RoutineFacadeClassGenerator.this.includedRoutinesFacades.entrySet();
          for(final Map.Entry<ReactionsSegment, ReactionsImportPath> includedRoutinesFacadeEntry : _entrySet) {
            final ReactionsSegment includedReactionsSegment = includedRoutinesFacadeEntry.getKey();
            _builder.newLineIfNotEmpty();
            final ReactionsImportPath includedSegmentImportPath = includedRoutinesFacadeEntry.getValue();
            _builder.newLineIfNotEmpty();
            final String includedRoutinesFacadeFieldName = includedReactionsSegment.getName();
            _builder.newLineIfNotEmpty();
            _builder.append("this.");
            _builder.append(includedRoutinesFacadeFieldName);
            _builder.append(" = ");
            StringConcatenationClient _generateGetRoutinesFacadeCall = RoutineFacadeClassGenerator.this.generateGetRoutinesFacadeCall(includedSegmentImportPath.relativeToRoot());
            _builder.append(_generateGetRoutinesFacadeCall);
            _builder.append(";");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    };
    return _client;
  }

  protected JvmOperation generateCallMethod(final Routine routine) {
    JvmOperation _xblockexpression = null;
    {
      final ClassNameGenerator routineNameGenerator = ClassNamesGenerators.getRoutineClassNameGenerator(routine);
      final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
        it.setVisibility(JvmVisibility.PUBLIC);
        EList<JvmFormalParameter> _parameters = it.getParameters();
        Iterable<JvmFormalParameter> _inputElementsParameter = this.getInputElementsParameter(routine);
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _inputElementsParameter);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            JvmTypeReference _typeRef = RoutineFacadeClassGenerator.this._typeReferenceBuilder.typeRef(RoutineFacadeClassGenerator.this.routinesFacadeNameGenerator.getQualifiedName());
            _builder.append(_typeRef);
            _builder.append(" _routinesFacade = ");
            StringConcatenationClient _generateGetOwnRoutinesFacade = RoutineFacadeClassGenerator.this.generateGetOwnRoutinesFacade();
            _builder.append(_generateGetOwnRoutinesFacade);
            _builder.append(";");
            _builder.newLineIfNotEmpty();
            _builder.append(ReactionExecutionState.class);
            _builder.append(" _executionState = ");
            _builder.append(RoutineFacadeClassGenerator.EXECUTION_STATE_ACCESS_CODE);
            _builder.append(";");
            _builder.newLineIfNotEmpty();
            _builder.append(CallHierarchyHaving.class);
            _builder.append(" _caller = this._getCurrentCaller();");
            _builder.newLineIfNotEmpty();
            JvmTypeReference _typeRef_1 = RoutineFacadeClassGenerator.this._typeReferenceBuilder.typeRef(routineNameGenerator.getQualifiedName());
            _builder.append(_typeRef_1);
            _builder.append(" routine = new ");
            JvmTypeReference _typeRef_2 = RoutineFacadeClassGenerator.this._typeReferenceBuilder.typeRef(routineNameGenerator.getQualifiedName());
            _builder.append(_typeRef_2);
            _builder.append("(_routinesFacade, _executionState, _caller");
            {
              EList<JvmFormalParameter> _parameters = it.getParameters();
              boolean _hasElements = false;
              for(final JvmFormalParameter parameter : _parameters) {
                if (!_hasElements) {
                  _hasElements = true;
                  _builder.append(", ");
                } else {
                  _builder.appendImmediate(", ", "");
                }
                String _name = parameter.getName();
                _builder.append(_name);
              }
            }
            _builder.append(");");
            _builder.newLineIfNotEmpty();
            _builder.append("return routine.execute();");
            _builder.newLine();
          }
        };
        this._typesBuilder.setBody(it, _client);
      };
      _xblockexpression = this._typesBuilder.<JvmOperation>associatePrimary(routine, this._typesBuilder.toMethod(routine, routine.getName(), this._typeReferenceBuilder.typeRef(Boolean.TYPE), _function));
    }
    return _xblockexpression;
  }

  private JvmOperation generateIncludedRoutineCallMethod(final Routine routine, final ReactionsImportPath relativeImportPath) {
    JvmOperation _xblockexpression = null;
    {
      final ClassNameGenerator routinesFacadeNameGenerator = ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(routine.getReactionsSegment());
      final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
        it.setVisibility(JvmVisibility.PUBLIC);
        EList<JvmFormalParameter> _parameters = it.getParameters();
        Iterable<JvmFormalParameter> _inputElementsParameter = this.getInputElementsParameter(routine);
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _inputElementsParameter);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            String _qualifiedName = routinesFacadeNameGenerator.getQualifiedName();
            _builder.append(_qualifiedName);
            _builder.append(" _routinesFacade = ");
            StringConcatenationClient _generateGetRoutinesFacadeCall = RoutineFacadeClassGenerator.this.generateGetRoutinesFacadeCall(relativeImportPath);
            _builder.append(_generateGetRoutinesFacadeCall);
            _builder.append(";");
            _builder.newLineIfNotEmpty();
            _builder.append("return _routinesFacade.");
            String _name = routine.getName();
            _builder.append(_name);
            _builder.append("(");
            {
              EList<JvmFormalParameter> _parameters = it.getParameters();
              boolean _hasElements = false;
              for(final JvmFormalParameter parameter : _parameters) {
                if (!_hasElements) {
                  _hasElements = true;
                } else {
                  _builder.appendImmediate(", ", "");
                }
                String _name_1 = parameter.getName();
                _builder.append(_name_1);
              }
            }
            _builder.append(");");
            _builder.newLineIfNotEmpty();
          }
        };
        this._typesBuilder.setBody(it, _client);
      };
      _xblockexpression = this._typesBuilder.<JvmOperation>associatePrimary(routine, this._typesBuilder.toMethod(routine, routine.getName(), this._typeReferenceBuilder.typeRef(Boolean.TYPE), _function));
    }
    return _xblockexpression;
  }

  private Iterable<JvmFormalParameter> getInputElementsParameter(final Routine routine) {
    return this._parameterGenerator.generateParameters(routine, this._parameterGenerator.getInputElements(routine.getInput().getModelInputElements(), routine.getInput().getJavaInputElements()));
  }

  protected StringConcatenationClient generateGetOwnRoutinesFacade() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("this");
      }
    };
    return _client;
  }

  private StringConcatenationClient generateGetRoutinesFacadeCall(final ReactionsImportPath relativeImportPath) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("this._getRoutinesFacadesProvider().getRoutinesFacade(this._getReactionsImportPath().append(");
        _builder.append(ReactionsImportPath.class);
        _builder.append(".fromPathString(\"");
        String _pathString = relativeImportPath.getPathString();
        _builder.append(_pathString);
        _builder.append("\")))");
      }
    };
    return _client;
  }
}
