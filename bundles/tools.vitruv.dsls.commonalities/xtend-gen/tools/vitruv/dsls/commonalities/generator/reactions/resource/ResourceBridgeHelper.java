package tools.vitruv.dsls.commonalities.generator.reactions.resource;

import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Commonality;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.runtime.resources.IntermediateResourceBridge;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class ResourceBridgeHelper extends ReactionsGenerationHelper {
  ResourceBridgeHelper() {
  }

  public XBlockExpression initExistingResourceBridge(@Extension final TypeProvider typeProvider, final ParticipationClass resourceClass, final XFeatureCall resourceBridge) {
    return this.setupResourceBridge(resourceClass, resourceBridge, typeProvider);
  }

  public XBlockExpression initNewResourceBridge(final ParticipationClass resourceClass, final XFeatureCall resourceBridge, final TypeProvider typeProvider) {
    XBlockExpression _setupResourceBridge = this.setupResourceBridge(resourceClass, resourceBridge, typeProvider);
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      EList<XExpression> _expressions = it.getExpressions();
      XAssignment _createXAssignment = XbaseFactory.eINSTANCE.createXAssignment();
      final Procedure1<XAssignment> _function_1 = (XAssignment it_1) -> {
        it_1.setAssignable(XbaseHelper.<XFeatureCall>copy(resourceBridge));
        it_1.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, IntermediateResourceBridge.class, "setFileExtension"));
        XStringLiteral _createXStringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
        final Procedure1<XStringLiteral> _function_2 = (XStringLiteral it_2) -> {
          it_2.setValue(resourceClass.getSuperMetaclass().getDomain().getName());
        };
        XStringLiteral _doubleArrow = ObjectExtensions.<XStringLiteral>operator_doubleArrow(_createXStringLiteral, _function_2);
        it_1.setValue(_doubleArrow);
      };
      XAssignment _doubleArrow = ObjectExtensions.<XAssignment>operator_doubleArrow(_createXAssignment, _function_1);
      _expressions.add(_doubleArrow);
      EList<XExpression> _expressions_1 = it.getExpressions();
      XAssignment _setIsPersistenceEnabled = this.setIsPersistenceEnabled(typeProvider, XbaseHelper.<XFeatureCall>copy(resourceBridge), false);
      _expressions_1.add(_setIsPersistenceEnabled);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_setupResourceBridge, _function);
  }

  public XAssignment setIsPersistenceEnabled(final TypeProvider typeProvider, final XFeatureCall resourceBridge, final boolean newIsPersistenceEnabled) {
    XAssignment _createXAssignment = XbaseFactory.eINSTANCE.createXAssignment();
    final Procedure1<XAssignment> _function = (XAssignment it) -> {
      it.setAssignable(resourceBridge);
      it.setFeature(typeProvider.findMethod(IntermediateResourceBridge.class, "setIsPersistenceEnabled"));
      it.setValue(XbaseHelper.booleanLiteral(newIsPersistenceEnabled));
    };
    return ObjectExtensions.<XAssignment>operator_doubleArrow(_createXAssignment, _function);
  }

  /**
   * The ResourceBridge setup that is common for both newly created resources
   * and already existing resources.
   */
  private XBlockExpression setupResourceBridge(final ParticipationClass resourceClass, final XFeatureCall resourceBridge, @Extension final TypeProvider typeProvider) {
    final Commonality commonality = CommonalitiesLanguageModelExtensions.getDeclaringCommonality(resourceClass);
    XBlockExpression _createXBlockExpression = XbaseFactory.eINSTANCE.createXBlockExpression();
    final Procedure1<XBlockExpression> _function = (XBlockExpression it) -> {
      EList<XExpression> _expressions = it.getExpressions();
      XAssignment _createXAssignment = XbaseFactory.eINSTANCE.createXAssignment();
      final Procedure1<XAssignment> _function_1 = (XAssignment it_1) -> {
        it_1.setAssignable(XbaseHelper.<XFeatureCall>copy(resourceBridge));
        it_1.setFeature(typeProvider.findMethod(IntermediateResourceBridge.class, "setResourceAccess"));
        it_1.setValue(typeProvider.resourceAccess());
      };
      XAssignment _doubleArrow = ObjectExtensions.<XAssignment>operator_doubleArrow(_createXAssignment, _function_1);
      XAssignment _createXAssignment_1 = XbaseFactory.eINSTANCE.createXAssignment();
      final Procedure1<XAssignment> _function_2 = (XAssignment it_1) -> {
        it_1.setAssignable(XbaseHelper.<XFeatureCall>copy(resourceBridge));
        it_1.setFeature(typeProvider.findMethod(IntermediateResourceBridge.class, "setCorrespondenceModel"));
        it_1.setValue(typeProvider.correspondenceModel());
      };
      XAssignment _doubleArrow_1 = ObjectExtensions.<XAssignment>operator_doubleArrow(_createXAssignment_1, _function_2);
      XAssignment _createXAssignment_2 = XbaseFactory.eINSTANCE.createXAssignment();
      final Procedure1<XAssignment> _function_3 = (XAssignment it_1) -> {
        it_1.setAssignable(XbaseHelper.<XFeatureCall>copy(resourceBridge));
        it_1.setFeature(typeProvider.findMethod(IntermediateResourceBridge.class, "setIntermediateNS"));
        XStringLiteral _createXStringLiteral = XbaseFactory.eINSTANCE.createXStringLiteral();
        final Procedure1<XStringLiteral> _function_4 = (XStringLiteral it_2) -> {
          it_2.setValue(this._generationContext.getMetamodelRootPackage(CommonalitiesLanguageModelExtensions.getConcept(commonality)).getNsURI());
        };
        XStringLiteral _doubleArrow_2 = ObjectExtensions.<XStringLiteral>operator_doubleArrow(_createXStringLiteral, _function_4);
        it_1.setValue(_doubleArrow_2);
      };
      XAssignment _doubleArrow_2 = ObjectExtensions.<XAssignment>operator_doubleArrow(_createXAssignment_2, _function_3);
      List<XExpression> _expressions_1 = XbaseHelper.expressions(_doubleArrow, _doubleArrow_1, _doubleArrow_2);
      Iterables.<XExpression>addAll(_expressions, _expressions_1);
    };
    return ObjectExtensions.<XBlockExpression>operator_doubleArrow(_createXBlockExpression, _function);
  }
}
