package mir.reactions.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import java.util.function.Function;
import mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class CreatedFamilyREgisterReaction extends AbstractReaction {
  private InsertRootEObject<FamilyRegister> insertChange;

  public CreatedFamilyREgisterReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertRootEObject insertChange, final FamilyRegister newValue, final int index, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      _routinesFacade.createInsuranceDatabase(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertRootEObject<?>)) {
    	return false;
    }
    
    InsertRootEObject<edu.kit.ipd.sdq.metamodels.families.FamilyRegister> _localTypedChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.families.FamilyRegister>) change;
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.families.FamilyRegister)) {
    	return false;
    }
    this.insertChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.families.FamilyRegister>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade routinesFacade = (mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.families.FamilyRegister newValue = (edu.kit.ipd.sdq.metamodels.families.FamilyRegister)insertChange.getNewValue();
    int index = insertChange.getIndex();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.familiesToInsurance.CreatedFamilyREgisterReaction.Call(executionState).updateModels(insertChange, newValue, index, routinesFacade);
  }
}
