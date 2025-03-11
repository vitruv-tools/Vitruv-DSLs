package tools.vitruv.dsls.reactions.tests.simpleChangesTests;

import java.util.EnumSet;
import java.util.Set;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;

@SuppressWarnings("all")
public final class SimpleChangesTestsExecutionMonitor implements ExecutionMonitor<SimpleChangesTestsExecutionMonitor.ChangeType> {
  public enum ChangeType {
    CreateEObject,

    DeleteEObject,

    UpdateSingleValuedPrimitveTypeEAttribute,

    UpdateSingleValuedEAttribute,

    UnsetEAttribute,

    CreateNonRootEObjectSingle,

    DeleteNonRootEObjectSingle,

    CreateNonRootEObjectInList,

    DeleteNonRootEObjectInList,

    InsertEAttributeValue,

    RemoveEAttributeValue,

    ReplaceEAttributeValue,

    InsertNonContainmentEReference,

    RemoveNonContainmentEReference,

    PermuteNonContainmentEReference,

    ReplaceNonContainmentEReference,

    UpdateSingleValuedNonContainmentEReference,

    UnsetNonContainmentEReference,

    Size;
  }

  public static final SimpleChangesTestsExecutionMonitor instance = new SimpleChangesTestsExecutionMonitor();

  private final EnumSet<SimpleChangesTestsExecutionMonitor.ChangeType> values = EnumSet.<SimpleChangesTestsExecutionMonitor.ChangeType>noneOf(SimpleChangesTestsExecutionMonitor.ChangeType.class);

  private SimpleChangesTestsExecutionMonitor() {
  }

  public boolean set(final SimpleChangesTestsExecutionMonitor.ChangeType type) {
    return this.values.add(type);
  }

  public void reset() {
    this.values.clear();
  }

  @Override
  public boolean equals(final Object object) {
    if ((object instanceof SimpleChangesTestsExecutionMonitor)) {
      final SimpleChangesTestsExecutionMonitor monitor = ((SimpleChangesTestsExecutionMonitor)object);
      return monitor.values.equals(this.values);
    }
    return false;
  }

  @Override
  public Set<SimpleChangesTestsExecutionMonitor.ChangeType> getObservedExecutions() {
    return this.values;
  }
}
