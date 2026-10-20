package tools.vitruv.dsls.reactions.builder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import tools.vitruv.dsls.reactions.language.MatchCheckStatement;
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveOneModelElement;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;
import tools.vitruv.dsls.reactions.language.RetrieveModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;

import static com.google.common.base.Preconditions.*;
import static tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants.*;
import tools.vitruv.dsls.reactions.language.LanguageFactory;
import tools.vitruv.dsls.common.elements.ElementsFactory;
import org.eclipse.xtext.xbase.XBlockExpression;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import org.eclipse.xtext.xbase.XFeatureCall;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;

public class FluentRoutineBuilder extends FluentReactionsSegmentChildBuilder {

  @Getter(AccessLevel.PACKAGE)
  protected Routine routine;
  @Getter(AccessLevel.PACKAGE)
  protected boolean requireOldValue = false;
  @Getter(AccessLevel.PACKAGE)
  protected boolean requireNewValue = false;
  @Getter(AccessLevel.PACKAGE)
  protected boolean requireAffectedEObject = false;
  @Getter(AccessLevel.PACKAGE)
  protected boolean requireAffectedValue = false;

  private EClassifier valueType;
  private EClass affectedObjectType;

  FluentRoutineBuilder(String routineName, FluentBuilderContext context) {
    super(context);
    this.routine = TopLevelElementsFactory.eINSTANCE.createRoutine();
    routine.setName(routineName);
    routine.setInput(TopLevelElementsFactory.eINSTANCE.createRoutineInput());
  }

  @Override
  protected void attachmentPreparation() {
    super.attachmentPreparation();
    checkState((!requireOldValue && !requireNewValue && !requireAffectedValue) || valueType != null,
        "Although required, there was no value type set on the "+this);
    checkState(!requireAffectedEObject || affectedObjectType != null,
        "Although required, there was no affected object type set on the "+this);
    if (requireAffectedEObject) {
      addInputElementIfNotExists(affectedObjectType, CHANGE_AFFECTED_ELEMENT_ATTRIBUTE);
    }
    if (requireOldValue) {
      addInputElementIfNotExists(valueType, CHANGE_OLD_VALUE_ATTRIBUTE);
    }
    if (requireNewValue) {
      addInputElementIfNotExists(valueType, CHANGE_NEW_VALUE_ATTRIBUTE);
    }
  }

  RoutineStartBuilder start() {
    return new RoutineStartBuilder(this);
  }

  private void addInputElementIfNotExists(EClassifier type, String parameterName) {
    if (routine.getInput().getModelInputElements()
        .stream()
        .anyMatch(inputElement -> inputElement.getName().equals(parameterName))) {
      return;
    }
    addInputElement(type, parameterName);
  }

  private void addInputElement(Object type, String parameterName) {
    if (type instanceof EClass eClass) {
      NamedMetaclassReference namedMetaclassReference = ElementsFactory.eINSTANCE.createNamedMetaclassReference();
      namedMetaclassReference.setName(parameterName);
      routine.getInput().getModelInputElements().add(reference(namedMetaclassReference, eClass));
    } else if (type instanceof EDataType dataType) {
      addInputElement(dataType.getInstanceClass(), parameterName);
    } else if (type instanceof Class<?> clazz) {
      NamedJavaElementReference namedJavaElementReference = TopLevelElementsFactory.eINSTANCE.createNamedJavaElementReference();
      namedJavaElementReference.setName(parameterName);
      routine.getInput().getJavaInputElements().add(reference(namedJavaElementReference, clazz));
    }
    throw new IllegalArgumentException("Unable to handle type "+type);
  }

  void setValueType(EClassifier type) {
    if (valueType == null) {
      valueType = type;
    }
    if (!isAssignableFrom(type, valueType)) {
      throw new IllegalStateException("The "+this+" already has the value type \""+valueType.getName()+"\""
					+" set, which is not a super type of \""+type.getName()+"\". The value type can thus not be set to" +
				"\""+type.getName()+"\"!");
    }
  }

  void setAffectedObjectType(EClass type) {
    if (affectedObjectType == null) {
      affectedObjectType = type;
    }
		if (!isAssignableFrom(type, valueType)) {
			throw new IllegalStateException("The "+this+" already has the affected object type \""+affectedObjectType.getName()+"\""
					+" set, which is not a super type of \""+type.getName()+"\". The value type can thus not be set to" +
					"\""+type.getName()+"\"!");
		}
  }

