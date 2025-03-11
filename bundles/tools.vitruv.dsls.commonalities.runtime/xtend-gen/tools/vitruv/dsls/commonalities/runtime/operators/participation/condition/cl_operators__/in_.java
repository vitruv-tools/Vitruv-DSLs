package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.cl_operators__;

import java.util.List;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.ContainmentOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.IParticipationClassConditionOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.IParticipationConditionOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.ParticipationConditionOperator;

@ParticipationConditionOperator(name = "in")
@SuppressWarnings("all")
public class in_ implements IParticipationClassConditionOperator, IParticipationConditionOperator {
  private final ContainmentOperator delegate;

  public in_(final Object leftOperand, final List<Object> rightOperands) {
    this.delegate = new ContainmentOperator(leftOperand, rightOperands);
  }

  public void enforce() {
     this.delegate.enforce();
  }

  public boolean check() {
    return  this.delegate.check();
  }
}
