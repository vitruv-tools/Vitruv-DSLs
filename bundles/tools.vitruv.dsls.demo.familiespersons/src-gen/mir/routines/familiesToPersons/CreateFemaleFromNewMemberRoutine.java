package mir.routines.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateFemaleFromNewMemberRoutine extends AbstractRoutine {
  private CreateFemaleFromNewMemberRoutine.InputValues inputValues;

  private CreateFemaleFromNewMemberRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newMother;

    public final Family family;

    public InputValues(final Member newMother, final Family family) {
      this.newMother = newMother;
      this.family = family;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public RetrievedValues() {
        
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSource1(final Member newMother, final Family family) {
      return newMother;
    }

    public CreateFemaleFromNewMemberRoutine.Match.RetrievedValues match(final Member newMother, final Family family) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(newMother, family), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.familiesToPersons.CreateFemaleFromNewMemberRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMother, final Family family, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.createFemale(newMother, family);
    }
  }

  public CreateFemaleFromNewMemberRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMother, final Family family) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateFemaleFromNewMemberRoutine.InputValues(newMother, family);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateFemaleFromNewMemberRoutine with input:");
    	getLogger().trace("   inputValues.newMother: " + inputValues.newMother);
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    }
    retrievedValues = new mir.routines.familiesToPersons.CreateFemaleFromNewMemberRoutine.Match(getExecutionState()).match(inputValues.newMother, inputValues.family);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.CreateFemaleFromNewMemberRoutine.Update(getExecutionState()).updateModels(inputValues.newMother, inputValues.family, getRoutinesFacade());
    return true;
  }
}
