package mir.reactions.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.Member;
import java.util.function.Function;
import mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade;
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

    public void updateModels(final DeleteEObject deleteChange, final Member affectedEObject, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.deletePerson(affectedEObject);
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
    mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade routinesFacade = (mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.families.Member affectedEObject = (edu.kit.ipd.sdq.metamodels.families.Member)deleteChange.getAffectedElement();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.familiesToPersons.DeletedMemberReaction.Call(executionState).updateModels(deleteChange, affectedEObject, routinesFacade);
  }
}
