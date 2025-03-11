package mir.routines.insuranceToPersons;

import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreatePersonRoutine extends AbstractRoutine {
  private CreatePersonRoutine.InputValues inputValues;

  public class InputValues {
    public final InsuranceClient client;

    public InputValues(final InsuranceClient client) {
      this.client = client;
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsuranceClient client, @Extension final InsuranceToPersonsRoutinesFacade _routinesFacade) {
      Gender _gender = client.getGender();
      if (_gender != null) {
        switch (_gender) {
          case MALE:
            _routinesFacade.createMalePerson(client);
            break;
          case FEMALE:
            _routinesFacade.createFemalePerson(client);
            break;
          default:
            throw new IllegalArgumentException("Unknown gender for persons");
        }
      } else {
        throw new IllegalArgumentException("Unknown gender for persons");
      }
    }
  }

  public CreatePersonRoutine(final InsuranceToPersonsRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final InsuranceClient client) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreatePersonRoutine.InputValues(client);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreatePersonRoutine with input:");
    	getLogger().trace("   inputValues.client: " + inputValues.client);
    }
    // This execution step is empty
    // This execution step is empty
    new mir.routines.insuranceToPersons.CreatePersonRoutine.Update(getExecutionState()).updateModels(inputValues.client, getRoutinesFacade());
    return true;
  }
}
