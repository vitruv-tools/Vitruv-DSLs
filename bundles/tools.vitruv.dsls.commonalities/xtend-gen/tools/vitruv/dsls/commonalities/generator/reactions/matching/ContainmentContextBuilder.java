package tools.vitruv.dsls.commonalities.generator.reactions.matching;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.runtime.matching.ContainmentContext;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

/**
 * Builds the expressions to create and setup a ContainmentContext at runtime.
 */
@SuppressWarnings("all")
class ContainmentContextBuilder {
  @Extension
  private final TypeProvider typeProvider;

  private final JvmDeclaredType containmentContextType;

  private final JvmOperation addNodeMethod;

  private final JvmOperation addReferenceEdgeMethod;

  private final JvmOperation addOperatorEdgeMethod;

  private final JvmOperation addAttributeReferenceEdgeMethod;

  private final XBlockExpression result = XbaseFactory.eINSTANCE.createXBlockExpression();

  private XVariableDeclaration containmentContextVar;

  public ContainmentContextBuilder(final TypeProvider typeProvider) {
    this.typeProvider = typeProvider;
    this.containmentContextType = this.typeProvider.<JvmDeclaredType>imported(JvmTypeProviderHelper.findDeclaredType(typeProvider, ContainmentContext.class));
    this.addNodeMethod = JvmTypeProviderHelper.findMethod(this.containmentContextType, "addNode", String.class, EClass.class, String.class);
    this.addReferenceEdgeMethod = JvmTypeProviderHelper.findMethod(this.containmentContextType, "addReferenceEdge", String.class, String.class, EReference.class);
    this.addOperatorEdgeMethod = JvmTypeProviderHelper.findMethod(this.containmentContextType, "addOperatorEdge", String.class, String.class, 
      IReferenceMappingOperator.class);
    this.addAttributeReferenceEdgeMethod = JvmTypeProviderHelper.findMethod(this.containmentContextType, "addAttributeReferenceEdge", String.class, 
      IReferenceMappingOperator.class);
  }

  private void checkHasContainmentContext() {
    Preconditions.checkState((this.containmentContextVar != null), "The ContainmentContext has not yet been created!");
  }

  /**
   * Gets the variable declaration for the created ContainmentContext.
   */
  public XVariableDeclaration getContainmentContextVar() {
    this.checkHasContainmentContext();
    return this.containmentContextVar;
  }

  /**
   * Gets the block of built expressions which create and set up the
   * ContainmentContext.
   */
  public XBlockExpression getResultExpressions() {
    this.checkHasContainmentContext();
    return this.result;
  }

