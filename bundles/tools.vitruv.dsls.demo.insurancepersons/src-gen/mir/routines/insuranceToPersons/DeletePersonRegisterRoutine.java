package mir.routines.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeletePersonRegisterRoutine extends AbstractRoutine {
  private DeletePersonRegisterRoutine.InputValues inputValues;

  private DeletePersonRegisterRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceDatabase insuranceDatabase;

    public InputValues(final InsuranceDatabase insuranceDatabase) {
      this.insuranceDatabase = insuranceDatabase;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final PersonRegister personRegister;

      public RetrievedValues(final PersonRegister personRegister) {
        this.personRegister = personRegister;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourcePersonRegister(final InsuranceDatabase insuranceDatabase) {
      return insuranceDatabase;
    }

    public DeletePersonRegisterRoutine.Match.RetrievedValues match(final InsuranceDatabase insuranceDatabase) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.PersonRegister personRegister = getCorrespondingElement(
      	getCorrepondenceSourcePersonRegister(insuranceDatabase), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.PersonRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (personRegister == null) {
      	return null;
      }
      return new mir.routines.insuranceToPersons.DeletePersonRegisterRoutine.Match.RetrievedValues(personRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceDatabase insuranceDatabase, final PersonRegister personRegister, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      this.removeObject(personRegister);
      this.removeCorrespondenceBetween(personRegister, insuranceDatabase);
    }
  }

  public DeletePersonRegisterRoutine(final InsuranceToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceDatabase insuranceDatabase) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeletePersonRegisterRoutine.InputValues(insuranceDatabase);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeletePersonRegisterRoutine with input:");
    	getLogger().trace("   inputValues.insuranceDatabase: " + inputValues.insuranceDatabase);
    }
    retrievedValues = new mir.routines.insuranceToPersons.DeletePersonRegisterRoutine.Match(getExecutionState()).match(inputValues.insuranceDatabase);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToPersons.DeletePersonRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceDatabase, retrievedValues.personRegister, getRoutinesFacade());
    return true;
  }
}
