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
public class ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine extends AbstractRoutine {
  private ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine.InputValues inputValues;

  private ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member insertedChild;

    public final Family newFamily;

    public InputValues(final Member insertedChild, final Family newFamily) {
      this.insertedChild = insertedChild;
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

    public boolean getCorrespondingModelElementsPreconditionCorrespondingPerson(final Member insertedChild, final Family newFamily, final Person it) {
      boolean _xblockexpression = false;
      {
        FamiliesToPersonsHelper.assertMale(it);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceCorrespondingPerson(final Member insertedChild, final Family newFamily) {
      return insertedChild;
    }

    public ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine.Match.RetrievedValues match(final Member insertedChild, final Family newFamily) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.Person correspondingPerson = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingPerson(insertedChild, newFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	(edu.kit.ipd.sdq.metamodels.persons.Person _element) -> getCorrespondingModelElementsPreconditionCorrespondingPerson(insertedChild, newFamily, _element), // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingPerson == null) {
      	return null;
      }
      return new mir.routines.familiesToPersons.ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine.Match.RetrievedValues(correspondingPerson);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member insertedChild, final Family newFamily, final Person correspondingPerson, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.updatePersonFamilyCorrespondence(insertedChild, newFamily);
    }
  }

  public ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member insertedChild, final Family newFamily) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine.InputValues(insertedChild, newFamily);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine with input:");
    	getLogger().trace("   inputValues.insertedChild: " + inputValues.insertedChild);
    	getLogger().trace("   inputValues.newFamily: " + inputValues.newFamily);
    }
    retrievedValues = new mir.routines.familiesToPersons.ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine.Match(getExecutionState()).match(inputValues.insertedChild, inputValues.newFamily);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine.Update(getExecutionState()).updateModels(inputValues.insertedChild, inputValues.newFamily, retrievedValues.correspondingPerson, getRoutinesFacade());
    return true;
  }
}
