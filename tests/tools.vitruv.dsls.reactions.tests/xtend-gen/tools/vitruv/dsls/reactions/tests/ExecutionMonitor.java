package tools.vitruv.dsls.reactions.tests;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.hamcrest.Matcher;

@SuppressWarnings("all")
public interface ExecutionMonitor<ExecutionType extends Enum<ExecutionType>> {
  Set<ExecutionType> getObservedExecutions();

  static <ExecutionType extends Enum<ExecutionType>> Matcher<ExecutionMonitor<ExecutionType>> observedExecutions(final ExecutionType... types) {
    Set<ExecutionType> _xifexpression = null;
    int _length = types.length;
    boolean _equals = (_length == 0);
    if (_equals) {
      _xifexpression = Collections.<ExecutionType>emptySet();
    } else {
      _xifexpression = EnumSet.<ExecutionType>of(types[0], types);
    }
    return new ObservedExecutionsMatcher<ExecutionType>(_xifexpression);
  }

  static <ExecutionType extends Enum<ExecutionType>> Matcher<ExecutionMonitor<ExecutionType>> observedExecutions(final Set<ExecutionType> types) {
    return new ObservedExecutionsMatcher<ExecutionType>(types);
  }
}
