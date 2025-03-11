package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.PositionPreference;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class InsertAsParentOrChildRoutine extends AbstractRoutine {
  private InsertAsParentOrChildRoutine.InputValues inputValues;

  private InsertAsParentOrChildRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insertedClient;

    public InputValues(final InsuranceClient insertedClient) {
      this.insertedClient = insertedClient;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public RetrievedValues() {
        
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean checkMatcherPrecondition1(final InsuranceClient insertedClient) {
      boolean _xblockexpression = false;
      {
        InsuranceToFamiliesHelper.assertValidName(insertedClient);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public InsertAsParentOrChildRoutine.Match.RetrievedValues match(final InsuranceClient insertedClient) throws IOException {
      if (!checkMatcherPrecondition1(insertedClient)) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.InsertAsParentOrChildRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insertedClient, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      final PositionPreference role = InsuranceToFamiliesHelper.askUserWhetherClientIsParentOrChild(this.executionState.getUserInteractor(), insertedClient);
      if (role != null) {
        switch (role) {
          case Child:
            _routinesFacade.createChild(insertedClient);
            break;
          case Parent:
            _routinesFacade.createParent(insertedClient);
            break;
          default:
            break;
        }
      }
    }
  }

  public InsertAsParentOrChildRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insertedClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertAsParentOrChildRoutine.InputValues(insertedClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertAsParentOrChildRoutine with input:");
    	getLogger().trace("   inputValues.insertedClient: " + inputValues.insertedClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.InsertAsParentOrChildRoutine.Match(getExecutionState()).match(inputValues.insertedClient);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.InsertAsParentOrChildRoutine.Update(getExecutionState()).updateModels(inputValues.insertedClient, getRoutinesFacade());
    return true;
  }
}
