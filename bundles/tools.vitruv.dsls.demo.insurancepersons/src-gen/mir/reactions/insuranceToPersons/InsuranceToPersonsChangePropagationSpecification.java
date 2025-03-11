package mir.reactions.insuranceToPersons;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class InsuranceToPersonsChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public InsuranceToPersonsChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.insurance")), 
    	MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.persons")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.insuranceToPersons.InsuranceToPersonsRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eINSTANCE);
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eINSTANCE);
    addReaction(new mir.reactions.insuranceToPersons.InsertedInsuranceDatabaseReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToPersons"))));
    addReaction(new mir.reactions.insuranceToPersons.DeletedInsuranceDatabaseReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToPersons"))));
    addReaction(new mir.reactions.insuranceToPersons.InsertedClientReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToPersons"))));
    addReaction(new mir.reactions.insuranceToPersons.ChangedNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToPersons"))));
    addReaction(new mir.reactions.insuranceToPersons.ChangedGenderReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToPersons"))));
    addReaction(new mir.reactions.insuranceToPersons.DeletedClientReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToPersons"))));
  }
}
