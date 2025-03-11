package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition;

import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.eclipse.xtend2.lib.StringConcatenation;
import tools.vitruv.dsls.commonalities.runtime.operators.AttributeOperand;

@ParticipationConditionOperator(name = "=")
@SuppressWarnings("all")
public class EqualsOperator extends AbstractSingleArgumentConditionOperator {
  private static final Logger logger = Logger.getLogger(EqualsOperator.class);

  public EqualsOperator(final Object leftOperand, final List<?> rightOperands) {
    super(leftOperand, rightOperands);
  }

  @Override
  public void enforce() {
    this.leftOperandObject.eSet(this.leftOperandFeature, this.getRightOperandValue());
  }

  @Override
  public boolean check() {
    final Object leftValue = this.leftOperandObject.eGet(this.leftOperandFeature);
    final Object rightValue = this.getRightOperandValue();
    final boolean result = Objects.equals(leftValue, this.getRightOperandValue());
    if ((!result)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Condition check failed. leftObject=\'");
      _builder.append(this.leftOperandObject);
      _builder.append("\', leftFeature=\'");
      _builder.append(this.leftOperandFeature);
      _builder.append("\', leftValue=\'");
      _builder.append(leftValue);
      _builder.append("\', rightValue=\'");
      _builder.append(rightValue);
      _builder.append("\'.");
      EqualsOperator.logger.debug(_builder);
    }
    return result;
  }

  private Object getRightOperandValue() {
    Object _rightOperand = this.getRightOperand();
    if ((_rightOperand instanceof AttributeOperand)) {
      Object _rightOperand_1 = this.getRightOperand();
      final AttributeOperand rightAttributeOperand = ((AttributeOperand) _rightOperand_1);
      return rightAttributeOperand.getObject().eGet(rightAttributeOperand.getFeature());
    } else {
      return this.getRightOperand();
    }
  }
}