	public static boolean isAssignableFrom(EClassifier a, EClassifier b) {
		if (a instanceof EClass eClassA && b instanceof EClass eClassB) {
			return EcoreUtil2.isAssignableFrom(eClassA, eClassB);
		}
		if (a instanceof EDataType eDataTypeA && b instanceof EDataType eDataTypeB) {
			return isAssignableFrom(eDataTypeA.eClass(), eDataTypeB.eClass());
		}
		if ((a instanceof EDataType && b instanceof EClass)
				|| (a instanceof EClass && b instanceof EDataType)) {
			return false;
		}
		throw new IllegalArgumentException("Cannot invoke isAssignableFrom with "+a.getClass()+" and "+b.getClass());
	}

  public static class CreatorOrUpdateBuilder extends UpdateBuilder {

    private CreatorOrUpdateBuilder(FluentRoutineBuilder builder) {
      super(builder);
    }

    public UpdateBuilder create(Consumer<CreateStatementBuilder> creates) {
      builder.routine.setCreateBlock(TopLevelElementsFactory.eINSTANCE.createCreateBlock());
      CreateStatementBuilder statementBuilder = new CreateStatementBuilder(builder);
      creates.accept(statementBuilder);
      return new UpdateBuilder(builder);
    }
  }

  public static class MatchBlockOrCreatorOrUpdateBuilder extends CreatorOrUpdateBuilder {

    private MatchBlockOrCreatorOrUpdateBuilder(FluentRoutineBuilder builder) {
      super(builder);
    }

    public CreatorOrUpdateBuilder match(Consumer<UndecidedMatchStatementBuilder> matches) {
			Routine r1 = builder.routine;
      MatchBlock matchBlock = TopLevelElementsFactory.eINSTANCE.createMatchBlock();
      r1.setMatchBlock(matchBlock);
      UndecidedMatchStatementBuilder statementsBuilder = new UndecidedMatchStatementBuilder(builder);
      matches.accept(statementsBuilder);
      if (r1.getMatchBlock().getMatchStatements().isEmpty()) {
        // remove matchBlock again:
        r1.setMatchBlock(null);
      }
      return new CreatorOrUpdateBuilder(builder);
    }
  }


  public static class InputOrMatchBlockOrCreatorOrUpdateBuilder extends MatchBlockOrCreatorOrUpdateBuilder {

    private InputOrMatchBlockOrCreatorOrUpdateBuilder(FluentRoutineBuilder builder) {
      super(builder);
    }

    public MatchBlockOrCreatorOrUpdateBuilder input(Consumer<InputBuilder> inputs) {
      inputs.accept(new InputBuilder(builder));
      return new MatchBlockOrCreatorOrUpdateBuilder(builder);
    }

  }

  public static class RoutineStartBuilder extends InputOrMatchBlockOrCreatorOrUpdateBuilder {

    private RoutineStartBuilder(FluentRoutineBuilder builder) {
      super(builder);
    }

    public FluentRoutineBuilder retrieveRoutineBuilder() {
      return builder;
    }

    public RoutineStartBuilder alwaysRequireAffectedEObject() {
      builder.requireAffectedEObject = true;
      return this;
    }

		public RoutineStartBuilder alwaysRequireNewValue() {
      builder.requireNewValue = true;
      return this;
    }

    public InputOrMatchBlockOrCreatorOrUpdateBuilder
				overrideAlongImportPath(FluentReactionsSegmentBuilder... importPathSegmentBuilders) {
      if (importPathSegmentBuilders != null && importPathSegmentBuilders.length > 0) {
        RoutineOverrideImportPath currentImportPath = null;
        for (FluentReactionsSegmentBuilder pathSegmentBuilder : importPathSegmentBuilders) {
          RoutineOverrideImportPath nextPathSegment
							= TopLevelElementsFactory.eINSTANCE.createRoutineOverrideImportPath();
          nextPathSegment.setReactionsSegment(pathSegmentBuilder.getSegment());
          nextPathSegment.setParent(currentImportPath);
          currentImportPath = nextPathSegment;
        }
        builder.routine.setOverrideImportPath(currentImportPath);
      }
      return new InputOrMatchBlockOrCreatorOrUpdateBuilder(builder);
    }
  }

  public static class InputBuilder {
    private final FluentRoutineBuilder builder;

