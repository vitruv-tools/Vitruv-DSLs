package tools.vitruv.dsls.reactions.tests.simplechangestests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;


/**
 * A {@link ExecutionMonitor} for the simple change tests ({@link SimpleChangesTests} and
 * {@link SimpleChangesExplicitTests}).
 *
 * <p>Implemented as a singleton monitor shared by all tests,
 * so tests must call {@link #reset()} before they start.
 * It is not thread-safe.
 */
public final class SimpleChangesTestsExecutionMonitor
    implements ExecutionMonitor<SimpleChangesTestsExecutionMonitor.ChangeType> {
  /**
   * Kinds of changes a reaction can report.
   * The name of a constant is the name of the change that triggers the reaction.
   */
  public enum ChangeType {
    CREATE_E_OBJECT,
    DELETE_E_OBJECT,

    UPDATE_SINGLE_VALUED_PRIMITVE_TYPE_E_ATTRIBUTE,
    UPDATE_SINGLE_VALUED_E_ATTRIBUTE,
    UNSET_E_ATTRIBUTE,

    CREATE_NON_ROOT_E_OBJECT_SINGLE,
    DELETE_NON_ROOT_E_OBJECT_SINGLE,
    CREATE_NON_ROOT_E_OBJECT_IN_LIST,
    DELETE_NON_ROOT_E_OBJECT_IN_LIST,

    INSERT_E_ATTRIBUTE_VALUE,
    REMOVE_E_ATTRIBUTE_VALUE,
    REPLACE_E_ATTRIBUTE_VALUE,

    INSERT_NON_CONTAINMENT_E_REFERENCE,
    REMOVE_NON_CONTAINMENT_E_REFERENCE,
    PERMUTE_NON_CONTAINMENT_E_REFERENCE,
    REPLACE_NON_CONTAINMENT_E_REFERENCE,

    UPDATE_SINGLE_VALUED_NON_CONTAINMENT_E_REFERENCE,
    UNSET_NON_CONTAINMENT_E_REFERENCE,

    SIZE
  }

  public static final SimpleChangesTestsExecutionMonitor instance
      = new SimpleChangesTestsExecutionMonitor();

  private final List<ChangeType> values =
      new ArrayList<>(ChangeType.values().length);

  private SimpleChangesTestsExecutionMonitor() {
  }

  /**
   * Records that a reaction to the given change was executed.
   *
   * @param type the observed change type
   * @return {@code true} if the change type was not observed before
   */
  public boolean set(final ChangeType type) {
    return values.add(type);
  }

  /**
   * Forgets all observed executions.
   */
  public void reset() {
    this.values.clear();
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof SimpleChangesTestsExecutionMonitor executionMonitor) {
      return executionMonitor.values.equals(this.values);
    }
    return false;
  }

  public List<SimpleChangesTestsExecutionMonitor.ChangeType> getOrderedObservedExecutions() {
    return List.copyOf(values);
  }

  @Override
  public Set<SimpleChangesTestsExecutionMonitor.ChangeType> getObservedExecutions() {
    return new HashSet<>(values);
  }
}
