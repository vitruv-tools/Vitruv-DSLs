package tools.vitruv.dsls.commonalities.generator.reactions.reference;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XTypeLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.operator.OperandHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.operator.OperatorContext;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.OperatorReferenceMapping;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.AttributeReferenceHelper;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ReferenceMappingOperatorHelper extends ReactionsGenerationHelper {
  public static class ReferenceMappingOperatorContext implements OperatorContext {
    @Extension
    private final TypeProvider typeProvider;

    public ReferenceMappingOperatorContext(final TypeProvider typeProvider) {
      Preconditions.<TypeProvider>checkNotNull(typeProvider, "typeProvider is null");
      this.typeProvider = typeProvider;
    }

    @Override
    public TypeProvider getTypeProvider() {
      return this.typeProvider;
    }

    private UnsupportedOperationException unsupportedOperationException() {
      return new UnsupportedOperationException("Unsupported in reference mapping context!");
    }

    @Override
    public boolean passParticipationAttributeValues() {
      throw this.unsupportedOperationException();
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
      throw this.unsupportedOperationException();
    }
  }

  @Inject
  @Extension
  private OperandHelper operandHelper;

  ReferenceMappingOperatorHelper() {
  }

  public XConstructorCall constructOperator(final OperatorReferenceMapping mapping, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
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
      Iterable<XExpression> _operandExpressions = this.operandHelper.getOperandExpressions(CommonalitiesLanguageModelExtensions.getPassedOperands(mapping), operatorContext);
      Iterables.<XExpression>addAll(_arguments_1, _operandExpressions);
    };
    return ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function);
  }

  private XMemberFeatureCall callOperatorMethod(final OperatorReferenceMapping mapping, final JvmOperation method, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(this.constructOperator(mapping, operatorContext));
      it.setFeature(method);
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  public XMemberFeatureCall callGetContainedObjects(final OperatorReferenceMapping mapping, final XExpression containerObject, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IReferenceMappingOperator.class, "getContainedObjects");
    XMemberFeatureCall _callOperatorMethod = this.callOperatorMethod(mapping, method, operatorContext);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      _memberCallArguments.add(containerObject);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_callOperatorMethod, _function);
  }

  public XMemberFeatureCall callGetContainer(final OperatorReferenceMapping mapping, final XExpression containedObject, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IReferenceMappingOperator.class, "getContainer");
    XMemberFeatureCall _callOperatorMethod = this.callOperatorMethod(mapping, method, operatorContext);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      _memberCallArguments.add(containedObject);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_callOperatorMethod, _function);
  }

  public XMemberFeatureCall callIsContained(final OperatorReferenceMapping mapping, final XExpression containerObject, final XExpression containedObject, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IReferenceMappingOperator.class, "isContained");
    XMemberFeatureCall _callOperatorMethod = this.callOperatorMethod(mapping, method, operatorContext);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      _memberCallArguments.add(containerObject);
      EList<XExpression> _memberCallArguments_1 = it.getMemberCallArguments();
      _memberCallArguments_1.add(containedObject);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_callOperatorMethod, _function);
  }

  public XMemberFeatureCall callInsert(final OperatorReferenceMapping mapping, final XExpression containerObject, final XExpression objectToInsert, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmOperation method = typeProvider.findMethod(IReferenceMappingOperator.class, "insert");
    XMemberFeatureCall _callOperatorMethod = this.callOperatorMethod(mapping, method, operatorContext);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      _memberCallArguments.add(containerObject);
      EList<XExpression> _memberCallArguments_1 = it.getMemberCallArguments();
      _memberCallArguments_1.add(objectToInsert);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_callOperatorMethod, _function);
  }

  public XMemberFeatureCall callGetPotentiallyContainedIntermediates(final OperatorReferenceMapping mapping, final XExpression containerObject, final EClass intermediateType, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmDeclaredType attributeReferenceHelperType = typeProvider.<JvmDeclaredType>imported(JvmTypeProviderHelper.findDeclaredType(typeProvider, AttributeReferenceHelper.class));
    final JvmOperation method = JvmTypeProviderHelper.findMethod(attributeReferenceHelperType, "getPotentiallyContainedIntermediates");
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(attributeReferenceHelperType, method);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setStaticWithDeclaringType(true);
      EList<JvmTypeReference> _typeArguments = it.getTypeArguments();
      JvmTypeReference _typeRef = typeProvider.getJvmTypeReferenceBuilder().typeRef(ReactionsHelper.getJavaClassName(intermediateType));
      _typeArguments.add(_typeRef);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XConstructorCall _constructOperator = this.constructOperator(mapping, operatorContext);
      XFeatureCall _correspondenceModel = typeProvider.correspondenceModel();
      XTypeLiteral _createXTypeLiteral = XbaseFactory.eINSTANCE.createXTypeLiteral();
      final Procedure1<XTypeLiteral> _function_1 = (XTypeLiteral it_1) -> {
        it_1.setType(this.getJvmTypeForQualifiedName(ReactionsHelper.getJavaClassName(intermediateType)));
      };
      XTypeLiteral _doubleArrow = ObjectExtensions.<XTypeLiteral>operator_doubleArrow(_createXTypeLiteral, _function_1);
      List<XExpression> _expressions = XbaseHelper.expressions(_constructOperator, containerObject, _correspondenceModel, _doubleArrow);
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public XMemberFeatureCall callGetPotentialContainerIntermediate(final OperatorReferenceMapping mapping, final XExpression containedObject, final EClass intermediateType, final ReferenceMappingOperatorHelper.ReferenceMappingOperatorContext operatorContext) {
    @Extension
    final TypeProvider typeProvider = operatorContext.typeProvider;
    final JvmDeclaredType attributeReferenceHelperType = typeProvider.<JvmDeclaredType>imported(JvmTypeProviderHelper.findDeclaredType(typeProvider, AttributeReferenceHelper.class));
    final JvmOperation method = JvmTypeProviderHelper.findMethod(attributeReferenceHelperType, "getPotentialContainerIntermediate");
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(attributeReferenceHelperType, method);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setStaticWithDeclaringType(true);
      final JvmTypeReference typeRef = typeProvider.getJvmTypeReferenceBuilder().typeRef(ReactionsHelper.getJavaClassName(intermediateType));
      EList<JvmTypeReference> _typeArguments = it.getTypeArguments();
      _typeArguments.add(typeRef);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XConstructorCall _constructOperator = this.constructOperator(mapping, operatorContext);
      XFeatureCall _correspondenceModel = typeProvider.correspondenceModel();
      XTypeLiteral _createXTypeLiteral = XbaseFactory.eINSTANCE.createXTypeLiteral();
      final Procedure1<XTypeLiteral> _function_1 = (XTypeLiteral it_1) -> {
        it_1.setType(this.getJvmTypeForQualifiedName(ReactionsHelper.getJavaClassName(intermediateType)));
      };
      XTypeLiteral _doubleArrow = ObjectExtensions.<XTypeLiteral>operator_doubleArrow(_createXTypeLiteral, _function_1);
      List<XExpression> _expressions = XbaseHelper.expressions(_constructOperator, containedObject, _correspondenceModel, _doubleArrow);
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  private JvmGenericType getJvmTypeForQualifiedName(final String name) {
    JvmGenericType _createJvmGenericType = TypesFactory.eINSTANCE.createJvmGenericType();
    final Procedure1<JvmGenericType> _function = (JvmGenericType it) -> {
      final int simpleNameSeparatorIndex = name.lastIndexOf(".");
      if ((simpleNameSeparatorIndex != (-1))) {
        it.setPackageName(name.substring(0, simpleNameSeparatorIndex));
      }
      it.setSimpleName(name.substring((simpleNameSeparatorIndex + 1)));
    };
    return ObjectExtensions.<JvmGenericType>operator_doubleArrow(_createJvmGenericType, _function);
  }
}
