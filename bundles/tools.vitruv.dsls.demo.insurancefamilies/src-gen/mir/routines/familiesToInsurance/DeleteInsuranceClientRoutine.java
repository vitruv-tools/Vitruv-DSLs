package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeleteInsuranceClientRoutine extends AbstractRoutine {
  private DeleteInsuranceClientRoutine.InputValues inputValues;

  private DeleteInsuranceClientRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member member;

    public InputValues(final Member member) {
      this.member = member;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final InsuranceClient insuranceClient;

      public final Family family;

      public RetrievedValues(final InsuranceClient insuranceClient, final Family family) {
        this.insuranceClient = insuranceClient;
        this.family = family;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceInsuranceClient(final Member member) {
      return member;
    }

    public EObject getCorrepondenceSourceFamily(final Member member, final InsuranceClient insuranceClient) {
      return insuranceClient;
    }

    public DeleteInsuranceClientRoutine.Match.RetrievedValues match(final Member member) throws IOException {
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
      edu.kit.ipd.sdq.metamodels.families.Family family = getCorrespondingElement(
      	getCorrepondenceSourceFamily(member, insuranceClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (family == null) {
      	return null;
      }
      return new mir.routines.familiesToInsurance.DeleteInsuranceClientRoutine.Match.RetrievedValues(insuranceClient, family);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member member, final InsuranceClient insuranceClient, final Family family, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      this.removeCorrespondenceBetween(member, insuranceClient);
      this.removeCorrespondenceBetween(family, insuranceClient);
      this.removeObject(insuranceClient);
      _routinesFacade.deleteFamilyIfEmpty(family);
    }
  }

  public DeleteInsuranceClientRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member member) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteInsuranceClientRoutine.InputValues(member);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteInsuranceClientRoutine with input:");
    	getLogger().trace("   inputValues.member: " + inputValues.member);
    }
    retrievedValues = new mir.routines.familiesToInsurance.DeleteInsuranceClientRoutine.Match(getExecutionState()).match(inputValues.member);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToInsurance.DeleteInsuranceClientRoutine.Update(getExecutionState()).updateModels(inputValues.member, retrievedValues.insuranceClient, retrievedValues.family, getRoutinesFacade());
    return true;
  }
}
