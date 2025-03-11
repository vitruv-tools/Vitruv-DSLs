package mir.routines.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Male;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.familiespersons.families2persons.FamiliesToPersonsHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateMaleRoutine extends AbstractRoutine {
  private CreateMaleRoutine.InputValues inputValues;

  private CreateMaleRoutine.Match.RetrievedValues retrievedValues;

  private CreateMaleRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final Member newMember;

    public final Family family;

    public InputValues(final Member newMember, final Family family) {
      this.newMember = newMember;
      this.family = family;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final PersonRegister personsRegister;

      public RetrievedValues(final PersonRegister personsRegister) {
        this.personsRegister = personsRegister;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean checkMatcherPrecondition1(final Member newMember, final Family family) {
      boolean _xblockexpression = false;
      {
        FamiliesToPersonsHelper.assertValidFirstname(newMember);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourcePersonsRegister(final Member newMember, final Family family) {
      FamilyRegister _register = FamiliesUtil.getRegister(family);
      return _register;
    }

    public CreateMaleRoutine.Match.RetrievedValues match(final Member newMember, final Family family) throws IOException {
      if (!checkMatcherPrecondition1(newMember, family)) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.persons.PersonRegister personsRegister = getCorrespondingElement(
      	getCorrepondenceSourcePersonsRegister(newMember, family), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.PersonRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (personsRegister == null) {
      	return null;
      }
      return new mir.routines.familiesToPersons.CreateMaleRoutine.Match.RetrievedValues(personsRegister);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Male person;

      public CreatedValues(final Male person) {
        this.person = person;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateMaleRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.persons.Male person = createObject(() -> {
      	return edu.kit.ipd.sdq.metamodels.persons.impl.PersonsFactoryImpl.eINSTANCE.createMale();
      });
      return new CreateMaleRoutine.Create.CreatedValues(person);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member newMember, final Family family, final PersonRegister personsRegister, final Male person, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      person.setFullName(FamiliesToPersonsHelper.getPersonName(newMember));
      EList<Person> _persons = personsRegister.getPersons();
      _persons.add(person);
      this.addCorrespondenceBetween(newMember, person);
      this.addCorrespondenceBetween(family, person);
    }
  }

  public CreateMaleRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member newMember, final Family family) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateMaleRoutine.InputValues(newMember, family);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateMaleRoutine with input:");
    	getLogger().trace("   inputValues.newMember: " + inputValues.newMember);
    	getLogger().trace("   inputValues.family: " + inputValues.family);
    }
    retrievedValues = new mir.routines.familiesToPersons.CreateMaleRoutine.Match(getExecutionState()).match(inputValues.newMember, inputValues.family);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.familiesToPersons.CreateMaleRoutine.Create(getExecutionState()).createElements();
    new mir.routines.familiesToPersons.CreateMaleRoutine.Update(getExecutionState()).updateModels(inputValues.newMember, inputValues.family, retrievedValues.personsRegister, createdValues.person, getRoutinesFacade());
    return true;
  }
}
