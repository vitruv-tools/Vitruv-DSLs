package tools.vitruv.dsls.demo.insurancefamilies.tests.util;

import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.util.function.Consumer;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.testutils.ChangePropagatingTestViewBasedTestModel;
import tools.vitruv.dsls.testutils.TestModel;
import tools.vitruv.dsls.testutils.TestViewBasedTestModel;
import tools.vitruv.testutils.views.NonTransactionalTestView;

@SuppressWarnings("all")
public class InsuranceFamiliesDefaultTestModelFactory implements InsuranceFamiliesTestModelFactory {
  private final TestViewBasedTestModel<InsuranceDatabase> insuranceModel;

  private final TestViewBasedTestModel<FamilyRegister> familiesModel;

  public InsuranceFamiliesDefaultTestModelFactory(final NonTransactionalTestView testView) {
    ChangePropagatingTestViewBasedTestModel<InsuranceDatabase> _changePropagatingTestViewBasedTestModel = new ChangePropagatingTestViewBasedTestModel<InsuranceDatabase>(testView, InsuranceDatabase.class);
    this.insuranceModel = _changePropagatingTestViewBasedTestModel;
    ChangePropagatingTestViewBasedTestModel<FamilyRegister> _changePropagatingTestViewBasedTestModel_1 = new ChangePropagatingTestViewBasedTestModel<FamilyRegister>(testView, FamilyRegister.class);
    this.familiesModel = _changePropagatingTestViewBasedTestModel_1;
  }

  @Override
  public void changeInsuranceModel(final Procedure1<? super TestModel<InsuranceDatabase>> modelModification) {
    this.insuranceModel.applyChanges(new Consumer<TestModel<InsuranceDatabase>>() {
        public void accept(TestModel<InsuranceDatabase> arg0) {
          modelModification.apply(arg0);
        }
    }, this.familiesModel);
  }

  @Override
  public void changeFamilyModel(final Procedure1<? super TestModel<FamilyRegister>> modelModification) {
    this.familiesModel.applyChanges(new Consumer<TestModel<FamilyRegister>>() {
        public void accept(TestModel<FamilyRegister> arg0) {
          modelModification.apply(arg0);
        }
    }, this.insuranceModel);
  }

  @Override
  public void validateInsuranceModel(final Procedure1<? super TestModel<InsuranceDatabase>> viewValidation) {
    viewValidation.apply(this.insuranceModel);
  }

  @Override
  public void validateFamilyModel(final Procedure1<? super TestModel<FamilyRegister>> viewValidation) {
    viewValidation.apply(this.familiesModel);
  }
}
