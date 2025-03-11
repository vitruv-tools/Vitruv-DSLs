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
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
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
import tools.vitruv.dsls.demo.familiespersons.families2persons.FamiliesToPersonsHelper;
import tools.vitruv.dsls.demo.familiespersons.persons2families.PersonsToFamiliesChangePropagationSpecification;
import tools.vitruv.testutils.TestLogging;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;
import tools.vitruv.testutils.TestUserInteraction;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.views.ChangePublishingTestView;
import tools.vitruv.testutils.views.TestView;

/**
 * Test to validate the transfer of changes from the FamilyModel to the PersonModel.
 * @author Dirk Neumann
 */
@ExtendWith({ TestLogging.class, TestProjectManager.class })
@SuppressWarnings("all")
public class FamiliesToPersonsTest implements TestView {
  private static final Logger logger = Logger.getLogger(FamiliesToPersonsTest.class);

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
    FamiliesToPersonsChangePropagationSpecification _familiesToPersonsChangePropagationSpecification = new FamiliesToPersonsChangePropagationSpecification();
    PersonsToFamiliesChangePropagationSpecification _personsToFamiliesChangePropagationSpecification = new PersonsToFamiliesChangePropagationSpecification();
    return Collections.<ChangePropagationSpecification>unmodifiableList(CollectionLiterals.<ChangePropagationSpecification>newArrayList(_familiesToPersonsChangePropagationSpecification, _personsToFamiliesChangePropagationSpecification));
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

