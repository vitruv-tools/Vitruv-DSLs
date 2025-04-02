package tools.vitruv.dsls.testutils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class InMemoryClassesCompilerTest {

  @TempDir
  Path tempDir; // JUnit will create a temporary directory for test isolation

  @Test
  void testCompile() throws IOException {
    // 1. Create a Java source file inside the temp directory
    Path javaFile = tempDir.resolve("TestClass.java");
    Files.writeString(javaFile, """
            public class TestClass {
                public String greet() {
                    return "Hello, World!";
                }
            }
        """);

    // 2. Initialize and compile the source
    InMemoryClassesCompiler compiler = new InMemoryClassesCompiler(tempDir);
    compiler.compile();

    // 3. Retrieve compiled classes
    Set<? extends Class<?>> compiledClasses = compiler.getCompiledClasses();

    // 4. Assertions: Ensure compilation was successful
    assertNotNull(compiledClasses, "Compiled classes should not be null");
    assertFalse(compiledClasses.isEmpty(), "There should be at least one compiled class");

    // 5. Ensure TestClass is among compiled classes
    boolean containsTestClass = compiledClasses.stream()
            .anyMatch(clazz -> clazz.getSimpleName().equals("TestClass"));
    assertTrue(containsTestClass, "TestClass should be compiled successfully");
  }
}