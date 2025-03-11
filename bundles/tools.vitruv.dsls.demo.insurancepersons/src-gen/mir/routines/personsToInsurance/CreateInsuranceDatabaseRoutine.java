package mir.routines.personsToInsurance;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateInsuranceDatabaseRoutine extends AbstractRoutine {
  private CreateInsuranceDatabaseRoutine.InputValues inputValues;

  private CreateInsuranceDatabaseRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final PersonRegister personRegister;

    public InputValues(final PersonRegister personRegister) {
      this.personRegister = personRegister;
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final InsuranceDatabase insuranceDatabase;

      public CreatedValues(final InsuranceDatabase insuranceDatabase) {
        this.insuranceDatabase = insuranceDatabase;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateInsuranceDatabaseRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase insuranceDatabase = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.insurance.impl.InsuranceFactoryImpl.eINSTANCE.createInsuranceDatabase();
      });
      return new CreateInsuranceDatabaseRoutine.Create.CreatedValues(insuranceDatabase);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final PersonRegister personRegister, final InsuranceDatabase insuranceDatabase, @Extension final PersonsToInsuranceRoutinesFacade _routinesFacade) {
      this.persistProjectRelative(personRegister, insuranceDatabase, "model/insurance.insurance");
      this.addCorrespondenceBetween(insuranceDatabase, personRegister);
    }
  }

  public CreateInsuranceDatabaseRoutine(final PersonsToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final PersonRegister personRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateInsuranceDatabaseRoutine.InputValues(personRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateInsuranceDatabaseRoutine with input:");
    	getLogger().trace("   inputValues.personRegister: " + inputValues.personRegister);
    }
    // This execution step is empty
    createdValues = new mir.routines.personsToInsurance.CreateInsuranceDatabaseRoutine.Create(getExecutionState()).createElements();
    new mir.routines.personsToInsurance.CreateInsuranceDatabaseRoutine.Update(getExecutionState()).updateModels(inputValues.personRegister, createdValues.insuranceDatabase, getRoutinesFacade());
    return true;
  }
}
