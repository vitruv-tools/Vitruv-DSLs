package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateFamilyRegisterRoutine extends AbstractRoutine {
  private CreateFamilyRegisterRoutine.InputValues inputValues;

  private CreateFamilyRegisterRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final InsuranceDatabase insuranceDatabase;

    public InputValues(final InsuranceDatabase insuranceDatabase) {
      this.insuranceDatabase = insuranceDatabase;
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final FamilyRegister familyRegister;

      public CreatedValues(final FamilyRegister familyRegister) {
        this.familyRegister = familyRegister;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateFamilyRegisterRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister familyRegister = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.families.impl.FamiliesFactoryImpl.eINSTANCE.createFamilyRegister();
      });
      return new CreateFamilyRegisterRoutine.Create.CreatedValues(familyRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceDatabase insuranceDatabase, final FamilyRegister familyRegister, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      this.persistProjectRelative(insuranceDatabase, familyRegister, "model/families.families");
      this.addCorrespondenceBetween(familyRegister, insuranceDatabase);
    }
  }

  public CreateFamilyRegisterRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceDatabase insuranceDatabase) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateFamilyRegisterRoutine.InputValues(insuranceDatabase);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateFamilyRegisterRoutine with input:");
    	getLogger().trace("   inputValues.insuranceDatabase: " + inputValues.insuranceDatabase);
    }
    // This execution step is empty
    createdValues = new mir.routines.insuranceToFamilies.CreateFamilyRegisterRoutine.Create(getExecutionState()).createElements();
    new mir.routines.insuranceToFamilies.CreateFamilyRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceDatabase, createdValues.familyRegister, getRoutinesFacade());
    return true;
  }
}
