package tools.vitruv.dsls.reactions.runtime.structure;

import lombok.AccessLevel;
import lombok.Getter;
import tools.vitruv.dsls.reactions.runtime.reactions.Reaction;
import tools.vitruv.dsls.reactions.runtime.routines.Routine;

/**
 * A CallHierarchyHaving object can be used to trace the calling hierarchy of {@link Reaction}s
 * and {@link Routine}s at runtime.
 */
public abstract class CallHierarchyHaving extends Loggable {
  @Getter(AccessLevel.PROTECTED) 
  private final CallHierarchyHaving calledBy;

  /**
   * Creates a new CallHierarchyHaving object without caller.
   */
  protected CallHierarchyHaving() {
    calledBy = null;
  }

  /**
   * Creates a new CallHierarchyHaving object with calledBy as caller.
   *
   * @param calledBy - CallHierarchyHaving
   */
  protected CallHierarchyHaving(CallHierarchyHaving calledBy) {
    this.calledBy = calledBy;
  }

  /**
   * Prints the calling hierarchy, that is, the actual class name of the {@link CallHierarchyHaving}
   * instance, plus the underlying hierarchy of calledBy.
   *
   * @return String
   */
  public String getCalledByString() {
    var builder = new StringBuilder(this.getClass().getSimpleName());
    if (calledBy != null) {
      builder.append(" called by ");
      builder.append(calledBy.toString());
    }
    return builder.toString();
  }
}