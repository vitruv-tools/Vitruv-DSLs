package tools.vitruv.dsls.mappings.runtime.updates

class SimpleMappingUpdateTransformation implements MappingUpdateTransformation {
	// just return the same value
	override transformToInterchangeableValue() {
		[it]
	}

	override transformToTargetValue() {
		[it]
	}

}
