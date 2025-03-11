package tools.vitruv.dsls.demo.insurancefamilies.families2insurance;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.metamodels.families.FamiliesUtil;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import org.eclipse.xtend2.lib.StringConcatenation;

@Utility
@SuppressWarnings("all")
public final class FamiliesToInsuranceHelper {
  public static final String EXCEPTION_MESSAGE_FIRSTNAME_NULL = "A member\'s firstname is not allowed to be null.";

  public static final String EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE = "A member\'s firstname has to contain at least one non-whitespace character.";

  public static final String EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES = "A member\'s firstname cannot contain any whitespace escape sequences.";

  /**
   * Returns the name of an InsuranceClient corresponding to a Member.
   * 
   *  @param member Member from which a corresponding Insurance Client should be given a correct name.
   *  @return Name that the corresponding Insurance Client from the insurance model should have.
   */
  public static String getInsuranceClientName(final Member member) {
    final StringBuilder name = new StringBuilder();
    name.append(member.getFirstName());
    if (((FamiliesUtil.getFamily(member).getLastName() != null) && (!FamiliesUtil.getFamily(member).getLastName().isEmpty()))) {
      String _lastName = FamiliesUtil.getFamily(member).getLastName();
      String _plus = (" " + _lastName);
      name.append(_plus);
    }
    return name.toString();
  }

  /**
   * Checks if a members firstname is <code>null</code>, empty or contains escape sequences.
   * @param member The member whose firstname is checked
   * @throws <code>IllegalArgumentException</code> if firstname is not valid
   */
  public static void assertValidFirstname(final Member member) {
    String _firstName = member.getFirstName();
    boolean _tripleEquals = (_firstName == null);
    if (_tripleEquals) {
      throw new IllegalStateException(FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL);
    } else {
      boolean _isEmpty = member.getFirstName().trim().isEmpty();
      if (_isEmpty) {
        throw new IllegalStateException(FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE);
      } else {
        if (((member.getFirstName().contains("\n") || member.getFirstName().contains("\t")) || member.getFirstName().contains("\r"))) {
          throw new IllegalStateException(FamiliesToInsuranceHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES);
        }
      }
    }
  }

  /**
   * Checks if a InsuranceClient has the expected gender and throws an exception if not.
   * @param insuranceClient The Insurance Client which is supposed to be of the expected gender.
   * @param expectedGender The expected Gender of the insuranceClient
   * @throws <code>UnsupportedOperationException</code>, if the insuranceClient is not of the expected gender.
   */
  public static void assertGender(final InsuranceClient insuranceClient, final Gender expectedGender) {
    Gender _gender = insuranceClient.getGender();
    boolean _tripleEquals = (_gender == expectedGender);
    boolean _not = (!_tripleEquals);
    if (_not) {
      String _xifexpression = null;
      if ((expectedGender == Gender.MALE)) {
        _xifexpression = "male";
      } else {
        _xifexpression = "female";
      }
      final String expectedGenderString = _xifexpression;
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("The position of a ");
      _builder.append(expectedGenderString);
      _builder.append(" family member can only be assigned to members with no or a ");
      _builder.append(expectedGenderString);
      _builder.append(" corresponding insurance client.");
      throw new UnsupportedOperationException(_builder.toString());
    }
  }

  public static Gender isMaleToGender(final Boolean isMale) {
    Gender _xifexpression = null;
    if ((isMale).booleanValue()) {
      _xifexpression = Gender.MALE;
    } else {
      _xifexpression = Gender.FEMALE;
    }
    return _xifexpression;
  }

  private FamiliesToInsuranceHelper() {
    
  }
}
