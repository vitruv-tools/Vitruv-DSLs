package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition;

import com.google.common.base.Preconditions;
import java.util.List;

@SuppressWarnings("all")
public abstract class AbstractNoArgumentConditionOperator extends AbstractParticipationConditionOperator {
  public AbstractNoArgumentConditionOperator(final Object leftOperand, final List<?> rightOperands) {
    super(leftOperand, rightOperands);
    Preconditions.checkArgument(rightOperands.isEmpty(), "This operator does not expect any right operand(s)!");
  }
}
