package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class TryRemoveCorrespondenceToOldFamilyRoutine extends AbstractRoutine {
  private TryRemoveCorrespondenceToOldFamilyRoutine.InputValues inputValues;

  private TryRemoveCorrespondenceToOldFamilyRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insuranceClient;

    public InputValues(final InsuranceClient insuranceClient) {
      this.insuranceClient = insuranceClient;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Family oldFamily;

      public RetrievedValues(final Family oldFamily) {
        this.oldFamily = oldFamily;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceOldFamily(final InsuranceClient insuranceClient) {
      return insuranceClient;
    }

    public TryRemoveCorrespondenceToOldFamilyRoutine.Match.RetrievedValues match(final InsuranceClient insuranceClient) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(insuranceClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.TryRemoveCorrespondenceToOldFamilyRoutine.Match.RetrievedValues(oldFamily);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insuranceClient, final Family oldFamily, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      this.removeCorrespondenceBetween(insuranceClient, oldFamily);
    }
  }

  public TryRemoveCorrespondenceToOldFamilyRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insuranceClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new TryRemoveCorrespondenceToOldFamilyRoutine.InputValues(insuranceClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine TryRemoveCorrespondenceToOldFamilyRoutine with input:");
    	getLogger().trace("   inputValues.insuranceClient: " + inputValues.insuranceClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.TryRemoveCorrespondenceToOldFamilyRoutine.Match(getExecutionState()).match(inputValues.insuranceClient);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.TryRemoveCorrespondenceToOldFamilyRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceClient, retrievedValues.oldFamily, getRoutinesFacade());
    return true;
  }
}
