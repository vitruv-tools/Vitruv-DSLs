package tools.vitruv.dsls.reactions.runtime.reactions

import org.eclipse.emf.ecore.EObject
import tools.vitruv.change.atomic.EChange
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

interface Reaction {
	def void execute(EChange<EObject> change, ReactionExecutionState executionState)
}
