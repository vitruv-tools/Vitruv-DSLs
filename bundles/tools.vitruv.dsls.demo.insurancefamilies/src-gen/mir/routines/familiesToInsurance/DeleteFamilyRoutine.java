package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import java.io.IOException;
import java.util.function.Consumer;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeleteFamilyRoutine extends AbstractRoutine {
  private DeleteFamilyRoutine.InputValues inputValues;

  public class InputValues {
    public final Family family;

    public InputValues(final Family family) {
      this.family = family;
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Family family, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      final Consumer<Member> _function = new Consumer<Member>() {
        public void accept(final Member it) {
          _routinesFacade.deleteInsuranceClient(it);
        }
      };
      FamiliesUtil.getMembers(family).forEach(_function);
    }
  }

  public DeleteFamilyRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Family family) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteFamilyRoutine.InputValues(family);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteFamilyRoutine with input:");
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    }
    // This execution step is empty
    // This execution step is empty
    new mir.routines.familiesToInsurance.DeleteFamilyRoutine.Update(getExecutionState()).updateModels(inputValues.family, getRoutinesFacade());
    return true;
  }
}
