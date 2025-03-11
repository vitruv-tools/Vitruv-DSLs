package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateParentInExistingFamilyRoutine extends AbstractRoutine {
  private CreateParentInExistingFamilyRoutine.InputValues inputValues;

  private CreateParentInExistingFamilyRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final InsuranceClient insertedClient;

    public final Family familyToInsertInto;

    public InputValues(final InsuranceClient insertedClient, final Family familyToInsertInto) {
      this.insertedClient = insertedClient;
      this.familyToInsertInto = familyToInsertInto;
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Member newMember;

      public CreatedValues(final Member newMember) {
        this.newMember = newMember;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateParentInExistingFamilyRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.families.Member newMember = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.families.impl.FamiliesFactoryImpl.eINSTANCE.createMember();
      });
      return new CreateParentInExistingFamilyRoutine.Create.CreatedValues(newMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insertedClient, final Family familyToInsertInto, final Member newMember, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      newMember.setFirstName(InsuranceToFamiliesHelper.getFirstName(insertedClient));
      _routinesFacade.insertExistingMemberIntoExistingFamilyAsParent(insertedClient, newMember, familyToInsertInto);
      this.addCorrespondenceBetween(insertedClient, newMember);
    }
  }

  public CreateParentInExistingFamilyRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insertedClient, final Family familyToInsertInto) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateParentInExistingFamilyRoutine.InputValues(insertedClient, familyToInsertInto);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateParentInExistingFamilyRoutine with input:");
    	getLogger().trace("   inputValues.insertedClient: " + inputValues.insertedClient);
    	getLogger().trace("   inputValues.familyToInsertInto: " + inputValues.familyToInsertInto);
    }
    // This execution step is empty
    createdValues = new mir.routines.insuranceToFamilies.CreateParentInExistingFamilyRoutine.Create(getExecutionState()).createElements();
    new mir.routines.insuranceToFamilies.CreateParentInExistingFamilyRoutine.Update(getExecutionState()).updateModels(inputValues.insertedClient, inputValues.familyToInsertInto, createdValues.newMember, getRoutinesFacade());
    return true;
  }
}
