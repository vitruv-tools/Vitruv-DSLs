package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeleteInsuranceDatabaseRoutine extends AbstractRoutine {
  private DeleteInsuranceDatabaseRoutine.InputValues inputValues;

  private DeleteInsuranceDatabaseRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final FamilyRegister familyRegister;

    public InputValues(final FamilyRegister familyRegister) {
      this.familyRegister = familyRegister;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final InsuranceDatabase insuranceDatabase;

      public RetrievedValues(final InsuranceDatabase insuranceDatabase) {
        this.insuranceDatabase = insuranceDatabase;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceInsuranceDatabase(final FamilyRegister familyRegister) {
      return familyRegister;
    }

    public DeleteInsuranceDatabaseRoutine.Match.RetrievedValues match(final FamilyRegister familyRegister) throws IOException {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase insuranceDatabase = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceDatabase(familyRegister), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceDatabase == null) {
      	return null;
      }
      return new mir.routines.familiesToInsurance.DeleteInsuranceDatabaseRoutine.Match.RetrievedValues(insuranceDatabase);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final FamilyRegister familyRegister, final InsuranceDatabase insuranceDatabase, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      this.removeObject(insuranceDatabase);
      this.removeCorrespondenceBetween(insuranceDatabase, familyRegister);
    }
  }

  public DeleteInsuranceDatabaseRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final FamilyRegister familyRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteInsuranceDatabaseRoutine.InputValues(familyRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteInsuranceDatabaseRoutine with input:");
    	getLogger().trace("   inputValues.familyRegister: " + inputValues.familyRegister);
    }
    retrievedValues = new mir.routines.familiesToInsurance.DeleteInsuranceDatabaseRoutine.Match(getExecutionState()).match(inputValues.familyRegister);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToInsurance.DeleteInsuranceDatabaseRoutine.Update(getExecutionState()).updateModels(inputValues.familyRegister, retrievedValues.insuranceDatabase, getRoutinesFacade());
    return true;
  }
}
