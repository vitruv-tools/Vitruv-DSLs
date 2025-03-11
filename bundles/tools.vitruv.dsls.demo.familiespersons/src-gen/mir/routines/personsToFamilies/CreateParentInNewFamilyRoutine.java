package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
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
public class CreateParentInNewFamilyRoutine extends AbstractRoutine {
  private CreateParentInNewFamilyRoutine.InputValues inputValues;

  private CreateParentInNewFamilyRoutine.Match.RetrievedValues retrievedValues;

  private CreateParentInNewFamilyRoutine.Create.CreatedValues createdValues;

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

    public CreateParentInNewFamilyRoutine.Match.RetrievedValues match(final Person insertedPerson) throws IOException {
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
      return new mir.routines.personsToFamilies.CreateParentInNewFamilyRoutine.Match.RetrievedValues(familiesRegister);
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

    public void updateModels(final Person insertedPerson, final FamilyRegister familiesRegister, final Family newFamily, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      newFamily.setLastName(PersonsToFamiliesHelper.getLastname(insertedPerson));
      EList<Family> _families = familiesRegister.getFamilies();
      _families.add(newFamily);
      _routinesFacade.createParentInExistingFamily(insertedPerson, newFamily);
    }
  }

  public CreateParentInNewFamilyRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person insertedPerson) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateParentInNewFamilyRoutine.InputValues(insertedPerson);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateParentInNewFamilyRoutine with input:");
    	getLogger().trace("   inputValues.insertedPerson: " + inputValues.insertedPerson);
    }
    retrievedValues = new mir.routines.personsToFamilies.CreateParentInNewFamilyRoutine.Match(getExecutionState()).match(inputValues.insertedPerson);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.personsToFamilies.CreateParentInNewFamilyRoutine.Create(getExecutionState()).createElements();
    new mir.routines.personsToFamilies.CreateParentInNewFamilyRoutine.Update(getExecutionState()).updateModels(inputValues.insertedPerson, retrievedValues.familiesRegister, createdValues.newFamily, getRoutinesFacade());
    return true;
  }
}
