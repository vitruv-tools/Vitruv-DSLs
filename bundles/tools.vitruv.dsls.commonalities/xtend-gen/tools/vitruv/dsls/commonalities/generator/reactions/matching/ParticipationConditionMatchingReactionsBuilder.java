package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.XBooleanLiteral;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.attribute.AttributeChangeReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.condition.CheckedParticipationConditionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.DeleteObjectRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.matching.MatchParticipationRoutineBuilder;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsSegmentScopedProvider;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.ParticipationConditionHelper;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.runtime.BooleanResult;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;
import tools.vitruv.dsls.reactions.builder.FluentReactionsSegmentBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

/**
 * Generates the reactions for attribute changes which might affect the
 * participation's checked conditions.
 * <p>
 * If a change is detected and a corresponding intermediate already exists, we
 * check if the conditions are still fulfilled and otherwise delete the
 * intermediate. If no intermediate exists, we invoke the participation
 * matching.
 */
@SuppressWarnings("all")
public class ParticipationConditionMatchingReactionsBuilder extends ReactionsGenerationHelper {
  public static class Provider extends ReactionsSegmentScopedProvider<ParticipationConditionMatchingReactionsBuilder> {
    @Override
    protected ParticipationConditionMatchingReactionsBuilder createFor(final FluentReactionsSegmentBuilder segment) {
      return this.<ParticipationConditionMatchingReactionsBuilder>injectMembers(new ParticipationConditionMatchingReactionsBuilder(segment));
    }

    public void generateParticipationConditionReactions(final FluentReactionsSegmentBuilder segment, final ParticipationContext participationContext) {
      this.getFor(segment).generateReactions(participationContext);
    }
  }

  @Inject
  @Extension
  private AttributeChangeReactionsHelper attributeChangeReactionsHelper;

  @Inject
  @Extension
  private CheckedParticipationConditionsHelper checkedParticipationConditionsHelper;

  @Inject
  @Extension
  private DeleteObjectRoutineBuilder.Provider deleteObjectRoutineBuilderProvider;

  @Inject
  @Extension
  private MatchParticipationRoutineBuilder.Provider matchParticipationRoutineBuilderProvider;

  private final FluentReactionsSegmentBuilder segment;

  private final Map<ParticipationContext, FluentRoutineBuilder> validateParticipationRoutines = new HashMap<ParticipationContext, FluentRoutineBuilder>();

  private ParticipationConditionMatchingReactionsBuilder(final FluentReactionsSegmentBuilder segment) {
    Preconditions.<FluentReactionsSegmentBuilder>checkNotNull(segment, "segment is null");
    this.segment = segment;
  }

  ParticipationConditionMatchingReactionsBuilder() {
    this.segment = null;
    throw new IllegalStateException("Use the Provider to get instances of this class!");
  }

  public void generateReactions(final ParticipationContext participationContext) {
    FluentRoutineBuilder _validateParticipationRoutine = this.getValidateParticipationRoutine(participationContext);
    this.segment.operator_add(_validateParticipationRoutine);
    FluentRoutineBuilder _deleteObjectRoutine = this.deleteObjectRoutineBuilderProvider.getDeleteObjectRoutine(this.segment);
    this.segment.operator_add(_deleteObjectRoutine);
    FluentRoutineBuilder _matchParticipationRoutine = this.matchParticipationRoutineBuilderProvider.getMatchParticipationRoutine(this.segment, participationContext);
    this.segment.operator_add(_matchParticipationRoutine);
    final String contextReactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
    final Procedure2<ParticipationCondition, Integer> _function = (ParticipationCondition condition, Integer conditionIndex) -> {
      final ParticipationConditionOperand leftOperand = condition.getLeftOperand();
      if ((leftOperand instanceof ParticipationAttributeOperand)) {
        String _leftOperandReactionNameSuffix = ParticipationConditionMatchingReactionsBuilder.getLeftOperandReactionNameSuffix((conditionIndex).intValue());
        final String reactionNameSuffix = (_leftOperandReactionNameSuffix + contextReactionNameSuffix);
        final ParticipationAttribute attribute = ((ParticipationAttributeOperand)leftOperand).getParticipationAttribute();
        this.reactionsForAttributeChange(participationContext, attribute, reactionNameSuffix);
      }
      final Procedure2<ParticipationAttributeOperand, Integer> _function_1 = (ParticipationAttributeOperand operand, Integer operandIndex) -> {
        String _rightOperandReactionNameSuffix = ParticipationConditionMatchingReactionsBuilder.getRightOperandReactionNameSuffix((conditionIndex).intValue(), (operandIndex).intValue());
        final String reactionNameSuffix_1 = (_rightOperandReactionNameSuffix + contextReactionNameSuffix);
        final ParticipationAttribute attribute_1 = operand.getParticipationAttribute();
        this.reactionsForAttributeChange(participationContext, attribute_1, reactionNameSuffix_1);
      };
      IterableExtensions.<ParticipationAttributeOperand>forEach(Iterables.<ParticipationAttributeOperand>filter(condition.getRightOperands(), ParticipationAttributeOperand.class), _function_1);
    };
    IterableExtensions.<ParticipationCondition>forEach(ParticipationConditionHelper.getCheckedParticipationConditions(participationContext), _function);
  }

