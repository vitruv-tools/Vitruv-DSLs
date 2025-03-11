package mir.reactions.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.util.function.Function;
import mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class ChangedGenderReaction extends AbstractReaction {
  private ReplaceSingleValuedEAttribute<InsuranceClient, Gender> replaceChange;

  public ChangedGenderReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ReplaceSingleValuedEAttribute replaceChange, final InsuranceClient affectedEObject, final EAttribute affectedFeature, final Gender oldValue, final Gender newValue, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      _routinesFacade.changeGender(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof ReplaceSingleValuedEAttribute<?, ?>)) {
    	return false;
    }
    
    ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient, edu.kit.ipd.sdq.metamodels.insurance.Gender> _localTypedChange = (ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient, edu.kit.ipd.sdq.metamodels.insurance.Gender>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("gender")) {
    	return false;
    }
    if (_localTypedChange.isFromNonDefaultValue() && !(_localTypedChange.getOldValue() instanceof edu.kit.ipd.sdq.metamodels.insurance.Gender)) {
    	return false;
    }
    if (_localTypedChange.isToNonDefaultValue() && !(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.insurance.Gender)) {
    	return false;
    }
    this.replaceChange = (ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient, edu.kit.ipd.sdq.metamodels.insurance.Gender>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade routinesFacade = (mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient affectedEObject = (edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient)replaceChange.getAffectedElement();
    EAttribute affectedFeature = replaceChange.getAffectedFeature();
    edu.kit.ipd.sdq.metamodels.insurance.Gender oldValue = (edu.kit.ipd.sdq.metamodels.insurance.Gender)replaceChange.getOldValue();
    edu.kit.ipd.sdq.metamodels.insurance.Gender newValue = (edu.kit.ipd.sdq.metamodels.insurance.Gender)replaceChange.getNewValue();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.insuranceToPersons.ChangedGenderReaction.Call(executionState).updateModels(replaceChange, affectedEObject, affectedFeature, oldValue, newValue, routinesFacade);
  }
}
