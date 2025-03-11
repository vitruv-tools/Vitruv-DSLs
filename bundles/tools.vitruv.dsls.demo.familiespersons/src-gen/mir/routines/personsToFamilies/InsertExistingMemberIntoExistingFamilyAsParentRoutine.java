package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
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
public class InsertExistingMemberIntoExistingFamilyAsParentRoutine extends AbstractRoutine {
  private InsertExistingMemberIntoExistingFamilyAsParentRoutine.InputValues inputValues;

  private InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Person renamedPerson;

    public final Family familyToInsertInto;

    public InputValues(final Person renamedPerson, final Family familyToInsertInto) {
      this.renamedPerson = renamedPerson;
      this.familyToInsertInto = familyToInsertInto;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Family oldFamily;

      public final Member correspondingMember;

      public RetrievedValues(final Family oldFamily, final Member correspondingMember) {
        this.oldFamily = oldFamily;
        this.correspondingMember = correspondingMember;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceOldFamily(final Person renamedPerson, final Family familyToInsertInto) {
      return renamedPerson;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final Person renamedPerson, final Family familyToInsertInto, final Family oldFamily) {
      return renamedPerson;
    }

    public InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match.RetrievedValues match(final Person renamedPerson, final Family familyToInsertInto) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(renamedPerson, familyToInsertInto), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Member correspondingMember = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingMember(renamedPerson, familyToInsertInto, oldFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingMember == null) {
      	return null;
      }
      return new mir.routines.personsToFamilies.InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match.RetrievedValues(oldFamily, correspondingMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person renamedPerson, final Family familyToInsertInto, final Family oldFamily, final Member correspondingMember, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      boolean _matched = false;
      if (renamedPerson instanceof Male) {
        _matched=true;
        familyToInsertInto.setFather(correspondingMember);
      }
      if (!_matched) {
        if (renamedPerson instanceof Female) {
          _matched=true;
          familyToInsertInto.setMother(correspondingMember);
        }
      }
      this.removeCorrespondenceBetween(renamedPerson, oldFamily);
      this.addCorrespondenceBetween(renamedPerson, familyToInsertInto);
    }
  }

  public InsertExistingMemberIntoExistingFamilyAsParentRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person renamedPerson, final Family familyToInsertInto) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertExistingMemberIntoExistingFamilyAsParentRoutine.InputValues(renamedPerson, familyToInsertInto);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertExistingMemberIntoExistingFamilyAsParentRoutine with input:");
    	getLogger().trace("   inputValues.renamedPerson: " + inputValues.renamedPerson);
    	getLogger().trace("   inputValues.familyToInsertInto: " + inputValues.familyToInsertInto);
    }
    retrievedValues = new mir.routines.personsToFamilies.InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match(getExecutionState()).match(inputValues.renamedPerson, inputValues.familyToInsertInto);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.InsertExistingMemberIntoExistingFamilyAsParentRoutine.Update(getExecutionState()).updateModels(inputValues.renamedPerson, inputValues.familyToInsertInto, retrievedValues.oldFamily, retrievedValues.correspondingMember, getRoutinesFacade());
    return true;
  }
}
