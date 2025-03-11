package mir.reactions.personsToInsurance;

import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.util.function.Function;
import mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacade;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.reference.InsertEReference;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class InsertedPersonReaction extends AbstractReaction {
  private InsertEReference<EObject> insertChange;

  public InsertedPersonReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertEReference insertChange, final PersonRegister affectedEObject, final EReference affectedFeature, final Person newValue, final int index, @Extension final PersonsToInsuranceRoutinesFacade _routinesFacade) {
      _routinesFacade.createInsuranceClient(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertEReference<?>)) {
    	return false;
    }
    
    InsertEReference<org.eclipse.emf.ecore.EObject> _localTypedChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.persons.PersonRegister)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("persons")) {
    	return false;
    }
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.persons.Person)) {
    	return false;
    }
    this.insertChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacade routinesFacade = (mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.persons.PersonRegister affectedEObject = (edu.kit.ipd.sdq.metamodels.persons.PersonRegister)insertChange.getAffectedElement();
    EReference affectedFeature = insertChange.getAffectedFeature();
    edu.kit.ipd.sdq.metamodels.persons.Person newValue = (edu.kit.ipd.sdq.metamodels.persons.Person)insertChange.getNewValue();
    int index = insertChange.getIndex();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.personsToInsurance.InsertedPersonReaction.Call(executionState).updateModels(insertChange, affectedEObject, affectedFeature, newValue, index, routinesFacade);
  }
}
