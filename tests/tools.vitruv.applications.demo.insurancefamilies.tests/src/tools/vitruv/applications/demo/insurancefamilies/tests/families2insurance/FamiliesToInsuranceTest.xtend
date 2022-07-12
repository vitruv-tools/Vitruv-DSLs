package tools.vitruv.applications.demo.insurancefamilies.tests.families2insurance

import edu.kit.ipd.sdq.metamodels.families.FamiliesFactory
import edu.kit.ipd.sdq.metamodels.families.Family
import edu.kit.ipd.sdq.metamodels.families.FamilyRegister
import edu.kit.ipd.sdq.metamodels.families.Member
import edu.kit.ipd.sdq.metamodels.insurance.Gender
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory
import java.nio.file.Path
import java.util.stream.Stream
import mir.reactions.insuranceToFamilies.InsuranceToFamiliesChangePropagationSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import tools.vitruv.applications.demo.insurancefamilies.families2insurance.FamiliesToInsuranceChangePropagationSpecification
import tools.vitruv.applications.demo.insurancefamilies.families2insurance.FamiliesToInsuranceHelper
import tools.vitruv.testutils.VitruvApplicationTest

import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static tools.vitruv.testutils.matchers.ModelMatchers.*
import tools.vitruv.change.propagation.ChangePropagationMode

enum MemberRole {
	Father,
	Mother,
	Son,
	Daughter
}

class FamiliesToInsuranceTest extends VitruvApplicationTest {
	
	// First Set of reused static strings for the first names of the persons
	final static String FIRST_DAD_1 = "Anton"
	final static String FIRST_MOM_1 = "Berta"
	final static String FIRST_SON_1 = "Chris"
	final static String FIRST_DAU_1 = "Daria"

	// Second Set of reused static strings for the first names of the persons
	final static String FIRST_DAD_2 = "Adam"
	final static String FIRST_MOM_2 = "Birgit"
	final static String FIRST_SON_2 = "Charles"
	final static String FIRST_DAU_2 = "Daniela"

	// Set of reused static strings for the last names of the persons
	final static String LAST_NAME_1 = "Meier"
	final static String LAST_NAME_2 = "Schulze"
	final static String LAST_NAME_3 = "Müller"
	
