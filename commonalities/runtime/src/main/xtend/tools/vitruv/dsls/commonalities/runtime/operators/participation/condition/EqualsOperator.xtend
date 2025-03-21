package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition

import java.util.List
import java.util.Objects
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager
import tools.vitruv.dsls.commonalities.runtime.operators.AttributeOperand

@ParticipationConditionOperator(name = '=')
class EqualsOperator extends AbstractSingleArgumentConditionOperator {

	static val Logger logger = LogManager.getLogger(EqualsOperator)

	new(Object leftOperand, List<?> rightOperands) {
		super(leftOperand, rightOperands)
	}

	override enforce() {
		leftOperandObject.eSet(leftOperandFeature, rightOperandValue)
	}

	override check() {
		val leftValue = leftOperandObject.eGet(leftOperandFeature)
		val rightValue = rightOperandValue
		val result = Objects.equals(leftValue, rightOperandValue)
		if (!result) {
			logger.debug('''Condition check failed. leftObject='«leftOperandObject»', leftFeature='«leftOperandFeature
				»', leftValue='«leftValue»', rightValue='«rightValue»'.''')
		}
		return result
	}

	private def Object getRightOperandValue() {
		if (rightOperand instanceof AttributeOperand) {
			val rightAttributeOperand = rightOperand as AttributeOperand
			return rightAttributeOperand.object.eGet(rightAttributeOperand.feature)
		} else {
			return rightOperand
		}
	}
}
