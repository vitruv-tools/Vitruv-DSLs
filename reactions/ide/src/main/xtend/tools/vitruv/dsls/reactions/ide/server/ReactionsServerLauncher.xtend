package tools.vitruv.dsls.reactions.ide.server

import com.google.inject.util.Modules
import org.eclipse.xtext.ide.server.ServerLauncher
import org.eclipse.xtext.ide.server.ServerModule

/**
 * Entry point for the language server jar, replacing the generic
 * {@code org.eclipse.xtext.ide.server.ServerLauncher} so that {@link ReactionsServerModuleOverrides}
 * can be applied to its global Guice module. Configured as the shaded jar's main class in
 * {@code ide/pom.xml}.
 */
class ReactionsServerLauncher {

	def static void main(String[] args) {
		ServerLauncher.launch(typeof(ReactionsServerLauncher).name, args,
			Modules.override(new ServerModule).with(new ReactionsServerModuleOverrides))
	}

}
