package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.commonalities.runtime.operators.AttributeOperand;

@SuppressWarnings("all")
public abstract class AbstractParticipationConditionOperator implements IParticipationConditionOperator {
  protected final EObject leftOperandObject;

  protected final EStructuralFeature leftOperandFeature;

  protected final List<?> rightOperands;

  public AbstractParticipationConditionOperator(final Object leftOperand, final List<?> rightOperands) {
    Preconditions.<Object>checkNotNull(leftOperand, "Left operand is null");
    Preconditions.<List<?>>checkNotNull(rightOperands, "Right operands is null");
    if ((this instanceof IParticipationClassConditionOperator)) {
      Preconditions.checkArgument((leftOperand instanceof EObject), "This operator expects a model object as left operand");
      this.leftOperandObject = ((EObject) leftOperand);
      this.leftOperandFeature = null;
    } else {
      Preconditions.checkArgument((leftOperand instanceof AttributeOperand), "This operator expects an attribute as left operand");
      final AttributeOperand leftAttributeOperand = ((AttributeOperand) leftOperand);
      this.leftOperandObject = leftAttributeOperand.getObject();
      this.leftOperandFeature = leftAttributeOperand.getFeature();
    }
    this.rightOperands = rightOperands;
  }
}
