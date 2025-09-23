package tools.vitruv.dsls.reactions.runtime.routines;

import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

/**
 * Provides the routines facades for all reactions segments of one specific import hierarchy.
 */
public interface RoutinesFacadesProvider {

  /**
   * Gets the routines facade for the specified absolute reactions import path.
   *
   * @param <T> - the type of the requested routines facade
   * @param reactionsImportPath -
   *     The absolute import path (starting with the root of the import hierarchy).
   * 
   * @return T - the routines facade.
   * @throws IllegalArgumentException 
   *      if the specified import path is not valid, e.g. does not exist in the import hierarchy.
   * @throws ClassCastException 
   *      if the specified routines facade type is not applicable to the actually returned
   *      routines facade.
   */
  <T extends AbstractRoutinesFacade> T getRoutinesFacade(ReactionsImportPath reactionsImportPath);
}
