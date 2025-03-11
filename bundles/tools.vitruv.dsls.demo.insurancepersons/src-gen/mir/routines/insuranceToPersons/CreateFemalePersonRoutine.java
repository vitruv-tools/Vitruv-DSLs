package mir.routines.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceUtil;
import edu.kit.ipd.sdq.metamodels.persons.Female;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateFemalePersonRoutine extends AbstractRoutine {
  private CreateFemalePersonRoutine.InputValues inputValues;

  private CreateFemalePersonRoutine.Match.RetrievedValues retrievedValues;

  private CreateFemalePersonRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final InsuranceClient client;

    public InputValues(final InsuranceClient client) {
      this.client = client;
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

    public EObject getCorrepondenceSourcePersonRegister(final InsuranceClient client) {
      InsuranceDatabase _insuranceDatabase = InsuranceUtil.getInsuranceDatabase(client);
      return _insuranceDatabase;
    }

    public CreateFemalePersonRoutine.Match.RetrievedValues match(final InsuranceClient client) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.PersonRegister personRegister = getCorrespondingElement(
      	getCorrepondenceSourcePersonRegister(client), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.PersonRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (personRegister == null) {
      	return null;
      }
      return new mir.routines.insuranceToPersons.CreateFemalePersonRoutine.Match.RetrievedValues(personRegister);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Female newPerson;

      public CreatedValues(final Female newPerson) {
        this.newPerson = newPerson;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateFemalePersonRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.persons.Female newPerson = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.persons.impl.PersonsFactoryImpl.eINSTANCE.createFemale();
      });
      return new CreateFemalePersonRoutine.Create.CreatedValues(newPerson);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient client, final PersonRegister personRegister, final Female newPerson, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      newPerson.setFullName(client.getName());
      EList<Person> _persons = personRegister.getPersons();
      _persons.add(newPerson);
      this.addCorrespondenceBetween(client, newPerson);
    }
  }

  public CreateFemalePersonRoutine(final InsuranceToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient client) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateFemalePersonRoutine.InputValues(client);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateFemalePersonRoutine with input:");
    	getLogger().trace("   inputValues.client: " + inputValues.client);
    }
    retrievedValues = new mir.routines.insuranceToPersons.CreateFemalePersonRoutine.Match(getExecutionState()).match(inputValues.client);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.insuranceToPersons.CreateFemalePersonRoutine.Create(getExecutionState()).createElements();
    new mir.routines.insuranceToPersons.CreateFemalePersonRoutine.Update(getExecutionState()).updateModels(inputValues.client, retrievedValues.personRegister, createdValues.newPerson, getRoutinesFacade());
    return true;
  }
}
