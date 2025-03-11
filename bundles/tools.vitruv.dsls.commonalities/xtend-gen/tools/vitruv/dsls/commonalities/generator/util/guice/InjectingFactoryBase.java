package tools.vitruv.dsls.commonalities.generator.util.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Base class for factories or providers which need to invoke the Guice
 * injection for objects created by them.
 */
@SuppressWarnings("all")
public abstract class InjectingFactoryBase {
  @Inject
  private Injector injector;

  public InjectingFactoryBase() {
  }

  protected <T extends Object> T injectMembers(final T object) {
    this.injector.injectMembers(object);
    return object;
  }
}
