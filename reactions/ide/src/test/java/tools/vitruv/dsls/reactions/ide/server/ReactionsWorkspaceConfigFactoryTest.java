package tools.vitruv.dsls.reactions.ide.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.inject.Guice;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.IWorkspaceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that every project created by {@link ReactionsWorkspaceConfigFactory} knows its
 * workspace configuration, so that the other workspace folders can be found from a linked file.
 */
class ReactionsWorkspaceConfigFactoryTest {

  @Test
  void projectsKnowTheirWorkspace(@TempDir Path workspace) {
    List<WorkspaceFolder> folders = List.of(
        new WorkspaceFolder(workspace.resolve("models").toUri().toString(), "models"),
        new WorkspaceFolder(workspace.resolve("reactions").toUri().toString(), "reactions"));

    IWorkspaceConfig workspaceConfig = Guice.createInjector()
        .getInstance(ReactionsWorkspaceConfigFactory.class).getWorkspaceConfig(folders);

    // besides the folders' projects, Xtext's WorkspaceConfig always holds a path-less "unknown" project
    List<IProjectConfig> folderProjects = workspaceConfig.getProjects().stream()
        .filter(project -> project.getPath() != null).map(IProjectConfig.class::cast).toList();
    assertEquals(2, folderProjects.size());
    folderProjects.forEach(project -> assertSame(workspaceConfig, project.getWorkspaceConfig()));
  }

}
