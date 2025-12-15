package tools.vitruv.dsls.common;

import edu.kit.ipd.sdq.activextendannotations.Utility;

/** Generates Java file contents and paths. */
@Utility
public final class JavaFileGenerator {
  public static final String JAVA_FILE_EXTENSION = ".java";
  public static final String FSA_SEPARATOR = "/";

  /** Private constructor for utility class. */
  private JavaFileGenerator() {
  }

  /**
   * Generates a Java class file content.
   *
   * @param classImplementation the implementation of the class
   * @param packageName         the package name
   * @param importHelper        the import helper
   * @return the Java class file content
   */
  public static String generateClass(
      CharSequence classImplementation, String packageName, JavaImportHelper importHelper) {
    StringBuilder sb = new StringBuilder();
    sb.append("package ").append(packageName).append(";\n\n");
    if (importHelper != null) {
      CharSequence imports = importHelper.generateImportCode();
      if (imports != null && imports.length() > 0) {
        sb.append(imports.toString());
        sb.append("\n\n");
      }
    }
    sb.append(classImplementation == null ? "" : classImplementation.toString());
    return sb.toString();
  }

  /**
   * Generates the file path for a Java class file from its qualified class name.
   *
   * @param qualifiedClassName the qualified class name
   * @return the file path for the Java class file
   */
  public static String getJavaFilePath(String qualifiedClassName) {
    if (qualifiedClassName == null) {
      return null;
    }
    return qualifiedClassName.replace('.', FSA_SEPARATOR.charAt(0)) + JAVA_FILE_EXTENSION;
  }

  /**
   * Generates the file path for a Java class file from its class name.
   *
   * @param className the class name
   * @return the file path for the Java class file
   */
  public static String getJavaFilePath(ClassNameGenerator className) {
    if (className == null) {
      return null;
    }
    return getJavaFilePath(className.getQualifiedName());
  }
}
