package mir.reactions.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import java.util.function.Function;
import mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade;
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
public class InsertedDaughterReaction extends AbstractReaction {
  private InsertEReference<EObject> insertChange;

  public InsertedDaughterReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertEReference insertChange, final Family affectedEObject, final EReference affectedFeature, final Member newValue, final int index, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      _routinesFacade.updateNameAndCorrespondencesOfCorrespondingInsuranceClient(newValue, affectedEObject, Gender.FEMALE);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertEReference<?>)) {
    	return false;
    }
    
    InsertEReference<org.eclipse.emf.ecore.EObject> _localTypedChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.families.Family)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("daughters")) {
    	return false;
    }
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.families.Member)) {
    	return false;
    }
    this.insertChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade routinesFacade = (mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.families.Family affectedEObject = (edu.kit.ipd.sdq.metamodels.families.Family)insertChange.getAffectedElement();
    EReference affectedFeature = insertChange.getAffectedFeature();
    edu.kit.ipd.sdq.metamodels.families.Member newValue = (edu.kit.ipd.sdq.metamodels.families.Member)insertChange.getNewValue();
    int index = insertChange.getIndex();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.familiesToInsurance.InsertedDaughterReaction.Call(executionState).updateModels(insertChange, affectedEObject, affectedFeature, newValue, index, routinesFacade);
  }
}
