package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateParentRoutine extends AbstractRoutine {
  private CreateParentRoutine.InputValues inputValues;

  private CreateParentRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Person insertedPerson;

    public InputValues(final Person insertedPerson) {
      this.insertedPerson = insertedPerson;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final FamilyRegister familiesRegister;

      public RetrievedValues(final FamilyRegister familiesRegister) {
        this.familiesRegister = familiesRegister;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceFamiliesRegister(final Person insertedPerson) {
      PersonRegister _register = PersonsToFamiliesHelper.getRegister(insertedPerson);
      return _register;
    }

    public CreateParentRoutine.Match.RetrievedValues match(final Person insertedPerson) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister familiesRegister = getCorrespondingElement(
      	getCorrepondenceSourceFamiliesRegister(insertedPerson), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (familiesRegister == null) {
      	return null;
      }
      return new mir.routines.personsToFamilies.CreateParentRoutine.Match.RetrievedValues(familiesRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person insertedPerson, final FamilyRegister familiesRegister, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      final Iterable<Family> matchingFamilies = IterableExtensions.<Family>filter(IterableExtensions.<Family>filter(familiesRegister.getFamilies(), ((Function1<? super Family, Boolean>)PersonsToFamiliesHelper.sameLastname(insertedPerson))), 
        ((Function1<? super Family, Boolean>)PersonsToFamiliesHelper.noParent(insertedPerson)));
      Family _xifexpression = null;
      boolean _isEmpty = IterableExtensions.isEmpty(matchingFamilies);
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = PersonsToFamiliesHelper.askUserWhichFamilyToInsertTheMemberIn(
          this.executionState.getUserInteractor(), insertedPerson, matchingFamilies);
      }
      final Family familyToInsertInto = _xifexpression;
      if ((familyToInsertInto == null)) {
        _routinesFacade.createParentInNewFamily(insertedPerson);
      } else {
        _routinesFacade.createParentInExistingFamily(insertedPerson, familyToInsertInto);
      }
    }
  }

  public CreateParentRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person insertedPerson) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateParentRoutine.InputValues(insertedPerson);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateParentRoutine with input:");
    	getLogger().trace("   inputValues.insertedPerson: " + inputValues.insertedPerson);
    }
    retrievedValues = new mir.routines.personsToFamilies.CreateParentRoutine.Match(getExecutionState()).match(inputValues.insertedPerson);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.CreateParentRoutine.Update(getExecutionState()).updateModels(inputValues.insertedPerson, retrievedValues.familiesRegister, getRoutinesFacade());
    return true;
  }
}
