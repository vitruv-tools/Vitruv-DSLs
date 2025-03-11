package tools.vitruv.dsls.reactions.runtime.reactions;

import java.util.function.Function;
import org.eclipse.emf.ecore.EObject;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

/**
 * A Reaction retrieves a routines facade upon each execution and applies the
 * current execution's state to that facade such that it gets
 * propagated through all routines and their used facades.
 */
@SuppressWarnings("all")
public abstract class AbstractReaction extends CallHierarchyHaving implements Reaction {
  private final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator;

  public AbstractReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    this.routinesFacadeGenerator = routinesFacadeGenerator;
  }

  @Override
  public void execute(final EChange<EObject> change, final ReactionExecutionState reactionExecutionState) {
    final RoutinesFacade routinesFacade = this.routinesFacadeGenerator.apply(reactionExecutionState);
    routinesFacade._pushCaller(this);
    try {
      this.executeReaction(change, reactionExecutionState, routinesFacade);
    } finally {
      routinesFacade._dropLastCaller();
    }
  }

  protected abstract void executeReaction(final EChange<EObject> change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacade);
}
