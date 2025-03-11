package tools.vitruv.dsls.reactions.runtime.routines;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

/**
 * A RoutinesFacadesProvider which caches created routines facades.
 * <p>
 * Implementations are required to override {@link #createRoutinesFacade(ReactionsImportPath)} to create the routines facades of
 * the handled import hierarchy there.
 */
@SuppressWarnings("all")
public abstract class AbstractRoutinesFacadesProvider implements RoutinesFacadesProvider {
  private final Map<ReactionsImportPath, AbstractRoutinesFacade> routinesFacades = new HashMap<ReactionsImportPath, AbstractRoutinesFacade>();

  private final ReactionExecutionState executionState;

  public AbstractRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    this.executionState = executionState;
  }

  protected abstract AbstractRoutinesFacade createRoutinesFacade(final ReactionsImportPath reactionsImportPath);

  @Override
  public <T extends AbstractRoutinesFacade> T getRoutinesFacade(final ReactionsImportPath reactionsImportPath) {
    Preconditions.<ReactionsImportPath>checkNotNull(reactionsImportPath, "reactionsImportPath is null");
    AbstractRoutinesFacade _get = this.routinesFacades.get(reactionsImportPath);
    T routinesFacade = ((T) _get);
    if ((routinesFacade != null)) {
      return routinesFacade;
    }
    AbstractRoutinesFacade _createRoutinesFacade = this.createRoutinesFacade(reactionsImportPath);
    routinesFacade = ((T) _createRoutinesFacade);
    if ((routinesFacade != null)) {
      routinesFacade._setExecutionState(this.executionState);
      this.routinesFacades.put(reactionsImportPath, routinesFacade);
      return routinesFacade;
    }
    return routinesFacade;
  }
}
