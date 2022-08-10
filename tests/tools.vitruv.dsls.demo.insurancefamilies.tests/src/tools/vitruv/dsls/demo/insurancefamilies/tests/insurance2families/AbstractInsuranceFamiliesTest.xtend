package tools.vitruv.dsls.demo.insurancefamilies.tests.insurance2families;

import edu.kit.ipd.sdq.metamodels.families.FamiliesFactory
import edu.kit.ipd.sdq.metamodels.families.Family
import edu.kit.ipd.sdq.metamodels.insurance.Gender
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory
import java.nio.file.Path
import mir.reactions.familiesToInsurance.FamiliesToInsuranceChangePropagationSpecification
import mir.reactions.insuranceToFamilies.InsuranceToFamiliesChangePropagationSpecification
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend.lib.annotations.Accessors
import org.junit.jupiter.api.BeforeEach
import tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceFamiliesViewFactory
import tools.vitruv.change.interaction.UserInteractionOptions.NotificationType
import tools.vitruv.change.propagation.ChangePropagationMode
import tools.vitruv.framework.views.View
import tools.vitruv.testutils.TestUserInteraction.MultipleChoiceInteractionDescription
import tools.vitruv.testutils.ViewBasedVitruvApplicationTest

import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static tools.vitruv.dsls.demo.insurancefamilies.tests.util.CreatorsUtil.createInsuranceClient
import static tools.vitruv.dsls.demo.insurancefamilies.tests.util.FamiliesQueryUtil.claimFamilyRegister
import static tools.vitruv.dsls.demo.insurancefamilies.tests.util.InsuranceQueryUtil.claimInsuranceDatabase
import static tools.vitruv.dsls.demo.insurancefamilies.tests.util.TransformationDirectionConfiguration.configureUnidirectionalExecution
import static tools.vitruv.testutils.matchers.ModelMatchers.*
import tools.vitruv.dsls.demo.insurancefamilies.insurance2families.PositionPreference
import org.eclipse.emf.ecore.util.EcoreUtil

enum FamilyPreference {
	New,
	Existing
}

abstract class AbstractInsuranceFamiliesTest extends ViewBasedVitruvApplicationTest {
	protected var extension InsuranceFamiliesViewFactory viewFactory
	
	// === setup ===
	
	@Accessors(PROTECTED_GETTER)
	static val FAMILY_MODEL_FILE_EXTENSION = "families"
	@Accessors(PROTECTED_GETTER)
	static val INSURANCE_MODEL_FILE_EXTENSION = "insurance"
	@Accessors(PROTECTED_GETTER)
	static val MODEL_FOLDER_NAME = "model"
	
	@BeforeEach
	def final void setupViewFactory(){
		viewFactory = new InsuranceFamiliesViewFactory(virtualModel);
	}
	
	@BeforeEach
	def setupTransformatonDirection() {
		configureUnidirectionalExecution(virtualModel)
	}
	
	@BeforeEach
	def disableTransitiveChangePropagation() {
		virtualModel.changePropagationMode = ChangePropagationMode.SINGLE_STEP
	}
	
	protected def getDefaultFamilyRegister(View view) {
		claimFamilyRegister(view)
	}

	protected def Path getProjectModelPath(String modelName, String modelFileExtension) {
		Path.of(MODEL_FOLDER_NAME).resolve(modelName + "." + modelFileExtension)
	}

	override protected getChangePropagationSpecifications() {
		return #[new FamiliesToInsuranceChangePropagationSpecification(), new InsuranceToFamiliesChangePropagationSpecification()]
	}

	protected def void createAndRegisterRoot(View view, EObject rootObject, URI persistenceUri) {
		view.registerRoot(rootObject, persistenceUri)
	}
	
	protected def void deleteRoot(View view, EObject rootObject) {
		EcoreUtil.delete(rootObject)
	}
	
	// === creators ===
	
	private def void createInsuranceDatabase((InsuranceDatabase)=> void insuranceDatabaseInitialization) {
		changeInsuranceView [
			var insuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase
			insuranceDatabaseInitialization.apply(insuranceDatabase)
			createAndRegisterRoot(insuranceDatabase,  getProjectModelPath("insurance", INSURANCE_MODEL_FILE_EXTENSION).uri)
		]
	}
	
	protected def String fullName(String firstName, String lastName) {
	 	return firstName + " " + lastName
	}
	
	// === initializers ===
	
	protected def void createEmptyInsuranceDatabase(){
		createInsuranceDatabase[]
	}
	
	protected def void createInsuranceDatabaseWithCompleteFamily() {
		createInsuranceDataBaseWithOptionalCompleteFamily(true, true, true, true)
	}
	