	/* Static reusable predefined InsuranceClients.
	 * The first number indicates from which string set (above) the forename is.
	 * the second number indicates from which string set (above) the lastname is.
	 */
	final static InsuranceClient DAD11 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAD_1 + " " + LAST_NAME_1 gender = Gender.MALE]
	final static InsuranceClient MOM11 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_MOM_1 + " " + LAST_NAME_1 gender = Gender.FEMALE]
	final static InsuranceClient SON11 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_SON_1 + " " + LAST_NAME_1 gender = Gender.MALE]
	final static InsuranceClient DAU11 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAU_1 + " " + LAST_NAME_1 gender = Gender.FEMALE]

	final static InsuranceClient DAD12 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAD_1 + " " + LAST_NAME_2 gender = Gender.MALE]
	final static InsuranceClient MOM12 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_MOM_1 + " " + LAST_NAME_2 gender = Gender.FEMALE]
	final static InsuranceClient SON12 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_SON_1 + " " + LAST_NAME_2 gender = Gender.MALE]
	final static InsuranceClient DAU12 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAU_1 + " " + LAST_NAME_2 gender = Gender.FEMALE]

	final static InsuranceClient DAD21 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAD_2 + " " + LAST_NAME_1 gender = Gender.MALE]
	final static InsuranceClient MOM21 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_MOM_2 + " " + LAST_NAME_1 gender = Gender.FEMALE]
	final static InsuranceClient SON21 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_SON_2 + " " + LAST_NAME_1 gender = Gender.MALE]
	final static InsuranceClient DAU21 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAU_2 + " " + LAST_NAME_1 gender = Gender.FEMALE]

	final static InsuranceClient DAD22 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAD_2 + " " + LAST_NAME_2 gender = Gender.MALE]
	final static InsuranceClient MOM22 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_MOM_2 + " " + LAST_NAME_2 gender = Gender.FEMALE]
	final static InsuranceClient SON22 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_SON_2 + " " + LAST_NAME_2 gender = Gender.MALE]
	final static InsuranceClient DAU22 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAU_2 + " " + LAST_NAME_2 gender = Gender.FEMALE]

	// Model Paths
	final static Path INSURANCE_MODEL = Path.of('model/insurance.insurance')
	final static Path FAMILIES_MODEL = Path.of("model/families.families")

	/**Set the correct set of reactions and routines for this test suite
	 */
	override protected getChangePropagationSpecifications() {
		return #[new FamiliesToInsuranceChangePropagationSpecification(), new InsuranceToFamiliesChangePropagationSpecification()]
	}
	
	@BeforeEach
	def disableTransitiveChangePropagation() {
		virtualModel.changePropagationMode = ChangePropagationMode.SINGLE_STEP
	}

	/**Before each test a new {@link FamilyRegister} is created as starting point.
	 * This is checked by several assertions to ensure correct preconditions for the tests.
	 */
	@BeforeEach
	def void insertRegister(TestInfo testInfo) {
		testInfo.getDisplayName()
		val x = resourceAt(FAMILIES_MODEL)
		x.propagate[contents += FamiliesFactory.eINSTANCE.createFamilyRegister]
		assertThat(resourceAt(INSURANCE_MODEL), exists)
		assertEquals(1, resourceAt(INSURANCE_MODEL).contents.size)
		assertEquals(1, resourceAt(INSURANCE_MODEL).allContents.size)
		assertThat(resourceAt(INSURANCE_MODEL).contents.get(0), instanceOf(InsuranceDatabase))
		assertEquals(0, resourceAt(INSURANCE_MODEL).contents.get(0).eAllContents().size)
	}
	
	// === TEST: FAMILY-REGISTER ===
	@Test
	def void testDeleteFamilyRegister(){
		this.createOneFamilyBeforeTesting()
		
		resourceAt(FAMILIES_MODEL).propagate[contents.clear()];
		
		assertEquals(0, resourceAt(FAMILIES_MODEL).contents.size())
		assertEquals(0, resourceAt(INSURANCE_MODEL).contents.size())
		assertThat(resourceAt(FAMILIES_MODEL), not(exists))
		assertThat(resourceAt(INSURANCE_MODEL), not(exists))
	}
	
	@Test
	def void deleteFamilyWithMatchingName(){
		this.createTwoFamiliesBeforeTesting();
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families.removeIf([it.lastName.equals(LAST_NAME_2)])
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD11, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase);
	}
	
	// === TESTS: FAMILY (Basic) ===

	@Test
	def void testInsertNewFamily(){
		val family = createFamily(LAST_NAME_1)
		
		FamilyRegister.from(FAMILIES_MODEL).propagate[families += family]
		
		val expectedInsuraceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase()
		assertCorrectInsuranceDatabase(expectedInsuraceDatabase)
	}
	
	@Test
	def void insertFamilyWithFather() {
		val family = createFamily(LAST_NAME_1)
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += family
			family.father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_1]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [
				name = FIRST_DAD_1 + " " + LAST_NAME_1
				gender = Gender.MALE
			]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void insertFamilyWithMother() {
		val family = createFamily(LAST_NAME_1)
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += family
			family.mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_1]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [
				name = FIRST_MOM_1 + " " + LAST_NAME_1
				gender = Gender.FEMALE
			]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void insertFamilyWithSon(){
		val family = createFamily(LAST_NAME_1)
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += family
			family.sons += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_SON_1]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [
				name = FIRST_SON_1 + " " + LAST_NAME_1
				gender = Gender.MALE
			]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void insertFamilyWithDaughter(){
		val family = createFamily(LAST_NAME_1)
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += family
			family.daughters += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAU_1]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [
				name = FIRST_DAU_1 + " " + LAST_NAME_1
				gender = Gender.FEMALE
			]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void deleteFatherFromFamily(){
		this.createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.father.firstName.equals(FIRST_DAD_1)
			]
			selectedFamily.father = null
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void deleteMotherFromFamily(){
		this.createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.mother.firstName.equals(FIRST_MOM_1)
			]
			selectedFamily.mother = null
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void deleteSonFromFamily(){
		this.createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.sons.stream().anyMatch[s | s.firstName.equals(FIRST_SON_1)]
			]
			selectedFamily.sons.removeIf[s | s.firstName.equals(FIRST_SON_1)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAU11, DAD11, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void deleteDautherFromFamily(){
		this.createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.daughters.stream().anyMatch[s | s.firstName.equals(FIRST_DAU_1)]
			]
			selectedFamily.daughters.removeIf[s | s.firstName.equals(FIRST_DAU_1)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAD11, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testChangeLastName() {
		this.createOneFamilyBeforeTesting();
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			selectedFamily.lastName = LAST_NAME_2
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON12, DAU12, DAD12, MOM12]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	
	// === TESTS: MEMBER ===
	
	@Test
	def void testChangeFirstNameFather() {
		this.createOneFamilyBeforeTesting()

		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.father.firstName.equals(FIRST_DAD_1)
			]
			selectedFamily.father.firstName = FIRST_DAD_2
		]

		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD21, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testChangeFirstNameMother() {
		this.createOneFamilyBeforeTesting()

		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.mother.firstName.equals(FIRST_MOM_1)
			]
			selectedFamily.mother.firstName = FIRST_MOM_2
		]

		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD11, MOM21]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testChangeFirstNameSon() {
		this.createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.sons.exists[son|son.firstName.equals(FIRST_SON_1)]
			]
			val sonToChange = selectedFamily.sons.findFirst[it.firstName.equals(FIRST_SON_1)]
			sonToChange.firstName = FIRST_SON_2
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON21, DAU11, DAD11, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testChangeFirstNameDaugther() {
		this.createOneFamilyBeforeTesting()

		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val selectedFamily = families.findFirst [
				it.lastName.equals(LAST_NAME_1) && it.daughters.exists[it.firstName.equals(FIRST_DAU_1)]
			]
			val daughterToChange = selectedFamily.daughters.findFirst[it.firstName.equals(FIRST_DAU_1)]
			daughterToChange.firstName = FIRST_DAU_2
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU21, DAD11, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	// === TESTS: FAMILY (Switch/Replace) ===
	
	// replacement of father causes deletion of old father and creates new father  
	@Test
	def void testReplaceFatherWithNewMember(){
		this.createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			family.father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_2]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, MOM11, DAD21]
		]
		
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	// replacement of father (1) with existing father (2) causes 
	// name change of existing father (2) 
	// deletion of existing father (1)
	@Test
	def void testReplaceFatherWithExistingFather(){
		this.createTwoFamiliesBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val family2 = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			family1.father = family2.father;
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD21, SON11, DAU11, MOM11, SON22, DAU22, MOM22]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	// replacement of the father (1) with a father (2) from a family with only one member causes
	// deletion of the original father (1)
	// new name of the new father (2)
	@Test
	def void testReplaceFatherWithExistingPreviouslyLonlyFather(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_2
				father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_2]
			]
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val family2 = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			family1.father = family2.father
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD21, MOM11, SON11, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	// replacement of the father (1) with a son (2) from another family causes
	// deletion of father (1)
	// name change of son/new father (2)
	@Test
	def void testReplaceFatherWithExistingSon(){
		this.createTwoFamiliesBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val family2 = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			family1.father = family2.sons.findFirst[son|son.firstName.equals(FIRST_SON_2)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON21, MOM11, SON11, DAU11, DAD22, MOM22, DAU22]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testReplaceMotherWithNewMember(){
		this.createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			family.mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_2]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD11, MOM21]
		]
		
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testReplaceMotherWithExistingMother(){
		this.createTwoFamiliesBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val family2 = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			family1.mother = family2.mother;
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, SON11, DAU11, MOM21, SON22, DAU22, DAD22]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testReplaceMotherWithExistingPreviouslyLonlyMother(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_2
				mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_2]
			]
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val family2 = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			family1.mother = family2.mother
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, MOM21, SON11, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testReplaceMotherWithExistingDaughter(){
		this.createTwoFamiliesBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val family2 = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			family1.mother = family2.daughters.findFirst[daugther|daugther.firstName.equals(FIRST_DAU_2)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD11, DAU21, SON22, DAD22, MOM22]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchFamilySamePositionFather(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.father = oldFamily.father
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD12, MOM11, SON11, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchFamilySamePositionMother(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]

		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.mother = oldFamily.mother
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, MOM12, SON11, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchFamilySamePositionSon(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.sons += oldFamily.sons.findFirst[son|son.firstName.equals(FIRST_SON_1)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, MOM11, SON12, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchFamilySamePositionDaugther(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.daughters += oldFamily.daughters.findFirst[daughter|daughter.firstName.equals(FIRST_DAU_1)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, MOM11, SON11, DAU12]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testRepetedlyMovingFatherBetweenFamilies(){
		val String first_mom_3 = "Beate"
		val InsuranceClient dad13 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAD_1 + " " + LAST_NAME_3 gender = Gender.MALE]
		val InsuranceClient mom33 = InsuranceFactory.eINSTANCE.createInsuranceClient => [name = first_mom_3 + " " + LAST_NAME_3 gender = Gender.FEMALE]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = createFamily(LAST_NAME_1)
			val family2 = createFamily(LAST_NAME_2)
			val family3 = createFamily(LAST_NAME_3)
			families += #[family1, family2, family3]
			family1.father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_1]
			family2.father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_2]

			family1.mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_1]
			family2.mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_2]
			family3.mother = FamiliesFactory.eINSTANCE.createMember => [firstName = first_mom_3]
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family1 = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val family2 = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			val family3 = families.findFirst[it.lastName.equals(LAST_NAME_3)]

			family3.father = family2.father
			family2.father = family1.father
			family1.father = family3.father
			family3.father = family2.father
			family2.father = family1.father
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[dad13, DAD22, MOM11, MOM22, mom33]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchSonToFather(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.father = oldFamily.sons.findFirst[son|son.firstName.equals(FIRST_SON_1)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, MOM11, SON12, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchFatherToSon(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.sons += oldFamily.father
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD12, MOM11, SON11, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchDautherToMother(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.mother = oldFamily.daughters.findFirst[daughter|daughter.firstName.equals(FIRST_DAU_1)]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, MOM11, SON11, DAU12]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	@Test
	def void testSwitchMotherToDaughter(){
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += this.createFamily(LAST_NAME_2)
		]
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val oldFamily = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			val newFamily = families.findFirst[it.lastName.equals(LAST_NAME_2)]
			newFamily.daughters += oldFamily.mother
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[DAD11, MOM12, SON11, DAU11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	// TODO
	@Disabled("Discuss if intended behavior is correct and how to achieve it")
	@Test
	def void familyGetsDeletedIfEmpty(){
		// case father
		createOneFamilyBeforeTesting()
		
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			val family = families.findFirst[it.lastName.equals(LAST_NAME_1)]
			family.mother = null
			family.sons -= family.sons
			family.daughters -= family.daughters
			
			family.father = null
		]
		
		val FamilyRegister expectedFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister
		assertCorrectFamilyRegister(expectedFamilyRegister)
		
		// TODO: add missing cases
	}
	
	@Test
	def void testExceptionSexChanges_AssignMotherToFather() {
		this.createTwoFamiliesBeforeTesting()
		val thrownExceptionAssignMotherToFather = assertThrows(UnsupportedOperationException, [
			FamilyRegister.from(FAMILIES_MODEL).propagate [
				val family1 = families.findFirst[family|family.lastName.equals(LAST_NAME_1)]
				val family2 = families.findFirst[family|family.lastName.equals(LAST_NAME_2)]
				family1.father = family2.mother
			]
		])
		
		val String expectedMessage = "The position of a male family member can only be assigned to members with no or a male corresponding insurance client."
		assertEquals(expectedMessage, thrownExceptionAssignMotherToFather.message)
	}
	
	@Test
	def void testExceptionSexChanges_AssignDaughterToSon() {
		this.createTwoFamiliesBeforeTesting()
		
		val thrownExceptionAssignDaughterToSon = assertThrows(UnsupportedOperationException, [
			FamilyRegister.from(FAMILIES_MODEL).propagate [
				val family1 = families.findFirst[family|family.lastName.equals(LAST_NAME_1)]
				val family2 = families.findFirst[family|family.lastName.equals(LAST_NAME_2)]
				family1.sons += family2.daughters.findFirst[daughter|daughter.firstName.equals(FIRST_DAU_2)]
			]
		])
		
		val String expectedMessage = "The position of a male family member can only be assigned to members with no or a male corresponding insurance client."
		assertEquals(expectedMessage, thrownExceptionAssignDaughterToSon.message)
	}

	@Test
	def void testExceptionSexChanges_AssignFatherToMother() {
		this.createTwoFamiliesBeforeTesting()
		
		val thrownExceptionAssignFatherToMother = assertThrows(UnsupportedOperationException, [
			FamilyRegister.from(FAMILIES_MODEL).propagate [
				val family1 = families.findFirst[family|family.lastName.equals(LAST_NAME_1)]
				val family2 = families.findFirst[family|family.lastName.equals(LAST_NAME_2)]
				family1.mother = family2.father
			]
		])
		
		val String expectedMessage = "The position of a female family member can only be assigned to members with no or a female corresponding insurance client."
		assertEquals(expectedMessage, thrownExceptionAssignFatherToMother.message)
	}

	@Test
	def void testExceptionSexChanges_AssignSonToDaughter() {
		this.createTwoFamiliesBeforeTesting()
		
		val thrownExceptionAssignSonToDaughter = assertThrows(UnsupportedOperationException, [
			FamilyRegister.from(FAMILIES_MODEL).propagate [
				val family1 = families.findFirst[family|family.lastName.equals(LAST_NAME_1)]
				val family2 = families.findFirst[family|family.lastName.equals(LAST_NAME_2)]
				family1.daughters += family2.sons.findFirst[son|son.firstName.equals(FIRST_SON_2)]
			]
		])
		
		val String expectedMessage = "The position of a female family member can only be assigned to members with no or a female corresponding insurance client."
		assertEquals(expectedMessage, thrownExceptionAssignSonToDaughter.message)
	}
	
	def String unescapeString(String string) {
		return string.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
	}

	@ParameterizedTest(name = "{index} => role={0}, escapedNewName={1}, expectedExceptionMessage={2}")
	@MethodSource("nameAndExceptionProvider")
	def void testExceptionRenamingMemberWithInvalidFirstName(MemberRole role, String escapedNewName, String expectedExceptionMessage) {

		val unescapedNewName = if (escapedNewName !== null) unescapeString(escapedNewName) else null
		this.createOneFamilyBeforeTesting()
		
		val thrownExceptionSetNullAsFirstName = assertThrows(IllegalStateException, [
			FamilyRegister.from(FAMILIES_MODEL).propagate [
				val family1 = families.findFirst[family|family.lastName.equals(LAST_NAME_1)]
				switch role {
					case MemberRole.Father: family1.father.firstName = unescapedNewName
					case MemberRole.Mother: family1.mother.firstName = unescapedNewName
					case MemberRole.Son: family1.sons.findFirst[son|son.firstName.equals(FIRST_SON_1)].firstName = unescapedNewName
					case MemberRole.Daughter: family1.daughters.findFirst[daughter|daughter.firstName.equals(FIRST_DAU_1)].firstName = unescapedNewName
				}
			]
		])
		
		val String expectedMessage = expectedExceptionMessage
		assertEquals(expectedMessage, thrownExceptionSetNullAsFirstName.message)
	}

	@ParameterizedTest(name = "{index} => role={0}, escapedNewName={1}, expectedExceptionMessage={2}")
	@MethodSource("nameAndExceptionProvider")
	def void testExceptionCreationOfMemberWithInvalidFirstName(MemberRole role, String escapedNewName, String expectedExceptionMessage) {
		val unescapedNewName = if (escapedNewName !== null) unescapeString(escapedNewName) else null
		this.createOneFamilyBeforeTesting()
		
		val thrownExceptionSetNullAsFirstName = assertThrows(IllegalStateException, [
			FamilyRegister.from(FAMILIES_MODEL).propagate [
				val family1 = families.findFirst[family|family.lastName.equals(LAST_NAME_1)]
				val Member newMember = FamiliesFactory.eINSTANCE.createMember => [firstName = unescapedNewName]
				switch role {
					case MemberRole.Father: family1.father = newMember
					case MemberRole.Mother: family1.mother = newMember
					case MemberRole.Son: family1.sons += newMember
					case MemberRole.Daughter: family1.daughters += newMember
				}
			]
		])
		
		val String expectedMessage = expectedExceptionMessage
		assertEquals(expectedMessage, thrownExceptionSetNullAsFirstName.message)
	}

	def static Stream<Arguments> nameAndExceptionProvider() {
		Stream.of(
			Arguments.of(MemberRole.Father, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL),
			Arguments.of(MemberRole.Father, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Father, "\\n\\t\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Father, FIRST_DAD_1 + "\\n", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Father, FIRST_DAD_1 + "\\t", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Father, FIRST_DAD_1 + "\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Mother, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL),
			Arguments.of(MemberRole.Mother, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Mother, "\\t\\n\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Mother, FIRST_MOM_1 + "\\n", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Mother, FIRST_MOM_1 + "\\t", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Mother, FIRST_MOM_1 + "\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Son, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL),
			Arguments.of(MemberRole.Son, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Son, "\\n\\t\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Son, FIRST_SON_1 + "\\n", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Son, FIRST_SON_1 + "\\t", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Son, FIRST_SON_1 + "\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Daughter, null, FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL),
			Arguments.of(MemberRole.Daughter, "", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Daughter, "\\t\\n\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE),
			Arguments.of(MemberRole.Daughter, FIRST_DAU_1 + "\\n", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Daughter, FIRST_DAU_1 + "\\t", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES),
			Arguments.of(MemberRole.Daughter, FIRST_DAU_1 + "\\r", FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES)
		)
	}
	
	def void testCreatingFamilyWithEmptyLastName() {
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += FamiliesFactory.eINSTANCE.createFamily => [
				lastName = ""
				father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_1]
				mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_1]
				sons += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_SON_1]
				daughters += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAU_1]
			]
		]
		
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAD_1 gender = Gender.MALE]
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_MOM_1 gender = Gender.FEMALE]
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_SON_1 gender = Gender.MALE]
			insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [name = FIRST_DAU_1 gender = Gender.FEMALE]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	// === HELPER ===
	
	// creators
	
	def createFamily(String familieName) {
		FamiliesFactory.eINSTANCE.createFamily => [lastName = familieName]
	}
	
	/**Used to build the starting point for many other tests like deleting and renaming operations.
	 * Creates a {@link Family} including a father, a mother, a son and a daughter and maps this
	 * changes to the {@link InsuranceDatabase} which then includes four {@link InsuranceClient}.
	 */
	def void createOneFamilyBeforeTesting() {
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_1
				father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_1]
				mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_1]
				sons += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_SON_1]
				daughters += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAU_1]
			]
		]
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD11, MOM11]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}

	/**Used to build an extended starting point for many other tests like replacing and moving members.
	 * Creates a {@link Family} including a father, a mother, a son and a daughter and maps this
	 * changes to the {@link InsuranceDatabase} which then includes eight {@link InsuranceClient}.
	 */
	def void createTwoFamiliesBeforeTesting() {
		this.createOneFamilyBeforeTesting()
		FamilyRegister.from(FAMILIES_MODEL).propagate [
			families += FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_2
				father = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAD_2]
				mother = FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_MOM_2]
				sons += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_SON_2]
				daughters += FamiliesFactory.eINSTANCE.createMember => [firstName = FIRST_DAU_2]
			]
		]
		val InsuranceDatabase expectedInsuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase => [
			insuranceclient += #[SON11, DAU11, DAD11, MOM11, SON22, DAU22, DAD22, MOM22]
		]
		assertCorrectInsuranceDatabase(expectedInsuranceDatabase)
	}
	
	// assertions
	
	def assertCorrectInsuranceDatabase(InsuranceDatabase expectedInsuranceDatabase) {
		val insuranceModel = resourceAt(INSURANCE_MODEL)
		assertThat(insuranceModel, exists)
		assertEquals(1, insuranceModel.contents.size)
		val insuranceDatabase = insuranceModel.contents.get(0)
		assertThat(insuranceDatabase, instanceOf(InsuranceDatabase))
		val InsuranceDatabase castedInsuranceDatabase = insuranceDatabase as InsuranceDatabase
		
		
		assertEquals(expectedInsuranceDatabase.insuranceclient.size(), castedInsuranceDatabase.insuranceclient.size());
		assertThat(castedInsuranceDatabase.insuranceclient, containsAllOf(expectedInsuranceDatabase.insuranceclient));
		assertThat(expectedInsuranceDatabase.insuranceclient, containsAllOf(castedInsuranceDatabase.insuranceclient));
		// TODO: check if other asserts are needed
		// assertThat(castedInsuranceDatabase, equalsDeeply(expectedInsuranceDatabase))
	}
	
	/**Checks if the actual {@link FamilyRegister looks like the expected one.
	 */
	def void assertCorrectFamilyRegister(FamilyRegister expectedFamilyRegister){
		val familyModel = resourceAt(FAMILIES_MODEL)
		assertThat(familyModel, exists)
		assertEquals(1, familyModel.contents.size)
		val familyRegister = familyModel.contents.get(0)
		assertThat(familyRegister, instanceOf(FamilyRegister))
		val FamilyRegister castedFamilyRegister = familyRegister as FamilyRegister
		assertThat(castedFamilyRegister, equalsDeeply(expectedFamilyRegister))
	}
}
