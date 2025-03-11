package tools.vitruv.dsls.demo.insurancefamilies.insurance2families;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import edu.kit.ipd.sdq.metamodels.families.Family;
import edu.kit.ipd.sdq.metamodels.families.Member;
import edu.kit.ipd.sdq.metamodels.insurance.Gender;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceClient;
import edu.kit.ipd.sdq.metamodels.insurance.InsuranceDatabase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.change.interaction.UserInteractionOptions;
import tools.vitruv.change.interaction.UserInteractor;

@Utility
@SuppressWarnings("all")
public final class InsuranceToFamiliesHelper {
  public static final String EXCEPTION_MESSAGE_FIRSTNAME_NULL = "A insurance clients\'s name is not allowed to be null.";

  public static final String EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE = "A insurance clients\'s name has to contain at least one non-whitespace character.";

  public static final String EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES = "A insurance clients\'s name cannot contain any whitespace escape sequences.";

  public static InsuranceDatabase getInsuranceDatabase(final InsuranceClient insuranceClient) {
    InsuranceDatabase _xifexpression = null;
    EObject _eContainer = insuranceClient.eContainer();
    if ((_eContainer instanceof InsuranceDatabase)) {
      EObject _eContainer_1 = insuranceClient.eContainer();
      _xifexpression = ((InsuranceDatabase) _eContainer_1);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  public static String getLastName(final InsuranceClient insuranceClient) {
    return IterableExtensions.<String>last(((Iterable<String>)Conversions.doWrapArray(insuranceClient.getName().split(" "))));
  }

  public static String getFirstName(final InsuranceClient insuranceClient) {
    return IterableExtensions.<String>head(((Iterable<String>)Conversions.doWrapArray(insuranceClient.getName().split(" "))));
  }

  public static Function1<? super Family, ? extends Boolean> sameLastName(final InsuranceClient insuranceClient) {
    final String newPersonLastname = InsuranceToFamiliesHelper.getLastName(insuranceClient);
    final Function1<Family, Boolean> _function = new Function1<Family, Boolean>() {
      public Boolean apply(final Family family) {
        return Boolean.valueOf(family.getLastName().equals(newPersonLastname));
      }
    };
    return _function;
  }

  public static Function1<? super Family, ? extends Boolean> noParent(final InsuranceClient insuranceClient) {
    final Function1<Family, Boolean> _function = new Function1<Family, Boolean>() {
      public Boolean apply(final Family family) {
        boolean _xifexpression = false;
        Gender _gender = insuranceClient.getGender();
        boolean _tripleEquals = (_gender == Gender.MALE);
        if (_tripleEquals) {
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

  public static void informUserAboutReplacementOfClient(final UserInteractor userInteractor, final InsuranceClient insuranceClient, final Family oldFamily) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Insurance Client ");
    String _name = insuranceClient.getName();
    _builder.append(_name);
    _builder.append(" has been replaced by another insurance client in his family (");
    String _lastName = oldFamily.getLastName();
    _builder.append(_lastName);
    _builder.append("). Please decide in which family and role ");
    String _name_1 = insuranceClient.getName();
    _builder.append(_name_1);
    _builder.append(" should be.");
    String message = _builder.toString();
    userInteractor.getNotificationDialogBuilder().message(message.toString()).title("Insurance Client has been replaced in his original family").startInteraction();
  }

  public static PositionPreference askUserWhetherClientIsParentOrChild(final UserInteractor userInteractor, final InsuranceClient insuranceClient) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("You have inserted ");
    String _name = insuranceClient.getName();
    _builder.append(_name);
    _builder.append(" into the insurance database which results into the creation of a corresponding member into the family register. Is this member supposed to be a parent or a child in the family register?");
    final String parentOrChildMessage = _builder.toString();
    Iterable<String> parentOrChildOptions = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("Parent", "Child"));
    final int parentOrChildSelection = (userInteractor.getSingleSelectionDialogBuilder().message(parentOrChildMessage).choices(parentOrChildOptions).title("Parent or Child?").windowModality(UserInteractionOptions.WindowModality.MODAL).startInteraction()).intValue();
    PositionPreference _xifexpression = null;
    int _indexOf = IterableExtensions.<String>toList(parentOrChildOptions).indexOf("Child");
    boolean _equals = (parentOrChildSelection == _indexOf);
    if (_equals) {
      _xifexpression = PositionPreference.Child;
    } else {
      _xifexpression = PositionPreference.Parent;
    }
    return _xifexpression;
  }

  public static Family askUserWhichFamilyToInsertTheMemberIn(final UserInteractor userInteractor, final InsuranceClient newClient, final Iterable<Family> selectableFamilies) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Please choose whether you want to create a new family or insert  ");
    String _name = newClient.getName();
    _builder.append(_name);
    _builder.append(" into one of the existing families.");
    String whichFamilyMessage = _builder.toString();
    final Collection<String> whichFamilyOptions = new ArrayList<String>();
    whichFamilyOptions.add("insert in a new family");
    final Function1<Family, String> _function = new Function1<Family, String>() {
      public String apply(final Family it) {
        return InsuranceToFamiliesHelper.stringifyFamily(it);
      }
    };
    Iterables.<String>addAll(whichFamilyOptions, IterableExtensions.<Family, String>map(selectableFamilies, _function));
    final Integer whichFamilyIndex = userInteractor.getSingleSelectionDialogBuilder().message(whichFamilyMessage).choices(whichFamilyOptions).title("New or Existing Family?").windowModality(UserInteractionOptions.WindowModality.MODAL).startInteraction();
    Family _xifexpression = null;
    if (((whichFamilyIndex).intValue() == 0)) {
      _xifexpression = null;
    } else {
      _xifexpression = ((Family[])Conversions.unwrapArray(selectableFamilies, Family.class))[((whichFamilyIndex).intValue() - 1)];
    }
    return _xifexpression;
  }

  public static PositionPreference askUserWhetherClientIsParentOrChildDuringRenaming(final UserInteractor userInteractor, final String oldFullname, final String newFullname, final boolean wasChildBefore) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("You have renamed ");
    _builder.append(oldFullname);
    _builder.append(" to ");
    _builder.append(newFullname);
    _builder.append(", which might cause the corresponding member in the families model to change its position inside a family. Which position should this member, who was a\t");
    {
      if (wasChildBefore) {
        _builder.append("child");
      } else {
        _builder.append("parent");
      }
    }
    _builder.append(" before, have after the renaming?");
    final String parentOrChildMessage = _builder.toString();
    Iterable<String> parentOrChildOptions = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("Parent", "Child"));
    final int parentOrChildSelection = (userInteractor.getSingleSelectionDialogBuilder().message(parentOrChildMessage).choices(parentOrChildOptions).title("Parent or Child?").windowModality(UserInteractionOptions.WindowModality.MODAL).startInteraction()).intValue();
    PositionPreference _xifexpression = null;
    int _indexOf = IterableExtensions.<String>toList(parentOrChildOptions).indexOf("Child");
    boolean _equals = (parentOrChildSelection == _indexOf);
    if (_equals) {
      _xifexpression = PositionPreference.Child;
    } else {
      _xifexpression = PositionPreference.Parent;
    }
    return _xifexpression;
  }

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
   * Checks if a insurance clients name is <code>null</code>, empty or contains escape sequences.
   * @param person The insurance client of which the name has to be valid.
   * @throws <code>IllegalStateException</code>, if the insurance clients name in invalid.
   */
  public static void assertValidName(final InsuranceClient insuranceClient) {
    String _name = insuranceClient.getName();
    boolean _tripleEquals = (_name == null);
    if (_tripleEquals) {
      throw new IllegalStateException(InsuranceToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_NULL);
    } else {
      boolean _isEmpty = insuranceClient.getName().trim().isEmpty();
      if (_isEmpty) {
        throw new IllegalStateException(InsuranceToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_WHITESPACE);
      } else {
        if (((insuranceClient.getName().contains("\n") || insuranceClient.getName().contains("\t")) || insuranceClient.getName().contains("\r"))) {
          throw new IllegalStateException(InsuranceToFamiliesHelper.EXCEPTION_MESSAGE_FIRSTNAME_ESCAPES);
        }
      }
    }
  }

  private InsuranceToFamiliesHelper() {
    
  }
}
