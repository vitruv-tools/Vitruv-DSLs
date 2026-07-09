package tools.vitruv.dsls.reactions.ide.workspace;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A {@link URLClassLoader} that can have entries added after construction, so that model
 * projects discovered later in the session (see {@link ReactionsClasspathRegistrar}) become
 * visible to Xbase's JVM type resolution without restarting the language server.
 */
public class MutableUrlClassLoader extends URLClassLoader {

  /**
   * Creates a new, empty {@link MutableUrlClassLoader} delegating to {@code parent}.
   *
   * @param parent - the parent {@link ClassLoader}
   */
  public MutableUrlClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }

  /**
   * Adds {@code url} to the set of URLs this class loader resolves classes and resources from.
   *
   * @param url - the {@link URL} to add
   */
  public void addUrl(URL url) {
    addURL(url);
  }

}
