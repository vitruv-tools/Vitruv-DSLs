package tools.vitruv.dsls.reactions.ide.workspace

import java.io.File
import org.eclipse.emf.common.util.URI

/**
 * Shared logic for finding the repository root of a linked file, used both by
 * {@link ReactionsEcoreWorkspaceRegistrar} (to find {@code .ecore} files) and
 * {@link ReactionsClasspathRegistrar} (to find {@code target/classes} directories).
 */
final class ReactionsWorkspaceRoots {
	static final int MAX_LEVELS_UP = 10

	private new() {
	}

	/**
	 * Walks up from {@code resourceUri} looking for a {@code .git} directory, which marks the
	 * repository root in the layout this is designed for (a Maven multi-module repo with the
	 * model project and the reactions project as sibling modules). Deliberately does not fall
	 * back to some arbitrarily high ancestor when no such marker is found, since that could mean
	 * recursively scanning a huge, unrelated part of the file system.
	 */
	def static URI findRepoRoot(URI resourceUri) {
		if (resourceUri === null || !resourceUri.isFile) {
			return null;
		}
		val start = new File(resourceUri.toFileString).parentFile
		if (start === null) {
			return null;
		}
		var probe = start
		var levels = 0
		while (probe !== null && levels < MAX_LEVELS_UP) {
			if (new File(probe, ".git").exists) {
				return URI.createFileURI(probe.absolutePath)
			}
			probe = probe.parentFile
			levels = levels + 1
		}
		return null;
	}

}
