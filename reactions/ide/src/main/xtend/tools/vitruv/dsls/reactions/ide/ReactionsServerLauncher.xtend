package tools.vitruv.dsls.reactions.ide

import com.google.inject.AbstractModule
import com.google.inject.util.Modules
import org.eclipse.xtext.ide.server.ServerLauncher
import org.eclipse.xtext.ide.server.ServerModule
import org.eclipse.xtext.util.IFileSystemScanner

class ReactionsServerLauncher {
	def static void main(String[] args) {
		val overridden = Modules.override(new ServerModule()).with(new AbstractModule() {
			override configure() {
				bind(IFileSystemScanner).to(BuildOutputSkippingFileSystemScanner)
			}
		})

		ServerLauncher.launch("Reactions LSP", args, overridden)
	}
}
