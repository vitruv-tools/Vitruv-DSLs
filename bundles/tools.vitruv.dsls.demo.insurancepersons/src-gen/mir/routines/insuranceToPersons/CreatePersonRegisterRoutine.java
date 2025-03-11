package mir.routines.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreatePersonRegisterRoutine extends AbstractRoutine {
  private CreatePersonRegisterRoutine.InputValues inputValues;

  private CreatePersonRegisterRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final InsuranceDatabase insuranceDatabase;

    public InputValues(final InsuranceDatabase insuranceDatabase) {
      this.insuranceDatabase = insuranceDatabase;
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final PersonRegister personRegister;

      public CreatedValues(final PersonRegister personRegister) {
        this.personRegister = personRegister;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreatePersonRegisterRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.persons.PersonRegister personRegister = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.persons.impl.PersonsFactoryImpl.eINSTANCE.createPersonRegister();
      });
      return new CreatePersonRegisterRoutine.Create.CreatedValues(personRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceDatabase insuranceDatabase, final PersonRegister personRegister, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      this.persistProjectRelative(insuranceDatabase, personRegister, "model/persons.persons");
      this.addCorrespondenceBetween(personRegister, insuranceDatabase);
    }
  }

  public CreatePersonRegisterRoutine(final InsuranceToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceDatabase insuranceDatabase) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreatePersonRegisterRoutine.InputValues(insuranceDatabase);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreatePersonRegisterRoutine with input:");
    	getLogger().trace("   inputValues.insuranceDatabase: " + inputValues.insuranceDatabase);
    }
    // This execution step is empty
    createdValues = new mir.routines.insuranceToPersons.CreatePersonRegisterRoutine.Create(getExecutionState()).createElements();
    new mir.routines.insuranceToPersons.CreatePersonRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceDatabase, createdValues.personRegister, getRoutinesFacade());
    return true;
  }
}
