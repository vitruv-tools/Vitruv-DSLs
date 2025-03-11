package mir.routines.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.familiespersons.families2persons.FamiliesToPersonsHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine extends AbstractRoutine {
  private UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine.InputValues inputValues;

  private UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newMember;

    public final Family newFamily;

    public InputValues(final Member newMember, final Family newFamily) {
      this.newMember = newMember;
      this.newFamily = newFamily;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Person correspondingPerson;

      public final Family oldFamily;

      public RetrievedValues(final Person correspondingPerson, final Family oldFamily) {
        this.correspondingPerson = correspondingPerson;
        this.oldFamily = oldFamily;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceCorrespondingPerson(final Member newMember, final Family newFamily) {
      return newMember;
    }

    public EObject getCorrepondenceSourceOldFamily(final Member newMember, final Family newFamily, final Person correspondingPerson) {
      return correspondingPerson;
    }

    public UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine.Match.RetrievedValues match(final Member newMember, final Family newFamily) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.Person correspondingPerson = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingPerson(newMember, newFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingPerson == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(newMember, newFamily, correspondingPerson), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      return new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine.Match.RetrievedValues(correspondingPerson, oldFamily);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMember, final Family newFamily, final Person correspondingPerson, final Family oldFamily, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      correspondingPerson.setFullName(FamiliesToPersonsHelper.getPersonName(newMember));
      this.removeCorrespondenceBetween(correspondingPerson, oldFamily);
      this.addCorrespondenceBetween(correspondingPerson, newFamily);
      _routinesFacade.deleteFamilyIfEmpty(oldFamily);
    }
  }

  public UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMember, final Family newFamily) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine.InputValues(newMember, newFamily);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine with input:");
    	getLogger().trace("   inputValues.newMember: " + inputValues.newMember);
    	getLogger().trace("   inputValues.newFamily: " + inputValues.newFamily);
    }
    retrievedValues = new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine.Match(getExecutionState()).match(inputValues.newMember, inputValues.newFamily);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine.Update(getExecutionState()).updateModels(inputValues.newMember, inputValues.newFamily, retrievedValues.correspondingPerson, retrievedValues.oldFamily, getRoutinesFacade());
    return true;
  }
}
