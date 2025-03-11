package mir.reactions.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.util.function.Function;
import mir.routines.personsToFamilies.PersonsToFamiliesRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class InsertedPersonRegisterReaction extends AbstractReaction {
  private InsertRootEObject<PersonRegister> insertChange;

  public InsertedPersonRegisterReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertRootEObject insertChange, final PersonRegister newValue, final int index, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      _routinesFacade.createFamilyRegister(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertRootEObject<?>)) {
    	return false;
    }
    
    InsertRootEObject<edu.kit.ipd.sdq.metamodels.persons.PersonRegister> _localTypedChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.persons.PersonRegister>) change;
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.persons.PersonRegister)) {
    	return false;
    }
    this.insertChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.persons.PersonRegister>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.personsToFamilies.PersonsToFamiliesRoutinesFacade routinesFacade = (mir.routines.personsToFamilies.PersonsToFamiliesRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.persons.PersonRegister newValue = (edu.kit.ipd.sdq.metamodels.persons.PersonRegister)insertChange.getNewValue();
    int index = insertChange.getIndex();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.personsToFamilies.InsertedPersonRegisterReaction.Call(executionState).updateModels(insertChange, newValue, index, routinesFacade);
  }
}
