package tools.vitruv.dsls.reactions.ide.workspace

import java.io.File
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import java.util.concurrent.atomic.AtomicInteger
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.emf.codegen.ecore.genmodel.GenModel
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl

/**
 * Keeps {@link EPackage.Registry#INSTANCE} in sync with {@code .ecore} files found in the
 * workspace of the file currently being linked.
 * <p>
 * {@code ReactionsLinkingService} resolves metamodel {@code import} statements directly against
 * {@code EPackage.Registry.INSTANCE}. When running as a Maven build, that registry gets populated
 * via generated {@code plugin.xml} entries on the classpath. The language server has no such
 * classpath into the user's model project, so without this registrar, metamodel imports never
 * resolve and every metaclass reference in a {@code .reactions} file is reported as an error.
 * <p>
 * Since the language server's global {@code ProjectManager}/workspace machinery lives in a
 * separate, language-agnostic Guice injector that this language's ide module cannot influence,
 * the registration is instead triggered lazily, right when a metamodel reference fails to
 * resolve (see {@link ReactionsIdeLinkingService}), by searching the project containing the file
 * being linked for {@code .ecore} files.
 */
class ReactionsEcoreWorkspaceRegistrar {
	static final Logger log = LogManager.getLogger(ReactionsEcoreWorkspaceRegistrar)
	static final String ECORE_EXTENSION = "ecore"
	static final String GENMODEL_EXTENSION = "genmodel"
	static final List<String> EXCLUDED_SEGMENTS = #["target", "bin", "build", "node_modules", ".git"]
	static final long MIN_RESCAN_INTERVAL_MILLIS = 2000L
	static final int MAX_VISITED_ENTRIES = 20000

	/** nsURI -> file it was last registered from, so we only ever touch registry entries we contributed ourselves. */
	val Map<String, URI> registeredFrom = new HashMap
	/** search root -> time it was last scanned, to avoid rescanning the same directory tree on every keystroke. */
	val Map<URI, Long> lastScanTimeByRoot = new HashMap

	/**
	 * Ensures that {@code .ecore} files near {@code resourceUri} have been scanned and their
	 * packages registered, unless that search root was already scanned recently.
	 */
	def void ensureRegisteredNear(URI resourceUri) {
		val root = ReactionsWorkspaceRoots.findRepoRoot(resourceUri)
		if (root === null) {
			return;
		}
		val now = System.currentTimeMillis
		val lastScan = lastScanTimeByRoot.get(root)
		if (lastScan !== null && now - lastScan < MIN_RESCAN_INTERVAL_MILLIS) {
			return;
		}
		lastScanTimeByRoot.put(root, now)
		scanAndRegister(root)
	}

	def private void scanAndRegister(URI baseDir) {
		val baseFile = new File(baseDir.toFileString)
		val ecoreFiles = new HashSet<File>
		collectEcoreFiles(baseFile, ecoreFiles, new AtomicInteger(0))
		for (ecoreFile : ecoreFiles) {
			registerFile(URI.createFileURI(ecoreFile.absolutePath))
		}
	}

	def private void collectEcoreFiles(File directory, Set<File> result, AtomicInteger visitedCount) {
		val children = directory.listFiles
		if (children === null) {
			return;
		}
		for (child : children) {
			if (visitedCount.get >= MAX_VISITED_ENTRIES) {
				return;
			}
			visitedCount.incrementAndGet
			if (child.directory) {
				if (!EXCLUDED_SEGMENTS.contains(child.name)) {
					collectEcoreFiles(child, result, visitedCount)
				}
			} else if (ECORE_EXTENSION.equals(URI.createFileURI(child.absolutePath).fileExtension())) {
				result.add(child)
			}
		}
	}

	def private void registerFile(URI ecoreFileUri) {
		try {
			val resourceSet = new ResourceSetImpl
			resourceSet.resourceFactoryRegistry.extensionToFactoryMap.put(ECORE_EXTENSION, new EcoreResourceFactoryImpl)
			resourceSet.resourceFactoryRegistry.extensionToFactoryMap.put(GENMODEL_EXTENSION, new EcoreResourceFactoryImpl)
			val resource = resourceSet.getResource(ecoreFileUri, true)
			resolveInstanceClassNamesFromGenModel(resourceSet, ecoreFileUri)
			resource.contents.filter(EPackage).forEach[register(it, ecoreFileUri)]
		} catch (Exception e) {
			log.warn('''Could not load «ecoreFileUri» while scanning the workspace for metamodels''', e)
		}
	}

	/**
	 * A {@code .ecore} file loaded directly from disk (rather than through a project's generated
	 * {@code *Package} Java class) never has {@link EClassifier#getInstanceClassName()} populated:
	 * that name is normally assigned by the generated package's {@code initializePackageContents()}
	 * method, which never runs for a plain XMI load. Without it, {@code ReactionsLanguageHelper}
	 * cannot map a metaclass reference to its Java type, and the Reactions JVM model inferrer
	 * silently falls back to {@code Object} for every routine parameter typed by that metaclass.
	 * <p>
	 * The sibling {@code .genmodel} file next to almost every hand- or tool-authored {@code .ecore}
	 * file records exactly the Java class name EMF's code generator would have assigned, so loading
	 * it lets us reproduce that assignment ourselves.
	 */
	def private void resolveInstanceClassNamesFromGenModel(ResourceSet resourceSet, URI ecoreFileUri) {
		val genModelUri = ecoreFileUri.trimFileExtension.appendFileExtension(GENMODEL_EXTENSION)
		if (!new File(genModelUri.toFileString).exists) {
			return;
		}
		try {
			GenModelPackage.eINSTANCE.getClass() // ensure the genmodel metamodel is registered
			val genModelResource = resourceSet.getResource(genModelUri, true)
			genModelResource.contents.filter(GenModel).forEach [
				genPackages.forEach [ genPackage |
					genPackage.genClassifiers.forEach [ genClassifier |
						val eClassifier = genClassifier.ecoreClassifier
						if (eClassifier !== null && eClassifier.instanceClassName === null) {
							eClassifier.instanceClassName = genClassifier.rawInstanceClassName
						}
					]
				]
			]
		} catch (Exception e) {
			log.warn('''Could not load «genModelUri» to resolve Java class names for «ecoreFileUri»''', e)
		}
	}

	def private void register(EPackage ePackage, URI sourceFile) {
		val nsUri = ePackage.nsURI
		if (nsUri !== null) {
			if (EPackage.Registry.INSTANCE.containsKey(nsUri) && !sourceFile.equals(registeredFrom.get(nsUri))) {
				log.warn('''EPackage with nsURI «nsUri» found in «sourceFile» overrides an already registered package''')
			}
			EPackage.Registry.INSTANCE.put(nsUri, ePackage)
			registeredFrom.put(nsUri, sourceFile)
		}
		ePackage.getESubpackages().forEach[register(it, sourceFile)]
	}

}
