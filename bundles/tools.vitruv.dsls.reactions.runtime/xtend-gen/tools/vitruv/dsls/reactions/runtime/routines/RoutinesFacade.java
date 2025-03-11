package tools.vitruv.dsls.reactions.runtime.routines;

import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

/**
 * Note: All methods start with an underscore here to not conflict with the methods that are generated from the routines by
 * concrete implementations of a facade
 */
@SuppressWarnings("all")
public interface RoutinesFacade {
  /**
   * Sets the execution state for the current execution run.
   */
  void _setExecutionState(final ReactionExecutionState executionState);

  /**
   * Returns the execution state of the current execution run.
   */
  ReactionExecutionState _getExecutionState();

  /**
   * Pushes the given caller to the call stack.
   */
  void _pushCaller(final CallHierarchyHaving caller);

  /**
   * Returns the current routine caller
   */
  CallHierarchyHaving _getCurrentCaller();

  /**
   * Drops the last caller from the call stack.
   */
  void _dropLastCaller();
}
