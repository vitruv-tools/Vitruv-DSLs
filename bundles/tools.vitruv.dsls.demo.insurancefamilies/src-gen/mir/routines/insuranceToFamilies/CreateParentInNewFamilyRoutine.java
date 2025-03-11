package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
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
public class CreateParentInNewFamilyRoutine extends AbstractRoutine {
  private CreateParentInNewFamilyRoutine.InputValues inputValues;

  private CreateParentInNewFamilyRoutine.Match.RetrievedValues retrievedValues;

  private CreateParentInNewFamilyRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final InsuranceClient insertedClient;

    public InputValues(final InsuranceClient insertedClient) {
      this.insertedClient = insertedClient;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final FamilyRegister insuranceDatabase;

      public RetrievedValues(final FamilyRegister insuranceDatabase) {
        this.insuranceDatabase = insuranceDatabase;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceInsuranceDatabase(final InsuranceClient insertedClient) {
      InsuranceDatabase _insuranceDatabase = InsuranceToFamiliesHelper.getInsuranceDatabase(insertedClient);
      return _insuranceDatabase;
    }

    public CreateParentInNewFamilyRoutine.Match.RetrievedValues match(final InsuranceClient insertedClient) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister insuranceDatabase = getCorrespondingElement(
      	getCorrepondenceSourceInsuranceDatabase(insertedClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (insuranceDatabase == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.CreateParentInNewFamilyRoutine.Match.RetrievedValues(insuranceDatabase);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Family newFamily;

      public CreatedValues(final Family newFamily) {
        this.newFamily = newFamily;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateParentInNewFamilyRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.families.Family newFamily = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.families.impl.FamiliesFactoryImpl.eINSTANCE.createFamily();
      });
      return new CreateParentInNewFamilyRoutine.Create.CreatedValues(newFamily);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insertedClient, final FamilyRegister insuranceDatabase, final Family newFamily, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      newFamily.setLastName(InsuranceToFamiliesHelper.getLastName(insertedClient));
      EList<Family> _families = insuranceDatabase.getFamilies();
      _families.add(newFamily);
      _routinesFacade.createParentInExistingFamily(insertedClient, newFamily);
    }
  }

  public CreateParentInNewFamilyRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insertedClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateParentInNewFamilyRoutine.InputValues(insertedClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateParentInNewFamilyRoutine with input:");
    	getLogger().trace("   inputValues.insertedClient: " + inputValues.insertedClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.CreateParentInNewFamilyRoutine.Match(getExecutionState()).match(inputValues.insertedClient);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.insuranceToFamilies.CreateParentInNewFamilyRoutine.Create(getExecutionState()).createElements();
    new mir.routines.insuranceToFamilies.CreateParentInNewFamilyRoutine.Update(getExecutionState()).updateModels(inputValues.insertedClient, retrievedValues.insuranceDatabase, createdValues.newFamily, getRoutinesFacade());
    return true;
  }
}
