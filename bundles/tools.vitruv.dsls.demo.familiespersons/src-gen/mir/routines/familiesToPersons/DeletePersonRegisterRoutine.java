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
public class DeletePersonRegisterRoutine extends AbstractRoutine {
  private DeletePersonRegisterRoutine.InputValues inputValues;

  private DeletePersonRegisterRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final FamilyRegister familyRegister;

    public InputValues(final FamilyRegister familyRegister) {
      this.familyRegister = familyRegister;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final PersonRegister personRegister;

      public RetrievedValues(final PersonRegister personRegister) {
        this.personRegister = personRegister;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourcePersonRegister(final FamilyRegister familyRegister) {
      return familyRegister;
    }

    public DeletePersonRegisterRoutine.Match.RetrievedValues match(final FamilyRegister familyRegister) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.PersonRegister personRegister = getCorrespondingElement(
      	getCorrepondenceSourcePersonRegister(familyRegister), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.PersonRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (personRegister == null) {
      	return null;
      }
      return new mir.routines.familiesToPersons.DeletePersonRegisterRoutine.Match.RetrievedValues(personRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final FamilyRegister familyRegister, final PersonRegister personRegister, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      this.removeObject(personRegister);
      this.removeCorrespondenceBetween(personRegister, familyRegister);
    }
  }

  public DeletePersonRegisterRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final FamilyRegister familyRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeletePersonRegisterRoutine.InputValues(familyRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeletePersonRegisterRoutine with input:");
    	getLogger().trace("   inputValues.familyRegister: " + inputValues.familyRegister);
    }
    retrievedValues = new mir.routines.familiesToPersons.DeletePersonRegisterRoutine.Match(getExecutionState()).match(inputValues.familyRegister);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.DeletePersonRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.familyRegister, retrievedValues.personRegister, getRoutinesFacade());
    return true;
  }
}
