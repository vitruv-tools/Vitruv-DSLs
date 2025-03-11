package tools.vitruv.dsls.commonalities.generator.reactions.attribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.AttributeMappingOperatorHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationObjects;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ApplyParticipationAttributesRoutineBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<ApplyParticipationAttributesRoutineBuilder> {
    @Override
    protected ApplyParticipationAttributesRoutineBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<ApplyParticipationAttributesRoutineBuilder>injectMembers(new ApplyParticipationAttributesRoutineBuilder(segment));
    }

    public FluentRoutineBuilder getApplyParticipationAttributesRoutine(final FluentReactionsSegmentBuilder segment, final Participation participation) {
      return this.getFor(segment).getApplyAttributesRoutine(participation);
    }
  }

  @Inject
  @Extension
  private AttributeMappingHelper attributeMappingHelper;

  @Inject
  @Extension
  private ParticipationObjectsHelper participationObjectsHelper;

  private final Map<Participation, FluentRoutineBuilder> applyParticipationAttributesRoutines = new HashMap<Participation, FluentRoutineBuilder>();

  private ApplyParticipationAttributesRoutineBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
  }

  ApplyParticipationAttributesRoutineBuilder() {
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public FluentRoutineBuilder getApplyAttributesRoutine(final Participation participation) {
    Preconditions.<Participation>checkNotNull(participation, "participation is null");
    final Function<Participation, FluentRoutineBuilder> _function = (Participation it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("applyParticipationAttributes_");
        String _reactionName = ReactionsGeneratorConventions.getReactionName(participation);
        _builder.append(_reactionName);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(this._generationContext.getChangeClass(commonality), ReactionsGeneratorConventions.INTERMEDIATE);
          it_1.plain(ParticipationObjects.class, ReactionsGeneratorConventions.PARTICIPATION_OBJECTS);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_2 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_3 = (TypeProvider typeProvider) -> {
            XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
            final Procedure1<XBlockExpression> _function_4 = (XBlockExpression it_2) -> {
              final Map<ParticipationClass, XVariableDeclaration> participationObjectVars = this.participationObjectsHelper.getParticipationObjectVars(participation, 
                typeProvider.variable(ReactionsGeneratorConventions.PARTICIPATION_OBJECTS), typeProvider);
              EList<XExpression> _expressions = it_2.getExpressions();
              Collection<XVariableDeclaration> _values = participationObjectVars.values();
              Iterables.<XExpression>addAll(_expressions, _values);
              final Function<ParticipationClass, XExpression> participationClassToObject = this.attributeMappingHelper.participationClassToNullableObject(participationObjectVars);
              final Supplier<XExpression> _function_5 = () -> {
                return typeProvider.variable(ReactionsGeneratorConventions.INTERMEDIATE);
              };
              final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext = new AttributeMappingOperatorHelper.AttributeMappingOperatorContext(typeProvider, _function_5, participationClassToObject);
              final Consumer<CommonalityAttributeMapping> _function_6 = (CommonalityAttributeMapping mapping) -> {
                EList<XExpression> _expressions_1 = it_2.getExpressions();
                XExpression _applyReadMapping = this.attributeMappingHelper.applyReadMapping(mapping, operatorContext);
                _expressions_1.add(_applyReadMapping);
              };
              this.attributeMappingHelper.getRelevantReadMappings(participation).forEach(_function_6);
            };
            return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function_4);
          };
          it_1.execute(_function_3);
        };
        _xblockexpression = this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).update(_function_2);
      }
      return _xblockexpression;
    };
    return this.applyParticipationAttributesRoutines.computeIfAbsent(participation, _function);
  }
}
