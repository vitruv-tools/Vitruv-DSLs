package mir.reactions.insuranceToFamilies;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class InsuranceToFamiliesChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public InsuranceToFamiliesChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.insurance")), 
    	MetamodelDescriptor.with(Set.of("edu.kit.ipd.sdq.metamodels.families")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.insuranceToFamilies.InsuranceToFamiliesRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.insurance.impl.InsurancePackageImpl.eINSTANCE);
    org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.putIfAbsent(edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eNS_URI, edu.kit.ipd.sdq.metamodels.families.impl.FamiliesPackageImpl.eINSTANCE);
    addReaction(new mir.reactions.insuranceToFamilies.CreatedInsuranceDatabaseReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToFamilies"))));
    addReaction(new mir.reactions.insuranceToFamilies.DeleteInsuranceDatabaseReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToFamilies"))));
    addReaction(new mir.reactions.insuranceToFamilies.CreatedInsuranceClientReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToFamilies"))));
    addReaction(new mir.reactions.insuranceToFamilies.DeletedInsuranceClientReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToFamilies"))));
    addReaction(new mir.reactions.insuranceToFamilies.ChangedGenderReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToFamilies"))));
    addReaction(new mir.reactions.insuranceToFamilies.ChangedNameReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("insuranceToFamilies"))));
  }
}
