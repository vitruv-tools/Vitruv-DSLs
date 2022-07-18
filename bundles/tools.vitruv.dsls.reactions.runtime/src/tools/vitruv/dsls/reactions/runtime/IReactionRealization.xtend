package tools.vitruv.dsls.reactions.runtime

import tools.vitruv.change.atomic.EChange

interface IReactionRealization {
	def void applyEvent(EChange change, ReactionExecutionState executionState);
}
