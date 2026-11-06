package tools.vitruv.dsls.reactions.tests;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.hamcrest.Matcher;

public interface ExecutionMonitor<ExecutionType extends Enum<ExecutionType>> {
  Set<ExecutionType> getObservedExecutions();

  static <ExecutionType extends Enum<ExecutionType>>
    Matcher<ExecutionMonitor<ExecutionType>> observedExecutions(ExecutionType... types) {
    if (types.length == 0) {
      return new ObservedExecutionsMatcher<ExecutionType>(Collections.emptySet());
    } else {
      return new ObservedExecutionsMatcher<>(EnumSet.of(types[0], types));
    }
  }

  static <ExecutionType extends Enum<ExecutionType>> Matcher<ExecutionMonitor<ExecutionType>>
    observedExecutions(Set<ExecutionType> types) {
    return new ObservedExecutionsMatcher<>(types);
  }
}
