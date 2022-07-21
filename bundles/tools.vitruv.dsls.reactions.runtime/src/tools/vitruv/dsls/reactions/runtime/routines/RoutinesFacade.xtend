package tools.vitruv.dsls.reactions.runtime.routines

import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.dsls.reactions.runtime.state.RoutinesFacadeExecutionState

/**
 * Note: All methods start with an underscore here to not conflict with the methods that are generated from the routines by
 * concrete implementations of a facade
 */
interface RoutinesFacade {
	def void _setReactionExecutionState(ReactionExecutionState reactionExecutionState, CallHierarchyHaving calledBy)

	def void _restoreExecutionState(RoutinesFacadeExecutionState executionStateToRestore)

	def void _resetExecutionState()
}