  private static String getLeftOperandReactionNameSuffix(final int conditionIndex) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("_forCondition_");
    _builder.append(conditionIndex);
    _builder.append("_leftOperand");
    return _builder.toString();
  }

  private static String getRightOperandReactionNameSuffix(final int conditionIndex, final int operandIndex) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("_forCondition_");
    _builder.append(conditionIndex);
    _builder.append("_rightOperand_");
    _builder.append(operandIndex);
    return _builder.toString();
  }

  private List<FluentReactionBuilder> reactionsForAttributeChange(final ParticipationContext participationContext, final ParticipationAttribute attribute, final String reactionNameSuffix) {
    final BiFunction<AttributeChangeReactionsHelper.AttributeChangeReactionType, FluentReactionBuilder.RoutineCallBuilder, FluentReactionBuilder> _function = (AttributeChangeReactionsHelper.AttributeChangeReactionType changeType, FluentReactionBuilder.RoutineCallBuilder it) -> {
      FluentReactionBuilder _xblockexpression = null;
      {
        it.call(this.getValidateParticipationRoutine(participationContext));
        FluentRoutineBuilder _matchParticipationRoutine = this.matchParticipationRoutineBuilderProvider.getMatchParticipationRoutine(this.segment, participationContext);
        final Function<TypeProvider, XExpression> _function_1 = (TypeProvider it_1) -> {
          return it_1.affectedEObject();
        };
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter = new FluentRoutineBuilder.RoutineCallParameter(_function_1);
        XBooleanLiteral _booleanLiteral = XbaseHelper.booleanLiteral(true);
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_1 = new FluentRoutineBuilder.RoutineCallParameter(_booleanLiteral);
        final Function<TypeProvider, XExpression> _function_2 = (TypeProvider it_1) -> {
          return XbaseHelper.noArgsConstructorCall(JvmTypeProviderHelper.findDeclaredType(it_1, BooleanResult.class));
        };
        FluentRoutineBuilder.RoutineCallParameter _routineCallParameter_2 = new FluentRoutineBuilder.RoutineCallParameter(_function_2);
        _xblockexpression = it.call(_matchParticipationRoutine, _routineCallParameter, _routineCallParameter_1, _routineCallParameter_2);
      }
      return _xblockexpression;
    };
    return this.attributeChangeReactionsHelper.getAttributeChangeReactions(attribute, reactionNameSuffix, _function);
  }

  /**
   * Checks if there is a corresponding intermediate object for the given
   * participation object and if the participation's checked conditions are
   * still fulfilled and otherwise deletes the intermediate.
   */
  private FluentRoutineBuilder getValidateParticipationRoutine(final ParticipationContext participationContext) {
    final Function<ParticipationContext, FluentRoutineBuilder> _function = (ParticipationContext it) -> {
      FluentRoutineBuilder _xblockexpression = null;
      {
        final Participation participation = participationContext.getParticipation();
        final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation);
        @Extension
        final ReactionsHelper.RoutineCallContext routineCallContext = new ReactionsHelper.RoutineCallContext();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("validateParticipation");
        String _reactionNameSuffix = ReactionsGeneratorConventions.getReactionNameSuffix(participationContext);
        _builder.append(_reactionNameSuffix);
        final Consumer<FluentRoutineBuilder.InputBuilder> _function_1 = (FluentRoutineBuilder.InputBuilder it_1) -> {
          it_1.model(EcorePackage.Literals.EOBJECT, ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
        };
        final Consumer<FluentRoutineBuilder.UndecidedMatchStatementBuilder> _function_2 = (FluentRoutineBuilder.UndecidedMatchStatementBuilder it_1) -> {
          it_1.vall(ReactionsGeneratorConventions.INTERMEDIATE).retrieve(this._generationContext.getChangeClass(commonality)).correspondingTo(ReactionsGeneratorConventions.PARTICIPATION_OBJECT);
          final Function1<ParticipationContext.ContextClass, Boolean> _function_3 = (ParticipationContext.ContextClass it_2) -> {
            boolean _isExternal = it_2.isExternal();
            return Boolean.valueOf((!_isExternal));
          };
          final Consumer<ParticipationContext.ContextClass> _function_4 = (ParticipationContext.ContextClass contextClass) -> {
            it_1.vall(ReactionsGeneratorConventions.correspondingVariableName(contextClass.getParticipationClass())).retrieveAsserted(this._generationContext.getChangeClass(contextClass.getParticipationClass())).correspondingTo(ReactionsGeneratorConventions.INTERMEDIATE).taggedWith(ReactionsGeneratorConventions.getCorrespondenceTag(contextClass.getParticipationClass()));
          };
          IterableExtensions.<ParticipationContext.ContextClass>filter(participationContext.getClasses(), _function_3).forEach(_function_4);
        };
        final Consumer<FluentRoutineBuilder.UpdateStatementBuilder> _function_3 = (FluentRoutineBuilder.UpdateStatementBuilder it_1) -> {
          final Function<TypeProvider, XExpression> _function_4 = (TypeProvider typeProvider) -> {
            XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
            final Procedure1<XIfExpression> _function_5 = (XIfExpression it_2) -> {
              it_2.setIf(XbaseHelper.negated(this.checkedParticipationConditionsHelper.checkParticipationConditions(participationContext, typeProvider), typeProvider));
              it_2.setThen(ReactionsHelper.createRoutineCall(routineCallContext, typeProvider, this.deleteObjectRoutineBuilderProvider.getDeleteObjectRoutine(this.segment), 
                typeProvider.variable(ReactionsGeneratorConventions.INTERMEDIATE)));
            };
            return ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function_5);
          };
          routineCallContext.setCallerContext(it_1.execute(_function_4));
        };
        _xblockexpression = routineCallContext.setCaller(this._reactionsGenerationContext.getCreate().routine(_builder.toString()).input(_function_1).match(_function_2).update(_function_3));
      }
      return _xblockexpression;
    };
    return this.validateParticipationRoutines.computeIfAbsent(participationContext, _function);
  }
}
