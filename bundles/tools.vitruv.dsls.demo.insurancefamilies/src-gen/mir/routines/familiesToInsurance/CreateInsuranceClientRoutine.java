package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.families2insurance.FamiliesToInsuranceHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateInsuranceClientRoutine extends AbstractRoutine {
  private CreateInsuranceClientRoutine.InputValues inputValues;

  private CreateInsuranceClientRoutine.Match.RetrievedValues retrievedValues;

  private CreateInsuranceClientRoutine.Create.CreatedValues createdValues;

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
      public final InsuranceDatabase insuranceDatabase;

      public RetrievedValues(final InsuranceDatabase insuranceDatabase) {
        this.insuranceDatabase = insuranceDatabase;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean checkMatcherPrecondition1(final Member newMember, final Family family, final Gender gender) {
      boolean _xblockexpression = false;
      {
        FamiliesToInsuranceHelper.assertValidFirstname(newMember);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceInsuranceDatabase(final Member newMember, final Family family, final Gender gender) {
      FamilyRegister _register = FamiliesUtil.getRegister(family);
      return _register;
    }

    public CreateInsuranceClientRoutine.Match.RetrievedValues match(final Member newMember, final Family family, final Gender gender) throws IOException {
      if (!checkMatcherPrecondition1(newMember, family, gender)) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase insuranceDatabase = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceDatabase(newMember, family, gender), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceDatabase == null) {
      	return null;
      }
      return new mir.routines.familiesToInsurance.CreateInsuranceClientRoutine.Match.RetrievedValues(insuranceDatabase);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final InsuranceClient insuranceClient;

      public CreatedValues(final InsuranceClient insuranceClient) {
        this.insuranceClient = insuranceClient;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateInsuranceClientRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient insuranceClient = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.insurance.impl.InsuranceFactoryImpl.eINSTANCE.createInsuranceClient();
      });
      return new CreateInsuranceClientRoutine.Create.CreatedValues(insuranceClient);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMember, final Family family, final Gender gender, final InsuranceDatabase insuranceDatabase, final InsuranceClient insuranceClient, @Extension final FamiliesToInsuranceRoutinesFacade _routinesFacade) {
      insuranceClient.setName(FamiliesToInsuranceHelper.getInsuranceClientName(newMember));
      insuranceClient.setGender(gender);
      EList<InsuranceClient> _insuranceclient = insuranceDatabase.getInsuranceclient();
      _insuranceclient.add(insuranceClient);
      this.addCorrespondenceBetween(newMember, insuranceClient);
      this.addCorrespondenceBetween(family, insuranceClient);
    }
  }

  public CreateInsuranceClientRoutine(final FamiliesToInsuranceRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMember, final Family family, final Gender gender) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateInsuranceClientRoutine.InputValues(newMember, family, gender);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateInsuranceClientRoutine with input:");
    	getLogger().trace("   inputValues.newMember: " + inputValues.newMember);
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    	getLogger().trace("   inputValues.gender: " + inputValues.gender);
    }
    retrievedValues = new mir.routines.familiesToInsurance.CreateInsuranceClientRoutine.Match(getExecutionState()).match(inputValues.newMember, inputValues.family, inputValues.gender);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.familiesToInsurance.CreateInsuranceClientRoutine.Create(getExecutionState()).createElements();
    new mir.routines.familiesToInsurance.CreateInsuranceClientRoutine.Update(getExecutionState()).updateModels(inputValues.newMember, inputValues.family, inputValues.gender, retrievedValues.insuranceDatabase, createdValues.insuranceClient, getRoutinesFacade());
    return true;
  }
}
