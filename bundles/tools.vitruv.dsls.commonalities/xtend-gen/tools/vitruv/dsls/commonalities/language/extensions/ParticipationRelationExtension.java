package tools.vitruv.dsls.commonalities.language.extensions;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.commonalities.participation.Containment;
import tools.vitruv.dsls.commonalities.participation.ReferenceContainment;
import tools.vitruv.dsls.commonalities.runtime.operators.CommonalitiesOperatorConventions;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.ContainmentOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.ParticipationRelationOperator;

@Utility
@SuppressWarnings("all")
final class ParticipationRelationExtension {
  private static final String containmentOperatorTypeQualifiedName = CommonalitiesOperatorConventions.toOperatorTypeQualifiedName(
    ContainmentOperator.class.getPackageName(), 
    IterableExtensions.<ParticipationRelationOperator>head(Iterables.<ParticipationRelationOperator>filter(((Iterable<?>)Conversions.doWrapArray(ContainmentOperator.class.getAnnotations())), ParticipationRelationOperator.class)).name());

  public static boolean isContainment(final ParticipationRelation relation) {
    String _qualifiedName = relation.getOperator().getQualifiedName();
    return Objects.equal(_qualifiedName, ParticipationRelationExtension.containmentOperatorTypeQualifiedName);
  }

  public static Iterable<Containment> getContainments(final ParticipationRelation relation) {
    boolean _isContainment = ParticipationRelationExtension.isContainment(relation);
    boolean _not = (!_isContainment);
    if (_not) {
      return CollectionLiterals.<Containment>emptyList();
    }
    final ParticipationClass container = ParticipationPartExtension.getDeclaredContainerClass(relation);
    if ((container == null)) {
      return CollectionLiterals.<Containment>emptyList();
    }
    final Function1<ParticipationPart, Containment> _function = (ParticipationPart containedPart) -> {
      ParticipationClass _declaredContainerClass = ParticipationPartExtension.getDeclaredContainerClass(containedPart);
      return new ReferenceContainment(container, _declaredContainerClass, null);
    };
    return ListExtensions.<ParticipationPart, Containment>map(relation.getLeftParts(), _function);
  }

  public static String getParticipationRelationOperatorName(final JvmDeclaredType operatorType) {
    return CommonalitiesOperatorConventions.toOperatorLanguageName(operatorType.getSimpleName());
  }

  public static String getOperatorName(final ParticipationRelation relation) {
    return ParticipationRelationExtension.getParticipationRelationOperatorName(relation.getOperator());
  }

  private ParticipationRelationExtension() {
    
  }
}
