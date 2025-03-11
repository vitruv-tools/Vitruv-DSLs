package tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.common.elements.NamedMetaclassReference;
import tools.vitruv.dsls.commonalities.generator.helper.ContainmentHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsSubGenerator;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.ApplyCommonalityAttributesRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.InsertIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.AttributeReferenceMatchingReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.ParticipationMatchingReactionsBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectInitializationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.reference.ReferenceMappingOperatorHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.resource.InsertResourceBridgeRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.resource.ResourceBridgeHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.util.guice.InjectingFactoryBase;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.Containment;
import tools.vitruv.dsls.commonalities.participation.OperatorContainment;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.participation.ParticipationContextHelper;
import tools.vitruv.dsls.commonalities.participation.ReferenceContainment;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class CommonalityInsertReactionsBuilder extends ReactionsSubGenerator {
  public static class Factory extends InjectingFactoryBase {
    public CommonalityInsertReactionsBuilder createFor(final Participation targetParticipation) {
      return this.<CommonalityInsertReactionsBuilder>injectMembers(new CommonalityInsertReactionsBuilder(targetParticipation));
    }
  }

  @Inject
  @Extension
  private ResourceBridgeHelper _resourceBridgeHelper;

  @Inject
  @Extension
  private ContainmentHelper _containmentHelper;

  @Inject
  @Extension
  private ParticipationObjectInitializationHelper _participationObjectInitializationHelper;

  @Inject
  @Extension
  private ReferenceMappingOperatorHelper _referenceMappingOperatorHelper;

  @Inject
  @Extension
  private IntermediateContainmentReactionsHelper _intermediateContainmentReactionsHelper;

  @Inject
  @Extension
  private InsertResourceBridgeRoutineBuilder.Provider _provider;

  @Inject
  private ParticipationMatchingReactionsBuilder.Provider participationMatchingReactionsBuilderProvider;

  @Inject
  @Extension
  private InsertIntermediateRoutineBuilder.Provider _provider_1;

  @Inject
  @Extension
  private ApplyCommonalityAttributesRoutineBuilder.Provider _provider_2;

  @Inject
  @Extension
  private AttributeReferenceMatchingReactionsBuilder.Provider _provider_3;

  private final Participation targetParticipation;

  private final Map<Participation, Optional<FluentRoutineBuilder>> matchSubParticipationsRoutines = new HashMap<Participation, Optional<FluentRoutineBuilder>>();

  private CommonalityInsertReactionsBuilder(final Participation targetParticipation) {
    Preconditions.<Participation>checkNotNull(targetParticipation, "targetParticipation is null");
    this.targetParticipation = targetParticipation;
  }

  CommonalityInsertReactionsBuilder() {
    this.targetParticipation = null;
    throw new IllegalStateException("Use the Factory to create instances of this class!");
  }

  public void generateReactions(final FluentReactionsSegmentBuilder segment) {
    final Optional<ParticipationContext> participationContext = ParticipationContextHelper.getParticipationContext(this.targetParticipation);
    final Consumer<ParticipationContext> _function = (ParticipationContext it) -> {
      FluentReactionBuilder _reactionForCommonalityInsert = this.reactionForCommonalityInsert(it, segment);
      segment.operator_add(_reactionForCommonalityInsert);
      FluentReactionBuilder _reactionForCommonalityRemove = this.reactionForCommonalityRemove(it);
      segment.operator_add(_reactionForCommonalityRemove);
    };
    participationContext.ifPresent(_function);
    final Function1<CommonalityReference, Iterable<ParticipationContext>> _function_1 = (CommonalityReference it) -> {
      return ParticipationContextHelper.getReferenceParticipationContexts(it);
    };
    final Function1<ParticipationContext, Boolean> _function_2 = (ParticipationContext it) -> {
      String _domainName = it.getParticipation().getDomainName();
      String _domainName_1 = this.targetParticipation.getDomainName();
      return Boolean.valueOf(Objects.equal(_domainName, _domainName_1));
    };
    final Consumer<ParticipationContext> _function_3 = (ParticipationContext it) -> {
      FluentReactionBuilder _reactionForCommonalityInsert = this.reactionForCommonalityInsert(it, segment);
      segment.operator_add(_reactionForCommonalityInsert);
      FluentReactionBuilder _reactionForCommonalityRemove = this.reactionForCommonalityRemove(it);
      segment.operator_add(_reactionForCommonalityRemove);
      final Consumer<FluentReactionBuilder> _function_4 = (FluentReactionBuilder it_1) -> {
        segment.operator_add(it_1);
      };
      this.reactionForCommonalityCreate(it, segment).ifPresent(_function_4);
    };
    IterableExtensions.<ParticipationContext>filter(IterableExtensions.<CommonalityReference, ParticipationContext>flatMap(this._generationContext.getCommonality().getReferences(), _function_1), _function_2).forEach(_function_3);
  }

  private FluentReactionBuilder reactionForCommonalityInsert(final ParticipationContext participationContext, final FluentReactionsSegmentBuilder segment) {
    final Participation participation = participationContext.getParticipation();
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
    FluentReactionBuilder.RoutineCallBuilder reaction = null;
    boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
    if (_isForReferenceMapping) {
      final CommonalityReference reference = participationContext.getDeclaringReference();
      StringConcatenation _builder = new StringConcatenation();
      String _reactionName = ReactionsGeneratorConventions.getReactionName(commonality);
      _builder.append(_reactionName);
      _builder.append("_insertedAt_");
      String _shortReactionName = ReactionsGeneratorConventions.getShortReactionName(reference);
      _builder.append(_shortReactionName);
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder.append(_reactionNameSuffix);
      final Function<TypeProvider, XExpression> _function = (TypeProvider it) -> {
        return this._intermediateContainmentReactionsHelper.isIntermediateContainerMatching(it, it.newValue(), it.affectedEObject(), reference);
      };
      reaction = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterElement(this._generationContext.getChangeClass(commonality)).insertedIn(this._generationContext.getCorrespondingEReference(reference)).with(_function);
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      String _reactionName_1 = ReactionsGeneratorConventions.getReactionName(commonality);
      _builder_1.append(_reactionName_1);
      _builder_1.append("_insertedAtRoot");
      String _reactionNameSuffix_1 = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder_1.append(_reactionNameSuffix_1);
      final Function<TypeProvider, XExpression> _function_1 = (TypeProvider it) -> {
        return this._intermediateContainmentReactionsHelper.isIntermediateContainedAtRoot(it, it.newValue());
      };
      reaction = ReactionsHelper.afterElementInsertedAsRoot(this._reactionsGenerationContext.getCreate().reaction(_builder_1.toString()), this._generationContext.getChangeClass(commonality)).with(_function_1);
    }
    final Optional<FluentRoutineBuilder> matchSubParticipationsRoutine = this.getMatchSubParticipationsRoutine(participationContext, segment);
    boolean _isPresent = matchSubParticipationsRoutine.isPresent();
    if (_isPresent) {
      FluentRoutineBuilder _get = matchSubParticipationsRoutine.get();
      final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it) -> {
        return it.newValue();
      };
      FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function_2);
      reaction.call(_get, _routineCallParameter);
    }
    boolean _isForSingletonRoot = participationContext.isForSingletonRoot();
    if (_isForSingletonRoot) {
      FluentRoutineBuilder _createSingletonRoutine = this.createSingletonRoutine(participationContext, segment);
      final Function<TypeProvider, XExpression> _function_3 = (TypeProvider it) -> {
        return it.newValue();
      };
      FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_function_3);
      reaction.call(_createSingletonRoutine, _routineCallParameter_1);
    }
    List<FluentRoutineBuilder.RoutineCallParameter> parameters = null;
    boolean _isForReferenceMapping_1 = participationContext.isForReferenceMapping();
    if (_isForReferenceMapping_1) {
      final Function<TypeProvider, XExpression> _function_4 = (TypeProvider it) -> {
        return it.newValue();
      };
      FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_2 = new FluentRoutineBuilder.RoutineCallParameter(_function_4);
      final Function<TypeProvider, XExpression> _function_5 = (TypeProvider it) -> {
        return it.affectedEObject();
      };
      FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_3 = new FluentRoutineBuilder.RoutineCallParameter(_function_5);
      parameters = Collections.<FluentRoutineBuilder.RoutineCallParameter>unmodifiableList(CollectionLiterals.<FluentRoutineBuilder.RoutineCallParameter>newArrayList(_routineCallParameter_2, _routineCallParameter_3));
    } else {
      final Function<TypeProvider, XExpression> _function_6 = (TypeProvider it) -> {
        return it.newValue();
      };
      FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_4 = new FluentRoutineBuilder.RoutineCallParameter(_function_6);
      parameters = Collections.<FluentRoutineBuilder.RoutineCallParameter>unmodifiableList(CollectionLiterals.<FluentRoutineBuilder.RoutineCallParameter>newArrayList(_routineCallParameter_4));
    }
    final List<FluentRoutineBuilder.RoutineCallParameter> _converted_parameters = (List<FluentRoutineBuilder.RoutineCallParameter>)parameters;
    return reaction.call(this.createParticipationRoutine(participationContext, segment), ((FluentRoutineBuilder.RoutineCallParameter[])Conversions.unwrapArray(_converted_parameters, FluentRoutineBuilder.RoutineCallParameter.class)));
  }

  private FluentRoutineBuilder createParticipationRoutine(final ParticipationContext participationContext, final FluentReactionsSegmentBuilder segment) {
    final Participation participation = participationContext.getParticipation();
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
    final List<ParticipationContext.ContextClass> managedClasses = IterableExtensions.<ParticipationContext.ContextClass>toList(participationContext.getManagedClasses());
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("createParticipation_");
    String _name = participation.getName();
    _builder.append(_name);
    String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
    _builder.append(_reactionNameSuffix);
    final Consumer<FluentRoutineBuilder.InputBuilder> _function = (FluentRoutineBuilder.InputBuilder it) -> {
      it.model(this._generationContext.getChangeClass(commonality), ReactionsGeneratorConventions.INTERMEDIATE);
      boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
      if (_isForReferenceMapping) {
        final Commonality referencingCommonality = participationContext.getReferencingCommonality();
        it.model(this._generationContext.getChangeClass(referencingCommonality), ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE);
      }
    };
    final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_1 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it) -> {
      boolean _isForSingletonRoot = participationContext.isForSingletonRoot();
      if (_isForSingletonRoot) {
        final ParticipationClass singletonClass = CommonalitiesLanguageModelExtensions.getSingletonClass(participation);
        final EClass singletonEClass = this._generationContext.getChangeClass(singletonClass);
        final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it_1) -> {
          return EmfAccessExpressions.getEClass(it_1, singletonEClass);
        };
        it.vall(ReactionsGeneratorConventions.correspondingVariableName(singletonClass)).retrieveAsserted(singletonEClass).correspondingTo(_function_2);
      }
      final Consumer<ParticipationContext.ContextClass> _function_3 = (ParticipationContext.ContextClass contextClass) -> {
        boolean _isExternal = contextClass.isExternal();
        boolean _not = (!_isExternal);
        XtendAssertHelper.assertTrue(_not);
        final ParticipationClass participationClass = contextClass.getParticipationClass();
        it.requireAbsenceOf(this._generationContext.getChangeClass(participationClass)).correspondingTo(ReactionsGeneratorConventions.INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
      };
      managedClasses.forEach(_function_3);
      boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
      if (_isForReferenceMapping) {
        final Consumer<ParticipationContext.ContextClass> _function_4 = (ParticipationContext.ContextClass contextClass) -> {
          XtendAssertHelper.assertTrue(contextClass.isExternal());
          final ParticipationClass externalParticipationClass = contextClass.getParticipationClass();
          it.vall(ReactionsGeneratorConventions.correspondingVariableName(contextClass)).retrieveAsserted(this._generationContext.getChangeClass(externalParticipationClass)).correspondingTo(ReactionsGeneratorConventions.REFERENCING_INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(externalParticipationClass));
        };
        participationContext.getReferenceRootClasses().forEach(_function_4);
      }
    };
    final Consumer<FluentRoutineBuilder.CreateStatementBuilder> _function_2 = (FluentRoutineBuilder.CreateStatementBuilder it) -> {
      final List<ParticipationContext.ContextClass> classes = managedClasses;
      this.createParticipationObjects(it, classes);
    };
    final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it) -> {
      final List<ParticipationContext.ContextClass> classes = managedClasses;
      final Iterable<ParticipationContext.ContextContainment<?>> containments = participationContext.getManagedContainments();
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_4 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Consumer<ParticipationContext.ContextClass> _function_5 = (ParticipationContext.ContextClass contextClass) -> {
          boolean _isExternal = contextClass.isExternal();
          boolean _not = (!_isExternal);
          XtendAssertHelper.assertTrue(_not);
          this.addIntermediateCorrespondence(it_1, contextClass.getParticipationClass());
        };
        managedClasses.forEach(_function_5);
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> correspondenceSetup = _function_4;
      final boolean applyAttributes = true;
      this.initializeParticipationObjects(it, segment, participationContext, classes, containments, applyAttributes, correspondenceSetup);
    };
    return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function).match(_function_1).create(_function_2).update(_function_3);
  }

  private FluentRoutineBuilder createSingletonRoutine(final ParticipationContext participationContext, final FluentReactionsSegmentBuilder segment) {
    XtendAssertHelper.assertTrue(participationContext.isForSingletonRoot());
    final Participation participation = participationContext.getParticipation();
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
    final ParticipationClass singletonClass = CommonalitiesLanguageModelExtensions.getSingletonClass(participation);
    final EClass singletonEClass = this._generationContext.getChangeClass(singletonClass);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("createSingleton_");
    String _name = participation.getName();
    _builder.append(_name);
    _builder.append("_");
    String _name_1 = singletonClass.getName();
    _builder.append(_name_1);
    final Consumer<FluentRoutineBuilder.InputBuilder> _function = (FluentRoutineBuilder.InputBuilder it) -> {
      it.model(this._generationContext.getChangeClass(commonality), ReactionsGeneratorConventions.INTERMEDIATE);
    };
    final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_1 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it) -> {
      final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it_1) -> {
        return EmfAccessExpressions.getEClass(it_1, singletonEClass);
      };
      it.requireAbsenceOf(singletonEClass).correspondingTo(_function_2);
    };
    final Consumer<FluentRoutineBuilder.CreateStatementBuilder> _function_2 = (FluentRoutineBuilder.CreateStatementBuilder it) -> {
      final List<ParticipationContext.ContextClass> classes = participationContext.getRootClasses();
      this.createParticipationObjects(it, classes);
    };
    final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it) -> {
      final List<ParticipationContext.ContextClass> classes = participationContext.getRootClasses();
      final List<ParticipationContext.ContextContainment<?>> containments = participationContext.getRootContainments();
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_4 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final ParticipationContext.ContextClass resourceClass = participationContext.getResourceClass();
        XtendAssertHelper.assertTrue((resourceClass != null));
        boolean _isExternal = resourceClass.isExternal();
        boolean _not = (!_isExternal);
        XtendAssertHelper.assertTrue(_not);
        this.addIntermediateCorrespondence(it_1, resourceClass.getParticipationClass());
        final Function<TypeProvider, XExpression> _function_5 = (TypeProvider it_2) -> {
          return EmfAccessExpressions.getEClass(it_2, singletonEClass);
        };
        it_1.addCorrespondenceBetween(ReactionsGeneratorConventions.correspondingVariableName(singletonClass)).and(_function_5);
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> correspondenceSetup = _function_4;
      final boolean applyAttributes = false;
      this.initializeParticipationObjects(it, segment, participationContext, classes, containments, applyAttributes, correspondenceSetup);
    };
    return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function).match(_function_1).create(_function_2).update(_function_3);
  }

  private void createParticipationObjects(@Extension final FluentRoutineBuilder.CreateStatementBuilder it, final Iterable<ParticipationContext.ContextClass> classes) {
    final Consumer<ParticipationContext.ContextClass> _function = (ParticipationContext.ContextClass contextClass) -> {
      this.createParticipationObject(it, contextClass.getParticipationClass());
    };
    classes.forEach(_function);
  }

  private XExpression initializeParticipationObjects(@Extension final FluentRoutineBuilder.UpdateStatementBuilder it, final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext, final Iterable<ParticipationContext.ContextClass> classes, final Iterable<ParticipationContext.ContextContainment<?>> containments, final boolean applyAttributes, final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> correspondenceSetup) {
    XExpression _xblockexpression = null;
    {
      final Participation participation = participationContext.getParticipation();
      final Consumer<ParticipationContext.ContextClass> _function = (ParticipationContext.ContextClass contextClass) -> {
        this.initializeParticipationObject(it, contextClass.getParticipationClass());
      };
      classes.forEach(_function);
      correspondenceSetup.accept(it);
      this.setupResourceBridgeCorrespondences(it, containments);
      this.executePostInitializers(it, participationContext, classes);
      if (applyAttributes) {
        FluentRoutineBuilder _applyAttributesRoutine = this._provider_2.getApplyAttributesRoutine(segment, participation);
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
        it.call(_applyAttributesRoutine, _routineCallParameter);
      }
      final Function1<ParticipationContext.ContextClass, Boolean> _function_1 = (ParticipationContext.ContextClass it_1) -> {
        return Boolean.valueOf(CommonalitiesLanguageModelExtensions.isForResource(it_1.getParticipationClass()));
      };
      final Consumer<ParticipationContext.ContextClass> _function_2 = (ParticipationContext.ContextClass it_1) -> {
        final ParticipationClass resourceClass = it_1.getParticipationClass();
        FluentRoutineBuilder _insertResourceBridgeRoutine = this._provider.getInsertResourceBridgeRoutine(segment, resourceClass);
        String _correspondingVariableName = ReactionsGeneratorConventions.correspondingVariableName(resourceClass);
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_correspondingVariableName);
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_2 = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
        it.call(_insertResourceBridgeRoutine, _routineCallParameter_1, _routineCallParameter_2);
      };
      IterableExtensions.<ParticipationContext.ContextClass>filter(classes, _function_1).forEach(_function_2);
      if ((participationContext.isRootContext() && CommonalitiesLanguageModelExtensions.isCommonalityParticipation(participation))) {
        this.insertCommonalityParticipationClasses(it, participation, segment);
      }
      final Function<TypeProvider, XExpression> _function_3 = (TypeProvider it_1) -> {
        return this.setupContainments(it_1, containments);
      };
      it.execute(_function_3);
      final Function1<ParticipationContext.ContextClass, ParticipationClass> _function_4 = (ParticipationContext.ContextClass it_1) -> {
        return it_1.getParticipationClass();
      };
      final Function1<ParticipationClass, Boolean> _function_5 = (ParticipationClass it_1) -> {
        return Boolean.valueOf(CommonalitiesLanguageModelExtensions.isForResource(it_1));
      };
      final List<ParticipationClass> resourceClasses = IterableExtensions.<ParticipationClass>toList(IterableExtensions.<ParticipationClass>filter(IterableExtensions.<ParticipationContext.ContextClass, ParticipationClass>map(classes, _function_4), _function_5));
      XExpression _xifexpression = null;
      boolean _isEmpty = resourceClasses.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function<TypeProvider, XExpression> _function_6 = (TypeProvider typeProvider) -> {
          XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
          final Procedure1<XBlockExpression> _function_7 = (XBlockExpression it_1) -> {
            final Consumer<ParticipationClass> _function_8 = (ParticipationClass resourceClass) -> {
              final XFeatureCall resourceBridge = typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(resourceClass));
              EList<XExpression> _expressions = it_1.getExpressions();
              XAssignment _setIsPersistenceEnabled = this._resourceBridgeHelper.setIsPersistenceEnabled(typeProvider, resourceBridge, true);
              _expressions.add(_setIsPersistenceEnabled);
            };
            resourceClasses.forEach(_function_8);
          };
          return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_7);
        };
        _xifexpression = it.execute(_function_6);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private void insertCommonalityParticipationClasses(@Extension final FluentRoutineBuilder.UpdateStatementBuilder it, final Participation participation, final FluentReactionsSegmentBuilder segment) {
    XtendAssertHelper.assertTrue(CommonalitiesLanguageModelExtensions.isCommonalityParticipation(participation));
    Set<ParticipationClass> _rootContainerClasses = CommonalitiesLanguageModelExtensions.getRootContainerClasses(participation);
    for (final ParticipationClass participationClass : _rootContainerClasses) {
      {
        final Commonality participatingCommonality = CommonalitiesLanguageModelExtensions.getParticipatingCommonality(participationClass);
        XtendAssertHelper.assertTrue((participatingCommonality != null));
        FluentRoutineBuilder _insertIntermediateRoutine = this._provider_1.getInsertIntermediateRoutine(segment, CommonalitiesLanguageModelExtensions.getConcept(participatingCommonality));
        String _correspondingVariableName = ReactionsGeneratorConventions.correspondingVariableName(participationClass);
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_correspondingVariableName);
        it.call(_insertIntermediateRoutine, _routineCallParameter);
      }
    }
  }

  private NamedMetaclassReference createParticipationObject(@Extension final FluentRoutineBuilder.CreateStatementBuilder createBuilder, final ParticipationClass participationClass) {
    NamedMetaclassReference _xblockexpression = null;
    {
      final String corresponding = ReactionsGeneratorConventions.correspondingVariableName(participationClass);
      _xblockexpression = createBuilder.vall(corresponding).create(this._generationContext.getChangeClass(participationClass));
    }
    return _xblockexpression;
  }

  private XExpression initializeParticipationObject(@Extension final FluentRoutineBuilder.UpdateStatementBuilder updateBuilder, final ParticipationClass participationClass) {
    XExpression _xblockexpression = null;
    {
      final Iterable<Function1<? super TypeProvider, ? extends XExpression>> initializers = this._participationObjectInitializationHelper.getInitializers(participationClass);
      XExpression _xifexpression = null;
      boolean _isEmpty = IterableExtensions.isEmpty(initializers);
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function<TypeProvider, XExpression> _function = (TypeProvider typeProvider) -> {
          return this._participationObjectInitializationHelper.toBlockExpression(initializers, typeProvider);
        };
        _xifexpression = updateBuilder.execute(_function);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private void addIntermediateCorrespondence(@Extension final FluentRoutineBuilder.UpdateStatementBuilder updateBuilder, final ParticipationClass participationClass) {
    updateBuilder.addCorrespondenceBetween(ReactionsGeneratorConventions.INTERMEDIATE).and(ReactionsGeneratorConventions.correspondingVariableName(participationClass)).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
  }

  private void setupResourceBridgeCorrespondences(@Extension final FluentRoutineBuilder.UpdateStatementBuilder updateBuilder, final Iterable<ParticipationContext.ContextContainment<?>> containments) {
    final Function1<ParticipationContext.ContextContainment<?>, Boolean> _function = (ParticipationContext.ContextContainment<?> it) -> {
      return Boolean.valueOf(CommonalitiesLanguageModelExtensions.isForResource(it.getContainer().getParticipationClass()));
    };
    final Consumer<ParticipationContext.ContextContainment<?>> _function_1 = (ParticipationContext.ContextContainment<?> it) -> {
      XtendAssertHelper.assertTrue(((!it.getContainer().isExternal()) && (!it.getContained().isExternal())));
      final ParticipationClass resourceClass = it.getContainer().getParticipationClass();
      final ParticipationClass containedClass = it.getContained().getParticipationClass();
      this.addResourceBridgeCorrespondence(updateBuilder, resourceClass, containedClass);
    };
    IterableExtensions.<ParticipationContext.ContextContainment<?>>filter(containments, _function).forEach(_function_1);
  }

  private void addResourceBridgeCorrespondence(@Extension final FluentRoutineBuilder.UpdateStatementBuilder updateBuilder, final ParticipationClass resourceClass, final ParticipationClass containedClass) {
    final String resourceBridge = ReactionsGeneratorConventions.correspondingVariableName(resourceClass);
    final String containedObject = ReactionsGeneratorConventions.correspondingVariableName(containedClass);
    updateBuilder.addCorrespondenceBetween(resourceBridge).and(containedObject).taggedWith(ReactionsGeneratorConventions.getResourceCorrespondenceTag(resourceClass, containedClass));
  }

  private XBlockExpression setupContainments(@Extension final TypeProvider typeProvider, final Iterable<ParticipationContext.ContextContainment<?>> containments) {
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      final Consumer<ParticipationContext.ContextContainment<?>> _function_1 = (ParticipationContext.ContextContainment<?> contextContainment) -> {
        final XFeatureCall containerVar = typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(contextContainment.getContainer()));
        final XFeatureCall containedVar = typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(contextContainment.getContained()));
        final Containment containment = contextContainment.getContainment();
        if ((containment instanceof ReferenceContainment)) {
          final EReference containmentReference = this._containmentHelper.getEReference(((ReferenceContainment)containment));
          boolean _isMany = containmentReference.isMany();
          if (_isMany) {
            EList<XExpression> _expressions = it.getExpressions();
            XBinaryOperation _addListFeatureValue = EmfAccessExpressions.addListFeatureValue(typeProvider, containerVar, containmentReference, containedVar);
            _expressions.add(_addListFeatureValue);
          } else {
            EList<XExpression> _expressions_1 = it.getExpressions();
            XAbstractFeatureCall _setFeatureValue = EmfAccessExpressions.setFeatureValue(typeProvider, containerVar, containmentReference, containedVar);
            _expressions_1.add(_setFeatureValue);
          }
        } else {
          if ((containment instanceof OperatorContainment)) {
            final OperatorReferenceMapping operatorMapping = ((OperatorContainment)containment).getMapping();
            final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext = new ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext(typeProvider);
            EList<XExpression> _expressions_2 = it.getExpressions();
            XMemberFeatureCall _callInsert = this._referenceMappingOperatorHelper.callInsert(operatorMapping, containerVar, containedVar, operatorContext);
            _expressions_2.add(_callInsert);
          }
        }
      };
      containments.forEach(_function_1);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }

  private void executePostInitializers(@Extension final FluentRoutineBuilder.UpdateStatementBuilder updateBuilder, final ParticipationContext participationContext, final Iterable<ParticipationContext.ContextClass> contextClasses) {
    final Consumer<ParticipationContext.ContextClass> _function = (ParticipationContext.ContextClass contextClass) -> {
      final List<Function1<? super TypeProvider, ? extends XExpression>> postInitializers = this._participationObjectInitializationHelper.getPostInitializers(participationContext, contextClass);
      boolean _isEmpty = postInitializers.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        final Function<TypeProvider, XExpression> _function_1 = (TypeProvider typeProvider) -> {
          return this._participationObjectInitializationHelper.toBlockExpression(postInitializers, typeProvider);
        };
        updateBuilder.execute(_function_1);
      }
    };
    contextClasses.forEach(_function);
  }

  private FluentReactionBuilder reactionForCommonalityRemove(final ParticipationContext participationContext) {
    FluentReactionBuilder _xblockexpression = null;
    {
      final Participation participation = participationContext.getParticipation();
      final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
      StringConcatenation _builder = new StringConcatenation();
      String _name = CommonalitiesLanguageModelExtensions.getConcept(commonality).getName();
      _builder.append(_name);
      _builder.append("_");
      String _name_1 = commonality.getName();
      _builder.append(_name_1);
      _builder.append("_deleted");
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder.append(_reactionNameSuffix);
      final Consumer<FluentRoutineBuilder.RoutineStartBuilder> _function = (FluentRoutineBuilder.RoutineStartBuilder it) -> {
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_1 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          final Consumer<ParticipationContext.ContextClass> _function_2 = (ParticipationContext.ContextClass contextClass) -> {
            boolean _isExternal = contextClass.isExternal();
            boolean _not = (!_isExternal);
            XtendAssertHelper.assertTrue(_not);
            final ParticipationClass participationClass = contextClass.getParticipationClass();
            it_1.vall(ReactionsGeneratorConventions.correspondingVariableName(participationClass)).retrieve(this._generationContext.getChangeClass(participationClass)).correspondingTo().affectedEObject().taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
          };
          participationContext.getManagedClasses().forEach(_function_2);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Consumer<ParticipationContext.ContextClass> _function_3 = (ParticipationContext.ContextClass contextClass) -> {
            final ParticipationClass participationClass = contextClass.getParticipationClass();
            it_1.delete(ReactionsGeneratorConventions.correspondingVariableName(participationClass));
            it_1.removeCorrespondenceBetween(ReactionsGeneratorConventions.correspondingVariableName(participationClass)).and().affectedEObject();
          };
          participationContext.getManagedClasses().forEach(_function_3);
        };
        it.match(_function_1).update(_function_2);
      };
      _xblockexpression = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterElement(this._generationContext.getChangeClass(commonality)).deleted().call(_function);
    }
    return _xblockexpression;
  }

  /**
   * Matches sub-participations for an existing parent participation.
   * <p>
   * Optional: Empty if there are no sub-participations to match.
   */
  private Optional<FluentRoutineBuilder> getMatchSubParticipationsRoutine(final ParticipationContext participationContext, final FluentReactionsSegmentBuilder segment) {
    final Participation participation = participationContext.getParticipation();
    final Function<Participation, Optional<FluentRoutineBuilder>> _function = (Participation it) -> {
      @Extension
      final ParticipationMatchingReactionsBuilder matchingReactionsBuilder = this.participationMatchingReactionsBuilderProvider.getFor(segment);
      final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
      final Function1<CommonalityReference, Iterable<ParticipationContext>> _function_1 = (CommonalityReference it_1) -> {
        return ParticipationContextHelper.getReferenceParticipationContexts(it_1);
      };
      final Function1<ParticipationContext, Boolean> _function_2 = (ParticipationContext it_1) -> {
        String _domainName = it_1.getParticipation().getDomainName();
        String _domainName_1 = participation.getDomainName();
        return Boolean.valueOf(Objects.equal(_domainName, _domainName_1));
      };
      final Function1<ParticipationContext, FluentRoutineBuilder> _function_3 = (ParticipationContext it_1) -> {
        return matchingReactionsBuilder.getMatchSubParticipationsRoutine(it_1);
      };
      final List<FluentRoutineBuilder> matchingRoutines = IterableExtensions.<FluentRoutineBuilder>toList(IterableExtensions.<ParticipationContext, FluentRoutineBuilder>map(IterableExtensions.<ParticipationContext>filter(IterableExtensions.<CommonalityReference, ParticipationContext>flatMap(commonality.getReferences(), _function_1), _function_2), _function_3));
      boolean _isEmpty = matchingRoutines.isEmpty();
      if (_isEmpty) {
        return Optional.<FluentRoutineBuilder>empty();
      }
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("matchSubParticipations_");
      _builder.append(participation);
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
      _builder.append(_reactionNameSuffix);
      final Consumer<FluentRoutineBuilder.InputBuilder> _function_4 = (FluentRoutineBuilder.InputBuilder it_1) -> {
        it_1.model(this._generationContext.getChangeClass(commonality), ReactionsGeneratorConventions.INTERMEDIATE);
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_5 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Consumer<FluentRoutineBuilder> _function_6 = (FluentRoutineBuilder matchingRoutine) -> {
          FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
          it_1.call(matchingRoutine, _routineCallParameter);
        };
        matchingRoutines.forEach(_function_6);
      };
      return Optional.<FluentRoutineBuilder>of(
        this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_4).update(_function_5));
    };
    return this.matchSubParticipationsRoutines.computeIfAbsent(participation, _function);
  }

  /**
   * Optional: Empty if there is no reaction to be created.
   */
  private Optional<FluentReactionBuilder> reactionForCommonalityCreate(final ParticipationContext participationContext, final FluentReactionsSegmentBuilder segment) {
    boolean _isForAttributeReferenceMapping = participationContext.isForAttributeReferenceMapping();
    boolean _not = (!_isForAttributeReferenceMapping);
    if (_not) {
      return Optional.<FluentReactionBuilder>empty();
    }
    final Commonality referencedCommonality = participationContext.getReferencedCommonality();
    StringConcatenation _builder = new StringConcatenation();
    String _reactionName = ReactionsGeneratorConventions.getReactionName(referencedCommonality);
    _builder.append(_reactionName);
    _builder.append("Create");
    String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
    _builder.append(_reactionNameSuffix);
    FluentReactionBuilder.PreconditionOrRoutineCallBuilder _created = this._reactionsGenerationContext.getCreate().reaction(_builder.toString()).afterElement(this._generationContext.getChangeClass(referencedCommonality)).created();
    FluentRoutineBuilder _matchAttributeReferenceContainerForIntermediateRoutine = this._provider_3.getMatchAttributeReferenceContainerForIntermediateRoutine(segment, participationContext);
    final Function<TypeProvider, XExpression> _function = (TypeProvider it) -> {
      return it.affectedEObject();
    };
    FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function);
    return Optional.<FluentReactionBuilder>of(
      _created.call(_matchAttributeReferenceContainerForIntermediateRoutine, _routineCallParameter));
  }
}
