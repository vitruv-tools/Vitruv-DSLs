package mir.routines.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.familiespersons.families2persons.FamiliesToPersonsHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdatePersonNameRoutine extends AbstractRoutine {
  private UpdatePersonNameRoutine.InputValues inputValues;

  private UpdatePersonNameRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Member member;

    public InputValues(final Member member) {
      this.member = member;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Person person;

      public RetrievedValues(final Person person) {
        this.person = person;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean checkMatcherPrecondition1(final Member member) {
      boolean _xblockexpression = false;
      {
        FamiliesToPersonsHelper.assertValidFirstname(member);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourcePerson(final Member member) {
      return member;
    }

    public UpdatePersonNameRoutine.Match.RetrievedValues match(final Member member) throws IOException {
      if (!checkMatcherPrecondition1(member)) {
      	return null;
      }
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
      return new mir.routines.familiesToPersons.UpdatePersonNameRoutine.Match.RetrievedValues(person);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Member member, final Person person, @Extension final FamiliesToPersonsRoutinesFacade _routinesFacade) {
      person.setFullName(FamiliesToPersonsHelper.getPersonName(member));
    }
  }

  public UpdatePersonNameRoutine(final FamiliesToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Member member) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdatePersonNameRoutine.InputValues(member);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdatePersonNameRoutine with input:");
    	getLogger().trace("   inputValues.member: " + inputValues.member);
    }
    retrievedValues = new mir.routines.familiesToPersons.UpdatePersonNameRoutine.Match(getExecutionState()).match(inputValues.member);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.familiesToPersons.UpdatePersonNameRoutine.Update(getExecutionState()).updateModels(inputValues.member, retrievedValues.person, getRoutinesFacade());
    return true;
  }
}
