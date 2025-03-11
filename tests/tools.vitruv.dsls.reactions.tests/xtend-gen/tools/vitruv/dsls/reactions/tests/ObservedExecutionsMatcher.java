package tools.vitruv.dsls.reactions.tests;

import com.google.common.base.Objects;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@FinalFieldsConstructor
@SuppressWarnings("all")
class ObservedExecutionsMatcher<ExecutionType extends Enum<ExecutionType>> extends TypeSafeMatcher<ExecutionMonitor<ExecutionType>> {
  private final Set<ExecutionType> expectedExecutions;

  @Override
  protected boolean matchesSafely(final ExecutionMonitor<ExecutionType> item) {
    Set<ExecutionType> _observedExecutions = item.getObservedExecutions();
    return Objects.equal(_observedExecutions, this.expectedExecutions);
  }

  @Override
  public void describeTo(final Description description) {
    description.appendText("the execution monitor to have observed ").<ExecutionType>appendValueList("[", ", ", "]", 
      this.expectedExecutions);
  }

  @Override
  public void describeMismatchSafely(final ExecutionMonitor<ExecutionType> item, final Description mismatchDescription) {
    Set<ExecutionType> occuredButShouldNot = null;
    Set<ExecutionType> occuredNotButShould = null;
    boolean _isEmpty = item.getObservedExecutions().isEmpty();
    if (_isEmpty) {
      occuredButShouldNot = Collections.<ExecutionType>emptySet();
      occuredNotButShould = this.expectedExecutions;
    } else {
      Class<? extends Enum> _class = (((ExecutionType[])Conversions.unwrapArray(item.getObservedExecutions(), Enum.class))[0]).getClass();
      final Class<ExecutionType> itemClass = ((Class<ExecutionType>) _class);
      occuredButShouldNot = EnumSet.<ExecutionType>noneOf(itemClass);
      occuredNotButShould = EnumSet.<ExecutionType>noneOf(itemClass);
      EnumSet<ExecutionType> _allOf = EnumSet.<ExecutionType>allOf(itemClass);
      for (final ExecutionType execution : _allOf) {
        if ((this.expectedExecutions.contains(execution) && (!item.getObservedExecutions().contains(execution)))) {
          occuredNotButShould.add(execution);
        } else {
          if ((item.getObservedExecutions().contains(execution) && (!this.expectedExecutions.contains(execution)))) {
            occuredButShouldNot.add(execution);
          }
        }
      }
    }
    boolean _isEmpty_1 = occuredNotButShould.isEmpty();
    boolean _not = (!_isEmpty_1);
    if (_not) {
      mismatchDescription.appendText("these expected executions were missing: ").<ExecutionType>appendValueList("[", ", ", "]", occuredNotButShould);
    }
    boolean _isEmpty_2 = occuredButShouldNot.isEmpty();
    boolean _not_1 = (!_isEmpty_2);
    if (_not_1) {
      boolean _isEmpty_3 = occuredNotButShould.isEmpty();
      boolean _not_2 = (!_isEmpty_3);
      if (_not_2) {
        mismatchDescription.appendText(" and ");
      }
      mismatchDescription.appendText("these observed executions were not expected: ").<ExecutionType>appendValueList("[", ", ", 
        "]", occuredButShouldNot);
    }
  }

  public ObservedExecutionsMatcher(final Set<ExecutionType> expectedExecutions) {
    super();
    this.expectedExecutions = expectedExecutions;
  }
}
