package mir.routines.personsToFamilies;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class PersonsToFamiliesRoutinesFacade extends AbstractRoutinesFacade {
  public PersonsToFamiliesRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createFamilyRegister(final PersonRegister createdPersonRegister) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateFamilyRegisterRoutine routine = new CreateFamilyRegisterRoutine(_routinesFacade, _executionState, _caller, createdPersonRegister);
    return routine.execute();
  }

  public boolean deleteFamilyRegister(final PersonRegister deletedPersonsRegister) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteFamilyRegisterRoutine routine = new DeleteFamilyRegisterRoutine(_routinesFacade, _executionState, _caller, deletedPersonsRegister);
    return routine.execute();
  }

  public boolean insertAsParentOrChild(final Person insertedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertAsParentOrChildRoutine routine = new InsertAsParentOrChildRoutine(_routinesFacade, _executionState, _caller, insertedPerson);
    return routine.execute();
  }

  public boolean createChild(final Person insertedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateChildRoutine routine = new CreateChildRoutine(_routinesFacade, _executionState, _caller, insertedPerson);
    return routine.execute();
  }

  public boolean createParent(final Person insertedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateParentRoutine routine = new CreateParentRoutine(_routinesFacade, _executionState, _caller, insertedPerson);
    return routine.execute();
  }

  public boolean createChildInNewFamily(final Person insertedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateChildInNewFamilyRoutine routine = new CreateChildInNewFamilyRoutine(_routinesFacade, _executionState, _caller, insertedPerson);
    return routine.execute();
  }

  public boolean createParentInNewFamily(final Person insertedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateParentInNewFamilyRoutine routine = new CreateParentInNewFamilyRoutine(_routinesFacade, _executionState, _caller, insertedPerson);
    return routine.execute();
  }

  public boolean createChildInExistingFamily(final Person insertedPerson, final Family familyToInsertInto) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateChildInExistingFamilyRoutine routine = new CreateChildInExistingFamilyRoutine(_routinesFacade, _executionState, _caller, insertedPerson, familyToInsertInto);
    return routine.execute();
  }

  public boolean createParentInExistingFamily(final Person insertedPerson, final Family familyToInsertInto) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateParentInExistingFamilyRoutine routine = new CreateParentInExistingFamilyRoutine(_routinesFacade, _executionState, _caller, insertedPerson, familyToInsertInto);
    return routine.execute();
  }

  public boolean changeNames(final Person renamedPerson, final String oldFullname) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ChangeNamesRoutine routine = new ChangeNamesRoutine(_routinesFacade, _executionState, _caller, renamedPerson, oldFullname);
    return routine.execute();
  }

  public boolean reactToLastnameAndFamilyRoleChanges(final Person renamedPerson, final String oldFullname) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ReactToLastnameAndFamilyRoleChangesRoutine routine = new ReactToLastnameAndFamilyRoleChangesRoutine(_routinesFacade, _executionState, _caller, renamedPerson, oldFullname);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoUserChosenFamilyAsParent(final Person renamedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoUserChosenFamilyAsParentRoutine routine = new InsertExistingMemberIntoUserChosenFamilyAsParentRoutine(_routinesFacade, _executionState, _caller, renamedPerson);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoUserChosenFamilyAsChild(final Person renamedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoUserChosenFamilyAsChildRoutine routine = new InsertExistingMemberIntoUserChosenFamilyAsChildRoutine(_routinesFacade, _executionState, _caller, renamedPerson);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoNewFamilyAsParent(final Person renamedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoNewFamilyAsParentRoutine routine = new InsertExistingMemberIntoNewFamilyAsParentRoutine(_routinesFacade, _executionState, _caller, renamedPerson);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoNewFamilyAsChild(final Person renamedPerson) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoNewFamilyAsChildRoutine routine = new InsertExistingMemberIntoNewFamilyAsChildRoutine(_routinesFacade, _executionState, _caller, renamedPerson);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoExistingFamilyAsParent(final Person renamedPerson, final Family familyToInsertInto) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoExistingFamilyAsParentRoutine routine = new InsertExistingMemberIntoExistingFamilyAsParentRoutine(_routinesFacade, _executionState, _caller, renamedPerson, familyToInsertInto);
    return routine.execute();
  }

  public boolean insertExistingMemberIntoExistingFamilyAsChild(final Person renamedPerson, final Family familyToInsertInto) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    InsertExistingMemberIntoExistingFamilyAsChildRoutine routine = new InsertExistingMemberIntoExistingFamilyAsChildRoutine(_routinesFacade, _executionState, _caller, renamedPerson, familyToInsertInto);
    return routine.execute();
  }

  public boolean deleteMember(final Person person) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteMemberRoutine routine = new DeleteMemberRoutine(_routinesFacade, _executionState, _caller, person);
    return routine.execute();
  }

  public boolean deleteFamilyIfEmpty(final Family family) {
    PersonsToFamiliesRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteFamilyIfEmptyRoutine routine = new DeleteFamilyIfEmptyRoutine(_routinesFacade, _executionState, _caller, family);
    return routine.execute();
  }
}
