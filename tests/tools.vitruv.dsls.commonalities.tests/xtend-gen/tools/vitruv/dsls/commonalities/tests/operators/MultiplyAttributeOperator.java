package tools.vitruv.dsls.commonalities.tests.operators;

import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AbstractAttributeMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeType;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Multiplies a primitive integer number on the participation side with a
 * specified constant and divides the primitive integer number on the
 * commonality side by the same constant. The result is rounded towards zero.
 */
@AttributeMappingOperator(name = "multiply", commonalityAttributeType = @AttributeType(multiValued = false, type = int.class), participationAttributeType = @AttributeType(multiValued = false, type = int.class))
@SuppressWarnings("all")
public class MultiplyAttributeOperator extends AbstractAttributeMappingOperator<Integer, Integer> {
  private int multiplier;

  public MultiplyAttributeOperator(final ReactionExecutionState executionState, final int multiplier) {
    super(executionState);
    this.multiplier = multiplier;
  }

  @Override
  public Integer applyTowardsCommonality(final Integer participationAttributeValue) {
    XtendAssertHelper.assertTrue((participationAttributeValue != null));
    return Integer.valueOf(((participationAttributeValue).intValue() * this.multiplier));
  }

  @Override
  public Integer applyTowardsParticipation(final Integer commonalityAttributeValue) {
    XtendAssertHelper.assertTrue((commonalityAttributeValue != null));
    return Integer.valueOf(((commonalityAttributeValue).intValue() / this.multiplier));
  }
}
