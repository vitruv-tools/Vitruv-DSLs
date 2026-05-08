package tools.vitruv.dsls.common;

/** Generates generic class names. */
public class GenericClassNameGenerator implements ClassNameGenerator {
  private final String packageName;
  private final String className;

  /**
   * Creates a GenericClassNameGenerator.
   *
   * @param packageName the package name
   * @param className the class name
   */
  public GenericClassNameGenerator(String packageName, String className) {
    this.packageName = packageName;
    this.className = className;
  }

  @Override
  public String getSimpleName() {
    return className;
  }

  @Override
  public String getPackageName() {
    return packageName;
  }
}
