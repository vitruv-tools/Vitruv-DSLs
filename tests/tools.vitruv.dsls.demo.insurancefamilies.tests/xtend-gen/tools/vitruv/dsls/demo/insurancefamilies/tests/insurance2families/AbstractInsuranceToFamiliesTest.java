package tools.vitruv.dsls.demo.insurancefamilies.tests.insurance2families;

import com.google.common.base.Objects;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.change.atomic.uuid.UuidResolver;
import tools.vitruv.change.composite.propagation.ChangeableModelRepository;
import tools.vitruv.change.interaction.UserInteractionOptions;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.propagation.ChangePropagationSpecificationRepository;
import tools.vitruv.change.propagation.impl.DefaultChangeRecordingModelRepository;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.InsuranceToFamiliesChangePropagationSpecification;
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.PositionPreference;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.CreatorsUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.FamiliesQueryUtil;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceFamiliesDefaultTestModelFactory;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceFamiliesTestModelFactory;
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceQueryUtil;
import tools.vitruv.dsls.testutils.TestModel;
import tools.vitruv.testutils.TestLogging;
import tools.vitruv.testutils.TestModelRepositoryFactory;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;
import tools.vitruv.testutils.TestUserInteraction;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.views.ChangePublishingTestView;
import tools.vitruv.testutils.views.NonTransactionalTestView;
import tools.vitruv.testutils.views.UriMode;

@ExtendWith({ TestLogging.class, TestProjectManager.class })
@SuppressWarnings("all")
public abstract class AbstractInsuranceToFamiliesTest {
  @Extension
  protected InsuranceFamiliesTestModelFactory _testModelFactory;

  protected Path testProjectPath;

  private TestUserInteraction userInteraction;

  /**
   * Can be used to set a different kind of test model factory and test user interaction to be used in subclasses.
   */
  protected TestUserInteraction setTestExecutionContext(final InsuranceFamiliesTestModelFactory testModelFactory, final TestUserInteraction testUserInteraction) {
    TestUserInteraction _xblockexpression = null;
    {
      this._testModelFactory = testModelFactory;
      _xblockexpression = this.userInteraction = testUserInteraction;
    }
    return _xblockexpression;
  }

  @BeforeEach
  public final void setupViewFactory(@TestProject final Path testProjectPath) {
    try {
      this.testProjectPath = testProjectPath;
      final TestUserInteraction userInteraction = new TestUserInteraction();
      NonTransactionalTestView _prepareTestView = this.prepareTestView(testProjectPath, userInteraction);
      InsuranceFamiliesDefaultTestModelFactory _insuranceFamiliesDefaultTestModelFactory = new InsuranceFamiliesDefaultTestModelFactory(_prepareTestView);
      this.setTestExecutionContext(_insuranceFamiliesDefaultTestModelFactory, userInteraction);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private NonTransactionalTestView prepareTestView(final Path testProjectPath, final TestUserInteraction userInteraction) throws IOException {
    Iterable<ChangePropagationSpecification> _changePropagationSpecifications = this.getChangePropagationSpecifications();
    final ChangePropagationSpecificationRepository changePropagationSpecificationProvider = new ChangePropagationSpecificationRepository(_changePropagationSpecifications);
    Path _createTempDirectory = Files.createTempDirectory(null);
    final DefaultChangeRecordingModelRepository modelRepository = new DefaultChangeRecordingModelRepository(null, _createTempDirectory);
    final ChangeableModelRepository changeableModelRepository = TestModelRepositoryFactory.createTestChangeableModelRepository(modelRepository, changePropagationSpecificationProvider, userInteraction);
    UuidResolver _uuidResolver = modelRepository.getUuidResolver();
    final Function<URI, Resource> _function = (URI it) -> {
      return modelRepository.getModelResource(it);
    };
    return new ChangePublishingTestView(testProjectPath, userInteraction, UriMode.FILE_URIS, changeableModelRepository, _uuidResolver, _function);
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
    return Path.of(AbstractInsuranceToFamiliesTest.MODEL_FOLDER_NAME).resolve(((modelName + ".") + modelFileExtension));
  }

  protected URI getUri(final Path viewRelativePath) {
    return URIUtil.createFileURI(this.testProjectPath.resolve(viewRelativePath).normalize().toFile());
  }

  protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    InsuranceToFamiliesChangePropagationSpecification _insuranceToFamiliesChangePropagationSpecification = new InsuranceToFamiliesChangePropagationSpecification();
    return Collections.<ChangePropagationSpecification>unmodifiableList(CollectionLiterals.<ChangePropagationSpecification>newArrayList(_insuranceToFamiliesChangePropagationSpecification));
  }

  protected void createAndRegisterRoot(final TestModel<InsuranceDatabase> model, final InsuranceDatabase rootObject, final URI persistenceUri) {
    model.registerRoot(rootObject, persistenceUri);
  }

  protected void deleteRoot(final EObject rootObject) {
    EcoreUtil.delete(rootObject);
  }

  private void createInsuranceDatabase(final Procedure1<? super InsuranceDatabase> insuranceDatabaseInitialization) {
    final Procedure1<TestModel<InsuranceDatabase>> _function = (TestModel<InsuranceDatabase> it) -> {
      InsuranceDatabase insuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
      insuranceDatabaseInitialization.apply(insuranceDatabase);
      this.createAndRegisterRoot(it, insuranceDatabase, this.getUri(this.getProjectModelPath("insurance", AbstractInsuranceToFamiliesTest.INSURANCE_MODEL_FILE_EXTENSION)));
    };
    this._testModelFactory.changeInsuranceModel(_function);
  }

  protected String fullName(final String firstName, final String lastName) {
    return ((firstName + " ") + lastName);
  }

  protected void createEmptyInsuranceDatabase() {
    final Procedure1<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
    };
    this.createInsuranceDatabase(_function);
  }

