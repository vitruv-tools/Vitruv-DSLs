package tools.vitruv.dsls.reactions.ide.workspace

import java.io.File
import java.util.List
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.workspace.ProjectConfigAdapter

/**
 * Shared logic for finding the folders to search near a linked file, used both by
 * {@link ReactionsEcoreWorkspaceRegistrar} (to find {@code .ecore} files) and
 * {@link ReactionsClasspathRegistrar} (to find {@code target/classes} directories).
 */
final class ReactionsWorkspaceRoots {

	private new() {
	}

	/**
	 * Returns the folders to search when linking {@code resource}: the language server's workspace
	 * folders (the projects of the Xtext workspace configuration installed on the resource's
	 * resource set), so that metamodels in any workspace folder resolve, however deep the file
	 * sits. Falls back to {@link #findRepoRoot} when the resource belongs to no workspace project,
	 * e.g. when no workspace configuration is installed or the file lies outside all workspace
	 * folders.
	 */
	def static List<URI> findSearchRoots(Resource resource) {
		val resourceUri = resource?.URI
		val resourceSet = resource?.resourceSet
		val workspace = if (resourceSet !== null) {
			ProjectConfigAdapter.findInEmfObject(resourceSet)?.projectConfig?.workspaceConfig
		}
		if (workspace !== null && resourceUri !== null && workspace.findProjectContaining(resourceUri) !== null) {
			val roots = workspace.projects.map[path].filter[it !== null && isFile].toList
			if (!roots.empty) {
				return roots
			}
		}
		return findRepoRoots(resourceUri)
	}

	/**
	 * The repository root of {@code resourceUri} (see {@link #findRepoRoot}) as a list, empty if
	 * there is none.
	 */
	def static List<URI> findRepoRoots(URI resourceUri) {
		val root = findRepoRoot(resourceUri)
		return if (root === null) #[] else #[root]
	}

	/**
	 * Walks up from {@code resourceUri} looking for a {@code .git} directory, which marks the
	 * repository root in the layout this is designed for (a Maven multi-module repo with the
	 * model project and the reactions project as sibling modules). Deliberately does not fall
	 * back to some arbitrarily high ancestor when no such marker is found, since that could mean
	 * recursively scanning a huge, unrelated part of the file system. The walk itself has no
	 * depth limit, since it only checks for the marker on the way up.
	 */
	def static URI findRepoRoot(URI resourceUri) {
		if (resourceUri === null || !resourceUri.isFile) {
			return null;
		}
		var probe = new File(resourceUri.toFileString).parentFile
		while (probe !== null) {
			if (new File(probe, ".git").exists) {
				return URI.createFileURI(probe.absolutePath)
			}
			probe = probe.parentFile
		}
		return null;
	}

}
