package tools.vitruv.dsls.reactions.tests;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.hamcrest.Matcher;

/**
 * Records the executed steps of a reactions test, so that tests can assert on them. Reaction
 * and routine code in the {@code .reactions} test resources reports each step it performs to such
 * a monitor, usually through a singleton instance. The monitor tracks the reported steps as a set
 * of constants; order and the number of repetitions are not recorded.
 *
 * <p>Tests compare the recorded set with the expected one using the matchers from
 * {@link #observedExecutions(Enum...)} and {@link #observedExecutions(Set)}.
 *
 * @param <ExecutionType> enum whose constants identify the steps that can be observed
 */
public interface ExecutionMonitor<ExecutionType extends Enum<ExecutionType>> {
  /**
   * Returns the executions that have been reported to this monitor.
   *
   * @return {@link Set} of the observed execution types
   */
  Set<ExecutionType> getObservedExecutions();

  /**
   * Builds a {@link Matcher} that expects that exactly the executions in {@code types} happen.
   *
   * @param types {@link Set} of ExecutionTypes
   * @param <ExecutionType> generic value
   * @return new {@link Matcher}
   */
  static <ExecutionType extends Enum<ExecutionType>>
      Matcher<ExecutionMonitor<ExecutionType>> observedExecutions(ExecutionType... types) {
    if (types.length == 0) {
      return new ObservedExecutionsMatcher<ExecutionType>(Collections.emptySet());
    } else {
      return new ObservedExecutionsMatcher<>(EnumSet.of(types[0], types));
    }
  }

  /**
   * Builds a {@link Matcher} that expects that exactly the executions in {@code types} happen.
   *
   * @param types {@link Set} of ExecutionTypes
   * @param <ExecutionType> generic value
   * @return new {@link Matcher}
   */
  static <ExecutionType extends Enum<ExecutionType>> Matcher<ExecutionMonitor<ExecutionType>>
      observedExecutions(Set<ExecutionType> types) {
    return new ObservedExecutionsMatcher<>(types);
  }
}
