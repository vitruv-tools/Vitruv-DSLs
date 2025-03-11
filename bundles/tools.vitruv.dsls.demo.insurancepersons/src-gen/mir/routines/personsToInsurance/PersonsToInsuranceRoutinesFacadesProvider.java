package mir.routines.personsToInsurance;

import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class PersonsToInsuranceRoutinesFacadesProvider extends AbstractRoutinesFacadesProvider {
  public PersonsToInsuranceRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    super(executionState);
  }

  public AbstractRoutinesFacade createRoutinesFacade(final ReactionsImportPath reactionsImportPath) {
    switch(reactionsImportPath.getPathString()) {
    	case "personsToInsurance": {
    		return new mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacade(this, reactionsImportPath);
    	}
    	default: {
    		throw new IllegalArgumentException("Unexpected import path: " + reactionsImportPath.getPathString());
    	}
    }
  }
}
