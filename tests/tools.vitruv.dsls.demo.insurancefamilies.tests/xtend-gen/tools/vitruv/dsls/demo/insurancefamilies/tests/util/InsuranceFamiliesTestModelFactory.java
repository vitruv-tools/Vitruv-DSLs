package tools.vitruv.dsls.demo.insurancefamilies.tests.util;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.testutils.TestModel;

@SuppressWarnings("all")
public interface InsuranceFamiliesTestModelFactory {
  void changeInsuranceModel(final Procedure1<? super TestModel<InsuranceDatabase>> modelModification);

  void changeFamilyModel(final Procedure1<? super TestModel<FamilyRegister>> modelModification);

  void validateInsuranceModel(final Procedure1<? super TestModel<InsuranceDatabase>> viewValidation);

  void validateFamilyModel(final Procedure1<? super TestModel<FamilyRegister>> viewValidation);
}
