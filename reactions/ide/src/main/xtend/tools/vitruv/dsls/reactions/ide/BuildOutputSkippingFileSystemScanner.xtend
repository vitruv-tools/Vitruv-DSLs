package tools.vitruv.dsls.reactions.ide

import com.google.inject.Singleton
import java.io.File
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.util.IAcceptor
import org.eclipse.xtext.util.IFileSystemScanner

 /**
  * File system scanner that skips build outputs for different ides. This is required, so only a unique file within {@code src/main/reactions/} and {@code target/classes/} is indexed by the LSP, to prevent duplicate type errors.
  */
@Singleton
class BuildOutputSkippingFileSystemScanner extends IFileSystemScanner.JavaIoFileSystemScanner {
	static val SKIPPED_DIRECTORIES = #{
		"target",
		"build",
		"bin",
		"out",
		"node_modules",
		".git",
		".gradle",
		".idea",
		".vscode",
		".settings"
	}

	override scanRec(File file, IAcceptor<URI> acceptor) {
		if (file.directory && SKIPPED_DIRECTORIES.contains(file.name)) {
			return
		}

		super.scanRec(file, acceptor)
	}
}
