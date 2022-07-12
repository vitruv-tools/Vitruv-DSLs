package tools.vitruv.dsls.mappings.runtime.updates

import java.util.function.Function

interface MappingUpdateTransformation {
	def Function<Object, Object> transformToInterchangeableValue()

	def Function<Object, Object> transformToTargetValue()
}