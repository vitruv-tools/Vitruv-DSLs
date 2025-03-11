package mir.reactions.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.util.function.Function;
import mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class InsertedInsuranceDatabaseReaction extends AbstractReaction {
  private InsertRootEObject<InsuranceDatabase> insertChange;

  public InsertedInsuranceDatabaseReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertRootEObject insertChange, final InsuranceDatabase newValue, final int index, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.createPersonRegister(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertRootEObject<?>)) {
    	return false;
    }
    
    InsertRootEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase> _localTypedChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase>) change;
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase)) {
    	return false;
    }
    this.insertChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade routinesFacade = (mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase newValue = (edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase)insertChange.getNewValue();
    int index = insertChange.getIndex();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.insuranceToPersons.InsertedInsuranceDatabaseReaction.Call(executionState).updateModels(insertChange, newValue, index, routinesFacade);
  }
}
