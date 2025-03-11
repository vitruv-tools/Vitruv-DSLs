package tools.vitruv.dsls.reactions.tests.importTests;

import com.google.common.collect.Iterables;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;

@SuppressWarnings("all")
public final class ImportTestsExecutionMonitor implements ExecutionMonitor<ImportTestsExecutionMonitor.ExecutionType> {
  public enum ExecutionType {
    RootReaction,

    RootRoutine,

    RootDirectSNOverriddenReaction,

    RootTransitiveSNOverriddenReaction,

    RootTransitiveSNOverriddenReaction2,

    RootDirectSNOverriddenRoutine,

    RootTransitiveSNOverriddenRoutine,

    RootTransitive3SNOverriddenRoutine,

    RootTransitiveSNOverriddenRoutine2,

    RootCommonRoutinesRoutine1,

    RootCommonRoutinesRoutine2,

    RootCommonRoutinesRoutine3,

    RootCommonRoutines2Routine3,

    DirectSNReaction,

    DirectSNRoutine,

    DirectSNInnerRoutine,

    DirectSNOverriddenReaction,

    DirectSNTransitiveSNOverriddenReaction2,

    DirectSNTransitiveSNOverriddenReaction3,

    DirectSNOverriddenRoutine,

    DirectSNTransitiveSNOverriddenRoutine2,

    DirectSNTransitiveSNOverriddenRoutine3,

    Direct2SNReaction,

    Direct2SNRoutine,

    TransitiveSNReaction,

    TransitiveSNRoutine,

    TransitiveSNInnerRoutine,

    TransitiveSNOverriddenReaction,

    TransitiveSNOverriddenReaction2,

    TransitiveSNOverriddenReaction3,

    TransitiveSNOverriddenRoutine,

    TransitiveSNOverriddenRoutine2,

    TransitiveSNOverriddenRoutine3,

    Transitive2SNReaction,

    Transitive2SNRoutine,

    Transitive3SNReaction,

    Transitive3SNRoutine,

    Transitive3SNOverriddenRoutine,

    DirectRoutinesQNReaction,

    DirectRoutinesQNRoutine,

    TransitiveRoutinesSNReaction,

    TransitiveRoutinesSNRoutine,

    TransitiveRoutinesQNReaction,

    TransitiveRoutinesQNRoutine,

    CommonRoutinesRoutine1,

    CommonRoutinesRoutine2,

    CommonRoutinesRoutine3;
  }

  private final EnumSet<ImportTestsExecutionMonitor.ExecutionType> values = EnumSet.<ImportTestsExecutionMonitor.ExecutionType>noneOf(ImportTestsExecutionMonitor.ExecutionType.class);

  @Accessors
  private static final ImportTestsExecutionMonitor instance = new ImportTestsExecutionMonitor();

  private ImportTestsExecutionMonitor() {
  }

  @Override
  public Set<ImportTestsExecutionMonitor.ExecutionType> getObservedExecutions() {
    return this.values;
  }

  public void set(final ImportTestsExecutionMonitor.ExecutionType type) {
    this.values.add(type);
  }

  public void setAll(final ImportTestsExecutionMonitor.ExecutionType... types) {
    Iterables.<ImportTestsExecutionMonitor.ExecutionType>addAll(this.values, ((Iterable<? extends ImportTestsExecutionMonitor.ExecutionType>)Conversions.doWrapArray(types)));
  }

  public void reset() {
    this.values.clear();
  }

  @Override
  public boolean equals(final Object object) {
    if ((object instanceof ImportTestsExecutionMonitor)) {
      return this.values.equals(((ImportTestsExecutionMonitor)object).values);
    }
    return false;
  }

  @Override
  public String toString() {
    return this.values.toString();
  }

  @Pure
  public static ImportTestsExecutionMonitor getInstance() {
    return ImportTestsExecutionMonitor.instance;
  }
}
