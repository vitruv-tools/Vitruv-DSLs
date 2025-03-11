package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Base class for {@link IReferenceMappingOperator reference mapping operators}
 * which involve a regular containment reference between a container and its
 * contained objects.
 * <p>
 * The default implementation mimics the behavior of simple reference mappings.
 * <p>
 * TODO This type of operator is not properly supported by the language yet.
 */
@SuppressWarnings("all")
public class ContainmentReferenceMappingOperator extends AbstractReferenceMappingOperator {
  protected final EReference reference;

  public ContainmentReferenceMappingOperator(final ReactionExecutionState executionState, final EReference reference) {
    super(executionState);
    Preconditions.<EReference>checkNotNull(reference, "reference is null");
    Preconditions.checkArgument(reference.isContainment(), "reference is not a containment");
    this.reference = reference;
  }

  protected void validateContainer(final EObject container) {
    Preconditions.<EObject>checkNotNull(container, "container is null");
    EClass _eClass = container.eClass();
    EClass _eContainingClass = this.reference.getEContainingClass();
    boolean _tripleEquals = (_eClass == _eContainingClass);
    String _name = this.reference.getEContainingClass().getName();
    String _plus = ("container is not of type " + _name);
    Preconditions.checkArgument(_tripleEquals, _plus);
  }

  protected void validateContainedObject(final EObject containedObject) {
    Preconditions.<EObject>checkNotNull(containedObject, "containedObject is null");
    EClass _eClass = containedObject.eClass();
    EClass _eReferenceType = this.reference.getEReferenceType();
    boolean _tripleEquals = (_eClass == _eReferenceType);
    String _name = this.reference.getEReferenceType().getName();
    String _plus = ("containedObject is not of type " + _name);
    Preconditions.checkArgument(_tripleEquals, _plus);
  }

  @Override
  public Iterable<? extends EObject> getContainedObjects(final EObject container) {
    this.validateContainer(container);
    final Object value = container.eGet(this.reference);
    boolean _isMany = this.reference.isMany();
    if (_isMany) {
      return ((List<? extends EObject>) value);
    } else {
      return Collections.<EObject>unmodifiableList(CollectionLiterals.<EObject>newArrayList(((EObject) value)));
    }
  }

  @Override
  public EObject getContainer(final EObject object) {
    this.validateContainedObject(object);
    return object.eContainer();
  }

  @Override
  public boolean isContained(final EObject container, final EObject contained) {
    this.validateContainer(container);
    this.validateContainedObject(contained);
    return ((contained.eContainer() == container) && (contained.eContainmentFeature() == this.reference));
  }

  @Override
  public void insert(final EObject container, final EObject object) {
    this.validateContainer(container);
    this.validateContainedObject(object);
    boolean _isMany = this.reference.isMany();
    if (_isMany) {
      Object _eGet = container.eGet(this.reference);
      ((List<EObject>) _eGet).add(object);
    } else {
      container.eSet(this.reference, object);
    }
  }
}
