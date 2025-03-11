package tools.vitruv.dsls.commonalities.generator.reactions.participation;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.condition.ParticipationConditionInitializationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel.IntermediateModelHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.relation.ParticipationRelationInitializationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.resource.ResourceBridgeHelper;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ParticipationObjectInitializationHelper extends ReactionsGenerationHelper {
  @Inject
  @Extension
  private ResourceBridgeHelper resourceBridgeHelper;

  @Inject
  @Extension
  private ParticipationRelationInitializationHelper participationRelationInitializationHelper;

  @Inject
  @Extension
  private ParticipationConditionInitializationHelper participationConditionInitializationHelper;

  ParticipationObjectInitializationHelper() {
  }

  public XBlockExpression toBlockExpression(final Iterable<Function1<? super TypeProvider, ? extends XExpression>> expressionBuilders, final TypeProvider typeProvider) {
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      EList<XExpression> _expressions = it.getExpressions();
      final Function1<Function1<? super TypeProvider, ? extends XExpression>, XExpression> _function_1 = (Function1<? super TypeProvider, ? extends XExpression> it_1) -> {
        return it_1.apply(typeProvider);
      };
      Iterable<XExpression> _map = IterableExtensions.<Function1<? super TypeProvider, ? extends XExpression>, XExpression>map(expressionBuilders, _function_1);
      Iterables.<XExpression>addAll(_expressions, _map);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }

  /**
   * Early initializations that only affect the specific participation object
   * itself.
   */
  public Iterable<Function1<? super TypeProvider, ? extends XExpression>> getInitializers(final ParticipationClass participationClass) {
    Function1<? super TypeProvider, ? extends XExpression> _resourceInitializer = this.getResourceInitializer(participationClass);
    Function1<? super TypeProvider, ? extends XExpression> _commonalityParticipationClassInitializer = this.getCommonalityParticipationClassInitializer(participationClass);
    return IterableExtensions.<Function1<? super TypeProvider, ? extends XExpression>>filterNull(Collections.<Function1<? super TypeProvider, ? extends XExpression>>unmodifiableList(CollectionLiterals.<Function1<? super TypeProvider, ? extends XExpression>>newArrayList(_resourceInitializer, _commonalityParticipationClassInitializer)));
  }

  private Function1<? super TypeProvider, ? extends XExpression> getResourceInitializer(final ParticipationClass participationClass) {
    Function1<TypeProvider, XExpression> _xifexpression = null;
    boolean _isForResource = CommonalitiesLanguageModelExtensions.isForResource(participationClass);
    boolean _not = (!_isForResource);
    if (_not) {
      _xifexpression = null;
    } else {
      final Function1<TypeProvider, XExpression> _function = (TypeProvider it) -> {
        XBlockExpression _xblockexpression = null;
        {
          final XFeatureCall resourceBridge = it.variable(ReactionsGeneratorConventions.correspondingVariableName(participationClass));
          _xblockexpression = this.resourceBridgeHelper.initNewResourceBridge(participationClass, resourceBridge, it);
        }
        return _xblockexpression;
      };
      _xifexpression = _function;
    }
    return _xifexpression;
  }

  private Function1<? super TypeProvider, ? extends XExpression> getCommonalityParticipationClassInitializer(final ParticipationClass participationClass) {
    Function1<TypeProvider, XExpression> _xifexpression = null;
    boolean _isCommonalityParticipation = CommonalitiesLanguageModelExtensions.isCommonalityParticipation(CommonalitiesLanguageModelExtensions.getParticipation(participationClass));
    boolean _not = (!_isCommonalityParticipation);
    if (_not) {
      _xifexpression = null;
    } else {
      final Function1<TypeProvider, XExpression> _function = (TypeProvider typeProvider) -> {
        return IntermediateModelHelper.claimIntermediateId(typeProvider, typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(participationClass)));
      };
      _xifexpression = _function;
    }
    return _xifexpression;
  }

  /**
   * Initializations that need to happen after all participation objects have
   * been created.
   * <p>
   * For example, this includes initializations done by operators since they
   * may want to reference other participation objects.
   */
  public List<Function1<? super TypeProvider, ? extends XExpression>> getPostInitializers(final ParticipationContext participationContext, final ParticipationContext.ContextClass contextClass) {
    Iterable<Function1<? super TypeProvider, ? extends XExpression>> _participationRelationsInitializers = this.participationRelationInitializationHelper.getParticipationRelationsInitializers(participationContext, contextClass);
    Iterable<Function1<? super TypeProvider, ? extends XExpression>> _participationConditionsInitializers = this.participationConditionInitializationHelper.getParticipationConditionsInitializers(participationContext, contextClass);
    return IterableExtensions.<Function1<? super TypeProvider, ? extends XExpression>>toList(Iterables.<Function1<? super TypeProvider, ? extends XExpression>>concat(_participationRelationsInitializers, _participationConditionsInitializers));
  }
}
