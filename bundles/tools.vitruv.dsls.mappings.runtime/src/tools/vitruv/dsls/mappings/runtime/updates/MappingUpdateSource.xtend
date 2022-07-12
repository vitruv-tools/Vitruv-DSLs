package tools.vitruv.dsls.mappings.runtime.updates

interface MappingUpdateSource extends AbstractMappingUpdateParameter {
	def MappingUpdateTransformation transfromation()
}
