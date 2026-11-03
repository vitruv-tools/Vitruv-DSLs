package tools.vitruv.dsls.reactions.ide.linking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.inject.Injector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.workspace.FileProjectConfig;
import org.eclipse.xtext.workspace.ProjectConfigAdapter;
import org.eclipse.xtext.workspace.WorkspaceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.vitruv.dsls.reactions.ide.ReactionsLanguageIdeSetup;

/**
 * Verifies that a {@code .reactions} file whose metamodel import points at a sibling
 * {@code .ecore} file (as in a typical Maven multi-module project, with the model project and the
 * reactions project as sibling modules) resolves without linking errors, even though the language
 * server has no compiled classpath into that model project. See
 * {@link ReactionsEcoreWorkspaceRegistrar}.
 */
class ReactionsIdeLinkingServiceTest {

  private static final String ECORE_CONTENT = """
      <?xml version="1.0" encoding="UTF-8"?>
      <ecore:EPackage xmi:version="2.0"
          xmlns:xmi="http://www.omg.org/XMI"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
          name="simple" nsURI="http://example.org/test/simple" nsPrefix="simple">
        <eClassifiers xsi:type="ecore:EClass" name="Root">
          <eStructuralFeatures xsi:type="ecore:EAttribute" name="id"
              eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
        </eClassifiers>
      </ecore:EPackage>
      """;

  private static final String REACTIONS_CONTENT = """
      import "http://example.org/test/simple" as simple

      reactions: test
      in reaction to changes in simple
      execute actions in simple

      reaction InsertRootTest {
      \tafter element simple::Root created
      \tcall {
      \t}
      }
      """;

  @Test
  void metamodelImportResolvesAgainstSiblingEcoreFile(@TempDir Path projectRoot)
      throws IOException {
    Files.createDirectory(projectRoot.resolve(".git"));
    Path modelDir = Files.createDirectories(projectRoot.resolve("model"));
    Files.writeString(modelDir.resolve("simple.ecore"), ECORE_CONTENT);
    Path reactionsDir = Files.createDirectories(projectRoot.resolve("reactions"));
    Path reactionsFile = reactionsDir.resolve("test.reactions");
    Files.writeString(reactionsFile, REACTIONS_CONTENT);

    Injector injector = new ReactionsLanguageIdeSetup().createInjectorAndDoEMFRegistration();
    XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
    Resource resource = resourceSet.getResource(URI.createFileURI(reactionsFile.toString()), true);
    EcoreUtil.resolveAll(resourceSet);

    assertEquals(0, resource.getErrors().size(),
        () -> "Unexpected linking errors: " + resource.getErrors());
  }

  /**
   * The repository root is found however deep below it the {@code .reactions} file sits (it used
   * to be searched at most 10 levels up). Uses its own nsURI, since registered packages stay in
   * the global registry across tests.
   */
  @Test
  void metamodelImportResolvesInFileMoreThanTenLevelsBelowRepositoryRoot(
      @TempDir Path projectRoot) throws IOException {
    String nsUri = "http://example.org/test/deep";
    Files.createDirectory(projectRoot.resolve(".git"));
    Path modelDir = Files.createDirectories(projectRoot.resolve("model"));
    Files.writeString(modelDir.resolve("simple.ecore"), withNsUri(ECORE_CONTENT, nsUri));
    Path reactionsDir = Files.createDirectories(
        projectRoot.resolve("consistency/src/main/reactions/a/b/c/d/e/f/g/h/i"));
    Path reactionsFile = reactionsDir.resolve("test.reactions");
    Files.writeString(reactionsFile, withNsUri(REACTIONS_CONTENT, nsUri));

    Injector injector = new ReactionsLanguageIdeSetup().createInjectorAndDoEMFRegistration();
    XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
    Resource resource = resourceSet.getResource(URI.createFileURI(reactionsFile.toString()), true);
    EcoreUtil.resolveAll(resourceSet);

    assertEquals(0, resource.getErrors().size(),
        () -> "Unexpected linking errors: " + resource.getErrors());
  }

  /**
   * Inside the language server, the workspace folders are searched, not the repository: here the
   * metamodel lies in another workspace folder and there is no {@code .git} marker at all.
   */
  @Test
  void metamodelImportResolvesAgainstEcoreFileInAnotherWorkspaceFolder(@TempDir Path workspace)
      throws IOException {
    String nsUri = "http://example.org/test/workspace";
    Path modelFolder = Files.createDirectories(workspace.resolve("models"));
    Files.writeString(modelFolder.resolve("simple.ecore"), withNsUri(ECORE_CONTENT, nsUri));
    Path reactionsFolder = Files.createDirectories(workspace.resolve("reactions-project"));
    Path reactionsFile = Files.createDirectories(reactionsFolder.resolve("src/main/reactions"))
        .resolve("test.reactions");
    Files.writeString(reactionsFile, withNsUri(REACTIONS_CONTENT, nsUri));

    WorkspaceConfig workspaceConfig = new WorkspaceConfig();
    FileProjectConfig reactionsProject =
        new FileProjectConfig(folderUri(reactionsFolder), "reactions-project", workspaceConfig);
    workspaceConfig.addProject(reactionsProject);
    workspaceConfig.addProject(
        new FileProjectConfig(folderUri(modelFolder), "models", workspaceConfig));

    Injector injector = new ReactionsLanguageIdeSetup().createInjectorAndDoEMFRegistration();
    XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
    ProjectConfigAdapter.install(resourceSet, reactionsProject);
    Resource resource = resourceSet.getResource(URI.createFileURI(reactionsFile.toString()), true);
    EcoreUtil.resolveAll(resourceSet);

    assertEquals(0, resource.getErrors().size(),
        () -> "Unexpected linking errors: " + resource.getErrors());
  }

  private static String withNsUri(String content, String nsUri) {
    return content.replace("http://example.org/test/simple", nsUri);
  }

  private static URI folderUri(Path folder) {
    return URI.createFileURI(folder.toString()).appendSegment("");
  }

}
