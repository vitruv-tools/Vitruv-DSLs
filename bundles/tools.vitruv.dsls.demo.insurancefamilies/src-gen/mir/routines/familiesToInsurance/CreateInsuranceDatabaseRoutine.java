package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
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
    public final FamilyRegister familyRegister;

    public InputValues(final FamilyRegister familyRegister) {
      this.familyRegister = familyRegister;
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

    public void updateModels(final FamilyRegister familyRegister, final InsuranceDatabase insuranceDatabase, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      this.persistProjectRelative(familyRegister, insuranceDatabase, "model/insurance.insurance");
      this.addCorrespondenceBetween(familyRegister, insuranceDatabase);
    }
  }

  public CreateInsuranceDatabaseRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final FamilyRegister familyRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateInsuranceDatabaseRoutine.InputValues(familyRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateInsuranceDatabaseRoutine with input:");
    	getLogger().trace("   inputValues.familyRegister: " + inputValues.familyRegister);
    }
    // This execution step is empty
    createdValues = new mir.routines.familiesToInsurance.CreateInsuranceDatabaseRoutine.Create(getExecutionState()).createElements();
    new mir.routines.familiesToInsurance.CreateInsuranceDatabaseRoutine.Update(getExecutionState()).updateModels(inputValues.familyRegister, createdValues.insuranceDatabase, getRoutinesFacade());
    return true;
  }
}
