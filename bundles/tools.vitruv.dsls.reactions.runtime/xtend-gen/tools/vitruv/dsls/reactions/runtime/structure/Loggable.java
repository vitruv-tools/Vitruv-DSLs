package tools.vitruv.dsls.reactions.runtime.structure;

import org.apache.log4j.Logger;

@SuppressWarnings("all")
public class Loggable {
  private final Logger LOGGER;

  public Loggable() {
    this.LOGGER = Logger.getLogger(this.getClass());
  }

  protected Logger getLogger() {
    return this.LOGGER;
  }
}
