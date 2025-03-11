package tools.vitruv.dsls.demo.insurancefamilies.tests.families2insurance;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.metamodels.families.FamiliesFactory;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.dsls.demo.insurancefamilies.families2insurance.FamiliesToInsuranceHelper;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.CreatorsUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.FamiliesQueryUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceQueryUtil;
import tools.vitruv.dsls.testutils.TestModel;

@SuppressWarnings("all")
public class FamiliesToInsuranceTest extends AbstractFamiliesToInsuranceTest {
  @Test
  public void testDeleteFamilyRegister() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      FamilyRegister familyRegister = FamiliesQueryUtil.claimFamilyRegister(it);
      this.deleteRoot(familyRegister);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      Assertions.assertEquals(0, it.getRootObjects().size());
    };
    this._testModelFactory.validateFamilyModel(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      Assertions.assertEquals(0, it.getRootObjects().size());
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void deleteFamilyWithMatchingName() {
    this.createTwoCompleteFamilies();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Predicate<Family> _function_2 = (Family it_2) -> {
          return it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        it_1.removeIf(_function_2);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testInsertNewFamily() {
    this.createEmptyFamilyRegister();
    final Procedure1<Family> _function = (Family it) -> {
      it.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_1);
    };
    final Family family = CreatorsUtil.createFamily(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        it_1.add(family);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
    };
    final InsuranceDatabase expectedInsuraceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuraceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void insertFamilyWithFather() {
    this.createEmptyFamilyRegister();
    final Procedure1<Family> _function = (Family it) -> {
      it.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_1);
    };
    final Family family = CreatorsUtil.createFamily(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        it_1.add(family);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_1);
        };
        family.setFather(CreatorsUtil.createFamilyMember(_function_3));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAD_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_3);
      _insuranceclient.add(_createInsuranceClient);
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void insertFamilyWithMother() {
    this.createEmptyFamilyRegister();
    final Procedure1<Family> _function = (Family it) -> {
      it.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_1);
    };
    final Family family = CreatorsUtil.createFamily(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        it_1.add(family);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_1);
        };
        family.setMother(CreatorsUtil.createFamilyMember(_function_3));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(((AbstractFamiliesToInsuranceTest.FIRST_MOM_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_3);
      _insuranceclient.add(_createInsuranceClient);
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void insertFamilyWithSon() {
    this.createEmptyFamilyRegister();
    final Procedure1<Family> _function = (Family it) -> {
      it.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_1);
    };
    final Family family = CreatorsUtil.createFamily(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        it_1.add(family);
        EList<Member> _sons = family.getSons();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_SON_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_3);
        _sons.add(_createFamilyMember);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(((AbstractFamiliesToInsuranceTest.FIRST_SON_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_3);
      _insuranceclient.add(_createInsuranceClient);
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void insertFamilyWithDaughter() {
    this.createEmptyFamilyRegister();
    final Procedure1<Family> _function = (Family it) -> {
      it.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_1);
    };
    final Family family = CreatorsUtil.createFamily(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        it_1.add(family);
        EList<Member> _daughters = family.getDaughters();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAU_1);
        };
        Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_3);
        _daughters.add(_createFamilyMember);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAU_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_3);
      _insuranceclient.add(_createInsuranceClient);
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void deleteFatherFromFamily() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && it_2.getFather().getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAD_1)));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        selectedFamily.setFather(null);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.MOM11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void deleteMotherFromFamily() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && it_2.getMother().getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_MOM_1)));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        selectedFamily.setMother(null);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void deleteSonFromFamily() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && it_2.getSons().stream().anyMatch(((Predicate<Member>) (Member s) -> {
            return s.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_1);
          }))));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Predicate<Member> _function_3 = (Member s) -> {
          return s.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_1);
        };
        selectedFamily.getSons().removeIf(_function_3);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void deleteDautherFromFamily() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && it_2.getDaughters().stream().anyMatch(((Predicate<Member>) (Member s) -> {
            return s.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_1);
          }))));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Predicate<Member> _function_3 = (Member s) -> {
          return s.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_1);
        };
        selectedFamily.getDaughters().removeIf(_function_3);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testChangelastName() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        selectedFamily.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON12, AbstractFamiliesToInsuranceTest.DAU12, AbstractFamiliesToInsuranceTest.DAD12, AbstractFamiliesToInsuranceTest.MOM12)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testChangeFirstNameFather() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && it_2.getFather().getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAD_1)));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        Member _father = selectedFamily.getFather();
        _father.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_2);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD21, AbstractFamiliesToInsuranceTest.MOM11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testChangeFirstNameMother() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && it_2.getMother().getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_MOM_1)));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        Member _mother = selectedFamily.getMother();
        _mother.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_2);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM21)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testChangeFirstNameSon() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && IterableExtensions.<Member>exists(it_2.getSons(), ((Function1<Member, Boolean>) (Member son) -> {
            return Boolean.valueOf(son.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_1));
          }))));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Function1<Member, Boolean> _function_3 = (Member it_2) -> {
          return Boolean.valueOf(it_2.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_1));
        };
        final Member sonToChange = IterableExtensions.<Member>findFirst(selectedFamily.getSons(), _function_3);
        sonToChange.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_SON_2);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON21, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testChangeFirstNameDaugther() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf((it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1) && IterableExtensions.<Member>exists(it_2.getDaughters(), ((Function1<Member, Boolean>) (Member it_3) -> {
            return Boolean.valueOf(it_3.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_1));
          }))));
        };
        final Family selectedFamily = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Function1<Member, Boolean> _function_3 = (Member it_2) -> {
          return Boolean.valueOf(it_2.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_1));
        };
        final Member daughterToChange = IterableExtensions.<Member>findFirst(selectedFamily.getDaughters(), _function_3);
        daughterToChange.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAU_2);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU21, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testReplaceFatherWithNewMember() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_2);
        };
        family.setFather(CreatorsUtil.createFamilyMember(_function_3));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.DAD21)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testReplaceFatherWithExistingFather() {
    this.createTwoCompleteFamilies();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_3);
        family1.setFather(family2.getFather());
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD21, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON22, AbstractFamiliesToInsuranceTest.DAU22, AbstractFamiliesToInsuranceTest.MOM22)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testReplaceFatherWithExistingPreviouslyLonlyFather() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
          final Procedure1<Member> _function_3 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_2);
          };
          it_2.setFather(CreatorsUtil.createFamilyMember(_function_3));
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_4);
        family1.setFather(family2.getFather());
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD21, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testReplaceFatherWithExistingSon() {
    this.createTwoCompleteFamilies();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Member, Boolean> _function_4 = (Member son) -> {
          return Boolean.valueOf(son.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_2));
        };
        family1.setFather(IterableExtensions.<Member>findFirst(family2.getSons(), _function_4));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON21, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD22, AbstractFamiliesToInsuranceTest.MOM22, AbstractFamiliesToInsuranceTest.DAU22)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testReplaceMotherWithNewMember() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_2);
        };
        family.setMother(CreatorsUtil.createFamilyMember(_function_3));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM21)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testReplaceMotherWithExistingMother() {
    this.createTwoCompleteFamilies();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_3);
        family1.setMother(family2.getMother());
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.MOM21, AbstractFamiliesToInsuranceTest.SON22, AbstractFamiliesToInsuranceTest.DAU22, AbstractFamiliesToInsuranceTest.DAD22)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testReplaceMotherWithExistingPreviouslyLonlyMother() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
          final Procedure1<Member> _function_3 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_2);
          };
          it_2.setMother(CreatorsUtil.createFamilyMember(_function_3));
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_4);
        family1.setMother(family2.getMother());
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM21, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testReplaceMotherWithExistingDaughter() {
    this.createTwoCompleteFamilies();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_2);
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Member, Boolean> _function_4 = (Member daugther) -> {
          return Boolean.valueOf(daugther.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_2));
        };
        family1.setMother(IterableExtensions.<Member>findFirst(family2.getDaughters(), _function_4));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.DAU21, AbstractFamiliesToInsuranceTest.SON22, AbstractFamiliesToInsuranceTest.DAD22, AbstractFamiliesToInsuranceTest.MOM22)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  @Test
  public void testSwitchFamilySamePositionFather() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
        final Procedure1<Family> _function_3 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily_1 = CreatorsUtil.createFamily(_function_3);
        it_1.add(_createFamily_1);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        newFamily.setFather(oldFamily.getFather());
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD12, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testSwitchFamilySamePositionMother() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        newFamily.setMother(oldFamily.getMother());
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM12, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testSwitchFamilySamePositionSon() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        EList<Member> _sons = newFamily.getSons();
        final Function1<Member, Boolean> _function_5 = (Member son) -> {
          return Boolean.valueOf(son.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_1));
        };
        Member _findFirst = IterableExtensions.<Member>findFirst(oldFamily.getSons(), _function_5);
        _sons.add(_findFirst);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON12, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testSwitchFamilySamePositionDaugther() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        EList<Member> _daughters = newFamily.getDaughters();
        final Function1<Member, Boolean> _function_5 = (Member daughter) -> {
          return Boolean.valueOf(daughter.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_1));
        };
        Member _findFirst = IterableExtensions.<Member>findFirst(oldFamily.getDaughters(), _function_5);
        _daughters.add(_findFirst);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU12)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testRepetedlyMovingFatherBetweenFamilies() {
    this.createEmptyFamilyRegister();
    final String first_mom_3 = "Beate";
    final Procedure1<InsuranceClient> _function = (InsuranceClient it) -> {
      it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAD_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_3));
      it.setGender(Gender.MALE);
    };
    final InsuranceClient dad13 = CreatorsUtil.createInsuranceClient(_function);
    final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it) -> {
      it.setName(((first_mom_3 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_3));
      it.setGender(Gender.FEMALE);
    };
    final InsuranceClient mom33 = CreatorsUtil.createInsuranceClient(_function_1);
    final Procedure1<TestModel<FamilyRegister>> _function_2 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_3 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_4 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_1);
        };
        final Family family1 = CreatorsUtil.createFamily(_function_4);
        final Procedure1<Family> _function_5 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        final Family family2 = CreatorsUtil.createFamily(_function_5);
        final Procedure1<Family> _function_6 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_3);
        };
        final Family family3 = CreatorsUtil.createFamily(_function_6);
        Iterables.<Family>addAll(it_1, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(family1, family2, family3)));
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_1);
        };
        family1.setFather(CreatorsUtil.createFamilyMember(_function_7));
        final Procedure1<Member> _function_8 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_2);
        };
        family2.setFather(CreatorsUtil.createFamilyMember(_function_8));
        final Procedure1<Member> _function_9 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_1);
        };
        family1.setMother(CreatorsUtil.createFamilyMember(_function_9));
        final Procedure1<Member> _function_10 = (Member it_2) -> {
          it_2.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_2);
        };
        family2.setMother(CreatorsUtil.createFamilyMember(_function_10));
        final Procedure1<Member> _function_11 = (Member it_2) -> {
          it_2.setFirstName(first_mom_3);
        };
        family3.setMother(CreatorsUtil.createFamilyMember(_function_11));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_3);
    };
    this._testModelFactory.changeFamilyModel(_function_2);
    final Procedure1<TestModel<FamilyRegister>> _function_3 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_4 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_5 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_5);
        final Function1<Family, Boolean> _function_6 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_6);
        final Function1<Family, Boolean> _function_7 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_3));
        };
        final Family family3 = IterableExtensions.<Family>findFirst(it_1, _function_7);
        family3.setFather(family2.getFather());
        family2.setFather(family1.getFather());
        family1.setFather(family3.getFather());
        family3.setFather(family2.getFather());
        family2.setFather(family1.getFather());
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_4);
    };
    this._testModelFactory.changeFamilyModel(_function_3);
    final Procedure1<InsuranceDatabase> _function_4 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(dad13, AbstractFamiliesToInsuranceTest.DAD22, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.MOM22, mom33)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_4);
    final Procedure1<TestModel<InsuranceDatabase>> _function_5 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_5);
  }

  @Test
  public void testSwitchSonToFather() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        final Function1<Member, Boolean> _function_5 = (Member son) -> {
          return Boolean.valueOf(son.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_1));
        };
        newFamily.setFather(IterableExtensions.<Member>findFirst(oldFamily.getSons(), _function_5));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON12, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testSwitchFatherToSon() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        EList<Member> _sons = newFamily.getSons();
        Member _father = oldFamily.getFather();
        _sons.add(_father);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD12, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testSwitchDautherToMother() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        final Function1<Member, Boolean> _function_5 = (Member daughter) -> {
          return Boolean.valueOf(daughter.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_1));
        };
        newFamily.setMother(IterableExtensions.<Member>findFirst(oldFamily.getDaughters(), _function_5));
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU12)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void testSwitchMotherToDaughter() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_3 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family oldFamily = IterableExtensions.<Family>findFirst(it_1, _function_3);
        final Function1<Family, Boolean> _function_4 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
        };
        final Family newFamily = IterableExtensions.<Family>findFirst(it_1, _function_4);
        EList<Member> _daughters = newFamily.getDaughters();
        Member _mother = oldFamily.getMother();
        _daughters.add(_mother);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
    };
    this._testModelFactory.changeFamilyModel(_function_1);
    final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM12, AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_2);
    final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_3);
  }

  @Test
  public void familyGetsDeletedIfEmpty_father() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family = IterableExtensions.<Family>findFirst(it_1, _function_2);
        family.setMother(null);
        EList<Member> _sons = family.getSons();
        EList<Member> _sons_1 = family.getSons();
        Iterables.removeAll(_sons, _sons_1);
        EList<Member> _daughters = family.getDaughters();
        EList<Member> _daughters_1 = family.getDaughters();
        Iterables.removeAll(_daughters, _daughters_1);
        family.setFather(null);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final FamilyRegister expectedFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final FamilyRegister familiyRegister = FamiliesQueryUtil.claimFamilyRegister(it);
      this.assertCorrectFamilyRegister(expectedFamilyRegister, familiyRegister);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void familyGetsDeletedIfEmpty_mother() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family = IterableExtensions.<Family>findFirst(it_1, _function_2);
        family.setFather(null);
        EList<Member> _sons = family.getSons();
        EList<Member> _sons_1 = family.getSons();
        Iterables.removeAll(_sons, _sons_1);
        EList<Member> _daughters = family.getDaughters();
        EList<Member> _daughters_1 = family.getDaughters();
        Iterables.removeAll(_daughters, _daughters_1);
        family.setMother(null);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final FamilyRegister expectedFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final FamilyRegister familiyRegister = FamiliesQueryUtil.claimFamilyRegister(it);
      this.assertCorrectFamilyRegister(expectedFamilyRegister, familiyRegister);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void familyGetsDeletedIfEmpty_son() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family = IterableExtensions.<Family>findFirst(it_1, _function_2);
        family.setMother(null);
        EList<Member> _daughters = family.getDaughters();
        EList<Member> _daughters_1 = family.getDaughters();
        Iterables.removeAll(_daughters, _daughters_1);
        family.setFather(null);
        EList<Member> _sons = family.getSons();
        EList<Member> _sons_1 = family.getSons();
        Iterables.removeAll(_sons, _sons_1);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final FamilyRegister expectedFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final FamilyRegister familiyRegister = FamiliesQueryUtil.claimFamilyRegister(it);
      this.assertCorrectFamilyRegister(expectedFamilyRegister, familiyRegister);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void familyGetsDeletedIfEmpty_daugther() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Function1<Family, Boolean> _function_2 = (Family it_2) -> {
          return Boolean.valueOf(it_2.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
        };
        final Family family = IterableExtensions.<Family>findFirst(it_1, _function_2);
        family.setMother(null);
        EList<Member> _sons = family.getSons();
        EList<Member> _sons_1 = family.getSons();
        Iterables.removeAll(_sons, _sons_1);
        family.setFather(null);
        EList<Member> _daughters = family.getDaughters();
        EList<Member> _daughters_1 = family.getDaughters();
        Iterables.removeAll(_daughters, _daughters_1);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final FamilyRegister expectedFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
      final FamilyRegister familiyRegister = FamiliesQueryUtil.claimFamilyRegister(it);
      this.assertCorrectFamilyRegister(expectedFamilyRegister, familiyRegister);
    };
    this._testModelFactory.validateFamilyModel(_function_1);
  }

  @Test
  public void testExceptionSexChanges_AssignMotherToFather() {
    this.createTwoCompleteFamilies();
    final Executable _function = () -> {
      final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
        EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
        final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
          final Function1<Family, Boolean> _function_3 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
          };
          final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
          final Function1<Family, Boolean> _function_4 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
          };
          final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_4);
          family1.setFather(family2.getMother());
        };
        ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
      };
      this._testModelFactory.changeFamilyModel(_function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignMotherToFather = Assertions.<UnsupportedOperationException>assertThrows(
      UnsupportedOperationException.class, _function);
    final String expectedMessage = "The position of a male family member can only be assigned to members with no or a male corresponding insurance client.";
    Assertions.assertEquals(expectedMessage, thrownExceptionAssignMotherToFather.getMessage());
  }

  @Test
  public void testExceptionSexChanges_AssignDaughterToSon() {
    this.createTwoCompleteFamilies();
    final Executable _function = () -> {
      final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
        EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
        final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
          final Function1<Family, Boolean> _function_3 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
          };
          final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
          final Function1<Family, Boolean> _function_4 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
          };
          final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_4);
          EList<Member> _sons = family1.getSons();
          final Function1<Member, Boolean> _function_5 = (Member daughter) -> {
            return Boolean.valueOf(daughter.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_2));
          };
          Member _findFirst = IterableExtensions.<Member>findFirst(family2.getDaughters(), _function_5);
          _sons.add(_findFirst);
        };
        ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
      };
      this._testModelFactory.changeFamilyModel(_function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignDaughterToSon = Assertions.<UnsupportedOperationException>assertThrows(
      UnsupportedOperationException.class, _function);
    final String expectedMessage = "The position of a male family member can only be assigned to members with no or a male corresponding insurance client.";
    Assertions.assertEquals(expectedMessage, thrownExceptionAssignDaughterToSon.getMessage());
  }

  @Test
  public void testExceptionSexChanges_AssignFatherToMother() {
    this.createTwoCompleteFamilies();
    final Executable _function = () -> {
      final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
        EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
        final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
          final Function1<Family, Boolean> _function_3 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
          };
          final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
          final Function1<Family, Boolean> _function_4 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
          };
          final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_4);
          family1.setMother(family2.getFather());
        };
        ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
      };
      this._testModelFactory.changeFamilyModel(_function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignFatherToMother = Assertions.<UnsupportedOperationException>assertThrows(
      UnsupportedOperationException.class, _function);
    final String expectedMessage = "The position of a female family member can only be assigned to members with no or a female corresponding insurance client.";
    Assertions.assertEquals(expectedMessage, thrownExceptionAssignFatherToMother.getMessage());
  }

  @Test
  public void testExceptionSexChanges_AssignSonToDaughter() {
    this.createTwoCompleteFamilies();
    final Executable _function = () -> {
      final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
        EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
        final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
          final Function1<Family, Boolean> _function_3 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
          };
          final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
          final Function1<Family, Boolean> _function_4 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_2));
          };
          final Family family2 = IterableExtensions.<Family>findFirst(it_1, _function_4);
          EList<Member> _daughters = family1.getDaughters();
          final Function1<Member, Boolean> _function_5 = (Member son) -> {
            return Boolean.valueOf(son.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_2));
          };
          Member _findFirst = IterableExtensions.<Member>findFirst(family2.getSons(), _function_5);
          _daughters.add(_findFirst);
        };
        ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
      };
      this._testModelFactory.changeFamilyModel(_function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignSonToDaughter = Assertions.<UnsupportedOperationException>assertThrows(
      UnsupportedOperationException.class, _function);
    final String expectedMessage = "The position of a female family member can only be assigned to members with no or a female corresponding insurance client.";
    Assertions.assertEquals(expectedMessage, thrownExceptionAssignSonToDaughter.getMessage());
  }

  public String unescapeString(final String string) {
    return string.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
  }

  @ParameterizedTest(name = "{index} => role={0}, escapedNewName={1}, expectedExceptionMessage={2}")
  @MethodSource("nameAndExceptionProvider")
  public void testExceptionRenamingMemberWithInvalidFirstName(final AbstractFamiliesToInsuranceTest.MemberRole role, final String escapedNewName, final String expectedExceptionMessage) {
    String _xifexpression = null;
    if ((escapedNewName != null)) {
      _xifexpression = this.unescapeString(escapedNewName);
    } else {
      _xifexpression = null;
    }
    final String unescapedNewName = _xifexpression;
    this.createOneCompleteFamily();
    final Executable _function = () -> {
      final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
        EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
        final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
          final Function1<Family, Boolean> _function_3 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
          };
          final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
          if (role != null) {
            switch (role) {
              case Father:
                Member _father = family1.getFather();
                _father.setFirstName(unescapedNewName);
                break;
              case Mother:
                Member _mother = family1.getMother();
                _mother.setFirstName(unescapedNewName);
                break;
              case Son:
                final Function1<Member, Boolean> _function_4 = (Member son) -> {
                  return Boolean.valueOf(son.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_SON_1));
                };
                Member _findFirst = IterableExtensions.<Member>findFirst(family1.getSons(), _function_4);
                _findFirst.setFirstName(unescapedNewName);
                break;
              case Daughter:
                final Function1<Member, Boolean> _function_5 = (Member daughter) -> {
                  return Boolean.valueOf(daughter.getFirstName().equals(AbstractFamiliesToInsuranceTest.FIRST_DAU_1));
                };
                Member _findFirst_1 = IterableExtensions.<Member>findFirst(family1.getDaughters(), _function_5);
                _findFirst_1.setFirstName(unescapedNewName);
                break;
              default:
                break;
            }
          }
        };
        ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
      };
      this._testModelFactory.changeFamilyModel(_function_1);
    };
    final IllegalStateException thrownExceptionSetNullAsFirstName = Assertions.<IllegalStateException>assertThrows(
      IllegalStateException.class, _function);
    final String expectedMessage = expectedExceptionMessage;
    Assertions.assertEquals(expectedMessage, thrownExceptionSetNullAsFirstName.getMessage());
  }

  @ParameterizedTest(name = "{index} => role={0}, escapedNewName={1}, expectedExceptionMessage={2}")
  @MethodSource("nameAndExceptionProvider")
  public void testExceptionCreationOfMemberWithInvalidFirstName(final AbstractFamiliesToInsuranceTest.MemberRole role, final String escapedNewName, final String expectedExceptionMessage) {
    String _xifexpression = null;
    if ((escapedNewName != null)) {
      _xifexpression = this.unescapeString(escapedNewName);
    } else {
      _xifexpression = null;
    }
    final String unescapedNewName = _xifexpression;
    this.createOneCompleteFamily();
    final Executable _function = () -> {
      final Procedure1<TestModel<FamilyRegister>> _function_1 = (TestModel<FamilyRegister> it) -> {
        EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
        final Procedure1<EList<Family>> _function_2 = (EList<Family> it_1) -> {
          final Function1<Family, Boolean> _function_3 = (Family family) -> {
            return Boolean.valueOf(family.getLastName().equals(AbstractFamiliesToInsuranceTest.LAST_NAME_1));
          };
          final Family family1 = IterableExtensions.<Family>findFirst(it_1, _function_3);
          final Procedure1<Member> _function_4 = (Member it_2) -> {
            it_2.setFirstName(unescapedNewName);
          };
          final Member newMember = CreatorsUtil.createFamilyMember(_function_4);
          if (role != null) {
            switch (role) {
              case Father:
                family1.setFather(newMember);
                break;
              case Mother:
                family1.setMother(newMember);
                break;
              case Son:
                EList<Member> _sons = family1.getSons();
                _sons.add(newMember);
                break;
              case Daughter:
                EList<Member> _daughters = family1.getDaughters();
                _daughters.add(newMember);
                break;
              default:
                break;
            }
          }
        };
        ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_2);
      };
      this._testModelFactory.changeFamilyModel(_function_1);
    };
    final IllegalStateException thrownExceptionSetNullAsFirstName = Assertions.<IllegalStateException>assertThrows(
      IllegalStateException.class, _function);
    final String expectedMessage = expectedExceptionMessage;
    Assertions.assertEquals(expectedMessage, thrownExceptionSetNullAsFirstName.getMessage());
  }

  public static Stream<Arguments> nameAndExceptionProvider() {
    return Stream.<Arguments>of(
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Father, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Father, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Father, "\\n\\t\\r", 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Father, (AbstractFamiliesToInsuranceTest.FIRST_DAD_1 + "\\n"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Father, (AbstractFamiliesToInsuranceTest.FIRST_DAD_1 + "\\t"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Father, (AbstractFamiliesToInsuranceTest.FIRST_DAD_1 + "\\r"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Mother, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Mother, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Mother, "\\t\\n\\r", 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Mother, (AbstractFamiliesToInsuranceTest.FIRST_MOM_1 + "\\n"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Mother, (AbstractFamiliesToInsuranceTest.FIRST_MOM_1 + "\\t"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Mother, (AbstractFamiliesToInsuranceTest.FIRST_MOM_1 + "\\r"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Son, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Son, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Son, "\\n\\t\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Son, (AbstractFamiliesToInsuranceTest.FIRST_SON_1 + "\\n"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Son, (AbstractFamiliesToInsuranceTest.FIRST_SON_1 + "\\t"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Son, (AbstractFamiliesToInsuranceTest.FIRST_SON_1 + "\\r"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Daughter, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Daughter, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Daughter, "\\t\\n\\r", 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Daughter, (AbstractFamiliesToInsuranceTest.FIRST_DAU_1 + "\\n"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Daughter, (AbstractFamiliesToInsuranceTest.FIRST_DAU_1 + "\\t"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(AbstractFamiliesToInsuranceTest.MemberRole.Daughter, (AbstractFamiliesToInsuranceTest.FIRST_DAU_1 + "\\r"), 
        FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES));
  }

  @Test
  public void testCreatingFamilyWithEmptylastName() {
    this.createEmptyFamilyRegister();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      EList<Family> _claimFamilies = FamiliesQueryUtil.claimFamilies(it);
      final Procedure1<EList<Family>> _function_1 = (EList<Family> it_1) -> {
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName("");
          final Procedure1<Member> _function_3 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_1);
          };
          it_2.setFather(CreatorsUtil.createFamilyMember(_function_3));
          final Procedure1<Member> _function_4 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_1);
          };
          it_2.setMother(CreatorsUtil.createFamilyMember(_function_4));
          EList<Member> _sons = it_2.getSons();
          final Procedure1<Member> _function_5 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_SON_1);
          };
          Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
          _sons.add(_createFamilyMember);
          EList<Member> _daughters = it_2.getDaughters();
          final Procedure1<Member> _function_6 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAU_1);
          };
          Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_6);
          _daughters.add(_createFamilyMember_1);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        it_1.add(_createFamily);
      };
      ObjectExtensions.<EList<Family>>operator_doubleArrow(_claimFamilies, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(AbstractFamiliesToInsuranceTest.FIRST_SON_1);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_2);
      _insuranceclient.add(_createInsuranceClient);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(AbstractFamiliesToInsuranceTest.FIRST_DAU_1);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _createInsuranceClient_1 = CreatorsUtil.createInsuranceClient(_function_3);
      _insuranceclient_1.add(_createInsuranceClient_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(AbstractFamiliesToInsuranceTest.FIRST_DAD_1);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _createInsuranceClient_2 = CreatorsUtil.createInsuranceClient(_function_4);
      _insuranceclient_2.add(_createInsuranceClient_2);
      EList<InsuranceClient> _insuranceclient_3 = it.getInsuranceclient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName(AbstractFamiliesToInsuranceTest.FIRST_MOM_1);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _createInsuranceClient_3 = CreatorsUtil.createInsuranceClient(_function_5);
      _insuranceclient_3.add(_createInsuranceClient_3);
    };
    final InsuranceDatabase expectedInsuranceDatabase = CreatorsUtil.createInsuranceDatabase(_function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      final InsuranceDatabase insuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, insuranceDatabase);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }
}
