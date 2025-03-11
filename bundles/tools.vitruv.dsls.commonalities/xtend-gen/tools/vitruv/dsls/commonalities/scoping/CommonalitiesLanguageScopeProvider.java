package tools.vitruv.dsls.commonalities.scoping;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.SimpleScope;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.commonalities.language.AttributeMappingOperand;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeReference;
import tools.vitruv.dsls.commonalities.language.LanguagePackage;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationClassOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.names.IEObjectDescriptionProvider;
import tools.vitruv.dsls.commonalities.names.QualifiedNameHelper;

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
@SuppressWarnings("all")
public class CommonalitiesLanguageScopeProvider extends AbstractCommonalitiesLanguageScopeProvider {
  @Inject
  private ParticipationClassesScope.Factory createParticipationClassesScope;

  @Inject
  private Provider<ParticipationAttributesScope> participationAttributesScope;

  @Inject
  private CommonalityAttributesScope.Factory createCommonalityAttributesScope;

  @Inject
  private ParticipationRelationOperatorScopeProvider relationOperatorScopeProvider;

  @Inject
  private ParticipationConditionOperatorScopeProvider conditionOperatorScopeProvider;

  @Inject
  private AttributeMappingOperatorScopeProvider attributeMappingOperatorScopeProvider;

  @Inject
  private ReferenceMappingOperatorScopeProvider referenceMappingOperatorScopeProvider;

  @Inject
  private IGlobalScopeProvider globalScopeProvider;

  @Inject
  @Extension
  private IQualifiedNameProvider qualifiedNameProvider;

  @Inject
  @Extension
  private IEObjectDescriptionProvider descriptionProvider;

