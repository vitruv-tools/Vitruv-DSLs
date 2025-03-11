package tools.vitruv.dsls.demo.insurancepersons.tests;

import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory;
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
import org.junit.jupiter.api.extension.ExtendWith;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.demo.insurancepersons.insurance2persons.InsuranceToPersonsChangePropagationSpecification;
import tools.vitruv.testutils.TestLogging;
import tools.vitruv.testutils.TestProject;
import tools.vitruv.testutils.TestProjectManager;
import tools.vitruv.testutils.TestUserInteraction;
import tools.vitruv.testutils.matchers.ModelMatchers;
import tools.vitruv.testutils.views.ChangePublishingTestView;
import tools.vitruv.testutils.views.TestView;

@ExtendWith({ TestLogging.class, TestProjectManager.class })
@SuppressWarnings("all")
public class InsuranceToPersonsTest implements TestView {
  @Delegate
  private TestView testView;

  /**
   * Can be used to set a different kind of test view to be used in subclasses.
   */
  protected TestView setTestView(final TestView testView) {
    return this.testView = testView;
  }

  protected Iterable<ChangePropagationSpecification> getChangePropagationSpecifications() {
    InsuranceToPersonsChangePropagationSpecification _insuranceToPersonsChangePropagationSpecification = new InsuranceToPersonsChangePropagationSpecification();
    return Collections.<ChangePropagationSpecification>unmodifiableList(CollectionLiterals.<ChangePropagationSpecification>newArrayList(_insuranceToPersonsChangePropagationSpecification));
  }

