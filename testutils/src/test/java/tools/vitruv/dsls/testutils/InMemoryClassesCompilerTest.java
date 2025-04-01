package tools.vitruv.dsls.testutils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class InMemoryClassesCompilerTest {

  @TempDir
  Path tempDir; // JUnit 5 Temporary Folder

  private InMemoryClassesCompiler compiler;

  @BeforeEach
  void setUp() throws IOException {
    // Create Java source file in temp directory
    String javaCode = """
            public class NewMemory {
                public String sayNewMemory() {
                    return "New, Memory!";
                }
            }
        """;

    Path javaFile = tempDir.resolve("NewMemory.java");
    Files.writeString(javaFile, javaCode);

    // Initialize compiler with temp directory
    compiler = new InMemoryClassesCompiler(tempDir);
  }

  @Test
  void testCompileAndRetrieveClasses() {
    // Act: Compile classes
    compiler.compile();

    // Assert: Ensure classes are compiled
    Set<? extends Class<?>> compiledClasses = compiler.getCompiledClasses();
    assertFalse(compiledClasses.isEmpty(), "No classes were compiled");

    // Assert: Ensure NewMemory class is compiled
    assertTrue(compiledClasses.stream().anyMatch(cls -> cls.getSimpleName().equals("NewMemory")),
            "NewMemory class not found in compiled classes");
  }

  @Test
  void testInstantiateCompiledClass() {
    // Act: Compile classes
    compiler.compile();
    Set<? extends Class<?>> compiledClasses = compiler.getCompiledClasses();

    // Find NewMemory class
    Class<?> newMemoryClass = compiledClasses.stream()
            .filter(cls -> cls.getSimpleName().equals("NewMemory"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("NewMemory class not compiled"));

    // Act: Instantiate NewMemory class
    Object instance = instantiateClass(newMemoryClass);

    // Assert: Instance should not be null
    assertNotNull(instance, "Failed to instantiate NewMemory class");

    // Act & Assert: Invoke `sayNewMemory` method
    try {
      var method = newMemoryClass.getMethod("sayNewMemory");
      String result = (String) method.invoke(instance);
      assertEquals("New, Memory!", result, "Method sayNewMemory() returned incorrect value");
    } catch (Exception e) {
      fail("Failed to invoke sayNewMemory() method: " + e.getMessage());
    }
  }

  @Test
  void testCompileFailsForInvalidJavaFile() {
    compiler = new InMemoryClassesCompiler(Path.of("invalid/java/file/path"));

    // Adjust the expected exception type to AssertionError
    assertThrows(IllegalStateException.class, compiler::compile);
  }

  @Test
  void testNoFilesToCompile() throws IOException {
    // Arrange: Create a new empty directory
    Path emptyDir = Files.createTempDirectory("empty-dir");
    InMemoryClassesCompiler emptyCompiler = new InMemoryClassesCompiler(emptyDir);

    // Act: Compile
    emptyCompiler.compile();

    // Assert: No compiled classes should be found
    Set<? extends Class<?>> compiledClasses = emptyCompiler.getCompiledClasses();
    assertTrue(compiledClasses.isEmpty(), "Compiled classes found in an empty directory");
  }

  // Helper method to instantiate class
  private Object instantiateClass(Class<?> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      return null;
    }
  }
}
