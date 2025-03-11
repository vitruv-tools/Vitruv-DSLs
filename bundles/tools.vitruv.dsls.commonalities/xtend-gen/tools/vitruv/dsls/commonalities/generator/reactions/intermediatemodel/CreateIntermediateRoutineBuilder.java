package tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.ApplyParticipationAttributesRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.InsertIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.InsertReferencedIntermediateRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.resource.InsertResourceBridgeRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.resource.SetupResourceBridgeRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.Containment;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationObjects;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class CreateIntermediateRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<CreateIntermediateRoutineBuilder> {
    @Override
    protected CreateIntermediateRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<CreateIntermediateRoutineBuilder>injectMembers(new CreateIntermediateRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getCreateIntermediateRoutine(final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext) {
      return this.getFor(segment).getCreateIntermediateRoutine(participationContext);
    }
  }

  @Inject
  @Extension
  private ParticipationObjectsHelper participationObjectsHelper;

  @Inject
  @Extension
  private SetupResourceBridgeRoutineBuilder.Provider setupResourceBridgeRoutineBuilderProvider;

  @Inject
  @Extension
  private InsertResourceBridgeRoutineBuilder.Provider insertResourceBridgeRoutineBuilderProvider;

  @Inject
  @Extension
  private InsertIntermediateRoutineBuilder.Provider insertIntermediateRoutineBuilderProvider;

  @Inject
  @Extension
  private InsertReferencedIntermediateRoutineBuilder.Provider insertReferencedIntermediateRoutineBuilderProvider;

  @Inject
  @Extension
  private ApplyParticipationAttributesRoutineBuilder.Provider applyParticipationAttributesRoutineBuilderProvider;

  private final FluentReactionsSegmentBuilder segment;

  private final Map<ParticipationContext, FluentRoutineBuilder> createIntermediateRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private CreateIntermediateRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
    this.segment = segment;
  }

  CreateIntermediateRoutineBuilder() {
    this.segment = null;
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public FluentRoutineBuilder getCreateIntermediateRoutine(final ParticipationContext participationContext) {
    final Participation participation = participationContext.getParticipation();
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("createIntermediate_");
      String _name = commonality.getName();
      _builder.append(_name);
      String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(it);
      _builder.append(_reactionNameSuffix);
      final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
        it_1.plain(ParticipationObjects.class, ReactionsGeneratorConventions.PARTICIPATION_OBJECTS);
      };
      final Consumer<FluentRoutineBuilder.CreateStatementBuilder> _function_2 = (FluentRoutineBuilder.CreateStatementBuilder it_1) -> {
        it_1.vall(ReactionsGeneratorConventions.INTERMEDIATE).create(this._generationContext.getChangeClass(commonality));
      };
      final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
        final Function<TypeProvider, XExpression> _function_4 = (TypeProvider it_2) -> {
          return IntermediateModelHelper.claimIntermediateId(it_2, it_2.variable(ReactionsGeneratorConventions.INTERMEDIATE));
        };
        it_1.execute(_function_4);
        boolean _isRootContext = participationContext.isRootContext();
        if (_isRootContext) {
          boolean _hasSingletonClass = CommonalitiesLanguageModelExtensions.hasSingletonClass(participation);
          if (_hasSingletonClass) {
            final ParticipationClass singletonClass = CommonalitiesLanguageModelExtensions.getSingletonClass(participation);
            FluentRoutineBuilder _setupSingletonRoutine = this.getSetupSingletonRoutine(singletonClass);
            FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
            final Function<TypeProvider, XExpression> _function_5 = (TypeProvider typeProvider) -> {
              return this.participationObjectsHelper.getParticipationObject(singletonClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
            };
            FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_function_5);
            FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_2 = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS);
            it_1.call(_setupSingletonRoutine, _routineCallParameter, _routineCallParameter_1, _routineCallParameter_2);
          } else {
            boolean _hasResourceClass = CommonalitiesLanguageModelExtensions.hasResourceClass(participation);
            if (_hasResourceClass) {
              this.setupAndInsertResourceBridge(it_1, participation);
            }
          }
        }
        final Consumer<ParticipationContext.ContextClass> _function_6 = (ParticipationContext.ContextClass contextClass) -> {
          boolean _isExternal = contextClass.isExternal();
          boolean _not = (!_isExternal);
          XtendAssertHelper.assertTrue(_not);
          final ParticipationClass participationClass = contextClass.getParticipationClass();
          final Function<TypeProvider, XExpression> _function_7 = (TypeProvider typeProvider) -> {
            return this.participationObjectsHelper.getParticipationObject(contextClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
          };
          it_1.addCorrespondenceBetween(ReactionsGeneratorConventions.INTERMEDIATE).and(_function_7).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(participationClass));
        };
        participationContext.getManagedClasses().forEach(_function_6);
        boolean _isForReferenceMapping = participationContext.isForReferenceMapping();
        if (_isForReferenceMapping) {
          final CommonalityReference reference = participationContext.getDeclaringReference();
          final ParticipationContext.ContextClass referenceRootClass = IterableExtensions.<ParticipationContext.ContextClass>head(participationContext.getReferenceRootClasses());
          FluentRoutineBuilder _insertReferencedIntermediateRoutine = this.insertReferencedIntermediateRoutineBuilderProvider.getInsertReferencedIntermediateRoutine(this.segment, reference);
          FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_3 = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
          final Function<TypeProvider, XExpression> _function_7 = (TypeProvider typeProvider) -> {
            return this.participationObjectsHelper.getParticipationObject(referenceRootClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
          };
          FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_4 = new FluentRoutineBuilder.RoutineCallParameter(_function_7);
          it_1.call(_insertReferencedIntermediateRoutine, _routineCallParameter_3, _routineCallParameter_4);
        } else {
          FluentRoutineBuilder _insertIntermediateRoutine = this.insertIntermediateRoutineBuilderProvider.getInsertIntermediateRoutine(this.segment, CommonalitiesLanguageModelExtensions.getConcept(commonality));
          FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_5 = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
          it_1.call(_insertIntermediateRoutine, _routineCallParameter_5);
        }
        FluentRoutineBuilder _applyParticipationAttributesRoutine = this.applyParticipationAttributesRoutineBuilderProvider.getApplyParticipationAttributesRoutine(this.segment, participation);
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_6 = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_7 = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS);
        it_1.call(_applyParticipationAttributesRoutine, _routineCallParameter_6, _routineCallParameter_7);
      };
      return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).create(_function_2).update(_function_3);
    };
    return this.createIntermediateRoutines.computeIfAbsent(participationContext, _function);
  }

  /**
   * Sets up a matched singleton root if that has not happened yet.
   */
  private FluentRoutineBuilder getSetupSingletonRoutine(final ParticipationClass singletonClass) {
    final Participation participation = CommonalitiesLanguageModelExtensions.getParticipation(singletonClass);
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
    final EClass singletonEClass = this._generationContext.getChangeClass(singletonClass);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("setupSingleton_");
    String _name = participation.getName();
    _builder.append(_name);
    _builder.append("_");
    String _name_1 = singletonClass.getName();
    _builder.append(_name_1);
    final Consumer<FluentRoutineBuilder.InputBuilder> _function = (FluentRoutineBuilder.InputBuilder it) -> {
      it.model(this._generationContext.getChangeClass(commonality), ReactionsGeneratorConventions.INTERMEDIATE);
      it.model(singletonEClass, ReactionsGeneratorConventions.SINGLETON);
      it.plain(ParticipationObjects.class, ReactionsGeneratorConventions.PARTICIPATION_OBJECTS);
    };
    final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_1 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it) -> {
      final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it_1) -> {
        return EmfAccessExpressions.getEClass(it_1, singletonEClass);
      };
      it.requireAbsenceOf(singletonEClass).correspondingTo(_function_2);
    };
    final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it) -> {
      final Function<TypeProvider, XExpression> _function_3 = (TypeProvider it_1) -> {
        return EmfAccessExpressions.getEClass(it_1, singletonEClass);
      };
      it.addCorrespondenceBetween(ReactionsGeneratorConventions.SINGLETON).and(_function_3);
      this.setupAndInsertResourceBridge(it, participation);
      final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
        XExpression _xblockexpression = null;
        {
          final ParticipationClass resourceClass = CommonalitiesLanguageModelExtensions.getResourceClass(participation);
          _xblockexpression = this.participationObjectsHelper.getParticipationObject(resourceClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
        }
        return _xblockexpression;
      };
      it.addCorrespondenceBetween(_function_4).and(ReactionsGeneratorConventions.INTERMEDIATE);
    };
    return this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function).match(_function_1).update(_function_2);
  }

  /**
   * Performs remaining setup and then inserts the ResourceBridge returned by
   * the ParticipationMatcher into the intermediate model.
   */
  private void setupAndInsertResourceBridge(@Extension final FluentRoutineBuilder.UpdateStatementBuilder it, final Participation participation) {
    final ParticipationClass resourceClass = CommonalitiesLanguageModelExtensions.getResourceClass(participation);
    XtendAssertHelper.assertTrue((resourceClass != null));
    FluentRoutineBuilder _setupResourceBridgeRoutine = this.setupResourceBridgeRoutineBuilderProvider.getSetupResourceBridgeRoutine(this.segment, resourceClass);
    final Function<TypeProvider, XExpression> _function = (TypeProvider typeProvider) -> {
      return this.participationObjectsHelper.getParticipationObject(resourceClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
    };
    FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function);
    it.call(_setupResourceBridgeRoutine, _routineCallParameter);
    final Function1<Containment, ParticipationClass> _function_1 = (Containment it_1) -> {
      return it_1.getContained();
    };
    final Consumer<ParticipationClass> _function_2 = (ParticipationClass containedClass) -> {
      final Function<TypeProvider, XExpression> _function_3 = (TypeProvider typeProvider) -> {
        return this.participationObjectsHelper.getParticipationObject(resourceClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
      };
      final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
        return this.participationObjectsHelper.getParticipationObject(containedClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
      };
      it.addCorrespondenceBetween(_function_3).and(_function_4).taggedWith(ReactionsGeneratorConventions.getResourceCorrespondenceTag(resourceClass, containedClass));
    };
    IterableExtensions.<Containment, ParticipationClass>map(CommonalitiesLanguageModelExtensions.getResourceContainments(participation), _function_1).forEach(_function_2);
    FluentRoutineBuilder _insertResourceBridgeRoutine = this.insertResourceBridgeRoutineBuilderProvider.getInsertResourceBridgeRoutine(this.segment, resourceClass);
    final Function<TypeProvider, XExpression> _function_3 = (TypeProvider typeProvider) -> {
      return this.participationObjectsHelper.getParticipationObject(resourceClass, typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
    };
    FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_function_3);
    FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_2 = new FluentRoutineBuilder.RoutineCallParameter(ReactionsGeneratorConventions.INTERMEDIATE);
    it.call(_insertResourceBridgeRoutine, _routineCallParameter_1, _routineCallParameter_2);
  }
}
