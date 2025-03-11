package mir.routines.personsToInsurance;

import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.persons.Female;
import edu.kit.ipd.sdq.metamodels.persons.Male;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import edu.kit.ipd.sdq.metamodels.persons.PersonsUtil;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateInsuranceClientRoutine extends AbstractRoutine {
  private CreateInsuranceClientRoutine.InputValues inputValues;

  private CreateInsuranceClientRoutine.Match.RetrievedValues retrievedValues;

  private CreateInsuranceClientRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final Person person;

    public InputValues(final Person person) {
      this.person = person;
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

    public EObject getCorrepondenceSourceInsuranceDatabase(final Person person) {
      PersonRegister _personRegister = PersonsUtil.getPersonRegister(person);
      return _personRegister;
    }

    public CreateInsuranceClientRoutine.Match.RetrievedValues match(final Person person) throws IOException {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase insuranceDatabase = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceDatabase(person), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceDatabase == null) {
      	return null;
      }
      return new mir.routines.personsToInsurance.CreateInsuranceClientRoutine.Match.RetrievedValues(insuranceDatabase);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final InsuranceClient newClient;

      public CreatedValues(final InsuranceClient newClient) {
        this.newClient = newClient;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateInsuranceClientRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient newClient = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.insurance.impl.InsuranceFactoryImpl.eINSTANCE.createInsuranceClient();
      });
      return new CreateInsuranceClientRoutine.Create.CreatedValues(newClient);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person person, final InsuranceDatabase insuranceDatabase, final InsuranceClient newClient, @Extension final PersonsToInsuranceRoutinesFacade _routinesFacade) {
      newClient.setName(person.getFullName());
      boolean _matched = false;
      if (person instanceof Male) {
        _matched=true;
        newClient.setGender(Gender.MALE);
      }
      if (!_matched) {
        if (person instanceof Female) {
          _matched=true;
          newClient.setGender(Gender.FEMALE);
        }
      }
      if (!_matched) {
        throw new IllegalArgumentException("Gender of client is unknown.");
      }
      EList<InsuranceClient> _insuranceclient = insuranceDatabase.getInsuranceclient();
      _insuranceclient.add(newClient);
      this.addCorrespondenceBetween(person, newClient);
    }
  }

  public CreateInsuranceClientRoutine(final PersonsToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person person) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateInsuranceClientRoutine.InputValues(person);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateInsuranceClientRoutine with input:");
    	getLogger().trace("   inputValues.person: " + inputValues.person);
    }
    retrievedValues = new mir.routines.personsToInsurance.CreateInsuranceClientRoutine.Match(getExecutionState()).match(inputValues.person);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.personsToInsurance.CreateInsuranceClientRoutine.Create(getExecutionState()).createElements();
    new mir.routines.personsToInsurance.CreateInsuranceClientRoutine.Update(getExecutionState()).updateModels(inputValues.person, retrievedValues.insuranceDatabase, createdValues.newClient, getRoutinesFacade());
    return true;
  }
}
