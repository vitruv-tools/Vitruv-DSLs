package tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute

import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

import static com.google.common.base.Preconditions.*

/**
 * Note: Only the operands that are common to both application directions of
 * this operator are passed to the constructor.
 */
abstract class AbstractAttributeMappingOperator<C, P> implements IAttributeMappingOperator<C, P> {

	protected val extension ReactionExecutionState executionState

	new(ReactionExecutionState executionState) {
		checkNotNull(executionState, "executionState is null")
		this.executionState = executionState
	}
}
