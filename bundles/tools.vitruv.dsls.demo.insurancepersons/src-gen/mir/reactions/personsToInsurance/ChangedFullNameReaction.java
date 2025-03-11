package mir.reactions.personsToInsurance;

import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.util.function.Function;
import mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacade;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class ChangedFullNameReaction extends AbstractReaction {
  private ReplaceSingleValuedEAttribute<Person, String> replaceChange;

  public ChangedFullNameReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ReplaceSingleValuedEAttribute replaceChange, final Person affectedEObject, final EAttribute affectedFeature, final String oldValue, final String newValue, @Extension final PersonsToInsuranceRoutinesFacade _routinesFacade) {
      _routinesFacade.changeNames(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof ReplaceSingleValuedEAttribute<?, ?>)) {
    	return false;
    }
    
    ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.persons.Person, java.lang.String> _localTypedChange = (ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.persons.Person, java.lang.String>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.persons.Person)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("fullName")) {
    	return false;
    }
    if (_localTypedChange.isFromNonDefaultValue() && !(_localTypedChange.getOldValue() instanceof java.lang.String)) {
    	return false;
    }
    if (_localTypedChange.isToNonDefaultValue() && !(_localTypedChange.getNewValue() instanceof java.lang.String)) {
    	return false;
    }
    this.replaceChange = (ReplaceSingleValuedEAttribute<edu.kit.ipd.sdq.metamodels.persons.Person, java.lang.String>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacade routinesFacade = (mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.persons.Person affectedEObject = (edu.kit.ipd.sdq.metamodels.persons.Person)replaceChange.getAffectedElement();
    EAttribute affectedFeature = replaceChange.getAffectedFeature();
    java.lang.String oldValue = (java.lang.String)replaceChange.getOldValue();
    java.lang.String newValue = (java.lang.String)replaceChange.getNewValue();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.personsToInsurance.ChangedFullNameReaction.Call(executionState).updateModels(replaceChange, affectedEObject, affectedFeature, oldValue, newValue, routinesFacade);
  }
}
