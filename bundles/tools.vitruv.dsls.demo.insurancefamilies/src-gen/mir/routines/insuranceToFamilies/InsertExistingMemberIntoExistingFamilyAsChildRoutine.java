package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class InsertExistingMemberIntoExistingFamilyAsChildRoutine extends AbstractRoutine {
  private InsertExistingMemberIntoExistingFamilyAsChildRoutine.InputValues inputValues;

  private InsertExistingMemberIntoExistingFamilyAsChildRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insuranceClient;

    public final Family familyToInsertInto;

    public InputValues(final InsuranceClient insuranceClient, final Family familyToInsertInto) {
      this.insuranceClient = insuranceClient;
      this.familyToInsertInto = familyToInsertInto;
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

    public EObject getCorrepondenceSourceOldFamily(final InsuranceClient insuranceClient, final Family familyToInsertInto) {
      return insuranceClient;
    }

    public EObject getCorrepondenceSourceCorrespondingMember(final InsuranceClient insuranceClient, final Family familyToInsertInto, final Family oldFamily) {
      return insuranceClient;
    }

    public InsertExistingMemberIntoExistingFamilyAsChildRoutine.Match.RetrievedValues match(final InsuranceClient insuranceClient, final Family familyToInsertInto) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.Family oldFamily = getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(insuranceClient, familyToInsertInto), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (oldFamily == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Member correspondingMember = getCorrespondingElement(
      	getCorrepondenceSourceCorrespondingMember(insuranceClient, familyToInsertInto, oldFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (correspondingMember == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.InsertExistingMemberIntoExistingFamilyAsChildRoutine.Match.RetrievedValues(oldFamily, correspondingMember);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insuranceClient, final Family familyToInsertInto, final Family oldFamily, final Member correspondingMember, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      Gender _gender = insuranceClient.getGender();
      if (_gender != null) {
        switch (_gender) {
          case MALE:
            EList<Member> _sons = familyToInsertInto.getSons();
            _sons.add(correspondingMember);
            break;
          case FEMALE:
            EList<Member> _daughters = familyToInsertInto.getDaughters();
            _daughters.add(correspondingMember);
            break;
          default:
            break;
        }
      }
      Member _father = oldFamily.getFather();
      boolean _tripleEquals = (_father == correspondingMember);
      if (_tripleEquals) {
        oldFamily.setFather(null);
      }
      Member _mother = oldFamily.getMother();
      boolean _tripleEquals_1 = (_mother == correspondingMember);
      if (_tripleEquals_1) {
        oldFamily.setMother(null);
      }
      EList<Member> _sons_1 = oldFamily.getSons();
      _sons_1.remove(correspondingMember);
      EList<Member> _daughters_1 = oldFamily.getDaughters();
      _daughters_1.remove(correspondingMember);
      this.removeCorrespondenceBetween(insuranceClient, oldFamily);
      this.addCorrespondenceBetween(insuranceClient, familyToInsertInto);
      this.addCorrespondenceBetween(insuranceClient, correspondingMember);
      _routinesFacade.deleteFamilyIfEmpty(oldFamily);
    }
  }

  public InsertExistingMemberIntoExistingFamilyAsChildRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insuranceClient, final Family familyToInsertInto) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertExistingMemberIntoExistingFamilyAsChildRoutine.InputValues(insuranceClient, familyToInsertInto);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertExistingMemberIntoExistingFamilyAsChildRoutine with input:");
    	getLogger().trace("   inputValues.insuranceClient: " + inputValues.insuranceClient);
    	getLogger().trace("   inputValues.familyToInsertInto: " + inputValues.familyToInsertInto);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.InsertExistingMemberIntoExistingFamilyAsChildRoutine.Match(getExecutionState()).match(inputValues.insuranceClient, inputValues.familyToInsertInto);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.InsertExistingMemberIntoExistingFamilyAsChildRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceClient, inputValues.familyToInsertInto, retrievedValues.oldFamily, retrievedValues.correspondingMember, getRoutinesFacade());
    return true;
  }
}