  protected void createInsuranceDatabaseWithCompleteFamily() {
    this.createInsuranceDataBaseWithOptionalCompleteFamily(true, true, true, true);
  }

  protected void createInsuranceDataBaseWithOptionalCompleteFamily(final boolean insertFather, final boolean insertMother, final boolean insertSon, final boolean insertDaugther) {
    if ((!(((insertFather || insertMother) || insertSon) || insertDaugther))) {
      throw new IllegalArgumentException("can\'t create empty family");
    }
    int insertCount = 0;
    final Procedure1<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
    };
    this.createInsuranceDatabase(_function);
    if (insertFather) {
      this.decideParentOrChild(PositionPreference.Parent);
      final Procedure1<TestModel<InsuranceDatabase>> _function_1 = (TestModel<InsuranceDatabase> it) -> {
        InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
        final Procedure1<InsuranceDatabase> _function_2 = (InsuranceDatabase it_1) -> {
          EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
          final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_2) -> {
            it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
            it_2.setGender(Gender.MALE);
          };
          InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_3);
          _insuranceclient.add(_createInsuranceClient);
        };
        ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_2);
      };
      this._testModelFactory.changeInsuranceModel(_function_1);
      insertCount++;
    }
    if (insertMother) {
      this.decideParentOrChild(PositionPreference.Parent);
      if ((insertCount > 0)) {
        this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
      }
      final Procedure1<TestModel<InsuranceDatabase>> _function_2 = (TestModel<InsuranceDatabase> it) -> {
        InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
        final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it_1) -> {
          EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
          final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_2) -> {
            it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
            it_2.setGender(Gender.FEMALE);
          };
          InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_4);
          _insuranceclient.add(_createInsuranceClient);
        };
        ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_3);
      };
      this._testModelFactory.changeInsuranceModel(_function_2);
      insertCount++;
    }
    if (insertSon) {
      this.decideParentOrChild(PositionPreference.Child);
      if ((insertCount > 0)) {
        this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
      }
      final Procedure1<TestModel<InsuranceDatabase>> _function_3 = (TestModel<InsuranceDatabase> it) -> {
        InsuranceDatabase _claimInsuranceDatabase = InsuranceQueryUtil.claimInsuranceDatabase(it);
        final Procedure1<InsuranceDatabase> _function_4 = (InsuranceDatabase it_1) -> {
          EList<InsuranceClient> _insuranceclient = it_1.getInsuranceclient();
          final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_2) -> {
            it_2.setName(this.fullName(AbstractInsuranceToFamiliesTest.FIRST_SON_1, AbstractInsuranceToFamiliesTest.LAST_NAME_1));
            it_2.setGender(Gender.MALE);
          };
          InsuranceClient _createInsuranceClient = CreatorsUtil.createInsuranceClient(_function_5);
          _insuranceclient.add(_createInsuranceClient);
        };
        ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_claimInsuranceDatabase, _function_4);
      };
      this._testModelFactory.changeInsuranceModel(_function_3);
    }
    if (insertDaugther) {
      this.decideParentOrChild(PositionPreference.Child);
      if ((insertCount > 0)) {
        this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
      }
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
    }
  }

  protected void awaitReplacementInformation(final String insuranceClientName, final String oldFamilyName) {
    final Function1<TestUserInteraction.NotificationInteractionDescription, Boolean> _function = (TestUserInteraction.NotificationInteractionDescription it) -> {
      return Boolean.valueOf(((Objects.equal(it.getMessage(), (((((("Insurance Client " + insuranceClientName) + 
        " has been replaced by another insurance client in his family (") + oldFamilyName) + 
        "). Please decide in which family and role ") + insuranceClientName) + " should be.")) && 
        Objects.equal(it.getTitle(), "Insurance Client has been replaced in his original family")) && 
        Objects.equal(it.getNotificationType(), UserInteractionOptions.NotificationType.INFORMATION)));
    };
    this.userInteraction.acknowledgeNotification(_function);
  }

  protected void decideParentOrChild(final PositionPreference preference) {
    final String parentChildTitle = "Parent or Child?";
    final Function1<TestUserInteraction.MultipleChoiceInteractionDescription, Boolean> _function = (TestUserInteraction.MultipleChoiceInteractionDescription it) -> {
      return Boolean.valueOf(it.getTitle().equals(parentChildTitle));
    };
    TestUserInteraction.MultipleChoiceResponseBuilder _onMultipleChoiceSingleSelection = this.userInteraction.onMultipleChoiceSingleSelection(_function);
    int _xifexpression = (int) 0;
    if ((preference == PositionPreference.Parent)) {
      _xifexpression = 0;
    } else {
      _xifexpression = 1;
    }
    _onMultipleChoiceSingleSelection.respondWithChoiceAt(_xifexpression);
  }

  protected void decideNewOrExistingFamily(final FamilyPreference preference, final int familyIndex) {
    final Function1<TestUserInteraction.MultipleChoiceInteractionDescription, Boolean> _function = (TestUserInteraction.MultipleChoiceInteractionDescription it) -> {
      return Boolean.valueOf(this.assertFamilyOptions(it));
    };
    TestUserInteraction.MultipleChoiceResponseBuilder _onMultipleChoiceSingleSelection = this.userInteraction.onMultipleChoiceSingleSelection(_function);
    int _xifexpression = (int) 0;
    if ((preference == FamilyPreference.New)) {
      _xifexpression = 0;
    } else {
      _xifexpression = familyIndex;
    }
    _onMultipleChoiceSingleSelection.respondWithChoiceAt(_xifexpression);
  }

  protected void assertFamily(final Family expected, final Family actual) {
    MatcherAssert.<Family>assertThat(actual, ModelMatchers.<Family>equalsDeeply(expected));
  }

  protected void assertNumberOfFamilies(final TestModel<FamilyRegister> model, final int expectedNumberOfFamilies) {
    Assertions.assertEquals(expectedNumberOfFamilies, FamiliesQueryUtil.claimFamilyRegister(model).getFamilies().size());
  }

  private final String newOrExistingFamilyTitle = "New or Existing Family?";

  protected boolean assertFamilyOptions(final TestUserInteraction.MultipleChoiceInteractionDescription interactionDescription) {
    Assertions.assertEquals(((Object[])Conversions.unwrapArray(interactionDescription.getChoices(), Object.class))[0], "insert in a new family");
    final Iterable<String> tail = IterableExtensions.<String>drop(interactionDescription.getChoices(), 1);
    int _size = IterableExtensions.size(tail);
    boolean _greaterThan = (_size > 0);
    Assertions.assertTrue(_greaterThan);
    final String familyName = (((String[])Conversions.unwrapArray(tail, String.class))[0]).split(":")[0];
    final Consumer<String> _function = (String familyOption) -> {
      (familyOption.split(":")[0]).equals(familyName);
    };
    tail.forEach(_function);
    return interactionDescription.getTitle().equals(this.newOrExistingFamilyTitle);
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

  protected static final Family COMPLETE_FAMILY_1 = ObjectExtensions.<Family>operator_doubleArrow(FamiliesFactory.eINSTANCE.createFamily(), ((Procedure1<Family>) (Family it) -> {
    it.setLastName(AbstractInsuranceToFamiliesTest.LAST_NAME_1);
    Member _createMember = FamiliesFactory.eINSTANCE.createMember();
    final Procedure1<Member> _function = (Member it_1) -> {
      it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAD_1);
    };
    Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function);
    it.setFather(_doubleArrow);
    Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
    final Procedure1<Member> _function_1 = (Member it_1) -> {
      it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_MOM_1);
    };
    Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_1);
    it.setMother(_doubleArrow_1);
    EList<Member> _sons = it.getSons();
    Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
    final Procedure1<Member> _function_2 = (Member it_1) -> {
      it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_SON_1);
    };
    Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_2);
    _sons.add(_doubleArrow_2);
    EList<Member> _daughters = it.getDaughters();
    Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
    final Procedure1<Member> _function_3 = (Member it_1) -> {
      it_1.setFirstName(AbstractInsuranceToFamiliesTest.FIRST_DAU_1);
    };
    Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_3);
    _daughters.add(_doubleArrow_3);
  }));

  @Pure
  protected static String getFAMILY_MODEL_FILE_EXTENSION() {
    return AbstractInsuranceToFamiliesTest.FAMILY_MODEL_FILE_EXTENSION;
  }

  @Pure
  protected static String getINSURANCE_MODEL_FILE_EXTENSION() {
    return AbstractInsuranceToFamiliesTest.INSURANCE_MODEL_FILE_EXTENSION;
  }

  @Pure
  protected static String getMODEL_FOLDER_NAME() {
    return AbstractInsuranceToFamiliesTest.MODEL_FOLDER_NAME;
  }
}
