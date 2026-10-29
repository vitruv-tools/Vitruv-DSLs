package tools.vitruv.dsls.reactions.ide.server

import java.util.Set
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.xtext.ide.server.MultiRootWorkspaceConfigFactory
import org.eclipse.xtext.workspace.FileProjectConfig
import org.eclipse.xtext.workspace.WorkspaceConfig

/**
 * Creates one project per workspace folder, as {@link MultiRootWorkspaceConfigFactory} does, but
 * links each project to the workspace configuration it belongs to. The default factory leaves
 * {@code IProjectConfig.getWorkspaceConfig()} {@code null}, so a linked file could not see the
 * other workspace folders, which
 * {@link tools.vitruv.dsls.reactions.ide.workspace.ReactionsWorkspaceRoots} searches for
 * metamodels.
 */
class ReactionsWorkspaceConfigFactory extends MultiRootWorkspaceConfigFactory {

	override protected void addProjectsForWorkspaceFolder(WorkspaceConfig workspaceConfig,
		WorkspaceFolder workspaceFolder, Set<String> existingNames) {
		if (workspaceFolder?.uri !== null) {
			val project = new FileProjectConfig(getUriExtensions().toUri(workspaceFolder.uri),
				getUniqueProjectName(workspaceFolder.name, existingNames), workspaceConfig)
			project.addSourceFolder(".")
			workspaceConfig.addProject(project)
		}
	}

}
