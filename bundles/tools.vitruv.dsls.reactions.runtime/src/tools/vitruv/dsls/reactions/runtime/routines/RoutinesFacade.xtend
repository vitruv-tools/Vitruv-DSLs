package tools.vitruv.dsls.reactions.runtime.routines

import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

/**
 * Note: All methods start with an underscore here to not conflict with the methods that are generated from the routines by
 * concrete implementations of a facade
 */
interface RoutinesFacade {
	/**
	 * Sets the execution state for the current execution run.
	 */
	def void _setExecutionState(ReactionExecutionState executionState)

	/**
	 * Returns the execution state of the current execution run.
	 */
	def ReactionExecutionState _getExecutionState()

	/**
	 * Pushes the given caller to the call stack.
	 */
	def void _pushCaller(CallHierarchyHaving caller)
	
	/**
	 * Returns the current routine caller
	 */
	def CallHierarchyHaving _getCurrentCaller()

	/**
	 * Drops the last caller from the call stack.
	 */
	def void _dropLastCaller()
}
