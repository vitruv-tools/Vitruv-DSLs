package tools.vitruv.dsls.commonalities.participation;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Collections;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;

@Utility
@SuppressWarnings("all")
public final class ParticipationConditionHelper {
  public static Iterable<ParticipationCondition> getCheckedParticipationConditions(final ParticipationContext participationContext) {
    final Participation participation = participationContext.getParticipation();
    final Function1<ParticipationCondition, Boolean> _function = (ParticipationCondition it) -> {
      return Boolean.valueOf(CommonalitiesLanguageModelExtensions.isChecked(it));
    };
    final Function1<ParticipationCondition, Boolean> _function_1 = (ParticipationCondition it) -> {
      boolean _isContainment = CommonalitiesLanguageModelExtensions.isContainment(it);
      return Boolean.valueOf((!_isContainment));
    };
    final Function1<ParticipationCondition, Boolean> _function_2 = (ParticipationCondition it) -> {
      boolean _xblockexpression = false;
      {
        Set<ParticipationConditionOperand> _singleton = Collections.<ParticipationConditionOperand>singleton(it.getLeftOperand());
        EList<ParticipationConditionOperand> _rightOperands = it.getRightOperands();
        final Iterable<ParticipationConditionOperand> operands = Iterables.<ParticipationConditionOperand>concat(_singleton, _rightOperands);
        final Function1<ParticipationConditionOperand, ParticipationClass> _function_3 = (ParticipationConditionOperand it_1) -> {
          return CommonalitiesLanguageModelExtensions.getParticipationClass(it_1);
        };
        final Function1<ParticipationClass, Boolean> _function_4 = (ParticipationClass operandParticipationClass) -> {
          final Function1<ParticipationContext.ContextClass, Boolean> _function_5 = (ParticipationContext.ContextClass it_1) -> {
            ParticipationClass _participationClass = it_1.getParticipationClass();
            return Boolean.valueOf(Objects.equal(_participationClass, operandParticipationClass));
          };
          return Boolean.valueOf(IterableExtensions.<ParticipationContext.ContextClass>exists(participationContext.getClasses(), _function_5));
        };
        _xblockexpression = IterableExtensions.<ParticipationClass>forall(IterableExtensions.<ParticipationClass>filterNull(IterableExtensions.<ParticipationConditionOperand, ParticipationClass>map(operands, _function_3)), _function_4);
      }
      return Boolean.valueOf(_xblockexpression);
    };
    return IterableExtensions.<ParticipationCondition>filter(IterableExtensions.<ParticipationCondition>filter(IterableExtensions.<ParticipationCondition>filter(participation.getConditions(), _function), _function_1), _function_2);
  }

  public static Iterable<ParticipationCondition> getEnforcedParticipationConditions(final ParticipationContext participationContext, final ParticipationContext.ContextClass involvedContextClass) {
    final Participation participation = participationContext.getParticipation();
    final ParticipationClass involvedParticipationClass = involvedContextClass.getParticipationClass();
    final Function1<ParticipationCondition, Boolean> _function = (ParticipationCondition it) -> {
      boolean _isContainment = CommonalitiesLanguageModelExtensions.isContainment(it);
      return Boolean.valueOf((!_isContainment));
    };
    final Function1<ParticipationCondition, Boolean> _function_1 = (ParticipationCondition it) -> {
      return Boolean.valueOf(CommonalitiesLanguageModelExtensions.isEnforced(it));
    };
    final Function1<ParticipationCondition, Boolean> _function_2 = (ParticipationCondition it) -> {
      ParticipationClass _participationClass = CommonalitiesLanguageModelExtensions.getParticipationClass(it.getLeftOperand());
      return Boolean.valueOf(Objects.equal(_participationClass, involvedParticipationClass));
    };
    final Function1<ParticipationCondition, Boolean> _function_3 = (ParticipationCondition it) -> {
      final Function1<ParticipationConditionOperand, ParticipationClass> _function_4 = (ParticipationConditionOperand it_1) -> {
        return CommonalitiesLanguageModelExtensions.getParticipationClass(it_1);
      };
      final Function1<ParticipationClass, Boolean> _function_5 = (ParticipationClass operandParticipationClass) -> {
        final Function1<ParticipationContext.ContextClass, Boolean> _function_6 = (ParticipationContext.ContextClass it_1) -> {
          ParticipationClass _participationClass = it_1.getParticipationClass();
          return Boolean.valueOf(Objects.equal(_participationClass, operandParticipationClass));
        };
        return Boolean.valueOf(IterableExtensions.<ParticipationContext.ContextClass>exists(participationContext.getClasses(), _function_6));
      };
      return Boolean.valueOf(IterableExtensions.<ParticipationClass>forall(IterableExtensions.<ParticipationClass>filterNull(ListExtensions.<ParticipationConditionOperand, ParticipationClass>map(it.getRightOperands(), _function_4)), _function_5));
    };
    return IterableExtensions.<ParticipationCondition>filter(IterableExtensions.<ParticipationCondition>filter(IterableExtensions.<ParticipationCondition>filter(IterableExtensions.<ParticipationCondition>filter(participation.getConditions(), _function), _function_1), _function_2), _function_3);
  }

  private ParticipationConditionHelper() {
    
  }
}
