package tools.vitruv.dsls.reactions.runtime.state

import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.propagation.ChangePropagationObservable
import org.eclipse.xtend.lib.annotations.Data
import tools.vitruv.change.utils.ResourceAccess
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence

@Data
class ReactionExecutionState {
	val UserInteractor userInteractor
	val EditableCorrespondenceModelView<ReactionsCorrespondence> correspondenceModel
	val ResourceAccess resourceAccess 
	val ChangePropagationObservable changePropagationObservable
}