package mir.reactions.personsToInsurance;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class PersonsToInsuranceChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public PersonsToInsuranceChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.persons")), 
    	MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.insurance")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.personsToInsurance.PersonsToInsuranceRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eINSTANCE);
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eINSTANCE);
    addReaction(new mir.reactions.personsToInsurance.InsertedPersonRegisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToInsurance"))));
    addReaction(new mir.reactions.personsToInsurance.DeletedPersonRegisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToInsurance"))));
    addReaction(new mir.reactions.personsToInsurance.InsertedPersonReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToInsurance"))));
    addReaction(new mir.reactions.personsToInsurance.ChangedFullNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToInsurance"))));
    addReaction(new mir.reactions.personsToInsurance.DeletedPersonReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToInsurance"))));
  }
}
