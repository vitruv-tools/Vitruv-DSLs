package tools.vitruv.dsls.reactions.runtime.state;

import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.interaction.UserInteractor;
import tools.vitruv.change.propagation.ChangePropagationObservable;
import tools.vitruv.change.utils.ResourceAccess;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;
import tools.vitruv.dsls.reactions.runtime.reactions.Reaction;

/**
 * When executed, a {@link Reaction} uses a {@link ReactionExecutionState} to look at the V-SUM.
 * It provides:
 * 
 * <ul>
 *  <li>an {@link UserInteractor} for getting user input, when required,</li>
 *  <li>an {@link EditableCorrespondenceModelView} to retrieve and update correspondences,</li>
 *  <li>a {@link ResourceAccess} object to load and save underlying models, and</li>
 *  <li>a {@link ChangePropagationObservable} to notify observers.</li>
 * </ul>
 */
public record ReactionExecutionState(
    UserInteractor userInteractor,
    EditableCorrespondenceModelView<ReactionsCorrespondence> getCorrespondenceModel,
    ResourceAccess getResourceAccess,
    ChangePropagationObservable changePropagationObservable
) {

}