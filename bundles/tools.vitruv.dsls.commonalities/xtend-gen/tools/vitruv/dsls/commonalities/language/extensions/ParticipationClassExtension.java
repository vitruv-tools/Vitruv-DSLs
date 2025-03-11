package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;
import tools.vitruv.dsls.commonalities.language.elements.ResourceMetaclass;

@Utility
@SuppressWarnings("all")
final class ParticipationClassExtension {
  public static ParticipationRelation getOptionalParticipationRelation(final ParticipationClass participationClass) {
    return CommonalitiesLanguageElementExtension.<ParticipationRelation>getOptionalDirectEContainer(participationClass, ParticipationRelation.class);
  }

  public static Domain getDomain(final ParticipationClass participationClass) {
    Metaclass _superMetaclass = participationClass.getSuperMetaclass();
    Domain _domain = null;
    if (_superMetaclass!=null) {
      _domain=_superMetaclass.getDomain();
    }
    return _domain;
  }

  public static Participation getParticipation(final ParticipationClass participationClass) {
    Participation _xblockexpression = null;
    {
      boolean _eIsProxy = participationClass.eIsProxy();
      if (_eIsProxy) {
        return null;
      }
      _xblockexpression = CommonalitiesLanguageElementExtension.<Participation>getEContainer(participationClass, Participation.class);
    }
    return _xblockexpression;
  }

  public static Commonality getParticipatingCommonality(final ParticipationClass participationClass) {
    final Metaclass metaclass = participationClass.getSuperMetaclass();
    if ((metaclass instanceof Commonality)) {
      return ((Commonality)metaclass);
    }
    return null;
  }

  /**
   * Returns the participation class that contains the given participation
   * class according to the specified containment relationships.
   * <p>
   * Returns <code>null</code> if no container class is found.
   */
  public static ParticipationClass getDeclaredContainerClass(final ParticipationClass contained) {
    ParticipationClass _elvis = null;
    final Function1<ParticipationRelation, Boolean> _function = (ParticipationRelation it) -> {
      return Boolean.valueOf(it.getLeftParts().contains(contained));
    };
    ParticipationRelation _findFirst = IterableExtensions.<ParticipationRelation>findFirst(ParticipationExtension.getAllContainmentRelations(ParticipationClassExtension.getParticipation(contained)), _function);
    ParticipationClass _declaredContainerClass = null;
    if (_findFirst!=null) {
      _declaredContainerClass=ParticipationPartExtension.getDeclaredContainerClass(_findFirst);
    }
    if (_declaredContainerClass != null) {
      _elvis = _declaredContainerClass;
    } else {
      final Function1<ParticipationCondition, Boolean> _function_1 = (ParticipationCondition it) -> {
        ParticipationClass _participationClass = OperandExtension.getParticipationClass(it.getLeftOperand());
        return Boolean.valueOf(Objects.equal(_participationClass, contained));
      };
      ParticipationCondition _findFirst_1 = IterableExtensions.<ParticipationCondition>findFirst(ParticipationExtension.getAllContainmentConditions(ParticipationClassExtension.getParticipation(contained)), _function_1);
      EList<ParticipationConditionOperand> _rightOperands = null;
      if (_findFirst_1!=null) {
        _rightOperands=_findFirst_1.getRightOperands();
      }
      ParticipationConditionOperand _head = null;
      if (_rightOperands!=null) {
        _head=IterableExtensions.<ParticipationConditionOperand>head(_rightOperands);
      }
      ParticipationClass _participationClass = null;
      if (_head!=null) {
        _participationClass=OperandExtension.getParticipationClass(_head);
      }
      _elvis = _participationClass;
    }
    return _elvis;
  }

  /**
   * Gets the root participation class that (transitively) contains the given
   * participation class.
   * <p>
   * Returns the given participation class itself if it has no container
   * class.
   */
  public static ParticipationClass getRootDeclaredContainerClass(final ParticipationClass participationClass) {
    ParticipationClass current = participationClass;
    ParticipationClass container = ParticipationClassExtension.getDeclaredContainerClass(current);
    while ((container != null)) {
      {
        current = container;
        container = ParticipationClassExtension.getDeclaredContainerClass(current);
      }
    }
    return current;
  }

  /**
   * Gets all participation classes along the chain of container classes of the given participation class.
   * <p>
   * This includes the direct and transitive container classes.
   * <p>
   * Empty if the given participation class has no container class.
   */
  public static Iterable<ParticipationClass> getTransitiveContainerClasses(final ParticipationClass participationClass) {
    final ParticipationClass directContainer = ParticipationClassExtension.getDeclaredContainerClass(participationClass);
    Iterable<ParticipationClass> _xifexpression = null;
    if ((directContainer != null)) {
      List<ParticipationClass> _of = List.<ParticipationClass>of(directContainer);
      Iterable<ParticipationClass> _transitiveContainerClasses = ParticipationClassExtension.getTransitiveContainerClasses(directContainer);
      _xifexpression = Iterables.<ParticipationClass>concat(_of, _transitiveContainerClasses);
    } else {
      _xifexpression = CollectionLiterals.<ParticipationClass>emptyList();
    }
    return _xifexpression;
  }

