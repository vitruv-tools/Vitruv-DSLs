package tools.vitruv.dsls.commonalities.generator.reactions.condition;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.xtext.xbase.XBooleanLiteral;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import tools.vitruv.dsls.commonalities.generator.reactions.helper.ReactionsGenerationHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.participation.ParticipationObjectsHelper;
import tools.vitruv.dsls.commonalities.generator.reactions.util.XbaseHelper;
import tools.vitruv.dsls.commonalities.language.ParticipationClass;
import tools.vitruv.dsls.commonalities.language.ParticipationCondition;
import tools.vitruv.dsls.commonalities.participation.ParticipationConditionHelper;
import tools.vitruv.dsls.commonalities.participation.ParticipationContext;
import tools.vitruv.dsls.commonalities.runtime.matching.ParticipationObjects;
import tools.vitruv.dsls.reactions.builder.TypeProvider;

@SuppressWarnings("all")
public class CheckedParticipationConditionsHelper extends ReactionsGenerationHelper {
  @Inject
  @Extension
  private ParticipationConditionOperatorHelper participationConditionOperatorHelper;

  @Inject
  @Extension
  private ParticipationObjectsHelper participationObjectsHelper;

  CheckedParticipationConditionsHelper() {
  }

  public XExpression checkParticipationConditions(final ParticipationContext participationContext, @Extension final TypeProvider typeProvider) {
    return this.checkParticipationConditions(participationContext, typeProvider, null);
  }

  /**
   * Returns an expression which checks the participation conditions relevant
   * for the given participation context.
   * <p>
   * The expression returns <code>false</code> if there is at least one
   * unfulfilled condition.
   * <p>
   * The <code>participationObjects</code> is optional. If it is
   * <code>null</code>, the participation objects are retrieved from the
   * current routine context. Otherwise they are retrieved from the given
   * {@link ParticipationObjects}.
   */
  public XExpression checkParticipationConditions(final ParticipationContext participationContext, @Extension final TypeProvider typeProvider, final XFeatureCall participationObjects) {
    final ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext operatorContext = new ParticipationConditionOperatorHelper.ParticipationConditionOperatorContext(typeProvider) {
      @Override
      public XExpression getParticipationObject(final ParticipationClass participationClass) {
        XExpression _xifexpression = null;
        if ((participationObjects != null)) {
          return CheckedParticipationConditionsHelper.this.participationObjectsHelper.getParticipationObject(participationClass, XbaseHelper.<XFeatureCall>copy(participationObjects), this.getTypeProvider());
        } else {
          _xifexpression = super.getParticipationObject(participationClass);
        }
        return _xifexpression;
      }
    };
    final Function1<ParticipationCondition, XExpression> _function = (ParticipationCondition it) -> {
      return this.participationConditionOperatorHelper.check(it, operatorContext);
    };
    final List<XExpression> checks = IterableExtensions.<XExpression>toList(IterableExtensions.<ParticipationCondition, XExpression>map(ParticipationConditionHelper.getCheckedParticipationConditions(participationContext), _function));
    boolean _isEmpty = checks.isEmpty();
    if (_isEmpty) {
      XBooleanLiteral _createXBooleanLiteral = XbaseFactory.eINSTANCE.createXBooleanLiteral();
      final Procedure1<XBooleanLiteral> _function_1 = (XBooleanLiteral it) -> {
        it.setIsTrue(true);
      };
      return ObjectExtensions.<XBooleanLiteral>operator_doubleArrow(_createXBooleanLiteral, _function_1);
    } else {
      final Function2<XExpression, XExpression, XExpression> _function_2 = (XExpression check1, XExpression check2) -> {
        return XbaseHelper.and(check1, check2, typeProvider);
      };
      return IterableExtensions.<XExpression>reduce(checks, _function_2);
    }
  }
}
