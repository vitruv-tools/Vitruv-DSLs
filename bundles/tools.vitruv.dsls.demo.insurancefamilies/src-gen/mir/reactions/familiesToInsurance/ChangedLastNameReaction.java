package mir.reactions.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Family;
import java.util.function.Function;
import mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class ChangedLastNameReaction extends AbstractReaction {
  private ReplaceSingleValuedEAttribute<Family, String> replaceChange;

  public ChangedLastNameReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ReplaceSingleValuedEAttribute replaceChange, final Family affectedEObject, final EAttribute affectedFeature, final String oldValue, final String newValue, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      _routinesFacade.changeFamilyName(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof ReplaceSingleValuedEAttribute<?, ?>)) {
    	return false;
    }
    
    ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.families.Family, java.lang.String> _localTypedChange = (ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.families.Family, java.lang.String>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.families.Family)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("lastName")) {
    	return false;
    }
    if (_localTypedChange.isFromNonDefaultValue() && !(_localTypedChange.getOldValue() instanceof java.lang.String)) {
    	return false;
    }
    if (_localTypedChange.isToNonDefaultValue() && !(_localTypedChange.getNewValue() instanceof java.lang.String)) {
    	return false;
    }
    this.replaceChange = (ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.families.Family, java.lang.String>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade routinesFacade = (mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.families.Family affectedEObject = (edu.kit.ipd.sdq.metamodels.families.Family)replaceChange.getAffectedElement();
    EAttribute affectedFeature = replaceChange.getAffectedFeature();
    java.lang.String oldValue = (java.lang.String)replaceChange.getOldValue();
    java.lang.String newValue = (java.lang.String)replaceChange.getNewValue();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.familiesToInsurance.ChangedLastNameReaction.Call(executionState).updateModels(replaceChange, affectedEObject, affectedFeature, oldValue, newValue, routinesFacade);
  }
}
