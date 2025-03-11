package mir.routines.personsToFamilies;

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
public class DeleteMemberRoutine extends AbstractRoutine {
  private DeleteMemberRoutine.InputValues inputValues;

  private DeleteMemberRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Person person;

    public InputValues(final Person person) {
      this.person = person;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Member member;

      public final Family family;

      public RetrievedValues(final Member member, final Family family) {
        this.member = member;
        this.family = family;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceMember(final Person person) {
      return person;
    }

    public EObject getCorrepondenceSourceFamily(final Person person, final Member member) {
      return person;
    }

    public DeleteMemberRoutine.Match.RetrievedValues match(final Person person) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.Member member = getCorrespondingElement(
      	getCorrepondenceSourceMember(person), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (member == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family family = getCorrespondingElement(
      	getCorrepondenceSourceFamily(person, member), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (family == null) {
      	return null;
      }
      return new mir.routines.personsToFamilies.DeleteMemberRoutine.Match.RetrievedValues(member, family);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person person, final Member member, final Family family, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      this.removeObject(member);
      this.removeCorrespondenceBetween(person, family);
      this.removeCorrespondenceBetween(person, member);
      _routinesFacade.deleteFamilyIfEmpty(family);
    }
  }

  public DeleteMemberRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person person) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new DeleteMemberRoutine.InputValues(person);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine DeleteMemberRoutine with input:");
    	getLogger().trace("   inputValues.person: " + inputValues.person);
    }
    retrievedValues = new mir.routines.personsToFamilies.DeleteMemberRoutine.Match(getExecutionState()).match(inputValues.person);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.DeleteMemberRoutine.Update(getExecutionState()).updateModels(inputValues.person, retrievedValues.member, retrievedValues.family, getRoutinesFacade());
    return true;
  }
}
