package tools.vitruv.dsls.reactions.runtime.reactions

import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.change.atomic.EChange
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade
import org.eclipse.xtend.lib.annotations.Accessors

abstract class AbstractReaction extends CallHierarchyHaving implements Reaction {
	val RoutinesFacade routinesFacade
	@Accessors(PROTECTED_GETTER)
	ReactionExecutionState executionState

	new(RoutinesFacade routinesFacade) {
		this.routinesFacade = routinesFacade
	}
	
	// generic return type for convenience; the requested type has to match the type of the facade provided during construction
	protected def <T extends RoutinesFacade> T getRoutinesFacade() {
		return routinesFacade as T
	}

	override execute(EChange change, ReactionExecutionState reactionExecutionState) {
		this.executionState = reactionExecutionState
		routinesFacade._setExecutionState(executionState)
		routinesFacade._pushCaller(this)
		try {
			executeReaction(change)
		} finally {
			routinesFacade._dropLastCaller()
		}
	}

	protected def void executeReaction(EChange change)

}
