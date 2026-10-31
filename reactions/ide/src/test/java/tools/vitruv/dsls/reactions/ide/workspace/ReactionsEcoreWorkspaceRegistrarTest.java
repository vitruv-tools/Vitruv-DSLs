package tools.vitruv.dsls.reactions.ide.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies how {@link ReactionsEcoreWorkspaceRegistrar} registers the {@code .ecore} files of the
 * workspace: Java class names from a UML2 genmodel, and which of several files with the same
 * nsURI wins.
 */
class ReactionsEcoreWorkspaceRegistrarTest {

  /**
   * Metamodels imported from UML (e.g. SysML v2) come with a UML2 genmodel. Its classes must get
   * their Java class names like with an EMF genmodel, or every routine parameter typed by them
   * becomes {@code unknown}.
   */
  @Test
  void readsInstanceClassNamesFromUml2GenModel(@TempDir Path root) throws IOException {
    String nsUri = "http://test/uml2genmodel";
    writeEcore(root.resolve("uml.ecore"), nsUri, "Part");
    Files.writeString(root.resolve("uml.genmodel"), """
        <?xml version="1.0" encoding="UTF-8"?>
        <genmodel:GenModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:genmodel="http://www.eclipse.org/uml2/2.2.0/GenModel"
            modelName="Uml" importerID="org.eclipse.uml2.uml.ecore.importer"
            cacheAdapterSupport="true">
          <genPackages xsi:type="genmodel:GenPackage" prefix="Uml" basePackage="org.example"
              ecorePackage="uml.ecore#/">
            <genClasses xsi:type="genmodel:GenClass" ecoreClass="uml.ecore#//Part"/>
          </genPackages>
        </genmodel:GenModel>
        """);

    new ReactionsEcoreWorkspaceRegistrar()
        .ensureRegisteredIn(List.of(URI.createFileURI(root.toString())));

    EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(nsUri);
    assertNotNull(ePackage);
    assertEquals("org.example.test.Part",
        ePackage.getEClassifier("Part").getInstanceClassName());
  }

  /**
   * A copy of a metamodel deeper in the tree (e.g. in a vendored clone) must not replace the one
   * closest to the search root, whatever order the directory listing has.
   */
  @Test
  void fileClosestToRootWinsForSameNsUri(@TempDir Path root) throws IOException {
    String nsUri = "http://test/samensuri";
    writeEcore(root.resolve("model.ecore"), nsUri, "Top");
    for (String copy : List.of("a", "b", "c", "d", "e")) {
      Path dir = Files.createDirectories(root.resolve("vendor/" + copy + "/model"));
      writeEcore(dir.resolve("model.ecore"), nsUri, "Copy");
      writeEcore(dir.resolve("other.ecore"), nsUri, "Copy");
    }

    new ReactionsEcoreWorkspaceRegistrar()
        .ensureRegisteredIn(List.of(URI.createFileURI(root.toString())));

    assertNotNull(EPackage.Registry.INSTANCE.getEPackage(nsUri).getEClassifier("Top"));
  }

  private void writeEcore(Path file, String nsUri, String className) throws IOException {
    Files.writeString(file, """
        <?xml version="1.0" encoding="UTF-8"?>
        <ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore" name="test" nsURI="%s"
            nsPrefix="test">
          <eClassifiers xsi:type="ecore:EClass" name="%s"/>
        </ecore:EPackage>
        """.formatted(nsUri, className));
  }

}
