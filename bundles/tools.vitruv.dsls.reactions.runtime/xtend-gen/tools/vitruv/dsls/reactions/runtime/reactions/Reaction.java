package tools.vitruv.dsls.reactions.runtime.reactions;

import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public interface Reaction {
  void execute(final EChange<EObject> change, final ReactionExecutionState executionState);
}
