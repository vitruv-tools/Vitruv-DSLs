package tools.vitruv.dsls.commonalities.generator.reactions.operator;

import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.CommonalityAttribute;
import tools.vitruv.dsls.commonalities.language.CommonalityAttributeOperand;
import tools.vitruv.dsls.commonalities.language.LiteralOperand;
import tools.vitruv.dsls.commonalities.language.Operand;
import tools.vitruv.dsls.commonalities.language.ParticipationAttribute;
import tools.vitruv.dsls.commonalities.language.ParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationClassOperand;
import tools.vitruv.dsls.commonalities.language.ReferencedParticipationAttributeOperand;
import tools.vitruv.dsls.commonalities.language.elements.Attribute;
import tools.vitruv.dsls.commonalities.runtime.operators.AttributeOperand;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class OperandHelper extends ReactionsGenerationHelper {
  OperandHelper() {
  }

  public Iterable<XExpression> getOperandExpressions(final Iterable<? extends Operand> operands, final OperatorContext context) {
    final Function1<Operand, XExpression> _function = (Operand it) -> {
      return this.getOperandExpression(it, context);
    };
    return IterableExtensions.<XExpression>filterNull(IterableExtensions.map(operands, _function));
  }

  protected XExpression _getOperandExpression(final LiteralOperand operand, final OperatorContext context) {
    return XbaseHelper.<XExpression>copy(operand.getExpression());
  }

  protected XExpression _getOperandExpression(final ParticipationClassOperand operand, final OperatorContext context) {
    return context.getParticipationObject(operand.getParticipationClass());
  }

  protected XExpression _getOperandExpression(final ParticipationAttributeOperand operand, final OperatorContext context) {
    final ParticipationAttribute attribute = operand.getParticipationAttribute();
    final ParticipationClass participationClass = attribute.getParticipationClass();
    final XExpression participationObject = context.getParticipationObject(participationClass);
    final boolean passValue = context.passParticipationAttributeValues();
    return this.getAttributeOperandOrValue(context, participationObject, attribute, passValue);
  }

  protected XExpression _getOperandExpression(final CommonalityAttributeOperand operand, final OperatorContext context) {
    final CommonalityAttribute attribute = operand.getAttributeReference().getAttribute();
    final XExpression intermediate = context.getIntermediate();
    final boolean passValue = context.passCommonalityAttributeValues();
    return this.getAttributeOperandOrValue(context, intermediate, attribute, passValue);
  }

  protected XExpression _getOperandExpression(final ReferencedParticipationAttributeOperand operand, final OperatorContext context) {
    return null;
  }

  protected XExpression _getOperandExpression(final Operand operand, final OperatorContext context) {
    String _name = operand.getClass().getName();
    String _plus = ("Unhandled operand type: " + _name);
    throw new IllegalStateException(_plus);
  }

  private XExpression getAttributeOperandOrValue(final OperatorContext context, final XExpression object, final Attribute attribute, final boolean passValue) {
    if (passValue) {
      return this.getAttributeValue(context.getTypeProvider(), object, attribute);
    } else {
      return this.getAttributeOperand(context.getTypeProvider(), object, attribute);
    }
  }

  private XConstructorCall getAttributeOperand(@Extension final TypeProvider typeProvider, final XExpression object, final Attribute attribute) {
    XConstructorCall _createXConstructorCall = XbaseFactory.eINSTANCE.createXConstructorCall();
    final Procedure1<XConstructorCall> _function = (XConstructorCall it) -> {
      final JvmDeclaredType operandType = JvmTypeProviderHelper.findDeclaredType(typeProvider, AttributeOperand.class);
      it.setConstructor(JvmTypeProviderHelper.findConstructor(operandType, EObject.class, EStructuralFeature.class));
      it.setExplicitConstructorCall(true);
      EList<XExpression> _arguments = it.getArguments();
      List<XExpression> _expressions = XbaseHelper.expressions(object, 
        EmfAccessExpressions.getEFeature(typeProvider, XbaseHelper.<XExpression>copy(object), this._generationContext.getCorrespondingEFeature(attribute)));
      Iterables.<XExpression>addAll(_arguments, _expressions);
    };
    return ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function);
  }

  private XExpression getAttributeValue(@Extension final TypeProvider typeProvider, final XExpression object, final Attribute attribute) {
    return EmfAccessExpressions.getFeatureValue(typeProvider, object, this._generationContext.getCorrespondingEFeature(attribute));
  }

  public XExpression getOperandExpression(final Operand operand, final OperatorContext context) {
    if (operand instanceof CommonalityAttributeOperand) {
      return _getOperandExpression((CommonalityAttributeOperand)operand, context);
    } else if (operand instanceof LiteralOperand) {
      return _getOperandExpression((LiteralOperand)operand, context);
    } else if (operand instanceof ParticipationAttributeOperand) {
      return _getOperandExpression((ParticipationAttributeOperand)operand, context);
    } else if (operand instanceof ParticipationClassOperand) {
      return _getOperandExpression((ParticipationClassOperand)operand, context);
    } else if (operand instanceof ReferencedParticipationAttributeOperand) {
      return _getOperandExpression((ReferencedParticipationAttributeOperand)operand, context);
    } else if (operand != null) {
      return _getOperandExpression(operand, context);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(operand, context).toString());
    }
  }
}
