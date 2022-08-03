package tools.vitruv.dsls.commonalities.tests.operators

import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator
import org.eclipse.emf.ecore.EObject
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.ReferenceMappingOperator
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

@ReferenceMappingOperator(
	name = "mock",
	isMultiValued = true,
	isAttributeReference = true
)
class MockReferenceMappingOperator implements IReferenceMappingOperator {
	new(ReactionExecutionState executionState) {}
	
	override getContainedObjects(EObject container) {
		throw new UnsupportedOperationException("This is a mock")
	}
	
	override getContainer(EObject object) {
		throw new UnsupportedOperationException("This is a mock")
	}
	
	override isContained(EObject container, EObject contained) {
		throw new UnsupportedOperationException("This is a mock")
	}
	
	override insert(EObject container, EObject object) {
		throw new UnsupportedOperationException("This is a mock")
	}
	
}