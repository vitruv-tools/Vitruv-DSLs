package tools.vitruv.dsls.commonalities.generator.reactions.attribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.operator.OperandHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.operator.OperatorContext;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.language.OperatorAttributeMapping;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute.IAttributeMappingOperator;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class AttributeMappingOperatorHelper extends ReactionsGenerationHelper {
  public static class AttributeMappingOperatorContext implements OperatorContext {
    @Extension
    private final TypeProvider typeProvider;

    private final Supplier<XExpression> intermediate;

    private final Function<ParticipationClass, XExpression> participationClassToObject;

    public AttributeMappingOperatorContext(final TypeProvider typeProvider, final Supplier<XExpression> intermediate, final Function<ParticipationClass, XExpression> participationClassToObject) {
      Preconditions.<TypeProvider>checkNotNull(typeProvider, "typeProvider is null");
      Preconditions.<Supplier<XExpression>>checkNotNull(intermediate, "intermediate is null");
      Preconditions.<Function<ParticipationClass, XExpression>>checkNotNull(participationClassToObject, "participationClassToObject is null");
      this.typeProvider = typeProvider;
      this.intermediate = intermediate;
      this.participationClassToObject = participationClassToObject;
    }

    @Override
    public TypeProvider getTypeProvider() {
      return this.typeProvider;
    }

    private UnsupportedOperationException unsupportedOperationException() {
      return new UnsupportedOperationException("Unsupported in attribute mapping context!");
    }

    @Override
    public boolean passParticipationAttributeValues() {
      throw this.unsupportedOperationException();
    }

    @Override
    public boolean passCommonalityAttributeValues() {
      return true;
    }

    @Override
    public XExpression getIntermediate() {
      return this.intermediate.get();
    }

    @Override
    public XExpression getParticipationObject(final ParticipationClass participationClass) {
      return this.participationClassToObject.apply(participationClass);
    }
  }

  @Inject
  @Extension
  private OperandHelper operandHelper;

  AttributeMappingOperatorHelper() {
  }

  public XConstructorCall constructOperator(final OperatorAttributeMapping mapping, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    XConstructorCall _createXConstructorCall = XbaseFactory.eINSTANCE.createXConstructorCall();
    final Procedure1<XConstructorCall> _function = (XConstructorCall it) -> {
      final JvmDeclaredType operatorType = typeProvider.<JvmDeclaredType>imported(mapping.getOperator());
      it.setConstructor(JvmTypeProviderHelper.findConstructor(operatorType));
      it.setExplicitConstructorCall(true);
      EList<XExpression> _arguments = it.getArguments();
      XFeatureCall _executionState = typeProvider.executionState();
      _arguments.add(_executionState);
      EList<XExpression> _arguments_1 = it.getArguments();
      Iterable<XExpression> _operandExpressions = this.operandHelper.getOperandExpressions(CommonalitiesLanguageModelExtensions.getCommonOperands(mapping), operatorContext);
      Iterables.<XExpression>addAll(_arguments_1, _operandExpressions);
    };
    return ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function);
  }

  private XMemberFeatureCall callOperatorMethod(final OperatorAttributeMapping mapping, final JvmOperation method, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(this.constructOperator(mapping, operatorContext));
      it.setFeature(method);
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  public XMemberFeatureCall applyTowardsCommonality(final OperatorAttributeMapping mapping, final XExpression participationAttributeValue, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IAttributeMappingOperator.class, "applyTowardsCommonality");
    XMemberFeatureCall _callOperatorMethod = this.callOperatorMethod(mapping, method, operatorContext);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      _memberCallArguments.add(participationAttributeValue);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_callOperatorMethod, _function);
  }

  public XMemberFeatureCall applyTowardsParticipation(final OperatorAttributeMapping mapping, final XExpression commonalityAttributeValue, final AttributeMappingOperatorHelper.AttributeMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IAttributeMappingOperator.class, "applyTowardsParticipation");
    XMemberFeatureCall _callOperatorMethod = this.callOperatorMethod(mapping, method, operatorContext);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      _memberCallArguments.add(commonalityAttributeValue);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_callOperatorMethod, _function);
  }
}
