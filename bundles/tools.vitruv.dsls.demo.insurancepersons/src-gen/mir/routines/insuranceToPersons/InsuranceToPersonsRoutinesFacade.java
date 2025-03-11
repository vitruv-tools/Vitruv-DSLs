package mir.routines.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class InsuranceToPersonsRoutinesFacade extends AbstractRoutinesFacade {
  public InsuranceToPersonsRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createPersonRegister(final InsuranceDatabase insuranceDatabase) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreatePersonRegisterRoutine routine = new CreatePersonRegisterRoutine(_routinesFacade, _executionState, _caller, insuranceDatabase);
    return routine.execute();
  }

  public boolean deletePersonRegister(final InsuranceDatabase insuranceDatabase) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeletePersonRegisterRoutine routine = new DeletePersonRegisterRoutine(_routinesFacade, _executionState, _caller, insuranceDatabase);
    return routine.execute();
  }

  public boolean createPerson(final InsuranceClient client) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreatePersonRoutine routine = new CreatePersonRoutine(_routinesFacade, _executionState, _caller, client);
    return routine.execute();
  }

  public boolean createMalePerson(final InsuranceClient client) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateMalePersonRoutine routine = new CreateMalePersonRoutine(_routinesFacade, _executionState, _caller, client);
    return routine.execute();
  }

  public boolean createFemalePerson(final InsuranceClient client) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateFemalePersonRoutine routine = new CreateFemalePersonRoutine(_routinesFacade, _executionState, _caller, client);
    return routine.execute();
  }

  public boolean changeFullName(final InsuranceClient client) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ChangeFullNameRoutine routine = new ChangeFullNameRoutine(_routinesFacade, _executionState, _caller, client);
    return routine.execute();
  }

  public boolean changeGender(final InsuranceClient client) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ChangeGenderRoutine routine = new ChangeGenderRoutine(_routinesFacade, _executionState, _caller, client);
    return routine.execute();
  }

  public boolean deletePerson(final InsuranceClient client) {
    InsuranceToPersonsRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeletePersonRoutine routine = new DeletePersonRoutine(_routinesFacade, _executionState, _caller, client);
    return routine.execute();
  }
}
