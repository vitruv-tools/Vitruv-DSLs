package tools.vitruv.dsls.commonalities.generator.reactions.util;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XBooleanLiteral;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XInstanceOfExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XNullLiteral;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XUnaryOperation;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.BooleanExtensions;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@Utility
@SuppressWarnings("all")
public final class XbaseHelper {
  public static XExpression join(final XExpression first, final XExpression second) {
    XExpression _xblockexpression = null;
    {
      if ((first == null)) {
        return second;
      }
      if ((second == null)) {
        return first;
      }
      _xblockexpression = XbaseHelper.doJoin(first, second);
    }
    return _xblockexpression;
  }

  private static XExpression _doJoin(final XExpression firstExpression, final XBlockExpression secondBlock) {
    XBlockExpression _xblockexpression = null;
    {
      EList<XExpression> _expressions = secondBlock.getExpressions();
      final ArrayList<XExpression> secondExpressions = new ArrayList<XExpression>(_expressions);
      secondBlock.getExpressions().clear();
      EList<XExpression> _expressions_1 = secondBlock.getExpressions();
      Iterable<XExpression> _plus = Iterables.<XExpression>concat(Collections.<XExpression>unmodifiableList(CollectionLiterals.<XExpression>newArrayList(firstExpression)), secondExpressions);
      Iterables.<XExpression>addAll(_expressions_1, _plus);
      _xblockexpression = secondBlock;
    }
    return _xblockexpression;
  }

  private static XExpression _doJoin(final XBlockExpression firstBlock, final XBlockExpression secondBlock) {
    XBlockExpression _xblockexpression = null;
    {
      EList<XExpression> _expressions = firstBlock.getExpressions();
      EList<XExpression> _expressions_1 = secondBlock.getExpressions();
      Iterables.<XExpression>addAll(_expressions, _expressions_1);
      _xblockexpression = firstBlock;
    }
    return _xblockexpression;
  }

  private static XExpression _doJoin(final XBlockExpression firstBlock, final XExpression secondExpression) {
    XBlockExpression _xblockexpression = null;
    {
      EList<XExpression> _expressions = firstBlock.getExpressions();
      _expressions.add(secondExpression);
      _xblockexpression = firstBlock;
    }
    return _xblockexpression;
  }

  private static XExpression _doJoin(final XExpression firstExpression, final XExpression secondExpression) {
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      EList<XExpression> _expressions = it.getExpressions();
      Iterables.<XExpression>addAll(_expressions, Collections.<XExpression>unmodifiableList(CollectionLiterals.<XExpression>newArrayList(firstExpression, secondExpression)));
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }

  public static List<XExpression> expressions(final XExpression... expressions) {
    return Arrays.<XExpression>asList(expressions);
  }

  public static XExpression stringLiteral(final String string) {
    XStringLiteral _xifexpression = null;
    if ((string == null)) {
      return XbaseHelper.nullLiteral();
    } else {
      XStringLiteral _createXStringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
      final Procedure1<XStringLiteral> _function = (XStringLiteral it) -> {
        it.setValue(string);
      };
      _xifexpression = ObjectExtensions.<XStringLiteral>operator_doubleArrow(_createXStringLiteral, _function);
    }
    return _xifexpression;
  }

  public static XBooleanLiteral booleanLiteral(final boolean value) {
    XBooleanLiteral _createXBooleanLiteral = XbaseFactory.eINSTANCE.createXBooleanLiteral();
    final Procedure1<XBooleanLiteral> _function = (XBooleanLiteral it) -> {
      it.setIsTrue(value);
    };
    return ObjectExtensions.<XBooleanLiteral>operator_doubleArrow(_createXBooleanLiteral, _function);
  }

  public static XMemberFeatureCall memberFeatureCall(final XExpression target) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(target);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  public static XMemberFeatureCall memberFeatureCall(final JvmIdentifiableElement targetElement) {
    return XbaseHelper.memberFeatureCall(XbaseHelper.featureCall(targetElement));
  }

  public static XMemberFeatureCall memberFeatureCall(final XExpression target, final JvmIdentifiableElement featureElement) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(target);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(featureElement);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall memberFeatureCall(final JvmIdentifiableElement targetElement, final JvmIdentifiableElement featureElement) {
    return XbaseHelper.memberFeatureCall(XbaseHelper.featureCall(targetElement), featureElement);
  }

  public static XMemberFeatureCall set(final XMemberFeatureCall target, final XMemberFeatureCall source) {
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(source.getMemberCallTarget());
      it.setFeature(source.getFeature());
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(target, _function);
  }