  @BeforeEach
  public void prepare(@TestProject final Path testProjectPath) {
    try {
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

  private static final String MALE_NAME = "Max Mustermann";

  private static final String MALE_NAME_2 = "Bernd Mustermann";

  private static final String FEMALE_NAME = "Erika Mustermann";

  private static final String FEMALE_NAME_2 = "Berta Mustermann";

  private static final String FEMALE_NAME_3 = "Berta Musterfrau";

  private static final String SPECIAL_CHAR_NAME = "Berta? Müster-frau";

  private static final Path PERSONS_MODEL = Path.of("model/persons.persons");

  private static final Path INSURANCE_MODEL = Path.of("model/insurance.insurance");

  /**
   * Before each test a new {@link InsuranceDatabase} has to be created as starting point.
   * This is checked by several assertions to ensure correct preconditions for the tests.
   */
  public void insertRegister() {
    final Consumer<Resource> _function = (Resource it) -> {
      EList<EObject> _contents = it.getContents();
      InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
      _contents.add(_createInsuranceDatabase);
    };
    this.<Resource>propagate(this.resourceAt(InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    MatcherAssert.<Resource>assertThat(this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL), ModelMatchers.exists());
    Assertions.assertEquals(1, this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL).getContents().size());
    Assertions.assertEquals(1, IteratorExtensions.size(this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL).getAllContents()));
    MatcherAssert.<EObject>assertThat(IterableExtensions.<EObject>head(this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL).getContents()), CoreMatchers.<EObject>instanceOf(PersonRegister.class));
    Assertions.assertEquals(0, IteratorExtensions.size(IterableExtensions.<EObject>head(this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL).getContents()).eAllContents()));
  }

  /**
   * Check if the actual {@link PersonRegister} looks like the expected one.
   */
  public void assertCorrectPersonRegister(final PersonRegister expectedPersonRegister) {
    final Resource personModel = this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL);
    MatcherAssert.<Resource>assertThat(personModel, ModelMatchers.exists());
    Assertions.assertEquals(1, personModel.getContents().size());
    final EObject personRegister = IterableExtensions.<EObject>head(personModel.getContents());
    MatcherAssert.<EObject>assertThat(personRegister, CoreMatchers.<EObject>instanceOf(PersonRegister.class));
    final PersonRegister castedPersonRegister = ((PersonRegister) personRegister);
    MatcherAssert.<PersonRegister>assertThat(castedPersonRegister, ModelMatchers.<PersonRegister>equalsDeeply(expectedPersonRegister));
  }

  /**
   * Check if the actual {@link InsuranceDatabase} looks like the expected one.
   */
  public void assertCorrectInsuranceDatabase(final InsuranceDatabase expectedInsuranceDatabase) {
    final Resource insuranceModel = this.resourceAt(InsuranceToPersonsTest.INSURANCE_MODEL);
    MatcherAssert.<Resource>assertThat(insuranceModel, ModelMatchers.exists());
    Assertions.assertEquals(1, insuranceModel.getContents().size());
    final EObject insuranceDatabase = IterableExtensions.<EObject>head(insuranceModel.getContents());
    MatcherAssert.<EObject>assertThat(insuranceDatabase, CoreMatchers.<EObject>instanceOf(InsuranceDatabase.class));
    final InsuranceDatabase castedInsuranceDatabase = ((InsuranceDatabase) insuranceDatabase);
    MatcherAssert.<InsuranceDatabase>assertThat(castedInsuranceDatabase, ModelMatchers.<InsuranceDatabase>equalsDeeply(expectedInsuranceDatabase));
  }

  @Test
  public void testCreateInsuranceDatabase() {
    this.insertRegister();
    MatcherAssert.<Resource>assertThat(this.resourceAt(InsuranceToPersonsTest.INSURANCE_MODEL), ModelMatchers.exists());
    MatcherAssert.<Resource>assertThat(this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL), ModelMatchers.exists());
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_1 = (PersonRegister it) -> {
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_1);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testDeleteInsuranceDatabase() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<Resource> _function_1 = (Resource it) -> {
      it.getContents().clear();
    };
    this.<Resource>propagate(this.resourceAt(InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    Assertions.assertEquals(0, this.resourceAt(InsuranceToPersonsTest.INSURANCE_MODEL).getContents().size());
    Assertions.assertEquals(0, this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL).getContents().size());
    MatcherAssert.<Resource>assertThat(this.resourceAt(InsuranceToPersonsTest.INSURANCE_MODEL), CoreMatchers.not(ModelMatchers.exists()));
    MatcherAssert.<Resource>assertThat(this.resourceAt(InsuranceToPersonsTest.PERSONS_MODEL), CoreMatchers.not(ModelMatchers.exists()));
  }

  @Test
  public void testCreatedClient() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_2);
      _insuranceclient.add(_doubleArrow);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testCreatedClient_multiple() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_2);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_3);
      _insuranceclient_1.add(_doubleArrow_1);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME);
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testChangedName() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_2);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_3);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getName().equals(InsuranceToPersonsTest.FEMALE_NAME_2));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      searchedClient.setName(InsuranceToPersonsTest.FEMALE_NAME_3);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME);
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_5 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME_3);
      };
      Female _doubleArrow_2 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_5);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_6 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_3);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_6);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testChangedName_empty() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_3);
      _insuranceclient_2.add(_doubleArrow_2);
      EList<InsuranceClient> _insuranceclient_3 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_3 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_2);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_3 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_3, _function_4);
      _insuranceclient_3.add(_doubleArrow_3);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getName().equals(InsuranceToPersonsTest.FEMALE_NAME));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      searchedClient.setName("");
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName("");
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME_2);
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME_2);
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName("");
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_5);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_6 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_6);
      _insuranceclient_2.add(_doubleArrow_2);
      EList<InsuranceClient> _insuranceclient_3 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_3 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_7 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_2);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_3 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_3, _function_7);
      _insuranceclient_3.add(_doubleArrow_3);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testChangedName_specialChars() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_3);
      _insuranceclient_2.add(_doubleArrow_2);
      EList<InsuranceClient> _insuranceclient_3 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_3 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_2);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_3 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_3, _function_4);
      _insuranceclient_3.add(_doubleArrow_3);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getName().equals(InsuranceToPersonsTest.FEMALE_NAME_2));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      searchedClient.setName(InsuranceToPersonsTest.SPECIAL_CHAR_NAME);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME);
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME_2);
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_5);
      _persons_2.add(_doubleArrow_2);
      EList<Person> _persons_3 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_6 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.SPECIAL_CHAR_NAME);
      };
      Female _doubleArrow_3 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_6);
      _persons_3.add(_doubleArrow_3);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_5);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_6 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_6);
      _insuranceclient_2.add(_doubleArrow_2);
      EList<InsuranceClient> _insuranceclient_3 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_3 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_7 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.SPECIAL_CHAR_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_3 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_3, _function_7);
      _insuranceclient_3.add(_doubleArrow_3);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testChangedGender_toFemale() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_3);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getGender().equals(Gender.MALE));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      searchedClient.setGender(Gender.FEMALE);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_3 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale_1 = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME);
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale_1, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME_2);
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_5);
      _persons_2.add(_doubleArrow_2);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_5);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_6 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_6);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testChangedGender_toMale() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_3);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getGender().equals(Gender.FEMALE));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      searchedClient.setGender(Gender.MALE);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Male _createMale_1 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_4 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME);
      };
      Male _doubleArrow_1 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_1, _function_4);
      _persons_1.add(_doubleArrow_1);
      EList<Person> _persons_2 = it.getPersons();
      Male _createMale_2 = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_5 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME_2);
      };
      Male _doubleArrow_2 = ObjectExtensions.<Male>operator_doubleArrow(_createMale_2, _function_5);
      _persons_2.add(_doubleArrow_2);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_5);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_6 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME_2);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_6);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testDeletedClient_first_notOnly() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getName().equals(InsuranceToPersonsTest.MALE_NAME));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      it.getInsuranceclient().remove(searchedClient);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_3 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME);
      };
      Female _doubleArrow = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_3);
      _persons.add(_doubleArrow);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testDeletedClient_middle_notOnly() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_2);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_3);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getName().equals(InsuranceToPersonsTest.FEMALE_NAME));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      it.getInsuranceclient().remove(searchedClient);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME_2);
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_2);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_5);
      _insuranceclient_1.add(_doubleArrow_1);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testDeletedClient_last_notOnly() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_2 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_2);
      _insuranceclient_1.add(_doubleArrow_1);
      EList<InsuranceClient> _insuranceclient_2 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_2 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_3 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME_2);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_2 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_2, _function_3);
      _insuranceclient_2.add(_doubleArrow_2);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getName().equals(InsuranceToPersonsTest.FEMALE_NAME_2));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      it.getInsuranceclient().remove(searchedClient);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
      EList<Person> _persons = it.getPersons();
      Male _createMale = PersonsFactory.eINSTANCE.createMale();
      final Procedure1<Male> _function_3 = (Male it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.MALE_NAME);
      };
      Male _doubleArrow = ObjectExtensions.<Male>operator_doubleArrow(_createMale, _function_3);
      _persons.add(_doubleArrow);
      EList<Person> _persons_1 = it.getPersons();
      Female _createFemale = PersonsFactory.eINSTANCE.createFemale();
      final Procedure1<Female> _function_4 = (Female it_1) -> {
        it_1.setFullName(InsuranceToPersonsTest.FEMALE_NAME);
      };
      Female _doubleArrow_1 = ObjectExtensions.<Female>operator_doubleArrow(_createFemale, _function_4);
      _persons_1.add(_doubleArrow_1);
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_4 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_4);
      _insuranceclient.add(_doubleArrow);
      EList<InsuranceClient> _insuranceclient_1 = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient_1 = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_5 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.FEMALE_NAME);
        it_1.setGender(Gender.FEMALE);
      };
      InsuranceClient _doubleArrow_1 = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient_1, _function_5);
      _insuranceclient_1.add(_doubleArrow_1);
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
  }

  @Test
  public void testDeletedClient_only() {
    this.insertRegister();
    final Consumer<InsuranceDatabase> _function = (InsuranceDatabase it) -> {
      EList<InsuranceClient> _insuranceclient = it.getInsuranceclient();
      InsuranceClient _createInsuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient();
      final Procedure1<InsuranceClient> _function_1 = (InsuranceClient it_1) -> {
        it_1.setName(InsuranceToPersonsTest.MALE_NAME);
        it_1.setGender(Gender.MALE);
      };
      InsuranceClient _doubleArrow = ObjectExtensions.<InsuranceClient>operator_doubleArrow(_createInsuranceClient, _function_1);
      _insuranceclient.add(_doubleArrow);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function);
    final Consumer<InsuranceDatabase> _function_1 = (InsuranceDatabase it) -> {
      final Function1<InsuranceClient, Boolean> _function_2 = (InsuranceClient x) -> {
        return Boolean.valueOf(x.getName().equals(InsuranceToPersonsTest.MALE_NAME));
      };
      final InsuranceClient searchedClient = IterableExtensions.<InsuranceClient>findFirst(it.getInsuranceclient(), _function_2);
      it.getInsuranceclient().remove(searchedClient);
    };
    this.<InsuranceDatabase>propagate(this.<InsuranceDatabase>from(InsuranceDatabase.class, InsuranceToPersonsTest.INSURANCE_MODEL), _function_1);
    PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
    final Procedure1<PersonRegister> _function_2 = (PersonRegister it) -> {
    };
    final PersonRegister expectedPersonRegister = ObjectExtensions.<PersonRegister>operator_doubleArrow(_createPersonRegister, _function_2);
    InsuranceDatabase _createInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase();
    final Procedure1<InsuranceDatabase> _function_3 = (InsuranceDatabase it) -> {
    };
    final InsuranceDatabase expectedInsuranceDatabase = ObjectExtensions.<InsuranceDatabase>operator_doubleArrow(_createInsuranceDatabase, _function_3);
    this.assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
    this.assertCorrectPersonRegister(expectedPersonRegister);
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