  /**
   * Static reusable predefined Persons.
   * The first number indicates from which string set (above) the forename is.
   * the second number indicates from which string set (above) the lastname is.
   */
  private static final Male DAD11 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAD_1 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Female MOM11 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_MOM_1 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Male SON11 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_SON_1 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Female DAU11 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAU_1 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Male DAD12 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAD_1 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  private static final Female MOM12 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_MOM_1 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  private static final Male SON12 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_SON_1 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  private static final Female DAU12 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAU_1 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  private static final Male DAD21 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAD_2 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Female MOM21 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_MOM_2 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Male SON21 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_SON_2 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Female DAU21 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAU_2 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
  }));

  private static final Male DAD22 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAD_2 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  private static final Female MOM22 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_MOM_2 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  private static final Male SON22 = ObjectExtensions.<Male>operator_doubleArrow(PersonsFactory.eINSTANCE.createMale(), ((Procedure1<Male>) (Male it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_SON_2 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  private static final Female DAU22 = ObjectExtensions.<Female>operator_doubleArrow(PersonsFactory.eINSTANCE.createFemale(), ((Procedure1<Female>) (Female it) -> {
    it.setFullName(((FamiliesToPersonsTest.FIRST_DAU_2 + " ") + FamiliesToPersonsTest.LAST_NAME_2));
  }));

  /**
   * Before each test a new {@link FamilyRegister} has to be created as starting point.
   * This is checked by several assertions to ensure correct preconditions for the tests.
   */
  public void insertRegister() {
    final Resource x = this.resourceAt(FamiliesToPersonsTest.FAMILIES_MODEL);
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
      _contents.add(_createFamilyRegister);
    };
    this.<Resource>propagate(x, _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL), ModelMatchers.exists());
    Assertions.assertEquals(1, this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL).getContents().size());
    Assertions.assertEquals(1, IteratorExtensions.size(this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL).getAllContents()));
    MatcherAssert.<EObject>assertThat(this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL).getContents().get(0), CoreMatchers.<EObject>instanceOf(PersonRegister.class));
    Assertions.assertEquals(0, IteratorExtensions.size(this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL).getContents().get(0).eAllContents()));
  }

  /**
   * Creates a {@link Family} with the given familieName. Used by the "insertFamilyWith..."-tests
   */
  public Family createFamily(final String familieName) {
    Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
    final Procedure1<Family> _function = (Family it) -> {
      it.setLastName(familieName);
    };
    return ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function);
  }

  /**
   * Inserts a new {@link Family}. This should not have any effect on the Persons-Model.
   */
  @Test
  public void testInsertNewFamily() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Family family = this.createFamily(FamiliesToPersonsTest.LAST_NAME_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      _families.add(family);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Checks if the actual {@link FamilyRegister looks like the expected one.
   */
  public void assertCorrectFamilyRegister(final FamilyRegister expectedFamilyRegister) {
    final Resource familyModel = this.resourceAt(FamiliesToPersonsTest.FAMILIES_MODEL);
    MatcherAssert.<Resource>assertThat(familyModel, ModelMatchers.exists());
    Assertions.assertEquals(1, familyModel.getContents().size());
    final EObject familyRegister = familyModel.getContents().get(0);
    MatcherAssert.<EObject>assertThat(familyRegister, CoreMatchers.<EObject>instanceOf(FamilyRegister.class));
    final FamilyRegister castedFamilyRegister = ((FamilyRegister) familyRegister);
    MatcherAssert.<FamilyRegister>assertThat(castedFamilyRegister, ModelMatchers.<FamilyRegister>equalsDeeply(expectedFamilyRegister));
  }

  /**
   * Checks if the actual {@link PersonRegister looks like the expected one.
   */
  public void assertCorrectPersonRegister(final PersonRegister expectedPersonRegister) {
    final Resource personModel = this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL);
    MatcherAssert.<Resource>assertThat(personModel, ModelMatchers.exists());
    Assertions.assertEquals(1, personModel.getContents().size());
    final EObject personRegister = personModel.getContents().get(0);
    MatcherAssert.<EObject>assertThat(personRegister, CoreMatchers.<EObject>instanceOf(PersonRegister.class));
    final PersonRegister castedPersonRegister = ((PersonRegister) personRegister);
    MatcherAssert.<PersonRegister>assertThat(castedPersonRegister, ModelMatchers.<PersonRegister>equalsDeeply(expectedPersonRegister));
  }

  /**
   * Inserts a new {@link Family} and insert a father into it afterwards.
   */
  @Test
  public void testInsertFamilyWithFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Family family = this.createFamily(FamiliesToPersonsTest.LAST_NAME_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      _families.add(family);
      Member _createMember = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_1 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
      };
      Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_1);
      family.setFather(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_2 = (Male it_1) -> {
        it_1.setFullName(((FamiliesToPersonsTest.FIRST_DAD_1 + " ") + FamiliesToPersonsTest.LAST_NAME_1));
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
      _persons.add(_doubleArrow);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Inserts a new {@link Family} and insert a mother into it afterwards.
   */
  @Test
  public void testInsertFamilyWithMother() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Family family = this.createFamily(FamiliesToPersonsTest.LAST_NAME_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      _families.add(family);
      Member _createMember = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_1 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
      };
      Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_1);
      family.setMother(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      _persons.add(FamiliesToPersonsTest.MOM11);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Inserts a new {@link Family} and insert a son into it afterwards.
   */
  @Test
  public void testInsertFamilyWithSon() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Family family = this.createFamily(FamiliesToPersonsTest.LAST_NAME_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      _families.add(family);
      EList<Member> _sons = family.getSons();
      Member _createMember = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_1 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
      };
      Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_1);
      _sons.add(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      _persons.add(FamiliesToPersonsTest.SON11);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Inserts a new {@link Family} and insert a daughter into it afterwards.
   */
  @Test
  public void testInsertFamilyWithDaughter() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Family family = this.createFamily(FamiliesToPersonsTest.LAST_NAME_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      _families.add(family);
      EList<Member> _daughters = family.getDaughters();
      Member _createMember = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_1 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
      };
      Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_1);
      _daughters.add(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      _persons.add(FamiliesToPersonsTest.DAU11);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Used to build the starting point for many other tests like deleting and renaming operations.
   * Creates a {@link Family} including a father, a mother, a son and a daughter and maps this
   * changes to the {@link PersonRegister} which then includes two {@link Male} and two {@link Female}.
   */
  public void createOneFamilyBeforeTesting() {
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_1 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_2 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_3);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_4);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_5);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_1);
      _families.add(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  /**
   * Used to build an extended starting point for many other tests like replacing and moving members.
   * Creates a {@link Family} including a father, a mother, a son and a daughter and maps this
   * changes to the {@link PersonRegister} which then includes two {@link Male} and two {@link Female}.
   */
  public void createTwoFamiliesBeforeTesting() {
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_1 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_2 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_3);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_4);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_5);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_1);
      _families.add(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11, FamiliesToPersonsTest.DAD22, FamiliesToPersonsTest.MOM22, FamiliesToPersonsTest.SON22, FamiliesToPersonsTest.DAU22)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  /**
   * Deletes a father from a {@link Family} and the corresponding {@link Male} from the {@link PersonRegister}.
   */
  @Test
  public void testDeleteFatherFromFamily() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && it_1.getFather().getFirstName().equals(FamiliesToPersonsTest.FIRST_DAD_1)));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      selectedFamily.setFather(null);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Deletes a son from a {@link Family} and the corresponding {@link Male} from the {@link PersonRegister}.
   */
  @Test
  public void testDeleteSonFromFamily() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && IterableExtensions.<Member>exists(it_1.getSons(), ((Function1<Member, Boolean>) (Member it_2) -> {
          return Boolean.valueOf(it_2.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
        }))));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Member, Boolean> _function_2 = (Member it_1) -> {
        return Boolean.valueOf(it_1.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
      };
      final Member sonToDelete = IterableExtensions.<Member>findFirst(selectedFamily.getSons(), _function_2);
      selectedFamily.getSons().remove(sonToDelete);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Deletes a mother from a {@link Family} and the corresponding {@link Female} from the {@link PersonRegister}.
   */
  @Test
  public void testDeleteMotherFromFamily() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && it_1.getMother().getFirstName().equals(FamiliesToPersonsTest.FIRST_MOM_1)));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      selectedFamily.setMother(null);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Deletes a daughter from a {@link Family} and the corresponding {@link Female} from the {@link PersonRegister}.
   */
  @Test
  public void testDeleteDaughterFromFamily() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && IterableExtensions.<Member>exists(it_1.getDaughters(), ((Function1<Member, Boolean>) (Member it_2) -> {
          return Boolean.valueOf(it_2.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
        }))));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Member, Boolean> _function_2 = (Member it_1) -> {
        return Boolean.valueOf(it_1.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
      };
      final Member daughterToDelete = IterableExtensions.<Member>findFirst(selectedFamily.getDaughters(), _function_2);
      selectedFamily.getDaughters().remove(daughterToDelete);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Changes the lastname of a {@link Family} and should edit the fullnames of
   * all corresponding {@link Person}s from the {@link PersonRegister}.
   */
  @Test
  public void testChangeLastName() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      selectedFamily.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD12, FamiliesToPersonsTest.MOM12, FamiliesToPersonsTest.SON12, FamiliesToPersonsTest.DAU12)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Changes the firstname of a father of a {@link Family} and should edit the
   * fullname of the corresponding {@link Male} in the {@link PersonRegister}.
   */
  @Test
  public void testChangeFirstNameFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && it_1.getFather().getFirstName().equals(FamiliesToPersonsTest.FIRST_DAD_1)));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      Member _father = selectedFamily.getFather();
      _father.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD21, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Changes the firstname of a son of a {@link Family} and should edit the
   * fullname of the corresponding {@link Male} in the {@link PersonRegister}.
   */
  @Test
  public void testChangeFirstNameSon() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && IterableExtensions.<Member>exists(it_1.getSons(), ((Function1<Member, Boolean>) (Member son) -> {
          return Boolean.valueOf(son.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
        }))));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Member, Boolean> _function_2 = (Member it_1) -> {
        return Boolean.valueOf(it_1.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
      };
      final Member sonToChange = IterableExtensions.<Member>findFirst(selectedFamily.getSons(), _function_2);
      sonToChange.setFirstName(FamiliesToPersonsTest.FIRST_SON_2);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON21, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Changes the firstname of a mother of a {@link Family} and should edit the
   * fullname of the corresponding {@link Female} in the {@link PersonRegister}.
   */
  @Test
  public void testChangeFirstNameMother() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && it_1.getMother().getFirstName().equals(FamiliesToPersonsTest.FIRST_MOM_1)));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      Member _mother = selectedFamily.getMother();
      _mother.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM21, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Changes the firstname of a daughter of a {@link Family} and should edit the
   * fullname of the corresponding {@link Female} in the {@link PersonRegister}.
   */
  @Test
  public void testChangeFirstNameDaughter() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf((it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1) && IterableExtensions.<Member>exists(it_1.getDaughters(), ((Function1<Member, Boolean>) (Member it_2) -> {
          return Boolean.valueOf(it_2.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
        }))));
      };
      final Family selectedFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Member, Boolean> _function_2 = (Member it_1) -> {
        return Boolean.valueOf(it_1.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
      };
      final Member daughterToChange = IterableExtensions.<Member>findFirst(selectedFamily.getDaughters(), _function_2);
      daughterToChange.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU21)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the father with a new member which causes the original father to be moved
   * to a new family with the same lastname in which he is the only member.
   */
  @Test
  public void testReplaceFatherWithNewMember() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      Member _createMember = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
      };
      Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
      family.setFather(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD21, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the father with a father from a different family which causes the original father
   * to be moved to a new family with the same lastname in which he is the only member.
   * The replacing father will be removed from his original family in return.
   */
  @Test
  public void testReplaceFatherWithExistingFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      family1.setFather(family2.getFather());
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD21, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11, FamiliesToPersonsTest.MOM22, FamiliesToPersonsTest.SON22, FamiliesToPersonsTest.DAU22)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the father with a father from a different family which causes the original father
   * to be moved to a new family with the same lastname in which he is the only member.
   * The replacing father will be removed from his original family in return.
   * 
   * In this version, the replacing father was the last member of his family before
   * the replacing happens. Therefore, his old family will be deleted afterwards.
   */
  @Test
  public void testReplaceFatherWithExistingPreviouslyLonlyFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_1 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_2 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
        it_1.setFather(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_1);
      _families.add(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      family1.setFather(family2.getFather());
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_7);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD21, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the father with a son from a different family which causes the original father
   * to be moved to a new family with the same lastname in which he is the only member.
   * The replacing son will be removed from his original family in return.
   */
  @Test
  public void testReplaceFatherWithExistingSon() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Member, Boolean> _function_3 = (Member son) -> {
        return Boolean.valueOf(son.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_2));
      };
      family1.setFather(IterableExtensions.<Member>findFirst(family2.getSons(), _function_3));
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow_1);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.SON21, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11, FamiliesToPersonsTest.DAD22, FamiliesToPersonsTest.MOM22, FamiliesToPersonsTest.DAU22)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the mother with a new member which causes the original mother to be moved
   * to a new family with the same lastname in which she is the only member.
   */
  @Test
  public void testReplaceMotherWithNewMember() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      Member _createMember = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_2 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
      };
      Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
      family.setMother(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM21, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the mother with a mother from a different family which causes the original mother
   * to be moved to a new family with the same lastname in which she is the only member.
   * The replacing mother will be removed from its original family in return.
   */
  @Test
  public void testReplaceMotherWithExistingMother() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      family1.setMother(family2.getMother());
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM21, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11, FamiliesToPersonsTest.DAD22, FamiliesToPersonsTest.SON22, FamiliesToPersonsTest.DAU22)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the mother with a daughter from a different family which causes the original mother
   * to be moved to a new family with the same lastname in which she is the only member.
   * The replacing daughter will be removed from her original family in return.
   */
  @Test
  public void testReplaceMotherWithExistingDaughter() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_1 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_1);
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Member, Boolean> _function_3 = (Member daughter) -> {
        return Boolean.valueOf(daughter.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_2));
      };
      family1.setMother(IterableExtensions.<Member>findFirst(family2.getDaughters(), _function_3));
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_6);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_2);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow_1);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_2);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_2);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons.add(_doubleArrow_3);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_3);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.DAU21, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11, FamiliesToPersonsTest.DAD22, FamiliesToPersonsTest.MOM22, FamiliesToPersonsTest.SON22)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Replace the mother with a daughter from a different family which causes the original mother
   * to be moved to a new family with the same lastname in which she is the only member.
   * The replacing daughter will be removed from her original family in return.
   * 
   * In this version, the replacing daughter was the last member of her family before
   * the replacing happens. Therefore, her old family will be deleted afterwards.
   */
  @Test
  public void testReplaceMotherWithExistingPreviouslyLonlyDaughter() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_1 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_2 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
        _daughters.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_1);
      _families.add(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      final Function1<Member, Boolean> _function_4 = (Member daughter) -> {
        return Boolean.valueOf(daughter.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_2));
      };
      family1.setMother(IterableExtensions.<Member>findFirst(family2.getDaughters(), _function_4));
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_7);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.DAU21, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A father switches his family and stays a father.
   */
  @Test
  public void testSwitchFamilySamePositionFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      newFamily.setFather(oldFamily.getFather());
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD12, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A mother switches her family and stays a mother.
   */
  @Test
  public void testSwitchFamilySamePositionMother() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      newFamily.setMother(oldFamily.getMother());
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM12, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A son switches his family and stays a son.
   */
  @Test
  public void testSwitchFamilySamePositionSon() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      EList<Member> _sons = newFamily.getSons();
      final Function1<Member, Boolean> _function_4 = (Member son) -> {
        return Boolean.valueOf(son.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
      };
      Member _findFirst = IterableExtensions.<Member>findFirst(oldFamily.getSons(), _function_4);
      _sons.add(_findFirst);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _sons.add(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON12, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A daughter switches her family and stays a daughter.
   */
  @Test
  public void testSwitchFamilySamePositionDaughter() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      EList<Member> _daughters = newFamily.getDaughters();
      final Function1<Member, Boolean> _function_4 = (Member daughter) -> {
        return Boolean.valueOf(daughter.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
      };
      Member _findFirst = IterableExtensions.<Member>findFirst(oldFamily.getDaughters(), _function_4);
      _daughters.add(_findFirst);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _daughters.add(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU12)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test to move around members of the families repeatedly to check if correspondences are maintained correctly.
   */
  @Test
  public void testRepeatedlyMovingFathersBetweenFamilies() {
    this.insertRegister();
    final String first_mom_3 = "Beate";
    Male _createMale = PersonsFactory.eINSTANCE.createMale();
    final Procedure1<Male> _function = (Male it) -> {
      it.setFullName(((FamiliesToPersonsTest.FIRST_DAD_1 + " ") + FamiliesToPersonsTest.LAST_NAME_3));
    };
    final Male dad13 = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function);
    Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
    final Procedure1<Female> _function_1 = (Female it) -> {
      it.setFullName(((first_mom_3 + " ") + FamiliesToPersonsTest.LAST_NAME_3));
    };
    final Female mom33 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Consumer<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      final Family family1 = this.createFamily(FamiliesToPersonsTest.LAST_NAME_1);
      final Family family2 = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      final Family family3 = this.createFamily(FamiliesToPersonsTest.LAST_NAME_3);
      EList<Family> _families = it.getFamilies();
      Iterables.<Family>addAll(_families, Collections.<Family>unmodifiableList(CollectionLiterals.<Family>newArrayList(family1, family2, family3)));
      Member _createMember = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_3 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
      };
      Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
      family1.setFather(_doubleArrow);
      Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_4 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
      };
      Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_4);
      family2.setFather(_doubleArrow_1);
      Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_5 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
      };
      Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_5);
      family1.setMother(_doubleArrow_2);
      Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_6 = (Member it_1) -> {
        it_1.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
      };
      Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_6);
      family2.setMother(_doubleArrow_3);
      Member _createMember_4 = FamiliesFactory.eINSTANCE.createMember();
      final Procedure1<Member> _function_7 = (Member it_1) -> {
        it_1.setFirstName(first_mom_3);
      };
      Member _doubleArrow_4 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_4, _function_7);
      family3.setMother(_doubleArrow_4);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.DAD22, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.MOM22, mom33)));
    };
    final PersonRegister expectedPersonRegisterAfterPreparation = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectPersonRegister(expectedPersonRegisterAfterPreparation);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_4 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_5 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_5);
      final Function1<Family, Boolean> _function_6 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_6);
      final Function1<Family, Boolean> _function_7 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_3));
      };
      final Family family3 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_7);
      family3.setFather(family2.getFather());
      family2.setFather(family1.getFather());
      family1.setFather(family3.getFather());
      family3.setFather(family2.getFather());
      family2.setFather(family1.getFather());
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_4);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_5 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_6 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_7 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_7);
        it_1.setMother(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_6);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_7 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_8 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_8);
        it_1.setMother(_doubleArrow_1);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_9 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_9);
        it_1.setFather(_doubleArrow_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_7);
      _families_1.add(_doubleArrow_1);
      EList<Family> _families_2 = it.getFamilies();
      Family _createFamily_2 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_8 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_3);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_9 = (Member it_2) -> {
          it_2.setFirstName(first_mom_3);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_9);
        it_1.setMother(_doubleArrow_2);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_10 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_10);
        it_1.setFather(_doubleArrow_3);
      };
      Family _doubleArrow_2 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_2, _function_8);
      _families_2.add(_doubleArrow_2);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_5);
    PersonRegister _createPersonRegister_1 = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_6 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(dad13, FamiliesToPersonsTest.DAD22, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.MOM22, mom33)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister_1, _function_6);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A son switches his family and becomes a father.
   */
  @Test
  public void testSwitchFamilyDifferentPositionSonToFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      final Function1<Member, Boolean> _function_4 = (Member son) -> {
        return Boolean.valueOf(son.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
      };
      newFamily.setFather(IterableExtensions.<Member>findFirst(oldFamily.getSons(), _function_4));
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON12, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A son switches his family and becomes a father.
   * Version in which the son was the last member in his family before. Therefore, the old family
   * of the son will be deleted as he becomes the father in the new family.
   */
  @Test
  public void testSwitchFamilyDifferentPositionLonlySonToFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_1 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_2 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
        _sons.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_1);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_2);
      _families_1.add(_doubleArrow_1);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.MOM22)));
    };
    final PersonRegister expectedPersonRegisterAfterPreparation = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegisterAfterPreparation);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      final Function1<Family, Boolean> _function_4 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_4);
      final Function1<Member, Boolean> _function_5 = (Member son) -> {
        return Boolean.valueOf(son.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
      };
      newFamily.setFather(IterableExtensions.<Member>findFirst(oldFamily.getSons(), _function_5));
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_2);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_3 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_6);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_4);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_3);
    PersonRegister _createPersonRegister_1 = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_4 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.SON12, FamiliesToPersonsTest.MOM22)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister_1, _function_4);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A daughter switches her family and becomes a mother.
   */
  @Test
  public void testSwitchFamilyDifferentPositionDaughterToMother() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      final Function1<Member, Boolean> _function_4 = (Member daughter) -> {
        return Boolean.valueOf(daughter.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
      };
      newFamily.setMother(IterableExtensions.<Member>findFirst(oldFamily.getDaughters(), _function_4));
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _sons.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU12)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A daughter switches her family and becomes a mother.
   * Version in which the daughter was the last member in her family before. Therefore, the old family
   * of the daughter will be deleted as she becomes the mother in the new family.
   */
  @Test
  public void testSwitchFamilyDifferentPositionLonlyDaughterToMother() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_1 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_2 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
        _daughters.add(_doubleArrow);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_1);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_2 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
        it_1.setFather(_doubleArrow_1);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_2);
      _families_1.add(_doubleArrow_1);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAU11, FamiliesToPersonsTest.DAD22)));
    };
    final PersonRegister expectedPersonRegisterAfterPreparation = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegisterAfterPreparation);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      final Function1<Family, Boolean> _function_4 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_4);
      final Function1<Member, Boolean> _function_5 = (Member daughter) -> {
        return Boolean.valueOf(daughter.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
      };
      newFamily.setMother(IterableExtensions.<Member>findFirst(oldFamily.getDaughters(), _function_5));
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_2);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_3 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_2);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_6);
        it_1.setMother(_doubleArrow_1);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_4);
      _families.add(_doubleArrow);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_3);
    PersonRegister _createPersonRegister_1 = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_4 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD22, FamiliesToPersonsTest.DAU12)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister_1, _function_4);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A father switches his family and becomes a son.
   */
  @Test
  public void testSwitchFamilyDifferentPositionFatherToSon() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      EList<Member> _sons = newFamily.getSons();
      Member _father = oldFamily.getFather();
      _sons.add(_father);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setMother(_doubleArrow);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        EList<Member> _sons = it_1.getSons();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _sons.add(_doubleArrow_1);
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD12, FamiliesToPersonsTest.MOM11, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * A mother switches her family and becomes a daughter.
   */
  @Test
  public void testSwitchFamilyDifferentPositionMotherToDaughter() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = this.createFamily(FamiliesToPersonsTest.LAST_NAME_2);
      _families.add(_createFamily);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
      final Function1<Family, Boolean> _function_2 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
      };
      final Family oldFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
      final Function1<Family, Boolean> _function_3 = (Family it_1) -> {
        return Boolean.valueOf(it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
      };
      final Family newFamily = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
      EList<Member> _daughters = newFamily.getDaughters();
      Member _mother = oldFamily.getMother();
      _daughters.add(_mother);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final Procedure1<FamilyRegister> _function_2 = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_3 = (Family it_1) -> {
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_1);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_4);
        it_1.setFather(_doubleArrow);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_5);
        _sons.add(_doubleArrow_1);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_6 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_6);
        _daughters.add(_doubleArrow_2);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_3);
      _families.add(_doubleArrow);
      EList<Family> _families_1 = it.getFamilies();
      Family _createFamily_1 = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_4 = (Family it_1) -> {
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_5);
        _daughters.add(_doubleArrow_1);
        it_1.setLastName(FamiliesToPersonsTest.LAST_NAME_2);
      };
      Family _doubleArrow_1 = ObjectExtensions.<Family>operator_doubleArrow(_createFamily_1, _function_4);
      _families_1.add(_doubleArrow_1);
    };
    final FamilyRegister expectedFamilyRegister = ObjectExtensions.<FamilyRegister>operator_doubleArrow(_createFamilyRegister, _function_2);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_3 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Iterables.<Person>addAll(_persons, Collections.<Person>unmodifiableList(CollectionLiterals.<Person>newArrayList(FamiliesToPersonsTest.DAD11, FamiliesToPersonsTest.MOM12, FamiliesToPersonsTest.SON11, FamiliesToPersonsTest.DAU11)));
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_3);
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test if exception is thrown when a former mother is assigned to be a father.
   */
  @Test
  public void testExceptionSexChanges_AssignMotherToFather() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
        final Function1<Family, Boolean> _function_2 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
        final Function1<Family, Boolean> _function_3 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
        family1.setFather(family2.getMother());
      };
      this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignMotherToFather = Assertions.<UnsupportedOperationException>assertThrows(UnsupportedOperationException.class, _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final String expectedMessage = "The position of a male family member can only be assigned to members with no or a male corresponding person.";
    Assertions.assertEquals(thrownExceptionAssignMotherToFather.getMessage(), expectedMessage);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test if exception is thrown when a former daughter is assigned to be a son.
   */
  @Test
  public void testExceptionSexChanges_AssignDaughterToSon() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
        final Function1<Family, Boolean> _function_2 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
        final Function1<Family, Boolean> _function_3 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
        EList<Member> _sons = family1.getSons();
        final Function1<Member, Boolean> _function_4 = (Member daughter) -> {
          return Boolean.valueOf(daughter.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_2));
        };
        Member _findFirst = IterableExtensions.<Member>findFirst(family2.getDaughters(), _function_4);
        _sons.add(_findFirst);
      };
      this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignDaughterToSon = Assertions.<UnsupportedOperationException>assertThrows(UnsupportedOperationException.class, _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final String expectedMessage = "The position of a male family member can only be assigned to members with no or a male corresponding person.";
    Assertions.assertEquals(thrownExceptionAssignDaughterToSon.getMessage(), expectedMessage);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test if exception is thrown when a former father is assigned to be a mother.
   */
  @Test
  public void testExceptionSexChanges_AssignFatherToMother() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
        final Function1<Family, Boolean> _function_2 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
        final Function1<Family, Boolean> _function_3 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
        family1.setMother(family2.getFather());
      };
      this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignFatherToMother = Assertions.<UnsupportedOperationException>assertThrows(UnsupportedOperationException.class, _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final String expectedMessage = "The position of a female family member can only be assigned to members with no or a female corresponding person.";
    Assertions.assertEquals(thrownExceptionAssignFatherToMother.getMessage(), expectedMessage);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Test if exception is thrown when a former son is assigned to be a daughter.
   */
  @Test
  public void testExceptionSexChanges_AssignSonToDaughter() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createTwoFamiliesBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
        final Function1<Family, Boolean> _function_2 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
        final Function1<Family, Boolean> _function_3 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_2));
        };
        final Family family2 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_3);
        EList<Member> _daughters = family1.getDaughters();
        final Function1<Member, Boolean> _function_4 = (Member son) -> {
          return Boolean.valueOf(son.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_2));
        };
        Member _findFirst = IterableExtensions.<Member>findFirst(family2.getSons(), _function_4);
        _daughters.add(_findFirst);
      };
      this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    };
    final UnsupportedOperationException thrownExceptionAssignSonToDaughter = Assertions.<UnsupportedOperationException>assertThrows(UnsupportedOperationException.class, _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final String expectedMessage = "The position of a female family member can only be assigned to members with no or a female corresponding person.";
    Assertions.assertEquals(thrownExceptionAssignSonToDaughter.getMessage(), expectedMessage);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Unescapes escaped escape sequences for linefeed, carriage return and tabulator escape sequences.
   * Unfortunately, <code>org.junit.jupiter.params.provider.Arguments.of(...)</code> is not able
   * to deal with escape sequences like </code>\n</code>. Therefore, these sequences have to be escaped for
   * the ParameterizedTest and then unescaped for the intended use.
   */
  public String unescapeString(final String string) {
    return string.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
  }

  @ParameterizedTest(name = "{index} => role={0}, escapedNewName={1}, expectedExceptionMessage={2}")
  @MethodSource("nameAndExceptionProvider")
  public void testExceptionRenamingMemberWithInvalidFirstName(final MemberRole role, final String escapedNewName, final String expectedExceptionMessage) {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    String _xifexpression = null;
    if ((escapedNewName != null)) {
      _xifexpression = this.unescapeString(escapedNewName);
    } else {
      _xifexpression = null;
    }
    final String unescapedNewName = _xifexpression;
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
        final Function1<Family, Boolean> _function_2 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
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
              final Function1<Member, Boolean> _function_3 = (Member son) -> {
                return Boolean.valueOf(son.getFirstName().equals(FamiliesToPersonsTest.FIRST_SON_1));
              };
              Member _findFirst = IterableExtensions.<Member>findFirst(family1.getSons(), _function_3);
              _findFirst.setFirstName(unescapedNewName);
              break;
            case Daughter:
              final Function1<Member, Boolean> _function_4 = (Member daughter) -> {
                return Boolean.valueOf(daughter.getFirstName().equals(FamiliesToPersonsTest.FIRST_DAU_1));
              };
              Member _findFirst_1 = IterableExtensions.<Member>findFirst(family1.getDaughters(), _function_4);
              _findFirst_1.setFirstName(unescapedNewName);
              break;
            default:
              break;
          }
        }
      };
      this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    };
    final IllegalStateException thrownExceptionSetNullAsFirstName = Assertions.<IllegalStateException>assertThrows(IllegalStateException.class, _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final String expectedMessage = expectedExceptionMessage;
    Assertions.assertEquals(thrownExceptionSetNullAsFirstName.getMessage(), expectedMessage);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  @ParameterizedTest(name = "{index} => role={0}, escapedNewName={1}, expectedExceptionMessage={2}")
  @MethodSource("nameAndExceptionProvider")
  public void testExceptionCreationOfMemberWithInvalidFirstName(final MemberRole role, final String escapedNewName, final String expectedExceptionMessage) {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    String _xifexpression = null;
    if ((escapedNewName != null)) {
      _xifexpression = this.unescapeString(escapedNewName);
    } else {
      _xifexpression = null;
    }
    final String unescapedNewName = _xifexpression;
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Executable _function = () -> {
      final Consumer<FamilyRegister> _function_1 = (FamilyRegister it) -> {
        final Function1<Family, Boolean> _function_2 = (Family family) -> {
          return Boolean.valueOf(family.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1));
        };
        final Family family1 = IterableExtensions.<Family>findFirst(it.getFamilies(), _function_2);
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_1) -> {
          it_1.setFirstName(unescapedNewName);
        };
        final Member newMember = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_3);
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
      this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function_1);
    };
    final IllegalStateException thrownExceptionSetNullAsFirstName = Assertions.<IllegalStateException>assertThrows(IllegalStateException.class, _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final String expectedMessage = expectedExceptionMessage;
    Assertions.assertEquals(thrownExceptionSetNullAsFirstName.getMessage(), expectedMessage);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  public static Stream<Arguments> nameAndExceptionProvider() {
    return Stream.<Arguments>of(
      Arguments.of(MemberRole.Father, null, FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(MemberRole.Father, "", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Father, "\\n\\t\\r", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Father, (FamiliesToPersonsTest.FIRST_DAD_1 + "\\n"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Father, (FamiliesToPersonsTest.FIRST_DAD_1 + "\\t"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Father, (FamiliesToPersonsTest.FIRST_DAD_1 + "\\r"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Mother, null, FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(MemberRole.Mother, "", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Mother, "\\t\\n\\r", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Mother, (FamiliesToPersonsTest.FIRST_MOM_1 + "\\n"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Mother, (FamiliesToPersonsTest.FIRST_MOM_1 + "\\t"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Mother, (FamiliesToPersonsTest.FIRST_MOM_1 + "\\r"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Son, null, FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(MemberRole.Son, "", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Son, "\\n\\t\\r", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Son, (FamiliesToPersonsTest.FIRST_SON_1 + "\\n"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Son, (FamiliesToPersonsTest.FIRST_SON_1 + "\\t"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Son, (FamiliesToPersonsTest.FIRST_SON_1 + "\\r"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Daughter, null, FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL), 
      Arguments.of(MemberRole.Daughter, "", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Daughter, "\\t\\n\\r", FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE), 
      Arguments.of(MemberRole.Daughter, (FamiliesToPersonsTest.FIRST_DAU_1 + "\\n"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Daughter, (FamiliesToPersonsTest.FIRST_DAU_1 + "\\t"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES), 
      Arguments.of(MemberRole.Daughter, (FamiliesToPersonsTest.FIRST_DAU_1 + "\\r"), FamiliesToPersonsHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES));
  }

  /**
   * Test the creation of a family without a lastname and the correct creation of corresponding
   * persons without a white space as seperaotr attached to the firstname of the member.
   */
  public void testCreatingFamilyWithEmptyLastName() {
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      EList<Family> _families = it.getFamilies();
      Family _createFamily = FamiliesFactory.eINSTANCE.createFamily();
      final Procedure1<Family> _function_1 = (Family it_1) -> {
        it_1.setLastName("");
        Member _createMember = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_2 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAD_1);
        };
        Member _doubleArrow = ObjectExtensions.<Member>operator_doubleArrow(_createMember, _function_2);
        it_1.setFather(_doubleArrow);
        Member _createMember_1 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_3 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_MOM_1);
        };
        Member _doubleArrow_1 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_1, _function_3);
        it_1.setMother(_doubleArrow_1);
        EList<Member> _sons = it_1.getSons();
        Member _createMember_2 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_4 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_SON_1);
        };
        Member _doubleArrow_2 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_2, _function_4);
        _sons.add(_doubleArrow_2);
        EList<Member> _daughters = it_1.getDaughters();
        Member _createMember_3 = FamiliesFactory.eINSTANCE.createMember();
        final Procedure1<Member> _function_5 = (Member it_2) -> {
          it_2.setFirstName(FamiliesToPersonsTest.FIRST_DAU_1);
        };
        Member _doubleArrow_3 = ObjectExtensions.<Member>operator_doubleArrow(_createMember_3, _function_5);
        _daughters.add(_doubleArrow_3);
      };
      Family _doubleArrow = ObjectExtensions.<Family>operator_doubleArrow(_createFamily, _function_1);
      _families.add(_doubleArrow);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_2 = (Male it_1) -> {
        it_1.setFullName(FamiliesToPersonsTest.FIRST_DAD_1);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_2);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_3 = (Female it_1) -> {
        it_1.setFullName(FamiliesToPersonsTest.FIRST_MOM_1);
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_4 = (Male it_1) -> {
        it_1.setFullName(FamiliesToPersonsTest.FIRST_SON_1);
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_5 = (Female it_1) -> {
        it_1.setFullName(FamiliesToPersonsTest.FIRST_DAU_1);
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Deletes all {@link Family}s with matching lastname from the {@link FamilyRegister}.
   * All {@link Member}s which were contained in these families will be deleted together
   * with there corresponding {@link Person}s in the {@link PersonRegister} as well.
   * If only families without members are deleted, the {@link PersonRegister}
   * will not be affected.
   */
  @Test
  public void testDeleteAllFamiliesWithMatchingName() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<FamilyRegister> _function = (FamilyRegister it) -> {
      final Predicate<Family> _function_1 = (Family it_1) -> {
        return it_1.getLastName().equals(FamiliesToPersonsTest.LAST_NAME_1);
      };
      it.getFamilies().removeIf(_function_1);
    };
    this.<FamilyRegister>propagate(this.<FamilyRegister>from(FamilyRegister.class, FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    final FamilyRegister expectedFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
    final PersonRegister expectedPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    this.assertCorrectFamilyRegister(expectedFamilyRegister);
    this.assertCorrectPersonRegister(expectedPersonRegister);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
  }

  /**
   * Deletes the {@link FamilyRegister} with all its contents which leads to
   * the deletion of the corresponding {@link PersonRegister} with all its contents.
   */
  @Test
  public void testDeleteFamilyRegister() {
    this.insertRegister();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - begin"));
    this.createOneFamilyBeforeTesting();
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - preparation done"));
    final Consumer<Resource> _function = (Resource it) -> {
      it.getContents().clear();
    };
    this.<Resource>propagate(this.resourceAt(FamiliesToPersonsTest.FAMILIES_MODEL), _function);
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - propagation done"));
    Assertions.assertEquals(0, this.resourceAt(FamiliesToPersonsTest.FAMILIES_MODEL).getContents().size());
    Assertions.assertEquals(0, this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL).getContents().size());
    MatcherAssert.<Resource>assertThat(this.resourceAt(FamiliesToPersonsTest.FAMILIES_MODEL), CoreMatchers.not(ModelMatchers.exists()));
    MatcherAssert.<Resource>assertThat(this.resourceAt(FamiliesToPersonsTest.PERSONS_MODEL), CoreMatchers.not(ModelMatchers.exists()));
    FamiliesToPersonsTest.logger.trace((this.nameOfTestMethod + " - finished without errors"));
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
