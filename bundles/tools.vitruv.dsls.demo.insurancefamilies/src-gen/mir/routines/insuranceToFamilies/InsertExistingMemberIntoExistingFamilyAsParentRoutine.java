package mir.routines.insuranceToFamilies;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesHelper;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class InsertExistingMemberIntoExistingFamilyAsParentRoutine extends AbstractRoutine {
  private InsertExistingMemberIntoExistingFamilyAsParentRoutine.InputValues inputValues;

  private InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final InsuranceClient insuranceClient;

    public final Member correspondingMember;

    public final Family familyToInsertInto;

    public InputValues(final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto) {
      this.insuranceClient = insuranceClient;
      this.correspondingMember = correspondingMember;
      this.familyToInsertInto = familyToInsertInto;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Optional<Family> oldFamily;

      public final Optional<InsuranceClient> possiblyReplacedFatherClient;

      public final Optional<InsuranceClient> possiblyReplacedMotherClient;

      public RetrievedValues(final Optional<Family> oldFamily, final Optional<InsuranceClient> possiblyReplacedFatherClient, final Optional<InsuranceClient> possiblyReplacedMotherClient) {
        this.oldFamily = oldFamily;
        this.possiblyReplacedFatherClient = possiblyReplacedFatherClient;
        this.possiblyReplacedMotherClient = possiblyReplacedMotherClient;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceOldFamily(final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto) {
      return insuranceClient;
    }

    public EObject getCorrepondenceSourcePossiblyReplacedFatherClient(final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto, final Optional<Family> oldFamily) {
      Member _father = familyToInsertInto.getFather();
      return _father;
    }

    public EObject getCorrepondenceSourcePossiblyReplacedMotherClient(final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto, final Optional<Family> oldFamily, final Optional<InsuranceClient> possiblyReplacedFatherClient) {
      Member _mother = familyToInsertInto.getMother();
      return _mother;
    }

    public InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match.RetrievedValues match(final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto) throws IOException {
      Optional<edu.kit.ipd.sdq.metamodels.families.Family> oldFamily = Optional.ofNullable(getCorrespondingElement(
      	getCorrepondenceSourceOldFamily(insuranceClient, correspondingMember, familyToInsertInto), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.families.Family.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      )
      );
      Optional<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient> possiblyReplacedFatherClient = Optional.ofNullable(getCorrespondingElement(
      	getCorrepondenceSourcePossiblyReplacedFatherClient(insuranceClient, correspondingMember, familyToInsertInto, oldFamily), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      )
      );
      Optional<edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient> possiblyReplacedMotherClient = Optional.ofNullable(getCorrespondingElement(
      	getCorrepondenceSourcePossiblyReplacedMotherClient(insuranceClient, correspondingMember, familyToInsertInto, oldFamily, possiblyReplacedFatherClient), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      )
      );
      return new mir.routines.insuranceToFamilies.InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match.RetrievedValues(oldFamily, possiblyReplacedFatherClient, possiblyReplacedMotherClient);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto, final Optional<Family> oldFamily, final Optional<InsuranceClient> possiblyReplacedFatherClient, final Optional<InsuranceClient> possiblyReplacedMotherClient, @Extension final InsuranceToFamiliesRoutinesFacade _routinesFacade) {
      Gender _gender = insuranceClient.getGender();
      if (_gender != null) {
        switch (_gender) {
          case MALE:
            final Member existingFather = familyToInsertInto.getFather();
            boolean _identityEquals = (existingFather == correspondingMember);
            boolean _not = (!_identityEquals);
            if (_not) {
              if (((existingFather != null) && (!Objects.equal(existingFather, correspondingMember)))) {
                _routinesFacade.deleteMember(possiblyReplacedFatherClient.get());
                familyToInsertInto.setFather(correspondingMember);
                InsuranceToFamiliesHelper.informUserAboutReplacementOfClient(this.executionState.getUserInteractor(), possiblyReplacedFatherClient.get(), familyToInsertInto);
                _routinesFacade.createParent(possiblyReplacedFatherClient.get());
              } else {
                familyToInsertInto.setFather(correspondingMember);
              }
            }
            break;
          case FEMALE:
            final Member existingMother = familyToInsertInto.getMother();
            boolean _identityEquals_1 = (existingMother == correspondingMember);
            boolean _not_1 = (!_identityEquals_1);
            if (_not_1) {
              if (((existingMother != null) && (!Objects.equal(existingMother, correspondingMember)))) {
                _routinesFacade.deleteMember(possiblyReplacedMotherClient.get());
                familyToInsertInto.setMother(correspondingMember);
                InsuranceToFamiliesHelper.informUserAboutReplacementOfClient(this.executionState.getUserInteractor(), possiblyReplacedMotherClient.get(), familyToInsertInto);
                _routinesFacade.createParent(possiblyReplacedMotherClient.get());
              } else {
                familyToInsertInto.setMother(correspondingMember);
              }
            }
            break;
          default:
            throw new IllegalArgumentException("undefined gender");
        }
      } else {
        throw new IllegalArgumentException("undefined gender");
      }
      _routinesFacade.tryRemoveCorrespondenceToOldFamily(insuranceClient);
      this.addCorrespondenceBetween(insuranceClient, familyToInsertInto);
    }
  }

  public InsertExistingMemberIntoExistingFamilyAsParentRoutine(final InsuranceToFamiliesRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new InsertExistingMemberIntoExistingFamilyAsParentRoutine.InputValues(insuranceClient, correspondingMember, familyToInsertInto);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine InsertExistingMemberIntoExistingFamilyAsParentRoutine with input:");
    	getLogger().trace("   inputValues.insuranceClient: " + inputValues.insuranceClient);
    	getLogger().trace("   inputValues.correspondingMember: " + inputValues.correspondingMember);
    	getLogger().trace("   inputValues.familyToInsertInto: " + inputValues.familyToInsertInto);
    }
    retrievedValues = new mir.routines.insuranceToFamilies.InsertExistingMemberIntoExistingFamilyAsParentRoutine.Match(getExecutionState()).match(inputValues.insuranceClient, inputValues.correspondingMember, inputValues.familyToInsertInto);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.insuranceToFamilies.InsertExistingMemberIntoExistingFamilyAsParentRoutine.Update(getExecutionState()).updateModels(inputValues.insuranceClient, inputValues.correspondingMember, inputValues.familyToInsertInto, retrievedValues.oldFamily, retrievedValues.possiblyReplacedFatherClient, retrievedValues.possiblyReplacedMotherClient, getRoutinesFacade());
    return true;
  }
}
