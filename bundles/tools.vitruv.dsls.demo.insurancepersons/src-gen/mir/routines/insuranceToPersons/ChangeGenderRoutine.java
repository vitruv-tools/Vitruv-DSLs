package mir.routines.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.persons.Female;
import edu.kit.ipd.sdq.metamodels.persons.Male;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class ChangeGenderRoutine extends AbstractRoutine {
  private ChangeGenderRoutine.InputValues inputValues;

  private ChangeGenderRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient client;

    public InputValues(final InsuranceClient client) {
      this.client = client;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Person person;

      public RetrievedValues(final Person person) {
        this.person = person;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourcePerson(final InsuranceClient client) {
      return client;
    }

    public ChangeGenderRoutine.Match.RetrievedValues match(final InsuranceClient client) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.Person person = getCorrespondingElement(
      	getCorrepondenceSourcePerson(client), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (person == null) {
      	return null;
      }
      return new mir.routines.insuranceToPersons.ChangeGenderRoutine.Match.RetrievedValues(person);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient client, final Person person, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      Gender _gender = client.getGender();
      if (_gender != null) {
        switch (_gender) {
          case MALE:
            if ((!(person instanceof Male))) {
              _routinesFacade.deletePerson(client);
              _routinesFacade.createMalePerson(client);
            }
            break;
          case FEMALE:
            if ((!(person instanceof Female))) {
              _routinesFacade.deletePerson(client);
              _routinesFacade.createFemalePerson(client);
            }
            break;
          default:
            throw new IllegalArgumentException("Gender of client is unknown.");
        }
      } else {
        throw new IllegalArgumentException("Gender of client is unknown.");
      }
    }
  }

  public ChangeGenderRoutine(final InsuranceToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient client) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ChangeGenderRoutine.InputValues(client);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ChangeGenderRoutine with input:");
    	getLogger().trace("   inputValues.client: " + inputValues.client);
    }
    retrievedValues = new mir.routines.insuranceToPersons.ChangeGenderRoutine.Match(getExecutionState()).match(inputValues.client);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToPersons.ChangeGenderRoutine.Update(getExecutionState()).updateModels(inputValues.client, retrievedValues.person, getRoutinesFacade());
    return true;
  }
}
