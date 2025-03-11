package tools.vitruv.dsls.commonalities.generator.reactions.condition;

import com.google.inject.Inject;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.participation.ParticipationConditionHelper;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ParticipationConditionInitializationHelper extends ReactionsGenerationHelper {
  @Inject
  @Extension
  private ParticipationConditionOperatorHelper participationConditionOperatorHelper;

  ParticipationConditionInitializationHelper() {
  }

  public Iterable<Function1<? super TypeProvider, ? extends XExpression>> getParticipationConditionsInitializers(final ParticipationContext participationContext, final ParticipationContext.ContextClass contextClass) {
    final Function1<ParticipationCondition, Function1<? super TypeProvider, ? extends XExpression>> _function = (ParticipationCondition it) -> {
      return this.getParticipationConditionInitializer(it);
    };
    return IterableExtensions.<ParticipationCondition, Function1<? super TypeProvider, ? extends XExpression>>map(ParticipationConditionHelper.getEnforcedParticipationConditions(participationContext, contextClass), _function);
  }

  private Function1<? super TypeProvider, ? extends XExpression> getParticipationConditionInitializer(final ParticipationCondition participationCondition) {
    final Function1<TypeProvider, XExpression> _function = (TypeProvider it) -> {
      ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext _participationConditionOperatorContext = new ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext(it);
      return this.participationConditionOperatorHelper.enforce(participationCondition, _participationConditionOperatorContext);
    };
    return _function;
  }
}
