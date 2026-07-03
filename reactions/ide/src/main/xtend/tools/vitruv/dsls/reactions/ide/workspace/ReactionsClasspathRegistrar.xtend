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
 * Adds {@code target/classes} directories found near a linked {@code .reactions} file to the
 * shared {@link MutableUrlClassLoader}, so that Java types generated from the user's Ecore model
 * (e.g. {@code Family}, {@code Person}) resolve for Xbase's JVM type system even though the
 * language server has no static classpath into the user's model project.
 * <p>
 * Unlike {@link ReactionsEcoreWorkspaceRegistrar}, which only needs the {@code .ecore} file's
 * structure, this needs actual compiled {@code .class} files, so it only helps once the model
 * project has been built at least once (e.g. via {@code mvn compile}).
 */
class ReactionsClasspathRegistrar {
	static final Logger log = LogManager.getLogger(ReactionsClasspathRegistrar)
	static final List<String> EXCLUDED_SEGMENTS = #["target", "bin", "build", "node_modules", ".git"]
	static final long MIN_RESCAN_INTERVAL_MILLIS = 2000L
	static final int MAX_VISITED_ENTRIES = 20000

	/** search root -> time it was last scanned, to avoid rescanning the same directory tree on every keystroke. */
	val Map<URI, Long> lastScanTimeByRoot = new HashMap
	val Set<String> addedClassesDirs = new HashSet

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
		collectClassesDirs(new File(root.toFileString), new AtomicInteger(0))
	}

	def private void collectClassesDirs(File directory, AtomicInteger visitedCount) {
		if (visitedCount.get >= MAX_VISITED_ENTRIES) {
			return;
		}
		visitedCount.incrementAndGet
		val classesDir = new File(directory, "target/classes")
		if (classesDir.directory) {
			addClassesDir(classesDir)
		}
		val children = directory.listFiles
		if (children === null) {
			return;
		}
		for (child : children) {
			if (child.directory && !EXCLUDED_SEGMENTS.contains(child.name)) {
				collectClassesDirs(child, visitedCount)
			}
		}
	}

	def private void addClassesDir(File classesDir) {
		val path = classesDir.absolutePath
		if (addedClassesDirs.add(path)) {
			log.info('''Adding «path» to the classpath used for JVM type resolution''')
			MutableUrlClassLoaderHolder.INSTANCE.addUrl(classesDir.toURI.toURL)
		}
	}

}
