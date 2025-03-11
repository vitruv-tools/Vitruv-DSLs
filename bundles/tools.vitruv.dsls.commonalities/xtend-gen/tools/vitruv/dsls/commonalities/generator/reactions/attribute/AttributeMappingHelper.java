package tools.vitruv.dsls.commonalities.generator.reactions.attribute;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeMapping;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.SimpleAttributeMapping;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.ParticipationContextHelper;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class AttributeMappingHelper extends ReactionsGenerationHelper {
  @Inject
  @Extension
  private AttributeMappingOperatorHelper attributeMappingOperatorHelper;

  @Inject
  @Extension
  private ParticipationObjectsHelper participationObjectsHelper;

  AttributeMappingHelper() {
  }

  /**
   * Read: From participation to commonality
   */
  public Iterable<CommonalityAttributeMapping> getRelevantReadMappings(final Participation participation) {
    final Function1<CommonalityAttribute, Iterable<CommonalityAttributeMapping>> _function = (CommonalityAttribute it) -> {
      return this.getRelevantReadMappings(it, participation);
    };
    return IterableExtensions.<CommonalityAttribute, CommonalityAttributeMapping>flatMap(CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation).getAttributes(), _function);
  }

  public Iterable<CommonalityAttributeMapping> getRelevantReadMappings(final CommonalityAttribute attribute, final Participation participation) {
    final Function1<CommonalityAttributeMapping, Boolean> _function = (CommonalityAttributeMapping it) -> {
      return Boolean.valueOf((it.isRead() && Objects.equal(CommonalitiesLanguageModelExtensions.getParticipation(it), participation)));
    };
    return IterableExtensions.<CommonalityAttributeMapping>filter(attribute.getMappings(), _function);
  }

  public Function<ParticipationClass, XExpression> participationClassToNullableObject(final Map<ParticipationClass, XVariableDeclaration> participationObjectVars) {
    final Function<ParticipationClass, XExpression> _function = (ParticipationClass participationClass) -> {
      return XbaseHelper.featureCall(participationObjectVars.get(participationClass));
    };
    return _function;
  }

  protected XExpression _applyReadMapping(final SimpleAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    final ParticipationClass participationClass = mapping.getAttribute().getParticipationClass();
    final Function1<ParticipationClass, XExpression> _function = (ParticipationClass it) -> {
      return operatorContext.getParticipationObject(it);
    };
    final Function1<ParticipationClass, XExpression> participationClassToObject = _function;
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    final Supplier<XExpression> _function_1 = () -> {
      return this.readAttribute(mapping, operatorContext);
    };
    return this.participationObjectsHelper.ifParticipationObjectsAvailable(typeProvider, Collections.<ParticipationClass>unmodifiableList(CollectionLiterals.<ParticipationClass>newArrayList(participationClass)), new Function<ParticipationClass, XExpression>() {
        public XExpression apply(ParticipationClass arg0) {
          return participationClassToObject.apply(arg0);
        }
    }, _function_1);
  }

  protected XExpression _applyReadMapping(final OperatorAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    final Collection<ParticipationClass> participationClasses = CommonalitiesLanguageModelExtensions.getInvolvedParticipationClasses(mapping);
    final Function1<ParticipationClass, XExpression> _function = (ParticipationClass it) -> {
      return operatorContext.getParticipationObject(it);
    };
    final Function1<ParticipationClass, XExpression> participationClassToObject = _function;
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    final Supplier<XExpression> _function_1 = () -> {
      return this.readAttribute(mapping, operatorContext);
    };
    return this.participationObjectsHelper.ifParticipationObjectsAvailable(typeProvider, participationClasses, new Function<ParticipationClass, XExpression>() {
        public XExpression apply(ParticipationClass arg0) {
          return participationClassToObject.apply(arg0);
        }
    }, _function_1);
  }

  private XExpression _readAttribute(final SimpleAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    final ParticipationAttribute participationAttribute = mapping.getAttribute();
    final XExpression participationAttributeValue = this.getParticipationAttributeValue(participationAttribute, operatorContext);
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    final XExpression intermediate = operatorContext.getIntermediate();
    return EmfAccessExpressions.replaceFeatureValue(typeProvider, intermediate, this._generationContext.getCommonalityEFeature(mapping), participationAttributeValue);
  }

  private XExpression _readAttribute(final OperatorAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      final ParticipationAttribute participationAttribute = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping);
      final XExpression participationAttributeValue = this.getParticipationAttributeValue(participationAttribute, operatorContext);
      XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
      final Procedure1<XVariableDeclaration> _function_1 = (XVariableDeclaration it_1) -> {
        it_1.setName("newFeatureValue");
        it_1.setRight(this.attributeMappingOperatorHelper.applyTowardsCommonality(mapping, participationAttributeValue, operatorContext));
      };
      final XVariableDeclaration newValueVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_1);
      EList<XExpression> _expressions = it.getExpressions();
      _expressions.add(newValueVar);
      final TypeProvider typeProvider = operatorContext.getTypeProvider();
      final XExpression intermediate = operatorContext.getIntermediate();
      EList<XExpression> _expressions_1 = it.getExpressions();
      XExpression _replaceFeatureValue = EmfAccessExpressions.replaceFeatureValue(typeProvider, intermediate, this._generationContext.getCommonalityEFeature(mapping), 
        XbaseHelper.featureCall(newValueVar));
      _expressions_1.add(_replaceFeatureValue);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }

  private XExpression getParticipationAttributeValue(final ParticipationAttribute participationAttribute, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    if ((participationAttribute == null)) {
      return XbaseHelper.nullLiteral();
    }
    final ParticipationClass participationClass = participationAttribute.getParticipationClass();
    final XExpression participationObject = operatorContext.getParticipationObject(participationClass);
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    return EmfAccessExpressions.retrieveFeatureValue(typeProvider, participationObject, this._generationContext.getCorrespondingEFeature(participationAttribute));
  }

  /**
   * Write: From commonality to participation
   */
  public Iterable<CommonalityAttributeMapping> getRelevantWriteMappings(final Participation participation) {
    final Function1<CommonalityAttribute, Iterable<CommonalityAttributeMapping>> _function = (CommonalityAttribute it) -> {
      return this.getRelevantWriteMappings(it, participation);
    };
    return IterableExtensions.<CommonalityAttribute, CommonalityAttributeMapping>flatMap(CommonalitiesLanguageModelExtensions.getDeclaringCommonality(participation).getAttributes(), _function);
  }

  public Iterable<CommonalityAttributeMapping> getRelevantWriteMappings(final CommonalityAttribute attribute, final Participation participation) {
    final Function1<CommonalityAttributeMapping, Boolean> _function = (CommonalityAttributeMapping it) -> {
      return Boolean.valueOf((it.isWrite() && Objects.equal(CommonalitiesLanguageModelExtensions.getParticipation(it), participation)));
    };
    final Function1<CommonalityAttributeMapping, Boolean> _function_1 = (CommonalityAttributeMapping it) -> {
      ParticipationAttribute _participationAttribute = CommonalitiesLanguageModelExtensions.getParticipationAttribute(it);
      return Boolean.valueOf((_participationAttribute != null));
    };
    return IterableExtensions.<CommonalityAttributeMapping>filter(IterableExtensions.<CommonalityAttributeMapping>filter(attribute.getMappings(), _function), _function_1);
  }

  public Function<ParticipationClass, XExpression> participationClassToOptionalObject(@Extension final TypeProvider typeProvider) {
    final Function<ParticipationClass, XExpression> _function = (ParticipationClass participationClass) -> {
      final XFeatureCall participationObjectVar = typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(participationClass));
      boolean _isRootClass = ParticipationContextHelper.isRootClass(participationClass);
      if (_isRootClass) {
        return XbaseHelper.optionalGetOrNull(participationObjectVar, typeProvider);
      } else {
        return participationObjectVar;
      }
    };
    return _function;
  }

  protected XExpression _applyWriteMapping(final SimpleAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    final ParticipationClass participationClass = mapping.getAttribute().getParticipationClass();
    final Function1<ParticipationClass, XExpression> _function = (ParticipationClass it) -> {
      return operatorContext.getParticipationObject(it);
    };
    final Function1<ParticipationClass, XExpression> participationClassToObject = _function;
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    final Supplier<XExpression> _function_1 = () -> {
      return this.writeAttribute(mapping, operatorContext);
    };
    return this.participationObjectsHelper.ifParticipationObjectsAvailable(typeProvider, Collections.<ParticipationClass>unmodifiableList(CollectionLiterals.<ParticipationClass>newArrayList(participationClass)), new Function<ParticipationClass, XExpression>() {
        public XExpression apply(ParticipationClass arg0) {
          return participationClassToObject.apply(arg0);
        }
    }, _function_1);
  }

  protected XExpression _applyWriteMapping(final OperatorAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    ParticipationAttribute _participationAttribute = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping);
    boolean _tripleNotEquals = (_participationAttribute != null);
    XtendAssertHelper.assertTrue(_tripleNotEquals);
    final Collection<ParticipationClass> participationClasses = CommonalitiesLanguageModelExtensions.getInvolvedParticipationClasses(mapping);
    final Function1<ParticipationClass, XExpression> _function = (ParticipationClass it) -> {
      return operatorContext.getParticipationObject(it);
    };
    final Function1<ParticipationClass, XExpression> participationClassToObject = _function;
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    final Supplier<XExpression> _function_1 = () -> {
      return this.writeAttribute(mapping, operatorContext);
    };
    return this.participationObjectsHelper.ifParticipationObjectsAvailable(typeProvider, participationClasses, new Function<ParticipationClass, XExpression>() {
        public XExpression apply(ParticipationClass arg0) {
          return participationClassToObject.apply(arg0);
        }
    }, _function_1);
  }

  private XExpression _writeAttribute(final SimpleAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    final XExpression intermediate = operatorContext.getIntermediate();
    final XExpression commonalityAttributeValue = this.getCommonalityAttributeValue(mapping, typeProvider, intermediate);
    return this.setParticipationAttributeValue(mapping, operatorContext, commonalityAttributeValue);
  }

  private XExpression _writeAttribute(final OperatorAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      final XExpression intermediate = operatorContext.getIntermediate();
      final XExpression commonalityAttributeValue = this.getCommonalityAttributeValue(mapping, typeProvider, intermediate);
      XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
      final Procedure1<XVariableDeclaration> _function_1 = (XVariableDeclaration it_1) -> {
        it_1.setName("newFeatureValue");
        it_1.setRight(this.attributeMappingOperatorHelper.applyTowardsParticipation(mapping, commonalityAttributeValue, operatorContext));
      };
      final XVariableDeclaration newValueVar = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_1);
      EList<XExpression> _expressions = it.getExpressions();
      _expressions.add(newValueVar);
      EList<XExpression> _expressions_1 = it.getExpressions();
      XExpression _setParticipationAttributeValue = this.setParticipationAttributeValue(mapping, operatorContext, XbaseHelper.featureCall(newValueVar));
      _expressions_1.add(_setParticipationAttributeValue);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }

  private XExpression getCommonalityAttributeValue(final CommonalityAttributeMapping mapping, @Extension final TypeProvider typeProvider, final XExpression intermediate) {
    return EmfAccessExpressions.retrieveFeatureValue(typeProvider, intermediate, this._generationContext.getCommonalityEFeature(mapping));
  }

  private XExpression setParticipationAttributeValue(final CommonalityAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext, final XExpression attributeValue) {
    ParticipationAttribute _participationAttribute = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping);
    boolean _tripleNotEquals = (_participationAttribute != null);
    XtendAssertHelper.assertTrue(_tripleNotEquals);
    EStructuralFeature _participationEFeature = this._generationContext.getParticipationEFeature(mapping);
    boolean _tripleNotEquals_1 = (_participationEFeature != null);
    XtendAssertHelper.assertTrue(_tripleNotEquals_1);
    final ParticipationClass participationClass = CommonalitiesLanguageModelExtensions.getParticipationAttribute(mapping).getParticipationClass();
    final XExpression participationObject = operatorContext.getParticipationObject(participationClass);
    final TypeProvider typeProvider = operatorContext.getTypeProvider();
    return EmfAccessExpressions.replaceFeatureValue(typeProvider, participationObject, this._generationContext.getParticipationEFeature(mapping), attributeValue);
  }

  public XExpression applyReadMapping(final CommonalityAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _applyReadMapping((OperatorAttributeMapping)mapping, operatorContext);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _applyReadMapping((SimpleAttributeMapping)mapping, operatorContext);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping, operatorContext).toString());
    }
  }

  private XExpression readAttribute(final CommonalityAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _readAttribute((OperatorAttributeMapping)mapping, operatorContext);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _readAttribute((SimpleAttributeMapping)mapping, operatorContext);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping, operatorContext).toString());
    }
  }

  public XExpression applyWriteMapping(final CommonalityAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _applyWriteMapping((OperatorAttributeMapping)mapping, operatorContext);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _applyWriteMapping((SimpleAttributeMapping)mapping, operatorContext);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping, operatorContext).toString());
    }
  }

  private XExpression writeAttribute(final CommonalityAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    if (mapping instanceof OperatorAttributeMapping) {
      return _writeAttribute((OperatorAttributeMapping)mapping, operatorContext);
    } else if (mapping instanceof SimpleAttributeMapping) {
      return _writeAttribute((SimpleAttributeMapping)mapping, operatorContext);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(mapping, operatorContext).toString());
    }
  }
}
