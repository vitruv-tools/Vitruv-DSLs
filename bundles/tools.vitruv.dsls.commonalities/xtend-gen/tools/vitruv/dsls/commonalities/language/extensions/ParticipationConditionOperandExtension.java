package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;

@Utility
@SuppressWarnings("all")
final class ParticipationConditionOperandExtension {
  public static boolean isInParticipationConditionContext(final ParticipationConditionOperand operand) {
    ParticipationCondition _participationCondition = ParticipationConditionOperandExtension.getParticipationCondition(operand);
    return (_participationCondition != null);
  }

  public static ParticipationCondition getParticipationCondition(final ParticipationConditionOperand operand) {
    return CommonalitiesLanguageElementExtension.<ParticipationCondition>getOptionalDirectEContainer(operand, ParticipationCondition.class);
  }

  public static Participation getParticipation(final ParticipationConditionOperand operand) {
    return CommonalitiesLanguageElementExtension.<Participation>getOptionalEContainer(operand, Participation.class);
  }

  private ParticipationConditionOperandExtension() {
    
  }
}
