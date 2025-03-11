package tools.vitruv.dsls.reactions.codegen.classgenerators;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.function.Function;
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
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.codegen.changetyperepresentation.ChangeTypeRepresentation;
import tools.vitruv.dsls.reactions.codegen.changetyperepresentation.ChangeTypeRepresentationExtractor;
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.EmptyStepExecutionClassGenerator;
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.StepExecutionClassGenerator;
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.UpdateBlockClassGenerator;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.Reaction;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineCall;
import tools.vitruv.dsls.reactions.language.toplevelelements.Trigger;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class ReactionClassGenerator extends ClassGenerator {
  private static final String EXECUTION_STATE_VARIABLE = "executionState";

  private static final String ROUTINES_FACADE_VARIABLE = "routinesFacade";

  private static final String EXECUTE_REACTION_METHOD_NAME = "executeReaction";

  private static final String MATCH_CHANGE_METHOD_NAME = "isCurrentChangeMatchingTrigger";

  private static final String USER_DEFINED_PRECONDITION_METHOD_NAME = "isUserDefinedPreconditionFulfilled";

  private final Reaction reaction;

  private final ChangeTypeRepresentation changeType;

  private final String reactionClassQualifiedName;

  private final StepExecutionClassGenerator routineCallClassGenerator;

  private JvmGenericType generatedClass;

  public ReactionClassGenerator(final Reaction reaction, final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    super(typesBuilderExtensionProvider);
    Preconditions.checkArgument((reaction != null), "reaction must not be null");
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(reaction.getName());
    boolean _not = (!_isNullOrEmpty);
    Preconditions.checkArgument(_not, "reaction must have a name");
    Trigger _trigger = reaction.getTrigger();
    boolean _tripleNotEquals = (_trigger != null);
    Preconditions.checkArgument(_tripleNotEquals, "reaction must have a defined trigger");
    this.reaction = reaction;
    this.changeType = ChangeTypeRepresentationExtractor.extractChangeType(reaction.getTrigger());
    this.reactionClassQualifiedName = ClassNamesGenerators.getReactionClassNameGenerator(reaction).getQualifiedName();
    StepExecutionClassGenerator _xifexpression = null;
    RoutineCall _callRoutine = reaction.getCallRoutine();
    boolean _tripleNotEquals_1 = (_callRoutine != null);
    if (_tripleNotEquals_1) {
      UpdateBlockClassGenerator _xblockexpression = null;
      {
        final String routinesFacadeClassName = ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(reaction.getReactionsSegment()).getQualifiedName();
        RoutineCall _callRoutine_1 = reaction.getCallRoutine();
        JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(routinesFacadeClassName);
        Iterable<AccessibleElement> _generatePropertiesParameterList = this.changeType.generatePropertiesParameterList();
        _xblockexpression = new UpdateBlockClassGenerator(typesBuilderExtensionProvider, (this.reactionClassQualifiedName + ".Call"), _callRoutine_1, _typeRef, _generatePropertiesParameterList);
      }
      _xifexpression = _xblockexpression;
    } else {
      _xifexpression = new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider);
    }
    this.routineCallClassGenerator = _xifexpression;
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    JvmGenericType _xblockexpression = null;
    {
      this.routineCallClassGenerator.generateEmptyClass();
      final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
        it.setVisibility(JvmVisibility.PUBLIC);
      };
      _xblockexpression = this.generatedClass = this._typesBuilder.toClass(this.reaction, this.reactionClassQualifiedName, _function);
    }
    return _xblockexpression;
  }

  @Override
  public JvmGenericType generateBody() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      this._typesBuilder.setDocumentation(it, this.getCommentWithoutMarkers(this.reaction.getDocumentation()));
      EList<JvmTypeReference> _superTypes = it.getSuperTypes();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractReaction.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
      EList<JvmMember> _members = it.getMembers();
      JvmField _field = this._typesBuilder.toField(this.reaction, this.changeType.getName(), this.changeType.getAccessibleElement().generateTypeRef(this._typeReferenceBuilder));
      this._typesBuilder.<JvmField>operator_add(_members, _field);
      EList<JvmMember> _members_1 = it.getMembers();
      JvmConstructor _generateConstructor = this.generateConstructor(this.reaction);
      this._typesBuilder.<JvmConstructor>operator_add(_members_1, _generateConstructor);
      EList<JvmMember> _members_2 = it.getMembers();
      JvmGenericType _generateBody = this.routineCallClassGenerator.generateBody();
      this._typesBuilder.<JvmGenericType>operator_add(_members_2, _generateBody);
      EList<JvmMember> _members_3 = it.getMembers();
      Iterable<JvmOperation> _generateMethodExecuteReactionAndDependentMethods = this.generateMethodExecuteReactionAndDependentMethods();
      this._typesBuilder.<JvmMember>operator_add(_members_3, _generateMethodExecuteReactionAndDependentMethods);
    };
    return ObjectExtensions.<JvmGenericType>operator_doubleArrow(
      this.generatedClass, _function);
  }

  private JvmConstructor generateConstructor(final Reaction reaction) {
    final Procedure1<JvmConstructor> _function = (JvmConstructor it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
      String _name = Function.class.getName();
      String _name_1 = ReactionExecutionState.class.getName();
      String _name_2 = RoutinesFacade.class.getName();
      AccessibleElement _accessibleElement = new AccessibleElement((ReactionClassGenerator.ROUTINES_FACADE_VARIABLE + "Generator"), _name, _name_1, _name_2);
      final JvmFormalParameter routinesFacadeParameter = this._parameterGenerator.generateParameter(it, _accessibleElement);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, routinesFacadeParameter);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("super(");
          String _name = routinesFacadeParameter.getName();
          _builder.append(_name);
          _builder.append(");");
          _builder.newLineIfNotEmpty();
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.toConstructor(reaction, _function);
  }

  private Iterable<JvmOperation> generateMethodExecuteReactionAndDependentMethods() {
    final JvmOperation matchChangeMethod = this.generateMatchChangeMethod();
    final JvmOperation userDefinedPreconditionMethod = this.generateUserDefinedPreconditionMethod();
    final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
      AccessibleElement _accessibleElement = new AccessibleElement("change", EChange.class);
      final JvmFormalParameter changeParameter = this._parameterGenerator.generateParameter(it, _accessibleElement);
      AccessibleElement _accessibleElement_1 = new AccessibleElement(ReactionClassGenerator.EXECUTION_STATE_VARIABLE, ReactionExecutionState.class);
      final JvmFormalParameter reactionExecutionStateParameter = this._parameterGenerator.generateParameter(it, _accessibleElement_1);
      AccessibleElement _accessibleElement_2 = new AccessibleElement((ReactionClassGenerator.ROUTINES_FACADE_VARIABLE + "Untyped"), RoutinesFacade.class);
      final JvmFormalParameter routinesFacadeParameter = this._parameterGenerator.generateParameter(it, _accessibleElement_2);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, changeParameter);
      EList<JvmFormalParameter> _parameters_1 = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_1, reactionExecutionStateParameter);
      EList<JvmFormalParameter> _parameters_2 = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_2, routinesFacadeParameter);
      final String facadeClassName = ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(this.reaction.getReactionsSegment()).getQualifiedName();
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append(facadeClassName);
          _builder.append(" ");
          _builder.append(ReactionClassGenerator.ROUTINES_FACADE_VARIABLE);
          _builder.append(" = (");
          _builder.append(facadeClassName);
          _builder.append(")");
          _builder.append(ReactionClassGenerator.ROUTINES_FACADE_VARIABLE);
          _builder.append("Untyped;");
          _builder.newLineIfNotEmpty();
          StringConcatenationClient _generateMatchChangeMethodCallCode = ReactionClassGenerator.this.generateMatchChangeMethodCallCode(matchChangeMethod, changeParameter.getName());
          _builder.append(_generateMatchChangeMethodCallCode);
          _builder.newLineIfNotEmpty();
          StringConcatenationClient _generatePropertiesAssignmentCode = ReactionClassGenerator.this.changeType.generatePropertiesAssignmentCode();
          _builder.append(_generatePropertiesAssignmentCode);
          _builder.newLineIfNotEmpty();
          StringConcatenationClient _generateUserDefinedPreconditionMethodCall = ReactionClassGenerator.this.generateUserDefinedPreconditionMethodCall(userDefinedPreconditionMethod);
          _builder.append(_generateUserDefinedPreconditionMethodCall);
          _builder.newLineIfNotEmpty();
          _builder.append("if (getLogger().isTraceEnabled()) {");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("getLogger().trace(\"Passed complete precondition check of Reaction \" + this.getClass().getName());");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          StringConcatenationClient _generateCallRoutineCode = ReactionClassGenerator.this.generateCallRoutineCode();
          _builder.append(_generateCallRoutineCode);
          _builder.newLineIfNotEmpty();
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    final JvmOperation executeReactionMethod = this._typesBuilder.toMethod(this.reaction, ReactionClassGenerator.EXECUTE_REACTION_METHOD_NAME, this._typeReferenceBuilder.typeRef(Void.TYPE), _function);
    return IterableExtensions.<JvmOperation>filterNull(Collections.<JvmOperation>unmodifiableList(CollectionLiterals.<JvmOperation>newArrayList(matchChangeMethod, userDefinedPreconditionMethod, executeReactionMethod)));
  }

  private StringConcatenationClient generateCallRoutineCode() {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
      }
    };
    final Function1<AccessibleElement, String> _function = (AccessibleElement it) -> {
      return it.getName();
    };
    Iterable<String> _map = IterableExtensions.<AccessibleElement, String>map(this.changeType.generatePropertiesParameterList(), _function);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
      }
    };
    return this.routineCallClassGenerator.generateStepExecutionCode(_client, 
      ReactionClassGenerator.EXECUTION_STATE_VARIABLE, 
      ReactionClassGenerator.ROUTINES_FACADE_VARIABLE, _map, _client_1);
  }

  private StringConcatenationClient generateMatchChangeMethodCallCode(final JvmOperation matchChangeMethod, final String changeParameterName) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("if (!");
        String _simpleName = matchChangeMethod.getSimpleName();
        _builder.append(_simpleName);
        _builder.append("(");
        _builder.append(changeParameterName);
        _builder.append(")) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("return;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    return _client;
  }

  private JvmOperation generateMatchChangeMethod() {
    final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      AccessibleElement _accessibleElement = new AccessibleElement("change", EChange.class);
      final JvmFormalParameter changeParameter = this._parameterGenerator.generateParameter(it, _accessibleElement);
      it.setVisibility(JvmVisibility.PUBLIC);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, changeParameter);
      this._typesBuilder.setBody(it, this.changeType.generateCheckMethodBody(changeParameter.getName()));
    };
    return this._typesBuilder.toMethod(this.reaction.getTrigger(), ReactionClassGenerator.MATCH_CHANGE_METHOD_NAME, this._typeReferenceBuilder.typeRef(Boolean.TYPE), _function);
  }

  private StringConcatenationClient generateUserDefinedPreconditionMethodCall(final JvmOperation userDefinedPreconditionMethod) {
    StringConcatenationClient _xifexpression = null;
    boolean _hasUserDefinedPrecondition = this.hasUserDefinedPrecondition();
    if (_hasUserDefinedPrecondition) {
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("if (getLogger().isTraceEnabled()) {");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("getLogger().trace(\"Passed change matching of Reaction \" + this.getClass().getName());");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
          _builder.append("if (!");
          String _simpleName = userDefinedPreconditionMethod.getSimpleName();
          _builder.append(_simpleName);
          _builder.append("(");
          {
            final Function1<AccessibleElement, String> _function = (AccessibleElement it) -> {
              return it.getName();
            };
            Iterable<String> _map = IterableExtensions.<AccessibleElement, String>map(ReactionClassGenerator.this.changeType.generatePropertiesParameterList(), _function);
            boolean _hasElements = false;
            for(final String argument : _map) {
              if (!_hasElements) {
                _hasElements = true;
              } else {
                _builder.appendImmediate(", ", "");
              }
              _builder.append(argument);
            }
          }
          _builder.append(")) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("return;");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      _xifexpression = _client;
    } else {
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        }
      };
      _xifexpression = _client_1;
    }
    return _xifexpression;
  }

  private JvmOperation generateUserDefinedPreconditionMethod() {
    boolean _hasUserDefinedPrecondition = this.hasUserDefinedPrecondition();
    boolean _not = (!_hasUserDefinedPrecondition);
    if (_not) {
      return null;
    }
    final XExpression precondition = this.reaction.getTrigger().getPrecondition();
    final String methodName = ReactionClassGenerator.USER_DEFINED_PRECONDITION_METHOD_NAME;
    final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      it.setVisibility(JvmVisibility.PRIVATE);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      Iterable<JvmFormalParameter> _generateAccessibleElementsParameters = this.generateAccessibleElementsParameters(it, this.changeType.generatePropertiesParameterList());
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _generateAccessibleElementsParameters);
      this._typesBuilder.setBody(it, precondition);
    };
    return this._typesBuilder.toMethod(this.reaction.getTrigger(), methodName, this._typeReferenceBuilder.typeRef(Boolean.TYPE), _function);
  }

  private boolean hasUserDefinedPrecondition() {
    Trigger _trigger = this.reaction.getTrigger();
    XExpression _precondition = null;
    if (_trigger!=null) {
      _precondition=_trigger.getPrecondition();
    }
    return (_precondition != null);
  }
}
