package mir.routines.insuranceToFamilies;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.PositionPreference;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class ReactToLastnameChangeRoutine extends AbstractRoutine {
  private ReactToLastnameChangeRoutine.InputValues inputValues;

  private ReactToLastnameChangeRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient renamedClient;

    public final String oldFullname;

    public InputValues(final InsuranceClient renamedClient, final String oldFullname) {
      this.renamedClient = renamedClient;
      this.oldFullname = oldFullname;
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

    public EObject getCorrepondenceSourceOldFamily(final InsuranceClient renamedClient, final String oldFullname) {
      return renamedClient;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final InsuranceClient renamedClient, final String oldFullname, final Family oldFamily) {
      return renamedClient;
    }

    public ReactToLastnameChangeRoutine.Match.RetrievedValues match(final InsuranceClient renamedClient, final String oldFullname) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(renamedClient, oldFullname), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Member correspondingMember = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingMember(renamedClient, oldFullname, oldFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingMember == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.ReactToLastnameChangeRoutine.Match.RetrievedValues(oldFamily, correspondingMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient renamedClient, final String oldFullname, final Family oldFamily, final Member correspondingMember, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      String _lastName = oldFamily.getLastName();
      String _lastName_1 = InsuranceToFamiliesHelper.getLastName(renamedClient);
      boolean _notEquals = (!Objects.equal(_lastName, _lastName_1));
      if (_notEquals) {
        int _size = IterableExtensions.size(FamiliesUtil.getMembers(oldFamily));
        boolean _equals = (_size == 1);
        if (_equals) {
          oldFamily.setLastName(InsuranceToFamiliesHelper.getLastName(renamedClient));
        } else {
          final boolean wasChildBeforeRenaming = ((correspondingMember.getFamilySon() == oldFamily) || (correspondingMember.getFamilyDaughter() == oldFamily));
          final PositionPreference positionPreference = InsuranceToFamiliesHelper.askUserWhetherClientIsParentOrChildDuringRenaming(this.executionState.getUserInteractor(), oldFullname, renamedClient.getName(), wasChildBeforeRenaming);
          if (positionPreference != null) {
            switch (positionPreference) {
              case Child:
                _routinesFacade.insertExistingMemberIntoUserChosenFamilyAsChild(renamedClient);
                break;
              case Parent:
                _routinesFacade.insertExistingMemberIntoUserChosenFamilyAsParent(renamedClient);
                break;
              default:
                break;
            }
          }
        }
      }
    }
  }

  public ReactToLastnameChangeRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient renamedClient, final String oldFullname) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ReactToLastnameChangeRoutine.InputValues(renamedClient, oldFullname);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ReactToLastnameChangeRoutine with input:");
    	getLogger().trace("   inputValues.renamedClient: " + inputValues.renamedClient);
    	getLogger().trace("   inputValues.oldFullname: " + inputValues.oldFullname);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.ReactToLastnameChangeRoutine.Match(getExecutionState()).match(inputValues.renamedClient, inputValues.oldFullname);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.ReactToLastnameChangeRoutine.Update(getExecutionState()).updateModels(inputValues.renamedClient, inputValues.oldFullname, retrievedValues.oldFamily, retrievedValues.correspondingMember, getRoutinesFacade());
    return true;
  }
}
