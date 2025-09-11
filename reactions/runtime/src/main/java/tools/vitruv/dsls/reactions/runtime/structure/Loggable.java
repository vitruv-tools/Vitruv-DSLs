package tools.vitruv.dsls.reactions.runtime.structure;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Loggable object provides a {@link Logger} that instances of its subclasses can use.
 */
public class Loggable {
  @Getter(AccessLevel.PROTECTED)
  private final Logger logger;

  /**
   * Creates a new Loggable.
   */
  public Loggable() {
    logger = LogManager.getLogger(this.getClass());
  }
}