package tools.vitruv.dsls.mappings.runtime.updates

interface AbstractMappingUpdateParameter {
	def Object currentValue()

	def void updateValue(Object value)
}
