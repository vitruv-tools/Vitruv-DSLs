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

		// set the reaction execution state and caller to use for all following routine calls:
		// note: reactions are executed one after the other, therefore we don't need to capture/restore the facade's previous execution state here,
		// resetting it after execution is sufficient
		routinesFacade._setReactionExecutionState(executionState, this)

		try {
			executeReaction(change)
		} finally {
			// reset the routines facade execution state:
			routinesFacade._resetExecutionState()
		}
	}

	protected def void executeReaction(EChange change)

}
