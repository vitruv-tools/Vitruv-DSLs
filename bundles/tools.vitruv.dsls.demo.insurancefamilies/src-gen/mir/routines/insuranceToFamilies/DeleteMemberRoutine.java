package mir.routines.insuranceToFamilies;

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
public class DeleteMemberRoutine extends AbstractRoutine {
  private DeleteMemberRoutine.InputValues inputValues;

  private DeleteMemberRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insuranceClient;

    public InputValues(final InsuranceClient insuranceClient) {
      this.insuranceClient = insuranceClient;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Member member;

      public final Family family;

      public RetrievedValues(final Member member, final Family family) {
        this.member = member;
        this.family = family;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceMember(final InsuranceClient insuranceClient) {
      return insuranceClient;
    }

    public EObject getCorrepondenceSourceFamily(final InsuranceClient insuranceClient, final Member member) {
      return insuranceClient;
    }

    public DeleteMemberRoutine.Match.RetrievedValues match(final InsuranceClient insuranceClient) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.Member member = getCorrespondingElement(
      	getCorrepondenceSourceMember(insuranceClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (member == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family family = getCorrespondingElement(
      	getCorrepondenceSourceFamily(insuranceClient, member), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (family == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.DeleteMemberRoutine.Match.RetrievedValues(member, family);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insuranceClient, final Member member, final Family family, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      this.removeCorrespondenceBetween(insuranceClient, member);
      this.removeCorrespondenceBetween(insuranceClient, family);
      this.removeObject(member);
      _routinesFacade.deleteFamilyIfEmpty(family);
    }
  }

  public DeleteMemberRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insuranceClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteMemberRoutine.InputValues(insuranceClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteMemberRoutine with input:");
    	getLogger().trace("   inputValues.insuranceClient: " + inputValues.insuranceClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.DeleteMemberRoutine.Match(getExecutionState()).match(inputValues.insuranceClient);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.DeleteMemberRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceClient, retrievedValues.member, retrievedValues.family, getRoutinesFacade());
    return true;
  }
}
