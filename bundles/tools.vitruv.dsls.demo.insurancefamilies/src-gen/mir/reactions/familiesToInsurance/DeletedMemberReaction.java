package mir.reactions.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Member;
import java.util.function.Function;
import mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class DeletedMemberReaction extends AbstractReaction {
  private DeleteEObject<Member> deleteChange;

  public DeletedMemberReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final DeleteEObject deleteChange, final Member affectedEObject, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      _routinesFacade.deleteInsuranceClient(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof DeleteEObject<?>)) {
    	return false;
    }
    
    DeleteEObject<edu.kit.ipd.sdq.metamodels.families.Member> _localTypedChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.families.Member>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.families.Member)) {
    	return false;
    }
    this.deleteChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.families.Member>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade routinesFacade = (mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.families.Member affectedEObject = (edu.kit.ipd.sdq.metamodels.families.Member)deleteChange.getAffectedElement();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.familiesToInsurance.DeletedMemberReaction.Call(executionState).updateModels(deleteChange, affectedEObject, routinesFacade);
  }
}
