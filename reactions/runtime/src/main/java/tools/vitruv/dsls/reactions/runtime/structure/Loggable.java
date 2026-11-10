package tools.vitruv.dsls.reactions.runtime.structure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Loggable object provides a {@link Logger} that instances of its subclasses can use.
 */
public class Loggable {
  /**
   * Loggers by class. Looking up a logger is expensive compared to the short-lived routines,
   * routines facades and update blocks that are created for every propagated change.
   */
  private static final ClassValue<Logger> LOGGERS = new ClassValue<>() {
    @Override
    protected Logger computeValue(Class<?> type) {
      return LogManager.getLogger(type);
    }
  };

  /**
   * Returns the logger of this object's class.
   *
   * @return the {@link Logger}
   */
  protected Logger getLogger() {
    return LOGGERS.get(this.getClass());
  }
}
