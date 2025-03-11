package mir.reactions.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.util.function.Function;
import mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class DeletedInsuranceDatabaseReaction extends AbstractReaction {
  private DeleteEObject<InsuranceDatabase> deleteChange;

  public DeletedInsuranceDatabaseReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final DeleteEObject deleteChange, final InsuranceDatabase affectedEObject, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.deletePersonRegister(affectedEObject);
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
    mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade routinesFacade = (mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase affectedEObject = (edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase)deleteChange.getAffectedElement();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.insuranceToPersons.DeletedInsuranceDatabaseReaction.Call(executionState).updateModels(deleteChange, affectedEObject, routinesFacade);
  }
}
