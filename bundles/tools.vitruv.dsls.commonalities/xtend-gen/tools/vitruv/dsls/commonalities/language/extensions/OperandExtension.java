package tools.vitruv.dsls.commonalities.language.extensions;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Arrays;
import tools.vitruv.dsls.commonalities.language.Operand;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationClassOperand;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;

@Utility
@SuppressWarnings("all")
final class OperandExtension {
  public static Participation getParticipation(final Operand operand) {
    ParticipationClass _participationClass = OperandExtension.getParticipationClass(operand);
    Participation _participation = null;
    if (_participationClass!=null) {
      _participation=ParticipationClassExtension.getParticipation(_participationClass);
    }
    return _participation;
  }

  protected static ParticipationClass _getParticipationClass(final ParticipationClassOperand operand) {
    return operand.getParticipationClass();
  }

  protected static ParticipationClass _getParticipationClass(final ParticipationAttributeOperand operand) {
    return operand.getParticipationAttribute().getParticipationClass();
  }

  protected static ParticipationClass _getParticipationClass(final ReferencedParticipationAttributeOperand operand) {
    return operand.getParticipationAttribute().getParticipationClass();
  }

  protected static ParticipationClass _getParticipationClass(final Operand operand) {
    return null;
  }

  protected static ParticipationAttribute _getParticipationAttribute(final ParticipationAttributeOperand operand) {
    return operand.getParticipationAttribute();
  }

  protected static ParticipationAttribute _getParticipationAttribute(final ReferencedParticipationAttributeOperand operand) {
    return operand.getParticipationAttribute();
  }

  protected static ParticipationAttribute _getParticipationAttribute(final Operand operand) {
    return null;
  }

  public static ParticipationClass getParticipationClass(final Operand operand) {
    if (operand instanceof ParticipationAttributeOperand) {
      return _getParticipationClass((ParticipationAttributeOperand)operand);
    } else if (operand instanceof ParticipationClassOperand) {
      return _getParticipationClass((ParticipationClassOperand)operand);
    } else if (operand instanceof ReferencedParticipationAttributeOperand) {
      return _getParticipationClass((ReferencedParticipationAttributeOperand)operand);
    } else if (operand != null) {
      return _getParticipationClass(operand);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(operand).toString());
    }
  }

  public static ParticipationAttribute getParticipationAttribute(final Operand operand) {
    if (operand instanceof ParticipationAttributeOperand) {
      return _getParticipationAttribute((ParticipationAttributeOperand)operand);
    } else if (operand instanceof ReferencedParticipationAttributeOperand) {
      return _getParticipationAttribute((ReferencedParticipationAttributeOperand)operand);
    } else if (operand != null) {
      return _getParticipationAttribute(operand);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(operand).toString());
    }
  }

  private OperandExtension() {
    
  }
}
