package mir.reactions.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.util.function.Function;
import mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade;
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
public class CreatedInsuranceClientReaction extends AbstractReaction {
  private InsertEReference<EObject> insertChange;

  public CreatedInsuranceClientReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertEReference insertChange, final InsuranceDatabase affectedEObject, final EReference affectedFeature, final InsuranceClient newValue, final int index, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      _routinesFacade.insertAsParentOrChild(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertEReference<?>)) {
    	return false;
    }
    
    InsertEReference<org.eclipse.emf.ecore.EObject> _localTypedChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("insuranceclient")) {
    	return false;
    }
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient)) {
    	return false;
    }
    this.insertChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade routinesFacade = (mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase affectedEObject = (edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase)insertChange.getAffectedElement();
    EReference affectedFeature = insertChange.getAffectedFeature();
    edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient newValue = (edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient)insertChange.getNewValue();
    int index = insertChange.getIndex();
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.insuranceToFamilies.CreatedInsuranceClientReaction.Call(executionState).updateModels(insertChange, affectedEObject, affectedFeature, newValue, index, routinesFacade);
  }
}
