package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference

import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

import static com.google.common.base.Preconditions.*

/**
 * Note: Attribute operands are not passed to the constructor of the operator.
 */
abstract class AbstractReferenceMappingOperator implements IReferenceMappingOperator {

	protected val extension ReactionExecutionState executionState

	new(ReactionExecutionState executionState) {
		checkNotNull(executionState, "executionState is null")
		this.executionState = executionState
	}
}
