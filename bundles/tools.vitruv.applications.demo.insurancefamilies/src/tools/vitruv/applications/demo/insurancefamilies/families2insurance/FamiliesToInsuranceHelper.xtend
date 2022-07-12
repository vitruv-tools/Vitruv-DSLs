package tools.vitruv.applications.demo.insurancefamilies.families2insurance

import edu.kit.ipd.sdq.activextendannotations.Utility
import edu.kit.ipd.sdq.metamodels.families.Member
import edu.kit.ipd.sdq.metamodels.insurance.Gender
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient

import static extension edu.kit.ipd.sdq.metamodels.families.FamiliesUtil.getFamily

@Utility
class FamiliesToInsuranceHelper {
	
	public final static String EXCEPTION_MESSAGE_FIRSTNAME_NULL = "A member's firstname is not allowed to be null."
	public final static String EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE = "A member's firstname has to contain at least one non-whitespace character."
	public final static String EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES = "A member's firstname cannot contain any whitespace escape sequences."

	/** Returns the name of an InsuranceClient corresponding to a Member.
	 *
	 *  @param member Member from which a corresponding Insurance Client should be given a correct name.
	 *  @return Name that the corresponding Insurance Client from the insurance model should have.
	 */
	def static String getInsuranceClientName(Member member) {
		// TODO: handling of empty/invalid/... names?
		return member.firstName + " " + member.family.lastName
	}

	// TODO: check/rework name handling
	/**Checks if a members firstname is <code>null</code>, empty or contains escape sequences.
	 * @param member The member whose firstname is checked
	 * @throws <code>IllegalArgumentException</code> if firstname is not valid
	 */
	def static void assertValidFirstname(Member member) {
		if (member.firstName === null) {
			throw new IllegalStateException(EXCEPTION_MESSAGE_FIRSTNAME_NULL)
		} else if (member.firstName.trim.empty) {
			throw new IllegalStateException(EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE)
		} else if (member.firstName.contains("\n") || member.firstName.contains("\t") || member.firstName.contains("\r")){
			throw new IllegalStateException(EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES)
		}
	}
	
	/**Checks if a InsuranceClient is a Male and throws an exception if not.
	 * @param insuranceClient The Insurance Client which is supposed to be a <code>Male</code>.
	 * @throws <code>UnsupportedOperationException</code>, if the insuranceClient is not a <code>Male</code>.
	 */
	def static void assertMale(InsuranceClient insuranceClient) {
		if (!(insuranceClient.gender === Gender.MALE)) {
			throw new UnsupportedOperationException(
				"The position of a male family member can only be assigned to members with no or a male corresponding insurance client."
			)
		}
	}

	/**Checks if a insurance client is a Female and throws an exception if not.
	 * @param insuranceClient The Insurance Client which is supposed to be a <code>Female</code>.
	 * @throws <code>UnsupportedOperationException</code>, if the insuranceClient is not a <code>Female</code>.
	 */
	def static void assertFemale(InsuranceClient insuranceClient) {
		if (!(insuranceClient.gender === Gender.FEMALE)) {
			throw new UnsupportedOperationException(
				"The position of a female family member can only be assigned to members with no or a female corresponding insurance client."
			)
		}
	}
	
	def static Gender isMaleToGender(Boolean isMale) {
		 isMale ? Gender.MALE : Gender.FEMALE
	}
}
