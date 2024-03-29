package tools.vitruv.dsls.commonalities.tests.operators

import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AbstractAttributeMappingOperator
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeMappingOperator
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.AttributeType
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

import static tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper.*

/**
 * Multiplies a primitive integer number on the participation side with a
 * specified constant and divides the primitive integer number on the
 * commonality side by the same constant. The result is rounded towards zero.
 */
@AttributeMappingOperator(
	name = 'multiply',
	commonalityAttributeType = @AttributeType(multiValued = false, type = int),
	participationAttributeType = @AttributeType(multiValued = false, type = int)
)
class MultiplyAttributeOperator extends AbstractAttributeMappingOperator<Integer, Integer> {
	int multiplier

	new(ReactionExecutionState executionState, int multiplier) {
		super(executionState)
		this.multiplier = multiplier
	}

	override applyTowardsCommonality(Integer participationAttributeValue) {
		assertTrue(participationAttributeValue !== null)
		return participationAttributeValue * multiplier
	}

	override applyTowardsParticipation(Integer commonalityAttributeValue) {
		assertTrue(commonalityAttributeValue !== null)
		return commonalityAttributeValue / multiplier
	}
}
