package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
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
 * Generates for a {@link Matcher} block of a routine a class providing a method (with the name defined in
 * {@link #MATCH_METHOD_NAME}) that accepts the inputs values of the routine as parameters and performs the
 * defined match statements.
 */
@SuppressWarnings("all")
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
   * @param matcher the matcher to create a class for, must not be {@code null}
   * @param inputElements the elements to be passed to the generated {@code match} method, must not be {@code null}
   */
  public MatchBlockClassGenerator(final TypesBuilderExtensionProvider typesBuilderExtensionProvider, final String qualifiedClassName, final MatchBlock matchBlock, final Iterable<AccessibleElement> inputElements) {
    super(typesBuilderExtensionProvider);
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(qualifiedClassName);
    boolean _not = (!_isNullOrEmpty);
    Preconditions.checkArgument(_not, "class name must not be null or empty");
    this.qualifiedClassName = qualifiedClassName;
    this.matchBlock = Preconditions.<MatchBlock>checkNotNull(matchBlock, "match block must not be null");
    this.inputElements = Preconditions.<Iterable<AccessibleElement>>checkNotNull(inputElements, "input elements must not be null");
    this.retrievedElementsClass = this.generateNewlyAccessibleElementsContainerClass(this.getRetrievedValuesClassQualifiedName());
  }

  private String getRetrievedValuesClassQualifiedName() {
    return ((this.qualifiedClassName + ".") + MatchBlockClassGenerator.RETRIEVED_ELEMENTS_SIMPLE_CLASS_NAME);
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      it.setVisibility(JvmVisibility.PRIVATE);
      it.setStatic(true);
    };
    this.generatedClass = this._typesBuilder.toClass(this.matchBlock, this.qualifiedClassName, _function);
    return this.generatedClass;
  }

  @Override
  public JvmGenericType generateBody() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      EList<JvmTypeReference> _superTypes = it.getSuperTypes();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractRoutine.Match.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
      EList<JvmMember> _members = it.getMembers();
      this._typesBuilder.<JvmGenericType>operator_add(_members, this.retrievedElementsClass);
      EList<JvmMember> _members_1 = it.getMembers();
      JvmConstructor _generateConstructor = this.generateConstructor();
      this._typesBuilder.<JvmConstructor>operator_add(_members_1, _generateConstructor);
      EList<JvmMember> _members_2 = it.getMembers();
      JvmOperation _generateMatchMethod = this.generateMatchMethod();
      this._typesBuilder.<JvmOperation>operator_add(_members_2, _generateMatchMethod);
    };
    return ObjectExtensions.<JvmGenericType>operator_doubleArrow(
      this.generatedClass, _function);
  }

  private JvmConstructor generateConstructor() {
    final Procedure1<JvmConstructor> _function = (JvmConstructor it) -> {
      AccessibleElement _accessibleElement = new AccessibleElement("reactionExecutionState", ReactionExecutionState.class);
      final JvmFormalParameter executionStateParameter = this._parameterGenerator.generateParameter(it, _accessibleElement);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, executionStateParameter);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("super(");
          String _name = executionStateParameter.getName();
          _builder.append(_name);
          _builder.append(");");
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.toConstructor(this.matchBlock, _function);
  }

  private JvmOperation generateMatchMethod() {
    final List<AccessibleElement> currentlyAccessibleElements = CollectionLiterals.<AccessibleElement>newArrayList(((AccessibleElement[])Conversions.unwrapArray(this.inputElements, AccessibleElement.class)));
    final Function1<MatchStatement, StringConcatenationClient> _function = (MatchStatement it) -> {
      return this.createStatements(it, currentlyAccessibleElements);
    };
    final List<StringConcatenationClient> matcherStatements = IterableUtil.<MatchStatement, StringConcatenationClient>mapFixed(this.matchBlock.getMatchStatements(), _function);
    final Procedure1<JvmOperation> _function_1 = (JvmOperation it) -> {
      it.setVisibility(JvmVisibility.PUBLIC);
      EList<JvmTypeReference> _exceptions = it.getExceptions();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(IOException.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_exceptions, _typeRef);
      EList<JvmFormalParameter> _parameters = it.getParameters();
      Iterable<JvmFormalParameter> _generateAccessibleElementsParameters = this.generateAccessibleElementsParameters(it, this.inputElements);
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _generateAccessibleElementsParameters);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          {
            for(final StringConcatenationClient matcherStatement : matcherStatements) {
              _builder.append(matcherStatement);
              _builder.newLineIfNotEmpty();
            }
          }
          _builder.append("return new ");
          String _retrievedValuesClassQualifiedName = MatchBlockClassGenerator.this.getRetrievedValuesClassQualifiedName();
          _builder.append(_retrievedValuesClassQualifiedName);
          _builder.append("(");
          {
            Iterable<AccessibleElement> _newlyAccessibleElementsAfterExecution = MatchBlockClassGenerator.this.getNewlyAccessibleElementsAfterExecution();
            boolean _hasElements = false;
            for(final AccessibleElement retrievedElement : _newlyAccessibleElementsAfterExecution) {
              if (!_hasElements) {
                _hasElements = true;
              } else {
                _builder.appendImmediate(", ", "");
              }
              String _name = retrievedElement.getName();
              _builder.append(_name);
            }
          }
          _builder.append(");");
          _builder.newLineIfNotEmpty();
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.toMethod(this.matchBlock, MatchBlockClassGenerator.MATCH_METHOD_NAME, this._typeReferenceBuilder.typeRef(this.getNewlyAccessibleElementsContainerType()), _function_1);
  }

  private StringConcatenationClient _createStatements(final RequireAbscenceOfModelElement elementAbscence, final List<AccessibleElement> currentlyAccessibleElements) {
    final StringConcatenationClient retrieveStatementArguments = this.getGeneralGetCorrespondingElementStatementArguments(elementAbscence, null, currentlyAccessibleElements);
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("if (");
        _builder.append(MatchBlockClassGenerator.PREDEFINED_HAS_CORRESPONDING_ELEMENTS_METHOD_NAME);
        _builder.append("(");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(retrieveStatementArguments, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append(")) {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("return null;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    final StringConcatenationClient statements = _client;
    return statements;
  }

  private StringConcatenationClient _createStatements(final RetrieveModelElement retrieveElement, final List<AccessibleElement> currentlyAccessibleElements) {
    final StringConcatenationClient retrieveStatementArguments = this.getGeneralGetCorrespondingElementStatementArguments(retrieveElement, 
      retrieveElement.getName(), currentlyAccessibleElements);
    MetaclassReference _elementType = retrieveElement.getElementType();
    EClassifier _metaclass = null;
    if (_elementType!=null) {
      _metaclass=_elementType.getMetaclass();
    }
    final EClassifier affectedElementClass = _metaclass;
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

  private StringConcatenationClient _createStatements(final RetrieveManyModelElements retrieveElement, final String name, final String typeName, final StringConcatenationClient generalArguments) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(name);
          boolean _not = (!_isNullOrEmpty);
          if (_not) {
            _builder.append(Iterable.class);
            _builder.append("<");
            _builder.append(typeName);
            _builder.append("> ");
            _builder.append(name);
            _builder.append(" = ");
          }
        }
        _builder.append(MatchBlockClassGenerator.PREDEFINED_GET_CORRESPONDING_ELEMENTS_METHOD_NAME);
        _builder.append("(");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(generalArguments, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append(");");
        _builder.newLine();
      }
    };
    final StringConcatenationClient statement = _client;
    return statement;
  }

  private StringConcatenationClient _createStatements(final RetrieveOneModelElement retrieveElement, final String name, final String typeName, final StringConcatenationClient generalArguments) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(MatchBlockClassGenerator.PREDEFINED_GET_CORRESPONDING_ELEMENT_METHOD_NAME);
    _builder.append("(");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append(generalArguments, "\t");
    _builder.append(", ");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    boolean _isAsserted = retrieveElement.isAsserted();
    _builder.append(_isAsserted, "\t");
    _builder.append(" // asserted");
    _builder.newLineIfNotEmpty();
    _builder.append(")");
    final String retrieveStatement = _builder.toString();
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(name);
    if (_isNullOrEmpty) {
      boolean _isOptional = retrieveElement.isOptional();
      boolean _not = (!_isOptional);
      if (_not) {
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("if (");
            _builder.append(retrieveStatement);
            _builder.append(" == null) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t\t\t\t");
            _builder.append("return null;");
            _builder.newLine();
            _builder.append("\t\t\t\t");
            _builder.append("}");
          }
        };
        return _client;
      }
    } else {
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          {
            boolean _isOptional = retrieveElement.isOptional();
            if (_isOptional) {
              _builder.append(Optional.class);
              _builder.append("<");
              _builder.append(typeName);
              _builder.append("> ");
              _builder.append(name);
              _builder.append(" = ");
              _builder.append(Optional.class);
              _builder.append(".ofNullable(");
              _builder.append(retrieveStatement);
              _builder.newLineIfNotEmpty();
              _builder.append(");");
              _builder.newLine();
            } else {
              _builder.append(typeName);
              _builder.append(" ");
              _builder.append(name);
              _builder.append(" = ");
              _builder.append(retrieveStatement);
              _builder.append(";");
              _builder.newLineIfNotEmpty();
            }
          }
          {
            boolean _isOptional_1 = retrieveElement.isOptional();
            boolean _not = (!_isOptional_1);
            if (_not) {
              _builder.append("if (");
              _builder.append(name);
              _builder.append(" == null) {");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("return null;");
              _builder.newLine();
              _builder.append("}");
            }
          }
          _builder.newLineIfNotEmpty();
        }
      };
      return _client_1;
    }
    return null;
  }

  private StringConcatenationClient _createStatements(final MatchCheckStatement checkStatement, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    final JvmOperation checkMethod = this.generateMethodMatcherPrecondition(checkStatement, currentlyAccessibleElements);
    String _userExecutionMethodCallString = null;
    if (checkMethod!=null) {
      _userExecutionMethodCallString=this.getUserExecutionMethodCallString(checkMethod);
    }
    final String checkMethodCall = _userExecutionMethodCallString;
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("if (!");
        _builder.append(checkMethodCall);
        _builder.append(") {");
        _builder.newLineIfNotEmpty();
        {
          boolean _isAsserted = checkStatement.isAsserted();
          if (_isAsserted) {
            _builder.append("\t");
            _builder.append("throw new ");
            _builder.append(IllegalStateException.class, "\t");
            _builder.append("();");
            _builder.newLineIfNotEmpty();
          } else {
            _builder.append("\t");
            _builder.append("return null;");
            _builder.newLine();
          }
        }
        _builder.append("}");
      }
    };
    return _client;
  }

  private StringConcatenationClient getTagString(final RetrieveOrRequireAbscenceOfModelElement retrieveElement, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    XExpression _tag = retrieveElement.getTag();
    boolean _tripleNotEquals = (_tag != null);
    if (_tripleNotEquals) {
      final JvmOperation tagMethod = this.generateMethodGetRetrieveTag(retrieveElement, currentlyAccessibleElements);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          String _userExecutionMethodCallString = MatchBlockClassGenerator.this.getUserExecutionMethodCallString(tagMethod);
          _builder.append(_userExecutionMethodCallString);
        }
      };
      return _client;
    } else {
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("null");
        }
      };
      return _client_1;
    }
  }

  private StringConcatenationClient getPreconditionChecker(final RetrieveOrRequireAbscenceOfModelElement retrieveElement, final String name, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    MetaclassReference _elementType = retrieveElement.getElementType();
    String _javaClassName = null;
    if (_elementType!=null) {
      _javaClassName=ReactionsLanguageHelper.getJavaClassName(_elementType);
    }
    final String affectedElementClass = _javaClassName;
    XExpression _precondition = retrieveElement.getPrecondition();
    boolean _tripleEquals = (_precondition == null);
    if (_tripleEquals) {
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("null");
        }
      };
      return _client;
    }
    final JvmOperation preconditionMethod = this.generateMethodCorrespondencePrecondition(retrieveElement, name, currentlyAccessibleElements);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("(");
        _builder.append(affectedElementClass);
        _builder.append(" _element) -> ");
        String _simpleName = preconditionMethod.getSimpleName();
        _builder.append(_simpleName);
        _builder.append("(");
        {
          Iterable<JvmFormalParameter> _filterNull = IterableExtensions.<JvmFormalParameter>filterNull(preconditionMethod.getParameters());
          boolean _hasElements = false;
          for(final JvmFormalParameter parameter : _filterNull) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              _builder.appendImmediate(", ", "");
            }
            String _xifexpression = null;
            String _name = parameter.getName();
            boolean _equals = Objects.equal(_name, "it");
            if (_equals) {
              _xifexpression = "_element";
            } else {
              _xifexpression = parameter.getName();
            }
            _builder.append(_xifexpression);
          }
        }
        _builder.append(")");
      }
    };
    return _client_1;
  }

  private StringConcatenationClient getGeneralGetCorrespondingElementStatementArguments(final RetrieveOrRequireAbscenceOfModelElement retrieveElement, final String name, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    MetaclassReference _elementType = retrieveElement.getElementType();
    String _javaClassName = null;
    if (_elementType!=null) {
      _javaClassName=ReactionsLanguageHelper.getJavaClassName(_elementType);
    }
    final String affectedElementClass = _javaClassName;
    final StringConcatenationClient correspondingElementPreconditionChecker = this.getPreconditionChecker(retrieveElement, name, currentlyAccessibleElements);
    final JvmOperation correspondenceSourceMethod = this.generateMethodGetCorrespondenceSource(retrieveElement, currentlyAccessibleElements);
    String _userExecutionMethodCallString = null;
    if (correspondenceSourceMethod!=null) {
      _userExecutionMethodCallString=this.getUserExecutionMethodCallString(correspondenceSourceMethod);
    }
    final String correspondenceSourceMethodCall = _userExecutionMethodCallString;
    final StringConcatenationClient tagString = this.getTagString(retrieveElement, currentlyAccessibleElements);
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(correspondenceSourceMethodCall);
        _builder.append(", // correspondence source supplier");
        _builder.newLineIfNotEmpty();
        String _elvis = null;
        if (affectedElementClass != null) {
          _elvis = affectedElementClass;
        } else {
          _elvis = MatchBlockClassGenerator.MISSING_TYPE;
        }
        _builder.append(_elvis);
        _builder.append(".class,");
        _builder.newLineIfNotEmpty();
        _builder.append(correspondingElementPreconditionChecker);
        _builder.append(", // correspondence precondition checker");
        _builder.newLineIfNotEmpty();
        _builder.append(tagString);
      }
    };
    return _client;
  }

  private JvmOperation generateMethodGetRetrieveTag(final RetrieveOrRequireAbscenceOfModelElement elementRetrieve, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    int _plusPlus = this.counterGetRetrieveTagMethods++;
    final String methodName = ("getRetrieveTag" + Integer.valueOf(_plusPlus));
    final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      EList<JvmFormalParameter> _parameters = it.getParameters();
      Iterable<JvmFormalParameter> _generateAccessibleElementsParameters = this.generateAccessibleElementsParameters(it, currentlyAccessibleElements);
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _generateAccessibleElementsParameters);
      this._typesBuilder.setBody(it, elementRetrieve.getTag());
    };
    return this.generateAndAddMethod(elementRetrieve, methodName, this._typeReferenceBuilder.typeRef(String.class), _function);
  }

  private JvmOperation generateMethodCorrespondencePrecondition(final RetrieveOrRequireAbscenceOfModelElement elementRetrieve, final String name, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    Object _elvis = null;
    String _retrieveOrRequireAbscenceMethodSuffix = this.getRetrieveOrRequireAbscenceMethodSuffix(elementRetrieve);
    if (_retrieveOrRequireAbscenceMethodSuffix != null) {
      _elvis = _retrieveOrRequireAbscenceMethodSuffix;
    } else {
      int _plusPlus = this.counterGetCorrespondenceSource++;
      _elvis = Integer.valueOf(_plusPlus);
    }
    final String methodName = ("getCorrespondingModelElementsPrecondition" + _elvis);
    XExpression _precondition = elementRetrieve.getPrecondition();
    JvmOperation _generateAndAddMethod = null;
    if (_precondition!=null) {
      final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
        String _javaClassName = ReactionsLanguageHelper.getJavaClassName(elementRetrieve.getElementType());
        final AccessibleElement element = new AccessibleElement("it", _javaClassName);
        EList<JvmFormalParameter> _parameters = it.getParameters();
        Iterable<JvmFormalParameter> _generateParameters = this._parameterGenerator.generateParameters(it, currentlyAccessibleElements);
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _generateParameters);
        EList<JvmFormalParameter> _parameters_1 = it.getParameters();
        JvmFormalParameter _generateParameter = this._parameterGenerator.generateParameter(it, element);
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters_1, _generateParameter);
        this._typesBuilder.setBody(it, elementRetrieve.getPrecondition());
      };
      _generateAndAddMethod=this.generateAndAddMethod(_precondition, methodName, this._typeReferenceBuilder.typeRef(Boolean.TYPE), _function);
    }
    return _generateAndAddMethod;
  }

  private JvmOperation generateMethodGetCorrespondenceSource(final RetrieveOrRequireAbscenceOfModelElement elementRetrieve, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    Object _elvis = null;
    String _retrieveOrRequireAbscenceMethodSuffix = this.getRetrieveOrRequireAbscenceMethodSuffix(elementRetrieve);
    if (_retrieveOrRequireAbscenceMethodSuffix != null) {
      _elvis = _retrieveOrRequireAbscenceMethodSuffix;
    } else {
      int _plusPlus = this.counterGetCorrespondenceSource++;
      _elvis = Integer.valueOf(_plusPlus);
    }
    final String methodName = ("getCorrepondenceSource" + _elvis);
    JvmOperation _generateAndAddMethod = null;
    if (elementRetrieve!=null) {
      final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
        EList<JvmFormalParameter> _parameters = it.getParameters();
        Iterable<JvmFormalParameter> _generateAccessibleElementsParameters = this.generateAccessibleElementsParameters(it, currentlyAccessibleElements);
        this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _generateAccessibleElementsParameters);
        this._typesBuilder.setBody(it, elementRetrieve.getCorrespondenceSource());
      };
      _generateAndAddMethod=this.generateAndAddMethod(elementRetrieve, methodName, this._typeReferenceBuilder.typeRef(EObject.class), _function);
    }
    return _generateAndAddMethod;
  }

  private String getUserExecutionMethodCallString(final JvmOperation method) {
    StringConcatenation _builder = new StringConcatenation();
    String _simpleName = method.getSimpleName();
    _builder.append(_simpleName);
    _builder.append("(");
    {
      Iterable<JvmFormalParameter> _filterNull = IterableExtensions.<JvmFormalParameter>filterNull(method.getParameters());
      boolean _hasElements = false;
      for(final JvmFormalParameter parameter : _filterNull) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate(", ", "");
        }
        String _name = parameter.getName();
        _builder.append(_name);
      }
    }
    _builder.append(")");
    return _builder.toString();
  }

  private JvmOperation generateMethodMatcherPrecondition(final MatchCheckStatement checkStatement, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    int _plusPlus = this.counterCheckMatcherPreconditionMethods++;
    final String methodName = ("checkMatcherPrecondition" + Integer.valueOf(_plusPlus));
    final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      EList<JvmFormalParameter> _parameters = it.getParameters();
      Iterable<JvmFormalParameter> _generateAccessibleElementsParameters = this.generateAccessibleElementsParameters(it, currentlyAccessibleElements);
      this._typesBuilder.<JvmFormalParameter>operator_add(_parameters, _generateAccessibleElementsParameters);
      this._typesBuilder.setBody(it, checkStatement.getCondition());
    };
    return this.generateAndAddMethod(checkStatement, methodName, this._typeReferenceBuilder.typeRef(Boolean.TYPE), _function);
  }

  private String _getRetrieveOrRequireAbscenceMethodSuffix(final RetrieveOrRequireAbscenceOfModelElement statement) {
    return null;
  }

  private String _getRetrieveOrRequireAbscenceMethodSuffix(final RetrieveModelElement statement) {
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(statement.getName());
    if (_isNullOrEmpty) {
      return null;
    } else {
      return StringExtensions.toFirstUpper(statement.getName());
    }
  }

  @Override
  public StringConcatenationClient generateStepExecutionCode(final StringConcatenationClient prefix, final String executionStateAccessExpression, final String routinesFacadeAccessExpression, final Iterable<String> accessibleElementsAccessExpressions, final StringConcatenationClient suffix) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(prefix);
        _builder.append("new ");
        _builder.append(MatchBlockClassGenerator.this.qualifiedClassName);
        _builder.append("(");
        _builder.append(executionStateAccessExpression);
        _builder.append(").");
        _builder.append(MatchBlockClassGenerator.MATCH_METHOD_NAME);
        _builder.append("(");
        {
          boolean _hasElements = false;
          for(final String accessibleElement : accessibleElementsAccessExpressions) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              _builder.appendImmediate(", ", "");
            }
            _builder.append(accessibleElement);
          }
        }
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
    final Function1<RetrieveModelElement, Boolean> _function = (RetrieveModelElement it) -> {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(it.getName());
      return Boolean.valueOf((!_isNullOrEmpty));
    };
    final Function1<RetrieveModelElement, AccessibleElement> _function_1 = (RetrieveModelElement it) -> {
      return this.getAccessibleElement(it);
    };
    return IterableUtil.<RetrieveModelElement, AccessibleElement>mapFixed(IterableExtensions.<RetrieveModelElement>filter(Iterables.<RetrieveModelElement>filter(this.matchBlock.getMatchStatements(), RetrieveModelElement.class), _function), _function_1);
  }

  private AccessibleElement getAccessibleElement(final RetrieveModelElement retrieveElement) {
    final RetrieveModelElementType retrievalType = retrieveElement.getRetrievalType();
    MetaclassReference _elementType = retrieveElement.getElementType();
    EClassifier _metaclass = null;
    if (_elementType!=null) {
      _metaclass=_elementType.getMetaclass();
    }
    String _javaClassName = null;
    if (_metaclass!=null) {
      _javaClassName=ReactionsLanguageHelper.getJavaClassName(_metaclass);
    }
    final String retrieveElementType = _javaClassName;
    boolean _matched = false;
    if (retrievalType instanceof RetrieveOneModelElement) {
      _matched=true;
      boolean _isOptional = ((RetrieveOneModelElement)retrievalType).isOptional();
      if (_isOptional) {
        String _name = retrieveElement.getName();
        String _name_1 = Optional.class.getName();
        return new AccessibleElement(_name, _name_1, retrieveElementType);
      } else {
        String _name_2 = retrieveElement.getName();
        return new AccessibleElement(_name_2, retrieveElementType);
      }
    }
    if (!_matched) {
      if (retrievalType instanceof RetrieveManyModelElements) {
        _matched=true;
        String _name = retrieveElement.getName();
        String _name_1 = Iterable.class.getName();
        return new AccessibleElement(_name, _name_1, retrieveElementType);
      }
    }
    return null;
  }

  @Override
  public JvmGenericType getNewlyAccessibleElementsContainerType() {
    return this.retrievedElementsClass;
  }

  public JvmOperation generateAndAddMethod(final EObject contextObject, final String methodName, final JvmTypeReference returnType, final Procedure1<? super JvmOperation> initializer) {
    final JvmOperation generatedMethod = this._typesBuilder.toMethod(contextObject, methodName, returnType, initializer);
    EList<JvmMember> _members = this.generatedClass.getMembers();
    this._typesBuilder.<JvmOperation>operator_add(_members, generatedMethod);
    return generatedMethod;
  }

  private StringConcatenationClient createStatements(final MatchStatement elementAbscence, final Iterable<AccessibleElement> currentlyAccessibleElements) {
    if (elementAbscence instanceof RequireAbscenceOfModelElement
         && currentlyAccessibleElements instanceof List) {
      return _createStatements((RequireAbscenceOfModelElement)elementAbscence, (List<AccessibleElement>)currentlyAccessibleElements);
    } else if (elementAbscence instanceof RetrieveModelElement
         && currentlyAccessibleElements instanceof List) {
      return _createStatements((RetrieveModelElement)elementAbscence, (List<AccessibleElement>)currentlyAccessibleElements);
    } else if (elementAbscence instanceof MatchCheckStatement
         && currentlyAccessibleElements != null) {
      return _createStatements((MatchCheckStatement)elementAbscence, currentlyAccessibleElements);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(elementAbscence, currentlyAccessibleElements).toString());
    }
  }

  private StringConcatenationClient createStatements(final RetrieveModelElementType retrieveElement, final String name, final String typeName, final StringConcatenationClient generalArguments) {
    if (retrieveElement instanceof RetrieveManyModelElements) {
      return _createStatements((RetrieveManyModelElements)retrieveElement, name, typeName, generalArguments);
    } else if (retrieveElement instanceof RetrieveOneModelElement) {
      return _createStatements((RetrieveOneModelElement)retrieveElement, name, typeName, generalArguments);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(retrieveElement, name, typeName, generalArguments).toString());
    }
  }

  private String getRetrieveOrRequireAbscenceMethodSuffix(final RetrieveOrRequireAbscenceOfModelElement statement) {
    if (statement instanceof RetrieveModelElement) {
      return _getRetrieveOrRequireAbscenceMethodSuffix((RetrieveModelElement)statement);
    } else if (statement != null) {
      return _getRetrieveOrRequireAbscenceMethodSuffix(statement);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(statement).toString());
    }
  }
}
