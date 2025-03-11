package tools.vitruv.dsls.demo.familiespersons.persons2families;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.persons.Male;
import edu.kit.ipd.sdq.metamodels.persons.Person;
import edu.kit.ipd.sdq.metamodels.persons.PersonRegister;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.change.interaction.UserInteractionOptions;
import tools.vitruv.change.interaction.UserInteractor;

@Utility
@SuppressWarnings("all")
public final class PersonsToFamiliesHelper {
  public static final String EXCEPTION_MESSAGE_FIRSTNAME_NULL = "A person\'s fullname is not allowed to be null.";

  public static final String EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE = "A person\'s fullname has to contain at least one non-whitespace character.";

  public static final String EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES = "A person\'s fullname cannot contain any whitespace escape sequences.";

  /**
   * Returns the eContainer of a Person casted as Personregister, if it is contained in a Personregister.
   * @return <code>person.eContainer</code> as PersonRegister, if it actually is one; <code>null</code>, else.
   */
  public static PersonRegister getRegister(final Person person) {
    PersonRegister _xifexpression = null;
    EObject _eContainer = person.eContainer();
    if ((_eContainer instanceof PersonRegister)) {
      EObject _eContainer_1 = person.eContainer();
      _xifexpression = ((PersonRegister) _eContainer_1);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  /**
   * Creates a string representation of a family which is used to label the different options during user interactions.
   * Representation contains names and positions of all members. Person model informations such as birthdates are not used.
   * @param family Family to represent as string.
   * @return String representation.
   */
  public static String stringifyFamily(final Family family) {
    final StringBuilder builder = new StringBuilder().append(family.getLastName()).append(": ");
    Member _father = family.getFather();
    boolean _tripleNotEquals = (_father != null);
    if (_tripleNotEquals) {
      builder.append("F: ").append(family.getFather().getFirstName()).append(";");
    }
    Member _mother = family.getMother();
    boolean _tripleNotEquals_1 = (_mother != null);
    if (_tripleNotEquals_1) {
      builder.append("M: ").append(family.getMother().getFirstName()).append(";");
    }
    if (((family.getSons() != null) && (family.getSons().size() > 0))) {
      final Function1<Member, CharSequence> _function = new Function1<Member, CharSequence>() {
        public CharSequence apply(final Member it) {
          return it.getFirstName();
        }
      };
      builder.append(IterableExtensions.<Member>join(family.getSons(), "S: (", ", ", ")", _function));
    }
    if (((family.getDaughters() != null) && (family.getDaughters().size() > 0))) {
      final Function1<Member, CharSequence> _function_1 = new Function1<Member, CharSequence>() {
        public CharSequence apply(final Member it) {
          return it.getFirstName();
        }
      };
      builder.append(IterableExtensions.<Member>join(family.getDaughters(), "D: (", ", ", ")", _function_1));
    }
    return builder.toString();
  }

  /**
   * Extension, which returns the part of the person's fullname which represents the firstname. This is currently by convention
   * everything except the last part which is separated by a space from the rest of the name. If the name does not contain a whitespace,
   * the whole name is considered to be the firstname, which symbolizes, that the person has no lastname.
   * @param person Person to retrieve the firstname from.
   * @return As firstname interpreted part of the fullname.
   */
  public static String getFirstname(final Person person) {
    final Iterable<String> nameParts = (Iterable<String>)Conversions.doWrapArray(person.getFullName().split(" "));
    String firstName = null;
    int _size = IterableExtensions.size(nameParts);
    boolean _equals = (_size == 1);
    if (_equals) {
      firstName = person.getFullName();
    } else {
      int _size_1 = IterableExtensions.size(nameParts);
      int _minus = (_size_1 - 1);
      firstName = IterableExtensions.join(IterableExtensions.<String>take(nameParts, _minus), " ");
    }
    return firstName;
  }

  /**
   * Extension, which returns the part of the person's fullname which represents the lastname. This is currently by convention
   * the last part which is separated by a space from the rest of the name. If the name does not contain a whitespace,
   * the empty string is returned, which symbolizes, that the person has only a firstname and no lastname.
   * @param person Person to retrieve the lastname from.
   * @return As lastname interpreted part of the fullname or <code>""</code>
   */
  public static String getLastname(final Person person) {
    boolean _contains = person.getFullName().contains(" ");
    boolean _not = (!_contains);
    if (_not) {
      return "";
    } else {
      return IterableExtensions.<String>last(((Iterable<String>)Conversions.doWrapArray(person.getFullName().split(" "))));
    }
  }

  public static FamilyRole askUserWhetherPersonIsParentOrChild(final UserInteractor userInteractor, final Person newPerson) {
    final StringBuilder parentOrChildMessageBuilder = new StringBuilder().append("You have inserted ").append(newPerson.getFullName()).append(" into the persons register which results into the creation of a corresponding member into the family register.").append(" Is this member supposed to be a parent or a child in the family register?");
    Iterable<String> parentOrChildOptions = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("Parent", "Child"));
    final int parentOrChildSelection = (userInteractor.getSingleSelectionDialogBuilder().message(parentOrChildMessageBuilder.toString()).choices(parentOrChildOptions).title("Parent or Child?").windowModality(UserInteractionOptions.WindowModality.MODAL).startInteraction()).intValue();
    FamilyRole _xifexpression = null;
    int _indexOf = IterableExtensions.<String>toList(parentOrChildOptions).indexOf("Child");
    boolean _equals = (parentOrChildSelection == _indexOf);
    if (_equals) {
      _xifexpression = FamilyRole.Child;
    } else {
      _xifexpression = FamilyRole.Parent;
    }
    return _xifexpression;
  }

  public static FamilyRole askUserWhetherPersonIsParentOrChildDuringRenaming(final UserInteractor userInteractor, final String oldFullname, final String newFullname, final boolean wasChildBefore) {
    StringBuilder _append = new StringBuilder().append("You have renamed ").append(oldFullname).append(" to ").append(newFullname).append(", which might cause the corresponding member in the families model to change its position inside a family.").append(" Which position should this member, who was a ");
    String _xifexpression = null;
    if (wasChildBefore) {
      _xifexpression = "child";
    } else {
      _xifexpression = "parent";
    }
    final StringBuilder parentOrChildMessageBuilder = _append.append(_xifexpression).append(" before, have after the renaming?");
    Iterable<String> parentOrChildOptions = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("Parent", "Child"));
    final int parentOrChildSelection = (userInteractor.getSingleSelectionDialogBuilder().message(parentOrChildMessageBuilder.toString()).choices(parentOrChildOptions).title("Parent or Child?").windowModality(UserInteractionOptions.WindowModality.MODAL).startInteraction()).intValue();
    FamilyRole _xifexpression_1 = null;
    int _indexOf = IterableExtensions.<String>toList(parentOrChildOptions).indexOf("Child");
    boolean _equals = (parentOrChildSelection == _indexOf);
    if (_equals) {
      _xifexpression_1 = FamilyRole.Child;
    } else {
      _xifexpression_1 = FamilyRole.Parent;
    }
    return _xifexpression_1;
  }

