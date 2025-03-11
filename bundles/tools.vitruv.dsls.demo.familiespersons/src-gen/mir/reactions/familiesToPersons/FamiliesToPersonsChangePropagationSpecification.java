package mir.reactions.familiesToPersons;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class FamiliesToPersonsChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public FamiliesToPersonsChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.families")), 
    	MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.persons")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.familiesToPersons.FamiliesToPersonsRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eINSTANCE);
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.persons.impl.PersonsPackageImpl.eINSTANCE);
    addReaction(new mir.reactions.familiesToPersons.InsertedFamilyRegisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.DeletedFamilyRegisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.DeletedFamilyReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.ChangedLastNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.CreatedFatherReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.CreatedMotherReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.InsertedSonReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.InsertedDaughterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.ChangedFirstNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
    addReaction(new mir.reactions.familiesToPersons.DeletedMemberReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToPersons"))));
  }
}
