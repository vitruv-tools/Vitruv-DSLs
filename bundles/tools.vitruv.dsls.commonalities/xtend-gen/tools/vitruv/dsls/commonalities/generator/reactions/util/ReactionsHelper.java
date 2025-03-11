package tools.vitruv.dsls.commonalities.generator.reactions.util;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.runtime.intermediatemodelbase.IntermediateModelBasePackage;
import tools.vitruv.dsls.reactions.builder.FluentReactionBuilder;
import tools.vitruv.dsls.reactions.builder.FluentRoutineBuilder;
import tools.vitruv.dsls.reactions.builder.TypeProvider;
import tools.vitruv.dsls.reactions.codegen.helper.ReactionsLanguageHelper;

@Utility
@SuppressWarnings("all")
public final class ReactionsHelper {
  /**
   * Keeps track of the calling routine and the caller context.
   * <p>
   * This is useful when making routine calls from within execution code
   * blocks.
   */
  public static class RoutineCallContext {
    private FluentRoutineBuilder caller;

    private XExpression callerContext;

    public RoutineCallContext() {
    }

    public FluentRoutineBuilder setCaller(final FluentRoutineBuilder caller) {
      this.caller = caller;
      return caller;
    }

    public XExpression setCallerContext(final XExpression callerContext) {
      this.callerContext = callerContext;
      return callerContext;
    }

    private ReactionsHelper.RoutineCallContext checkReady() {
      if ((this.caller == null)) {
        throw new IllegalStateException("Caller has not been set yet for the RoutineCallContext!");
      }
      if ((this.callerContext == null)) {
        throw new IllegalStateException("CallerContext has not been set yet for the RoutineCallContext!");
      }
      return this;
    }
  }

  public static String getJavaClassName(final EClassifier classifier) {
    return ReactionsLanguageHelper.getJavaClassName(classifier);
  }

  public static FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterElementInsertedAsRoot(final FluentReactionBuilder.TriggerBuilder reactionTriggerBuilder, final EClass changeClass) {
    FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xifexpression = null;
    boolean _contains = changeClass.getESuperTypes().contains(IntermediateModelBasePackage.eINSTANCE.getIntermediate());
    if (_contains) {
      _xifexpression = reactionTriggerBuilder.afterElement(changeClass).insertedIn(
        IntermediateModelBasePackage.eINSTANCE.getRoot_Intermediates());
    } else {
      _xifexpression = reactionTriggerBuilder.afterElement(changeClass).insertedAsRoot();
    }
    return _xifexpression;
  }

  public static FluentReactionBuilder.PreconditionOrRoutineCallBuilder afterElementRemovedAsRoot(final FluentReactionBuilder.TriggerBuilder reactionTriggerBuilder, final EClass changeClass) {
    FluentReactionBuilder.PreconditionOrRoutineCallBuilder _xifexpression = null;
    boolean _contains = changeClass.getESuperTypes().contains(IntermediateModelBasePackage.eINSTANCE.getIntermediate());
    if (_contains) {
      _xifexpression = reactionTriggerBuilder.afterElement(changeClass).removedFrom(
        IntermediateModelBasePackage.eINSTANCE.getRoot_Intermediates());
    } else {
      _xifexpression = reactionTriggerBuilder.afterElement(changeClass).removedAsRoot();
    }
    return _xifexpression;
  }

  public static XMemberFeatureCall callGetMetadataModelURI(@Extension final TypeProvider typeProvider, final Iterable<String> metadataKey) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(typeProvider.resourceAccess());
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider.resourceAccessType(), "getMetadataModelURI"));
      it.setExplicitOperationCall(true);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      final Function1<String, XExpression> _function_1 = (String it_1) -> {
        return XbaseHelper.stringLiteral(it_1);
      };
      Iterable<XExpression> _map = IterableExtensions.<String, XExpression>map(metadataKey, _function_1);
      Iterables.<XExpression>addAll(_memberCallArguments, _map);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XMemberFeatureCall callGetModelResource(@Extension final TypeProvider typeProvider, final XFeatureCall uri) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(typeProvider.resourceAccess());
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider.resourceAccessType(), "getModelResource"));
      it.setExplicitOperationCall(true);
      EList<XExpression> _memberCallArguments = it.getMemberCallArguments();
      _memberCallArguments.add(uri);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  public static XFeatureCall createRoutineCall(final ReactionsHelper.RoutineCallContext callerContext, final TypeProvider typeProvider, final FluentRoutineBuilder routine, final XExpression... parameters) {
    callerContext.checkReady();
    return ReactionsHelper.createRoutineCall(callerContext.caller, callerContext.callerContext, typeProvider, routine, parameters);
  }

  public static XFeatureCall createRoutineCall(final FluentRoutineBuilder caller, final XExpression callerContext, final TypeProvider typeProvider, final FluentRoutineBuilder routine, final XExpression... parameters) {
    XFeatureCall _createXFeatureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
    final Procedure1<XFeatureCall> _function = (XFeatureCall it) -> {
      it.setExplicitOperationCall(true);
      it.setFeature(routine.getJvmOperation());
      it.setImplicitReceiver(caller.getJvmOperationRoutineFacade(callerContext));
      EList<XExpression> _featureCallArguments = it.getFeatureCallArguments();
      Iterables.<XExpression>addAll(_featureCallArguments, ((Iterable<? extends XExpression>)Conversions.doWrapArray(parameters)));
    };
    return ObjectExtensions.<XFeatureCall>operator_doubleArrow(_createXFeatureCall, _function);
  }

  private ReactionsHelper() {
    
  }
}
