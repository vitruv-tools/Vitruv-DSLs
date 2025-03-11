package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Female;
import edu.kit.ipd.sdq.metamodels.persons.Male;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateParentInExistingFamilyRoutine extends AbstractRoutine {
  private CreateParentInExistingFamilyRoutine.InputValues inputValues;

  private CreateParentInExistingFamilyRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final Person insertedPerson;

    public final Family familyToInsertInto;

    public InputValues(final Person insertedPerson, final Family familyToInsertInto) {
      this.insertedPerson = insertedPerson;
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

    public void updateModels(final Person insertedPerson, final Family familyToInsertInto, final Member newMember, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      newMember.setFirstName(PersonsToFamiliesHelper.getFirstname(insertedPerson));
      boolean _matched = false;
      if (insertedPerson instanceof Male) {
        _matched=true;
        familyToInsertInto.setFather(newMember);
      }
      if (!_matched) {
        if (insertedPerson instanceof Female) {
          _matched=true;
          familyToInsertInto.setMother(newMember);
        }
      }
      this.addCorrespondenceBetween(insertedPerson, familyToInsertInto);
      this.addCorrespondenceBetween(insertedPerson, newMember);
    }
  }

  public CreateParentInExistingFamilyRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person insertedPerson, final Family familyToInsertInto) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateParentInExistingFamilyRoutine.InputValues(insertedPerson, familyToInsertInto);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateParentInExistingFamilyRoutine with input:");
    	getLogger().trace("   inputValues.insertedPerson: " + inputValues.insertedPerson);
    	getLogger().trace("   inputValues.familyToInsertInto: " + inputValues.familyToInsertInto);
    }
    // This execution step is empty
    createdValues = new mir.routines.personsToFamilies.CreateParentInExistingFamilyRoutine.Create(getExecutionState()).createElements();
    new mir.routines.personsToFamilies.CreateParentInExistingFamilyRoutine.Update(getExecutionState()).updateModels(inputValues.insertedPerson, inputValues.familyToInsertInto, createdValues.newMember, getRoutinesFacade());
    return true;
  }
}
