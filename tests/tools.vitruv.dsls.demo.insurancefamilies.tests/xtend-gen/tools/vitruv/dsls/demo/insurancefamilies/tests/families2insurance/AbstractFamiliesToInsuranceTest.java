package tools.vitruv.dsls.demo.insurancefamilies.tests.families2insurance;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.commons.util.org.eclipse.emf.common.util.URIUtil;
import edu.kit.ipd.sdq.metamodels.families.FamiliesFactory;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.demo.insurancefamilies.families2insurance.FamiliesToInsuranceChangePropagationSpecification;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.CreatorsUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.FamiliesQueryUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceFamiliesDefaultTestModelFactory;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceFamiliesTestModelFactory;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceQueryUtil;
import tools.vitruv.dsls.testutils.TestModel;
import tools.vitruv.testutils.TestLogging;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.views.ChangePublishingTestView;
import tools.vitruv.testutils.views.NonTransactionalTestView;

@ExtendWith({ TestLogging.class, TestProjectManager.class })
@SuppressWarnings("all")
public abstract class AbstractFamiliesToInsuranceTest {
  protected enum MemberRole {
    Father,

    Mother,

    Son,

    Daughter;
  }

  @Extension
  protected InsuranceFamiliesTestModelFactory _testModelFactory;

  protected Path testProjectPath;

  /**
   * Can be used to set a different kind of test model factory to be used in subclasses.
   */
  protected InsuranceFamiliesTestModelFactory setTestModelFactory(final InsuranceFamiliesTestModelFactory testModelFactory) {
    return this._testModelFactory = testModelFactory;
  }

  @BeforeEach
  public final void setupViewFactory(@TestProject final Path testProjectPath) {
    try {
      this.testProjectPath = testProjectPath;
      NonTransactionalTestView _prepareTestView = this.prepareTestView(testProjectPath);
      InsuranceFamiliesDefaultTestModelFactory _insuranceFamiliesDefaultTestModelFactory = new InsuranceFamiliesDefaultTestModelFactory(_prepareTestView);
      this.setTestModelFactory(_insuranceFamiliesDefaultTestModelFactory);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private NonTransactionalTestView prepareTestView(final Path testProjectPath) throws IOException {
    return ChangePublishingTestView.createDefaultChangePublishingTestView(testProjectPath, this.getChangePropagationSpecifications());
  }

  protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    FamiliesToInsuranceChangePropagationSpecification _familiesToInsuranceChangePropagationSpecification = new FamiliesToInsuranceChangePropagationSpecification();
    return Collections.<ChangePropagationSpecification>unmodifiableList(CollectionLiterals.<ChangePropagationSpecification>newArrayList(_familiesToInsuranceChangePropagationSpecification));
  }

  @Accessors(AccessorType.PROTECTED_GETTER)
  private static final String FAMILY_MODEL_FILE_EXTENSION = "families";

  @Accessors(AccessorType.PROTECTED_GETTER)
  private static final String INSURANCE_MODEL_FILE_EXTENSION = "insurance";

  @Accessors(AccessorType.PROTECTED_GETTER)
  private static final String MODEL_FOLDER_NAME = "model";

  protected FamilyRegister getDefaultFamilyRegister(final TestModel<FamilyRegister> model) {
    return FamiliesQueryUtil.claimFamilyRegister(model);
  }

  protected Path getProjectModelPath(final String modelName, final String modelFileExtension) {
    return Path.of(AbstractFamiliesToInsuranceTest.MODEL_FOLDER_NAME).resolve(((modelName + ".") + modelFileExtension));
  }

  protected URI getUri(final Path viewRelativePath) {
    return URIUtil.createFileURI(this.testProjectPath.resolve(viewRelativePath).normalize().toFile());
  }

  protected void createAndRegisterRoot(final TestModel<FamilyRegister> model, final FamilyRegister rootObject, final URI persistenceUri) {
    model.registerRoot(rootObject, persistenceUri);
  }

  protected void deleteRoot(final EObject rootObject) {
    EcoreUtil.delete(rootObject);
  }

  private void createFamilyRegister(final Procedure1<? super FamilyRegister> familyRegisterInitalization) {
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      FamilyRegister familyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
      familyRegisterInitalization.apply(familyRegister);
      this.createAndRegisterRoot(it, familyRegister, this.getUri(this.getProjectModelPath("families", AbstractFamiliesToInsuranceTest.FAMILY_MODEL_FILE_EXTENSION)));
    };
    this._testModelFactory.changeFamilyModel(_function);
  }

