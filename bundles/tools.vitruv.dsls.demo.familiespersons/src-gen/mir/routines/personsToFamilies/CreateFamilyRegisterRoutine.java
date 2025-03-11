package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateFamilyRegisterRoutine extends AbstractRoutine {
  private CreateFamilyRegisterRoutine.InputValues inputValues;

  private CreateFamilyRegisterRoutine.Match.RetrievedValues retrievedValues;

  private CreateFamilyRegisterRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final PersonRegister createdPersonRegister;

    public InputValues(final PersonRegister createdPersonRegister) {
      this.createdPersonRegister = createdPersonRegister;
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

    public EObject getCorrepondenceSource1(final PersonRegister createdPersonRegister) {
      return createdPersonRegister;
    }

    public CreateFamilyRegisterRoutine.Match.RetrievedValues match(final PersonRegister createdPersonRegister) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(createdPersonRegister), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.personsToFamilies.CreateFamilyRegisterRoutine.Match.RetrievedValues();
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final FamilyRegister newFamilyRegister;

      public CreatedValues(final FamilyRegister newFamilyRegister) {
        this.newFamilyRegister = newFamilyRegister;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateFamilyRegisterRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister newFamilyRegister = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.families.impl.FamiliesFactoryImpl.eINSTANCE.createFamilyRegister();
      });
      return new CreateFamilyRegisterRoutine.Create.CreatedValues(newFamilyRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final PersonRegister createdPersonRegister, final FamilyRegister newFamilyRegister, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      this.persistProjectRelative(createdPersonRegister, newFamilyRegister, "model/families.families");
      this.addCorrespondenceBetween(newFamilyRegister, createdPersonRegister);
    }
  }

  public CreateFamilyRegisterRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final PersonRegister createdPersonRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateFamilyRegisterRoutine.InputValues(createdPersonRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateFamilyRegisterRoutine with input:");
    	getLogger().trace("   inputValues.createdPersonRegister: " + inputValues.createdPersonRegister);
    }
    retrievedValues = new mir.routines.personsToFamilies.CreateFamilyRegisterRoutine.Match(getExecutionState()).match(inputValues.createdPersonRegister);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.personsToFamilies.CreateFamilyRegisterRoutine.Create(getExecutionState()).createElements();
    new mir.routines.personsToFamilies.CreateFamilyRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.createdPersonRegister, createdValues.newFamilyRegister, getRoutinesFacade());
    return true;
  }
}
