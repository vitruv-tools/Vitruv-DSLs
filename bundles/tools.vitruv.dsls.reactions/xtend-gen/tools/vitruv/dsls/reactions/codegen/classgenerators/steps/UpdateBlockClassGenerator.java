package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import com.google.common.base.Preconditions;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.CodeExecutionBlock;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Generates for an {@link UpdateBlock} of a routine a class with a method (with the name defined in
 * {@link #UPDATE_MODELS_METHOD_NAME}) that accepts the specified accessible elements and performs the
 * given update code on them.
 */
@SuppressWarnings("all")
public class UpdateBlockClassGenerator extends StepExecutionClassGenerator {
  private static final String UPDATE_MODELS_METHOD_NAME = "updateModels";

  private static final String ROUTINES_FACADE_CLASS_PARAMETER_NAME = ReactionsLanguageConstants.CALL_BLOCK_FACADE_PARAMETER_NAME;

  private final String qualifiedClassName;

  private final CodeExecutionBlock updateBlock;

  private final JvmTypeReference routinesFacadeClassReference;

  private final Iterable<AccessibleElement> accessibleElements;

  private JvmGenericType generatedClass;

  /**
   * Create a class generator for the given routine update block. The generated class has
   * the given qualified name and accepts the given accessible elements as parameters in the
   * provided {@code updateModels} method.
   * 
   * @param typesBuilderExtensionProvider the Xtext types builder, must not be {@code null}
   * @param qualifiedClassName the qualified name of the class to create, may not be {@code null} or empty
   * @param updateBlock the code block with update code to create a class for, must not be {@code null}
   * @param routinesFacadeClassReference a type reference to the facade class for calling other routines
   * @param accessibleElements the elements to be passed to the generated {@code match} method, must not be {@code null}
   */
  public UpdateBlockClassGenerator(final TypesBuilderExtensionProvider typesBuilderExtensionProvider, final String qualifiedClassName, final CodeExecutionBlock updateBlock, final JvmTypeReference routinesFacadeClassReference, final Iterable<AccessibleElement> accessibleElements) {
    super(typesBuilderExtensionProvider);
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(qualifiedClassName);
    boolean _not = (!_isNullOrEmpty);
    Preconditions.checkArgument(_not, "class name must not be null or empty");
    this.qualifiedClassName = qualifiedClassName;
    this.updateBlock = Preconditions.<CodeExecutionBlock>checkNotNull(updateBlock, "update block must not be null");
    this.routinesFacadeClassReference = Preconditions.<JvmTypeReference>checkNotNull(routinesFacadeClassReference, 
      "facade class reference must not be null");
    this.accessibleElements = Preconditions.<Iterable<AccessibleElement>>checkNotNull(accessibleElements, "accessible elements must not be null");
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      it.setVisibility(JvmVisibility.PRIVATE);
      it.setStatic(true);
    };
    this.generatedClass = this._typesBuilder.toClass(this.updateBlock, this.qualifiedClassName, _function);
    return this.generatedClass;
  }

  @Override
  public JvmGenericType generateBody() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      EList<JvmTypeReference> _superTypes = it.getSuperTypes();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractRoutine.Update.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
      EList<JvmMember> _members = it.getMembers();
      JvmConstructor _generateConstructor = this.generateConstructor();
      this._typesBuilder.<JvmConstructor>operator_add(_members, _generateConstructor);
      EList<JvmMember> _members_1 = it.getMembers();
      JvmOperation _generateUpdateMethod = this.generateUpdateMethod();
      this._typesBuilder.<JvmOperation>operator_add(_members_1, _generateUpdateMethod);
    };
    return ObjectExtensions.<JvmGenericType>operator_doubleArrow(
      this.generatedClass, _function);
  }

  private JvmConstructor generateConstructor() {
    final Procedure1<JvmConstructor> _function = (JvmConstructor it) -> {
      AccessibleElement _accessibleElement = new AccessibleElement("reactionExecutionState", ReactionExecutionState.class);
      final JvmFormalParameter reactionExecutionStateParameter = this._parameterGenerator.generateParameter(it, _accessibleElement);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, reactionExecutionStateParameter);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("super(");
          String _name = reactionExecutionStateParameter.getName();
          _builder.append(_name);
          _builder.append(");");
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.toConstructor(this.updateBlock, _function);
  }

  private JvmOperation generateUpdateMethod() {
    final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      EList<JvmFormalParameter> _parameters = it.getParameters();
      Iterable<JvmFormalParameter> _generateAccessibleElementsParameters = this.generateAccessibleElementsParameters(it, this.accessibleElements);
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _generateAccessibleElementsParameters);
      final JvmFormalParameter facadeParam = this._typesBuilder.toParameter(it, UpdateBlockClassGenerator.ROUTINES_FACADE_CLASS_PARAMETER_NAME, this.routinesFacadeClassReference);
      EList<JvmAnnotationReference> _annotations = facadeParam.getAnnotations();
      JvmAnnotationReference _annotationRef = this._annotationTypesBuilder.annotationRef(Extension.class);
      this._typesBuilder.<JvmAnnotationReference>operator_add(_annotations, _annotationRef);
      EList<JvmFormalParameter> _parameters_1 = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_1, facadeParam);
      XExpression _code = null;
      if (this.updateBlock!=null) {
        _code=this.updateBlock.getCode();
      }
      this._typesBuilder.setBody(it, _code);
    };
    return this._typesBuilder.toMethod(this.updateBlock, UpdateBlockClassGenerator.UPDATE_MODELS_METHOD_NAME, this._typeReferenceBuilder.typeRef(Void.TYPE), _function);
  }

  @Override
  public StringConcatenationClient generateStepExecutionCode(final StringConcatenationClient prefix, final String executionStateArgument, final String routineFacadeArgument, final Iterable<String> accessibleElementArguments, final StringConcatenationClient suffix) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(prefix);
        _builder.append("new ");
        _builder.append(UpdateBlockClassGenerator.this.qualifiedClassName);
        _builder.append("(");
        _builder.append(executionStateArgument);
        _builder.append(").");
        _builder.append(UpdateBlockClassGenerator.UPDATE_MODELS_METHOD_NAME);
        _builder.append("(");
        {
          boolean _hasElements = false;
          for(final String argument : accessibleElementArguments) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              _builder.appendImmediate(", ", "");
            }
            _builder.append(argument);
          }
          if (_hasElements) {
            _builder.append(", ");
          }
        }
        _builder.append(routineFacadeArgument);
        _builder.append(");");
        _builder.newLineIfNotEmpty();
        _builder.append(suffix);
        _builder.newLineIfNotEmpty();
      }
    };
    return _client;
  }

  @Override
  public Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
    return CollectionLiterals.<AccessibleElement>emptyList();
  }

  @Override
  public JvmGenericType getNewlyAccessibleElementsContainerType() {
    return null;
  }
}
