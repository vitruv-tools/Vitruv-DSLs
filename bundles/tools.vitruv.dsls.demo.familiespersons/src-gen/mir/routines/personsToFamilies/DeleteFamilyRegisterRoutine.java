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
public class DeleteFamilyRegisterRoutine extends AbstractRoutine {
  private DeleteFamilyRegisterRoutine.InputValues inputValues;

  private DeleteFamilyRegisterRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final PersonRegister deletedPersonsRegister;

    public InputValues(final PersonRegister deletedPersonsRegister) {
      this.deletedPersonsRegister = deletedPersonsRegister;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final FamilyRegister familyRegister;

      public RetrievedValues(final FamilyRegister familyRegister) {
        this.familyRegister = familyRegister;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceFamilyRegister(final PersonRegister deletedPersonsRegister) {
      return deletedPersonsRegister;
    }

    public DeleteFamilyRegisterRoutine.Match.RetrievedValues match(final PersonRegister deletedPersonsRegister) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister familyRegister = getCorrespondingElement(
      	getCorrepondenceSourceFamilyRegister(deletedPersonsRegister), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (familyRegister == null) {
      	return null;
      }
      return new mir.routines.personsToFamilies.DeleteFamilyRegisterRoutine.Match.RetrievedValues(familyRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final PersonRegister deletedPersonsRegister, final FamilyRegister familyRegister, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      this.removeObject(familyRegister);
      this.removeCorrespondenceBetween(familyRegister, deletedPersonsRegister);
    }
  }

  public DeleteFamilyRegisterRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final PersonRegister deletedPersonsRegister) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteFamilyRegisterRoutine.InputValues(deletedPersonsRegister);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteFamilyRegisterRoutine with input:");
    	getLogger().trace("   inputValues.deletedPersonsRegister: " + inputValues.deletedPersonsRegister);
    }
    retrievedValues = new mir.routines.personsToFamilies.DeleteFamilyRegisterRoutine.Match(getExecutionState()).match(inputValues.deletedPersonsRegister);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.DeleteFamilyRegisterRoutine.Update(getExecutionState()).updateModels(inputValues.deletedPersonsRegister, retrievedValues.familyRegister, getRoutinesFacade());
    return true;
  }
}
