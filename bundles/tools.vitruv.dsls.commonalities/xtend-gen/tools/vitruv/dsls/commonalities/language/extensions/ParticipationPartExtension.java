package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Arrays;
import java.util.List;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;

@Utility
@SuppressWarnings("all")
final class ParticipationPartExtension {
  public static Participation getDeclaringParticipation(final ParticipationPart participationPart) {
    return CommonalitiesLanguageElementExtension.<Participation>getEContainer(participationPart, Participation.class);
  }

  public static Commonality getDeclaringCommonality(final ParticipationPart participationPart) {
    return ParticipationExtension.getDeclaringCommonality(ParticipationPartExtension.getDeclaringParticipation(participationPart));
  }

  protected static Iterable<ParticipationClass> _getAllParticipationClasses(final ParticipationClass pClass) {
    return List.<ParticipationClass>of(pClass);
  }

  protected static Iterable<ParticipationClass> _getAllParticipationClasses(final ParticipationRelation relation) {
    final Function1<ParticipationPart, Iterable<ParticipationClass>> _function = (ParticipationPart it) -> {
      return ParticipationPartExtension.getAllParticipationClasses(it);
    };
    Iterable<ParticipationClass> _flatMap = IterableExtensions.<ParticipationPart, ParticipationClass>flatMap(relation.getLeftParts(), _function);
    final Function1<ParticipationPart, Iterable<ParticipationClass>> _function_1 = (ParticipationPart it) -> {
      return ParticipationPartExtension.getAllParticipationClasses(it);
    };
    Iterable<ParticipationClass> _flatMap_1 = IterableExtensions.<ParticipationPart, ParticipationClass>flatMap(relation.getRightParts(), _function_1);
    return Iterables.<ParticipationClass>concat(_flatMap, _flatMap_1);
  }

  protected static Iterable<ParticipationRelation> _getAllParticipationRelations(final ParticipationClass pClass) {
    return CollectionLiterals.<ParticipationRelation>emptyList();
  }

  protected static Iterable<ParticipationRelation> _getAllParticipationRelations(final ParticipationRelation relation) {
    List<ParticipationRelation> _of = List.<ParticipationRelation>of(relation);
    final Function1<ParticipationPart, Iterable<ParticipationRelation>> _function = (ParticipationPart it) -> {
      return ParticipationPartExtension.getAllParticipationRelations(it);
    };
    Iterable<ParticipationRelation> _flatMap = IterableExtensions.<ParticipationPart, ParticipationRelation>flatMap(relation.getLeftParts(), _function);
    Iterable<ParticipationRelation> _plus = Iterables.<ParticipationRelation>concat(_of, _flatMap);
    final Function1<ParticipationPart, Iterable<ParticipationRelation>> _function_1 = (ParticipationPart it) -> {
      return ParticipationPartExtension.getAllParticipationRelations(it);
    };
    Iterable<ParticipationRelation> _flatMap_1 = IterableExtensions.<ParticipationPart, ParticipationRelation>flatMap(relation.getRightParts(), _function_1);
    return Iterables.<ParticipationRelation>concat(_plus, _flatMap_1);
  }

  protected static ParticipationClass _getDeclaredContainerClass(final ParticipationClass pClass) {
    return pClass;
  }

  protected static ParticipationClass _getDeclaredContainerClass(final ParticipationRelation relation) {
    ParticipationPart _head = IterableExtensions.<ParticipationPart>head(relation.getRightParts());
    ParticipationClass _declaredContainerClass = null;
    if (_head!=null) {
      _declaredContainerClass=ParticipationPartExtension.getDeclaredContainerClass(_head);
    }
    return _declaredContainerClass;
  }

  public static Iterable<ParticipationClass> getAllParticipationClasses(final ParticipationPart pClass) {
    if (pClass instanceof ParticipationClass) {
      return _getAllParticipationClasses((ParticipationClass)pClass);
    } else if (pClass instanceof ParticipationRelation) {
      return _getAllParticipationClasses((ParticipationRelation)pClass);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(pClass).toString());
    }
  }

  public static Iterable<ParticipationRelation> getAllParticipationRelations(final ParticipationPart pClass) {
    if (pClass instanceof ParticipationClass) {
      return _getAllParticipationRelations((ParticipationClass)pClass);
    } else if (pClass instanceof ParticipationRelation) {
      return _getAllParticipationRelations((ParticipationRelation)pClass);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(pClass).toString());
    }
  }

  public static ParticipationClass getDeclaredContainerClass(final ParticipationPart pClass) {
    if (pClass instanceof ParticipationClass) {
      return _getDeclaredContainerClass((ParticipationClass)pClass);
    } else if (pClass instanceof ParticipationRelation) {
      return _getDeclaredContainerClass((ParticipationRelation)pClass);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(pClass).toString());
    }
  }

  private ParticipationPartExtension() {
    
  }
}
