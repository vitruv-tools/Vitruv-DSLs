package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateChildInExistingFamilyRoutine extends AbstractRoutine {
  private CreateChildInExistingFamilyRoutine.InputValues inputValues;

  private CreateChildInExistingFamilyRoutine.Create.CreatedValues createdValues;

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

    public CreateChildInExistingFamilyRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.families.Member newMember = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.families.impl.FamiliesFactoryImpl.eINSTANCE.createMember();
      });
      return new CreateChildInExistingFamilyRoutine.Create.CreatedValues(newMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insertedClient, final Family familyToInsertInto, final Member newMember, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      newMember.setFirstName(InsuranceToFamiliesHelper.getFirstName(insertedClient));
      Gender _gender = insertedClient.getGender();
      if (_gender != null) {
        switch (_gender) {
          case MALE:
            EList<Member> _sons = familyToInsertInto.getSons();
            _sons.add(newMember);
            break;
          case FEMALE:
            EList<Member> _daughters = familyToInsertInto.getDaughters();
            _daughters.add(newMember);
            break;
          default:
            break;
        }
      }
      this.addCorrespondenceBetween(insertedClient, familyToInsertInto);
      this.addCorrespondenceBetween(insertedClient, newMember);
    }
  }

  public CreateChildInExistingFamilyRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insertedClient, final Family familyToInsertInto) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateChildInExistingFamilyRoutine.InputValues(insertedClient, familyToInsertInto);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateChildInExistingFamilyRoutine with input:");
    	getLogger().trace("   inputValues.insertedClient: " + inputValues.insertedClient);
    	getLogger().trace("   inputValues.familyToInsertInto: " + inputValues.familyToInsertInto);
    }
    // This execution step is empty
    createdValues = new mir.routines.insuranceToFamilies.CreateChildInExistingFamilyRoutine.Create(getExecutionState()).createElements();
    new mir.routines.insuranceToFamilies.CreateChildInExistingFamilyRoutine.Update(getExecutionState()).updateModels(inputValues.insertedClient, inputValues.familyToInsertInto, createdValues.newMember, getRoutinesFacade());
    return true;
  }
}
