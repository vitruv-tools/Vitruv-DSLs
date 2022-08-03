package tools.vitruv.dsls.reactions.runtime.routines

import java.util.HashMap
import java.util.Map
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath

import static com.google.common.base.Preconditions.*
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState

/**
 * A RoutinesFacadesProvider which caches created routines facades.
 * <p>
 * Implementations are required to override {@link #createRoutinesFacade(ReactionsImportPath)} to create the routines facades of
 * the handled import hierarchy there.
 */
abstract class AbstractRoutinesFacadesProvider implements RoutinesFacadesProvider {

	// the routines facades that were created so far:
	val Map<ReactionsImportPath, AbstractRoutinesFacade> routinesFacades = new HashMap<ReactionsImportPath, AbstractRoutinesFacade>()

	val ReactionExecutionState executionState
	
	new(ReactionExecutionState executionState) {
		this.executionState = executionState
	}

	// creates the specified routines facade:
	protected def abstract AbstractRoutinesFacade createRoutinesFacade(ReactionsImportPath reactionsImportPath)

	override <T extends AbstractRoutinesFacade> T getRoutinesFacade(ReactionsImportPath reactionsImportPath) {
		checkNotNull(reactionsImportPath, "reactionsImportPath is null")
		// check if we already created the requested routines facade:
		var T routinesFacade = routinesFacades.get(reactionsImportPath) as T
		if(routinesFacade !== null) return routinesFacade
		// create the routines facade:
		routinesFacade = this.createRoutinesFacade(reactionsImportPath) as T
		if (routinesFacade !== null) {
			routinesFacade._setExecutionState(executionState)
			// store created routines facade:
			routinesFacades.put(reactionsImportPath, routinesFacade)
			return routinesFacade
		}
		return routinesFacade
	}
}
