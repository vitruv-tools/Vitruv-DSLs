package mir.reactions.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.util.function.Function;
import mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class DeletedInsuranceClientReaction extends AbstractReaction {
  private DeleteEObject<InsuranceClient> deleteChange;

  public DeletedInsuranceClientReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final DeleteEObject deleteChange, final InsuranceClient affectedEObject, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      _routinesFacade.deleteMember(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof DeleteEObject<?>)) {
    	return false;
    }
    
    DeleteEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient> _localTypedChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient)) {
    	return false;
    }
    this.deleteChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade routinesFacade = (mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient affectedEObject = (edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient)deleteChange.getAffectedElement();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.insuranceToFamilies.DeletedInsuranceClientReaction.Call(executionState).updateModels(deleteChange, affectedEObject, routinesFacade);
  }
}
