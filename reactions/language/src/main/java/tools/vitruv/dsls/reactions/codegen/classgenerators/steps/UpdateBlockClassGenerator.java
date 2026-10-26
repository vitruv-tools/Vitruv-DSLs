package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.CALL_BLOCK_FACADE_PARAMETER_NAME;

import java.util.List;
import java.util.function.UnaryOperator;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.CodeExecutionBlock;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Generates for an {@link CodeExecutionBlock} of a routine a class with a method (with the name defined in
 * {@link #UPDATE_MODELS_METHOD_NAME}) that accepts the specified accessible elements and performs the
 * given update code on them.
 */
public class UpdateBlockClassGenerator extends StepExecutionClassGenerator {
  private static final String UPDATE_MODELS_METHOD_NAME = "updateModels";
  private static final String ROUTINES_FACADE_CLASS_PARAMETER_NAME = CALL_BLOCK_FACADE_PARAMETER_NAME;
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
   * @param qualifiedClassName
   *    the qualified name of the class to create, may not be {@code null} or empty
   * @param updateBlock
   *    the code block with update code to create a class for, must not be {@code null}
   * @param routinesFacadeClassReference
   *    a type reference to the facade class for calling other routines
   * @param accessibleElements
   *    the elements to be passed to the generated {@code match} method, must not be {@code null}
   */
  public UpdateBlockClassGenerator(
      TypesBuilderExtensionProvider typesBuilderExtensionProvider,
      String qualifiedClassName,
      CodeExecutionBlock updateBlock,
      JvmTypeReference routinesFacadeClassReference,
      Iterable<AccessibleElement> accessibleElements) {
    super(typesBuilderExtensionProvider);
    checkArgument(qualifiedClassName != null && !qualifiedClassName.isEmpty(),
        "class name must not be null or empty");
    this.qualifiedClassName = qualifiedClassName;
    this.updateBlock = checkNotNull(updateBlock, "update block must not be null");
    this.routinesFacadeClassReference = checkNotNull(routinesFacadeClassReference,
      "facade class reference must not be null");
    this.accessibleElements = checkNotNull(accessibleElements,
        "accessible elements must not be null");
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    generatedClass = _typesBuilder.toClass(
        updateBlock, qualifiedClassName, clazz -> {
      clazz.setVisibility(JvmVisibility.PRIVATE);
      clazz.setStatic(true);
    });
    return generatedClass;
  }

  @Override
  public JvmGenericType generateBody() {
    UnaryOperator<JvmGenericType> addClassMembers = emptyClazz -> {
      _typesBuilder.operator_add(
          emptyClazz.getSuperTypes(),
          _typeReferenceBuilder.typeRef(AbstractRoutine.Update.class)
      );
      EList<JvmMember> members = emptyClazz.getMembers();
      _typesBuilder.operator_add(members, generateConstructor());
      _typesBuilder.operator_add(members, generateUpdateMethod());
      return emptyClazz;
    };
    return addClassMembers.apply(this.generatedClass);
  }

  private JvmConstructor generateConstructor() {
    Procedure1<JvmConstructor> constructorProcedure = (JvmConstructor constructor) -> {
      AccessibleElement element =
          new AccessibleElement("reactionExecutionState", ReactionExecutionState.class);
      JvmFormalParameter reactionExecutionStateParameter =
          _parameterGenerator.generateParameter(constructor, element);

      _typesBuilder.<JvmFormalParameter>operator_add(constructor.getParameters(), reactionExecutionStateParameter);
      _typesBuilder.setBody(constructor, new StringConcatenationClient() {
        @Override
        protected void appendTo(TargetStringConcatenation builder) {
          builder.append("super(");
          builder.append(reactionExecutionStateParameter.getName());
          builder.append(");");
        }
      });
    };
    return _typesBuilder.toConstructor(updateBlock, constructorProcedure);
  }

  private JvmOperation generateUpdateMethod() {
    final Procedure1<JvmOperation> updateProcedure = (JvmOperation updateMethod) -> {
      EList<JvmFormalParameter> parameters = updateMethod.getParameters();
      _typesBuilder.<JvmFormalParameter>operator_add(parameters,
          generateAccessibleElementsParameters(updateMethod, accessibleElements));
      JvmFormalParameter facadeParam =
          _typesBuilder.toParameter(updateMethod,
              UpdateBlockClassGenerator.ROUTINES_FACADE_CLASS_PARAMETER_NAME,
              routinesFacadeClassReference
          );

      _typesBuilder.operator_add(facadeParam.getAnnotations(), _annotationTypesBuilder.annotationRef(Extension.class));
      _typesBuilder.operator_add(parameters, facadeParam);
      _typesBuilder.setBody(updateMethod, updateBlock != null ? updateBlock.getCode() : null);
    };
    return _typesBuilder.toMethod(
        updateBlock,
        UpdateBlockClassGenerator.UPDATE_MODELS_METHOD_NAME,
        _typeReferenceBuilder.typeRef(Void.TYPE),
        updateProcedure
    );
  }

  @Override
  public StringConcatenationClient generateStepExecutionCode(
      StringConcatenationClient prefix,
      String executionStateArgument,
      String routineFacadeArgument,
      Iterable<String> accessibleElementArguments,
      StringConcatenationClient suffix) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation builder) {
        builder.append(prefix);
        builder.append("new ");
        builder.append(UpdateBlockClassGenerator.this.qualifiedClassName);
        builder.append("(");
        builder.append(executionStateArgument);
        builder.append(").");
        builder.append(UpdateBlockClassGenerator.UPDATE_MODELS_METHOD_NAME);
        builder.append("(");
        {
          boolean _hasElements = false;
          for(final String argument : accessibleElementArguments) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              builder.appendImmediate(", ", "");
            }
            builder.append(argument);
          }
          if (_hasElements) {
            builder.append(", ");
          }
        }
        builder.append(routineFacadeArgument);
        builder.append(");");
        builder.newLineIfNotEmpty();
        builder.append(suffix);
        builder.newLineIfNotEmpty();
      }
    };
  }

  @Override
  public Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
    return List.of();
  }

  @Override
  public JvmGenericType getNewlyAccessibleElementsContainerType() {
    return null;
  }
}
