package tools.vitruv.dsls.demo.familiespersons.tests;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.metamodels.families.FamiliesFactory;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Female;
import edu.kit.ipd.sdq.metamodels.persons.Male;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import edu.kit.ipd.sdq.metamodels.persons.PersonsFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.demo.familiespersons.families2persons.FamiliesToPersonsChangePropagationSpecification;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesChangePropagationSpecification;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesHelper;
import tools.vitruv.testutils.TestLogging;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;
import tools.vitruv.testutils.TestUserInteraction;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.views.ChangePublishingTestView;
import tools.vitruv.testutils.views.TestView;

/**
 * Test to validate the transfer of changes from the PersonModel to the FamilyModel.
 * @author Dirk Neumann
 */
@ExtendWith({ TestLogging.class, TestProjectManager.class })
@SuppressWarnings("all")
public class PersonsToFamiliesTest implements TestView {
  private static final Logger logger = Logger.getLogger(PersonsToFamiliesTest.class);

  private String nameOfTestMethod = null;

  @Delegate
  private TestView testView;

  /**
   * Can be used to set a different kind of test view to be used in subclasses.
   */
  protected TestView setTestView(final TestView testView) {
    return this.testView = testView;
  }

  protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    PersonsToFamiliesChangePropagationSpecification _personsToFamiliesChangePropagationSpecification = new PersonsToFamiliesChangePropagationSpecification();
    FamiliesToPersonsChangePropagationSpecification _familiesToPersonsChangePropagationSpecification = new FamiliesToPersonsChangePropagationSpecification();
    return Collections.<ChangePropagationSpecification>unmodifiableList(CollectionLiterals.<ChangePropagationSpecification>newArrayList(_personsToFamiliesChangePropagationSpecification, _familiesToPersonsChangePropagationSpecification));
  }

  @BeforeEach
  public void prepare(final TestInfo testInfo, @TestProject final Path testProjectPath) {
    try {
      this.nameOfTestMethod = testInfo.getDisplayName();
      this.testView = this.prepareTestView(testProjectPath);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private TestView prepareTestView(final Path testProjectPath) throws IOException {
    return ChangePublishingTestView.createDefaultChangePublishingTestView(testProjectPath, this.getChangePropagationSpecifications());
  }

  @AfterEach
  public void cleanup() {
    try {
      this.testView.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private static final String FIRST_DAD_1 = "Anton";

  private static final String FIRST_MOM_1 = "Berta";

  private static final String FIRST_SON_1 = "Chris";

  private static final String FIRST_DAU_1 = "Daria";

  private static final String FIRST_DAD_2 = "Adam";

  private static final String FIRST_MOM_2 = "Birgit";

  private static final String FIRST_SON_2 = "Charles";

  private static final String FIRST_DAU_2 = "Daniela";

  private static final String LAST_NAME_1 = "Meier";

  private static final String LAST_NAME_2 = "Schulze";

  private static final String LAST_NAME_3 = "Müller";

  private static final Path PERSONS_MODEL = Path.of("model/persons.persons");

  private static final Path FAMILIES_MODEL = Path.of("model/families.families");

  private boolean preferParent = false;

  public void decideParentOrChild(final PositionPreference preference) {
    final String parentChildTitle = "Parent or Child?";
    this.preferParent = (preference == PositionPreference.Parent);
    final Function1<TestUserInteraction.MultipleChoiceInteractionDescription, Boolean> _function = (TestUserInteraction.MultipleChoiceInteractionDescription it) -> {
      return Boolean.valueOf(it.getTitle().equals(parentChildTitle));
    };
    TestUserInteraction.MultipleChoiceResponseBuilder _onMultipleChoiceSingleSelection = this.getUserInteraction().onMultipleChoiceSingleSelection(_function);
    int _xifexpression = (int) 0;
    if ((preference == PositionPreference.Parent)) {
      _xifexpression = 0;
    } else {
      _xifexpression = 1;
    }
    _onMultipleChoiceSingleSelection.respondWithChoiceAt(_xifexpression);
  }

  public void decideNewOrExistingFamily(final FamilyPreference preference) {
    int _xifexpression = (int) 0;
    if ((preference == FamilyPreference.New)) {
      _xifexpression = 0;
    } else {
      _xifexpression = 1;
    }
    this.decideNewOrExistingFamily(preference, _xifexpression);
  }

  private final String newOrExistingFamilyTitle = "New or Existing Family?";

  public void decideNewOrExistingFamily(final FamilyPreference preference, final int familyIndex) {
    final Function1<TestUserInteraction.MultipleChoiceInteractionDescription, Boolean> _function = (TestUserInteraction.MultipleChoiceInteractionDescription it) -> {
      return Boolean.valueOf(this.assertFamilyOptions(it));
    };
    TestUserInteraction.MultipleChoiceResponseBuilder _onMultipleChoiceSingleSelection = this.getUserInteraction().onMultipleChoiceSingleSelection(_function);
    int _xifexpression = (int) 0;
    if ((preference == FamilyPreference.New)) {
      _xifexpression = 0;
    } else {
      _xifexpression = familyIndex;
    }
    _onMultipleChoiceSingleSelection.respondWithChoiceAt(_xifexpression);
  }

  public boolean assertFamilyOptions(final TestUserInteraction.MultipleChoiceInteractionDescription interactionDescription) {
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
    if (this.preferParent) {
      final Function1<String, Boolean> _function_1 = (String it) -> {
        boolean _matches = it.matches(".*F:.*;.*");
        return Boolean.valueOf((!_matches));
      };
      final boolean noFathers = IterableExtensions.<String>forall(tail, _function_1);
      final Function1<String, Boolean> _function_2 = (String it) -> {
        boolean _matches = it.matches(".*M:.*;.*");
        return Boolean.valueOf((!_matches));
      };
      final boolean noMothers = IterableExtensions.<String>forall(tail, _function_2);
      Assertions.assertTrue((noFathers || noMothers));
    }
    return interactionDescription.getTitle().equals(this.newOrExistingFamilyTitle);
  }

  /**
   * Before each test a new {@link PersonRegister} has to be created as starting point.
   * This is checked by several assertions to ensure correct preconditions for the tests.
   */
  public void insertRegister() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
      _contents.add(_createPersonRegister);
    };
    this.<Resource>propagate(this.resourceAt(PersonsToFamiliesTest.PERSONS_MODEL), _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL), ModelMatchers.exists());
    Assertions.assertEquals(1, this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL).getContents().size());
    Assertions.assertEquals(1, IteratorExtensions.size(this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL).getAllContents()));
    MatcherAssert.<EObject>assertThat(this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL).getContents().get(0), CoreMatchers.<EObject>instanceOf(FamilyRegister.class));
    Assertions.assertEquals(0, IteratorExtensions.size(this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL).getContents().get(0).eAllContents()));
  }

  /**
   * Check if the actual {@link FamilyRegister looks like the expected one.
   */
  public void assertCorrectFamilyRegister(final FamilyRegister expectedFamilyRegister) {
    final Resource familyModel = this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL);
    MatcherAssert.<Resource>assertThat(familyModel, ModelMatchers.exists());
    Assertions.assertEquals(1, familyModel.getContents().size());
    final EObject familyRegister = familyModel.getContents().get(0);
    MatcherAssert.<EObject>assertThat(familyRegister, CoreMatchers.<EObject>instanceOf(FamilyRegister.class));
    final FamilyRegister castedFamilyRegister = ((FamilyRegister) familyRegister);
    MatcherAssert.<FamilyRegister>assertThat(castedFamilyRegister, ModelMatchers.<FamilyRegister>equalsDeeply(expectedFamilyRegister));
  }

  /**
   * Check if the actual {@link PersonRegister looks like the expected one.
   */
  public void assertCorrectPersonRegister(final PersonRegister expectedPersonRegister) {
    final Resource personModel = this.resourceAt(PersonsToFamiliesTest.PERSONS_MODEL);
    MatcherAssert.<Resource>assertThat(personModel, ModelMatchers.exists());
    Assertions.assertEquals(1, personModel.getContents().size());
    final EObject personRegister = personModel.getContents().get(0);
    MatcherAssert.<EObject>assertThat(personRegister, CoreMatchers.<EObject>instanceOf(PersonRegister.class));
    final PersonRegister castedPersonRegister = ((PersonRegister) personRegister);
    MatcherAssert.<PersonRegister>assertThat(castedPersonRegister, ModelMatchers.<PersonRegister>equalsDeeply(expectedPersonRegister));
  }

  /**
   * Create two families which then build the starting point for other tests
   * in which families in the {@link FamilyRegister} are needed.
   */
  @Test
  public void createFamiliesForTesting() {
    this.insertRegister();
    this.decideParentOrChild(PositionPreference.Parent);
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.decideParentOrChild(PositionPreference.Parent);
    final Consumer<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_2 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_2);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_1);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    final Consumer<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_2);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing, 1);
    final Consumer<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_3);
    this.getUserInteraction().assertAllInteractionsOccurred();
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_4 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_5 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_6);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_7);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_5);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_6 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_7);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_8 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_8);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_6);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_4);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_5 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_6 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_6);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_7 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_7);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_8 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_8);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_9 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_9);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_5);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testCreateMale_Father_EmptyRegister() {
    this.insertRegister();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.decideParentOrChild(PositionPreference.Parent);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Father_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
      EList<Person> _persons_4 = it.getPersons();
      Male _createMale_2 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_7 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow_4 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_2, _function_7);
      _persons_4.add(_doubleArrow_4);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Father_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Male _createMale_2 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_6 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_4 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Father_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow_1);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_2);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Male _createMale_2 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_6 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_4 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Father_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDad.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        _daughters.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_3);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Father_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDad.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        _daughters.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Father_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDad.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        _daughters.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow_1);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_2);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Son_EmptyRegister() {
    this.insertRegister();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.decideParentOrChild(PositionPreference.Child);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        _sons.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Son_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_3);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Male _createMale_2 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_6 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
    };
    Male _doubleArrow_4 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Son_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Male _createMale_2 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_6 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow_4 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Son_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_1 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _sons_1 = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_2);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons_1.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Male _createMale_2 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_6 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_4 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Son_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedSon = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedSon.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_3);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Son_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedSon = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedSon.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Son_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedSon = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedSon.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _sons.add(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Mother_EmptyRegister() {
    this.insertRegister();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.decideParentOrChild(PositionPreference.Parent);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setMother(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_3 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
      _persons.add(_doubleArrow);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateFemale_Mother_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setMother(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Female _createFemale_2 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_6 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_4 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateFemale_Mother_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setMother(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Female _createFemale_2 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_6 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_4 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateFemale_Mother_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Female _createFemale_2 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_6 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_4 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Mother_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedMom = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedMom.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        _sons.add(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_3);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setMother(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Mother_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedMom = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedMom.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        _sons.add(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setMother(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Mother_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedMom = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedMom.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        _sons.add(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateMale_Daughter_EmptyRegister() {
    this.insertRegister();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.decideParentOrChild(PositionPreference.Child);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        _daughters.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_3 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
      _persons.add(_doubleArrow);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateFemale_Daughter_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_3);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Female _createFemale_2 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_6 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
    };
    Female _doubleArrow_4 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateFemale_Daughter_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Female _createFemale_2 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_6 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_4 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testCreateFemale_Daughter_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_1 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
      _persons.add(_doubleArrow);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_2);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_2 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_3 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
    Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function_4 = (Male it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
    Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_5 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
    Female _createFemale_2 = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_6 = (Female it) -> {
      it.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    Female _doubleArrow_4 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_2, _function_6);
    expectedPersonRegister.getPersons().addAll(
      Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3, _doubleArrow_4)));
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Daughter_AutomaticNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDaughter = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDaughter.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_3);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_3));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Daughter_ChoosingNewFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDaughter = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDaughter.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_4);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testRename_Daughter_ChoosingExistingFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDaughter = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDaughter.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.getUserInteraction().assertAllInteractionsOccurred();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Unescapes escaped escape-sequences for linefeed, carriage return and tabulator escape-sequences.
   * Unfortunately, <code>org.junit.jupiter.params.provider.Arguments.of(...)</code> is not able
   * to deal with escape-sequences like </code>\n</code>. Therefore, these sequences have to be escaped for
   * the ParameterizedTest and then unescaped for the intended use.
   */
  public String unescapeString(final String string) {
    return string.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
  }

  /**
   * Test that error is thrown when trying to rename a {@link Person} with an empty name.
   */
  @ParameterizedTest(name = " {index} => escapedNewName= {0}, expectedExceptionMessage= {1}")
  @MethodSource("nameAndExceptionProvider")
  public void testException_CreateWithInvalidFullname(final String escapedNewName, final String expectedExceptionMessage) {
    this.insertRegister();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    String _xifexpression = null;
    if ((escapedNewName != null)) {
      _xifexpression = this.unescapeString(escapedNewName);
    } else {
      _xifexpression = null;
    }
    final String unescapedNewName = _xifexpression;
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<PersonRegister> _function_1 = (PersonRegister it) -> {
        EList<Person> _persons = it.getPersons();
        Male _createMale = PersonsFactory.eINSTANCE.createMale();
        final Procedure1<Male> _function_2 = (Male it_1) -> {
          it_1.setFullName(unescapedNewName);
        };
        Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
        _persons.add(_doubleArrow);
      };
      this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_1);
    };
    final IllegalStateException thrownException = Assertions.<IllegalStateException>assertThrows(IllegalStateException.class, _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    Assertions.assertEquals(thrownException.getMessage(), expectedExceptionMessage);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test that error is thrown when trying to rename a {@link Person} with an empty name.
   */
  @ParameterizedTest(name = " {index} => escapedNewName= {0}, expectedExceptionMessage= {1}")
  @MethodSource("nameAndExceptionProvider")
  public void testException_RenameWithInvalidFullname(final String escapedNewName, final String expectedExceptionMessage) {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    String _xifexpression = null;
    if ((escapedNewName != null)) {
      _xifexpression = this.unescapeString(escapedNewName);
    } else {
      _xifexpression = null;
    }
    final String unescapedNewName = _xifexpression;
    this.createFamiliesForTesting();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<PersonRegister> _function_1 = (PersonRegister it) -> {
        final Function1<Person, Boolean> _function_2 = (Person person) -> {
          return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
        };
        final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_2);
        searchedDad.setFullName(unescapedNewName);
      };
      this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_1);
    };
    final IllegalStateException thrownException = Assertions.<IllegalStateException>assertThrows(IllegalStateException.class, _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    Assertions.assertEquals(thrownException.getMessage(), expectedExceptionMessage);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  public static Stream<Arguments> nameAndExceptionProvider() {
    return Stream.<Arguments>of(
      Arguments.of(null, PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of("", PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of("\\n\\t\\r", PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of((PersonsToFamiliesTest.FIRST_DAD_1 + "\\n"), PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of((PersonsToFamiliesTest.FIRST_DAD_1 + "\\t"), PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of((PersonsToFamiliesTest.FIRST_DAD_1 + "\\r"), PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES));
  }

  /**
   * Test the renaming of the firstname of a single person which should
   * only effect this person and the corresponding {@link Member}.
   */
  @Test
  public void testRenamingOfFirstname() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDad.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    final Consumer<PersonRegister> _function_1 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_2 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedMom = IterableExtensions.<Person>findFirst(it.getPersons(), _function_2);
      searchedMom.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_1);
    final Consumer<PersonRegister> _function_2 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_3 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedSon = IterableExtensions.<Person>findFirst(it.getPersons(), _function_3);
      searchedSon.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_2);
    final Consumer<PersonRegister> _function_3 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_4 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDaughter = IterableExtensions.<Person>findFirst(it.getPersons(), _function_4);
      searchedDaughter.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_3);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_4 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_5 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_6);
        it_1.setFather(_doubleArrow);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_7);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_5);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_6 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_7);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_8 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_8);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_6);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_4);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_5 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_6 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_6);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_7 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_7);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_8 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_2 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_8);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_9 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_2 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_9);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_5);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @Test
  public void testChangeFamilyRoleAfterRenaming() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDad.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.decideParentOrChild(PositionPreference.Parent);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    final Consumer<PersonRegister> _function_1 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_2 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedMom = IterableExtensions.<Person>findFirst(it.getPersons(), _function_2);
      searchedMom.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_1);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.Existing);
    final Consumer<PersonRegister> _function_2 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_3 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDaughter = IterableExtensions.<Person>findFirst(it.getPersons(), _function_3);
      searchedDaughter.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_2);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    this.getUserInteraction().assertAllInteractionsOccurred();
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_3 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setMother(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_4);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_5 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_6);
        _daughters.add(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_7);
        _sons.add(_doubleArrow_2);
        EList<Member> _sons_1 = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_8 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAD_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_8);
        _sons_1.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_5);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_3);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_4 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_5);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_6);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_7 = (Male it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_7);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_8 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_8);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_4);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test different special names which do not match the scheme firstname + " " + lastname.
   * In the cases of more than two parts in the name separated by spaces, the last part is
   * the lastname and everything else is the firstname.
   * In the case of no spaces the name will be used as firstname and as lastname.
   */
  @Test
  public void testSpecialNames() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    this.decideParentOrChild(PositionPreference.Parent);
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      searchedDad.setFullName("The Earl of Dorincourt");
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    this.decideParentOrChild(PositionPreference.Parent);
    final Consumer<PersonRegister> _function_1 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_2 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedMom = IterableExtensions.<Person>findFirst(it.getPersons(), _function_2);
      searchedMom.setFullName("Cindy aus Marzahn");
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_1);
    this.decideParentOrChild(PositionPreference.Child);
    final Consumer<PersonRegister> _function_2 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_3 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedSon = IterableExtensions.<Person>findFirst(it.getPersons(), _function_3);
      searchedSon.setFullName("Saruman");
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_2);
    this.decideParentOrChild(PositionPreference.Child);
    this.decideNewOrExistingFamily(FamilyPreference.New);
    final Consumer<PersonRegister> _function_3 = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_4 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDaughter = IterableExtensions.<Person>findFirst(it.getPersons(), _function_4);
      searchedDaughter.setFullName("Daenerys_Targaryen");
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function_3);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    this.getUserInteraction().assertAllInteractionsOccurred();
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_4 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_5 = (Family it_1) -> {
        it_1.setLastName("");
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName("Saruman");
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_6);
        _sons.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_5);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_6 = (Family it_1) -> {
        it_1.setLastName("Dorincourt");
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName("The Earl of");
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_7);
        it_1.setFather(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_6);
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_7 = (Family it_1) -> {
        it_1.setLastName("Marzahn");
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_8 = (Member it_2) -> {
          it_2.setFirstName("Cindy aus");
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_8);
        it_1.setMother(_doubleArrow_2);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_7);
      Family _createFamily_3 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_8 = (Family it_1) -> {
        it_1.setLastName("");
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_9 = (Member it_2) -> {
          it_2.setFirstName("Daenerys_Targaryen");
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_9);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow_3 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_3, _function_8);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1, _doubleArrow_2, _doubleArrow_3)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_4);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_5 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_6 = (Male it_1) -> {
        it_1.setFullName("The Earl of Dorincourt");
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_6);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_7 = (Female it_1) -> {
        it_1.setFullName("Cindy aus Marzahn");
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_7);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_8 = (Male it_1) -> {
        it_1.setFullName("Saruman");
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_8);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_9 = (Female it_1) -> {
        it_1.setFullName("Daenerys_Targaryen");
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_9);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_5);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test the deletion of a person when the corresponding {@link Family} still contains other
   * {@link Member}s. In this case, this family and the remaining members should be untouched.
   */
  @Test
  public void testDeletePerson_NotLastInFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAD_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDad = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      it.getPersons().remove(searchedDad);
      final Function1<Person, Boolean> _function_2 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_SON_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedSon = IterableExtensions.<Person>findFirst(it.getPersons(), _function_2);
      it.getPersons().remove(searchedSon);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_DAU_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        _daughters.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(PersonsToFamiliesTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(PersonsToFamiliesTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(_doubleArrow, _doubleArrow_1)));
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_3 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2));
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1));
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_4);
      _persons_1.add(_doubleArrow_1);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test the deletion of a person when the corresponding {@link Family} does not contain
   * any other {@link Member}s anymore. Without members a family should not exist. (Design-Decision!)
   * Therefore, the empty family gets deleted as well.
   */
  @Test
  public void testDeletePerson_LastInFamily() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.testDeletePerson_NotLastInFamily();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<PersonRegister> _function = (PersonRegister it) -> {
      final Function1<Person, Boolean> _function_1 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_MOM_1 + " ") + PersonsToFamiliesTest.LAST_NAME_2)));
      };
      final Person searchedMom = IterableExtensions.<Person>findFirst(it.getPersons(), _function_1);
      it.getPersons().remove(searchedMom);
      final Function1<Person, Boolean> _function_2 = (Person person) -> {
        return Boolean.valueOf(person.getFullName().equals(((PersonsToFamiliesTest.FIRST_DAU_1 + " ") + PersonsToFamiliesTest.LAST_NAME_1)));
      };
      final Person searchedDau = IterableExtensions.<Person>findFirst(it.getPersons(), _function_2);
      it.getPersons().remove(searchedDau);
    };
    this.<PersonRegister>propagate(this.<PersonRegister>from(PersonRegister.class, PersonsToFamiliesTest.PERSONS_MODEL), _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final FamilyRegister expectedFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test the deletion of the {@link PersonRegister}. The corresponding {@link FamilyRegister}
   * will be then deleted as well and all contained elements in both of the registers.
   */
  @Test
  public void testDeletePersonsRegister() {
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createFamiliesForTesting();
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<Resource> _function = (Resource it) -> {
      it.getContents().clear();
    };
    this.<Resource>propagate(this.resourceAt(PersonsToFamiliesTest.PERSONS_MODEL), _function);
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    Assertions.assertEquals(0, this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL).getContents().size());
    Assertions.assertEquals(0, this.resourceAt(PersonsToFamiliesTest.PERSONS_MODEL).getContents().size());
    MatcherAssert.<Resource>assertThat(this.resourceAt(PersonsToFamiliesTest.FAMILIES_MODEL), CoreMatchers.not(ModelMatchers.exists()));
    MatcherAssert.<Resource>assertThat(this.resourceAt(PersonsToFamiliesTest.PERSONS_MODEL), CoreMatchers.not(ModelMatchers.exists()));
    PersonsToFamiliesTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  public void close() throws Exception {
    this.testView.close();
  }

  public <T extends EObject> T from(final Class<T> arg0, final Path arg1) {
    return this.testView.from(arg0, arg1);
  }

  public <T extends EObject> T from(final Class<T> arg0, final Resource arg1) {
    return this.testView.from(arg0, arg1);
  }

  public <T extends EObject> T from(final Class<T> arg0, final URI arg1) {
    return this.testView.from(arg0, arg1);
  }

  public URI getUri(final Path arg0) {
    return this.testView.getUri(arg0);
  }

  public TestUserInteraction getUserInteraction() {
    return this.testView.getUserInteraction();
  }

  public void moveTo(final Resource arg0, final Path arg1) {
    this.testView.moveTo(arg0, arg1);
  }

  public void moveTo(final Resource arg0, final URI arg1) {
    this.testView.moveTo(arg0, arg1);
  }

  public <T extends Notifier> List<PropagatedChange> propagate(final T arg0, final Consumer<T> arg1) {
    return this.testView.propagate(arg0, arg1);
  }

  public <T extends Notifier> T record(final T arg0, final Consumer<T> arg1) {
    return this.testView.record(arg0, arg1);
  }

  public Resource resourceAt(final Path arg0) {
    return this.testView.resourceAt(arg0);
  }

  public Resource resourceAt(final URI arg0) {
    return this.testView.resourceAt(arg0);
  }
}
