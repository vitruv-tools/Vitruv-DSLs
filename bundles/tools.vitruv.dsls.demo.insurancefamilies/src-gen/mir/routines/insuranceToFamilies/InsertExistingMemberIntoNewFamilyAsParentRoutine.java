package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class InsertExistingMemberIntoNewFamilyAsParentRoutine extends AbstractRoutine {
  private InsertExistingMemberIntoNewFamilyAsParentRoutine.InputValues inputValues;

  private InsertExistingMemberIntoNewFamilyAsParentRoutine.Match.RetrievedValues retrievedValues;

  private InsertExistingMemberIntoNewFamilyAsParentRoutine.Create.CreatedValues createdValues;

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

    public InsertExistingMemberIntoNewFamilyAsParentRoutine.Match.RetrievedValues match(final InsuranceClient insuranceClient) throws IOException {
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
      return new mir.routines.insuranceToFamilies.InsertExistingMemberIntoNewFamilyAsParentRoutine.Match.RetrievedValues(insuranceDatabase, correspondingMember);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Family familyToInsertInto;

      public CreatedValues(final Family familyToInsertInto) {
        this.familyToInsertInto = familyToInsertInto;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public InsertExistingMemberIntoNewFamilyAsParentRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.families.Family familyToInsertInto = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.families.impl.FamiliesFactoryImpl.eINSTANCE.createFamily();
      });
      return new InsertExistingMemberIntoNewFamilyAsParentRoutine.Create.CreatedValues(familyToInsertInto);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insuranceClient, final FamilyRegister insuranceDatabase, final Member correspondingMember, final Family familyToInsertInto, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      familyToInsertInto.setLastName(InsuranceToFamiliesHelper.getLastName(insuranceClient));
      EList<Family> _families = insuranceDatabase.getFamilies();
      _families.add(familyToInsertInto);
      _routinesFacade.insertExistingMemberIntoExistingFamilyAsParent(insuranceClient, correspondingMember, familyToInsertInto);
    }
  }

  public InsertExistingMemberIntoNewFamilyAsParentRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insuranceClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertExistingMemberIntoNewFamilyAsParentRoutine.InputValues(insuranceClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertExistingMemberIntoNewFamilyAsParentRoutine with input:");
    	getLogger().trace("   inputValues.insuranceClient: " + inputValues.insuranceClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.InsertExistingMemberIntoNewFamilyAsParentRoutine.Match(getExecutionState()).match(inputValues.insuranceClient);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.insuranceToFamilies.InsertExistingMemberIntoNewFamilyAsParentRoutine.Create(getExecutionState()).createElements();
    new mir.routines.insuranceToFamilies.InsertExistingMemberIntoNewFamilyAsParentRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceClient, retrievedValues.insuranceDatabase, retrievedValues.correspondingMember, createdValues.familyToInsertInto, getRoutinesFacade());
    return true;
  }
}
