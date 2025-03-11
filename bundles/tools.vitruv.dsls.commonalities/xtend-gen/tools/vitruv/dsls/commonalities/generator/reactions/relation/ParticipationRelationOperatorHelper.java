package tools.vitruv.dsls.commonalities.generator.reactions.relation;

import com.google.common.collect.Iterables;
import edu.kit.ipd.sdq.activextendannotations.Utility;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XListLiteral;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.ReactionsGeneratorConventions;
import tools.vitruv.dsls.commonalities.generator.reactions.util.JvmTypeProviderHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationRelation;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@Utility
@SuppressWarnings("all")
final class ParticipationRelationOperatorHelper {
  private static XConstructorCall constructOperator(final ParticipationRelation relation, @Extension final TypeProvider typeProvider) {
    XConstructorCall _createXConstructorCall = XbaseFactory.eINSTANCE.createXConstructorCall();
    final Procedure1<XConstructorCall> _function = (XConstructorCall it) -> {
      final JvmDeclaredType operatorType = typeProvider.<JvmDeclaredType>imported(relation.getOperator());
      it.setConstructor(JvmTypeProviderHelper.findConstructor(operatorType, JvmTypeProviderHelper.getArrayClass(EObject.class), JvmTypeProviderHelper.getArrayClass(EObject.class)));
      it.setExplicitConstructorCall(true);
      EList<XExpression> _arguments = it.getArguments();
      XListLiteral _createXListLiteral = XbaseFactory.eINSTANCE.createXListLiteral();
      final Procedure1<XListLiteral> _function_1 = (XListLiteral it_1) -> {
        EList<XExpression> _elements = it_1.getElements();
        final Function1<ParticipationClass, XFeatureCall> _function_2 = (ParticipationClass it_2) -> {
          return typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(it_2));
        };
        Iterable<XFeatureCall> _map = IterableExtensions.<ParticipationClass, XFeatureCall>map(Iterables.<ParticipationClass>filter(relation.getLeftParts(), ParticipationClass.class), _function_2);
        Iterables.<XExpression>addAll(_elements, _map);
      };
      XListLiteral _doubleArrow = ObjectExtensions.<XListLiteral>operator_doubleArrow(_createXListLiteral, _function_1);
      XListLiteral _createXListLiteral_1 = XbaseFactory.eINSTANCE.createXListLiteral();
      final Procedure1<XListLiteral> _function_2 = (XListLiteral it_1) -> {
        EList<XExpression> _elements = it_1.getElements();
        final Function1<ParticipationClass, XFeatureCall> _function_3 = (ParticipationClass it_2) -> {
          return typeProvider.variable(ReactionsGeneratorConventions.correspondingVariableName(it_2));
        };
        Iterable<XFeatureCall> _map = IterableExtensions.<ParticipationClass, XFeatureCall>map(Iterables.<ParticipationClass>filter(relation.getRightParts(), ParticipationClass.class), _function_3);
        Iterables.<XExpression>addAll(_elements, _map);
      };
      XListLiteral _doubleArrow_1 = ObjectExtensions.<XListLiteral>operator_doubleArrow(_createXListLiteral_1, _function_2);
      List<XExpression> _expressions = XbaseHelper.expressions(_doubleArrow, _doubleArrow_1);
      Iterables.<XExpression>addAll(_arguments, _expressions);
    };
    return ObjectExtensions.<XConstructorCall>operator_doubleArrow(_createXConstructorCall, _function);
  }

  public static XMemberFeatureCall callRelationOperation(final ParticipationRelation relation, final JvmOperation operation, @Extension final TypeProvider typeProvider) {
    XMemberFeatureCall _createXMemberFeatureCall = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
    final Procedure1<XMemberFeatureCall> _function = (XMemberFeatureCall it) -> {
      it.setMemberCallTarget(ParticipationRelationOperatorHelper.constructOperator(relation, typeProvider));
      it.setFeature(operation);
      it.setExplicitOperationCall(true);
    };
    return ObjectExtensions.<XMemberFeatureCall>operator_doubleArrow(_createXMemberFeatureCall, _function);
  }

  private ParticipationRelationOperatorHelper() {
    
  }
}
