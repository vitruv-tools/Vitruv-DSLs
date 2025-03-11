package tools.vitruv.dsls.commonalities.runtime.operators.mapping.attribute;

import com.google.common.base.Preconditions;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

/**
 * Note: Only the operands that are common to both application directions of
 * this operator are passed to the constructor.
 */
@SuppressWarnings("all")
public abstract class AbstractAttributeMappingOperator<C extends Object, P extends Object> implements IAttributeMappingOperator<C, P> {
  @Extension
  protected final ReactionExecutionState executionState;

  public AbstractAttributeMappingOperator(final ReactionExecutionState executionState) {
    Preconditions.<ReactionExecutionState>checkNotNull(executionState, "executionState is null");
    this.executionState = executionState;
  }
}
