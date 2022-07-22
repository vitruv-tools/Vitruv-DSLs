package tools.vitruv.dsls.reactions.runtime.routines

import tools.vitruv.dsls.reactions.runtime.structure.Loggable
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving
import java.util.Stack

/**
 * Note: All methods start with an underscore here to not conflict with the methods that are generated from the routines by
 * concrete implementations.
 */
abstract class AbstractRoutinesFacade extends Loggable implements RoutinesFacade {
	// used by concrete implementations to request routines facades of executed routines: 
	val RoutinesFacadesProvider routinesFacadesProvider
	// absolute path inside the import hierarchy to the segment this routines facade belongs to, never null:
	val ReactionsImportPath reactionsImportPath
	// shared execution state among all routines facades in the import hierarchy:
	var ReactionExecutionState executionState
	val Stack<CallHierarchyHaving> callerStack = new Stack()
	
	new(RoutinesFacadesProvider routinesFacadesProvider, ReactionsImportPath reactionsImportPath) {
		this.routinesFacadesProvider = routinesFacadesProvider
		this.reactionsImportPath = reactionsImportPath
	}

	protected def RoutinesFacadesProvider _getRoutinesFacadesProvider() {
		return routinesFacadesProvider
	}

	protected def ReactionsImportPath _getReactionsImportPath() {
		return reactionsImportPath
	}
	
	override _setExecutionState(ReactionExecutionState executionState) {
		this.executionState = executionState
	}
	
	override _getExecutionState() {
		return executionState
	}
	
	override _pushCaller(CallHierarchyHaving caller) {
		callerStack.push(caller)
	}
	
	override _getCurrentCaller() {
		return if (!callerStack.empty()) callerStack.peek() else null
	}
	
	override _dropLastCaller() {
		callerStack.pop()
	}
	
}
