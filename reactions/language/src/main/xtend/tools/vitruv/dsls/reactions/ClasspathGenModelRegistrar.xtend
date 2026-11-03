package tools.vitruv.dsls.reactions

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashSet
import java.util.Map
import java.util.Set
import java.util.jar.JarFile
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EcorePackage
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Registers the generated packages of metamodels on the classpath that do not declare them in a
 * {@code plugin.xml}, e.g. metamodels published as plain Maven artifacts.
 * <p>
 * {@code ReactionsLinkingService} resolves metamodel {@code import} statements against
 * {@link EPackage.Registry#INSTANCE}. Outside of Equinox, that registry is only filled from
 * {@code plugin.xml} files (see {@code EcorePlugin.ExtensionProcessor}). This registrar
 * additionally searches the classpath for {@code .genmodel} files, derives the qualified name of
 * each generated {@code *Package} interface from the genmodel and its {@code .ecore} file, and
 * registers a lazy descriptor for its namespace URI if the interface is on the classpath.
 * Namespace URIs that are already registered are left untouched.
 */
class ClasspathGenModelRegistrar {
	static final Logger log = LogManager.getLogger(ClasspathGenModelRegistrar)
	static final String GENMODEL_EXTENSION = ".genmodel"
	static final String JAR_SEPARATOR = "!/"
	static final String PLUGIN_XML = "plugin.xml"

	val ClassLoader classLoader
	/** ecore file URL -> parsed document, as several genmodels and packages can refer to the same file */
	val Map<String, Document> ecoreDocuments = new HashMap

	private new(ClassLoader classLoader) {
		this.classLoader = classLoader
	}

	/**
	 * Registers the generated packages of all {@code .genmodel} files found in the jars and
	 * directories visible to {@code classLoader}.
	 */
	def static void registerGeneratedPackages(ClassLoader classLoader) {
		// Ecore registers itself when first used. Jars repackaged without plugin.xml (e.g. the
		// language server's shaded jar) still hold Ecore.genmodel, and a lazy descriptor for Ecore
		// would make EcoreFactory's initialization recurse into the Ecore package's, which fails.
		EcorePackage.eINSTANCE.getClass()
		val registrar = new ClasspathGenModelRegistrar(classLoader)
		for (genModelUrl : registrar.findGenModels) {
			try {
				registrar.registerGenModel(genModelUrl)
			} catch (Exception e) {
				log.warn('''Could not register the generated packages of «genModelUrl»''', e)
			}
		}
	}

	/**
	 * Finds the genmodels in jars and directories without {@code plugin.xml}. Those with one are
	 * left to {@code EcorePlugin.ExtensionProcessor}; registering their packages here could even
	 * break EMF's own initialization, as for the Ecore package itself.
	 */
	def private Set<URL> findGenModels() {
		val result = new LinkedHashSet<URL>
		for (manifestUrl : Collections.list(classLoader.getResources(JarFile.MANIFEST_NAME))) {
			val url = manifestUrl.toString
			if (manifestUrl.protocol == "jar" && url.contains(JAR_SEPARATOR)) {
				val jarUrl = url.substring(0, url.indexOf(JAR_SEPARATOR))
				try (val jarFile = new JarFile(new File(new URL(jarUrl.substring("jar:".length)).toURI))) {
					if (jarFile.getEntry(PLUGIN_XML) === null) {
						jarFile.stream.filter[name.endsWith(GENMODEL_EXTENSION)].forEach [
							result += new URL(jarUrl + JAR_SEPARATOR + name)
						]
					}
				} catch (Exception e) {
					log.debug('''Could not search «jarUrl» for genmodels''', e)
				}
			}
		}
		for (rootUrl : Collections.list(classLoader.getResources(""))) {
			if (rootUrl.protocol == "file") {
				val root = Paths.get(rootUrl.toURI)
				if (!Files.exists(root.resolve(PLUGIN_XML))) {
					try (val files = Files.walk(root)) {
						files.filter[fileName.toString.endsWith(GENMODEL_EXTENSION)].forEach[result += toUri.toURL]
					} catch (Exception e) {
						log.debug('''Could not search «rootUrl» for genmodels''', e)
					}
				}
			}
		}
		return result
	}

	def private void registerGenModel(URL genModelUrl) {
		val genModel = parse(genModelUrl).documentElement
		for (genPackage : genModel.childElements("genPackages")) {
			registerGenPackage(genPackage, genModelUrl, null)
		}
	}

	/**
	 * Registers {@code genPackage} and its nested packages. The Java package of a nested package
	 * is the one of its parent extended by its own name, as in EMF's code generator.
	 */
	def private void registerGenPackage(Element genPackage, URL genModelUrl, String parentJavaPackage) {
		val ePackage = findEcorePackage(genPackage, genModelUrl)
		if (ePackage === null) {
			log.debug('''Skipping a package of «genModelUrl» whose ecore package cannot be found''')
			return
		}
		val name = ePackage.getAttribute("name")
		val basePackage = genPackage.getAttribute("basePackage")
		val javaPackage = if (parentJavaPackage !== null) {
			parentJavaPackage + "." + name
		} else if (basePackage.empty) {
			name
		} else {
			basePackage + "." + name
		}
		val interfacePackageSuffix = genPackage.getAttribute("interfacePackageSuffix")
		val interfacePackage = if (interfacePackageSuffix.empty) javaPackage else javaPackage + "." + interfacePackageSuffix
		register(ePackage.getAttribute("nsURI"), interfacePackage + "." + genPackage.getAttribute("prefix") + "Package")
		for (nestedGenPackage : genPackage.childElements("nestedGenPackages")) {
			registerGenPackage(nestedGenPackage, genModelUrl, javaPackage)
		}
	}

	/**
	 * Finds the {@code EPackage} element a gen package refers to, given either as
	 * {@code ecorePackage} attribute or as {@code ecorePackage} child with an {@code href}.
	 * Supports the default fragment paths {@code /} and {@code //sub/subsub}.
	 */
	def private Element findEcorePackage(Element genPackage, URL genModelUrl) {
		var reference = genPackage.getAttribute("ecorePackage")
		if (reference.empty) {
			reference = genPackage.childElements("ecorePackage").map[getAttribute("href")].head ?: ""
		}
		val fragmentStart = reference.indexOf("#")
		if (fragmentStart <= 0) {
			return null
		}
		val ecoreUrl = new URL(genModelUrl, reference.substring(0, fragmentStart))
		val fragment = reference.substring(fragmentStart + 1)
		val document = ecoreDocuments.computeIfAbsent(ecoreUrl.toString)[parse(ecoreUrl)]
		var ePackage = document.documentElement
		if (ePackage.getAttribute("nsURI").empty || !fragment.startsWith("/")) {
			return null
		}
		if (fragment != "/") {
			if (!fragment.startsWith("//")) {
				return null
			}
			for (subpackageName : fragment.substring(2).split("/")) {
				ePackage = ePackage.childElements("eSubpackages").findFirst[getAttribute("name") == subpackageName]
				if (ePackage === null) {
					return null
				}
			}
		}
		return ePackage
	}

	def private void register(String nsUri, String packageInterfaceName) {
		if (nsUri.empty || EPackage.Registry.INSTANCE.containsKey(nsUri)) {
			return
		}
		if (classLoader.getResource(packageInterfaceName.replace('.', '/') + ".class") === null) {
			log.debug('''Not registering «nsUri» because «packageInterfaceName» is not on the classpath''')
			return
		}
		val loader = classLoader
		EPackage.Registry.INSTANCE.put(nsUri, new EPackage.Descriptor() {
			override getEPackage() {
				Class.forName(packageInterfaceName, true, loader).getField("eINSTANCE").get(null) as EPackage
			}

			// Null while the package is still being initialized, e.g. when its factory looks itself up
			override getEFactory() {
				getEPackage?.EFactoryInstance
			}
		})
		log.debug('''Registered «packageInterfaceName» for «nsUri»''')
	}

	def private static Iterable<Element> childElements(Element parent, String tagName) {
		val children = parent.childNodes
		return (0 ..< children.length).map[children.item(it)].filter(Element).filter[it.tagName == tagName].toList
	}

	def private static Document parse(URL url) {
		val factory = DocumentBuilderFactory.newInstance
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
		try (val input = url.openStream) {
			return factory.newDocumentBuilder.parse(input)
		}
	}
}
