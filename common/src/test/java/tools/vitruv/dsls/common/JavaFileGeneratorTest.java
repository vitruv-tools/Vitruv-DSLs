package tools.vitruv.dsls.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Tests the JavaFileGenerator. */
class JavaFileGeneratorTest {

  @Test
  void testGenerateSimple() {
    String result = JavaFileGenerator.generateClass("", "test", null);
    String expectedResult = "package test;\n\n";
    Assertions.assertEquals(expectedResult, result, "only package should be printed");

    result = JavaFileGenerator.generateClass("implementation", "test", null);
    expectedResult = "package test;\n\nimplementation";
    Assertions.assertEquals(expectedResult, result, "implementation is not printed");
  }

  @Test
  void testGenerateImportHelper() {
    JavaImportHelper importHelper = new JavaImportHelper();
    StringBuilder expectedResult = new StringBuilder("package test;\n\n");

    Assertions.assertEquals(
        expectedResult.toString(),
        generateClassWithImportHelper(importHelper),
        "Empty Import failed");

    importHelper.typeRef(JavaFileGenerator.class);
    expectedResult.append("import ").append(JavaFileGenerator.class.getName()).append(";\n");
    Assertions.assertEquals(
        expectedResult + "\n\n",
        generateClassWithImportHelper(importHelper),
        "normal Import failed");

    importHelper.typeRef(JavaFileGenerator.class.getName());
    Assertions.assertEquals(
        expectedResult + "\n\n",
        generateClassWithImportHelper(importHelper),
        "duplicate import not deduplicated");

    importHelper.staticRef(JavaFileGenerator.class, "test");
    expectedResult.append("import static ").append(JavaFileGenerator.class.getName()).append(";\n");
    Assertions.assertEquals(
        expectedResult + "\n\n",
        generateClassWithImportHelper(importHelper),
        "Static Import failed");
  }

  private String generateClassWithImportHelper(JavaImportHelper importHelper) {
    return JavaFileGenerator.generateClass("", "test", importHelper);
  }

  @Test
  void testGetJavaFilePath() {
    String expectedResult = "package" + JavaFileGenerator.FSA_SEPARATOR + "class.java";
    String result = JavaFileGenerator.getJavaFilePath("package.class");
    Assertions.assertEquals(expectedResult, result, "Simple test failed");

    GenericClassNameGenerator nameGenerator = new GenericClassNameGenerator("package", "class");
    Assertions.assertEquals(
        expectedResult,
        JavaFileGenerator.getJavaFilePath(nameGenerator),
        "Name Generator does not work");

    expectedResult =
        "package1"
            + JavaFileGenerator.FSA_SEPARATOR
            + "package2"
            + JavaFileGenerator.FSA_SEPARATOR
            + "class.java";
    result = JavaFileGenerator.getJavaFilePath("package1.package2.class");
    Assertions.assertEquals(expectedResult, result, "several directories does not work");
  }
}
