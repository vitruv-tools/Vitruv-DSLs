package tools.vitruv.dsls.reactions.runtime.routines;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;


/**
 * A RoutinesFacadesProvider which caches created routines facades.
 *
 * <p>Implementations are required to override {@link #createRoutinesFacade(ReactionsImportPath)} 
 * to create the routines facades of the handled import hierarchy there.
 */
public abstract class AbstractRoutinesFacadesProvider implements RoutinesFacadesProvider {

  /**
   * Routine facades that were created so far.
   */
  private final Map<ReactionsImportPath, AbstractRoutinesFacade> routinesFacades = new HashMap<>();

  private final ReactionExecutionState executionState;
  
  /**
   * Creates a new {@link AbstractRoutinesFacadesProvider}.
   *
   * @param executionState - {@link ReactionExecutionState}
   */
  protected AbstractRoutinesFacadesProvider(ReactionExecutionState executionState) {
    this.executionState = executionState;
  }

  /**
   * Creates the specified routines facade for the given import path.
   *
   * @param reactionsImportPath - {@link ReactionsImportPath}
   * @return new {@link AbstractRoutinesFacade}
   */
  protected abstract AbstractRoutinesFacade createRoutinesFacade(
      ReactionsImportPath reactionsImportPath);

  @Override
  public <T extends AbstractRoutinesFacade> T getRoutinesFacade(
      ReactionsImportPath reactionsImportPath) {
    checkNotNull(reactionsImportPath, "reactionsImportPath is null");
    // check if we already created the requested routines facade:
    var routinesFacade = (T) routinesFacades.get(reactionsImportPath);
    if (routinesFacade != null) {
      return routinesFacade;
    }

    // create the routines facade:
    routinesFacade = (T) this.createRoutinesFacade(reactionsImportPath);
    if (routinesFacade != null) {
      routinesFacade.setExecutionState(executionState);
      // store created routines facade:
      routinesFacades.put(reactionsImportPath, routinesFacade);
      return routinesFacade;
    }
    return routinesFacade;
  }
}
