package tools.vitruv.dsls.reactions.runtime

import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import tools.vitruv.change.atomic.EChange
import tools.vitruv.change.interaction.UserInteractor

abstract class AbstractReaction extends CallHierarchyHaving implements Reaction {
	val AbstractRoutinesFacade routinesFacade;
	protected UserInteractor userInteractor;
	protected ReactionExecutionState executionState;
	
	new(AbstractRoutinesFacade routinesFacade) {
		this.routinesFacade = routinesFacade;
	}
	
	// generic return type for convenience; the requested type has to match the type of the facade provided during construction
	protected def <T extends AbstractRoutinesFacade> T getRoutinesFacade() {
		return routinesFacade as T;
	}
	
	override execute(EChange change, ReactionExecutionState reactionExecutionState) {
		this.executionState = reactionExecutionState;
		this.userInteractor = reactionExecutionState.userInteractor;

		// set the reaction execution state and caller to use for all following routine calls:
		// note: reactions are executed one after the other, therefore we don't need to capture/restore the facade's previous execution state here,
		// resetting it after execution is sufficient
		routinesFacade._getExecutionState().setExecutionState(executionState, this);

		try {	
			executeReaction(change);
		} finally {
			// reset the routines facade execution state:
			routinesFacade._getExecutionState().reset();
		}
	}
	
	protected def void executeReaction(EChange change);
	
}