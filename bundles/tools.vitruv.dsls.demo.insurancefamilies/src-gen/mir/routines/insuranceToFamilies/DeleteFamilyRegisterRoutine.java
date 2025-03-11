package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeleteFamilyRegisterRoutine extends AbstractRoutine {
  private DeleteFamilyRegisterRoutine.InputValues inputValues;

  private DeleteFamilyRegisterRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceDatabase insuranceDatabase;

    public InputValues(final InsuranceDatabase insuranceDatabase) {
      this.insuranceDatabase = insuranceDatabase;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final FamilyRegister familyRegister;

      public RetrievedValues(final FamilyRegister familyRegister) {
        this.familyRegister = familyRegister;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceFamilyRegister(final InsuranceDatabase insuranceDatabase) {
      return insuranceDatabase;
    }

    public DeleteFamilyRegisterRoutine.Match.RetrievedValues match(final InsuranceDatabase insuranceDatabase) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister familyRegister = getCorrespondingElement(
      	getCorrepondenceSourceFamilyRegister(insuranceDatabase), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (familyRegister == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.DeleteFamilyRegisterRoutine.Match.RetrievedValues(familyRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceDatabase insuranceDatabase, final FamilyRegister familyRegister, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      this.removeObject(familyRegister);
      this.removeCorrespondenceBetween(familyRegister, insuranceDatabase);
    }
  }

  public DeleteFamilyRegisterRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceDatabase insuranceDatabase) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteFamilyRegisterRoutine.InputValues(insuranceDatabase);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteFamilyRegisterRoutine with input:");
    	getLogger().trace("   inputValues.insuranceDatabase: " + inputValues.insuranceDatabase);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.DeleteFamilyRegisterRoutine.Match(getExecutionState()).match(inputValues.insuranceDatabase);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.DeleteFamilyRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceDatabase, retrievedValues.familyRegister, getRoutinesFacade());
    return true;
  }
}
