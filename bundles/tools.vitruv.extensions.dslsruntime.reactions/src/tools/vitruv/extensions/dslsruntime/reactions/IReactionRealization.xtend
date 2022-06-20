package tools.vitruv.extensions.dslsruntime.reactions

import tools.vitruv.change.atomic.EChange

interface IReactionRealization {
	def void applyEvent(EChange change, ReactionExecutionState executionState);
}
