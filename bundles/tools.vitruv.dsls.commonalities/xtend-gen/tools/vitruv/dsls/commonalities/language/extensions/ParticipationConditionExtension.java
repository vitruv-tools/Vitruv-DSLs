package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Arrays;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.BidirectionalParticipationCondition;
import tools.vitruv.dsls.commonalities.language.CheckedParticipationCondition;
import tools.vitruv.dsls.commonalities.language.EnforcedParticipationCondition;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.participation.Containment;
import tools.vitruv.dsls.commonalities.participation.ReferenceContainment;
import tools.vitruv.dsls.commonalities.runtime.operators.CommonalitiesOperatorConventions;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.ContainmentOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.ParticipationConditionOperator;

@Utility
@SuppressWarnings("all")
final class ParticipationConditionExtension {
  private static final String containmentOperatorTypeQualifiedName = CommonalitiesOperatorConventions.toOperatorTypeQualifiedName(
    ContainmentOperator.class.getPackageName(), 
    IterableExtensions.<ParticipationConditionOperator>head(Iterables.<ParticipationConditionOperator>filter(((Iterable<?>)Conversions.doWrapArray(ContainmentOperator.class.getAnnotations())), ParticipationConditionOperator.class)).name());

  public static String getParticipationConditionOperatorName(final JvmDeclaredType operatorType) {
    return CommonalitiesOperatorConventions.toOperatorLanguageName(operatorType.getSimpleName());
  }

  public static String getName(final ParticipationCondition condition) {
    return ParticipationConditionExtension.getParticipationConditionOperatorName(condition.getOperator());
  }

  public static boolean isContainment(final ParticipationCondition condition) {
    String _qualifiedName = condition.getOperator().getQualifiedName();
    return Objects.equal(_qualifiedName, ParticipationConditionExtension.containmentOperatorTypeQualifiedName);
  }

  public static Participation getParticipation(final ParticipationCondition participationCondition) {
    return CommonalitiesLanguageElementExtension.<Participation>getDirectEContainer(participationCondition, Participation.class);
  }

  protected static boolean _isEnforced(final BidirectionalParticipationCondition condition) {
    return true;
  }

  protected static boolean _isEnforced(final EnforcedParticipationCondition condition) {
    return true;
  }

  protected static boolean _isEnforced(final CheckedParticipationCondition condition) {
    return false;
  }

  protected static boolean _isChecked(final BidirectionalParticipationCondition condition) {
    return true;
  }

  protected static boolean _isChecked(final EnforcedParticipationCondition condition) {
    return false;
  }

  protected static boolean _isChecked(final CheckedParticipationCondition condition) {
    return true;
  }

  public static Containment getContainment(final ParticipationCondition condition) {
    boolean _isContainment = ParticipationConditionExtension.isContainment(condition);
    boolean _not = (!_isContainment);
    if (_not) {
      return null;
    }
    final ParticipationConditionOperand containerOperand = IterableExtensions.<ParticipationConditionOperand>head(condition.getRightOperands());
    ParticipationClass _participationClass = null;
    if (containerOperand!=null) {
      _participationClass=OperandExtension.getParticipationClass(containerOperand);
    }
    final ParticipationClass container = _participationClass;
    if ((container == null)) {
      return null;
    }
    final ParticipationClass contained = OperandExtension.getParticipationClass(condition.getLeftOperand());
    ParticipationAttribute _participationAttribute = OperandExtension.getParticipationAttribute(containerOperand);
    return new ReferenceContainment(container, contained, _participationAttribute);
  }

  public static boolean isEnforced(final ParticipationCondition condition) {
    if (condition instanceof BidirectionalParticipationCondition) {
      return _isEnforced((BidirectionalParticipationCondition)condition);
    } else if (condition instanceof CheckedParticipationCondition) {
      return _isEnforced((CheckedParticipationCondition)condition);
    } else if (condition instanceof EnforcedParticipationCondition) {
      return _isEnforced((EnforcedParticipationCondition)condition);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(condition).toString());
    }
  }

  public static boolean isChecked(final ParticipationCondition condition) {
    if (condition instanceof BidirectionalParticipationCondition) {
      return _isChecked((BidirectionalParticipationCondition)condition);
    } else if (condition instanceof CheckedParticipationCondition) {
      return _isChecked((CheckedParticipationCondition)condition);
    } else if (condition instanceof EnforcedParticipationCondition) {
      return _isChecked((EnforcedParticipationCondition)condition);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(condition).toString());
    }
  }

  private ParticipationConditionExtension() {
    
  }
}
