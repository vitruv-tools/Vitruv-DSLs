package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.List;
import java.util.Set;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.Concept;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.commonalities.language.elements.Domain;
import tools.vitruv.dsls.commonalities.language.elements.Metaclass;
import tools.vitruv.dsls.commonalities.participation.Containment;

@Utility
@SuppressWarnings("all")
final class ParticipationExtension {
  public static Commonality getDeclaringCommonality(final Participation participation) {
    return CommonalitiesLanguageElementExtension.<Commonality>getDirectEContainer(participation, Commonality.class);
  }

  public static Iterable<ParticipationClass> getAllClasses(final Participation participation) {
    final Function1<ParticipationPart, Iterable<ParticipationClass>> _function = (ParticipationPart it) -> {
      return ParticipationPartExtension.getAllParticipationClasses(it);
    };
    return IterableExtensions.<ParticipationPart, ParticipationClass>flatMap(participation.getParts(), _function);
  }

  public static Domain getDomain(final Participation participation) {
    final Function1<ParticipationClass, Boolean> _function = (ParticipationClass it) -> {
      Metaclass _superMetaclass = it.getSuperMetaclass();
      return Boolean.valueOf((_superMetaclass != null));
    };
    ParticipationClass _findFirst = IterableExtensions.<ParticipationClass>findFirst(ParticipationExtension.getAllClasses(participation), _function);
    Domain _domain = null;
    if (_findFirst!=null) {
      _domain=ParticipationClassExtension.getDomain(_findFirst);
    }
    return _domain;
  }

  public static boolean isCommonalityParticipation(final Participation participation) {
    Concept _participationConcept = ParticipationExtension.getParticipationConcept(participation);
    return (_participationConcept != null);
  }

  public static Concept getParticipationConcept(final Participation participation) {
    final Domain domain = ParticipationExtension.getDomain(participation);
    Concept _xifexpression = null;
    if ((domain instanceof Concept)) {
      _xifexpression = ((Concept)domain);
    } else {
      _xifexpression = null;
    }
    return _xifexpression;
  }

  public static Iterable<ParticipationRelation> getAllRelations(final Participation participation) {
    final Function1<ParticipationPart, Iterable<ParticipationRelation>> _function = (ParticipationPart it) -> {
      return ParticipationPartExtension.getAllParticipationRelations(it);
    };
    return IterableExtensions.<ParticipationPart, ParticipationRelation>flatMap(participation.getParts(), _function);
  }

  public static Iterable<ParticipationRelation> getAllContainmentRelations(final Participation participation) {
    final Function1<ParticipationRelation, Boolean> _function = (ParticipationRelation it) -> {
      return Boolean.valueOf(ParticipationRelationExtension.isContainment(it));
    };
    return IterableExtensions.<ParticipationRelation>filter(ParticipationExtension.getAllRelations(participation), _function);
  }

  public static Iterable<ParticipationRelation> getAllNonContainmentRelations(final Participation participation) {
    final Function1<ParticipationRelation, Boolean> _function = (ParticipationRelation it) -> {
      boolean _isContainment = ParticipationRelationExtension.isContainment(it);
      return Boolean.valueOf((!_isContainment));
    };
    return IterableExtensions.<ParticipationRelation>filter(ParticipationExtension.getAllRelations(participation), _function);
  }

  public static Iterable<ParticipationCondition> getAllContainmentConditions(final Participation participation) {
    final Function1<ParticipationCondition, Boolean> _function = (ParticipationCondition it) -> {
      return Boolean.valueOf(ParticipationConditionExtension.isContainment(it));
    };
    return IterableExtensions.<ParticipationCondition>filter(participation.getConditions(), _function);
  }

