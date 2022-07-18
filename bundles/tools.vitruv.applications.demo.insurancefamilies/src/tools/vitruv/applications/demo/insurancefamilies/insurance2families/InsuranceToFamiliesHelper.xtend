package tools.vitruv.applications.demo.insurancefamilies.insurance2families;

import edu.kit.ipd.sdq.activextendannotations.Utility
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase
import edu.kit.ipd.sdq.metamodels.families.Family
import java.util.Collection
import java.util.ArrayList
import edu.kit.ipd.sdq.metamodels.insurance.Gender
import tools.vitruv.change.interaction.UserInteractor
import tools.vitruv.change.interaction.UserInteractionOptions.WindowModality

enum FamilyRole {
	Parent,
	Child
}

@Utility
class InsuranceToFamiliesHelper {
	public final static String EXCEPTION_MESSAGE_FIRSTNAME_NULL = "A insurance clients's name is not allowed to be null."
	public final static String EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE = "A insurance clients's name has to contain at least one non-whitespace character."
	public final static String EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES = "A insurance clients's name cannot contain any whitespace escape sequences."
	
	def static InsuranceDatabase getInsuranceDatabase(InsuranceClient insuranceClient) {
		return if (insuranceClient.eContainer instanceof InsuranceDatabase)	insuranceClient.eContainer as InsuranceDatabase else null
	}
	
	def static String getLastName(InsuranceClient insuranceClient) {
		insuranceClient.name.split(" ").last
	}
	
	def static String getFirstName(InsuranceClient insuranceClient) {
		insuranceClient.name.split(" ").get(0)
	}
	
	def static (Family)=>boolean sameLastName(InsuranceClient insuranceClient) {
		val String newPersonLastname = insuranceClient.lastName
		return [family|family.lastName.equals(newPersonLastname)]
	}
	
	def static (Family)=>boolean noParent(InsuranceClient insuranceClient) {
		return [family|if(insuranceClient.gender === Gender.MALE) family.father === null else family.mother === null]
	}
	
	def static void informUserAboutReplacementOfClient(UserInteractor userInteractor, InsuranceClient insuranceClient, Family oldFamily){
		var StringBuilder message = new StringBuilder()
			.append("Insurance Client ")
			.append(insuranceClient.name)
			.append(" has been replaced by another insurance client in his family (")
			.append(oldFamily.lastName)
			.append("). Please decide in which family and role ")
			.append(insuranceClient.name)
			.append(" should be.")
		
		userInteractor.notificationDialogBuilder.message(message.toString()).title("Insurance Client has been replaced in his original family").startInteraction()
	}
	
	def static FamilyRole askUserWhetherClientIsParentOrChild(UserInteractor userInteractor, InsuranceClient insuranceClient) {

		val StringBuilder parentOrChildMessageBuilder = new StringBuilder()
			.append("You have inserted ")
			.append(insuranceClient.name)
			.append(" into the insurance database which results into the creation of a corresponding member into the family register.")
			.append(" Is this member supposed to be a parent or a child in the family register?")

		var Iterable<String> parentOrChildOptions = #["Parent", "Child"]

		val int parentOrChildSelection = userInteractor
			.singleSelectionDialogBuilder
			.message(parentOrChildMessageBuilder.toString())
			.choices(parentOrChildOptions)
			.title("Parent or Child?")
			.windowModality(WindowModality.MODAL)
			.startInteraction()

		return if (parentOrChildSelection == parentOrChildOptions.toList.indexOf("Child")) FamilyRole.Child else FamilyRole.Parent
	}
	
	def static Family askUserWhichFamilyToInsertTheMemberIn(UserInteractor userInteractor, InsuranceClient newClient, Iterable<Family> selectableFamilies) {
		// Let user select the family
		var StringBuilder whichFamilyMessageBuilder = new StringBuilder()
			.append("Please choose whether you want to create a new family or insert ")
			.append(newClient.name)
			.append(" into one of the existing families.")

		// Prepare options to select from
		val Collection<String> whichFamilyOptions = new ArrayList<String>()
		whichFamilyOptions.^add("insert in a new family")
		whichFamilyOptions.addAll(selectableFamilies.map[stringifyFamily(it)])

		// Start interaction
		val whichFamilyIndex = userInteractor
			.singleSelectionDialogBuilder
			.message(whichFamilyMessageBuilder.toString())
			.choices(whichFamilyOptions)
			.title("New or Existing Family?")
			.windowModality(WindowModality.MODAL)
			.startInteraction()

		return if (whichFamilyIndex === 0) null else selectableFamilies.get(whichFamilyIndex-1)
	}
	
	def static FamilyRole askUserWhetherClientIsParentOrChildDuringRenaming(UserInteractor userInteractor, String oldFullname, String newFullname, boolean wasChildBefore) {

		val StringBuilder parentOrChildMessageBuilder = new StringBuilder()
			.append("You have renamed ")
			.append(oldFullname)
			.append(" to ")
			.append(newFullname)
			.append(", which might cause the corresponding member in the families model to change its position inside a family.")
			.append(" Which position should this member, who was a ")
			.append(if (wasChildBefore) "child" else "parent")
			.append(" before, have after the renaming?")

		var Iterable<String> parentOrChildOptions = #["Parent", "Child"]

		val int parentOrChildSelection = userInteractor
			.singleSelectionDialogBuilder
			.message(parentOrChildMessageBuilder.toString())
			.choices(parentOrChildOptions)
			.title("Parent or Child?")
			.windowModality(WindowModality.MODAL)
			.startInteraction()

		return if (parentOrChildSelection == parentOrChildOptions.toList.indexOf("Child")) FamilyRole.Child else FamilyRole.Parent
	}
	
	def static String stringifyFamily(Family family) {
		val StringBuilder builder = new StringBuilder().append(family.lastName).append(": ")
		if (family.father !== null) { builder.append("F: ").append(family.father.firstName).append(";") }
		if (family.mother !== null) { builder.append("M: ").append(family.mother.firstName).append(";") }
		if (family.sons !== null && family.sons.size > 0) { builder.append(family.sons.join("S: (", ", ", ")", [it.firstName])) }
		if (family.daughters !== null && family.daughters.size > 0) { builder.append(family.daughters.join("D: (", ", ", ")", [it.firstName])) }
		return builder.toString()
	}
	
	/**Checks if a insurance clients name is <code>null</code>, empty or contains escape sequences.
	 * @param person The insurance client of which the name has to be valid.
	 * @throws <code>IllegalStateException</code>, if the insurance clients name in invalid.
	 */
	def static void assertValidName(InsuranceClient insuranceClient) {
		if (insuranceClient.name === null) {
			throw new IllegalStateException(EXCEPTION_MESSAGE_FIRSTNAME_NULL)
		} else if (insuranceClient.name.trim.empty) {
			throw new IllegalStateException(EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE)
		} else if (insuranceClient.name.contains("\n") || insuranceClient.name.contains("\t") || insuranceClient.name.contains("\r")) {
			throw new IllegalStateException(EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES)
		}
	}
	
}
