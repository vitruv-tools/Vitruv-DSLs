package mir.routines.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class DeletePersonRoutine extends AbstractRoutine {
  private DeletePersonRoutine.InputValues inputValues;

  private DeletePersonRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member member;

    public InputValues(final Member member) {
      this.member = member;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Person person;

      public final Family family;

      public RetrievedValues(final Person person, final Family family) {
        this.person = person;
        this.family = family;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourcePerson(final Member member) {
      return member;
    }

    public EObject getCorrepondenceSourceFamily(final Member member, final Person person) {
      return person;
    }

    public DeletePersonRoutine.Match.RetrievedValues match(final Member member) throws IOException {
      edu.kit.ipd.sdq.metamodels.persons.Person person = getCorrespondingElement(
      	getCorrepondenceSourcePerson(member), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.persons.Person.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (person == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family family = getCorrespondingElement(
      	getCorrepondenceSourceFamily(member, person), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (family == null) {
      	return null;
      }
      return new mir.routines.familiesToPersons.DeletePersonRoutine.Match.RetrievedValues(person, family);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member member, final Person person, final Family family, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      this.removeObject(person);
      this.removeCorrespondenceBetween(member, person);
      this.removeCorrespondenceBetween(family, person);
    }
  }

  public DeletePersonRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member member) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeletePersonRoutine.InputValues(member);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeletePersonRoutine with input:");
    	getLogger().trace("   inputValues.member: " + inputValues.member);
    }
    retrievedValues = new mir.routines.familiesToPersons.DeletePersonRoutine.Match(getExecutionState()).match(inputValues.member);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.DeletePersonRoutine.Update(getExecutionState()).updateModels(inputValues.member, retrievedValues.person, retrievedValues.family, getRoutinesFacade());
    return true;
  }
}