  @Override
  public IScope getScope(final EObject context, final EReference reference) {
    boolean _matched = false;
    if (Objects.equal(reference, LanguagePackage.Literals.PARTICIPATION_CLASS_OPERAND__PARTICIPATION_CLASS)) {
      _matched=true;
      if ((context instanceof ParticipationClassOperand)) {
        boolean _isInParticipationConditionContext = CommonalitiesLanguageModelExtensions.isInParticipationConditionContext(((ParticipationConditionOperand)context));
        if (_isInParticipationConditionContext) {
          return this.createUnqualifiedParticipationClassScope(CommonalitiesLanguageModelExtensions.getParticipation(((ParticipationConditionOperand)context)));
        } else {
          boolean _isInAttributeMappingContext = CommonalitiesLanguageModelExtensions.isInAttributeMappingContext(((AttributeMappingOperand)context));
          if (_isInAttributeMappingContext) {
            return this.createParticipationClassesScope.forCommonality(CommonalitiesLanguageModelExtensions.<Commonality>getEContainer(context, Commonality.class));
          } else {
            throw new IllegalStateException("Unexpected ParticipationClassOperand context");
          }
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.OPERATOR_REFERENCE_MAPPING__PARTICIPATION_CLASS)) {
        _matched=true;
        if ((context instanceof OperatorReferenceMapping)) {
          return this.createParticipationClassesScope.forCommonality(CommonalitiesLanguageModelExtensions.<Commonality>getEContainer(context, Commonality.class));
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.PARTICIPATION_ATTRIBUTE__PARTICIPATION_CLASS)) {
        _matched=true;
        if ((context instanceof ParticipationAttribute)) {
          final ParticipationCondition participationCondition = CommonalitiesLanguageModelExtensions.<ParticipationCondition>getOptionalEContainer(context, ParticipationCondition.class);
          if ((participationCondition != null)) {
            return this.createUnqualifiedParticipationClassScope(CommonalitiesLanguageModelExtensions.getParticipation(participationCondition));
          }
          final OperatorReferenceMapping referenceMapping = CommonalitiesLanguageModelExtensions.<OperatorReferenceMapping>getOptionalEContainer(context, OperatorReferenceMapping.class);
          if ((referenceMapping != null)) {
            boolean _hasEContainer = CommonalitiesLanguageModelExtensions.hasEContainer(context, ReferencedParticipationAttributeOperand.class);
            if (_hasEContainer) {
              return this.createUnqualifiedParticipationClassScope(CommonalitiesLanguageModelExtensions.getReferencedParticipation(referenceMapping));
            } else {
              boolean _hasEContainer_1 = CommonalitiesLanguageModelExtensions.hasEContainer(context, ParticipationAttributeOperand.class);
              if (_hasEContainer_1) {
                return this.createUnqualifiedParticipationClassScope(CommonalitiesLanguageModelExtensions.getParticipation(referenceMapping));
              }
            }
          }
          return this.createParticipationClassesScope.forCommonality(CommonalitiesLanguageModelExtensions.<Commonality>getEContainer(context, Commonality.class));
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.PARTICIPATION_ATTRIBUTE__ATTRIBUTE)) {
        _matched=true;
        if ((context instanceof ParticipationAttribute)) {
          return this.createUnqualifiedParticipationAttributeScope(((ParticipationAttribute)context).getParticipationClass());
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.PARTICIPATION_CLASS__SUPER_METACLASS)) {
        _matched=true;
        if ((context instanceof ParticipationClass)) {
          final Participation participation = CommonalitiesLanguageModelExtensions.getParticipation(((ParticipationClass)context));
          final QualifiedName domainName = QualifiedNameHelper.getQualifiedDomainName(participation.getDomainName());
          final IScope globalScope = this.globalScopeProvider.getScope(((ParticipationClass)context).eResource(), reference, null);
          return new PrefixedScope(globalScope, domainName);
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.COMMONALITY_ATTRIBUTE_REFERENCE__COMMONALITY)) {
        _matched=true;
        if ((context instanceof CommonalityAttributeReference)) {
          final Commonality commonality = CommonalitiesLanguageModelExtensions.<Commonality>getEContainer(context, Commonality.class);
          final String conceptName = CommonalitiesLanguageModelExtensions.getConcept(commonality).getName();
          final SimpleScope commonalityScope = this.createSingleCommonalityScope(commonality);
          return this.createUnqualifiedCommonalityScope(conceptName, commonalityScope);
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.COMMONALITY_ATTRIBUTE_REFERENCE__ATTRIBUTE)) {
        _matched=true;
        if ((context instanceof CommonalityAttributeReference)) {
          return this.createUnqualifiedCommonalityAttributeScope(((CommonalityAttributeReference)context).getCommonality());
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.OPERATOR_ATTRIBUTE_MAPPING__OPERATOR)) {
        _matched=true;
        return this.attributeMappingOperatorScopeProvider.getScope(context, reference);
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.OPERATOR_REFERENCE_MAPPING__OPERATOR)) {
        _matched=true;
        return this.referenceMappingOperatorScopeProvider.getScope(context, reference);
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.PARTICIPATION_RELATION__OPERATOR)) {
        _matched=true;
        return this.relationOperatorScopeProvider.getScope(context, reference);
      }
    }
    if (!_matched) {
      if (Objects.equal(reference, LanguagePackage.Literals.PARTICIPATION_CONDITION__OPERATOR)) {
        _matched=true;
        return this.conditionOperatorScopeProvider.getScope(context, reference);
      }
    }
    return this.globalScopeProvider.getScope(context.eResource(), reference, null);
  }

  private IScope createUnqualifiedParticipationClassScope(final Participation participation) {
    if ((participation == null)) {
      return IScope.NULLSCOPE;
    }
    final ParticipationClassesScope participationClassScope = this.createParticipationClassesScope.forCommonality(CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation));
    final QualifiedName parentQualifiedName = this.qualifiedNameProvider.getFullyQualifiedName(participation);
    IScope _xifexpression = null;
    if ((parentQualifiedName != null)) {
      _xifexpression = new PrefixedScope(participationClassScope, parentQualifiedName);
    } else {
      _xifexpression = participationClassScope;
    }
    return _xifexpression;
  }

  private IScope createUnqualifiedParticipationAttributeScope(final ParticipationClass participationClass) {
    boolean _eIsProxy = participationClass.eIsProxy();
    if (_eIsProxy) {
      return IScope.NULLSCOPE;
    }
    final ParticipationAttributesScope participationAttributeScope = this.participationAttributesScope.get().forParticipationClass(participationClass);
    final QualifiedName parentQualifiedName = this.qualifiedNameProvider.getFullyQualifiedName(participationClass);
    return new PrefixedScope(participationAttributeScope, parentQualifiedName);
  }

  private SimpleScope createSingleCommonalityScope(final Commonality commonality) {
    List<IEObjectDescription> _of = List.<IEObjectDescription>of(this.descriptionProvider.describe(commonality));
    return new SimpleScope(IScope.NULLSCOPE, _of);
  }

  private PrefixedScope createUnqualifiedCommonalityScope(final String conceptName, final IScope qualifiedCommonalityScope) {
    QualifiedName _qualifiedDomainName = QualifiedNameHelper.getQualifiedDomainName(conceptName);
    return new PrefixedScope(qualifiedCommonalityScope, _qualifiedDomainName);
  }

  private IScope createUnqualifiedCommonalityAttributeScope(final Commonality commonality) {
    boolean _eIsProxy = commonality.eIsProxy();
    if (_eIsProxy) {
      return IScope.NULLSCOPE;
    }
    final CommonalityAttributesScope commonalityAttributeScope = this.createCommonalityAttributesScope.forCommonality(commonality);
    final QualifiedName parentQualifiedName = this.qualifiedNameProvider.getFullyQualifiedName(commonality);
    return new PrefixedScope(commonalityAttributeScope, parentQualifiedName);
  }
}
