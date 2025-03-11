package tools.vitruv.dsls.commonalities.generator.reactions.intermediatemodel;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XInstanceOfExpression;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.EmfAccessExpressions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.CommonalityReference;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.Root;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class IntermediateContainmentReactionsHelper extends ReactionsGenerationHelper {
  IntermediateContainmentReactionsHelper() {
  }

  /**
   * Gets the intermediate's container if it is contained by the given commonality reference, or <code>null</code> if
   * no such container exists.
   */
  public XExpression getIntermediateContainer(@Extension final TypeProvider typeProvider, final XExpression containedIntermediate, final CommonalityReference commonalityReference) {
    XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
    final Procedure1<XIfExpression> _function = (XIfExpression it) -> {
      it.setIf(this.isIntermediateContainmentReferenceMatching(typeProvider, containedIntermediate, commonalityReference));
      XCastedExpression _createXCastedExpression = XbaseFactory.eINSTANCE.createXCastedExpression();
      final Procedure1<XCastedExpression> _function_1 = (XCastedExpression it_1) -> {
        final Commonality containerCommonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(commonalityReference);
        it_1.setTarget(EmfAccessExpressions.getEContainer(typeProvider, XbaseHelper.<XExpression>copy(containedIntermediate)));
        it_1.setType(typeProvider.getJvmTypeReferenceBuilder().typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(containerCommonality))));
      };
      XCastedExpression _doubleArrow = ObjectExtensions.<XCastedExpression>operator_doubleArrow(_createXCastedExpression, _function_1);
      it.setThen(_doubleArrow);
      it.setElse(XbaseHelper.nullLiteral());
    };
    return ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function);
  }

  /**
   * Checks if the reference containing the given intermediate matches the specified commonality reference.
   */
  public XExpression isIntermediateContainmentReferenceMatching(@Extension final TypeProvider typeProvider, final XExpression containedIntermediate, final CommonalityReference commonalityReference) {
    final EReference commonalityEReference = this._generationContext.getCorrespondingEReference(commonalityReference);
    return XbaseHelper.equals(EmfAccessExpressions.getEContainmentFeature(typeProvider, containedIntermediate), EmfAccessExpressions.getEReference(typeProvider, commonalityEReference), typeProvider);
  }

  /**
   * Checks if the given intermediate is contained by the commonality reference of the given container intermediate.
   * <p>
   * The container commonality might have multiple commonality references which are able to contain the intermediate.
   * We therefore not only compare the container, but also check if the containment reference matches the expected
   * commonality reference.
   */
  public XExpression isIntermediateContainerMatching(@Extension final TypeProvider typeProvider, final XExpression containedIntermediate, final XExpression containerIntermediate, final CommonalityReference commonalityReference) {
    final XBinaryOperation checkContainer = XbaseHelper.equals(EmfAccessExpressions.getEContainer(typeProvider, containedIntermediate), containerIntermediate, typeProvider);
    final XExpression checkContainmentReference = this.isIntermediateContainmentReferenceMatching(typeProvider, 
      XbaseHelper.<XExpression>copy(containedIntermediate), commonalityReference);
    return XbaseHelper.and(checkContainer, checkContainmentReference, typeProvider);
  }

  /**
   * Checks if the given intermediate is contained by the intermediate model root.
   */
  public XExpression isIntermediateContainedAtRoot(@Extension final TypeProvider typeProvider, final XExpression intermediate) {
    XInstanceOfExpression _createXInstanceOfExpression = XbaseFactory.eINSTANCE.createXInstanceOfExpression();
    final Procedure1<XInstanceOfExpression> _function = (XInstanceOfExpression it) -> {
      it.setExpression(EmfAccessExpressions.getEContainer(typeProvider, intermediate));
      it.setType(typeProvider.getJvmTypeReferenceBuilder().typeRef(Root.class));
    };
    return ObjectExtensions.<XInstanceOfExpression>operator_doubleArrow(_createXInstanceOfExpression, _function);
  }
}
