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
import org.eclipse.emf.common.util.URI

/**
 * Adds {@code target/classes} directories and {@code .jar} files found near a linked
 * {@code .reactions} file to the shared {@link MutableUrlClassLoader}, so that Java types generated
 * from the user's Ecore model (e.g. {@code Family}, {@code Person}), helper classes and classes of
 * library jars kept in the workspace resolve for Xbase's JVM type system even though the
 * language server has no static classpath into the user's model project.
 * <p>
 * Unlike {@link ReactionsEcoreWorkspaceRegistrar}, which only needs the {@code .ecore} file's
 * structure, this needs actual compiled {@code .class} files, so it only helps once the model
 * project has been built at least once (e.g. via {@code mvn compile}).
 * <p>
 * The {@code target/classes} of the Maven module that contains the {@code .reactions} file being
 * linked (found by walking up to the nearest {@code pom.xml}) is only added for the classes whose
 * source lies in that module's {@link #OWN_MODULE_SOURCE_FOLDERS} (its hand-written helpers):
 * a previous {@code mvn compile} of that same module leaves behind class files generated from
 * the very reactions sources being edited (e.g. the routines facade). Adding that directory to
 * the classpath makes those stale, already-compiled classes resolvable under the same qualified
 * name as the live, in-memory JVM model this language server derives from the open file, so Xbase
 * ends up binding calls (and, transitively, the "N callers" search and orphaned-routine
 * validation, which only look for references to the live derived model) to the stale external
 * class instead of the current in-memory one. Generated classes have no source there, so they stay
 * invisible.
 */
class ReactionsClasspathRegistrar {
	static final Logger log = LogManager.getLogger(ReactionsClasspathRegistrar)
	static final List<String> EXCLUDED_SEGMENTS = #["target", "bin", "build", "node_modules", ".git"]
	static final long MIN_RESCAN_INTERVAL_MILLIS = 2000L
	static final int MAX_VISITED_ENTRIES = 20000
	/** Source folders of the own module whose classes are added; generated sources live in {@code target}. */
	public static final List<String> OWN_MODULE_SOURCE_FOLDERS = #["src/main/java", "src/main/xtend"]
	static final List<String> SOURCE_EXTENSIONS = #[".java", ".xtend"]

	/** search root -> time it was last scanned, to avoid rescanning the same directory tree on every keystroke. */
	val Map<URI, Long> lastScanTimeByRoot = new HashMap
	val Set<String> addedClassesDirs = new HashSet

	def void ensureRegisteredNear(URI resourceUri) {
		ensureRegisteredIn(ReactionsWorkspaceRoots.findRepoRoots(resourceUri), resourceUri)
	}

	/**
	 * Adds the {@code target/classes} directories and {@code .jar} files below each of {@code roots}
	 * (the {@code target/classes} of the module containing {@code resourceUri} only for its
	 * hand-written classes), skipping roots that were already scanned recently.
	 */
	def void ensureRegisteredIn(List<URI> roots, URI resourceUri) {
		val ownModuleRoot = findOwnModuleRoot(resourceUri)
		for (root : roots) {
			val now = System.currentTimeMillis
			val lastScan = lastScanTimeByRoot.get(root)
			if (lastScan === null || now - lastScan >= MIN_RESCAN_INTERVAL_MILLIS) {
				lastScanTimeByRoot.put(root, now)
				collectClassesDirs(new File(root.toFileString), ownModuleRoot, new AtomicInteger(0))
			}
		}
	}

	/**
	 * Walks up from {@code resourceUri} to the nearest ancestor directory containing a
	 * {@code pom.xml}, i.e. the Maven module the {@code .reactions} file belongs to.
	 */
	def private File findOwnModuleRoot(URI resourceUri) {
		if (resourceUri === null || !resourceUri.isFile) {
			return null;
		}
		var probe = new File(resourceUri.toFileString).parentFile
		while (probe !== null) {
			if (new File(probe, "pom.xml").exists) {
				return probe;
			}
			probe = probe.parentFile
		}
		return null;
	}

	def private void collectClassesDirs(File directory, File ownModuleRoot, AtomicInteger visitedCount) {
		if (visitedCount.get >= MAX_VISITED_ENTRIES) {
			return;
		}
		visitedCount.incrementAndGet
		val classesDir = new File(directory, "target/classes")
		if (classesDir.directory) {
			if (directory.equals(ownModuleRoot)) {
				addOwnModuleClassesDir(classesDir, ownModuleRoot)
			} else {
				addClassesDir(classesDir)
			}
		}
		val children = directory.listFiles
		if (children === null) {
			return;
		}
		for (child : children) {
			if (child.directory && !EXCLUDED_SEGMENTS.contains(child.name)) {
				collectClassesDirs(child, ownModuleRoot, visitedCount)
			} else if (child.file && child.name.endsWith(".jar")) {
				addClassesDir(child)
			}
		}
	}

	def private void addOwnModuleClassesDir(File classesDir, File moduleRoot) {
		val path = classesDir.absolutePath
		if (addedClassesDirs.add(path)) {
			log.info('''Adding the classes with a source in «moduleRoot.absolutePath» «OWN_MODULE_SOURCE_FOLDERS» from «path» to the classpath used for JVM type resolution''')
			MutableUrlClassLoaderHolder.INSTANCE.addFilteredClassesDirectory(classesDir,
				[className|hasOwnSource(moduleRoot, className)])
		}
	}

	/** Whether the top-level class of {@code className} has a source file in the module's source folders. */
	def private static boolean hasOwnSource(File moduleRoot, String className) {
		val dollar = className.indexOf('$')
		val topLevelPath = (if (dollar < 0) className else className.substring(0, dollar)).replace('.', '/')
		OWN_MODULE_SOURCE_FOLDERS.exists [ folder |
			SOURCE_EXTENSIONS.exists[suffix|new File(moduleRoot, folder + "/" + topLevelPath + suffix).file]
		]
	}

	/** Adds a {@code target/classes} directory or a {@code .jar} file. */
	def private void addClassesDir(File classesDir) {
		val path = classesDir.absolutePath
		if (addedClassesDirs.add(path)) {
			log.info('''Adding «path» to the classpath used for JVM type resolution''')
			MutableUrlClassLoaderHolder.INSTANCE.addClasspathUrl(classesDir.toURI.toURL)
		}
	}

}
