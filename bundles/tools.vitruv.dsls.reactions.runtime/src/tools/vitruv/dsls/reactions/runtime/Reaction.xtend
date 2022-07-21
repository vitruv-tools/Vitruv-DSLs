package tools.vitruv.dsls.reactions.runtime

import tools.vitruv.change.atomic.EChange

interface Reaction {
	def void execute(EChange change, ReactionExecutionState executionState)
}
