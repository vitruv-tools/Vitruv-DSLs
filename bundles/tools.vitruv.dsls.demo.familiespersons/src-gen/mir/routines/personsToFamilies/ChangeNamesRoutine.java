package mir.routines.personsToFamilies;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

/**
 *  Apply firstname changes to the corresponding {@link Member}nApply lastname changes to the corresponding {@link Family}
 *  
 */
@SuppressWarnings("all")
public class ChangeNamesRoutine extends AbstractRoutine {
  private ChangeNamesRoutine.InputValues inputValues;

  private ChangeNamesRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Person renamedPerson;

    public final String oldFullname;

    public InputValues(final Person renamedPerson, final String oldFullname) {
      this.renamedPerson = renamedPerson;
      this.oldFullname = oldFullname;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Family oldFamily;

      public final Member correspondingMember;

      public RetrievedValues(final Family oldFamily, final Member correspondingMember) {
        this.oldFamily = oldFamily;
        this.correspondingMember = correspondingMember;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public boolean checkMatcherPrecondition1(final Person renamedPerson, final String oldFullname) {
      boolean _xblockexpression = false;
      {
        PersonsToFamiliesHelper.assertValidFullname(renamedPerson);
        _xblockexpression = true;
      }
      return _xblockexpression;
    }

    public EObject getCorrepondenceSourceOldFamily(final Person renamedPerson, final String oldFullname) {
      return renamedPerson;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final Person renamedPerson, final String oldFullname, final Family oldFamily) {
      return renamedPerson;
    }

    public ChangeNamesRoutine.Match.RetrievedValues match(final Person renamedPerson, final String oldFullname) throws IOException {
      if (!checkMatcherPrecondition1(renamedPerson, oldFullname)) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(renamedPerson, oldFullname), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Member correspondingMember = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingMember(renamedPerson, oldFullname, oldFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingMember == null) {
      	return null;
      }
      return new mir.routines.personsToFamilies.ChangeNamesRoutine.Match.RetrievedValues(oldFamily, correspondingMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person renamedPerson, final String oldFullname, final Family oldFamily, final Member correspondingMember, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      correspondingMember.setFirstName(PersonsToFamiliesHelper.getFirstname(renamedPerson));
      String _lastName = FamiliesUtil.getFamily(correspondingMember).getLastName();
      String _lastname = PersonsToFamiliesHelper.getLastname(renamedPerson);
      boolean _notEquals = (!Objects.equal(_lastName, _lastname));
      if (_notEquals) {
        _routinesFacade.reactToLastnameAndFamilyRoleChanges(renamedPerson, oldFullname);
      }
    }
  }

  public ChangeNamesRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person renamedPerson, final String oldFullname) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ChangeNamesRoutine.InputValues(renamedPerson, oldFullname);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ChangeNamesRoutine with input:");
    	getLogger().trace("   inputValues.renamedPerson: " + inputValues.renamedPerson);
    	getLogger().trace("   inputValues.oldFullname: " + inputValues.oldFullname);
    }
    retrievedValues = new mir.routines.personsToFamilies.ChangeNamesRoutine.Match(getExecutionState()).match(inputValues.renamedPerson, inputValues.oldFullname);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.ChangeNamesRoutine.Update(getExecutionState()).updateModels(inputValues.renamedPerson, inputValues.oldFullname, retrievedValues.oldFamily, retrievedValues.correspondingMember, getRoutinesFacade());
    return true;
  }
}
