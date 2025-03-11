package mir.routines.familiesToInsurance;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class FamiliesToInsuranceRoutinesFacade extends AbstractRoutinesFacade {
  public FamiliesToInsuranceRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createInsuranceDatabase(final FamilyRegister familyRegister) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateInsuranceDatabaseRoutine routine = new CreateInsuranceDatabaseRoutine(_routinesFacade, _executionState, _caller, familyRegister);
    return routine.execute();
  }

  public boolean deleteInsuranceDatabase(final FamilyRegister familyRegister) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteInsuranceDatabaseRoutine routine = new DeleteInsuranceDatabaseRoutine(_routinesFacade, _executionState, _caller, familyRegister);
    return routine.execute();
  }

  public boolean deleteFamily(final Family family) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteFamilyRoutine routine = new DeleteFamilyRoutine(_routinesFacade, _executionState, _caller, family);
    return routine.execute();
  }

  public boolean changeFamilyName(final Family family) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ChangeFamilyNameRoutine routine = new ChangeFamilyNameRoutine(_routinesFacade, _executionState, _caller, family);
    return routine.execute();
  }

  public boolean tryCreateInsuranceClientFromNewMember(final Member newMember, final Family family, final Gender gender) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    TryCreateInsuranceClientFromNewMemberRoutine routine = new TryCreateInsuranceClientFromNewMemberRoutine(_routinesFacade, _executionState, _caller, newMember, family, gender);
    return routine.execute();
  }

  public boolean createInsuranceClient(final Member newMember, final Family family, final Gender gender) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateInsuranceClientRoutine routine = new CreateInsuranceClientRoutine(_routinesFacade, _executionState, _caller, newMember, family, gender);
    return routine.execute();
  }

  public boolean updateNameAndCorrespondencesOfCorrespondingInsuranceClient(final Member newMember, final Family newFamily, final Gender gender) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine routine = new UpdateNameAndCorrespondencesOfCorrespondingInsuranceClientRoutine(_routinesFacade, _executionState, _caller, newMember, newFamily, gender);
    return routine.execute();
  }

  public boolean updateInsuranceClientName(final Member member) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    UpdateInsuranceClientNameRoutine routine = new UpdateInsuranceClientNameRoutine(_routinesFacade, _executionState, _caller, member);
    return routine.execute();
  }

  public boolean deleteInsuranceClient(final Member member) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteInsuranceClientRoutine routine = new DeleteInsuranceClientRoutine(_routinesFacade, _executionState, _caller, member);
    return routine.execute();
  }

  public boolean deleteFamilyIfEmpty(final Family family) {
    FamiliesToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteFamilyIfEmptyRoutine routine = new DeleteFamilyIfEmptyRoutine(_routinesFacade, _executionState, _caller, family);
    return routine.execute();
  }
}
