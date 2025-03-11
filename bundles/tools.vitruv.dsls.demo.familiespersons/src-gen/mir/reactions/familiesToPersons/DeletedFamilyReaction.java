package mir.reactions.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import java.util.function.Consumer;
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
public class DeletedFamilyReaction extends AbstractReaction {
  private DeleteEObject<Family> deleteChange;

  public DeletedFamilyReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final DeleteEObject deleteChange, final Family affectedEObject, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      final Consumer<Member> _function = new Consumer<Member>() {
        public void accept(final Member it) {
          _routinesFacade.deletePerson(it);
        }
      };
      FamiliesUtil.getMembers(affectedEObject).forEach(_function);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof DeleteEObject<?>)) {
    	return false;
    }
    
    DeleteEObject<edu.kit.ipd.sdq.metamodels.families.Family> _localTypedChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.families.Family>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.families.Family)) {
    	return false;
    }
    this.deleteChange = (DeleteEObject<edu.kit.ipd.sdq.metamodels.families.Family>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade routinesFacade = (mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.families.Family affectedEObject = (edu.kit.ipd.sdq.metamodels.families.Family)deleteChange.getAffectedElement();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.familiesToPersons.DeletedFamilyReaction.Call(executionState).updateModels(deleteChange, affectedEObject, routinesFacade);
  }
}
