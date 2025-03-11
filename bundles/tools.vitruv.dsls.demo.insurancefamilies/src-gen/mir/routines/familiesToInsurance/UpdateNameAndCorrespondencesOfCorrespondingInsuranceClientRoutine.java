package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.families2insurance.FamiliesToInsuranceHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine extends AbstractRoutine {
  private UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine.InputValues inputValues;

  private UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newMember;

    public final Family newFamily;

    public final Gender gender;

    public InputValues(final Member newMember, final Family newFamily, final Gender gender) {
      this.newMember = newMember;
      this.newFamily = newFamily;
      this.gender = gender;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final InsuranceClient correspondingInsuranceClient;

      public final Family oldFamily;

      public RetrievedValues(final InsuranceClient correspondingInsuranceClient, final Family oldFamily) {
        this.correspondingInsuranceClient = correspondingInsuranceClient;
        this.oldFamily = oldFamily;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean getCorrespondingModelElementsPreconditionCorrespondingInsuranceClient(final Member newMember, final Family newFamily, final Gender gender, final InsuranceClient it) {
      boolean _xblockexpression = false;
      {
        FamiliesToInsuranceHelper.assertGender(it, gender);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceCorrespondingInsuranceClient(final Member newMember, final Family newFamily, final Gender gender) {
      return newMember;
    }

    public EObject getCorrepondenceSourceOldFamily(final Member newMember, final Family newFamily, final Gender gender, final InsuranceClient correspondingInsuranceClient) {
      return correspondingInsuranceClient;
    }

    public UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine.Match.RetrievedValues match(final Member newMember, final Family newFamily, final Gender gender) throws IOException {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient correspondingInsuranceClient = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingInsuranceClient(newMember, newFamily, gender), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient.class,
      	(edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient _element) -> getCorrespondingModelElementsPreconditionCorrespondingInsuranceClient(newMember, newFamily, gender, _element), // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingInsuranceClient == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(newMember, newFamily, gender, correspondingInsuranceClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      return new mir.routines.familiesToInsurance.UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine.Match.RetrievedValues(correspondingInsuranceClient, oldFamily);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMember, final Family newFamily, final Gender gender, final InsuranceClient correspondingInsuranceClient, final Family oldFamily, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      correspondingInsuranceClient.setName(FamiliesToInsuranceHelper.getInsuranceClientName(newMember));
      this.removeCorrespondenceBetween(correspondingInsuranceClient, oldFamily);
      this.addCorrespondenceBetween(correspondingInsuranceClient, newFamily);
      _routinesFacade.deleteFamilyIfEmpty(oldFamily);
    }
  }

  public UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMember, final Family newFamily, final Gender gender) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine.InputValues(newMember, newFamily, gender);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine with input:");
    	getLogger().trace("   inputValues.newMember: " + inputValues.newMember);
    	getLogger().trace("   inputValues.newFamily: " + inputValues.newFamily);
    	getLogger().trace("   inputValues.gender: " + inputValues.gender);
    }
    retrievedValues = new mir.routines.familiesToInsurance.UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine.Match(getExecutionState()).match(inputValues.newMember, inputValues.newFamily, inputValues.gender);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToInsurance.UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine.Update(getExecutionState()).updateModels(inputValues.newMember, inputValues.newFamily, inputValues.gender, retrievedValues.correspondingInsuranceClient, retrievedValues.oldFamily, getRoutinesFacade());
    return true;
  }
}