  protected void createEmptyFamilyRegister() {
    final Procedure1<FamilyRegister> _function = (FamilyRegister it) -> {
    };
    this.createFamilyRegister(_function);
  }

  protected void createOneCompleteFamily() {
    this.createOneOptionalFamily(true, true, true, true);
  }

  protected void createOneOptionalFamily(final boolean insertFather, final boolean insertMother, final boolean insertSon, final boolean insertDaugther) {
    if ((!(((insertFather || insertMother) || insertSon) || insertDaugther))) {
      throw new IllegalArgumentException("can\'t create empty family");
    }
    this.createEmptyFamilyRegister();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      FamilyRegister _claimFamilyRegister = FamiliesQueryUtil.claimFamilyRegister(it);
      final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it_1) -> {
        EList<Family> _families = it_1.getFamilies();
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_1);
          if (insertFather) {
            final Procedure1<Member> _function_3 = (Member it_3) -> {
              it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_1);
            };
            it_2.setFather(CreatorsUtil.createFamilyMember(_function_3));
          }
          if (insertMother) {
            final Procedure1<Member> _function_4 = (Member it_3) -> {
              it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_1);
            };
            it_2.setMother(CreatorsUtil.createFamilyMember(_function_4));
          }
          if (insertSon) {
            EList<Member> _sons = it_2.getSons();
            final Procedure1<Member> _function_5 = (Member it_3) -> {
              it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_SON_1);
            };
            Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
            _sons.add(_createFamilyMember);
          }
          if (insertDaugther) {
            EList<Member> _daughters = it_2.getDaughters();
            final Procedure1<Member> _function_6 = (Member it_3) -> {
              it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAU_1);
            };
            Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_6);
            _daughters.add(_createFamilyMember_1);
          }
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        _families.add(_createFamily);
      };
      ObjectExtensions.<FamilyRegister>operator_doubleArrow(_claimFamilyRegister, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    final List<InsuranceClient> expectedInsuranceClients = new ArrayList<InsuranceClient>();
    if (insertSon) {
      expectedInsuranceClients.add(AbstractFamiliesToInsuranceTest.SON11);
    }
    if (insertDaugther) {
      expectedInsuranceClients.add(AbstractFamiliesToInsuranceTest.DAU11);
    }
    if (insertFather) {
      expectedInsuranceClients.add(AbstractFamiliesToInsuranceTest.DAD11);
    }
    if (insertMother) {
      expectedInsuranceClients.add(AbstractFamiliesToInsuranceTest.MOM11);
    }
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, expectedInsuranceClients);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it_1) -> {
        this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, it_1);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_3);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  protected void createTwoCompleteFamilies() {
    this.createOneCompleteFamily();
    final Procedure1<TestModel<FamilyRegister>> _function = (TestModel<FamilyRegister> it) -> {
      FamilyRegister _claimFamilyRegister = FamiliesQueryUtil.claimFamilyRegister(it);
      final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it_1) -> {
        EList<Family> _families = it_1.getFamilies();
        final Procedure1<Family> _function_2 = (Family it_2) -> {
          it_2.setLastName(AbstractFamiliesToInsuranceTest.LAST_NAME_2);
          final Procedure1<Member> _function_3 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAD_2);
          };
          it_2.setFather(CreatorsUtil.createFamilyMember(_function_3));
          final Procedure1<Member> _function_4 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_MOM_2);
          };
          it_2.setMother(CreatorsUtil.createFamilyMember(_function_4));
          EList<Member> _sons = it_2.getSons();
          final Procedure1<Member> _function_5 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_SON_2);
          };
          Member _createFamilyMember = CreatorsUtil.createFamilyMember(_function_5);
          _sons.add(_createFamilyMember);
          EList<Member> _daughters = it_2.getDaughters();
          final Procedure1<Member> _function_6 = (Member it_3) -> {
            it_3.setFirstName(AbstractFamiliesToInsuranceTest.FIRST_DAU_2);
          };
          Member _createFamilyMember_1 = CreatorsUtil.createFamilyMember(_function_6);
          _daughters.add(_createFamilyMember_1);
        };
        Family _createFamily = CreatorsUtil.createFamily(_function_2);
        _families.add(_createFamily);
      };
      ObjectExtensions.<FamilyRegister>operator_doubleArrow(_claimFamilyRegister, _function_1);
    };
    this._testModelFactory.changeFamilyModel(_function);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      Iterables.<InsuranceClient>addAll(_insuranceclient, Collections.<InsuranceClient>unmodifiableList(CollectionLiterals.<InsuranceClient>newArrayList(AbstractFamiliesToInsuranceTest.SON11, AbstractFamiliesToInsuranceTest.DAU11, AbstractFamiliesToInsuranceTest.DAD11, AbstractFamiliesToInsuranceTest.MOM11, AbstractFamiliesToInsuranceTest.SON22, AbstractFamiliesToInsuranceTest.DAU22, AbstractFamiliesToInsuranceTest.DAD22, AbstractFamiliesToInsuranceTest.MOM22)));
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_1);
    final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
      final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it_1) -> {
        this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase, it_1);
      };
      ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_3);
    };
    this._testModelFactory.validateInsuranceModel(_function_2);
  }

  protected void assertCorrectInsuranceDatabase(final InsuranceDatabase expectedInsuranceDatabase, final InsuranceDatabase insuranceDatabase) {
    Assertions.assertEquals(expectedInsuranceDatabase.getInsuranceclient().size(), insuranceDatabase.getInsuranceclient().size());
    MatcherAssert.<EList<InsuranceClient>>assertThat(insuranceDatabase.getInsuranceclient(), ModelMatchers.containsAllOf(expectedInsuranceDatabase.getInsuranceclient()));
    MatcherAssert.<EList<InsuranceClient>>assertThat(expectedInsuranceDatabase.getInsuranceclient(), ModelMatchers.containsAllOf(insuranceDatabase.getInsuranceclient()));
  }

  public void assertCorrectFamilyRegister(final FamilyRegister expectedFamilyRegister, final FamilyRegister familyRegister) {
    MatcherAssert.<FamilyRegister>assertThat(familyRegister, ModelMatchers.<FamilyRegister>equalsDeeply(expectedFamilyRegister));
  }

  protected static final String FIRST_DAD_1 = "Anton";

  protected static final String FIRST_MOM_1 = "Berta";

  protected static final String FIRST_SON_1 = "Chris";

  protected static final String FIRST_DAU_1 = "Daria";

  protected static final String FIRST_DAD_2 = "Adam";

  protected static final String FIRST_MOM_2 = "Birgit";

  protected static final String FIRST_SON_2 = "Charles";

  protected static final String FIRST_DAU_2 = "Daniela";

  protected static final String LAST_NAME_1 = "Meier";

  protected static final String LAST_NAME_2 = "Schulze";

  protected static final String LAST_NAME_3 = "Müller";

  /**
   * Static reusable predefined InsuranceClients.
   * The first number indicates from which string set (above) the forename is.
   * the second number indicates from which string set (above) the lastname is.
   */
  protected static final InsuranceClient DAD11 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAD_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient MOM11 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_MOM_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.FEMALE);
  }));

  protected static final InsuranceClient SON11 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_SON_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient DAU11 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAU_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.FEMALE);
  }));

  protected static final InsuranceClient DAD12 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAD_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient MOM12 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_MOM_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.FEMALE);
  }));

  protected static final InsuranceClient SON12 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_SON_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient DAU12 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAU_1 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.FEMALE);
  }));

  protected static final InsuranceClient DAD21 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAD_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient MOM21 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_MOM_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.FEMALE);
  }));

  protected static final InsuranceClient SON21 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_SON_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient DAU21 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAU_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_1));
    it.setGender(Gender.FEMALE);
  }));

  protected static final InsuranceClient DAD22 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAD_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient MOM22 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_MOM_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.FEMALE);
  }));

  protected static final InsuranceClient SON22 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_SON_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.MALE);
  }));

  protected static final InsuranceClient DAU22 = CreatorsUtil.createInsuranceClient(((Procedure1<InsuranceClient>) (InsuranceClient it) -> {
    it.setName(((AbstractFamiliesToInsuranceTest.FIRST_DAU_2 + " ") + AbstractFamiliesToInsuranceTest.LAST_NAME_2));
    it.setGender(Gender.FEMALE);
  }));

  @Pure
  protected static String getFAMILY_MODEL_FILE_EXTENSION() {
    return AbstractFamiliesToInsuranceTest.FAMILY_MODEL_FILE_EXTENSION;
  }

  @Pure
  protected static String getINSURANCE_MODEL_FILE_EXTENSION() {
    return AbstractFamiliesToInsuranceTest.INSURANCE_MODEL_FILE_EXTENSION;
  }

  @Pure
  protected static String getMODEL_FOLDER_NAME() {
    return AbstractFamiliesToInsuranceTest.MODEL_FOLDER_NAME;
  }
}
