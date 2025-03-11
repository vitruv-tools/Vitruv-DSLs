package tools.vitruv.dsls.commonalities.tests.operators.cl_operators__;

import java.util.List;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeType;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.IAttributeMappingOperator;
import tools.vitruv.dsls.commonalities.tests.operators.DigitsAttributeOperator;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@AttributeMappingOperator(name = "digits", commonalityAttributeType = @AttributeType(multiValued = true, type = Integer.class), participationAttributeType = @AttributeType(multiValued = false, type = Integer.class))
@SuppressWarnings("all")
public class digits implements IAttributeMappingOperator<List<Integer>, Integer> {
  private final DigitsAttributeOperator delegate;

  public digits(final ReactionExecutionState executionState) {
    this.delegate = new DigitsAttributeOperator(executionState);
  }

  public List<Integer> applyTowardsCommonality(final Integer arg0) {
    return  this.delegate.applyTowardsCommonality(arg0);
  }

  public Integer applyTowardsParticipation(final List<Integer> arg0) {
    return  this.delegate.applyTowardsParticipation(arg0);
  }
}
