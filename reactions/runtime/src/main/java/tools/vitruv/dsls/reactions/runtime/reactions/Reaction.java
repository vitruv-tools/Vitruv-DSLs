package tools.vitruv.dsls.reactions.runtime.reactions;

import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * A {@link Reaction} can be executed in response to an {@link EChange} to 
 * maintain the consistency of a V-SUM.
 */
public interface Reaction {
  /**
   * Executes the reaction on the V-SUM, as represented by execution state.
   *
   * @param change - {@link EChange} The change which triggered the reaction.
   * @param executionState - {@link ReactionExecutionState}
   */
  void execute(EChange<EObject> change, ReactionExecutionState executionState);
}
