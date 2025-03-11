package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class InsertExistingMemberIntoNewFamilyAsChildRoutine extends AbstractRoutine {
  private InsertExistingMemberIntoNewFamilyAsChildRoutine.InputValues inputValues;

  private InsertExistingMemberIntoNewFamilyAsChildRoutine.Match.RetrievedValues retrievedValues;

  private InsertExistingMemberIntoNewFamilyAsChildRoutine.Create.CreatedValues createdValues;

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

    public InsertExistingMemberIntoNewFamilyAsChildRoutine.Match.RetrievedValues match(final Person renamedPerson) throws IOException {
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
      return new mir.routines.personsToFamilies.InsertExistingMemberIntoNewFamilyAsChildRoutine.Match.RetrievedValues(familiesRegister, correspondingMember);
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

    public InsertExistingMemberIntoNewFamilyAsChildRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.families.Family familyToInsertInto = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.families.impl.FamiliesFactoryImpl.eINSTANCE.createFamily();
      });
      return new InsertExistingMemberIntoNewFamilyAsChildRoutine.Create.CreatedValues(familyToInsertInto);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person renamedPerson, final FamilyRegister familiesRegister, final Member correspondingMember, final Family familyToInsertInto, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      familyToInsertInto.setLastName(PersonsToFamiliesHelper.getLastname(renamedPerson));
      EList<Family> _families = familiesRegister.getFamilies();
      _families.add(familyToInsertInto);
      _routinesFacade.insertExistingMemberIntoExistingFamilyAsChild(renamedPerson, familyToInsertInto);
    }
  }

  public InsertExistingMemberIntoNewFamilyAsChildRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person renamedPerson) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertExistingMemberIntoNewFamilyAsChildRoutine.InputValues(renamedPerson);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertExistingMemberIntoNewFamilyAsChildRoutine with input:");
    	getLogger().trace("   inputValues.renamedPerson: " + inputValues.renamedPerson);
    }
    retrievedValues = new mir.routines.personsToFamilies.InsertExistingMemberIntoNewFamilyAsChildRoutine.Match(getExecutionState()).match(inputValues.renamedPerson);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.personsToFamilies.InsertExistingMemberIntoNewFamilyAsChildRoutine.Create(getExecutionState()).createElements();
    new mir.routines.personsToFamilies.InsertExistingMemberIntoNewFamilyAsChildRoutine.Update(getExecutionState()).updateModels(inputValues.renamedPerson, retrievedValues.familiesRegister, retrievedValues.correspondingMember, createdValues.familyToInsertInto, getRoutinesFacade());
    return true;
  }
}
