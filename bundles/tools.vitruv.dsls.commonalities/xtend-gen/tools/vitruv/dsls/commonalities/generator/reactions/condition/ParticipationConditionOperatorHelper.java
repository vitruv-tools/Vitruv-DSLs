package tools.vitruv.dsls.commonalities.generator.reactions.condition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XListLiteral;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.operator.OperandHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.operator.OperatorContext;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.language.ParticipationConditionOperand;
import tools.vitruv.dsls.commonalities.runtime.operators.participation.condition.IParticipationConditionOperator;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ParticipationConditionOperatorHelper extends ReactionsGenerationHelper {
  public static class ParticipationConditionOperatorContext implements OperatorContext {
    @Extension
    private final TypeProvider typeProvider;

    public ParticipationConditionOperatorContext(final TypeProvider typeProvider) {
      Preconditions.<TypeProvider>checkNotNull(typeProvider, "typeProvider is null");
      this.typeProvider = typeProvider;
    }

    @Override
    public TypeProvider getTypeProvider() {
      return this.typeProvider;
    }

    private UnsupportedOperationException unsupportedOperationException() {
      return new UnsupportedOperationException("Unsupported in participation condition context!");
    }

    @Override
    public boolean passParticipationAttributeValues() {
      return false;
    }

    @Override
    public boolean passCommonalityAttributeValues() {
      throw this.unsupportedOperationException();
    }

    @Override
    public XExpression getIntermediate() {
      throw this.unsupportedOperationException();
    }

    @Override
    public XExpression getParticipationObject(final ParticipationClass participationClass) {
      return this.typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(participationClass));
    }
  }

  private static final String ENFORCE_METHOD = "enforce";

  private static final String CHECK_METHOD = "check";

  @Inject
  @Extension
  private OperandHelper operandHelper;

  ParticipationConditionOperatorHelper() {
  }

  public XConstructorCall constructOperator(final ParticipationCondition participationCondition, final ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final ParticipationConditionOperand leftOperand = participationCondition.getLeftOperand();
    XConstructorCall _createXConstructorCall = XbaseFactory.eINSTANCE.createXConstructorCall();
    final Procedure1<XConstructorCall> _function = (XConstructorCall it) -> {
      final JvmDeclaredType operatorType = typeProvider.<JvmDeclaredType>imported(participationCondition.getOperator());
      it.setConstructor(JvmTypeProviderHelper.findConstructor(operatorType, Object.class, List.class));
      it.setExplicitConstructorCall(true);
      EList<XExpression> _arguments = it.getArguments();
      XExpression _operandExpression = this.operandHelper.getOperandExpression(leftOperand, operatorContext);
      _arguments.add(_operandExpression);
      EList<XExpression> _arguments_1 = it.getArguments();
      XListLiteral _createXListLiteral = XbaseFactory.eINSTANCE.createXListLiteral();
      final Procedure1<XListLiteral> _function_1 = (XListLiteral it_1) -> {
        EList<XExpression> _elements = it_1.getElements();
        final Function1<ParticipationConditionOperand, XExpression> _function_2 = (ParticipationConditionOperand it_2) -> {
          return this.operandHelper.getOperandExpression(it_2, operatorContext);
        };
        List<XExpression> _map = ListExtensions.<ParticipationConditionOperand, XExpression>map(participationCondition.getRightOperands(), _function_2);
        Iterables.<XExpression>addAll(_elements, _map);
      };
      XListLiteral _doubleArrow = ObjectExtensions.<XListLiteral>operator_doubleArrow(_createXListLiteral, _function_1);
      _arguments_1.add(_doubleArrow);
    };
    return ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function);
  }

  private XMemberFeatureCall callOperatorMethod(final ParticipationCondition participationCondition, final JvmOperation method, final ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext operatorContext) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(this.constructOperator(participationCondition, operatorContext));
      it.setFeature(method);
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  public XExpression enforce(final ParticipationCondition participationCondition, final ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IParticipationConditionOperator.class, ParticipationConditionOperatorHelper.ENFORCE_METHOD);
    return this.callOperatorMethod(participationCondition, method, operatorContext);
  }

  public XExpression check(final ParticipationCondition participationCondition, final ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IParticipationConditionOperator.class, ParticipationConditionOperatorHelper.CHECK_METHOD);
    return this.callOperatorMethod(participationCondition, method, operatorContext);
  }
}
