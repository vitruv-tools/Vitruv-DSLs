package tools.vitruv.dsls.reactions.ide.server

import com.google.inject.AbstractModule
import org.eclipse.xtext.ide.server.LanguageServerImpl
import org.eclipse.xtext.util.IFileSystemScanner

/**
 * Overrides bindings of the generic, language-agnostic {@code org.eclipse.xtext.ide.server.ServerModule}
 * that this language's own IDE Guice module ({@code ReactionsLanguageIdeModule}) cannot reach,
 * since {@code ProjectManager}/{@code WorkspaceManager} and their dependencies are constructed
 * from a separate injector built solely from {@code ServerModule}. See
 * {@link ReactionsServerLauncher}.
 */
class ReactionsServerModuleOverrides extends AbstractModule {

	override protected void configure() {
		bind(IFileSystemScanner).to(ReactionsFileSystemScanner)
		// ServerLauncher injects the concrete LanguageServerImpl type (not the LanguageServer
		// interface that ServerModule itself binds it to), so that is the key we must override.
		bind(LanguageServerImpl).to(ReactionsLanguageServerImpl)
	}

}
