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

  private static final String ECORE_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<ecore:EPackage xmi:version=\"2.0\"\n"
      + "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
      + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
      + "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
      + "    name=\"simple\" nsURI=\"http://example.org/test/simple\" nsPrefix=\"simple\">\n"
      + "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Root\">\n"
      + "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"id\"\n"
      + "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
      + "  </eClassifiers>\n"
      + "</ecore:EPackage>\n";

  private static final String REACTIONS_CONTENT = "import \"http://example.org/test/simple\" "
      + "as simple\n\n"
      + "reactions: test\n"
      + "in reaction to changes in simple\n"
      + "execute actions in simple\n\n"
      + "reaction InsertRootTest {\n"
      + "\tafter element simple::Root created\n"
      + "\tcall {\n"
      + "\t}\n"
      + "}\n";

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

}
