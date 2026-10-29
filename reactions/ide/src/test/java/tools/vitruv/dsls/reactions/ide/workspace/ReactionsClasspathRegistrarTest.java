package tools.vitruv.dsls.reactions.ide.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
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
  void addsSiblingModuleClassesDirectoryToClassLoader(@TempDir Path repoRoot)
      throws IOException {
    Files.createDirectory(repoRoot.resolve(".git"));
    Path classesDir = Files.createDirectories(repoRoot.resolve("model/target/classes"));
    compileDummyClass(repoRoot, classesDir);
    Path reactionsFile = Files.createDirectories(repoRoot.resolve("consistency/reactions"))
        .resolve("test.reactions");
    Files.writeString(reactionsFile, "content");

    new ReactionsClasspathRegistrar()
        .ensureRegisteredNear(URI.createFileURI(reactionsFile.toString()));

    assertDoesNotThrow(
        () -> MutableUrlClassLoaderHolder.INSTANCE.loadClass("dummy.GeneratedModelClass"));
  }

  /**
   * Verifies that a previous {@code mvn compile} of the reactions module itself does not shadow
   * the language server's live, in-memory JVM model: {@code target/classes} belonging to the
   * same Maven module as the linked {@code .reactions} file (found via the nearest {@code
   * pom.xml}) must not be added to the classpath, while a sibling module's {@code target/classes}
   * (e.g. the model project) still must be.
   */
  @Test
  void doesNotAddOwnModulesClassesDirectoryToClassLoader(@TempDir Path repoRoot)
      throws IOException {
    Files.createDirectory(repoRoot.resolve(".git"));
    Path modelClassesDir = Files.createDirectories(repoRoot.resolve("model/target/classes"));
    compileDummyClass(repoRoot, "model", "SiblingModuleClass", modelClassesDir);

    Path reactionsModuleDir = Files.createDirectories(repoRoot.resolve("consistency"));
    Files.writeString(reactionsModuleDir.resolve("pom.xml"), "<project/>");
    Path reactionsClassesDir =
        Files.createDirectories(reactionsModuleDir.resolve("target/classes"));
    compileDummyClass(repoRoot, "consistency", "OwnModuleClass", reactionsClassesDir);

    Path reactionsFile = Files.createDirectories(reactionsModuleDir.resolve("src/main/reactions"))
        .resolve("test.reactions");
    Files.writeString(reactionsFile, "content");

    new ReactionsClasspathRegistrar()
        .ensureRegisteredNear(URI.createFileURI(reactionsFile.toString()));

    assertDoesNotThrow(
        () -> MutableUrlClassLoaderHolder.INSTANCE.loadClass("dummy.SiblingModuleClass"));
    assertThrows(ClassNotFoundException.class,
        () -> MutableUrlClassLoaderHolder.INSTANCE.loadClass("dummy.OwnModuleClass"));
  }

  /**
   * Verifies that the hand-written helper classes of the reactions' own module (source in its
   * {@code src/main/java}, including nested classes) resolve, while classes generated from the
   * reactions into the same {@code target/classes} (no source there) stay invisible.
   */
  @Test
  void addsOwnModulesHandWrittenClassesButNotGeneratedOnes(@TempDir Path repoRoot)
      throws IOException {
    Files.createDirectory(repoRoot.resolve(".git"));
    Path moduleDir = Files.createDirectories(repoRoot.resolve("consistency"));
    Files.writeString(moduleDir.resolve("pom.xml"), "<project/>");
    Path classesDir = Files.createDirectories(moduleDir.resolve("target/classes"));
    Path helperSource = Files.createDirectories(moduleDir.resolve("src/main/java/helpers"))
        .resolve("OwnHelper.java");
    Files.writeString(helperSource,
        "package helpers;\npublic class OwnHelper { public static class Nested {} }\n");
    compile(helperSource, classesDir);
    Path generatedSource =
        Files.createDirectories(moduleDir.resolve("target/generated-sources/reactions/helpers"))
            .resolve("GeneratedRoutines.java");
    Files.writeString(generatedSource, "package helpers;\npublic class GeneratedRoutines {}\n");
    compile(generatedSource, classesDir);
    Path reactionsFile = Files.createDirectories(moduleDir.resolve("src/main/reactions"))
        .resolve("test.reactions");
    Files.writeString(reactionsFile, "content");

    new ReactionsClasspathRegistrar()
        .ensureRegisteredNear(URI.createFileURI(reactionsFile.toString()));

    MutableUrlClassLoader classLoader = MutableUrlClassLoaderHolder.INSTANCE;
    assertDoesNotThrow(() -> classLoader.loadClass("helpers.OwnHelper"));
    assertDoesNotThrow(() -> classLoader.loadClass("helpers.OwnHelper$Nested"));
    assertNotNull(classLoader.getResource("helpers/OwnHelper.class"));
    assertThrows(ClassNotFoundException.class,
        () -> classLoader.loadClass("helpers.GeneratedRoutines"));
    assertNull(classLoader.getResource("helpers/GeneratedRoutines.class"));
  }

  /**
   * Verifies that a {@code .jar} file in the workspace (e.g. a library the model's Java classes
   * come from) is added, so that its classes resolve.
   */
  @Test
  void addsJarFileToClassLoader(@TempDir Path repoRoot) throws IOException {
    Files.createDirectory(repoRoot.resolve(".git"));
    Path jarClassesDir = Files.createDirectories(repoRoot.resolve("jar-content"));
    compileDummyClass(repoRoot, "library", "JarLibraryClass", jarClassesDir);
    Path jarFile = Files.createDirectories(repoRoot.resolve("lib")).resolve("library.jar");
    try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarFile))) {
      jar.putNextEntry(new JarEntry("dummy/JarLibraryClass.class"));
      jar.write(Files.readAllBytes(jarClassesDir.resolve("dummy/JarLibraryClass.class")));
      jar.closeEntry();
    }
    Path reactionsFile = Files.createDirectories(repoRoot.resolve("consistency/reactions"))
        .resolve("test.reactions");
    Files.writeString(reactionsFile, "content");

    new ReactionsClasspathRegistrar()
        .ensureRegisteredNear(URI.createFileURI(reactionsFile.toString()));

    assertDoesNotThrow(
        () -> MutableUrlClassLoaderHolder.INSTANCE.loadClass("dummy.JarLibraryClass"));
  }

  private void compile(Path sourceFile, Path classesDir) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    int result = compiler.run(null, null, null, "-d", classesDir.toString(), sourceFile.toString());
    if (result != 0) {
      throw new IllegalStateException("Failed to compile test fixture class");
    }
  }

  private void compileDummyClass(Path repoRoot, Path classesDir) throws IOException {
    compileDummyClass(repoRoot, "model", "GeneratedModelClass", classesDir);
  }

  private void compileDummyClass(
      Path repoRoot, String moduleDirName, String simpleClassName, Path classesDir)
      throws IOException {
    Path sourceDir =
        Files.createDirectories(repoRoot.resolve(moduleDirName + "/src-for-test/dummy"));
    Path sourceFile = sourceDir.resolve(simpleClassName + ".java");
    Files.writeString(sourceFile, "package dummy;\npublic class " + simpleClassName + " {}\n");

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    int result = compiler.run(null, null, null, "-d", classesDir.toString(), sourceFile.toString());
    if (result != 0) {
      throw new IllegalStateException("Failed to compile test fixture class");
    }
  }

}
