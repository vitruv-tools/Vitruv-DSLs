package tools.vitruv.dsls.commonalities.runtime.operators.participation.condition;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import tools.vitruv.dsls.commonalities.runtime.operators.AttributeOperand;

@ParticipationConditionOperator(name = "in")
@SuppressWarnings("all")
public class ContainmentOperator extends AbstractSingleArgumentConditionOperator implements IParticipationClassConditionOperator {
  public ContainmentOperator(final Object leftOperand, final List<Object> rightOperands) {
    super(leftOperand, rightOperands);
    Object _rightOperand = this.getRightOperand();
    Preconditions.checkArgument((_rightOperand instanceof EObject), "Right operand is not of type EObject");
  }

  @Override
  public void enforce() {
    final EObject left = this.leftOperandObject;
    final EObject right = this.getRightOperandObject();
    final EReference containmentReference = this.getContainmentReference();
    int _upperBound = containmentReference.getUpperBound();
    boolean _notEquals = (_upperBound != 1);
    if (_notEquals) {
      Object _eGet = right.eGet(containmentReference);
      ((List<EObject>) _eGet).add(left);
    } else {
      right.eSet(containmentReference, left);
    }
  }

  @Override
  public boolean check() {
    final EObject left = this.leftOperandObject;
    final EObject right = this.getRightOperandObject();
    final EReference containmentReference = this.getContainmentReference();
    int _upperBound = containmentReference.getUpperBound();
    boolean _notEquals = (_upperBound != 1);
    if (_notEquals) {
      Object _eGet = right.eGet(containmentReference);
      return ((List<EObject>) _eGet).contains(left);
    } else {
      Object _eGet_1 = right.eGet(containmentReference);
      return Objects.equal(_eGet_1, left);
    }
  }

  private EObject getRightOperandObject() {
    Object _rightOperand = this.getRightOperand();
    if ((_rightOperand instanceof AttributeOperand)) {
      Object _rightOperand_1 = this.getRightOperand();
      final AttributeOperand rightAttributeOperand = ((AttributeOperand) _rightOperand_1);
      return rightAttributeOperand.getObject();
    } else {
      Object _rightOperand_2 = this.getRightOperand();
      return ((EObject) _rightOperand_2);
    }
  }

  private EReference getContainmentReference() {
    final EObject left = this.leftOperandObject;
    Object _rightOperand = this.getRightOperand();
    if ((_rightOperand instanceof AttributeOperand)) {
      Object _rightOperand_1 = this.getRightOperand();
      final AttributeOperand rightAttributeOperand = ((AttributeOperand) _rightOperand_1);
      EStructuralFeature _feature = rightAttributeOperand.getFeature();
      return ((EReference) _feature);
    } else {
      Object _rightOperand_2 = this.getRightOperand();
      final EObject right = ((EObject) _rightOperand_2);
      return tools.vitruv.dsls.commonalities.runtime.operators.participation.relation.ContainmentOperator.getContainmentReference(right.eClass(), left.eClass());
    }
  }
}
