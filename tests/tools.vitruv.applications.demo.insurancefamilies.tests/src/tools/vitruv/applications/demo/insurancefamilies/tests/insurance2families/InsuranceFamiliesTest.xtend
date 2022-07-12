package tools.vitruv.applications.demo.insurancefamilies.tests.insurance2families

import edu.kit.ipd.sdq.metamodels.insurance.Gender
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static tools.vitruv.applications.demo.insurancefamilies.tests.util.FamiliesQueryUtil.claimFamiliesModel
import static tools.vitruv.applications.demo.insurancefamilies.tests.util.FamiliesQueryUtil.claimFamily
import static tools.vitruv.applications.demo.insurancefamilies.tests.util.InsuranceQueryUtil.claimInsuranceDatabase
import static tools.vitruv.applications.demo.insurancefamilies.tests.util.InsuranceQueryUtil.claimInsuranceClient
import edu.kit.ipd.sdq.metamodels.families.FamiliesFactory

class InsuranceFamiliesTest extends AbstractInsuranceFamiliesTest {
	
	// === TESTS: InsuranceDatabase ===
	
	/*
	 * - create InsuranceDatabase
	 * - delete InsuranceDatabase
	 */
	
	// === TESTS: Client ===
	
	@Test
	def void testCreatedClient_asFatherOfNewFamily(){
		decideParentOrChild(PositionPreference.Parent)
		createEmptyInsuranceDatabase();
		
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += InsuranceFactory.eINSTANCE.createInsuranceClient => [name = fullName(FIRST_DAD_1, LAST_NAME_1) gender = Gender.MALE]
			]
		]
		
		val expectedFamily = FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_1
				father = FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_DAD_1]
			]
		validateFamilyViewView [
			val family = claimFamily(defaultFamiliesModel, LAST_NAME_1)
			assertFamily(expectedFamily, family)
		]
	}
	
	@Test
	def void testCreatedClient_multiple(){
		createEmptyInsuranceDatabase();
		
		decideParentOrChild(PositionPreference.Parent)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient [name = fullName(FIRST_DAD_1, LAST_NAME_1) gender = Gender.MALE]
			]
		]
		decideParentOrChild(PositionPreference.Parent)
		decideNewOrExistingFamily(FamilyPreference.Existing, 1)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient [name = fullName(FIRST_MOM_1, LAST_NAME_1) gender = Gender.FEMALE]
			]
		]
		decideParentOrChild(PositionPreference.Child)
		decideNewOrExistingFamily(FamilyPreference.Existing, 1)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient [name = fullName(FIRST_SON_1, LAST_NAME_1) gender = Gender.MALE]
			]
		]
		decideParentOrChild(PositionPreference.Child)
		decideNewOrExistingFamily(FamilyPreference.Existing, 1)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient [name = fullName(FIRST_SON_2, LAST_NAME_1) gender = Gender.MALE]
			]
		]
		decideParentOrChild(PositionPreference.Child)
		decideNewOrExistingFamily(FamilyPreference.Existing, 1)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient [name = fullName(FIRST_DAU_1, LAST_NAME_1) gender = Gender.FEMALE]
			]
		]
		
		val expectedFamily = FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_1
				father = FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_DAD_1]
				mother = FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_MOM_1]
				sons += FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_SON_1]
				sons += FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_SON_2]
				daughters += FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_DAU_1]
			]
		validateFamilyViewView [
			val family = claimFamily(defaultFamiliesModel, LAST_NAME_1)
			assertFamily(expectedFamily, family)
		]
	}
	
	@Test
	def void testChangeClient_lastNameToNewFamily() {
		createInsuranceDatabaseWithCompleteFamily()
		
		decideParentOrChild(PositionPreference.Parent)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				claimInsuranceClient(it, FIRST_DAD_1, LAST_NAME_1) => [
					name = fullName(FIRST_DAD_1, LAST_NAME_2)
				]
			]
		]
		
		val expectedFamily1 = FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_1
				mother = FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_MOM_1]
				sons += FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_SON_1]
				daughters += FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_DAU_1]
			]
		val expectedFamily2 = FamiliesFactory.eINSTANCE.createFamily => [
				lastName = LAST_NAME_2
				father = FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_DAD_1]
			]
		validateFamilyViewView [
			val familiesCount = claimFamiliesModel(it).families.size
			assertEquals(2, familiesCount)
			
			val family1 = claimFamily(defaultFamiliesModel, LAST_NAME_1)
			assertFamily(expectedFamily1, family1)
			
			val family2 = claimFamily(defaultFamiliesModel, LAST_NAME_2)
			assertFamily(expectedFamily2, family2)
		]
	}
	
	// TODO: complete tests
}