package tools.vitruv.dsls.commonalities.generator.reactions.util;

import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.Collection;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.CollectionExtensions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@Utility
@SuppressWarnings("all")
public final class XbaseCollectionHelper {
  public static XBinaryOperation addToCollection(@Extension final TypeProvider typeProvider, final XExpression collection, final XExpression newValue) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(collection);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, CollectionExtensions.class, "operator_add", Collection.class, JvmTypeProviderHelper.typeVariable()));
      it.setRightOperand(newValue);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation addAllToCollection(@Extension final TypeProvider typeProvider, final XExpression collection, final XExpression newValues) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(collection);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, CollectionExtensions.class, "operator_add", Collection.class, Iterable.class));
      it.setRightOperand(newValues);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XBinaryOperation removeFromCollection(@Extension final TypeProvider typeProvider, final XExpression collection, final XExpression newValue) {
    XBinaryOperation _createXBinaryOperation = XbaseFactory.eINSTANCE.createXBinaryOperation();
    final Procedure1<XBinaryOperation> _function = (XBinaryOperation it) -> {
      it.setLeftOperand(collection);
      it.setFeature(JvmTypeProviderHelper.findMethod(typeProvider, CollectionExtensions.class, "operator_remove", Collection.class, JvmTypeProviderHelper.typeVariable()));
      it.setRightOperand(newValue);
    };
    return ObjectExtensions.<XBinaryOperation>operator_doubleArrow(_createXBinaryOperation, _function);
  }

  public static XMemberFeatureCall clearCollection(@Extension final TypeProvider typeProvider, final XExpression collection) {
    XMemberFeatureCall _memberFeatureCall = XbaseHelper.memberFeatureCall(collection);
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setFeature(typeProvider.findMethod(Collection.class, "clear"));
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_memberFeatureCall, _function);
  }

  private XbaseCollectionHelper() {
    
  }
}
