package mir.routines.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreatePersonRegisterRoutine extends AbstractRoutine {
  private CreatePersonRegisterRoutine.InputValues inputValues;

  private CreatePersonRegisterRoutine.Match.RetrievedValues retrievedValues;

  private CreatePersonRegisterRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final FamilyRegister familyRegister;

    public InputValues(final FamilyRegister familyRegister) {
      this.familyRegister = familyRegister;
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

    public EObject getCorrepondenceSource1(final FamilyRegister familyRegister) {
      return familyRegister;
    }

    public CreatePersonRegisterRoutine.Match.RetrievedValues match(final FamilyRegister familyRegister) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(familyRegister), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.PersonRegister.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.familiesToPersons.CreatePersonRegisterRoutine.Match.RetrievedValues();
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final PersonRegister personRegister;

      public CreatedValues(final PersonRegister personRegister) {
        this.personRegister = personRegister;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreatePersonRegisterRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.persons.PersonRegister personRegister = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.persons.impl.PersonsFactoryImpl.eINSTANCE.createPersonRegister();
      });
      return new CreatePersonRegisterRoutine.Create.CreatedValues(personRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final FamilyRegister familyRegister, final PersonRegister personRegister, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      this.persistProjectRelative(familyRegister, personRegister, "model/persons.persons");
      this.addCorrespondenceBetween(personRegister, familyRegister);
    }
  }

  public CreatePersonRegisterRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final FamilyRegister familyRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreatePersonRegisterRoutine.InputValues(familyRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreatePersonRegisterRoutine with input:");
    	getLogger().trace("   inputValues.familyRegister: " + inputValues.familyRegister);
    }
    retrievedValues = new mir.routines.familiesToPersons.CreatePersonRegisterRoutine.Match(getExecutionState()).match(inputValues.familyRegister);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.familiesToPersons.CreatePersonRegisterRoutine.Create(getExecutionState()).createElements();
    new mir.routines.familiesToPersons.CreatePersonRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.familyRegister, createdValues.personRegister, getRoutinesFacade());
    return true;
  }
}
