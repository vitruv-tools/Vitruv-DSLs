package mir.reactions.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
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
public class DeleteInsuranceDatabaseReaction extends AbstractReaction {
  private DeleteEObject<InsuranceDatabase> deleteChange;

  public DeleteInsuranceDatabaseReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final DeleteEObject deleteChange, final InsuranceDatabase affectedEObject, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      _routinesFacade.deleteFamilyRegister(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof DeleteEObject<?>)) {
    	return false;
    }
    
    DeleteEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase> _localTypedChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase)) {
    	return false;
    }
    this.deleteChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade routinesFacade = (mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase affectedEObject = (edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase)deleteChange.getAffectedElement();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.insuranceToFamilies.DeleteInsuranceDatabaseReaction.Call(executionState).updateModels(deleteChange, affectedEObject, routinesFacade);
  }
}
