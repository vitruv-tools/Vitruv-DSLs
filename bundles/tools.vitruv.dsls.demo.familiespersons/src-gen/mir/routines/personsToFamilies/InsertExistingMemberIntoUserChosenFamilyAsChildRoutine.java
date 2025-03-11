package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
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
public class InsertExistingMemberIntoUserChosenFamilyAsChildRoutine extends AbstractRoutine {
  private InsertExistingMemberIntoUserChosenFamilyAsChildRoutine.InputValues inputValues;

  private InsertExistingMemberIntoUserChosenFamilyAsChildRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Person renamedPerson;

    public InputValues(final Person renamedPerson) {
      this.renamedPerson = renamedPerson;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final FamilyRegister familiesRegister;

      public final Member correspondingMember;

      public RetrievedValues(final FamilyRegister familiesRegister, final Member correspondingMember) {
        this.familiesRegister = familiesRegister;
        this.correspondingMember = correspondingMember;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceFamiliesRegister(final Person renamedPerson) {
      PersonRegister _register = PersonsToFamiliesHelper.getRegister(renamedPerson);
      return _register;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final Person renamedPerson, final FamilyRegister familiesRegister) {
      return renamedPerson;
    }

    public InsertExistingMemberIntoUserChosenFamilyAsChildRoutine.Match.RetrievedValues match(final Person renamedPerson) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister familiesRegister = getCorrespondingElement(
      	getCorrepondenceSourceFamiliesRegister(renamedPerson), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (familiesRegister == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Member correspondingMember = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingMember(renamedPerson, familiesRegister), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingMember == null) {
      	return null;
      }
      return new mir.routines.personsToFamilies.InsertExistingMemberIntoUserChosenFamilyAsChildRoutine.Match.RetrievedValues(familiesRegister, correspondingMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person renamedPerson, final FamilyRegister familiesRegister, final Member correspondingMember, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      final Iterable<Family> matchingFamilies = IterableExtensions.<Family>filter(familiesRegister.getFamilies(), ((Function1<? super Family, Boolean>)PersonsToFamiliesHelper.sameLastname(renamedPerson)));
      Family _xifexpression = null;
      boolean _isEmpty = IterableExtensions.isEmpty(matchingFamilies);
      if (_isEmpty) {
        _xifexpression = null;
      } else {
        _xifexpression = PersonsToFamiliesHelper.askUserWhichFamilyToInsertTheMemberIn(
          this.executionState.getUserInteractor(), renamedPerson, matchingFamilies);
      }
      final Family chosenFamily = _xifexpression;
      if ((chosenFamily == null)) {
        _routinesFacade.insertExistingMemberIntoNewFamilyAsChild(renamedPerson);
      } else {
        _routinesFacade.insertExistingMemberIntoExistingFamilyAsChild(renamedPerson, chosenFamily);
      }
    }
  }

  public InsertExistingMemberIntoUserChosenFamilyAsChildRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person renamedPerson) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertExistingMemberIntoUserChosenFamilyAsChildRoutine.InputValues(renamedPerson);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertExistingMemberIntoUserChosenFamilyAsChildRoutine with input:");
    	getLogger().trace("   inputValues.renamedPerson: " + inputValues.renamedPerson);
    }
    retrievedValues = new mir.routines.personsToFamilies.InsertExistingMemberIntoUserChosenFamilyAsChildRoutine.Match(getExecutionState()).match(inputValues.renamedPerson);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.InsertExistingMemberIntoUserChosenFamilyAsChildRoutine.Update(getExecutionState()).updateModels(inputValues.renamedPerson, retrievedValues.familiesRegister, retrievedValues.correspondingMember, getRoutinesFacade());
    return true;
  }
}
