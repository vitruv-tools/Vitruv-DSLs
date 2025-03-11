package mir.reactions.personsToFamilies;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class PersonsToFamiliesChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public PersonsToFamiliesChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.persons")), 
    	MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.families")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.personsToFamilies.PersonsToFamiliesRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eINSTANCE);
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eINSTANCE);
    addReaction(new mir.reactions.personsToFamilies.InsertedPersonRegisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToFamilies"))));
    addReaction(new mir.reactions.personsToFamilies.DeletedPersonRegisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToFamilies"))));
    addReaction(new mir.reactions.personsToFamilies.InsertedPersonReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToFamilies"))));
    addReaction(new mir.reactions.personsToFamilies.ChangedFullNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToFamilies"))));
    addReaction(new mir.reactions.personsToFamilies.DeletePersonReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("personsToFamilies"))));
  }
}
