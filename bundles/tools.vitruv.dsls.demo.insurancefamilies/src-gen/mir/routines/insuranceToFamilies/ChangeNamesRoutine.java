package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class ChangeNamesRoutine extends AbstractRoutine {
  private ChangeNamesRoutine.InputValues inputValues;

  private ChangeNamesRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient renamedClient;

    public final String oldFullName;

    public InputValues(final InsuranceClient renamedClient, final String oldFullName) {
      this.renamedClient = renamedClient;
      this.oldFullName = oldFullName;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Family oldFamily;

      public final Member correspondingMember;

      public RetrievedValues(final Family oldFamily, final Member correspondingMember) {
        this.oldFamily = oldFamily;
        this.correspondingMember = correspondingMember;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean checkMatcherPrecondition1(final InsuranceClient renamedClient, final String oldFullName) {
      boolean _xblockexpression = false;
      {
        InsuranceToFamiliesHelper.assertValidName(renamedClient);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceOldFamily(final InsuranceClient renamedClient, final String oldFullName) {
      return renamedClient;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final InsuranceClient renamedClient, final String oldFullName, final Family oldFamily) {
      return renamedClient;
    }

    public ChangeNamesRoutine.Match.RetrievedValues match(final InsuranceClient renamedClient, final String oldFullName) throws IOException {
      if (!checkMatcherPrecondition1(renamedClient, oldFullName)) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(renamedClient, oldFullName), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Member correspondingMember = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingMember(renamedClient, oldFullName, oldFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingMember == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.ChangeNamesRoutine.Match.RetrievedValues(oldFamily, correspondingMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient renamedClient, final String oldFullName, final Family oldFamily, final Member correspondingMember, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      correspondingMember.setFirstName(InsuranceToFamiliesHelper.getFirstName(renamedClient));
      _routinesFacade.reactToLastnameChange(renamedClient, oldFullName);
    }
  }

  public ChangeNamesRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient renamedClient, final String oldFullName) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ChangeNamesRoutine.InputValues(renamedClient, oldFullName);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ChangeNamesRoutine with input:");
    	getLogger().trace("   inputValues.renamedClient: " + inputValues.renamedClient);
    	getLogger().trace("   inputValues.oldFullName: " + inputValues.oldFullName);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.ChangeNamesRoutine.Match(getExecutionState()).match(inputValues.renamedClient, inputValues.oldFullName);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.ChangeNamesRoutine.Update(getExecutionState()).updateModels(inputValues.renamedClient, inputValues.oldFullName, retrievedValues.oldFamily, retrievedValues.correspondingMember, getRoutinesFacade());
    return true;
  }
}
