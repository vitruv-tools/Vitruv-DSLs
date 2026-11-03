package tools.vitruv.dsls.reactions;

import static org.eclipse.emf.ecore.EPackage.Registry.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that {@link ClasspathGenModelRegistrar} registers generated packages described by a
 * genmodel on the classpath. The genmodel describes EMF's own generated packages, so that their
 * interfaces are on the classpath without compiling test fixtures.
 */
class ClasspathGenModelRegistrarTest {
  private static final String NS_URI_PREFIX = "test://classpath-genmodel-registrar/";
  private static final String ROOT_NS_URI = NS_URI_PREFIX + "ecore";
  private static final String WITHOUT_CLASS_NS_URI = NS_URI_PREFIX + "xml";
  private static final String NESTED_NS_URI = NS_URI_PREFIX + "type";
  private static final String PREREGISTERED_NS_URI = NS_URI_PREFIX + "preregistered";

  private static final EPackage.Registry registry = INSTANCE;

  @TempDir Path modelDirectory;

  @AfterEach
  void removeTestPackages() {
    registry.keySet().removeIf(nsUri -> nsUri.startsWith(NS_URI_PREFIX));
  }

  @Test
  void registersPackagesFromGenModel() throws IOException {
    writeModel();
    registerFromModelDirectory();
    assertSame(EcorePackage.eINSTANCE, registry.getEPackage(ROOT_NS_URI));
  }

  @Test
  void registersNestedPackagesAndSkipsMissingClasses() throws IOException {
    writeModel();
    registerFromModelDirectory();
    assertSame(XMLTypePackage.eINSTANCE, registry.getEPackage(NESTED_NS_URI));
    assertFalse(registry.containsKey(WITHOUT_CLASS_NS_URI));
  }

  @Test
  void keepsAlreadyRegisteredPackages() throws IOException {
    registry.put(PREREGISTERED_NS_URI, XMLTypePackage.eINSTANCE);
    writeModel();
    registerFromModelDirectory();
    assertSame(
        XMLTypePackage.eINSTANCE, registry.getEPackage(PREREGISTERED_NS_URI));
  }

  /**
   * A jar without {@code plugin.xml}, such as the language server's shaded jar, holds EMF's own
   * {@code Ecore.genmodel}. Before Ecore registered itself, a lazy descriptor for it made {@code
   * EcoreFactory}'s initialization fail. Runs on fresh copies of the classes, in which Ecore is not
   * initialized yet.
   */
  @Test
  void ecoreStaysUsableWithEcoreGenModelOutsidePluginJar() throws Exception {
    Path ecoreModelDirectory = Files.createDirectories(modelDirectory.resolve("model"));
    for (String file : List.of("Ecore.genmodel", "Ecore.ecore")) {
      try (InputStream in = EcorePackage.class.getResourceAsStream("/model/" + file)) {
        Files.copy(in, ecoreModelDirectory.resolve(file));
      }
    }
    List<URL> urls = new ArrayList<>();
    urls.add(modelDirectory.toUri().toURL());
    for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
      urls.add(Path.of(entry).toUri().toURL());
    }
    try (URLClassLoader classLoader =
        new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader())) {
      classLoader
          .loadClass(ClasspathGenModelRegistrar.class.getName())
          .getMethod("registerGeneratedPackages", ClassLoader.class)
          .invoke(null, classLoader);
      assertNotNull(
          classLoader.loadClass(EcoreFactory.class.getName()).getField("eINSTANCE").get(null));
    }
  }

  private void registerFromModelDirectory() throws IOException {
    try (URLClassLoader classLoader =
        new URLClassLoader(
            new URL[] {modelDirectory.toUri().toURL()}, getClass().getClassLoader())) {
      ClasspathGenModelRegistrar.registerGeneratedPackages(classLoader);
    }
  }

  private void writeModel() throws IOException {
    Path ecoreDirectory = Files.createDirectories(modelDirectory.resolve("src/main/ecore"));
    Files.writeString(
        ecoreDirectory.resolve("test.ecore"),
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
            name="ecore" nsURI="%s" nsPrefix="ecore">
          <eSubpackages name="xml" nsURI="%s" nsPrefix="xml">
            <eSubpackages name="type" nsURI="%s" nsPrefix="type"/>
          </eSubpackages>
        </ecore:EPackage>
        """
            .formatted(ROOT_NS_URI, WITHOUT_CLASS_NS_URI, NESTED_NS_URI));
    Files.writeString(
        ecoreDirectory.resolve("test.genmodel"),
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <genmodel:GenModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:genmodel="http://www.eclipse.org/emf/2002/GenModel"
            modelName="Test">
          <foreignModel>test.ecore</foreignModel>
          <genPackages prefix="Ecore" basePackage="org.eclipse.emf" ecorePackage="test.ecore#/">
            <nestedGenPackages prefix="Xml" ecorePackage="test.ecore#//xml">
              <nestedGenPackages prefix="XMLType">
                <ecorePackage href="test.ecore#//xml/type"/>
              </nestedGenPackages>
            </nestedGenPackages>
          </genPackages>
        </genmodel:GenModel>
        """);
    Files.writeString(
        ecoreDirectory.resolve("preregistered.ecore"),
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
            name="ecore" nsURI="%s" nsPrefix="ecore"/>
        """
            .formatted(PREREGISTERED_NS_URI));
    Files.writeString(
        ecoreDirectory.resolve("preregistered.genmodel"),
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <genmodel:GenModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:genmodel="http://www.eclipse.org/emf/2002/GenModel"
            modelName="Preregistered">
          <genPackages prefix="Ecore" basePackage="org.eclipse.emf" ecorePackage="preregistered.ecore#/"/>
        </genmodel:GenModel>
        """);
  }
}
