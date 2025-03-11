package tools.vitruv.dsls.demo.insurancefamilies.tests.insurance2families;

import com.google.common.base.Objects;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory;
import java.util.ArrayList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.PositionPreference;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.CreatorsUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.FamiliesQueryUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceQueryUtil;
import tools.vitruv.dsls.testutils.TestModel;

@SuppressWarnings("all")
public class InsuranceToFamiliesTest extends AbstractInsuranceToFamiliesTest {
  @Test
  public void testCreateInsuranceDatabase() {
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase insuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
      this.createAndRegisterRoot(it, insuranceDatabase, this.getUri(this.getProjectModelPath("insurance", AbstractInsuranceToFamiliesTest.getINSURANCE_MODEL_FILE_EXTENSION())));
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      Assertions.assertEquals(1, it.getRootObjects().size());
      this.assertNumberOfFamilies(it, 0);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testDeleteInsuranceDatabase() {
    this.createEmptyInsuranceDatabase();
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.deleteRoot(insuranceDatabase);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      Assertions.assertEquals(0, it.getRootObjects().size());
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testCreatedClient_father() {
    this.createEmptyInsuranceDatabase();
    this.decideParentOrChild(PositionPreference.Parent);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
      };
      it.setFather(CreatorsUtil.createFamilyMember(_function_2));
    };
    final Family expectedFamily = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_mother() {
    this.createEmptyInsuranceDatabase();
    this.decideParentOrChild(PositionPreference.Parent);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
      };
      it.setMother(CreatorsUtil.createFamilyMember(_function_2));
    };
    final Family expectedFamily = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_son() {
    this.createEmptyInsuranceDatabase();
    this.decideParentOrChild(PositionPreference.Child);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      EList<Member> _sons = it.getSons();
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
      };
      Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_2);
      _sons.add(_createFamilyMember);
    };
    final Family expectedFamily = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_dauther() {
    this.createEmptyInsuranceDatabase();
    this.decideParentOrChild(PositionPreference.Child);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      EList<Member> _daughters = it.getDaughters();
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
      };
      Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_2);
      _daughters.add(_createFamilyMember);
    };
    final Family expectedFamily = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_addMultipleToExistingFamily() {
    this.createEmptyInsuranceDatabase();
    this.decideParentOrChild(PositionPreference.Parent);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_1 = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_3);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_2);
    };
    this._testModelFactory.changeInsuranceModel(_function_1);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_4);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_3);
    };
    this._testModelFactory.changeInsuranceModel(_function_2);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_4 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_5);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_4);
    };
    this._testModelFactory.changeInsuranceModel(_function_3);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_4 = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_5 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_6 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_6);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_5);
    };
    this._testModelFactory.changeInsuranceModel(_function_4);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_5 = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_6 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_7 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAU_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_7);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_6);
    };
    this._testModelFactory.changeInsuranceModel(_function_5);
    final Procedure1<Family> _function_6 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_7 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
      };
      it.setFather(CreatorsUtil.createFamilyMember(_function_7));
      final Procedure1<Member> _function_8 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
      };
      it.setMother(CreatorsUtil.createFamilyMember(_function_8));
      EList<Member> _sons = it.getSons();
      final Procedure1<Member> _function_9 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
      };
      Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_9);
      _sons.add(_createFamilyMember);
      EList<Member> _sons_1 = it.getSons();
      final Procedure1<Member> _function_10 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_2);
      };
      Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_10);
      _sons_1.add(_createFamilyMember_1);
      EList<Member> _daughters = it.getDaughters();
      final Procedure1<Member> _function_11 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
      };
      Member _createFamilyMember_2 = CreatorsUtil.createFamilyMember(_function_11);
      _daughters.add(_createFamilyMember_2);
      EList<Member> _daughters_1 = it.getDaughters();
      final Procedure1<Member> _function_12 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_2);
      };
      Member _createFamilyMember_3 = CreatorsUtil.createFamilyMember(_function_12);
      _daughters_1.add(_createFamilyMember_3);
    };
    final Family expectedFamily = CreatorsUtil.createFamily(_function_6);
    final Procedure1<TestModel<FamilyRegister>> _function_7 = (TestModel<FamilyRegister> it) -> {
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_7);
  }

  @Test
  public void testCreatedClient_fatherNewFamily() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Family expectedFamily1 = AbstractInsuranceToFamiliesTest.COMPLETE_FAMILY_1;
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_2);
      };
      it.setFather(CreatorsUtil.createFamilyMember(_function_2));
    };
    final Family expectedFamily2 = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      this.assertFamily(expectedFamily1, families.get(0));
      this.assertFamily(expectedFamily2, families.get(1));
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_motherNewFamily() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Family expectedFamily1 = AbstractInsuranceToFamiliesTest.COMPLETE_FAMILY_1;
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_2);
      };
      it.setMother(CreatorsUtil.createFamilyMember(_function_2));
    };
    final Family expectedFamily2 = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      this.assertFamily(expectedFamily1, families.get(0));
      this.assertFamily(expectedFamily2, families.get(1));
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_sonNewFamily() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Family expectedFamily1 = AbstractInsuranceToFamiliesTest.COMPLETE_FAMILY_1;
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      EList<Member> _sons = it.getSons();
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_2);
      };
      Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_2);
      _sons.add(_createFamilyMember);
    };
    final Family expectedFamily2 = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      this.assertFamily(expectedFamily1, families.get(0));
      this.assertFamily(expectedFamily2, families.get(1));
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_daugtherNewFamily() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAU_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Family expectedFamily1 = AbstractInsuranceToFamiliesTest.COMPLETE_FAMILY_1;
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      EList<Member> _daughters = it.getDaughters();
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_2);
      };
      Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_2);
      _daughters.add(_createFamilyMember);
    };
    final Family expectedFamily2 = CreatorsUtil.createFamily(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      this.assertFamily(expectedFamily1, families.get(0));
      this.assertFamily(expectedFamily2, families.get(1));
    };
    this._testModelFactory.validateFamilyModel(_function_2);
  }

  @Test
  public void testCreatedClient_fatherExistingFamilyBlocked() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    this.awaitReplacementInformation(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.MALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_2);
      };
      it.setFather(CreatorsUtil.createFamilyMember(_function_2));
      final Procedure1<Member> _function_3 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
      };
      it.setMother(CreatorsUtil.createFamilyMember(_function_3));
      EList<Member> _sons = it.getSons();
      final Procedure1<Member> _function_4 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
      };
      Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
      _sons.add(_createFamilyMember);
      EList<Member> _daughters = it.getDaughters();
      final Procedure1<Member> _function_5 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
      };
      Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
      _daughters.add(_createFamilyMember_1);
    };
    final Family expectedFamily1 = CreatorsUtil.createFamily(_function_1);
    final Procedure1<Family> _function_2 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_3 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
      };
      it.setFather(CreatorsUtil.createFamilyMember(_function_3));
    };
    final Family expectedFamily2 = CreatorsUtil.createFamily(_function_2);
    final Procedure1<TestModel<FamilyRegister>> _function_3 = (TestModel<FamilyRegister> it) -> {
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      this.assertFamily(expectedFamily1, families.get(0));
      this.assertFamily(expectedFamily2, families.get(1));
    };
    this._testModelFactory.validateFamilyModel(_function_3);
  }

  @Test
  public void testCreatedClient_motherExistingFamilyBlocked() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    this.awaitReplacementInformation(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_2) -> {
          it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
          it_2.setGender(Gender.FEMALE);
        };
        InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
        _insuranceclient.add(_createInsuranceClient);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<Family> _function_1 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
      };
      it.setFather(CreatorsUtil.createFamilyMember(_function_2));
      final Procedure1<Member> _function_3 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_2);
      };
      it.setMother(CreatorsUtil.createFamilyMember(_function_3));
      EList<Member> _sons = it.getSons();
      final Procedure1<Member> _function_4 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
      };
      Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
      _sons.add(_createFamilyMember);
      EList<Member> _daughters = it.getDaughters();
      final Procedure1<Member> _function_5 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
      };
      Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
      _daughters.add(_createFamilyMember_1);
    };
    final Family expectedFamily1 = CreatorsUtil.createFamily(_function_1);
    final Procedure1<Family> _function_2 = (Family it) -> {
      it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      final Procedure1<Member> _function_3 = (Member it_1) -> {
        it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
      };
      it.setMother(CreatorsUtil.createFamilyMember(_function_3));
    };
    final Family expectedFamily2 = CreatorsUtil.createFamily(_function_2);
    final Procedure1<TestModel<FamilyRegister>> _function_3 = (TestModel<FamilyRegister> it) -> {
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      this.assertFamily(expectedFamily1, families.get(0));
      this.assertFamily(expectedFamily2, families.get(1));
    };
    this._testModelFactory.validateFamilyModel(_function_3);
  }

  @Test
  public void testDeleteClient_father() {
    this.createInsuranceDatabaseWithCompleteFamily();
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient client) -> {
          String _name = client.getName();
          String _fullName = this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
          return Boolean.valueOf(Objects.equal(_name, _fullName));
        };
        InsuranceClient _findFirst = IterableExtensions.<InsuranceClient>findFirst(it_1.getInsuranceclient(), _function_2);
        _insuranceclient.remove(_findFirst);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_3));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
        _sons.add(_createFamilyMember);
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember_1);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testDeleteClient_mother() {
    this.createInsuranceDatabaseWithCompleteFamily();
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient client) -> {
          String _name = client.getName();
          String _fullName = this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
          return Boolean.valueOf(Objects.equal(_name, _fullName));
        };
        InsuranceClient _findFirst = IterableExtensions.<InsuranceClient>findFirst(it_1.getInsuranceclient(), _function_2);
        _insuranceclient.remove(_findFirst);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
        _sons.add(_createFamilyMember);
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember_1);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testDeleteClient_son() {
    this.createInsuranceDatabaseWithCompleteFamily();
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient client) -> {
          String _name = client.getName();
          String _fullName = this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
          return Boolean.valueOf(Objects.equal(_name, _fullName));
        };
        InsuranceClient _findFirst = IterableExtensions.<InsuranceClient>findFirst(it_1.getInsuranceclient(), _function_2);
        _insuranceclient.remove(_findFirst);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testDeleteClient_daughter() {
    this.createInsuranceDatabaseWithCompleteFamily();
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient client) -> {
          String _name = client.getName();
          String _fullName = this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
          return Boolean.valueOf(Objects.equal(_name, _fullName));
        };
        InsuranceClient _findFirst = IterableExtensions.<InsuranceClient>findFirst(it_1.getInsuranceclient(), _function_2);
        _insuranceclient.remove(_findFirst);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
        _sons.add(_createFamilyMember);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testDeleteClient_deleteFamilyIfEmpty() {
    this.createInsuranceDatabaseWithCompleteFamily();
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
        ArrayList<InsuranceClient> existingClients = new ArrayList<InsuranceClient>(_insuranceclient);
        for (final InsuranceClient existingClient : existingClients) {
          EList<InsuranceClient> _insuranceclient_1 = it_1.getInsuranceclient();
          _insuranceclient_1.remove(existingClient);
        }
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      this.assertNumberOfFamilies(it, 0);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeGender_MotherToFather() {
    this.createInsuranceDataBaseWithOptionalCompleteFamily(false, true, true, true);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setGender(Gender.MALE);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
        _sons.add(_createFamilyMember);
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember_1);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeGender_FatherToMother() {
    this.createInsuranceDataBaseWithOptionalCompleteFamily(true, false, true, true);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setGender(Gender.FEMALE);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_3));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
        _sons.add(_createFamilyMember);
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember_1);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeGender_DaugtherToSon() {
    this.createInsuranceDataBaseWithOptionalCompleteFamily(true, true, true, true);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_DAU_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setGender(Gender.MALE);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
        _sons.add(_createFamilyMember);
        EList<Member> _sons_1 = it_1.getSons();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_6);
        _sons_1.add(_createFamilyMember_1);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeGender_SonToDaughter() {
    this.createInsuranceDataBaseWithOptionalCompleteFamily(true, true, true, true);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setGender(Gender.FEMALE);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember);
        EList<Member> _daughters_1 = it_1.getDaughters();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_6);
        _daughters_1.add(_createFamilyMember_1);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeGender_MotherToFatherBlocked() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.awaitReplacementInformation(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setGender(Gender.MALE);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
        _sons.add(_createFamilyMember);
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember_1);
      };
      Family expectedFamily1 = CreatorsUtil.createFamily(_function_2);
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_4));
      };
      Family expectedFamily2 = CreatorsUtil.createFamily(_function_3);
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      Family family1 = families.get(0);
      Family family2 = families.get(1);
      this.assertFamily(expectedFamily1, family1);
      this.assertFamily(expectedFamily2, family2);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeGender_FatherToMotherBlocked() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.awaitReplacementInformation(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New, 0);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setGender(Gender.FEMALE);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_3));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
        _sons.add(_createFamilyMember);
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember_1);
      };
      Family expectedFamily1 = CreatorsUtil.createFamily(_function_2);
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
      };
      Family expectedFamily2 = CreatorsUtil.createFamily(_function_3);
      EList<Family> families = FamiliesQueryUtil.claimFamilyRegister(it).getFamilies();
      this.assertNumberOfFamilies(it, 2);
      Family family1 = families.get(0);
      Family family2 = families.get(1);
      this.assertFamily(expectedFamily1, family1);
      this.assertFamily(expectedFamily2, family2);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeName_firstName() {
    this.createInsuranceDatabaseWithCompleteFamily();
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_2, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_2);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
        _sons.add(_createFamilyMember);
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_6);
        _daughters.add(_createFamilyMember_1);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeName_onlyMemberInFamily() {
    this.createInsuranceDataBaseWithOptionalCompleteFamily(false, false, true, false);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_2));
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_2);
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_3);
        _sons.add(_createFamilyMember);
      };
      Family expectedFamily = CreatorsUtil.createFamily(_function_2);
      this.assertNumberOfFamilies(it, 1);
      final Family family = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_2);
      this.assertFamily(expectedFamily, family);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeName_newFamilyAsParent() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Parent);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_2));
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember);
      };
      Family expectedFamily1 = CreatorsUtil.createFamily(_function_2);
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_2);
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_4));
      };
      Family expectedFamily2 = CreatorsUtil.createFamily(_function_3);
      this.assertNumberOfFamilies(it, 2);
      final Family family1 = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily1, family1);
      final Family family2 = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_2);
      this.assertFamily(expectedFamily2, family2);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testChangeName_newFamilyAsChild() {
    this.createInsuranceDatabaseWithCompleteFamily();
    this.decideParentOrChild(PositionPreference.Child);
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it_1) -> {
        InsuranceClient _claimInsuranceClient = InsuranceQueryUtil.claimInsuranceClient(it_1, AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        _claimInsuranceClient.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_2));
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_1);
    };
    this._testModelFactory.changeInsuranceModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
        };
        it_1.setFather(CreatorsUtil.createFamilyMember(_function_3));
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
        };
        it_1.setMother(CreatorsUtil.createFamilyMember(_function_4));
        EList<Member> _daughters = it_1.getDaughters();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
        _daughters.add(_createFamilyMember);
      };
      Family expectedFamily1 = CreatorsUtil.createFamily(_function_2);
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_2);
        EList<Member> _sons = it_1.getSons();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_4);
        _sons.add(_createFamilyMember);
      };
      Family expectedFamily2 = CreatorsUtil.createFamily(_function_3);
      this.assertNumberOfFamilies(it, 2);
      final Family family1 = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_1);
      this.assertFamily(expectedFamily1, family1);
      final Family family2 = FamiliesQueryUtil.claimFamily(this.getDefaultFamilyRegister(it), AbstractInsuranceToFamiliesTest.LAST_NAME_2);
      this.assertFamily(expectedFamily2, family2);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }
}
