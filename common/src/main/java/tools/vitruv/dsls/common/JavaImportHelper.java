package tools.vitruv.dsls.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClassifier;

/** Helper class to manage Java imports when generating Java code. */
public class JavaImportHelper {
  public static final char FQN_SEPARATOR = '.';
  private static final Set<String> NO_IMPORT_NEEDED = Collections.singleton("java.lang");

  private final Map<String, String> imports = new HashMap<>();
  private final Map<String, String> staticImports = new HashMap<>();

  /**
   * Generates import code.
   *
   * @return the import code
   */
  public String generateImportCode() {
    StringBuilder sb = new StringBuilder();
    for (String i : imports.values()) {
      sb.append("import ").append(i).append(";\n");
    }
    for (String i : staticImports.values()) {
      sb.append("import static ").append(i).append(";\n");
    }
    return sb.toString();
  }

  /**
   * Generates a static reference.
   *
   * @param javaClass the Java class
   * @param methodName the method name
   * @return the static reference
   */
  public String staticRef(Class<?> javaClass, String methodName) {
    String result = staticImports.putIfAbsent(methodName, javaClass.getName());
    if (result == null) {
      return methodName;
    }
    return javaClass.getName() + FQN_SEPARATOR + methodName;
  }

  /**
   * Generates a type reference.
   *
   * @param nameGenerator the class name generator
   * @return the type reference
   */
  public String typeRef(ClassNameGenerator nameGenerator) {
    return typeRef(nameGenerator.getQualifiedName());
  }

  /**
   * Generates a type reference.
   *
   * @param javaClass the Java class
   * @return the type reference
   */
  public String typeRef(Class<?> javaClass) {
    return typeRef(javaClass.getName());
  }

  /**
   * Generates a type reference.
   *
   * @param classifier the EClassifier
   * @return the type reference
   */
  public String typeRef(EClassifier classifier) {
    return typeRef(classifier.getInstanceTypeName());
  }

  /**
   * Generates a type reference.
   *
   * @param fullyQualifiedJvmName the fully qualified JVM name
   * @return the type reference
   */
  public String typeRef(CharSequence fullyQualifiedJvmName) {
    if (fullyQualifiedJvmName == null) {
      return null;
    }
    String fullyQualifiedJvmNameString = fullyQualifiedJvmName.toString();
    if (isSimpleName(fullyQualifiedJvmNameString)) {
      return fullyQualifiedJvmNameString;
    }

    ClassNameGenerator className =
        ClassNameGenerator.fromQualifiedName(fullyQualifiedJvmNameString);

    if (NO_IMPORT_NEEDED.contains(className.getPackageName())) {
      return className.getSimpleName();
    }

    String simple = className.getSimpleName();
    imports.putIfAbsent(simple, className.getQualifiedName());
    if (!imports.get(simple).equals(fullyQualifiedJvmNameString)) {
      return className.getQualifiedName();
    }

    return simple;
  }

  private static boolean isSimpleName(String fqn) {
    int lastSeparatorPos = fqn.lastIndexOf(FQN_SEPARATOR);
    return (lastSeparatorPos == -1);
  }
}
