package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeleteFamilyIfEmptyRoutine extends AbstractRoutine {
  private DeleteFamilyIfEmptyRoutine.InputValues inputValues;

  private DeleteFamilyIfEmptyRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Family family;

    public InputValues(final Family family) {
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

    public boolean checkMatcherPrecondition1(final Family family) {
      int _size = IterableExtensions.size(FamiliesUtil.getMembers(family));
      boolean _equals = (_size == 0);
      return _equals;
    }

    public DeleteFamilyIfEmptyRoutine.Match.RetrievedValues match(final Family family) throws IOException {
      if (!checkMatcherPrecondition1(family)) {
      	return null;
      }
      return new mir.routines.familiesToInsurance.DeleteFamilyIfEmptyRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Family family, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      this.removeObject(family);
    }
  }

  public DeleteFamilyIfEmptyRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Family family) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteFamilyIfEmptyRoutine.InputValues(family);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteFamilyIfEmptyRoutine with input:");
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    }
    retrievedValues = new mir.routines.familiesToInsurance.DeleteFamilyIfEmptyRoutine.Match(getExecutionState()).match(inputValues.family);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToInsurance.DeleteFamilyIfEmptyRoutine.Update(getExecutionState()).updateModels(inputValues.family, getRoutinesFacade());
    return true;
  }
}