    private InputBuilder(FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public void model(EClass eClass, String parameterName) {
      detectWellKnownType(eClass, parameterName);
      builder.addInputElement(eClass, parameterName);
    }

    public void model(EClass eClass, WellKnownModelInput wellKnown) {
      wellKnown.apply(eClass);
    }

    private void detectWellKnownType(EClass eClass, String parameterName) {
      switch (parameterName) {
        case CHANGE_OLD_VALUE_ATTRIBUTE:
        case CHANGE_NEW_VALUE_ATTRIBUTE: builder.valueType = eClass;
        case CHANGE_AFFECTED_ELEMENT_ATTRIBUTE: builder.affectedObjectType = eClass;
      }
    }

    public WellKnownModelInput newValue() {
      builder.requireNewValue = true;
      return (EClass eClass) -> builder.valueType = eClass;
    }

    public WellKnownModelInput oldValue() {
      builder.requireOldValue = true;
      return (EClass eClass) -> builder.valueType = eClass;
    }

    public WellKnownModelInput affectedEObject() {
      builder.requireAffectedEObject = true;
      return (EClass eClass) -> builder.affectedObjectType = eClass;
    }

    public void plain(Class<?> javaClass, String parameterName) {
      builder.addInputElement(javaClass, parameterName);
    }
  }

  public interface WellKnownModelInput {
    void apply(EClass type);
  }

  public static class RetrieveModelElementMatchBlockStatementBuilder {
    private final FluentRoutineBuilder builder;
    private final RetrieveModelElement statement;

