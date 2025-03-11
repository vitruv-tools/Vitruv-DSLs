package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class TryCreateInsuranceClientFromNewMemberRoutine extends AbstractRoutine {
  private TryCreateInsuranceClientFromNewMemberRoutine.InputValues inputValues;

  private TryCreateInsuranceClientFromNewMemberRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member newMember;

    public final Family family;

    public final Gender gender;

    public InputValues(final Member newMember, final Family family, final Gender gender) {
      this.newMember = newMember;
      this.family = family;
      this.gender = gender;
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

    public EObject getCorrepondenceSource1(final Member newMember, final Family family, final Gender gender) {
      return newMember;
    }

    public TryCreateInsuranceClientFromNewMemberRoutine.Match.RetrievedValues match(final Member newMember, final Family family, final Gender gender) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(newMember, family, gender), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.familiesToInsurance.TryCreateInsuranceClientFromNewMemberRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMember, final Family family, final Gender gender, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      _routinesFacade.createInsuranceClient(newMember, family, gender);
    }
  }

  public TryCreateInsuranceClientFromNewMemberRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMember, final Family family, final Gender gender) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new TryCreateInsuranceClientFromNewMemberRoutine.InputValues(newMember, family, gender);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine TryCreateInsuranceClientFromNewMemberRoutine with input:");
    	getLogger().trace("   inputValues.newMember: " + inputValues.newMember);
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    	getLogger().trace("   inputValues.gender: " + inputValues.gender);
    }
    retrievedValues = new mir.routines.familiesToInsurance.TryCreateInsuranceClientFromNewMemberRoutine.Match(getExecutionState()).match(inputValues.newMember, inputValues.family, inputValues.gender);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToInsurance.TryCreateInsuranceClientFromNewMemberRoutine.Update(getExecutionState()).updateModels(inputValues.newMember, inputValues.family, inputValues.gender, getRoutinesFacade());
    return true;
  }
}
