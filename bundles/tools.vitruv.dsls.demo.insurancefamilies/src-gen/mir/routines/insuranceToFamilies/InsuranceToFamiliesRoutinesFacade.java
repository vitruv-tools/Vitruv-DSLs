package mir.routines.insuranceToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class InsuranceToFamiliesRoutinesFacade extends AbstractRoutinesFacade {
  public InsuranceToFamiliesRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createFamilyRegister(final InsuranceDatabase insuranceDatabase) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateFamilyRegisterRoutine routine = new CreateFamilyRegisterRoutine(_routinesFacade, _executionState, _caller, insuranceDatabase);
    return routine.execute();
  }

  public boolean deleteFamilyRegister(final InsuranceDatabase insuranceDatabase) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteFamilyRegisterRoutine routine = new DeleteFamilyRegisterRoutine(_routinesFacade, _executionState, _caller, insuranceDatabase);
    return routine.execute();
  }

  public boolean insertAsParentOrChild(final InsuranceClient insertedClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertAsParentOrChildRoutine routine = new InsertAsParentOrChildRoutine(_routinesFacade, _executionState, _caller, insertedClient);
    return routine.execute();
  }

  public boolean createChild(final InsuranceClient insertedClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateChildRoutine routine = new CreateChildRoutine(_routinesFacade, _executionState, _caller, insertedClient);
    return routine.execute();
  }

  public boolean createParent(final InsuranceClient insertedClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateParentRoutine routine = new CreateParentRoutine(_routinesFacade, _executionState, _caller, insertedClient);
    return routine.execute();
  }

  public boolean createChildInNewFamily(final InsuranceClient insertedClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateChildInNewFamilyRoutine routine = new CreateChildInNewFamilyRoutine(_routinesFacade, _executionState, _caller, insertedClient);
    return routine.execute();
  }

  public boolean createParentInNewFamily(final InsuranceClient insertedClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateParentInNewFamilyRoutine routine = new CreateParentInNewFamilyRoutine(_routinesFacade, _executionState, _caller, insertedClient);
    return routine.execute();
  }

  public boolean createChildInExistingFamily(final InsuranceClient insertedClient, final Family familyToInsertInto) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateChildInExistingFamilyRoutine routine = new CreateChildInExistingFamilyRoutine(_routinesFacade, _executionState, _caller, insertedClient, familyToInsertInto);
    return routine.execute();
  }

  public boolean createParentInExistingFamily(final InsuranceClient insertedClient, final Family familyToInsertInto) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateParentInExistingFamilyRoutine routine = new CreateParentInExistingFamilyRoutine(_routinesFacade, _executionState, _caller, insertedClient, familyToInsertInto);
    return routine.execute();
  }

  public boolean deleteMember(final InsuranceClient insuranceClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteMemberRoutine routine = new DeleteMemberRoutine(_routinesFacade, _executionState, _caller, insuranceClient);
    return routine.execute();
  }

  public boolean deleteFamilyIfEmpty(final Family family) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteFamilyIfEmptyRoutine routine = new DeleteFamilyIfEmptyRoutine(_routinesFacade, _executionState, _caller, family);
    return routine.execute();
  }

  public boolean changeGender(final InsuranceClient insuranceClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ChangeGenderRoutine routine = new ChangeGenderRoutine(_routinesFacade, _executionState, _caller, insuranceClient);
    return routine.execute();
  }

  public boolean changeNames(final InsuranceClient renamedClient, final String oldFullName) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ChangeNamesRoutine routine = new ChangeNamesRoutine(_routinesFacade, _executionState, _caller, renamedClient, oldFullName);
    return routine.execute();
  }

  public boolean reactToLastnameChange(final InsuranceClient renamedClient, final String oldFullname) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ReactToLastnameChangeRoutine routine = new ReactToLastnameChangeRoutine(_routinesFacade, _executionState, _caller, renamedClient, oldFullname);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoUserChosenFamilyAsParent(final InsuranceClient insuranceClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoUserChosenFamilyAsParentRoutine routine = new InsertExistingMemberIntoUserChosenFamilyAsParentRoutine(_routinesFacade, _executionState, _caller, insuranceClient);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoUserChosenFamilyAsChild(final InsuranceClient insuranceClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoUserChosenFamilyAsChildRoutine routine = new InsertExistingMemberIntoUserChosenFamilyAsChildRoutine(_routinesFacade, _executionState, _caller, insuranceClient);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoNewFamilyAsParent(final InsuranceClient insuranceClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoNewFamilyAsParentRoutine routine = new InsertExistingMemberIntoNewFamilyAsParentRoutine(_routinesFacade, _executionState, _caller, insuranceClient);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoNewFamilyAsChild(final InsuranceClient insuranceClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoNewFamilyAsChildRoutine routine = new InsertExistingMemberIntoNewFamilyAsChildRoutine(_routinesFacade, _executionState, _caller, insuranceClient);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoExistingFamilyAsParent(final InsuranceClient insuranceClient, final Member correspondingMember, final Family familyToInsertInto) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoExistingFamilyAsParentRoutine routine = new InsertExistingMemberIntoExistingFamilyAsParentRoutine(_routinesFacade, _executionState, _caller, insuranceClient, correspondingMember, familyToInsertInto);
    return routine.execute();
  }

  public boolean tryRemoveCorrespondenceToOldFamily(final InsuranceClient insuranceClient) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    TryRemoveCorrespondenceToOldFamilyRoutine routine = new TryRemoveCorrespondenceToOldFamilyRoutine(_routinesFacade, _executionState, _caller, insuranceClient);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoExistingFamilyAsChild(final InsuranceClient insuranceClient, final Family familyToInsertInto) {
    InsuranceToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoExistingFamilyAsChildRoutine routine = new InsertExistingMemberIntoExistingFamilyAsChildRoutine(_routinesFacade, _executionState, _caller, insuranceClient, familyToInsertInto);
    return routine.execute();
  }
}