  public static XFeatureCall featureCall(final JvmIdentifiableElement featureElement) {
    XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
      it.setFeature(featureElement);
    };
    return ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function);
  }

  public static <T extends XExpression> T copy(final T expression) {
    return EcoreUtil.<T>copy(expression);
  }

  public static XConstructorCall noArgsConstructorCall(final JvmDeclaredType type) {
    XConstructorCall _createXConstructorCall = XbaseFactory.eINSTANCE.createXConstructorCall();
    final Procedure1<XConstructorCall> _function = (XConstructorCall it) -> {
      it.setConstructor(JvmTypeProviderHelper.findNoArgsConstructor(type));
      it.setExplicitConstructorCall(true);
    };
    return ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function);
  }

  public static XNullLiteral nullLiteral() {
    return XbaseFactory.eINSTANCE.createXNullLiteral();
  }

  public static XUnaryOperation negated(final XExpression operand, final IJvmTypeProvider typeProvider) {
    XUnaryOperation _createXUnaryOperation = XbaseFactory.eINSTANCE.createXUnaryOperation();
    final Procedure1<XUnaryOperation> _function = (XUnaryOperation it) -> {
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, BooleanExtensions.class, "operator_not"));
      it.setOperand(operand);
    };
    return ObjectExtensions.<XUnaryOperation>operator_doubleArrow(_createXUnaryOperation, _function);
  }

  public static XBinaryOperation or(final XExpression leftOperand, final XExpression rightOperand, final IJvmTypeProvider typeProvider) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(leftOperand);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, BooleanExtensions.class, "operator_or"));
      it.setRightOperand(rightOperand);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation and(final XExpression leftOperand, final XExpression rightOperand, final IJvmTypeProvider typeProvider) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(leftOperand);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, BooleanExtensions.class, "operator_and"));
      it.setRightOperand(rightOperand);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation equals(final XExpression leftOperand, final XExpression rightOperand, final IJvmTypeProvider typeProvider) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(leftOperand);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, ObjectExtensions.class, "operator_equals"));
      it.setRightOperand(rightOperand);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation notEquals(final XExpression leftOperand, final XExpression rightOperand, final IJvmTypeProvider typeProvider) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(leftOperand);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, ObjectExtensions.class, "operator_notEquals"));
      it.setRightOperand(rightOperand);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation identityEquals(final XExpression leftOperand, final XExpression rightOperand, final IJvmTypeProvider typeProvider) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(leftOperand);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, ObjectExtensions.class, "operator_tripleEquals"));
      it.setRightOperand(rightOperand);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation notIdentityEquals(final XExpression leftOperand, final XExpression rightOperand, final IJvmTypeProvider typeProvider) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(leftOperand);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, ObjectExtensions.class, "operator_tripleNotEquals"));
      it.setRightOperand(rightOperand);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation equalsNull(final XExpression leftOperand, final IJvmTypeProvider typeProvider) {
    return XbaseHelper.identityEquals(leftOperand, XbaseHelper.nullLiteral(), typeProvider);
  }

  public static XBinaryOperation notEqualsNull(final XExpression leftOperand, final IJvmTypeProvider typeProvider) {
    return XbaseHelper.notIdentityEquals(leftOperand, XbaseHelper.nullLiteral(), typeProvider);
  }

  public static XInstanceOfExpression isInstanceOf(final XExpression leftOperand, final JvmTypeReference type) {
    XInstanceOfExpression _createXInstanceOfExpression = XbaseFactory.eINSTANCE.createXInstanceOfExpression();
    final Procedure1<XInstanceOfExpression> _function = (XInstanceOfExpression it) -> {
      it.setExpression(leftOperand);
      it.setType(type);
    };
    return ObjectExtensions.<XInstanceOfExpression>operator_doubleArrow(_createXInstanceOfExpression, _function);
  }

  public static XMemberFeatureCall optionalIsPresent(final XExpression optional, final IJvmTypeProvider typeProvider) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(optional);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.findDeclaredType(typeProvider, Optional.class), "isPresent"));
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall optionalGetOrNull(final XExpression optional, final IJvmTypeProvider typeProvider) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(optional);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.findDeclaredType(typeProvider, Optional.class), "orElse"));
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XNullLiteral _nullLiteral = XbaseHelper.nullLiteral();
      _memberCallArguments.add(_nullLiteral);
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall optionalGet(final XExpression optional, final IJvmTypeProvider typeProvider) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(optional);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.findDeclaredType(typeProvider, Optional.class), "get"));
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  private static XExpression doJoin(final XExpression firstBlock, final XExpression secondBlock) {
    if (firstBlock instanceof XBlockExpression
         && secondBlock instanceof XBlockExpression) {
      return _doJoin((XBlockExpression)firstBlock, (XBlockExpression)secondBlock);
    } else if (firstBlock instanceof XBlockExpression
         && secondBlock != null) {
      return _doJoin((XBlockExpression)firstBlock, secondBlock);
    } else if (firstBlock != null
         && secondBlock instanceof XBlockExpression) {
      return _doJoin(firstBlock, (XBlockExpression)secondBlock);
    } else if (firstBlock != null
         && secondBlock != null) {
      return _doJoin(firstBlock, secondBlock);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(firstBlock, secondBlock).toString());
    }
  }

  private XbaseHelper() {
    
  }
}
