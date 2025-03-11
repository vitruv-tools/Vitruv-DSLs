package tools.vitruv.dsls.reactions.builder;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XNullLiteral;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.common.elements.ElementsFactory;
import tools.vitruv.dsls.common.elements.MetaclassReference;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import tools.vitruv.dsls.reactions.codegen.ReactionsLanguageConstants;
import tools.vitruv.dsls.reactions.language.LanguageFactory;
import tools.vitruv.dsls.reactions.language.MatchCheckStatement;
import tools.vitruv.dsls.reactions.language.RequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveOneModelElement;
import tools.vitruv.dsls.reactions.language.RetrieveOrRequireAbscenceOfModelElement;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchBlock;
import tools.vitruv.dsls.reactions.language.toplevelelements.MatchStatement;
import tools.vitruv.dsls.reactions.language.toplevelelements.NamedJavaElementReference;
import tools.vitruv.dsls.reactions.language.toplevelelements.Routine;
import tools.vitruv.dsls.reactions.language.toplevelelements.RoutineOverrideImportPath;
import tools.vitruv.dsls.reactions.language.toplevelelements.TopLevelElementsFactory;
import tools.vitruv.dsls.reactions.language.toplevelelements.UpdateBlock;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;

@SuppressWarnings("all")
public class FluentRoutineBuilder extends FluentReactionsSegmentChildBuilder {
  public static class CreatorOrUpdateBuilder extends FluentRoutineBuilder.UpdateBuilder {
    private CreatorOrUpdateBuilder(final FluentRoutineBuilder builder) {
      super(builder);
    }

    public FluentRoutineBuilder.UpdateBuilder create(final Consumer<FluentRoutineBuilder.CreateStatementBuilder> creates) {
      FluentRoutineBuilder.UpdateBuilder _xblockexpression = null;
      {
        this.builder.routine.setCreateBlock(TopLevelElementsFactory.eINSTANCE.createCreateBlock());
        final FluentRoutineBuilder.CreateStatementBuilder statementBuilder = new FluentRoutineBuilder.CreateStatementBuilder(this.builder);
        creates.accept(statementBuilder);
        _xblockexpression = new FluentRoutineBuilder.UpdateBuilder(this.builder);
      }
      return _xblockexpression;
    }
  }

  public static class MatchBlockOrCreatorOrUpdateBuilder extends FluentRoutineBuilder.CreatorOrUpdateBuilder {
    private MatchBlockOrCreatorOrUpdateBuilder(final FluentRoutineBuilder builder) {
      super(builder);
    }

    public FluentRoutineBuilder.CreatorOrUpdateBuilder match(final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> matches) {
      FluentRoutineBuilder.CreatorOrUpdateBuilder _xblockexpression = null;
      {
        final MatchBlock matchBlock = TopLevelElementsFactory.eINSTANCE.createMatchBlock();
        this.builder.routine.setMatchBlock(matchBlock);
        final FluentRoutineBuilder.UndecidedMatchStatementBuilder statementsBuilder = new FluentRoutineBuilder.UndecidedMatchStatementBuilder(this.builder);
        matches.accept(statementsBuilder);
        int _size = this.builder.routine.getMatchBlock().getMatchStatements().size();
        boolean _equals = (_size == 0);
        if (_equals) {
          this.builder.routine.setMatchBlock(null);
        }
        _xblockexpression = new FluentRoutineBuilder.CreatorOrUpdateBuilder(this.builder);
      }
      return _xblockexpression;
    }
  }

  public static class InputOrMatchBlockOrCreatorOrUpdateBuilder extends FluentRoutineBuilder.MatchBlockOrCreatorOrUpdateBuilder {
    private InputOrMatchBlockOrCreatorOrUpdateBuilder(final FluentRoutineBuilder builder) {
      super(builder);
    }

    public FluentRoutineBuilder.MatchBlockOrCreatorOrUpdateBuilder input(final Consumer<FluentRoutineBuilder.InputBuilder> inputs) {
      FluentRoutineBuilder.MatchBlockOrCreatorOrUpdateBuilder _xblockexpression = null;
      {
        FluentRoutineBuilder.InputBuilder _inputBuilder = new FluentRoutineBuilder.InputBuilder(this.builder);
        inputs.accept(_inputBuilder);
        _xblockexpression = new FluentRoutineBuilder.MatchBlockOrCreatorOrUpdateBuilder(this.builder);
      }
      return _xblockexpression;
    }
  }

  public static class RoutineStartBuilder extends FluentRoutineBuilder.InputOrMatchBlockOrCreatorOrUpdateBuilder {
    private RoutineStartBuilder(final FluentRoutineBuilder builder) {
      super(builder);
    }

    public FluentRoutineBuilder retrieveRoutineBuilder() {
      return this.builder;
    }

