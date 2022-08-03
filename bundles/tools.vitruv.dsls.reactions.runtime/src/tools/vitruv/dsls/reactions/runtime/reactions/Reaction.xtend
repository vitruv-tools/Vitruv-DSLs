package tools.vitruv.dsls.reactions.runtime.reactions

import tools.vitruv.change.atomic.EChange
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

interface Reaction {
	def void execute(EChange change, ReactionExecutionState executionState)
}
