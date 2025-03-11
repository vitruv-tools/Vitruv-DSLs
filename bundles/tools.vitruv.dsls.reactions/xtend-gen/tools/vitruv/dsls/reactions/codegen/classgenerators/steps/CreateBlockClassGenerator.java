package tools.vitruv.dsls.reactions.codegen.classgenerators.steps;

import com.google.common.base.Preconditions;
import edu.kit.ipd.sdq.commons.util.java.lang.IterableUtil;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import tools.vitruv.dsls.reactions.codegen.helper.AccessibleElement;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;
import tools.vitruv.dsls.reactions.codegen.typesbuilder.TypesBuilderExtensionProvider;
import tools.vitruv.dsls.reactions.language.toplevelelements.CreateBlock;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Generates for a {@link CreateBlock} of a routine a class providing a creation method (with the name defined in {@link #CREATE_ELEMENTS_METHOD_NAME})
 * that returns the created elements.
 */
@SuppressWarnings("all")
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
  public CreateBlockClassGenerator(final TypesBuilderExtensionProvider typesBuilderExtensionProvider, final String qualifiedClassName, final CreateBlock createBlock) {
    super(typesBuilderExtensionProvider);
    boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(qualifiedClassName);
    boolean _not = (!_isNullOrEmpty);
    Preconditions.checkArgument(_not, "class name must not be null or empty");
    this.qualifiedClassName = qualifiedClassName;
    this.createBlock = Preconditions.<CreateBlock>checkNotNull(createBlock, "create block must not be null");
    this.createdElementsClass = this.generateNewlyAccessibleElementsContainerClass(this.getCreatedValuesClassQualifiedName());
  }

  private String getCreatedValuesClassQualifiedName() {
    return ((this.qualifiedClassName + ".") + CreateBlockClassGenerator.CREATED_ELEMENTS_SIMPLE_CLASS_NAME);
  }

  @Override
  public Iterable<AccessibleElement> getNewlyAccessibleElementsAfterExecution() {
    final Function1<NamedMetaclassReference, AccessibleElement> _function = (NamedMetaclassReference it) -> {
      String _elvis = null;
      String _name = it.getName();
      if (_name != null) {
        _elvis = _name;
      } else {
        _elvis = CreateBlockClassGenerator.MISSING_NAME;
      }
      EClassifier _metaclass = it.getMetaclass();
      String _javaClassName = null;
      if (_metaclass!=null) {
        _javaClassName=ReactionsLanguageHelper.getJavaClassName(_metaclass);
      }
      return new AccessibleElement(_elvis, _javaClassName);
    };
    return IterableUtil.<NamedMetaclassReference, AccessibleElement>mapFixed(this.createBlock.getCreateStatements(), _function);
  }

  @Override
  public JvmGenericType getNewlyAccessibleElementsContainerType() {
    return this.createdElementsClass;
  }

  @Override
  public JvmGenericType generateEmptyClass() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      it.setVisibility(JvmVisibility.PRIVATE);
      it.setStatic(true);
    };
    this.generatedClass = this._typesBuilder.toClass(this.createBlock, this.qualifiedClassName, _function);
    return this.generatedClass;
  }

  @Override
  public JvmGenericType generateBody() {
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      EList<JvmTypeReference> _superTypes = it.getSuperTypes();
      JvmTypeReference _typeRef = this._typeReferenceBuilder.typeRef(AbstractRoutine.Create.class);
      this._typesBuilder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
      EList<JvmMember> _members = it.getMembers();
      this._typesBuilder.<JvmGenericType>operator_add(_members, this.createdElementsClass);
      EList<JvmMember> _members_1 = it.getMembers();
      JvmConstructor _generateConstructor = this.generateConstructor();
      this._typesBuilder.<JvmConstructor>operator_add(_members_1, _generateConstructor);
      EList<JvmMember> _members_2 = it.getMembers();
      JvmOperation _generateMethodCreate = this.generateMethodCreate();
      this._typesBuilder.<JvmOperation>operator_add(_members_2, _generateMethodCreate);
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
    return this._typesBuilder.toConstructor(this.createBlock, _function);
  }

  private JvmOperation generateMethodCreate() {
    final Procedure1<JvmOperation> _function = (JvmOperation it) -> {
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          {
            EList<NamedMetaclassReference> _createStatements = CreateBlockClassGenerator.this.createBlock.getCreateStatements();
            for(final NamedMetaclassReference createStatement : _createStatements) {
              StringConcatenationClient _elementCreationCode = CreateBlockClassGenerator.this.getElementCreationCode(createStatement);
              _builder.append(_elementCreationCode);
              _builder.newLineIfNotEmpty();
            }
          }
          _builder.append("return new ");
          JvmGenericType _newlyAccessibleElementsContainerType = CreateBlockClassGenerator.this.getNewlyAccessibleElementsContainerType();
          _builder.append(_newlyAccessibleElementsContainerType);
          _builder.append("(");
          {
            Iterable<AccessibleElement> _newlyAccessibleElementsAfterExecution = CreateBlockClassGenerator.this.getNewlyAccessibleElementsAfterExecution();
            boolean _hasElements = false;
            for(final AccessibleElement createdElement : _newlyAccessibleElementsAfterExecution) {
              if (!_hasElements) {
                _hasElements = true;
              } else {
                _builder.appendImmediate(", ", "");
              }
              String _name = createdElement.getName();
              _builder.append(_name);
            }
          }
          _builder.append(");");
          _builder.newLineIfNotEmpty();
        }
      };
      this._typesBuilder.setBody(it, _client);
    };
    return this._typesBuilder.toMethod(this.createBlock, CreateBlockClassGenerator.CREATE_ELEMENTS_METHOD_NAME, this._typeReferenceBuilder.typeRef(this.getNewlyAccessibleElementsContainerType()), _function);
  }

  private StringConcatenationClient getElementCreationCode(final NamedMetaclassReference elementCreate) {
    final EClassifier affectedElementClass = elementCreate.getMetaclass();
    EPackage _ePackage = null;
    if (affectedElementClass!=null) {
      _ePackage=affectedElementClass.getEPackage();
    }
    EFactory _eFactoryInstance = null;
    if (_ePackage!=null) {
      _eFactoryInstance=_ePackage.getEFactoryInstance();
    }
    String _runtimeClassName = null;
    if (_eFactoryInstance!=null) {
      _runtimeClassName=ReactionsLanguageHelper.getRuntimeClassName(_eFactoryInstance);
    }
    final String createdClassFactory = _runtimeClassName;
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        String _javaClassName = ReactionsLanguageHelper.getJavaClassName(affectedElementClass);
        _builder.append(_javaClassName);
        _builder.append(" ");
        String _elvis = null;
        String _name = elementCreate.getName();
        if (_name != null) {
          _elvis = _name;
        } else {
          _elvis = CreateBlockClassGenerator.MISSING_NAME;
        }
        _builder.append(_elvis);
        _builder.append(" = ");
        _builder.append(CreateBlockClassGenerator.PREDEFINED_CREATE_OBJECT_METHOD_NAME);
        _builder.append("(() -> {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("return ");
        String _elvis_1 = null;
        if (createdClassFactory != null) {
          _elvis_1 = createdClassFactory;
        } else {
          _elvis_1 = CreateBlockClassGenerator.MISSING_TYPE;
        }
        _builder.append(_elvis_1, "\t");
        _builder.append(".eINSTANCE.create");
        String _elvis_2 = null;
        String _name_1 = null;
        if (affectedElementClass!=null) {
          _name_1=affectedElementClass.getName();
        }
        if (_name_1 != null) {
          _elvis_2 = _name_1;
        } else {
          _elvis_2 = CreateBlockClassGenerator.MISSING_TYPE;
        }
        _builder.append(_elvis_2, "\t");
        _builder.append("();");
        _builder.newLineIfNotEmpty();
        _builder.append("});");
        _builder.newLine();
      }
    };
    return _client;
  }

  @Override
  public StringConcatenationClient generateStepExecutionCode(final StringConcatenationClient prefix, final String executionStateAccessExpression, final String routinesFacadeAccessExpression, final Iterable<String> accessibleElementsAccessExpressions, final StringConcatenationClient suffix) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(prefix);
        _builder.append("new ");
        _builder.append(CreateBlockClassGenerator.this.qualifiedClassName);
        _builder.append("(");
        _builder.append(executionStateAccessExpression);
        _builder.append(").");
        _builder.append(CreateBlockClassGenerator.CREATE_ELEMENTS_METHOD_NAME);
        _builder.append("();");
        _builder.newLineIfNotEmpty();
        _builder.append(suffix);
        _builder.newLineIfNotEmpty();
      }
    };
    return _client;
  }
}