	protected def createInsuranceDataBaseWithOptionalCompleteFamily(boolean insertFather, boolean insertMother, boolean insertSon, boolean insertDaugther) {
		if(!(insertFather || insertMother || insertSon || insertDaugther)){
			throw new IllegalArgumentException("can't create empty family")
		}
		
		var insertCount = 0
		
		createInsuranceDatabase[]
		
		if(insertFather){
			decideParentOrChild(PositionPreference.Parent)
			changeInsuranceView [
				claimInsuranceDatabase(it) => [
					insuranceclient += createInsuranceClient[ name = fullName(FIRST_DAD_1, LAST_NAME_1) gender = Gender.MALE]
				]
			]
			insertCount++;
		}
		
		if(insertMother){
			decideParentOrChild(PositionPreference.Parent)
			if(insertCount > 0) decideNewOrExistingFamily(FamilyPreference.Existing, 1)
			changeInsuranceView [
				claimInsuranceDatabase(it) => [
					insuranceclient += createInsuranceClient[ name = fullName(FIRST_MOM_1, LAST_NAME_1) gender = Gender.FEMALE]
				]
			]
			insertCount++;
		}
	
		if(insertSon){
			decideParentOrChild(PositionPreference.Child)
			if(insertCount > 0) decideNewOrExistingFamily(FamilyPreference.Existing, 1)
			changeInsuranceView [
				claimInsuranceDatabase(it) => [
					insuranceclient += createInsuranceClient[ name = fullName(FIRST_SON_1, LAST_NAME_1) gender = Gender.MALE]
				]
			]
		}
		
		if(insertDaugther){
			decideParentOrChild(PositionPreference.Child)
			if(insertCount > 0) decideNewOrExistingFamily(FamilyPreference.Existing, 1)
			changeInsuranceView [
				claimInsuranceDatabase(it) => [
					insuranceclient += createInsuranceClient[ name = fullName(FIRST_DAU_1, LAST_NAME_1) gender = Gender.FEMALE]
				]
			]
		}
	}
	
	// === interaction ===
	
	protected def void awaitReplacementInformation(String insuranceClientName, String oldFamilyName){
		userInteraction.acknowledgeNotification[
			it.message == "Insurance Client " + insuranceClientName + 
				" has been replaced by another insurance client in his family (" + oldFamilyName +
				"). Please decide in which family and role " + insuranceClientName + " should be." 
				&&
			it.title == "Insurance Client has been replaced in his original family" &&
			it.notificationType == NotificationType.INFORMATION
		]
	}
	
	protected def void decideParentOrChild(PositionPreference preference) {
		val String parentChildTitle = "Parent or Child?"
		userInteraction.onMultipleChoiceSingleSelection[title.equals(parentChildTitle)].respondWithChoiceAt(if (preference === PositionPreference.Parent) 0 else 1)
	}
	
	protected def void decideNewOrExistingFamily(FamilyPreference preference, int familyIndex) {
		userInteraction
			.onMultipleChoiceSingleSelection[assertFamilyOptions(it)]
			.respondWithChoiceAt(if (preference === FamilyPreference.New) 0 else familyIndex)
	}
	
	// === assertions ===
	
	protected def void assertFamily(Family expected, Family actual) {
		assertThat(actual, equalsDeeply(expected));
	}
	
	protected def void assertNumberOfFamilies(View view, int expectedNumberOfFamilies){
		assertEquals(expectedNumberOfFamilies, claimFamilyRegister(view).families.size)
	}
	
	val String newOrExistingFamilyTitle = "New or Existing Family?"
	
	protected def boolean assertFamilyOptions(MultipleChoiceInteractionDescription interactionDescription) {
		//First option is always a new family
		assertEquals(interactionDescription.choices.get(0), "insert in a new family")
		val tail = interactionDescription.choices.drop(1)
		//There must be a second option otherwise there would not be an interaction
		assertTrue(tail.size > 0)
		val familyName = tail.get(0).split(":").get(0)
		//All other options have to offer families with the same name
		tail.forEach[familyOption|familyOption.split(":").get(0).equals(familyName)]
		
		return interactionDescription.title.equals(newOrExistingFamilyTitle)
	}
	
	// === data ===
	
	// First Set of reused static strings for the first names of the persons
	protected final static String FIRST_DAD_1 = "Anton"
	protected final static String FIRST_MOM_1 = "Berta"
	protected final static String FIRST_SON_1 = "Chris"
	protected final static String FIRST_DAU_1 = "Daria"

	// Second Set of reused static strings for the first names of the persons
	protected final static String FIRST_DAD_2 = "Adam"
	protected final static String FIRST_MOM_2 = "Birgit"
	protected final static String FIRST_SON_2 = "Charles"
	protected final static String FIRST_DAU_2 = "Daniela"

	// Set of reused static strings for the last names of the persons
	protected final static String LAST_NAME_1 = "Meier"
	protected final static String LAST_NAME_2 = "Schulze"
	protected final static String LAST_NAME_3 = "Müller"
	
	protected final static Family COMPLETE_FAMILY_1 = FamiliesFactory.eINSTANCE.createFamily => [
		lastName = LAST_NAME_1
		father = FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_DAD_1]
		mother = FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_MOM_1]
		sons += FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_SON_1]
		daughters += FamiliesFactory.eINSTANCE.createMember => [ firstName = FIRST_DAU_1]
	]
}
