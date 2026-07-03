package tools.vitruv.dsls.reactions.ide.workspace;

/**
 * Holds the single {@link MutableUrlClassLoader} instance shared between
 * {@code ReactionsLanguageIdeModule} (which binds it as the language server's JVM type resolution
 * classloader) and {@link ReactionsClasspathRegistrar} (which adds discovered {@code
 * target/classes} directories to it as the workspace is explored).
 */
public final class MutableUrlClassLoaderHolder {

	public static final MutableUrlClassLoader INSTANCE = new MutableUrlClassLoader(
			MutableUrlClassLoaderHolder.class.getClassLoader());

	private MutableUrlClassLoaderHolder() {
	}

}
