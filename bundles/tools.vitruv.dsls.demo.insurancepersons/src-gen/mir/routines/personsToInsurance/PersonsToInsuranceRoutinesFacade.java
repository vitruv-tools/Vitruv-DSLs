package mir.routines.personsToInsurance;

import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class PersonsToInsuranceRoutinesFacade extends AbstractRoutinesFacade {
  public PersonsToInsuranceRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createInsuranceDatabase(final PersonRegister personRegister) {
    PersonsToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateInsuranceDatabaseRoutine routine = new CreateInsuranceDatabaseRoutine(_routinesFacade, _executionState, _caller, personRegister);
    return routine.execute();
  }

  public boolean deleteInsuranceDatabase(final PersonRegister personsRegister) {
    PersonsToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteInsuranceDatabaseRoutine routine = new DeleteInsuranceDatabaseRoutine(_routinesFacade, _executionState, _caller, personsRegister);
    return routine.execute();
  }

  public boolean createInsuranceClient(final Person person) {
    PersonsToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    CreateInsuranceClientRoutine routine = new CreateInsuranceClientRoutine(_routinesFacade, _executionState, _caller, person);
    return routine.execute();
  }

  public boolean changeNames(final Person person) {
    PersonsToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    ChangeNamesRoutine routine = new ChangeNamesRoutine(_routinesFacade, _executionState, _caller, person);
    return routine.execute();
  }

  public boolean deleteClient(final Person person) {
    PersonsToInsuranceRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = _getExecutionState();
    CallHierarchyHaving _caller = this._getCurrentCaller();
    DeleteClientRoutine routine = new DeleteClientRoutine(_routinesFacade, _executionState, _caller, person);
    return routine.execute();
  }
}
