package tools.vitruv.dsls.commonalities.runtime.resources.impl

// TODO remove once resources are handled by domains
class ResourceOverwrittenFactory extends ResourcesFactoryImpl {

	override createIntermediateResourceBridge() {
		new IntermediateResourceBridgeI
	}
}
