package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.familiespersons.persons2families.FamilyRole;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class InsertAsParentOrChildRoutine extends AbstractRoutine {
  private InsertAsParentOrChildRoutine.InputValues inputValues;

  private InsertAsParentOrChildRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Person insertedPerson;

    public InputValues(final Person insertedPerson) {
      this.insertedPerson = insertedPerson;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public RetrievedValues() {
        
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSource1(final Person insertedPerson) {
      return insertedPerson;
    }

    public boolean checkMatcherPrecondition1(final Person insertedPerson) {
      boolean _xblockexpression = false;
      {
        PersonsToFamiliesHelper.assertValidFullname(insertedPerson);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public InsertAsParentOrChildRoutine.Match.RetrievedValues match(final Person insertedPerson) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(insertedPerson), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      if (!checkMatcherPrecondition1(insertedPerson)) {
      	return null;
      }
      return new mir.routines.personsToFamilies.InsertAsParentOrChildRoutine.Match.RetrievedValues();
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person insertedPerson, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      final FamilyRole role = PersonsToFamiliesHelper.askUserWhetherPersonIsParentOrChild(this.executionState.getUserInteractor(), insertedPerson);
      if (role != null) {
        switch (role) {
          case Child:
            _routinesFacade.createChild(insertedPerson);
            break;
          case Parent:
            _routinesFacade.createParent(insertedPerson);
            break;
          default:
            break;
        }
      }
    }
  }

  public InsertAsParentOrChildRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person insertedPerson) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertAsParentOrChildRoutine.InputValues(insertedPerson);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertAsParentOrChildRoutine with input:");
    	getLogger().trace("   inputValues.insertedPerson: " + inputValues.insertedPerson);
    }
    retrievedValues = new mir.routines.personsToFamilies.InsertAsParentOrChildRoutine.Match(getExecutionState()).match(inputValues.insertedPerson);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.InsertAsParentOrChildRoutine.Update(getExecutionState()).updateModels(inputValues.insertedPerson, getRoutinesFacade());
    return true;
  }
}
