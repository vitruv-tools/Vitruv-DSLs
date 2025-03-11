package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.cl_operators__;

import java.util.List;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.EqualsOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.IParticipationConditionOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.ParticipationConditionOperator;

@ParticipationConditionOperator(name = "=")
@SuppressWarnings("all")
public class equals_ implements IParticipationConditionOperator {
  private final EqualsOperator delegate;

  public equals_(final Object leftOperand, final List<?> rightOperands) {
    this.delegate = new EqualsOperator(leftOperand, rightOperands);
  }

  public void enforce() {
     this.delegate.enforce();
  }

  public boolean check() {
    return  this.delegate.check();
  }
}
