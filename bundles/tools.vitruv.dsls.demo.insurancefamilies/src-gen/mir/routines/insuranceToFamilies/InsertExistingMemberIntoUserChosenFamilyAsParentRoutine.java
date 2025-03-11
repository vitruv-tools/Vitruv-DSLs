package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
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
public class InsertExistingMemberIntoUserChosenFamilyAsParentRoutine extends AbstractRoutine {
  private InsertExistingMemberIntoUserChosenFamilyAsParentRoutine.InputValues inputValues;

  private InsertExistingMemberIntoUserChosenFamilyAsParentRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insuranceClient;

    public InputValues(final InsuranceClient insuranceClient) {
      this.insuranceClient = insuranceClient;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final FamilyRegister insuranceDatabase;

      public final Member correspondingMember;

      public RetrievedValues(final FamilyRegister insuranceDatabase, final Member correspondingMember) {
        this.insuranceDatabase = insuranceDatabase;
        this.correspondingMember = correspondingMember;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceInsuranceDatabase(final InsuranceClient insuranceClient) {
      InsuranceDatabase _insuranceDatabase = InsuranceToFamiliesHelper.getInsuranceDatabase(insuranceClient);
      return _insuranceDatabase;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final InsuranceClient insuranceClient, final FamilyRegister insuranceDatabase) {
      return insuranceClient;
    }

    public InsertExistingMemberIntoUserChosenFamilyAsParentRoutine.Match.RetrievedValues match(final InsuranceClient insuranceClient) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister insuranceDatabase = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceDatabase(insuranceClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceDatabase == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Member correspondingMember = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingMember(insuranceClient, insuranceDatabase), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingMember == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.InsertExistingMemberIntoUserChosenFamilyAsParentRoutine.Match.RetrievedValues(insuranceDatabase, correspondingMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insuranceClient, final FamilyRegister insuranceDatabase, final Member correspondingMember, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      final Iterable<Family> matchingFamilies = IterableExtensions.<Family>filter(IterableExtensions.<Family>filter(insuranceDatabase.getFamilies(), ((Function1<? super Family, Boolean>)InsuranceToFamiliesHelper.sameLastName(insuranceClient))), ((Function1<? super Family, Boolean>)InsuranceToFamiliesHelper.noParent(insuranceClient)));
      Family _xifexpression = null;
      boolean _isEmpty = IterableExtensions.isEmpty(matchingFamilies);
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = InsuranceToFamiliesHelper.askUserWhichFamilyToInsertTheMemberIn(this.executionState.getUserInteractor(), insuranceClient, matchingFamilies);
      }
      final Family chosenFamily = _xifexpression;
      if ((chosenFamily == null)) {
        _routinesFacade.insertExistingMemberIntoNewFamilyAsParent(insuranceClient);
      } else {
        _routinesFacade.insertExistingMemberIntoExistingFamilyAsParent(insuranceClient, correspondingMember, chosenFamily);
      }
    }
  }

  public InsertExistingMemberIntoUserChosenFamilyAsParentRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insuranceClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertExistingMemberIntoUserChosenFamilyAsParentRoutine.InputValues(insuranceClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertExistingMemberIntoUserChosenFamilyAsParentRoutine with input:");
    	getLogger().trace("   inputValues.insuranceClient: " + inputValues.insuranceClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.InsertExistingMemberIntoUserChosenFamilyAsParentRoutine.Match(getExecutionState()).match(inputValues.insuranceClient);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.InsertExistingMemberIntoUserChosenFamilyAsParentRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceClient, retrievedValues.insuranceDatabase, retrievedValues.correspondingMember, getRoutinesFacade());
    return true;
  }
}
