package tools.vitruv.dsls.commonalities.generator.reactions.attribute;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.AttributeMappingOperatorHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectsRetrievalHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ApplyCommonalityAttributesRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<ApplyCommonalityAttributesRoutineBuilder> {
    @Override
    protected ApplyCommonalityAttributesRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<ApplyCommonalityAttributesRoutineBuilder>injectMembers(new ApplyCommonalityAttributesRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getApplyAttributesRoutine(final FluentReactionsSegmentBuilder segment, final Participation participation) {
      return this.getFor(segment).getApplyAttributesRoutine(participation);
    }
  }

  @Inject
  @Extension
  private AttributeMappingHelper attributeMappingHelper;

  @Inject
  @Extension
  private ParticipationObjectsRetrievalHelper participationObjectsRetrievalHelper;

  private final Map<Participation, FluentRoutineBuilder> routines = new HashMap<Participation, FluentRoutineBuilder>();

  private ApplyCommonalityAttributesRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
  }

  ApplyCommonalityAttributesRoutineBuilder() {
    throw new IllegalStateException("Use the Factory to create instances of this class!");
  }

  public FluentRoutineBuilder getApplyAttributesRoutine(final Participation participation) {
    final Function<Participation, FluentRoutineBuilder> _function = (Participation it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("applyCommonalityAttributes_");
        String _reactionName = ReactionsGeneratorConventions.getReactionName(participation);
        _builder.append(_reactionName);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(this._generationContext.getChangeClass(commonality), ReactionsGeneratorConventions.INTERMEDIATE);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          final Consumer<ParticipationClass> _function_3 = (ParticipationClass participationClass) -> {
            final Function<TypeProvider, XExpression> _function_4 = (TypeProvider it_2) -> {
              return it_2.variable(ReactionsGeneratorConventions.INTERMEDIATE);
            };
            this.participationObjectsRetrievalHelper.retrieveAssertedParticipationObject(it_1, participationClass, _function_4);
          };
          CommonalitiesLanguageModelExtensions.getAllClasses(participation).forEach(_function_3);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
            XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
            final Procedure1<XBlockExpression> _function_5 = (XBlockExpression it_2) -> {
              final Function<ParticipationClass, XExpression> participationClassToObject = this.attributeMappingHelper.participationClassToOptionalObject(typeProvider);
              final Supplier<XExpression> _function_6 = () -> {
                return typeProvider.variable(ReactionsGeneratorConventions.INTERMEDIATE);
              };
              final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext = new AttributeMappingOperatorHelper.AttributeMappingOperatorContext(typeProvider, _function_6, participationClassToObject);
              final Consumer<CommonalityAttributeMapping> _function_7 = (CommonalityAttributeMapping mapping) -> {
                EList<XExpression> _expressions = it_2.getExpressions();
                XExpression _applyWriteMapping = this.attributeMappingHelper.applyWriteMapping(mapping, operatorContext);
                _expressions.add(_applyWriteMapping);
              };
              this.attributeMappingHelper.getRelevantWriteMappings(participation).forEach(_function_7);
            };
            return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_5);
          };
          it_1.execute(_function_4);
        };
        _xblockexpression = this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3);
      }
      return _xblockexpression;
    };
    return this.routines.computeIfAbsent(participation, _function);
  }
}
