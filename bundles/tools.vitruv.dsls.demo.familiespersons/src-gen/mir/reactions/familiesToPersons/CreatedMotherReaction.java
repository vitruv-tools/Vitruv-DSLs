package mir.reactions.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import java.util.function.Function;
import mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class CreatedMotherReaction extends AbstractReaction {
  private ReplaceSingleValuedEReference<EObject> replaceChange;

  public CreatedMotherReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ReplaceSingleValuedEReference replaceChange, final Family affectedEObject, final EReference affectedFeature, final Member oldValue, final Member newValue, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.updateNameAndCorrespondencesAfterMemberBecameMother(newValue, affectedEObject);
      _routinesFacade.createFemaleFromNewMember(newValue, affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof ReplaceSingleValuedEReference<?>)) {
    	return false;
    }
    
    ReplaceSingleValuedEReference<org.eclipse.emf.ecore.EObject> _localTypedChange = (ReplaceSingleValuedEReference<org.eclipse.emf.ecore.EObject>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.families.Family)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("mother")) {
    	return false;
    }
    if (_localTypedChange.isFromNonDefaultValue() && !(_localTypedChange.getOldValue() instanceof edu.kit.ipd.sdq.metamodels.families.Member)) {
    	return false;
    }
    if (_localTypedChange.isToNonDefaultValue() && !(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.families.Member)) {
    	return false;
    }
    this.replaceChange = (ReplaceSingleValuedEReference<org.eclipse.emf.ecore.EObject>) change;
    return true;
  }

  private boolean isUserDefinedPreconditionFulfilled(final ReplaceSingleValuedEReference replaceChange, final Family affectedEObject, final EReference affectedFeature, final Member oldValue, final Member newValue) {
    return (newValue != null);
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade routinesFacade = (mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.families.Family affectedEObject = (edu.kit.ipd.sdq.metamodels.families.Family)replaceChange.getAffectedElement();
    EReference affectedFeature = replaceChange.getAffectedFeature();
    edu.kit.ipd.sdq.metamodels.families.Member oldValue = (edu.kit.ipd.sdq.metamodels.families.Member)replaceChange.getOldValue();
    edu.kit.ipd.sdq.metamodels.families.Member newValue = (edu.kit.ipd.sdq.metamodels.families.Member)replaceChange.getNewValue();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed change matching of Reaction " + this.getClass().getName());
    }
    if (!isUserDefinedPreconditionFulfilled(replaceChange, affectedEObject, affectedFeature, oldValue, newValue)) {
    	return;
    }
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.familiesToPersons.CreatedMotherReaction.Call(executionState).updateModels(replaceChange, affectedEObject, affectedFeature, oldValue, newValue, routinesFacade);
  }
}
