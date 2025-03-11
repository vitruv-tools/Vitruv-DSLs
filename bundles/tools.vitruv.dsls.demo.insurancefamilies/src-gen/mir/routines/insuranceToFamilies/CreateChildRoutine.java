package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateChildRoutine extends AbstractRoutine {
  private CreateChildRoutine.InputValues inputValues;

  private CreateChildRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insertedClient;

    public InputValues(final InsuranceClient insertedClient) {
      this.insertedClient = insertedClient;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final FamilyRegister insuranceDatabase;

      public RetrievedValues(final FamilyRegister insuranceDatabase) {
        this.insuranceDatabase = insuranceDatabase;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceInsuranceDatabase(final InsuranceClient insertedClient) {
      InsuranceDatabase _insuranceDatabase = InsuranceToFamiliesHelper.getInsuranceDatabase(insertedClient);
      return _insuranceDatabase;
    }

    public CreateChildRoutine.Match.RetrievedValues match(final InsuranceClient insertedClient) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister insuranceDatabase = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceDatabase(insertedClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceDatabase == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.CreateChildRoutine.Match.RetrievedValues(insuranceDatabase);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insertedClient, final FamilyRegister insuranceDatabase, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      final Iterable<Family> matchingFamilies = IterableExtensions.<Family>filter(insuranceDatabase.getFamilies(), ((Function1<? super Family, Boolean>)InsuranceToFamiliesHelper.sameLastName(insertedClient)));
      Family _xifexpression = null;
      boolean _isEmpty = IterableExtensions.isEmpty(matchingFamilies);
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = InsuranceToFamiliesHelper.askUserWhichFamilyToInsertTheMemberIn(this.executionState.getUserInteractor(), insertedClient, matchingFamilies);
      }
      final Family familyToInsertInto = _xifexpression;
      if ((familyToInsertInto == null)) {
        _routinesFacade.createChildInNewFamily(insertedClient);
      } else {
        _routinesFacade.createChildInExistingFamily(insertedClient, familyToInsertInto);
      }
    }
  }

  public CreateChildRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insertedClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateChildRoutine.InputValues(insertedClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateChildRoutine with input:");
    	getLogger().trace("   inputValues.insertedClient: " + inputValues.insertedClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.CreateChildRoutine.Match(getExecutionState()).match(inputValues.insertedClient);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.CreateChildRoutine.Update(getExecutionState()).updateModels(inputValues.insertedClient, retrievedValues.insuranceDatabase, getRoutinesFacade());
    return true;
  }
}
