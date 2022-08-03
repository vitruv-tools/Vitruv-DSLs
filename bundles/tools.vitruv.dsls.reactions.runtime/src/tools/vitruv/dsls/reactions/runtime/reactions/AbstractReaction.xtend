package tools.vitruv.dsls.reactions.runtime.reactions

import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.change.atomic.EChange
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade
import java.util.function.Function

/**
 * A Reaction retrieves a routines facade upon each execution and applies the
 * current execution's state to that facade such that it gets
 * propagated through all routines and their used facades.
 */
abstract class AbstractReaction extends CallHierarchyHaving implements Reaction {
	val Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator
	
	new(Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
		this.routinesFacadeGenerator = routinesFacadeGenerator
	}
	
	override execute(EChange change, ReactionExecutionState reactionExecutionState) {
		val routinesFacade = routinesFacadeGenerator.apply(reactionExecutionState)
		routinesFacade._pushCaller(this)
		try {
			executeReaction(change, reactionExecutionState, routinesFacade)
		} finally {
			routinesFacade._dropLastCaller()
		}
	}

	protected def void executeReaction(EChange change, ReactionExecutionState executionState, RoutinesFacade routinesFacade)

}