  /**
   * Filters out families which already have a parent of the same sex as the given person.
   * @param person Person whose last name is used for the filter.
   * @return Lambda to filter out all families whose last name is different from the newPerson's.
   */
  public static Function1<? super Family, ? extends Boolean> sameLastname(final Person person) {
    final String newPersonLastname = PersonsToFamiliesHelper.getLastname(person);
    final Function1<Family, Boolean> _function = new Function1<Family, Boolean>() {
      public Boolean apply(final Family family) {
        return Boolean.valueOf(family.getLastName().equals(newPersonLastname));
      }
    };
    return _function;
  }

  /**
   * Filters out families which already have a parent of the same sex as the given person.
   * @param person Person whose sex determines whether families with father or with mother are filtered out.
   * @return Lambda to filter out all families which already have a parent with the same sex as the newPerson.
   */
  public static Function1<? super Family, ? extends Boolean> noParent(final Person person) {
    final Function1<Family, Boolean> _function = new Function1<Family, Boolean>() {
      public Boolean apply(final Family family) {
        boolean _xifexpression = false;
        if ((person instanceof Male)) {
          Member _father = family.getFather();
          _xifexpression = (_father == null);
        } else {
          Member _mother = family.getMother();
          _xifexpression = (_mother == null);
        }
        return Boolean.valueOf(_xifexpression);
      }
    };
    return _function;
  }

  /**
   * Sets up an user interaction to ask the user if he wants to insert the corresponding member to the {@code newPerson}
   * either into a new family or into one of the {@code selectableFamilies} and if so, in which.
   * @return chosen family, if existing family was selected; <code>null</code>, if users wants to create and insert into a new family
   */
  public static Family askUserWhichFamilyToInsertTheMemberIn(final UserInteractor userInteractor, final Person newPerson, final Iterable<Family> selectableFamilies) {
    StringBuilder whichFamilyMessageBuilder = new StringBuilder().append("Please choose whether you want to create a new family or insert ").append(newPerson.getFullName()).append(" into one of the existing families.");
    final Collection<String> whichFamilyOptions = new ArrayList<String>();
    whichFamilyOptions.add("insert in a new family");
    final Function1<Family, String> _function = new Function1<Family, String>() {
      public String apply(final Family it) {
        return PersonsToFamiliesHelper.stringifyFamily(it);
      }
    };
    Iterables.<String>addAll(whichFamilyOptions, IterableExtensions.<Family, String>map(selectableFamilies, _function));
    final Integer whichFamilyIndex = userInteractor.getSingleSelectionDialogBuilder().message(whichFamilyMessageBuilder.toString()).choices(whichFamilyOptions).title("New or Existing Family?").windowModality(UserInteractionOptions.WindowModality.MODAL).startInteraction();
    Family _xifexpression = null;
    if (((whichFamilyIndex).intValue() == 0)) {
      _xifexpression = null;
    } else {
      _xifexpression = ((Family[])Conversions.unwrapArray(selectableFamilies, Family.class))[((whichFamilyIndex).intValue() - 1)];
    }
    return _xifexpression;
  }

  /**
   * Checks if a persons fullname is <code>null</code>, empty or contains escape sequences.
   * @param person The person of which the fullname has to be valid.
   * @throws <code>IllegalStateException</code>, if the persons fullname in invalid.
   */
  public static void assertValidFullname(final Person person) {
    String _fullName = person.getFullName();
    boolean _tripleEquals = (_fullName == null);
    if (_tripleEquals) {
      throw new IllegalStateException(PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL);
    } else {
      boolean _isEmpty = person.getFullName().trim().isEmpty();
      if (_isEmpty) {
        throw new IllegalStateException(PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE);
      } else {
        if (((person.getFullName().contains("\n") || person.getFullName().contains("\t")) || person.getFullName().contains("\r"))) {
          throw new IllegalStateException(PersonsToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES);
        }
      }
    }
  }

  private PersonsToFamiliesHelper() {
    
  }
}
