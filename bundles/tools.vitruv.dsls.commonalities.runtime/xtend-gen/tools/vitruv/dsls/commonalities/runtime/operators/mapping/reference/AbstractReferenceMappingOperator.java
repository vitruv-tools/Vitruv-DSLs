package tools.vitruv.dsls.commonalities.runtime.operators.mapping.reference;

import com.google.common.base.Preconditions;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Note: Attribute operands are not passed to the constructor of the operator.
 */
@SuppressWarnings("all")
public abstract class AbstractReferenceMappingOperator implements IReferenceMappingOperator {
  @Extension
  protected final ReactionExecutionState executionState;

  public AbstractReferenceMappingOperator(final ReactionExecutionState executionState) {
    Preconditions.<ReactionExecutionState>checkNotNull(executionState, "executionState is null");
    this.executionState = executionState;
  }
}
