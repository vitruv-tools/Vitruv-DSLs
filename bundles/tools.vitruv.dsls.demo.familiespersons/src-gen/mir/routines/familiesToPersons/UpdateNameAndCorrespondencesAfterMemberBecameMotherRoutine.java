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
public class UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine extends AbstractRoutine {
  private UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine.InputValues inputValues;

  private UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newMother;

    public final Family newFamily;

    public InputValues(final Member newMother, final Family newFamily) {
      this.newMother = newMother;
      this.newFamily = newFamily;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Person correspondingPerson;

      public RetrievedValues(final Person correspondingPerson) {
        this.correspondingPerson = correspondingPerson;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean getCorrespondingModelElementsPreconditionCorrespondingPerson(final Member newMother, final Family newFamily, final Person it) {
      boolean _xblockexpression = false;
      {
        FamiliesToPersonsHelper.assertFemale(it);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceCorrespondingPerson(final Member newMother, final Family newFamily) {
      return newMother;
    }

    public UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine.Match.RetrievedValues match(final Member newMother, final Family newFamily) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.Person correspondingPerson = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingPerson(newMother, newFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	(edu.kit.ipd.sdq.metamodels.persons.Person _element) -> getCorrespondingModelElementsPreconditionCorrespondingPerson(newMother, newFamily, _element), // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingPerson == null) {
      	return null;
      }
      return new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine.Match.RetrievedValues(correspondingPerson);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMother, final Family newFamily, final Person correspondingPerson, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.updateNameAndCorrespondencesOfCorrespondingPerson(newMother, newFamily);
    }
  }

  public UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMother, final Family newFamily) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine.InputValues(newMother, newFamily);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine with input:");
    	getLogger().trace("   inputValues.newMother: " + inputValues.newMother);
    	getLogger().trace("   inputValues.newFamily: " + inputValues.newFamily);
    }
    retrievedValues = new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine.Match(getExecutionState()).match(inputValues.newMother, inputValues.newFamily);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine.Update(getExecutionState()).updateModels(inputValues.newMother, inputValues.newFamily, retrievedValues.correspondingPerson, getRoutinesFacade());
    return true;
  }
}
