package tools.vitruv.dsls.commonalities.generator.reactions.participation;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.ReactionsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.Participation;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.extensions.CommonalitiesLanguageModelExtensions;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.participation.ParticipationContextHelper;
import tools.vitruv.dsls.commonalities.runtime.helper.XtendAssertHelper;
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationObjects;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

/**
 * Helper methods related to interfacing with the {@link ParticipationObjects}
 * during runtime.
 */
@SuppressWarnings("all")
public class ParticipationObjectsHelper extends ReactionsGenerationHelper {
  ParticipationObjectsHelper() {
  }

  public XExpression getParticipationObject(final ParticipationContext.ContextClass contextClass, final XFeatureCall participationObjects, final TypeProvider typeProvider) {
    return this.getParticipationObject(contextClass.getParticipationClass(), ReactionsGeneratorConventions.getName(contextClass), participationObjects, typeProvider);
  }

  public XExpression getParticipationObject(final ParticipationClass participationClass, final XFeatureCall participationObjects, final TypeProvider typeProvider) {
    return this.getParticipationObject(participationClass, participationClass.getName(), participationObjects, typeProvider);
  }

  private XExpression getParticipationObject(final ParticipationClass participationClass, final String objectName, final XFeatureCall participationObjects, final TypeProvider typeProvider) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(participationObjects);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(JvmTypeProviderHelper.findMethod(JvmTypeProviderHelper.findDeclaredType(typeProvider, ParticipationObjects.class), "getObject", String.class));
      EList<JvmTypeReference> _typeArguments = it.getTypeArguments();
      JvmTypeReference _typeRef = typeProvider.getJvmTypeReferenceBuilder().typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(participationClass)));
      _typeArguments.add(_typeRef);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      XExpression _stringLiteral = XbaseHelper.stringLiteral(objectName);
      _memberCallArguments.add(_stringLiteral);
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public Map<ParticipationClass, XVariableDeclaration> getParticipationObjectVars(final Participation participation, final XFeatureCall participationObjects, @Extension final TypeProvider typeProvider) {
    final Function1<ParticipationClass, XVariableDeclaration> _function = (ParticipationClass participationClass) -> {
      XVariableDeclaration _createXVariableDeclaration = XbaseFactory.eINSTANCE.createXVariableDeclaration();
      final Procedure1<XVariableDeclaration> _function_1 = (XVariableDeclaration it) -> {
        it.setType(typeProvider.getJvmTypeReferenceBuilder().typeRef(ReactionsHelper.getJavaClassName(this._generationContext.getChangeClass(participationClass))));
        it.setName(ReactionsGeneratorConventions.correspondingVariableName(participationClass));
        it.setRight(this.getParticipationObject(participationClass, XbaseHelper.<XFeatureCall>copy(participationObjects), typeProvider));
      };
      return ObjectExtensions.<XVariableDeclaration>operator_doubleArrow(_createXVariableDeclaration, _function_1);
    };
    return IterableExtensions.<ParticipationClass, XVariableDeclaration>toInvertedMap(CommonalitiesLanguageModelExtensions.getAllClasses(participation), _function);
  }

  public XExpression ifParticipationObjectsAvailable(@Extension final TypeProvider typeProvider, final Iterable<ParticipationClass> participationClasses, final Function<ParticipationClass, XExpression> participationClassToObject, final Supplier<XExpression> then) {
    final Function1<ParticipationClass, Boolean> _function = (ParticipationClass it) -> {
      return Boolean.valueOf(ParticipationContextHelper.isRootClass(it));
    };
    final Iterable<ParticipationClass> rootParticipationClasses = IterableExtensions.<ParticipationClass>filter(participationClasses, _function);
    boolean _isEmpty = IterableExtensions.isEmpty(rootParticipationClasses);
    if (_isEmpty) {
      return then.get();
    }
    XIfExpression rootIfExpr = null;
    XIfExpression currentIfExpr = null;
    for (final ParticipationClass participationClass : rootParticipationClasses) {
      {
        final XExpression participationObject = participationClassToObject.apply(participationClass);
        XIfExpression _createXIfExpression = XbaseFactory.eINSTANCE.createXIfExpression();
        final Procedure1<XIfExpression> _function_1 = (XIfExpression it) -> {
          it.setIf(XbaseHelper.notEqualsNull(participationObject, typeProvider));
        };
        final XIfExpression ifExpr = ObjectExtensions.<XIfExpression>operator_doubleArrow(_createXIfExpression, _function_1);
        if ((rootIfExpr == null)) {
          rootIfExpr = ifExpr;
        } else {
          XtendAssertHelper.assertTrue((currentIfExpr != null));
          currentIfExpr.setThen(ifExpr);
        }
        currentIfExpr = ifExpr;
      }
    }
    XtendAssertHelper.assertTrue((rootIfExpr != null));
    XtendAssertHelper.assertTrue((currentIfExpr != null));
    currentIfExpr.setThen(then.get());
    return rootIfExpr;
  }
}
