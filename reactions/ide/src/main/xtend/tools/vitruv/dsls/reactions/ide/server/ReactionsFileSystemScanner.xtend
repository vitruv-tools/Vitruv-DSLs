package tools.vitruv.dsls.reactions.ide.server

import com.google.inject.Inject
import java.io.File
import java.nio.file.Paths
import java.util.List
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.util.IAcceptor
import org.eclipse.xtext.util.IFileSystemScanner
import org.eclipse.xtext.util.UriExtensions

/**
 * Replaces the default {@link IFileSystemScanner.JavaIoFileSystemScanner} for the language
 * server's whole-project resource discovery, skipping common build-output and VCS directories.
 * <p>
 * Without this, a Maven build that copies {@code .reactions} source files into
 * {@code target/classes} (a common resource pattern in this ecosystem) causes the language server
 * to index both the original file and its build-output copy as separate resources, which then
 * triggers spurious "already defined" duplicate-type errors between them.
 */
class ReactionsFileSystemScanner implements IFileSystemScanner {
	static final List<String> EXCLUDED_SEGMENTS = #["target", "bin", "build", "node_modules", ".git"]

	@Inject UriExtensions uriExtensions

	override void scan(URI root, IAcceptor<URI> acceptor) {
		scanRec(new File(root.toFileString), acceptor)
	}

	def private void scanRec(File file, IAcceptor<URI> acceptor) {
		if (file.directory && EXCLUDED_SEGMENTS.contains(file.name)) {
			return;
		}
		val path = Paths.get(file.absoluteFile.toURI)
		acceptor.accept(uriExtensions.toEmfUri(path.toUri))
		if (file.directory) {
			val children = file.listFiles
			if (children !== null) {
				for (child : children) {
					scanRec(child, acceptor)
				}
			}
		}
	}

}
