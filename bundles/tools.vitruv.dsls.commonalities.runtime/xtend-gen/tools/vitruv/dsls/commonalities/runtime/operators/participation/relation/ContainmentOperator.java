package tools.vitruv.dsls.commonalities.runtime.operators.participation.relation;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@ParticipationRelationOperator(name = "in")
@SuppressWarnings("all")
public class ContainmentOperator extends AbstractParticipationRelationOperator {
  public ContainmentOperator(final EObject[] leftObjects, final EObject[] rightObjects) {
    super(leftObjects, rightObjects);
  }

  @Override
  public void enforce() {
    for (final EObject right : this.rightObjects) {
      for (final EObject left : this.leftObjects) {
        {
          final EReference containmentReference = ContainmentOperator.getContainmentReference(right.eClass(), left.eClass());
          int _upperBound = containmentReference.getUpperBound();
          boolean _notEquals = (_upperBound != 1);
          if (_notEquals) {
            Object _eGet = right.eGet(containmentReference);
            ((List<EObject>) _eGet).add(left);
          } else {
            right.eSet(containmentReference, left);
          }
        }
      }
    }
  }

  @Override
  public boolean check() {
    for (final EObject right : this.rightObjects) {
      for (final EObject left : this.leftObjects) {
        {
          final EReference containmentReference = ContainmentOperator.getContainmentReference(right.eClass(), left.eClass());
          int _upperBound = containmentReference.getUpperBound();
          boolean _notEquals = (_upperBound != 1);
          if (_notEquals) {
            Object _eGet = right.eGet(containmentReference);
            boolean _contains = ((List<EObject>) _eGet).contains(left);
            boolean _not = (!_contains);
            if (_not) {
              return false;
            }
          } else {
            Object _eGet_1 = right.eGet(containmentReference);
            boolean _notEquals_1 = (!Objects.equal(_eGet_1, left));
            if (_notEquals_1) {
              return false;
            }
          }
        }
      }
    }
    return false;
  }

  public static EReference getContainmentReference(final EClass container, final EClass contained) {
    final Function1<EReference, Boolean> _function = (EReference it) -> {
      return Boolean.valueOf(((it.isContainment() && (it.getEType() instanceof EClass)) && ContainmentOperator.isAssignableFrom(((EClass) it.getEType()), contained)));
    };
    final EReference containmentFeature = IterableExtensions.<EReference>findFirst(container.getEAllReferences(), _function);
    if ((containmentFeature == null)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Could not find any containment feature in ‹");
      String _name = container.getName();
      _builder.append(_name);
      _builder.append("› that may contain ‹");
      String _name_1 = contained.getName();
      _builder.append(_name_1);
      _builder.append("›.");
      throw new IllegalStateException(_builder.toString());
    }
    return containmentFeature;
  }

  private static boolean isAssignableFrom(final EClass superType, final EClass candidate) {
    return (Objects.equal(superType, EcorePackage.Literals.EOBJECT) || superType.isSuperTypeOf(candidate));
  }
}
