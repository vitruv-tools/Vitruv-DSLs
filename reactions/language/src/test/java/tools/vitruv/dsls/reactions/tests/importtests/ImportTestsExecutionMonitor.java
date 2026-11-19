package tools.vitruv.dsls.reactions.tests.importtests;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.xtext.xbase.lib.Pure;
import tools.vitruv.dsls.reactions.tests.ExecutionMonitor;

/**
 * {@link ExecutionMonitor} for the import tests ({@link ImportTests}).
 * Allows the tests to check which reactions and routines of the root segment and of
 * the directly and transitively imported segments were executed, in particular with overriding.
 *
 * <p>Implemented as a singleton monitor shared by all tests,
 * so tests must call {@link #reset()} before they start.
 * It is not thread-safe.
 */
public final class ImportTestsExecutionMonitor
    implements ExecutionMonitor<ImportTestsExecutionMonitor.ExecutionType> {
  /**
   * Steps that can be observed in the import tests.
   *
   * <p>The name of a constant is the name of the reaction or routine that reports it.
   * The prefix names the segment that contains it:
   * {@code ROOT}, {@code DIRECT_SN}, {@code TRANSITIVE_SN} etc.
   * {@code SN} and {@code QN} mean that the segment is imported using simple or qualified names.
   * A name that contains two segment names, such as
   * {@code DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_2}, is a
   * reaction or routine of the second segment that is overridden by the first.
   */
  public enum ExecutionType {
    ROOT_REACTION,
    ROOT_ROUTINE,
    ROOT_DIRECT_SN_OVERRIDDEN_REACTION,
    ROOT_TRANSITIVE_SN_OVERRIDDEN_REACTION,
    ROOT_TRANSITIVE_SN_OVERRIDDEN_REACTION_2,
    ROOT_DIRECT_SN_OVERRIDDEN_ROUTINE,
    ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE,
    ROOT_TRANSITIVE_3_SN_OVERRIDDEN_ROUTINE,
    ROOT_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_2,
    ROOT_COMMON_ROUTINES_ROUTINE_1,
    ROOT_COMMON_ROUTINES_ROUTINE_2,
    ROOT_COMMON_ROUTINES_ROUTINE_3,
    ROOT_COMMON_ROUTINES_2_ROUTINE_3,

    DIRECT_SN_REACTION,
    DIRECT_SN_ROUTINE,
    DIRECT_SN_INNER_ROUTINE,
    DIRECT_SN_OVERRIDDEN_REACTION,
    DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_REACTION_2,
    DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_REACTION_3,
    DIRECT_SN_OVERRIDDEN_ROUTINE,
    DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_2,
    DIRECT_SN_TRANSITIVE_SN_OVERRIDDEN_ROUTINE_3,

    DIRECT_2_SN_REACTION,
    DIRECT_2_SN_ROUTINE,

    TRANSITIVE_SN_REACTION,
    TRANSITIVE_SN_ROUTINE,
    TRANSITIVE_SN_INNER_ROUTINE,
    TRANSITIVE_SN_OVERRIDDEN_REACTION,
    TRANSITIVE_SN_OVERRIDDEN_REACTION_2,
    TRANSITIVE_SN_OVERRIDDEN_REACTION_3,
    TRANSITIVE_SN_OVERRIDDEN_ROUTINE,
    TRANSITIVE_SN_OVERRIDDEN_ROUTINE_2,
    TRANSITIVE_SN_OVERRIDDEN_ROUTINE_3,

    TRANSITIVE_2_SN_REACTION,
    TRANSITIVE_2_SN_ROUTINE,

    TRANSITIVE_3_SN_REACTION,
    TRANSITIVE_3_SN_ROUTINE,
    TRANSITIVE_3_SN_OVERRIDDEN_ROUTINE,

    DIRECT_ROUTINES_QN_REACTION,
    DIRECT_ROUTINES_QN_ROUTINE,

    TRANSITIVE_ROUTINES_SN_REACTION,
    TRANSITIVE_ROUTINES_SN_ROUTINE,

    TRANSITIVE_ROUTINES_QN_REACTION,
    TRANSITIVE_ROUTINES_QN_ROUTINE,

    COMMON_ROUTINES_ROUTINE_1,
    COMMON_ROUTINES_ROUTINE_2,
    COMMON_ROUTINES_ROUTINE_3
  }

  private final EnumSet<ImportTestsExecutionMonitor.ExecutionType> values =
      EnumSet.noneOf(ImportTestsExecutionMonitor.ExecutionType.class);

  private static final ImportTestsExecutionMonitor instance = new ImportTestsExecutionMonitor();

  private ImportTestsExecutionMonitor() {
  }

  /**
   * {@inheritDoc}
   *
   * @return a copy of the observed executions; later changes of the monitor do not affect it
   */
  @Override
  public Set<ImportTestsExecutionMonitor.ExecutionType> getObservedExecutions() {
    return values.clone();
  }

  /**
   * Records that the given execution happened.
   *
   * @param type {@link ExecutionType}
   */
  public void set(ImportTestsExecutionMonitor.ExecutionType type) {
    values.add(type);
  }

  /**
   * Records that all given executions happened.
   *
   * @param types {@link ExecutionType}
   */
  public void setAll(ImportTestsExecutionMonitor.ExecutionType... types) {
    Collections.addAll(values, types);
  }

  /**
   * Forgets all observed executions.
   */
  public void reset() {
    values.clear();
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof ImportTestsExecutionMonitor importTestsExecutionMonitor) {
      return this.values.equals(importTestsExecutionMonitor.values);
    }
    return false;
  }

  @Override
  public String toString() {
    return values.toString();
  }

  /**
   * Returns the singleton monitor.
   *
   * @return the shared {@link ImportTestsExecutionMonitor}
   */
  @Pure
  public static ImportTestsExecutionMonitor getInstance() {
    return ImportTestsExecutionMonitor.instance;
  }
}
