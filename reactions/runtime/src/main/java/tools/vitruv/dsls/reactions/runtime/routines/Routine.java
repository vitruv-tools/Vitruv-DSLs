package tools.vitruv.dsls.reactions.runtime.routines;

import tools.vitruv.dsls.reactions.runtime.reactions.Reaction;

/**
 * {@link Routine}s are called by a {@link Reaction} to actually maintain consistency of a V-SUM.
 * Their execution involves retrieving correspondences, modifying the underlying models,
 * and updating correspondences.
 */
public interface Routine {
  /**
   * Executes the routine.
   *
   * @return whether the execution succeeded, or not.
   */
  boolean execute();
}
