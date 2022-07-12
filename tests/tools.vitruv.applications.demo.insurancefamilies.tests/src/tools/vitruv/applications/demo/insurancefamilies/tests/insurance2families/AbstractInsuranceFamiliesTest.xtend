package tools.vitruv.applications.demo.insurancefamilies.tests.insurance2families;

import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceFactory
import java.nio.file.Path
import mir.reactions.familiesToInsurance.FamiliesToInsuranceChangePropagationSpecification
import mir.reactions.insuranceToFamilies.InsuranceToFamiliesChangePropagationSpecification
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend.lib.annotations.Accessors
import org.junit.jupiter.api.BeforeEach
import tools.vitruv.applications.demo.insurancefamilies.tests.util.InsuranceFamiliesViewFactory
import tools.vitruv.framework.views.View
import tools.vitruv.testutils.ViewBasedVitruvApplicationTest

import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static tools.vitruv.testutils.matchers.ModelMatchers.*

import static tools.vitruv.applications.demo.insurancefamilies.tests.util.FamiliesQueryUtil.claimFamiliesModel
import static tools.vitruv.applications.demo.insurancefamilies.tests.util.InsuranceQueryUtil.claimInsuranceDatabase
import static tools.vitruv.applications.demo.insurancefamilies.tests.util.TransformationDirectionConfiguration.configureUnidirectionalExecution
import edu.kit.ipd.sdq.metamodels.families.Family
import tools.vitruv.change.propagation.ChangePropagationMode
import edu.kit.ipd.sdq.metamodels.insurance.Gender
import tools.vitruv.testutils.TestUserInteraction.MultipleChoiceInteractionDescription

enum PositionPreference {
	Parent,
	Child
}

enum FamilyPreference {
	New,
	Existing
}

abstract class AbstractInsuranceFamiliesTest extends ViewBasedVitruvApplicationTest {
	boolean preferParent
	protected var extension InsuranceFamiliesViewFactory viewFactory
	
	// === setup ===
	
	@Accessors(PROTECTED_GETTER)
	static val MODEL_FILE_EXTENSION = "families"
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
	
	protected def getDefaultFamiliesModel(View view) {
		claimFamiliesModel(view)
	}

	protected def Path getProjectModelPath(String modelName) {
		Path.of(MODEL_FOLDER_NAME).resolve(modelName + "." + MODEL_FILE_EXTENSION)
	}

	override protected getChangePropagationSpecifications() {
		return #[new FamiliesToInsuranceChangePropagationSpecification(), new InsuranceToFamiliesChangePropagationSpecification()]
	}

	protected def void createAndRegisterRoot(View view, EObject rootObject, URI persistenceUri) {
		view.registerRoot(rootObject, persistenceUri)
	}
	
	// === creators ===
	
	private def void createInsuranceDatabase((InsuranceDatabase)=> void insuranceDatabaseInitialization) {
		changeInsuranceView [
			var insuranceDatabase = InsuranceFactory.eINSTANCE.createInsuranceDatabase
			insuranceDatabaseInitialization.apply(insuranceDatabase)
			createAndRegisterRoot(insuranceDatabase,  getProjectModelPath("insurance").uri)
		]
	}
	
	protected def InsuranceClient createInsuranceClient((InsuranceClient)=> void insuranceClientInitialization) {
		var insuranceClient = InsuranceFactory.eINSTANCE.createInsuranceClient
		insuranceClientInitialization.apply(insuranceClient)
		return insuranceClient
	}
	
	protected def String fullName(String firstName, String lastName) {
	 	return firstName + " " + lastName
	}
	
	// === initializers ===
	
	protected def void createEmptyInsuranceDatabase(){
		createInsuranceDatabase[]
	}
	
	protected def void createInsuranceDatabaseWithCompleteFamily() {
		createInsuranceDatabase[]
		
		decideParentOrChild(PositionPreference.Parent)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient[ name = fullName(FIRST_DAD_1, LAST_NAME_1) gender = Gender.MALE]
			]
		]
		
		decideParentOrChild(PositionPreference.Parent)
		decideNewOrExistingFamily(FamilyPreference.Existing, 1)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient[ name = fullName(FIRST_MOM_1, LAST_NAME_1) gender = Gender.FEMALE]
			]
		]
		
		decideParentOrChild(PositionPreference.Child)
		decideNewOrExistingFamily(FamilyPreference.Existing, 1)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient[ name = fullName(FIRST_SON_1, LAST_NAME_1) gender = Gender.MALE]
			]
		]
		
		decideParentOrChild(PositionPreference.Child)
		decideNewOrExistingFamily(FamilyPreference.Existing, 1)
		changeInsuranceView [
			claimInsuranceDatabase(it) => [
				insuranceclient += createInsuranceClient[ name = fullName(FIRST_DAU_1, LAST_NAME_1) gender = Gender.FEMALE]
			]
		]
	}
	
	// === interaction ===
	
	protected def void decideParentOrChild(PositionPreference preference) {
		val String parentChildTitle = "Parent or Child?"
		this.preferParent = preference === PositionPreference.Parent
		userInteraction.onMultipleChoiceSingleSelection[title.equals(parentChildTitle)].respondWithChoiceAt(if (preference === PositionPreference.Parent) 0 else 1)
	}
	
	def void decideNewOrExistingFamily(FamilyPreference preference, int familyIndex) {
		userInteraction
			.onMultipleChoiceSingleSelection[assertFamilyOptions(it)]
			.respondWithChoiceAt(if (preference === FamilyPreference.New) 0 else familyIndex)
	}
	
	// === assertions ===
	
	protected def void assertFamily(Family expected, Family actual) {
		assertThat(actual, equalsDeeply(expected));
	}
	
	val String newOrExistingFamilyTitle = "New or Existing Family?"
	
	def boolean assertFamilyOptions(MultipleChoiceInteractionDescription interactionDescription) {
		//First option is always a new family
		assertEquals(interactionDescription.choices.get(0), "insert in a new family")
		val tail = interactionDescription.choices.drop(1)
		//There must be a second option otherwise there would not be an interaction
		assertTrue(tail.size > 0)
		val familyName = tail.get(0).split(":").get(0)
		//All other options have to offer families with the same name
		tail.forEach[familyOption|familyOption.split(":").get(0).equals(familyName)]

		if (preferParent) {
			//If we want to insert a parent, each offered family has to not have this kind of parent
			//Therefore all families either must not have a father or must not have a mother
			val noFathers = tail.forall[!it.matches(".*F:.*;.*")]
			val noMothers = tail.forall[!it.matches(".*M:.*;.*")]
			assertTrue(noFathers || noMothers)
		}

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
}
