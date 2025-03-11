package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.families2insurance.FamiliesToInsuranceHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateInsuranceClientNameRoutine extends AbstractRoutine {
  private UpdateInsuranceClientNameRoutine.InputValues inputValues;

  private UpdateInsuranceClientNameRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member member;

    public InputValues(final Member member) {
      this.member = member;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final InsuranceClient insuranceClient;

      public RetrievedValues(final InsuranceClient insuranceClient) {
        this.insuranceClient = insuranceClient;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean checkMatcherPrecondition1(final Member member) {
      boolean _xblockexpression = false;
      {
        FamiliesToInsuranceHelper.assertValidFirstname(member);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceInsuranceClient(final Member member) {
      return member;
    }

    public UpdateInsuranceClientNameRoutine.Match.RetrievedValues match(final Member member) throws IOException {
      if (!checkMatcherPrecondition1(member)) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient insuranceClient = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceClient(member), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceClient == null) {
      	return null;
      }
      return new mir.routines.familiesToInsurance.UpdateInsuranceClientNameRoutine.Match.RetrievedValues(insuranceClient);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member member, final InsuranceClient insuranceClient, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      insuranceClient.setName(FamiliesToInsuranceHelper.getInsuranceClientName(member));
    }
  }

  public UpdateInsuranceClientNameRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member member) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateInsuranceClientNameRoutine.InputValues(member);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateInsuranceClientNameRoutine with input:");
    	getLogger().trace("   inputValues.member: " + inputValues.member);
    }
    retrievedValues = new mir.routines.familiesToInsurance.UpdateInsuranceClientNameRoutine.Match(getExecutionState()).match(inputValues.member);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToInsurance.UpdateInsuranceClientNameRoutine.Update(getExecutionState()).updateModels(inputValues.member, retrievedValues.insuranceClient, getRoutinesFacade());
    return true;
  }
}
