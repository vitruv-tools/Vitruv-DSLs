package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import java.util.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.xbase.lib.Procedures;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtend2.lib.StringConcatenationClient;

import static tools.vitruv.dsls.reactions.codegen.helper.ReactionsElementsCompletionChecker.nullOrEmpty;
import static tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.getRuntimeClassName;
import static tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper.getJavaClassName;
import static edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil.mapFixed;
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Generates for a {@link CreateBlock} of a routine a class providing a creation method (with the name defined in {@link #CREATE_ELEMENTS_METHOD_NAME})
 * that returns the created elements.
 */
public class CreateBlockClassGenerator extends StepExecutionClassGenerator {
  private static final String PREDEFINED_CREATE_OBJECT_METHOD_NAME = "createObject";

  private static final String MISSING_NAME = "/* Name missing */";
  private static final String MISSING_TYPE = "/* Type missing */";
  private static final String CREATED_ELEMENTS_SIMPLE_CLASS_NAME = "CreatedValues";
  private static final String CREATE_ELEMENTS_METHOD_NAME = "createElements";

  private final String qualifiedClassName;
  private final CreateBlock createBlock;

  private final JvmGenericType createdElementsClass;
  private JvmGenericType generatedClass;

  /**
   * Create a class generator for the given routine creator block. The generated class has
   * the given qualified name and provides the {@code createElements} method to create and return
   * the defined elements.
   *
   * @param typesBuilderExtensionProvider the Xtext types builder, must not be {@code null}
   * @param qualifiedClassName the qualified name of the class to create, may not be {@code null} or empty
   * @param createBlock the code block with create statements to create a class for, must not be {@code null}
   */
  public CreateBlockClassGenerator(
      TypesBuilderExtensionProvider typesBuilderExtensionProvider,
      String qualifiedClassName,
      CreateBlock createBlock) {
    super(typesBuilderExtensionProvider);
    checkArgument(!nullOrEmpty(qualifiedClassName), "class name must not be null or empty");
    this.qualifiedClassName = qualifiedClassName;
    this.createBlock = checkNotNull(createBlock, "create block must not be null");
    this.createdElementsClass =
        generateNewlyAccessibleElementsContainerClass(getCreatedValuesClassQualifiedName());
  }

  private String getCreatedValuesClassQualifiedName() {
    return qualifiedClassName + "." + CREATED_ELEMENTS_SIMPLE_CLASS_NAME;
  }

  @Override
  public Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
    return createBlock.getCreateStatements().stream()
        .map(createStatement -> new AccessibleElement(
            createStatement.getName() == null ? MISSING_NAME : createStatement.getName(),
            createStatement.getMetaclass() == null ? null : getJavaClassName(createStatement.getMetaclass())
        ))
        .toList();
  }

  @Override
  public JvmGenericType getNewlyAccessibleElementsContainerType() {
    return createdElementsClass;
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    generatedClass = _typesBuilder.toClass(createBlock, qualifiedClassName);
    generatedClass.setVisibility(JvmVisibility.PRIVATE);
    generatedClass.setStatic(true);

    return generatedClass;
  }

  @Override
  public JvmGenericType generateBody() {
    generatedClass.getSuperTypes().add(_typeReferenceBuilder.typeRef(AbstractRoutine.Create.class));
    generatedClass.getMembers().add(createdElementsClass);
    generatedClass.getMembers().add(generateConstructor());
    generatedClass.getMembers().add(generateMethodCreate());
    return generatedClass;
  }

  private JvmConstructor generateConstructor() {
    Procedures.Procedure1<JvmConstructor> initConstructor = (constructor) -> {
      AccessibleElement element = new AccessibleElement("reactionExecutionState", ReactionExecutionState.class);
      JvmFormalParameter reactionExecutionStateParameter = _parameterGenerator
          .generateParameter(constructor, element);
      constructor.getParameters().add(reactionExecutionStateParameter);
      _typesBuilder.setBody(constructor, new StringConcatenationClient() {
        @Override
        protected void appendTo(TargetStringConcatenation target) {
          target.append("super(");
          target.append(reactionExecutionStateParameter.getName());
          target.append(");");
        }
      });
    };
    return  _typesBuilder.toConstructor(createBlock, initConstructor);
  }

  private JvmOperation generateMethodCreate() {
    Procedures.Procedure1<JvmOperation> createMethod = (JvmOperation it) -> {
      StringConcatenationClient client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation builder) {
          {
            for(NamedMetaclassReference createStatement :
                CreateBlockClassGenerator.this.createBlock.getCreateStatements()) {
              StringConcatenationClient _elementCreationCode = CreateBlockClassGenerator.this.getElementCreationCode(createStatement);
              builder.append(_elementCreationCode);
              builder.newLineIfNotEmpty();
            }
          }
          builder.append("return new ");
          builder.append(CreateBlockClassGenerator.this.getNewlyAccessibleElementsContainerType());
          builder.append("(");
          {
            Iterable<AccessibleElement> _newlyAccessibleElementsAfterExecution
                = CreateBlockClassGenerator.this.getNewlyAccessibleElementsAfterExecution();
            boolean _hasElements = false;
            for(AccessibleElement createdElement : _newlyAccessibleElementsAfterExecution) {
              if (!_hasElements) {
                _hasElements = true;
              } else {
                builder.appendImmediate(", ", "");
              }
              String _name = createdElement.getName();
              builder.append(_name);
            }
          }
          builder.append(");");
          builder.newLineIfNotEmpty();
        }
      };
      _typesBuilder.setBody(it, client);
    };
    return _typesBuilder.toMethod(createBlock,
        CreateBlockClassGenerator.CREATE_ELEMENTS_METHOD_NAME,
        _typeReferenceBuilder.typeRef(getNewlyAccessibleElementsContainerType()), createMethod);
  }

  private StringConcatenationClient getElementCreationCode(NamedMetaclassReference elementCreate) {

    EClassifier affectedElementClass = elementCreate.getMetaclass();
    String createdClassFactoryTypeName;
    if (affectedElementClass != null
      && affectedElementClass.getEPackage() != null
      && affectedElementClass.getEPackage().getEFactoryInstance() != null
      && getRuntimeClassName(affectedElementClass.getEPackage().getEFactoryInstance()) != null) {
      createdClassFactoryTypeName =
          getRuntimeClassName(affectedElementClass.getEPackage().getEFactoryInstance());
    } else {
      createdClassFactoryTypeName = MISSING_TYPE;
    }

    return new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation builder) {
        builder.append(ReactionsLanguageHelper.getJavaClassName(affectedElementClass));
        builder.append(" ");

        String name = Objects.requireNonNullElse(elementCreate.getName(),
            CreateBlockClassGenerator.MISSING_NAME);
        builder.append(name);
        builder.append(" = ");
        builder.append(CreateBlockClassGenerator.PREDEFINED_CREATE_OBJECT_METHOD_NAME);
        builder.append("(() -> {");
        builder.newLineIfNotEmpty();
        builder.append("\t");
        builder.append("return ");

        builder.append(createdClassFactoryTypeName, "\t");
        builder.append(".eINSTANCE.create");
        String affectedElementClassName =
            (affectedElementClass != null && affectedElementClass.getName() != null)
            ? affectedElementClass.getName() : MISSING_TYPE;
        builder.append(affectedElementClassName, "\t");

        builder.append("();");
        builder.newLineIfNotEmpty();
        builder.append("});");
        builder.newLine();
      }
    };
  }

  @Override
  public StringConcatenationClient generateStepExecutionCode(
      StringConcatenationClient prefix,
      String executionStateAccessExpression,
      String routinesFacadeAccessExpression,
      Iterable<String> accessibleElementsAccessExpressions,
      StringConcatenationClient suffix
  ) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation builder) {
        builder.append(prefix);
        builder.append("new ");
        builder.append(CreateBlockClassGenerator.this.qualifiedClassName);
        builder.append("(");
        builder.append(executionStateAccessExpression);
        builder.append(").");
        builder.append(CreateBlockClassGenerator.CREATE_ELEMENTS_METHOD_NAME);
        builder.append("();");
        builder.newLineIfNotEmpty();
        builder.append(suffix);
        builder.newLineIfNotEmpty();
      }
    };
  }
}