    public FluentRoutineBuilder.RoutineStartBuilder alwaysRequireAffectedEObject() {
      FluentRoutineBuilder.RoutineStartBuilder _xblockexpression = null;
      {
        this.builder.requireAffectedEObject = true;
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.RoutineStartBuilder alwaysRequireNewValue() {
      FluentRoutineBuilder.RoutineStartBuilder _xblockexpression = null;
      {
        this.builder.requireNewValue = true;
        _xblockexpression = this;
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.InputOrMatchBlockOrCreatorOrUpdateBuilder overrideAlongImportPath(final FluentReactionsSegmentBuilder... importPathSegmentBuilders) {
      FluentRoutineBuilder.InputOrMatchBlockOrCreatorOrUpdateBuilder _xblockexpression = null;
      {
        boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(((Iterable<?>)Conversions.doWrapArray(importPathSegmentBuilders)));
        boolean _not = (!_isNullOrEmpty);
        if (_not) {
          RoutineOverrideImportPath currentImportPath = null;
          for (final FluentReactionsSegmentBuilder pathSegmentBuilder : importPathSegmentBuilders) {
            {
              final RoutineOverrideImportPath nextPathSegment = TopLevelElementsFactory.eINSTANCE.createRoutineOverrideImportPath();
              nextPathSegment.setReactionsSegment(pathSegmentBuilder.getSegment());
              nextPathSegment.setParent(currentImportPath);
              currentImportPath = nextPathSegment;
            }
          }
          this.builder.routine.setOverrideImportPath(currentImportPath);
        }
        _xblockexpression = new FluentRoutineBuilder.InputOrMatchBlockOrCreatorOrUpdateBuilder(this.builder);
      }
      return _xblockexpression;
    }
  }

  public static class InputBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private InputBuilder(final FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public void model(final EClass eClass, final String parameterName) {
      this.detectWellKnownType(eClass, parameterName);
      this.builder.addInputElement(eClass, parameterName);
    }

    public void model(final EClass eClass, final FluentRoutineBuilder.WellKnownModelInput wellKnown) {
      wellKnown.apply(eClass);
    }

    private EClassifier detectWellKnownType(final EClass eClass, final String parameterName) {
      EClassifier _switchResult = null;
      if (parameterName != null) {
        switch (parameterName) {
          case ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE:
          case ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE:
            _switchResult = this.builder.valueType = eClass;
            break;
          case ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE:
            _switchResult = this.builder.affectedObjectType = eClass;
            break;
        }
      }
      return _switchResult;
    }

    public FluentRoutineBuilder.WellKnownModelInput newValue() {
      this.builder.requireNewValue = true;
      final FluentRoutineBuilder.WellKnownModelInput _function = (EClass it) -> {
        this.builder.valueType = it;
      };
      return _function;
    }

    public FluentRoutineBuilder.WellKnownModelInput oldValue() {
      this.builder.requireOldValue = true;
      final FluentRoutineBuilder.WellKnownModelInput _function = (EClass it) -> {
        this.builder.valueType = it;
      };
      return _function;
    }

    public FluentRoutineBuilder.WellKnownModelInput affectedEObject() {
      this.builder.requireAffectedEObject = true;
      final FluentRoutineBuilder.WellKnownModelInput _function = (EClass it) -> {
        this.builder.affectedObjectType = it;
      };
      return _function;
    }

    public void plain(final Class<?> javaClass, final String parameterName) {
      this.builder.addInputElement(javaClass, parameterName);
    }
  }

  public interface WellKnownModelInput {
    void apply(final EClass type);
  }

  public static class RetrieveModelElementMatchBlockStatementBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final RetrieveModelElement statement;

    private RetrieveModelElementMatchBlockStatementBuilder(final FluentRoutineBuilder builder, final RetrieveModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieve(final EClass modelElement) {
      this.internalRetrieveOne(modelElement);
      return new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(this.builder, this.statement);
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieveOptional(final EClass modelElement) {
      final RetrieveOneModelElement retrieveOneStatement = this.internalRetrieveOne(modelElement);
      retrieveOneStatement.setOptional(true);
      return new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(this.builder, this.statement);
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieveAsserted(final EClass modelElement) {
      final RetrieveOneModelElement retrieveOneStatement = this.internalRetrieveOne(modelElement);
      retrieveOneStatement.setAsserted(true);
      return new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(this.builder, this.statement);
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder retrieveMany(final EClass modelElement) {
      this.reference(modelElement);
      this.statement.setRetrievalType(LanguageFactory.eINSTANCE.createRetrieveManyModelElements());
      return new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(this.builder, this.statement);
    }

    private RetrieveOneModelElement internalRetrieveOne(final EClass modelElement) {
      this.reference(modelElement);
      final RetrieveOneModelElement retrieveOneElement = LanguageFactory.eINSTANCE.createRetrieveOneModelElement();
      this.statement.setRetrievalType(retrieveOneElement);
      return retrieveOneElement;
    }

    private void reference(final EClass modelElement) {
      this.statement.setElementType(this.builder.<MetaclassReference>reference(ElementsFactory.eINSTANCE.createMetaclassReference(), modelElement));
    }
  }

  public static class RetrieveModelElementMatchBlockStatementCorrespondenceBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final RetrieveOrRequireAbscenceOfModelElement statement;

    private RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(final FluentRoutineBuilder builder, final RetrieveOrRequireAbscenceOfModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder correspondingTo() {
      return new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder(this.builder, this.statement);
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder correspondingTo(final String element) {
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder _xblockexpression = null;
      {
        this.statement.setCorrespondenceSource(this.builder.correspondingElement(element));
        _xblockexpression = new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder(this.builder, this.statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder correspondingTo(final Function<TypeProvider, XExpression> expressionBuilder) {
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder _xblockexpression = null;
      {
        this.statement.setCorrespondenceSource(this.builder.correspondingElement(expressionBuilder));
        _xblockexpression = new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder(this.builder, this.statement);
      }
      return _xblockexpression;
    }
  }

  public static class RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final RetrieveOrRequireAbscenceOfModelElement statement;

    private RetrieveModelElementMatchBlockStatementCorrespondenceElementBuilder(final FluentRoutineBuilder builder, final RetrieveOrRequireAbscenceOfModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder affectedEObject() {
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder _xblockexpression = null;
      {
        this.builder.requireAffectedEObject = true;
        this.statement.setCorrespondenceSource(this.builder.correspondingElement(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE));
        _xblockexpression = new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder(this.builder, this.statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder newValue() {
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder _xblockexpression = null;
      {
        this.builder.requireNewValue = true;
        this.statement.setCorrespondenceSource(this.builder.correspondingElement(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE));
        _xblockexpression = new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder(this.builder, this.statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder oldValue() {
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder _xblockexpression = null;
      {
        this.builder.requireOldValue = true;
        this.statement.setCorrespondenceSource(this.builder.correspondingElement(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE));
        _xblockexpression = new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementTagBuilder(this.builder, this.statement);
      }
      return _xblockexpression;
    }
  }

  public static class UndecidedMatchStatementBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private UndecidedMatchStatementBuilder(final FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementBuilder vall(final String valName) {
      FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementBuilder _xblockexpression = null;
      {
        RetrieveModelElement _createRetrieveModelElement = LanguageFactory.eINSTANCE.createRetrieveModelElement();
        final Procedure1<RetrieveModelElement> _function = (RetrieveModelElement it) -> {
          it.setName(valName);
        };
        final RetrieveModelElement statement = ObjectExtensions.<RetrieveModelElement>operator_doubleArrow(_createRetrieveModelElement, _function);
        EList<MatchStatement> _matchStatements = this.builder.routine.getMatchBlock().getMatchStatements();
        _matchStatements.add(statement);
        _xblockexpression = new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementBuilder(this.builder, statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder requireAbsenceOf(final EClass absentMetaclass) {
      RequireAbscenceOfModelElement _createRequireAbscenceOfModelElement = LanguageFactory.eINSTANCE.createRequireAbscenceOfModelElement();
      final Procedure1<RequireAbscenceOfModelElement> _function = (RequireAbscenceOfModelElement it) -> {
        it.setElementType(this.builder.<MetaclassReference>reference(ElementsFactory.eINSTANCE.createMetaclassReference(), absentMetaclass));
      };
      final RequireAbscenceOfModelElement statement = ObjectExtensions.<RequireAbscenceOfModelElement>operator_doubleArrow(_createRequireAbscenceOfModelElement, _function);
      EList<MatchStatement> _matchStatements = this.builder.routine.getMatchBlock().getMatchStatements();
      _matchStatements.add(statement);
      return new FluentRoutineBuilder.RetrieveModelElementMatchBlockStatementCorrespondenceBuilder(this.builder, statement);
    }

    public MatchCheckStatement check(final Function<TypeProvider, XExpression> expressionBuilder) {
      MatchCheckStatement _createMatchCheckStatement = LanguageFactory.eINSTANCE.createMatchCheckStatement();
      final Procedure1<MatchCheckStatement> _function = (MatchCheckStatement it) -> {
        final Consumer<XBlockExpression> _function_1 = (XBlockExpression it_1) -> {
          EList<XExpression> _expressions = it_1.getExpressions();
          List<XExpression> _extractExpressions = FluentReactionsSegmentChildBuilder.extractExpressions(expressionBuilder.apply(this.builder.getTypeProvider(it_1)));
          Iterables.<XExpression>addAll(_expressions, _extractExpressions);
        };
        it.setCondition(this.builder.<XBlockExpression>whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), _function_1));
      };
      final MatchCheckStatement statement = ObjectExtensions.<MatchCheckStatement>operator_doubleArrow(_createMatchCheckStatement, _function);
      EList<MatchStatement> _matchStatements = this.builder.routine.getMatchBlock().getMatchStatements();
      _matchStatements.add(statement);
      return statement;
    }

    public void checkAsserted(final Function<TypeProvider, XExpression> expressionBuilder) {
      final MatchCheckStatement statement = this.check(expressionBuilder);
      statement.setAsserted(true);
    }
  }

  public static class RetrieveModelElementMatchBlockStatementTagBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final RetrieveOrRequireAbscenceOfModelElement statement;

    private RetrieveModelElementMatchBlockStatementTagBuilder(final FluentRoutineBuilder builder, final RetrieveOrRequireAbscenceOfModelElement statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public void taggedWithAnything() {
      this.statement.setTag(XbaseFactory.eINSTANCE.createXNullLiteral());
    }

    public void taggedWith(final String tag) {
      XStringLiteral _createXStringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
      final Procedure1<XStringLiteral> _function = (XStringLiteral it) -> {
        it.setValue(tag);
      };
      XStringLiteral _doubleArrow = ObjectExtensions.<XStringLiteral>operator_doubleArrow(_createXStringLiteral, _function);
      this.statement.setTag(_doubleArrow);
    }

    public void taggedWith(final Function<TypeProvider, XExpression> tagExpressionBuilder) {
      final Consumer<XBlockExpression> _function = (XBlockExpression it) -> {
        EList<XExpression> _expressions = it.getExpressions();
        List<XExpression> _extractExpressions = FluentReactionsSegmentChildBuilder.extractExpressions(tagExpressionBuilder.apply(this.builder.getTypeProvider(it)));
        Iterables.<XExpression>addAll(_expressions, _extractExpressions);
      };
      this.statement.setTag(this.builder.<XBlockExpression>whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), _function));
    }
  }

  public static class CreateStatementBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private CreateStatementBuilder(final FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public FluentRoutineBuilder.CreateStatementTypeBuilder vall(final String vallName) {
      FluentRoutineBuilder.CreateStatementTypeBuilder _xblockexpression = null;
      {
        NamedMetaclassReference _createNamedMetaclassReference = ElementsFactory.eINSTANCE.createNamedMetaclassReference();
        final Procedure1<NamedMetaclassReference> _function = (NamedMetaclassReference it) -> {
          it.setName(vallName);
        };
        final NamedMetaclassReference statement = ObjectExtensions.<NamedMetaclassReference>operator_doubleArrow(_createNamedMetaclassReference, _function);
        EList<NamedMetaclassReference> _createStatements = this.builder.routine.getCreateBlock().getCreateStatements();
        _createStatements.add(statement);
        _xblockexpression = new FluentRoutineBuilder.CreateStatementTypeBuilder(this.builder, statement);
      }
      return _xblockexpression;
    }
  }

  public static class CreateStatementTypeBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final NamedMetaclassReference statement;

    private CreateStatementTypeBuilder(final FluentRoutineBuilder builder, final NamedMetaclassReference statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public NamedMetaclassReference create(final EClass element) {
      return this.builder.<NamedMetaclassReference>reference(this.statement, element);
    }
  }

  public static class UpdateBuilder {
    @Extension
    protected final FluentRoutineBuilder builder;

    private UpdateBuilder(final FluentRoutineBuilder builder) {
      this.builder = builder;
    }

    public FluentRoutineBuilder withoutUpdate() {
      this.builder.readyToBeAttached = true;
      return this.builder;
    }

    public FluentRoutineBuilder update(final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> updates) {
      this.builder.routine.setUpdateBlock(TopLevelElementsFactory.eINSTANCE.createUpdateBlock());
      final FluentRoutineBuilder.UpdateStatementBuilder statementBuilder = new FluentRoutineBuilder.UpdateStatementBuilder(this.builder);
      updates.accept(statementBuilder);
      this.builder.readyToBeAttached = true;
      return this.builder;
    }
  }

  public static class UpdateStatementBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final XBlockExpression expressionBlock;

    private UpdateStatementBuilder(final FluentRoutineBuilder builder) {
      this.builder = builder;
      this.expressionBlock = XbaseFactory.eINSTANCE.createXBlockExpression();
      UpdateBlock _updateBlock = this.builder.routine.getUpdateBlock();
      _updateBlock.setCode(this.expressionBlock);
    }

    public boolean delete(final String existingElement) {
      boolean _xblockexpression = false;
      {
        XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
        final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
          it.setExplicitOperationCall(true);
        };
        final Consumer<XFeatureCall> _function_1 = (XFeatureCall it) -> {
          it.setFeature(this.builder.getTypeProvider(it).findMethod(AbstractRoutine.Update.class, "removeObject"));
          EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
          XFeatureCall _existingElement = this.builder.existingElement(existingElement);
          _featureCallArguments.add(_existingElement);
        };
        final XFeatureCall statement = this.builder.<XFeatureCall>whenJvmTypes(ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function), _function_1);
        EList<XExpression> _expressions = this.expressionBlock.getExpressions();
        _xblockexpression = _expressions.add(statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.CorrespondenceTargetBuilder> addCorrespondenceBetween() {
      FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.CorrespondenceTargetBuilder> _xblockexpression = null;
      {
        final XFeatureCall statement = this.createCorrespondenceMethodCall();
        EList<XExpression> _expressions = this.expressionBlock.getExpressions();
        _expressions.add(statement);
        FluentRoutineBuilder.CorrespondenceTargetBuilder _correspondenceTargetBuilder = new FluentRoutineBuilder.CorrespondenceTargetBuilder(this.builder, statement);
        final Consumer<XExpression> _function = (XExpression it) -> {
          statement.getFeatureCallArguments().add(0, it);
        };
        _xblockexpression = new FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.CorrespondenceTargetBuilder>(this.builder, _correspondenceTargetBuilder, _function);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.CorrespondenceTargetBuilder addCorrespondenceBetween(final String existingElement) {
      FluentRoutineBuilder.CorrespondenceTargetBuilder _xblockexpression = null;
      {
        XFeatureCall _createCorrespondenceMethodCall = this.createCorrespondenceMethodCall();
        final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
          EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
          XFeatureCall _existingElement = this.builder.existingElement(existingElement);
          _featureCallArguments.add(_existingElement);
        };
        final XFeatureCall statement = ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createCorrespondenceMethodCall, _function);
        EList<XExpression> _expressions = this.expressionBlock.getExpressions();
        _expressions.add(statement);
        _xblockexpression = new FluentRoutineBuilder.CorrespondenceTargetBuilder(this.builder, statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.CorrespondenceTargetBuilder addCorrespondenceBetween(final Function<TypeProvider, XExpression> expressionBuilder) {
      FluentRoutineBuilder.CorrespondenceTargetBuilder _xblockexpression = null;
      {
        XFeatureCall _createCorrespondenceMethodCall = this.createCorrespondenceMethodCall();
        final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
          EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
          XBlockExpression _existingElement = this.builder.existingElement(expressionBuilder);
          _featureCallArguments.add(_existingElement);
        };
        final XFeatureCall statement = ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createCorrespondenceMethodCall, _function);
        EList<XExpression> _expressions = this.expressionBlock.getExpressions();
        _expressions.add(statement);
        _xblockexpression = new FluentRoutineBuilder.CorrespondenceTargetBuilder(this.builder, statement);
      }
      return _xblockexpression;
    }

    private XFeatureCall createCorrespondenceMethodCall() {
      XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
      final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
        it.setExplicitOperationCall(true);
      };
      final Consumer<XFeatureCall> _function_1 = (XFeatureCall it) -> {
        it.setFeature(this.builder.getTypeProvider(it).findMethod(AbstractRoutine.Update.class, "addCorrespondenceBetween"));
      };
      return this.builder.<XFeatureCall>whenJvmTypes(ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function), _function_1);
    }

    public FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.CorrespondenceTargetBuilder> removeCorrespondenceBetween() {
      FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.CorrespondenceTargetBuilder> _xblockexpression = null;
      {
        final XFeatureCall statement = this.deleteCorrespondenceMethodCall();
        EList<XExpression> _expressions = this.expressionBlock.getExpressions();
        _expressions.add(statement);
        FluentRoutineBuilder.CorrespondenceTargetBuilder _correspondenceTargetBuilder = new FluentRoutineBuilder.CorrespondenceTargetBuilder(this.builder, statement);
        final Consumer<XExpression> _function = (XExpression it) -> {
          EList<XExpression> _featureCallArguments = statement.getFeatureCallArguments();
          _featureCallArguments.add(it);
        };
        _xblockexpression = new FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.CorrespondenceTargetBuilder>(this.builder, _correspondenceTargetBuilder, _function);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.CorrespondenceTargetBuilder removeCorrespondenceBetween(final String existingElement) {
      FluentRoutineBuilder.CorrespondenceTargetBuilder _xblockexpression = null;
      {
        XFeatureCall _deleteCorrespondenceMethodCall = this.deleteCorrespondenceMethodCall();
        final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
          EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
          XFeatureCall _existingElement = this.builder.existingElement(existingElement);
          _featureCallArguments.add(_existingElement);
        };
        final XFeatureCall statement = ObjectExtensions.<XFeatureCall>operator_doubleArrow(_deleteCorrespondenceMethodCall, _function);
        EList<XExpression> _expressions = this.expressionBlock.getExpressions();
        _expressions.add(statement);
        _xblockexpression = new FluentRoutineBuilder.CorrespondenceTargetBuilder(this.builder, statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.CorrespondenceTargetBuilder removeCorrespondenceBetween(final Function<TypeProvider, XExpression> expressionBuilder) {
      FluentRoutineBuilder.CorrespondenceTargetBuilder _xblockexpression = null;
      {
        XFeatureCall _deleteCorrespondenceMethodCall = this.deleteCorrespondenceMethodCall();
        final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
          EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
          XBlockExpression _existingElement = this.builder.existingElement(expressionBuilder);
          _featureCallArguments.add(_existingElement);
        };
        final XFeatureCall statement = ObjectExtensions.<XFeatureCall>operator_doubleArrow(_deleteCorrespondenceMethodCall, _function);
        EList<XExpression> _expressions = this.expressionBlock.getExpressions();
        _expressions.add(statement);
        _xblockexpression = new FluentRoutineBuilder.CorrespondenceTargetBuilder(this.builder, statement);
      }
      return _xblockexpression;
    }

    private XFeatureCall deleteCorrespondenceMethodCall() {
      XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
      final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
        it.setExplicitOperationCall(true);
      };
      final Consumer<XFeatureCall> _function_1 = (XFeatureCall it) -> {
        it.setFeature(this.builder.getTypeProvider(it).findMethod(AbstractRoutine.Update.class, "removeCorrespondenceBetween"));
      };
      return this.builder.<XFeatureCall>whenJvmTypes(ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function), _function_1);
    }

    public XExpression execute(final Function<TypeProvider, XExpression> expressionBuilder) {
      final XBlockExpression placeholderExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
      EList<XExpression> _expressions = this.expressionBlock.getExpressions();
      _expressions.add(placeholderExpression);
      final Consumer<XBlockExpression> _function = (XBlockExpression it) -> {
        it.getExpressions().addAll(it.getExpressions().indexOf(placeholderExpression), FluentReactionsSegmentChildBuilder.extractExpressions(expressionBuilder.apply(this.builder.getTypeProvider(it))));
        EList<XExpression> _expressions_1 = it.getExpressions();
        _expressions_1.remove(placeholderExpression);
      };
      this.builder.<XBlockExpression>whenJvmTypes(this.expressionBlock, _function);
      return this.expressionBlock;
    }

    public XExpression call(final Function<TypeProvider, XExpression> expressionBuilder) {
      this.execute(expressionBuilder);
      return this.expressionBlock;
    }

    public void call(final FluentRoutineBuilder routineBuilder, final FluentRoutineBuilder.RoutineCallParameter... parameters) {
      Preconditions.<FluentRoutineBuilder>checkNotNull(routineBuilder);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("The ");
      _builder.append(routineBuilder);
      _builder.append(" is not sufficiently initialised to be set on the ");
      _builder.append(this.builder);
      Preconditions.checkState(routineBuilder.readyToBeAttached, _builder);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("The ");
      _builder_1.append(routineBuilder);
      _builder_1.append(" requires a new value, and can thus only be called from reactions, not routines!");
      Preconditions.checkState((!routineBuilder.requireNewValue), _builder_1);
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("The ");
      _builder_2.append(routineBuilder);
      _builder_2.append(" requires an old value, and can thus only be called from reactions, not routines!");
      Preconditions.checkState(((!routineBuilder.requireOldValue) || (this.builder.valueType != null)), _builder_2);
      boolean hasFittingAffectedEObjectParameter = false;
      int _size = ((List<FluentRoutineBuilder.RoutineCallParameter>)Conversions.doWrapArray(parameters)).size();
      boolean _greaterThan = (_size > 0);
      if (_greaterThan) {
        final FluentRoutineBuilder.RoutineCallParameter param = parameters[0];
        boolean _isParameterArgumentType = param.isParameterArgumentType();
        if (_isParameterArgumentType) {
          hasFittingAffectedEObjectParameter = true;
        }
      }
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("The ");
      _builder_3.append(routineBuilder);
      _builder_3.append(" requires an affectedEObject, and can thus only be called from reactions, not");
      _builder_3.append(" routines!");
      Preconditions.checkState(
        ((!routineBuilder.requireAffectedEObject) || (routineBuilder.requireAffectedEObject && hasFittingAffectedEObjectParameter)), _builder_3);
      this.builder.transferReactionsSegmentTo(this.builder, routineBuilder);
      this.addRoutineCall(routineBuilder, parameters);
    }

    private boolean addRoutineCall(final FluentRoutineBuilder routineBuilder, final FluentRoutineBuilder.RoutineCallParameter... parameters) {
      EList<XExpression> _expressions = this.expressionBlock.getExpressions();
      XFeatureCall _routineCall = this.routineCall(routineBuilder, parameters);
      return _expressions.add(_routineCall);
    }

    private XFeatureCall routineCall(final FluentRoutineBuilder routineBuilder, final FluentRoutineBuilder.RoutineCallParameter... parameters) {
      XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
      final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
        it.setExplicitOperationCall(true);
      };
      final Consumer<XFeatureCall> _function_1 = (XFeatureCall it) -> {
        it.setFeature(routineBuilder.getJvmOperation());
        it.setImplicitReceiver(this.builder.getJvmOperationRoutineFacade(it));
        final TypeProvider typeProvider = this.builder.getTypeProvider(it);
        EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
        final Function1<FluentRoutineBuilder.RoutineCallParameter, XExpression> _function_2 = (FluentRoutineBuilder.RoutineCallParameter it_1) -> {
          return it_1.getExpression(typeProvider);
        };
        List<XExpression> _map = ListExtensions.<FluentRoutineBuilder.RoutineCallParameter, XExpression>map(((List<FluentRoutineBuilder.RoutineCallParameter>)Conversions.doWrapArray(parameters)), _function_2);
        Iterables.<XExpression>addAll(_featureCallArguments, _map);
      };
      return this.builder.<XFeatureCall>whenJvmTypes(ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function), _function_1);
    }
  }

  public static class RoutineCallParameter {
    private Object argument;

    public RoutineCallParameter(final String parameter) {
      this.argument = parameter;
    }

    public RoutineCallParameter(final XExpression expression) {
      this.argument = expression;
    }

    public RoutineCallParameter(final Function<TypeProvider, XExpression> expressionBuilder) {
      this.argument = expressionBuilder;
    }

    public boolean isParameterArgumentType() {
      return (this.argument instanceof String);
    }

    public XExpression getExpression(final TypeProvider typeProvider) {
      boolean _isParameterArgumentType = this.isParameterArgumentType();
      if (_isParameterArgumentType) {
        return typeProvider.variable(((String) this.argument));
      } else {
        if ((this.argument instanceof XExpression)) {
          return ((XExpression)this.argument);
        } else {
          final Function<TypeProvider, XExpression> expressionBuilder = ((Function<TypeProvider, XExpression>) this.argument);
          return expressionBuilder.apply(typeProvider);
        }
      }
    }
  }

  public static class CorrespondenceElementBuilder<NextType extends Object> {
    @Extension
    private final FluentRoutineBuilder builder;

    private final Consumer<XExpression> elementConsumer;

    private final NextType next;

    private CorrespondenceElementBuilder(final FluentRoutineBuilder builder, final NextType next, final Consumer<XExpression> elementConsumer) {
      this.builder = builder;
      this.elementConsumer = elementConsumer;
      this.next = next;
    }

    public NextType oldValue() {
      NextType _xblockexpression = null;
      {
        this.builder.requireOldValue = true;
        this.elementConsumer.accept(this.builder.existingElement(ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE));
        _xblockexpression = this.next;
      }
      return _xblockexpression;
    }

    public NextType newValue() {
      NextType _xblockexpression = null;
      {
        this.builder.requireNewValue = true;
        this.elementConsumer.accept(this.builder.existingElement(ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE));
        _xblockexpression = this.next;
      }
      return _xblockexpression;
    }

    public NextType affectedEObject() {
      NextType _xblockexpression = null;
      {
        this.builder.requireAffectedEObject = true;
        this.elementConsumer.accept(this.builder.existingElement(ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE));
        _xblockexpression = this.next;
      }
      return _xblockexpression;
    }
  }

  public static class CorrespondenceTargetBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final XFeatureCall statement;

    private CorrespondenceTargetBuilder(final FluentRoutineBuilder builder, final XFeatureCall statement) {
      this.builder = builder;
      this.statement = statement;
    }

    public FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.TagWithBuilder> and() {
      FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.TagWithBuilder> _xblockexpression = null;
      {
        final FluentRoutineBuilder.TagWithBuilder tagBuilder = new FluentRoutineBuilder.TagWithBuilder(this.builder, this.statement);
        final Consumer<XExpression> _function = (XExpression it) -> {
          this.setSecondElement(this.statement, it);
        };
        _xblockexpression = new FluentRoutineBuilder.CorrespondenceElementBuilder<FluentRoutineBuilder.TagWithBuilder>(this.builder, tagBuilder, _function);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.TagWithBuilder and(final String existingElement) {
      FluentRoutineBuilder.TagWithBuilder _xblockexpression = null;
      {
        this.setSecondElement(this.statement, this.builder.existingElement(existingElement));
        _xblockexpression = new FluentRoutineBuilder.TagWithBuilder(this.builder, this.statement);
      }
      return _xblockexpression;
    }

    public FluentRoutineBuilder.TagWithBuilder and(final Function<TypeProvider, XExpression> expressionBuilder) {
      FluentRoutineBuilder.TagWithBuilder _xblockexpression = null;
      {
        this.setSecondElement(this.statement, this.builder.existingElement(expressionBuilder));
        _xblockexpression = new FluentRoutineBuilder.TagWithBuilder(this.builder, this.statement);
      }
      return _xblockexpression;
    }

    private XFeatureCall setSecondElement(final XFeatureCall correspondenceStatement, final XExpression existingElement) {
      final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
        EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
        _featureCallArguments.add(existingElement);
      };
      return ObjectExtensions.<XFeatureCall>operator_doubleArrow(correspondenceStatement, _function);
    }
  }

  public static class TagWithBuilder {
    @Extension
    private final FluentRoutineBuilder builder;

    private final XFeatureCall correspondenceCreation;

    private TagWithBuilder(final FluentRoutineBuilder builder, final XFeatureCall correspondenceCreation) {
      this.builder = builder;
      this.correspondenceCreation = correspondenceCreation;
    }

    public void taggedWithAnything() {
      EList<XExpression> _featureCallArguments = this.correspondenceCreation.getFeatureCallArguments();
      XNullLiteral _createXNullLiteral = XbaseFactory.eINSTANCE.createXNullLiteral();
      _featureCallArguments.add(_createXNullLiteral);
    }

    public void taggedWith(final String tag) {
      EList<XExpression> _featureCallArguments = this.correspondenceCreation.getFeatureCallArguments();
      XStringLiteral _createXStringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
      final Procedure1<XStringLiteral> _function = (XStringLiteral it) -> {
        it.setValue(tag);
      };
      XStringLiteral _doubleArrow = ObjectExtensions.<XStringLiteral>operator_doubleArrow(_createXStringLiteral, _function);
      _featureCallArguments.add(_doubleArrow);
    }

    public void taggedWith(final Function<TypeProvider, XExpression> tagExpressionBuilder) {
      EList<XExpression> _featureCallArguments = this.correspondenceCreation.getFeatureCallArguments();
      final Consumer<XBlockExpression> _function = (XBlockExpression it) -> {
        EList<XExpression> _expressions = it.getExpressions();
        List<XExpression> _extractExpressions = FluentReactionsSegmentChildBuilder.extractExpressions(tagExpressionBuilder.apply(this.builder.getTypeProvider(it)));
        Iterables.<XExpression>addAll(_expressions, _extractExpressions);
      };
      XBlockExpression _whenJvmTypes = this.builder.<XBlockExpression>whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), _function);
      _featureCallArguments.add(_whenJvmTypes);
    }
  }

  @Accessors(AccessorType.PACKAGE_GETTER)
  protected Routine routine;

  @Accessors(AccessorType.PACKAGE_GETTER)
  protected boolean requireOldValue = false;

  @Accessors(AccessorType.PACKAGE_GETTER)
  protected boolean requireNewValue = false;

  @Accessors(AccessorType.PACKAGE_GETTER)
  protected boolean requireAffectedEObject = false;

  @Accessors(AccessorType.PACKAGE_GETTER)
  protected boolean requireAffectedValue = false;

  private EClassifier valueType;

  private EClass affectedObjectType;

  FluentRoutineBuilder(final String routineName, final FluentBuilderContext context) {
    super(context);
    Routine _createRoutine = TopLevelElementsFactory.eINSTANCE.createRoutine();
    final Procedure1<Routine> _function = (Routine it) -> {
      it.setName(routineName);
      it.setInput(TopLevelElementsFactory.eINSTANCE.createRoutineInput());
    };
    Routine _doubleArrow = ObjectExtensions.<Routine>operator_doubleArrow(_createRoutine, _function);
    this.routine = _doubleArrow;
  }

  @Override
  protected void attachmentPreparation() {
    super.attachmentPreparation();
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Although required, there was no value type set on the ");
    _builder.append(this);
    Preconditions.checkState(((((!this.requireOldValue) && (!this.requireNewValue)) && (!this.requireAffectedValue)) || (this.valueType != null)), _builder);
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("Although required, there was no affected object type set on the ");
    _builder_1.append(this);
    Preconditions.checkState(((!this.requireAffectedEObject) || (this.affectedObjectType != null)), _builder_1);
    if (this.requireAffectedEObject) {
      this.addInputElementIfNotExists(this.affectedObjectType, ReactionsLanguageConstants.CHANGE_AFFECTED_ELEMENT_ATTRIBUTE);
    }
    if (this.requireOldValue) {
      this.addInputElementIfNotExists(this.valueType, ReactionsLanguageConstants.CHANGE_OLD_VALUE_ATTRIBUTE);
    }
    if (this.requireNewValue) {
      this.addInputElementIfNotExists(this.valueType, ReactionsLanguageConstants.CHANGE_NEW_VALUE_ATTRIBUTE);
    }
  }

  FluentRoutineBuilder.RoutineStartBuilder start() {
    return new FluentRoutineBuilder.RoutineStartBuilder(this);
  }

  private void addInputElementIfNotExists(final EClassifier type, final String parameterName) {
    final Function1<NamedMetaclassReference, Boolean> _function = (NamedMetaclassReference it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, parameterName));
    };
    NamedMetaclassReference _findFirst = IterableExtensions.<NamedMetaclassReference>findFirst(this.routine.getInput().getModelInputElements(), _function);
    boolean _tripleNotEquals = (_findFirst != null);
    if (_tripleNotEquals) {
      return;
    }
    this.addInputElement(type, parameterName);
  }

  private void _addInputElement(final EClass type, final String parameterName) {
    EList<NamedMetaclassReference> _modelInputElements = this.routine.getInput().getModelInputElements();
    NamedMetaclassReference _createNamedMetaclassReference = ElementsFactory.eINSTANCE.createNamedMetaclassReference();
    final Procedure1<NamedMetaclassReference> _function = (NamedMetaclassReference it) -> {
      it.setName(parameterName);
    };
    NamedMetaclassReference _reference = this.<NamedMetaclassReference>reference(ObjectExtensions.<NamedMetaclassReference>operator_doubleArrow(_createNamedMetaclassReference, _function), type);
    _modelInputElements.add(_reference);
  }

  private void _addInputElement(final EDataType type, final String parameterName) {
    this.addInputElement(type.getInstanceClass(), parameterName);
  }

  private void _addInputElement(final Class<?> type, final String parameterName) {
    EList<NamedJavaElementReference> _javaInputElements = this.routine.getInput().getJavaInputElements();
    NamedJavaElementReference _createNamedJavaElementReference = TopLevelElementsFactory.eINSTANCE.createNamedJavaElementReference();
    final Procedure1<NamedJavaElementReference> _function = (NamedJavaElementReference it) -> {
      it.setName(parameterName);
    };
    NamedJavaElementReference _reference = this.<NamedJavaElementReference>reference(ObjectExtensions.<NamedJavaElementReference>operator_doubleArrow(_createNamedJavaElementReference, _function), type);
    _javaInputElements.add(_reference);
  }

  void setValueType(final EClassifier type) {
    if ((this.valueType == null)) {
      this.valueType = type;
    }
    boolean _isAssignableFrom = FluentRoutineBuilder.isAssignableFrom(this.valueType, type);
    boolean _not = (!_isAssignableFrom);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("The ");
      _builder.append(this);
      _builder.append(" already has the value type “");
      String _name = this.valueType.getName();
      _builder.append(_name);
      _builder.append("” set, which is not a super type of “");
      String _name_1 = type.getName();
      _builder.append(_name_1);
      _builder.append("”. The value type can thus not be set to “");
      String _name_2 = type.getName();
      _builder.append(_name_2);
      _builder.append("”!");
      throw new IllegalStateException(_builder.toString());
    }
  }

  void setAffectedObjectType(final EClass type) {
    if ((this.affectedObjectType == null)) {
      this.affectedObjectType = type;
    }
    boolean _isAssignableFrom = FluentRoutineBuilder.isAssignableFrom(this.affectedObjectType, type);
    boolean _not = (!_isAssignableFrom);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("The ");
      _builder.append(this);
      _builder.append(" already has the affected object type “");
      String _name = this.affectedObjectType.getName();
      _builder.append(_name);
      _builder.append("” set, which is not a super type of “");
      String _name_1 = type.getName();
      _builder.append(_name_1);
      _builder.append("”. The affected element type can thus not be set to “");
      String _name_2 = type.getName();
      _builder.append(_name_2);
      _builder.append("”!");
      throw new IllegalStateException(_builder.toString());
    }
  }

  protected static boolean _isAssignableFrom(final EDataType a, final EClass b) {
    return false;
  }

  protected static boolean _isAssignableFrom(final EClass a, final EDataType b) {
    return false;
  }

  protected static boolean _isAssignableFrom(final EClass a, final EClass b) {
    return EcoreUtil2.isAssignableFrom(a, b);
  }

  protected static boolean _isAssignableFrom(final EDataType a, final EDataType b) {
    return a.getInstanceClass().isAssignableFrom(b.getInstanceClass());
  }

  private XFeatureCall existingElement(final String name) {
    final Consumer<XFeatureCall> _function = (XFeatureCall it) -> {
      it.setFeature(this.correspondingMethodParameter(it, name));
    };
    return this.<XFeatureCall>whenJvmTypes(XbaseFactory.eINSTANCE.createXFeatureCall(), _function);
  }

  private XBlockExpression existingElement(final Function<TypeProvider, XExpression> expressionBuilder) {
    final Consumer<XBlockExpression> _function = (XBlockExpression it) -> {
      EList<XExpression> _expressions = it.getExpressions();
      List<XExpression> _extractExpressions = FluentReactionsSegmentChildBuilder.extractExpressions(expressionBuilder.apply(this.getTypeProvider(it)));
      Iterables.<XExpression>addAll(_expressions, _extractExpressions);
    };
    return this.<XBlockExpression>whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), _function);
  }

  private XFeatureCall correspondingElement(final String name) {
    final Consumer<XFeatureCall> _function = (XFeatureCall it) -> {
      it.setFeature(this.correspondingMethodParameter(it, name));
    };
    return this.<XFeatureCall>whenJvmTypes(XbaseFactory.eINSTANCE.createXFeatureCall(), _function);
  }

  private XBlockExpression correspondingElement(final Function<TypeProvider, XExpression> expressionBuilder) {
    final Consumer<XBlockExpression> _function = (XBlockExpression it) -> {
      EList<XExpression> _expressions = it.getExpressions();
      List<XExpression> _extractExpressions = FluentReactionsSegmentChildBuilder.extractExpressions(expressionBuilder.apply(this.getTypeProvider(it)));
      Iterables.<XExpression>addAll(_expressions, _extractExpressions);
    };
    return this.<XBlockExpression>whenJvmTypes(XbaseFactory.eINSTANCE.createXBlockExpression(), _function);
  }

  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("routine builder for ");
    String _name = this.routine.getName();
    _builder.append(_name);
    return _builder.toString();
  }

  public JvmOperation getJvmOperation() {
    final EObject jvmMethod = this.context.getJvmModelAssociator().getPrimaryJvmElement(this.routine);
    if ((jvmMethod instanceof JvmOperation)) {
      return ((JvmOperation)jvmMethod);
    }
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Could not find the routine facade method corresponding to the routine “");
    String _name = this.routine.getName();
    _builder.append(_name);
    _builder.append("”");
    throw new IllegalStateException(_builder.toString());
  }

  @Override
  protected String getCreatedElementName() {
    return this.routine.getName();
  }

  @Override
  protected String getCreatedElementType() {
    return "routine";
  }

  private void addInputElement(final Object type, final String parameterName) {
    if (type instanceof EClass) {
      _addInputElement((EClass)type, parameterName);
      return;
    } else if (type instanceof EDataType) {
      _addInputElement((EDataType)type, parameterName);
      return;
    } else if (type instanceof Class) {
      _addInputElement((Class<?>)type, parameterName);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(type, parameterName).toString());
    }
  }

  public static boolean isAssignableFrom(final EClassifier a, final EClassifier b) {
    if (a instanceof EClass
         && b instanceof EClass) {
      return _isAssignableFrom((EClass)a, (EClass)b);
    } else if (a instanceof EClass
         && b instanceof EDataType) {
      return _isAssignableFrom((EClass)a, (EDataType)b);
    } else if (a instanceof EDataType
         && b instanceof EClass) {
      return _isAssignableFrom((EDataType)a, (EClass)b);
    } else if (a instanceof EDataType
         && b instanceof EDataType) {
      return _isAssignableFrom((EDataType)a, (EDataType)b);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(a, b).toString());
    }
  }

  @Pure
  Routine getRoutine() {
    return this.routine;
  }

  @Pure
  boolean getRequireOldValue() {
    return this.requireOldValue;
  }

  @Pure
  boolean getRequireNewValue() {
    return this.requireNewValue;
  }

  @Pure
  boolean isRequireAffectedEObject() {
    return this.requireAffectedEObject;
  }

  @Pure
  boolean getRequireAffectedValue() {
    return this.requireAffectedValue;
  }
}
