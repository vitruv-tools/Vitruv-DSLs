package tools.vitruv.dsls.commonalities.generator.reactions.relation;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationPart;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ParticipationRelationInitializationHelper extends ReactionsGenerationHelper {
  private static final String RELATION_ENFORCE_METHOD = "enforce";

  ParticipationRelationInitializationHelper() {
  }

  public Iterable<Function1<? super TypeProvider, ? extends XExpression>> getParticipationRelationsInitializers(final ParticipationContext participationContext, final ParticipationContext.ContextClass contextClass) {
    return this.getParticipationRelationsInitializers(participationContext.getParticipation(), participationContext, contextClass);
  }

  private Iterable<Function1<? super TypeProvider, ? extends XExpression>> getParticipationRelationsInitializers(final Participation participation, final ParticipationContext participationContext, final ParticipationContext.ContextClass contextClass) {
    final Function1<ParticipationPart, Function1<? super TypeProvider, ? extends XExpression>> _function = (ParticipationPart it) -> {
      return this.getParticipationRelationInitializer(it, participationContext, contextClass);
    };
    return IterableExtensions.<Function1<? super TypeProvider, ? extends XExpression>>filterNull(ListExtensions.<ParticipationPart, Function1<? super TypeProvider, ? extends XExpression>>map(participation.getParts(), _function));
  }

  private Function1<? super TypeProvider, ? extends XExpression> _getParticipationRelationInitializer(final ParticipationRelation relation, final ParticipationContext participationContext, final ParticipationContext.ContextClass contextClass) {
    boolean _isContainment = CommonalitiesLanguageModelExtensions.isContainment(relation);
    if (_isContainment) {
      return null;
    }
    if ((relation.getLeftParts().isEmpty() || relation.getRightParts().isEmpty())) {
      return null;
    }
    final ParticipationClass participationClass = contextClass.getParticipationClass();
    int _indexOf = relation.getRightParts().indexOf(participationClass);
    int _size = relation.getRightParts().size();
    int _minus = (_size - 1);
    boolean _notEquals = (_indexOf != _minus);
    if (_notEquals) {
      return null;
    }
    EList<ParticipationPart> _leftParts = relation.getLeftParts();
    EList<ParticipationPart> _rightParts = relation.getRightParts();
    final Iterable<ParticipationPart> relationClasses = Iterables.<ParticipationPart>concat(_leftParts, _rightParts);
    final Function1<ParticipationPart, Boolean> _function = (ParticipationPart relationClass) -> {
      final Function1<ParticipationContext.ContextClass, Boolean> _function_1 = (ParticipationContext.ContextClass it) -> {
        ParticipationClass _participationClass = it.getParticipationClass();
        return Boolean.valueOf(Objects.equal(_participationClass, relationClass));
      };
      boolean _exists = IterableExtensions.<ParticipationContext.ContextClass>exists(participationContext.getClasses(), _function_1);
      return Boolean.valueOf((!_exists));
    };
    boolean _exists = IterableExtensions.<ParticipationPart>exists(relationClasses, _function);
    if (_exists) {
      return null;
    }
    final Function1<TypeProvider, XExpression> _function_1 = (TypeProvider it) -> {
      return this.enforceRelation(it, relation);
    };
    return _function_1;
  }

  private Function1<? super TypeProvider, ? extends XExpression> _getParticipationRelationInitializer(final ParticipationClass pClass, final ParticipationContext participationContext, final ParticipationContext.ContextClass contextClass) {
    return null;
  }

  private XExpression enforceRelation(final TypeProvider typeProvider, final ParticipationRelation participationRelation) {
    final JvmDeclaredType operatorType = participationRelation.getOperator();
    final JvmOperation enforceMethod = JvmTypeProviderHelper.findMethod(operatorType, ParticipationRelationInitializationHelper.RELATION_ENFORCE_METHOD);
    return ParticipationRelationOperatorHelper.callRelationOperation(participationRelation, enforceMethod, typeProvider);
  }

  private Function1<? super TypeProvider, ? extends XExpression> getParticipationRelationInitializer(final ParticipationPart pClass, final ParticipationContext participationContext, final ParticipationContext.ContextClass contextClass) {
    if (pClass instanceof ParticipationClass) {
      return _getParticipationRelationInitializer((ParticipationClass)pClass, participationContext, contextClass);
    } else if (pClass instanceof ParticipationRelation) {
      return _getParticipationRelationInitializer((ParticipationRelation)pClass, participationContext, contextClass);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(pClass, participationContext, contextClass).toString());
    }
  }
}
