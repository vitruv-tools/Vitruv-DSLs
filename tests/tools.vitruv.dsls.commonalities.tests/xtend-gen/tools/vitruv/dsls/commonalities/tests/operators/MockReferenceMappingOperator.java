package tools.vitruv.dsls.commonalities.tests.operators;

import org.eclipse.emf.ecore.EObject;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.IReferenceMappingOperator;
import tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference.ReferenceMappingOperator;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@ReferenceMappingOperator(name = "mock", isMultiValued = true, isAttributeReference = true)
@SuppressWarnings("all")
public class MockReferenceMappingOperator implements IReferenceMappingOperator {
  public MockReferenceMappingOperator(final ReactionExecutionState executionState) {
  }

  @Override
  public Iterable<? extends EObject> getContainedObjects(final EObject container) {
    throw new UnsupportedOperationException("This is a mock");
  }

  @Override
  public EObject getContainer(final EObject object) {
    throw new UnsupportedOperationException("This is a mock");
  }

  @Override
  public boolean isContained(final EObject container, final EObject contained) {
    throw new UnsupportedOperationException("This is a mock");
  }

  @Override
  public void insert(final EObject container, final EObject object) {
    throw new UnsupportedOperationException("This is a mock");
  }
}
