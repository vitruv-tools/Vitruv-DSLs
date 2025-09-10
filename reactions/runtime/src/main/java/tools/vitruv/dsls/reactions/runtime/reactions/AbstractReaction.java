package tools.vitruv.dsls.reactions.runtime.reactions;

import java.util.function.Function;
import lombok.val;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

/**
 * An {@link AbstractReaction} retrieves a routines facade upon each execution and applies the
 * current execution's state to that facade such that it gets
 * propagated through all routines and their used facades.
 */
public abstract class AbstractReaction extends CallHierarchyHaving implements Reaction {
  private final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator;

  /**
   * Assigns routinesFacadeGenerator to retrieve routine facades from.
   *
   * @param routinesFacadeGenerator - {@link Function}
   */
  protected AbstractReaction(
      Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    this.routinesFacadeGenerator = routinesFacadeGenerator;
  }

  @Override
  public void execute(EChange<EObject> change, ReactionExecutionState reactionExecutionState) {
    val routinesFacade = routinesFacadeGenerator.apply(reactionExecutionState);
    routinesFacade.pushCaller(this);
    try {
      executeReaction(change, reactionExecutionState, routinesFacade);
    } finally {
      routinesFacade.dropLastCaller();
    }
  }

  /**
   * Acutally executes this reaction with the appropriate routines, as represented by routineFacade.
   *
   * @param change - {@link EChange}
   * @param executionState - {@link ReactionExecutionState}
   * @param routinesFacade - {@link RoutinesFacade}
   */
  protected abstract void executeReaction(EChange<EObject> change,
      ReactionExecutionState executionState, RoutinesFacade routinesFacade);

}
