package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition

interface IParticipationConditionOperator {
	def void enforce()

	def boolean check()
}
