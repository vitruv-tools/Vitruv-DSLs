package mir.routines.personsToFamilies;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Female;
import edu.kit.ipd.sdq.metamodels.persons.Male;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.demo.familiespersons.persons2families.FamilyRole;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class ReactToLastnameAndFamilyRoleChangesRoutine extends AbstractRoutine {
  private ReactToLastnameAndFamilyRoleChangesRoutine.InputValues inputValues;

  private ReactToLastnameAndFamilyRoleChangesRoutine.Match.RetrievedValues retrievedValues;

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

      public final FamilyRegister familiesRegister;

      public RetrievedValues(final Family oldFamily, final Member correspondingMember, final FamilyRegister familiesRegister) {
        this.oldFamily = oldFamily;
        this.correspondingMember = correspondingMember;
        this.familiesRegister = familiesRegister;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceOldFamily(final Person renamedPerson, final String oldFullname) {
      return renamedPerson;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final Person renamedPerson, final String oldFullname, final Family oldFamily) {
      return renamedPerson;
    }

    public EObject getCorrepondenceSourceFamiliesRegister(final Person renamedPerson, final String oldFullname, final Family oldFamily, final Member correspondingMember) {
      PersonRegister _register = PersonsToFamiliesHelper.getRegister(renamedPerson);
      return _register;
    }

    public ReactToLastnameAndFamilyRoleChangesRoutine.Match.RetrievedValues match(final Person renamedPerson, final String oldFullname) throws IOException {
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
      edu.kit.ipd.sdq.metamodels.families.FamilyRegister familiesRegister = getCorrespondingElement(
      	getCorrepondenceSourceFamiliesRegister(renamedPerson, oldFullname, oldFamily, correspondingMember), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.FamilyRegister.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (familiesRegister == null) {
      	return null;
      }
      return new mir.routines.personsToFamilies.ReactToLastnameAndFamilyRoleChangesRoutine.Match.RetrievedValues(oldFamily, correspondingMember, familiesRegister);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Person renamedPerson, final String oldFullname, final Family oldFamily, final Member correspondingMember, final FamilyRegister familiesRegister, @Extension final PersonsToFamiliesRoutinesFacade _routinesFacade) {
      final boolean wasChildBeforeRenaming = ((correspondingMember.getFamilySon() == oldFamily) || 
        (correspondingMember.getFamilyDaughter() == oldFamily));
      FamilyRole _askUserWhetherPersonIsParentOrChildDuringRenaming = PersonsToFamiliesHelper.askUserWhetherPersonIsParentOrChildDuringRenaming(this.executionState.getUserInteractor(), oldFullname, renamedPerson.getFullName(), wasChildBeforeRenaming);
      final boolean isSupposedToBeAChild = Objects.equal(_askUserWhetherPersonIsParentOrChildDuringRenaming, FamilyRole.Child);
      if (((IterableExtensions.size(FamiliesUtil.getMembers(oldFamily)) == 1) && IterableExtensions.isEmpty(IterableExtensions.<Family>filter(familiesRegister.getFamilies(), ((Function1<? super Family, Boolean>)PersonsToFamiliesHelper.sameLastname(renamedPerson)))))) {
        oldFamily.setLastName(PersonsToFamiliesHelper.getLastname(renamedPerson));
        if ((Boolean.valueOf(wasChildBeforeRenaming) != Boolean.valueOf(isSupposedToBeAChild))) {
          if (isSupposedToBeAChild) {
            boolean _matched = false;
            if (renamedPerson instanceof Male) {
              _matched=true;
              EList<Member> _sons = oldFamily.getSons();
              _sons.add(correspondingMember);
            }
            if (!_matched) {
              if (renamedPerson instanceof Female) {
                _matched=true;
                EList<Member> _daughters = oldFamily.getDaughters();
                _daughters.add(correspondingMember);
              }
            }
          } else {
            boolean _matched_1 = false;
            if (renamedPerson instanceof Male) {
              _matched_1=true;
              oldFamily.setFather(correspondingMember);
            }
            if (!_matched_1) {
              if (renamedPerson instanceof Female) {
                _matched_1=true;
                oldFamily.setMother(correspondingMember);
              }
            }
          }
        }
      } else {
        if (isSupposedToBeAChild) {
          _routinesFacade.insertExistingMemberIntoUserChosenFamilyAsChild(renamedPerson);
        } else {
          _routinesFacade.insertExistingMemberIntoUserChosenFamilyAsParent(renamedPerson);
        }
        _routinesFacade.deleteFamilyIfEmpty(oldFamily);
      }
    }
  }

  public ReactToLastnameAndFamilyRoleChangesRoutine(final PersonsToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Person renamedPerson, final String oldFullname) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ReactToLastnameAndFamilyRoleChangesRoutine.InputValues(renamedPerson, oldFullname);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ReactToLastnameAndFamilyRoleChangesRoutine with input:");
    	getLogger().trace("   inputValues.renamedPerson: " + inputValues.renamedPerson);
    	getLogger().trace("   inputValues.oldFullname: " + inputValues.oldFullname);
    }
    retrievedValues = new mir.routines.personsToFamilies.ReactToLastnameAndFamilyRoleChangesRoutine.Match(getExecutionState()).match(inputValues.renamedPerson, inputValues.oldFullname);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.personsToFamilies.ReactToLastnameAndFamilyRoleChangesRoutine.Update(getExecutionState()).updateModels(inputValues.renamedPerson, inputValues.oldFullname, retrievedValues.oldFamily, retrievedValues.correspondingMember, retrievedValues.familiesRegister, getRoutinesFacade());
    return true;
  }
}
