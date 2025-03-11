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
public class UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine extends AbstractRoutine {
  private UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine.InputValues inputValues;

  private UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newFather;

    public final Family newFamily;

    public InputValues(final Member newFather, final Family newFamily) {
      this.newFather = newFather;
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

    public boolean getCorrespondingModelElementsPreconditionCorrespondingPerson(final Member newFather, final Family newFamily, final Person it) {
      boolean _xblockexpression = false;
      {
        FamiliesToPersonsHelper.assertMale(it);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceCorrespondingPerson(final Member newFather, final Family newFamily) {
      return newFather;
    }

    public UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine.Match.RetrievedValues match(final Member newFather, final Family newFamily) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.Person correspondingPerson = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingPerson(newFather, newFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	(edu.kit.ipd.sdq.metamodels.persons.Person _element) -> getCorrespondingModelElementsPreconditionCorrespondingPerson(newFather, newFamily, _element), // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingPerson == null) {
      	return null;
      }
      return new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine.Match.RetrievedValues(correspondingPerson);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newFather, final Family newFamily, final Person correspondingPerson, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.updateNameAndCorrespondencesOfCorrespondingPerson(newFather, newFamily);
    }
  }

  public UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newFather, final Family newFamily) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine.InputValues(newFather, newFamily);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine with input:");
    	getLogger().trace("   inputValues.newFather: " + inputValues.newFather);
    	getLogger().trace("   inputValues.newFamily: " + inputValues.newFamily);
    }
    retrievedValues = new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine.Match(getExecutionState()).match(inputValues.newFather, inputValues.newFamily);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine.Update(getExecutionState()).updateModels(inputValues.newFather, inputValues.newFamily, retrievedValues.correspondingPerson, getRoutinesFacade());
    return true;
  }
}
