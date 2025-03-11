package tools.vitruv.dsls.reactions.codegen.classgenerators;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.io.IOException;
import java.util.List;
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
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.CreateBlockClassGenerator;
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.EmptyStepExecutionClassGenerator;
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.MatchBlockClassGenerator;
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.StepExecutionClassGenerator;
import tools.vitruv.dsls.reactions.codegen.classgenerators.steps.UpdateBlockClassGenerator;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ClassNamesGenerators;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.UpdateBlock;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class RoutineClassGenerator extends ClassGenerator {
  private static final String EXECUTION_STATE_VARIABLE = "getExecutionState()";

  private static final String ROUTINES_FACADE_VARIABLE = "getRoutinesFacade()";

  private static final String INPUT_VALUES_SIMPLE_CLASS_NAME = "InputValues";

  private static final String INPUT_VALUES_FIELD_NAME = "inputValues";

  private static final String RETRIEVED_VALUES_FIELD_NAME = "retrievedValues";

  private static final String CREATED_VALUES_FIELD_NAME = "createdValues";

  private static final String MATCH_SIMPLE_CLASS_NAME = "Match";

  private static final String CREATE_SIMPLE_CLASS_NAME = "Create";

  private static final String UPDATE_SIMPLE_CLASS_NAME = "Update";

  private final Routine routine;

  private final ClassNameGenerator routineClassNameGenerator;

  private final Iterable<AccessibleElement> inputElements;

  private final StepExecutionClassGenerator matchBlockClassGenerator;

  private final StepExecutionClassGenerator createBlockClassGenerator;

  private final StepExecutionClassGenerator updateBlockClassGenerator;

  private final String routinesFacadeQualifiedName;

  private final JvmGenericType inputValuesClass;

  private JvmGenericType generatedClass;

  public RoutineClassGenerator(final Routine routine, final TypesBuilderExtensionProvider typesBuilderExtensionProvider) {
    super(typesBuilderExtensionProvider);
    boolean _isReferenceable = ReactionsElementsCompletionChecker.isReferenceable(routine);
    boolean _not = (!_isReferenceable);
    if (_not) {
      throw new IllegalArgumentException("incomplete");
    }
    this.routine = routine;
    this.routineClassNameGenerator = ClassNamesGenerators.getRoutineClassNameGenerator(routine);
    this.routinesFacadeQualifiedName = ClassNamesGenerators.getRoutinesFacadeClassNameGenerator(routine.getReactionsSegment()).getQualifiedName();
    this.inputElements = this._parameterGenerator.getInputElements(routine.getInput().getModelInputElements(), routine.getInput().getJavaInputElements());
    JvmGenericType _xifexpression = null;
    boolean _hasInputValues = this.hasInputValues();
    if (_hasInputValues) {
      _xifexpression = this.generateElementsContainerClass(RoutineClassGenerator.INPUT_VALUES_SIMPLE_CLASS_NAME, this.inputElements);
    }
    this.inputValuesClass = _xifexpression;
    StepExecutionClassGenerator _xifexpression_1 = null;
    MatchBlock _matchBlock = routine.getMatchBlock();
    boolean _tripleNotEquals = (_matchBlock != null);
    if (_tripleNotEquals) {
      String _nestedClassName = this.getNestedClassName(RoutineClassGenerator.MATCH_SIMPLE_CLASS_NAME);
      MatchBlock _matchBlock_1 = routine.getMatchBlock();
      _xifexpression_1 = new MatchBlockClassGenerator(typesBuilderExtensionProvider, _nestedClassName, _matchBlock_1, this.inputElements);
    } else {
      _xifexpression_1 = new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider);
    }
    this.matchBlockClassGenerator = _xifexpression_1;
    StepExecutionClassGenerator _xifexpression_2 = null;
    CreateBlock _createBlock = routine.getCreateBlock();
    boolean _tripleNotEquals_1 = (_createBlock != null);
    if (_tripleNotEquals_1) {
      String _nestedClassName_1 = this.getNestedClassName(RoutineClassGenerator.CREATE_SIMPLE_CLASS_NAME);
      CreateBlock _createBlock_1 = routine.getCreateBlock();
      _xifexpression_2 = new CreateBlockClassGenerator(typesBuilderExtensionProvider, _nestedClassName_1, _createBlock_1);
    } else {
      _xifexpression_2 = new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider);
    }
    this.createBlockClassGenerator = _xifexpression_2;
    Iterable<AccessibleElement> _newlyAccessibleElementsAfterExecution = this.matchBlockClassGenerator.getNewlyAccessibleElementsAfterExecution();
    Iterable<AccessibleElement> _plus = Iterables.<AccessibleElement>concat(this.inputElements, _newlyAccessibleElementsAfterExecution);
    Iterable<AccessibleElement> _newlyAccessibleElementsAfterExecution_1 = this.createBlockClassGenerator.getNewlyAccessibleElementsAfterExecution();
    final Iterable<AccessibleElement> updateAccessibleElements = Iterables.<AccessibleElement>concat(_plus, _newlyAccessibleElementsAfterExecution_1);
    StepExecutionClassGenerator _xifexpression_3 = null;
    UpdateBlock _updateBlock = routine.getUpdateBlock();
    boolean _tripleNotEquals_2 = (_updateBlock != null);
    if (_tripleNotEquals_2) {
      String _nestedClassName_2 = this.getNestedClassName(RoutineClassGenerator.UPDATE_SIMPLE_CLASS_NAME);
      UpdateBlock _updateBlock_1 = routine.getUpdateBlock();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(this.routinesFacadeQualifiedName);
      _xifexpression_3 = new UpdateBlockClassGenerator(typesBuilderExtensionProvider, _nestedClassName_2, _updateBlock_1, _typeRef, updateAccessibleElements);
    } else {
      _xifexpression_3 = new EmptyStepExecutionClassGenerator(typesBuilderExtensionProvider);
    }
    this.updateBlockClassGenerator = _xifexpression_3;
  }

  private String getNestedClassName(final String nestedClassSimpleName) {
    String _qualifiedName = this.routineClassNameGenerator.getQualifiedName();
    String _plus = (_qualifiedName + ".");
    return (_plus + nestedClassSimpleName);
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    JvmGenericType _xblockexpression = null;
    {
      this.matchBlockClassGenerator.generateEmptyClass();
      this.createBlockClassGenerator.generateEmptyClass();
      this.updateBlockClassGenerator.generateEmptyClass();
      final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
        it.setVisibility(JvmVisibility.PUBLIC);
      };
      _xblockexpression = this.generatedClass = this._typesBuilder.toClass(this.routine, this.routineClassNameGenerator.getQualifiedName(), _function);
    }
    return _xblockexpression;
  }

  @Override
  public JvmGenericType generateBody() {
    JvmGenericType _xblockexpression = null;
    {
      final JvmOperation executeMethod = this.generateMethodExecuteRoutine();
      final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
        this._typesBuilder.setDocumentation(it, this.getCommentWithoutMarkers(this.routine.getDocumentation()));
        EList<JvmTypeReference> _superTypes = it.getSuperTypes();
        JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractRoutine.class);
        this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
        boolean _hasInputValues = this.hasInputValues();
        if (_hasInputValues) {
          EList<JvmMember> _members = it.getMembers();
          JvmField _field = this._typesBuilder.toField(this.routine, RoutineClassGenerator.INPUT_VALUES_FIELD_NAME, this._typeReferenceBuilder.typeRef(this.inputValuesClass));
          this._typesBuilder.<JvmField>operator_add(_members, _field);
        }
        EList<JvmMember> _members_1 = it.getMembers();
        JvmField _xifexpression = null;
        boolean _isEmpty = this.matchBlockClassGenerator.isEmpty();
        boolean _not = (!_isEmpty);
        if (_not) {
          _xifexpression = this._typesBuilder.toField(this.routine, RoutineClassGenerator.RETRIEVED_VALUES_FIELD_NAME, 
            this._typeReferenceBuilder.typeRef(this.matchBlockClassGenerator.getNewlyAccessibleElementsContainerType()));
        }
        this._typesBuilder.<JvmField>operator_add(_members_1, _xifexpression);
        EList<JvmMember> _members_2 = it.getMembers();
        JvmField _xifexpression_1 = null;
        boolean _isEmpty_1 = this.createBlockClassGenerator.isEmpty();
        boolean _not_1 = (!_isEmpty_1);
        if (_not_1) {
          _xifexpression_1 = this._typesBuilder.toField(this.routine, RoutineClassGenerator.CREATED_VALUES_FIELD_NAME, 
            this._typeReferenceBuilder.typeRef(this.createBlockClassGenerator.getNewlyAccessibleElementsContainerType()));
        }
        this._typesBuilder.<JvmField>operator_add(_members_2, _xifexpression_1);
        EList<JvmMember> _members_3 = it.getMembers();
        this._typesBuilder.<JvmGenericType>operator_add(_members_3, this.inputValuesClass);
        EList<JvmMember> _members_4 = it.getMembers();
        JvmGenericType _generateBody = this.matchBlockClassGenerator.generateBody();
        this._typesBuilder.<JvmGenericType>operator_add(_members_4, _generateBody);
        EList<JvmMember> _members_5 = it.getMembers();
        JvmGenericType _generateBody_1 = this.createBlockClassGenerator.generateBody();
        this._typesBuilder.<JvmGenericType>operator_add(_members_5, _generateBody_1);
        EList<JvmMember> _members_6 = it.getMembers();
        JvmGenericType _generateBody_2 = this.updateBlockClassGenerator.generateBody();
        this._typesBuilder.<JvmGenericType>operator_add(_members_6, _generateBody_2);
        EList<JvmMember> _members_7 = it.getMembers();
        JvmConstructor _generateConstructor = this.generateConstructor(this.routine);
        this._typesBuilder.<JvmConstructor>operator_add(_members_7, _generateConstructor);
        EList<JvmMember> _members_8 = it.getMembers();
        this._typesBuilder.<JvmOperation>operator_add(_members_8, executeMethod);
      };
      _xblockexpression = ObjectExtensions.<JvmGenericType>operator_doubleArrow(
        this.generatedClass, _function);
    }
    return _xblockexpression;
  }

  private boolean hasInputValues() {
    boolean _isEmpty = IterableExtensions.isEmpty(this.inputElements);
    return (!_isEmpty);
  }

  private JvmConstructor generateConstructor(final Routine routine) {
    final Procedure1<JvmConstructor> _function = (JvmConstructor it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
      AccessibleElement _accessibleElement = new AccessibleElement("routinesFacade", this.routinesFacadeQualifiedName);
      final JvmFormalParameter routinesFacadeParameter = this._parameterGenerator.generateParameter(it, _accessibleElement);
      AccessibleElement _accessibleElement_1 = new AccessibleElement("reactionExecutionState", ReactionExecutionState.class);
      final JvmFormalParameter executionStateParameter = this._parameterGenerator.generateParameter(it, _accessibleElement_1);
      AccessibleElement _accessibleElement_2 = new AccessibleElement("calledBy", CallHierarchyHaving.class);
      final JvmFormalParameter calledByParameter = this._parameterGenerator.generateParameter(it, _accessibleElement_2);
      final Iterable<JvmFormalParameter> inputParameters = this._parameterGenerator.generateParameters(routine, this.inputElements);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, routinesFacadeParameter);
      EList<JvmFormalParameter> _parameters_1 = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_1, executionStateParameter);
      EList<JvmFormalParameter> _parameters_2 = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_2, calledByParameter);
      EList<JvmFormalParameter> _parameters_3 = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_3, inputParameters);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("super(");
          String _name = routinesFacadeParameter.getName();
          _builder.append(_name);
          _builder.append(", ");
          String _name_1 = executionStateParameter.getName();
          _builder.append(_name_1);
          _builder.append(", ");
          String _name_2 = calledByParameter.getName();
          _builder.append(_name_2);
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          {
            boolean _hasInputValues = RoutineClassGenerator.this.hasInputValues();
            if (_hasInputValues) {
              _builder.append("this.");
              _builder.append(RoutineClassGenerator.INPUT_VALUES_FIELD_NAME);
              _builder.append(" = new ");
              _builder.append(RoutineClassGenerator.this.inputValuesClass);
              _builder.append("(");
              {
                boolean _hasElements = false;
                for(final JvmFormalParameter inputParameter : inputParameters) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(", ", "");
                  }
                  String _name_3 = inputParameter.getName();
                  _builder.append(_name_3);
                }
              }
              _builder.append(");");
              _builder.newLineIfNotEmpty();
            }
          }
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.toConstructor(routine, _function);
  }

  private JvmOperation generateMethodExecuteRoutine() {
    final String methodName = "executeRoutine";
    final Function1<AccessibleElement, String> _function = (AccessibleElement it) -> {
      String _name = it.getName();
      return ((RoutineClassGenerator.INPUT_VALUES_FIELD_NAME + ".") + _name);
    };
    final List<String> inputElements = IterableUtil.<AccessibleElement, String>mapFixed(this.inputElements, _function);
    final Function1<AccessibleElement, String> _function_1 = (AccessibleElement it) -> {
      String _name = it.getName();
      return ((RoutineClassGenerator.RETRIEVED_VALUES_FIELD_NAME + ".") + _name);
    };
    List<String> _mapFixed = IterableUtil.<AccessibleElement, String>mapFixed(this.matchBlockClassGenerator.getNewlyAccessibleElementsAfterExecution(), _function_1);
    final Iterable<String> inputAndRetrievedElements = Iterables.<String>concat(inputElements, _mapFixed);
    final Function1<AccessibleElement, String> _function_2 = (AccessibleElement it) -> {
      String _name = it.getName();
      return ((RoutineClassGenerator.CREATED_VALUES_FIELD_NAME + ".") + _name);
    };
    List<String> _mapFixed_1 = IterableUtil.<AccessibleElement, String>mapFixed(this.createBlockClassGenerator.getNewlyAccessibleElementsAfterExecution(), _function_2);
    final Iterable<String> inputAndRetrievedAndCreatedElements = Iterables.<String>concat(inputAndRetrievedElements, _mapFixed_1);
    final Procedure1<JvmOperation> _function_3 = (JvmOperation it) -> {
      it.setVisibility(JvmVisibility.PROTECTED);
      EList<JvmTypeReference> _exceptions = it.getExceptions();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(IOException.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_exceptions, _typeRef);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          StringConcatenationClient _generateDebugCode = RoutineClassGenerator.this.generateDebugCode(inputElements);
          _builder.append(_generateDebugCode);
          _builder.newLineIfNotEmpty();
          StringConcatenationClient _generateMatchCode = RoutineClassGenerator.this.generateMatchCode(inputElements);
          _builder.append(_generateMatchCode);
          _builder.newLineIfNotEmpty();
          StringConcatenationClient _generateCreateCode = RoutineClassGenerator.this.generateCreateCode(inputAndRetrievedElements);
          _builder.append(_generateCreateCode);
          _builder.newLineIfNotEmpty();
          StringConcatenationClient _generateUpdateCode = RoutineClassGenerator.this.generateUpdateCode(inputAndRetrievedAndCreatedElements);
          _builder.append(_generateUpdateCode);
          _builder.newLineIfNotEmpty();
          _builder.append("return true;");
          _builder.newLine();
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.generateUnassociatedMethod(methodName, this._typeReferenceBuilder.typeRef(Boolean.TYPE), _function_3);
  }

  private StringConcatenationClient generateDebugCode(final Iterable<String> inputElementsAccessExpressions) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("if (getLogger().isTraceEnabled()) {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("getLogger().trace(\"Called routine ");
        String _simpleName = RoutineClassGenerator.this.routineClassNameGenerator.getSimpleName();
        _builder.append(_simpleName, "\t");
        _builder.append(" with input:\");");
        _builder.newLineIfNotEmpty();
        {
          for(final String inputElementAccessExpression : inputElementsAccessExpressions) {
            _builder.append("\t");
            _builder.append("getLogger().trace(\"   ");
            _builder.append(inputElementAccessExpression, "\t");
            _builder.append(": \" + ");
            _builder.append(inputElementAccessExpression, "\t");
            _builder.append(");");
            _builder.newLineIfNotEmpty();
          }
        }
        _builder.append("}");
        _builder.newLine();
      }
    };
    return _client;
  }

  private StringConcatenationClient generateMatchCode(final Iterable<String> accessibleElementsAccessExpressions) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(RoutineClassGenerator.RETRIEVED_VALUES_FIELD_NAME);
        _builder.append(" = ");
      }
    };
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("if (");
        _builder.append(RoutineClassGenerator.RETRIEVED_VALUES_FIELD_NAME);
        _builder.append(" == null) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("return false;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    return this.matchBlockClassGenerator.generateStepExecutionCode(_client, RoutineClassGenerator.EXECUTION_STATE_VARIABLE, RoutineClassGenerator.ROUTINES_FACADE_VARIABLE, accessibleElementsAccessExpressions, _client_1);
  }

  private StringConcatenationClient generateCreateCode(final Iterable<String> accessibleElementsAccessExpressions) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(RoutineClassGenerator.CREATED_VALUES_FIELD_NAME);
        _builder.append(" = ");
      }
    };
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
      }
    };
    return this.createBlockClassGenerator.generateStepExecutionCode(_client, 
      RoutineClassGenerator.EXECUTION_STATE_VARIABLE, 
      RoutineClassGenerator.ROUTINES_FACADE_VARIABLE, 
      null, _client_1);
  }

  private StringConcatenationClient generateUpdateCode(final Iterable<String> accessibleElementsAccessExpressions) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
      }
    };
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
      }
    };
    return this.updateBlockClassGenerator.generateStepExecutionCode(_client, 
      RoutineClassGenerator.EXECUTION_STATE_VARIABLE, 
      RoutineClassGenerator.ROUTINES_FACADE_VARIABLE, accessibleElementsAccessExpressions, _client_1);
  }
}