  /**
   * Begins the building of the ContainmentContext.
   */
  public void newContainmentContext(final String variableName) {
    Preconditions.checkState((this.containmentContextVar == null), "ContainmentContextBuilder can only be used once!");
    XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
    final Procedure1<XVariableDeclaration> _function = (XVariableDeclaration it) -> {
      it.setName(variableName);
      it.setWriteable(false);
      it.setType(this.typeProvider.getJvmTypeReferenceBuilder().typeRef(this.containmentContextType));
      XConstructorCall _createXConstructorCall = XbaseFactory.eINSTANCE.createXConstructorCall();
      final Procedure1<XConstructorCall> _function_1 = (XConstructorCall it_1) -> {
        it_1.setConstructor(JvmTypeProviderHelper.findNoArgsConstructor(this.containmentContextType));
      };
      XConstructorCall _doubleArrow = ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function_1);
      it.setRight(_doubleArrow);
    };
    XVariableDeclaration _doubleArrow = ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function);
    this.containmentContextVar = _doubleArrow;
    EList<XExpression> _expressions = this.result.getExpressions();
    _expressions.add(this.containmentContextVar);
  }

  private XMemberFeatureCall containmentContextMemberCall(final JvmIdentifiableElement featureElement) {
    return XbaseHelper.memberFeatureCall(XbaseHelper.featureCall(this.containmentContextVar), featureElement);
  }

  public void setRootIntermediateType(final EClass rootIntermediateType) {
    this.checkHasContainmentContext();
    final JvmOperation setRootIntermediateTypeMethod = JvmTypeProviderHelper.findMethod(this.containmentContextType, "setRootIntermediateType", EClass.class);
    EList<XExpression> _expressions = this.result.getExpressions();
    XMemberFeatureCall _containmentContextMemberCall = this.containmentContextMemberCall(setRootIntermediateTypeMethod);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setExplicitOperationCall(true);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XMemberFeatureCall _eClass = EmfAccessExpressions.getEClass(this.typeProvider, rootIntermediateType);
      _memberCallArguments.add(_eClass);
    };
    XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_containmentContextMemberCall, _function);
    _expressions.add(_doubleArrow);
  }

  public void addNode(final String nodeName, final EClass nodeType, final String correspondenceTag) {
    this.checkHasContainmentContext();
    EList<XExpression> _expressions = this.result.getExpressions();
    XMemberFeatureCall _containmentContextMemberCall = this.containmentContextMemberCall(this.addNodeMethod);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions_1 = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(nodeName), 
        EmfAccessExpressions.getEClass(this.typeProvider, nodeType), 
        XbaseHelper.stringLiteral(correspondenceTag));
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions_1);
    };
    XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_containmentContextMemberCall, _function);
    _expressions.add(_doubleArrow);
  }

  public void addReferenceEdge(final String containerNode, final String containedNode, final EReference containmentEReference) {
    this.checkHasContainmentContext();
    EList<XExpression> _expressions = this.result.getExpressions();
    XMemberFeatureCall _containmentContextMemberCall = this.containmentContextMemberCall(this.addReferenceEdgeMethod);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions_1 = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(containerNode), 
        XbaseHelper.stringLiteral(containedNode), 
        EmfAccessExpressions.getEReference(this.typeProvider, containmentEReference));
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions_1);
    };
    XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_containmentContextMemberCall, _function);
    _expressions.add(_doubleArrow);
  }

  public void addOperatorEdge(final String containerNode, final String containedNode, final XExpression operator) {
    this.checkHasContainmentContext();
    EList<XExpression> _expressions = this.result.getExpressions();
    XMemberFeatureCall _containmentContextMemberCall = this.containmentContextMemberCall(this.addOperatorEdgeMethod);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions_1 = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(containerNode), 
        XbaseHelper.stringLiteral(containedNode), operator);
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions_1);
    };
    XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_containmentContextMemberCall, _function);
    _expressions.add(_doubleArrow);
  }

  public void setAttributeReferenceRootNode(final String nodeName, final EClass nodeType, final String correspondenceTag) {
    this.checkHasContainmentContext();
    final JvmOperation setAttributeReferenceRootNodeMethod = JvmTypeProviderHelper.findMethod(this.containmentContextType, "setAttributeReferenceRootNode", 
      String.class, EClass.class, String.class);
    EList<XExpression> _expressions = this.result.getExpressions();
    XMemberFeatureCall _containmentContextMemberCall = this.containmentContextMemberCall(setAttributeReferenceRootNodeMethod);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setExplicitOperationCall(true);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions_1 = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(nodeName), 
        EmfAccessExpressions.getEClass(this.typeProvider, nodeType), 
        XbaseHelper.stringLiteral(correspondenceTag));
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions_1);
    };
    XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_containmentContextMemberCall, _function);
    _expressions.add(_doubleArrow);
  }

  public void addAttributeReferenceEdge(final String containedNode, final XExpression operator) {
    this.checkHasContainmentContext();
    EList<XExpression> _expressions = this.result.getExpressions();
    XMemberFeatureCall _containmentContextMemberCall = this.containmentContextMemberCall(this.addAttributeReferenceEdgeMethod);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      List<XExpression> _expressions_1 = XbaseHelper.expressions(
        XbaseHelper.stringLiteral(containedNode), operator);
      Iterables.<XExpression>addAll(_memberCallArguments, _expressions_1);
    };
    XMemberFeatureCall _doubleArrow = ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_containmentContextMemberCall, _function);
    _expressions.add(_doubleArrow);
  }
}
