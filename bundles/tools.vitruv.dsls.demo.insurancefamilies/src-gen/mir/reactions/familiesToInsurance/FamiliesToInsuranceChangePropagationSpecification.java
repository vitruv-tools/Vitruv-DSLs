package mir.reactions.familiesToInsurance;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class FamiliesToInsuranceChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public FamiliesToInsuranceChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.families")), 
    	MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.insurance")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.familiesToInsurance.FamiliesToInsuranceRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eINSTANCE);
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eINSTANCE);
    addReaction(new mir.reactions.familiesToInsurance.CreatedFamilyREgisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.DeletedFamilyRegisterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.DeletedFamilyReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.ChangedLastNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.CreatedFatherReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.CreatedMotherReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.CreatedAndInsertedSonReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.InsertedSonReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.CreatedAndInsertedDaughterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.InsertedDaughterReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.ChangedFirstNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
    addReaction(new mir.reactions.familiesToInsurance.DeletedMemberReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("familiesToInsurance"))));
  }
}
