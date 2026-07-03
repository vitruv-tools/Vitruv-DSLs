package tools.vitruv.dsls.reactions.ide.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that {@link ReactionsClasspathRegistrar} finds a sibling module's
 * {@code target/classes} directory and adds it to {@link MutableUrlClassLoaderHolder#INSTANCE},
 * so that Java types generated from the user's Ecore model (which live there once the model
 * project has been built) become resolvable for Xbase's JVM type system.
 */
class ReactionsClasspathRegistrarTest {

	@Test
	void addsSiblingModuleClassesDirectoryToClassLoader(@TempDir Path repoRoot) throws IOException {
		Files.createDirectory(repoRoot.resolve(".git"));
		Path classesDir = Files.createDirectories(repoRoot.resolve("model/target/classes"));
		compileDummyClass(repoRoot, classesDir);
		Path reactionsFile = Files.createDirectories(repoRoot.resolve("consistency/reactions"))
				.resolve("test.reactions");
		Files.writeString(reactionsFile, "content");

		new ReactionsClasspathRegistrar().ensureRegisteredNear(URI.createFileURI(reactionsFile.toString()));

		assertDoesNotThrow(() -> MutableUrlClassLoaderHolder.INSTANCE.loadClass("dummy.GeneratedModelClass"));
	}

	private void compileDummyClass(Path repoRoot, Path classesDir) throws IOException {
		Path sourceDir = Files.createDirectories(repoRoot.resolve("model/src-for-test/dummy"));
		Path sourceFile = sourceDir.resolve("GeneratedModelClass.java");
		Files.writeString(sourceFile, "package dummy;\npublic class GeneratedModelClass {}\n");

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int result = compiler.run(null, null, null, "-d", classesDir.toString(), sourceFile.toString());
		if (result != 0) {
			throw new IllegalStateException("Failed to compile test fixture class");
		}
	}

}
