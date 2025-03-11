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
public class CreateMaleFromNewMemberRoutine extends AbstractRoutine {
  private CreateMaleFromNewMemberRoutine.InputValues inputValues;

  private CreateMaleFromNewMemberRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newFather;

    public final Family family;

    public InputValues(final Member newFather, final Family family) {
      this.newFather = newFather;
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

    public EObject getCorrepondenceSource1(final Member newFather, final Family family) {
      return newFather;
    }

    public CreateMaleFromNewMemberRoutine.Match.RetrievedValues match(final Member newFather, final Family family) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(newFather, family), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.familiesToPersons.CreateMaleFromNewMemberRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newFather, final Family family, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.createMale(newFather, family);
    }
  }

  public CreateMaleFromNewMemberRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newFather, final Family family) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateMaleFromNewMemberRoutine.InputValues(newFather, family);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateMaleFromNewMemberRoutine with input:");
    	getLogger().trace("   inputValues.newFather: " + inputValues.newFather);
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    }
    retrievedValues = new mir.routines.familiesToPersons.CreateMaleFromNewMemberRoutine.Match(getExecutionState()).match(inputValues.newFather, inputValues.family);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.CreateMaleFromNewMemberRoutine.Update(getExecutionState()).updateModels(inputValues.newFather, inputValues.family, getRoutinesFacade());
    return true;
  }
}
