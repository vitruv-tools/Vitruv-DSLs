package tools.vitruv.dsls.reactions.ide.workspace;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A {@link URLClassLoader} that can have entries added after construction, so that model
 * projects discovered later in the session (see {@link ReactionsClasspathRegistrar}) become
 * visible to Xbase's JVM type resolution without restarting the language server.
 */
public class MutableUrlClassLoader extends URLClassLoader {

	public MutableUrlClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}

	public void addUrl(URL url) {
		addURL(url);
	}

}
