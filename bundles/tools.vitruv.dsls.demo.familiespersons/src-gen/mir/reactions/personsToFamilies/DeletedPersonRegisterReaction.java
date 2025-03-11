package mir.reactions.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.util.function.Function;
import mir.routines.personsToFamilies.PersonsToFamiliesRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.eobject.DeleteEObject;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class DeletedPersonRegisterReaction extends AbstractReaction {
  private DeleteEObject<PersonRegister> deleteChange;

  public DeletedPersonRegisterReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final DeleteEObject deleteChange, final PersonRegister affectedEObject, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      _routinesFacade.deleteFamilyRegister(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof DeleteEObject<?>)) {
    	return false;
    }
    
    DeleteEObject<edu.kit.ipd.sdq.metamodels.persons.PersonRegister> _localTypedChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.persons.PersonRegister>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.persons.PersonRegister)) {
    	return false;
    }
    this.deleteChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.persons.PersonRegister>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.personsToFamilies.PersonsToFamiliesRoutinesFacade routinesFacade = (mir.routines.personsToFamilies.PersonsToFamiliesRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.persons.PersonRegister affectedEObject = (edu.kit.ipd.sdq.metamodels.persons.PersonRegister)deleteChange.getAffectedElement();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.personsToFamilies.DeletedPersonRegisterReaction.Call(executionState).updateModels(deleteChange, affectedEObject, routinesFacade);
  }
}