  public static Iterable<ParticipationCondition> getAllNonContainmentConditions(final Participation participation) {
    final Function1<ParticipationCondition, Boolean> _function = (ParticipationCondition it) -> {
      boolean _isContainment = ParticipationConditionExtension.isContainment(it);
      return Boolean.valueOf((!_isContainment));
    };
    return IterableExtensions.<ParticipationCondition>filter(participation.getConditions(), _function);
  }

  public static Iterable<Containment> getContainments(final Participation participation) {
    final Function1<ParticipationRelation, Iterable<Containment>> _function = (ParticipationRelation it) -> {
      return ParticipationRelationExtension.getContainments(it);
    };
    Iterable<Containment> _flatMap = IterableExtensions.<ParticipationRelation, Containment>flatMap(ParticipationExtension.getAllContainmentRelations(participation), _function);
    final Function1<ParticipationCondition, Containment> _function_1 = (ParticipationCondition it) -> {
      return ParticipationConditionExtension.getContainment(it);
    };
    Iterable<Containment> _filterNull = IterableExtensions.<Containment>filterNull(IterableExtensions.<ParticipationCondition, Containment>map(ParticipationExtension.getAllContainmentConditions(participation), _function_1));
    return Iterables.<Containment>concat(_flatMap, _filterNull);
  }

  public static Set<ParticipationClass> getRootContainerClasses(final Participation participation) {
    final Function1<ParticipationClass, ParticipationClass> _function = (ParticipationClass it) -> {
      return ParticipationClassExtension.getRootDeclaredContainerClass(it);
    };
    return IterableExtensions.<ParticipationClass>toSet(IterableExtensions.<ParticipationClass, ParticipationClass>map(ParticipationExtension.getAllClasses(participation), _function));
  }

  public static boolean hasResourceClass(final Participation participation) {
    ParticipationClass _resourceClass = ParticipationExtension.getResourceClass(participation);
    return (_resourceClass != null);
  }

  public static ParticipationClass getResourceClass(final Participation participation) {
    final Function1<ParticipationClass, Boolean> _function = (ParticipationClass it) -> {
      return Boolean.valueOf(ParticipationClassExtension.isForResource(it));
    };
    return IterableExtensions.<ParticipationClass>findFirst(ParticipationExtension.getAllClasses(participation), _function);
  }

  public static Iterable<Containment> getResourceContainments(final Participation participation) {
    final Function1<Containment, Boolean> _function = (Containment it) -> {
      return Boolean.valueOf(ParticipationClassExtension.isForResource(it.getContainer()));
    };
    return IterableExtensions.<Containment>filter(ParticipationExtension.getContainments(participation), _function);
  }

  public static boolean hasSingletonClass(final Participation participation) {
    ParticipationClass _singletonClass = ParticipationExtension.getSingletonClass(participation);
    return (_singletonClass != null);
  }

  public static ParticipationClass getSingletonClass(final Participation participation) {
    final Function1<ParticipationClass, Boolean> _function = (ParticipationClass it) -> {
      return Boolean.valueOf(it.isSingleton());
    };
    return IterableExtensions.<ParticipationClass>findFirst(ParticipationExtension.getAllClasses(participation), _function);
  }

  /**
   * A class marked as singleton and its containers also act as root of the
   * participation. This returns these classes.
   */
  public static Iterable<ParticipationClass> getSingletonRootClasses(final Participation participation) {
    final ParticipationClass singletonClass = ParticipationExtension.getSingletonClass(participation);
    Iterable<ParticipationClass> _xifexpression = null;
    if ((singletonClass != null)) {
      List<ParticipationClass> _of = List.<ParticipationClass>of(singletonClass);
      Iterable<ParticipationClass> _transitiveContainerClasses = ParticipationClassExtension.getTransitiveContainerClasses(singletonClass);
      _xifexpression = Iterables.<ParticipationClass>concat(_of, _transitiveContainerClasses);
    } else {
      _xifexpression = CollectionLiterals.<ParticipationClass>emptyList();
    }
    return _xifexpression;
  }

  private ParticipationExtension() {
    
  }
}
