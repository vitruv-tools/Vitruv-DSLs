package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public abstract class AbstractSingleArgumentConditionOperator extends AbstractParticipationConditionOperator {
  public AbstractSingleArgumentConditionOperator(final Object leftOperand, final List<?> rightOperands) {
    super(leftOperand, rightOperands);
    boolean _isEmpty = rightOperands.isEmpty();
    boolean _not = (!_isEmpty);
    Preconditions.checkArgument(_not, "Missing right operand!");
    int _size = rightOperands.size();
    boolean _equals = (_size == 1);
    Preconditions.checkArgument(_equals, "Too many right operands!");
  }

  protected Object getRightOperand() {
    return IterableExtensions.head(this.rightOperands);
  }
}
