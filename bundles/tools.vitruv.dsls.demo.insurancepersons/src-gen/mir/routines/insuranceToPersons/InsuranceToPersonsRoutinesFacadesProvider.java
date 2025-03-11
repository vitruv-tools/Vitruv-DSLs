package mir.routines.insuranceToPersons;

import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class InsuranceToPersonsRoutinesFacadesProvider extends AbstractRoutinesFacadesProvider {
  public InsuranceToPersonsRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    super(executionState);
  }

  public AbstractRoutinesFacade createRoutinesFacade(final ReactionsImportPath reactionsImportPath) {
    switch(reactionsImportPath.getPathString()) {
    	case "insuranceToPersons": {
    		return new mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade(this, reactionsImportPath);
    	}
    	default: {
    		throw new IllegalArgumentException("Unexpected import path: " + reactionsImportPath.getPathString());
    	}
    }
  }
}
