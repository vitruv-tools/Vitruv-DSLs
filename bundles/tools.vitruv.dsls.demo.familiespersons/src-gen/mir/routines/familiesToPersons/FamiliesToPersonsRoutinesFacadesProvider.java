package mir.routines.familiesToPersons;

import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class FamiliesToPersonsRoutinesFacadesProvider extends AbstractRoutinesFacadesProvider {
  public FamiliesToPersonsRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    super(executionState);
  }

  public AbstractRoutinesFacade createRoutinesFacade(final ReactionsImportPath reactionsImportPath) {
    switch(reactionsImportPath.getPathString()) {
    	case "familiesToPersons": {
    		return new mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade(this, reactionsImportPath);
    	}
    	default: {
    		throw new IllegalArgumentException("Unexpected import path: " + reactionsImportPath.getPathString());
    	}
    }
  }
}
