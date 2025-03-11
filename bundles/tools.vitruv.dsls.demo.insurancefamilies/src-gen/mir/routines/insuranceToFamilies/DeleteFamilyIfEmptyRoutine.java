package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
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
      Member _father = family.getFather();
      boolean _tripleEquals = (_father == null);
      return _tripleEquals;
    }

    public boolean checkMatcherPrecondition2(final Family family) {
      Member _mother = family.getMother();
      boolean _tripleEquals = (_mother == null);
      return _tripleEquals;
    }

    public boolean checkMatcherPrecondition3(final Family family) {
      boolean _isEmpty = family.getSons().isEmpty();
      return _isEmpty;
    }

    public boolean checkMatcherPrecondition4(final Family family) {
      boolean _isEmpty = family.getDaughters().isEmpty();
      return _isEmpty;
    }

    public DeleteFamilyIfEmptyRoutine.Match.RetrievedValues match(final Family family) throws IOException {
      if (!checkMatcherPrecondition1(family)) {
      	return null;
      }
      if (!checkMatcherPrecondition2(family)) {
      	return null;
      }
      if (!checkMatcherPrecondition3(family)) {
      	return null;
      }
      if (!checkMatcherPrecondition4(family)) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.DeleteFamilyIfEmptyRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Family family, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      this.removeObject(family);
    }
  }

  public DeleteFamilyIfEmptyRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Family family) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteFamilyIfEmptyRoutine.InputValues(family);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteFamilyIfEmptyRoutine with input:");
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.DeleteFamilyIfEmptyRoutine.Match(getExecutionState()).match(inputValues.family);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.DeleteFamilyIfEmptyRoutine.Update(getExecutionState()).updateModels(inputValues.family, getRoutinesFacade());
    return true;
  }
}
