package tools.vitruv.dsls.reactions.runtime.routines;

import java.util.Stack;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.Loggable;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

/**
 * Note: All methods start with an underscore here to not conflict with the methods that are generated from the routines by
 * concrete implementations.
 */
@SuppressWarnings("all")
public abstract class AbstractRoutinesFacade extends Loggable implements RoutinesFacade {
  private final RoutinesFacadesProvider routinesFacadesProvider;

  private final ReactionsImportPath reactionsImportPath;

  private ReactionExecutionState executionState;

  private final Stack<CallHierarchyHaving> callerStack = new Stack<CallHierarchyHaving>();

  public AbstractRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    this.routinesFacadesProvider = routinesFacadesProvider;
    this.reactionsImportPath = reactionsImportPath;
  }

  protected RoutinesFacadesProvider _getRoutinesFacadesProvider() {
    return this.routinesFacadesProvider;
  }

  protected ReactionsImportPath _getReactionsImportPath() {
    return this.reactionsImportPath;
  }

  @Override
  public void _setExecutionState(final ReactionExecutionState executionState) {
    this.executionState = executionState;
  }

  @Override
  public ReactionExecutionState _getExecutionState() {
    return this.executionState;
  }

  @Override
  public void _pushCaller(final CallHierarchyHaving caller) {
    this.callerStack.push(caller);
  }

  @Override
  public CallHierarchyHaving _getCurrentCaller() {
    CallHierarchyHaving _xifexpression = null;
    boolean _empty = this.callerStack.empty();
    boolean _not = (!_empty);
    if (_not) {
      _xifexpression = this.callerStack.peek();
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  @Override
  public void _dropLastCaller() {
    this.callerStack.pop();
  }
}