  /**
   * Gets the participation classes that are (directly) contained by the given participation class.
   * <p>
   * Empty if there are no contained classes.
   */
  public static Iterable<ParticipationClass> getContainedClasses(final ParticipationClass container) {
    final Function1<ParticipationRelation, Boolean> _function = (ParticipationRelation it) -> {
      ParticipationClass _declaredContainerClass = ParticipationPartExtension.getDeclaredContainerClass(it);
      return Boolean.valueOf(Objects.equal(_declaredContainerClass, container));
    };
    final Function1<ParticipationRelation, Iterable<ParticipationClass>> _function_1 = (ParticipationRelation it) -> {
      return Iterables.<ParticipationClass>filter(it.getLeftParts(), ParticipationClass.class);
    };
    Iterable<ParticipationClass> _flatMap = IterableExtensions.<ParticipationRelation, ParticipationClass>flatMap(IterableExtensions.<ParticipationRelation>filter(ParticipationExtension.getAllContainmentRelations(ParticipationClassExtension.getParticipation(container)), _function), _function_1);
    final Function1<ParticipationCondition, Boolean> _function_2 = (ParticipationCondition it) -> {
      final Function1<ParticipationConditionOperand, ParticipationClass> _function_3 = (ParticipationConditionOperand it_1) -> {
        return OperandExtension.getParticipationClass(it_1);
      };
      return Boolean.valueOf(ListExtensions.<ParticipationConditionOperand, ParticipationClass>map(it.getRightOperands(), _function_3).contains(container));
    };
    final Function1<ParticipationCondition, ParticipationConditionOperand> _function_3 = (ParticipationCondition it) -> {
      return it.getLeftOperand();
    };
    final Function1<ParticipationConditionOperand, ParticipationClass> _function_4 = (ParticipationConditionOperand it) -> {
      return OperandExtension.getParticipationClass(it);
    };
    Iterable<ParticipationClass> _filterNull = IterableExtensions.<ParticipationClass>filterNull(IterableExtensions.<ParticipationConditionOperand, ParticipationClass>map(IterableExtensions.<ParticipationCondition, ParticipationConditionOperand>map(IterableExtensions.<ParticipationCondition>filter(ParticipationExtension.getAllContainmentConditions(ParticipationClassExtension.getParticipation(container)), _function_2), _function_3), _function_4));
    return Iterables.<ParticipationClass>concat(_flatMap, _filterNull);
  }

  /**
   * Starting at the given class, this finds the leaf participation classes
   * that are (transitively) contained by the given class and don't contain
   * any other participation classes themselves.
   * <p>
   * This returns the given participation class itself if it contains no
   * other participation classes.
   */
  public static Iterable<ParticipationClass> getLeafClasses(final ParticipationClass participationClass) {
    final Iterable<ParticipationClass> containedClasses = ParticipationClassExtension.getContainedClasses(participationClass);
    Iterable<ParticipationClass> _xifexpression = null;
    boolean _isEmpty = IterableExtensions.isEmpty(containedClasses);
    if (_isEmpty) {
      _xifexpression = List.<ParticipationClass>of(participationClass);
    } else {
      final Function1<ParticipationClass, Iterable<ParticipationClass>> _function = (ParticipationClass it) -> {
        return ParticipationClassExtension.getLeafClasses(it);
      };
      _xifexpression = IterableExtensions.<ParticipationClass, ParticipationClass>flatMap(containedClasses, _function);
    }
    return _xifexpression;
  }

  /**
   * Gets all participation classes that are directly and transitively contained by the given participation class.
   * <p>
   * Empty if the given participation class contains no other classes.
   */
  public static Iterable<ParticipationClass> getTransitiveContainedClasses(final ParticipationClass participationClass) {
    final Iterable<ParticipationClass> directContained = ParticipationClassExtension.getContainedClasses(participationClass);
    final Function1<ParticipationClass, Iterable<ParticipationClass>> _function = (ParticipationClass it) -> {
      return ParticipationClassExtension.getTransitiveContainedClasses(it);
    };
    Iterable<ParticipationClass> _flatMap = IterableExtensions.<ParticipationClass, ParticipationClass>flatMap(directContained, _function);
    return Iterables.<ParticipationClass>concat(directContained, _flatMap);
  }

  public static boolean isForResource(final ParticipationClass participationClass) {
    Metaclass _superMetaclass = participationClass.getSuperMetaclass();
    return (_superMetaclass instanceof ResourceMetaclass);
  }

  public static boolean isInSingletonRoot(final ParticipationClass participationClass) {
    return IterableExtensions.contains(ParticipationExtension.getSingletonRootClasses(ParticipationClassExtension.getParticipation(participationClass)), participationClass);
  }

  private ParticipationClassExtension() {
    
  }
}
