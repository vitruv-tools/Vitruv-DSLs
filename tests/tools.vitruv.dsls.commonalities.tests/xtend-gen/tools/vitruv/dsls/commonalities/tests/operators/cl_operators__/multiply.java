package tools.vitruv.dsls.commonalities.tests.operators.cl_operators__;

import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeType;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.IAttributeMappingOperator;
import tools.vitruv.dsls.commonalities.tests.operators.MultiplyAttributeOperator;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@AttributeMappingOperator(name = "multiply", commonalityAttributeType = @AttributeType(multiValued = false, type = int.class), participationAttributeType = @AttributeType(multiValued = false, type = int.class))
@SuppressWarnings("all")
public class multiply implements IAttributeMappingOperator<Integer, Integer> {
  private final MultiplyAttributeOperator delegate;

  public multiply(final ReactionExecutionState executionState, final int multiplier) {
    this.delegate = new MultiplyAttributeOperator(executionState, multiplier);
  }

  public Integer applyTowardsCommonality(final Integer arg0) {
    return  this.delegate.applyTowardsCommonality(arg0);
  }

  public Integer applyTowardsParticipation(final Integer arg0) {
    return  this.delegate.applyTowardsParticipation(arg0);
  }
}
