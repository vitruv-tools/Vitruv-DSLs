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
public class CreateOrFindFemaleRoutine extends AbstractRoutine {
  private CreateOrFindFemaleRoutine.InputValues inputValues;

  private CreateOrFindFemaleRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newMember;

    public final Family family;

    public InputValues(final Member newMember, final Family family) {
      this.newMember = newMember;
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

    public EObject getCorrepondenceSource1(final Member newMember, final Family family) {
      return newMember;
    }

    public CreateOrFindFemaleRoutine.Match.RetrievedValues match(final Member newMember, final Family family) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(newMember, family), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Female.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.familiesToPersons.CreateOrFindFemaleRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMember, final Family family, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.createFemale(newMember, family);
    }
  }

  public CreateOrFindFemaleRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMember, final Family family) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateOrFindFemaleRoutine.InputValues(newMember, family);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateOrFindFemaleRoutine with input:");
    	getLogger().trace("   inputValues.newMember: " + inputValues.newMember);
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    }
    retrievedValues = new mir.routines.familiesToPersons.CreateOrFindFemaleRoutine.Match(getExecutionState()).match(inputValues.newMember, inputValues.family);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.CreateOrFindFemaleRoutine.Update(getExecutionState()).updateModels(inputValues.newMember, inputValues.family, getRoutinesFacade());
    return true;
  }
}
