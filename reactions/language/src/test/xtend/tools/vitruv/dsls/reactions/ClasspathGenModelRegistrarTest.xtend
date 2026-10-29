package tools.vitruv.dsls.reactions

import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.emf.ecore.xml.type.XMLTypePackage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertSame

/**
 * Verifies that {@link ClasspathGenModelRegistrar} registers generated packages described by a
 * genmodel on the classpath. The genmodel describes EMF's own generated packages, so that their
 * interfaces are on the classpath without compiling test fixtures.
 */
class ClasspathGenModelRegistrarTest {
	static val NS_URI_PREFIX = "test://classpath-genmodel-registrar/"
	static val ROOT_NS_URI = NS_URI_PREFIX + "ecore"
	static val WITHOUT_CLASS_NS_URI = NS_URI_PREFIX + "xml"
	static val NESTED_NS_URI = NS_URI_PREFIX + "type"
	static val PREREGISTERED_NS_URI = NS_URI_PREFIX + "preregistered"

	@TempDir
	Path modelDirectory

	@AfterEach
	def void removeTestPackages() {
		EPackage.Registry.INSTANCE.keySet.removeIf[startsWith(NS_URI_PREFIX)]
	}

	@Test
	def void registersPackagesFromGenModel() {
		writeModel()
		registerFromModelDirectory()
		assertSame(EcorePackage.eINSTANCE, EPackage.Registry.INSTANCE.getEPackage(ROOT_NS_URI))
	}

	@Test
	def void registersNestedPackagesAndSkipsMissingClasses() {
		writeModel()
		registerFromModelDirectory()
		assertSame(XMLTypePackage.eINSTANCE, EPackage.Registry.INSTANCE.getEPackage(NESTED_NS_URI))
		assertFalse(EPackage.Registry.INSTANCE.containsKey(WITHOUT_CLASS_NS_URI))
	}

	@Test
	def void keepsAlreadyRegisteredPackages() {
		EPackage.Registry.INSTANCE.put(PREREGISTERED_NS_URI, XMLTypePackage.eINSTANCE)
		writeModel()
		registerFromModelDirectory()
		assertSame(XMLTypePackage.eINSTANCE, EPackage.Registry.INSTANCE.getEPackage(PREREGISTERED_NS_URI))
	}

	/**
	 * A jar without {@code plugin.xml}, such as the language server's shaded jar, holds EMF's own
	 * {@code Ecore.genmodel}. Before Ecore registered itself, a lazy descriptor for it made
	 * {@code EcoreFactory}'s initialization fail. Runs on fresh copies of the classes, in which
	 * Ecore is not initialized yet.
	 */
	@Test
	def void ecoreStaysUsableWithEcoreGenModelOutsidePluginJar() {
		val ecoreModelDirectory = Files.createDirectories(modelDirectory.resolve("model"))
		for (file : #["Ecore.genmodel", "Ecore.ecore"]) {
			try (val in = EcorePackage.getResourceAsStream("/model/" + file)) {
				Files.copy(in, ecoreModelDirectory.resolve(file))
			}
		}
		val urls = newArrayList(modelDirectory.toUri.toURL)
		urls += System.getProperty("java.class.path").split(File.pathSeparator).map[Path.of(it).toUri.toURL]
		try (val classLoader = new URLClassLoader(urls, ClassLoader.platformClassLoader)) {
			classLoader.loadClass(ClasspathGenModelRegistrar.name).getMethod("registerGeneratedPackages", ClassLoader)
				.invoke(null, classLoader)
			assertNotNull(classLoader.loadClass(EcoreFactory.name).getField("eINSTANCE").get(null))
		}
	}

	def private void registerFromModelDirectory() {
		try (val classLoader = new URLClassLoader(#[modelDirectory.toUri.toURL], class.classLoader)) {
			ClasspathGenModelRegistrar.registerGeneratedPackages(classLoader)
		}
	}

	def private void writeModel() {
		val ecoreDirectory = Files.createDirectories(modelDirectory.resolve("src/main/ecore"))
		Files.writeString(ecoreDirectory.resolve("test.ecore"), '''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
			    name="ecore" nsURI="«ROOT_NS_URI»" nsPrefix="ecore">
			  <eSubpackages name="xml" nsURI="«WITHOUT_CLASS_NS_URI»" nsPrefix="xml">
			    <eSubpackages name="type" nsURI="«NESTED_NS_URI»" nsPrefix="type"/>
			  </eSubpackages>
			</ecore:EPackage>
		''')
		Files.writeString(ecoreDirectory.resolve("test.genmodel"), '''
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
		''')
		Files.writeString(ecoreDirectory.resolve("preregistered.ecore"), '''
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
			    name="ecore" nsURI="«PREREGISTERED_NS_URI»" nsPrefix="ecore"/>
		''')
		Files.writeString(ecoreDirectory.resolve("preregistered.genmodel"), '''
			<?xml version="1.0" encoding="UTF-8"?>
			<genmodel:GenModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:genmodel="http://www.eclipse.org/emf/2002/GenModel"
			    modelName="Preregistered">
			  <genPackages prefix="Ecore" basePackage="org.eclipse.emf" ecorePackage="preregistered.ecore#/"/>
			</genmodel:GenModel>
		''')
	}
}
