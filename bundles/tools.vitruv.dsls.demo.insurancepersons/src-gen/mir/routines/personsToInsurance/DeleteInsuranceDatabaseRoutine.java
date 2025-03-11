package mir.routines.personsToInsurance;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeleteInsuranceDatabaseRoutine extends AbstractRoutine {
  private DeleteInsuranceDatabaseRoutine.InputValues inputValues;

  private DeleteInsuranceDatabaseRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final PersonRegister personsRegister;

    public InputValues(final PersonRegister personsRegister) {
      this.personsRegister = personsRegister;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final InsuranceDatabase insuranceDatabase;

      public RetrievedValues(final InsuranceDatabase insuranceDatabase) {
        this.insuranceDatabase = insuranceDatabase;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceInsuranceDatabase(final PersonRegister personsRegister) {
      return personsRegister;
    }

    public DeleteInsuranceDatabaseRoutine.Match.RetrievedValues match(final PersonRegister personsRegister) throws IOException {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase insuranceDatabase = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceDatabase(personsRegister), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceDatabase == null) {
      	return null;
      }
      return new mir.routines.personsToInsurance.DeleteInsuranceDatabaseRoutine.Match.RetrievedValues(insuranceDatabase);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final PersonRegister personsRegister, final InsuranceDatabase insuranceDatabase, @Extension final PersonsToInsuranceRoutinesFacade _routinesFacade) {
      this.removeObject(insuranceDatabase);
      this.removeCorrespondenceBetween(insuranceDatabase, personsRegister);
    }
  }

  public DeleteInsuranceDatabaseRoutine(final PersonsToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final PersonRegister personsRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteInsuranceDatabaseRoutine.InputValues(personsRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteInsuranceDatabaseRoutine with input:");
    	getLogger().trace("   inputValues.personsRegister: " + inputValues.personsRegister);
    }
    retrievedValues = new mir.routines.personsToInsurance.DeleteInsuranceDatabaseRoutine.Match(getExecutionState()).match(inputValues.personsRegister);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToInsurance.DeleteInsuranceDatabaseRoutine.Update(getExecutionState()).updateModels(inputValues.personsRegister, retrievedValues.insuranceDatabase, getRoutinesFacade());
    return true;
  }
}
