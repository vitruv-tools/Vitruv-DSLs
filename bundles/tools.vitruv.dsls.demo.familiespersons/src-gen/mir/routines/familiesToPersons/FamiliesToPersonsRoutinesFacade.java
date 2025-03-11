package mir.routines.familiesToPersons;

import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class FamiliesToPersonsRoutinesFacade extends AbstractRoutinesFacade {
  public FamiliesToPersonsRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createPersonRegister(final FamilyRegister familyRegister) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreatePersonRegisterRoutine routine = new CreatePersonRegisterRoutine(_routinesFacade, _executionState, _caller, familyRegister);
    return routine.execute();
  }

  public boolean deletePersonRegister(final FamilyRegister familyRegister) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeletePersonRegisterRoutine routine = new DeletePersonRegisterRoutine(_routinesFacade, _executionState, _caller, familyRegister);
    return routine.execute();
  }

  public boolean createOrFindMale(final Member newMember, final Family family) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateOrFindMaleRoutine routine = new CreateOrFindMaleRoutine(_routinesFacade, _executionState, _caller, newMember, family);
    return routine.execute();
  }

  public boolean createMaleFromNewMember(final Member newFather, final Family family) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateMaleFromNewMemberRoutine routine = new CreateMaleFromNewMemberRoutine(_routinesFacade, _executionState, _caller, newFather, family);
    return routine.execute();
  }

  public boolean createMale(final Member newMember, final Family family) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateMaleRoutine routine = new CreateMaleRoutine(_routinesFacade, _executionState, _caller, newMember, family);
    return routine.execute();
  }

  public boolean createOrFindFemale(final Member newMember, final Family family) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateOrFindFemaleRoutine routine = new CreateOrFindFemaleRoutine(_routinesFacade, _executionState, _caller, newMember, family);
    return routine.execute();
  }

  public boolean createFemaleFromNewMember(final Member newMother, final Family family) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateFemaleFromNewMemberRoutine routine = new CreateFemaleFromNewMemberRoutine(_routinesFacade, _executionState, _caller, newMother, family);
    return routine.execute();
  }

  public boolean createFemale(final Member newMember, final Family family) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateFemaleRoutine routine = new CreateFemaleRoutine(_routinesFacade, _executionState, _caller, newMember, family);
    return routine.execute();
  }

  public boolean updateNameAndCorrespondencesAfterMemberBecameFather(final Member newFather, final Family newFamily) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine routine = new UpdateNameAndCorrespondencesAfterMemberBecameFatherRoutine(_routinesFacade, _executionState, _caller, newFather, newFamily);
    return routine.execute();
  }

  public boolean updateNameAndCorrespondencesAfterMemberBecameMother(final Member newMother, final Family newFamily) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine routine = new UpdateNameAndCorrespondencesAfterMemberBecameMotherRoutine(_routinesFacade, _executionState, _caller, newMother, newFamily);
    return routine.execute();
  }

  public boolean updateNameAndCorrespondencesOfCorrespondingPerson(final Member newMember, final Family newFamily) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine routine = new UpdateNameAndCorrespondencesOfCorrespondingPersonRoutine(_routinesFacade, _executionState, _caller, newMember, newFamily);
    return routine.execute();
  }

  public boolean existingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMale(final Member insertedChild, final Family newFamily) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine routine = new ExistingMemberBecameSon_updateNameAndCorrespondencesOfCorrespondingMaleRoutine(_routinesFacade, _executionState, _caller, insertedChild, newFamily);
    return routine.execute();
  }

  public boolean existingMemberBecameDaughter_updateNameAndCorrespondencesOfCorrespondingFemale(final Member insertedChild, final Family newFamily) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ExistingMemberBecameDaughter_updateNameAndCorrespondencesOfCorrespondingFemaleRoutine routine = new ExistingMemberBecameDaughter_updateNameAndCorrespondencesOfCorrespondingFemaleRoutine(_routinesFacade, _executionState, _caller, insertedChild, newFamily);
    return routine.execute();
  }

  public boolean updatePersonFamilyCorrespondence(final Member insertedChild, final Family newFamily) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    UpdatePersonFamilyCorrespondenceRoutine routine = new UpdatePersonFamilyCorrespondenceRoutine(_routinesFacade, _executionState, _caller, insertedChild, newFamily);
    return routine.execute();
  }

  public boolean updatePersonName(final Member member) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    UpdatePersonNameRoutine routine = new UpdatePersonNameRoutine(_routinesFacade, _executionState, _caller, member);
    return routine.execute();
  }

  public boolean deletePerson(final Member member) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeletePersonRoutine routine = new DeletePersonRoutine(_routinesFacade, _executionState, _caller, member);
    return routine.execute();
  }

  public boolean deleteFamilyIfEmpty(final Family family) {
    FamiliesToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteFamilyIfEmptyRoutine routine = new DeleteFamilyIfEmptyRoutine(_routinesFacade, _executionState, _caller, family);
    return routine.execute();
  }
}
