package tools.vitruv.extensions.dslsruntime.reactions

import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.correspondence.CorrespondenceModel
import tools.vitruv.change.propagation.ChangePropagationObservable
import org.eclipse.xtend.lib.annotations.Data
import tools.vitruv.change.propagation.ResourceAccess

@Data
class ReactionExecutionState {
	val UserInteractor userInteractor
	val CorrespondenceModel correspondenceModel
	val ResourceAccess resourceAccess 
	val ChangePropagationObservable changePropagationObservable
}