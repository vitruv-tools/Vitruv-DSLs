package mir.routines.insuranceToFamilies;

import com.google.common.base.Objects;
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
public class ChangeGenderRoutine extends AbstractRoutine {
  private ChangeGenderRoutine.InputValues inputValues;

  private ChangeGenderRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insuranceClient;

    public InputValues(final InsuranceClient insuranceClient) {
      this.insuranceClient = insuranceClient;
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

    public EObject getCorrepondenceSourceMember(final InsuranceClient insuranceClient) {
      return insuranceClient;
    }

    public EObject getCorrepondenceSourceFamily(final InsuranceClient insuranceClient, final Member member) {
      return insuranceClient;
    }

    public ChangeGenderRoutine.Match.RetrievedValues match(final InsuranceClient insuranceClient) throws IOException {
      edu.kit.ipd.sdq.metamodels.families.Member member = getCorrespondingElement(
      	getCorrepondenceSourceMember(insuranceClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Member.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (member == null) {
      	return null;
      }
      edu.kit.ipd.sdq.metamodels.families.Family family = getCorrespondingElement(
      	getCorrepondenceSourceFamily(insuranceClient, member), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (family == null) {
      	return null;
      }
      return new mir.routines.insuranceToFamilies.ChangeGenderRoutine.Match.RetrievedValues(member, family);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insuranceClient, final Member member, final Family family, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      final Gender newGender = insuranceClient.getGender();
      final boolean wasParent = (Objects.equal(family.getFather(), member) || Objects.equal(family.getMother(), member));
      if (wasParent) {
        _routinesFacade.insertExistingMemberIntoExistingFamilyAsParent(insuranceClient, member, family);
      } else {
        if (newGender != null) {
          switch (newGender) {
            case MALE:
              EList<Member> _daughters = family.getDaughters();
              final boolean wasDaughterBefore = _daughters.remove(member);
              if (wasDaughterBefore) {
                EList<Member> _sons = family.getSons();
                _sons.add(member);
              }
              break;
            case FEMALE:
              EList<Member> _sons_1 = family.getSons();
              final boolean wasSonBefore = _sons_1.remove(member);
              if (wasSonBefore) {
                EList<Member> _daughters_1 = family.getDaughters();
                _daughters_1.add(member);
              }
              break;
            default:
              break;
          }
        }
      }
    }
  }

  public ChangeGenderRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insuranceClient) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new ChangeGenderRoutine.InputValues(insuranceClient);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine ChangeGenderRoutine with input:");
    	getLogger().trace("   inputValues.insuranceClient: " + inputValues.insuranceClient);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.ChangeGenderRoutine.Match(getExecutionState()).match(inputValues.insuranceClient);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.ChangeGenderRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceClient, retrievedValues.member, retrievedValues.family, getRoutinesFacade());
    return true;
  }
}