    private RetrieveModelElementMatchBlockStatementBuilder(FluentRoutineBuilder builder, RetrieveModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieve(EClass modelElement) {
      internalRetrieveOne(modelElement);
      return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieveOptional(EClass modelElement) {
      RetrieveOneModelElement retrieveOneStatement = internalRetrieveOne(modelElement);
      retrieveOneStatement.setOptional(true);
      return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieveAsserted(EClass modelElement) {
      RetrieveOneModelElement retrieveOneStatement = internalRetrieveOne(modelElement);
      retrieveOneStatement.setAsserted(true);
      return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieveMany(EClass modelElement) {
      reference(modelElement);
      statement.setRetrievalType(LanguageFactory.eINSTANCE.createRetrieveManyModelElements());
      return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement);
    }

    private RetrieveOneModelElement internalRetrieveOne(EClass modelElement) {
      reference(modelElement);
      RetrieveOneModelElement retrieveOneElement = LanguageFactory.eINSTANCE.createRetrieveOneModelElement();
      statement.setRetrievalType(retrieveOneElement);
      return retrieveOneElement;
    }

    private void reference(EClass modelElement) {
      statement.setElementType(
          builder.reference(ElementsFactory.eINSTANCE.createMetaclassReference(), modelElement));
    }
  }

  public static class RetrieveModelElementMatchBlockStatementCorrespondenceBuilder {
    private final FluentRoutineBuilder builder;
    private final RetrieveOrRequireAbscenceOfModelElement statement;

    private RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(
        FluentRoutineBuilder builder, RetrieveOrRequireAbscenceOfModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder correspondingTo() {
      return new RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementTagBuilder correspondingTo(String element) {
      statement.setCorrespondenceSource(builder.correspondingElement(element));
      return new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementTagBuilder correspondingTo(Function<TypeProvider, XExpression> expressionBuilder) {
      statement.setCorrespondenceSource(builder.correspondingElement(expressionBuilder));
      return new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement);
    }
  }

  public static class RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder {
    private final FluentRoutineBuilder builder;
    private final  RetrieveOrRequireAbscenceOfModelElement statement;

    private RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder
        (FluentRoutineBuilder builder, RetrieveOrRequireAbscenceOfModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public RetrieveModelElementMatchBlockStatementTagBuilder affectedEObject() {
      builder.requireAffectedEObject = true;
      statement.setCorrespondenceSource(builder.correspondingElement(CHANGE_AFFECTED_ELEMENT_ATTRIBUTE));
      return new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementTagBuilder newValue() {
      builder.requireNewValue = true;
      statement.setCorrespondenceSource(builder.correspondingElement(CHANGE_NEW_VALUE_ATTRIBUTE));
      return new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementTagBuilder oldValue() {
      builder.requireOldValue = true;
      statement.setCorrespondenceSource(builder.correspondingElement(CHANGE_OLD_VALUE_ATTRIBUTE));
      return new RetrieveModelElementMatchBlockStatementTagBuilder(builder, statement);
    }
  }

  public static class UndecidedMatchStatementBuilder {
    private final FluentRoutineBuilder builder;

    private UndecidedMatchStatementBuilder(FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public RetrieveModelElementMatchBlockStatementBuilder vall(String valName) {
      RetrieveModelElement statement = LanguageFactory.eINSTANCE.createRetrieveModelElement();
      statement.setName(valName);
      builder.routine.getMatchBlock().getMatchStatements().add(statement);
      return new RetrieveModelElementMatchBlockStatementBuilder(builder, statement);
    }

    public RetrieveModelElementMatchBlockStatementCorrespondenceBuilder requireAbsenceOf(EClass absentMetaclass) {
      RequireAbscenceOfModelElement statement = LanguageFactory.eINSTANCE.createRequireAbscenceOfModelElement();
      statement.setElementType(
          builder.reference(ElementsFactory.eINSTANCE.createMetaclassReference(), absentMetaclass)
      );
      builder.routine.getMatchBlock().getMatchStatements().add(statement);
      return new RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(builder, statement);
    }

    public MatchCheckStatement check(Function<TypeProvider, XExpression> expressionBuilder) {
      MatchCheckStatement statement = LanguageFactory.eINSTANCE.createMatchCheckStatement();
      statement.setCondition(
          builder.whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(),
              blockExpression -> {
                blockExpression.getExpressions().add(expressionBuilder.apply(builder.getTypeProvider(blockExpression)));
              })
      );
      builder.routine.getMatchBlock().getMatchStatements().add(statement);
      return statement;
    }

    public void checkAsserted(Function<TypeProvider, XExpression> expressionBuilder) {
      MatchCheckStatement statement = check(expressionBuilder);
      statement.setAsserted(true);
    }
  }

  public static class RetrieveModelElementMatchBlockStatementTagBuilder {
    private final FluentRoutineBuilder builder;
    private final  RetrieveOrRequireAbscenceOfModelElement statement;

    private RetrieveModelElementMatchBlockStatementTagBuilder(
        FluentRoutineBuilder builder, RetrieveOrRequireAbscenceOfModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public void taggedWithAnything() {
      statement.setTag(XbaseFactory.eINSTANCE.createXNullLiteral());
    }

    public void taggedWith(String tag) {
      XStringLiteral literal = XbaseFactory.eINSTANCE.createXStringLiteral();
      literal.setValue(tag);
      statement.setTag(literal);
    }

    public void taggedWith(Function<TypeProvider, XExpression> tagExpressionBuilder) {
      XBlockExpression blockTag = builder.whenJvmTypes(
          XbaseFactory.eINSTANCE.createXBlockExpression(),
          blockExpression -> {
            TypeProvider typeProvider = builder.getTypeProvider(blockExpression);
            blockExpression.getExpressions().addAll(extractExpressions(tagExpressionBuilder.apply(typeProvider)));
          }
      );
      statement.setTag(blockTag);
    }
  }

  public static class CreateStatementBuilder {
    private final FluentRoutineBuilder builder;

    private CreateStatementBuilder(FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public CreateStatementTypeBuilder vall(String vallName) {
			NamedMetaclassReference statement = ElementsFactory.eINSTANCE.createNamedMetaclassReference();
			statement.setName(vallName);
      builder.routine.getCreateBlock().getCreateStatements().add(statement);
      return new CreateStatementTypeBuilder(builder, statement);
    }
  }

  public static class CreateStatementTypeBuilder {
    private final FluentRoutineBuilder builder;
		private final NamedMetaclassReference statement;

    private CreateStatementTypeBuilder(FluentRoutineBuilder builder, NamedMetaclassReference statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public NamedMetaclassReference create(EClass element) {
      return builder.reference(statement, element);
    }
  }

  public static class UpdateBuilder {
    protected final FluentRoutineBuilder builder;

    private UpdateBuilder(FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public FluentRoutineBuilder withoutUpdate() {
      builder.readyToBeAttached = true;
      return builder;
    }

    public FluentRoutineBuilder update(Consumer<UpdateStatementBuilder> updates) {
      builder.routine.setUpdateBlock(TopLevelElementsFactory.eINSTANCE.createUpdateBlock());
      UpdateStatementBuilder statementBuilder = new UpdateStatementBuilder(builder);
      updates.accept(statementBuilder);
      builder.readyToBeAttached = true;
      return builder;
    }
  }

  public static class UpdateStatementBuilder {
    private final FluentRoutineBuilder builder;
		private final XBlockExpression expressionBlock;

    private UpdateStatementBuilder(FluentRoutineBuilder builder) {
      this.builder = builder;
      this.expressionBlock = XbaseFactory.eINSTANCE.createXBlockExpression();
      this.builder.routine.getUpdateBlock().setCode( expressionBlock);
    }

    public void delete(String existingElement) {
      XFeatureCall statement = XbaseFactory.eINSTANCE.createXFeatureCall();
			statement.setExplicitOperationCall(true);
			builder.whenJvmTypes(statement, stat -> {
				stat.setFeature(builder.getTypeProvider(stat).findMethod(AbstractRoutine.Update.class, "removeObject"));
				stat.getFeatureCallArguments().add(builder.existingElement(existingElement));
			});
      expressionBlock.getExpressions().add(statement);
    }

    public CorrespondenceElementBuilder<Consumer<XExpression>> addCorrespondenceBetween() {
      XFeatureCall statement = createCorrespondenceMethodCall();
      expressionBlock.getExpressions().add(statement);

      return new CorrespondenceElementBuilder(
          builder,
          new CorrespondenceTargetBuilder(builder, statement),
          (it) -> statement.getFeatureCallArguments().addFirst((XExpression) it)
          );
    }

    public CorrespondenceTargetBuilder addCorrespondenceBetween(String existingElement) {
      XFeatureCall statement = createCorrespondenceMethodCall();
      statement.getFeatureCallArguments().add(builder.existingElement(existingElement));
      return new CorrespondenceTargetBuilder(builder, statement);
    }

    public CorrespondenceTargetBuilder addCorrespondenceBetween(Function<TypeProvider, XExpression> expressionBuilder) {
      XFeatureCall statement = createCorrespondenceMethodCall();
      statement.getFeatureCallArguments().add(builder.existingElement(expressionBuilder));
      return new CorrespondenceTargetBuilder(builder, statement);
    }

    private XFeatureCall createCorrespondenceMethodCall() {
      return builder.whenJvmTypes(
          XbaseFactory.eINSTANCE.createXFeatureCall(),
          xFeatureCall ->
              builder.getTypeProvider(xFeatureCall).findMethod(
                  AbstractRoutine.Update.class,
                  "addCorrespondenceBetween")
      );
    }

    public CorrespondenceElementBuilder<CorrespondenceTargetBuilder> removeCorrespondenceBetween() {
      XFeatureCall statement = deleteCorrespondenceMethodCall();
      expressionBlock.getExpressions().add(statement);
      return new CorrespondenceElementBuilder<CorrespondenceTargetBuilder>(
          builder,
          new CorrespondenceTargetBuilder(builder, statement),
          (XExpression it) -> statement.getFeatureCallArguments().add(it)
      );
    }

    public CorrespondenceTargetBuilder removeCorrespondenceBetween(String existingElement) {
      XFeatureCall statement = deleteCorrespondenceMethodCall();
      statement.getFeatureCallArguments().add(builder.existingElement(existingElement));
      expressionBlock.getExpressions().add(statement);
      return new CorrespondenceTargetBuilder(builder, statement);
    }

    public CorrespondenceTargetBuilder removeCorrespondenceBetween(Function<TypeProvider, XExpression> expressionBuilder) {
      XFeatureCall statement = deleteCorrespondenceMethodCall();
      statement.getFeatureCallArguments().add(builder.existingElement(expressionBuilder));
      expressionBlock.getExpressions().add(statement);
      return new CorrespondenceTargetBuilder(builder, statement);
    }

    private XFeatureCall deleteCorrespondenceMethodCall() {
      return builder.whenJvmTypes(
          XbaseFactory.eINSTANCE.createXFeatureCall(),
          xFeatureCall -> {
            xFeatureCall.setExplicitOperationCall(true);
            TypeProvider typeProvider = builder.getTypeProvider(xFeatureCall);
            xFeatureCall.setFeature(
                typeProvider.findMethod(AbstractRoutine.Update.class, "removeCorrespondenceBetween")
            );
          }
      );
    }

    public XExpression execute(Function<TypeProvider, XExpression> expressionBuilder) {
      XBlockExpression placeholderExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
      expressionBlock.getExpressions().add(placeholderExpression);
      builder.whenJvmTypes(expressionBlock, xBlockExpression -> {
        EList<XExpression> expressions = xBlockExpression.getExpressions();
        TypeProvider typeProvider = builder.getTypeProvider(xBlockExpression);
        expressions.addAll(
            expressions.indexOf(placeholderExpression),
            extractExpressions(expressionBuilder.apply(typeProvider))
        );
        expressions.remove(placeholderExpression);
      });
      return expressionBlock;
    }

    public XExpression call(Function<TypeProvider, XExpression> expressionBuilder) {
      execute(expressionBuilder);
      return expressionBlock;
    }

    public void call(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
      checkNotNull(routineBuilder);
      checkState(routineBuilder.readyToBeAttached,
          "The "+routineBuilder+
              " is not sufficiently initialised to be set on the "+builder);
      checkState(!routineBuilder.requireNewValue,
          "The "+routineBuilder+" requires a new value," +
              " and can thus only be called from reactions, not routines!");
      checkState(!routineBuilder.requireOldValue || builder.valueType != null,
          "The "+routineBuilder+" requires an old value," +
              " and can thus only be called from reactions, not routines!");
      boolean hasFittingAffectedEObjectParameter = false;
      if (parameters.length > 0) {
        RoutineCallParameter param = parameters[0];
        if (param.isParameterArgumentType()) {
          // TODO: check if matching type
          hasFittingAffectedEObjectParameter = true;
        }
      }
      checkState(!routineBuilder.requireAffectedEObject || hasFittingAffectedEObjectParameter,
          "The "+routineBuilder+" requires an affectedEObject, and can thus only be called from reactions, not routines!");

      builder.transferReactionsSegmentTo(builder, routineBuilder);
      addRoutineCall(routineBuilder, parameters);
    }

    private void addRoutineCall(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
      expressionBlock.getExpressions().add(routineCall(routineBuilder, parameters));
    }

    private XFeatureCall routineCall(FluentRoutineBuilder routineBuilder, RoutineCallParameter... parameters) {
      XFeatureCall routineCall = XbaseFactory.eINSTANCE.createXFeatureCall();
      routineCall.setExplicitOperationCall(true);
      return builder.whenJvmTypes(routineCall, xFeatureCall -> {
        xFeatureCall.setFeature(routineBuilder.getJvmOperation());
        xFeatureCall.setImplicitReceiver(builder.getJvmOperationRoutineFacade(xFeatureCall));
        TypeProvider typeProvider = builder.getTypeProvider(xFeatureCall);
        List<XExpression> parameterExpression =
            Arrays.stream(parameters).map(param -> param.getExpression(typeProvider)).toList();
        xFeatureCall.getFeatureCallArguments().addAll(parameterExpression);
      });
    }
  }

  public static class RoutineCallParameter {
    private Object argument;

		public RoutineCallParameter(String parameter) {
      this.argument = parameter;
    }

    public RoutineCallParameter(XExpression expression) {
      this.argument = expression;
    }

    public RoutineCallParameter(Function<TypeProvider, XExpression> expressionBuilder) {
      this.argument = expressionBuilder;
    }

    public boolean isParameterArgumentType() {
      return argument instanceof String;
    }

    public XExpression getExpression(TypeProvider typeProvider) {
      if (isParameterArgumentType()) {
        return typeProvider.variable((String) argument);
      } else if (argument instanceof XExpression expression) {
        return expression;
      } else {
        Function<TypeProvider, XExpression> expressionBuilder = (Function<TypeProvider, XExpression>) argument;
        return expressionBuilder.apply(typeProvider);
      }
    }
  }

  protected static class CorrespondenceElementBuilder<NextType> {
    private final FluentRoutineBuilder builder;
    private final Consumer<XExpression> elementConsumer;
    private final NextType next;

    private CorrespondenceElementBuilder(FluentRoutineBuilder builder, NextType next, Consumer<XExpression> elementConsumer) {
      this.builder = builder;
      this.elementConsumer = elementConsumer;
      this.next = next;
    }

    public NextType oldValue() {
      builder.requireOldValue = true;
      elementConsumer.accept(builder.existingElement(CHANGE_OLD_VALUE_ATTRIBUTE));
      return next;
    }

    public NextType newValue() {
      builder.requireNewValue = true;
      elementConsumer.accept(builder.existingElement(CHANGE_NEW_VALUE_ATTRIBUTE));
      return next;
    }

    public NextType affectedEObject() {
      builder.requireAffectedEObject = true;
      elementConsumer.accept(builder.existingElement(CHANGE_AFFECTED_ELEMENT_ATTRIBUTE));
      return next;
    }
  }

  public static class CorrespondenceTargetBuilder {
    private final FluentRoutineBuilder builder;
    private final XFeatureCall statement;

    private CorrespondenceTargetBuilder(FluentRoutineBuilder builder, XFeatureCall statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public CorrespondenceElementBuilder<TagWithBuilder> and() {
      TagWithBuilder tagBuilder = new TagWithBuilder(builder, statement);
      return new CorrespondenceElementBuilder<TagWithBuilder>(builder, tagBuilder,
          (XExpression expr) -> setSecondElement(statement, expr)
      );
    }

    public TagWithBuilder and(String existingElement) {
      setSecondElement(statement, builder.existingElement(existingElement));
      return new TagWithBuilder(builder, statement);
    }

    public TagWithBuilder and(Function<TypeProvider, XExpression> expressionBuilder) {
      setSecondElement(statement, builder.existingElement(expressionBuilder));
      return new TagWithBuilder(builder, statement);
    }

    private void setSecondElement(XFeatureCall correspondenceStatement,
                                 XExpression existingElement) {
      correspondenceStatement.getFeatureCallArguments().add(existingElement);
    }
  }

  public static class TagWithBuilder {
    private final FluentRoutineBuilder builder;
    private final XFeatureCall correspondenceCreation;

    private TagWithBuilder(FluentRoutineBuilder builder, XFeatureCall correspondenceCreation) {
      this.builder = builder;
      this.correspondenceCreation = correspondenceCreation;
    }

    public void taggedWithAnything() {
      correspondenceCreation.getFeatureCallArguments().add(
          XbaseFactory.eINSTANCE.createXNullLiteral()
      );
    }

    public void taggedWith(String tag) {
      XStringLiteral literal = XbaseFactory.eINSTANCE.createXStringLiteral();
      literal.setValue(tag);
      correspondenceCreation.getFeatureCallArguments().add(literal);
    }

    public void taggedWith(Function<TypeProvider, XExpression> tagExpressionBuilder) {
      XBlockExpression blockExpression = builder.whenJvmTypes(
          XbaseFactory.eINSTANCE.createXBlockExpression(),
          block -> {
            TypeProvider typeProvider = builder.getTypeProvider(block);
            block.getExpressions().addAll(extractExpressions(tagExpressionBuilder.apply(typeProvider)));
          }
      );
      correspondenceCreation.getFeatureCallArguments().add(blockExpression);
    }
  }

  private XExpression existingElement(String name) {
		return whenJvmTypes(XbaseFactory.eINSTANCE.createXFeatureCall(), xFeatureCall -> {
			xFeatureCall.setFeature(correspondingMethodParameter(xFeatureCall, name));
		});
  }

  private XExpression existingElement(Function<TypeProvider, XExpression> expressionBuilder) {
		return whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), xBlock -> {
			xBlock.getExpressions().addAll(extractExpressions(expressionBuilder.apply(getTypeProvider(xBlock))));
		});
  }

  private XExpression correspondingElement(String name) {
    return whenJvmTypes(XbaseFactory.eINSTANCE.createXFeatureCall(), xFeatureCall -> {
			xFeatureCall.setFeature(correspondingMethodParameter(xFeatureCall, name));
		});
  }

  private XExpression correspondingElement(Function<TypeProvider, XExpression> expressionBuilder) {
		return whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), xBlock -> {
			xBlock.getExpressions()
					.addAll(extractExpressions(expressionBuilder.apply(getTypeProvider(xBlock))));
		});
  }

	@Override
  public String toString() {
    return "routine builder for "+routine.getName();
  }

  public JvmOperation getJvmOperation() {
    EObject jvmElement = context.getJvmModelAssociator().getPrimaryJvmElement(routine);
    if (jvmElement instanceof JvmOperation jvmMethod) {
      return jvmMethod;
    }
    throw new IllegalStateException("Could not find the routine facade method corresponding to the routine \""+routine.getName()+"\"");
  }

	@Override
	protected String getCreatedElementName() {
    return routine.getName();
  }

	@Override
	protected String getCreatedElementType() {
    return "routine";
  }
}