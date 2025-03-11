package tools.vitruv.dsls.commonalities.tests.operators.cl_operators__;

import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.ReferenceMappingOperator;
import tools.vitruv.dsls.commonalities.tests.operators.MockReferenceMappingOperator;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@ReferenceMappingOperator(name = "mock", isMultiValued = true, isAttributeReference = true)
@SuppressWarnings("all")
public class mock implements IReferenceMappingOperator {
  private final MockReferenceMappingOperator delegate;

  public mock(final ReactionExecutionState executionState) {
    this.delegate = new MockReferenceMappingOperator(executionState);
  }

  public Iterable<? extends EObject> getContainedObjects(final EObject arg0) {
    return  this.delegate.getContainedObjects(arg0);
  }

  public EObject getContainer(final EObject arg0) {
    return  this.delegate.getContainer(arg0);
  }

  public boolean isContained(final EObject arg0, final EObject arg1) {
    return  this.delegate.isContained(arg0, arg1);
  }

  public void insert(final EObject arg0, final EObject arg1) {
     this.delegate.insert(arg0, arg1);
  }
}
