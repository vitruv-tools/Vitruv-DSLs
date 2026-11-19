package tools.vitruv.dsls.reactions.tests;

import static java.util.EnumSet.allOf;

import java.util.HashSet;
import java.util.Set;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A {@link TypeSafeMatcher} for {@link ExecutionMonitor}s. It matches if the monitor
 * has observed exactly the expected executions: missing or additional executions are
 * mismatches. Reports the missing and the unexpected executions separately.
 *
 * <p>Create instances with {@link ExecutionMonitor#observedExecutions(Enum...)} or
 * {@link ExecutionMonitor#observedExecutions(Set)}.
 *
 * @param <ExecutionType> enum whose constants identify the observable executions
 */
class ObservedExecutionsMatcher<ExecutionType extends Enum<ExecutionType>> extends
    TypeSafeMatcher<ExecutionMonitor<ExecutionType>> {
  private final Set<ExecutionType> expectedExecutions;

  /**
   * Creates a matcher for {@code expectedExecutions}.
   *
   * @param expectedExecutions {@link Set}
   */
  ObservedExecutionsMatcher(Set<ExecutionType> expectedExecutions) {
    this.expectedExecutions = expectedExecutions;
  }

  @Override
  protected boolean matchesSafely(ExecutionMonitor<ExecutionType> item) {
    return item.getObservedExecutions().equals(expectedExecutions);
  }

  @Override
  public void describeTo(Description description) {
    description
        .appendText("the execution monitor to have observed ")
        .appendValueList("[", ", ", "]", expectedExecutions);
  }

  @Override
  public void describeMismatchSafely(
      ExecutionMonitor<ExecutionType> item,
      Description mismatchDescription) {
    Set<ExecutionType> occuredButShouldNot = null;
    Set<ExecutionType> occuredNotButShould = null;

    if (item.getObservedExecutions().isEmpty()) {
      occuredButShouldNot = Set.of();
      occuredNotButShould = expectedExecutions;
    } else {
      Class<ExecutionType> itemClass =
          (Class<ExecutionType>) item.getObservedExecutions().iterator().next().getClass();
      occuredButShouldNot = new HashSet<>();
      occuredNotButShould = new HashSet<>();

      for (var execution : allOf(itemClass)) {
        if (expectedExecutions.contains(execution)
            && !item.getObservedExecutions().contains(execution)) {
          occuredNotButShould.add(execution);
        } else if (item.getObservedExecutions().contains(execution)
            && !expectedExecutions.contains(execution)) {
          occuredButShouldNot.add(execution);
        }
      }
    }
    if (!occuredNotButShould.isEmpty()) {
      mismatchDescription
          .appendText("these expected executions were missing: ")
          .appendValueList("[", ",", "]", occuredNotButShould);
    }
    if (!occuredButShouldNot.isEmpty()) {
      if (!occuredNotButShould.isEmpty()) {
        mismatchDescription.appendText(" and ");
      }
      mismatchDescription
          .appendText("these observed executions were not expected: ")
          .appendValueList("[", ",", "]", occuredButShouldNot);
    }
  }
}
