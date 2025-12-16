package tools.vitruv.dsls.common;

/** Generates class names. */
public interface ClassNameGenerator {
  String QNAME_SEPARATOR = ".";

  /**
   * Gets the qualified name of the class.
   *
   * @return The qualified name.
   */
  default String getQualifiedName() {
    return getPackageName() + QNAME_SEPARATOR + getSimpleName();
  }

  /**
   * Gets the simple name of the class.
   *
   * @return The simple name.
   */
  String getSimpleName();

  /**
   * Gets the package name of the class.
   *
   * @return The package name.
   */
  String getPackageName();

  /**
   * Creates a ClassNameGenerator from a qualified name.
   *
   * @param qualifiedName The qualified name.
   * @return The ClassNameGenerator.
   */
  static ClassNameGenerator fromQualifiedName(String qualifiedName) {
    int lastIndex = qualifiedName.lastIndexOf(QNAME_SEPARATOR);
    if (lastIndex == -1) {
      return new GenericClassNameGenerator("", qualifiedName);
    } else {
      return new GenericClassNameGenerator(
          qualifiedName.substring(0, lastIndex),
          qualifiedName.substring(lastIndex + QNAME_SEPARATOR.length()));
    }
  }
}
