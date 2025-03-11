package mir.routines.personsToInsurance;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class ChangeNamesRoutine extends AbstractRoutine {
  private ChangeNamesRoutine.InputValues inputValues;

  private ChangeNamesRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Person person;

    public InputValues(final Person person) {
      this.person = person;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final InsuranceClient client;

      public RetrievedValues(final InsuranceClient client) {
        this.client = client;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceClient(final Person person) {
      return person;
    }

    public ChangeNamesRoutine.Match.RetrievedValues match(final Person person) throws IOException {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient client = getCorrespondingElement(
      	getCorrepondenceSourceClient(person), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (client == null) {
      	return null;
      }
      return new mir.routines.personsToInsurance.ChangeNamesRoutine.Match.RetrievedValues(client);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person person, final InsuranceClient client, @Extension final PersonsToInsuranceRoutinesFacade _routinesFacade) {
      client.setName(person.getFullName());
    }
  }

  public ChangeNamesRoutine(final PersonsToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person person) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ChangeNamesRoutine.InputValues(person);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ChangeNamesRoutine with input:");
    	getLogger().trace("   inputValues.person: " + inputValues.person);
    }
    retrievedValues = new mir.routines.personsToInsurance.ChangeNamesRoutine.Match(getExecutionState()).match(inputValues.person);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToInsurance.ChangeNamesRoutine.Update(getExecutionState()).updateModels(inputValues.person, retrievedValues.client, getRoutinesFacade());
    return true;
  }
}
