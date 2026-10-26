package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xbase.lib.XbaseGenerated;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.MatchCheckStatement;
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveManyModelElements;
import tools.vitruv.dsls.reactions.language.RetrieveModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveModelElementType;
import tools.vitruv.dsls.reactions.language.RetrieveOneModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchStatement;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Generates for a {@link MatchBlock} of a routine a class providing a method (with the name defined in
 * {@link #MATCH_METHOD_NAME}) that accepts the inputs values of the routine as parameters and performs the
 * defined match statements.
 */
public class MatchBlockClassGenerator extends StepExecutionClassGenerator {
  private static final String PREDEFINED_HAS_CORRESPONDING_ELEMENTS_METHOD_NAME = "hasCorrespondingElements";
  private static final String PREDEFINED_GET_CORRESPONDING_ELEMENT_METHOD_NAME = "getCorrespondingElement";
  private static final String PREDEFINED_GET_CORRESPONDING_ELEMENTS_METHOD_NAME = "getCorrespondingElements";
  private static final String MATCH_METHOD_NAME = "match";
  private static final String MISSING_TYPE = "/* Type missing */";
  private static final String RETRIEVED_ELEMENTS_SIMPLE_CLASS_NAME = "RetrievedValues";

  private final String qualifiedClassName;
  private final MatchBlock matchBlock;
  private final Iterable<AccessibleElement> inputElements;
  private final JvmGenericType retrievedElementsClass;
  private JvmGenericType generatedClass;

  private int counterGetRetrieveTagMethods = 1;
  private int counterCheckMatcherPreconditionMethods = 1;
  private int counterGetCorrespondenceSource = 1;

  /**
   * Creates a class generator for the given routine matcher. The generated class has
   * the given qualified name and accepts the given input elements as parameters in the
   * provided {@code match} method.
   * 
   * @param typesBuilderExtensionProvider the Xtext types builder, must not be {@code null}
   * @param qualifiedClassName the qualified name of the class to create, may not be {@code null} or empty
   * @param matchBlock the match block to create a class for, must not be {@code null}
   * @param inputElements the elements to be passed to the generated {@code match} method, must not be {@code null}
   */
  public MatchBlockClassGenerator(
      TypesBuilderExtensionProvider typesBuilderExtensionProvider,
      String qualifiedClassName,
      MatchBlock matchBlock,
      Iterable<AccessibleElement> inputElements) {
    super(typesBuilderExtensionProvider);
    Preconditions.checkArgument(qualifiedClassName != null && !qualifiedClassName.isEmpty(),
        "class name must not be null or empty");
    this.qualifiedClassName = qualifiedClassName;
    this.matchBlock = checkNotNull(matchBlock, "match block must not be null");
    this.inputElements = checkNotNull(inputElements, "input elements must not be null");
    this.retrievedElementsClass = generateNewlyAccessibleElementsContainerClass(getRetrievedValuesClassQualifiedName());
  }

  private String getRetrievedValuesClassQualifiedName() {
    return qualifiedClassName + "." + MatchBlockClassGenerator.RETRIEVED_ELEMENTS_SIMPLE_CLASS_NAME;
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    generatedClass = _typesBuilder.toClass(
        matchBlock, qualifiedClassName, clazz -> {
          clazz.setVisibility(JvmVisibility.PRIVATE);
          clazz.setStatic(true);
        });
    return generatedClass;
  }

  @Override
  public JvmGenericType generateBody() {
    final Procedure1<JvmGenericType> bodyForClassGenerator = (JvmGenericType emptyClazz) -> {
      _typesBuilder.operator_add(
          emptyClazz.getSuperTypes(),
          _typeReferenceBuilder.typeRef(AbstractRoutine.Match.class));
      EList<JvmMember> _members = emptyClazz.getMembers();
      _typesBuilder.operator_add(_members, this.retrievedElementsClass);
      _typesBuilder.operator_add(_members, this.generateConstructor());
      _typesBuilder.operator_add(_members, this.generateMatchMethod());
    };
    bodyForClassGenerator.apply(generatedClass);
    return generatedClass;
  }

  private JvmConstructor generateConstructor() {
    final Procedure1<JvmConstructor> _function = (JvmConstructor it) -> {
      AccessibleElement accessibleElement = new AccessibleElement("reactionExecutionState", ReactionExecutionState.class);
      JvmFormalParameter executionStateParameter =
          _parameterGenerator.generateParameter(it, accessibleElement);
      _typesBuilder.operator_add(it.getParameters(), executionStateParameter);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation builder) {
          builder.append("super(");
          builder.append(executionStateParameter.getName());
          builder.append(");");
        }
      };
      _typesBuilder.setBody(it, _client);
    };
    return _typesBuilder.toConstructor(matchBlock, _function);
  }

  private JvmOperation generateMatchMethod() {
    List<AccessibleElement> currentlyAccessibleElements = new ArrayList<>();
    inputElements.forEach(currentlyAccessibleElements::add);

    Function1<MatchStatement, StringConcatenationClient> statementCreator =
        (MatchStatement statement) -> createStatements(statement, currentlyAccessibleElements);
    List<StringConcatenationClient> matcherStatements =
        IterableUtil.mapFixed(matchBlock.getMatchStatements(), statementCreator);

    Procedure1<JvmOperation> matcherMethodGenerator = (JvmOperation matcherMethod) -> {
      matcherMethod.setVisibility(JvmVisibility.PUBLIC);
      this._typesBuilder.operator_add(matcherMethod.getExceptions(),
          this._typeReferenceBuilder.typeRef(IOException.class));
      this._typesBuilder.operator_add(matcherMethod.getParameters(),
          this.generateAccessibleElementsParameters(matcherMethod, this.inputElements));
      StringConcatenationClient matcherBodyGenerator = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation builder) {
          for(StringConcatenationClient matcherStatement : matcherStatements) {
            builder.append(matcherStatement);
            builder.newLineIfNotEmpty();
          }
          builder.append("return new ");
          builder.append(MatchBlockClassGenerator.this.getRetrievedValuesClassQualifiedName());
          builder.append("(");

          Iterable<AccessibleElement> _newlyAccessibleElementsAfterExecution = MatchBlockClassGenerator.this.getNewlyAccessibleElementsAfterExecution();
          boolean _hasElements = false;
          for(AccessibleElement retrievedElement : _newlyAccessibleElementsAfterExecution) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              builder.appendImmediate(", ", "");
            }
            builder.append(retrievedElement.getName());
          }

          builder.append(");");
          builder.newLineIfNotEmpty();
        }
      };
      _typesBuilder.setBody(matcherMethod, matcherBodyGenerator);
    };
    return _typesBuilder.toMethod(
        matchBlock,
        MatchBlockClassGenerator.MATCH_METHOD_NAME,
        _typeReferenceBuilder.typeRef(this.getNewlyAccessibleElementsContainerType()),
        matcherMethodGenerator);
  }

  private StringConcatenationClient _createStatements(
      RequireAbscenceOfModelElement elementAbscence,
      List<AccessibleElement> currentlyAccessibleElements) {
    StringConcatenationClient retrieveStatementArguments =
        getGeneralGetCorrespondingElementStatementArguments(elementAbscence, currentlyAccessibleElements);
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation builder) {
        builder.append("if (");
        builder.append(MatchBlockClassGenerator.PREDEFINED_HAS_CORRESPONDING_ELEMENTS_METHOD_NAME);
        builder.append("(");
        builder.newLineIfNotEmpty();
        builder.append("\t");
        builder.append(retrieveStatementArguments, "\t");
        builder.newLineIfNotEmpty();
        builder.append(")) {");
        builder.newLine();
        builder.append("\t");
        builder.append("return null;");
        builder.newLine();
        builder.append("}");
        builder.newLine();
      }
    };
  }

  private StringConcatenationClient createRetrievalStatements(final RetrieveModelElement retrieveElement, final List<AccessibleElement> currentlyAccessibleElements) {
    StringConcatenationClient retrieveStatementArguments = this.getGeneralGetCorrespondingElementStatementArguments(retrieveElement,
      currentlyAccessibleElements);
    MetaclassReference _elementType = retrieveElement.getElementType();
    EClassifier _metaclass = null;
    if (_elementType!=null) {
      _metaclass=_elementType.getMetaclass();
    }
    EClassifier affectedElementClass = _metaclass;
    RetrieveModelElementType _retrievalType = retrieveElement.getRetrievalType();
    String _name = retrieveElement.getName();
    String _javaClassName = null;
    if (affectedElementClass!=null) {
      _javaClassName=ReactionsLanguageHelper.getJavaClassName(affectedElementClass);
    }
    final StringConcatenationClient createdStatements = this.createStatements(_retrievalType, _name, _javaClassName, retrieveStatementArguments);
    AccessibleElement _accessibleElement = this.getAccessibleElement(retrieveElement);
    currentlyAccessibleElements.add(_accessibleElement);
    return createdStatements;
  }

  private StringConcatenationClient createRetrieveManyStatements(
      String name, String typeName, StringConcatenationClient generalArguments) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation builder) {
        if (name != null && !name.isEmpty()) {
          builder.append(Iterable.class);
          builder.append("<");
          builder.append(typeName);
          builder.append("> ");
          builder.append(name);
          builder.append(" = ");
        }
        builder.append(MatchBlockClassGenerator.PREDEFINED_GET_CORRESPONDING_ELEMENTS_METHOD_NAME);
        builder.append("(");
        builder.newLineIfNotEmpty();
        builder.append("\t");
        builder.append(generalArguments, "\t");
        builder.newLineIfNotEmpty();
        builder.append(");");
        builder.newLine();
      }
    };
  }

  private StringConcatenationClient createRetrieveOneStatements(
      RetrieveOneModelElement retrieveElement, String name, String typeName, StringConcatenationClient generalArguments) {
    StringConcatenation builder = new StringConcatenation();
    builder.append(MatchBlockClassGenerator.PREDEFINED_GET_CORRESPONDING_ELEMENT_METHOD_NAME);
    builder.append("(");
    builder.newLineIfNotEmpty();
    builder.append("\t");
    builder.append(generalArguments, "\t");
    builder.append(", ");
    builder.newLineIfNotEmpty();
    builder.append("\t");
    builder.append(retrieveElement.isAsserted(), "\t");
    builder.append(" // asserted");
    builder.newLineIfNotEmpty();
    builder.append(")");
    String retrieveStatement = builder.toString();
    if (name == null || name.isEmpty()) {
      if (!retrieveElement.isOptional()) {
        return new StringConcatenationClient() {
          @Override
          protected void appendTo(TargetStringConcatenation builder) {
            builder.append("if (");
            builder.append(retrieveStatement);
            builder.append(" == null) {");
            builder.newLineIfNotEmpty();
            builder.append("\t\t\t\t\t");
            builder.append("return null;");
            builder.newLine();
            builder.append("\t\t\t\t");
            builder.append("}");
          }
        };
      }
    } else {
      return new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation builder) {
          var isOptional = retrieveElement.isOptional();
          if (isOptional) {
            builder.append(Optional.class);
            builder.append("<");
            builder.append(typeName);
            builder.append("> ");
            builder.append(name);
            builder.append(" = ");
            builder.append(Optional.class);
            builder.append(".ofNullable(");
            builder.append(retrieveStatement);
            builder.newLineIfNotEmpty();
            builder.append(");");
            builder.newLine();
          } else {
            builder.append(typeName);
            builder.append(" ");
            builder.append(name);
            builder.append(" = ");
            builder.append(retrieveStatement);
            builder.append(";");
            builder.newLineIfNotEmpty();
          }
          if (!isOptional) {
            builder.append("if (");
            builder.append(name);
            builder.append(" == null) {");
            builder.newLineIfNotEmpty();
            builder.append("\t");
            builder.append("return null;");
            builder.newLine();
            builder.append("}");
          }
          builder.newLineIfNotEmpty();
        }
      };
    }
    return null;
  }

  private StringConcatenationClient createMatchCheckStatements(MatchCheckStatement checkStatement,
                                                               Iterable<AccessibleElement> currentlyAccessibleElements) {
    final JvmOperation checkMethod = generateMethodMatcherPrecondition(checkStatement, currentlyAccessibleElements);
    String _userExecutionMethodCallString = null;
    if (checkMethod!=null) {
      _userExecutionMethodCallString=getUserExecutionMethodCallString(checkMethod);
    }
    final String checkMethodCall = _userExecutionMethodCallString;
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation builder) {
        builder.append("if (!");
        builder.append(checkMethodCall);
        builder.append(") {");
        builder.newLineIfNotEmpty();

        if (checkStatement.isAsserted()) {
          builder.append("\t");
          builder.append("throw new ");
          builder.append(IllegalStateException.class, "\t");
          builder.append("();");
          builder.newLineIfNotEmpty();
        } else {
          builder.append("\t");
          builder.append("return null;");
          builder.newLine();
        }

        builder.append("}");
      }
    };
  }

  private StringConcatenationClient getTagString(
      RetrieveOrRequireAbscenceOfModelElement retrieveElement,
      Iterable<AccessibleElement> currentlyAccessibleElements) {
    if (retrieveElement.getTag() != null) {
      JvmOperation tagMethod =
          generateMethodGetRetrieveTag(retrieveElement, currentlyAccessibleElements);
      return new StringConcatenationClient() {
        @Override
        protected void appendTo(TargetStringConcatenation builder) {
          builder.append(MatchBlockClassGenerator.this.getUserExecutionMethodCallString(tagMethod));
        }
      };
    } else {
      return new StringConcatenationClient() {
        @Override
        protected void appendTo(TargetStringConcatenation builder) {
          builder.append("null");
        }
      };
    }
  }

  private StringConcatenationClient getPreconditionChecker(
      RetrieveOrRequireAbscenceOfModelElement retrieveElement,
      Iterable<AccessibleElement> currentlyAccessibleElements) {
    MetaclassReference elementType = retrieveElement.getElementType();
    String javaClassName = null;
    if (elementType != null) {
      javaClassName = ReactionsLanguageHelper.getJavaClassName(elementType);
    }
    String affectedElementClass = javaClassName;
    if ((retrieveElement.getPrecondition() == null)) {
      return new StringConcatenationClient() {
        @Override
        protected void appendTo(TargetStringConcatenation builder) {
          builder.append("null");
        }
      };
    }
    JvmOperation preconditionMethod = generateMethodCorrespondencePrecondition(retrieveElement, currentlyAccessibleElements);
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation builder) {
        builder.append("(");
        builder.append(affectedElementClass);
        builder.append(" _element) -> ");
        builder.append(preconditionMethod.getSimpleName());
        builder.append("(");

        Iterable<JvmFormalParameter> nonNullParameters = IterableExtensions.filterNull(preconditionMethod.getParameters());
        boolean _hasElements = false;
        for(JvmFormalParameter parameter : nonNullParameters) {
          if (!_hasElements) {
            _hasElements = true;
          } else {
            builder.appendImmediate(", ", "");
          }

          if (Objects.equals(parameter.getName(), "it")) {
            builder.append("_element");
          } else {
            builder.append( parameter.getName());
          }
        }
        builder.append(")");
      }
    };
  }

  private StringConcatenationClient getGeneralGetCorrespondingElementStatementArguments(
      RetrieveOrRequireAbscenceOfModelElement retrieveElement,
      Iterable<AccessibleElement> currentlyAccessibleElements) {
    MetaclassReference elementType = retrieveElement.getElementType();
    String javaClassName = null;
    if (elementType != null) {
      javaClassName = ReactionsLanguageHelper.getJavaClassName(elementType);
    }
    String affectedElementClass = javaClassName;
    StringConcatenationClient correspondingElementPreconditionChecker
        = getPreconditionChecker(retrieveElement, currentlyAccessibleElements);
    JvmOperation correspondenceSourceMethod =
        generateMethodGetCorrespondenceSource(retrieveElement, currentlyAccessibleElements);

    String userExecutionMethodCallString = null;
    if (correspondenceSourceMethod!=null) {
      userExecutionMethodCallString=this.getUserExecutionMethodCallString(correspondenceSourceMethod);
    }
    String correspondenceSourceMethodCall = userExecutionMethodCallString;
    StringConcatenationClient tagString = this.getTagString(retrieveElement, currentlyAccessibleElements);
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation builder) {
        builder.append(correspondenceSourceMethodCall);
        builder.append(", // correspondence source supplier");
        builder.newLineIfNotEmpty();
        builder.append(
            Objects.requireNonNullElse(affectedElementClass,
                MatchBlockClassGenerator.MISSING_TYPE)
        );
        builder.append(".class,");
        builder.newLineIfNotEmpty();
        builder.append(correspondingElementPreconditionChecker);
        builder.append(", // correspondence precondition checker");
        builder.newLineIfNotEmpty();
        builder.append(tagString);
      }
    };
  }

  private JvmOperation generateMethodGetRetrieveTag(final RetrieveOrRequireAbscenceOfModelElement elementRetrieve, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    int noOfTagMethods = this.counterGetRetrieveTagMethods++;
    String methodName = ("getRetrieveTag" + noOfTagMethods);

    Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      _typesBuilder.operator_add(
          it.getParameters(),
          generateAccessibleElementsParameters(it, currentlyAccessibleElements));
      _typesBuilder.setBody(it, elementRetrieve.getTag());
    };

    return generateAndAddMethod(
        elementRetrieve,
        methodName,
        _typeReferenceBuilder.typeRef(String.class),
        _function);
  }

  private JvmOperation generateMethodCorrespondencePrecondition(
      RetrieveOrRequireAbscenceOfModelElement elementRetrieve,
      Iterable<AccessibleElement> currentlyAccessibleElements) {
    String retrieveOrRequireAbscenceMethodSuffix = this.getRetrieveOrRequireAbscenceMethodSuffix(elementRetrieve);
    Object methodSuffix =
        Objects.requireNonNullElseGet(retrieveOrRequireAbscenceMethodSuffix,
            () -> this.counterGetCorrespondenceSource++);

    String methodName = ("getCorrespondingModelElementsPrecondition" + methodSuffix);
    XExpression precondition = elementRetrieve.getPrecondition();
    JvmOperation generateAndAddMethod = null;
    if (precondition != null) {
      final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
        String _javaClassName = ReactionsLanguageHelper.getJavaClassName(elementRetrieve.getElementType());
        AccessibleElement element = new AccessibleElement("it", _javaClassName);
        _typesBuilder.operator_add(it.getParameters(),
            this._parameterGenerator.generateParameters(it, currentlyAccessibleElements));
        _typesBuilder.operator_add(it.getParameters(),
            _parameterGenerator.generateParameter(it, element));
        _typesBuilder.setBody(it, elementRetrieve.getPrecondition());
      };
      generateAndAddMethod=generateAndAddMethod(precondition, methodName, _typeReferenceBuilder.typeRef(Boolean.TYPE), _function);
    }
    return generateAndAddMethod;
  }

  private JvmOperation generateMethodGetCorrespondenceSource(
      RetrieveOrRequireAbscenceOfModelElement elementRetrieve,
      Iterable<AccessibleElement> currentlyAccessibleElements) {

    String retrieveOrRequireAbscenceMethodSuffix = this.getRetrieveOrRequireAbscenceMethodSuffix(elementRetrieve);
    Object methodSuffix =
        Objects.requireNonNullElseGet(retrieveOrRequireAbscenceMethodSuffix,
            () -> this.counterGetCorrespondenceSource++);
    String methodName = ("getCorrepondenceSource" + methodSuffix);

    final Procedure1<JvmOperation> generateCorrespondenceMethodGenerator = (JvmOperation method) -> {
      _typesBuilder.operator_add(method.getParameters(),
          this.generateAccessibleElementsParameters(method, currentlyAccessibleElements));
      _typesBuilder.setBody(method,
          elementRetrieve.getCorrespondenceSource());
    };
    return generateAndAddMethod(
        elementRetrieve,
        methodName,
        _typeReferenceBuilder.typeRef(EObject.class),
        generateCorrespondenceMethodGenerator
    );
  }

  private String getUserExecutionMethodCallString(final JvmOperation method) {
    StringConcatenation builder = new StringConcatenation();
    builder.append(method.getSimpleName());
    builder.append("(");

    Iterable<JvmFormalParameter> _filterNull = IterableExtensions.filterNull(method.getParameters());
    boolean _hasElements = false;
    for(JvmFormalParameter parameter : _filterNull) {
      if (!_hasElements) {
        _hasElements = true;
      } else {
        builder.appendImmediate(", ", "");
      }
      builder.append(parameter.getName());
    }

    builder.append(")");
    return builder.toString();
  }

  private JvmOperation generateMethodMatcherPrecondition(
      MatchCheckStatement checkStatement,
      Iterable<AccessibleElement> currentlyAccessibleElements) {
    int counterPreconditionMethods = this.counterCheckMatcherPreconditionMethods++;
    String methodName = ("checkMatcherPrecondition" + counterPreconditionMethods);
    Procedure1<JvmOperation> configurePrecondition = (JvmOperation preconditionOperation) -> {
      _typesBuilder.operator_add(
          preconditionOperation.getParameters(),
          generateAccessibleElementsParameters(preconditionOperation, currentlyAccessibleElements));
      _typesBuilder.setBody(preconditionOperation, checkStatement.getCondition());
    };
    return generateAndAddMethod(checkStatement, methodName, _typeReferenceBuilder.typeRef(Boolean.TYPE), configurePrecondition);
  }

  @Override
  public StringConcatenationClient generateStepExecutionCode(StringConcatenationClient prefix, String executionStateAccessExpression, String routinesFacadeAccessExpression, Iterable<String> accessibleElementsAccessExpressions, final StringConcatenationClient suffix) {
    return new StringConcatenationClient() {
      @Override
      protected void appendTo(TargetStringConcatenation builder) {
        builder.append(prefix);
        builder.append("new ");
        builder.append(MatchBlockClassGenerator.this.qualifiedClassName);
        builder.append("(");
        builder.append(executionStateAccessExpression);
        builder.append(").");
        builder.append(MatchBlockClassGenerator.MATCH_METHOD_NAME);
        builder.append("(");

        boolean _hasElements = false;
        for (String accessibleElement : accessibleElementsAccessExpressions) {
          if (!_hasElements) {
            _hasElements = true;
          } else {
            builder.appendImmediate(", ", "");
          }
          builder.append(accessibleElement);
        }

        builder.append(");");
        builder.newLineIfNotEmpty();
        builder.append(suffix);
        builder.newLineIfNotEmpty();
      }
    };
  }

  @Override
  public Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
    return IterableUtil.mapFixed(
        IterableExtensions.filter(
          Iterables.filter(this.matchBlock.getMatchStatements(), RetrieveModelElement.class),
        statement -> statement.getName() != null && !statement.getName().isEmpty()),
      this::getAccessibleElement);
  }

  private AccessibleElement getAccessibleElement(final RetrieveModelElement retrieveElement) {
    RetrieveModelElementType retrievalType = retrieveElement.getRetrievalType();
    MetaclassReference elementType = retrieveElement.getElementType();
    EClassifier metaclass = null;
    if (elementType != null) {
      metaclass=elementType.getMetaclass();
    }
    String javaClassName = null;
    if (metaclass != null) {
      javaClassName = ReactionsLanguageHelper.getJavaClassName(metaclass);
    }
    String retrieveElementType = javaClassName;
    String retrieveElementName = retrieveElement.getName();
    if (retrievalType instanceof RetrieveOneModelElement retrieveOneModelElement) {
      if (retrieveOneModelElement.isOptional()) {
        return new AccessibleElement(retrieveElementName, Optional.class.getName(), retrieveElementType);
      } else {
        return new AccessibleElement(retrieveElementName, retrieveElementType);
      }
    }
    if (retrievalType instanceof RetrieveManyModelElements) {
      return new AccessibleElement(retrieveElementName, Iterable.class.getName(), retrieveElementType);
    }
    return null;
  }

  @Override
  public JvmGenericType getNewlyAccessibleElementsContainerType() {
    return this.retrievedElementsClass;
  }

  public JvmOperation generateAndAddMethod(
      EObject contextObject,
      String methodName,
      JvmTypeReference returnType,
      Procedure1<? super JvmOperation> initializer) {
    JvmOperation generatedMethod = _typesBuilder.toMethod(contextObject, methodName, returnType, initializer);
    _typesBuilder.operator_add(generatedClass.getMembers(), generatedMethod);
    return generatedMethod;
  }

  @XbaseGenerated
  private StringConcatenationClient createStatements(final MatchStatement elementAbscence, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    return switch (elementAbscence) {
      case
          RequireAbscenceOfModelElement requireAbscenceOfModelElement
          when currentlyAccessibleElements instanceof List ->
          _createStatements(requireAbscenceOfModelElement, (List<AccessibleElement>) currentlyAccessibleElements);
      case RetrieveModelElement retrieveModelElement
          when currentlyAccessibleElements instanceof List ->
          createRetrievalStatements(retrieveModelElement, (List<AccessibleElement>) currentlyAccessibleElements);
      case MatchCheckStatement matchCheckStatement
          when currentlyAccessibleElements != null ->
          createMatchCheckStatements(matchCheckStatement, currentlyAccessibleElements);
      case null, default -> throw new IllegalArgumentException("Unhandled parameter types: " +
          Arrays.asList(elementAbscence, currentlyAccessibleElements));
    };
  }

  private StringConcatenationClient createStatements(final RetrieveModelElementType retrieveElement, final String name, final String typeName, final StringConcatenationClient generalArguments) {
    return switch (retrieveElement) {
      case RetrieveManyModelElements retrieveManyModelElements ->
          createRetrieveManyStatements(name, typeName, generalArguments);
      case RetrieveOneModelElement retrieveOneModelElement ->
          createRetrieveOneStatements(retrieveOneModelElement, name, typeName, generalArguments);
      default -> throw new IllegalArgumentException("Unhandled parameter types: " +
          Arrays.asList(retrieveElement, name, typeName, generalArguments));
    };
  }


  private String getRetrieveOrRequireAbscenceMethodSuffix(final RetrieveOrRequireAbscenceOfModelElement statement) {
    if (statement instanceof RetrieveModelElement retrieveModelElement) {
      if (StringExtensions.isNullOrEmpty(retrieveModelElement.getName())) {
        return null;
      } else {
        return StringExtensions.toFirstUpper(retrieveModelElement.getName());
      }
    } else if (statement != null) {
      return null;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
          Collections.<Object>singletonList(statement));
    }
  }
}
